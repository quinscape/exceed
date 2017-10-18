import React from "react";
import Icon from "../../../ui/Icon";

class InfoBlock extends React.Component {
    render ()
    {
        const { heading, text } = this.props;

        return (
            <div className="info-block text-info">
                <h5>
                    <Icon className="glyphicon-info-sign"/>
                    { heading }
                </h5>
                <p>
                    { text }
                </p>
            </div>
        );
    }
}

export default InfoBlock
