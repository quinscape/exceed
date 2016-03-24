var viewService = require("../service/view");

module.exports = function (model)
{
    return viewService.navigateTo(model.url);
};
