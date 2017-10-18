import React from "react"

import keys from "../../src/main/js/util/keys"

import ExpressionChapter from "./ExpressionChapter"

function sortByName(a,b)
{
    return a.name.localeCompare(b.name);
}

class ExpressionDocs extends React.Component {

    render()
    {
        const { definitions } = this.props.definitions;

        const byChapter = {};

        for (let name in definitions)
        {
            if (definitions.hasOwnProperty(name))
            {
                const def = definitions[name];
                const chapter = def.chapter;
                let array = byChapter[chapter];
                if (!array)
                {
                    array = [ def ];
                    byChapter[chapter] = array;
                }
                else
                {
                    array.push(def);
                }
            }
        }
        const chapterNames = keys(byChapter);
        chapterNames.sort();

        const chapters = [];

        for (let i = 0; i < chapterNames.length; i++)
        {
            const name = chapterNames[i];
            const array = byChapter[name];
            array.sort(sortByName);

            chapters.push(
                <ExpressionChapter
                    key={ chapters.length }
                    name={ name }
                    definitions={ array }
                />
            );
        }

        return (
            <div>
                {
                    chapters
                }
            </div>
        )
    }
}

export default ExpressionDocs
