import React from "react"

import store from "../../../service/store"
import i18n from "../../../service/i18n"
import renderWithContext from "../../../util/render-with-context"
import { updateScope } from "../../../actions/scope"

import { getFieldState } from "../../../reducers/form-state"


import Toolbar from "./Toolbar"
import FieldState from "../../../form/field-state";

const componentService = require("../../../service/component");
const domainService = require("../../../service/domain");

class ListEditorControl extends React.Component
{
    removeItem = ev => this.props.removeItem(this.props.index);
    moveItemUp = ev => this.props.moveItemUp(this.props.index);
    moveItemDown = ev => this.props.moveItemDown(this.props.index);

    render()
    {
        const { canReorder, removeLabel, removeItem, moveItemUp, moveItemDown } = this.props;

        return (
            <div className="list-editor-control">
                {
                    canReorder &&
                    <button type="button" className="btn btn-default" disabled={ !moveItemUp } onClick={ this.moveItemUp }>
                        <span className="glyphicon glyphicon-arrow-up"/>
                    </button>
                }
                {
                    canReorder &&
                    <button type="button" className="btn btn-default" disabled={ !moveItemDown } onClick={ this.moveItemDown }>
                        <span className="glyphicon glyphicon-arrow-down"/>

                    </button>
                }
                {
                    removeItem &&
                    <button type="button" className="btn btn-default" onClick={ this.removeItem } title={ removeLabel }>
                        <span className="glyphicon glyphicon-remove"/>
                    </button>
                }
            </div>
        )
        
    }
}


class ListEditor extends React.Component {

    static defaultProps = {
        canAdd: true,
        canRemove: true,
        canReorder: true,
        addLabel: i18n("Add"),
        removeLabel: i18n("Remove"),
        minItems: 0,
        maxItems: -1
    };

    newItem = ev => {
        const { data } = this.props;

        const newValue = domainService.create(data.getPropertyType().typeParam);
        store.dispatch(
            updateScope(
                data.getPath(),
                data.get().concat(newValue)
            )
        );

        //console.log("after NEW-ITEM", store.getState());

        if (this.props.onNewObject)
        {
            // we delay the action to have it executed in the new state after updateScope
            window.setTimeout(this.props.onNewObject, 1);
        }
    };

    moveItemUp = index => {
        this.swapItems(index, index - 1);
    };

    moveItemDown = index => {
        this.swapItems(index, index + 1);
    };

    swapItems(index, other)
    {
        const { data } = this.props;

        const array = data.get().slice();

        const h = array[other];
        array[other] = array[index];
        array[index] = h;

        //console.log("SWAP", index, "before",  data.get(), "after", array);

        store.dispatch(
            updateScope(
                data.getPath(),
                array
            )
        )
    }

    removeItem = index => {
        const { data } = this.props;

        //console.log("REMOVE", index);

        const array = data.get().slice();

        array.splice(index, 1);

        store.dispatch(
            updateScope(
                data.getPath(),
                array
            )
        )
    };

    render()
    {
        const { id, children, data, canReorder, canRemove, canAdd, addLabel, removeLabel, minItems, maxItems } = this.props;

        const state = store.getState();

        const disabled = getFieldState(state, id) !== FieldState.NORMAL;


        const value = data.get();

        let rows = false;
        let len = 0;
        if (value)
        {
            len = value.length;
            const last = len - 1;
            rows = new Array(len);

            for (let i=0; i < len; i++)
            {
                rows[i] = (
                    <div key={ i } className="list-editor-row">
                        <ListEditorControl
                            index={ i }
                            removeItem={ !disabled && canRemove && len > minItems && this.removeItem }
                            moveItemUp={ !disabled &&i > 0 && canReorder && this.moveItemUp }
                            moveItemDown={ !disabled &&i < last && canReorder && this.moveItemDown }
                            canReorder={ !disabled && canReorder }
                            removeLabel={ removeLabel }
                        />
                        <div className="list-editor-field">
                            {
                                renderWithContext(children, data.getCursor([i]))
                            }
                        </div>
                    </div>
                );
            }
        }


        return (
            <div className="list-editor">
                {
                    !disabled && canAdd && (maxItems < 0 || len < maxItems) &&
                    <Toolbar>
                            <button type="button" className="btn btn-default" onClick={this.newItem}>
                                <span className="glyphicon glyphicon-plus"/>
                                {" " + this.props.addLabel }
                            </button>
                    </Toolbar>
                }
                {
                    rows
                }
            </div>
        );
    }
}

export default ListEditor
