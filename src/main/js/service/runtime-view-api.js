/**
 * Runtime view API. Gets created inside the generated views.
 *
 * @param model     view model
 * @param data      view data
 * @constructor
 */
function RTView(model, data)
{
    this.name = model.name;
    this.root = model.root;
    this.data = data;
}

/**
 * Injects data into the static props of a component if the static props of a component
 * do not contain a value for that property.
 *
 * This way we can always override the data query / injection mechanisms in favor of local data.
 *
 * @param props     static props
 * @param data      data injection for this prop as per component query definition
 * @returns {*}
 */
RTView.prototype.inject = function (props, data)
{
    for (var k in data)
    {
        if (data.hasOwnProperty(k) && !props.hasOwnProperty(k))
        {
            props[k] = data[k];
        }
    }
    return props;
};

RTView.prototype.param = function (name)
{
    return this.data._exceed.locationParams[name];
};

module.exports = RTView;
