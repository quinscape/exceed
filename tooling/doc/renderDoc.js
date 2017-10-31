import React from "react"
import ReactDOMServer from "react-dom/server"
import cx from "classnames"
import path from "path"
import fs from "fs"
import Template from "./template"

import DocNav from "./DocNav"
import index from "../../src/main/js/reducers/editor/index";

const html = fs.readFileSync(path.join(__dirname, "../../src/main/doc/theme/doc-template.html"), "UTF-8");
const template = new Template(html);

function Link(props)
{
    const { className, docs,label, index } = props;

    return (
        index >= 0 && index < docs.length &&

            <a className={ cx("btn btn-link", className) } href={ "./" + docs[index].name + ".html" }>
                <small>
                {
                    label + docs[index].title
                }
                </small>
            </a>
        )
}

function Paging(props)
{
    const { docs, index } = props;

    return (
        <div className="toolbar clearfix">
            <Link
                docs={docs}
                label="Previous :"
                index={index - 1}
            />
            <Link
                className="pull-right"
                docs={docs}
                label="Next :"
                index={index + 1}
            />
        </div>
    );
}


export default function(docDir, docs, index)
{
    const doc = docs[index];

    const { name, title, component } = doc;

    const html = template.render({
        TITLE: title + " &ndash; Exceed Documentation",
        CONTENT:
            ReactDOMServer.renderToStaticMarkup(
                <div className="row">
                    <div className="col-md-1">

                    </div>
                    <div className="col-md-9">
                        <Paging docs={ docs } index={ index }/>
                        {
                            component
                        }
                        <Paging docs={ docs } index={ index }/>
                    </div>
                    <div className="col-md-2">
                        <DocNav docs={docs} active={ index }/>
                    </div>
                </div>
            )
    });

    fs.writeFileSync(
        path.join(docDir, name + ".html"),
        html,
        "UTF-8"
    );
}
