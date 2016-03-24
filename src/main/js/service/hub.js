var cando = require("../cando");

var Promise = require("es6-promise-polyfill").Promise;

var security = require("./security");
var sys = require("../sys");

var Hub;

if ( process.env.NODE_ENV === "production")
{
    const ERROR = new Error("Hub inactive");
    Hub =  {
        register: function () { throw ERROR; },
        send: function () { throw ERROR; },
        request: function () { throw Promise.reject(ERROR); },
        init:function (cid) {  }
    };
}
else
{
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // REAL HUB

    var ws = null;

    var attempts = 0;
    var wasConnected = false;
    var preferFallback = false;

    var connectionId = null;

    var messageCount = 0;

    function WebSocketFallback()
    {

    }

    function startFallback()
    {
        throw new Error("Websocket fallback not implemented yet.");
    }

    function createWebSocket()
    {
        attempts++;
        if (preferFallback || !cando.webSockets)
        {
            return new WebSocketFallback();
        }
        else
        {
            var url = "ws://" + location.hostname + ":9876/appsocket";
            return new WebSocket(url);
        }
    }

    function prepare(outgoing)
    {
        var messageId = ++messageCount;

        outgoing.meta = {
            messageId: messageId,
            connectionId: connectionId,
            appName: sys.appName
        };

        return messageId;
    }

    var handlers  = {};

    var requestDeferreds = {};

    function lookupDeferred(key)
    {
        if (!key)
        {
            return null;
        }

        var d = requestDeferreds[key];
        if (d)
        {
            delete requestDeferreds[key];
        }
        return d;
    }

    function send(json)
    {
        ws.send(json);
    }

    var groupCount = 0;
    var groupStore = {};

    function GroupHub()
    {
        this.id = ++groupCount;

        groupStore[this.id] = [];
    }

    function groupMessages(id)
    {
        var messages = groupStore[id];
        if (!messages)
        {
            throw new Error("Invalid group: " + id);
        }
        return messages;
    }


    GroupHub.prototype.send = function (outgoing)
    {
        prepare(outgoing);
        groupMessages(this.id).push(outgoing);
    };

    GroupHub.prototype.request = function (outgoing)
    {
        var messageId = prepare(outgoing);
        groupMessages(this.id).push(outgoing);

        return new Promise((resolve, reject) =>
        {
            requestDeferreds[messageId] = {
                resolve: resolve,
                reject: reject
            };
        });
    };

    GroupHub.prototype.close = function ()
    {
        var messages = groupMessages(this.id);
        delete groupStore[this.id];

        var group = {
            type: "message.Group",
            messages: messages
        };

        prepare(group);
        var json = JSON.stringify(group);
        send(json);
    };


    Hub = {
        createGroup: function()
        {
            return new GroupHub();
        },
        register:
            function(type,fn)
            {
                var a = handlers[type];
                if (!a)
                {
                    handlers[type] = a = [];
                }

                a.push(fn);
            },
        send:
        // Send message to server over socket.
            function(outgoing)
            {
                var messageId = prepare(outgoing);

                console.debug("send: %o", outgoing);

                var json = JSON.stringify(outgoing);
                ws.send(json);

                return messageId;
            },
        request:
        // Send message to server over socket.
            function(outgoing)
            {
                return new Promise((resolve, reject) =>
                {
                    var messageId = this.send(outgoing);

                    requestDeferreds[messageId] = {
                        resolve: resolve,
                        reject: reject
                    };
                });
            },
        init:
            function(cid)
            {
                connectionId = cid;

                if (!security.hasRole("ROLE_EDITOR"))
                {
                    return Promise.resolve(true);
                }
                return new Promise(function (resolve, reject)
                {
                    function onMessage(e)
                    {
                        var data = JSON.parse(e.data);

                        //console.debug("RECEIVE: %o", data);

                        var a = handlers[data.type];
                        if (!a)
                        {
                            console.warn("unhandled message: ", e.data);
                        }
                        else
                        {
                            for (var i = 0; i < a.length; i++)
                            {
                                a[i].call(Hub, data);
                            }
                        }
                    }

                    ws = createWebSocket();

                    ws.onopen = function ()
                    {
                        console.info("connected");

                        wasConnected = true;
                        attempts = 0;
                        //start = new Date().getTime();
                        resolve();
                    };
                    ws.onclose = function ()
                    {
                        if (!wasConnected || attempts > 2)
                        {
                            preferFallback = true;
                            ws = createWebSocket();
                        }
                        else
                        {
                            setTimeout(function ()
                            {
                                ws = createWebSocket();
                            }, 1000);
                        }
                    };
                    ws.onerror = function(err)
                    {
                        ws.onclose();
                        reject(err);
                    };
                    ws.onmessage = onMessage;
                });
            }
    };

    Hub.register("message.Error", function(data){

        console.error("ERROR: %s", data.message);
    });

    Hub.register("message.Reply", function(reply)
    {
        //console.debug("REPLY: %o", reply);

        var deferred = lookupDeferred(reply.responseId);
        var message = reply.message;
        if (deferred)
        {
            if (reply.ok)
            {
                deferred.resolve(message);
            }
            else
            {
                deferred.reject(new Error(JSON.stringify(message)));
            }
        }
        else
        {
            console.warn("received unsolicited reply: %o", message);
        }
    });

}
module.exports = Hub;
