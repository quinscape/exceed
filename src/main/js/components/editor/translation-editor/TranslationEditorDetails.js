const React = require("react");

const assign = require("object-assign");

const values = require("../../../util/values");

const Link = require("../../../ui/Link");
const i18n = require("../../../service/i18n");

const Form = require("../../std/form/Form");
const Field = require("../../std/form/Field");

const Modal = require("react-bootstrap/lib/Modal");


function describeLocation(processName, viewName)
{
    if (processName)
    {
        if (viewName)
        {
            return i18n("View '{1}' in Process '{0}'", processName, viewName )
        }
        else
        {
            return i18n("Process '{0}'", processName)
        }
    }
    else
    {
        if (viewName)
        {
            return i18n("View '{0}'", viewName);
        }
        else
        {
            return i18n("No Location");
        }
    }
}

var TranslationEditorDetails = React.createClass({
    render: function ()
    {
        const detailCursor = this.props.detailCursor;

        if (!detailCursor)
        {
            return false;
        }

        const detail = this.props.detail;
        const supportedLocales = this.props.supportedLocales;

        var tagName = detailCursor.get(['name']);

        var refs = detailCursor.get(['references']);

        return (
            <Modal show={ detail !== null } onHide={ this.props.onClose } bsSize="lg">
                <Modal.Header closeButton>
                    <Modal.Title>{ i18n("Translation {0}", tagName ) }</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <div className="container-fluid">
                        <div className="row">
                            <div className="col-md-12">
                            {
                                <Form data={ detailCursor } horizontal={ false }>
                                    <fieldset>
                                        <legend>{ i18n('Translations') }</legend>

                                        {
                                            supportedLocales.map(locale =>
                                                <Field key={ locale } label={ i18n(locale) } value={ detailCursor.getCursor(["translations", locale, "translation"]) }/>
                                            )
                                        }
                                    </fieldset>
                                    <fieldset>
                                        <legend>{ i18n('Local Translations') }</legend>
                                        <table className="table table-striped table-hover table-bordered">
                                            <thead>
                                            <tr>
                                                <th>{ i18n('Locale') }</th>
                                                <th>{ i18n('Rule Location') }</th>
                                                <th>{ i18n('Translation') }</th>
                                                <th>{ i18n('Action') }</th>
                                            </tr>
                                            </thead>
                                            <tbody>
                                            {
                                                detailCursor.get(['localTranslations']).map((translation, idx) =>
                                                    <tr key={ idx } >
                                                        <td>
                                                            <div className="form-group">
                                                                <label htmlFor={ "localeSel" + idx } className="sr-only">Locale</label>
                                                                <select
                                                                    id={ "localeSel" + idx }
                                                                    className="form-control"
                                                                    defaultValue={ supportedLocales[0] }
                                                                    onChange={ (e) => this.props.setLocale(detail, idx, e.target.value) }>
                                                                    {
                                                                        supportedLocales.map( (locale, localeIndex) =>
                                                                            <option key={localeIndex} value={ locale }> { i18n(locale) }</option>
                                                                        )
                                                                    }
                                                                </select>
                                                            </div>
                                                        </td>
                                                        <td>
                                                            <div className="form-group">
                                                                <label htmlFor={ "ruleeSel" + idx } className="sr-only">Rule Location</label>
                                                                <select
                                                                    id={ "ruleeSel" + idx }
                                                                    className="form-control"
                                                                    defaultValue={ describeLocation(translation.processName, translation.viewName) }
                                                                    onChange={ (e) => this.props.setRule(detail, idx, e.target.value) }>
                                                                    <option value={ -1 }> { describeLocation(null, null) }</option>
                                                                    {
                                                                        this.props.ruleLocations.map( (ruleLocation, ruleIndex) =>
                                                                            <option key={ruleIndex} value={ ruleIndex }> { describeLocation(ruleLocation.processName, ruleLocation.viewName) }</option>
                                                                        )
                                                                    }
                                                                </select>
                                                            </div>
                                                        </td>
                                                        <td>
                                                            <Field labelClass="sr-only" label={ i18n("Translation") } value={ detailCursor.getCursor(['localTranslations', idx,'translation']) } />
                                                        </td>
                                                        <td>
                                                            <Link icon="remove" text="Remove" onClick={ e => this.props.removeLocalTranslation(detail, idx) } />
                                                        </td>
                                                    </tr>
                                                )
                                            }
                                            <tr>
                                                <td/>
                                                <td/>
                                                <td/>
                                                <td>
                                                    <Link icon="plus" text="New" onClick={ e => this.props.addLocalTranslation(detail) } />
                                                </td>
                                            </tr>
                                            </tbody>
                                        </table>

                                    </fieldset>
                                    {
                                        refs && refs.length &&
                                        <fieldset>
                                            <legend>{ i18n('References') }</legend>
                                            <table className="table table-striped table-hover table-bordered">
                                                <thead>
                                                <tr>
                                                    <th>{ i18n('Type') }</th>
                                                    <th>{ i18n('Name') }</th>
                                                </tr>
                                                </thead>
                                                <tbody>
                                                {
                                                    refs.map((ref,idx) =>
                                                        <tr key={ idx }>
                                                            <td>{ ref.type }</td>
                                                            <td>{ ref.name }</td>
                                                        </tr>
                                                    )
                                                }
                                                </tbody>
                                            </table>
                                        </fieldset>

                                    }
                                </Form>
                            }
                            </div>
                        </div>
                    </div>
                </Modal.Body>
            </Modal>
        );
    }
});

module.exports = TranslationEditorDetails;
