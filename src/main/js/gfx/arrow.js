const React = require("react");

const Vector = require("./vector");
const bezier = require("./bezier");

function mapToNumber(s)
{
    return +s;
}

function calculateControlPoint(x0, y0, x1, y1, x2, y2)
{
    var xm = (x2 + x0) / 2;
    var ym = (y2 + y0) / 2;

    // the curve will go through the wanted point when the control point is twice as far away
    // as our target point x1/y1 is from the transition node center
    x1 += (x1 - xm);
    y1 += (y1 - ym);

    return new Vector(x1, y1);
}

var ArrowComponent = React.createClass({

    getDefaultProps: function ()
    {
        return {
            color: "#999",
            tipColor: "#777",
            width: 4
        };
    },

    shouldComponentUpdate: function (nextProps)
    {
        var curProps = this.props;
        return (
            curProps.x0 !== nextProps.x0 ||
            curProps.y0 !== nextProps.y0 ||
            curProps.x1 !== nextProps.x1 ||
            curProps.y1 !== nextProps.y1 ||
            curProps.x2 !== nextProps.x2 ||
            curProps.y2 !== nextProps.y2
        );
    },

    render: function ()
    {
        var x0 = this.props.x0;
        var y0 = this.props.y0;
        var x1 = this.props.x1;
        var y1 = this.props.y1;
        var x2 = this.props.x2;
        var y2 = this.props.y2;

        var shape = this.props.targetShape;
        var aabb = this.props.targetAABB;

        var curvePath, tipPath, xm, ym, collisionResult;

        // if the start and end point are the same but the middle point is different
        if (x0 === x2 && y0 === y2)
        {
            if (x0 == x1 && y0 == y1)
            {
                // all points are the same.. very unfortunate. we render nothing
                curvePath = "";
                tipPath = "";
            }
            else
            {
                xm = (x1 + x0) / 2;
                ym = (y1 + y0) / 2;

                // our arrow starts and ends at the same points. we special-case
                // this by rendering a 2 quadratic bezier curves

                // we create control points for the new curves by nudging the center sideways
                // into opposite directions a bit
                var span = new Vector(x1 - x0, y1 - y0).norm(30).rotate90();

                var x01 = xm + span.x;
                var y01 = ym + span.y;

                var x12 = xm - span.x;
                var y12 = ym - span.y;

                var cp01 = calculateControlPoint(x0, y0, x01, y01, x1, y1);
                var cp12 = calculateControlPoint(x1, y1, x12, y12, x2, y2);

                curvePath = "M" + x0 + " " + y0 + " Q " + cp01.x + " " + cp01.y + " " + x1 + " " + y1 + " Q " + cp12.x + " " + cp12.y + " " + x2 + " " + y2;

                // calculate tip for the second bezier curve
                x0 = x1;
                y0 = y1;
                x1 = x12;
                y1 = y12;
                // end point stays the same
            }
        }

        var cp = calculateControlPoint(x0, y0, x1, y1, x2, y2);

        if (tipPath === undefined)
        {
            //if (shape === "circle")
            //{
            //    collisionResult = common.collideCurveWithCircle(x0, y0, cp.x, cp.y, x2, y2, aabb.width / 2 + 2);
            //}
            //else if (shape === "rect")
            //{
            //    collisionResult = common.collideCurveWithRect(x0, y0, cp.x, cp.y, x2, y2, aabb.width + 6, aabb.height + 6);
            //}
            //else
            //{
            //    throw new Error("Unknown shape '" + shape + "'.");
            //}
            //
            //var pos = collisionResult.pos;
            var pos = new Vector(x2,y2);
            var prev = bezier(x0, y0, cp.x, cp.y, x2, y2, 0.8);//collisionResult.t * 0.99);

            var vCounter = prev.subtract(pos).norm(10);
            var tail = pos.copy().add(vCounter);

            var vSide = vCounter.copy().rotate90().scale(0.5);
            vCounter.scale(0.3);
            var e1 = tail.copy().add(vCounter).add(vSide);
            var e2 = tail.copy().add(vCounter).subtract(vSide);

            tipPath = "M" + pos.x + " " + pos.y + " L " + e1.x + " " + e1.y + " L " + tail.x + "  " + tail.y + " L " + e2.x + "  " + e2.y + " Z";

        }

        if (curvePath === undefined)
        {
            curvePath = "M" + x0 + " " + y0 + " Q " + cp.x + " " + cp.y + " " + x2 + " " + y2;
        }

        return (
            <g>
                <path d={ curvePath }
                      strokeDasharray={ this.props.strokeDasharray }
                      style={{
                        fill: "transparent",
                        stroke: this.props.color,
                        strokeWidth: 4
                    }}
                />
                <path
                    d={ tipPath }
                    strokeDasharray={ this.props.strokeDasharray }
                    style={{
                        fill: this.props.tipColor,
                        stroke: this.props.tipColor,
                        strokeWidth: 4
                    }}
                />
            </g>
        );
    }
});

module.exports = ArrowComponent;
