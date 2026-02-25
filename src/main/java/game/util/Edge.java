package game.util;

public class Edge<Vertex, Path> {
    public final Vertex from;
    public final Vertex to;
    public final Path path;

    public Edge(final Vertex from, final Vertex to, final Path path) {
        this.from = from;
        this.to = to;
        this.path = path;
    }

    public Path weight() {
        return path;
    }
}
