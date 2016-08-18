var React = require("react");
var cx = require("classnames");

var i18n = require("../../../service/i18n");

var GlyphButton = React.createClass({

    propTypes: {
        glyphicon: React.PropTypes.string.isRequired,
        text: React.PropTypes.string.isRequired,
        glyphOnly: React.PropTypes.bool
    },

    render: function ()
    {
        var glyphicon = this.props.glyphicon;
        var text = this.props.text || i18n(this.props.glyphicon);
        var glyphOnly = this.props.glyphOnly;

        return (
            <button type="button" {...this.props} className={ cx("btn", this.props.className || "btn-default") } title={ glyphOnly && text }>
                <span className={ cx("glyphicon",  "glyphicon-" + glyphicon) } aria-hidden="true"/>
                { glyphOnly ? <span className="sr-only">{ text }</span> : text }
            </button> );
    }
});

module.exports = GlyphButton;
