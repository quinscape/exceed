import React from "react"

import renderWithContext from "../util/render-with-context"

export class Rule extends React.Component {

    render()
    {
        const { children } = this.props;

        if (React.Children.count(children))
        {
            return children;
        }
        else
        {
            return (
                <span>
                {
                    children
                }
            </span>
            )
        }
    }
}

export function countGroups(regexp)
{
    let count = 0;

    let prevPos = 0;
    let pos;
    while ((pos = regexp.indexOf("(", prevPos)) >= 0)
    {
        if (pos === 0 || regexp.charAt(pos - 1) !== '\\')
        {
            count++;
        }
        prevPos = pos + 1;
    }
    return count;
}

class Annotate extends React.Component {

    render()
    {
        const { value} = this.props;
        const children = React.Children.toArray(this.props.children);

        const elements = [];

        const ruleCount = children.length;
        const combined = children.map( kid => "(" + kid.props.regexp + ")" ).join('|');

        const startIndexes = new Array(ruleCount + 1);

        // first index ( m[0] ) is the complete match for all rules
        let count = 1;
        children.forEach( (kid,idx) => {
            startIndexes[idx] = count;
            count += countGroups(kid.props.regexp) + 1
        });
        startIndexes[ruleCount] = count;

        //console.log({combined, startIndexes});

        const re =  new RegExp( combined, "g");

        let index = 0;

        let m;
        do
        {
            m = re.exec(value);
            if (m)
            {
                if (m.index > index)
                {
                    elements.push(value.substring(index, m.index));
                }

                // 0 is complete match, we want the group brackets -> 1
                for (let i=0; i < ruleCount; i++)
                {
                    const idx = startIndexes[i];
                    const end = startIndexes[i + 1];

                    const match = m[idx];
                    //console.log("match", idx, match);
                    if (match)
                    {
                        index = m.index + match.length;

                        const element = children[i].props.children;
                        if (typeof element === "function")
                        {
                            const args = Array.prototype.slice.call(m, idx, end);
                            elements.push(
                                React.cloneElement(
                                    element(... args),
                                    {
                                        key: elements.length
                                    }
                                )
                            );
                        }
                        else
                        {
                            elements.push(
                                React.cloneElement(
                                    element,
                                    {
                                        key: elements.length
                                    }
                                )
                            );
                        }

                        break;
                    }
                }
            }
        } while(m);

        elements.push(value.substring(index));

        return (
            <span className="annotated">
                {
                    elements
                }
            </span>
        )
    }
}

export default Annotate
