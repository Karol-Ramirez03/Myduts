package co.uts.duts.novedades;

import java.time.LocalDateTime;

/** Información de una actividad publicada en MyDUTS. */
public class Novedad {
    private final long id;
    private final String titulo;
    private final String descripcion;
    private final String condiciones;
    private final LocalDateTime fechaInicio;
    private final LocalDateTime fechaFin;
    private final int dutsRecompensa;
    private final Integer cupoMaximo;

    public Novedad(long id, String titulo, String descripcion, String condiciones,
            LocalDateTime fechaInicio, LocalDateTime fechaFin, int dutsRecompensa, Integer cupoMaximo) {
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.condiciones = condiciones;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.dutsRecompensa = dutsRecompensa;
        this.cupoMaximo = cupoMaximo;
    }

    public long getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getDescripcion() { return descripcion; }
    public String getCondiciones() { return condiciones; }
    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public LocalDateTime getFechaFin() { return fechaFin; }
    public int getDutsRecompensa() { return dutsRecompensa; }
    public Integer getCupoMaximo() { return cupoMaximo; }

    @Override
    public String toString() {
        return titulo + " — " + dutsRecompensa + " DUTS";
    }
}
