-- ==========================================================
-- SCRIPT DE CREACIÓN DE BASE DE DATOS: BIBLIOTECH PRO
-- PROYECTO: GESTIÓN DE BIBLIOTECA CON ROLES INTERNOS
-- ==========================================================

DROP DATABASE IF EXISTS bibliotech_db;
CREATE DATABASE bibliotech_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE bibliotech_db;

-- 1. TABLA: USUARIOS DEL SISTEMA (Personal Administrativo)
-- Maneja el acceso al software según el rol (ADMIN/EMPLEADO)
CREATE TABLE usuarios_sistema (
    id_usuario_sis INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    rol ENUM('ADMIN', 'EMPLEADO') NOT NULL,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. TABLA: AUTORES
CREATE TABLE autores (
    id_autor INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    nacionalidad VARCHAR(50),
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. TABLA: CATEGORÍAS
CREATE TABLE categorias (
    id_categoria INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    descripcion TEXT,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. TABLA: LIBROS
-- Incluye control de stock para validación de negocio 
CREATE TABLE libros (
    id_libro INT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(200) NOT NULL,
    isbn VARCHAR(20) NOT NULL UNIQUE,
    anio_publicacion INT,
    cantidad_total INT NOT NULL DEFAULT 0,
    cantidad_disponible INT NOT NULL DEFAULT 0,
    id_autor INT,
    id_categoria INT,
    ultima_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_libro_autor FOREIGN KEY (id_autor) REFERENCES autores(id_autor),
    CONSTRAINT fk_libro_categoria FOREIGN KEY (id_categoria) REFERENCES categorias(id_categoria),
    INDEX idx_isbn (isbn),
    INDEX idx_titulo (titulo)
);

-- 5. TABLA: CLIENTES (Socios/Lectores)
-- Tabla separada de los usuarios del sistema para mayor claridad
CREATE TABLE clientes (
    id_cliente INT AUTO_INCREMENT PRIMARY KEY,
    dni VARCHAR(8) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE,
    telefono VARCHAR(20),
    direccion VARCHAR(255),
    activo BOOLEAN DEFAULT TRUE,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_dni (dni)
);

-- 6. TABLA: PRÉSTAMOS
-- Gestión de transacciones críticas
CREATE TABLE prestamos (
    id_prestamo INT AUTO_INCREMENT PRIMARY KEY,
    id_libro INT NOT NULL,
    id_cliente INT NOT NULL,
    fecha_prestamo DATE NOT NULL,
    fecha_devolucion_esperada DATE NOT NULL,
    fecha_devolucion_real DATE,
    estado ENUM('ACTIVO', 'DEVUELTO', 'VENCIDO') DEFAULT 'ACTIVO',
    observaciones TEXT,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_prestamo_libro FOREIGN KEY (id_libro) REFERENCES libros(id_libro),
    CONSTRAINT fk_prestamo_cliente FOREIGN KEY (id_cliente) REFERENCES clientes(id_cliente),
    INDEX idx_estado (estado),
    INDEX idx_fecha_devolucion (fecha_devolucion_esperada)
);

-- ==========================================================
-- INSERCIÓN DE DATOS DE PRUEBA INICIALES
-- ==========================================================

-- Usuarios del Sistema (Para el Login)
INSERT INTO usuarios_sistema (nombre, username, password, rol) VALUES 
('Admin BiblioTech', 'admin', 'admin123', 'ADMIN'),
('Empleado Soporte', 'empleado', '12345', 'EMPLEADO');

-- Autores
INSERT INTO autores (nombre, nacionalidad) VALUES 
('Robert C. Martin', 'USA'),
('Gabriel García Márquez', 'Colombia'),
('J.K. Rowling', 'Reino Unido'),
('George Orwell', 'Reino Unido'),
('Isaac Asimov', 'USA');

-- Categorías
INSERT INTO categorias (nombre, descripcion) VALUES 
('Tecnología', 'Libros sobre software y hardware'),
('Literatura', 'Novelas y cuentos clásicos'),
('Ciencia Ficción', 'Relatos futuristas y fantásticos'),
('Historia', 'Libros sobre eventos históricos'),
('Autoayuda', 'Desarrollo personal y profesional');

-- Libros
INSERT INTO libros (titulo, isbn, anio_publicacion, cantidad_total, cantidad_disponible, id_autor, id_categoria) VALUES 
('Clean Code', '9780132350884', 2008, 5, 5, 1, 1),
('Cien Años de Soledad', '9780307474728', 1967, 3, 3, 2, 2),
('Harry Potter y la Piedra Filosofal', '9788478884451', 1997, 10, 10, 3, 3),
('1984', '9780451524935', 1949, 4, 4, 4, 3),
('Fundación', '9780553293357', 1951, 3, 3, 5, 3),
('The Clean Coder', '9780137081073', 2011, 2, 2, 1, 1);

-- Clientes de prueba
INSERT INTO clientes (dni, nombre, apellido, email, telefono, activo) VALUES 
('12345678', 'Carlos', 'Mendoza', 'carlos@mail.com', '987654321', 1),
('87654321', 'Ana', 'García', 'ana@mail.com', '912345678', 1),
('11223344', 'Luis', 'Torres', 'luis@mail.com', '956781234', 0),
('55667788', 'María', 'López', 'maria@mail.com', '923456789', 1),
('99887766', 'Pedro', 'Sánchez', 'pedro@mail.com', '934567890', 1);

-- Préstamos de prueba
INSERT INTO prestamos (id_libro, id_cliente, fecha_prestamo, fecha_devolucion_esperada, estado) VALUES 
(1, 1, CURRENT_DATE, DATE_ADD(CURRENT_DATE, INTERVAL 14 DAY), 'ACTIVO'),
(2, 2, DATE_SUB(CURRENT_DATE, INTERVAL 20 DAY), DATE_SUB(CURRENT_DATE, INTERVAL 6 DAY), 'VENCIDO'),
(4, 4, DATE_SUB(CURRENT_DATE, INTERVAL 10 DAY), DATE_SUB(CURRENT_DATE, INTERVAL 3 DAY), 'DEVUELTO');

-- Actualizar stock de libros prestados
UPDATE libros SET cantidad_disponible = cantidad_disponible - 1 WHERE id_libro = 1;
UPDATE libros SET cantidad_disponible = cantidad_disponible - 1 WHERE id_libro = 2;

-- Actualizar la fecha de devolución real del préstamo devuelto
UPDATE prestamos SET fecha_devolucion_real = DATE_SUB(CURRENT_DATE, INTERVAL 4 DAY) WHERE id_prestamo = 3;
UPDATE libros SET cantidad_disponible = cantidad_disponible WHERE id_libro = 4; -- Ya devuelto, stock restaurado

-- ==========================================================
-- VERIFICACIÓN DE DATOS
-- ==========================================================
SELECT 'Usuarios del Sistema:' AS '';
SELECT * FROM usuarios_sistema;

SELECT 'Libros disponibles:' AS '';
SELECT l.titulo, l.isbn, l.cantidad_disponible, a.nombre AS autor, c.nombre AS categoria 
FROM libros l 
LEFT JOIN autores a ON l.id_autor = a.id_autor 
LEFT JOIN categorias c ON l.id_categoria = c.id_categoria;

SELECT 'Clientes registrados:' AS '';
SELECT dni, CONCAT(nombre, ' ', apellido) AS nombre_completo, activo FROM clientes;

SELECT 'Préstamos:' AS '';
SELECT p.id_prestamo, l.titulo, CONCAT(c.nombre, ' ', c.apellido) AS cliente, p.estado, p.fecha_devolucion_esperada
FROM prestamos p
JOIN libros l ON p.id_libro = l.id_libro
JOIN clientes c ON p.id_cliente = c.id_cliente;
