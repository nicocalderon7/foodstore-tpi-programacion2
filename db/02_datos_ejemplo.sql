-- =====================================================================
-- Food Store - Datos de ejemplo (opcional, para pruebas y el video)
-- Ejecutar una sola vez, despues de 01_crear_esquema.sql:
--   mysql -u root -p foodstore < db/02_datos_ejemplo.sql
-- =====================================================================

USE foodstore;

INSERT INTO categoria (nombre, descripcion) VALUES
    ('Pizzas',       'Pizzas a la piedra y al molde'),
    ('Hamburguesas', 'Hamburguesas caseras con papas'),
    ('Empanadas',    'Empanadas al horno y fritas'),
    ('Bebidas',      'Gaseosas, aguas y cervezas'),
    ('Postres',      'Postres caseros y helados');

INSERT INTO producto (nombre, precio, descripcion, stock, imagen, disponible, categoria_id) VALUES
    ('Pizza muzzarella',       9500.00, 'Salsa de tomate, muzzarella y aceitunas', 20, 'pizza_muzza.jpg',   TRUE,  (SELECT id FROM categoria WHERE nombre = 'Pizzas')),
    ('Pizza napolitana',      11000.00, 'Muzzarella, tomate en rodajas y ajo',      15, 'pizza_napo.jpg',    TRUE,  (SELECT id FROM categoria WHERE nombre = 'Pizzas')),
    ('Pizza fugazzeta',       10500.00, 'Rellena de muzzarella y cebolla',          10, 'pizza_fugazza.jpg', TRUE,  (SELECT id FROM categoria WHERE nombre = 'Pizzas')),
    ('Hamburguesa clasica',    8500.00, 'Carne, lechuga, tomate y queso',           25, 'burger_clasica.jpg', TRUE, (SELECT id FROM categoria WHERE nombre = 'Hamburguesas')),
    ('Hamburguesa doble bacon',11500.00,'Doble carne, cheddar y panceta',           12, 'burger_bacon.jpg',  TRUE,  (SELECT id FROM categoria WHERE nombre = 'Hamburguesas')),
    ('Hamburguesa veggie',     9000.00, 'Medallon de lentejas y vegetales',          0, 'burger_veggie.jpg', TRUE,  (SELECT id FROM categoria WHERE nombre = 'Hamburguesas')),
    ('Empanada de carne',      1500.00, 'Carne cortada a cuchillo',                 60, 'emp_carne.jpg',     TRUE,  (SELECT id FROM categoria WHERE nombre = 'Empanadas')),
    ('Empanada de jamon y queso',1400.00,'Jamon cocido y muzzarella',               60, 'emp_jyq.jpg',       TRUE,  (SELECT id FROM categoria WHERE nombre = 'Empanadas')),
    ('Empanada de humita',     1400.00, 'Choclo cremoso',                           40, 'emp_humita.jpg',    TRUE,  (SELECT id FROM categoria WHERE nombre = 'Empanadas')),
    ('Gaseosa 500 ml',         2200.00, 'Linea cola, lima-limon o naranja',         50, 'gaseosa.jpg',       TRUE,  (SELECT id FROM categoria WHERE nombre = 'Bebidas')),
    ('Agua mineral 500 ml',    1800.00, 'Con o sin gas',                            50, 'agua.jpg',          TRUE,  (SELECT id FROM categoria WHERE nombre = 'Bebidas')),
    ('Cerveza artesanal 500 ml',4200.00,'IPA, Golden o Stout',                      30, 'cerveza.jpg',       FALSE, (SELECT id FROM categoria WHERE nombre = 'Bebidas')),
    ('Flan casero',            3500.00, 'Con dulce de leche o crema',               15, 'flan.jpg',          TRUE,  (SELECT id FROM categoria WHERE nombre = 'Postres')),
    ('Helado 1/4 kg',          4800.00, 'Dos gustos a eleccion',                    20, 'helado.jpg',        TRUE,  (SELECT id FROM categoria WHERE nombre = 'Postres'));
