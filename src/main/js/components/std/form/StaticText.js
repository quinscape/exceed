import React from "react";
import FormElement from "./FormElement";
import cx from "classnames";

const StaticText = FormElement(
    class StaticText extends React.PureComponent
    {
        render ()
        {
            const { id, value, className} = this.props;

            return (
                <p id={ id } className={ cx(className, "form-control-static") }>
                    { value }
                </p>
            );
        }
    }
);

export default StaticText
