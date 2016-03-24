var immutableUpdate = require("react-addons-update");

var count = 0;

/**
 * Creates a new Form context
 *
 * @param dataList          dataList instance
 * @param horizontal        true if horizontal form
 * @param labelClass        default label classes
 * @param wrapperClass      defaul wrapper classes
 * @constructor
 */
function FormContext( dataList, horizontal, labelClass, wrapperClass, errorsLink)
{
    this.id = ++count;
    this.fieldIdCount = 0;

    this.dataList = dataList;
    this.horizontal = horizontal;
    this._labelClass = labelClass;
    this._wrapperClass = wrapperClass;
    this.errorsLink = errorsLink;
}

FormContext.prototype.nextId = function ()
{
    var id = ++this.fieldIdCount;
    return "f" + this.id + "-" + id;
};


FormContext.prototype.labelClass = function (component)
{
    return component.props.labelClass || this._labelClass;
};

FormContext.prototype.wrapperClass = function (component)
{
    return component.props.wrapperClass || this._wrapperClass;
};

FormContext.prototype.signalError = function (id, error)
{
    var newErrors = immutableUpdate(this.errorsLink.value, {
        [id] : { $set: error }
    });

    this.errorsLink.requestChange(newErrors);
};

FormContext.prototype.hasError = function (id)
{
    var errors = this.errorsLink.value;
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
    var errors = this.errorsLink.value;
    return errors.hasOwnProperty(id) && errors[id];
};

module.exports = FormContext;
