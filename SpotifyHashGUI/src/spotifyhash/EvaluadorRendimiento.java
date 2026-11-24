package spotifyhash;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

//Ejecuta la batería de pruebas de rendimiento requerida para el Reporte Técnico:
//Inicializa tabla vacía esperando 1000 claves (m primo = 1327).
//Evalúa cargas de N = 100, 200, 300, 500, 800 y 1000 elementos.
//Mide inserción por lote, factor de carga, colisiones, búsqueda de 25% claves existentes, búsqueda de 25% claves inexistentes y eliminación de 25% elementos.
//Repite cada experimento 3 veces y calcula promedios.

public final class EvaluadorRendimiento {

    public static final int CAPACIDAD_ESPERADA = 1000;
    public static final int REPETICIONES = 3;
    public static final int[] TAMANOS_PRUEBA = {100, 200, 300, 500, 800, 1000};

    public static final String ESTRATEGIA_ENCADENAMIENTO = "Encadenamiento Separado";
    public static final String ESTRATEGIA_LINEAL = "Dir. Abierto (Sondeo Lineal)";
    public static final String ESTRATEGIA_DOBLE = "Dir. Abierto (Doble Dispersión)";

    public static class ResultadoFila {
        public final String estrategia;
        public final int n;
        public final int capacidad;
        public final double factorCargaPorcentaje;
        public final double colisionesPromedio;
        public final double tiempoInsercionMs;
        public final double tiempoBusqExistenteMs;
        public final double tiempoBusqInexistenteMs;
        public final double tiempoEliminacionMs;

        public ResultadoFila(String estrategia, int n, int capacidad,
                             double factorCargaPorcentaje, double colisionesPromedio,
                             double tiempoInsercionMs, double tiempoBusqExistenteMs,
                             double tiempoBusqInexistenteMs, double tiempoEliminacionMs) {
            this.estrategia = estrategia;
            this.n = n;
            this.capacidad = capacidad;
            this.factorCargaPorcentaje = factorCargaPorcentaje;
            this.colisionesPromedio = colisionesPromedio;
            this.tiempoInsercionMs = tiempoInsercionMs;
            this.tiempoBusqExistenteMs = tiempoBusqExistenteMs;
            this.tiempoBusqInexistenteMs = tiempoBusqInexistenteMs;
            this.tiempoEliminacionMs = tiempoEliminacionMs;
        }
    }

    private EvaluadorRendimiento() {
        // Clase de utilidades
    }

    public static List<ResultadoFila> ejecutarPruebasCompletas(List<Cancion> datasetExterno) {
        List<Cancion> dataset = prepararDataset(datasetExterno, 1000);
        List<ResultadoFila> resultados = new ArrayList<>();

        calentarJVM(dataset);

        resultados.addAll(ejecutarParaEstrategia(ESTRATEGIA_ENCADENAMIENTO, 1, dataset));
        resultados.addAll(ejecutarParaEstrategia(ESTRATEGIA_LINEAL, 2, dataset));
        resultados.addAll(ejecutarParaEstrategia(ESTRATEGIA_DOBLE, 3, dataset));

        return resultados;
    }

    private static void calentarJVM(List<Cancion> dataset) {
        for (int tipo = 1; tipo <= 3; tipo++) {
            TablaHash t = crearTabla(tipo);
            for (int i = 0; i < 100; i++) {
                t.insertar(dataset.get(i));
            }
            for (int i = 0; i < 25; i++) {
                t.buscar(dataset.get(i).getId());
                t.buscar("WARMUP_" + i);
                t.eliminar(dataset.get(i).getId());
            }
        }
    }

    private static List<ResultadoFila> ejecutarParaEstrategia(String nombreEstrategia, int tipoTabla,
                                                              List<Cancion> dataset) {
        List<ResultadoFila> filas = new ArrayList<>();
        int capacidadPrima = FuncionHash.calcularTamanoOptimo(CAPACIDAD_ESPERADA);

        for (int n : TAMANOS_PRUEBA) {
            int muestra25 = Math.max(1, (int) Math.round(n * 0.25));

            double sumaInsercionMs = 0;
            double sumaBusqExMs = 0;
            double sumaBusqInexMs = 0;
            double sumaEliminacionMs = 0;
            double sumaColisiones = 0;
            double factorCarga = 0;

            for (int rep = 0; rep < REPETICIONES; rep++) {
                TablaHash tabla = crearTabla(tipoTabla);

                // 1. Inserción por lote de N elementos
                long inicioIns = System.nanoTime();
                for (int i = 0; i < n; i++) {
                    tabla.insertar(dataset.get(i));
                }
                long finIns = System.nanoTime();
                sumaInsercionMs += (finIns - inicioIns) / 1_000_000.0;

                // 2. Factor de carga y colisiones
                factorCarga = tabla.getFactorCarga();
                sumaColisiones += tabla.getColisiones();

                // 3. Búsqueda de 25% claves existentes
                long inicioBusqEx = System.nanoTime();
                for (int i = 0; i < muestra25; i++) {
                    int idx = (i * 4) % n;
                    tabla.buscar(dataset.get(idx).getId());
                }
                long finBusqEx = System.nanoTime();
                sumaBusqExMs += (finBusqEx - inicioBusqEx) / 1_000_000.0;

                // 4. Búsqueda de 25% claves NO existentes
                long inicioBusqInex = System.nanoTime();
                for (int i = 0; i < muestra25; i++) {
                    tabla.buscar("NO_EXISTE_" + n + "_" + i);
                }
                long finBusqInex = System.nanoTime();
                sumaBusqInexMs += (finBusqInex - inicioBusqInex) / 1_000_000.0;

                // 5. Eliminación de 25% elementos
                long inicioElim = System.nanoTime();
                for (int i = 0; i < muestra25; i++) {
                    int idx = (i * 4) % n;
                    tabla.eliminar(dataset.get(idx).getId());
                }
                long finElim = System.nanoTime();
                sumaEliminacionMs += (finElim - inicioElim) / 1_000_000.0;
            }

            filas.add(new ResultadoFila(
                    nombreEstrategia,
                    n,
                    capacidadPrima,
                    factorCarga * 100.0,
                    sumaColisiones / REPETICIONES,
                    sumaInsercionMs / REPETICIONES,
                    sumaBusqExMs / REPETICIONES,
                    sumaBusqInexMs / REPETICIONES,
                    sumaEliminacionMs / REPETICIONES
            ));
        }
        return filas;
    }

    private static TablaHash crearTabla(int tipoTabla) {
        if (tipoTabla == 1) {
            return new TablaHashEncadenamiento(CAPACIDAD_ESPERADA);
        }
        if (tipoTabla == 2) {
            return new TablaHashDireccionamientoAbierto(
                    CAPACIDAD_ESPERADA,
                    TablaHashDireccionamientoAbierto.TipoSondeo.SONDEO_LINEAL);
        }
        return new TablaHashDireccionamientoAbierto(
                CAPACIDAD_ESPERADA,
                TablaHashDireccionamientoAbierto.TipoSondeo.DOBLE_DISPERSION);
    }

    private static List<Cancion> prepararDataset(List<Cancion> base, int minimo) {
        List<Cancion> resultado = new ArrayList<>();
        if (base != null) {
            resultado.addAll(base);
        }

        String[] tipos = {"Single", "Álbum", "EP", "En Vivo", "Acústico"};
        String[] generos = {"Rock", "Metal", "Pop", "Jazz", "Electrónica", "Indie", "Clásica"};
        String[] paises = {"México", "USA", "India", "Suecia", "Canadá", "Japón"};

        int contador = resultado.size() + 1;
        while (resultado.size() < minimo) {
            String id = String.format("SP%04d", contador);
            resultado.add(new Cancion(
                    id,
                    "Cancion_" + contador,
                    tipos[contador % tipos.length],
                    0.50 + ((contador % 50) / 100.0),
                    generos[contador % generos.length],
                    paises[contador % paises.length],
                    1985 + (contador % 42)
            ));
            contador++;
        }
        return resultado;
    }

    public static void exportarResultadosCSV(List<ResultadoFila> resultados, File archivoDestino) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(archivoDestino), StandardCharsets.UTF_8))) {
            bw.write('\uFEFF');
            bw.write("Estrategia,N,Capacidad_m,Factor_Carga_%,Colisiones_Promedio,Insercion_ms,Busqueda_Existente_ms,Busqueda_Inexistente_ms,Eliminacion_ms");
            bw.newLine();
            for (ResultadoFila r : resultados) {
                bw.write(String.format(Locale.US,
                        "\"%s\",%d,%d,%.2f,%.2f,%.4f,%.4f,%.4f,%.4f",
                        r.estrategia,
                        r.n,
                        r.capacidad,
                        r.factorCargaPorcentaje,
                        r.colisionesPromedio,
                        r.tiempoInsercionMs,
                        r.tiempoBusqExistenteMs,
                        r.tiempoBusqInexistenteMs,
                        r.tiempoEliminacionMs));
                bw.newLine();
            }
        }
    }
}
