import React from "react"
import ReactDOMServer from "react-dom/server"
import path from "path"
import fs from "fs"
import Template from "./template"

import DocNav from "./DocNav"

const html = fs.readFileSync(path.join(__dirname, "doc-template.html"), "UTF-8");
const template = new Template(html);

const docDir = path.join(__dirname, "../../doc");

export default function(docs, index)
{
    const doc = docs[index];

    const { name, title, component } = doc;

    const html = template.render({
        TITLE: title,
        CONTENT:
            ReactDOMServer.renderToStaticMarkup(
                <div>
                    <DocNav docs={ docs }/>
                    { component }
                </div>
            )
    });

    fs.writeFileSync(
        path.join(docDir, name + ".html"),
        html,
        "UTF-8"
    );
}
