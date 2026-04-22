-- =============================================================
-- BaseDatos.sql - Devsu Banking App
-- PostgreSQL
-- =============================================================

-- Crear base de datos (ejecutar como superusuario si es necesario)
-- CREATE DATABASE devsubank;
-- \c devsubank;

-- =============================================================
-- SCHEMA
-- =============================================================

CREATE TABLE IF NOT EXISTS personas (
    id          BIGSERIAL PRIMARY KEY,
    nombre      VARCHAR(100) NOT NULL,
    genero      VARCHAR(20),
    edad        INTEGER,
    identificacion VARCHAR(20) NOT NULL UNIQUE,
    direccion   VARCHAR(200),
    telefono    VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS clientes (
    cliente_id  BIGINT PRIMARY KEY REFERENCES personas(id) ON DELETE CASCADE,
    contrasena  VARCHAR(100) NOT NULL,
    estado      BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS cuentas (
    id              BIGSERIAL PRIMARY KEY,
    numero_cuenta   VARCHAR(20) NOT NULL UNIQUE,
    tipo_cuenta     VARCHAR(20) NOT NULL,
    saldo_inicial   NUMERIC(12, 2) NOT NULL DEFAULT 0,
    estado          BOOLEAN NOT NULL DEFAULT TRUE,
    cliente_id      BIGINT NOT NULL REFERENCES clientes(cliente_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS movimientos (
    id              BIGSERIAL PRIMARY KEY,
    fecha           DATE NOT NULL,
    tipo_movimiento VARCHAR(20) NOT NULL,
    valor           NUMERIC(12, 2) NOT NULL,
    saldo           NUMERIC(12, 2) NOT NULL,
    cuenta_id       BIGINT NOT NULL REFERENCES cuentas(id) ON DELETE CASCADE
);

-- =============================================================
-- DATOS DE PRUEBA
-- =============================================================

-- Caso 1: Creación de usuarios
INSERT INTO personas (nombre, genero, edad, identificacion, direccion, telefono)
VALUES
    ('Jose Lema',          'Masculino', 30, 'ID-001', 'Otavalo sn y principal', '098254785'),
    ('Marianela Montalvo', 'Femenino',  28, 'ID-002', 'Amazonas y NNUU',        '097548965'),
    ('Juan Osorio',        'Masculino', 35, 'ID-003', '13 junio y Equinoccial', '098874587');

INSERT INTO clientes (cliente_id, contrasena, estado)
VALUES
    (1, '1234', TRUE),
    (2, '5678', TRUE),
    (3, '1245', TRUE);

-- Caso 2: Creación de cuentas
INSERT INTO cuentas (numero_cuenta, tipo_cuenta, saldo_inicial, estado, cliente_id)
VALUES
    ('478758', 'Ahorro',    2000.00, TRUE, 1),  -- Jose Lema
    ('225487', 'Corriente',  100.00, TRUE, 2),  -- Marianela Montalvo
    ('495878', 'Ahorros',      0.00, TRUE, 3),  -- Juan Osorio
    ('496825', 'Ahorros',    540.00, TRUE, 2);  -- Marianela Montalvo

-- Caso 3: Nueva cuenta corriente para Jose Lema
INSERT INTO cuentas (numero_cuenta, tipo_cuenta, saldo_inicial, estado, cliente_id)
VALUES
    ('585545', 'Corriente', 1000.00, TRUE, 1);  -- Jose Lema

-- Caso 4: Movimientos
-- Cuenta 478758 (Jose Lema / Ahorro) - Retiro 575  → saldo: 2000 - 575 = 1425
INSERT INTO movimientos (fecha, tipo_movimiento, valor, saldo, cuenta_id)
VALUES ('2022-02-10', 'Retiro', -575.00, 1425.00, 1);

-- Cuenta 225487 (Marianela / Corriente) - Depósito 600 → saldo: 100 + 600 = 700
INSERT INTO movimientos (fecha, tipo_movimiento, valor, saldo, cuenta_id)
VALUES ('2022-02-10', 'Depósito', 600.00, 700.00, 2);

-- Cuenta 495878 (Juan Osorio / Ahorros) - Depósito 150 → saldo: 0 + 150 = 150
INSERT INTO movimientos (fecha, tipo_movimiento, valor, saldo, cuenta_id)
VALUES ('2022-02-10', 'Depósito', 150.00, 150.00, 3);

-- Cuenta 496825 (Marianela / Ahorros) - Retiro 540 → saldo: 540 - 540 = 0
INSERT INTO movimientos (fecha, tipo_movimiento, valor, saldo, cuenta_id)
VALUES ('2022-02-08', 'Retiro', -540.00, 0.00, 4);
