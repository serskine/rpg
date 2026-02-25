package game.common;

public enum RoomSize {
    CRAMPED(4),
    ROOMY(16),
    VAST(36);

    public final int numSquares;

    private RoomSize(final int numSquares) {
        this.numSquares = numSquares;
    }
}
