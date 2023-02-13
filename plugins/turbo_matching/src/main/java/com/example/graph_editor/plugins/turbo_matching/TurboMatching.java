package com.example.graph_editor.plugins.turbo_matching;

import graph_editor.extensions.OnOptionSelection;
import graph_editor.extensions.Plugin;
import graph_editor.graph.VersionStack;
import graph_editor.graph.Vertex;
import graph_editor.properties.PropertySupportingGraph;
import graph_editor.visual.GraphVisualization;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TurboMatching implements Plugin {
    private static final String releasePath = "marketplace/";
    private static final String pluginDirectory = "bipartite_matching";

    @Override
    public void activate(Proxy proxy) {
        System.out.println("activating...");
        proxy.registerOption(this, "evaluate matching", new Handler());
        System.out.println("activated");
    }

    @Override
    public void deactivate(Proxy proxy) {
        proxy.releasePluginResources(this);
    }
    private static class Handler implements OnOptionSelection {
        @Override
        public void handle(VersionStack<GraphVisualization<PropertySupportingGraph>> versionStack) {
            PropertySupportingGraph graph = versionStack.getCurrent().getGraph();
            Iterable<Vertex> vertices = graph.getVertices();
            if (isBipartite(vertices)) {
                System.out.println("Is bipartite!");
                Map<Vertex, Vertex> matching = evaluateMatching(vertices);
                for (Vertex v : vertices) {
                    if (matching.get(v) != null) {
                        System.out.println(v.getIndex() + " matches " + matching.get(v).getIndex());
                    } else {
                        System.out.println(v.getIndex() + " does not match any vertex");
                    }
                }
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
            Map<Vertex, Vertex> match = new HashMap<>();
            while (true) {
                Set<Vertex> visited = new HashSet<>();
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
                if (match.get(adj) == null || alternatingPath(match.get(adj), visited, match)) {
                    match.put(v, adj);
                    match.put(adj, v);
                    return true;
                }
            }
            return false;
        }
    }

}
