package ui;

import gestores.GestorStatsProvider;
import gestores.ServicioConsulta;
import gestores.GestorStatsProvider.StatConvenio;
import modelo.Estudiante;
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
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Panel de análisis para auditores.
 *
 * Incluye graficas y analisis estadisticos.
 * Incluye Exportar a Excel.
 */
public class AnalisisPanel extends JPanel {

    private final ServicioConsulta consulta;
    private final GestorStatsProvider statsProvider;

    private JLabel kpiPostulantes, kpiPostulaciones, kpiPromedio, kpiConvenios;
    private JPanel chartsContainer;

    // Boton de exportacion
    private JButton btnExportar;

    public AnalisisPanel(ServicioConsulta consulta) {
        this.consulta = consulta;
        this.statsProvider = new GestorStatsProvider(consulta);
        init();
        refresh();
    }

    private void init() {
        setLayout(new BorderLayout(16, 16));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        setBackground(UIManager.getColor("Panel.background"));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel headerText = new JPanel(new GridLayout(2, 1));
        headerText.setOpaque(false);

        JLabel titulo = new JLabel("Análisis Estadístico");
        titulo.setForeground(UIManager.getColor("Label.foreground"));
        titulo.setFont(getFont().deriveFont(Font.BOLD, 22f));

        JLabel subtitulo = new JLabel("Visión general de postulaciones y rendimiento académico");
        subtitulo.setForeground(UIManager.getColor("Label.disabledForeground"));
        subtitulo.setFont(getFont().deriveFont(Font.PLAIN, 14f));

        headerText.add(titulo);
        headerText.add(subtitulo);

        // Boton Exportar
        btnExportar = new JButton("Exportar a Excel");
        btnExportar.addActionListener(e -> exportarAExcel());
        JPanel headerActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        headerActions.setOpaque(false);
        headerActions.add(btnExportar);

        header.add(headerText, BorderLayout.CENTER);
        header.add(headerActions, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        JPanel kpiPanel = new JPanel(new GridLayout(1, 4, 16, 0));
        kpiPanel.setOpaque(false);
        kpiPanel.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        kpiPostulantes = makeKpiValueLabel();
        kpiPostulaciones = makeKpiValueLabel();
        kpiPromedio = makeKpiValueLabel();
        kpiConvenios = makeKpiValueLabel();

        kpiPanel.add(makeKpiCard("Postulantes", kpiPostulantes));
        kpiPanel.add(makeKpiCard("Postulaciones", kpiPostulaciones));
        kpiPanel.add(makeKpiCard("Promedio Acad.", kpiPromedio));
        kpiPanel.add(makeKpiCard("Convenios", kpiConvenios));

        // graficos/tablas
        chartsContainer = new JPanel();
        chartsContainer.setOpaque(false);
        chartsContainer.setLayout(new BoxLayout(chartsContainer, BoxLayout.Y_AXIS));

        JPanel center = new JPanel(new BorderLayout(0, 16));
        center.setOpaque(false);
        center.add(kpiPanel, BorderLayout.NORTH);
        center.add(chartsContainer, BorderLayout.CENTER);

        add(center, BorderLayout.CENTER);
    }

    public void refresh() {
        // 1) Actualizar KPIs
        kpiPostulantes.setText(String.valueOf(statsProvider.totalPostulantes()));
        kpiPostulaciones.setText(String.valueOf(statsProvider.totalPostulaciones()));
        kpiPromedio.setText(String.format("%.2f", statsProvider.promedioGeneral()));
        kpiConvenios.setText(String.valueOf(statsProvider.totalConvenios()));

        // 2) Reconstruir gráficos/tabla
        chartsContainer.removeAll();
        chartsContainer.add(Box.createVerticalStrut(8));

        chartsContainer.add(makeSectionLabel("Promedio académico por estudiante"));
        chartsContainer.add(makeCard(makePromediosBarChart()));
        chartsContainer.add(Box.createVerticalStrut(16));

        chartsContainer.add(makeSectionLabel("Distribución de postulaciones por convenio (%)"));
        List<StatConvenio> statsConvenios = statsProvider.statsPostulacionesPorConvenio();
        chartsContainer.add(makeCard(makeConveniosPieChartClean(statsConvenios)));
        chartsContainer.add(Box.createVerticalStrut(8));

        chartsContainer.add(makeSectionLabel("Análisis de postulaciones por convenio"));
        chartsContainer.add(makeCard(makeConveniosStatsTable(statsConvenios)));
        chartsContainer.add(Box.createVerticalStrut(8));

        chartsContainer.revalidate();
        chartsContainer.repaint();
    }

    private JLabel makeKpiValueLabel() {
        JLabel lbl = new JLabel("--", SwingConstants.CENTER);
        lbl.setFont(getFont().deriveFont(Font.BOLD, 26f));
        lbl.setForeground(new Color(0x4A95FF));
        return lbl;
    }

    private JLabel makeSectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(UIManager.getColor("Label.foreground"));
        l.setFont(getFont().deriveFont(Font.BOLD, 16f));
        l.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        return l;
    }

    private JPanel makeCard(JComponent inner) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(0x2E2E2E));
        card.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    private JPanel makeKpiCard(String title, JLabel valueLabel) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(0x2E2E2E));
        card.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setFont(getFont().deriveFont(Font.PLAIN, 14f));
        lblTitle.setForeground(new Color(0xCCCCCC));
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(lblTitle, BorderLayout.SOUTH);
        return card;
    }

    // Grafico de barras

    private ChartPanel makePromediosBarChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        consulta.getTodosLosProgramas().forEach(programa ->
                programa.getPostulaciones().forEach(postulacion -> {
                    var estOpt = consulta.buscarEstudiantePorRut(postulacion.getRutEstudiante());
                    Estudiante est = estOpt.orElse(null);
                    if (est != null) {
                        dataset.addValue(est.getPromedio(), "Promedio", est.getNombreCompleto());
                    }
                })
        );

        JFreeChart chart = ChartFactory.createBarChart(null,"Estudiante","Promedio",dataset,PlotOrientation.VERTICAL,false, true, false);

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
        panel.setPreferredSize(new Dimension(900, 360));
        panel.setMouseWheelEnabled(true);
        panel.setOpaque(false);
        return panel;
    }

    // Grafico Pie
    private ChartPanel makeConveniosPieChartClean(List<StatConvenio> stats) {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        for (StatConvenio sc : stats) {
            dataset.setValue(sc.id(), sc.count());
        }

        JFreeChart chart = ChartFactory.createPieChart(null,dataset,false,true,false);

        chart.setBackgroundPaint(UIManager.getColor("Panel.background"));
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setBackgroundPaint(UIManager.getColor("Panel.background"));

        plot.setLabelGenerator(null);
        plot.setSimpleLabels(false);

        Map<String, StatConvenio> idx = stats.stream().collect(Collectors.toMap(StatConvenio::id, s -> s, (a,b)->a, LinkedHashMap::new));

        DecimalFormat pctFmt = new DecimalFormat("0.0'%'");
        plot.setToolTipGenerator((PieToolTipGenerator) (PieDataset ds, Comparable key) -> {
            String id = String.valueOf(key);
            StatConvenio sc = idx.get(id);
            if (sc == null) return id;
            return sc.label() + ": " + pctFmt.format(sc.percent() * 100.0);
        });

        ChartPanel panel = new ChartPanel(chart);
        panel.setPreferredSize(new Dimension(900, 320));
        panel.setMouseWheelEnabled(true);
        panel.setOpaque(false);
        return panel;
    }

    // Tabla de analisis de postulaciones por convenio
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
        table.setShowGrid(false);
        table.setForeground(UIManager.getColor("Label.foreground"));
        table.setBackground(new Color(0x2E2E2E));
        table.setSelectionBackground(new Color(0x3A3A3A));
        table.setSelectionForeground(Color.WHITE);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setBackground(new Color(0x2A2A2A));
        table.getTableHeader().setForeground(new Color(0xDDDDDD));
        table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(Font.BOLD));

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(new Color(0x2E2E2E));
        return sp;
    }

    // Exportacion a excel
    private void exportarAExcel() {
        // Recolectar datos actuales
        long totalPostulantes = statsProvider.totalPostulantes();
        long totalPostulaciones = statsProvider.totalPostulaciones();
        double promedioGeneral = statsProvider.promedioGeneral();
        long totalConvenios = statsProvider.totalConvenios();

        // Promedios por estudiante
        DefaultCategoryDataset dsPromedios = new DefaultCategoryDataset();
        consulta.getTodosLosProgramas().forEach(programa ->
                programa.getPostulaciones().forEach(postulacion -> {
                    var estOpt = consulta.buscarEstudiantePorRut(postulacion.getRutEstudiante());
                    Estudiante est = estOpt.orElse(null);
                    if (est != null) {
                        dsPromedios.addValue(est.getPromedio(), "Promedio", est.getNombreCompleto());
                    }
                })
        );

        // Postulaciones por convenio
        List<StatConvenio> statsConvenios = statsProvider.statsPostulacionesPorConvenio();

        // Elegir archivo destino
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Guardar análisis como Excel");
        chooser.setSelectedFile(new File("analisis_intercambios.xlsx"));
        int resp = chooser.showSaveDialog(this);
        if (resp != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".xlsx")) {
            file = new File(file.getParentFile(), file.getName() + ".xlsx");
        }

        try (Workbook wb = new XSSFWorkbook(); FileOutputStream fos = new FileOutputStream(file)) {

            Sheet shKpi = wb.createSheet("KPIs");
            int r = 0;
            Row h0 = shKpi.createRow(r++);
            h0.createCell(0).setCellValue("Métrica");
            h0.createCell(1).setCellValue("Valor");

            Row r1 = shKpi.createRow(r++);
            r1.createCell(0).setCellValue("Postulantes");
            r1.createCell(1).setCellValue(totalPostulantes);

            Row r2 = shKpi.createRow(r++);
            r2.createCell(0).setCellValue("Postulaciones");
            r2.createCell(1).setCellValue(totalPostulaciones);

            Row r3 = shKpi.createRow(r++);
            r3.createCell(0).setCellValue("Promedio Académico");
            r3.createCell(1).setCellValue(promedioGeneral);

            Row r4 = shKpi.createRow(r++);
            r4.createCell(0).setCellValue("Convenios");
            r4.createCell(1).setCellValue(totalConvenios);

            shKpi.autoSizeColumn(0); shKpi.autoSizeColumn(1);

            Sheet shConv = wb.createSheet("Postulaciones por convenio");
            Row h = shConv.createRow(0);
            h.createCell(0).setCellValue("#");
            h.createCell(1).setCellValue("Convenio");
            h.createCell(2).setCellValue("Postulaciones");
            h.createCell(3).setCellValue("% del total");

            int i = 1;
            for (StatConvenio sc : statsConvenios) {
                Row rr = shConv.createRow(i);
                rr.createCell(0).setCellValue(i);
                rr.createCell(1).setCellValue(sc.label());
                rr.createCell(2).setCellValue(sc.count());
                rr.createCell(3).setCellValue(sc.percent() * 100.0); // valor en %
                i++;
            }
            for (int c = 0; c <= 3; c++) shConv.autoSizeColumn(c);

            Sheet shEst = wb.createSheet("Promedios por estudiante");
            Row he = shEst.createRow(0);
            he.createCell(0).setCellValue("Estudiante");
            he.createCell(1).setCellValue("Promedio");

            int er = 1;
            for (int col = 0; col < dsPromedios.getColumnCount(); col++) {
                String estNombre = (String) dsPromedios.getColumnKey(col);
                Number val = dsPromedios.getValue(0, col); // solo una serie
                Row re = shEst.createRow(er++);
                re.createCell(0).setCellValue(estNombre);
                re.createCell(1).setCellValue(val != null ? val.doubleValue() : 0.0);
            }
            shEst.autoSizeColumn(0); shEst.autoSizeColumn(1);

            // Guardar
            wb.write(fos);
            JOptionPane.showMessageDialog(this, "Excel exportado:\n" + file.getAbsolutePath(), "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al exportar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
