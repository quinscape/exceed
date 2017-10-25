import React from "react"
import cx from "classnames"
import keys from "../../../util/keys"

import DocumentClick from "../../../util/DocumentClick";

import SVGLayout from "../../../gfx/svg-layout"
import GUIContext from "../../../editor/gui/gui-context"
import GUIContainer from "../../../editor/gui/GUIContainer"
import GUIElement from "../../../editor/gui/GUIElement"
import UIState from "../../../editor/gui/ui-state";
import Popover from "react-bootstrap/lib/Popover"

import propertyConverter from "../../../service/property-converter";

const{ TextSize } = SVGLayout;

const SIZE_LABEL = TextSize.NORMAL;
const SIZE_INFO_TITLE = TextSize.LARGER;
const SIZE_INFO_TEXT = TextSize.SMALL;

// lazy initialized from constants above
let LAYOUT_LABEL;
let LAYOUT_INFO_TITLE;
let LAYOUT_INFO_TEXT;
let FONT_SIZE_LABEL;
let FONT_SIZE_INFO_TITLE;
let FONT_SIZE_INFO_TEXT;

const TAU = Math.PI*2;
const TO_DEGREE_FACTOR = 360 / TAU;

function initLayout()
{
    const exampleTextSizes = SVGLayout.getExampleTextSizes();

    LAYOUT_LABEL = exampleTextSizes[SIZE_LABEL];
    LAYOUT_INFO_TITLE = exampleTextSizes[SIZE_INFO_TITLE];
    LAYOUT_INFO_TEXT = exampleTextSizes[SIZE_INFO_TEXT];

    FONT_SIZE_LABEL = SVGLayout.getFontSize(SIZE_LABEL);
    FONT_SIZE_INFO_TITLE = SVGLayout.getFontSize(SIZE_INFO_TITLE);
    FONT_SIZE_INFO_TEXT = SVGLayout.getFontSize(SIZE_INFO_TEXT);
}



function buildData(queries)
{
    const labels = {};
    const names = [];
    const titles = [];
    for (let i = 0; i < queries.length; i++)
    {
        const query = queries[i];
        const { rootObject } = query.data;

        titles.push(query.name);
        names.push(query.className);

        for (let j = 0; j < rootObject.length; j++)
        {
            labels[rootObject[j].label] = true;
        }
    }

    const sorted = keys(labels).sort();

    const series = [];

    let max = 0;
    for (let i = 0; i < queries.length; i++)
    {
        const query = queries[i];
        const { rootObject } = query.data;

        const data = new Array(sorted.length);
        for (let j = 0; j < sorted.length; j++)
        {
            const label = sorted[j];

            let value = 0;
            for (let k = 0; k < rootObject.length; k++)
            {
                const row = rootObject[k];
                if (row.label === label )
                {
                    value = row.value;
                }
            }

            data[j] = value;
            if (value > max)
            {
                max = value;
            }
        }

        series.push(data);
    }

    return { names, titles, labels: sorted, series, max };

}

function Labels(props)
{
    const { labels} = props;

    const { halfWidth, halfHeight } = props.chartConfig;

    const angleStep = TAU / labels.length;

    const labelRadius = halfHeight *  0.95;
    const invLabelRadius = halfHeight *  0.975;

    return (
        <g>
            {
                labels.map((label, idx) => {
                    const radians = (idx * angleStep);
                    const degrees = radians * TO_DEGREE_FACTOR;

                    const labelWidth = LAYOUT_LABEL.estimateWidth(label);

                    const shouldFlip = degrees > 90 && degrees < 270;
                    const r = shouldFlip ? invLabelRadius : labelRadius;

                    const x = Math.sin(radians) * r;
                    const y = -Math.cos(radians) * r;

                    const transform = "translate(" + x + ", " + y + ") rotate(" + (shouldFlip ? degrees + 180 : degrees ) + "," + halfWidth + ", " + halfHeight + ")";

                    return (

                        <g
                            key={ label }
                            transform={ transform }
                        >
                            <text
                                x={ halfWidth - labelWidth/2 }
                                y={ halfHeight }
                                fontSize={ FONT_SIZE_LABEL }
                            >
                                { label }
                            </text>
                        </g>
                    );
                })
            }
        </g>
    )
}

function DataSet(props)
{
    const { name, title, dataSet, max, labels, onInteraction } = props;

    const { width, height, halfWidth, halfHeight } = props.chartConfig;

    const angleStep = TAU / dataSet.length;

    const scale = halfHeight *  0.9 / max;

    const infos = [];

    let path = "";

    for (let i = 0, angle = 0; i < dataSet.length; i++, angle += angleStep)
    {
        const value = dataSet[i];

        // angle 0 is -radius, 0
        const x = halfWidth + Math.sin(angle) * value * scale;
        const y = halfHeight - Math.cos(angle) * value * scale;

        path += ( i === 0 ? "M" : "L") + x + " " + y;

        const id = name + "-" + labels[i];

        infos.push(
            <PointInfo
                key={ id }
                id={ id }
                position={ { x , y } }
                name={ name }
                title={ title }
                label={ labels[i] }
                value={ value }
                onInteraction={ onInteraction }
            />
        )
    }

    path += "Z";

    return (
        <g>
            <path className={ "radar-" + name } d={ path }/>
            { infos }
        </g>
    );
}

class Grid extends React.Component
{

    onUpdate = () =>
    {
        this.forceUpdate();
    };

    render()
    {
        const { count, close } = this.props;

        const { width, height, halfWidth, halfHeight } = this.props.chartConfig;

        const angleStep = TAU / count;

        const radius = halfHeight *  0.9;

        let maxValueRing = "";
        let halfValueRing = "";
        let spokes = "";

        for (let i = 0, angle = 0; i < count; i++, angle += angleStep)
        {

            // angle 0 is -radius, 0
            const sx = Math.sin(angle) * radius;
            const sy = -Math.cos(angle) * radius;

            const x = halfWidth + sx;
            const y = halfHeight + sy;
            const x2 = halfWidth + sx/2;
            const y2 = halfHeight + sy/2;

            maxValueRing += ( i === 0 ? "M" : "L") + x + " " + y;
            halfValueRing += ( i === 0 ? "M" : "L") + x2 + " " + y2;
            spokes += "M" + halfWidth + " " + halfHeight + " L" + x + " " + y;

        }

        maxValueRing += "Z";
        halfValueRing += "Z";

        return (
            <g className="chart-grid">
                <path d={ spokes }/>
                <path d={ maxValueRing }/>
                <path d={ halfValueRing }/>
            </g>
        );

    }
}


class PointInfo extends React.Component
{
    onUpdate = () => {
        this.forceUpdate();
    };

    onInteraction = () => {

        const { title, label, value, position } = this.props;

        this.props.onInteraction(title, label, value, position);
    };

    render()
    {
        const { id, position } = this.props;

        const uiState = GUIContext.getElementState(id, UIState.NORMAL);

        return (
            <GUIElement
                id={ id }
                className={ cx("point-info", uiState.toLowerCase()) }
                position={ position }
                onUpdate={ this.onUpdate }
                onInteraction={ this.onInteraction }
                draggable={ false }
            >
                <circle r="10" cx={ position.x } cy={ position.y }/>
            </GUIElement>
        )
    }
}

class InfoWindow extends  React.Component
{
    render()
    {
        const { name, label, value, chartConfig, position } = this.props;

        const { chartId, propertyType } = chartConfig;

        return (
            <Popover
                id={ chartId + "-info" }
                placement="right"
                positionLeft={ position.x + 14 }
                positionTop={ position.y + 22 }
                title={ name }
            >
                { label } <br/>
                Value: { propertyConverter.renderStatic(value, propertyType) }
            </Popover>
        );

    }
}

function Legend(props)
{
    const { names, titles } = props;

    const { width, height, halfWidth, halfHeight } = props.chartConfig;

    return (
        <g className="legend">
            {
                names.map((name,idx) => {

                    const yPos = height - 10 - ( names.length - idx) * LAYOUT_INFO_TEXT.height;

                    return (
                        <g key={name}
                           className={"legend-" + name}
                        >
                            <rect
                                x={ 10 }
                                y={ yPos - LAYOUT_INFO_TEXT.height + 5 }
                                width={ 10 }
                                height={ 10 }
                            />

                            <text
                                x={ 30 }
                                y={ yPos }
                                fontSize={ SIZE_INFO_TEXT }
                            >
                                { titles[idx] }
                            </text>
                        </g>
                    );
                })

            }
        </g>
    );
}

const RadarChart = DocumentClick(
    class extends React.Component {

        state = {
            currentInfo: null
        };

        onInteraction = (name, label, value, position) => {

            this.setState({
                currentInfo: {
                    name,
                    label,
                    value,
                    position
                }
            });

        };

        onDocumentClick = () => {

            this.setState({
                currentInfo: null
            });

        };

        render()
        {
            if (!LAYOUT_LABEL)
            {
                initLayout();
            }

            const {id, width, height, queries} = this.props;

            const chartConfig = {
                width,
                height,
                halfWidth: width / 2,
                halfHeight: height / 2,
                chartId: id,
                propertyType: queries[0].data.columns.value
            };

            const {currentInfo} = this.state;

            const data = buildData(queries);

            const {names, titles} = data;

            return (
                <div id={id} className="radar-chart">
                    <GUIContainer
                        chartConfig={chartConfig}
                        width={width}
                        height={height}
                        centerX={width / 2}
                        centerY={height / 2}
                        zoom={false}
                        pan={false}
                    >
                        <Legend
                            chartConfig={chartConfig}
                            names={names}
                            titles={titles}
                        />
                        {
                            <Grid
                                chartConfig={chartConfig}
                                count={data.labels.length}
                            />
                        }
                        {
                            data.series.map((dataSet, idx) => {
                                    const name = names[idx];
                                    const title = titles[idx];
                                    return <DataSet
                                        key={name}
                                        name={name}
                                        title={title}
                                        chartConfig={chartConfig}
                                        dataSet={dataSet}
                                        labels={data.labels}
                                        titles={data.labels}
                                        max={data.max}
                                        onInteraction={this.onInteraction}
                                    />;
                                }
                            )

                        }
                        {
                            <Labels
                                chartConfig={chartConfig}
                                labels={data.labels}
                            />
                        }
                    </GUIContainer>
                    {
                        currentInfo &&
                        <InfoWindow
                            chartConfig={chartConfig}
                            {...currentInfo}
                        />
                    }
                </div>
            )
        }
    }
);

export default RadarChart
