import React from "react";
import cx from "classnames";
import i18n from "../../../service/i18n";

import PropTypes from 'prop-types'

class GlyphButton extends React.Component
{

    static propTypes = {
        glyphicon: PropTypes.string.isRequired,
        text: PropTypes.string.isRequired,
        glyphOnly: PropTypes.bool
    };

    render()
    {
        const glyphicon = this.props.glyphicon;
        const text = this.props.text || i18n(this.props.glyphicon);
        const glyphOnly = this.props.glyphOnly;

        return (
            <button
                type="button"
                className={
                    cx(
                        "btn",
                        this.props.className || "btn-default"
                    )
                }
                onClick={ this.props.onClick }
                title={ glyphOnly && text }
            >
                <span
                    className={
                        cx(
                            "glyphicon",
                            "glyphicon-" + glyphicon
                        )
                    }
                    aria-hidden="true"
                />
                { glyphOnly ? <span className="sr-only">{ text }</span> : text }
            </button> );
    }
};


export default GlyphButton
