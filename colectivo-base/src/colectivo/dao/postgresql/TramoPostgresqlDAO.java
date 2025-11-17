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

public class TramoPostgresqlDAO implements TramoDAO {

    private static final Logger logger = LogManager.getLogger(TramoPostgresqlDAO.class);

    private final ParadaDAO paradaDAO;

    private Map<Integer, Parada> paradas;

    Map<String, Tramo> tramos = new HashMap<>();

    private boolean actualizar;

    private final Connection con = BDConexion.getConnection();

    public TramoPostgresqlDAO() {
        // Obtener ParadaDAO desde Factory (singleton compartido)
        this(Factory.getInstancia("PARADA", ParadaDAO.class));
    }

    public TramoPostgresqlDAO(ParadaDAO paradaDAO) {

        this.paradaDAO = paradaDAO;
        this.paradas = cargarParadas();

        actualizar = true;
    }

    private Map<Integer, Parada> cargarParadas() {
        Map<Integer, Parada> paradas = new TreeMap<Integer, Parada>();
        try {
            paradas = this.paradaDAO.buscarTodos();
        } catch (RuntimeException re) {
            logger.error("Error de ejecución cargando paradas", re);
            throw re;
        } catch (Exception e) {
            logger.warn("No se pudieron cargar las paradas: " + e.getMessage(), e);
            paradas = new TreeMap<>();
        }
        return paradas;
    }

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

    private Map<String, Tramo> leerBDDTramo() throws Exception {

        tramos.clear();

        String sql = "SELECT inicio, fin, tiempo, tipo FROM tramo";
        try (
            PreparedStatement ps = this.con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {
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
                        // Agregar tramo inverso para caminando
                        Tramo tramoInverso = new Tramo(paradaFin, paradaInicio, tiempo, 0);
                        // Para evitar duplicacion de carga de paradas caminando, seteamos el tipo
                        // despues
                        tramoInverso.setTipo(Constantes.CAMINANDO);
                        String claveTramoInverso = paradaCodigoFin + "-" + paradaCodigoInicio + "-" + Constantes.CAMINANDO;
                        tramos.put(claveTramoInverso, tramoInverso);
                    }
                } else {
                    logger.warn("Paradas no encontradas para tramo: inicio=" + paradaCodigoInicio + ", fin=" + paradaCodigoFin);
                }
            }
        } catch (Exception e) {
            logger.error("Error leyendo tabla 'tramo'", e);
            throw e;
        }

        return tramos;
    }
}
