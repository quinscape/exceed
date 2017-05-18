var React = require("react");

var Modal = require("react-overlays").Modal;

var timerId;

var haveWindow = typeof window !== "undefined";

var component = null;


export class ThrobberComponent extends React.Component
{
    state = { active : false };

    componentDidMount()
    {
        component = this;
    }

    componentWillUnmount()
    {
        component = null;
    }

    shouldComponentUpdate(nextProps, nextState)
    {
        return this.state.active != nextState.active;
    }

    render()
    {
        return (
            <Modal
                aria-labelledby="throbber-label"
                backdropClassName="throbber"
                show={ this.state.active }
            >
                <div className="throbber-body">
                    <h3 id="throbber-label">Please Wait..</h3>
                </div>
            </Modal>
        )
    }
};

export default {
    enable: function ()
    {
        if (haveWindow && !timerId)
        {
            timerId = window.setTimeout(function ()
            {
                if (component)
                {
                    component.setState({
                        active: true
                    });
                }
            }, 2000);
        }
    },
    disable: function ()
    {
        if (timerId)
        {
            window.clearTimeout(timerId);
            timerId = null;
        }

        if (component)
        {
            component.setState({
                active: false
            });
        }
    },
    ThrobberComponent: ThrobberComponent
}
