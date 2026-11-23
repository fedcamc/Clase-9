package spotifyhash;

import java.util.ArrayList;
import java.util.List;

//Implementación de Tabla Hash con manejo de colisiones por Encadenamiento Separado.
//Utiliza una lista enlazada simple propia (clase interna Nodo) en cada cubeta.
public class TablaHashEncadenamiento implements TablaHash {

    private static class Nodo {
        Cancion dato;
        Nodo siguiente;

        Nodo(Cancion dato) {
            this.dato = dato;
            this.siguiente = null;
        }
    }

    private final Nodo[] tabla;
    private final int capacidad;
    private int cantidadElementos;
    private long colisiones;

    public TablaHashEncadenamiento(int itemsEsperados) {
        this.capacidad = FuncionHash.calcularTamanoOptimo(itemsEsperados);
        this.tabla = new Nodo[this.capacidad];
        this.cantidadElementos = 0;
        this.colisiones = 0;
    }

    @Override
    public boolean insertar(Cancion cancion) {
        if (cancion == null || cancion.getId().isEmpty()) {
            return false;
        }

        int indice = FuncionHash.hashPrimario(cancion.getId(), capacidad);
        Nodo actual = tabla[indice];

        // Caso 1: Cubeta vacía (sin colisión)
        if (actual == null) {
            tabla[indice] = new Nodo(cancion);
            cantidadElementos++;
            return true;
        }

        // Caso 2: Cubeta ocupada (verificar duplicados y recorrer la cadena)
        Nodo anterior = null;
        while (actual != null) {
            if (actual.dato.getId().equals(cancion.getId())) {
                return false;
            }
            anterior = actual;
            actual = actual.siguiente;
        }

        colisiones++;
        anterior.siguiente = new Nodo(cancion);
        cantidadElementos++;
        return true;
    }

    @Override
    public Cancion buscar(String id) {
        if (id == null || id.trim().isEmpty()) {
            return null;
        }
        String clave = id.trim();
        int indice = FuncionHash.hashPrimario(clave, capacidad);
        Nodo actual = tabla[indice];

        while (actual != null) {
            if (actual.dato.getId().equals(clave)) {
                return actual.dato;
            }
            actual = actual.siguiente;
        }
        return null;
    }

    @Override
    public boolean eliminar(String id) {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }
        String clave = id.trim();
        int indice = FuncionHash.hashPrimario(clave, capacidad);
        Nodo actual = tabla[indice];
        Nodo anterior = null;

        while (actual != null) {
            if (actual.dato.getId().equals(clave)) {
                if (anterior == null) {
                    tabla[indice] = actual.siguiente;
                } else {
                    anterior.siguiente = actual.siguiente;
                }
                cantidadElementos--;
                return true;
            }
            anterior = actual;
            actual = actual.siguiente;
        }
        return false;
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
            Nodo actual = tabla[i];
            if (actual == null) {
                if (!soloOcupados) {
                    filas.add(new Object[]{i, "VACÍO", "-", "-", "-", "-", "-", "-", "-"});
                }
            } else {
                int longitud = 0;
                Nodo temp = actual;
                while (temp != null) {
                    longitud++;
                    temp = temp.siguiente;
                }

                int pos = 1;
                while (actual != null) {
                    Cancion c = actual.dato;
                    String estado = (longitud == 1)
                            ? "OCUPADO"
                            : String.format("OCUPADO (cadena %d/%d)", pos, longitud);
                    filas.add(new Object[]{
                            i,
                            estado,
                            c.getId(),
                            c.getNombre(),
                            c.getTipoCategoria(),
                            c.getAtributoNumericoFormateado(),
                            c.getGenero(),
                            c.getVersionIdioma(),
                            String.valueOf(c.getVersionAño())
                    });
                    pos++;
                    actual = actual.siguiente;
                }
            }
        }
        return filas;
    }

    @Override
    public String getResumenEstructura() {
        int cubetasUsadas = 0;
        int longitudMaxima = 0;

        for (int i = 0; i < capacidad; i++) {
            if (tabla[i] != null) {
                cubetasUsadas++;
                int longitud = 0;
                Nodo actual = tabla[i];
                while (actual != null) {
                    longitud++;
                    actual = actual.siguiente;
                }
                if (longitud > longitudMaxima) {
                    longitudMaxima = longitud;
                }
            }
        }
        return String.format("Cubetas usadas: %d/%d   Longitud máxima de cadena: %d",
                cubetasUsadas, capacidad, longitudMaxima);
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
        return "Encadenamiento Separado";
    }

    @Override
    public void limpiar() {
        for (int i = 0; i < capacidad; i++) {
            tabla[i] = null;
        }
        cantidadElementos = 0;
        colisiones = 0;
    }
}
