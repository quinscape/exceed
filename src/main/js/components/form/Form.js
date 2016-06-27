var React = require("react");
var cx = require("classnames");

var DataList = require("../../util/data-list");
var DataListCursor = require("../../util/data-list-cursor");
var Scope = require("../../service/scope");
var domainService = require("../../service/domain");

var FormContext = require("../../util/form-context");

var LinkedStateMixin = require("react-addons-linked-state-mixin");


function isRawDataList(input)
{
    return input && input.rows && input.columns;
}

function objectToDataList(input)
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
        types: {
            [typeName] : domainType
        },
        columns: cols,
        rows: [ obj ],
        rowCount: 1
    };
}

var Form = React.createClass({

    mixins: [ LinkedStateMixin ],

    childContextTypes: {
        formContext: React.PropTypes.instanceOf(FormContext)
    },

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

        if (isRawDataList(data))
        {
            return new DataList(types, data, this.onChange).getCursor([this.props.index]);
        }
        else if (data instanceof DataListCursor)
        {
            console.log("CURSOR", data);

            var value = data.get();
            if (!value || typeof value !== "object")
            {
                throw new Error("Invalid form cursor: points to " + value);
            }
            return data;

        }
        else if (data._type)
        {
            return new DataList(types, objectToDataList(data), this.onChange).getCursor([this.props.index]);
        }
        else
        {
            console.error("Cannot handle data", data);
        }
    },
    getInitialState: function ()
    {
        return {
            cursor: this.cursorFromData(this.props.data)
        };
    },


    onChange: function (newRows, path)
    {
        console.log("onChange", JSON.stringify(newRows), path);

        var dataList = this.state.cursor.dataList;
        dataList.rows = newRows;
        this.forceUpdate();
    },

    //componentWillReceiveProps: function (nextProps)
    //{
    //
    //    if (nextProps.data !== this.props.data)
    //    {
    //        // XXX: merge changed props in current state with new state?
    //        console.log("Update state", nextProps);
    //        this.setState({
    //            cursor: this.cursorFromData(nextProps.data)
    //        });
    //    }
    //},

    render: function ()
    {
        var cursor = this.state.cursor;
        return ( <form className={ cx("form", this.props.horizontal && "form-horizontal") } onSubmit={ this.onSubmit }>
            { this.props.renderChildrenWithContext(cursor) }
        </form> );
    }
});

module.exports = Form;
