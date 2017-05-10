const assign = require("object-assign");

const i18n = require("../../../service/i18n");
const converter = require("../../../service/property-converter");
const keys = require("../../../util/keys");

const React = require("react");
const ReactDOM = require("react-dom");

const Form = require("../../std/form/Form");
const Field = require("../../std/form/Field");
const SelectField = require("../../std/form/SelectField");
const StaticText = require("../../std/form/StaticText");
const ErrorMessages = require("../../std/form/ErrorMessages");
const Options = require("../../std/form/Options");

const Link = require("../../../ui/Link");

const Modal = require("react-bootstrap/lib/Modal");

import DataCursor from "../../../domain/cursor"

var EnumTypeForm = React.createClass({

    propTypes: {
        cursor: React.PropTypes.instanceOf(DataCursor)
    },


    componentWillReceiveProps: function (nextProps)
    {
        if (this.props.name !== nextProps.name)
        {
            this.setState({
                editing: 0
            })
        }
    },


    moveUp: function (idx)
    {
        this.props.cursor.apply(["values"], (values) => {
            var h = values[idx-1];
            values[idx - 1] = values[idx];
            values[idx] = h;

            return values;
        });
    },

    moveDown: function (idx)
    {
        this.props.cursor.apply(["values"], (values) => {
            var h = values[idx + 1];
            values[idx + 1] = values[idx];
            values[idx] = h;

            return values;
        });
    },

    remove: function (idx)
    {
        this.props.cursor.splice(["values"], [[idx,1]]);

    },

    newValue: function ()
    {
        this.props.cursor.push(["values"], ["New"]);
    },

    render: function ()
    {
        const enumTypeName = this.props.name;
        if (!enumTypeName)
        {
            return false;
        }

        const enumTypeCursor = this.props.cursor;

        const valuesCursor = enumTypeCursor.getCursor(["values"]);
        const values = valuesCursor.value;

        return (
            <div className="domain-type-form">
                <h3>{ i18n('Enum Type {0}', enumTypeName) }</h3>
                <ErrorMessages/>
                {
                    <Form data={ enumTypeCursor } horizontal={ false }>
                        <div style={{ margin: 10 }}>
                            <div className="btn-toolbar" role="toolbar">
                                <Link icon="arrow-left" text={ i18n("Close") } onClick={ e => this.props.edit(null) } accessKey="C"/>
                                <Link text={ i18n("Rename") } onClick={ e => prompt(i18n('Rename Enum Type'), enumTypeName) }/>
                                <Link icon="erase" text={ i18n("Delete") } onClick={ e => console.log(e) }/>
                            </div>
                            <div className="row">
                                <div className="col-md-6">
                                    <table className="table table-responsive">
                                        <thead>
                                        <tr>
                                            <th width="66%">Value</th>
                                            <th width="33%">Action</th>
                                        </tr>
                                        </thead>
                                        <tbody>
                                        {
                                            values.map((value, idx, array) =>
                                            {
                                                return (
                                                    <tr key={ value }>
                                                        <td>
                                                            <Field labelClass="sr-only" value={ valuesCursor.getCursor([idx]) } autoFocus={ valuesCursor.get([idx]) === "New" } />
                                                        </td>
                                                        <td>
                                                            <div className="btn-toolbar" role="toolbar">
                                                                <Link icon="arrow-up" disabled={idx == 0} onClick={ e => this.moveUp(idx) }/>
                                                                <Link icon="arrow-down" disabled={ idx === array.length -1 } onClick={ e => this.moveDown(idx) }/>
                                                                <Link icon="erase" text={ i18n('Delete') } onClick={ e => this.remove(idx) }/>
                                                            </div>
                                                        </td>
                                                    </tr>
                                                );
                                            })
                                        }
                                        <tr>
                                            <td/>
                                            <td>
                                                <Link
                                                    icon="plus"
                                                    text={ i18n('New (V)alue') }
                                                    accessKey="V"
                                                    onClick={ this.newValue }
                                                />
                                            </td>
                                        </tr>
                                        </tbody>
                                    </table>
                                </div>
                                <div className="col-md-6">
                                </div>
                            </div>
                        </div>
                    </Form>
                }
                <hr/>
            </div>
        );
    }
});

module.exports = EnumTypeForm;
