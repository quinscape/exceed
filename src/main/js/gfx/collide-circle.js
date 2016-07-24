const quadraticBezier = require("./bezier");

/**
 * Collision check between a quadratic bezier curve and a circle where the circle center is also the end point of the curve.
 *
 * @param x0        curve start x coordinate
 * @param y0        curve start y coordinate
 * @param x1        control point x coordinate
 * @param y1        control point y coordinate
 *
 * @param cx        circle center x coordinate
 * @param cy        circle center y coordinate
 * @param r         circle radius
 *
 * @returns {{pos: *, t: *}}
 */
function collideCurveWithCircle(x0,y0,x1,y1,cx, cy, r)
{
    var lo = 0.5;
    var hi = 1;
    var pt,mid;

    do
    {
        mid = (lo + hi)/2;

        pt = quadraticBezier(x0, y0, x1, y1, cx, cy, mid);
        var distance = pt.copy().subtract(cx,cy).len();

        if (distance === r)
        {
            break;
        }
        else if (distance < r)
        {
            hi = mid;
        }
        else
        {
            lo = mid;
        }

    } while (Math.abs(distance - r) > 3 && hi - lo > 0.01);

    return {pos: pt, t: mid};
}

module.exports = collideCurveWithCircle;
