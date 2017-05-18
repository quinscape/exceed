import React from "react"

import sys from "../sys"
import ScopeEditor from "./code/ScopeEditor";
import i18n from "../service/i18n";


import { getEditorData } from "../reducers/editor"

import { updateConfig } from "../actions/editor"

import Form from "../components/std/form/Form"
import Field from "../components/std/form/Field"
import StaticText from "../components/std/form/StaticText"

import FormProvider from "../ui/FormProvider";

class ConfigEditor extends React.Component {

    render()
    {
        const { location, model, store } = this.props;

        const state = store.getState();
        return (
            <FormProvider update={ updateConfig }>
                <h4> { i18n('{0} configuration', sys.appName) } </h4>
                <Form data={ getEditorData(state) } path={ ["config"] }>
                    {
                        context => {
                            return (
                                <Field value="schema" context={ context } />
                            );
                        }
                    }
                </Form>
            </FormProvider>
        )
    }
}

export default ConfigEditor
