var immutableUpdate = require("react-addons-update");
const assign = require("object-assign");

var count = 0;

/**
 * Creates a new Form context
 *
 * @param horizontal        true if horizontal form
 * @param labelClass        default label classes
 * @param wrapperClass      defaul wrapper classes
 * @param errorsLink        ValueLink for the errors map.
 * @constructor
 */
function FormContext(horizontal, labelClass, wrapperClass)
{
    this.id = ++count;
    this.fieldIdCount = 0;

    this.horizontal = horizontal;
    this._labelClass = labelClass;
    this._wrapperClass = wrapperClass;
    this.errors = {};
    this._errorMessages = null;
}

FormContext.prototype.nextId = function ()
{
    var id = ++this.fieldIdCount;
    return "f" + this.id + "-" + id;
};


FormContext.prototype.labelClass = function (component)
{
    var classes = component.props.labelClass || this._labelClass;

    if (this.horizontal)
    {
        return classes.replace(/col-.*?\b/g, " ");
    }

    return classes;
};

FormContext.prototype.wrapperClass = function (component)
{
    return component.props.wrapperClass || this._wrapperClass;
};

FormContext.prototype.signalError = function (id, error)
{
    var newErrors = assign({}, this.errors);
    newErrors[id] = error;
    this.errors = newErrors;

    if (this._errorMessages)
    {
        this._errorMessages.forceUpdate();
    }
};

FormContext.prototype.hasError = function (id)
{
    var errors = this.errors;
    if (id)
    {
        return errors.hasOwnProperty(id) && !!errors[id];
    }
    else
    {
        for (var name in errors)
        {
            if (errors.hasOwnProperty(name) && errors[name])
            {
                return true;
            }
        }
        return false;
    }
};

FormContext.prototype.getErrorMessage = function (id)
{
    var errors = this.errors;
    return errors.hasOwnProperty(id) && errors[id];
};

FormContext.prototype.deregister = function (id)
{
    var errors = this.errors;
    delete errors[id];
};

module.exports = FormContext;
