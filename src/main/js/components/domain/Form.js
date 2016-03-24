var React = require("react");
var cx = require("classnames");

var DataList = require("../../util/data-list");

var FormContext = require("./form-context");

var LinkedStateMixin = require("react-addons-linked-state-mixin");

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
        dataList: React.PropTypes.object.isRequired,
        horizontal: React.PropTypes.bool,
        labelClass: React.PropTypes.string,
        wrapperClass: React.PropTypes.string
    },

    getDefaultProps: function ()
    {
        return {
            horizontal: true,
            labelClass: "col-md-2",
            wrapperClass: "col-md-4"
        };
    },

    getInitialState: function ()
    {
        return {
            dataList: new DataList(this.props.dataList, this.onChange),
            errors: {}
        };
    },


    onChange: function (newRows, path)
    {
        console.log("onChange", JSON.stringify(newRows), path);

        var dataList = this.state.dataList;
        dataList.rows = newRows;

        this.setState({
            dataList: dataList
        });
    },

    componentWillReceiveProps: function (nextProps)
    {
        //console.log("NEXTPROPS", nextProps);

        if (nextProps.dataList.rows[0] !== this.props.dataList.rows[0])
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

        //console.log(this.props.dataList);

        if (dataList.rowCount != 1)
        {
            throw new Error("Need exactly 1 result here");
        }

        var cursor = dataList.getCursor([0]);

        //console.log("CURSOR", cursor);

        return ( <form className={ cx("form", this.props.horizontal && "form-horizontal") } onSubmit={ this.onSubmit }>
            { this.props.renderChildrenWithContext(cursor) }
        </form> );
    }
});

module.exports = Form;
