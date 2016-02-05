var React = require("react");

var extend = require("extend");

var classes = require("classnames");
var ValueLink = require("../util/value-link");
var ajax = require("../service/ajax");

var Promise = require("es6-promise-polyfill").Promise;

var i18n = require("../service/i18n");

var Modal = require("react-bootstrap/lib/Modal");

var Form = require("../components/form/Form");
var Switch = require("../components/form/FormSwitch");
var Case = require("../components/form/FormCase");
var Button = require("../components/form/Button");
var TextField = require("../components/form/TextField");
var SelectField = require("../components/form/SelectField");
var ListEditor = require("../components/form/ListEditor");
var Checkbox = require("../components/form/Checkbox");

var _keys = require("lodash.keys");


function createDataList(model)
{
    return {
        types: {},
        fields: {
            "name": {
                validate: function (ctx, field, value)
                {
//                    console.log("name", ctx,field,value);

                    return {ok:true};
                }
            },
            "Property.type" : {
                validate: function (ctx, field, value)
                {
//                    console.log("Property.Type", ctx,field,value);

                    return {ok:true};
                }
            },
            "Property.typeParam" : {
                validate: function (ctx, field, value)
                {
//                    console.log("Property.Type", ctx,field,value);

                    return {ok:true};
                }
            }
        },
        rows: [model]
    };
}


/**
 * Modal popup for editing DomainTypes.
 *
 * @type {*|Function}
 */
var EntityModal = React.createClass({

    closeModal: function ()
    {
        console.log("close modal");
        this.props.openLink.requestChange(false);
    },

    onCancel: function (ev)
    {
        this.closeModal();
        ev.preventDefault();
    },

    onSave: function (ev)
    {
        this.closeModal();
        ev.preventDefault();
    },

    render: function ()
    {
        var model = this.props.modelLink.value;

        var domainTypeName = model && model.name;
        console.log("EntityModal", domainTypeName);

        var dataList = createDataList(model);

        var openLink = this.props.openLink;
        return (

            <Modal show={ openLink.value } onHide={ function()
            {
                openLink.requestChange(false)
            } }>
                <Modal.Header closeButton>
                    <Modal.Title>Modal heading</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <Form dataList={ dataList } horizontal={ true }>
                        <div className="row">
                            <div className="col-md-12">
                                <TextField labelClasses="col-md-2" wrapperClasses="col-md-3" containerClasses="form-group-sm" name="name"/>
                            </div>
                        </div>
                        <h4>Properties</h4>
                        <ListEditor name="properties" fieldPrefix="Property.">
                            <div className="row">
                                <div className="col-md-1">
                                    <ListEditor.RowControls/>
                                </div>
                                <div className="col-md-5">
                                    <TextField labelClasses="col-md-5" wrapperClasses="col-md-7" containerClasses="form-group-sm"  name="name"/>
                                    <div className="col-md-7 col-md-push-5">
                                        <Checkbox name="required" label="Required"/>
                                    </div>
                                </div>
                                <div className="col-md-5">
                                    <SelectField labelClasses="col-md-5" wrapperClasses="col-md-7" containerClasses="form-group-sm" name="type" options={ _keys(this.props.propertyTypes).sort() } />
                                    <Switch name="type">
                                        <Case values={['RichText', 'PlainText']}>
                                            <TextField labelClasses="col-md-5" wrapperClasses="col-md-7" containerClasses="form-group-sm"  name="maxLength" />
                                        </Case>
                                        <Case value={ 'UUID' }>
                                            <TextField labelClasses="col-md-5" wrapperClasses="col-md-7" containerClasses="form-group-sm"  name="maxLength" disabled={ true } />
                                        </Case>
                                        <Case value="Enum">
                                            <SelectField
                                                name="typeParam"
                                                labelClasses="col-md-5"
                                                wrapperClasses="col-md-7"
                                                containerClasses="form-group-sm"
                                                options={ _keys(this.props.enums).sort() }/>
                                        </Case>
                                    </Switch>
                                </div>
                            </div>
                            <hr/>
                        </ListEditor>
                        <h4>Foreign Keys</h4>
                        <Modal.Footer>
                            <Button onClick={this.close}>Close</Button>
                        </Modal.Footer>
                    </Form>
                    </Modal.Body>
                </Modal>
        );

    }
});

module.exports = EntityModal;
