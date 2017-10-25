import React from "react"

import marked from "marked"

import Highlight from "highlight.js"

const renderer = new marked.Renderer();

/**
 * Extend marked to render bootstrap tables
 * 
 * @param header
 * @param body
 * @returns {string}
 */
renderer.table = function(header, body) {
    return '<table class="table table-striped table-hover table-bordered">\n'
        + '<thead>\n'
        + header
        + '</thead>\n'
        + '<tbody>\n'
        + body
        + '</tbody>\n'
        + '</table>\n';
};



marked.setOptions({
    renderer: renderer,
    highlight: function (code) {
        return Highlight.highlightAuto(code).value;
    }
});

class Markdown extends React.Component {

    shouldComponentUpdate(nextProps, nextState)
    {
        return this.props.data !== nextProps.data;
    }

    render()
    {
        return (
            <div dangerouslySetInnerHTML={ this.getHTML() }/>
        )
    }

    getHTML()
    {
        return { __html: marked(this.props.data)}
    }
}

export default Markdown
