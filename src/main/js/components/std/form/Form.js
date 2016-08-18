var React = require("react");
var cx = require("classnames");

var DataGraph = require("../../../util/data-graph");
var DataCursor = require("../../../util/data-cursor");
var Scope = require("../../../service/scope");
var domainService = require("../../../service/domain");

var FormContext = require("../../../util/form-context");

var LinkedStateMixin = require("react-addons-linked-state-mixin");



function objectToDataGraph(input)
{
    var typeName = input._type;
    var domainType = Scope.objectType(typeName);

    var cols = {};
    var obj = {};
    for (var name in input)
    {
        if (input.hasOwnProperty(name))
        {
            if (name === "_type")
            {
                obj[name] = input[name];
            }
            else
            {
                var qualified = typeName + "." + name;
                obj[qualified] = input[name];
                cols[qualified] = {
                    type: typeName,
                    name: name
                };
            }
        }
    }

    return {
        type : "ARRAY",
        types: {
            [typeName] : domainType
        },
        columns: cols,
        rootObject: [ obj ],
        count: 1
    };
}

var Form = React.createClass({

    mixins: [ LinkedStateMixin ],

    contextTypes: {
        formContext: React.PropTypes.instanceOf(FormContext)
    },

    // childContextTypes: {
    //     formContext: React.PropTypes.instanceOf(FormContext)
    // },

    propTypes: {
        data: React.PropTypes.object.isRequired,
        horizontal: React.PropTypes.bool,
        labelClass: React.PropTypes.string,
        wrapperClass: React.PropTypes.string,
        index: React.PropTypes.number
    },

    getDefaultProps: function ()
    {
        return {
            horizontal: true,
            labelClass: "col-md-2",
            wrapperClass: "col-md-4",
            index: 0
        };
    },

    cursorFromData: function(data)
    {
        if (!data)
        {
            throw new Error("No data");
        }

        var types = domainService.getDomainTypes();

        if (DataGraph.prototype.isRawDataGraph(data))
        {
            return new DataGraph(types, data, this.onChange).getCursor([this.props.index]);
        }
        else if (data instanceof DataCursor)
        {
            return data;
        }
        else if (data._type)
        {
            return new DataGraph(types, objectToDataGraph(data), this.onChange).getCursor([this.props.index]);
        }
        else
        {
            console.error("Cannot handle data", data);
        }
    },

    onChange: function (newGraph, path)
    {
        //console.log("onChange", JSON.stringify(newGraph), path);

        this.setState({
            dataGraph: newGraph
        });
    },

    render: function ()
    {
        var ctx = this.context.formContext;

        var cursor = this.cursorFromData(this.props.data);
        return ( <form className={ cx("form", ctx.horizontal && "form-horizontal") } onSubmit={ this.onSubmit }>
            { this.props.renderChildren ? this.props.renderChildren(cursor) : this.props.children }
        </form> );
    }
});

module.exports = Form;
