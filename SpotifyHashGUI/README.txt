
1. REQUISITOS PREVIOS
   - Java Development Kit (JDK) 8, 11, 17 o superior instalado.
   - Apache NetBeans (recomendado).

2. ESTRUCTURA DEL PROYECTO
   SpotifyHashGUI/
   ├── README.txt
   ├── spotifi_2025_aroundenglish.csv
   ├── spotify_2025_aroundtheworld.csv
   └── src/
       └── spotifyhash/
           ├── Cancion.java
           ├── FuncionHash.java
           ├── TablaHash.java
           ├── TablaHashEncadenamiento.java
           ├── TablaHashDireccionamientoAbierto.java
           ├── CargadorArchivo.java
           ├── EvaluadorRendimiento.java
           ├── PanelGraficaColisiones.java
           ├── VentanaPrincipal.java
           └── Main.java

3. EJECUCIÓN EN APACHE NETBEANS
   a) Coloque la carpeta SpotifyHashGUI en su carpeta NetBeansProjects.
   b) En Apache NetBeans vaya a: File > New Project...
   c) Elija: Java with Ant > Java Project with Existing Sources > Next.
   d) En Project Name escriba SpotifyHashGUI y en Project Folder seleccione
      la carpeta descomprimida SpotifyHashGUI > Next.
   e) En Source Package Folders haga clic en Add Folder... y seleccione la
      subcarpeta "src" dentro de SpotifyHashGUI > Finish.
   f) Abra src/spotifyhash/Main.java y presione Shift + F6 (o clic derecho > Run File).


4. USO DE LA INTERFAZ GRÁFICA
   - Pestaña "Gestión e Inventario":
     * Configure el número máximo de ítems esperados (ej. 1000 -> m primo = 1327)
       y el mecanismo de colisiones (Encadenamiento Separado o Direccionamiento Abierto).
     * Use "Cargar CSV..." para elegir uno o ambos archivos CSV desde el explorador
       (spotifi_2025_aroundenglish.csv y spotify_2025_aroundtheworld.csv suman 1,050 canciones).
     * Realice inserciones, búsquedas por ID, actualizaciones y eliminaciones desde el
       panel izquierdo, o haga clic en cualquier fila de la tabla para editarla.
     * Observe en la barra superior las métricas en tiempo real (capacidad, elementos,
       colisiones registradas y factor de carga).

   - Pestaña "Pruebas de Rendimiento":
     * Los dos archivos CSV se detectan automáticamente si están en la carpeta raíz
       (también puede elegirlos manualmente con "1. Seleccionar CSV(s)...").
     * Haga clic en "2. Ejecutar Pruebas (N = 100..1000, 3 rep.)" para generar la tabla
       de tiempos promedio y la gráfica de Colisiones vs Carga.
     * Use "Exportar Tabla CSV" y "Guardar Gráfica PNG" para guardar los resultados.
