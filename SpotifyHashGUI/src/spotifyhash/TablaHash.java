package spotifyhash;

import java.util.List;

//Interfaz común para las implementaciones de tabla hash del sistema.
// Permite intercambiar entre Encadenamiento Separado y Direccionamiento Abiertomanteniendo exactamente el mismo contrato de operaciones y métricas.
public interface TablaHash {

    //Inserta una nueva canción. Devuelve false si la clave ya existe o la tabla está llena.
    boolean insertar(Cancion cancion);

    // Busca una canción por su clave (ID). Devuelve null si no existe.
    Cancion buscar(String id);

    // Elimina una canción por su clave (ID). Devuelve true si se eliminó correctamente.
    boolean eliminar(String id);

    // Actualiza los datos de una canción existente manteniendo su clave (ID).
    boolean actualizar(String id, String nombre, String tipoCategoria,
                       double atributoNumerico, String genero,
                       String versionIdioma, int versionAnio);

    //Devuelve las filas del estado actual de la tabla para ser mostradas en la interfaz gráfica.
    // Cada arreglo tiene 9 columnas
    List<Object[]> obtenerFilasEstado(boolean soloOcupados);

    // Devuelve una descripción corta con detalles estructurales (cadenas o tombstones).
    String getResumenEstructura();

    // Devuelve el número total de elementos actualmente almacenados.
    int getCantidadElementos();

    // Devuelve la capacidad total (número de cubetas/celdas) de la tabla.
    int getCapacidad();

    // Devuelve el total de colisiones acumuladas durante las inserciones.
    long getColisiones();

    // Calcula el factor de carga actual: elementos / capacidad.
    double getFactorCarga();

    // Nombre descriptivo de la estrategia de colisiones utilizada.
    String getNombreEstrategia();

    // Vacía la tabla y reinicia los contadores de métricas.
    void limpiar();
}
