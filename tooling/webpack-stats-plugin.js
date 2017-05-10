const path = require("path");
const fs = require("fs");

const endsWith = require("../src/main/js/util/endsWith");

/**
 * Mini webpack plugin to receive a reduced version of the current webpacks stats.
 *
 * (Server-side container is de.quinscape.exceed.model.meta.WebpackStats )
 *
 * @param dest      path to write the resulting json file to.
 *
 * @returns {Function}  plugin function
 */
module.exports = function (dest)
{
    return function()
    {
        this.plugin("done", function (stats)
        {
            const data = stats.toJson();

            //console.log(JSON.stringify(data.assets, null, 4));

            const exceedWebpackStats = {
                entries: data.assets

                    // we only want entry point .js files
                    .filter(asset => endsWith(asset.name, ".js") && asset.chunkNames.length )

                    // and for now we just sort by name so "common" is initialized before "main"
                    .sort(sortByName)
            };
            
            fs.writeFileSync(
                dest,
                JSON.stringify(exceedWebpackStats)
            );
        });
    }
};


function sortByName(a,b)
{
    const nameA = a.name;
    const nameB = b.name;

    if (nameA < nameB)
    {
        return -1;
    }
    else if (nameA > nameB)
    {
        return 1;
    }
    else
    {
        return 0;
    }
}
