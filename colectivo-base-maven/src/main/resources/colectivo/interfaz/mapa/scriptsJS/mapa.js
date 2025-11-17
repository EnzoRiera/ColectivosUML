// Configuración Global del Mapa

const map = L.map('map').setView([-43.01, -65.17], 10);
const capaDeRuta = L.layerGroup().addTo(map);

L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 19,
    attribution: '&copy; OpenStreetMap'
}).addTo(map);

const mapLoaderOverlay = document.getElementById('map-loader-overlay');

// Exponer la función de limpieza globalmente
function limpiarRuta() {
    capaDeRuta.clearLayers();
}
window.limpiarRuta = limpiarRuta;

// Definiciones de Iconos
const shadowUrl = 'images/marker-shadow.png';

const iconOrigen = new L.Icon({
    iconUrl: 'images/marker-icon-2x.png',
    shadowUrl: shadowUrl,
    iconSize: [25, 41],
    iconAnchor: [12, 41],
    popupAnchor: [1, -34],
    shadowSize: [41, 41]
});

const iconDestino = new L.Icon({
    iconUrl: 'images/marker-icon-2x-red.png',
    shadowUrl: shadowUrl,
    iconSize: [25, 41],
    iconAnchor: [12, 41],
    popupAnchor: [1, -34],
    shadowSize: [41, 41]
});

// Estilo para paradas que no son ni origen ni destino
const estiloIntermedio = {
    radius: 5,
    fillColor: "#808080",
    color: "#000",
    weight: 1,
    opacity: 1,
    fillOpacity: 0.6
};

// Funciones Auxiliares

/**
 * Dibuja los marcadores de origen, destino y paradas intermedias para todas las opciones.
 * @param {Array} opciones - El arreglo de opciones de ruta.
 * @returns {Array} Un arreglo de todos los puntos [lat, lng] para ajustar los límites del mapa.
 */
function dibujarMarcadoresDeParadas(opciones) {
    let allBounds = [];

    opciones.forEach((opcion, opIndex) => {
        if (!opcion || opcion.length === 0) return;

        // 'flat()' aplana los segmentos en un solo arreglo de paradas
        const paradasDeEstaOpcion = opcion.flat();
        if (paradasDeEstaOpcion.length === 0) return;

        paradasDeEstaOpcion.forEach((parada, index) => {
            let textoPopup = `Opción ${opIndex + 1} - Parada ${index + 1}`;
            let marcador;

            if (index === 0) {
                // Es Origen
                textoPopup = `Opción ${opIndex + 1} - Origen`;
                marcador = L.marker(parada, { icon: iconOrigen });

            } else if (index === paradasDeEstaOpcion.length - 1) {
                // Es Destino
                textoPopup = `Opción ${opIndex + 1} - Destino`;
                marcador = L.marker(parada, { icon: iconDestino });

            } else {
                // Es Intermedia
                marcador = L.circleMarker(parada, estiloIntermedio);
            }

            marcador.addTo(capaDeRuta).bindPopup(textoPopup);
            allBounds.push(parada);
        });
    });

    return allBounds;
}

/**
 * Procesa un lote de paradas y devuelve las coordenadas de la ruta desde GraphHopper.
 * @param {Array} loteDeParadas - Un arreglo de paradas [lat, lng] para este lote.
 * @param {Object} estilo - El objeto de estilo (color, opacidad) para esta polilínea.
 * @param {string} apiKey - La clave de API de GraphHopper.
 * @returns {Promise<Object>} Una promesa que resuelve a { coords, estilo }.
 */
async function fetchLoteDeRuta(loteDeParadas, estilo, apiKey) {
    // GraphHopper espera [lon, lat], pero Leaflet usa [lat, lon]. Invertimos.
    const ghPoints = loteDeParadas.map(p => [p[1], p[0]]);
    const postBody = {
        points: ghPoints,
        vehicle: 'car',
        locale: 'es',
        points_encoded: false
    };
    const url = `https://graphhopper.com/api/1/route?key=${apiKey}`;

    const response = await fetch(url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(postBody)
    });

    if (!response.ok) {
        const err = await response.json();
        throw new Error(err.message || "Error de red al contactar GraphHopper");
    }

    const data = await response.json();
    if (!data.paths || data.paths.length === 0) {
        throw new Error(data.message || "No se encontró una ruta válida en la respuesta de GraphHopper");
    }

    // La respuesta está en [lon, lat]. Volvemos a invertir para Leaflet [lat, lon].
    const leafletCoords = data.paths[0].points.coordinates.map(c => [c[1], c[0]]);

    return { coords: leafletCoords, estilo: estilo };
}

/**
 * Prepara un arreglo de promesas (una por cada lote) para obtener las rutas.
 * @param {Array} opciones - El arreglo de opciones de ruta.
 * @param {string} apiKey - La clave de API de GraphHopper.
 * @returns {Array<Promise>} Un arreglo de promesas listas para ser ejecutadas.
 */
function obtenerPromesasDeRuta(opciones, apiKey) {
    const MAX_PUNTOS_POR_LOTE = 5;
    let promesasDeLotes = [];

    // Usamos .entries() para obtener el índice (opIndex) fácilmente
    for (const [opIndex, opcion] of opciones.entries()) {
        const estilo = {
            color: (opIndex === 0) ? '#0000FF' : '#808080', // Azul para la primera, gris para las demás
            weight: 6,
            opacity: (opIndex === 0) ? 0.8 : 0.6
        };

        // Aplanamos los segmentos en un solo arreglo de paradas
        const paradasParaRutear = opcion.flat();
        if (paradasParaRutear.length < 2) {
            continue;
        }

        // Dividir la ruta en lotes, ya que la api soporta 5 puntos por petición
        for (let i = 0; i < paradasParaRutear.length - 1; i += (MAX_PUNTOS_POR_LOTE - 1)) {
            const loteDeParadas = paradasParaRutear.slice(i, i + MAX_PUNTOS_POR_LOTE);
            if (loteDeParadas.length < 2) {
                continue;
            }

            promesasDeLotes.push(
                fetchLoteDeRuta(loteDeParadas, estilo, apiKey)
            );
        }
    }
    return promesasDeLotes;
}

/**
 * Funcion Principal dibuja las opciones de ruta de colectivo en el mapa.
 * Orquesta el dibujado de marcadores y la obtención y dibujado de rutas.
 * @param {string} opcionesDeRutaString - Un string JSON que contiene las opciones de ruta.
 */
async function dibujarRutasDeColectivo(opcionesDeRutaString) {
    try {
        mapLoaderOverlay.classList.add('visible');
        // Pequeña pausa para asegurar que el overlay se renderice
        await new Promise(resolve => setTimeout(resolve, 10));

        capaDeRuta.clearLayers();

        const GH_API_KEY = window.GH_API_KEY;
        const opciones = JSON.parse(opcionesDeRutaString);

        // --- INICIO: Lógica solicitada "sin return solos" ---
        // Aquí validamos las pre-condiciones.

        const sonEntradasValidas = GH_API_KEY && opciones && opciones.length > 0;

        if (sonEntradasValidas) {
            // 1. Dibujar todos los marcadores primero
            const allBounds = dibujarMarcadoresDeParadas(opciones);
            if (allBounds.length > 0) {
                map.fitBounds(allBounds);
            }

            // 2. Preparar todas las promesas de rutas
            const promesasDeLotes = obtenerPromesasDeRuta(opciones, GH_API_KEY);

            if (promesasDeLotes.length === 0) {
                console.log("No hay lotes de rutas para procesar.");
            } else {
                // 3. Esperar a que todos los lotes terminen (fallen o no)
                const resultados = await Promise.allSettled(promesasDeLotes);

                // 4. Dibujar las rutas que SÍ funcionaron
                resultados.forEach((resultado) => {
                    if (resultado.status === 'fulfilled') {
                        const { coords, estilo } = resultado.value;
                        L.polyline(coords, estilo).addTo(capaDeRuta);
                    } else {
                        // Si un lote falla, solo lo reportamos y continuamos
                        console.error(`Lote de ruta fallido: ${resultado.reason.message || "Error desconocido"}`);
                    }
                });
            }

        } else {
            if (!GH_API_KEY) {
                console.error("No se encontró GH_API_KEY.");
            }
            if (!opciones || opciones.length === 0) {
                console.log("No se encontraron opciones de ruta válidas.");
            }
        }

    } catch (e) {
        // Captura errores de JSON.parse() o cualquier otro error inesperado
        console.error("Error general al procesar rutas de colectivo:", e);
    } finally {
        // Esto se ejecuta siempre, haya error o no
        mapLoaderOverlay.classList.remove('visible');
        if (window.javaApp) {
            window.javaApp.notificarMapaTerminado();
        }
    }
}

// Exponer la función principal globalmente
window.dibujarRutasDeColectivo = dibujarRutasDeColectivo;