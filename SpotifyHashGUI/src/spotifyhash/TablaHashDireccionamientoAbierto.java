package spotifyhash;

import java.util.ArrayList;
import java.util.List;

//Implementación de Tabla Hash con manejo de colisiones por Direccionamiento Abierto.
//Soporta Sondeo Lineal y Doble Dispersión, utilizando borrado lógico (tombstones)
//para preservar la integridad de las secuencias de búsqueda tras eliminaciones.
public class TablaHashDireccionamientoAbierto implements TablaHash {

    public enum TipoSondeo {
        SONDEO_LINEAL,
        DOBLE_DISPERSION
    }

    private enum EstadoCelda {
        VACIO,
        OCUPADO,
        ELIMINADO
    }

    private final Cancion[] tabla;
    private final EstadoCelda[] estados;
    private final int capacidad;
    private final TipoSondeo tipoSondeo;
    private int cantidadElementos;
    private long colisiones;

    public TablaHashDireccionamientoAbierto(int itemsEsperados, TipoSondeo tipoSondeo) {
        this.capacidad = FuncionHash.calcularTamanoOptimo(itemsEsperados);
        this.tabla = new Cancion[this.capacidad];
        this.estados = new EstadoCelda[this.capacidad];
        this.tipoSondeo = (tipoSondeo != null) ? tipoSondeo : TipoSondeo.DOBLE_DISPERSION;
        limpiar();
    }

    private int calcularPaso(String clave) {
        if (tipoSondeo == TipoSondeo.SONDEO_LINEAL) {
            return 1;
        }
        return FuncionHash.hashSecundario(clave, capacidad);
    }

    @Override
    public boolean insertar(Cancion cancion) {
        if (cancion == null || cancion.getId().isEmpty()) {
            return false;
        }
        if (cantidadElementos >= capacidad) {
            return false;
        }

        String clave = cancion.getId();
        int hashBase = FuncionHash.hashPrimario(clave, capacidad);
        int paso = calcularPaso(clave);
        int primerEliminado = -1;
        long colisionesEnInsercion = 0;

        for (int intento = 0; intento < capacidad; intento++) {
            int indice = (int) ((hashBase + (long) intento * paso) % capacidad);

            if (estados[indice] == EstadoCelda.VACIO) {
                int destino = (primerEliminado != -1) ? primerEliminado : indice;
                tabla[destino] = cancion;
                estados[destino] = EstadoCelda.OCUPADO;
                cantidadElementos++;
                colisiones += colisionesEnInsercion;
                return true;
            }

            if (estados[indice] == EstadoCelda.ELIMINADO) {
                if (primerEliminado == -1) {
                    primerEliminado = indice;
                }
            } else if (estados[indice] == EstadoCelda.OCUPADO) {
                if (tabla[indice].getId().equals(clave)) {
                    return false;
                }
                colisionesEnInsercion++;
            }
        }

        if (primerEliminado != -1) {
            tabla[primerEliminado] = cancion;
            estados[primerEliminado] = EstadoCelda.OCUPADO;
            cantidadElementos++;
            colisiones += colisionesEnInsercion;
            return true;
        }
        return false;
    }

    private int buscarIndice(String clave) {
        int hashBase = FuncionHash.hashPrimario(clave, capacidad);
        int paso = calcularPaso(clave);

        for (int intento = 0; intento < capacidad; intento++) {
            int indice = (int) ((hashBase + (long) intento * paso) % capacidad);

            if (estados[indice] == EstadoCelda.VACIO) {
                return -1;
            }
            if (estados[indice] == EstadoCelda.OCUPADO && tabla[indice].getId().equals(clave)) {
                return indice;
            }
        }
        return -1;
    }

    @Override
    public Cancion buscar(String id) {
        if (id == null || id.trim().isEmpty()) {
            return null;
        }
        int indice = buscarIndice(id.trim());
        return (indice != -1) ? tabla[indice] : null;
    }

    @Override
    public boolean eliminar(String id) {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }
        int indice = buscarIndice(id.trim());
        if (indice == -1) {
            return false;
        }
        tabla[indice] = null;
        estados[indice] = EstadoCelda.ELIMINADO;
        cantidadElementos--;
        return true;
    }

    @Override
    public boolean actualizar(String id, String nombre, String tipoCategoria,
                              double atributoNumerico, String genero,
                              String versionIdioma, int versionAño) {
        Cancion encontrada = buscar(id);
        if (encontrada == null) {
            return false;
        }
        encontrada.setNombre(nombre);
        encontrada.setTipoCategoria(tipoCategoria);
        encontrada.setAtributoNumerico(atributoNumerico);
        encontrada.setGenero(genero);
        encontrada.setVersionIdioma(versionIdioma);
        encontrada.setVersionAño(versionAño);
        return true;
    }

    @Override
    public List<Object[]> obtenerFilasEstado(boolean soloOcupados) {
        List<Object[]> filas = new ArrayList<>();

        for (int i = 0; i < capacidad; i++) {
            if (estados[i] == EstadoCelda.OCUPADO) {
                Cancion c = tabla[i];
                filas.add(new Object[]{
                        i,
                        "OCUPADO",
                        c.getId(),
                        c.getNombre(),
                        c.getTipoCategoria(),
                        c.getAtributoNumericoFormateado(),
                        c.getGenero(),
                        c.getVersionIdioma(),
                        String.valueOf(c.getVersionAño())
                });
            } else if (estados[i] == EstadoCelda.ELIMINADO) {
                if (!soloOcupados) {
                    filas.add(new Object[]{i, "ELIMINADO", "-", "-", "-", "-", "-", "-", "-"});
                }
            } else {
                if (!soloOcupados) {
                    filas.add(new Object[]{i, "VACÍO", "-", "-", "-", "-", "-", "-", "-"});
                }
            }
        }
        return filas;
    }

    @Override
    public String getResumenEstructura() {
        int eliminados = 0;
        for (int i = 0; i < capacidad; i++) {
            if (estados[i] == EstadoCelda.ELIMINADO) {
                eliminados++;
            }
        }
        int libres = capacidad - cantidadElementos - eliminados;
        return String.format("Ocupadas: %d    Eliminadas: %d    Libres: %d",
                cantidadElementos, eliminados, libres);
    }

    @Override
    public int getCantidadElementos() {
        return cantidadElementos;
    }

    @Override
    public int getCapacidad() {
        return capacidad;
    }

    @Override
    public long getColisiones() {
        return colisiones;
    }

    @Override
    public double getFactorCarga() {
        return (double) cantidadElementos / capacidad;
    }

    @Override
    public String getNombreEstrategia() {
        String subtipo = (tipoSondeo == TipoSondeo.SONDEO_LINEAL)
                ? "Sondeo Lineal"
                : "Doble Dispersión";
        return "Direccionamiento Abierto (" + subtipo + ")";
    }

    @Override
    public void limpiar() {
        for (int i = 0; i < capacidad; i++) {
            tabla[i] = null;
            estados[i] = EstadoCelda.VACIO;
        }
        cantidadElementos = 0;
        colisiones = 0;
    }
}
