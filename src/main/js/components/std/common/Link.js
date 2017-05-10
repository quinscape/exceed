import sys from "../../../sys";
import uri from "../../../util/uri";
import React from "react";

import { navigateView } from "../../../actions/view"
import store from "../../../service/store"

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

                    try
                    {
                        store.dispatch(
                            navigateView({
                                url: ev.target.href
                            })
                        );
                    }
                    catch(e)
                    {
                        console.error(e);
                    }

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
