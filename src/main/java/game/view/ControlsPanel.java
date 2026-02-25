package game.view;

import javax.swing.*;
import java.awt.*;
import java.util.function.BiConsumer;

public class ControlsPanel extends JPanel {

    private final JSpinner partyLevelSpinner;
    private final JSpinner partySizeSpinner;
    private final JButton generateButton;
    private BiConsumer<Integer, Integer> onGenerateListener;

    public ControlsPanel() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Party Level
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JLabel("Average Party Level:"), gbc);

        gbc.gridx = 1;
        partyLevelSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 20, 1));
        add(partyLevelSpinner, gbc);

        // Party Size
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(new JLabel("Average Party Size:"), gbc);

        gbc.gridx = 1;
        partySizeSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 20, 1));
        add(partySizeSpinner, gbc);

        // Generate Button
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        generateButton = new JButton("Generate World");
        add(generateButton, gbc);

        generateButton.addActionListener(e -> {
            if (onGenerateListener != null) {
                int level = (Integer) partyLevelSpinner.getValue();
                int size = (Integer) partySizeSpinner.getValue();
                onGenerateListener.accept(size, level);
            }
        });
    }

    public void setOnGenerateListener(BiConsumer<Integer, Integer> onGenerateListener) {
        this.onGenerateListener = onGenerateListener;
    }

    public void setDefaults(int size, int level) {
        partySizeSpinner.setValue(size);
        partyLevelSpinner.setValue(level);
    }
}
