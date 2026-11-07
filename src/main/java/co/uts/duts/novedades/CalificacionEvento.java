package co.uts.duts.novedades;

import java.time.LocalDateTime;

/** Calificación escrita por un estudiante para un evento al que asistió/estuvo inscrito. */
public class CalificacionEvento {
    private final long idEstudiante;
    private final long idNovedad;
    private final int estrellas;
    private final String comentario;
    private final LocalDateTime fecha;

    public CalificacionEvento(long idEstudiante, long idNovedad, int estrellas,
            String comentario, LocalDateTime fecha) {
        this.idEstudiante = idEstudiante;
        this.idNovedad = idNovedad;
        this.estrellas = estrellas;
        this.comentario = comentario;
        this.fecha = fecha;
    }

    public long getIdEstudiante() { return idEstudiante; }
    public long getIdNovedad() { return idNovedad; }
    public int getEstrellas() { return estrellas; }
    public String getComentario() { return comentario; }
    public LocalDateTime getFecha() { return fecha; }
}
