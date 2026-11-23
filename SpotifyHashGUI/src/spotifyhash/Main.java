package spotifyhash;

import java.awt.Color;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 Benemérita Universidad Autónoma de Puebla 
 Facultad de Ciencias de la Computación / Ingeniería en Ciencia de Datos
 Estructuras de Datos y sus Aplicaciones
 Federico Camacho Cagal, Dana Dominguez Arce / 202500554, 202519851
 */
public class Main {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            UIManager.put("TabbedPane.selected", new Color(29, 185, 84));
            UIManager.put("TabbedPane.background", new Color(35, 35, 35));
            UIManager.put("TabbedPane.foreground", new Color(245, 245, 245));
            UIManager.put("TabbedPane.contentAreaColor", new Color(18, 18, 18));
            UIManager.put("TabbedPane.focus", new Color(29, 185, 84));
            UIManager.put("TabbedPane.darkShadow", new Color(18, 18, 18));
            UIManager.put("TabbedPane.shadow", new Color(40, 40, 40));
            UIManager.put("TabbedPane.light", new Color(55, 55, 55));
            UIManager.put("TabbedPane.highlight", new Color(55, 55, 55));
        } catch (Exception ignored) {
            // Continúa con el Look & Feel por defecto
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                VentanaPrincipal ventana = new VentanaPrincipal();
                ventana.setVisible(true);
            }
        });
    }
}
