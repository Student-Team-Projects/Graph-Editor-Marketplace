package com.example.graph_editor.plugins.refined_draw;

import graph_editor.draw.AbstractGraphDrawer;
import graph_editor.draw.GraphDrawer;
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

public class RefinedDraw implements DrawingPlugin {
    private static final String vertexGroup = "color::vertex::";
    private static final String vertexSecondRing = "color::vertex2::";
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
    public GraphDrawer<PropertySupportingGraph> getGraphDrawer(PointMapper mapper, CanvasDrawer canvasDrawer) {
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
                    if (propertyName.startsWith(vertexGroup) || propertyName.startsWith(vertexSecondRing) || propertyName.startsWith(edgeGroup)) {
                        result.add(new Choice(propertyName, behaviour));
                    }
                }
            }
            return result;
        }

        public GraphDrawer<PropertySupportingGraph> getDrawer(PointMapper mapper, CanvasDrawer canvasDrawer) {
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
            } else if (name.startsWith(vertexSecondRing)) {
                behaviour.vertexBehaviour2 = name;
            } else {
                behaviour.edgeBehaviour = name;
            }
        }
    }

    private static class Drawer extends AbstractGraphDrawer<PropertySupportingGraph> {
        private static final float radius = 20.0f;
        private final PointMapper mapper;
        private final CanvasDrawer drawer;
        private final Behaviour behaviour;

        private Map<Vertex, String> vertexColor;
        private Map<Vertex, String> vertexColor2;
        private Map<Edge, String> edgeColor;

        private Drawer(PointMapper mapper, CanvasDrawer drawer, Behaviour behaviour) {
            this.mapper = mapper;
            this.drawer = drawer;
            this.behaviour = behaviour;
        }

        @Override
        public void drawGraph(GraphVisualization<PropertySupportingGraph> visual) {
            super.drawGraph(visual);
            vertexColor = new HashMap<>();
            vertexColor2 = new HashMap<>();
            edgeColor = new HashMap<>();
            readColors(visual.getGraph());
        }

        @Override
        protected void moveCursorTo(Point point) {
            drawer.drawCircle(mapper.mapIntoView(point), radius, );
        }

        @Override
        protected void drawLineTo(Point point) {

        }

        @Override
        protected void drawCircle(Point point) {

        }

        private void readColors(PropertySupportingGraph graph) {
            readToMap(graph, behaviour.vertexBehaviour, graph.getVertices(), vertexColor);
            readToMap(graph, behaviour.vertexBehaviour2, graph.getVertices(), vertexColor2);
            readToMap(graph, behaviour.edgeBehaviour, graph.getEdges(), edgeColor);
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
