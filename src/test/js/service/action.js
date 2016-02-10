var assert = require("power-assert");
var Promise = require("es6-promise-polyfill").Promise;
var sinon = require("sinon");
var proxyquire = require("proxyquire");

var sys = require("../../../../src/main/js/sys");
sys.init("/test-context", "TestApp");

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
            opts.data.calls.push({
                server: true
            });

            return Promise.resolve(opts.data);
        }
        else if (opts.url.indexOf("/test-context/action/TestApp/bar") === 0)
        {
            opts.data.calls.push({
                failed: true,
                error: new Error("Bar always fails")
            });
            return Promise.resolve(opts.data);
        }

        return Promise.reject(new Error("Unhandled ajax test opts" + JSON.stringify(opts)));
    }

});

describe("Action Service", function(){

    before(function ()
    {
        actionService.clearActions();
    });


	it("registers bulk required client actions", function()
	{
        var testAction = function (model, data)
        {
            if (model.fail)
            {
                throw new Error("TEST_FAIL");
            }

            data.calls.push({
                model: model
            });

            return Promise.resolve(data);
        };

        testAction.catch = function(e, model, data)
        {
            data.calls.push({
                model: model,
                failed: true,
                error: e
            });
            return Promise.resolve(data);
        };

        var wrappingAction = function (model, data)
        {
            return actionService.execute({ action : "bar" }, data).catch(function (e)
            {
                data.calls.push({
                    model: model,
                    failed: true,
                    error: e
                });
                return Promise.resolve(data);
            });
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
        var model = {
            action: "test"
        };
        return actionService.execute(model, { calls: []})
            .then(function(data)
            {
                assert(data.calls.length === 1);
                assert(data.calls[0].model === model);
            })
    });

    it ("optionally handles client action failure", function ()
    {
        return actionService.execute(
            {
                action: "test",
                fail: true
            },
            {
                calls:[]
            })
            .then(function(data)
            {
                assert(data.calls.length === 1);
                assert(data.calls[0].failed);
                assert(String(data.calls[0].error).indexOf("TEST_FAIL") > 0);
            })
    });

    it ("can handle wrapped server function failure", function ()
    {
        return actionService.execute(
            {
                action: "serverWrap"
            },
            {
                calls:[]
            })
            .then(function(data)
            {
                assert(data.calls.length === 1);
                assert(data.calls[0].failed);
                assert(String(data.calls[0].error).indexOf("Bar always fails") > 0);
            })
    });

    it ("executes server actions", function ()
    {
        var model = {
            action: "foo"
        };
        return actionService.execute(model, { calls: []})
            .then(function(data)
            {
                assert(data.calls.length === 1);
                assert(data.calls[0].server);
            })
    });

    it ("pipes actions", function ()
    {
        return actionService.execute([
            {
                action: "test",
                id: 1
            },
            { action: "foo"},
            {
                action: "test",
                id: 2
            }
        ], { calls: []})
            .then(function(data)
            {
                assert(data.calls.length === 3);
                assert(data.calls[0].model.id === 1);
                assert(data.calls[1].server);
                assert(data.calls[2].model.id === 2);
            })
    });


});
