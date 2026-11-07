package co.uts.duts.novedades;

/** Pregunta y respuesta publicadas en preguntas frecuentes. */
public class PreguntaFrecuente {
    private final long id;
    private final String pregunta;
    private final String respuesta;

    public PreguntaFrecuente(long id, String pregunta, String respuesta) {
        this.id = id;
        this.pregunta = pregunta;
        this.respuesta = respuesta;
    }

    public long getId() { return id; }
    public String getPregunta() { return pregunta; }
    public String getRespuesta() { return respuesta; }

    @Override
    public String toString() { return pregunta; }
}
