import React from "react"

import ReactDOMServer from "react-dom/server"

import cx from "classnames"

import marked from "marked"

import Highlight from "highlight.js"

class Markdown extends React.Component {


    getOptions()
    {
        if (!this._options)
        {
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

            /**
             * Extend marked to render images
             *
             * @param header
             * @param body
             * @returns {string}
             */
            renderer.image = function (href, title, text)
            {
                let s = '<img class="' + cx(href.indexOf("-right.") >= 0 ? "pull-right" : "img-responsive") + '" src="' + href+ '" alt="' + text + '"';

                if (title)
                {
                    s += ' title="' + title + '"';
                }

                return s + ">";
            };

            /**
             * Extend marked to render turn .md links into .html links
             *
             * @param header
             * @param body
             * @returns {string}
             */
            renderer.link = function(href, title, text)
            {
                const end = href.length - 3;
                if (href.indexOf(".md") === end)
                {
                    href = href.substring(0, end) + ".html";
                }

                let s = '<a class="" href="' + href + '"';

                if (title)
                {
                    s += ' title="' + title + '"';
                }

                return s + '>' + text + "</a>";
            };


            /**
             * Repurpose "del" text with embedded components
             *
             * @param header
             * @param body
             * @returns {string}
             */
            renderer.del = (text) =>
            {
                const Component = this.props.components[text];
                return ReactDOMServer.renderToStaticMarkup(Component);
            };


            this._options = {
                renderer: renderer,
                highlight: function (code) {
                    return Highlight.highlightAuto(code).value;
                }
            };

        }

        return this._options;

    }

    shouldComponentUpdate(nextProps, nextState)
    {
        return this.props.data !== nextProps.data;
    }

    render()
    {
        return (
            <div dangerouslySetInnerHTML={ {__html: marked(this.props.data, this.getOptions()) } } />
        )
    }
}

export default Markdown
