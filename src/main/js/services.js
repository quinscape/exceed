/**
 * This module is exported as package "exceed-services" for external code.
 *
 * @type {{Hub: (Hub|exports|module.exports)}}
 * @module "exceed-services"
 */
module.exports = {
    hub: require("./service/hub"),
    componentService: require("./service/component"),
    render: require("./service/render")
};
