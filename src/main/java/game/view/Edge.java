package game.view;

import java.awt.*;

public enum Edge {
    EMPTY(null, null, null),
    WALL(new BasicStroke(2), Color.BLACK, null),
    DOOR_OPEN(new BasicStroke(2), Color.BLACK, true),
    DOOR_CLOSED(new BasicStroke(2), Color.BLACK, false),
    ;

    public final Stroke stroke;
    public final Color color;
    public final Boolean openDoor;

    private Edge(BasicStroke stroke, Color color, Boolean openDoor) {
        this.stroke = stroke;
        this.color = color;
        this.openDoor = openDoor;
    }


}
