import React from "react";

class FormProvider extends React.Component {
    render()
    {
        const { children } = this.props;

        return (
            <div>
                { children }
            </div>
        );
    }
}

export default FormProvider
