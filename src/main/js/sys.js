/**
 * Holds several fields containing general system and deployment information. The actual values are filled
 * in at startup.
 * @type {{contextPath: string, appName: string}}
 */
module.exports  ={
    contextPath : null,
    appName: null,

    init: function(contextPath, appName)
    {
        this.contextPath = contextPath;
        this.appName = appName;
    }
};
