var assign = require("object-assign");


var angle = 0;

const STEP =  1.61803398875;

function Vector(x,y)
{
    this.x = x;
    this.y = y;
}

assign(Vector.prototype, {
    add: function(x,y)
    {
        if (y === undefined)
        {
            this.x += x.x;
            this.y += x.y;
        }
        else
        {
            this.x += x;
            this.y += y;
        }
        return this;
    },
    subtract: function(x,y)
    {
        if (y === undefined)
        {
            this.x -= x.x;
            this.y -= x.y;
        }
        else
        {
            this.x -= x;
            this.y -= y;
        }
        return this;
    },
    scale: function(s)
    {
        this.x *= s;
        this.y *= s;
        return this;
    },
    len: function ()
    {
        return Math.sqrt(this.x * this.x + this.y * this.y);
    },
    norm: function(len)
    {
        var invLen = (len || 1) / this.len();
        return this.scale(invLen);
    },
    normOrPush: function(len)
    {
        var curLen = this.len();

        len = len || 1;

        if (curLen === 0)
        {
            this.x = Math.cos(angle) * len;
            this.y = Math.sin(angle) * len;

            angle += STEP;

            return (this);
        }

        var invLen = len / curLen;
        return this.scale(invLen);
    },
    copy: function ()
    {
        return new Vector(this.x,this.y);
    },
    rotate90: function()
    {
        var tmp = this.x;
        this.x = -this.y;
        this.y = tmp;

        return (this);
    },
    toString: function()
    {
        return "( " + this.x + ", " + this.y +")";
    }
});


module.exports = Vector;
