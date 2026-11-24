package spotifyhash;

//Utilidades matemáticas y funciones de dispersión implementadas
public final class FuncionHash {

    private static final int BASE_PRIMARIA = 31;
    private static final int BASE_SECUNDARIA = 37;

    private FuncionHash() {
        // Clase de utilidades; no instanciable
    }

    //Función hash primaria mediante dispersión polinomial (Método de Horner).
    public static int hashPrimario(String clave, int capacidad) {
        if (clave == null || clave.isEmpty() || capacidad <= 0) {
            return 0;
        }
        long hash = 0;
        for (int i = 0; i < clave.length(); i++) {
            hash = (hash * BASE_PRIMARIA + clave.charAt(i)) % capacidad;
        }
        long resultado = ((hash % capacidad) + capacidad) % capacidad;
        return (int) resultado;
    }

    //Función hash secundaria para doble dispersión.
    //Devuelve un paso en [1, capacidad - 1]. Como la capacidad es prima,
    //mcd(paso, capacidad) = 1, garantizando que el sondeo visite toda la tabla.
     
    public static int hashSecundario(String clave, int capacidad) {
        if (capacidad <= 2) {
            return 1;
        }
        int modulo = capacidad - 1;
        long hash = 0;
        if (clave != null) {
            for (int i = 0; i < clave.length(); i++) {
                hash = (hash * BASE_SECUNDARIA + clave.charAt(i)) % modulo;
            }
        }
        long resultado = ((hash % modulo) + modulo) % modulo;
        return 1 + (int) resultado;
    }

    // Calcula el tamaño óptimo de la tabla hash: el número primo más cercano
    //a 1.33 veces el número máximo esperado de ítems.
   
    public static int calcularTamanoOptimo(int itemsEsperados) {
        if (itemsEsperados <= 0) {
            itemsEsperados = 10;
        }
        int objetivo = (int) Math.round(itemsEsperados * 1.33);
        if (objetivo < 3) {
            return 3;
        }
        return encontrarPrimoMasCercano(objetivo);
    }

    //Encuentra el número primo más cercano a un entero positivo dado.
     
    public static int encontrarPrimoMasCercano(int numero) {
        if (numero <= 2) {
            return 2;
        }
        if (esPrimo(numero)) {
            return numero;
        }

        int menor = numero - 1;
        int mayor = numero + 1;

        while (true) {
            boolean mayorEsPrimo = esPrimo(mayor);
            boolean menorEsPrimo = (menor >= 2) && esPrimo(menor);

            if (mayorEsPrimo && menorEsPrimo) {
                return mayor;
            }
            if (mayorEsPrimo) {
                return mayor;
            }
            if (menorEsPrimo) {
                return menor;
            }
            menor--;
            mayor++;
        }
    }
    
    //Determina si un número entero es primo mediante división por tentativa (6k ± 1).
    public static boolean esPrimo(int n) {
        if (n <= 1) return false;
        if (n <= 3) return true;
        if (n % 2 == 0 || n % 3 == 0) return false;
        for (int i = 5; (long) i * i <= n; i += 6) {
            if (n % i == 0 || n % (i + 2) == 0) {
                return false;
            }
        }
        return true;
    }
}
