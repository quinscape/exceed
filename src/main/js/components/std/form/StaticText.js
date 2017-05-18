import React from "react";
import FormElement from "./FormElement";


var StaticText = FormElement(class StaticText extends
    React.Component{
        render ()
        {
            return (
                <p id={ this.props.id } className="form-control-static">
                    { this.props.valueLink.value }
                </p>
            );
        }
    }
);

export default StaticText
