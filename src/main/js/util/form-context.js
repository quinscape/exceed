import assign from "object-assign"

let count = 0;

class FormContext {
    /**
     * Creates a new Form context
     *
     * @param horizontal        {boolean}true if horizontal form
     * @param labelClass        {string} default label classes
     * @param wrapperClass      {string} default wrapper classes
     * @param updateAction      {function} action producer (cursor,newValue) => action
     * @constructor
     */
    constructor(horizontal, labelClass, wrapperClass, updateAction)
    {
        //console.log("NEW FORM-CONTEXT");

        this.id = ++count;
        this.fieldIdCount = 0;

        this.horizontal = horizontal;
        this._labelClass = labelClass;
        this._wrapperClass = wrapperClass;
        this.errors = {};
        this._errorMessages = null;

        this.update = updateAction;

        this.timerId = false;
    }

    nextId()
    {
        const id = ++this.fieldIdCount;
        return "f" + this.id + "-" + id;
    }

    labelClass(component)
    {

        if (this.horizontal)
        {
            return component.props.labelClass || this._labelClass;
        }

        return "";
    }

    wrapperClass(component)
    {
        return component.props.wrapperClass || this._wrapperClass;
    }

    signalError(id, error)
    {
        const newErrors = assign({}, this.errors);
        newErrors[id] = error;
        this.errors = newErrors;

        //console.log("ERRORS", this.errors);

        if (this._errorMessages)
        {
            this._errorMessages.forceUpdate();
        }
    }

    hasError(id)
    {
        const errors = this.errors;
        if (id)
        {
            return errors.hasOwnProperty(id) && !!errors[id];
        }
        else
        {
            for (let name in errors)
            {
                if (errors.hasOwnProperty(name) && errors[name])
                {
                    return true;
                }
            }
            return false;
        }
    }

    getErrorMessage(id)
    {
        const errors = this.errors;
        const messages = errors.hasOwnProperty(id) && errors[id];

        //console.log("getErrorMessage", id, messages);

        return messages;
    }

    deregister(id)
    {
        const errors = this.errors;
        delete errors[id];
    }

}

export default FormContext
