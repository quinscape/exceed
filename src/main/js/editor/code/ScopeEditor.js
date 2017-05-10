import React from "react"
import cx from "classnames"
import Memoizer from "../../util/memoizer"
import { getScopeDeclarations } from "../../reducers/scope"

const i18n = require("../../service/i18n");

function sortDeclarations(a,b)
{
    return a.name.localeCompare(b.name);
}

export const SORTED_SCOPE_DECLS = Memoizer((declarations) => {

    const array = [];

    for (let name in declarations)
    {
        if (declarations.hasOwnProperty(name))
        {
            array.push(declarations[name]);
        }
    }

    array.sort(sortDeclarations);

    return array;

});

const EDITABLE_SCOPES = {
    "VIEW" : true,
    "LAYOUT" : true,
};

function describeType(type, typeParam)
{
    return type + "(" + typeParam + ")"
}

class ScopeEditor extends React.Component {

    render()
    {
        const { store, document } = this.props;

        const declarations = getScopeDeclarations(store.getState());

        const sorted = SORTED_SCOPE_DECLS(declarations);

        //console.log("ScopeEditor", document);

        return (
            <div className={ cx("scope-editor", "container-fluid", this.props.className ) }>
                <div className="row">
                    <div className="col-md-12">
                        <h3>{ i18n('Scoped Values in View {0}', document.viewName) }</h3>
                        <table className="table">
                            <thead>
                            <tr>
                                <th>
                                    { i18n("Scope:name") }
                                </th>
                                <th>
                                    { i18n("Scope:type") }
                                </th>
                                <th>
                                    { i18n("Scope:default") }
                                </th>
                                <th>
                                    { i18n("Scope:description") }
                                </th>
                            </tr>
                            </thead>
                            <tbody>
                            {
                                sorted.map(
                                    (decl,idx) =>
                                        <tr key={ idx }>
                                            <td>
                                                { decl.name }
                                            </td>
                                            <td>
                                                { decl.model ? describeType(decl.model.type, decl.model.typeParam) : "*" }
                                            </td>
                                            <td>
                                                { decl.model ? decl.model.defaultValue : "-" }
                                            </td>
                                            <td>
                                                { decl.model ? decl.model.description : i18n("Current Object") }
                                            </td>
                                        </tr>
                                )
                            }
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        );
    }

}

export default ScopeEditor
