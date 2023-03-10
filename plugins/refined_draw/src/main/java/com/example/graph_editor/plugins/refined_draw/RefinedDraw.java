package com.example.graph_editor.plugins.refined_draw;

import graph_editor.draw.IGraphDrawer;
import graph_editor.draw.point_mapping.CanvasDrawer;
import graph_editor.draw.point_mapping.PointMapper;
import graph_editor.extensions.OnPropertyReaderSelection;
import graph_editor.extensions.Plugin;
import graph_editor.geometry.Point;
import graph_editor.graph.Edge;
import graph_editor.graph.Vertex;
import graph_editor.properties.PropertyRepository;
import graph_editor.properties.PropertySupportingGraph;
import graph_editor.visual.GraphVisualization;

import java.util.*;
import java.util.List;

public class RefinedDraw extends Plugin {
    private static final String vertexGroup = "color::vertex::";
    private static final String edgeGroup = "color::edge::";

    private final ChosenProperty vertexColor = new ChosenProperty();
    private final ChosenProperty borderColor = new ChosenProperty();
    private final ChosenProperty edgeColor = new ChosenProperty();

    @Override
    public void activate(Proxy proxy) {
        proxy.registerDeclaredPropertiesReader(this, "change vertex coloring property", new Reader(vertexGroup, vertexColor));
        proxy.registerDeclaredPropertiesReader(this, "change vertex border property", new Reader(vertexGroup, borderColor));
        proxy.registerDeclaredPropertiesReader(this, "change edge coloring property", new Reader(edgeGroup, edgeColor));
    }
    @Override
    public void deactivate(Proxy proxy) {
        proxy.releasePluginResources(this);
    }

    @Override
    public boolean supportsDirectedGraphs() {
        return false;
    }

    @Override
    public boolean supportsUndirectedGraphs() {
        return true;
    }

    @Override
    public Iterable<graph_editor.extensions.Plugin.Drawer> getGraphDrawers() {
        return Collections.singleton(new DrawerFactory());
    }

    private static class Reader implements OnPropertyReaderSelection {
        private final String groupName;
        private final ChosenProperty choice;
        Reader(String groupName, ChosenProperty choice) {
            this.groupName = groupName;
            this.choice = choice;
        }
        @Override
        public List<SettingChoice> handle(List<String> list) {
            List<SettingChoice> result = new ArrayList<>();
            for (String propertyName : list) {
                if (propertyName.startsWith(groupName)) {
                    result.add(new Choice(propertyName, choice));
                }
            }
            result.add(new Reset(choice));
            return result;
        }
    }
    private static class ChosenProperty {
        String name;
    }
    private static class Reset implements OnPropertyReaderSelection.SettingChoice {
        private final ChosenProperty property;

        private Reset(ChosenProperty property) {
            this.property = property;
        }

        @Override
        public String getName() {
            return "none";
        }

        @Override
        public void choose() {
            property.name = null;
        }
    }
    private static class Choice implements OnPropertyReaderSelection.SettingChoice {
        private final String name;
        private final ChosenProperty property;
        private Choice(String name, ChosenProperty property) {
            this.name = name;
            this.property = property;
        }
        @Override
        public String getName() {
            return name;
        }

        @Override
        public void choose() {
            property.name = name;
        }
    }
    private class DrawerFactory implements Plugin.Drawer {
        @Override
        public IGraphDrawer<PropertySupportingGraph> getGraphDrawer(PointMapper pointMapper, CanvasDrawer canvasDrawer) {
            return new Drawer(pointMapper, canvasDrawer);
        }
    }

    private class Drawer implements IGraphDrawer<PropertySupportingGraph> {
        private static final float radius = 20.0f;
        private static final int defaultColor = 0xff000000;
        private static final int errorColor = 0xff7f7f7f;
        private final PointMapper mapper;
        private final CanvasDrawer drawer;

        private Drawer(PointMapper mapper, CanvasDrawer drawer) {
            this.mapper = mapper;
            this.drawer = drawer;
        }

        @Override
        public void drawGraph(GraphVisualization<PropertySupportingGraph> visual) {
            Map<Vertex, String> vertexMap = new HashMap<>();
            Map<Vertex, String> borderMap = new HashMap<>();
            Map<Edge, String> edgeMap = new HashMap<>();

            PropertySupportingGraph graph = visual.getGraph();
            readToVMap(graph, vertexColor, graph.getVertices(), vertexMap);
            readToVMap(graph, borderColor, graph.getVertices(), borderMap);
            readToEMap(graph, edgeColor, graph.getEdges(), edgeMap);

            Map<Vertex, Point> coordinates = visual.getVisualization();
            for (Vertex v : graph.getVertices()) {
                int color = borderColor.name != null ? toInt(borderMap.get(v)) : defaultColor;
                drawer.drawCircle(mapper.mapIntoView(
                        coordinates.get(v)),
                        radius,
                        color);
            }
            for (Vertex v : graph.getVertices()) {
                int color = vertexColor.name != null ? toInt(vertexMap.get(v)) : defaultColor;
                drawer.drawCircle(mapper.mapIntoView(
                                coordinates.get(v)),
                        radius * 0.7f,
                        color);
            }
            for (Edge e : graph.getEdges()) {
                int color = edgeColor.name != null ? toInt(edgeMap.get(e)) : defaultColor;
                drawer.drawLine(
                        mapper.mapIntoView(coordinates.get(e.getSource())),
                        mapper.mapIntoView(coordinates.get(e.getTarget())),
                        color
                );
            }
        }
        private int toInt(String s) {
            try {
                return (int)Long.parseLong(s, 16);
            } catch (Exception e) {
                System.out.println("failed to parse " + s);
                return Drawer.errorColor;
            }
        }

        private void readToVMap(PropertyRepository repository, ChosenProperty property, Iterable<Vertex> source, Map<Vertex, String> target) {
            if (property.name != null) {
                for (Vertex element : source) {
                    String color = repository.getPropertyValue(property.name, element);
                    if (color != null) {
                        target.put(element, color);
                    }
                }
            }
        }
        private void readToEMap(PropertyRepository repository, ChosenProperty property, Iterable<Edge> source, Map<Edge, String> target) {
            if (property.name != null) {
                for (Edge element : source) {
                    String color = repository.getPropertyValue(property.name, element);
                    if (color != null) {
                        target.put(element, color);
                    }
                }
            }
        }
    }
}
