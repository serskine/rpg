package game.web;

import game.util.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Web server for the RPG application.
 * Starts a Spring Boot application when --web flag is provided.
 */
@SpringBootApplication
public class WebServer {
    private static ConfigurableApplicationContext applicationContext;
    private static int partySize = 5;
    private static int partyLevel = 1;

    public static void start(final int initialPartySize, final int initialPartyLevel) {
        partySize = initialPartySize;
        partyLevel = initialPartyLevel;

        try {
            SpringApplication app = new SpringApplication(WebServer.class);
            applicationContext = app.run();
            Logger.info("Web server started on http://localhost:8080");
        } catch (Exception e) {
            Logger.error("Failed to start web server", e);
        }
    }

    public static int getPartySize() {
        return partySize;
    }

    public static int getPartyLevel() {
        return partyLevel;
    }

    public static void setPartySize(final int size) {
        partySize = size;
    }

    public static void setPartyLevel(final int level) {
        partyLevel = level;
    }

    public static void stop() {
        if (applicationContext != null) {
            applicationContext.close();
        }
    }
}

