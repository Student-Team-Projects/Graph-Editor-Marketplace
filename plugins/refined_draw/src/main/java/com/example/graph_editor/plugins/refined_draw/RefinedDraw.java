package com.example.graph_editor.plugins.refined_draw;

import graph_editor.draw.IGraphDrawer;
import graph_editor.draw.point_mapping.CanvasDrawer;
import graph_editor.draw.point_mapping.PointMapper;
import graph_editor.extensions.DrawingBehaviour;
import graph_editor.extensions.DrawingPlugin;
import graph_editor.extensions.OnPropertyManagerSelection;
import graph_editor.geometry.Point;
import graph_editor.graph.Edge;
import graph_editor.graph.GraphElement;
import graph_editor.graph.Vertex;
import graph_editor.properties.PropertyRepository;
import graph_editor.properties.PropertySupportingGraph;
import graph_editor.properties.PropertyUser;
import graph_editor.visual.GraphVisualization;

import java.util.*;
import java.util.List;

public class RefinedDraw implements DrawingPlugin {
    private static final String vertexGroup = "color::vertex::";
    private static final String vertexGroup2 = "color::vertex2::";
    private static final String edgeGroup = "color::edge::";

    private final Behaviour behaviour = new Behaviour();
    private final Manager manager = new Manager();

    @Override
    public void activate(DrawingProxy proxy) {
        proxy.registerPropertyOption(this, "customize drawing behaviour", manager);
    }

    @Override
    public void deactivate(DrawingProxy proxy) {
        proxy.releasePluginResources(this);
    }

    @Override
    public Iterable<String> usedPropertiesNames() {
        return Collections.emptyList();
    }

    @Override
    public IGraphDrawer<PropertySupportingGraph> getGraphDrawer(PointMapper mapper, CanvasDrawer canvasDrawer) {
        return manager.getDrawer(mapper, canvasDrawer);
    }
    private static class Behaviour implements DrawingBehaviour {
        private String vertexBehaviour;
        private String vertexBehaviour2;
        private String edgeBehaviour;
    }
    private class Manager implements OnPropertyManagerSelection {
        @Override
        public List<SettingChoice> handle(List<PropertyUser> list) {
            List<SettingChoice> result = new ArrayList<>();
            for (PropertyUser user : list) {
                Iterable<String> used = user.usedPropertiesNames();
                for (String propertyName : used) {
                    if (propertyName.startsWith(vertexGroup) || propertyName.startsWith(vertexGroup2) || propertyName.startsWith(edgeGroup)) {
                        result.add(new Choice(propertyName, behaviour));
                    }
                }
            }
            return result;
        }

        public IGraphDrawer<PropertySupportingGraph> getDrawer(PointMapper mapper, CanvasDrawer canvasDrawer) {
            return new Drawer(mapper, canvasDrawer, behaviour);
        }
    }
    private static class Choice implements OnPropertyManagerSelection.SettingChoice {
        private final String name;
        private final Behaviour behaviour;
        private Choice(String name, Behaviour behaviour) {
            this.name = name;
            this.behaviour = behaviour;
        }
        @Override
        public String getName() {
            return name;
        }
        @Override
        public void choose() {
            if (name.startsWith(vertexGroup)) {
                behaviour.vertexBehaviour = name;
            } else if (name.startsWith(vertexGroup2)) {
                behaviour.vertexBehaviour2 = name;
            } else {
                behaviour.edgeBehaviour = name;
            }
        }
    }

    private static class Drawer implements IGraphDrawer<PropertySupportingGraph> {
        private static final float radius = 20.0f;
        private static final int defaultColor = 0x00000000; //black
        private final PointMapper mapper;
        private final CanvasDrawer drawer;
        private final Behaviour behaviour;

        private Drawer(PointMapper mapper, CanvasDrawer drawer, Behaviour behaviour) {
            this.mapper = mapper;
            this.drawer = drawer;
            this.behaviour = behaviour;
        }

        @Override
        public void drawGraph(GraphVisualization<PropertySupportingGraph> visual) {
            Map<Vertex, String> vertexColor = new HashMap<>();
            Map<Vertex, String> vertexColor2 = new HashMap<>();
            Map<Edge, String> edgeColor = new HashMap<>();

            PropertySupportingGraph graph = visual.getGraph();
            readToMap(graph, behaviour.vertexBehaviour, graph.getVertices(), vertexColor);
            readToMap(graph, behaviour.vertexBehaviour2, graph.getVertices(), vertexColor2);
            readToMap(graph, behaviour.edgeBehaviour, graph.getEdges(), edgeColor);

            Map<Vertex, Point> coordinates = visual.getVisualization();
            for (Vertex v : graph.getVertices()) {
                int color = toInt(vertexColor2.get(v));
                drawer.drawCircle(mapper.mapIntoView(
                        coordinates.get(v)),
                        radius,
                        behaviour.vertexBehaviour2 == null ? defaultColor : color);
            }
            for (Vertex v : graph.getVertices()) {
                int color = toInt(vertexColor.get(v));
                drawer.drawCircle(mapper.mapIntoView(
                                coordinates.get(v)),
                        radius / 2,
                        behaviour.vertexBehaviour == null ? defaultColor : color);
            }
            for (Edge e : graph.getEdges()) {
                int color = toInt(edgeColor.get(e));
                drawer.drawLine(
                        mapper.mapIntoView(coordinates.get(e.getSource())),
                        mapper.mapIntoView(coordinates.get(e.getTarget())),
                        behaviour.vertexBehaviour == null ? defaultColor : color
                );
            }
        }
        private static int toInt(String s) {
            try {
                return Integer.parseInt(s);
            } catch (Exception e) {
                return Drawer.defaultColor;
            }
        }

        private <T extends GraphElement> void readToMap(PropertyRepository repository,  String propertyName, Iterable<T> source, Map<T, String> target) {
            if (propertyName != null) {
                for (T element : source) {
                    String color = repository.getPropertyValue(propertyName, element);
                    if (color != null) {
                        target.put(element, color);
                    }
                }
            }
        }
    }
}
