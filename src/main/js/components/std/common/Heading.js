import React from "react";

class Heading extends React.Component
{
    render()
    {
        const { icon } = this.props;

        return (
            <h2>
                { icon && <span className={ "text-info glyphicon glyphicon-" + icon }/> }
                { this.props.value }
            </h2>
        )
    }
};

export default Heading
