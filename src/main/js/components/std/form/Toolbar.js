const React = require("react");
const cx = require("classnames");

function Toolbar(props)
{
    return ( <div className={ cx("btn-toolbar", props.className) }>{ props.children } </div> );
}

var Separator = React.createClass({
    render: function ()
    {
        return (
            <span className="form-control-static" style={{ padding: "0 0.5em"}}>|</span>
        );
    }
});

Toolbar.Separator = Separator;

module.exports = Toolbar;

