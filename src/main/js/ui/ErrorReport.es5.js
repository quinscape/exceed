var React = require("react");

// we're using this ErrorReport for react-transform-catch-errors which leads to the curious situation of that plugin
// trying to wrap an error handler around ErrorReport using ErrorReport which does not work
// To fix this, we have excluded this file from Babel-transformation and will have to use naked ES5 without JSX support
// here

var div = React.createFactory("div");
var pre = React.createFactory("pre");
var ul = React.createFactory("ul");
var li = React.createFactory("li");
var a = React.createFactory("a");
var p = React.createFactory("p");

var ErrorStackParser = require("error-stack-parser");

var ErrorReport = React.createClass({

    getDefaultProps: function ()
    {
        return {
            text: ""
        };
    },

    render: function ()
    {
        var error = this.props.error;

        if (typeof error === "object")
        {
            return (
                div(
                    {
                        className: "error-report bg-danger"
                    },
                    p(null, this.props.text + "\n\n"),
                    p(null,JSON.stringify(error))
                )
            );
        }
        else
        {
            return (
                div(
                    {
                        className: "error-report bg-danger"
                    },
                    p(null, this.props.text + String(error) + "\n\n"),
                    ul(null,
                        ErrorStackParser.parse(error).map(function(entry, index)
                        {
                            return li(
                                {
                                    key: index
                                },
                                a(
                                    {
                                        href: "view-source:" + entry.fileName,
                                        target: "exceed-source"
                                    },
                                    entry.functionName + " ( Line " + entry.lineNumber +", Column " + entry.column+ ")"
                                )
                            );
                        }, this)
                    )
                )
            );

        }


    }
});

module.exports = ErrorReport;
