var React = require("react");

// we're using this ErrorReport for react-transform-catch-errors which leads to the curious situation of that plugin
// trying to wrap an error handler around Alert using Alert which does not work
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
    render: function ()
    {
        return (
            div(
                {
                    className: "error-report bg-danger"
                },
                p(null, String(this.props.error) + "\n\n"),
                ul(null,
                    ErrorStackParser.parse(this.props.error).map(function(entry, index)
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
});

module.exports = ErrorReport;
