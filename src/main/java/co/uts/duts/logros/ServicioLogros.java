package co.uts.duts.logros;

import co.uts.duts.datos.texto.EstudianteArchivoRepository;
import co.uts.duts.datos.texto.LogroArchivoRepository;
import co.uts.duts.usuarios.Estudiante;
import co.uts.duts.usuarios.Usuario;
import java.io.IOException;
import java.util.List;

/** Desbloquea y consulta logros según los DUTS acumulados. */
public class ServicioLogros {
    private final EstudianteArchivoRepository estudiantes = new EstudianteArchivoRepository();
    private final LogroArchivoRepository logros = new LogroArchivoRepository();

    public List<Logro> actualizarYListar(long idEstudiante) throws IOException {
        Usuario usuario = estudiantes.consultar(idEstudiante);
        if (!(usuario instanceof Estudiante) || !usuario.isActivo()) {
            throw new IOException("No se encontró un estudiante activo para consultar sus logros.");
        }
        return logros.actualizarYListar((Estudiante) usuario);
    }
}
