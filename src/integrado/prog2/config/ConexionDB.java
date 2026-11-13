package integrado.prog2.config;

import integrado.prog2.exception.PersistenciaException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Fabrica de conexiones JDBC.
 *
 * Lee los datos de conexion de META-INF/persistence.xml con el parser DOM del JDK
 * (sin librerias externas). Ninguna otra clase conoce la URL, el usuario ni la
 * contrasena: si se cambia de motor o de credenciales, solo se toca el XML.
 *
 * Esta clase no ejecuta sentencias SQL: solo abre conexiones y traduce los
 * errores de conexion a mensajes que indican la causa probable.
 */
public final class ConexionDB {

    private static final String RECURSO = "/META-INF/persistence.xml";
    private static final String ARCHIVO_ALTERNATIVO = "src/META-INF/persistence.xml";
    private static final String PROPIEDAD_UNIDAD = "foodstore.pu";

    private static ConfiguracionConexion configuracion;

    private ConexionDB() {
    }

    /** Devuelve la configuracion activa (la lee del XML la primera vez). */
    public static synchronized ConfiguracionConexion getConfiguracion() throws PersistenciaException {
        if (configuracion == null) {
            configuracion = leerPersistenceXml();
        }
        return configuracion;
    }

    /**
     * Abre una conexion nueva. Quien la pide es responsable de cerrarla
     * (en los DAO se usa try-with-resources).
     */
    public static Connection getConnection() throws PersistenciaException {
        ConfiguracionConexion cfg = getConfiguracion();
        try {
            Class.forName(cfg.driver());
        } catch (ClassNotFoundException e) {
            throw new PersistenciaException(
                    "No se encontro el driver JDBC '" + cfg.driver() + "'." + System.lineSeparator()
                  + "  -> Verifique que el .jar del driver (por ej. mysql-connector-j-9.x.x.jar) este en la carpeta lib/"
                  + System.lineSeparator()
                  + "  -> y que se ejecute con lib/* en el classpath: java -cp \"bin:lib/*\" integrado.prog2.Main", e);
        }
        try {
            return DriverManager.getConnection(cfg.url(), cfg.usuario(), cfg.password());
        } catch (SQLException e) {
            throw new PersistenciaException(diagnosticar(e, cfg), e);
        }
    }

    /** Traduce un error de conexion a un mensaje con la causa probable y que revisar. */
    private static String diagnosticar(SQLException e, ConfiguracionConexion cfg) {
        String nl = System.lineSeparator();
        String estado = e.getSQLState() == null ? "" : e.getSQLState();
        String mensaje = e.getMessage() == null ? "" : e.getMessage();
        int codigo = e.getErrorCode();

        if (mensaje.contains("No suitable driver")) {
            return "La URL JDBC no corresponde al driver cargado: " + cfg.url() + nl
                 + "  -> Revise la propiedad jakarta.persistence.jdbc.url del persistence.xml.";
        }
        if (codigo == 1045 || estado.startsWith("28")) {
            return "Usuario o contrasena incorrectos para la base de datos." + nl
                 + "  -> Revise jakarta.persistence.jdbc.user y .password en persistence.xml.";
        }
        if (codigo == 1049) {
            return "La base de datos indicada en la URL no existe." + nl
                 + "  -> Ejecute el script db/01_crear_esquema.sql para crearla.";
        }
        if (codigo == 1044) {
            // MySQL responde asi cuando la base no existe o el usuario no tiene permisos sobre ella
            return "El usuario '" + cfg.usuario() + "' no tiene acceso a la base indicada en la URL (o la base no existe)." + nl
                 + "  -> Ejecute db/01_crear_esquema.sql y db/03_usuario_aplicacion.sql, y revise la URL del persistence.xml.";
        }
        if (estado.startsWith("08")) {
            return "No se pudo conectar al servidor de base de datos (" + cfg.url() + ")." + nl
                 + "  -> Verifique que el servidor MySQL este iniciado y que host/puerto sean correctos.";
        }
        return "Error al conectar con la base de datos: " + mensaje;
    }

    // ------------------------------------------------------------------
    // Lectura del persistence.xml con DOM
    // ------------------------------------------------------------------

    private static ConfiguracionConexion leerPersistenceXml() throws PersistenciaException {
        try (InputStream in = abrirArchivo()) {
            DocumentBuilderFactory fabrica = DocumentBuilderFactory.newInstance();
            fabrica.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            fabrica.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder builder = fabrica.newDocumentBuilder();
            Document doc = builder.parse(in);

            Element unidad = elegirUnidad(doc.getElementsByTagName("persistence-unit"));
            Map<String, String> props = new HashMap<>();
            NodeList propiedades = unidad.getElementsByTagName("property");
            for (int i = 0; i < propiedades.getLength(); i++) {
                Element p = (Element) propiedades.item(i);
                props.put(p.getAttribute("name"), p.getAttribute("value"));
            }

            return new ConfiguracionConexion(
                    unidad.getAttribute("name"),
                    obligatoria(props, "jakarta.persistence.jdbc.driver"),
                    obligatoria(props, "jakarta.persistence.jdbc.url"),
                    obligatoria(props, "jakarta.persistence.jdbc.user"),
                    props.getOrDefault("jakarta.persistence.jdbc.password", ""));
        } catch (PersistenciaException e) {
            throw e;
        } catch (Exception e) {
            throw new PersistenciaException("No se pudo leer META-INF/persistence.xml: " + e.getMessage(), e);
        }
    }

    /** Busca el XML en el classpath (bin/META-INF) y, si no esta, en src/META-INF. */
    private static InputStream abrirArchivo() throws IOException, PersistenciaException {
        InputStream in = ConexionDB.class.getResourceAsStream(RECURSO);
        if (in != null) {
            return in;
        }
        File archivo = new File(ARCHIVO_ALTERNATIVO);
        if (archivo.exists()) {
            return new FileInputStream(archivo);
        }
        throw new PersistenciaException(
                "No se encontro META-INF/persistence.xml." + System.lineSeparator()
              + "  -> Copielo al classpath al compilar: cp -r src/META-INF bin/META-INF");
    }

    /** Unidad indicada con -Dfoodstore.pu=..., o la primera del archivo. */
    private static Element elegirUnidad(NodeList unidades) throws PersistenciaException {
        if (unidades.getLength() == 0) {
            throw new PersistenciaException("persistence.xml no contiene ninguna <persistence-unit>.");
        }
        String pedida = System.getProperty(PROPIEDAD_UNIDAD);
        if (pedida == null || pedida.isBlank()) {
            return (Element) unidades.item(0);
        }
        for (int i = 0; i < unidades.getLength(); i++) {
            Element u = (Element) unidades.item(i);
            if (pedida.equals(u.getAttribute("name"))) {
                return u;
            }
        }
        throw new PersistenciaException("No existe la unidad de persistencia '" + pedida + "' en persistence.xml.");
    }

    private static String obligatoria(Map<String, String> props, String nombre) throws PersistenciaException {
        String valor = props.get(nombre);
        if (valor == null || valor.isBlank()) {
            throw new PersistenciaException("Falta la propiedad '" + nombre + "' en persistence.xml.");
        }
        return valor;
    }
}
