var React = require("react");

function Toolbar(props)
{
    return ( <div className="btn-toolbar">{ props.children } </div> );
}

module.exports = Toolbar;

