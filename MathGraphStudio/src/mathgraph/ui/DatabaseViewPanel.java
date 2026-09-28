package mathgraph.ui;

import mathgraph.model.CalculatedDataset;
import mathgraph.model.FunctionDataPoint;
import mathgraph.model.PointType;
import mathgraph.theme.Theme;
import mathgraph.theme.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class DatabaseViewPanel extends JPanel implements ThemeManager.ThemeChangeListener {
    private CalculatedDataset dataset;
    private List<FunctionDataPoint> displayedRows;

    private JTable table;
    private DatasetTableModel tableModel;
    private JTextField queryField;
    private JLabel statusLabel;
    private JLabel statsLabel;

    public DatabaseViewPanel() {
        setLayout(new BorderLayout(0, 10));
        setBorder(new EmptyBorder(12, 12, 12, 12));
        ThemeManager.getInstance().addListener(this);

        initUI();
        applyTheme(ThemeManager.getInstance().getCurrentTheme());
    }

    private void initUI() {
        // Top Toolbar: SQL query bar & quick filters
        JPanel topPanel = new JPanel(new BorderLayout(8, 8));
        topPanel.setOpaque(false);

        // Header line
        JPanel headerLine = new JPanel(new BorderLayout());
        headerLine.setOpaque(false);
        JLabel titleLabel = new JLabel("Explorador de Base de Datos Matematica (In-Memory SQL Studio)");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statusLabel = new JLabel("Tabla: function_dataset | Estado: LISTO");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        headerLine.add(titleLabel, BorderLayout.WEST);
        headerLine.add(statusLabel, BorderLayout.EAST);
        topPanel.add(headerLine, BorderLayout.NORTH);

        // Query Bar
        JPanel queryBar = new JPanel(new BorderLayout(8, 0));
        queryBar.setOpaque(false);

        JLabel sqlPrompt = new JLabel("SELECT * FROM points WHERE:");
        sqlPrompt.setFont(new Font("Consolas", Font.BOLD, 13));

        queryField = new JTextField();
        queryField.setFont(new Font("Consolas", Font.PLAIN, 13));
        queryField.putClientProperty("JTextField.placeholderText", "Escribe condicion (ej: y > 0, x < 2, root, max, min, critical)...");
        queryField.addActionListener(e -> applyFilter(queryField.getText()));

        JButton runQueryBtn = new JButton("Ejecutar Filtro");
        runQueryBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        runQueryBtn.addActionListener(e -> applyFilter(queryField.getText()));

        JButton clearBtn = new JButton("Limpiar");
        clearBtn.addActionListener(e -> {
            queryField.setText("");
            applyFilter("");
        });

        queryBar.add(sqlPrompt, BorderLayout.WEST);
        queryBar.add(queryField, BorderLayout.CENTER);

        JPanel queryBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        queryBtns.setOpaque(false);
        queryBtns.add(runQueryBtn);
        queryBtns.add(clearBtn);
        queryBar.add(queryBtns, BorderLayout.EAST);

        // Quick Filter Buttons
        JPanel filterPills = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        filterPills.setOpaque(false);

        filterPills.add(createPillButton("Todos", ""));
        filterPills.add(createPillButton("Raices [f(x)=0]", "root"));
        filterPills.add(createPillButton("Maximos", "max"));
        filterPills.add(createPillButton("Minimos", "min"));
        filterPills.add(createPillButton("y > 0", "y > 0"));
        filterPills.add(createPillButton("y < 0", "y < 0"));
        filterPills.add(createPillButton("Puntos Criticos", "critical"));

        JPanel queryGroup = new JPanel(new BorderLayout(0, 6));
        queryGroup.setOpaque(false);
        queryGroup.add(queryBar, BorderLayout.NORTH);
        queryGroup.add(filterPills, BorderLayout.SOUTH);

        topPanel.add(queryGroup, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // Center: JTable
        tableModel = new DatasetTableModel();
        table = new JTable(tableModel);
        table.setRowHeight(26);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowGrid(true);
        table.setFillsViewportHeight(true);

        // Custom Cell Renderers
        table.setDefaultRenderer(Object.class, new ModernTableCellRenderer());

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(ThemeManager.getInstance().getCurrentTheme().border));
        add(scrollPane, BorderLayout.CENTER);

        // Bottom Panel: Stats & Export Actions
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.setOpaque(false);

        statsLabel = new JLabel("Filas: 0 | Dominio: N/A | Rango: N/A | Area Total: 0.0000");
        statsLabel.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));
        bottomPanel.add(statsLabel, BorderLayout.WEST);

        JPanel exportBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        exportBtns.setOpaque(false);

        JButton exportSqlBtn = new JButton("Exportar SQL");
        exportSqlBtn.addActionListener(e -> exportToFile("sql"));

        JButton exportCsvBtn = new JButton("Exportar CSV");
        exportCsvBtn.addActionListener(e -> exportToFile("csv"));

        JButton exportJsonBtn = new JButton("Exportar JSON");
        exportJsonBtn.addActionListener(e -> exportToFile("json"));

        JButton copyBtn = new JButton("Copiar Tabla");
        copyBtn.addActionListener(e -> copyTableToClipboard());

        exportBtns.add(exportSqlBtn);
        exportBtns.add(exportCsvBtn);
        exportBtns.add(exportJsonBtn);
        exportBtns.add(copyBtn);

        bottomPanel.add(exportBtns, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JButton createPillButton(String text, String filterQuery) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btn.setFocusPainted(false);
        btn.addActionListener(e -> {
            queryField.setText(filterQuery);
            applyFilter(filterQuery);
        });
        return btn;
    }

    public void setDataset(CalculatedDataset dataset) {
        this.dataset = dataset;
        if (dataset != null) {
            this.displayedRows = dataset.getAllPoints();
            tableModel.fireTableDataChanged();
            updateStats();
        }
    }

    private void applyFilter(String query) {
        if (dataset == null) return;
        displayedRows = dataset.filter(query);
        tableModel.fireTableDataChanged();
        statusLabel.setText(String.format("Tabla: function_dataset | Coincidencias: %d de %d registros", displayedRows.size(), dataset.size()));
    }

    private void updateStats() {
        if (dataset == null) return;
        statsLabel.setText(String.format(Locale.US,
                "📊 Total: %d filas | Dom: [%.2f, %.2f] | Ran: [%.2f, %.2f] | Raíces: %d | Extremos: %d | Área ∫: %.4f | Promedio ȳ: %.4f",
                dataset.size(), dataset.getMinX(), dataset.getMaxX(), dataset.getMinY(), dataset.getMaxY(),
                dataset.getRootCount(), dataset.getMaxCount() + dataset.getMinCount(),
                dataset.getTotalIntegral(), dataset.getAvgY()));
    }

    private void exportToFile(String format) {
        if (dataset == null) {
            JOptionPane.showMessageDialog(this, "No hay datos para exportar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("dataset_math." + format));
        int res = fc.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            try (FileWriter fw = new FileWriter(f)) {
                if (format.equals("sql")) fw.write(dataset.exportToSQL());
                else if (format.equals("csv")) fw.write(dataset.exportToCSV());
                else if (format.equals("json")) fw.write(dataset.exportToJSON());
                JOptionPane.showMessageDialog(this, "Datos exportados con éxito a:\n" + f.getAbsolutePath(), "Exportación Exitosa", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error al exportar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void copyTableToClipboard() {
        if (dataset == null) return;
        String csv = dataset.exportToCSV();
        StringSelection sel = new StringSelection(csv);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, null);
        JOptionPane.showMessageDialog(this, "Datos copiados al portapapeles en formato CSV.", "Copiado", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void onThemeChanged(Theme newTheme) {
        applyTheme(newTheme);
    }

    private void applyTheme(Theme theme) {
        setBackground(theme.bgSidebar);
        statusLabel.setForeground(theme.textSecondary);
        statsLabel.setForeground(theme.textPrimary);

        queryField.setBackground(theme.bgInput);
        queryField.setForeground(theme.textPrimary);
        queryField.setCaretColor(theme.textPrimary);
        queryField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(theme.border),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)
        ));

        table.setBackground(theme.bgCard);
        table.setForeground(theme.textPrimary);
        table.setGridColor(theme.gridMinor);
        table.setSelectionBackground(theme.tableSelection);
        table.setSelectionForeground(theme.textPrimary);

        JTableHeader header = table.getTableHeader();
        header.setBackground(theme.tableHeaderBg);
        header.setForeground(theme.textAccent);
        header.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));

        repaint();
    }

    // Table Model
    private class DatasetTableModel extends AbstractTableModel {
        private final String[] columns = {
                "ID", "X", "f(X) [Y]", "f'(X) [Derivada]", "f''(X) [2da Deriv.]", "∫ f(x)dx [Integral]", "Ángulo (°)", "Tipo de Punto", "Metadatos"
        };

        @Override
        public int getRowCount() {
            return displayedRows != null ? displayedRows.size() : 0;
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int column) {
            return columns[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (displayedRows == null || rowIndex >= displayedRows.size()) return "";
            FunctionDataPoint p = displayedRows.get(rowIndex);
            switch (columnIndex) {
                case 0: return p.getId();
                case 1: return String.format(Locale.US, "%.5f", p.getX());
                case 2: return String.format(Locale.US, "%.5f", p.getY());
                case 3: return String.format(Locale.US, "%.5f", p.getDerivative());
                case 4: return String.format(Locale.US, "%.5f", p.getSecondDerivative());
                case 5: return String.format(Locale.US, "%.5f", p.getCumulativeIntegral());
                case 6: return String.format(Locale.US, "%.1f°", p.getSlopeAngleDeg());
                case 7: return p.getPointType();
                case 8: return p.getMetadata();
                default: return "";
            }
        }
    }

    // Modern Table Cell Renderer with Badges
    private class ModernTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            Theme theme = ThemeManager.getInstance().getCurrentTheme();

            c.setOpaque(true);
            c.setFont(new Font("Consolas", Font.PLAIN, 12));
            c.setHorizontalAlignment(column == 7 || column == 8 ? SwingConstants.LEFT : SwingConstants.RIGHT);

            if (!isSelected) {
                if (row % 2 == 0) {
                    c.setBackground(theme.tableRowEven);
                } else {
                    c.setBackground(theme.tableRowOdd);
                }
                c.setForeground(theme.textPrimary);
            }

            // Special Point Type badge styling
            if (column == 7 && value instanceof PointType) {
                PointType type = (PointType) value;
                c.setText(" " + type.getDisplayName() + " ");
                if (!isSelected) {
                    switch (type) {
                        case ROOT:
                            c.setForeground(theme.rootBadge);
                            c.setFont(new Font("Segoe UI", Font.BOLD, 12));
                            break;
                        case LOCAL_MAX:
                            c.setForeground(theme.maxBadge);
                            c.setFont(new Font("Segoe UI", Font.BOLD, 12));
                            break;
                        case LOCAL_MIN:
                            c.setForeground(theme.minBadge);
                            c.setFont(new Font("Segoe UI", Font.BOLD, 12));
                            break;
                        default:
                            c.setForeground(theme.textSecondary);
                            break;
                    }
                }
            }

            return c;
        }
    }
}
