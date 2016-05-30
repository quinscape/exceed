var React = require("react");
var cx = require("classnames");

var DataList = require("../../util/data-list");
var Scope = require("../../service/scope");

var FormContext = require("./form-context");

var LinkedStateMixin = require("react-addons-linked-state-mixin");

function isRawDataList(input)
{
    return input && input.types && input.rows && input.columns;
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

    getChildContext: function()
    {
        return {
            formContext: new FormContext(
                this.state.dataList,
                this.props.horizontal,
                this.props.labelClass,
                this.props.wrapperClass,
                this.linkState("errors")
            )
        };
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

    getDataList: function(input)
    {
        if (!input)
        {
            throw new Error("No data");
        }

        if (isRawDataList(input))
        {
            return new DataList(input, this.onChange);
        }
        else if (input._type)
        {
            return new DataList( objectToDataList(input), this.onChange);

        }
        else
        {
            console.error("Cannot handle data", input);
        }
    },
    getInitialState: function ()
    {
        return {
            dataList: this.getDataList(this.props.data),
            errors: {}
        };
    },


    onChange: function (newRows, path)
    {
        console.log("onChange", JSON.stringify(newRows), path);

        var dataList =  new DataList(this.state.dataList);
        dataList.rows = newRows;

        this.setState({
            dataList: dataList
        });
    },

    componentWillReceiveProps: function (nextProps)
    {
        //console.log("NEXTPROPS", nextProps);

        if (nextProps.dataList !== this.props.data)
        {
            // XXX: merge changed props in current state with new state?

            this.setState({
                dataList: new DataList(nextProps.dataList, this.onChange)
            });
        }
    },

    render: function ()
    {
        var dataList = this.state.dataList;

        //console.log(this.props.data);

        if (dataList.rowCount != 1)
        {
            throw new Error("Need exactly 1 result here");
        }

        var cursor = dataList.getCursor([this.props.index]);

        //console.log("CURSOR", cursor);

        return ( <form className={ cx("form", this.props.horizontal && "form-horizontal") } onSubmit={ this.onSubmit }>
            { this.props.renderChildrenWithContext(cursor) }
        </form> );
    }
});

module.exports = Form;
