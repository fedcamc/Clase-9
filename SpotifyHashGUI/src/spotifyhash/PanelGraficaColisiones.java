package spotifyhash;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

/**
 Componente gráfico propio (Java 2D) que dibuja la gráfica comparativa
 de Colisiones vs Carga (N) para las tres estrategias de resolución de colisiones.
 Permite exportar la gráfica directamente a PNG para el reporte técnico.
 */
public class PanelGraficaColisiones extends JPanel {

    private static final Color FONDO = new Color(24, 24, 24);
    private static final Color CUADRICULA = new Color(55, 55, 55);
    private static final Color EJE = new Color(170, 170, 170);
    private static final Color TEXTO = new Color(235, 235, 235);
    private static final Color TEXTO_SECUNDARIO = new Color(165, 165, 165);

    private static final Color COLOR_ENCADENAMIENTO = new Color(29, 185, 84);
    private static final Color COLOR_LINEAL = new Color(255, 107, 107);
    private static final Color COLOR_DOBLE = new Color(76, 201, 240);

    private List<EvaluadorRendimiento.ResultadoFila> resultados;

    public PanelGraficaColisiones() {
        this.resultados = new ArrayList<>();
        setBackground(FONDO);
        setPreferredSize(new Dimension(760, 290));
    }

    public void setResultados(List<EvaluadorRendimiento.ResultadoFila> resultados) {
        this.resultados = (resultados != null) ? resultados : new ArrayList<>();
        repaint();
    }

    public boolean tieneDatos() {
        return resultados != null && !resultados.isEmpty();
    }

    public void exportarComoPNG(File archivoDestino) throws IOException {
        int ancho = Math.max(getWidth(), 960);
        int alto = Math.max(getHeight(), 440);
        BufferedImage imagen = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = imagen.createGraphics();
        dibujarGrafica(g2, ancho, alto);
        g2.dispose();
        ImageIO.write(imagen, "png", archivoDestino);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        dibujarGrafica(g2, getWidth(), getHeight());
        g2.dispose();
    }

    private void dibujarGrafica(Graphics2D g2, int w, int h) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2.setColor(FONDO);
        g2.fillRect(0, 0, w, h);

        if (resultados == null || resultados.isEmpty()) {
            g2.setColor(TEXTO_SECUNDARIO);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 14));
            String msg = "Ejecute las pruebas de rendimiento para generar la gráfica Colisiones vs Carga.";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(msg, (w - fm.stringWidth(msg)) / 2, h / 2);
            return;
        }

        int margenIzq = 84;
        int margenDer = 36;
        int margenSup = 60;
        int margenInf = 52;

        int anchoGraf = Math.max(100, w - margenIzq - margenDer);
        int altoGraf = Math.max(80, h - margenSup - margenInf);

        g2.setColor(TEXTO);
        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2.drawString("Colisiones Promedio vs Carga    Capacidad prima m = 1327", margenIzq, 24);

        dibujarLeyenda(g2, margenIzq, 44);

        List<EvaluadorRendimiento.ResultadoFila> serieEnc = filtrarSerie(EvaluadorRendimiento.ESTRATEGIA_ENCADENAMIENTO);
        List<EvaluadorRendimiento.ResultadoFila> serieLin = filtrarSerie(EvaluadorRendimiento.ESTRATEGIA_LINEAL);
        List<EvaluadorRendimiento.ResultadoFila> serieDob = filtrarSerie(EvaluadorRendimiento.ESTRATEGIA_DOBLE);

        double maxColisiones = 10.0;
        for (EvaluadorRendimiento.ResultadoFila r : resultados) {
            if (r.colisionesPromedio > maxColisiones) {
                maxColisiones = r.colisionesPromedio;
            }
        }
        double yMax = Math.ceil((maxColisiones * 1.15) / 50.0) * 50.0;
        if (yMax < 50.0) {
            yMax = Math.ceil(maxColisiones * 1.2);
        }

        // Cuadrícula y eje Y
        int divisionesY = 5;
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        for (int i = 0; i <= divisionesY; i++) {
            int y = margenSup + altoGraf - (int) ((double) i / divisionesY * altoGraf);
            double valorY = (yMax * i) / divisionesY;

            g2.setColor(CUADRICULA);
            g2.setStroke(new BasicStroke(1f));
            g2.drawLine(margenIzq, y, margenIzq + anchoGraf, y);

            g2.setColor(TEXTO_SECUNDARIO);
            String etiquetaY = String.format("%.0f", valorY);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(etiquetaY, margenIzq - fm.stringWidth(etiquetaY) - 8, y + 4);
        }

        // Ejes X e Y
        g2.setColor(EJE);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(margenIzq, margenSup, margenIzq, margenSup + altoGraf);
        g2.drawLine(margenIzq, margenSup + altoGraf, margenIzq + anchoGraf, margenSup + altoGraf);

        // Eje X
        int[] tamanos = EvaluadorRendimiento.TAMANOS_PRUEBA;
        for (int i = 0; i < tamanos.length; i++) {
            int x = calcularX(tamanos[i], margenIzq, anchoGraf);
            g2.setColor(CUADRICULA);
            g2.drawLine(x, margenSup, x, margenSup + altoGraf);

            g2.setColor(TEXTO_SECUNDARIO);
            String etX = String.valueOf(tamanos[i]);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(etX, x - fm.stringWidth(etX) / 2, margenSup + altoGraf + 18);
        }

        // Etiqueta eje X
        g2.setColor(TEXTO);
        g2.setFont(new Font("SansSerif", Font.BOLD, 11));
        String tituloX = "Número de elementos insertados (N)";
        FontMetrics fmX = g2.getFontMetrics();
        g2.drawString(tituloX, margenIzq + (anchoGraf - fmX.stringWidth(tituloX)) / 2, h - 10);

        // Etiqueta rotada eje Y
        AffineTransform origTransform = g2.getTransform();
        g2.rotate(-Math.PI / 2.0);
        String tituloY = "Colisiones promedio";
        int centroY = margenSup + altoGraf / 2;
        g2.drawString(tituloY, -(centroY + g2.getFontMetrics().stringWidth(tituloY) / 2), 20);
        g2.setTransform(origTransform);

        // Dibujar series
        dibujarSerie(g2, serieLin, COLOR_LINEAL, margenIzq, margenSup, anchoGraf, altoGraf, yMax, -10);
        dibujarSerie(g2, serieDob, COLOR_DOBLE, margenIzq, margenSup, anchoGraf, altoGraf, yMax, -8);
        dibujarSerie(g2, serieEnc, COLOR_ENCADENAMIENTO, margenIzq, margenSup, anchoGraf, altoGraf, yMax, 16);
    }

    private void dibujarLeyenda(Graphics2D g2, int xInicio, int y) {
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        int x = xInicio;
        x = dibujarItemLeyenda(g2, x, y, COLOR_ENCADENAMIENTO, EvaluadorRendimiento.ESTRATEGIA_ENCADENAMIENTO);
        x = dibujarItemLeyenda(g2, x + 18, y, COLOR_LINEAL, EvaluadorRendimiento.ESTRATEGIA_LINEAL);
        dibujarItemLeyenda(g2, x + 18, y, COLOR_DOBLE, EvaluadorRendimiento.ESTRATEGIA_DOBLE);
    }

    private int dibujarItemLeyenda(Graphics2D g2, int x, int y, Color color, String texto) {
        g2.setColor(color);
        g2.fillRect(x, y - 8, 12, 8);
        g2.setColor(TEXTO);
        g2.drawString(texto, x + 17, y);
        return x + 17 + g2.getFontMetrics().stringWidth(texto);
    }

    private void dibujarSerie(Graphics2D g2, List<EvaluadorRendimiento.ResultadoFila> serie,
                              Color color, int margenIzq, int margenSup, int anchoGraf,
                              int altoGraf, double yMax, int offsetEtiquetaY) {
        if (serie.isEmpty()) {
            return;
        }
        g2.setColor(color);
        g2.setStroke(new BasicStroke(2.4f));

        int prevX = -1;
        int prevY = -1;

        for (EvaluadorRendimiento.ResultadoFila r : serie) {
            int x = calcularX(r.n, margenIzq, anchoGraf);
            int y = margenSup + altoGraf - (int) ((r.colisionesPromedio / yMax) * altoGraf);

            if (prevX != -1) {
                g2.drawLine(prevX, prevY, x, y);
            }
            prevX = x;
            prevY = y;
        }

        g2.setFont(new Font("SansSerif", Font.BOLD, 10));
        for (EvaluadorRendimiento.ResultadoFila r : serie) {
            int x = calcularX(r.n, margenIzq, anchoGraf);
            int y = margenSup + altoGraf - (int) ((r.colisionesPromedio / yMax) * altoGraf);

            g2.setColor(color);
            g2.fillOval(x - 4, y - 4, 8, 8);

            String valor = String.format("%.0f", r.colisionesPromedio);
            int yTexto = Math.max(margenSup + 12, Math.min(margenSup + altoGraf - 4, y + offsetEtiquetaY));
            g2.drawString(valor, x - 10, yTexto);
        }
    }

    private int calcularX(int n, int margenIzq, int anchoGraf) {
        double proporcion = (n - 100.0) / (1000.0 - 100.0);
        return margenIzq + 18 + (int) (proporcion * (anchoGraf - 36));
    }

    private List<EvaluadorRendimiento.ResultadoFila> filtrarSerie(String estrategia) {
        List<EvaluadorRendimiento.ResultadoFila> lista = new ArrayList<>();
        for (EvaluadorRendimiento.ResultadoFila r : resultados) {
            if (r.estrategia.equals(estrategia)) {
                lista.add(r);
            }
        }
        return lista;
    }
}
