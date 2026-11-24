package spotifyhash;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

//Módulo encargado de leer registros de canciones desde archivos CSV o TXT.
//Soporta automáticamente:Datasets de 10 columnas (nombre_cancion, interprete, genero_musical,   duracion, año_lanzamiento, album, bailabilidad, energia, valencia, país). 
//Formato estándar (ID, Nombre, Tipo/Categoría, AtributoNumérico,   Género, VersiónIdioma, VersiónAño).

public final class CargadorArchivo {

    private static int contadorIdGlobal = 1;

    private CargadorArchivo() {
        // Clase de utilidades
    }

    public static void reiniciarContadorIds() {
        contadorIdGlobal = 1;
    }

    //Busca automáticamente los archivos CSV dados como ejemplo para la ejecución del proyecto
    public static File[] detectarDatasetsPorDefecto() {
        String[] nombres = {"spotifi_2025_aroundenglish.csv", "spotify_2025_aroundtheworld.csv"};
        List<File> encontrados = new ArrayList<>();
        File base = new File(System.getProperty("user.dir", "."));
        for (String nombre : nombres) {
            File f = new File(base, nombre);
            if (f.exists() && f.isFile()) {
                encontrados.add(f);
            }
        }
        return encontrados.isEmpty() ? null : encontrados.toArray(new File[0]);
    }

    //Lee todas las canciones válidas desde un archivo CSV o TXT

    public static List<Cancion> leerCancionesDesdeArchivo(File archivo) throws IOException {
        List<Cancion> canciones = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(archivo), StandardCharsets.UTF_8))) {

            String linea;
            while ((linea = br.readLine()) != null) {
                linea = limpiarBOM(linea).trim();
                if (linea.isEmpty()) {
                    continue;
                }

                char delimitador = detectarDelimitador(linea);
                List<String> campos = dividirLineaCSV(linea, delimitador);

                if (campos.size() >= 10) {
                    Cancion c = parsearFormato10Columnas(campos);
                    if (c != null) {
                        canciones.add(c);
                    }
                } else if (campos.size() >= 7) {
                    Cancion c = parsearFormato7Columnas(campos);
                    if (c != null) {
                        canciones.add(c);
                    }
                }
            }
        }
        return canciones;
    }

    //Carga canciones desde uno o varios archivos directamente en la TablaHash.
    public static int[] cargarArchivosEnTabla(File[] archivos, TablaHash tabla) throws IOException {
        int totalLeidos = 0;
        int insertados = 0;
        int omitidos = 0;

        for (File archivo : archivos) {
            List<Cancion> lista = leerCancionesDesdeArchivo(archivo);
            totalLeidos += lista.size();
            for (Cancion c : lista) {
                if (tabla.insertar(c)) {
                    insertados++;
                } else {
                    omitidos++;
                }
            }
        }
        return new int[]{insertados, omitidos, totalLeidos};
    }

    private static Cancion parsearFormato10Columnas(List<String> campos) {
        try {
            String nombre = campos.get(0).trim();
            String interprete = campos.get(1).trim();
            String genero = campos.get(2).trim();
            int año = Integer.parseInt(campos.get(4).trim());
            String album = campos.get(5).trim();
            double bailabilidad = Double.parseDouble(campos.get(6).trim());
            String pais = campos.get(9).trim();

            if (nombre.isEmpty()) {
                return null;
            }

            String idGenerado = String.format("SP%04d", contadorIdGlobal++);
            String tipoCategoria = interprete + "  Álbum: " + album;

            return new Cancion(idGenerado, nombre, tipoCategoria, bailabilidad, genero, pais, año);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Cancion parsearFormato7Columnas(List<String> campos) {
        try {
            String id = campos.get(0).trim();
            String nombre = campos.get(1).trim();
            String tipoCategoria = campos.get(2).trim();
            double atributoNumerico = Double.parseDouble(campos.get(3).trim());
            String genero = campos.get(4).trim();
            String versionIdioma = campos.get(5).trim();
            int versionAño = Integer.parseInt(campos.get(6).trim());

            if (id.isEmpty()) {
                return null;
            }
            return new Cancion(id, nombre, tipoCategoria, atributoNumerico, genero, versionIdioma, versionAño);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String limpiarBOM(String texto) {
        if (texto != null && !texto.isEmpty() && texto.charAt(0) == '\uFEFF') {
            return texto.substring(1);
        }
        return texto;
    }

    private static char detectarDelimitador(String linea) {
        int comas = contarFueraDeComillas(linea, ',');
        int puntosYComa = contarFueraDeComillas(linea, ';');
        int tabs = contarFueraDeComillas(linea, '\t');

        if (puntosYComa >= comas && puntosYComa >= tabs && puntosYComa >= 6) {
            return ';';
        }
        if (tabs >= comas && tabs >= puntosYComa && tabs >= 6) {
            return '\t';
        }
        return ',';
    }

    private static int contarFueraDeComillas(String linea, char objetivo) {
        boolean enComillas = false;
        int conteo = 0;
        for (int i = 0; i < linea.length(); i++) {
            char c = linea.charAt(i);
            if (c == '"') {
                enComillas = !enComillas;
            } else if (c == objetivo && !enComillas) {
                conteo++;
            }
        }
        return conteo;
    }

    private static List<String> dividirLineaCSV(String linea, char delimitador) {
        List<String> resultado = new ArrayList<>();
        StringBuilder campoActual = new StringBuilder();
        boolean enComillas = false;

        for (int i = 0; i < linea.length(); i++) {
            char c = linea.charAt(i);
            if (c == '"') {
                if (enComillas && i + 1 < linea.length() && linea.charAt(i + 1) == '"') {
                    campoActual.append('"');
                    i++;
                } else {
                    enComillas = !enComillas;
                }
            } else if (c == delimitador && !enComillas) {
                resultado.add(campoActual.toString());
                campoActual.setLength(0);
            } else {
                campoActual.append(c);
            }
        }
        resultado.add(campoActual.toString());
        return resultado;
    }
}
