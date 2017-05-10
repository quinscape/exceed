"use strict";

import { updateComponent } from "../../../actions/component"
import store from "../../../service/store"

import React from "react"
import i18n from "../../../service/i18n";
import ValueLink from "../../../util/value-link";
import DataGraph, { validateDataGraph } from "../../../domain/graph";
import DataCursor from "../../../domain/cursor";
import immutableUpdate from "react-addons-update";
import domainService from "../../../service/domain";
import converter from "../../../service/property-converter";
import Enum from "../../../util/enum";
import debounce from "../../../util/debounce";
import PagingComponent from "../../../ui/PagingComponent";
import renderWithContext from "../../../util/render-with-context";

export class Column extends React.Component
{
    static propTypes = {
        name: React.PropTypes.string.isRequired
    };

    render()
    {
        if (typeof this.props.children === "function")
        {
            return (
                <td>
                    { this.props.children(this.props.context) }
                </td>
            );
        }
        else
        {
            const cursor = this.props.context;
            const propertyType = cursor.getPropertyType(null);

            return (
                <td>
                    <p className="form-control-static">
                        { converter.toUser(this.props.context.get(), propertyType).value }
                    </p>
                </td>
            );
        }
    }
}

class FilterField extends React.Component
{
    handleChange = debounce(
        (value) => {
            this.props.valueLink.requestChange(value);
        },
        250
    );

    render()
    {
        return (
            <th>
                <input type="text" className="form-control" defaultValue={ this.props.valueLink.value }
                       onChange={ (ev) => {
                           this.handleChange(ev.target.value)
                       } }/>
            </th>
        );
    }
}

class Header extends React.Component
{
    toggle = (ev) =>
    {
        const currentSort = this.props.currentSortLink.value;

        if (currentSort && currentSort.length === 1 && currentSort[0] === this.props.sort)
        {
            this.props.currentSortLink.requestChange("!" + this.props.sort)
        }
        else
        {
            this.props.currentSortLink.requestChange(this.props.sort)
        }
        ev.preventDefault();
    };

    render()
    {
        const currentSort = this.props.currentSortLink.value;

        //console.log("SORT", currentSort, this.props.sort);

        let arrow = false;
        if (currentSort && currentSort.length === 1)
        {
            if (currentSort[0] === this.props.sort)
            {
                arrow = (
                    <span className="sort-indicator">
                    <span className="glyphicon glyphicon-sort-by-attributes"></span>
                </span>
                );
            }
            else if (currentSort[0] === "!" + this.props.sort)
            {
                arrow = (
                    <span className="sort-indicator">
                        <span className="glyphicon glyphicon-sort-by-attributes-alt"></span>
                    </span>
                );
            }
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
}


class DataGrid extends React.Component
{

    static propTypes = {
        orderBy: React.PropTypes.string,
        limit: React.PropTypes.number,
        result: React.PropTypes.object
    };

    changeSort = (newValue) =>
    {
        store.dispatch(
            updateComponent(this.props.id, {
                orderBy: [newValue],
                // restart paging on sort change
                offset: 0
            })
        );
    };

    setFilter(v)
    {
        store.dispatch(
            updateComponent(this.props.id, {
                filter: immutableUpdate(this.props.vars.filter, {
                    [v.name]: {$set: v.value}
                }),
                // restart paging on filter change
                offset: 0
            })
        );
    }

    setPagingOffset = (offset) =>
    {
        store.dispatch(
            updateComponent(this.props.id, {offset})
        );
    };

    renderHeader()
    {
        const currentSortLink = new ValueLink(this.props.vars.orderBy, this.changeSort);

        const kids = this.props.model.kids;
        const headers = [];
        for (let i = 0; i < kids.length; i++)
        {
            const kid = kids[i];
            if (kid.name !== "DataGrid.Column")
            {
                throw new Error("Datagrid component should only have Datagrid.Column children.");
            }

            const name = kid.attrs.name;

            //console.log("kid", name, kid);

            const column = this.props.result.columns[name];
            headers.push(
                <Header
                    key={ i }
                    currentSortLink={ currentSortLink }
                    heading={ i18n(kid.attrs.heading || (column.domainType ? column.domainType : column.type) + ":" + column.name) }
                    sort={ name }
                />
            );

        }

        return (
            <tr>
                { headers }
            </tr>
        );
    }

    renderFilter()
    {
        const activeFilters = this.props.vars.filter || {};

        let colCount = 0;

        return (
            <tr>
                {
                    this.props.model.kids.map((kidModel) =>
                    {
                        const name = kidModel.attrs.name;
                        return (
                            <FilterField
                                key={ colCount++ }
                                name={ name }
                                valueLink={
                                    new ValueLink(activeFilters[name], (value) =>
                                        {
                                            this.setFilter({
                                                name: name,
                                                value: value
                                            });
                                        }
                                    )
                                }
                            />
                        );
                    })

                }
            </tr>
        );
    }

//     onChange: function (newList, path)
//     {
// //        console.log("ONCHANGE", newList, path);
//     },

    static cursorFromData(data)
    {
        if (!data)
        {
            throw new Error("No data");
        }

        if (validateDataGraph(data))
        {
            return new DataCursor(domainService.getDomainTypes(), DataGraph(data), []);
        }
        else if (data instanceof DataCursor)
        {
            return data;
        }
        else
        {
            console.error("Cannot handle data", data);
        }
    }

    render()
    {
        //console.dir(this.props);
        const cursor = DataGrid.cursorFromData(this.props.result);
        //console.log("DATAGRID", cursor);

        let count = cursor.get().length;
        let rows;
        if (!count)
        {
            rows = <tr>
                <td colSpan={ this.props.childCount }>{ i18n("No Rows") }</td>
            </tr>
        }
        else
        {
            rows = [];

            for (let i = 0; i < count; i++)
            {
                const context = cursor.getCursor([i]);

                //console.log("CONTEXT", context);

                rows[i] = (
                    <tr key={ i }>
                        { renderWithContext(this.props.children, context) }
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
                    offsetLink={ new ValueLink(this.props.vars.offset, this.setPagingOffset) }
                    limit={ this.props.vars.limit }
                    rowCount={ this.props.result.count }
                />
            </div>
        );
    }
}

export default DataGrid;
