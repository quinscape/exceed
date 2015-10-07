"use strict";

var React = require("react/addons");

var DataGrid = React.createClass({
    render: function ()
    {
        return ( <div/> );

    }
});

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
        name: React.PropTypes.string.isRequired,
        value: React.PropTypes.any
    },
    getDefaultProps: function ()
    {
        return {
            filterMode: "CONTAINS"
        };
    },
    render: function ()
    {
        var kids = this.props.children;

        if (React.Children.count(kids) === 0)
        {
            return (
                <td>
                    <p className="form-control-static">
                        { this.props.value }
                    </p>
                </td>
            );
        }
        return (
            <td>
                { ri.cloneWithContext(kids, this.props.value) }
            </td>
        );
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
                    { i18n( ri.prop("heading")) }
                </a>
                { arrow }
            </th>
        );
    }
});


var DataGrid = React.createClass({

    //statics: {
    //    vars: {
    //        orderBy: ri.prop("orderBy"),
    //        filter: {},
    //        offset: 0,
    //        limit: ri.prop("limit")
    //    },
    //    queries: {
    //        rows: {
    //            from: ri.prop("type"),
    //            orderBy: ri.var("orderBy"),
    //            filter: ri.var("filter"),
    //            limit: ri.var("limit"),
    //            offset: ri.var("offset")
    //        },
    //        count: {
    //            from: ri.prop("type"),
    //            filter: ri.var("filter"),
    //            count: true
    //        }
    //    }
    //},

    propTypes: {
        type: React.PropTypes.string.isRequired,
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
        var colCount = 0;

        var type = this.props.type;

        var currentSortLink = new ValueLink( ri.var("orderBy"), this.changeSort);
        return (
            <tr>
                {
                    React.Children.map(this.props.children, function (kid)
                    {
                        if (kid.type !== Column)
                        {
                            throw new Error("Datagrid component should only have Datagrid.Column children.");
                        }
                        return (
                            <Header
                                key={colCount++}
                                currentSortLink={ currentSortLink }
                                heading={ type + "." + kid.props.name }
                                sort={ kid.props.name }
                                />
                        );
                    }, this)

                }
            </tr>
        );
    },

    renderFilter: function ()
    {
        var colCount = 0;
        var activeFilters = ri.var("filter");

        var dataGridComponent = this;

        return (
            <tr>
                {
                    React.Children.map(this.props.children, function (kid)
                    {
                        return (
                            <FilterField key={ colCount++ }
                                         name={ kid.props.name }
                                         valueLink={ new ValueLink( activeFilters[kid.props.name], function(value){
                                    dataGridComponent.setFilter({name: kid.props.name, value: value});
                                }) }
                                />
                        );
                    }, this)

                }
            </tr>
        );
    },

    render: function ()
    {
        //console.log("COUNT", this.props.count);

        var colCount;
        var rowCount = 0;
        var rows = this.props.rows.map(function (row)
        {
            rowCount++;
            colCount = 0;

            return (
                <tr key={rowCount}>
                    {
                        React.Children.map(this.props.children, function (kid)
                        {
                            return React.cloneElement(kid, {
                                key: colCount++,
                                value: row[kid.props.name]
                            });
                        }, this)
                    }
                </tr>
            )

        },this);

        if (!rows.length)
        {
            rows = <tr><td colSpan={ React.Children.count(this.props.children) }>{ i18n("No Rows") }</td></tr>
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
                    offsetLink={ new ValueLink(ri.var("offset"), this.setPagingOffset ) }
                    limit={ ri.var("limit") }
                    rowCount={ this.props.count }
                    />
            </div>
        );
    }
});

DataGrid.Column = Column;

module.exports = DataGrid;
