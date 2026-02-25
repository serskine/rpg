package game;

import game.util.Logger;
import game.view.WorldView;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        int partySize = 5;
        int partyLevel = 1;

        if (args.length == 2) {
            try {
                partySize = Integer.parseInt(args[0]);
                partyLevel = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                Logger.warn("Invalid arguments. Using defaults.");
            }
        } else if (args.length > 0) {
             Logger.warn("Expected 2 arguments: <party size>, <avg party level>. Using defaults.");
        }

        final int finalPartySize = partySize;
        final int finalPartyLevel = partyLevel;

        SwingUtilities.invokeLater(() -> {
            WorldView view = new WorldView(finalPartySize, finalPartyLevel);
            view.setVisible(true);
        });
    }
}
