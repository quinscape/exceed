import React from "react"
import ErrorReport from "./ErrorReport.es5"

class ErrorBoundary extends React.Component {
    state = {
        error: null
    };

    componentDidCatch(error, info) {
        // Display fallback UI
        this.setState({ error: { error, info }});
        // You can also log the error to an error reporting service
        console.error(error, info);
    }

    render() {

        const { error } = this.state;

        if (error) {
            // You can render any custom fallback UI
            return <ErrorReport error={ error.error } />;
        }
        return this.props.children;
    }
}

export default ErrorBoundary
