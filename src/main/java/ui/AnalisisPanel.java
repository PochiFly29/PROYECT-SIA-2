package ui;

import gestores.GestorStatsProvider;
import gestores.ServicioConsulta;
import gestores.GestorStatsProvider.StatConvenio;
import modelo.Estudiante;
import net.miginfocom.swing.MigLayout;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.PieToolTipGenerator;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AnalisisPanel extends JPanel {

    private final ServicioConsulta consulta;
    private final GestorStatsProvider statsProvider;
    private JPanel contentPanel; // Panel principal que irá dentro del JScrollPane

    public AnalisisPanel(ServicioConsulta consulta) {
        this.consulta = consulta;
        this.statsProvider = new GestorStatsProvider(consulta);
        init();
        refresh();
    }

    private void init() {
        setLayout(new BorderLayout(16, 16));
        setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));

        // --- 1. Header (Título y botón de exportar) ---
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel titulo = new JLabel("Análisis Estadístico");
        titulo.setFont(getFont().deriveFont(Font.BOLD, 22f));

        JButton btnExportar = new JButton("Exportar a Excel");
        btnExportar.addActionListener(e -> exportarAExcel());
        Color excelGreen = new Color(0x217346);
        btnExportar.setBackground(excelGreen);
        btnExportar.setForeground(Color.WHITE);

        header.add(titulo, BorderLayout.WEST);
        header.add(btnExportar, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // --- 2. Panel de Contenido Principal (con MigLayout para mejor espaciado) ---
        contentPanel = new JPanel(new MigLayout("wrap, fillx, insets 0", "[fill]", "[]15[]15[]15[]"));
        contentPanel.setOpaque(false);

        // --- 3. Panel Deslizante (JScrollPane) ---
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
    }

    public void refresh() {
        contentPanel.removeAll();

        // --- Sección de KPIs (Indicadores Clave) ---
        JPanel kpiPanel = new JPanel(new GridLayout(1, 4, 16, 16));
        kpiPanel.setOpaque(false);

        kpiPanel.add(makeKpiCard("Postulantes Únicos", new JLabel(String.valueOf(statsProvider.totalPostulantes()))));
        kpiPanel.add(makeKpiCard("Total Postulaciones", new JLabel(String.valueOf(statsProvider.totalPostulaciones()))));
        kpiPanel.add(makeKpiCard("Promedio General", new JLabel(String.format("%.2f", statsProvider.promedioGeneral()))));
        kpiPanel.add(makeKpiCard("Convenios Activos", new JLabel(String.valueOf(statsProvider.totalConvenios()))));

        contentPanel.add(kpiPanel, "growx");

        // --- Sección de Gráficos y Tablas ---
        List<StatConvenio> statsConvenios = statsProvider.statsPostulacionesPorConvenio();

        contentPanel.add(makeSectionLabel("Distribución de Postulaciones por Convenio"), "gaptop 20");
        contentPanel.add(makeCard(makeConveniosPieChart(statsConvenios)), "growx");

        contentPanel.add(makeSectionLabel("Rendimiento Académico por Postulante"), "gaptop 20");
        contentPanel.add(makeCard(makePromediosBarChart()), "growx");

        contentPanel.add(makeSectionLabel("Análisis Detallado por Convenio"), "gaptop 20");
        contentPanel.add(makeCard(makeConveniosStatsTable(statsConvenios)), "growx");

        revalidate();
        repaint();
    }

    // --- Métodos de Ayuda para construir la UI ---

    private JPanel makeKpiCard(String title, JLabel valueLabel) {
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        valueLabel.setFont(getFont().deriveFont(Font.BOLD, 28f));

        JPanel card = new JPanel(new BorderLayout(0, 5));
        card.setOpaque(false);

        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setFont(getFont().deriveFont(14f));

        card.add(valueLabel, BorderLayout.CENTER);
        card.add(lblTitle, BorderLayout.SOUTH);

        return makeCard(card);
    }

    private JLabel makeSectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(getFont().deriveFont(Font.BOLD, 18f));
        l.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 0));
        return l;
    }

    private JPanel makeCard(JComponent inner) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(UIManager.getColor("Panel.background").darker());
        card.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    // --- Métodos para crear los gráficos y tablas ---

    private ChartPanel makePromediosBarChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        consulta.getTodosLosProgramas().forEach(programa ->
                programa.getPostulaciones().forEach(postulacion -> {
                    consulta.buscarEstudiantePorRut(postulacion.getRutEstudiante()).ifPresent(est ->
                            dataset.addValue(est.getPromedio(), "Promedio", est.getNombreCompleto()));
                })
        );

        JFreeChart chart = ChartFactory.createBarChart(null, "Estudiante", "Promedio", dataset, PlotOrientation.VERTICAL, false, true, false);
        chart.setBackgroundPaint(UIManager.getColor("Panel.background"));

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(UIManager.getColor("Panel.background"));
        plot.setDomainGridlinePaint(new Color(80, 80, 80));
        plot.setRangeGridlinePaint(new Color(100, 100, 100));

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(46, 134, 255));
        renderer.setShadowVisible(false);
        renderer.setDefaultItemLabelsVisible(true);
        renderer.setDefaultItemLabelPaint(Color.WHITE);
        renderer.setDefaultItemLabelFont(getFont().deriveFont(Font.BOLD, 12f));
        renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator("{2}", NumberFormat.getNumberInstance()));

        ChartPanel panel = new ChartPanel(chart);
        panel.setPreferredSize(new Dimension(800, 350));
        panel.setMouseWheelEnabled(true);
        panel.setOpaque(false);
        return panel;
    }

    private ChartPanel makeConveniosPieChart(List<StatConvenio> stats) {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        stats.forEach(sc -> dataset.setValue(sc.id(), sc.count()));

        JFreeChart chart = ChartFactory.createPieChart(null, dataset, false, true, false);
        chart.setBackgroundPaint(UIManager.getColor("Panel.background"));

        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setBackgroundPaint(UIManager.getColor("Panel.background"));
        plot.setLabelGenerator(null);
        plot.setSimpleLabels(false);

        Map<String, StatConvenio> idx = stats.stream().collect(Collectors.toMap(StatConvenio::id, s -> s, (a, b) -> a, LinkedHashMap::new));
        DecimalFormat pctFmt = new DecimalFormat("0.0'%'");
        plot.setToolTipGenerator((PieDataset ds, Comparable key) -> {
            StatConvenio sc = idx.get(String.valueOf(key));
            return sc != null ? sc.label() + ": " + pctFmt.format(sc.percent() * 100.0) : String.valueOf(key);
        });

        ChartPanel panel = new ChartPanel(chart);
        panel.setPreferredSize(new Dimension(800, 300));
        panel.setMouseWheelEnabled(true);
        panel.setOpaque(false);
        return panel;
    }

    private JComponent makeConveniosStatsTable(List<StatConvenio> stats) {
        String[] cols = {"#", "Convenio", "Postulaciones", "% del total"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        DecimalFormat pct = new DecimalFormat("0.0'%'");
        int i = 1;
        for (StatConvenio sc : stats) {
            model.addRow(new Object[]{i++, sc.label(), sc.count(), pct.format(sc.percent() * 100.0)});
        }

        JTable table = new JTable(model);
        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
        table.setRowHeight(24);
        table.getTableHeader().setReorderingAllowed(false);

        JScrollPane sp = new JScrollPane(table);
        sp.setPreferredSize(new Dimension(800, 200));
        return sp;
    }

    // --- Exportación a Excel ---
    private void exportarAExcel() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Guardar análisis como Excel");
        chooser.setSelectedFile(new File("analisis_intercambios.xlsx"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".xlsx")) {
            file = new File(file.getParentFile(), file.getName() + ".xlsx");
        }

        try (Workbook wb = new XSSFWorkbook(); FileOutputStream fos = new FileOutputStream(file)) {
            // Hoja 1: KPIs
            Sheet shKpi = wb.createSheet("KPIs");
            shKpi.createRow(0).createCell(0).setCellValue("Métrica");
            shKpi.getRow(0).createCell(1).setCellValue("Valor");
            shKpi.createRow(1).createCell(0).setCellValue("Postulantes Únicos");
            shKpi.getRow(1).createCell(1).setCellValue(statsProvider.totalPostulantes());
            shKpi.createRow(2).createCell(0).setCellValue("Total Postulaciones");
            shKpi.getRow(2).createCell(1).setCellValue(statsProvider.totalPostulaciones());
            shKpi.createRow(3).createCell(0).setCellValue("Promedio General");
            shKpi.getRow(3).createCell(1).setCellValue(statsProvider.promedioGeneral());
            shKpi.createRow(4).createCell(0).setCellValue("Convenios Activos");
            shKpi.getRow(4).createCell(1).setCellValue(statsProvider.totalConvenios());
            shKpi.autoSizeColumn(0); shKpi.autoSizeColumn(1);

            // Hoja 2: Postulaciones por convenio
            Sheet shConv = wb.createSheet("Postulaciones por Convenio");
            Row hConv = shConv.createRow(0);
            hConv.createCell(0).setCellValue("#");
            hConv.createCell(1).setCellValue("Convenio");
            hConv.createCell(2).setCellValue("Nº Postulaciones");
            hConv.createCell(3).setCellValue("% del Total");
            int i = 1;
            for (StatConvenio sc : statsProvider.statsPostulacionesPorConvenio()) {
                Row r = shConv.createRow(i++);
                r.createCell(0).setCellValue(i - 1);
                r.createCell(1).setCellValue(sc.label());
                r.createCell(2).setCellValue(sc.count());
                r.createCell(3).setCellValue(sc.percent());
            }
            for (int c = 0; c <= 3; c++) shConv.autoSizeColumn(c);

            // Hoja 3: Promedios por estudiante
            Sheet shEst = wb.createSheet("Promedios por Estudiante");
            Row hEst = shEst.createRow(0);
            hEst.createCell(0).setCellValue("Estudiante");
            hEst.createCell(1).setCellValue("Promedio");
            final int[] rEstIdx = {1};
            consulta.getTodosLosProgramas().forEach(programa ->
                    programa.getPostulaciones().forEach(postulacion ->
                            consulta.buscarEstudiantePorRut(postulacion.getRutEstudiante()).ifPresent(est -> {
                                Row r = shEst.createRow(rEstIdx[0]++);
                                r.createCell(0).setCellValue(est.getNombreCompleto());
                                r.createCell(1).setCellValue(est.getPromedio());
                            })
                    )
            );
            shEst.autoSizeColumn(0); shEst.autoSizeColumn(1);

            wb.write(fos);
            JOptionPane.showMessageDialog(this, "Excel exportado exitosamente:\n" + file.getAbsolutePath(), "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al exportar a Excel: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}