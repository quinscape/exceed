import React from "react"

export default function(Component)
{
    return class extends React.Component
    {
        componentDidMount()
        {
            document.addEventListener("mousedown", this.onDocumentClick);
        }

        componentWillUnmount()
        {
            document.removeEventListener("mousedown", this.onDocumentClick);
        }

        onDocumentClick = ev =>
        {
            this._component && this._component.onDocumentClick();
        };

        assignComponent = c => this._component = c;

        render()
        {
            return (
                <Component
                    ref={ this.assignComponent }
                    {... this.props}
                />
            )
        }
    }
}
