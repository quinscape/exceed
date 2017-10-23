import React from "react";
import assign from "object-assign";
import PropTypes from 'prop-types'

function stylesEqual(a, b)
{
    for (var k in a)
    {
        if (a.hasOwnProperty(k))
        {
            if (a[k] !== b[k])
            {
                return false;
            }
        }
    }
    return true;
}


class SymbolLayer extends React.Component
{

    shouldComponentUpdate(nextProps)
    {
        return (
            this.props.transform !== nextProps.transform ||
            !stylesEqual(this.props.style, nextProps.style) ||
            this.props.content !== nextProps.content
        );
    }

    render()
    {
        return (
            <g
                transform={ this.props.transform }
                style={ this.props.style }
                dangerouslySetInnerHTML={ {
                    __html : this.props.content
                } }
            />
        )
    }
};

class Symbol extends React.Component
{
    static propTypes = {
        symbols: PropTypes.object.isRequired,
        name: PropTypes.string.isRequired,
        layer: PropTypes.string,
        x: PropTypes.number.isRequired,
        y: PropTypes.number.isRequired,
        transform: PropTypes.string,
        style: PropTypes.object
    }

    shouldComponentUpdate(nextProps)
    {
        return (
            this.props.x !== nextProps.x ||
            this.props.y !== nextProps.y ||
            this.props.name !== nextProps.name ||
            this.props.layer !== nextProps.layer ||
            this.props.transform !== nextProps.transform ||
            this.props.symbols !== nextProps.symbols ||
            this.props.style !== nextProps.style
        );
    }

    render()
    {
        const symbolName = this.props.name;
        const symbol = this.props.symbols[symbolName];
        const layer = this.props.layer;

        //console.log("SYMBOL", symbol);

        const effectiveStyle = !this.props.style ? symbol.styles[symbolName] : assign(
            {},
            this.props.style,
            symbol.styles[symbolName]
        );

        var groups = [];

        for (let i = 0; i < symbol.groups.length; i++)
        {
            let currentLayer = symbol.layers[i];

            var centerX = (symbol.aabb.minX + symbol.aabb.maxX)/2;
            var centerY = (symbol.aabb.minY + symbol.aabb.maxY)/2;

            const symbolTransform = symbol.transforms[i];
            const localTransform = "translate(" + ( -centerX + this.props.x ) + "," + (-centerY + this.props.y) + ")";
            const effectiveTransform = symbolTransform ? symbolTransform + " " + localTransform : localTransform;

            if (!layer || currentLayer === layer)
            {
                groups.push(
                    <SymbolLayer
                        key={ currentLayer }
                        transform={ effectiveTransform }
                        style={ effectiveStyle }
                        content={ symbol.groups[i] }
                    />
                );
            }
        }
        return (
            <g>
                { groups }
            </g>
        );
    }
}

export default Symbol

