package co.uts.duts.novedades;

import co.uts.duts.datos.texto.CalificacionEventoArchivoRepository;
import co.uts.duts.datos.texto.EstudianteArchivoRepository;
import co.uts.duts.datos.texto.InscripcionArchivoRepository;
import co.uts.duts.datos.texto.NovedadArchivoRepository;
import co.uts.duts.usuarios.Estudiante;
import co.uts.duts.usuarios.Usuario;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ServicioCalificaciones {
    private final EstudianteArchivoRepository usuarios = new EstudianteArchivoRepository();
    private final InscripcionArchivoRepository inscripciones = new InscripcionArchivoRepository();
    private final NovedadArchivoRepository novedades = new NovedadArchivoRepository();
    private final CalificacionEventoArchivoRepository calificaciones = new CalificacionEventoArchivoRepository();

    public List<Novedad> eventosCalificables(long idEstudiante) throws IOException {
        verificarEstudiante(idEstudiante);
        List<Novedad> resultado = new ArrayList<>();
        for (long idNovedad : inscripciones.listarNovedadesDe(idEstudiante)) {
            Novedad novedad = novedades.consultar(idNovedad);
            if (novedad != null && novedad.getFechaFin().isBefore(LocalDateTime.now())
                    && !calificaciones.yaCalifico(idEstudiante, idNovedad)) {
                resultado.add(novedad);
            }
        }
        return resultado;
    }

    public void calificar(long idEstudiante, long idNovedad, int estrellas, String comentario) throws IOException {
        verificarEstudiante(idEstudiante);
        if (estrellas < 1 || estrellas > 5) {
            throw new IllegalArgumentException("Selecciona una calificación de 1 a 5 estrellas.");
        }
        if (!inscripciones.estaInscrito(idEstudiante, idNovedad)) {
            throw new IllegalArgumentException("Solo puedes calificar eventos en los que estuviste inscrito.");
        }
        Novedad novedad = novedades.consultar(idNovedad);
        if (novedad == null || !novedad.getFechaFin().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Solo puedes calificar un evento que ya haya finalizado.");
        }
        if (calificaciones.yaCalifico(idEstudiante, idNovedad)) {
            throw new IllegalArgumentException("Ya calificaste este evento.");
        }
        calificaciones.guardar(new CalificacionEvento(idEstudiante, idNovedad, estrellas,
                comentario == null ? "" : comentario.trim(), LocalDateTime.now()));
    }

    public List<CalificacionEvento> listarCalificaciones(long idAdministrador, long idNovedad)
            throws IOException {
        Usuario administrador = usuarios.consultar(idAdministrador);
        if (administrador == null || !"ADMIN".equals(administrador.getRol()) || !administrador.isActivo()) {
            throw new IllegalArgumentException("Se requiere una cuenta de administrador activa.");
        }
        return calificaciones.listarPorEvento(idNovedad);
    }

    private void verificarEstudiante(long idEstudiante) throws IOException {
        Usuario usuario = usuarios.consultar(idEstudiante);
        if (!(usuario instanceof Estudiante) || !usuario.isActivo()) {
            throw new IllegalArgumentException("Solo un estudiante activo puede calificar eventos.");
        }
    }
}
