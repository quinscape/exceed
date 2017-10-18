import i18n from "../service/i18n"

import assign from "object-assign";

import ReactDOM from "react-dom";
import React from "react";
import Modal from "react-bootstrap/lib/Modal";
import Button from "./../ui/Button";
import Toolbar from "../components/std/form/Toolbar";
import SelectField from "../components/std/form/SelectField";
import Field from "../components/std/form/Field";
import ValueLink from "./value-link";
import { Promise } from "es6-promise-polyfill";

const DEFAULT_OPTS = {

    // default buttons
    choices: [
        i18n("Cancel"),
        i18n("Ok")
    ],

    // modal title
    title: "",
    
    // text to display in modal body
    text: "",

    // close uses the first choice (Cancel) by default
    closeChoice: 0,
    // primary choice is last choice (Do Operation) by default
    primaryChoice: -1,

    // setting this to false reverses the order of buttons displayed in the dialog. Default is left to right, setting
    // it to false turns that into right to left.
    cancelIsFirst: true
};



let dialogComponentInstance;

class DialogComponent extends React.Component
{
    state = {
        opts: null,
        inputs: null,
        resolve: null,
        modalOpen: false
    };
    
    componentDidMount()
    {
        this._buttons = [];
    }

    prompt(opts)
    {
        return new Promise((resolve, reject) =>
        {
            const inputs = {};
            const properties = opts.properties;
            for (let i = 0; i < properties.length; i++)
            {
                const domainProperty = properties[i];
                const selectValues = domainProperty.selectValues;
                if (selectValues)
                {
                    let selectValue = selectValues[selectValues.length - 1];
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
    }

    close = () => this.choose(this.getChoiceIndex(this.state.opts.closeChoice));

    submit(ev)
    {
        try
        {
            ReactDOM.findDOMNode(this._buttons[0]).focus();
            window.setTimeout(() =>
            {
                try
                {
                    this.choose(this.getChoiceIndex(this.state.opts.primaryChoice))
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
    }


    /**
     * Resolves an index expression to an actual index.
     *
     * @param numExpr   {number} index, negative values interpreted as nth absolute value from the end of the choice array
     * @returns {*}
     */
    getChoiceIndex(numExpr)
    {
        if (numExpr < 0)
        {
            return this.state.opts.choices.length + numExpr;
        }
        return numExpr;
    }


    choose(idx)
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
    }

    render()
    {
        let opts = this.state.opts;
        if (!opts)
        {
            return false;
        }

        const { properties, text, choices, cancelIsFirst } = opts;

        const numberOfChoices = choices.length;
        
        //console.log({opts});

        const primaryChoice = this.getChoiceIndex(opts.primaryChoice);

        return (
            <Modal show={ this.state.modalOpen } onHide={ this.close }>
                <Modal.Header closeButton>
                    <Modal.Title>{ opts.title }</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    {
                        text &&
                        <p>
                            { text }
                        </p>
                    }
                    <form className="form" onSubmit={ this.submit }>
                        {
                            properties && properties.map((propertyType, idx) =>
                            {
                                const selectValues = propertyType.selectValues;

                                const link = new ValueLink(this.state.inputs[propertyType.name], (value) =>
                                {
                                    const newInputs = assign({}, this.state.inputs);
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
                            {
                                choices.map((txt, idx) =>
                                    <Button
                                        ref={c => this._buttons[idx] = c}
                                        key={idx}
                                        className={ primaryChoice === idx && "btn-primary"}
                                        text={choices[idx]}
                                        onClick={e => this.choose(idx)}
                                    />
                                )
                            }
                        </Toolbar>
                    </form>
                </Modal.Body>
            </Modal>
        );
    }
}

/**
 * @name DialogResult
 * @type {{
 *     choice: number,
 *     inputs: object
 * }}
 */

/**
 * Dialog helper functions for easy procedural alerts and (extended) prompts.
 *
 *  Dialog.alert("Ey!");
 *
 *  will show an alter saying "Ey!" with an Ok button and return a promise that resolves when the user clicks that button.
 *
 *  # Extended example
 *
 *  Dialog.prompt({
 *      title: i18n("Enter Name"),
 *      choices: [ i18n("Cancel"), i18n("Create New Frobnitz")],
 *      properties: [
 *      {
 *          name: "name",
 *          type: "PlainText"
 *      }
 *  ]})
 *
 *  will create a prompt that lets the user input one plaintext value. It will resolve with an dialog result object. the
 *  choice property contains the index of the user choice, the inputs object is a map mapping the declared input names
 *  ("name" in the example above) to the value the user entered.
 */
export default {

    /**
     * Bootstrap modal alert
     *
     * @param text      text to display
     * @returns {Promise}
     */
    alert: function(text) {
        return this.prompt({
            choices: [ i18n("Ok") ],
            title: "Alert",
            properties: [],
            text: text
        });
    },

    /**
     * Prompts for user input with the given dialog options.
     *
     * @param opts.title            {string} dialog title
     * @param opts.choices          {array} array of button texts to offer as choices. (default is [i18n("Cancel"), i18n("Ok")] )
     * @param opts.text             {string} dialog body text
     * @param opts.closeChoice      {string} choice to use when the user closes the prompt ( default: 0, negative values count from the right)
     * @param opts.primaryChoice    {string} choice to use when hits return / submits the form ( default: -1, i.e. first button from the right)
     * @param opts.properties       {array} array of domainProperty with additional "selectValues" key to force <SelectField/>
     *
     * @returns {Promise.<DialogResult>} user choice and input values
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
}
