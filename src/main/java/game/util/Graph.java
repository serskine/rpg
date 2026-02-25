package game.util;

import java.util.*;
import java.util.stream.Stream;

public class Graph<Vertex, Path> {
    protected final Map<Vertex, Map<Vertex, Path>> childMap = new HashMap<>();
    protected final Map<Vertex, Map<Vertex, Path>> parentMap = new HashMap<>();
    protected final Set<Vertex> allVertex = new HashSet<>();

    public final Optional<Path> getPath(Vertex from, Vertex to) {
        return Optional.ofNullable(childMap.getOrDefault(from, Collections.emptyMap()).get(to));
    }

    public final void setPath(final Vertex from, final Vertex to, final Path path) {
        childMap.computeIfAbsent(from, k -> new HashMap<>()).put(to, path);
        parentMap.computeIfAbsent(to, k -> new HashMap<>()).put(from, path);
        allVertex.add(from);
        allVertex.add(to);
    }

    public final void removePath(final Vertex from, final Vertex to) {
        childMap.getOrDefault(from, Collections.emptyMap()).remove(to);
        parentMap.getOrDefault(to, Collections.emptyMap()).remove(from);
    }

    public final void setPathsBetween(final Vertex from, final Vertex to, final Path path) {
        setPath(from, to, path);
        setPath(to, from, path);
    }

    public final void removePathsBetween(final Vertex from, final Vertex to) {
        removePath(from, to);
        removePath(to, from);
    }

    public final Map<Vertex, Path> getChildrenOf(Vertex vertex) {
        return Collections.unmodifiableMap(childMap.getOrDefault(vertex, Collections.emptyMap()));
    }

    public final Map<Vertex, Path> getParentsOf(Vertex vertex) {
        return Collections.unmodifiableMap(parentMap.getOrDefault(vertex, Collections.emptyMap()));
    }

    public final Set<Vertex> getAllVertex() {
        return Collections.unmodifiableSet(allVertex);
    }

    public final Stream<Edge<Vertex, Path>> getAllEdgesStream() {
        return childMap.entrySet().stream()
                .flatMap(entry -> entry.getValue().entrySet().stream()
                        .map(childEntry -> new Edge<>(entry.getKey(), childEntry.getKey(), childEntry.getValue())));
    }

    public final List<Edge<Vertex, Path>> getAllEdges() {
        return getAllEdgesStream().toList();
    }

    public final void clear() {
        childMap.clear();
        parentMap.clear();
        allVertex.clear();
    }

    public final void removeChildrenOf(final Vertex vertex) {
        final Map<Vertex, Path> children = childMap.remove(vertex);
        if (children != null) {
            for (final Vertex child : children.keySet()) {
                parentMap.getOrDefault(child, Collections.emptyMap()).remove(vertex);
            }
        }
    }

    public final void removeParentsOf(final Vertex vertex) {
        final Map<Vertex, Path> parents = parentMap.remove(vertex);
        if (parents != null) {
            for (final Vertex parent : parents.keySet()) {
                childMap.getOrDefault(parent, Collections.emptyMap()).remove(vertex);
            }
        }
    }

    public final void isolateVertex(final Vertex vertex) {
        removeChildrenOf(vertex);
        removeParentsOf(vertex);
    }

    public final void removeVertex(final Vertex vertex) {
        isolateVertex(vertex);
        allVertex.remove(vertex);
    }



}
