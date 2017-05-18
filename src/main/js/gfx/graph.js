import Vector from "./vector"

const TAU = Math.PI * 2;

const STEP = TAU / 1.61803398875;

const assign = require("object-assign");

function ring(graph, visited, center, minArc, maxArc, depth)
{
    var nodes = graph.nodes;


    visited[center] = true;

    var centerNode = nodes[center];
    var i, angle, refs = centerNode.biref;


    var count = 0;
    for (i = 0; i < refs.length; i++)
    {
        if (!visited[refs[i].id])
        {
            count++;
        }
    }

    if (!count)
    {
        return depth;
    }

    var arcStep = (maxArc - minArc) / count;
    //console.log("arcStep", arcStep);

    var max = -Infinity;
    for (i = 0, angle = minArc; i < refs.length; i++, angle += arcStep)
    {
        var id = refs[i].id;

        if (!visited[id])
        {
            var node = nodes[id];

            var dist = (graph.options.separationLimit * depth * 2);
            node.x = Math.cos(angle) * dist;
            node.y = Math.sin(angle) * dist;
            node.depth = depth;

            var d = ring(graph, visited, node.id, angle, angle + arcStep, depth + 1);
            if (d > max)
            {
                max = d;
            }
        }
    }

    return max;
}

const DEFAULT_SIZE = 40;

const DEFAULT_SIZE_SQUARED = DEFAULT_SIZE * DEFAULT_SIZE;

var defaultOptions = {
    /**
     * Zero-force distance for the separating force
     */
    separationLimit : DEFAULT_SIZE * 2.5,
    /**
     * Zero-force distance for the pulling edge force
     */
    edgeLimit : DEFAULT_SIZE * 1.4,
    /**
     * Linear multiplier of the separation force
     */
    separationForceMultiplier: 0.75,
    /** How much velocity gets lost every frame to due friction? */
    friction: 0.16,

    edgeOffset: null,

    createLookup: false,
    considerSize: false
};

function Graph(options)
{
    if (options instanceof Graph)
    {
        this.options = options.options;
    }
    else
    {
        this.options = assign({}, defaultOptions, options);
    }
    this.nodes = [];

    if (this.options.friction > 1)
    {
        throw new Error("Friction must be between 0 and 1");
    }

    this.prevTension = 1000;

    this.lookup = !!this.options.createLookup && {};
}

/**
 *
 * Returns a shallow copy of the graph.
 *
 * @param newNodes  new nodes array assumed to be a immutable copy of this graph's nodes.
 *
 * @returns {Graph} copy
 */
Graph.prototype.copy = function(newNodes)
{
    var graph = new Graph(this);
    graph.nodes = newNodes;

    var lookup = graph.lookup;
    for (var i = 0; i < newNodes.length; i++)
    {
        var n = newNodes[i];
        var v = this.options.createLookup;
        if (v)
        {
            lookup[n[v]] = n;
        }
    }

    return graph;
};

Graph.prototype.growRandom = function(max)
{
    if (typeof max !== "number" && max <= 0)
    {
        throw new Error("Need max count > 0");
    }

    var nodes = this.nodes;

    this.newNode();

    for (var i = 0; i < max - 1; i++)
    {
        var idx = (Math.random() * nodes.length)|0;

        var n = this.newNode();
        nodes[idx].ref.push({ id: n.id});
    }
};

Graph.prototype.newNode = function(data)
{
    var nodes = this.nodes;

    var n = {
        x: 0,
        y: 0,
        dx: 0,
        dy: 0,
        id: nodes.length,
        ref: []
    };

    if (data)
    {
        n = assign(data, n);
    }

    nodes.push(n);

    var v = this.options.createLookup;
    if (v)
    {
         this.lookup[n[v]] = n;
    }
    return n;
};


Graph.prototype.ease = function()
{
    //console.log("EASE");

    var nodes = this.nodes;

    var tension = 0;
    var tensionCount = 1;
    var force, i, n, d;

    var separationLimit = this.options.separationLimit;
    var edgeLimit = this.options.edgeLimit;
    var considerSize = this.options.considerSize;
    var centerLimit = edgeLimit;
    var separationForceMultiplier = this.options.separationForceMultiplier;

    for (i = 0; i < nodes.length; i++)
    {
        n = nodes[i];
        for (var j = nodes.length - 1; j > i; j--)
        {

            var n2 = nodes[j];
            var v = new Vector(n.x, n.y);
            v.subtract(n2);

            d = v.len();

            if (considerSize)
            {
                separationLimit = (Math.max(n2.width, n2.height) + Math.max(n.width, n.height))/2 + this.options.separationLimit;
                //console.log("separationLimit", separationLimit);
            }

            if (d < separationLimit && d != 0)
            {
                force = Math.sqrt(separationLimit - d) * separationForceMultiplier;

                v.norm(force);

                tension += force;
                tensionCount++;

                n.dx += v.x;
                n.dy += v.y;

                n2.dx -= v.x;
                n2.dy -= v.y;
            }
        }

        var refs = n.ref;
        for (j = 0; j < refs.length; j++)
        {
            n2 = nodes[refs[j].id];

            v = new Vector(n.x, n.y);
            v.subtract(n2);

            if (this.options.edgeOffset)
            {
                this.options.edgeOffset(n, j, v);
            }

            d = v.len();

            if (d > edgeLimit)
            {
                force = Math.sqrt(d - edgeLimit);
                v.norm(force);

                tension += force;
                tensionCount++;

                n.dx -= v.x;
                n.dy -= v.y;

                n2.dx += v.x;
                n2.dy += v.y;
            }
        }

        v = new Vector(n);
        d = v.len();
        if (d > centerLimit)
        {
            force = Math.sqrt(d - centerLimit) / 10;
            v.norm(force);
            n.dx += v.x;
            n.dy += v.y;
        }
    }

    for (i = 0; i < nodes.length; i++)
    {
        var node = nodes[i];

        node.x += node.dx;
        node.y += node.dy;


        var m = 1 - this.options.friction * node.weight;

        node.dx *= m;
        node.dy *= m;
    }

    var current = tension / tensionCount;
    var result = (current + this.prevTension) / 2;
    this.prevTension = current;

    //console.log("TENSION = ", result);

    return result;
};

/**
 * Calculates the distance that the graph with the given id has to all other nodes in the graph walking the edges in both
 * directions
 *
 * @param graph     graph
 * @param id        id
 * @param visited   recursive state: nodes visited
 * @param count     recursive state: current distance sum
 * @param depth     recursive state: current depth
 *
 * @returns current distance sum
 */
function distanceToAllOtherNodes(graph, id, visited, count, depth)
{
    if (!visited[id])
    {
        // if we haven't yet visited the node, count it
        count += depth;

        // and remember we were here
        visited[id] = true;


        // walk over all connected nodes
        var nodes = graph.nodes;
        var ref = nodes[id].biref;
        for (var i = 0; i < ref.length; i++)
        {
            var n2 = nodes[ref[i].id];
            // and continue the walk, now every node counts one step more than we did
            count = distanceToAllOtherNodes(graph, n2.id, visited, count, depth + 1);
        }
    }

    return count;
}


Graph.prototype.arrange = function(center)
{
    var nodes = this.nodes;

    if (!nodes.length)
    {
        return;
    }

    var i;

    for (i = 0; i < nodes.length; i++)
    {
        var node = nodes[i];
        if (!node.biref)
        {
            node.biref = node.ref.slice();

            for (var j = 0; j < nodes.length; j++)
            {
                var n2 = nodes[j];

                for (var k = 0; k < n2.ref.length; k++)
                {
                    if (n2.ref[k].id === node.id)
                    {
                        node.biref.push({ id: n2.id});
                    }
                }
            }

        }
        node.weight = 0.8 + node.biref.length;
    }

    if (!center)
    {
        var min = Infinity;
        for (i = 0; i < nodes.length; i++)
        {
            node = nodes[i];
            var sum = distanceToAllOtherNodes(this, node.id, {}, 0, 0);

            //console.log("graph distance", num);

            if (sum < min)
            {
                center = node.id;
                min = sum;
            }
        }
        //console.log("min", num, center);
    }

    nodes[center].x = 0;
    nodes[center].y = 0;
    nodes[center].depth = 0;

    var visited = {};
    var max = ring(this, visited, center, 0, TAU, 1);

    var angle = 0;


    var separationLimit = this.options.separationLimit * 2;
    for (i = 0; i < nodes.length; i++)
    {
        if (!visited[i])
        {
            node = nodes[i];
            node.x = Math.cos(angle) * separationLimit * max;
            node.y = Math.sin(angle) * separationLimit * max;
            angle += STEP;
        }
    }

    return center;
};


Graph.prototype.getNodes = function()
{
    return this.nodes;
};

Graph.prototype.clean = function()
{
    var cleaned = new Array(this.nodes.length);

    var nodes = this.nodes;
    for (var i = 0; i < nodes.length; i++)
    {
        var node = nodes[i];
        cleaned[i] = {
            x: node.x|0,
            y: node.y|0,
            ref: node.ref
        };
    }

    return cleaned;
};


Graph.prototype.getAABB = function()
{
    var nodes = this.nodes;

    var minX = Infinity, minY = Infinity;
    var maxX = -Infinity, maxY = -Infinity;

    var edgeLimit = this.options.edgeLimit / 2;

    for (var i = 0; i < nodes.length; i++)
    {
        var node = nodes[i];

        minX = Math.min(node.x - edgeLimit, minX);
        minY = Math.min(node.y - edgeLimit, minY);
        maxX = Math.max(node.x - edgeLimit, maxX);
        maxY = Math.max(node.y - edgeLimit, maxY);
        minX = Math.min(node.x + edgeLimit, minX);
        minY = Math.min(node.y + edgeLimit, minY);
        maxX = Math.max(node.x + edgeLimit, maxX);
        maxY = Math.max(node.y + edgeLimit, maxY);
    }

    return {
        minX: minX,
        minY: minY,
        maxX: maxX,
        maxY: maxY
    };
};


export default Graph
