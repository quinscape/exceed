import React from "react";

class Counter extends React.Component
{
    state = { count: this.props.value || 0 };

    render ()
    {
        return (
            <div className="counter">
                <h3>{ this.state.count }</h3>
                <input type="submit" className="btn btn-primary" value="++" onClick={ () => {
                        this.setState({
                            count: this.state.count + 1
                        });
                } } />
            </div>
        );
    }
};

export default Counter
