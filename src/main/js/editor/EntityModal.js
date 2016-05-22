var React = require("react");

var assign = require("object.assign").getPolyfill();

var classes = require("classnames");
var ValueLink = require("../util/value-link");
var ajax = require("../service/ajax");

var Promise = require("es6-promise-polyfill").Promise;

var i18n = require("../service/i18n");

var Modal = require("react-bootstrap/lib/Modal");

//var Form = require("../components/form/Form");
//var Switch = require("../components/form/FormSwitch");
//var Case = require("../components/form/FormCase");
//var Button = require("../components/form/Button");
//var TextField = require("../components/form/TextField");
//var SelectField = require("../components/form/SelectField");
//var ListEditor = require("../components/form/ListEditor");
//var Checkbox = require("../components/form/Checkbox");

var _keys = require("lodash.keys");

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

        return (
            <div className="container-fluid">
/*
                <Form dataList={ dataList } horizontal={ true }>
                    <div className="row">
                        <div className="col-md-12">
                            <TextField labelClasses="col-md-2" wrapperClasses="col-md-3" containerClasses="form-group-sm" name="name"/>
                        </div>
                    </div>
                    <h4>Properties</h4>
                    <hr/>
                    <ListEditor name="properties" fieldPrefix="Property.">
                        <TextField labelClasses="col-md-4" wrapperClasses="col-md-8" name="name"/>
                        <SelectField labelClasses="col-md-4" wrapperClasses="col-md-8" name="type" options={ _keys(this.props.propertyTypes).sort() } />
                        <Switch name="type">
                            <Case values={['RichText', 'PlainText']}>
                                <TextField labelClasses="col-md-4" wrapperClasses="col-md-8" name="maxLength" />
                            </Case>
                            <Case value={ 'UUID' }>
                                <TextField labelClasses="col-md-4" wrapperClasses="col-md-8" name="maxLength" disabled={ true } />
                            </Case>
                            <Case value="Enum">
                                <SelectField
                                    name="typeParam"
                                    labelClasses="col-md-4"
                                    wrapperClasses="col-md-8"
                                    options={ _keys(this.props.enums).sort() }/>
                            </Case>
                        </Switch>
                        <div className="col-md-12 col-md-push-4">
                            <Checkbox name="required" label="Required"/>
                        </div>
                        <hr/>
                    </ListEditor>
    */
                    <h4>Foreign Keys</h4>
                    <Modal.Footer>
                        <Button onClick={this.close}>Close</Button>
                    </Modal.Footer>
                </Form>
            </div>
        );
    }
});

module.exports = EntityModal;
