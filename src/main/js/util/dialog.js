import FormContext from "./form-context";
const Promise = require("es6-promise-polyfill").Promise;
const i18n = require("../service/i18n");
const ValueLink = require("./value-link");
const Field = require("../components/std/form/Field");
const SelectField = require("../components/std/form/SelectField");
const Toolbar = require("../components/std/form/Toolbar");
const Button = require("./../ui/Button");
const Modal = require("react-bootstrap/lib/Modal");
const assign = require("object-assign");

const React = require("react");
const ReactDOM = require("react-dom");

const DEFAULT_OPTS = {

    // default buttons
    choices: [
        i18n("Cancel"),
        i18n("Ok")
    ],

    // close uses the leftmost choice by default
    closeChoice: 0,
    // primary choice is rightmost choice by default
    primaryChoice: -1
};

var dialogComponentInstance;

var DialogComponent = React.createClass({

    getInitialState: function ()
    {
        return {
            opts: null,
            inputs: null,
            resolve: null,
            modalOpen: false
        };
    },

    childContextTypes: {
        formContext: React.PropTypes.instanceOf(FormContext)
    },

    getChildContext: function ()
    {
        return {
            formContext: new FormContext(
                false,
                null,
                null,
                this.props.update
            )
        };
    },

    componentDidMount: function()
    {
        this._buttons = [];
    },

    prompt: function (opts)
    {
        return new Promise((resolve, reject) =>
        {
            var inputs = {};
            var properties = opts.properties;
            for (var i = 0; i < properties.length; i++)
            {
                var domainProperty = properties[i];
                var selectValues = domainProperty.selectValues;
                if (selectValues)
                {
                    var selectValue = selectValues[selectValues.length - 1];
                    inputs[domainProperty.name] = typeof selectValue === "object" ? selectValue.value : selectValue;
                }
                else
                {
                    inputs[domainProperty.name] = "";
                }
            }

            this.setState({
                opts: opts,
                inputs: inputs,
                resolve: resolve,
                modalOpen: true
            });
        });
    },

    close: function ()
    {
        this.choose(this.state.opts.closeChoice);
    },

    submit: function (ev)
    {
        try
        {
            ReactDOM.findDOMNode(this._buttons[0]).focus();
            window.setTimeout(() =>
            {
                try
                {
                    this.choose(this.getPrimaryChoice())
                }
                catch (e)
                {
                    console.error(e);
                }
            }, 10);
        }
        catch (e)
        {
            console.error(e);
        }
        ev.preventDefault();
    },

    getPrimaryChoice: function ()
    {
        var primaryChoice = this.state.opts.primaryChoice;
        if (primaryChoice < 0)
        {
            return this.state.opts.choices.length + primaryChoice;
        }
        return primaryChoice;
    },

    choose: function (idx)
    {
        this.state.resolve({
            choice: idx,
            inputs: this.state.inputs
        });

        this.setState({
            opts: null,
            inputs: null,
            modalOpen: false
        });
    },

    render: function ()
    {
        var opts = this.state.opts;
        if (!opts)
        {
            return false;
        }

        var properties = opts.properties;
        var primaryChoice = this.getPrimaryChoice();

        return (
            <Modal show={ this.state.modalOpen } onHide={ this.close }>
                <Modal.Header closeButton>
                    <Modal.Title>{ opts.title }</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <form className="form" onSubmit={ this.submit }>
                        {
                            properties && properties.map((propertyType, idx) =>
                            {
                                const selectValues = propertyType.selectValues;

                                const link = new ValueLink(this.state.inputs[propertyType.name], (value) =>
                                {
                                    var newInputs = assign({}, this.state.inputs);
                                    newInputs[propertyType.name] = value;

                                    this.setState({
                                        inputs: newInputs
                                    });
                                });

                                if (selectValues)
                                {
                                    return (
                                        <SelectField
                                            key={ idx }
                                            data={ selectValues }
                                            value={ link }
                                            propertyType={ propertyType }
                                        />
                                    );
                                }
                                else
                                {
                                    return (
                                        <Field
                                            key={ idx }
                                            autoFocus={ idx === 0 }
                                            value={ link }
                                            propertyType={ propertyType }
                                        />
                                    );
                                }
                            })
                        }
                        <Toolbar>
                            { opts.choices.map( (txt,idx) =>
                                <Button
                                    ref={ c => this._buttons[idx] = c }
                                    key={ txt }
                                    className={ primaryChoice === idx && "btn-primary" }
                                    text={ opts.choices[idx] }
                                    onClick={ e => this.choose(idx) }
                                />
                            ) }
                        </Toolbar>
                    </form>
                </Modal.Body>
            </Modal>
        );
    }
});

/**
 * Helper function for easiy creation of simple value input dialogs.
 *
 * @param title         dialog title
 * @param properties    array of domainProperties with addition selectValues property for <SelectField/> fields.
 * @returns {Promise} resolves when the user enters something
 */
module.exports = {
    /**
     * Prompts for user input with the given dialog options.
     *
     * @param opts.title        {string} dialog title
     * @param opts.okLabel      {string} label to use for the ok button (default i18n('Ok') )
     * @param opts.properties   {array} array of domainProperty with additional "selectValues" key to force <SelectField/>
     *
     * @returns {*}
     */
    prompt: function (opts)
    {
        opts = assign({}, DEFAULT_OPTS, opts);

        if (!opts.title)
        {
            return Promise.reject(new Error("Dialog needs title option"));
        }

        if (!opts.properties)
        {
            return Promise.reject(new Error("Dialog needs properties option"));
        }

        if (!dialogComponentInstance)
        {
            return Promise.reject(new Error("DialogComponent anchor not rendered."));
        }

        return dialogComponentInstance.prompt(opts);
    },
    render: function ()
    {
        return (
            <DialogComponent ref={ c =>
            {
                dialogComponentInstance = c;
            }}/>
        )
    }
};
