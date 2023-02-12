package com.example.graph_editor.plugins;

import graph_editor.extensions.Plugin;
import graph_editor.graph.VersionStack;
import graph_editor.graph.Vertex;
import graph_editor.properties.PropertySupportingGraph;
import graph_editor.visual.GraphVisualization;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TurboMatching implements Plugin, Serializable {
    private static final String releasePath = "marketplace/";
    private static final String pluginDirectory = "bipartite_matching";
    public static void main(String[] args) {
        try {
            File releaseDirectory = new File(releasePath, pluginDirectory);
            releaseDirectory.mkdirs();
            try (FileOutputStream fos = new FileOutputStream(new File(releaseDirectory, "plugin.stream"))) {
                try (ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                    oos.writeObject(new TurboMatching());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void activate(Proxy proxy) {
        proxy.registerOption(this, "evaluate matching", this::OnMatchingRequested);
    }

    @Override
    public void deactivate(Proxy proxy) {
        proxy.releasePluginResources(this);
    }

    private void OnMatchingRequested(VersionStack<GraphVisualization<PropertySupportingGraph>> versionStack) {
        PropertySupportingGraph graph = versionStack.getCurrent().getGraph();
        Iterable<Vertex> vertices = graph.getVertices();
        if (isBipartite(vertices)) {
            Map<Vertex, Vertex> matching = evaluateMatching(vertices);
            matching.forEach((v1, v2) -> System.out.println(v1.getIndex() + " matches " + v2.getIndex()));
            System.out.println("Computed matching!");
        } else {
            System.out.println("Your graph is not bipartite!");
        }
    }

    private boolean visit(Vertex v, Set<Vertex> vColor, Set<Vertex> otherColor) {
        for (Vertex adj : v.getAdjacent()) {
            if (vColor.contains(adj)) {
                return true;
            } else {
                otherColor.add(adj);
                if (visit(adj, otherColor, vColor)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isBipartite(Iterable<Vertex> vertices) {
        Set<Vertex> green = new HashSet<>();
        Set<Vertex> red = new HashSet<>();
        for (Vertex v : vertices) {
            if (!green.contains(v) && !red.contains(v)) {
                green.add(v);
                if (visit(v, green, red)) {
                    return false;
                }
            }
        }
        return true;
    }

    private Map<Vertex, Vertex> evaluateMatching(Iterable<Vertex> vertices) {
        while (true) {
            Set<Vertex> visited = new HashSet<>();
            Map<Vertex, Vertex> match = new HashMap<>();
            boolean improvement = false;
            for (Vertex v : vertices) {
                if (match.get(v) == null) {
                    improvement |= alternatingPath(v, visited, match);
                }
            }
            if (!improvement) { return match; }
        }
    }

    private boolean alternatingPath(Vertex v, Set<Vertex> visited, Map<Vertex, Vertex> match) {
        if (!visited.add(v)) {
            return false;
        }
        for (Vertex adj : v.getAdjacent()) {
            if (match.get(adj) == null || alternatingPath(adj, visited, match)) {
                match.put(v, adj);
                match.put(adj, v);
                return true;
            }
        }
        return false;
    }
}
