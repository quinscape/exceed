const assert = require("power-assert");
const Promise = require("es6-promise-polyfill").Promise;
const sinon = require("sinon");
const proxyquire = require("proxyquire");

const sys = require("../../../../src/main/js/sys");
const keys = require("../../../../src/main/js/util/keys");
sys.init("/test-context", "TestApp");

let data;

function initData()
{
    data = {
        calls: []
    }
}

const actionService = proxyquire
    .noCallThru() // XXX: Why does this work?
    .load("../../../../src/main/js/service/action", {
        "./ajax": function (opts) {
            //console.log("AJAX OPTS", opts);

            if (opts.url === "/test-context/action/TestApp")
            {
                return Promise.resolve({actionNames: ["foo", "bar"]});
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

    before(actionService.reset);


	it("registers bulk required client actions", function()
	{
        initData();

        const testAction = function (fail) {
            if (fail)
            {
                throw new Error("TEST_FAIL");
            }
            data.calls.push({ fail });
        };

        testAction.catch = function(e, fail)
        {
            //console.error(e);

            data.calls.push({
                fail,
                error: e
            });

            return Promise.resolve();
        };

        const wrappingAction = function (model) {
            return actionService.serverAction("bar", data).catch(function (e) {
                data.calls.push({
                    model: model,
                    failed: true,
                    error: e
                });
                return Promise.reject(new Error("caught"));
            });
        };

        actionService.register("test", testAction, testAction.catch);
        actionService.register("serverWrap", wrappingAction);

        const actions = actionService.getActions();
        assert(actions.test.client === true);
        assert(actions.test.handler === testAction);

	});

	it("registers named server actions", function()
	{
		actionService.initServerActions(["foo", "bar"]);
        const actions = actionService.getActions();
        assert(actions.foo.client === false);
        assert(actions.bar.client === false);

	});


    it ("executes client actions", function ()
    {
        initData();
        return actionService.execute("test")
            .then(function()
            {
                assert(data.calls.length === 1);
                assert(data.calls[0].fail === undefined);
            })
    });

    it ("optionally handles client action failure", function ()
    {
        initData();

        return actionService.execute("test", [true])
            .then(function()
            {
                assert(data.calls.length === 1);
                assert(data.calls[0].fail);
                assert(String(data.calls[0].error).indexOf("TEST_FAIL") > 0);
            })
    });

    it ("can handle wrapped server function failure", function ()
    {
        initData();
        //console.log(actionService);
        return actionService.execute("serverWrap")
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
        return actionService.execute("foo")
            .then(function()
            {
                assert(data.calls.length === 1);
                assert(data.calls[0].server);
            })
    });
});
