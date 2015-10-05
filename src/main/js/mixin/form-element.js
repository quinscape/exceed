"use strict";

var FormContext = require("../util/form-context");

var FormElement = {
    propType: {
        path: React.PropTypes.string.isRequired,
        formContext: React.PropTypes.instanceOf(FormContext)
    },
    statics:
    {
        _FormElement_ : true
    }
};

module.exports = FormElement;
