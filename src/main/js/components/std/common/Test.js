import React from "react"

import assign from "object-assign"

class View extends React.Component
{
    render()
    {
        const props = assign({}, this.props);

        delete props.children;

        return (
            <div>
                <p>
                    Props:
                </p>
                <pre>{ JSON.stringify(props, null, 4) }</pre>
                <p>
                    Children: { this.props.children }
                </p>
            </div>
        );
    }
}

module.exports = View;
