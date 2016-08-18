var React = require("react");

var uri = require("../../../util/uri");
var sys = require("../../../sys");

var Link = React.createClass({
    render: function ()
    {
        var params = {};

        React.Children.forEach(this.props.children, function (kid)
        {
            if (kid.type === LinkParam)
            {
                params[kid.props.name] = kid.props.value;
            }
        });

        //console.log("params", params);

        var target = uri("/app/" + sys.appName + this.props.location, params);

        return (
            <a href={ target }
               target={ this.props.target }
               className="btn btn-link"
                onClick={function(ev) {
                    var viewService = require("../../../service/view");
                    viewService.navigateTo(ev.target.href);
                    ev.preventDefault();
                }}>
                { this.props.text }
            </a>
        );
    }
});

var LinkParam = function(props)
{

};

Link.Param = LinkParam;


module.exports = Link;
