package game.sprite.view;

import game.sprite.Polygon;
import game.sprite.SpriteDataParser;
import game.sprite.SpriteFile;
import game.sprite.SpriteValidationResult;
import game.util.Logger;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Main sprite editor panel with large canvas preview (50%) and editable text editor (50%).
 */
public class SpriteEditorPanel extends JPanel {
    // Font constants
    private static final String FONT_NAME = "Consolas";
    private static final int INITIAL_FONT_SIZE = 14;
    private static final int MIN_FONT_SIZE = 8;
    private static final int MAX_FONT_SIZE = 32;
    
    private JTextPane textEditor;
    private SpriteCanvasPanel canvasPanel;
    private JLabel statusLabel;
    private JButton loadButton;
    private JButton saveButton;
    private JButton newButton;
    private JButton increaseFontButton;
    private JButton decreaseFontButton;
    private JLabel fontSizeLabel;
    private JCheckBox saturatedColorsCheckbox;
    private JComboBox<String> spriteFileSelector;
    
    private SpriteFile currentSpriteFile;
    private List<Polygon> currentPolygons = new ArrayList<>();
    private File currentFile;
    private File lastOpenedDirectory;  // Remember last directory used
    private boolean isTextEditorUpdating = false;
    private boolean isCanvasUpdating = false;
    private int currentFontSize = INITIAL_FONT_SIZE;
    private int selectedPolygonIndex = -1;  // Track which polygon is selected
    
    public SpriteEditorPanel() {
        this(null);
    }

     public SpriteEditorPanel(String workingDirectory) {
         setLayout(new BorderLayout());
         
         // Set working directory if provided
         if (workingDirectory != null && !workingDirectory.isEmpty()) {
             lastOpenedDirectory = new File(workingDirectory);
             if (!lastOpenedDirectory.exists()) {
                 Logger.warn("Working directory does not exist: " + workingDirectory);
                 lastOpenedDirectory = null;
             }
         }
         
         // Set larger fonts for UI components
         UIManager.put("Button.font", new Font("Dialog", Font.PLAIN, 13));
         UIManager.put("Label.font", new Font("Dialog", Font.PLAIN, 13));
         UIManager.put("Menu.font", new Font("Dialog", Font.PLAIN, 13));
         
         // Toolbar
         final JPanel toolbar = createToolbar();
         add(toolbar, BorderLayout.NORTH);
         
         // Main content: split pane with text editor (left 50%) and canvas (right 50%)
         textEditor = createTextEditor();
         
         // Wrap text editor with scrollbars
         final JScrollPane textScrollPane = new JScrollPane(textEditor);
         textScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
         textScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
         
         final JPanel textWrapper = new JPanel(new BorderLayout());
         textWrapper.add(textScrollPane, BorderLayout.CENTER);
         textWrapper.setBorder(new CompoundBorder(
             new LineBorder(Color.BLACK, 1),
             new EmptyBorder(5, 5, 5, 5)
         ));
         
         canvasPanel = new SpriteCanvasPanel();
         
         final JPanel canvasWrapper = new JPanel(new BorderLayout());
         canvasWrapper.add(canvasPanel, BorderLayout.CENTER);
         canvasWrapper.setBorder(new CompoundBorder(
             new LineBorder(Color.GREEN, 3),
             new EmptyBorder(5, 5, 5, 5)
         ));
         
         final JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, textWrapper, canvasWrapper);
         mainSplit.setDividerLocation(0.5);  // 50/50 split
         mainSplit.setResizeWeight(0.5);     // Grow evenly
         add(mainSplit, BorderLayout.CENTER);
         
         // Bottom: Status bar
         statusLabel = new JLabel("Ready");
         statusLabel.setFont(new Font("Dialog", Font.PLAIN, 13));
         statusLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
         add(statusLabel, BorderLayout.SOUTH);
         
         // Setup interactions
          setupInteractions();
          
          // Defer keyboard shortcuts setup until we're part of a frame
          SwingUtilities.invokeLater(this::setupKeyboardShortcuts);
          
          // Populate sprite file list from working directory
          SwingUtilities.invokeLater(this::refreshSpriteList);
          
          // Initial state
          initializeEmpty();
          
          // Load test.dat by default
          SwingUtilities.invokeLater(this::loadTestDatByDefault);
      }
    
    private JPanel createToolbar() {
        final JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        panel.setBorder(new EmptyBorder(8, 8, 8, 8));
        
        loadButton = new JButton("Load File");
        loadButton.setFont(new Font("Dialog", Font.PLAIN, 13));
        loadButton.addActionListener(e -> onLoadFile());
        panel.add(loadButton);
        
        saveButton = new JButton("Save");
        saveButton.setFont(new Font("Dialog", Font.PLAIN, 13));
        saveButton.addActionListener(e -> onSaveFile());
        panel.add(saveButton);
        
        final JButton exportButton = new JButton("Export");
        exportButton.setFont(new Font("Dialog", Font.PLAIN, 13));
        exportButton.addActionListener(e -> onExportFile());
        panel.add(exportButton);
        
        newButton = new JButton("New");
        newButton.setFont(new Font("Dialog", Font.PLAIN, 13));
        newButton.addActionListener(e -> onNew());
        panel.add(newButton);
        
        final JButton refreshButton = new JButton("Refresh");
        refreshButton.setFont(new Font("Dialog", Font.PLAIN, 13));
        refreshButton.addActionListener(e -> onRefresh());
        panel.add(refreshButton);
        
        panel.add(new JSeparator(SwingConstants.VERTICAL));
        
        // Font size controls
        decreaseFontButton = new JButton("A-");
        decreaseFontButton.setFont(new Font("Dialog", Font.PLAIN, 13));
        decreaseFontButton.setPreferredSize(new Dimension(50, 30));
        decreaseFontButton.addActionListener(e -> decreaseFontSize());
        panel.add(decreaseFontButton);
        
        fontSizeLabel = new JLabel(String.valueOf(currentFontSize));
        fontSizeLabel.setFont(new Font("Dialog", Font.PLAIN, 13));
        fontSizeLabel.setPreferredSize(new Dimension(30, 30));
        fontSizeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(fontSizeLabel);
        
        increaseFontButton = new JButton("A+");
        increaseFontButton.setFont(new Font("Dialog", Font.PLAIN, 13));
        increaseFontButton.setPreferredSize(new Dimension(50, 30));
        increaseFontButton.addActionListener(e -> increaseFontSize());
        panel.add(increaseFontButton);
        
        panel.add(new JSeparator(SwingConstants.VERTICAL));
        
        // Saturated colors toggle
        saturatedColorsCheckbox = new JCheckBox("Saturated Colors");
        saturatedColorsCheckbox.setFont(new Font("Dialog", Font.PLAIN, 13));
        saturatedColorsCheckbox.addActionListener(e -> onSaturatedColorsToggled());
        panel.add(saturatedColorsCheckbox);
        
        panel.add(new JSeparator(SwingConstants.VERTICAL));
        
        // Sprite file selector dropdown
        final JLabel spriteLabel = new JLabel("Open Sprite:");
        spriteLabel.setFont(new Font("Dialog", Font.PLAIN, 13));
        panel.add(spriteLabel);
        
        spriteFileSelector = new JComboBox<>();
        spriteFileSelector.setFont(new Font("Dialog", Font.PLAIN, 13));
        spriteFileSelector.setPreferredSize(new Dimension(250, 30));
        spriteFileSelector.addActionListener(e -> onSpriteFileSelected());
        panel.add(spriteFileSelector);
        
        // Refresh sprite list button
        final JButton refreshSpriteListButton = new JButton("Refresh List");
        refreshSpriteListButton.setFont(new Font("Dialog", Font.PLAIN, 13));
        refreshSpriteListButton.addActionListener(e -> onRefreshSpriteList());
        panel.add(refreshSpriteListButton);
        
        return panel;
    }
    
    private JTextPane createTextEditor() {
        final JTextPane editor = new JTextPane();
        // Use Consolas font with initial size
        editor.setFont(new Font(FONT_NAME, Font.PLAIN, currentFontSize));
        editor.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(final javax.swing.event.DocumentEvent e) {
                onTextEditorChanged();
            }
            
            @Override
            public void removeUpdate(final javax.swing.event.DocumentEvent e) {
                onTextEditorChanged();
            }
            
            @Override
            public void changedUpdate(final javax.swing.event.DocumentEvent e) {
                onTextEditorChanged();
            }
        });
        
        // Add key listener to convert tab key to 4 spaces
        editor.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(final java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_TAB) {
                    e.consume();
                    final int caretPos = editor.getCaretPosition();
                    try {
                        editor.getDocument().insertString(caretPos, "    ", null);
                    } catch (final javax.swing.text.BadLocationException ex) {
                        Logger.error("Failed to insert spaces", ex);
                    }
                }
            }
        });
        
        return editor;
    }
    
    private void setupInteractions() {
        canvasPanel.setOnPolygonsChanged(() -> {
            currentPolygons = canvasPanel.getPolygons();
            updateTextEditorFromCanvas();
        });
    }
    
    private void setupKeyboardShortcuts() {
        final JRootPane rootPane = getRootPane();
        if (rootPane == null) {
            // Not yet added to frame, try again later
            SwingUtilities.invokeLater(this::setupKeyboardShortcuts);
            return;
        }
        
        final KeyStroke undoKey = KeyStroke.getKeyStroke(KeyEvent.VK_Z, java.awt.event.InputEvent.CTRL_DOWN_MASK);
        final KeyStroke redoKey = KeyStroke.getKeyStroke(KeyEvent.VK_Y, java.awt.event.InputEvent.CTRL_DOWN_MASK);
        final KeyStroke increaseFontKey = KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, java.awt.event.InputEvent.CTRL_DOWN_MASK);
        final KeyStroke decreaseFontKey = KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, java.awt.event.InputEvent.CTRL_DOWN_MASK);
        
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(undoKey, "undo");
        rootPane.getActionMap().put("undo", new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                // TODO: Implement undo
            }
        });
        
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(redoKey, "redo");
        rootPane.getActionMap().put("redo", new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                // TODO: Implement redo
            }
        });
        
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(increaseFontKey, "increaseFont");
        rootPane.getActionMap().put("increaseFont", new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                increaseFontSize();
            }
        });
        
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(decreaseFontKey, "decreaseFont");
        rootPane.getActionMap().put("decreaseFont", new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                decreaseFontSize();
            }
        });
    }
    
    private void increaseFontSize() {
        if (currentFontSize < MAX_FONT_SIZE) {
            currentFontSize++;
            updateTextEditorFont();
        }
    }
    
    private void decreaseFontSize() {
        if (currentFontSize > MIN_FONT_SIZE) {
            currentFontSize--;
            updateTextEditorFont();
        }
    }
    
    private void onSaturatedColorsToggled() {
        final boolean useSaturated = saturatedColorsCheckbox.isSelected();
        canvasPanel.setUseSaturatedColors(useSaturated);
    }
    
    private void updateTextEditorFont() {
        textEditor.setFont(new Font(FONT_NAME, Font.PLAIN, currentFontSize));
        fontSizeLabel.setText(String.valueOf(currentFontSize));
    }
    
    private void initializeEmpty() {
        currentPolygons = new ArrayList<>();
        currentSpriteFile = new SpriteFile(
            java.util.Map.of("black", "#000000", "white", "#FFFFFF"),
            new ArrayList<>(currentPolygons)
        );
        currentFile = null;
        
        canvasPanel.setSpriteFile(currentSpriteFile);
        updateTextEditorFromCanvas();
        updateValidationStatus(true, null);
    }
    
    private void onLoadFile() {
        final JFileChooser chooser = new JFileChooser();
        chooser.setPreferredSize(new Dimension(900, 600));  // Make dialog much larger
        
        // Set to last opened directory if available
        if (lastOpenedDirectory != null && lastOpenedDirectory.exists()) {
            chooser.setCurrentDirectory(lastOpenedDirectory);
        }
        
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Data files (*.dat)", "dat"));
        
        final int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            final File file = chooser.getSelectedFile();
            // Remember this directory for future file operations
            lastOpenedDirectory = file.getParentFile();
            loadFile(file);
        }
    }
    
    private void loadFile(final File file) {
        try {
            String content = new String(Files.readAllBytes(file.toPath()));
            // Convert all tabs to exactly 4 spaces
            content = content.replace("\t", "    ");
            
            final SpriteValidationResult validationResult = SpriteDataParser.parse(content);
            
            if (!validationResult.isValid()) {
                updateValidationStatus(false, validationResult.getErrorMessage());
                statusLabel.setText("Error: " + validationResult.getErrorMessage());
                return;
            }
            
            currentSpriteFile = validationResult.getSpriteFile();
            currentPolygons = new ArrayList<>(currentSpriteFile.getPolygons());
            currentFile = file;
            
            isTextEditorUpdating = true;
            textEditor.setText(content);
            isTextEditorUpdating = false;
            
            canvasPanel.setSpriteFile(currentSpriteFile);
            
            updateValidationStatus(true, null);
            statusLabel.setText("Loaded: " + file.getName());
        } catch (final IOException e) {
            updateValidationStatus(false, "Failed to read file: " + e.getMessage());
            statusLabel.setText("Error loading file: " + e.getMessage());
        }
    }
    
    private void onSaveFile() {
        if (currentFile == null) {
            onExportFile();
            return;
        }
        
        try {
            String content = textEditor.getText();
            // Convert all tabs to exactly 4 spaces before saving
            content = content.replace("\t", "    ");
            Files.write(Paths.get(currentFile.toURI()), content.getBytes());
            statusLabel.setText("Saved: " + currentFile.getName());
        } catch (final IOException e) {
            statusLabel.setText("Error saving file: " + e.getMessage());
        }
    }
    
    private void onExportFile() {
        final JFileChooser chooser = new JFileChooser();
        chooser.setPreferredSize(new Dimension(900, 600));  // Make dialog much larger
        
        // Set to last opened directory if available
        if (lastOpenedDirectory != null && lastOpenedDirectory.exists()) {
            chooser.setCurrentDirectory(lastOpenedDirectory);
        }
        
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Data files (*.dat)", "dat"));
        
        final int result = chooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            final File file = chooser.getSelectedFile();
            // Remember this directory for future file operations
            lastOpenedDirectory = file.getParentFile();
            try {
                String content = textEditor.getText();
                // Convert all tabs to exactly 4 spaces before saving
                content = content.replace("\t", "    ");
                Files.write(Paths.get(file.toURI()), content.getBytes());
                currentFile = file;
                statusLabel.setText("Exported: " + file.getName());
            } catch (final IOException e) {
                statusLabel.setText("Error exporting file: " + e.getMessage());
            }
        }
    }
    
    private void onNew() {
        initializeEmpty();
        currentFile = null;
        statusLabel.setText("New sprite file created");
    }
    
    private void onRefresh() {
        // Reload the sprite from the current text editor content
        final String text = textEditor.getText();
        final SpriteValidationResult result = SpriteDataParser.parse(text);
        
        if (!result.isValid()) {
            updateValidationStatus(false, result.getErrorMessage());
            statusLabel.setText("Refresh failed: " + result.getErrorMessage());
            return;
        }
        
        // Valid! Update the canvas with the refreshed data
        currentSpriteFile = result.getSpriteFile();
        currentPolygons = new ArrayList<>(currentSpriteFile.getPolygons());
        
        isCanvasUpdating = true;
        canvasPanel.setSpriteFile(currentSpriteFile);
        isCanvasUpdating = false;
        
        updateValidationStatus(true, null);
        statusLabel.setText("Sprite refreshed from text");
    }
    
    private void onTextEditorChanged() {
        if (isTextEditorUpdating || isCanvasUpdating) {
            return;
        }
        
        final String text = textEditor.getText();
        final SpriteValidationResult result = SpriteDataParser.parse(text);
        
        if (!result.isValid()) {
            updateValidationStatus(false, result.getErrorMessage());
            return;
        }
        
        // Valid! Update the canvas
        currentSpriteFile = result.getSpriteFile();
        currentPolygons = new ArrayList<>(currentSpriteFile.getPolygons());
        
        isCanvasUpdating = true;
        canvasPanel.setSpriteFile(currentSpriteFile);
        isCanvasUpdating = false;
        
        updateValidationStatus(true, null);
    }
    
    /**
     * Called when the caret position changes in the text editor.
     * Detects which polygon the cursor is in and selects it in the canvas.
     */
    
    private void updateTextEditorFromCanvas() {
        if (isCanvasUpdating) {
            return;
        }
        
        isTextEditorUpdating = true;
        currentSpriteFile = new SpriteFile(currentSpriteFile.colors(), new ArrayList<>(currentPolygons));
        textEditor.setText(SpriteDataParser.formatToText(currentSpriteFile));
        isTextEditorUpdating = false;
    }
    
    private void updateValidationStatus(final boolean isValid, final String errorMessage) {
        // Get the main split pane
        JComponent mainSplitComponent = null;
        for (final Component comp : getComponents()) {
            if (comp instanceof JSplitPane) {
                mainSplitComponent = (JComponent) comp;
                break;
            }
        }
        
        if (!(mainSplitComponent instanceof JSplitPane)) {
            return;
        }
        
        final JSplitPane mainSplit = (JSplitPane) mainSplitComponent;
        final JPanel canvasWrapper = (JPanel) mainSplit.getRightComponent();
        
        if (isValid) {
            canvasWrapper.setBorder(new CompoundBorder(
                new LineBorder(new Color(0, 200, 0), 3),
                new EmptyBorder(5, 5, 5, 5)
            ));
        } else {
            canvasWrapper.setBorder(new CompoundBorder(
                new LineBorder(new Color(200, 0, 0), 3),
                new EmptyBorder(5, 5, 5, 5)
            ));
            if (errorMessage != null) {
                statusLabel.setText("Validation error: " + errorMessage);
            }
        }
    }
     
     private int findPolygonEndPosition(final String text, final int startPos) {
         int braceCount = 0;
         
         for (int i = startPos; i < text.length(); i++) {
             final char c = text.charAt(i);
             
             if (c == '{') {
                 braceCount++;
             } else if (c == '}') {
                 braceCount--;
                 if (braceCount == 0) {
                     return i + 1;
                 }
             }
         }
         
         return -1;
     }
     
     private void loadTestDatByDefault() {
         try {
             // Load test.dat from resources
             final String testDatPath = "src/main/resources/features/human/test.dat";
             final File testDatFile = new File(testDatPath);
             
             if (testDatFile.exists()) {
                 loadFile(testDatFile);
                 statusLabel.setText("Loaded default test file: test.dat");
             } else {
                 Logger.warn("Default test.dat file not found at: " + testDatPath);
             }
          } catch (final Exception e) {
              Logger.warn("Failed to load default test.dat: " + e.getMessage());
          }
      }
      
      /**
       * Scan the working directory for .dat sprite files and populate the dropdown.
       */
      private void refreshSpriteList() {
          spriteFileSelector.removeAllItems();
          
          if (lastOpenedDirectory == null || !lastOpenedDirectory.exists()) {
              spriteFileSelector.addItem("(No directory selected)");
              return;
          }
          
          // Scan directory recursively for .dat files
          final List<File> datFiles = new ArrayList<>();
          scanForDatFiles(lastOpenedDirectory, datFiles);
          
          if (datFiles.isEmpty()) {
              spriteFileSelector.addItem("(No .dat files found)");
              return;
          }
          
          // Sort by filename
          datFiles.sort((a, b) -> a.getName().compareTo(b.getName()));
          
          // Add each file to dropdown
          for (final File file : datFiles) {
              spriteFileSelector.addItem(file.getAbsolutePath());
          }
          
          statusLabel.setText("Found " + datFiles.size() + " sprite file(s)");
      }
      
      /**
       * Recursively scan a directory for .dat files.
       */
      private void scanForDatFiles(final File directory, final List<File> results) {
          final File[] files = directory.listFiles();
          if (files == null) {
              return;
          }
          
          for (final File file : files) {
              if (file.isDirectory()) {
                  // Recursively scan subdirectories
                  scanForDatFiles(file, results);
              } else if (file.getName().endsWith(".dat")) {
                  results.add(file);
              }
          }
      }
      
      /**
       * Handle sprite file selection from dropdown.
       */
      private void onSpriteFileSelected() {
          final Object selectedItem = spriteFileSelector.getSelectedItem();
          if (selectedItem == null) {
              return;
          }
          
          final String selectedPath = selectedItem.toString();
          
          // Skip placeholder items
          if (selectedPath.startsWith("(")) {
              return;
          }
          
          final File selectedFile = new File(selectedPath);
          if (!selectedFile.exists()) {
              statusLabel.setText("Selected file does not exist: " + selectedPath);
              return;
          }
          
          loadFile(selectedFile);
      }
      
      /**
       * Refresh the sprite list from the working directory.
       */
      private void onRefreshSpriteList() {
          refreshSpriteList();
      }
}
