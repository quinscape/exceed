import React from "react";
import cx from "classnames";
import i18n from "../../../service/i18n";


class GlyphButton extends React.Component
{

    static propTypes = {
        glyphicon: React.PropTypes.string.isRequired,
        text: React.PropTypes.string.isRequired,
        glyphOnly: React.PropTypes.bool
    }

    render()
    {
        const glyphicon = this.props.glyphicon;
        const text = this.props.text || i18n(this.props.glyphicon);
        const glyphOnly = this.props.glyphOnly;

        return (
            <button type="button" {...this.props} className={ cx("btn", this.props.className || "btn-default") } title={ glyphOnly && text }>
                <span className={ cx("glyphicon",  "glyphicon-" + glyphicon) } aria-hidden="true"/>
                { glyphOnly ? <span className="sr-only">{ text }</span> : text }
            </button> );
    }
};


export default GlyphButton
