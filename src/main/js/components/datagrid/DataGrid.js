"use strict";

var React = require("react");

var i18n = require("../../service/i18n");

var ValueLink = require("../../util/value-link");

var DebounceMixin = require("../../mixin/debounce-mixin");
var ComplexComponent = require("../../mixin/complex-component");
var StatelessComponent = require("../../mixin/stateless-component");

var PagingComponent = require("../../ui/PagingComponent");

var Enum = require("../../util/enum");

const MAX = 1<<31;

var FilterMode = new Enum({
    CONTAINS: 1,
    EQUALS: 1,
    STARTS_WITH: 1
});


var Column = React.createClass({
    propTypes: {
        filterMode: React.PropTypes.oneOf(FilterMode.values()).isRequired,
        name: React.PropTypes.string.isRequired
    },
    getDefaultProps: function ()
    {
        return {
            filterMode: "CONTAINS"
        };
    },
    render: function ()
    {
        var renderKids = this.props.renderChildrenWithContext;
        if (!renderKids)
        {
            return (
                <td>
                    <p className="form-control-static">
                        { String(this.props.context) }
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

        if (currentSort ===  this.props.sort)
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
            rows: React.PropTypes.object
    },

    mixins: [ ComplexComponent, StatelessComponent ],

    changeSort: function (newValue)
    {
        this.updateVars({
            orderBy: newValue
        });
    },

    setFilter: function(v)
    {
        var vars = this.getVars();

        this.setVars(React.addons.update(vars, {
            filter: {
                [v.name]: { $set: v.value }
            },
            // restart paging on filter change
            offset: {$set: 0}
        }));
    },

    setPagingOffset: function(offset)
    {
        this.updateVars({
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
                    heading={ i18n(column.domainType + "." + column.name) }
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

    render: function ()
    {
        //console.log("COUNT", this.props.count);

        var queryResult = this.props.result;

        var rows = queryResult.rows.map(function (row, idx)
        {
            return (
                <tr key = { idx }>
                    { this.props.renderChildrenWithContext(row) }
                </tr>
            );

        },this);

        if (!queryResult.rows.length)
        {
            rows = <tr><td colSpan={ this.props.childCount }>{ i18n("No Rows") }</td></tr>
        }


        return (
            <div className="datagrid">
                <table className="table table-striped table-hover table-bordered">
                    <thead>
                    { this.renderHeader() }
                    </thead>
                    <tbody>
                    {  rows }
                    </tbody>
                </table>
                <PagingComponent
                    offsetLink={ new ValueLink(this.props.vars.offset, this.setPagingOffset ) }
                    limit={ this.props.vars.limit }
                    rowCount={ this.props.result.rowCount }
                    />
            </div>
        );
    }
});

DataGrid.Column = Column;

module.exports = DataGrid;
