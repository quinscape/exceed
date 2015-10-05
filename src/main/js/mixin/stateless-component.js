var StatelessComponent = {

    /**
     * Decides updates based on the object identity of the original injection value. Since we do immutable updates
     * when updating the injection tree, this can quickly tell that things could not possibly have changed for this
     * component as all injections (and therefore vars) are the exact same as they were before.
     *
     * Do *not* mix this in if your component has state.
     *
     * @param nextProps
     * @returns {boolean}
     */
    shouldComponentUpdate: function (nextProps)
    {
        if (!this.props._injection)
        {
            return true;
        }
        var result = this.props._injection.injection !== nextProps._injection.injection;
        console.info("Update %s => %s", this.__proto__.constructor.displayName, result);
        return result;
    }

};

module.exports = StatelessComponent;
