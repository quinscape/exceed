// shelljs ( https://github.com/shelljs/shelljs )

import "./webpack"

import React from "react"
import shell from "shelljs"
import fs from "fs"
import path from "path"
import renderDoc from "./doc/renderDoc"

import IndexPage from "./doc/IndexPage"
import ModelDocs from "./doc/ModelDocs"
import ComponentDocs from "./doc/ComponentDocs"
import ExpressionDocs from "./doc/ExpressionDocs"
import ComponentClassIndex from "./doc/ComponentClassIndex"
import Markdown from "./doc/Markdown"

const  MODEL_DOCS_PATH = "../src/main/base/resources/js/model-docs.json";
/**
 * ModelDocs data resulting from the current state of annotated java source files for models.
 * Data generated by de.quinscape.exceed.tooling.GenerateModelDocs
 */
const modelDocsData = JSON.parse(fs.readFileSync(path.join(__dirname, MODEL_DOCS_PATH), "UTF-8"));

//console.log({modelDocsData});

if (!modelDocsData)
{
    throw new Error("Cannot read " + MODEL_DOCS_PATH);
}

const componentData = {
    names : [],
    descriptors: {}
};

/**
 * Read all current components.json files
 */
shell.find( "src/main/js/components/std/**/components.json").forEach(file => {
    const json = fs.readFileSync(file, "UTF-8");

    try
    {
        const { components } = JSON.parse(json);

        for (let name in components)
        {
            if (components.hasOwnProperty(name))
            {
                const d = components[name];
                d.pkg = file;

                componentData.names.push(name);
                componentData.descriptors[name] = d;
            }
        }
    }
    catch(e)
    {
        console.error("Error reading " + file, e);
    }

});

componentData.names = componentData.names.sort();

if (shell.test("-d", "doc"))
{
    shell.rm("-R", "doc");
}
shell.mkdir("doc");
shell.mkdir("doc/css");
shell.mkdir("doc/fonts");
shell.cp("src/main/base/resources/css/*.min.css", "doc/css");
shell.cp("src/main/base/resources/fonts/*", "doc/fonts");


const markDownData = [];

/**
 * Read markdown docs
 */
shell.find( "src/main/doc/**/*.md").forEach(file => {

    const content = fs.readFileSync(file, "UTF-8");

    const title = /(.*)\n/.exec(content)[1];

    markDownData.push({
        name: path.basename(file, ".md"),
        title: title,
        component: <Markdown data={ content }/>
    });
});


const content = [
    {
        name: "index",
        title: "Overview",
        component: <IndexPage/>
    },
    {
        name: "model",
        title: "Model Docs",
        component: <ModelDocs {...modelDocsData}/>
    },
    {
        name: "component",
        title: "Component Docs",
        component: <ComponentDocs data={ componentData }/>
    },
    {
        name: "expression",
        title: "Expression Docs",
        component: <ExpressionDocs definitions={ modelDocsData.definitions }/>
    },
    {
        name: "class-index",
        title: "Components by class",
        component: <ComponentClassIndex descriptors={ componentData.descriptors }/>
    }
];

Array.prototype.splice.apply(content, [1,0].concat(markDownData));

for (let i = 0; i < content.length; i++)
{
    renderDoc(content, i);
}

