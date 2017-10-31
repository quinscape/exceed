import React from "react";
import Icon from "../../../ui/Icon";

class InfoBlock extends React.Component {
    render ()
    {
        const { heading, text, children } = this.props;

        return (
            <div className="info-block">
                <h5 className="text-info">
                    <Icon className="glyphicon-info-sign"/>
                    { " " + heading }
                </h5>
                <p>
                    { text || children }
                </p>
            </div>
        );
    }
}

export default InfoBlock
