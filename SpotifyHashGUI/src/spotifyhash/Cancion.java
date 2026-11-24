package spotifyhash;

//Representa un registro de canción dentro del inventario
// Almacena los  atributos requeridos: ID, Nombre, Tipo/Categoría, Atributo numérico, Género, Versión (idioma/país) y Versión 2 (año).
public class Cancion {
    private final String id;
    private String nombre;
    private String tipoCategoria;
    private double atributoNumerico;
    private String genero;
    private String versionIdioma;
    private int versionAño;

    public Cancion(String id, String nombre, String tipoCategoria,
                   double atributoNumerico, String genero,
                   String versionIdioma, int versionAño) {
        this.id = (id == null) ? "" : id.trim();
        this.nombre = (nombre == null) ? "" : nombre.trim();
        this.tipoCategoria = (tipoCategoria == null) ? "" : tipoCategoria.trim();
        this.atributoNumerico = atributoNumerico;
        this.genero = (genero == null) ? "" : genero.trim();
        this.versionIdioma = (versionIdioma == null) ? "" : versionIdioma.trim();
        this.versionAño = versionAño;
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = (nombre == null) ? "" : nombre.trim();
    }

    public String getTipoCategoria() {
        return tipoCategoria;
    }

    public void setTipoCategoria(String tipoCategoria) {
        this.tipoCategoria = (tipoCategoria == null) ? "" : tipoCategoria.trim();
    }

    public double getAtributoNumerico() {
        return atributoNumerico;
    }

    public void setAtributoNumerico(double atributoNumerico) {
        this.atributoNumerico = atributoNumerico;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = (genero == null) ? "" : genero.trim();
    }

    public String getVersionIdioma() {
        return versionIdioma;
    }

    public void setVersionIdioma(String versionIdioma) {
        this.versionIdioma = (versionIdioma == null) ? "" : versionIdioma.trim();
    }

    public int getVersionAño() {
        return versionAño;
    }

    public void setVersionAño(int versionAño) {
        this.versionAño = versionAño;
    }

    public String getAtributoNumericoFormateado() {
        if (atributoNumerico == (long) atributoNumerico) {
            return String.format("%d", (long) atributoNumerico);
        }
        return String.format(java.util.Locale.US, "%.2f", atributoNumerico);
    }

    @Override
    public String toString() {
        return String.format(
                "ID: %s  Nombre: %s  Tipo: %s  Num: %s  Género: %s  Idioma/País: %s  Año: %d",
                id, nombre, tipoCategoria, getAtributoNumericoFormateado(), genero, versionIdioma, versionAño
        );
    }
}
