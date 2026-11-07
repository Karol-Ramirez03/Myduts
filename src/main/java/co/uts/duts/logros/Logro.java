package co.uts.duts.logros;

import java.time.LocalDateTime;

public class Logro {
    private final String codigo;
    private final String nombre;
    private final String descripcion;
    private final int dutsRequeridos;
    private final LocalDateTime fechaObtencion;

    public Logro(String codigo, String nombre, String descripcion, int dutsRequeridos,
            LocalDateTime fechaObtencion) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.dutsRequeridos = dutsRequeridos;
        this.fechaObtencion = fechaObtencion;
    }

    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public int getDutsRequeridos() { return dutsRequeridos; }
    public LocalDateTime getFechaObtencion() { return fechaObtencion; }
}
