package game.rolltable;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Service locator for accessing the RollTableService in GUI mode.
 * Initializes a minimal Spring context just to load the persistence layer.
 */
@SpringBootApplication
public class RollTableServiceLocator {
    private static ConfigurableApplicationContext context;
    private static RollTableService service;

    /**
     * Initializes the Spring context and returns the RollTableService.
     * This should be called once at application startup.
     */
    public static RollTableService getService() {
        if (service == null) {
            initializeContext();
        }
        return service;
    }

    private static synchronized void initializeContext() {
        if (context != null) {
            return;
        }

        try {
            // Create a minimal Spring application context with just the persistence layer
            SpringApplication app = new SpringApplication(RollTableServiceLocator.class);
            app.setWebApplicationType(WebApplicationType.NONE);
            context = app.run();
            service = context.getBean(RollTableService.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize RollTableService", e);
        }
    }

    public static void shutdown() {
        if (context != null) {
            context.close();
            context = null;
            service = null;
        }
    }
}
