package game.view;

import game.rolltable.RollTable;
import game.rolltable.RollTableRecord;
import game.rolltable.RollTableService;
import game.rolltable.RollTableServiceLocator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Panel for displaying and rolling on roll tables.
 * Allows users to select a table from the database and view roll results.
 */
public class RollTablePanel extends JPanel {

    private final RollTableService rollTableService;
    private JComboBox<RollTable> tableComboBox;
    private JTextArea equationDisplay;
    private JButton rollButton;
    private JButton refreshButton;
    private JTextArea resultsDisplay;
    private final JLabel statusLabel;

    public RollTablePanel() {
        this.rollTableService = RollTableServiceLocator.getService();
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setBackground(new Color(240, 240, 240));

        // Top panel: Table selection and roll button
        final JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);

        // Middle panel: Equation display
        final JPanel middlePanel = createMiddlePanel();
        add(middlePanel, BorderLayout.CENTER);

        // Bottom panel: Status
        statusLabel = new JLabel("Ready");
        add(statusLabel, BorderLayout.SOUTH);

        // Load tables on initialization
        loadTables();
    }

    private JPanel createTopPanel() {
        final JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panel.setBackground(new Color(240, 240, 240));

        final JLabel label = new JLabel("Select Table:");
        panel.add(label);

        tableComboBox = new JComboBox<>();
        tableComboBox.setPreferredSize(new Dimension(250, 30));
        tableComboBox.addActionListener(e -> updateEquationDisplay());
        panel.add(tableComboBox);

        rollButton = new JButton("Roll");
        rollButton.setPreferredSize(new Dimension(100, 30));
        rollButton.addActionListener(e -> performRoll());
        panel.add(rollButton);

        refreshButton = new JButton("Refresh Tables");
        refreshButton.setPreferredSize(new Dimension(120, 30));
        refreshButton.addActionListener(e -> loadTables());
        panel.add(refreshButton);

        return panel;
    }

    private JPanel createMiddlePanel() {
        final JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(240, 240, 240));

        // Left side: Equation display
        final JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setBackground(new Color(240, 240, 240));
        leftPanel.setBorder(BorderFactory.createTitledBorder("Equation"));

        equationDisplay = new JTextArea(2, 30);
        equationDisplay.setEditable(false);
        equationDisplay.setLineWrap(true);
        equationDisplay.setWrapStyleWord(true);
        equationDisplay.setBackground(new Color(255, 255, 255));
        equationDisplay.setFont(new Font("Monospaced", Font.PLAIN, 14));
        leftPanel.add(new JScrollPane(equationDisplay), BorderLayout.CENTER);

        // Right side: Results display
        final JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
        rightPanel.setBackground(new Color(240, 240, 240));
        rightPanel.setBorder(BorderFactory.createTitledBorder("Roll Results"));

        resultsDisplay = new JTextArea(10, 40);
        resultsDisplay.setEditable(false);
        resultsDisplay.setLineWrap(true);
        resultsDisplay.setWrapStyleWord(true);
        resultsDisplay.setBackground(new Color(255, 255, 255));
        resultsDisplay.setFont(new Font("SansSerif", Font.PLAIN, 12));
        rightPanel.add(new JScrollPane(resultsDisplay), BorderLayout.CENTER);

        // Split pane
        final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setResizeWeight(0.3);
        panel.add(splitPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Loads all tables from the database and populates the combo box.
     */
    public void loadTables() {
        try {
            tableComboBox.removeAllItems();
            final List<RollTable> tables = rollTableService.getAllTables();
            for (final RollTable table : tables) {
                tableComboBox.addItem(table);
            }
            statusLabel.setText("Loaded " + tables.size() + " table(s)");
        } catch (final Exception e) {
            statusLabel.setText("Error loading tables: " + e.getMessage());
        }
    }

    /**
     * Updates the equation display based on selected table.
     */
    private void updateEquationDisplay() {
        final RollTable selected = (RollTable) tableComboBox.getSelectedItem();
        if (selected != null) {
            equationDisplay.setText(selected.getEquation());
            resultsDisplay.setText("");
        }
    }

    /**
     * Performs a roll on the selected table.
     */
    private void performRoll() {
        final RollTable selected = (RollTable) tableComboBox.getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Please select a table");
            return;
        }

        try {
            final List<RollTableRecord> results = rollTableService.rollOnTable(selected.getId());
            displayResults(results);
            statusLabel.setText("Roll completed");
        } catch (final Exception e) {
            statusLabel.setText("Error rolling: " + e.getMessage());
            resultsDisplay.setText("Error: " + e.getMessage());
        }
    }

    /**
     * Displays the roll results in a formatted way.
     */
    private void displayResults(final List<RollTableRecord> results) {
        final StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════\n");
        sb.append("Roll Results\n");
        sb.append("═══════════════════════════════════════\n\n");

        for (int i = 0; i < results.size(); i++) {
            final RollTableRecord record = results.get(i);
            sb.append("Step ").append(i + 1).append(": ").append(record.getTitle());
            if (record.getWeight() > 1) {
                sb.append(" (weight: ").append(record.getWeight()).append(")");
            }
            sb.append("\n");
            if (record.getReferencedTableId().isPresent()) {
                sb.append("  └─ References Table #").append(record.getReferencedTableId().get()).append("\n");
            }
            sb.append("\n");
        }

        sb.append("═══════════════════════════════════════\n");
        resultsDisplay.setText(sb.toString());
        resultsDisplay.setCaretPosition(0);
    }
}
