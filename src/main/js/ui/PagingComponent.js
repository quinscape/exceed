"use strict";

var React = require("react/addons");

var hasClass = require("../util/has-class");

var classnames = require("classnames");

var PagingLink = React.createClass({
    onClick: function (ev)
    {
        // check disabled class to make sure we're not executing a link in case the CSS pointer event rule
        // doesn't catch
        var classes = ev.target.className;
        if (!hasClass(classes, "disabled"))
        {
            this.props.ctx.offsetLink.requestChange( this.props.newOffset );
        }
        ev.preventDefault();
    },
    render: function ()
    {

        var newOffset = this.props.newOffset;

        var ctx = this.props.ctx;
        var rowCount = ctx.rowCount;
        var limit = ctx.limit;
        var offsetLink = ctx.offsetLink;
        var max = ctx.maxOffset;

        var disabled = false;
        var isCurrent = newOffset === offsetLink.value;
        if (isCurrent)
        {
            disabled = true;
        }

        if (newOffset < 0)
        {
            newOffset = 0;
            disabled = true;
        }
        else if (newOffset > max)
        {
            newOffset = max;
            disabled = true;
        }

        if (disabled)
        {
            return (
                <span
                    className={ classnames({
                        "btn": true,
                        "btn-link": true,
                        "disabled": true,
                        "current" : isCurrent
                    }) }>
                    { this.props.label }
                </span>
            );
        }

        return (
            <a
                onClick={ this.onClick }
                href={ "#jump-to-" + newOffset }
                className={ classnames({
                    "btn": true,
                    "btn-link": true,
                }) }>
                { this.props.label }
            </a>
        );
    }
});


var PagingComponent = React.createClass({

    render: function ()
    {
        var rowCount = this.props.rowCount;
        var limit = this.props.limit;
        var offset = this.props.offsetLink.value;

        var max = Math.floor((rowCount - 1) / limit) * this.props.limit;

        // if we have nothing to page, don't render anything
        if (max === 0)
        {
            return false;
        }

        var ctx = {
            limit: this.props.limit,
            rowCount: this.props.rowCount,
            offsetLink: this.props.offsetLink,
            maxOffset: max
        } ;

        var links = [
            <PagingLink key={"first"} newOffset={0} label="|<" ctx={ ctx } />,
            <PagingLink key={"prev"} newOffset={offset - limit} label="<" ctx={ ctx } />
        ];

        var page, currentPage = ((offset/limit)|0) + 1;
        for (var i = -2; i <= 2; i++)
        {
            page = currentPage + i;

            var newOffset = offset + i * limit;

            var label = page > 0 && newOffset >= 0 && newOffset <= max ? page : "\u00a0";

            links.push(
                <PagingLink key={i} newOffset={ newOffset } label={ label } ctx={ ctx }/>
            );
        }

        links.push(
            <PagingLink key={"next"} newOffset={offset + limit} label=">" ctx={ ctx } />,
            <PagingLink key={"last"} newOffset={max} label=">|" ctx={ ctx } />
        );

        return (
            <div className="paging">
                { links }
            </div>
        );
    }
});

module.exports = PagingComponent;
