require("brace/mode/xml");

import { refetchView, showError } from "../../actions"

import { getViewModel, getCurrentViewDocument } from "../../reducers"
import Tokens from "./tokens";

const escapedRe = "\\\\(?:x[0-9a-fA-F]{2}|" + "u[0-9a-fA-F]{4}|" + "u{[0-9a-fA-F]{1,6}}|" + "[0-2][0-7]{0,2}|" + "3[0-7][0-7]?|" + "[4-7][0-7]?|" + ".)";

ace.define('ace/mode/exceed_expr_highlight_rules',["require","exports","module"], function (ace_require, exports, module)
{
    "use strict";
    const oop = ace_require("ace/lib/oop");
    const TextHighlightRules = ace_require("ace/mode/text_highlight_rules").TextHighlightRules;
    const identifierRe = "[a-zA-Z\\$_\u00a1-\uffff][a-zA-Z\\d\\$_\u00a1-\uffff]*\\b";
    const ExceedExprHighlightRules = function ()
    {
        const keywordMapper = this.createKeywordMapper({
            "variable.language": "context|model|props|vars",
            "constant.language": "null",
            "constant.language.boolean": "true|false"
        }, "identifier");

        this.braceNesting = 0;
        this.exprEnd = false;

        this.$rules = {
            "start": [
                {
                    token: "string",
                    regex: "'(?=.)",
                    next: "qstring"
                }, {
                    token: "constant.numeric",
                    regex: /0(?:[xX][0-9a-fA-F]+|[bB][01]+)\b/
                }, {
                    token: "constant.numeric",
                    regex: /[+-]?\d[\d_]*(?:(?:\.\d*)?(?:[eE][+-]?\d+)?)?\b/
                },
                {
                    token: ["punctuation.operator.exceed-expr"],
                    regex: /(console)(\.)(warn|info|log|error|time|trace|timeEnd|assert)\b/
                },
                {
                    token: keywordMapper,
                    regex: identifierRe
                },
                {
                    token: "punctuation.operator",
                    regex: /[.](?![.])/
                },
                {
                    token: "keyword.operator",
                    regex: /--|\+\+|\.{3}|===|==|=|!=|!==|<+=?|>+=?|!|&&|\|\||\?\:|[!$%&*+\-~\/^]=?/,
                    next: "start"
                },
                {
                    token: "punctuation.operator",
                    regex: /[?:,;.]/,
                    next: "start"
                },
                {
                    token: (paren) =>
                    {
                        // for attribute expressions, we have the unique '"{'and '}"' markers, but for expressions
                        // in xml text the simple {} conflict with object literals. So we track opening and closing
                        // braces
                        if (paren === "{")
                        {
                            //console.log("open brace", this.braceNesting);
                            this.braceNesting++;
                        }

                        return "paren.lparen";
                    },
                    regex: /[\[({]/,
                    next: "start"
                },
                {
                    token: (paren) =>
                    {
                        if (paren === "}")
                        {
                            // if we're already on level 0 here we are closing the expression (the braces are unbalanced because
                            // the first opening brace gets parses as "expression-open.xml"
                            if (this.braceNesting === 0)
                            {
                                // remember closing for state transition
                                this.exprEnd = true;
                                return "expression-close.xml";
                            }
                            else
                            {
                                // remember not closing for state transition
                                this.exprEnd = false;
                                //console.log("close brace", this.braceNesting);
                                this.braceNesting--;
                            }
                        }

                        return "paren.rparen";
                    },
                    regex: /[\])}]/,
                    next: (currentState, stack) =>
                    {
                        // This will be called after resolution of token has taken place.
                        // if we closed the expression
                        if (this.exprEnd)
                        {
                            this.exprEnd = false;
                            this.braceNesting = 0;

                            // we check if our current state stack depth
                            if (stack.length > 0)
                            {
                                // there is still a state on the stack, this will never actually happen cause
                                // embedRules cuts us off with its exit rule before
                                return "pop";
                            }
                            else
                            {
                                // if the stack is empty we've been coming out of "text.xml" and need to return
                                // that / the default state of the XmlHighlightRules
                                return "start";
                            }
                        }
                        else
                        {
                            // normal parens don't change state
                            return currentState;
                        }
                    }
                },
                {
                    token: "comment",
                    regex: /^#!.*$/
                }],
            "qstring": [
                {
                    token: "constant.language.escape",
                    regex: escapedRe
                },
                {
                    token: "string",
                    regex: "\\\\$",
                    next: "qstring"
                },
                {
                    token: "string",
                    regex: "'|$",
                    next: "start"
                },
                {
                    defaultToken: "string"
                }]
        };
        this.normalizeRules();
    };
    oop.inherits(ExceedExprHighlightRules, TextHighlightRules);

    exports.ExceedExprHighlightRules = ExceedExprHighlightRules;
});

ace.define('ace/mode/exceed_view_highlight_rules',["require","exports","module"], function(ace_require, exports, module) {

    const oop = ace_require("ace/lib/oop");

    const XmlHighlightRules = ace_require("ace/mode/xml_highlight_rules").XmlHighlightRules;
    const ExceedExprHighlightRules = ace_require("ace/mode/exceed_expr_highlight_rules").ExceedExprHighlightRules;



    const ExceedViewHighlightRules = function ()
    {

        XmlHighlightRules.call(this);

        this.$rules['start'].unshift([
            {
                token: "expression-open.xml",
                regex: '\\{\\s*',
                push: [
                    {
                        token: "expression-close.xml",
                        regex: '\\s*\\}',
                        next: "pop"
                    },
                    {
                        include: "expr-start"
                    },
                    {
                        defaultToken: "expr-start"
                    }]
            }
        ]);

        this.$rules['attribute_value'] = [
            {
                token: "expression-open.xml",
                regex: '"\\{\\s*',
                push: [
                    {
                        token: "expression-close.xml",
                        regex: '\\s*\\}\\s*"',
                        next: "pop"
                    },
                    {
                        include: "expr-start"
                    },
                    {
                    defaultToken: "expr-start"
                }]
            },
            {
                token: "string.attribute-value.xml",
                regex: '"',
                push: [
                    {
                        token: "string.attribute-value.xml",
                        regex: '"',
                        next: "pop"
                    },
                    {
                        include: "attr_reference"
                    },
                    {
                        defaultToken: "string.attribute-value.xml"
                    }
                ]
            }
        ];

        this.embedRules(ExceedExprHighlightRules, "expr-", [
            {
                token: "expression-close.xml",
                regex: '\\s*\\}\\s*"',
                next: "pop"
            }
        ]);

        //console.log("view",JSON.stringify(this.$rules['attribute_value'], null, "  "));
        this.normalizeRules();
    };

    oop.inherits(ExceedViewHighlightRules, XmlHighlightRules);

    exports.ExceedViewHighlightRules = ExceedViewHighlightRules;
});

ace.define('ace/mode/exceed_view',["require","exports","module"], function(ace_require, exports, module) {

    const oop = ace_require("ace/lib/oop");
    const XmlMode = ace_require("ace/mode/xml").Mode;

    const store = require("../../service/store").default;

    const ExceedViewHighlightRules = ace_require("ace/mode/exceed_view_highlight_rules").ExceedViewHighlightRules;

    const Mode = function ()
    {
        XmlMode.call(this);
        this.HighlightRules = ExceedViewHighlightRules;

        const old = this.createWorker;
        this.createWorker = function (session)
        {
            const worker = old(session);
            //worker.first = true;

            worker.on("error", (e) =>
            {
                //console.log("worker error", e);

                if (session.suppressPreviewOnce)
                {
                    session.suppressPreviewOnce = false;
                    return;
                }

                const annos = session.getAnnotations();

                if (!annos.length)
                {
                    Tokens.syncSession(store, session);

                    const state = store.getState();
                    const document = getCurrentViewDocument(state);
                    const viewModel = getViewModel(state);

                    if (document.viewName === viewModel.name)
                    {
                        store.dispatch(
                            refetchView()
                        )
                        .then(function (data)
                        {
                            if (data.error)
                            {
                                const expressionErrors = data.detail;
                                if (expressionErrors)
                                {
                                    Tokens.fillInErrorLocations(session, expressionErrors);
                                    session.setAnnotations(annos.concat(expressionErrors));
                                }
                            }
                            //worker.first = false;
                        })
                        .catch(function (e)
                        {
                            console.error(e);

                            showError(new Error(JSON.stringify(e)));
                        });
                    }
                }
                session.setAnnotations(annos);

                //
                //annos.push({
                //    row: 0,
                //    column: 0,
                //    text: "test error",
                //    type: "error"
                //});
                //
                //console.log("latched on", e);
            });
            return worker;
        }

    };
    oop.inherits(Mode, XmlMode);

    exports.Mode = Mode;
});

