var React = require("react");

var viewService = require("../../service/view");
var uri = require("../../util/uri");
var cx = require("classnames");

var LinkedStateMixin = require("react-addons-linked-state-mixin");

function maxNameLen(props)
{
    var max = -Infinity;
    for (var i = 0; i < props.length; i++)
    {
        max = Math.max(props[i].name.length, max);
    }
    return max;
}

var tenSpaces = "          ";

function indent(n)
{
    var s = "";
    while (n > 10)
    {
        s+=tenSpaces;
        n-=10;
    }
    return n > 0 ? s + tenSpaces.substr(10 - n) : s;
}

const MODEL_PACKAGE = "de.quinscape.exceed.model";
const JAVA_PACKAGE = "java.lang.";

function typeName(name)
{
    if (name.indexOf(MODEL_PACKAGE) == 0)
    {
        return name.substring(MODEL_PACKAGE.length + 1);
    }
    else if (name === "java.lang.Object")
    {
        return "any";
    }
    else if (name.indexOf(JAVA_PACKAGE) === 0)
    {
        return name.substring(JAVA_PACKAGE.length);
    }
    return name;
}

function transformDocs(docs)
{
    return docs.replace(/@return/g,"")
        .replace(/\{@link (.*?)}/g, function (match, link)
        {
            if (link.indexOf("de.quinscape.exceed.runtime") < 0)
            {
                return "<span class=\"link\">" + link + "</span>";
            }
            return link;
        })
        .replace(/\s*$/g,"");
}
var ModelDocs = React.createClass({

    mixins: [ LinkedStateMixin ],

    getInitialState: function ()
    {
        return {
            typeInfo: null,
            showIgnored: false
        };
    },

    onClick: function (ev)
    {
        if (ev.target.className === "link")
        {
            alert(ev.target.innerHTML);
        }
    },

    renderRows: function (parent, typeInfo)
    {
        var rows = [];

        typeInfo.properties.map((propInfo, idx) =>
        {
            var type = propInfo.type;
            var typeHint = propInfo.typeHint;

            var valueElem;
            if (type.indexOf("de.quinscape.exceed") === 0)
            {
                valueElem = (
                    <a className="type" onClick={ (ev) => viewService.updateComponent(this.props.id,  { type: type }, uri(window.location.href, { type: type}, true)) } > { typeName(type) } </a>
                );
            }
            else if ((type === "java.util.List" || type === "java.util.Set"))
            {
                valueElem = (
                    <span>
                        { 'Array of '}
                        {
                            typeHint ?
                                <a className="type"
                                   onClick={ (ev) => viewService.updateComponent(this.props.id,  { type: typeHint }, uri(window.location.href, { type: typeHint}, true)) }> { typeName(typeHint) } </a>
                                :
                                ""
                        }
                                </span>
                );
            }
            else if ((type === "java.util.Map"))
            {
                valueElem = (
                    <span>
                                    { 'Map of '}

                        {
                            typeHint ?
                                <a className="type"
                                   onClick={ (ev) => viewService.updateComponent(this.props.id,  { type: typeHint }, uri(window.location.href, { type: typeHint}, true)) }> { typeName(typeHint) } </a>
                                :
                                ""
                        }
                                </span>
                );
            }
            else
            {
                valueElem = (
                    <span>{ typeName(type) }</span>
                );
            }

            if (!propInfo.ignored || this.state.showIgnored)
            {
                rows.push(
                    <tr key={ typeInfo.type + "." + propInfo.name } className={ cx({ "ignored" : propInfo.ignored }) }>
                        <td className="prop">{ parent ? parent + propInfo.name : propInfo.name }</td>
                        <td>{ valueElem }</td>
                        <td>{ propInfo.docs && <span className="prop-doc" dangerouslySetInnerHTML={{ __html: transformDocs(propInfo.docs) }}></span> }</td>
                    </tr>
                );
            }

            var includedTypeInfo = propInfo.included;
            if (includedTypeInfo)
            {

                rows.push(
                    <tr key={ includedTypeInfo.type + "." + includedTypeInfo.type } className={ cx({ "ignored" : propInfo.ignored }) }>
                        <th colSpan="3">{ includedTypeInfo.type }</th>
                    </tr>

                );
                rows = rows.concat( this.renderRows(parent + "\u00a0\u00a0\u00a0\u00a0", includedTypeInfo));
            }
        });

        return rows;
    },

    render: function ()
    {
        var typeInfo = this.props.typeInfo;
        return (
            <div className="model-docs" onClick={this.onClick}>
                <h1>{ typeInfo.type }</h1>
                { typeInfo.docs && <div className="class-doc" dangerouslySetInnerHTML={{ __html: transformDocs(typeInfo.docs) }} /> }
                <table className="table">
                    <thead>
                    <tr>
                        <th>
                            Property
                        </th>
                        <th>
                            Type
                        </th>
                        <th>
                            Description
                        </th>
                    </tr>
                    </thead>
                    <tbody>
                    { this.renderRows("", typeInfo) }
                    </tbody>
                </table>
                <form className="form-inline" onSubmit={ (ev) => ev.preventDefault() }>
                    <div className="checkbox">
                        <label>
                            <input type="checkbox" checkedLink={ { value: this.state.showIgnored, requestChange: (newVal) =>
                            {
//                                console.log("NEWVAL", newVal);

                                this.setState({
                                    showIgnored: newVal
                                })
                            }} }/> Show ignored props
                        </label>
                    </div>
                </form>
            </div> );
    }
});

module.exports = ModelDocs;
