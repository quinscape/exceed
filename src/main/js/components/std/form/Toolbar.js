import React from "react";
import cx from "classnames";

function Toolbar(props)
{
    return ( <div className={ cx("btn-toolbar", props.className) }>{ props.children } </div> );
}

class Separator extends React.Component {
    render ()
    {
        return (
            <span className="form-control-static" style={{ padding: "0 0.5em"}}>|</span>
        );
    }
};

Toolbar.Separator = Separator;

export default Toolbar

