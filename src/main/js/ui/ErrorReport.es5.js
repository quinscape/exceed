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
var span = React.createFactory("span");
var p = React.createFactory("p");

var ErrorStackParser = require("error-stack-parser");

var createReactClass = require("create-react-class");

var ErrorReport = createReactClass({

    getDefaultProps: function ()
    {
        return {
            text: ""
        };
    },

    render: function ()
    {
        const error = this.props.error;

        if (error instanceof Error)
        {
            console.error(error);
            
            return (

                div(
                    {
                        className: "error-report"
                    },
                    p(null, this.props.text + String(error) + "\n\n"),
                    ul(null,
                        ErrorStackParser.parse(error).map(function (entry, index)
                        {
                            return li(
                                {
                                    key: index
                                },
                                span(
                                    {
                                        href: "view-source:" + entry.fileName,
                                        target: "exceed-source"
                                    },
                                    entry.functionName + " ( " + entry.fileName + ", Line " + entry.lineNumber + ", Column " + entry.column + ")"
                                )
                            );
                        }, this)
                    )
                )
            );

        } else
        {
            return (
                div(
                    {
                        className: "error-report bg-danger"
                    },
                    p(null, this.props.text + "\n\n"),
                    p(null, JSON.stringify(error))
                )
            );
        }


    }
});

module.exports = ErrorReport;
