var assert = require("power-assert");
var Promise = require("es6-promise-polyfill").Promise;
var sinon = require("sinon");
var proxyquire = require("proxyquire");

var sys = require("../../../../src/main/js/sys");
sys.init("/test-context", "TestApp");

var data;

function initData()
{
    data = {
        calls: []
    }
}


var actionService = proxyquire("../../../../src/main/js/service/action", {
    "./ajax" : function(opts)
    {
        //console.log("AJAX OPTS", opts);

        if (opts.url === "/test-context/action/TestApp")
        {
            return Promise.resolve({ actionNames: ["foo", "bar"] });
        }
        else if (opts.url.indexOf("/test-context/action/TestApp/foo") === 0)
        {
            data.calls.push({
                server: true
            });
        }
        else if (opts.url.indexOf("/test-context/action/TestApp/bar") === 0)
        {
            data.calls.push({
                failed: true,
                error: new Error("Bar always fails")
            });
        }

        return Promise.resolve();
    }

});

describe("Action Service", function(){

    before(function ()
    {
        actionService._clearActions();
    });


	it("registers bulk required client actions", function()
	{
        initData();

        var testAction = function (model)
        {
            if (model.fail)
            {
                throw new Error("TEST_FAIL");
            }

            data.calls.push({
                model: model
            });
        };

        testAction.catch = function(e, model)
        {
            data.calls.push({
                model: model,
                failed: true,
                error: e
            });

            return Promise.resolve();
        };

        var wrappingAction = function (model)
        {
            return actionService.execute({ action : "bar" }, data).catch(function (e)
            {
                data.calls.push({
                    model: model,
                    failed: true,
                    error: e
                });
                return Promise.reject(new Error("caught"));            });
        };

        actionService.registerBulk({
            test: testAction,
            serverWrap: wrappingAction
        });

        var actions = actionService.getActions();
        assert(actions.test.client === true);
        assert(actions.test.handler === testAction);

	});

	it("registers named server actions", function()
	{
		actionService.initServerActions(["foo", "bar"]);
        var actions = actionService.getActions();
        assert(actions.foo.client === false);
        assert(actions.bar.client === false);

	});


    it ("executes client actions", function ()
    {
        initData();
        var model = {
            action: "test"
        };
        return actionService.execute(model)
            .then(function()
            {
                assert(data.calls.length === 1);
                assert(data.calls[0].model === model);
            })
    });

    it ("optionally handles client action failure", function ()
    {
        initData();

        return actionService.execute(
            {
                action: "test",
                fail: true
            })
            .then(function()
            {
                assert(data.calls.length === 1);
                assert(data.calls[0].failed);
                assert(String(data.calls[0].error).indexOf("TEST_FAIL") > 0);
            })
    });

    it ("can handle wrapped server function failure", function ()
    {
        initData();
        return actionService.execute(
            {
                action: "serverWrap"
            })
            .then(function()
            {
                assert(data.calls.length === 1);
                assert(data.calls[0].failed);
                assert(String(data.calls[0].error).indexOf("Bar always fails") > 0);
            })
    });

    it ("executes server actions", function ()
    {
        initData();
        var model = {
            action: "foo"
        };
        return actionService.execute(model)
            .then(function()
            {
                assert(data.calls.length === 1);
                assert(data.calls[0].server);
            })
    });


});
