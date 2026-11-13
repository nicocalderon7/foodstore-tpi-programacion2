-- =====================================================================
-- Food Store - Usuario de la aplicacion (opcional pero recomendado)
-- Crea el usuario que figura en src/META-INF/persistence.xml.
--
-- Solo recibe SELECT, INSERT y UPDATE: la aplicacion nunca borra
-- registros (la baja es logica con UPDATE ... SET eliminado = TRUE),
-- por eso no necesita DELETE ni permisos para modificar tablas.
--
--   mysql -u root -p < db/03_usuario_aplicacion.sql
--
-- Si prefiere usar root, no ejecute este script y cambie usuario y
-- contrasena en persistence.xml.
-- =====================================================================

CREATE USER IF NOT EXISTS 'foodstore'@'localhost' IDENTIFIED BY 'foodstore';
GRANT SELECT, INSERT, UPDATE ON foodstore.* TO 'foodstore'@'localhost';
FLUSH PRIVILEGES;
