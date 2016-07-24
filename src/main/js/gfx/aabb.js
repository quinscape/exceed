const assign = require("object-assign");

/**
 * Axis aligned bounding box of an element.
 *
 * @type {*}
 */
/**
 * Create a new AABB instance
 * @constructor
 *
 * @param x     lowest x
 * @param y     lowest y
 * @param w     width
 * @param h     height
 */
var AABB = function (x, y, w, h)
{
    if (typeof x === "number")
    {
        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;
    }
    else
    {
        this.x = x.x;
        this.y = x.y;
        this.width = x.width;
        this.height = x.height;
    }
};

assign(AABB.prototype, {
    getCenter: function ()
    {

        var wh = this.width / 2;
        var hh = this.height / 2;

        return new Vector(this.x + wh, this.y + hh);
    },
    scale:
        function(scale)
        {
            var center = this.getCenter();

            // scale size
            this.width *= scale;
            this.height *= scale;

            // starting point
            this.x = center.x - this.width / 2;
            this.y = center.y - this.height / 2;

            return this;
        },
    shrinkKeepingAspect: function(aabb, scale)
    {
        scale = scale || 1;

        if (aabb.height === 0)
        {
            throw new Error("Height can't be 0.");
        }

        var ratio = aabb.width / aabb.height;


        if (ratio < 1)
        {
            this.x *= scale;
            this.y = this.x * ratio;
        }
        else
        {
            this.y *= scale;
            this.x = this.y * ratio;
        }
    },
    extend: function(aabb)
    {
        var delta;
        if (aabb.x < this.x)
        {
            delta = aabb.x - this.x;
            this.x += delta;
            this.width -= delta;

            return true;
        }
        if (aabb.y < this.y)
        {
            delta = aabb.y - this.y;
            this.y += delta;
            this.height -= delta;

            return true;
        }
        if (aabb.x + aabb.width > this.x + this.width)
        {
            delta = (aabb.x + aabb.width) - (this.x + this.width);
            this.width += delta;
            return true;
        }
        if (aabb.y + aabb.height > this.y + this.height)
        {
            delta = (aabb.y + aabb.height) - (this.y + this.height);
            this.height += delta;
            return true;
        }

        return false;
    },

    copy: function()
    {
        return new AABB(this);
    }
});

module.exports = AABB;
