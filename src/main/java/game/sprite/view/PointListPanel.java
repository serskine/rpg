package game.sprite.view;

import game.sprite.PathPoint;
import game.sprite.PathSegmentType;
import game.sprite.Polygon;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;
import java.util.List;

/**
 * Panel for displaying and editing the points of the selected polygon.
 */
public class PointListPanel extends JPanel {
    private JTable pointTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> polygonSelector;
    private JButton addPointButton;
    private JButton removePointButton;
    private JComboBox<String> fillColorSelector;
    private JComboBox<String> lineColorSelector;
    private JButton fillColorPickerButton;
    private JButton lineColorPickerButton;
    
    private List<Polygon> polygons;
    private int selectedPolygonIndex = -1;
    private Runnable onPointsChanged;
    private Runnable onColorChanged;
    
    public PointListPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Top panel: polygon selector and color controls
        final JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);
        
        // Middle panel: point table
        final JPanel tablePanel = createTablePanel();
        add(tablePanel, BorderLayout.CENTER);
        
        // Bottom panel: action buttons
        final JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createTopPanel() {
        final JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT));
        
        panel.add(new JLabel("Polygon:"));
        polygonSelector = new JComboBox<>();
        polygonSelector.addActionListener(e -> onPolygonSelected());
        panel.add(polygonSelector);
        
        panel.add(Box.createHorizontalStrut(20));
        
        panel.add(new JLabel("Fill:"));
        fillColorSelector = new JComboBox<>();
        fillColorSelector.addActionListener(e -> onFillColorChanged());
        panel.add(fillColorSelector);
        
        fillColorPickerButton = new JButton("...");
        fillColorPickerButton.setPreferredSize(new Dimension(40, 25));
        fillColorPickerButton.addActionListener(e -> onFillColorPickerClicked());
        panel.add(fillColorPickerButton);
        
        panel.add(Box.createHorizontalStrut(10));
        
        panel.add(new JLabel("Line:"));
        lineColorSelector = new JComboBox<>();
        lineColorSelector.addActionListener(e -> onLineColorChanged());
        panel.add(lineColorSelector);
        
        lineColorPickerButton = new JButton("...");
        lineColorPickerButton.setPreferredSize(new Dimension(40, 25));
        lineColorPickerButton.addActionListener(e -> onLineColorPickerClicked());
        panel.add(lineColorPickerButton);
        
        return panel;
    }
    
    private JPanel createTablePanel() {
        final JPanel panel = new JPanel(new BorderLayout());
        
        tableModel = new DefaultTableModel(new String[]{"Index", "X", "Y"}, 0) {
            @Override
            public boolean isCellEditable(final int row, final int column) {
                return column > 0;  // Only X and Y are editable
            }
        };
        
        pointTable = new JTable(tableModel);
        pointTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        pointTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(final ListSelectionEvent e) {
                // Can add logic here if needed
            }
        });
        
        final JScrollPane scrollPane = new JScrollPane(pointTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createBottomPanel() {
        final JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT));
        
        addPointButton = new JButton("Add Point");
        addPointButton.addActionListener(e -> onAddPoint());
        panel.add(addPointButton);
        
        removePointButton = new JButton("Remove Point");
        removePointButton.addActionListener(e -> onRemovePoint());
        panel.add(removePointButton);
        
        return panel;
    }
    
    /**
     * Set the polygons list and update the display.
     */
    public void setPolygons(final List<Polygon> polygons) {
        this.polygons = polygons;
        updatePolygonSelector();
    }
    
    /**
     * Set the selected polygon and update the point table.
     */
    public void setSelectedPolygonIndex(final int index) {
        this.selectedPolygonIndex = index;
        
        if (polygonSelector != null && index >= 0 && index < polygonSelector.getItemCount()) {
            polygonSelector.setSelectedIndex(index);
        }
        
        updatePointTable();
        updateColorSelectors();
    }
    
    /**
     * Set the color names available for selection.
     */
    public void setAvailableColors(final List<String> colorNames) {
        fillColorSelector.removeAllItems();
        lineColorSelector.removeAllItems();
        
        fillColorSelector.addItem("(none)");
        lineColorSelector.addItem("(none)");
        
        for (final String colorName : colorNames) {
            fillColorSelector.addItem(colorName);
            lineColorSelector.addItem(colorName);
        }
    }
    
    /**
     * Set callback for when points change.
     */
    public void setOnPointsChanged(final Runnable callback) {
        this.onPointsChanged = callback;
    }
    
    /**
     * Set callback for when colors change.
     */
    public void setOnColorChanged(final Runnable callback) {
        this.onColorChanged = callback;
    }
    
    private void updatePolygonSelector() {
        polygonSelector.removeAllItems();
        if (polygons != null) {
            for (int i = 0; i < polygons.size(); i++) {
                polygonSelector.addItem("Polygon " + (i + 1));
            }
        }
    }
    
    private void updatePointTable() {
        tableModel.setRowCount(0);
        
         if (selectedPolygonIndex < 0 || selectedPolygonIndex >= polygons.size()) {
             return;
         }
         
         final Polygon polygon = polygons.get(selectedPolygonIndex);
         for (int i = 0; i < polygon.path().size(); i++) {
             final PathPoint pathPoint = polygon.path().get(i);
             tableModel.addRow(new Object[]{
                 i,
                 (int)pathPoint.x(),
                 (int)pathPoint.y()
             });
         }
    }
    
    private void updateColorSelectors() {
        if (selectedPolygonIndex < 0 || selectedPolygonIndex >= polygons.size()) {
            return;
        }
        
        final Polygon polygon = polygons.get(selectedPolygonIndex);
        
        final String fillColor = polygon.fillColor();
        fillColorSelector.setSelectedItem(fillColor != null ? fillColor : "(none)");
        
        final String lineColor = polygon.lineColor();
        lineColorSelector.setSelectedItem(lineColor != null ? lineColor : "(none)");
    }
    
    private void onPolygonSelected() {
        setSelectedPolygonIndex(polygonSelector.getSelectedIndex());
    }
    
     private void onAddPoint() {
         if (selectedPolygonIndex < 0 || selectedPolygonIndex >= polygons.size()) {
             return;
         }
         
         final Polygon polygon = polygons.get(selectedPolygonIndex);
         final List<PathPoint> newPath = new java.util.ArrayList<>(polygon.path());
         // New points default to STRAIGHT segment type
         newPath.add(new PathPoint(PathSegmentType.STRAIGHT, 0, 0));
         polygons.set(selectedPolygonIndex, polygon.withPath(newPath));
         
         updatePointTable();
         if (onPointsChanged != null) {
             onPointsChanged.run();
         }
     }
    
     private void onRemovePoint() {
         final int selectedRow = pointTable.getSelectedRow();
         if (selectedRow < 0 || selectedPolygonIndex < 0 || selectedPolygonIndex >= polygons.size()) {
             return;
         }
         
         final Polygon polygon = polygons.get(selectedPolygonIndex);
         final List<PathPoint> newPath = new java.util.ArrayList<>(polygon.path());
         if (selectedRow < newPath.size()) {
             newPath.remove(selectedRow);
             polygons.set(selectedPolygonIndex, polygon.withPath(newPath));
             updatePointTable();
             if (onPointsChanged != null) {
                 onPointsChanged.run();
             }
         }
     }
    
    private void onFillColorChanged() {
        if (selectedPolygonIndex < 0 || selectedPolygonIndex >= polygons.size()) {
            return;
        }
        
        final String selected = (String)fillColorSelector.getSelectedItem();
        final String colorName = "(none)".equals(selected) ? null : selected;
        
        final Polygon polygon = polygons.get(selectedPolygonIndex);
        polygons.set(selectedPolygonIndex, polygon.withFillColor(colorName));
        
        if (onColorChanged != null) {
            onColorChanged.run();
        }
    }
    
    private void onLineColorChanged() {
        if (selectedPolygonIndex < 0 || selectedPolygonIndex >= polygons.size()) {
            return;
        }
        
        final String selected = (String)lineColorSelector.getSelectedItem();
        final String colorName = "(none)".equals(selected) ? null : selected;
        
        final Polygon polygon = polygons.get(selectedPolygonIndex);
        polygons.set(selectedPolygonIndex, polygon.withLineColor(colorName));
        
        if (onColorChanged != null) {
            onColorChanged.run();
        }
    }
    
    private void onFillColorPickerClicked() {
        // TODO: Implement color picker dialog
        JOptionPane.showMessageDialog(this, "Color picker not yet implemented");
    }
    
    private void onLineColorPickerClicked() {
        // TODO: Implement color picker dialog
        JOptionPane.showMessageDialog(this, "Color picker not yet implemented");
    }
}
