import renderWithContext from "../../../util/render-with-context";
import React from "react";
import PropTypes from 'prop-types'

class FormBlock extends React.Component
{
    static propTypes = {
        horizontal: PropTypes.bool,
        labelClass: PropTypes.string,
        wrapperClass: PropTypes.string
    };

    render ()
    {
        const { data, value, children} = this.props;

//        console.log({ data, value, children});

        return (
            <div className="form-block">
                { renderWithContext(children, data || value ) }
            </div>
        );
    }
}

export default FormBlock
