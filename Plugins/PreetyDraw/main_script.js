var option_id = 0;
var action_id = 0;
var Paint = android.graphics.Paint;
var Debug = com.example.graph_editor.draw.graph_view.extensions.DebugCanvas
var Toast = Packages.android.widget.Toast;

function activate(coreProxy /* com.example.graph_editor.extentions.ScriptProxy*/) {
    option_id = coreProxy.registerGraphMenuOption("Extension's option", "foo");
    coreProxy.customizeVertexDrawingBehaviour("drawVertex");
    coreProxy.customizeEdgeDrawingBehaviour("drawEdge");
    action_id = coreProxy.registerGraphAction("app_icon.png", "bar");
}

function addGraphElement(elementType/* string*/, graph) {
//com.example.graph_editor.draw.graph_view.extensions
}

function canvasX(x, rectangle, canvas) {
    return (x - rectangle.getLeft()) * canvas.getWidth() / rectangle.getWidth();
}
function canvasY(y, rectangle, canvas) {
    return (y - rectangle.getTop()) * canvas.getHeight() / rectangle.getHeight();
}

function drawVertex(vertex, rectangle, canvas) {
    var point = vertex.getPoint()
    var aPaint = new Paint;
    aPaint.setARGB(255, 255, 0, 255);
    aPaint.setStyle(Paint.Style.FILL);
    aPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
    var cx = canvasX(point.getX(), rectangle, canvas);
    var cy = canvasY(point.getY(), rectangle, canvas);

    var strokePaint1 = new Paint;
    strokePaint1.setStyle(Paint.Style.STROKE);
    strokePaint1.setARGB(255, 0, 255, 0);
    strokePaint1.setStrokeWidth(0.005 * canvas.getWidth() / rectangle.getWidth());
    strokePaint1.setStrokeCap(Paint.Cap.ROUND);

    var strokePaint2 = new Paint;
    strokePaint2.setStyle(Paint.Style.STROKE);
    strokePaint2.setARGB(255, 255, 0, 0);
    strokePaint2.setStrokeWidth(0.005 * canvas.getWidth() / rectangle.getWidth());
    strokePaint2.setStrokeCap(Paint.Cap.ROUND);

    canvas.drawCircle(cx, cy, 0.005 * canvas.getWidth() / rectangle.getWidth(), aPaint);
    canvas.drawCircle(cx, cy, 0.005 * canvas.getWidth() / rectangle.getWidth(), strokePaint1);
    canvas.drawCircle(cx, cy, 0.010 * canvas.getWidth() / rectangle.getWidth(), strokePaint2);
}

function drawEdge(edge, rectangle, canvas) {
    var p1 = edge.getSource().getPoint()
    var p2 = edge.getTarget().getPoint()
    var edgePaint = new Paint();
    edgePaint.setARGB(255, 255, 255, 0);
    edgePaint.setStyle(Paint.Style.STROKE);
    edgePaint.setStrokeWidth(0.005 * canvas.getWidth() / rectangle.getWidth());
    edgePaint.setFlags(Paint.ANTI_ALIAS_FLAG);

    var x1 = canvasX(p1.getX(), rectangle, canvas);
    var y1 = canvasY(p1.getY(), rectangle, canvas);
    var x2 = canvasX(p2.getX(), rectangle, canvas);
    var y2 = canvasY(p2.getY(), rectangle, canvas);

//    float dx = x2-x1;
//    float dy = y2-y1;
//    if (dx*dx + dy*dy < 0.1*0.1) return;

    canvas.drawLine(x1, y1, x2, y2, edgePaint);
//    if (type == GraphType.DIRECTED) {
//        drawArrow(edgePaint, canvas, x1, y1, x2, y2);
//    }
}

function bar(view, event, stateStack, data) {
    var Toast = Packages.android.widget.Toast;
    Toast.makeText(view.getContext(), "You are using new graph action", Toast.LENGTH_SHORT).show();
}


//function saveVertexProperty(id, propertyName, value) {
//    ...
//}

//function polygonDrawer(rectangle, canvas) {
//    readFile(polygons/ww)
//    (1,2,3,4)
//    ,(3,3)
//    canvas.drawPath(Fill)
//}
// This method is called when your extension is deactivated

function deactivate(coreProxy/* com.example.graph_editor.extentions.ScriptProxy*/) {
    coreProxy.deregisterGraphMenuOption(option_id);
    coreProxy.restoreDefaultVertexDrawingBehaviour();
    coreProxy.restoreDefaultEdgeDrawingBehaviour();
    coreProxy.deregisterGraphAction(action_id);
}
