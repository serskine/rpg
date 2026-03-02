package game;

import game.util.Logger;
import game.view.WorldView;
import game.web.WebServer;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Check for --web flag
        boolean webMode = false;
        int partySize = 5;
        int partyLevel = 1;

        // Parse arguments
        int argIndex = 0;
        while (argIndex < args.length) {
            String arg = args[argIndex];
            if ("--web".equals(arg)) {
                webMode = true;
                argIndex++;
            } else {
                // Try to parse as party size and level
                try {
                    partySize = Integer.parseInt(arg);
                    if (argIndex + 1 < args.length && !args[argIndex + 1].equals("--web")) {
                        partyLevel = Integer.parseInt(args[argIndex + 1]);
                        argIndex += 2;
                    } else {
                        argIndex++;
                    }
                } catch (NumberFormatException e) {
                    Logger.warn("Invalid argument: " + arg);
                    argIndex++;
                }
            }
        }

        final int finalPartySize = partySize;
        final int finalPartyLevel = partyLevel;

        if (webMode) {
            // Start web server
            Logger.info("Starting RPG application in web mode...");
            WebServer.start(finalPartySize, finalPartyLevel);
        } else {
            // Start Swing GUI
            Logger.info("Starting RPG application in GUI mode...");
            SwingUtilities.invokeLater(() -> {
                WorldView view = new WorldView(finalPartySize, finalPartyLevel);
                view.setVisible(true);
            });
        }
    }
}
