import Modal from "react-bootstrap/lib/Modal";
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
import assign from "object-assign";


import DataCursor from "../../../domain/cursor"

class EnumTypeForm extends React.Component
{

    static propTypes = {
        cursor: React.PropTypes.instanceOf(DataCursor)
    }


    componentWillReceiveProps(nextProps)
    {
        if (this.props.name !== nextProps.name)
        {
            this.setState({
                editing: 0
            })
        }
    }


    moveUp(idx)
    {
        this.props.cursor.apply(["values"], (values) => {
            var h = values[idx-1];
            values[idx - 1] = values[idx];
            values[idx] = h;

            return values;
        });
    }

    moveDown(idx)
    {
        this.props.cursor.apply(["values"], (values) => {
            var h = values[idx + 1];
            values[idx + 1] = values[idx];
            values[idx] = h;

            return values;
        });
    }

    remove(idx)
    {
        this.props.cursor.splice(["values"], [[idx,1]]);

    }

    newValue()
    {
        this.props.cursor.push(["values"], ["New"]);
    }

    render()
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
}

export default EnumTypeForm