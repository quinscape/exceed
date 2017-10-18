import React from "react";
import cx from "classnames";
import DataCursor from "../../../domain/cursor"
import { validateDataGraph } from "../../../domain/graph"
import { getFormConfig } from "../../../reducers/form-state"
import domainService from "../../../service/domain"
import store from "../../../service/store"

import PropTypes from 'prop-types'

import renderWithContext from "../../../util/render-with-context";


class Form extends React.Component
{
    static propTypes = {
        data: PropTypes.object.isRequired,
        horizontal: PropTypes.bool,
        labelClass: PropTypes.string,
        wrapperClass: PropTypes.string,
        path: PropTypes.array
    };

    static defaultProps = {
        horizontal: true,
        path: [0]
    };

    cursorFromData(data)
    {
        if (!data)
        {
            throw new Error("No data");
        }

        if (validateDataGraph(data))
        {
            return new DataCursor(domainService.getDomainData(), data, this.props.path);
        }
        else if (data instanceof DataCursor)
        {
            return data;
        }
        else if (data._type)
        {
            return new DataCursor(
                domainService.getDomainData(),
                objectToDataGraph(data),
                this.props.path
            );
        }
        else
        {
            console.error("Cannot handle data", data);
        }
    }

    onSubmit = ev => {

        const primaryButtons = document.querySelectorAll(".btn-primary");
        if (primaryButtons.length && !primaryButtons[0].disabled)
        {
            primaryButtons[0].focus();
        }
        else
        {
            const tmp = document.createElement("input");
            document.body.appendChild(tmp);
            tmp.focus();
            document.body.removeChild(tmp);
        }

        ev.preventDefault();
    };

    // onChange: function (newGraph, path)
    // {
    //     //console.log("onChange", JSON.stringify(newGraph), path);
    //
    //     this.setState({
    //         dataGraph: newGraph
    //     });
    // },

    render()
    {
        const state = store.getState();

        const cursor = DataCursor.from(this.props.data);
        const cfg = getFormConfig(state, this.props.id);

        return (
            <form className={ cx( cfg.horizontal ? "form-horizontal" : "form") } onSubmit={ this.onSubmit }>
                { renderWithContext(this.props.children, cursor) }
            </form>
        );
    }
}

export default Form;

