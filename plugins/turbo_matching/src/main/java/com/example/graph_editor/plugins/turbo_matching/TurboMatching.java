package com.example.graph_editor.plugins.turbo_matching;

import graph_editor.extensions.EditingPlugin;
import graph_editor.extensions.OnGraphOptionSelection;
import graph_editor.graph.*;
import graph_editor.properties.GraphDebuilder;
import graph_editor.properties.PropertyGraphBuilder;
import graph_editor.properties.PropertySupportingGraph;
import graph_editor.visual.BuilderVisualizer;
import graph_editor.visual.GraphVisualization;

import java.util.*;

public class TurboMatching implements EditingPlugin {
    private static final String vertexProperty = "color::vertex::turbo_matching";
    private static final String edgeProperty = "color::edge::turbo_matching";
    private static final Iterable<String> used = Arrays.asList(vertexProperty, edgeProperty);

    @Override
    public void activate(Proxy<OnGraphOptionSelection> proxy) {
        proxy.registerOnSelection(this, "evaluate matching", new Handler());
    }

    @Override
    public void deactivate(Proxy<OnGraphOptionSelection> proxy) {
        proxy.releasePluginResources(this);
    }

    @Override
    public Iterable<String> usedPropertiesNames() {
        return used;
    }

    private static class Handler implements OnGraphOptionSelection {
        @Override
        public void handle(VersionStack<GraphVisualization<PropertySupportingGraph>> versionStack) {
            PropertySupportingGraph graph = versionStack.getCurrent().getGraph();
            Iterable<Vertex> vertices = graph.getVertices();
            VertexColouring colouring = isBipartite(vertices);
            GraphVisualization<PropertySupportingGraph> visualization;
            if (colouring.isBipartite) {
                Map<Vertex, Vertex> matching = evaluateMatching(vertices);
                visualization = saveProperties(versionStack.getCurrent(), colouring.green, matching);
            } else {
                visualization = resetProperties(versionStack.getCurrent());
            }
            versionStack.push(visualization);
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

        private static class VertexColouring {
            private final boolean isBipartite;
            private final Set<Vertex> green;
            private final Set<Vertex> red;
            private VertexColouring(boolean isBipartite, Set<Vertex> green, Set<Vertex> red) {
                this.isBipartite = isBipartite;
                this.green = green;
                this.red = red;
            }

        }
        private VertexColouring isBipartite(Iterable<Vertex> vertices) {
            Set<Vertex> green = new HashSet<>();
            Set<Vertex> red = new HashSet<>();
            for (Vertex v : vertices) {
                if (!green.contains(v) && !red.contains(v)) {
                    green.add(v);
                    if (visit(v, green, red)) {
                        return new VertexColouring(false, green, red);
                    }
                }
            }
            return new VertexColouring(true, green, red);
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

        private GraphVisualization<PropertySupportingGraph> saveProperties(GraphVisualization<PropertySupportingGraph> visualization, Set<Vertex> green, Map<Vertex, Vertex> matching) {
            PropertySupportingGraph graph = visualization.getGraph();
            UndirectedGraph.Builder builder = new UndirectedGraph.Builder(graph.getVertices().size());
            BuilderVisualizer visualizer = new BuilderVisualizer();
            PropertyGraphBuilder propertyGraphBuilder = GraphDebuilder.deBuild(graph, builder, visualizer, visualization.getVisualization().entrySet());
            propertyGraphBuilder.registerProperty(vertexProperty);
            for (Vertex v : graph.getVertices()) {
                propertyGraphBuilder.addElementProperty(v, vertexProperty, green.contains(v) ? "0x00ff00" : "0xff0000");
            }
            propertyGraphBuilder.registerProperty(edgeProperty);
            for (Edge e : graph.getEdges()) {
                propertyGraphBuilder.addElementProperty(
                        e,
                        edgeProperty,
                        matching.get(e.getSource()).equals(e.getTarget()) ? "matching" : "non_matching"
                );
            }
            return visualizer.generateVisual(propertyGraphBuilder.build());
        }
        private GraphVisualization<PropertySupportingGraph> resetProperties(GraphVisualization<PropertySupportingGraph> visualization) {
            PropertySupportingGraph graph = visualization.getGraph();
            UndirectedGraph.Builder builder = new UndirectedGraph.Builder(graph.getVertices().size());
            BuilderVisualizer visualizer = new BuilderVisualizer();
            PropertyGraphBuilder propertyGraphBuilder = GraphDebuilder.deBuild(graph, builder, visualizer, visualization.getVisualization().entrySet());
            propertyGraphBuilder.registerProperty(vertexProperty);
            return visualizer.generateVisual(propertyGraphBuilder.build());
        }
    }
}
