import React from "react"
import cx from "classnames"
import propertyConverter from "../../../service/property-converter"

import SVGLayout from "../../../gfx/svg-layout"
import GUIContainer from "../../../editor/gui/GUIContainer"
import GUIContext from "../../../editor/gui/gui-context"

import Popover from "react-bootstrap/lib/Popover"
import GUIElement from "../../../editor/gui/GUIElement";
import UIState from "../../../editor/gui/ui-state";
import DocumentClick from "../../../util/DocumentClick";

const{ TextSize } = SVGLayout;

const SIZE_LABEL = TextSize.NORMAL;

// lazy initialized from constants above
let LAYOUT_LABEL;
let FONT_SIZE_LABEL;

const TAU = Math.PI*2;
const TO_DEGREE_FACTOR = 360 / TAU;

function initLayout()
{
    const exampleTextSizes = SVGLayout.getExampleTextSizes();

    LAYOUT_LABEL = exampleTextSizes[SIZE_LABEL];

    FONT_SIZE_LABEL = SVGLayout.getFontSize(SIZE_LABEL);
}

function CartesianGrid(props)
{
    const { chartConfig } = props;
    const { x, y, width, height, max, propertyType, round } = chartConfig;

    const scale = height / max;

    const rounded = Math.floor(max/round) * round;

    const maxLabel = propertyConverter.toUser( rounded, propertyType);

    const x2 = x + width;
    const y2 = y + height;

    const scaleMarkers = [];

    let current = rounded;
    while (current > 0)
    {
        const markerY = y + height - current * scale;
        scaleMarkers.push(
            <path key={ current } d={ "M"+ x + "," + markerY + " L" + x2 + "," + markerY }/>
        );

        if (current === rounded)
        {
            scaleMarkers.push(
                <text key="label" x={ x + 8 } y={ markerY + LAYOUT_LABEL.height/5 } fontSize={ FONT_SIZE_LABEL }>
                    { maxLabel }
                </text>
            )
        }

        current -= round;
    }

    return (
        <g className="chart-grid">
            <path d={ "M"+ x + "," + y2 + " L" + ( x2 + 25 ) + "," + y2 }/>
            <path d={ "M"+ x + "," + ( y - 25 ) + " L" + x + "," + y2 }/>
            { scaleMarkers }
        </g>
    )
}

class Series extends React.Component
{
    onUpdate = () => this.forceUpdate();

    render()
    {
        const { chartConfig, query, onFocus } = this.props;
        const { x, y, width, height, max } = chartConfig;
        const { data } = query;

        const step = (width / data.rootObject.length);
        const barWidth = (step * 0.95)|0;

        const array = query.data.rootObject;

        const scale = height / max;

        return (

            <g className="graph-series">

                {
                    array.map( (row, idx) => {

                        const v = row.value * scale;

                        const bx = x + idx * step;
                        const by = y + height - v;

                        const id = query.className + "-" + idx;

                        const uiState = GUIContext.getElementState(id, UIState.NORMAL);


                        return (
                            <GUIElement
                                key={ id }
                                id={ id }
                                position={{ x: bx, y: by }}
                                className={ cx("bar", uiState.toLowerCase()) }
                                draggable={ false }
                                onUpdate={ this.onUpdate }
                                onInteraction={ () => onFocus(query, idx) }
                            >
                                <rect
                                    x={ bx }
                                    y={ by }
                                    width={ barWidth }
                                    height={ v }
                                />
                                <text x={ bx } y= { y + height + LAYOUT_LABEL.height } fontSize={ FONT_SIZE_LABEL}>{ row.label }</text>
                            </GUIElement>
                        );
                    })
                }
            </g>
        );
    }
}

function findMaximum(queries)
{
    let max = -Infinity;
    for (let i = 0; i < queries.length; i++)
    {
        const query = queries[i];
        const array = query.data.rootObject;

        for (let i = 0; i < array.length; i++)
        {
            const row = array[i];
            max = Math.max(row.value, max);
        }
    }

    return max;
}

function InfoWindow(props)
{
    const { query, index, chartConfig } = props;
    const { x, y, width, height, max, propertyType, chartId } = chartConfig;

    const { data } = query;
    const step = (width / data.rootObject.length);

    const scale = height / max;

    const row = data.rootObject[index];
    const { label, value } = row;
    return (
        <Popover
            id={ chartId + "-info" }
            placement="top"
            positionLeft={ x + step * index - 44 }
            positionTop={ height - value * scale }
            title={ query.name }
        >
            { label } <br/>
            Value: { propertyConverter.renderStatic(value, propertyType) }
        </Popover>
    );
}

const BarChart = DocumentClick(
    class extends React.Component {

    state = {
        info : null
    };

    onDocumentClick = () =>
        this.setState({
            info: null
        });

    updatePopover = (query, index) =>
        this.setState({
            info: {
                query,
                index
            }
        });

    render()
    {
        if (!LAYOUT_LABEL)
        {
            initLayout();
        }

        const { id, width, height, queries, round } = this.props;
        const { info } = this.state;

        const size = 0.9;
        const graphWidth = width * size;
        const graphHeight = height * size;

        const max = findMaximum(queries);

        const chartConfig = {
            x: width / 2 - graphWidth / 2,
            y: height / 2 - graphHeight / 2,
            width: graphWidth,
            height: graphHeight,
            max,
            round,
            propertyType: queries[0].data.columns.value,
            chartId : id
        };


        return (
            <div id={ id } className="bar-chart">

                <GUIContainer
                    width={ width }
                    height={ height }
                    centerX={ width/2 }
                    centerY={ height/2 }
                    zoom={ false }
                    pan={ false }
                >
                    <CartesianGrid
                        chartConfig={ chartConfig }
                    />
                    {
                        queries.map(
                            query =>
                                <Series
                                    key={ query.className }
                                    chartConfig={ chartConfig }
                                    query={ query }
                                    onFocus={ this.updatePopover }
                                />
                        )
                    }
                </GUIContainer>
                {
                    info &&
                    <InfoWindow
                        chartConfig={ chartConfig }
                        {... info }
                    />
                }
            </div>
        )
    }
});

export default BarChart
