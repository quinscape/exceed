import React from "react"

class ShopAddress extends React.Component {

    render()
    {
        const { value } = this.props;

        const obj = value.get();

        return (
            <pre className="small">
                { obj.recipient + "\n" }
                { obj.addressLine + "\n" }
                { obj.addressExtra + "\n" }
                { obj.postal + "\n" }
                { obj.country + "\n" }
            </pre>
        );
    }
}

export default ShopAddress
