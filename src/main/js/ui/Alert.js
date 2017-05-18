import React from "react";

class Alert extends React.Component
{
    static propTypes =  React.PropTypes.string.isRequired;

    render()
    {
        return (
            <div className="bg-danger">
                { this.props.message }
            </div>
        );
    }
}

export default Alert
