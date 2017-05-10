const SIZE = 160;

const SEPARATION_LIMIT = SIZE / 2;
const EDGE_LIMIT = SIZE * 1.5;

const TENSION_LIMIT = Math.pow(SIZE, 0.1);

const GOLDEN = 1.61803398875;
const GOLDEN_SQUARED = GOLDEN * GOLDEN;
const GOLDEN_CUBED = GOLDEN * GOLDEN * GOLDEN;


const DomainLayout = require("./domain-layout");

const Graph = require("../../../gfx/graph");

const Enum = require("../../../util/enum");

const NodeType = new Enum({
    ENTITY: true,
    FK: true
});


function findPropertyIndexWithName(type, name)
{
    let properties = type.properties;
    for (let i = 0; i < properties.length; i++)
    {
        let domainProperty = properties[i];
        if (domainProperty.name === name)
        {
            return i;
        }
    }

    return null;
}

function fkKey(name, idx)
{
    return "fk_" + name + "_" + idx;
}

function addToGraph(graph, domainType)
{
    let domainProperty;

    const  headingLayout = DomainLayout.getHeadingLayout();

    let width = headingLayout.ppc * domainType.name.length;
    let height = headingLayout.height;
    let nameWidth = 0;

    let properties = domainType.properties;

    const propLayout = DomainLayout.getPropLayout();

    for (let i = 0; i < properties.length; i++)
    {
        domainProperty = properties[i];
        let propertyName = domainProperty.name;

        height += propLayout.height;
        let nameWidthEstimate = propLayout.ppc * propertyName.length;

        let labelText = DomainLayout.getLabelForType(domainProperty.type);
        if (!labelText)
        {
            throw new Error("No label for type'" + domainProperty.type + "'");
        }

        let decoratorWidthEstimate = propLayout.ppc * labelText.length;

        width = Math.max(width, nameWidthEstimate + decoratorWidthEstimate);
        nameWidth = Math.max(nameWidth, nameWidthEstimate);
    }

    return graph.newNode({
        type: NodeType.ENTITY,
        name: domainType.name,
        width: width + 40,
        height: height + 28,
        nameWidth: nameWidth,
        headerHeight: headingLayout.height,
        lineHeight: propLayout.height
    });
}

function setFKNodePosition(srcNode, dstNode, node)
{
    let x0 = srcNode.x + ( dstNode.x < srcNode.x ? -srcNode.width / 2 : srcNode.width / 2 );
    let y0 = srcNode.y - srcNode.height / 2 + srcNode.headerHeight + srcNode.headerHeight + node.srcPropIndex * DomainLayout.getPropLayout().height + DomainLayout.getPropLayout().height / 2;

    let x2 = dstNode.x + ( dstNode.x >= srcNode.x ? -dstNode.width / 2 : dstNode.width / 2 );
    let y2 = dstNode.y - dstNode.height / 2 + dstNode.headerHeight + dstNode.headerHeight + node.dstPropIndex * DomainLayout.getPropLayout().height + DomainLayout.getPropLayout().height / 2;

    let dx = (x2 - x0) / 2;
    let dy = (y2 - y0) / 2;

    let x1 = x0 + dx;
    let y1 = y0 + dy;

    x1 += dy / GOLDEN_CUBED;
    y1 -= dx / GOLDEN_CUBED;

    node.x = x1;
    node.y = y1;
}
/**
 * Factory for domain model graphs. Creates a graph with a node for each entity. For foreign keys, the entities are connected
 * to a node representing the arrow bezier curve control point
 *
 * @type {{create: function}}
 */
module.exports = {
    /**
     * Creates a new  graph for the domain editor
     *
     * @param positions     input positions
     * @param domainTypes   map of domain types by name
     * @param visible       map of visibility by name
     * @param minRuns       graph easing runs to perform at minimum
     *
     * @returns {Graph} new graph
     */
    create: function (positions, domainTypes, visible, minRuns)
    {
        //console.log("DomainGraph.create", positions, domainTypes, visible, minRuns);

        let graph = new Graph({
            createLookup: "name",
            separationLimit: SEPARATION_LIMIT,
            edgeLimit: EDGE_LIMIT,
            separationForceMultiplier: 2,
            considerSize: true,
            edgeOffset: (n, refIndex, v) => {
                let ref = n.ref[refIndex];

                let srcOff = DomainLayout.getPropLayout().height * ref.srcPropIndex;
                let dstOff = DomainLayout.getPropLayout().height * ref.dstPropIndex;

                if (v.x < 0)
                {
                    v.x += n.width /2;
                }
                else
                {
                    v.x -= n.width /2;
                }
                v.y += -srcOff + dstOff;
            }
        });

        let copied = false;

        for (let name in domainTypes)
        {
            if (domainTypes.hasOwnProperty(name) && visible[name])
            {
                let domainType = domainTypes[name];
                let node = addToGraph(graph, domainType);

                if (positions)
                {
                    let pos = positions[name];
                    if (pos)
                    {
                        //console.log("COPY", pos);
                        node.x = pos.x;
                        node.y = pos.y;
                        copied = true;
                    }
                }
            }
        }

        let nodes = graph.nodes;
        let lookup = graph.lookup;
        var firstFKNode = nodes.length;
        for (let i = 0; i < firstFKNode; i++)
        {
            let node = nodes[i];
            var typeName = node.name;
            let domainType = domainTypes[typeName];
            let properties = domainType.properties;
            for (let j = 0; j < properties.length; j++)
            {
                let propertyModel = properties[j];
                let foreignKey = propertyModel.foreignKey;
                if (foreignKey)
                {
                    let type = foreignKey.type;
                    let targetType = lookup[type];

                    if (targetType)
                    {
                        let srcPropIndex = j;
                        let dstPropIndex = findPropertyIndexWithName(domainTypes[type], foreignKey.property);

                        graph.newNode({
                            type: NodeType.FK,
                            name: fkKey(typeName, j),
                            width: 20,
                            height: 20,

                            srcId: i,
                            dstId: targetType.id,

                            srcPropIndex: srcPropIndex,
                            dstPropIndex: dstPropIndex
                        });

                        //console.log("FK NODE", fkNode);

                        node.ref.push({
                            id: targetType.id,
                            srcPropIndex: srcPropIndex,
                            dstPropIndex: dstPropIndex
                        });
                    }
                }
            }
        }

        if (!copied)
        {
            graph.arrange();
            let tension = Infinity;
            for (let i = 0; i < minRuns || tension > TENSION_LIMIT; i++)
            {
                tension = graph.ease();
            }

            //console.log("END TENSION = ", tension, "limit = ", TENSION_LIMIT);
        }


        // iterate over all foreign key nodes.
        for (let i = firstFKNode; i < nodes.length; i++)
        {
            let node = nodes[i];
            let name = node.name;

            var pos = positions && positions[name];
            if (pos)
            {
                node.x = pos.x;
                node.y = pos.y;
            }
            else
            {

                let srcNode = nodes[node.srcId];
                let dstNode = nodes[node.dstId];
                setFKNodePosition(srcNode, dstNode, node);
                // node.x = (srcNode.x + dstNode.x) /2 ;
                // node.y = (srcNode.y + dstNode.y) /2;
            }
        }

//        console.log("TENSION:", tension);

        return graph;

    },

    NodeType : NodeType,

    setFKNodePosition: setFKNodePosition

};
