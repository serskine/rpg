package game.view.features;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public interface Feature {
    default void render(Graphics2D g, Rectangle2D r) {
        
    }
}
