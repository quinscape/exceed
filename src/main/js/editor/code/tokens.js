var TokenIterator = require("brace").acequire("ace/token_iterator").TokenIterator;

function trim(s)
{
    return s.replace(/^\s*(.*?)\s*$/, "$1");
}

/**
 * Creates a new model location object
 * @constructor
 */
function ModelLocation()
{
    /**
     * Current model chain starting with the current model followed by all its parents.
     *
     * The entries are objects with {model: Model, index: current index within the model kids on that level}
     *
     * @type Array
     */
    this.parentPath = [];

    /**
     * current attribute or null
     * @type {?string}
     */
    this.attr = null;

    /**
     * if attr is set and this is true, the action happend in the attribute value,
     * otherwise it happened on the attribute name
     * @type {boolean}
     */
    this.attrValue = false;

    /**
     *
     * true if the current attribute value is an expression
     *
     * @type {boolean}
     */
    this.expression = false;
}

function setAttr(model, attrName, value)
{
    var attrs = model.attrs;
    if (!attrs)
    {
        attrs = model.attrs = {};
    }
    attrs[attrName] = value;
}
/**
 * Helper module for handling code editor tokens
 *
 * @type {{currentLocation: module.exports.currentLocation}}
 */
module.exports = {
    /**
     * Inspects the token stream to find the model position corresponding to the current editor position.
     *
     * @param session   {EditSession}
     * @param row       {number} row of position to locate
     * @param column    {number} column of position to locate
     * @param model     view model
     *
     * @returns {ModelLocation} model location
     */
    currentLocation: function (session, row, column, model)
    {

        var entry;
        var iterator = new TokenIterator(session, 0, 0);

        var loc = new ModelLocation();

        var stack = loc.parentPath;

        var start = session.getTokenAt(row, column);

        if (!start)
        {
            return null;
        }


        var tokenStart = start.start;

        var startTokenLen = start.value.length;

        //console.log("start token", start, column);
        if ((start.type === "meta.tag.punctuation.tag-close.xml" || start.type === "meta.tag.punctuation.end-tag-open.xml") && column < start.start + startTokenLen)
        {
            return null;
        }

        var token, last;

        token = iterator.getCurrentToken();
        while (token)
        {
            //console.log("token", JSON.stringify(token));

            if (last && last.type === "meta.tag.punctuation.tag-open.xml")
            {
                if (!token || token.type !== "meta.tag.tag-name.xml")
                {
                    break;
                }

                loc.valid = false;

                if (!stack.length)
                {
                    stack.unshift({
                        model: model.root,
                        index: -1
                    });
                }
                else
                {
                    entry = stack[0];
                    stack.unshift({
                        model: entry.model.kids[++entry.index],
                        index: -1
                    });
                }
            }
            else if (token.type === "entity.other.attribute-name.xml")
            {
                loc.attr = token.value;
            }
            else if (token.type === "expression-open.xml")
            {
                loc.expression = true;
            }
            else if (token.type === "expression-close.xml")
            {
                loc.expression = false;
            }
            else if (token.type === "text.tag-whitespace.xml")
            {
                loc.attr = null;
            }
            else if (token.type === "meta.tag.punctuation.tag-close.xml")
            {
                loc.attr = null;
                loc.valid = true;

                if (token.value === "/>")
                {
                    stack.shift();
                    loc.attr = null;
                }
            }
            else if (token.type === "meta.tag.punctuation.end-tag-open.xml")
            {
                stack.shift();
                loc.attr = null;
            }

            loc.attrValue = (start.type === "string.attribute-value.xml" &&  column < start.start + startTokenLen) || loc.expression;

            var currentRow = iterator.getCurrentTokenRow();
            var currentColumn = iterator.getCurrentTokenColumn();

            //console.log("check", currentRow, row, currentColumn, tokenStart, loc);

            if (currentRow === row && currentColumn === tokenStart)
            {
                if (!loc.valid)
                {
                    // if the location is marked not valid, we have an incomplete tag which we remove from the
                    // stack
                    var removed = loc.parentPath.shift();
                    console.log("Remove invalid parent", removed);
                }

                // if we did not parse any tag-names yet, we just before or on the tag open of the root model
                if (!loc.parentPath.length)
                {
                    if (entry)
                    {
                        return null;
                    }

                    //console.log("Add root to empty path");
                    loc.parentPath = [{
                        model: model.root,
                        index: -1
                    }];
                }

                return loc;
            }

            last = token;
            token = iterator.stepForward();

        }
        return null;
    },
    /**
     * Converts the current editor session state to a JSON model.
     *
     * @param session
     * @param withPosition if true, add pos attribute with start end and pos
     * @returns view model
     */
    toModel: function (session, withPosition)
    {
        var iterator = new TokenIterator(session, 0, 0);

        var viewModel = { root: null };

        var token, last, currentComponent, attrName, attrValue, record = false;

        var stack = [];

        var addKid = function (kid)
        {
            var kids = currentComponent.kids;
            if (!kids)
            {
                currentComponent.kids = [ kid ];
            }
            else
            {
                kids.push( kid);
            }
        };

        token = iterator.getCurrentToken();
        while (token)
        {
            //console.log("toModel: token", JSON.stringify(token));


            if (last && last.type === "meta.tag.punctuation.tag-open.xml")
            {
                if (!token || token.type !== "meta.tag.tag-name.xml")
                {
                    break;
                }

                var newComponent = {
                    name: token.value
                };

                if (withPosition)
                {
                    newComponent.pos = {
                        start: {
                            row: iterator.getCurrentTokenRow(),
                            column: iterator.getCurrentTokenColumn() - 1
                        }
                    };
                    //console.log("SET START", newComponent.name, newComponent.pos);
                }

                if (!currentComponent)
                {
                    if (viewModel.root !== null)
                    {
                        throw new Error("Already have root");
                    }

                    viewModel.root = currentComponent = newComponent;

                }
                else
                {
                    addKid(newComponent);
                }
                stack.unshift(currentComponent);
                currentComponent = newComponent;
            }
            else if (token.type === "entity.other.attribute-name.xml")
            {
                attrName = token.value;
                attrValue = "";
            }
            else if (token.type === "expression-open.xml")
            {
                record = true;
                attrValue = "{ ";

                last = token;
                token = iterator.stepForward();
                continue;
            }
            else if (token.type === "expression-close.xml")
            {
                if (attrName)
                {
                    setAttr(currentComponent, attrName, attrValue + " }");
                }
                else
                {
                    addKid({
                        name: "[String]",
                        attrs: {
                            "value" : attrValue + "}"
                        }
                    });

                }
                record = false;
            }
            else if (token.type === "string.attribute-value.xml")
            {
                setAttr(currentComponent, attrName, token.value.substring(1,token.value.length - 1));
            }
            else if (token.type === "meta.tag.punctuation.tag-close.xml")
            {
                attrName = null;

                if (token.value === "/>")
                {
                    if (withPosition)
                    {
                        var end = {
                            row: iterator.getCurrentTokenRow(),
                            column: iterator.getCurrentTokenColumn() + token.value.length
                        };
                        currentComponent.pos.end = end;

                        //console.log("SET END", currentComponent.name, end);
                    }

                    currentComponent = stack.shift();
                }
            }
            else if (token.type === "meta.tag.punctuation.end-tag-open.xml")
            {
                if (withPosition)
                {
                    currentComponent.pos.end = {
                        row: iterator.getCurrentTokenRow(),
                        column: iterator.getCurrentTokenColumn() + token.value.length + currentComponent.name.length + 1
                    };
                    //console.log("SET END", currentComponent.name, currentComponent.pos.end);
                }

                currentComponent = stack.shift();
                attrName = null;
            }
            else if (token.type === "text.xml")
            {
                var text = trim(token.value);
                if (text.length > 0)
                {
                    addKid({
                        name: "[String]",
                        attrs: {
                            "value" : text
                        }
                    });
                }
            }

            if (record)
            {
                attrValue += token.value;
            }

            last = token;
            token = iterator.stepForward();
        }
        return viewModel;

    },
    /**
     * Fills in rows and columns for in the list of given expression errors.
     *
     * The errors come from the server with component index and attribute name and this walks through the token
     * stream and walks along the error list (sorted by component index) to find the row and column of first expression
     * token
     *
     * @param session               editor session
     * @param expressionErrors      {Array} Array of expression errors (ace annotation objects with additional fields)
     */
    fillInErrorLocations: function(session, expressionErrors)
    {
        var row, column;
        var iterator = new TokenIterator(session, 0, 0);

        var pos = 0;

        var componentIndex = -1;

        var token, attrName, inTag = false;

        var currentError = expressionErrors[pos];

        token = iterator.getCurrentToken();
        while (token && pos < expressionErrors.length)
        {
            //console.log("token", JSON.stringify(token));

            var text = null;

            if (token.type === "meta.tag.punctuation.tag-open.xml")
            {
                componentIndex++;
                inTag = true;
            }
            else if (token.type === "text.xml")
            {
                text = trim(token.value);
                if (text.length > 0)
                {
                    componentIndex++;
                    attrName = "value";
                }
            }
            else if (token.type === "meta.tag.punctuation.tag-close.xml")
            {
                attrName = null;
                inTag = false;
            }
            else if (!inTag && token.type === "expression-open.xml")
            {
                componentIndex++;
                attrName = "value";
            }


            if (componentIndex === currentError.componentIndex)
            {
                if (!currentError.attrName)
                {
                    row = iterator.getCurrentTokenRow();
                    column = iterator.getCurrentTokenColumn();
                    currentError.row = row;
                    currentError.column = column;
                    return;
                }

                if (token.type === "entity.other.attribute-name.xml")
                {
                    attrName = token.value;
                }
                else if (token.type === "expression-open.xml")
                {
                    if (currentError.attrName === attrName)
                    {
                        row = iterator.getCurrentTokenRow();
                        column = iterator.getCurrentTokenColumn();
                        currentError.row = row;
                        currentError.column = column;

                        currentError.text = currentError.text.replace(/at line ([0-9]+), column ([0-9]+)/, function (match, line,col)
                        {
                            return "at line " + ( +line + row) + ", column " + ( +col + column + 1);
                        });

                        currentError = expressionErrors[++pos];
                        return;
                    }
                }
            }

            token = iterator.stepForward();
        }
        return;
    }
};
