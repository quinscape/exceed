"use strict";

var extend = require("extend");
var ValueLink = require("../util/value-link");
var cx = require("classnames");

var i18n = require("../service/i18n");

function FormContext(formComponent, path, baseId)
{
    if (!formComponent || !path || !baseId)
    {
        throw new Error("Args missing:" + Array.prototype.slice.call(arguments));
    }

    var type = formComponent.getCurrentDomainType();
    if (!type)
    {
        throw new Error("No domain type");
    }

    this.formComponent = formComponent;
    this.path = path;
    this.id = baseId + "." + path;

    this.type = type;
}

extend(FormContext.prototype, {
    hasError: function()
    {
        return this.formComponent.hasErrors(this.path);
    },

    renderErrors: function()
    {
        return this.formComponent.renderErrors(this.path);
    },

    getValueLink: function()
    {
        var ctx = this;
        var path = ctx.path;
        var current = ctx.formComponent.getCurrentValue();

        return new ValueLink(current[path], function (value)
        {
            ctx.formComponent.requestFieldValueChange(path, value, ctx.id);
        });
    },

    getFormGroupClasses: function ()
    {
        return cx({
            "form-group" : true,
            "has-error" : this.hasError()
        });
    },

    getElementLabel: function ()
    {
        return i18n(this.type + "." + this.path );
    }

});

module.exports = FormContext;
