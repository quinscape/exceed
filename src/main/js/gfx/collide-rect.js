import quadraticBezier from "./bezier"

function collideCurveWithRect(x0,y0,x1,y1, x2, y2, w, h)
{
    var lo = 0.5;
    var hi = 1;

    var xLimit;
    var yLimit;

    var xSign;
    var ySign;

    var xMin = x2 - w/2;
    var xMax = x2 + w/2;
    var yMin = y2 - h/2;
    var yMax = y2 + h/2;


//            if (x0 > xMin &&
//                x0 < xMax &&
//                y0 > yMin &&
//                y0 < yMax)
//            {
//                return {
//                    t:0,
//                    pos: new Vector((x0+x2)/2,(y0+y2)/2)
//                };
//            }

    if (x1 > xMax)
    {
        xLimit = xMax;
        xSign = -1;
    }
    else
    {
        xLimit = xMin;
        xSign = 1;
    }
    if (y1 > yMax)
    {
        yLimit = yMax;
        ySign = -1;
    }
    else
    {
        yLimit = yMin;
        ySign = 1;
    }

    var pt,mid;
    do
    {
        mid = (lo + hi)/2;

        pt = quadraticBezier(x0,y0,x1,y1, x2,y2, mid);

        var dy = (yLimit - pt.y) * ySign;
        var dx = (xLimit - pt.x) * xSign;

        var delta = dx > dy ? dx : dy;

        if (delta == 0)
        {
            break;
        }
        else if (delta < 0)
        {
            hi = mid;
        }
        else
        {
            lo = mid;
        }


    } while (Math.abs(delta) > 2 && hi - lo > 0.01);

    return {pos: pt, t: mid};
}

export default collideCurveWithRect();
