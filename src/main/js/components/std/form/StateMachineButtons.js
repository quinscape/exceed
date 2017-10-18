import React from "react"
import cx from "classnames"

import store from "../../../service/store"
import process from "../../../service/process"
import i18n from "../../../service/i18n"
import { getStateMachine } from "../../../reducers/meta"

import FormElement from "./FormElement"

class StateMachineButton extends React.Component {

    changeValue = ev => {

        const { stateName, changeValue} = this.props;

        changeValue(stateName);
    };

    render()
    {
        const { stateName, className, icon, stateMachineName } = this.props;
        return (
            <button type="button" className={ cx("btn", className || "btn-default") } onClick={ this.changeValue }>
                { icon && <span className={ "glyphicon glyphicon-" + icon } /> }
                { " " + i18n("Set " + stateMachineName + " " + stateName) }
            </button>
        );
    }
}

const StateMachineButtons = FormElement(
    class extends React.Component {

        changeValue = name => {

            const { onChange, transition } = this.props;

            onChange(name);

            return process.transition(transition);
        };

        render()
        {
            const state = store.getState();

            const { value, propertyType, stateClasses, stateIcons, stateMachine } = this.props;

            const typeFromCursor = propertyType.typeParam;
            if (stateMachine !== typeFromCursor)
            {
                throw new Error("State machine mismatch, static type is " + stateMachine + ", but cursor type is " + typeFromCursor)
            }

            const stateMachineModel = getStateMachine(state, stateMachine);

            const buttons = [];

            const { states } = stateMachineModel;

            const validTransitions = states[value];

            for (let i = 0; i < validTransitions.length; i++)
            {
                const name = validTransitions[i];

                buttons.push(
                    <StateMachineButton
                        key={ name }
                        value={ value }
                        stateName={ name }
                        changeValue={ this.changeValue }
                        className={ stateClasses[name] }
                        icon={ stateIcons[name] }
                        stateMachineName={ stateMachine }
                    />
                );
            }

            return (
                <div className="btn-group" role="group">
                    { buttons }
                </div>
            )
        }
    },
    // opts
    {
        decorate: false
    }
);

export default StateMachineButtons
