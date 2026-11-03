package co.uts.duts;

public class Estudiante {
    private String nombre;
    private String codigoEstudiantil;
    private String correo;
    private int saldoDuts;

    public Estudiante(String nombre, String codigoEstudiantil, String correo) {
        this.nombre = nombre;
        this.codigoEstudiantil = codigoEstudiantil;
        this.correo = correo;
        this.saldoDuts = 0;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCodigoEstudiantil() { return codigoEstudiantil; }
    public void setCodigoEstudiantil(String codigoEstudiantil) { this.codigoEstudiantil = codigoEstudiantil; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public int getSaldoDuts() { return saldoDuts; }
}
