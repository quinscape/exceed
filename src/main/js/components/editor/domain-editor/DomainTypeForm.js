import Modal from "react-bootstrap/lib/Modal";
import Button from "../../../ui/Button";
import Link from "../../../ui/Link";
import Options from "../../std/form/Options";
import ErrorMessages from "../../std/form/ErrorMessages";
import StaticText from "../../std/form/StaticText";
import SelectField from "../../std/form/SelectField";
import Field from "../../std/form/Field";
import Form from "../../std/form/Form";
import ReactDOM from "react-dom";
import React from "react";
import keys from "../../../util/keys";
import converter from "../../../service/property-converter";
import i18n from "../../../service/i18n";
import cx from "classnames";
import assign from "object-assign";

import PropTypes from 'prop-types'

import DataCursor from "../../../domain/cursor"

function mapToName(type)
{
    return type.name;
}

function findPropertyIndexWithName(type, name)
{
    var properties = type.properties;
    for (var i = 0; i < properties.length; i++)
    {
        var domainProperty = properties[i];
        if (domainProperty.name === name)
        {
            return i;
        }
    }

    return null;
}

function findPropertyWithName(type, name)
{
    var idx = findPropertyIndexWithName(type, name);
    if (idx === null)
    {
        return null;
    }
    return type.properties[idx];
}

class DomainTypeForm extends React.Component
{

    static propTypes = {
        cursor: PropTypes.instanceOf(DataCursor)
    }

    edit(index)
    {
        this.props.editingPropLink.requestChange(index);
    }

    moveUp(idx)
    {
        this.props.cursor.apply(["properties"], (props) => {
            var h = props[idx-1];
            props[idx - 1] = props[idx];
            props[idx] = h;

            return props;
        });
    }

    moveDown(idx)
    {
        this.props.cursor.apply(["properties"], (props) => {
            var h = props[idx + 1];
            props[idx + 1] = props[idx];
            props[idx] = h;

            return props;
        });
    }

    newProperty(fk)
    {

        var newProp = {
            name: "",
            type: "PlainText",
            typeParam: null,
            maxLength: 0
        };

        if (fk)
        {
            var domainType = this.props.domainTypes[this.props.domainTypeNames[0]];

            var foreignKey = {
                type: domainType.name,
                property: "id"
            };

            var idProp = findPropertyWithName(domainType, "id");

            newProp.foreignKey = foreignKey;
            newProp.type = idProp.type;
            newProp.typeParam = idProp.typeParam;
            newProp.maxLength = idProp.maxLength;
        }

        this.props.cursor.push("properties", [newProp]);

        this.props.editingPropLink.requestChange(this.props.cursor.get(["properties", "length"]) - 1);
        if (this._propNameField)
        {
            this._propNameField.getInputField().focus();
        }
    }

    validateDomainPropertyName(ctx, id, name)
    {
        var currentType = this.props.cursor.value;
        var idx = findPropertyIndexWithName(currentType, name);
        if (idx !== null && idx !== this.props.editingPropLink.value)
        {
            ctx.signalError(id, i18n('Property Name Already Exists'));
            return false;
        }
        return true;
    }

    removeProperty(index)
    {
        //console.log("REMOVE", currentProps, index);
        this.props.cursor.splice(["properties"], [[index, 1]]);
    }

    render()
    {
        const domainTypeName = this.props.name;
        const domainTypeCursor = this.props.cursor;
        const properties = domainTypeCursor.get(["properties"]);

        if (!domainTypeName)
        {
            return false;
        }
        var  editing = this.props.editingPropLink.value;
        if (editing >= properties.length)
        {
            editing = properties.length - 1;
        }

        const propertyCursor = domainTypeCursor.getCursor(["properties", editing]);
        const foreignKey = propertyCursor.get(['foreignKey']);

        //console.log("FOREIGN KEY: ", foreignKey);

        const propertyTypeCursor = propertyCursor.getCursor([ 'type' ]);
        const propertyTypeParamCursor = propertyCursor.getCursor([ 'typeParam' ]);
        const currentType = propertyTypeCursor.value;

        //console.log("TYPE PARAM", propertyTypeParamCursor);

        var domainTypes = this.props.domainTypes;
        var domainTypeNames = this.props.domainTypeNames;

        var foreignKeyPropCursor = foreignKey && propertyCursor.getCursor(['foreignKey', 'property'], (newGraph, path) => {

//            console.log("foreignKeyPropCursor", newGraph, path);

            var newKey = newGraph.getCursor(foreignKeyPropCursor.path.slice(0,-1)).value;

            var targetProperty = findPropertyWithName(domainTypes[newKey.type], newKey.property);
            if (targetProperty)
            {
                return newGraph
                    .getCursor(propertyCursor.path, false)
                    .apply(null, (prop) =>
                    {

                        var newProp = assign({}, prop);

                        newProp.type = targetProperty.type;
                        newProp.typeParam = targetProperty.typeParam;
                        newProp.maxLength = targetProperty.maxLength;

                        return newProp;
                    });
            }
        });

        return (
            <div className="domain-type-form">
                <h3>{ i18n('Domain Type {0}', domainTypeName) }</h3>
                <ErrorMessages/>
                {
                    <Form data={ domainTypeCursor } horizontal={ false }>
                        <div style={{ margin: 10 }}>
                            <div className="btn-toolbar" role="toolbar">
                                <Button icon="arrow-left" text={ i18n("Close") } onClick={ e => this.props.edit(null) } accessKey="C"/>
                                <Button icon="plus" text={  i18n('New (P)roperty') } accessKey="P" onClick={ e => this.newProperty(false) }/>
                                <Button icon="plus" text={  i18n('New (F)oreign Key') } accessKey="F" onClick={ e => this.newProperty(true) }/>
                                <Button text={ i18n("Rename") } onClick={ e => prompt(i18n('Rename Domain Type'), domainTypeName) }/>
                                <Button icon="erase" text={ i18n("Delete") } onClick={ e => this.props.remove(domainTypeName) }/>
                            </div>
                            <div className="row">
                                <div className="col-md-4">
                                    <Field value={ domainTypeCursor.getCursor([ "description" ]) }/>
                                </div>
                                <div className="col-md-4">
                                    <SelectField data={ this.props.storageOptions } value={ domainTypeCursor.getCursor([ "storage" ]) }/>
                                </div>
                                <div className="col-md-4">
                                </div>
                            </div>
                            <h4>{ i18n('Properties') }</h4>
                            <div className="row">
                                <div className="col-md-4">
                                    <table className="table table-responsive table-striped table-hover">
                                        <thead className="sr-only">
                                            <tr>
                                                <th>{ i18n("Action") }</th>
                                                <th>{ i18n("Name") }</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                        {
                                            properties.map( (prop, idx, array) =>
                                                <tr key={ prop.name } className={ idx === editing && "selected" }>
                                                    <td width="25%">
                                                        <Link
                                                            icon="arrow-up"
                                                            disabled={ idx === 0 }
                                                            onClick={ e => this.moveUp(idx) }
                                                        />
                                                        <Link
                                                            icon="arrow-down"
                                                            disabled={ idx === array.length -1 }
                                                            onClick={ e => this.moveDown(idx) }
                                                        />
                                                    </td>
                                                    <td width="75%">
                                                        <Link
                                                            className={ cx( "case-sensitive", prop.required && "prop-required") }
                                                            icon={ prop.foreignKey ? "link" : "space" }
                                                            text={ prop.name }
                                                            onClick={ e => this.edit(idx) }
                                                        />
                                                    </td>
                                                </tr>
                                            )
                                        }
                                        </tbody>
                                    </table>
                                </div>
                                <div className="col-md-8">
                                    <Field
                                        ref={ component => this._propNameField = component }
                                        value={ domainTypeCursor.getCursor(['properties', editing, 'name']) }
                                        validate={ this.validateDomainPropertyName }
                                    />
                                    <SelectField data={ converter.propertyTypes() } value={ propertyTypeCursor } disabled={ !!foreignKey } />
                                    { currentType == "PlainText" &&  <Field value={ domainTypeCursor.getCursor(['properties', editing, 'maxLength']) } disabled={ !!foreignKey }/> }
                                    {
                                        (
                                            currentType == "DomainType" ||
                                            currentType == "Map" ||
                                            currentType == "List"
                                        ) &&  <SelectField label={ i18n('Domain Type') } data={ domainTypeNames } value={ domainTypeCursor.getCursor(['properties', editing, 'typeParam']) } />
                                    }
                                    {
                                        currentType == "Enum"  &&  <SelectField label={ i18n('Enum Type') } data={ this.props.enumTypes } value={ propertyTypeParamCursor } />
                                    }
                                    <Field value={ domainTypeCursor.getCursor(['properties', editing, 'required']) }/>
                                    {
                                        foreignKey &&
                                        <div>
                                            <SelectField label={ i18n('Target Domain Type') } data={ domainTypeNames } value={ domainTypeCursor.getCursor(['properties', editing, 'foreignKey', 'type']) } />
                                            <SelectField label={ i18n('Target Property') } data={ foreignKey && this.props.domainTypes[foreignKey.type].properties.map(mapToName) } value={ foreignKeyPropCursor } />
                                        </div>
                                    }
                                    <div className="btn-toolbar" role="toolbar">
                                        <Button icon="erase" text="Delete Property" onClick={ e => this.removeProperty(editing) } disabled={ properties.length == 1 } />
                                    </div>
                                </div>
                            </div>
                        </div>
                    </Form>
                }
                <hr/>
            </div>
        );
    }
}

export default DomainTypeForm
