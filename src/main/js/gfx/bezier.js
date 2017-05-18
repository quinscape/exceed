import Vector from "./vector"

var bzv0 = new Vector();
var bzv1 = new Vector();


function bezierPoint(vector, x0,y0,x1,y1,t)
{
    var x = x0 + (x1 - x0) * t;
    var y = y0 + (y1 - y0) * t;

    vector.x = x;
    vector.y = y;

    return vector;
}
function bezierPointV(vector, v0,v1,t)
{
    var x0 = v0.x;
    var y0 = v0.y;
    var x = x0 + (v1.x - x0) * t;
    var y = y0 + (v1.y - y0) * t;

    vector.x = x;
    vector.y = y;

    return vector;
}

function quadraticBezier(x0,y0,x1,y1,x2,y2, current)
{
    var a = bezierPoint(bzv0, x0, y0, x1, y1, current);
    var b = bezierPoint(bzv1, x1, y1, x2, y2, current);

    return bezierPointV(new Vector(), a, b, current);
}

export default quadraticBezier;
