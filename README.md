# Food Store: Sistema de gestión de pedidos de comida (consola + JDBC)

Trabajo Práctico Integrador de **Programación 2**, Tecnicatura Universitaria en Programación a Distancia (UTN).

Aplicación de consola en **Java 21** para gestionar categorías, productos, usuarios y pedidos de un negocio de comidas. El almacenamiento es **mixto**:

| Entidad | Dónde se guarda | Cómo |
|---|---|---|
| Categoria | **Base de datos (MySQL 8)** | JDBC, `CategoriaDAO` |
| Producto | **Base de datos (MySQL 8)** | JDBC, `ProductoDAO` |
| Usuario | **Memoria** | `ArrayList` en `UsuarioRepository` |
| Pedido | **Memoria** | dentro de la lista de pedidos de cada `Usuario` |
| DetallePedido | **Memoria** | dentro de cada `Pedido` (composición) |

Los usuarios y pedidos **se pierden al cerrar el programa**. Las categorías y productos **quedan guardados**.

- **Integrantes:** _completar_
- **Video demostrativo:** _completar con el link público_
- **Documentación (PDF):** _completar con el link o indicar que está en la raíz del repositorio_

---

## 1. Requisitos

- **JDK 21** (`java -version` y `javac -version` deben mostrar 21).
- **MySQL 8** (Server, más MySQL Workbench o la consola `mysql`).
- **Driver JDBC MySQL Connector/J** (no se usa Maven ni Gradle).

## 2. Driver JDBC (carpeta `lib/`)

1. Descargar **MySQL Connector/J** desde <https://dev.mysql.com/downloads/connector/j/>. Elegir *Platform Independent* y bajar el `.zip`.
2. Copiar el archivo `mysql-connector-j-X.Y.Z.jar` que trae adentro en la carpeta **`lib/`** del proyecto.

## 3. Crear la base de datos

Los scripts están en la carpeta `db/`. Se ejecutan como `root`, en este orden:

| Script | Qué hace | ¿Obligatorio? |
|---|---|---|
| `01_crear_esquema.sql` | Crea la base `foodstore` y las tablas `categoria` y `producto` con PK, FK y CHECK | Sí |
| `02_datos_ejemplo.sql` | Carga 5 categorías y 14 productos de prueba | Opcional (ejecutar una sola vez) |
| `03_usuario_aplicacion.sql` | Crea el usuario `foodstore` / `foodstore` con permisos SELECT, INSERT y UPDATE | Recomendado |

Desde la consola:

```bash
mysql -u root -p < db/01_crear_esquema.sql
mysql -u root -p < db/02_datos_ejemplo.sql
mysql -u root -p < db/03_usuario_aplicacion.sql
```

Desde **MySQL Workbench**: *File → Open SQL Script*, abrir cada archivo y ejecutarlo con el rayo ⚡.

> Si no se ejecuta `03_usuario_aplicacion.sql`, hay que poner el usuario y la contraseña propios (por ejemplo `root`) en `src/META-INF/persistence.xml`.

## 4. Configuración de la conexión

Los datos de conexión están **solo** en `src/META-INF/persistence.xml`. El archivo usa las propiedades estándar de Jakarta Persistence, pero se lee con el parser DOM del JDK, sin JPA ni librerías externas:

```xml
<persistence-unit name="foodstorePU-mysql">
    <properties>
        <property name="jakarta.persistence.jdbc.driver"   value="com.mysql.cj.jdbc.Driver"/>
        <property name="jakarta.persistence.jdbc.url"      value="jdbc:mysql://localhost:3306/foodstore?..."/>
        <property name="jakarta.persistence.jdbc.user"     value="foodstore"/>
        <property name="jakarta.persistence.jdbc.password" value="foodstore"/>
    </properties>
</persistence-unit>
```

Para cambiar de servidor, puerto o credenciales se edita solo este archivo. La opción **5** del menú muestra la unidad de persistencia activa y la URL en uso.

## 5. Compilar y ejecutar (sin Maven ni Gradle)

**Linux / macOS** (o el script `./ejecutar.sh`):

```bash
javac -encoding UTF-8 -d bin $(find src -name "*.java")
cp -r src/META-INF bin/META-INF
java -cp "bin:lib/*" integrado.prog2.Main
```

**Windows** (o doble clic en `ejecutar.bat`):

```bat
dir /s /b src\*.java > fuentes.txt
javac -encoding UTF-8 -d bin @fuentes.txt
xcopy /e /i src\META-INF bin\META-INF
java -cp "bin;lib/*" integrado.prog2.Main
```

> En Windows el separador del classpath es `;`. En Linux y macOS es `:`.

**Desde un IDE (IntelliJ / NetBeans / Eclipse):** abrir la carpeta como proyecto Java sin build tool, marcar `src` como carpeta de fuentes, agregar el `.jar` de `lib/` como librería y ejecutar `integrado.prog2.Main`.

Al iniciar, el programa **prueba la conexión** e informa la causa probable si algo falla: falta el driver, la contraseña es incorrecta, la base no existe, el servidor está apagado o faltan las tablas. Aunque haya un error de conexión, el programa sigue funcionando con las opciones en memoria.

## 6. Estructura del proyecto

```
├── db/                         Scripts SQL (esquema, datos de ejemplo, usuario)
├── lib/                        Driver JDBC (.jar)
├── src/
│   ├── META-INF/persistence.xml
│   └── integrado/prog2/
│       ├── Main.java           Arma las dependencias, diagnóstico inicial y menú
│       ├── config/             ConexionDB (lee persistence.xml), ConfiguracionConexion
│       ├── entities/           Base, Categoria, Producto, Usuario, Pedido, DetallePedido
│       ├── enums/              Rol, Estado, FormaPago
│       ├── interfaces/         Calculable
│       ├── exception/          FoodStoreException y excepciones propias
│       ├── dao/                GenericDAO, CategoriaDAO, ProductoDAO, EsquemaDAO (único lugar con SQL)
│       ├── repository/         UsuarioRepository, PedidoRepository (colecciones en memoria)
│       ├── service/            Reglas de negocio y validaciones
│       └── ui/                 Menús de consola y lectura validada (Consola)
├── ejecutar.sh / ejecutar.bat
└── README.md
```

### Reglas de diseño aplicadas

- **Relaciones unidireccionales, como en el UML.** `Producto` no tiene un atributo `categoria` y `Pedido` no tiene un atributo `usuario`. El id de la categoría viaja como parámetro (`insert(Producto, Long idCategoria)`, `reasignarCategoria(idProducto, idCategoria)`) y solo el DAO conoce la columna `categoria_id`.
- **SQL solo en la capa `dao`.** Todas las sentencias son `PreparedStatement` parametrizadas. `ConexionDB` (en `config`) solo abre conexiones.
- **Baja lógica en los dos almacenamientos.** En la base se hace `UPDATE ... SET eliminado = TRUE` (nunca `DELETE`) y en memoria se marca `eliminado = true`.
- **Transacción de stock.** Al confirmar un pedido, el descuento de stock de todos sus productos se hace en una sola transacción (`setAutoCommit(false)` / `commit` / `rollback`). Si un producto no alcanza, no se descuenta nada y el pedido no se registra en memoria.
- **Excepciones propias.** Toda `SQLException` se transforma en `PersistenciaException` con un mensaje comprensible. El programa nunca termina con un stack trace.

## 7. Historias de usuario y opciones del menú

| Historia | Menú |
|---|---|
| HU-CAT-01 a 04 | 1) Categorías → Listar / Crear / Editar / Eliminar |
| HU-PROD-01 | 2) Productos → Listar |
| HU-PROD-02 | 2) Productos → Listar por categoría |
| HU-PROD-03 | 2) Productos → Crear |
| HU-PROD-04 | 2) Productos → Editar y Cambiar de categoría |
| HU-PROD-05 | 2) Productos → Eliminar |
| HU-USR-01 a 04 | 3) Usuarios → Listar / Crear / Editar / Eliminar |
| HU-PED-01 | 4) Pedidos → Listar todos / Listar por usuario |
| HU-PED-02 | 4) Pedidos → Crear pedido (agregar/quitar productos, resumen, confirmar) |
| HU-PED-03 | 4) Pedidos → Actualizar estado / forma de pago |
| HU-PED-04 | 4) Pedidos → Eliminar |
| HU-CFG-01 | 5) Ver configuración de conexión |
| HU-CFG-02 | Diagnóstico automático al iniciar |
| HU-CFG-03 | Scripts de la carpeta `db/` |
