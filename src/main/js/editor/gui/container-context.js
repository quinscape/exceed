function ContainerContext(containerComponent)
{
    this.containerComponent = containerComponent;
    this.svgElem = null;
}


ContainerContext.prototype.init = function (svgElem)
{
    //console.log("INIT SVG ELEM", svgElem);
    this.svgElem = svgElem;
};


ContainerContext.prototype.toObjectCoordinates = function (x, y)
{
    var svg = this.svgElem;
    var pt = svg.createSVGPoint();
    pt.x = x;
    pt.y = y;
    pt = pt.matrixTransform(svg.getScreenCTM().inverse());

    return {
        x: pt.x,
        y: pt.y
    };
};

export default ContainerContext
