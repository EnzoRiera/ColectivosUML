package colectivo.dao.postgresql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import colectivo.aplicacion.Constantes;
import colectivo.conexion.BDConexion;
import colectivo.dao.ParadaDAO;
import colectivo.dao.TramoDAO;
import colectivo.modelo.Parada;
import colectivo.modelo.Tramo;
import colectivo.util.Factory;

/**
 * Implementación de TramoDAO para PostgreSQL.
 * Esta clase gestiona la persistencia y recuperación de tramos desde una base de datos PostgreSQL.
 * Maneja tanto tramos unidireccionales (colectivo) como bidireccionales (caminando).
 */
public class TramoPostgresqlDAO implements TramoDAO {

    private static final Logger logger = LogManager.getLogger(TramoPostgresqlDAO.class);

    private final ParadaDAO paradaDAO;

    private final Map<Integer, Parada> paradas;

    Map<String, Tramo> tramos = new HashMap<>();

    private boolean actualizar;

    private final Connection con = BDConexion.getConnection();

    /**
     * Constructor por defecto.
     * Obtiene una instancia de ParadaDAO desde la Factory y carga las paradas.
     */
    public TramoPostgresqlDAO() {
        // Obtener ParadaDAO desde Factory (singleton compartido)
        this(Factory.getInstancia("PARADA", ParadaDAO.class));
    }

    /**
     * Constructor con dependencia de ParadaDAO.
     * Inicializa el DAO de tramos con un ParadaDAO específico y carga las paradas.
     *
     * @param paradaDAO el DAO de paradas a utilizar para obtener las paradas relacionadas
     */
    public TramoPostgresqlDAO(ParadaDAO paradaDAO) {

        this.paradaDAO = paradaDAO;
        this.paradas = cargarParadas();

        actualizar = true;
    }

    /**
     * Carga todas las paradas desde el ParadaDAO.
     * En caso de error al cargar las paradas, retorna un mapa vacío.
     *
     * @return un mapa con las paradas indexadas por su código
     */
    private Map<Integer, Parada> cargarParadas() {
        Map<Integer, Parada> paradas;
        try {
            paradas = this.paradaDAO.buscarTodos();
        } catch (RuntimeException re) {
            logger.error("Error de ejecución cargando paradas", re);
            throw re;
        } catch (Exception e) {
            logger.warn("No se pudieron cargar las paradas" , e);
            paradas = new TreeMap<>();
        }
        return paradas;
    }

    /**
     * Busca y retorna todos los tramos almacenados.
     * Si es necesario actualizar los datos, los carga desde la base de datos.
     * Los tramos están indexados por una clave compuesta: "codigoInicio-codigoFin-tipo".
     *
     * @return un mapa con todos los tramos indexados por su clave compuesta
     */
    public Map<String, Tramo> buscarTodos() {
        if (actualizar) {
            try {
                tramos = leerBDDTramo();
                actualizar = false;
            } catch (Exception e) {
                logger.error("Error cargando tramos desde la base de datos", e);
            }
        }
        return tramos;
    }

    /**
     * Lee todos los tramos desde la base de datos PostgreSQL.
     * Procesa tanto tramos unidireccionales (colectivo) como bidireccionales (caminando).
     * Para los tramos de tipo "caminando", crea automáticamente el tramo inverso
     * y actualiza las relaciones bidireccionales entre paradas.
     *
     * @return un mapa con todos los tramos leídos desde la base de datos
     * @throws Exception si ocurre un error al leer desde la base de datos
     */
    private Map<String, Tramo> leerBDDTramo() throws Exception {

        tramos.clear();

        String sql = "SELECT inicio, fin, tiempo, tipo FROM tramo";
        try (PreparedStatement ps = this.con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {

                int paradaCodigoInicio = rs.getInt("inicio");
                int paradaCodigoFin = rs.getInt("fin");
                int tiempo = rs.getInt("tiempo");
                int tipo = rs.getInt("tipo");

                Parada paradaInicio = paradas.get(paradaCodigoInicio);
                Parada paradaFin = paradas.get(paradaCodigoFin);

                if (paradaInicio != null && paradaFin != null) {
                    // Crear y agregar el tramo al mapa
                    // Los tramos tipo 1 son unidireccionales (colectivo)
                    Tramo tramo = new Tramo(paradaInicio, paradaFin, tiempo, tipo);
                    String claveTramo = paradaCodigoInicio + "-" + paradaCodigoFin + "-" + tipo;
                    tramos.put(claveTramo, tramo);

                    if (tipo == Constantes.CAMINANDO) {
                        // Control de duplicados: verificar si la relación bidireccional ya existe
                        if (!paradaInicio.getParadaCaminando().contains(paradaFin)) {
                            paradaInicio.agregarParadaCaminado(paradaFin);
                            logger.debug("Relación caminando agregada: parada {} -> {}", paradaCodigoInicio, paradaCodigoFin);
                        } else {
                            logger.debug("Relación caminando {} -> {} ya existe, evitando duplicado", paradaCodigoInicio, paradaCodigoFin);
                        }
                        if (!paradaFin.getParadaCaminando().contains(paradaInicio)) {
                            paradaFin.agregarParadaCaminado(paradaInicio);
                        }

                        // Agregar tramo inverso para caminando
                        Tramo tramoInverso = new Tramo(paradaFin, paradaInicio, tiempo, 0);
                        // Para evitar duplicacion de carga de paradas caminando, seteamos el tipo
                        // despues
                        tramoInverso.setTipo(Constantes.CAMINANDO);
                        String claveTramoInverso = paradaCodigoFin + "-" + paradaCodigoInicio + "-"
                                + Constantes.CAMINANDO;
                        tramos.put(claveTramoInverso, tramoInverso);
                    }
                } else {
                    logger.warn("Paradas no encontradas para tramo: inicio={}, fin={}", paradaCodigoInicio, paradaCodigoFin);
                }
            }
        } catch (Exception e) {
            logger.error("Error leyendo tabla 'tramo'", e);
            throw e;
        }

        return tramos;
    }

}