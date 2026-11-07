package co.uts.duts.novedades;

import co.uts.duts.datos.texto.EstudianteArchivoRepository;
import co.uts.duts.datos.texto.InscripcionArchivoRepository;
import co.uts.duts.datos.texto.NovedadArchivoRepository;
import co.uts.duts.usuarios.Estudiante;
import co.uts.duts.usuarios.Usuario;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Reglas de consulta, publicación e inscripción a novedades. */
public class ServicioNovedades {
    private final EstudianteArchivoRepository usuarios = new EstudianteArchivoRepository();
    private final NovedadArchivoRepository novedades = new NovedadArchivoRepository();
    private final InscripcionArchivoRepository inscripciones = new InscripcionArchivoRepository();

    public List<Novedad> consultarNovedades() throws IOException {
        return novedades.listar();
    }

    public int contarInscritos(long idNovedad) throws IOException {
        return inscripciones.contarInscritos(idNovedad);
    }

    public boolean estaInscrito(long idEstudiante, long idNovedad) throws IOException {
        return inscripciones.estaInscrito(idEstudiante, idNovedad);
    }

    public List<Novedad> misInscripciones(long idEstudiante) throws IOException {
        List<Novedad> resultado = new ArrayList<>();
        for (long id : inscripciones.listarNovedadesDe(idEstudiante)) {
            Novedad novedad = novedades.consultar(id);
            if (novedad != null) resultado.add(novedad);
        }
        return resultado;
    }

    public synchronized void inscribirse(long idEstudiante, long idNovedad) throws IOException {
        verificarEstudiante(idEstudiante);
        Novedad novedad = novedades.consultar(idNovedad);
        if (novedad == null) throw new IllegalArgumentException("La novedad ya no existe.");
        if (LocalDateTime.now().isAfter(novedad.getFechaFin())) {
            throw new IllegalArgumentException("El periodo de esta novedad ya terminó.");
        }
        if (inscripciones.estaInscrito(idEstudiante, idNovedad)) {
            throw new IllegalArgumentException("Ya tienes una inscripción activa para esta novedad.");
        }
        Integer cupo = novedad.getCupoMaximo();
        if (cupo != null && inscripciones.contarInscritos(idNovedad) >= cupo) {
            throw new IllegalArgumentException("La novedad ya completó su cupo.");
        }
        inscripciones.inscribir(idEstudiante, idNovedad);
    }

    public synchronized boolean cancelarInscripcion(long idEstudiante, long idNovedad) throws IOException {
        verificarEstudiante(idEstudiante);
        Novedad novedad = novedades.consultar(idNovedad);
        if (novedad == null) throw new IllegalArgumentException("La novedad ya no existe.");
        if (!LocalDateTime.now().isBefore(novedad.getFechaInicio())) {
            throw new IllegalArgumentException("La inscripción solo se puede cancelar antes del inicio.");
        }
        return inscripciones.cancelar(idEstudiante, idNovedad);
    }

    public Novedad crear(long idAdministrador, String titulo, String descripcion, String condiciones,
            LocalDateTime inicio, LocalDateTime fin, int duts, Integer cupo) throws IOException {
        verificarAdministrador(idAdministrador);
        return novedades.crear(titulo, descripcion, condiciones, inicio, fin, duts, cupo);
    }

    public boolean actualizar(long idAdministrador, long idNovedad, String titulo, String descripcion,
            String condiciones, LocalDateTime inicio, LocalDateTime fin, int duts, Integer cupo)
            throws IOException {
        verificarAdministrador(idAdministrador);
        int inscritos = inscripciones.contarInscritos(idNovedad);
        if (cupo != null && cupo < inscritos) {
            throw new IllegalArgumentException("El cupo no puede ser menor al número de inscritos actuales ("
                    + inscritos + ").");
        }
        return novedades.actualizar(idNovedad, titulo, descripcion, condiciones, inicio, fin, duts, cupo);
    }

    public synchronized boolean eliminar(long idAdministrador, long idNovedad) throws IOException {
        verificarAdministrador(idAdministrador);
        boolean eliminada = novedades.eliminar(idNovedad);
        if (eliminada) inscripciones.eliminarPorNovedad(idNovedad);
        return eliminada;
    }

    private void verificarEstudiante(long id) throws IOException {
        Usuario usuario = usuarios.consultar(id);
        if (!(usuario instanceof Estudiante) || !usuario.isActivo()) {
            throw new IllegalArgumentException("Solo un estudiante activo puede inscribirse.");
        }
    }

    private void verificarAdministrador(long id) throws IOException {
        Usuario usuario = usuarios.consultar(id);
        if (usuario == null || !"ADMIN".equals(usuario.getRol()) || !usuario.isActivo()) {
            throw new IllegalArgumentException("Se requiere una cuenta de administrador activa.");
        }
    }
}
