import React from "react"
import cx from "classnames"

import FormElement from "./FormElement";
import Modal from "react-bootstrap/lib/Modal";
import PagingComponent from "../../../ui/PagingComponent";
import Toolbar from "./Toolbar";
import ValueLink from "../../../util/value-link";

import i18n from "../../../service/i18n";
import store from "../../../service/store";
import propertyConverter from "../../../service/property-converter";
import { updateComponent } from "../../../actions/component";

const domainService = require("../../../service/domain")

export function Property(props)
{
    return false;
}

function renderTargetLabel(graph, target, propModels) {
    const {columns} = graph;


    if (!target)
    {
        return (<em>{ "\u00a0" + i18n("---") }</em>)
    }
    else
    {
        //console.log("TARGET", target);


        const l = ["\u00a0"];
        for (let i = 0; i < propModels.length; i++)
        {
            const propModel = propModels[i];

            if (propModel.name === "[String]")
            {
                l.push(propModel.attrs.value + " ");
            }
            else
            {
                const attrName = propModel.attrs.name;

                const component = propertyConverter.renderStatic(target[attrName], columns[attrName]);
                l.push(React.cloneElement(component, { key: i }), " ");
            }
        }
        return l;
    }
}

/**
 * Form field part of the input
 */
class TargetField extends React.PureComponent {
    render()
    {
        const {id, icon, disabled, target, propModels, candidates } = this.props;

        return (
            <div className="input-group">
                {
                    icon &&
                    <div className="input-group-addon">
                        <span className={ cx("glyphicon glyphicon-" + icon, "text-info") }/>
                    </div>
                }
                <p
                    className={ cx("form-control-static target", disabled && "disabled") }
                    onClick={ !disabled ? this.props.openModal : null }
                >
                    { renderTargetLabel(candidates, target, propModels) }
                </p>
                <span className="input-group-btn">
                    <button
                        className="btn btn-default"
                        type="button"
                        disabled={ disabled }
                        onClick={ this.props.openModal }
                    >
                        &hellip;
                    </button>
                </span>
            </div>
        );
    }
}


class Target extends React.PureComponent
{
    onSelect = ev => {
        this.props.onSelect(this.props.target.id);
        ev.preventDefault();
    };

    render()
    {
        const {icon, target, candidates, propModels} = this.props;

        //console.log({target});
        return (
            <tr>
                <td>
                    <a
                        href="#select"
                        className="btn btn-link"
                        onClick={ this.onSelect }
                    >
                        <span className="glyphicon glyphicon-circle-arrow-down"/>
                        { "  " }
                        <span className="fk-target">
                        {
                            icon && <span className={"glyphicon glyphicon-" + icon}/>
                        }
                        {
                            "\u00a0"
                        }
                        {
                            renderTargetLabel(candidates, target, propModels)
                        }
                        </span>
                    </a>
                </td>
            </tr>
        );

    }
}

class TargetList extends React.PureComponent
{
    onFilterChange = ev => this.props.onFilterChange( ev.target.value );
    onSubmit = ev => {

        const { onSubmit } = this.props;

        if (onSubmit)
        {
            onSubmit( ev.target.value );
        }
        ev.preventDefault();
    };

    render()
    {

        const {candidates, icon, propModels, filter, onSelect } = this.props;

        return (
            <div className="fk-targets">
                <form className="form-inline" onSubmit={ this.onSubmit }>
                    <input
                        type="text"
                        className="form-control"
                        placeholder={ i18n("Filter Targets") }
                        defaultValue={ filter }
                        autoFocus={ true }
                        onChange={ this.onFilterChange }
                    />
                </form>
                <table className="table table-striped table-hover table-bordered">
                    <tbody>

                    {
                        candidates.rootObject.map((target, idx) =>
                            <Target
                                key={ idx }
                                icon={ icon }
                                target={ target }
                                propModels={ propModels }
                                candidates={ candidates }
                                onSelect={ onSelect }
                            />
                        )
                    }
                    </tbody>
                </table>
            </div>
        );
    }
}

const FKSelect = FormElement(class extends React.PureComponent {

    static displayName = "FKSelect";

    state = {
        modalOpen: false
    };

    getInputField()
    {
        return this._input;
    }

    onChange = ev => this.props.onChange(ev.target.value);

    openModal = ev => {

        const {id, vars, propertyType} = this.props;
        //console.log({id, vars, propertyType});

        const {foreignKey} = propertyType;

        if (!foreignKey)
        {
            throw new Error("Target property has no foreign key");
        }

        this.setState({modalOpen: true});
    };

    closeModal = ev => this.setState({modalOpen: false});

    selectNone = () => this.onSelect(null);

    onSelect = targetId => {

        this.setState({
            modalOpen: false
        }, () => {
            this.props.onChange(targetId);

            return store.dispatch(
                updateComponent(
                    this.props.modelId,
                    {
                    }
                )
            )

        });
    };

    onFilterChange = filter => {

        const { modelId } = this.props;

        return store.dispatch(
            updateComponent(
                modelId,
                {
                    filter
                }
            )
        );
    };

    setPagingOffset = offset => {

        const { modelId } = this.props;

        return store.dispatch(
            updateComponent(
                modelId,
                {
                    offset
                }
            )
        );
    };


    render()
    {
        const { candidates, disabled, icon, id, limit, model, propertyType, title, value, vars } = this.props;
        //console.log("RENDER FKSelect", { candidates, icon, id, limit, model, propertyType, title, value, vars });

        const target = candidates && candidates.rootObject.filter( t => t.id === value)[0];

        const propModels = model.kids;
        
        return (
            <div className="fk-select">
                <TargetField
                    ref={elem => this._input = elem}
                    id={ id }
                    icon={ icon }
                    disabled={ disabled }
                    target={ target }
                    type={ propertyType.foreignKey.type }
                    propModels={ propModels }
                    candidates={ candidates }
                    openModal={ this.openModal }
                />
                <Modal
                    show={ this.state.modalOpen }
                    onHide={ this.closeModal }
                >
                    <Modal.Header closeButton>
                        <Modal.Title>
                            {
                                title || i18n("Select FK Target")
                            }
                        </Modal.Title>
                    </Modal.Header>
                    <Modal.Body>
                        {
                            this.state.modalOpen &&
                            <TargetList
                                icon={ icon }
                                candidates={ candidates }
                                propModels={ propModels }
                                componentId={ id }
                                onSelect={ this.onSelect }
                                filter={ vars.filter }
                                onFilterChange={ this.onFilterChange }
                                onSubmit={ this.onSubmit }
                            />
                        }
                        <PagingComponent
                            offsetLink={ new ValueLink(vars.offset, this.setPagingOffset) }
                            limit={ limit }
                            rowCount={ candidates.count }
                        />
                        <Toolbar>
                            {
                                !propertyType.required &&
                                    <button type="button" className="btn btn-default" onClick={ this.selectNone }>
                                        {i18n("Select None")}
                                    </button>
                            }
                            <button type="button" className="btn btn-default" onClick={ this.closeModal }>
                                { i18n("Close") }
                            </button>
                        </Toolbar>
                    </Modal.Body>
                </Modal>
            </div>
        );
    }
});

export default FKSelect
