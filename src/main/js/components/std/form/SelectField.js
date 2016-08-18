const React = require("react");

const FormElement =  require("./FormElement");

const DataGraph =  require("../../../util/data-graph");
const DataCursor =  require("../../../util/data-cursor");


var SelectField = FormElement(React.createClass({

    getInputField: function ()
    {
        return this._input;
    },

    options: function ()
    {
        var input = this.props.data;
        var array;
        if (DataGraph.prototype.isRawDataGraph(input))
        {
            //console.log("RENDER DATA LIST OPTIONS");
            array = input.rootObject;
            var display = this.props.display;
            var value = this.props.value;

            return ( array.map(
                    display ?
                        o => <option key={ o[value] } value={ o[value] }>{ o[display] }</option> :
                        o => <option key={ o[value] } value={ o[value] }>{ o[value] }</option>
                )
            );
        }
        else if (input instanceof DataCursor)
        {
            //console.log("GET CURSOR");
            array = input.value;

        }
        else if (input && input.length && input[0] !== undefined)
        {
            //console.log("GET ARRAY");
            array = input;
        }

        if (array)
        {
            //console.log("RENDER ARRAY");

            var firstElement = array[0];
            if (firstElement && typeof firstElement == "object")
            {
                return (
                    array.map(
                        (o, idx) => <option key={ idx } value={ o.value }>{ o.display }</option>
                    )
                );
            }
            else
            {
                return (
                    array.map(
                        (o, idx) => <option key={ idx } value={ o }>{ o }</option>
                    )
                );
            }
        }

        throw new Error("Unhandled input: " + input);
    },

    render: function ()
    {
        var value = this.props.valueLink.value;

        //console.log("SELECT VALUE", value);

        return (
            <select
                id={ this.props.id }
                ref={ elem => this._input = elem}
                className="form-control"
                value ={ value }
                disabled={ this.props.disabled }
                onChange={ (ev) => this.props.onChange( ev.target.value) }
            >
                { this.options(value) }
            </select>
        )
    }
}));

module.exports = SelectField;

