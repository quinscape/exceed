/**
 * Finds an occurence of a substring in a multiline document and returns the row and column
 * @param doc       multiline document
 * @param sub       substring
 * @param skip      instances to skip
 *
 * @returns {{row: number, col: number}}
 */
function posInDocument(doc, sub, skip)
{
    var pos;
    var start = 0;

    skip = skip || 0;

    do {
        pos = doc.indexOf(sub, start);
        //console.log("find -> ", pos);
        if (pos < 0)
        {
            throw new Error("Not found:" + sub);
        }
        start = pos + sub.length;
    } while (skip--);

    var until = doc.substring(0, pos);
    //console.log("until -> ", until);
    var lines = until.split(/\r?\n/);

    //console.log("lines -> ", lines);
    return {
        row: lines.length - 1,
        col: lines[lines.length - 1].length
    };
}

export default posInDocument
