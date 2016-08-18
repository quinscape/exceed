const React = require("react");
const Link = require("../../../ui/Link");
const Button = require("../../../ui/Button");

const i18n = require("../../../service/i18n");

var NamedSelector = React.createClass({
    render: function ()
    {
        const editing = this.props.editing;

        const filter = this.props.filter.toLocaleLowerCase();

        const names = filter ?  this.props.names.filter(n => n.toLocaleLowerCase().indexOf(filter) >= 0) : this.props.names ;

        const visibleCheck = !!this.props.visible;

        return (
            <div>
                <fieldset>
                    <legend>{ this.props.title }</legend>
                    <table className="table table-responsive table-condensed table-striped table-hover">
                        <thead>
                        <tr>
                            <th width="75%">Name</th>
                            <th width="25%">Action</th>
                        </tr>
                        </thead>
                        <tbody>
                        {
                            names.map(name =>
                            {
                                return (
                                    <tr key={ name } className={ name === editing && "selected" }>
                                        <td>
                                            {
                                                visibleCheck ?
                                                    <div className="checkbox">
                                                        <label>
                                                            <input type="checkbox"
                                                                   checked={ this.props.visible[name] }
                                                                   onChange={  e => this.props.toggle(name) }
                                                            />
                                                            { name }
                                                        </label>
                                                    </div>
                                                    : name
                                            }
                                        </td>
                                        <td>
                                            <div className="btn-toolbar" role="toolbar">
                                                <Link icon={ editing === name ? "arrow-left" : "edit" }
                                                      text={ editing === name ? i18n("Close") : i18n("Detail") }
                                                      onClick={ e => this.props.edit(editing !== name && name) }/>
                                            </div>
                                        </td>
                                    </tr>
                                );
                            })
                        }
                        <tr>
                            <td>
                            </td>
                            <td>
                                <Button
                                    icon="plus"
                                    text={ this.props.newLabel }
                                    accessKey={ this.props.newAccessKey }
                                    onClick={ this.props.new }
                                />
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </fieldset>
            </div>
        );

    }
});

module.exports = NamedSelector;
