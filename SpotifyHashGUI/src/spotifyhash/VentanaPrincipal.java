package spotifyhash;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class VentanaPrincipal extends JFrame {

    private static final Color BG_PRINCIPAL = new Color(18, 18, 18);
    private static final Color BG_TARJETA = new Color(28, 28, 28);
    private static final Color BG_CAMPO = new Color(42, 42, 42);
    private static final Color BORDE = new Color(58, 58, 58);
    private static final Color VERDE_SPOTIFY = new Color(29, 185, 84);
    private static final Color AZUL_ACCION = new Color(59, 130, 246);
    private static final Color AMBAR_ACCION = new Color(217, 119, 6);
    private static final Color ROJO_ACCION = new Color(220, 38, 38);
    private static final Color GRIS_BOTON = new Color(75, 75, 75);
    private static final Color TEXTO_CLARO = new Color(245, 245, 245);
    private static final Color TEXTO_TENUE = new Color(179, 179, 179);

    private TablaHash tablaHash;

    // Métricas en tiempo real
    private JLabel lblMetricaEstrategia;
    private JLabel lblMetricaCapacidad;
    private JLabel lblMetricaElementos;
    private JLabel lblMetricaColisiones;
    private JLabel lblMetricaFactor;
    private JProgressBar barraFactorCarga;

    // Configuración de tabla
    private JTextField txtItemsEsperados;
    private JLabel lblPrimoCalculado;
    private JComboBox<String> cmbEstrategia;

    // Formulario CRUD 
    private JTextField txtId;
    private JTextField txtNombre;
    private JTextField txtTipo;
    private JTextField txtAtributoNum;
    private JTextField txtGenero;
    private JTextField txtIdioma;
    private JTextField txtAnio;

    // Visor de tabla hash
    private DefaultTableModel modeloTablaHash;
    private JTable jTableHash;
    private JCheckBox chkSoloOcupados;
    private JLabel lblResumenEstructura;

    // Pestaña de Pruebas de Rendimiento
    private File[] archivosBenchmark;
    private JLabel lblArchivosBenchmark;
    private JButton btnEjecutarBenchmark;
    private JButton btnExportarCSV;
    private JButton btnGuardarPNG;
    private DefaultTableModel modeloBenchmark;
    private PanelGraficaColisiones panelGrafica;
    private List<EvaluadorRendimiento.ResultadoFila> ultimosResultados;

    // Barra de estado inferior
    private JLabel lblEstado;

    public VentanaPrincipal() {
        super("SPOTIFY: Inventario de Canciones");
        this.tablaHash = new TablaHashEncadenamiento(1000);
        this.ultimosResultados = new ArrayList<>();
        this.archivosBenchmark = CargadorArchivo.detectarDatasetsPorDefecto();

        configurarVentana();
        construirInterfaz();
        actualizarVistaCompleta();
        establecerEstado("Tabla inicializada con Encadenamiento Separado (capacidad prima m = " + tablaHash.getCapacidad() + ").");
    }

    private void configurarVentana() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 820);
        setMinimumSize(new Dimension(1080, 700));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_PRINCIPAL);
        setLayout(new BorderLayout(0, 8));
    }

    private void construirInterfaz() {
        JPanel panelSuperior = new JPanel();
        panelSuperior.setLayout(new BoxLayout(panelSuperior, BoxLayout.Y_AXIS));
        panelSuperior.setBackground(BG_PRINCIPAL);
        panelSuperior.add(crearPanelEncabezado());
        panelSuperior.add(crearPanelMetricas());
        add(panelSuperior, BorderLayout.NORTH);

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.setBackground(new Color(35, 35, 35));
        pestanas.setForeground(TEXTO_CLARO);
        pestanas.setFont(new Font("SansSerif", Font.BOLD, 13));
        pestanas.addTab("  Gestión e Inventario", crearPestanaGestion());
        pestanas.addTab("  Pruebas de Rendimiento", crearPestanaRendimiento());
        add(pestanas, BorderLayout.CENTER);

        JPanel panelEstado = new JPanel(new BorderLayout());
        panelEstado.setBackground(new Color(22, 22, 22));
        panelEstado.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDE),
                new EmptyBorder(6, 14, 6, 14)
        ));
        lblEstado = new JLabel("Listo.");
        lblEstado.setForeground(TEXTO_TENUE);
        lblEstado.setFont(new Font("SansSerif", Font.PLAIN, 12));
        panelEstado.add(lblEstado, BorderLayout.WEST);
        add(panelEstado, BorderLayout.SOUTH);
    }

    private JPanel crearPanelEncabezado() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(20, 20, 20));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, VERDE_SPOTIFY),
                new EmptyBorder(10, 16, 10, 16)
        ));

        JPanel izq = new JPanel();
        izq.setLayout(new BoxLayout(izq, BoxLayout.Y_AXIS));
        izq.setBackground(new Color(20, 20, 20));

        JLabel lblTitulo = new JLabel("SPOTIFY: Inventario de Canciones — Tabla Hash desde Cero");
        lblTitulo.setForeground(VERDE_SPOTIFY);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 18));

        JLabel lblSubtitulo = new JLabel("BUAP  Facultad de Ciencias de la Computación  Estructuras de Datos y sus Aplicaciones");
        lblSubtitulo.setForeground(TEXTO_TENUE);
        lblSubtitulo.setFont(new Font("SansSerif", Font.PLAIN, 12));

        izq.add(lblTitulo);
        izq.add(lblSubtitulo);

        JLabel lblAutores = new JLabel("<html><div style='text-align:right;'>"
                + "<b>Examen Departamental</b><br>"
                + "Camacho Cagal Federico &amp; Dana Dominguez Arce</div></html>");
        lblAutores.setForeground(TEXTO_TENUE);
        lblAutores.setFont(new Font("SansSerif", Font.PLAIN, 11));

        panel.add(izq, BorderLayout.WEST);
        panel.add(lblAutores, BorderLayout.EAST);
        return panel;
    }

    private JPanel crearPanelMetricas() {
        JPanel contenedor = new JPanel(new GridLayout(1, 5, 10, 0));
        contenedor.setBackground(BG_PRINCIPAL);
        contenedor.setBorder(new EmptyBorder(10, 14, 4, 14));

        lblMetricaEstrategia = new JLabel("-");
        lblMetricaCapacidad = new JLabel("-");
        lblMetricaElementos = new JLabel("-");
        lblMetricaColisiones = new JLabel("-");
        lblMetricaFactor = new JLabel("-");

        contenedor.add(crearTarjetaMetrica("ESTRATEGIA ACTIVA", lblMetricaEstrategia, VERDE_SPOTIFY, 13));
        contenedor.add(crearTarjetaMetrica("CAPACIDAD PRIMA (m)", lblMetricaCapacidad, TEXTO_CLARO, 17));
        contenedor.add(crearTarjetaMetrica("ELEMENTOS (n)", lblMetricaElementos, TEXTO_CLARO, 17));
        contenedor.add(crearTarjetaMetrica("COLISIONES REGISTRADAS", lblMetricaColisiones, new Color(255, 135, 135), 17));

        JPanel tarjetaAlfa = new JPanel(new BorderLayout(0, 4));
        tarjetaAlfa.setBackground(BG_TARJETA);
        tarjetaAlfa.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDE, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        JLabel titAlfa = new JLabel("FACTOR DE CARGA (α)");
        titAlfa.setForeground(TEXTO_TENUE);
        titAlfa.setFont(new Font("SansSerif", Font.BOLD, 10));

        lblMetricaFactor.setForeground(new Color(76, 201, 240));
        lblMetricaFactor.setFont(new Font("SansSerif", Font.BOLD, 15));

        barraFactorCarga = new JProgressBar(0, 100);
        barraFactorCarga.setBackground(BG_CAMPO);
        barraFactorCarga.setForeground(VERDE_SPOTIFY);
        barraFactorCarga.setBorderPainted(false);
        barraFactorCarga.setPreferredSize(new Dimension(100, 8));

        tarjetaAlfa.add(titAlfa, BorderLayout.NORTH);
        tarjetaAlfa.add(lblMetricaFactor, BorderLayout.CENTER);
        tarjetaAlfa.add(barraFactorCarga, BorderLayout.SOUTH);
        contenedor.add(tarjetaAlfa);

        return contenedor;
    }

    private JPanel crearTarjetaMetrica(String titulo, JLabel valorLabel, Color colorValor, int tamanoFuente) {
        JPanel tarjeta = new JPanel(new BorderLayout(0, 4));
        tarjeta.setBackground(BG_TARJETA);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDE, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));

        JLabel lblTit = new JLabel(titulo);
        lblTit.setForeground(TEXTO_TENUE);
        lblTit.setFont(new Font("SansSerif", Font.BOLD, 10));

        valorLabel.setForeground(colorValor);
        valorLabel.setFont(new Font("SansSerif", Font.BOLD, tamanoFuente));

        tarjeta.add(lblTit, BorderLayout.NORTH);
        tarjeta.add(valorLabel, BorderLayout.CENTER);
        return tarjeta;
    }

    private JPanel crearPestanaGestion() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BG_PRINCIPAL);
        panel.setBorder(new EmptyBorder(10, 14, 10, 14));

        panel.add(crearBarraConfiguracion(), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                crearPanelFormularioCRUD(),
                crearPanelVisorTabla());
        split.setDividerLocation(375);
        split.setResizeWeight(0.0);
        split.setBackground(BG_PRINCIPAL);
        split.setBorder(null);

        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearBarraConfiguracion() {
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        barra.setBackground(BG_TARJETA);
        barra.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDE, 1, true),
                new EmptyBorder(2, 8, 2, 8)
        ));

        JLabel lblEsp = crearEtiqueta("Ítems esperados:");
        txtItemsEsperados = crearCampoTexto("1000", 6);
        lblPrimoCalculado = new JLabel("Primo óptimo (m): 1327");
        lblPrimoCalculado.setForeground(VERDE_SPOTIFY);
        lblPrimoCalculado.setFont(new Font("SansSerif", Font.BOLD, 12));

        txtItemsEsperados.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { actualizarPreviewPrimo(); }
            @Override public void removeUpdate(DocumentEvent e) { actualizarPreviewPrimo(); }
            @Override public void changedUpdate(DocumentEvent e) { actualizarPreviewPrimo(); }
        });

        JLabel lblEst = crearEtiqueta("Colisiones:");
        cmbEstrategia = new JComboBox<>(new String[]{
                "Encadenamiento Separado",
                "Direccionamiento Abierto - Sondeo Lineal",
                "Direccionamiento Abierto - Doble Dispersión"
        });
        cmbEstrategia.setBackground(BG_CAMPO);
        cmbEstrategia.setForeground(TEXTO_CLARO);
        cmbEstrategia.setFont(new Font("SansSerif", Font.PLAIN, 12));
        cmbEstrategia.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                c.setBackground(isSelected ? VERDE_SPOTIFY : BG_CAMPO);
                c.setForeground(TEXTO_CLARO);
                return c;
            }
        });

        JButton btnAplicar = crearBoton("Aplicar / Reiniciar Tabla", AZUL_ACCION);
        btnAplicar.addActionListener(e -> aplicarConfiguracionTabla());

        JButton btnCargarCSV = crearBoton("Cargar CSV...", VERDE_SPOTIFY);
        btnCargarCSV.addActionListener(e -> cargarCSVDesdeSelector());

        barra.add(lblEsp);
        barra.add(txtItemsEsperados);
        barra.add(lblPrimoCalculado);
        barra.add(lblEst);
        barra.add(cmbEstrategia);
        barra.add(btnAplicar);
        barra.add(btnCargarCSV);

        return barra;
    }

    private JPanel crearPanelFormularioCRUD() {
        JPanel tarjeta = new JPanel(new BorderLayout(0, 10));
        tarjeta.setBackground(BG_TARJETA);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDE, 1, true),
                new EmptyBorder(14, 14, 14, 14)
        ));

        JLabel titulo = new JLabel("Registro de Canción");
        titulo.setForeground(TEXTO_CLARO);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 14));
        tarjeta.add(titulo, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(BG_TARJETA);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 4, 5, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtId = crearCampoTexto("", 16);
        txtNombre = crearCampoTexto("", 16);
        txtTipo = crearCampoTexto("", 16);
        txtAtributoNum = crearCampoTexto("", 16);
        txtGenero = crearCampoTexto("", 16);
        txtIdioma = crearCampoTexto("", 16);
        txtAnio = crearCampoTexto("", 16);

        agregarFilaForm(form, gbc, 0, "ID (clave):", txtId);
        agregarFilaForm(form, gbc, 1, "Nombre:", txtNombre);
        agregarFilaForm(form, gbc, 2, "Tipo / Categoría:", txtTipo);
        agregarFilaForm(form, gbc, 3, "Atributo numérico:", txtAtributoNum);
        agregarFilaForm(form, gbc, 4, "Género:", txtGenero);
        agregarFilaForm(form, gbc, 5, "Versión (idioma/país):", txtIdioma);
        agregarFilaForm(form, gbc, 6, "Versión 2 (año):", txtAnio);

        JLabel notaTip = new JLabel("<html><i>Tip: Haz clic en cualquier fila ocupada de la tabla para cargar sus datos aquí.</i></html>");
        notaTip.setForeground(TEXTO_TENUE);
        notaTip.setFont(new Font("SansSerif", Font.PLAIN, 11));
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 4, 4, 4);
        form.add(notaTip, gbc);

        tarjeta.add(form, BorderLayout.CENTER);

        JPanel botones = new JPanel(new GridLayout(3, 2, 8, 8));
        botones.setBackground(BG_TARJETA);

        JButton btnInsertar = crearBoton("Insertar", VERDE_SPOTIFY);
        JButton btnBuscar = crearBoton("Buscar por ID", AZUL_ACCION);
        JButton btnActualizar = crearBoton("Actualizar", AMBAR_ACCION);
        JButton btnEliminar = crearBoton("Eliminar por ID", ROJO_ACCION);
        JButton btnLimpiarForm = crearBoton("Limpiar Campos", GRIS_BOTON);
        JButton btnVaciarTabla = crearBoton("Vaciar Tabla", new Color(110, 45, 45));

        btnInsertar.addActionListener(e -> accionInsertar());
        btnBuscar.addActionListener(e -> accionBuscar());
        btnActualizar.addActionListener(e -> accionActualizar());
        btnEliminar.addActionListener(e -> accionEliminar());
        btnLimpiarForm.addActionListener(e -> limpiarFormulario());
        btnVaciarTabla.addActionListener(e -> accionVaciarTabla());

        botones.add(btnInsertar);
        botones.add(btnBuscar);
        botones.add(btnActualizar);
        botones.add(btnEliminar);
        botones.add(btnLimpiarForm);
        botones.add(btnVaciarTabla);

        tarjeta.add(botones, BorderLayout.SOUTH);
        return tarjeta;
    }

    private void agregarFilaForm(JPanel panel, GridBagConstraints gbc, int fila, String texto, JTextField campo) {
        gbc.gridwidth = 1;
        gbc.gridy = fila;

        gbc.gridx = 0;
        gbc.weightx = 0.0;
        panel.add(crearEtiqueta(texto), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(campo, gbc);
    }

    private JPanel crearPanelVisorTabla() {
        JPanel tarjeta = new JPanel(new BorderLayout(0, 8));
        tarjeta.setBackground(BG_TARJETA);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDE, 1, true),
                new EmptyBorder(12, 12, 12, 12)
        ));

        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setBackground(BG_TARJETA);

        JLabel titulo = new JLabel("Estado Interno de la Tabla Hash");
        titulo.setForeground(TEXTO_CLARO);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 14));

        chkSoloOcupados = new JCheckBox("Mostrar solo cubetas/celdas ocupadas", true);
        chkSoloOcupados.setBackground(BG_TARJETA);
        chkSoloOcupados.setForeground(TEXTO_CLARO);
        chkSoloOcupados.setFont(new Font("SansSerif", Font.PLAIN, 12));
        chkSoloOcupados.addActionListener(e -> refrescarTablaVisual());

        cabecera.add(titulo, BorderLayout.WEST);
        cabecera.add(chkSoloOcupados, BorderLayout.EAST);
        tarjeta.add(cabecera, BorderLayout.NORTH);

        String[] columnas = {
                "Índice", "Estado", "ID", "Nombre", "Tipo / Categoría",
                "Num.", "Género", "Idioma / País", "Año"
        };
        modeloTablaHash = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        jTableHash = new JTable(modeloTablaHash);
        estilizarTabla(jTableHash);
        jTableHash.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        int[] anchos = {55, 135, 70, 155, 195, 55, 105, 85, 50};
        for (int i = 0; i < anchos.length; i++) {
            jTableHash.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }

        jTableHash.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String texto = (value != null) ? value.toString() : "";
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? new Color(32, 32, 32) : new Color(38, 38, 38));
                    if (texto.startsWith("OCUPADO")) {
                        c.setForeground(VERDE_SPOTIFY);
                    } else if (texto.startsWith("ELIMINADO")) {
                        c.setForeground(new Color(245, 158, 11));
                    } else {
                        c.setForeground(TEXTO_TENUE);
                    }
                }
                setFont(new Font("SansSerif", Font.BOLD, 11));
                return c;
            }
        });

        jTableHash.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int fila = jTableHash.getSelectedRow();
                if (fila >= 0) {
                    String id = String.valueOf(modeloTablaHash.getValueAt(fila, 2));
                    if (!"-".equals(id)) {
                        Cancion c = tablaHash.buscar(id);
                        if (c != null) {
                            llenarFormulario(c);
                        }
                    }
                }
            }
        });

        JScrollPane scroll = new JScrollPane(jTableHash);
        scroll.getViewport().setBackground(new Color(26, 26, 26));
        scroll.setBorder(new LineBorder(BORDE, 1));
        tarjeta.add(scroll, BorderLayout.CENTER);

        lblResumenEstructura = new JLabel("-");
        lblResumenEstructura.setForeground(TEXTO_TENUE);
        lblResumenEstructura.setFont(new Font("SansSerif", Font.PLAIN, 12));
        tarjeta.add(lblResumenEstructura, BorderLayout.SOUTH);

        return tarjeta;
    }

    private JPanel crearPestanaRendimiento() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BG_PRINCIPAL);
        panel.setBorder(new EmptyBorder(10, 14, 10, 14));

        JPanel barra = new JPanel(new BorderLayout(10, 6));
        barra.setBackground(BG_TARJETA);
        barra.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDE, 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));

        JPanel botonesIzq = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        botonesIzq.setBackground(BG_TARJETA);

        JButton btnSelArchivos = crearBoton("1. Seleccionar CSV(s)", AZUL_ACCION);
        btnSelArchivos.addActionListener(e -> seleccionarArchivosBenchmark());

        btnEjecutarBenchmark = crearBoton("2. Ejecutar Pruebas (N = 100..1000, 3 rep.)", VERDE_SPOTIFY);
        btnEjecutarBenchmark.addActionListener(e -> ejecutarPruebasEnSegundoPlano());

        btnExportarCSV = crearBoton("Exportar Tabla CSV", GRIS_BOTON);
        btnExportarCSV.setEnabled(false);
        btnExportarCSV.addActionListener(e -> exportarBenchmarkCSV());

        btnGuardarPNG = crearBoton("Guardar Gráfica PNG", GRIS_BOTON);
        btnGuardarPNG.setEnabled(false);
        btnGuardarPNG.addActionListener(e -> exportarGraficaPNG());

        botonesIzq.add(btnSelArchivos);
        botonesIzq.add(btnEjecutarBenchmark);
        botonesIzq.add(btnExportarCSV);
        botonesIzq.add(btnGuardarPNG);

        lblArchivosBenchmark = new JLabel();
        lblArchivosBenchmark.setFont(new Font("SansSerif", Font.PLAIN, 12));
        actualizarTextoDatasetsBenchmark();

        barra.add(botonesIzq, BorderLayout.NORTH);
        barra.add(lblArchivosBenchmark, BorderLayout.SOUTH);
        panel.add(barra, BorderLayout.NORTH);

        String[] colsBench = {
                "Estrategia", "N", "Capacidad (m)", "Factor Carga (%)",
                "Colisiones Prom.", "Inserción Lote (ms)",
                "Búsq. 25% Exist. (ms)", "Búsq. 25% Inexist. (ms)", "Eliminación 25% (ms)"
        };
        modeloBenchmark = new DefaultTableModel(colsBench, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable tablaBench = new JTable(modeloBenchmark);
        estilizarTabla(tablaBench);
        tablaBench.getColumnModel().getColumn(0).setPreferredWidth(210);

        JScrollPane scrollBench = new JScrollPane(tablaBench);
        scrollBench.getViewport().setBackground(new Color(26, 26, 26));
        scrollBench.setBorder(new LineBorder(BORDE, 1));

        panelGrafica = new PanelGraficaColisiones();
        JPanel contenedorGrafica = new JPanel(new BorderLayout());
        contenedorGrafica.setBackground(BG_TARJETA);
        contenedorGrafica.setBorder(new LineBorder(BORDE, 1));
        contenedorGrafica.add(panelGrafica, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollBench, contenedorGrafica);
        split.setDividerLocation(255);
        split.setResizeWeight(0.45);
        split.setBackground(BG_PRINCIPAL);
        split.setBorder(null);

        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    private void actualizarTextoDatasetsBenchmark() {
        if (archivosBenchmark != null && archivosBenchmark.length > 0) {
            StringBuilder sb = new StringBuilder("Dataset activo: ");
            for (int i = 0; i < archivosBenchmark.length; i++) {
                sb.append(archivosBenchmark[i].getName());
                if (i + 1 < archivosBenchmark.length) {
                    sb.append(", ");
                }
            }
            lblArchivosBenchmark.setText(sb.toString());
            lblArchivosBenchmark.setForeground(VERDE_SPOTIFY);
        } else {
            lblArchivosBenchmark.setText("Dataset: Generación automática (o selecciona tus CSV con el botón 1)");
            lblArchivosBenchmark.setForeground(TEXTO_TENUE);
        }
    }

    // Acciones del usuario


    private void actualizarPreviewPrimo() {
        try {
            int esperado = Integer.parseInt(txtItemsEsperados.getText().trim());
            if (esperado > 0) {
                int primo = FuncionHash.calcularTamanoOptimo(esperado);
                lblPrimoCalculado.setText(" Primo óptimo (m): " + primo);
                lblPrimoCalculado.setForeground(VERDE_SPOTIFY);
            } else {
                lblPrimoCalculado.setText(" Ingrese entero > 0");
                lblPrimoCalculado.setForeground(ROJO_ACCION);
            }
        } catch (NumberFormatException ex) {
            lblPrimoCalculado.setText(" Valor inválido");
            lblPrimoCalculado.setForeground(ROJO_ACCION);
        }
    }

    private void aplicarConfiguracionTabla() {
        int esperado;
        try {
            esperado = Integer.parseInt(txtItemsEsperados.getText().trim());
            if (esperado <= 0) {
                mostrarError("El número esperado de ítems debe ser un entero positivo.");
                return;
            }
        } catch (NumberFormatException ex) {
            mostrarError("Ingrese un número entero válido para los ítems esperados.");
            return;
        }

        int seleccion = cmbEstrategia.getSelectedIndex();
        if (seleccion == 0) {
            tablaHash = new TablaHashEncadenamiento(esperado);
        } else if (seleccion == 1) {
            tablaHash = new TablaHashDireccionamientoAbierto(
                    esperado, TablaHashDireccionamientoAbierto.TipoSondeo.SONDEO_LINEAL);
        } else {
            tablaHash = new TablaHashDireccionamientoAbierto(
                    esperado, TablaHashDireccionamientoAbierto.TipoSondeo.DOBLE_DISPERSION);
        }

        CargadorArchivo.reiniciarContadorIds();
        limpiarFormulario();
        actualizarVistaCompleta();
        establecerEstado("Tabla reconfigurada: " + tablaHash.getNombreEstrategia()
                + "  Capacidad prima m = " + tablaHash.getCapacidad());
    }

    private void cargarCSVDesdeSelector() {
        JFileChooser chooser = crearFileChooserCSV(true);
        int opcion = chooser.showOpenDialog(this);
        if (opcion != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File[] archivos = chooser.getSelectedFiles();
        if (archivos == null || archivos.length == 0) {
            File unico = chooser.getSelectedFile();
            if (unico != null) {
                archivos = new File[]{unico};
            } else {
                return;
            }
        }

        try {
            long inicio = System.nanoTime();
            int[] resumen = CargadorArchivo.cargarArchivosEnTabla(archivos, tablaHash);
            long fin = System.nanoTime();
            double ms = (fin - inicio) / 1_000_000.0;

            actualizarVistaCompleta();

            String msg = String.format(
                    "Carga completada en %.4f ms\n\n"
                    + " Archivos seleccionados: %d\n"
                    + " Registros leídos: %d\n"
                    + " Insertados en la tabla: %d\n"
                    + " Omitidos (duplicados o tabla llena): %d\n"
                    + " Factor de carga actual: %.2f%%\n"
                    + " Colisiones acumuladas: %d",
                    ms, archivos.length, resumen[2], resumen[0], resumen[1],
                    tablaHash.getFactorCarga() * 100.0, tablaHash.getColisiones()
            );
            establecerEstado(String.format("Cargados %d registros desde %d archivo(s) en %.4f ms.",
                    resumen[0], archivos.length, ms));
            JOptionPane.showMessageDialog(this, msg, "Resultado de Carga CSV", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            mostrarError("Error al leer el archivo: " + ex.getMessage());
        }
    }

    private void accionInsertar() {
        Cancion cancion = construirCancionDesdeFormulario();
        if (cancion == null) {
            return;
        }

        long inicio = System.nanoTime();
        boolean exito = tablaHash.insertar(cancion);
        long fin = System.nanoTime();
        double ms = (fin - inicio) / 1_000_000.0;

        if (exito) {
            actualizarVistaCompleta();
            seleccionarFilaPorId(cancion.getId());
            establecerEstado(String.format("Canción '%s' insertada en %.4f ms (cubeta hash = %d).",
                    cancion.getId(), ms, FuncionHash.hashPrimario(cancion.getId(), tablaHash.getCapacidad())));
        } else {
            mostrarError("No se pudo insertar la canción.\nVerifique que el ID '" + cancion.getId()
                    + "' no esté duplicado y que la tabla tenga espacio disponible.");
        }
    }

    private void accionBuscar() {
        String id = txtId.getText().trim();
        if (id.isEmpty()) {
            mostrarError("Ingrese el ID (clave) de la canción que desea buscar.");
            return;
        }

        long inicio = System.nanoTime();
        Cancion encontrada = tablaHash.buscar(id);
        long fin = System.nanoTime();
        double ms = (fin - inicio) / 1_000_000.0;

        if (encontrada != null) {
            llenarFormulario(encontrada);
            seleccionarFilaPorId(id);
            establecerEstado(String.format("Canción '%s' encontrada en %.4f ms: %s",
                    id, ms, encontrada.getNombre()));
        } else {
            establecerEstado(String.format("No se encontró ninguna canción con ID '%s' (%.4f ms).", id, ms));
            JOptionPane.showMessageDialog(this,
                    "No existe ninguna canción registrada con el ID: " + id,
                    "Búsqueda sin resultados", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void accionActualizar() {
        Cancion datosNuevos = construirCancionDesdeFormulario();
        if (datosNuevos == null) {
            return;
        }

        long inicio = System.nanoTime();
        boolean exito = tablaHash.actualizar(
                datosNuevos.getId(),
                datosNuevos.getNombre(),
                datosNuevos.getTipoCategoria(),
                datosNuevos.getAtributoNumerico(),
                datosNuevos.getGenero(),
                datosNuevos.getVersionIdioma(),
                datosNuevos.getVersionAño()
        );
        long fin = System.nanoTime();
        double ms = (fin - inicio) / 1_000_000.0;

        if (exito) {
            actualizarVistaCompleta();
            seleccionarFilaPorId(datosNuevos.getId());
            establecerEstado(String.format("Canción '%s' actualizada correctamente en %.4f ms.",
                    datosNuevos.getId(), ms));
        } else {
            mostrarError("No se encontró ninguna canción con el ID '" + datosNuevos.getId() + "' para actualizar.");
        }
    }

    private void accionEliminar() {
        String id = txtId.getText().trim();
        if (id.isEmpty()) {
            mostrarError("Ingrese el ID (clave) de la canción que desea eliminar.");
            return;
        }

        long inicio = System.nanoTime();
        boolean eliminada = tablaHash.eliminar(id);
        long fin = System.nanoTime();
        double ms = (fin - inicio) / 1_000_000.0;

        if (eliminada) {
            limpiarFormulario();
            actualizarVistaCompleta();
            establecerEstado(String.format("Canción '%s' eliminada correctamente en %.4f ms.", id, ms));
        } else {
            mostrarError("No se encontró ninguna canción registrada con el ID: " + id);
        }
    }

    private void accionVaciarTabla() {
        int conf = JOptionPane.showConfirmDialog(this,
                "¿Desea eliminar todos los registros de la tabla hash actual?",
                "Confirmar vaciado", JOptionPane.YES_NO_OPTION);
        if (conf == JOptionPane.YES_OPTION) {
            tablaHash.limpiar();
            CargadorArchivo.reiniciarContadorIds();
            limpiarFormulario();
            actualizarVistaCompleta();
            establecerEstado("Tabla hash vaciada y contadores reiniciados.");
        }
    }

    private void seleccionarArchivosBenchmark() {
        JFileChooser chooser = crearFileChooserCSV(true);
        int opcion = chooser.showOpenDialog(this);
        if (opcion == JFileChooser.APPROVE_OPTION) {
            File[] sel = chooser.getSelectedFiles();
            if (sel == null || sel.length == 0) {
                File unico = chooser.getSelectedFile();
                if (unico != null) {
                    sel = new File[]{unico};
                }
            }
            if (sel != null && sel.length > 0) {
                archivosBenchmark = sel;
                actualizarTextoDatasetsBenchmark();
            }
        }
    }

    private void ejecutarPruebasEnSegundoPlano() {
        btnEjecutarBenchmark.setEnabled(false);
        btnExportarCSV.setEnabled(false);
        btnGuardarPNG.setEnabled(false);
        establecerEstado("Ejecutando pruebas de rendimiento (N = 100..1000, 3 repeticiones por estrategia)");

        SwingWorker<List<EvaluadorRendimiento.ResultadoFila>, Void> worker =
                new SwingWorker<List<EvaluadorRendimiento.ResultadoFila>, Void>() {
            private int registrosArchivo = 0;

            @Override
            protected List<EvaluadorRendimiento.ResultadoFila> doInBackground() throws Exception {
                List<Cancion> dataset = new ArrayList<>();
                if (archivosBenchmark != null && archivosBenchmark.length > 0) {
                    CargadorArchivo.reiniciarContadorIds();
                    for (File f : archivosBenchmark) {
                        dataset.addAll(CargadorArchivo.leerCancionesDesdeArchivo(f));
                    }
                    registrosArchivo = dataset.size();
                }
                return EvaluadorRendimiento.ejecutarPruebasCompletas(dataset.isEmpty() ? null : dataset);
            }

            @Override
            protected void done() {
                btnEjecutarBenchmark.setEnabled(true);
                try {
                    ultimosResultados = get();
                    modeloBenchmark.setRowCount(0);
                    for (EvaluadorRendimiento.ResultadoFila r : ultimosResultados) {
                        modeloBenchmark.addRow(new Object[]{
                                r.estrategia,
                                r.n,
                                r.capacidad,
                                String.format("%.2f%%", r.factorCargaPorcentaje),
                                String.format("%.1f", r.colisionesPromedio),
                                String.format("%.4f", r.tiempoInsercionMs),
                                String.format("%.4f", r.tiempoBusqExistenteMs),
                                String.format("%.4f", r.tiempoBusqInexistenteMs),
                                String.format("%.4f", r.tiempoEliminacionMs)
                        });
                    }
                    panelGrafica.setResultados(ultimosResultados);
                    btnExportarCSV.setEnabled(true);
                    btnGuardarPNG.setEnabled(true);

                    String origen = (registrosArchivo > 0)
                            ? registrosArchivo + " registros leídos de archivo(s) CSV"
                            : "1000 registros generados automáticamente";
                    establecerEstado("Pruebas de rendimiento finalizadas (" + origen + ").");
                } catch (Exception ex) {
                    mostrarError("Error durante las pruebas de rendimiento: " + ex.getMessage());
                    establecerEstado("Error al ejecutar pruebas de rendimiento.");
                }
            }
        };
        worker.execute();
    }

    private void exportarBenchmarkCSV() {
        if (ultimosResultados == null || ultimosResultados.isEmpty()) {
            return;
        }
        JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
        chooser.setDialogTitle("Guardar resultados de rendimiento como CSV");
        chooser.setSelectedFile(new File("resultados_rendimiento_hash.csv"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File destino = chooser.getSelectedFile();
            if (!destino.getName().toLowerCase().endsWith(".csv")) {
                destino = new File(destino.getAbsolutePath() + ".csv");
            }
            try {
                EvaluadorRendimiento.exportarResultadosCSV(ultimosResultados, destino);
                establecerEstado("Resultados exportados a: " + destino.getAbsolutePath());
                JOptionPane.showMessageDialog(this,
                        "Archivo CSV exportado correctamente en:\n" + destino.getAbsolutePath(),
                        "Exportación Exitosa", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                mostrarError("No se pudo guardar el CSV: " + ex.getMessage());
            }
        }
    }

    private void exportarGraficaPNG() {
        if (!panelGrafica.tieneDatos()) {
            return;
        }
        JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
        chooser.setDialogTitle("Guardar gráfica de Colisiones vs Carga (PNG)");
        chooser.setSelectedFile(new File("grafica_colisiones_vs_carga.png"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File destino = chooser.getSelectedFile();
            if (!destino.getName().toLowerCase().endsWith(".png")) {
                destino = new File(destino.getAbsolutePath() + ".png");
            }
            try {
                panelGrafica.exportarComoPNG(destino);
                establecerEstado("Gráfica guardada en: " + destino.getAbsolutePath());
                JOptionPane.showMessageDialog(this,
                        "Gráfica PNG guardada correctamente en:\n" + destino.getAbsolutePath(),
                        "Gráfica Guardada", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                mostrarError("No se pudo guardar la imagen PNG: " + ex.getMessage());
            }
        }
    }

    // Utilidades de interfaz y sincronización

    private void actualizarVistaCompleta() {
        lblMetricaEstrategia.setText(tablaHash.getNombreEstrategia());
        lblMetricaCapacidad.setText(String.valueOf(tablaHash.getCapacidad()));
        lblMetricaElementos.setText(String.valueOf(tablaHash.getCantidadElementos()));
        lblMetricaColisiones.setText(String.valueOf(tablaHash.getColisiones()));

        double porcentaje = tablaHash.getFactorCarga() * 100.0;
        lblMetricaFactor.setText(String.format("%.4f (%.2f%%)", tablaHash.getFactorCarga(), porcentaje));
        barraFactorCarga.setValue((int) Math.min(100, Math.round(porcentaje)));
        if (porcentaje >= 85.0) {
            barraFactorCarga.setForeground(ROJO_ACCION);
        } else if (porcentaje >= 70.0) {
            barraFactorCarga.setForeground(AMBAR_ACCION);
        } else {
            barraFactorCarga.setForeground(VERDE_SPOTIFY);
        }

        refrescarTablaVisual();
    }

    private void refrescarTablaVisual() {
        modeloTablaHash.setRowCount(0);
        List<Object[]> filas = tablaHash.obtenerFilasEstado(chkSoloOcupados.isSelected());
        for (Object[] fila : filas) {
            modeloTablaHash.addRow(fila);
        }
        lblResumenEstructura.setText(tablaHash.getResumenEstructura());
    }

    private void seleccionarFilaPorId(String idBuscado) {
        for (int i = 0; i < modeloTablaHash.getRowCount(); i++) {
            if (idBuscado.equals(String.valueOf(modeloTablaHash.getValueAt(i, 2)))) {
                jTableHash.setRowSelectionInterval(i, i);
                jTableHash.scrollRectToVisible(jTableHash.getCellRect(i, 0, true));
                return;
            }
        }
    }

    private Cancion construirCancionDesdeFormulario() {
        String id = txtId.getText().trim();
        String nombre = txtNombre.getText().trim();
        String tipo = txtTipo.getText().trim();
        String numTexto = txtAtributoNum.getText().trim();
        String genero = txtGenero.getText().trim();
        String idioma = txtIdioma.getText().trim();
        String anioTexto = txtAnio.getText().trim();

        if (id.isEmpty() || nombre.isEmpty() || tipo.isEmpty()
                || numTexto.isEmpty() || genero.isEmpty() || idioma.isEmpty() || anioTexto.isEmpty()) {
            mostrarError("Todos los campos del registro son obligatorios.");
            return null;
        }

        double atributoNum;
        try {
            atributoNum = Double.parseDouble(numTexto);
        } catch (NumberFormatException ex) {
            mostrarError("El campo 'Atributo numérico' debe ser un número válido (ej. 0.82 o 215).");
            return null;
        }

        int anio;
        try {
            anio = Integer.parseInt(anioTexto);
        } catch (NumberFormatException ex) {
            mostrarError("El campo 'Versión 2 (año)' debe ser un número entero válido (ej. 2025).");
            return null;
        }

        return new Cancion(id, nombre, tipo, atributoNum, genero, idioma, anio);
    }

    private void llenarFormulario(Cancion c) {
        txtId.setText(c.getId());
        txtNombre.setText(c.getNombre());
        txtTipo.setText(c.getTipoCategoria());
        txtAtributoNum.setText(c.getAtributoNumericoFormateado());
        txtGenero.setText(c.getGenero());
        txtIdioma.setText(c.getVersionIdioma());
        txtAnio.setText(String.valueOf(c.getVersionAño()));
    }

    private void limpiarFormulario() {
        txtId.setText("");
        txtNombre.setText("");
        txtTipo.setText("");
        txtAtributoNum.setText("");
        txtGenero.setText("");
        txtIdioma.setText("");
        txtAnio.setText("");
        jTableHash.clearSelection();
    }

    private JFileChooser crearFileChooserCSV(boolean multiSeleccion) {
        JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
        chooser.setDialogTitle("Seleccionar archivo(s) CSV / TXT de canciones");
        chooser.setMultiSelectionEnabled(multiSeleccion);
        chooser.setFileFilter(new FileNameExtensionFilter("Archivos CSV / TXT (*.csv, *.txt)", "csv", "txt"));
        return chooser;
    }

    private void estilizarTabla(JTable tabla) {
        tabla.setBackground(new Color(32, 32, 32));
        tabla.setForeground(TEXTO_CLARO);
        tabla.setGridColor(new Color(48, 48, 48));
        tabla.setRowHeight(25);
        tabla.setFont(new Font("SansSerif", Font.PLAIN, 12));
        tabla.setSelectionBackground(new Color(29, 185, 84, 90));
        tabla.setSelectionForeground(TEXTO_CLARO);
        tabla.setShowVerticalLines(false);

        JTableHeader header = tabla.getTableHeader();
        header.setBackground(new Color(22, 22, 22));
        header.setForeground(TEXTO_CLARO);
        header.setFont(new Font("SansSerif", Font.BOLD, 12));
        header.setReorderingAllowed(false);

        DefaultTableCellRenderer zebra = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? new Color(32, 32, 32) : new Color(38, 38, 38));
                    c.setForeground(TEXTO_CLARO);
                }
                if (column == 0 || column == 2 || column == 5 || column == 8) {
                    setHorizontalAlignment(SwingConstants.CENTER);
                } else {
                    setHorizontalAlignment(SwingConstants.LEFT);
                }
                return c;
            }
        };
        tabla.setDefaultRenderer(Object.class, zebra);
    }

    private JLabel crearEtiqueta(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setForeground(TEXTO_CLARO);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        return lbl;
    }

    private JTextField crearCampoTexto(String valorInicial, int columnas) {
        JTextField campo = new JTextField(valorInicial, columnas);
        campo.setBackground(BG_CAMPO);
        campo.setForeground(TEXTO_CLARO);
        campo.setCaretColor(VERDE_SPOTIFY);
        campo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        campo.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDE, 1, true),
                new EmptyBorder(5, 8, 5, 8)
        ));
        return campo;
    }

    private JButton crearBoton(String texto, Color colorFondo) {
        JButton btn = new JButton(texto);
        btn.setBackground(colorFondo);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 12, 8, 12));
        return btn;
    }

    private void establecerEstado(String mensaje) {
        lblEstado.setText(mensaje);
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Aviso", JOptionPane.ERROR_MESSAGE);
    }
}
