import React from "react";
import hasClass from "../util/has-class";
import cx from "classnames";

class PagingLink extends React.Component
{
    onClick(ev)
    {
        // check disabled class to make sure we're not executing a link in case the CSS pointer event rule
        // doesn't catch
        const classes = ev.target.className;

        if (!hasClass(classes, "disabled"))
        {
            console.log("offsetLink", this.props.ctx.offsetLink, this.props.newOffset);
            this.props.ctx.offsetLink.requestChange( this.props.newOffset );
        }
        ev.preventDefault();
    }

    render()
    {

        let newOffset = this.props.newOffset;

        const ctx = this.props.ctx;
        const rowCount = ctx.rowCount;
        const limit = ctx.limit;
        const offsetLink = ctx.offsetLink;
        const max = ctx.maxOffset;

        let disabled = false;
        const isCurrent = newOffset === offsetLink.value;
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
                    className={ cx({
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
                onClick={ (e) => this.onClick(e) }
                href={ "#jump-to-" + newOffset }
                className={ cx({
                    "btn": true,
                    "btn-link": true,
                }) }>
                { this.props.label }
            </a>
        );
    }
};


class PagingComponent extends React.Component
{

    render()
    {
        const rowCount = this.props.rowCount;
        const limit = this.props.limit;
        const offset = this.props.offsetLink.value;

        const max = Math.floor((rowCount - 1) / limit) * this.props.limit;

        // if we have nothing to page, don't render anything
        if (max === 0)
        {
            return false;
        }

        const ctx = {
            limit: this.props.limit,
            rowCount: this.props.rowCount,
            offsetLink: this.props.offsetLink,
            maxOffset: max
        };

        const links = [
            <PagingLink key={"first"} newOffset={0} label="|<" ctx={ctx}/>,
            <PagingLink key={"prev"} newOffset={offset - limit} label="<" ctx={ctx}/>
        ];

        let page;
        const currentPage = ((offset / limit) | 0) + 1;
        for (let i = -2; i <= 2; i++)
        {
            page = currentPage + i;

            const newOffset = offset + i * limit;

            const label = page > 0 && newOffset >= 0 && newOffset <= max ? page : "\u00a0";

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
}

export default PagingComponent
