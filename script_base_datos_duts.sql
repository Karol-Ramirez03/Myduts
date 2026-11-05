-- MyDuts: Esquema Relacional Estándar (Sin InnoDB ni On Duplicate Key)
CREATE DATABASE IF NOT EXISTS myduts
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_spanish_ci;

USE myduts;

-- 1. Configuración del Sistema (Meta de graduación: 70.000 DUTS)[cite: 1]
CREATE TABLE IF NOT EXISTS configuracion_sistema (
    clave VARCHAR(80) NOT NULL PRIMARY KEY,
    valor VARCHAR(255) NOT NULL,
    descripcion VARCHAR(255) NOT NULL
);

INSERT INTO configuracion_sistema (clave, valor, descripcion)
VALUES ('META_GRADUACION_DUTS', '70000', 'DUTS que debe acumular el estudiante para cumplir la meta de graduación');

-- 2. usuario y Saldo DUTS[cite: 1]
CREATE TABLE IF NOT EXISTS usuario (
    id_usuario BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    codigo_estudiantil VARCHAR(30) NOT NULL UNIQUE,
    nombre_completo VARCHAR(120) NOT NULL,
    correo VARCHAR(150) NOT NULL UNIQUE,
    contrasena_hash VARCHAR(255) NOT NULL,
    saldo_duts BIGINT UNSIGNED NOT NULL DEFAULT 0,
    saldo_minimo_alerta BIGINT UNSIGNED NOT NULL DEFAULT 5000,
    fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

-- 3. Periodos Académicos (Enfoque a 2 semestres)
CREATE TABLE IF NOT EXISTS periodo_academico (
    id_periodo BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    anio SMALLINT UNSIGNED NOT NULL,
    semestre TINYINT UNSIGNED NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    CONSTRAINT uq_periodo_anio_semestre UNIQUE (anio, semestre),
    CONSTRAINT chk_periodo_semestre CHECK (semestre IN (1, 2))
);

-- 4. Notas Académicas (Calculadora de notas y promedios)
CREATE TABLE IF NOT EXISTS nota_academica (
    id_nota BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    id_estudiante BIGINT UNSIGNED NOT NULL,
    id_periodo BIGINT UNSIGNED NOT NULL,
    asignatura VARCHAR(120) NOT NULL,
    actividad VARCHAR(150) NOT NULL,
    nota DECIMAL(6, 2) NOT NULL,
    porcentaje DECIMAL(5, 2) NOT NULL,
    fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_nota_estudiante FOREIGN KEY (id_estudiante) REFERENCES estudiante (id_estudiante) ON DELETE CASCADE,
    CONSTRAINT fk_nota_periodo FOREIGN KEY (id_periodo) REFERENCES periodo_academico (id_periodo) ON DELETE CASCADE
);

-- 5. Novedades / Eventos Institucionales[cite: 1]
CREATE TABLE IF NOT EXISTS novedad (
    id_novedad BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(150) NOT NULL,
    descripcion TEXT NOT NULL,
    fecha_inicio DATETIME NOT NULL,
    fecha_fin DATETIME NOT NULL,
    duts_recompensa BIGINT UNSIGNED NOT NULL DEFAULT 0,
    cupo_maximo INT UNSIGNED NULL,
    estado ENUM('BORRADOR', 'PUBLICADA', 'FINALIZADA', 'CANCELADA') NOT NULL DEFAULT 'PUBLICADA'
);

-- 6. Inscripción a Novedades (Inscribirse y cancelar inscripción)[cite: 1]
CREATE TABLE IF NOT EXISTS inscripcion_novedad (
    id_inscripcion BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    id_estudiante BIGINT UNSIGNED NOT NULL,
    id_novedad BIGINT UNSIGNED NOT NULL,
    fecha_inscripcion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado ENUM('INSCRITO', 'ASISTIO', 'NO_ASISTIO', 'CANCELADO') NOT NULL DEFAULT 'INSCRITO',
    CONSTRAINT uq_inscripcion_estudiante_novedad UNIQUE (id_estudiante, id_novedad),
    CONSTRAINT fk_insc_estudiante FOREIGN KEY (id_estudiante) REFERENCES estudiante (id_estudiante) ON DELETE CASCADE,
    CONSTRAINT fk_insc_novedad FOREIGN KEY (id_novedad) REFERENCES novedad (id_novedad) ON DELETE CASCADE
);

-- 7. Movimientos DUTS (Historial de transferencias y recompensas)[cite: 1, 2]
CREATE TABLE IF NOT EXISTS movimiento_duts (
    id_movimiento BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    id_estudiante_origen BIGINT UNSIGNED NULL,
    id_estudiante_destino BIGINT UNSIGNED NULL,
    tipo ENUM('TRANSFERENCIA', 'RECOMPENSA_EVENTO', 'AJUSTE') NOT NULL,
    cantidad_duts BIGINT UNSIGNED NOT NULL,
    descripcion VARCHAR(255) NULL,
    estado ENUM('PENDIENTE', 'CONFIRMADO', 'RECHAZADO') NOT NULL DEFAULT 'CONFIRMADO',
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_mov_origen FOREIGN KEY (id_estudiante_origen) REFERENCES estudiante (id_estudiante) ON DELETE SET NULL,
    CONSTRAINT fk_mov_destino FOREIGN KEY (id_estudiante_destino) REFERENCES estudiante (id_estudiante) ON DELETE CASCADE
);

-- 8. Sistema de Logros[cite: 2]
CREATE TABLE IF NOT EXISTS logro (
    id_logro BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS estudiante_logro (
    id_estudiante BIGINT UNSIGNED NOT NULL,
    id_logro BIGINT UNSIGNED NOT NULL,
    fecha_obtencion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_estudiante, id_logro),
    CONSTRAINT fk_est_logro_est FOREIGN KEY (id_estudiante) REFERENCES estudiante (id_estudiante) ON DELETE CASCADE,
    CONSTRAINT fk_est_logro_log FOREIGN KEY (id_logro) REFERENCES logro (id_logro) ON DELETE CASCADE
);

-- 9. Preguntas Frecuentes[cite: 2]
CREATE TABLE IF NOT EXISTS pregunta_frecuente (
    id_pregunta BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    pregunta VARCHAR(255) NOT NULL,
    respuesta TEXT NOT NULL
);