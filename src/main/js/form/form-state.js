import assign from "object-assign"
import { findComponents, findParent } from "../util/component-util"

import DataCursor from "../domain/cursor";
import propertyConverter from "../service/property-converter";
import propertyRenderer from "../service/property-renderer";
import store from "../service/store";
import FieldState from "./field-state";
import RTView from "../service/runtime-view-api";


import { getComponentInjections } from "../reducers/component"
import { getFieldValues } from "../reducers/form-state";
import { getViewModel, getComponentConfig } from "../reducers/meta"

import ComponentClasses from "../components/component-classes";
import { describe, locateComponent } from "../util/component-util";

const componentService = require("../service/component");
const components  = componentService.getComponents();

let formCounter = 0;

const FORM_COMPONENT = "Form";

let defaultConfig;

/**
 * Form-state local runtime view instance that will contain the future redux state being prepared on updates.
 */
const rtView = new RTView(null, null);

function safeValue(value)
{
    if (value === null || typeof value === "undefined")
    {
        return "";
    }
    return value;
}

function isFormComponent(c)
{
    return c.name === FORM_COMPONENT;
}
// function isFormBlock(c)
// {
//     return !!FORM_BLOCK_COMPONENTS[c.name];
// }

function hasFieldClass(c)
{
    const descriptor = components[c.name];
    return descriptor && descriptor.classes && descriptor.classes.indexOf(ComponentClasses.FIELD) >= 0;
}

function hasFormContainerClass(c)
{
    const descriptor = components[c.name];
    return descriptor && descriptor.classes && descriptor.classes.indexOf(ComponentClasses.FORM_CONTAINER) >= 0;
}

function hasConfigContainerClass(c)
{
    const descriptor = components[c.name];
    return descriptor && descriptor.classes && descriptor.classes.indexOf(ComponentClasses.FORM_CONFIG_CONTAINER) >= 0;
}

function findParentBlockId(field)
{
    let current = field.parent;
    while (current !== null)
    {
        if (hasFormContainerClass(current))
        {
            return current.attrs.id;
        }
        current = current.parent;
    }
    return null;
}

function getConfig(props, defaultConfig)
{
    if (!props)
    {
        return defaultConfig;
    }

    const { horizontal, wrapperClass, labelClass, propagate } = props;

    const haveHorizontal = typeof horizontal !== "undefined";
    const haveWrapperClass = typeof wrapperClass !== "undefined";
    const haveLabelClass = typeof labelClass !== "undefined";
    const havePropagate = typeof propagate !== "undefined";

    if (
        haveHorizontal ||
        haveWrapperClass ||
        haveLabelClass
    )
    {
        const config = assign( {}, defaultConfig);

        if (haveHorizontal)
        {
            config.horizontal = horizontal;
        }
        if (haveWrapperClass)
        {
            config.wrapperClass = wrapperClass;
        }
        if (haveLabelClass)
        {
            config.labelClass = labelClass;
        }
        if (havePropagate)
        {
            config.propagate = propagate;
        }
        return config;
    }
    return defaultConfig;
}

function nextFormId()
{
    return ("form-" + ++formCounter);
}

function findForm(fieldComponent, defaultFormId)
{
    const formComponent = findParent(fieldComponent, isFormComponent);

    let formId;
    if (formComponent)
    {
        formId = formComponent.attrs.id || nextFormId();

    }
    else
    {
        formId = defaultFormId;
    }

    return formId;
}

function getFormConfig(formState, fieldComponent)
{
    const isConfigContainer = hasConfigContainerClass(fieldComponent);

    if (isConfigContainer)
    {
        const config = formState.formConfig[fieldComponent.attrs.id];
        if (config)
        {
            return config;
        }
    }

    const parent = findParent(fieldComponent, hasConfigContainerClass);

    let parentConfig;
    if (!parent)
    {
        parentConfig = defaultConfig;
    }
    else
    {
        parentConfig = getFormConfig(formState, parent);
    }

    if (isConfigContainer)
    {
        const config = getConfig(fieldComponent, parentConfig);
        formState.formConfig[fieldComponent.attrs.id] = config;
        return config;
    }
    else
    {
        return parentConfig;
    }
}

function getContext(model, contextCache, reduxState)
{
    const modelId = model.attrs.id;

    let cursor = contextCache[modelId];

    if (!cursor)
    {
        if (hasFormContainerClass(model))
        {
            const injections = getComponentInjections(reduxState, modelId);
            if (injections && injections.data)
            {
                cursor = DataCursor.from(injections.data);
            }
            else
            {
                const dataExpr = model.exprs && model.exprs.data;
                if (dataExpr && dataExpr.fn)
                {
                    const parent = dataExpr.parent;
                    let context = null;
                    if (parent)
                    {
                        context = getContext(parent, contextCache, reduxState);
                    }
                    cursor = dataExpr.fn(context);
                }
            }

            if (!(cursor instanceof DataCursor))
            {
                throw new Error("Could not get data for component '" + modelId + "'");
            }
        }
    }
    contextCache[modelId] = cursor;

    return cursor;
}

/**
 * Reads the current value from the field cursor and compares it to the current value from the input.
 *
 * If changed, it returns a new string value otherwise null
 *
 * @param field             {Field} field structure
 * @param index             {number} iteration index
 * @param contextCache      {object} context cache
 * @param reduxState        {object} redux state
 * @param currentValue      {string} current input value
 * @returns {?string} changed value or null
 */
export function compareFieldValue(field, index, contextCache, reduxState, currentValue)
{
    const { cursor } = findFieldCursor(field, index, contextCache, reduxState);

    // always return render static properties
    if (field.model.name === "StaticText")
    {
        return propertyRenderer.renderStatic(cursor.get(), field.propertyType);
    }

    // read previous form field value and convert it to js
    const prevConverted = propertyConverter.fromUser(currentValue, field.propertyType);
    // reread new value
    const newValue = cursor.get();

    // only update when the field value was erroneous or when the converted value differs
    // from the new value. The latter makes input fields with Decimal or Currency values play nice with updates and
    // does not reformat the value while the user is editing.
    if (!prevConverted.ok || prevConverted.value !== newValue)
    {
        // convert to user and update
        return safeValue(propertyConverter.toUser(newValue, field.propertyType));
    }

    // keep as-is
    return null;
}

function findFieldCursor(field, index, contextCache, reduxState)
{
    const { valueExpr } = field;

    let result = null;
    let iterationParent = null;
    if (valueExpr)
    {
        const model = valueExpr.parent;

        try
        {
            let parentCursor;
            if (model)
            {
                parentCursor = getContext(model, contextCache, reduxState);
                if (parentCursor.type === "List")
                {
                    iterationParent = parentCursor;
                    parentCursor = parentCursor.getCursor([index]);

                    // const array = parentCursor.get();
                    // if (!array || !array.length)
                    // {
                    //     return { cursor: parentCursor, iterationParent: iterationParent };
                    // }
                }
            }
            else
            {
                parentCursor = null;
            }
            result = valueExpr.fn(parentCursor);
        }
        catch(e)
        {
            throw new Error("Error fetching cursor for " + describe(field.model) + ": " + valueExpr.fn.toString() + ": " + e);
        }

        let qualifier;
        if (result && (qualifier = result.getGraph().qualifier) !== "SCOPE")
        {
            throw new Error("Unsupported data graph qualifier: " + qualifier);
        }

    }
    return {
        cursor: result,
        iterationParent: iterationParent
    };
}

function initializeFields(formState, reduxState, contextCache)
{
    const { fields, fieldValues, fieldStates } = formState;

    for (let modelId in fields)
    {
        if (fields.hasOwnProperty(modelId))
        {
            const field = fields[modelId];
            const fieldCursorInfo = findFieldCursor(field, 0, contextCache, reduxState);
            const cursor = fieldCursorInfo.cursor;

            if (cursor)
            {
                const path = cursor.getPath();
                const pathLen = path.length;

                const propertyType = cursor.getPropertyType();
                field.propertyType = propertyType;
                field.cursorPath = path;

                // iterative context?
                if (fieldCursorInfo.iterationParent)
                {
                    field.iterationIndex = fieldCursorInfo.iterationParent.getPath().length;
                    const list = fieldCursorInfo.iterationParent.get();

                    for (let i = 0; i < list.length; i++)
                    {
                        const value = list[i][path[pathLen - 1]];
                        const fieldId = modelId + "-" + i;
                        fieldValues[fieldId] =  propertyConverter.toUser(value, propertyType);
                        fieldStates[fieldId] = evaluateState( field.model, field.parentBlockId, fieldStates, contextCache, reduxState);
                    }


                }
                else
                {
                    field.iterationIndex = -1;
                    fieldValues[modelId] =  propertyConverter.toUser(cursor.get(), propertyType);

                    fieldStates[modelId] = evaluateState( field.model, field.parentBlockId, fieldStates, contextCache, reduxState);
                }

                //console.log({field});
            }
            else
            {
                field.cursorPath = null;
                field.propertyType = null;
                fieldStates[modelId] = evaluateState( field.model, field.parentBlockId, fieldStates, contextCache, reduxState);
                fieldValues[modelId] =  "";
            }
        }
    }
}

export function evaluateState(component, parentBlockId, fieldStates, contextCache, reduxState)
{
    if (parentBlockId !== null)
    {
        const blockState = fieldStates[parentBlockId];
        if (blockState !== FieldState.NORMAL)
        {
            return blockState;
        }
    }

    const context = getContext(component, contextCache, reduxState);

    if (component.exprs.readOnlyIf)
    {
        const isReadOnly = component.exprs.readOnlyIf.fn(context);
        if (isReadOnly)
        {
            return FieldState.READ_ONLY;
        }
    }

    if (component.exprs.disabledIf)
    {
        const isDisabled = component.exprs.disabledIf.fn(context);
        if (isDisabled)
        {
            return FieldState.DISABLED;
        }
    }
    return FieldState.NORMAL;
}

function getFieldConfig(props, defaults)
{
    const propagateFromProps = props.propagate;
    if (propagateFromProps !== undefined && propagateFromProps !== defaults.propagate )
    {
        const copy = assign({}, defaults);
        copy.propagate = propagateFromProps;
        return copy;
    }
    return defaults;
}

export function newFormState(state)
{
    if (!defaultConfig)
    {
        defaultConfig = {
            horizontal: false,
            labelClass: "col-md-2",
            wrapperClass: "col-md-4",
            propagate: getComponentConfig(state, "instantTextFields")
        }
    }

    const viewModel = getViewModel(state);

    rtView.updateState(state);
    
    const formState = {
        errors: [],
        formConfig: {},
        blocks: {},
        fields: {},
        fieldStates: {},
        fieldValues: {}
    };

    const defaultFormId = nextFormId();

    const contextCache = {};
    findComponents(viewModel, hasFormContainerClass)
        .forEach( containerComponent =>
            {
                // findComponents finds all our blocks after their parents

                const blockId = containerComponent.attrs.id;

                const formId = findForm(containerComponent, defaultFormId);

                const formConfig = getFormConfig(formState, containerComponent);


                const parentBlockId = findParentBlockId(containerComponent);
                formState.blocks[blockId] = {
                    id: blockId,
                    formId: formId,
                    model: containerComponent,
                    parentBlockId: parentBlockId,
                    config: getConfig(containerComponent.attrs, formConfig),
                };

                formState.fieldStates[blockId] = evaluateState( containerComponent, parentBlockId, formState.fieldStates, contextCache, state);
            }
        );

    findComponents(viewModel, hasFieldClass)
        .forEach( fieldComponent =>
            {
                const formId = findForm(fieldComponent, defaultFormId);

                const modelId = fieldComponent.attrs.id;
                const parentBlockId = findParentBlockId(fieldComponent);
                const field = {
                    id: modelId,
                    model: fieldComponent,
                    formId: formId,
                    parentBlockId: parentBlockId,
                    config: getFieldConfig(fieldComponent.attrs, parentBlockId ? formState.blocks[parentBlockId].config : defaultConfig ),
                    valueExpr: fieldComponent.exprs.value,
                };
                //console.log(describe(field.model), field.config);
                formState.fields[modelId] = field;

            }
        );

    initializeFields(formState, state, contextCache);

    return formState;
}


export function validateField(state, fieldId)
{
    const fieldValues = getFieldValues(state);
    return propertyConverter.fromUser(fieldValues[fieldId]);
}



export function prepareViewModel(mutableState)
{
    const viewModel = getViewModel(mutableState);

    updateFormExpressionState(mutableState);

    const { content } = viewModel;
    for (let name in content)
    {
        if (content.hasOwnProperty(name))
        {
            prepareComponentsRecursive(content[name], null, components, viewModel);
        }
    }
}

function prepareComponentsRecursive(component, parent, components, viewModel)
{
    component.parent = parent;

    const descriptor = components[component.name];
    if (descriptor)
    {
        component.descriptor = descriptor;

        const { propTypes } = descriptor;

        const exprs = component.exprs;
        for (let name in propTypes)
        {
            if (propTypes.hasOwnProperty(name))
            {
                const propDecl = propTypes[name];
                if (propDecl.type === "CURSOR_EXPRESSION" || propDecl.type === "CONTEXT_EXPRESSION")
                {

                    const entry = exprs[name];
                    if (entry)
                    {
                        //console.log(propDecl.type, component, name);

                        const code = entry.code;

                        let contextName = entry.contextName;
                        if (!contextName)
                        {
                            contextName = "ctx";
                        }

                        // we create an actual js function from the code and wrap in a function providing access
                        // to our rtView. rtView will always contain a reference to the future state we're constructing
                        // so all scope references resolve to the actual current values etc.
                        const fn = new Function(contextName, "_v", code);
                        entry.fn = function(ctx)
                        {
                            return fn(ctx, rtView);
                        };

                        if (entry.parent)
                        {
                            const parentComponent = locateComponent(viewModel, entry.parent);
                            if (!parentComponent)
                            {
                                throw new Error("No parent component found for " + entry.parent + " in " + viewModel.name )
                            }
                            entry.parent = parentComponent;
                        }
                    }
                }
            }
        }
    }

    const kids = component.kids;
    if (kids)
    {
        for (let i = 0; i < kids.length; i++)
        {
            prepareComponentsRecursive(kids[i], component, components, viewModel);
        }
    }
}

export function updateFormExpressionState(state)
{
    rtView.updateState(state);
}
