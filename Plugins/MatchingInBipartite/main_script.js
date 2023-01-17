var Utils = com.example.graph_editor.draw.Utils
var Paint = android.graphics.Paint;
var Toast = Packages.android.widget.Toast;
var Debug = com.example.graph_editor.draw.graph_view.extensions.DebugCanvas

var option_id = 0;
var vertexColorPropertyName = "matching::vertexColor";
var edgeColorPropertyName = "matching::edgeColor"

function activate(proxy /* com.example.graph_editor.extensions.ScriptProxy*/) {
    option_id = proxy.registerGraphMenuOption("Draw Matching", "matchingRequested");
    proxy.customizeVertexDrawingBehaviour("drawVertex");
    proxy.customizeEdgeDrawingBehaviour("drawEdge");
}

function deactivate(coreProxy/* com.example.graph_editor.extensions.ScriptProxy*/) {
    coreProxy.deregisterGraphMenuOption(option_id);
    coreProxy.restoreDefaultVertexDrawingBehaviour();
    coreProxy.restoreDefaultEdgeDrawingBehaviour();
}

function createVertex(vertexId, ids) {
    return {id:vertexId, adjacentIds:ids, color:-1, match:null, visited:false}
}


function buildVertices(coreGraph) {
    var vertices = {}
    for (coreVertex in Iterator(coreGraph.getVertices())) {
        var adjacentIds = []
        for (coreEdge in Iterator(coreVertex.getEdges())) {
            adjacentIds.push(coreEdge.getTarget().getIndex())
        }
        var id = coreVertex.getIndex()
        vertices[id] = createVertex(id, adjacentIds)
    }
    return vertices
}

function saveProperties(verticesMap, coreGraph) {
    if (verticesMap == null) {
        coreGraph.removeVertexProperty(vertexColorPropertyName)
        coreGraph.removeEdgeProperty(edgeColorPropertyName)
        return;
    }
    for (coreVertex in Iterator(coreGraph.getVertices())) {
        jsVertex = verticesMap[coreVertex.getIndex()];
        coreGraph.setVertexProperty(coreVertex, vertexColorPropertyName, jsVertex.color.toString())
    }
    for (coreEdge in Iterator(coreGraph.getEdges())) {
        if (coreEdge.getTarget().getIndex() == verticesMap[coreEdge.getSource().getIndex()].match.id) {
            coreGraph.setEdgeProperty(coreEdge, edgeColorPropertyName, "match-true")
        } else {
            coreGraph.setEdgeProperty(coreEdge, edgeColorPropertyName, "match-false")
        }
    }
}

function visit(vertices, vertex) {
    vertex.visited = true
    for (i in vertex.adjacentIds) {
        var id = vertex.adjacentIds[i]
        var adjacent = vertices[id]
        if (adjacent.visited == false) {
            adjacent.color = 1 - vertex.color
            if (!visit(vertices, adjacent)) {
                return false
            }
        } else if (adjacent.color == vertex.color) {
            return false
        }
    }
    return true
}

function bipartite(vertices) {
    for (id in vertices) {
        var vertex = vertices[id]
        if (vertex.visited == false) {
            vertex.color = 0

            if (!visit(vertices, vertex)) {
                return false
            }
        }
    }
    return true
}

function alternating_path(vertices, vertex) {
    if (vertex.visited) return false;
    vertex.visited = true;
    for (i in vertex.adjacentIds) {
        var id = vertex.adjacentIds[i]
        var adjacent = vertices[id]
        if (adjacent.match == null || alternating_path(vertices, adjacent.match) == 1) {
            vertex.match = adjacent;
            adjacent.match = vertex;
            return true;
        }
    }
    return false;
}
//turbo matching
function evaluateMatching(vertices) {
    while (true) {
        for (id in vertices) {
            vertices[id].visited = false
        }
        var improvement = 0 // = false
        for (id in vertices) {
            var vertex = vertices[id]
            if (vertex.match == null) {
                improvement |= alternating_path(vertices, vertex)
            }
        }
        if (!improvement) return
    }
}

function matchingRequested(stateStack, coreGraph, view) {
    var verticesMap = buildVertices(coreGraph)
    stateStack.backup();
    if (bipartite(verticesMap)) {
        evaluateMatching(verticesMap);
        saveProperties(verticesMap, coreGraph);
        Toast.makeText(view.getContext(), "Computed matching!", Toast.LENGTH_SHORT).show();
    } else {
        verticesMap = null
        saveProperties(verticesMap, coreGraph);
        Toast.makeText(view.getContext(), "Your graph is not bipartite!", Toast.LENGTH_SHORT).show();
    }
    view.invalidate();
}

function drawVertex(vertex, rectangle, canvas) {
    var aPaint = new Paint;
    var color = vertex.getProperty(vertexColorPropertyName);
    if (color == null) {
        aPaint.setARGB(255, 0, 0, 0);
    } else if (color == "0") {
        aPaint.setARGB(255, 255, 0, 0)
    } else if (color == "1") {
        aPaint.setARGB(255, 0, 255, 0);
    } else {
        Debug.print(color)
        aPaint.setARGB(255, 255, 255, 0);
    }
    aPaint.setStyle(Paint.Style.FILL);
    aPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
    var cp = Utils.canvasPoint(vertex.getPoint(), rectangle, canvas)
    canvas.drawCircle(cp.getX(), cp.getY(), 0.005 * canvas.getWidth() / rectangle.getWidth(), aPaint);
}

function drawEdge(edge, rectangle, canvas) {
    var edgePaint = new Paint();
    var source = edge.getSource();
    var target = edge.getTarget();

    var matchingStatus = edge.getProperty(edgeColorPropertyName)
    if (matchingStatus == "match-true") {
        edgePaint.setARGB(255, 0, 0, 255);
    } else {
        edgePaint.setARGB(255, 127, 127, 127);
    }

    edgePaint.setStyle(Paint.Style.STROKE);
    edgePaint.setStrokeWidth(0.005 * canvas.getWidth() / rectangle.getWidth());
    edgePaint.setFlags(Paint.ANTI_ALIAS_FLAG);

    var cp1 = Utils.canvasPoint(source.getPoint(), rectangle, canvas)
    var cp2 = Utils.canvasPoint(target.getPoint(), rectangle, canvas)

    canvas.drawLine(cp1.getX(), cp1.getY(), cp2.getX(), cp2.getY(), edgePaint);
}
