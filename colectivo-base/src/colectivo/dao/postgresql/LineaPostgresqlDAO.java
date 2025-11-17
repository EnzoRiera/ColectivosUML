package colectivo.dao.postgresql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import colectivo.aplicacion.Configuracion;
import colectivo.conexion.BDConexion;
import colectivo.dao.LineaDAO;
import colectivo.dao.ParadaDAO;
import colectivo.modelo.Linea;
import colectivo.modelo.Parada;
import colectivo.util.Factory;

public class LineaPostgresqlDAO implements LineaDAO {

    private static final Logger logger = LogManager.getLogger(LineaPostgresqlDAO.class);

    private final ParadaDAO paradaDAO;
    private Map<Integer, Parada> paradas;
    private Map<String, Linea> lineas;
    private boolean actualizar;
    private final Connection con = BDConexion.getConnection();

    public LineaPostgresqlDAO() {
        this(Factory.getInstancia("PARADA", ParadaDAO.class));
    }

    public LineaPostgresqlDAO(ParadaDAO paradaDAO) {
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

    public Map<String, Linea> buscarTodos() throws Exception {
        if (actualizar) {
            lineas = new TreeMap<String, Linea>();
            try {
                lineas = leerBDDLinea();
                actualizar = false;
            } catch (Exception e) {
                logger.error("Error consultando líneas desde la base de datos", e);
                throw e;
            }
        }
        return lineas;
    }

    private Map<String, Linea> leerBDDLinea() throws Exception {
        String sql = "SELECT codigo, nombre FROM linea";
        try (PreparedStatement ps = this.con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String codigo = rs.getString("codigo");
                String nombre = rs.getString("nombre");
                Linea linea = new Linea(codigo, nombre);
                lineas.put(codigo, linea);
            }
        } catch (Exception e) {
            logger.error("Error leyendo tabla 'linea'", e);
            throw e;
        }

        try {
            leerBDDLineaParada(lineas);
        } catch (Exception e) {
            logger.error("Error leyendo tabla 'linea_parada'", e);
            throw e;
        }

        return lineas;
    }

    private Map<String, Linea> leerBDDLineaParada(Map<String, Linea> lineas) throws Exception {
        String sql = "SELECT linea, parada FROM linea_parada ORDER BY linea, secuencia";
        try (PreparedStatement ps = this.con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String codigo = rs.getString("linea");
                int codigoParada = rs.getInt("parada");

                Parada parada = paradas.get(codigoParada);
                Linea linea = lineas.get(codigo);

                if (parada != null && linea != null) {
                    linea.agregarParada(parada);
                } else if (parada == null) {
                    logger.warn("Código de parada " + codigoParada + " no encontrado para la línea " + codigo);
                } else {
                    logger.warn("Línea " + codigo + " inexistente al cargar paradas");
                }
            }
        } catch (Exception e) {
            logger.error("Error al leer 'linea_parada'", e);
            throw e;
        }

        try {
            leerBDDLineaFrecuencia(lineas);
        } catch (Exception e) {
            logger.error("Error leyendo 'linea_frecuencia'", e);
            throw e;
        }

        return lineas;
    }

    private Map<String, Linea> leerBDDLineaFrecuencia(Map<String, Linea> lineas) throws Exception {
        String sql = "SELECT linea, diasemana, hora FROM linea_frecuencia ORDER BY linea, diasemana, hora";
        try (PreparedStatement ps = this.con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String codigo = rs.getString("linea");
                int diaSemana = rs.getInt("diasemana");
                LocalTime hora = rs.getTime("hora").toLocalTime();

                Linea linea = lineas.get(codigo);
                if (linea != null) {
                    try {
                        linea.agregarFrecuencia(diaSemana, hora);
                    } catch (DateTimeParseException dtpe) {
                        logger.warn("Formato de hora inválido '" + hora + "' para la línea " + codigo, dtpe);
                    }
                } else {
                    logger.warn("Frecuencia para línea inexistente " + codigo);
                }
            }
        } catch (Exception e) {
            logger.error("Error al leer 'linea_frecuencia'", e);
            throw e;
        }

        return lineas;
    }
}
