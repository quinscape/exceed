import React from "react"
import cx from "classnames"

export default function (props)
{
    const { className } = props;

    return (
        className && <span className={ cx("glyphicon", className) } />
    );
}

