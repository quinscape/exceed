"use strict";

var React = require("react");

var i18n = require("../../../service/i18n");

var ValueLink = require("../../../util/value-link");

var DebounceMixin = require("../../../mixin/debounce-mixin");
var ComponentUpdateMixin = require("../../../mixin/component-update-mixin");

var DataGraph = require("../../../util/data-graph");
var DataCursor = require("../../../util/data-cursor");

var PagingComponent = require("../../../ui/PagingComponent");

var Enum = require("../../../util/enum");

var converter = require("../../../service/property-converter");
var domainService = require("../../../service/domain");

var immutableUpdate = require("react-addons-update");

var Column = React.createClass({
    propTypes: {
        name: React.PropTypes.string.isRequired
    },
    render: function ()
    {
        var renderKids = this.props.renderChildren;
        if (!renderKids)
        {
            var cursor = this.props.context;

            var propertyType = cursor.getPropertyType(null);

            return (
                <td>
                    <p className="form-control-static">
                        { converter.toUser(this.props.context.value, propertyType).value }
                    </p>
                </td>
            );
        }
        else
        {
            return (
                <td>
                    { renderKids(this.props.context) }
                </td>
            );
        }
    }
});

var FilterField = React.createClass({

    mixins: [ DebounceMixin ],

    handleChange: function(ev)
    {
        this.debounce(function(value)
        {
            this.props.valueLink.requestChange(value);

        }, 250, ev.target.value);
    },

    render: function ()
    {
        return (
            <th>
                <input type="text" className="form-control" defaultValue={ this.props.valueLink.value } onChange={ this.handleChange } />
            </th>
        );
    }
});

var Header = React.createClass({

    toggle: function(ev)
    {
        var currentSort = this.props.currentSortLink.value;

        if (currentSort && currentSort.length == 1 && currentSort[0] ===  this.props.sort)
        {
            this.props.currentSortLink.requestChange("!" + this.props.sort)
        }
        else
        {
            this.props.currentSortLink.requestChange(this.props.sort)
        }
        ev.preventDefault();
    },

    render: function ()
    {
        var currentSort = this.props.currentSortLink.value;

        var arrow = false;
        if (currentSort ===  this.props.sort)
        {
            arrow = <span className="sort-indicator">{"\u21D3"}</span>
        }
        else if (currentSort === "!" + this.props.sort)
        {
            arrow = <span className="sort-indicator">{"\u21D1"}</span>
        }

        return (
            <th>
                <a className="header" href="#sort" onClick={ this.toggle }>
                    { this.props.heading }
                </a>
                { arrow }
            </th>
        );
    }
});


var DataGrid = React.createClass({

    propTypes: {
        orderBy: React.PropTypes.string,
        limit: React.PropTypes.number,
        result: React.PropTypes.object
    },

    mixins: [ ComponentUpdateMixin ],

    changeSort: function (newValue)
    {
        this.updateComponent({
            orderBy: [ newValue ],
            // restart paging on sort change
            offset: 0
        });
    },

    setFilter: function(v)
    {
        this.updateComponent({
                filter: immutableUpdate( this.props.vars.filter, {
                    [v.name]: { $set: v.value}
                }),
                // restart paging on filter change
                offset: 0
        });
    },

    setPagingOffset: function(offset)
    {
        this.updateComponent({
            offset: offset
        });
    },

    renderHeader: function ()
    {
        var currentSortLink = new ValueLink( this.props.vars.orderBy, this.changeSort);

        var kids = this.props.model.kids;
        var headers = [];
        for (var i=0; i < kids.length; i++)
        {
            var kid = kids[i];
            if (kid.name !== "DataGrid.Column")
            {
                throw new Error("Datagrid component should only have Datagrid.Column children.");
            }

            var name = kid.attrs.name;

            //console.log("kid", name, kid);

            var column = this.props.result.columns[name];
            headers.push(
                <Header
                    key={ i }
                    currentSortLink={ currentSortLink }
                    heading={ i18n(kid.attrs.heading || (column.domainType  ? column.domainType : column.type) + ":" + column.name) }
                    sort={ name }
                    />
            );

        }

        return (
            <tr>
                { headers }
            </tr>
        );
    },

    renderFilter: function ()
    {
        var colCount = 0;
        var activeFilters = this.props.vars.filter || {};

        return (
            <tr>
            {
                this.props.model.kids.map((kidModel) =>
                {
                    var name = kidModel.attrs.name;
                    return (
                        <FilterField
                            key={ colCount++ }
                            name={ name }
                            valueLink={
                                new ValueLink( activeFilters[name], (value) =>
                                    {
                                        this.setFilter({name: name, value: value});
                                    }
                                )
                            }
                        />
                    );
                })

            }
            </tr>
        );
    },


    onChange: function (newList, path)
    {
//        console.log("ONCHANGE", newList, path);
    },

    cursorFromData: function(data)
    {
        if (!data)
        {
            throw new Error("No data");
        }

        var types = domainService.getDomainTypes();

        if (DataGraph.prototype.isRawDataGraph(data))
        {
            return new DataGraph(
                domainService.getDomainTypes(),
                data,
                this.onChange
            );
        }
        else if (data instanceof DataCursor)
        {
            return data;
        }
        else
        {
            console.error("Cannot handle data", data);
        }
    },

    render: function ()
    {
        //console.log("DATAGRID");
        //console.dir(this.props);
        var resultList = this.cursorFromData(this.props.result);

        var count = resultList.rootObject.length;
        var rows;
        if (!count)
        {
            rows = <tr><td colSpan={ this.props.childCount }>{ i18n("No Rows") }</td></tr>
        }
        else
        {
            rows = [];

            for (var i = 0; i < count; i++)
            {
                rows[i] = (
                    <tr key = { i }>
                        { this.props.renderChildren ? this.props.renderChildren( resultList.getCursor([i])) : this.props.children }
                    </tr>
                )
            }
        }

        return (
            <div className="datagrid">
                <table className="table table-striped table-hover table-bordered">
                    <thead>
                    { this.renderHeader() }
                    { this.renderFilter() }
                    </thead>
                    <tbody>
                    {  rows }
                    </tbody>
                </table>
                <PagingComponent
                    offsetLink={ new ValueLink(this.props.vars.offset, this.setPagingOffset ) }
                    limit={ this.props.vars.limit }
                    rowCount={ this.props.result.count }
                    />
            </div>
        );
    }
});

DataGrid.Column = Column;

module.exports = DataGrid;
