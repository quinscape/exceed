"use strict";

import { updateComponent } from "../../../actions/component";
import store from "../../../service/store";

import React from "react";
import ValueLink from "../../../util/value-link";
import DataGraph, { validateDataGraph } from "../../../domain/graph";
import DataCursor from "../../../domain/cursor";
import immutableUpdate from "immutability-helper";
import domainService from "../../../service/domain";
import propertyConverter from "../../../service/property-converter";
import debounce from "../../../util/debounce";
import PagingComponent from "../../../ui/PagingComponent";
import renderWithContext from "../../../util/render-with-context";
import cx from "classnames";

import i18n from "../../../service/i18n";

import PropTypes from 'prop-types'

import PropertySelect from "../form/PropertySelect";
/**
 * Additional classes per property type, currently used for text-alignment. The visualization of properties is
 * controlled mostly by propertyContext.renderStatic() 
 */
const COLUMN_ALIGNMENT = {
    "Currency" : "text-right",
    "Integer" : "text-right",
    "Long" : "text-right",
    "Decimal" : "text-right",
    "Boolean" : "text-center"
};

function supplyBooleanValues()
{
    return [
        <option key={ 'none' } value={ "" }>
            { i18n( '---' ) }
        </option>,
        <option key={ 'true' } value={ true }>
            { i18n( 'True' ) }
        </option>,
        <option key={ 'false' } value={ false }>
            { i18n( 'False' ) }
        </option>
    ]
}

function supplyStateMachineValues({ propertyType })
{
    const stateMachineModel = domainService.getStateMachine(propertyType.typeParam);

    const options = [
        <option key={ 'none' } value={ "" }>
            { i18n( '---' ) }
        </option>

    ];
    const states = stateMachineModel.states;

    for (let name in states)
    {
        if (states.hasOwnProperty(name))
        {
            options.push(
                <option key={ name } value={ name }>
                    { i18n( stateMachineModel.name + " " + name ) }
                </option>
            );
        }
    }

    return options;
}

function supplyEnumValues({ propertyType })
{
    const enumModel = domainService.getEnumType(propertyType.typeParam);

    const options = enumModel.values.map(
        (name,idx) =>
            <option key={ name } value={ idx }>
                { i18n( enumModel.name + " " + name ) }
            </option>
    );

    options.unshift(
        <option key={ 'none' } value={ "" }>
            { i18n( '---' ) }
        </option>
    );

    return options;
}

export class Column extends React.Component
{
    static propTypes = {
        name: PropTypes.string.isRequired
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

            const value = this.props.context.get();

            return (
                <td>
                    <p className={ cx("form-control-static", COLUMN_ALIGNMENT[propertyType.type]) }>
                        { propertyConverter.renderStatic(value, propertyType) }
                    </p>
                </td>
            );
        }
    }
}

class FilterField extends React.Component
{
    handleChange = value => {
        const { name, setFilter } = this.props;

        setFilter({
            name,
            value
        });

    };

    debouncedChange = debounce(
        this.handleChange,
        250
    );


    render()
    {

        const { value, propertyType } = this.props;

        if (propertyType.type === "State")
        {
            return (
                <td>
                    <PropertySelect
                        value={ value }
                        disabled={ false }
                        className=""
                        propertyType={ propertyType }
                        supplier={ supplyStateMachineValues }
                        onChange={ this.handleChange }
                    />
                </td>
            )
        }

        if (propertyType.type === "Enum")
        {
            return (
                <td>
                    <PropertySelect
                        value={ value }
                        disabled={ false }
                        className=""
                        propertyType={ propertyType }
                        supplier={ supplyEnumValues }
                        onChange={ this.handleChange }
                    />
                </td>
            )
        }

        if (propertyType.type === "Boolean")
        {
            return (
                <td>
                    <PropertySelect
                        value={ value }
                        disabled={ false }
                        className=""
                        propertyType={ propertyType }
                        supplier={ supplyBooleanValues }
                        onChange={ this.handleChange }
                    />
                </td>
            )
        }


        return (
            <td>
                <input type="text" className="form-control" defaultValue={ value }
                       onChange={ (ev) => {
                           this.debouncedChange(ev.target.value)
                       } }/>
            </td>
        );
    }
}

class Header extends React.Component
{

    toggle = (ev) =>
    {
        const { currentSortLink, sort } = this.props;

        const currentSort = currentSortLink.value;

        if (currentSort && currentSort.length === 1 && currentSort[0] === sort)
        {
            currentSortLink.requestChange("!" + sort)
        }
        else
        {
            currentSortLink.requestChange(sort)
        }
        ev.preventDefault();
    };

    render()
    {
        const { currentSortLink, sort, heading } = this.props;
        const currentSort = currentSortLink.value;

        //console.log("SORT", currentSort, sort);

        let arrow = false;
        if (currentSort && currentSort.length === 1)
        {
            if (currentSort[0] === sort)
            {
                arrow = (
                    <span className="sort-indicator">
                        <span className="glyphicon glyphicon-sort-by-attributes" />
                    </span>
                );
            }
            else if (currentSort[0] === "!" + sort)
            {
                arrow = (
                    <span className="sort-indicator">
                        <span className="glyphicon glyphicon-sort-by-attributes-alt" />
                    </span>
                );
            }
        }

        return (
            <th>
                <a className="header" href="#sort" onClick={ this.toggle }>
                    { heading }
                </a>
                { arrow }
            </th>
        );
    }
}


class DataGrid extends React.Component
{

    static propTypes = {
        orderBy: PropTypes.string,
        limit: PropTypes.number,
        result: PropTypes.object
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

    setFilter = (v) =>
    {
        const { id, vars} = this.props;
        store.dispatch(
            updateComponent(id, {
                filter: immutableUpdate(vars.filter, {
                    [v.name]: {$set: v.value}
                }),
                // restart paging on filter change
                offset: 0
            })
        );
    };

    setPagingOffset = (offset) =>
    {
        store.dispatch(
            updateComponent(this.props.id, {offset})
        );
    };

    renderHeader()
    {
        const { vars, model, result } = this.props;
        const currentSortLink = new ValueLink(vars.orderBy, this.changeSort);

        const kids = model.kids;
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

            const column = result.columns[name];
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

    renderFilter(cursor)
    {
        const { vars, model } = this.props;

        const activeFilters = vars.filter || {};

        let colCount = 0;



        return (
            <tr>
                {
                    model.kids.map( kidModel =>
                        {
                            const name = kidModel.attrs.name;

                            const columnCursor = cursor.getCursor([0, name]);

                            return (
                                <FilterField
                                    key={ colCount++ }
                                    name={ name }
                                    propertyType={ columnCursor.getPropertyType() }
                                    value={ activeFilters[name] }
                                    setFilter={ this.setFilter }
                                />
                            );
                        }
                    )
                }
            </tr>
        );
    }

    static cursorFromData(data)
    {
        if (!data)
        {
            throw new Error("No data");
        }

        if (data instanceof DataCursor)
        {
            return data;
        }
        else if (validateDataGraph(data))
        {
            return new DataCursor(domainService.getDomainData(), data, []);
        }
        else
        {
            throw new Error("Cannot handle data", data);
        }
    }

    render()
    {
        const { result, children, vars } = this.props;

        const childCount = React.Children.count(children);

        const cursor = DataGrid.cursorFromData(result);
        //console.log("DATAGRID", cursor);

        let count = cursor.get().length;
        let rows;
        if (count === 0)
        {
            rows = (
                <tr>
                    <td colSpan={ childCount }>{ i18n("No Rows") }</td>
                </tr>
            );
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
                        { renderWithContext(children, context) }
                    </tr>
                )
            }
        }

        return (
            <div className="datagrid">
                <table className="table table-striped table-hover table-bordered">
                    <thead>
                    { this.renderHeader() }
                    { this.renderFilter(cursor) }
                    </thead>
                    <tbody>
                    {  rows }
                    </tbody>
                </table>
                <PagingComponent
                    offsetLink={ new ValueLink(vars.offset, this.setPagingOffset) }
                    limit={ vars.limit }
                    rowCount={ result.count }
                />
            </div>
        );
    }
}

export default DataGrid;
