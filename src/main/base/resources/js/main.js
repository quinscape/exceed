(function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof require=="function"&&require;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof require=="function"&&require;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(require,module,exports){
(function (global){
"use strict";

var React = (typeof window !== "undefined" ? window['React'] : typeof global !== "undefined" ? global['React'] : null);
var ReactDOM = (typeof window !== "undefined" ? window['ReactDOM'] : typeof global !== "undefined" ? global['ReactDOM'] : null);


var componentService = require("./service/component");
var viewService = require("./service/view");
var ajax = require("./service/ajax");
var security = require("./service/security");

var ValueLink = require("./util/value-link");

var Alert = require("./ui/Alert");
var ViewComponent = require("./ui/ViewComponent");

var componentsMap = ({"components":({"counter":({"component":require("./components/counter/component.json"),"Counter":require("./components/counter/Counter.js")}),"datagrid":({"component":require("./components/datagrid/component.json"),"DataGrid":require("./components/datagrid/DataGrid.js")}),"heading":({"component":require("./components/heading/component.json"),"Heading":require("./components/heading/Heading.js")}),"input":({"component":require("./components/input/component.json"),"InputElement":require("./components/input/InputElement.js")})})});

componentService.registerBulk(componentsMap);

console.dir(componentService.getComponents());

var domready = require("domready");
window.domready = domready;
function evaluateEmbedded(elemId, mediaType) {
    var elem = document.getElementById(elemId);
    if (!elem || elem.getAttribute("type") !== mediaType) {
        throw new Error("#" + elemId + " is not a script of type '" + mediaType + "': " + elem);
    }

    return JSON.parse(elem.innerHTML);
}

var rootElem;
var currentViewName;
var currentViewData;

function timestampURL(href) {
    var marker = ";reload=" + Date.now();
    var pos = href.indexOf(";");
    if (pos < 0) {
        href += marker;
    } else {
        href = href.substr(0, pos) + marker;
    }

    return href;
}

function pollChanges() {
    ajax({
        url: contextPath + "/reload/" + appName
    }).then(function (model) {
        //alert("CHANGE" + JSON.stringify(model));

        if (model._type === "view.View") {
            var ViewComponent = viewService.getViewComponent(model.name, model, true);

            if (currentViewName === model.name) {
                ReactDOM.render(React.createElement(ViewComponent, {
                    model: model,
                    componentData: currentViewData
                }), rootElem);
            }
        } else if (model._type === "change.Shutdown") {
            alert("Server has been shut down.\nYou might want to reload");
            return;
        } else if (model._type === "change.CodeChange") {
            console.log("CodeChange");

            var elem = document.createElement("script");
            elem.setAttribute("src", contextPath + "/res/" + appName + "/js/main.js");

            var head = document.getElementsByTagName("head")[0];
            head.appendChild(elem);
        } else if (model._type === "change.StyleChange") {
            var link = document.getElementById("application-styles");
            link.href = timestampURL(link.href);

            console.log("StyleChange");
        }

        pollChanges();
    });
}
domready(function () {
    security.init();

    var contextPath = document.body.dataset.contextPath;
    var appName = document.body.dataset.appName;

    rootElem = document.getElementById("root");

    window.appName = appName;
    window.contextPath = contextPath;

    var path = location.pathname.substring(contextPath.length);

    var model = evaluateEmbedded("root-model", "x-ceed/view-model");
    var data = evaluateEmbedded("root-data", "x-ceed/view-data");

    currentViewName = model.name;
    currentViewData = data;
    ReactDOM.render(React.createElement(ViewComponent, {
        model: model,
        componentData: data
    }), rootElem);

    pollChanges();
});

}).call(this,typeof global !== "undefined" ? global : typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {})

},{"./components/counter/Counter.js":8,"./components/counter/component.json":9,"./components/datagrid/DataGrid.js":10,"./components/datagrid/component.json":11,"./components/heading/Heading.js":12,"./components/heading/component.json":13,"./components/input/InputElement.js":14,"./components/input/component.json":15,"./service/ajax":21,"./service/component":22,"./service/security":23,"./service/view":24,"./ui/Alert":25,"./ui/ViewComponent":27,"./util/value-link":32,"domready":4}],2:[function(require,module,exports){
// shim for using process in browser

var process = module.exports = {};
var queue = [];
var draining = false;

function drainQueue() {
    if (draining) {
        return;
    }
    draining = true;
    var currentQueue;
    var len = queue.length;
    while(len) {
        currentQueue = queue;
        queue = [];
        var i = -1;
        while (++i < len) {
            currentQueue[i]();
        }
        len = queue.length;
    }
    draining = false;
}
process.nextTick = function (fun) {
    queue.push(fun);
    if (!draining) {
        setTimeout(drainQueue, 0);
    }
};

process.title = 'browser';
process.browser = true;
process.env = {};
process.argv = [];
process.version = ''; // empty string to avoid regexp issues
process.versions = {};

function noop() {}

process.on = noop;
process.addListener = noop;
process.once = noop;
process.off = noop;
process.removeListener = noop;
process.removeAllListeners = noop;
process.emit = noop;

process.binding = function (name) {
    throw new Error('process.binding is not supported');
};

// TODO(shtylman)
process.cwd = function () { return '/' };
process.chdir = function (dir) {
    throw new Error('process.chdir is not supported');
};
process.umask = function() { return 0; };

},{}],3:[function(require,module,exports){
/*!
  Copyright (c) 2015 Jed Watson.
  Licensed under the MIT License (MIT), see
  http://jedwatson.github.io/classnames
*/
/* global define */

(function () {
	'use strict';

	var hasOwn = {}.hasOwnProperty;

	function classNames () {
		var classes = '';

		for (var i = 0; i < arguments.length; i++) {
			var arg = arguments[i];
			if (!arg) continue;

			var argType = typeof arg;

			if (argType === 'string' || argType === 'number') {
				classes += ' ' + arg;
			} else if (Array.isArray(arg)) {
				classes += ' ' + classNames.apply(null, arg);
			} else if (argType === 'object') {
				for (var key in arg) {
					if (hasOwn.call(arg, key) && arg[key]) {
						classes += ' ' + key;
					}
				}
			}
		}

		return classes.substr(1);
	}

	if (typeof module !== 'undefined' && module.exports) {
		module.exports = classNames;
	} else if (typeof define === 'function' && typeof define.amd === 'object' && define.amd) {
		// register as 'classnames', consistent with npm package name
		define('classnames', function () {
			return classNames;
		});
	} else {
		window.classNames = classNames;
	}
}());

},{}],4:[function(require,module,exports){
/*!
  * domready (c) Dustin Diaz 2014 - License MIT
  */
!function (name, definition) {

  if (typeof module != 'undefined') module.exports = definition()
  else if (typeof define == 'function' && typeof define.amd == 'object') define(definition)
  else this[name] = definition()

}('domready', function () {

  var fns = [], listener
    , doc = document
    , hack = doc.documentElement.doScroll
    , domContentLoaded = 'DOMContentLoaded'
    , loaded = (hack ? /^loaded|^c/ : /^loaded|^i|^c/).test(doc.readyState)


  if (!loaded)
  doc.addEventListener(domContentLoaded, listener = function () {
    doc.removeEventListener(domContentLoaded, listener)
    loaded = 1
    while (listener = fns.shift()) listener()
  })

  return function (fn) {
    loaded ? setTimeout(fn, 0) : fns.push(fn)
  }

});

},{}],5:[function(require,module,exports){
(function (process,global){
/*!
 * @overview es6-promise - a tiny implementation of Promises/A+.
 * @copyright Copyright (c) 2014 Yehuda Katz, Tom Dale, Stefan Penner and contributors (Conversion to ES6 API by Jake Archibald)
 * @license   Licensed under MIT license
 *            See https://raw.githubusercontent.com/jakearchibald/es6-promise/master/LICENSE
 * @version   2.3.0
 */

(function() {
    "use strict";
    function lib$es6$promise$utils$$objectOrFunction(x) {
      return typeof x === 'function' || (typeof x === 'object' && x !== null);
    }

    function lib$es6$promise$utils$$isFunction(x) {
      return typeof x === 'function';
    }

    function lib$es6$promise$utils$$isMaybeThenable(x) {
      return typeof x === 'object' && x !== null;
    }

    var lib$es6$promise$utils$$_isArray;
    if (!Array.isArray) {
      lib$es6$promise$utils$$_isArray = function (x) {
        return Object.prototype.toString.call(x) === '[object Array]';
      };
    } else {
      lib$es6$promise$utils$$_isArray = Array.isArray;
    }

    var lib$es6$promise$utils$$isArray = lib$es6$promise$utils$$_isArray;
    var lib$es6$promise$asap$$len = 0;
    var lib$es6$promise$asap$$toString = {}.toString;
    var lib$es6$promise$asap$$vertxNext;
    var lib$es6$promise$asap$$customSchedulerFn;

    var lib$es6$promise$asap$$asap = function asap(callback, arg) {
      lib$es6$promise$asap$$queue[lib$es6$promise$asap$$len] = callback;
      lib$es6$promise$asap$$queue[lib$es6$promise$asap$$len + 1] = arg;
      lib$es6$promise$asap$$len += 2;
      if (lib$es6$promise$asap$$len === 2) {
        // If len is 2, that means that we need to schedule an async flush.
        // If additional callbacks are queued before the queue is flushed, they
        // will be processed by this flush that we are scheduling.
        if (lib$es6$promise$asap$$customSchedulerFn) {
          lib$es6$promise$asap$$customSchedulerFn(lib$es6$promise$asap$$flush);
        } else {
          lib$es6$promise$asap$$scheduleFlush();
        }
      }
    }

    function lib$es6$promise$asap$$setScheduler(scheduleFn) {
      lib$es6$promise$asap$$customSchedulerFn = scheduleFn;
    }

    function lib$es6$promise$asap$$setAsap(asapFn) {
      lib$es6$promise$asap$$asap = asapFn;
    }

    var lib$es6$promise$asap$$browserWindow = (typeof window !== 'undefined') ? window : undefined;
    var lib$es6$promise$asap$$browserGlobal = lib$es6$promise$asap$$browserWindow || {};
    var lib$es6$promise$asap$$BrowserMutationObserver = lib$es6$promise$asap$$browserGlobal.MutationObserver || lib$es6$promise$asap$$browserGlobal.WebKitMutationObserver;
    var lib$es6$promise$asap$$isNode = typeof process !== 'undefined' && {}.toString.call(process) === '[object process]';

    // test for web worker but not in IE10
    var lib$es6$promise$asap$$isWorker = typeof Uint8ClampedArray !== 'undefined' &&
      typeof importScripts !== 'undefined' &&
      typeof MessageChannel !== 'undefined';

    // node
    function lib$es6$promise$asap$$useNextTick() {
      var nextTick = process.nextTick;
      // node version 0.10.x displays a deprecation warning when nextTick is used recursively
      // setImmediate should be used instead instead
      var version = process.versions.node.match(/^(?:(\d+)\.)?(?:(\d+)\.)?(\*|\d+)$/);
      if (Array.isArray(version) && version[1] === '0' && version[2] === '10') {
        nextTick = setImmediate;
      }
      return function() {
        nextTick(lib$es6$promise$asap$$flush);
      };
    }

    // vertx
    function lib$es6$promise$asap$$useVertxTimer() {
      return function() {
        lib$es6$promise$asap$$vertxNext(lib$es6$promise$asap$$flush);
      };
    }

    function lib$es6$promise$asap$$useMutationObserver() {
      var iterations = 0;
      var observer = new lib$es6$promise$asap$$BrowserMutationObserver(lib$es6$promise$asap$$flush);
      var node = document.createTextNode('');
      observer.observe(node, { characterData: true });

      return function() {
        node.data = (iterations = ++iterations % 2);
      };
    }

    // web worker
    function lib$es6$promise$asap$$useMessageChannel() {
      var channel = new MessageChannel();
      channel.port1.onmessage = lib$es6$promise$asap$$flush;
      return function () {
        channel.port2.postMessage(0);
      };
    }

    function lib$es6$promise$asap$$useSetTimeout() {
      return function() {
        setTimeout(lib$es6$promise$asap$$flush, 1);
      };
    }

    var lib$es6$promise$asap$$queue = new Array(1000);
    function lib$es6$promise$asap$$flush() {
      for (var i = 0; i < lib$es6$promise$asap$$len; i+=2) {
        var callback = lib$es6$promise$asap$$queue[i];
        var arg = lib$es6$promise$asap$$queue[i+1];

        callback(arg);

        lib$es6$promise$asap$$queue[i] = undefined;
        lib$es6$promise$asap$$queue[i+1] = undefined;
      }

      lib$es6$promise$asap$$len = 0;
    }

    function lib$es6$promise$asap$$attemptVertex() {
      try {
        var r = require;
        var vertx = r('vertx');
        lib$es6$promise$asap$$vertxNext = vertx.runOnLoop || vertx.runOnContext;
        return lib$es6$promise$asap$$useVertxTimer();
      } catch(e) {
        return lib$es6$promise$asap$$useSetTimeout();
      }
    }

    var lib$es6$promise$asap$$scheduleFlush;
    // Decide what async method to use to triggering processing of queued callbacks:
    if (lib$es6$promise$asap$$isNode) {
      lib$es6$promise$asap$$scheduleFlush = lib$es6$promise$asap$$useNextTick();
    } else if (lib$es6$promise$asap$$BrowserMutationObserver) {
      lib$es6$promise$asap$$scheduleFlush = lib$es6$promise$asap$$useMutationObserver();
    } else if (lib$es6$promise$asap$$isWorker) {
      lib$es6$promise$asap$$scheduleFlush = lib$es6$promise$asap$$useMessageChannel();
    } else if (lib$es6$promise$asap$$browserWindow === undefined && typeof require === 'function') {
      lib$es6$promise$asap$$scheduleFlush = lib$es6$promise$asap$$attemptVertex();
    } else {
      lib$es6$promise$asap$$scheduleFlush = lib$es6$promise$asap$$useSetTimeout();
    }

    function lib$es6$promise$$internal$$noop() {}

    var lib$es6$promise$$internal$$PENDING   = void 0;
    var lib$es6$promise$$internal$$FULFILLED = 1;
    var lib$es6$promise$$internal$$REJECTED  = 2;

    var lib$es6$promise$$internal$$GET_THEN_ERROR = new lib$es6$promise$$internal$$ErrorObject();

    function lib$es6$promise$$internal$$selfFullfillment() {
      return new TypeError("You cannot resolve a promise with itself");
    }

    function lib$es6$promise$$internal$$cannotReturnOwn() {
      return new TypeError('A promises callback cannot return that same promise.');
    }

    function lib$es6$promise$$internal$$getThen(promise) {
      try {
        return promise.then;
      } catch(error) {
        lib$es6$promise$$internal$$GET_THEN_ERROR.error = error;
        return lib$es6$promise$$internal$$GET_THEN_ERROR;
      }
    }

    function lib$es6$promise$$internal$$tryThen(then, value, fulfillmentHandler, rejectionHandler) {
      try {
        then.call(value, fulfillmentHandler, rejectionHandler);
      } catch(e) {
        return e;
      }
    }

    function lib$es6$promise$$internal$$handleForeignThenable(promise, thenable, then) {
       lib$es6$promise$asap$$asap(function(promise) {
        var sealed = false;
        var error = lib$es6$promise$$internal$$tryThen(then, thenable, function(value) {
          if (sealed) { return; }
          sealed = true;
          if (thenable !== value) {
            lib$es6$promise$$internal$$resolve(promise, value);
          } else {
            lib$es6$promise$$internal$$fulfill(promise, value);
          }
        }, function(reason) {
          if (sealed) { return; }
          sealed = true;

          lib$es6$promise$$internal$$reject(promise, reason);
        }, 'Settle: ' + (promise._label || ' unknown promise'));

        if (!sealed && error) {
          sealed = true;
          lib$es6$promise$$internal$$reject(promise, error);
        }
      }, promise);
    }

    function lib$es6$promise$$internal$$handleOwnThenable(promise, thenable) {
      if (thenable._state === lib$es6$promise$$internal$$FULFILLED) {
        lib$es6$promise$$internal$$fulfill(promise, thenable._result);
      } else if (thenable._state === lib$es6$promise$$internal$$REJECTED) {
        lib$es6$promise$$internal$$reject(promise, thenable._result);
      } else {
        lib$es6$promise$$internal$$subscribe(thenable, undefined, function(value) {
          lib$es6$promise$$internal$$resolve(promise, value);
        }, function(reason) {
          lib$es6$promise$$internal$$reject(promise, reason);
        });
      }
    }

    function lib$es6$promise$$internal$$handleMaybeThenable(promise, maybeThenable) {
      if (maybeThenable.constructor === promise.constructor) {
        lib$es6$promise$$internal$$handleOwnThenable(promise, maybeThenable);
      } else {
        var then = lib$es6$promise$$internal$$getThen(maybeThenable);

        if (then === lib$es6$promise$$internal$$GET_THEN_ERROR) {
          lib$es6$promise$$internal$$reject(promise, lib$es6$promise$$internal$$GET_THEN_ERROR.error);
        } else if (then === undefined) {
          lib$es6$promise$$internal$$fulfill(promise, maybeThenable);
        } else if (lib$es6$promise$utils$$isFunction(then)) {
          lib$es6$promise$$internal$$handleForeignThenable(promise, maybeThenable, then);
        } else {
          lib$es6$promise$$internal$$fulfill(promise, maybeThenable);
        }
      }
    }

    function lib$es6$promise$$internal$$resolve(promise, value) {
      if (promise === value) {
        lib$es6$promise$$internal$$reject(promise, lib$es6$promise$$internal$$selfFullfillment());
      } else if (lib$es6$promise$utils$$objectOrFunction(value)) {
        lib$es6$promise$$internal$$handleMaybeThenable(promise, value);
      } else {
        lib$es6$promise$$internal$$fulfill(promise, value);
      }
    }

    function lib$es6$promise$$internal$$publishRejection(promise) {
      if (promise._onerror) {
        promise._onerror(promise._result);
      }

      lib$es6$promise$$internal$$publish(promise);
    }

    function lib$es6$promise$$internal$$fulfill(promise, value) {
      if (promise._state !== lib$es6$promise$$internal$$PENDING) { return; }

      promise._result = value;
      promise._state = lib$es6$promise$$internal$$FULFILLED;

      if (promise._subscribers.length !== 0) {
        lib$es6$promise$asap$$asap(lib$es6$promise$$internal$$publish, promise);
      }
    }

    function lib$es6$promise$$internal$$reject(promise, reason) {
      if (promise._state !== lib$es6$promise$$internal$$PENDING) { return; }
      promise._state = lib$es6$promise$$internal$$REJECTED;
      promise._result = reason;

      lib$es6$promise$asap$$asap(lib$es6$promise$$internal$$publishRejection, promise);
    }

    function lib$es6$promise$$internal$$subscribe(parent, child, onFulfillment, onRejection) {
      var subscribers = parent._subscribers;
      var length = subscribers.length;

      parent._onerror = null;

      subscribers[length] = child;
      subscribers[length + lib$es6$promise$$internal$$FULFILLED] = onFulfillment;
      subscribers[length + lib$es6$promise$$internal$$REJECTED]  = onRejection;

      if (length === 0 && parent._state) {
        lib$es6$promise$asap$$asap(lib$es6$promise$$internal$$publish, parent);
      }
    }

    function lib$es6$promise$$internal$$publish(promise) {
      var subscribers = promise._subscribers;
      var settled = promise._state;

      if (subscribers.length === 0) { return; }

      var child, callback, detail = promise._result;

      for (var i = 0; i < subscribers.length; i += 3) {
        child = subscribers[i];
        callback = subscribers[i + settled];

        if (child) {
          lib$es6$promise$$internal$$invokeCallback(settled, child, callback, detail);
        } else {
          callback(detail);
        }
      }

      promise._subscribers.length = 0;
    }

    function lib$es6$promise$$internal$$ErrorObject() {
      this.error = null;
    }

    var lib$es6$promise$$internal$$TRY_CATCH_ERROR = new lib$es6$promise$$internal$$ErrorObject();

    function lib$es6$promise$$internal$$tryCatch(callback, detail) {
      try {
        return callback(detail);
      } catch(e) {
        lib$es6$promise$$internal$$TRY_CATCH_ERROR.error = e;
        return lib$es6$promise$$internal$$TRY_CATCH_ERROR;
      }
    }

    function lib$es6$promise$$internal$$invokeCallback(settled, promise, callback, detail) {
      var hasCallback = lib$es6$promise$utils$$isFunction(callback),
          value, error, succeeded, failed;

      if (hasCallback) {
        value = lib$es6$promise$$internal$$tryCatch(callback, detail);

        if (value === lib$es6$promise$$internal$$TRY_CATCH_ERROR) {
          failed = true;
          error = value.error;
          value = null;
        } else {
          succeeded = true;
        }

        if (promise === value) {
          lib$es6$promise$$internal$$reject(promise, lib$es6$promise$$internal$$cannotReturnOwn());
          return;
        }

      } else {
        value = detail;
        succeeded = true;
      }

      if (promise._state !== lib$es6$promise$$internal$$PENDING) {
        // noop
      } else if (hasCallback && succeeded) {
        lib$es6$promise$$internal$$resolve(promise, value);
      } else if (failed) {
        lib$es6$promise$$internal$$reject(promise, error);
      } else if (settled === lib$es6$promise$$internal$$FULFILLED) {
        lib$es6$promise$$internal$$fulfill(promise, value);
      } else if (settled === lib$es6$promise$$internal$$REJECTED) {
        lib$es6$promise$$internal$$reject(promise, value);
      }
    }

    function lib$es6$promise$$internal$$initializePromise(promise, resolver) {
      try {
        resolver(function resolvePromise(value){
          lib$es6$promise$$internal$$resolve(promise, value);
        }, function rejectPromise(reason) {
          lib$es6$promise$$internal$$reject(promise, reason);
        });
      } catch(e) {
        lib$es6$promise$$internal$$reject(promise, e);
      }
    }

    function lib$es6$promise$enumerator$$Enumerator(Constructor, input) {
      var enumerator = this;

      enumerator._instanceConstructor = Constructor;
      enumerator.promise = new Constructor(lib$es6$promise$$internal$$noop);

      if (enumerator._validateInput(input)) {
        enumerator._input     = input;
        enumerator.length     = input.length;
        enumerator._remaining = input.length;

        enumerator._init();

        if (enumerator.length === 0) {
          lib$es6$promise$$internal$$fulfill(enumerator.promise, enumerator._result);
        } else {
          enumerator.length = enumerator.length || 0;
          enumerator._enumerate();
          if (enumerator._remaining === 0) {
            lib$es6$promise$$internal$$fulfill(enumerator.promise, enumerator._result);
          }
        }
      } else {
        lib$es6$promise$$internal$$reject(enumerator.promise, enumerator._validationError());
      }
    }

    lib$es6$promise$enumerator$$Enumerator.prototype._validateInput = function(input) {
      return lib$es6$promise$utils$$isArray(input);
    };

    lib$es6$promise$enumerator$$Enumerator.prototype._validationError = function() {
      return new Error('Array Methods must be provided an Array');
    };

    lib$es6$promise$enumerator$$Enumerator.prototype._init = function() {
      this._result = new Array(this.length);
    };

    var lib$es6$promise$enumerator$$default = lib$es6$promise$enumerator$$Enumerator;

    lib$es6$promise$enumerator$$Enumerator.prototype._enumerate = function() {
      var enumerator = this;

      var length  = enumerator.length;
      var promise = enumerator.promise;
      var input   = enumerator._input;

      for (var i = 0; promise._state === lib$es6$promise$$internal$$PENDING && i < length; i++) {
        enumerator._eachEntry(input[i], i);
      }
    };

    lib$es6$promise$enumerator$$Enumerator.prototype._eachEntry = function(entry, i) {
      var enumerator = this;
      var c = enumerator._instanceConstructor;

      if (lib$es6$promise$utils$$isMaybeThenable(entry)) {
        if (entry.constructor === c && entry._state !== lib$es6$promise$$internal$$PENDING) {
          entry._onerror = null;
          enumerator._settledAt(entry._state, i, entry._result);
        } else {
          enumerator._willSettleAt(c.resolve(entry), i);
        }
      } else {
        enumerator._remaining--;
        enumerator._result[i] = entry;
      }
    };

    lib$es6$promise$enumerator$$Enumerator.prototype._settledAt = function(state, i, value) {
      var enumerator = this;
      var promise = enumerator.promise;

      if (promise._state === lib$es6$promise$$internal$$PENDING) {
        enumerator._remaining--;

        if (state === lib$es6$promise$$internal$$REJECTED) {
          lib$es6$promise$$internal$$reject(promise, value);
        } else {
          enumerator._result[i] = value;
        }
      }

      if (enumerator._remaining === 0) {
        lib$es6$promise$$internal$$fulfill(promise, enumerator._result);
      }
    };

    lib$es6$promise$enumerator$$Enumerator.prototype._willSettleAt = function(promise, i) {
      var enumerator = this;

      lib$es6$promise$$internal$$subscribe(promise, undefined, function(value) {
        enumerator._settledAt(lib$es6$promise$$internal$$FULFILLED, i, value);
      }, function(reason) {
        enumerator._settledAt(lib$es6$promise$$internal$$REJECTED, i, reason);
      });
    };
    function lib$es6$promise$promise$all$$all(entries) {
      return new lib$es6$promise$enumerator$$default(this, entries).promise;
    }
    var lib$es6$promise$promise$all$$default = lib$es6$promise$promise$all$$all;
    function lib$es6$promise$promise$race$$race(entries) {
      /*jshint validthis:true */
      var Constructor = this;

      var promise = new Constructor(lib$es6$promise$$internal$$noop);

      if (!lib$es6$promise$utils$$isArray(entries)) {
        lib$es6$promise$$internal$$reject(promise, new TypeError('You must pass an array to race.'));
        return promise;
      }

      var length = entries.length;

      function onFulfillment(value) {
        lib$es6$promise$$internal$$resolve(promise, value);
      }

      function onRejection(reason) {
        lib$es6$promise$$internal$$reject(promise, reason);
      }

      for (var i = 0; promise._state === lib$es6$promise$$internal$$PENDING && i < length; i++) {
        lib$es6$promise$$internal$$subscribe(Constructor.resolve(entries[i]), undefined, onFulfillment, onRejection);
      }

      return promise;
    }
    var lib$es6$promise$promise$race$$default = lib$es6$promise$promise$race$$race;
    function lib$es6$promise$promise$resolve$$resolve(object) {
      /*jshint validthis:true */
      var Constructor = this;

      if (object && typeof object === 'object' && object.constructor === Constructor) {
        return object;
      }

      var promise = new Constructor(lib$es6$promise$$internal$$noop);
      lib$es6$promise$$internal$$resolve(promise, object);
      return promise;
    }
    var lib$es6$promise$promise$resolve$$default = lib$es6$promise$promise$resolve$$resolve;
    function lib$es6$promise$promise$reject$$reject(reason) {
      /*jshint validthis:true */
      var Constructor = this;
      var promise = new Constructor(lib$es6$promise$$internal$$noop);
      lib$es6$promise$$internal$$reject(promise, reason);
      return promise;
    }
    var lib$es6$promise$promise$reject$$default = lib$es6$promise$promise$reject$$reject;

    var lib$es6$promise$promise$$counter = 0;

    function lib$es6$promise$promise$$needsResolver() {
      throw new TypeError('You must pass a resolver function as the first argument to the promise constructor');
    }

    function lib$es6$promise$promise$$needsNew() {
      throw new TypeError("Failed to construct 'Promise': Please use the 'new' operator, this object constructor cannot be called as a function.");
    }

    var lib$es6$promise$promise$$default = lib$es6$promise$promise$$Promise;
    /**
      Promise objects represent the eventual result of an asynchronous operation. The
      primary way of interacting with a promise is through its `then` method, which
      registers callbacks to receive either a promise's eventual value or the reason
      why the promise cannot be fulfilled.

      Terminology
      -----------

      - `promise` is an object or function with a `then` method whose behavior conforms to this specification.
      - `thenable` is an object or function that defines a `then` method.
      - `value` is any legal JavaScript value (including undefined, a thenable, or a promise).
      - `exception` is a value that is thrown using the throw statement.
      - `reason` is a value that indicates why a promise was rejected.
      - `settled` the final resting state of a promise, fulfilled or rejected.

      A promise can be in one of three states: pending, fulfilled, or rejected.

      Promises that are fulfilled have a fulfillment value and are in the fulfilled
      state.  Promises that are rejected have a rejection reason and are in the
      rejected state.  A fulfillment value is never a thenable.

      Promises can also be said to *resolve* a value.  If this value is also a
      promise, then the original promise's settled state will match the value's
      settled state.  So a promise that *resolves* a promise that rejects will
      itself reject, and a promise that *resolves* a promise that fulfills will
      itself fulfill.


      Basic Usage:
      ------------

      ```js
      var promise = new Promise(function(resolve, reject) {
        // on success
        resolve(value);

        // on failure
        reject(reason);
      });

      promise.then(function(value) {
        // on fulfillment
      }, function(reason) {
        // on rejection
      });
      ```

      Advanced Usage:
      ---------------

      Promises shine when abstracting away asynchronous interactions such as
      `XMLHttpRequest`s.

      ```js
      function getJSON(url) {
        return new Promise(function(resolve, reject){
          var xhr = new XMLHttpRequest();

          xhr.open('GET', url);
          xhr.onreadystatechange = handler;
          xhr.responseType = 'json';
          xhr.setRequestHeader('Accept', 'application/json');
          xhr.send();

          function handler() {
            if (this.readyState === this.DONE) {
              if (this.status === 200) {
                resolve(this.response);
              } else {
                reject(new Error('getJSON: `' + url + '` failed with status: [' + this.status + ']'));
              }
            }
          };
        });
      }

      getJSON('/posts.json').then(function(json) {
        // on fulfillment
      }, function(reason) {
        // on rejection
      });
      ```

      Unlike callbacks, promises are great composable primitives.

      ```js
      Promise.all([
        getJSON('/posts'),
        getJSON('/comments')
      ]).then(function(values){
        values[0] // => postsJSON
        values[1] // => commentsJSON

        return values;
      });
      ```

      @class Promise
      @param {function} resolver
      Useful for tooling.
      @constructor
    */
    function lib$es6$promise$promise$$Promise(resolver) {
      this._id = lib$es6$promise$promise$$counter++;
      this._state = undefined;
      this._result = undefined;
      this._subscribers = [];

      if (lib$es6$promise$$internal$$noop !== resolver) {
        if (!lib$es6$promise$utils$$isFunction(resolver)) {
          lib$es6$promise$promise$$needsResolver();
        }

        if (!(this instanceof lib$es6$promise$promise$$Promise)) {
          lib$es6$promise$promise$$needsNew();
        }

        lib$es6$promise$$internal$$initializePromise(this, resolver);
      }
    }

    lib$es6$promise$promise$$Promise.all = lib$es6$promise$promise$all$$default;
    lib$es6$promise$promise$$Promise.race = lib$es6$promise$promise$race$$default;
    lib$es6$promise$promise$$Promise.resolve = lib$es6$promise$promise$resolve$$default;
    lib$es6$promise$promise$$Promise.reject = lib$es6$promise$promise$reject$$default;
    lib$es6$promise$promise$$Promise._setScheduler = lib$es6$promise$asap$$setScheduler;
    lib$es6$promise$promise$$Promise._setAsap = lib$es6$promise$asap$$setAsap;
    lib$es6$promise$promise$$Promise._asap = lib$es6$promise$asap$$asap;

    lib$es6$promise$promise$$Promise.prototype = {
      constructor: lib$es6$promise$promise$$Promise,

    /**
      The primary way of interacting with a promise is through its `then` method,
      which registers callbacks to receive either a promise's eventual value or the
      reason why the promise cannot be fulfilled.

      ```js
      findUser().then(function(user){
        // user is available
      }, function(reason){
        // user is unavailable, and you are given the reason why
      });
      ```

      Chaining
      --------

      The return value of `then` is itself a promise.  This second, 'downstream'
      promise is resolved with the return value of the first promise's fulfillment
      or rejection handler, or rejected if the handler throws an exception.

      ```js
      findUser().then(function (user) {
        return user.name;
      }, function (reason) {
        return 'default name';
      }).then(function (userName) {
        // If `findUser` fulfilled, `userName` will be the user's name, otherwise it
        // will be `'default name'`
      });

      findUser().then(function (user) {
        throw new Error('Found user, but still unhappy');
      }, function (reason) {
        throw new Error('`findUser` rejected and we're unhappy');
      }).then(function (value) {
        // never reached
      }, function (reason) {
        // if `findUser` fulfilled, `reason` will be 'Found user, but still unhappy'.
        // If `findUser` rejected, `reason` will be '`findUser` rejected and we're unhappy'.
      });
      ```
      If the downstream promise does not specify a rejection handler, rejection reasons will be propagated further downstream.

      ```js
      findUser().then(function (user) {
        throw new PedagogicalException('Upstream error');
      }).then(function (value) {
        // never reached
      }).then(function (value) {
        // never reached
      }, function (reason) {
        // The `PedgagocialException` is propagated all the way down to here
      });
      ```

      Assimilation
      ------------

      Sometimes the value you want to propagate to a downstream promise can only be
      retrieved asynchronously. This can be achieved by returning a promise in the
      fulfillment or rejection handler. The downstream promise will then be pending
      until the returned promise is settled. This is called *assimilation*.

      ```js
      findUser().then(function (user) {
        return findCommentsByAuthor(user);
      }).then(function (comments) {
        // The user's comments are now available
      });
      ```

      If the assimliated promise rejects, then the downstream promise will also reject.

      ```js
      findUser().then(function (user) {
        return findCommentsByAuthor(user);
      }).then(function (comments) {
        // If `findCommentsByAuthor` fulfills, we'll have the value here
      }, function (reason) {
        // If `findCommentsByAuthor` rejects, we'll have the reason here
      });
      ```

      Simple Example
      --------------

      Synchronous Example

      ```javascript
      var result;

      try {
        result = findResult();
        // success
      } catch(reason) {
        // failure
      }
      ```

      Errback Example

      ```js
      findResult(function(result, err){
        if (err) {
          // failure
        } else {
          // success
        }
      });
      ```

      Promise Example;

      ```javascript
      findResult().then(function(result){
        // success
      }, function(reason){
        // failure
      });
      ```

      Advanced Example
      --------------

      Synchronous Example

      ```javascript
      var author, books;

      try {
        author = findAuthor();
        books  = findBooksByAuthor(author);
        // success
      } catch(reason) {
        // failure
      }
      ```

      Errback Example

      ```js

      function foundBooks(books) {

      }

      function failure(reason) {

      }

      findAuthor(function(author, err){
        if (err) {
          failure(err);
          // failure
        } else {
          try {
            findBoooksByAuthor(author, function(books, err) {
              if (err) {
                failure(err);
              } else {
                try {
                  foundBooks(books);
                } catch(reason) {
                  failure(reason);
                }
              }
            });
          } catch(error) {
            failure(err);
          }
          // success
        }
      });
      ```

      Promise Example;

      ```javascript
      findAuthor().
        then(findBooksByAuthor).
        then(function(books){
          // found books
      }).catch(function(reason){
        // something went wrong
      });
      ```

      @method then
      @param {Function} onFulfilled
      @param {Function} onRejected
      Useful for tooling.
      @return {Promise}
    */
      then: function(onFulfillment, onRejection) {
        var parent = this;
        var state = parent._state;

        if (state === lib$es6$promise$$internal$$FULFILLED && !onFulfillment || state === lib$es6$promise$$internal$$REJECTED && !onRejection) {
          return this;
        }

        var child = new this.constructor(lib$es6$promise$$internal$$noop);
        var result = parent._result;

        if (state) {
          var callback = arguments[state - 1];
          lib$es6$promise$asap$$asap(function(){
            lib$es6$promise$$internal$$invokeCallback(state, child, callback, result);
          });
        } else {
          lib$es6$promise$$internal$$subscribe(parent, child, onFulfillment, onRejection);
        }

        return child;
      },

    /**
      `catch` is simply sugar for `then(undefined, onRejection)` which makes it the same
      as the catch block of a try/catch statement.

      ```js
      function findAuthor(){
        throw new Error('couldn't find that author');
      }

      // synchronous
      try {
        findAuthor();
      } catch(reason) {
        // something went wrong
      }

      // async with promises
      findAuthor().catch(function(reason){
        // something went wrong
      });
      ```

      @method catch
      @param {Function} onRejection
      Useful for tooling.
      @return {Promise}
    */
      'catch': function(onRejection) {
        return this.then(null, onRejection);
      }
    };
    function lib$es6$promise$polyfill$$polyfill() {
      var local;

      if (typeof global !== 'undefined') {
          local = global;
      } else if (typeof self !== 'undefined') {
          local = self;
      } else {
          try {
              local = Function('return this')();
          } catch (e) {
              throw new Error('polyfill failed because global object is unavailable in this environment');
          }
      }

      var P = local.Promise;

      if (P && Object.prototype.toString.call(P.resolve()) === '[object Promise]' && !P.cast) {
        return;
      }

      local.Promise = lib$es6$promise$promise$$default;
    }
    var lib$es6$promise$polyfill$$default = lib$es6$promise$polyfill$$polyfill;

    var lib$es6$promise$umd$$ES6Promise = {
      'Promise': lib$es6$promise$promise$$default,
      'polyfill': lib$es6$promise$polyfill$$default
    };

    /* global define:true module:true window: true */
    if (typeof define === 'function' && define['amd']) {
      define(function() { return lib$es6$promise$umd$$ES6Promise; });
    } else if (typeof module !== 'undefined' && module['exports']) {
      module['exports'] = lib$es6$promise$umd$$ES6Promise;
    } else if (typeof this !== 'undefined') {
      this['ES6Promise'] = lib$es6$promise$umd$$ES6Promise;
    }

    lib$es6$promise$polyfill$$default();
}).call(this);


}).call(this,require('_process'),typeof global !== "undefined" ? global : typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {})

},{"_process":2}],6:[function(require,module,exports){
'use strict';

var hasOwn = Object.prototype.hasOwnProperty;
var toStr = Object.prototype.toString;

var isArray = function isArray(arr) {
	if (typeof Array.isArray === 'function') {
		return Array.isArray(arr);
	}

	return toStr.call(arr) === '[object Array]';
};

var isPlainObject = function isPlainObject(obj) {
	if (!obj || toStr.call(obj) !== '[object Object]') {
		return false;
	}

	var hasOwnConstructor = hasOwn.call(obj, 'constructor');
	var hasIsPrototypeOf = obj.constructor && obj.constructor.prototype && hasOwn.call(obj.constructor.prototype, 'isPrototypeOf');
	// Not own constructor property must be Object
	if (obj.constructor && !hasOwnConstructor && !hasIsPrototypeOf) {
		return false;
	}

	// Own properties are enumerated firstly, so to speed up,
	// if last one is own, then all properties are own.
	var key;
	for (key in obj) {/**/}

	return typeof key === 'undefined' || hasOwn.call(obj, key);
};

module.exports = function extend() {
	var options, name, src, copy, copyIsArray, clone,
		target = arguments[0],
		i = 1,
		length = arguments.length,
		deep = false;

	// Handle a deep copy situation
	if (typeof target === 'boolean') {
		deep = target;
		target = arguments[1] || {};
		// skip the boolean and the target
		i = 2;
	} else if ((typeof target !== 'object' && typeof target !== 'function') || target == null) {
		target = {};
	}

	for (; i < length; ++i) {
		options = arguments[i];
		// Only deal with non-null/undefined values
		if (options != null) {
			// Extend the base object
			for (name in options) {
				src = target[name];
				copy = options[name];

				// Prevent never-ending loop
				if (target !== copy) {
					// Recurse if we're merging plain objects or arrays
					if (deep && copy && (isPlainObject(copy) || (copyIsArray = isArray(copy)))) {
						if (copyIsArray) {
							copyIsArray = false;
							clone = src && isArray(src) ? src : [];
						} else {
							clone = src && isPlainObject(src) ? src : {};
						}

						// Never move original objects, clone them
						target[name] = extend(deep, clone, copy);

					// Don't bring in undefined values
					} else if (typeof copy !== 'undefined') {
						target[name] = copy;
					}
				}
			}
		}
	}

	// Return the modified object
	return target;
};


},{}],7:[function(require,module,exports){
"use strict";

var createXMLHTTPObject = require("./util/xhr-factory");
var xmlParser = require("./util/parseXML");

/**
 * Runs feature detections and stores the result for future use.
 *
 * @type {{pushState: boolean, ajax: boolean, localStorage: boolean, sessionStorage: boolean, svg: boolean, parseXML: boolean }}
 */
module.exports = {
    /**
     * Browser supports history.pushState()
     */
    pushState: typeof window !== "undefined" && "pushState" in window.history && typeof window.history["pushState"] === "function",
    /**
     * Browser supports AJAX
     */
    ajax: !!createXMLHTTPObject(),

    localStorage: (function () {
        try {
            return typeof window !== "undefined" && "localStorage" in window && window["localStorage"] !== null;
        } catch (e) {
            return false;
        }
    })(),

    sessionStorage: (function () {
        try {
            return typeof window !== "undefined" && "sessionStorage" in window && window["sessionStorage"] !== null;
        } catch (e) {
            return false;
        }
    })(),

    svg: (function () {
        try {
            return typeof document !== "undefined" && !!document.createElementNS && !!document.createElementNS('http://www.w3.org/2000/svg', "svg").createSVGRect;
        } catch (e) {
            return false;
        }
    })(),

    parseXML: !!xmlParser
};

},{"./util/parseXML":30,"./util/xhr-factory":34}],8:[function(require,module,exports){
(function (global){
"use strict";

var React = (typeof window !== "undefined" ? window['React'] : typeof global !== "undefined" ? global['React'] : null);

var Counter = React.createClass({
    displayName: "Counter",

    getInitialState: function () {
        return {
            count: this.props.value
        };
    },

    onChange: function () {
        this.setState({
            count: this.state.count + 1
        });
    },

    render: function () {
        return React.createElement(
            "div",
            { className: "counter" },
            React.createElement(
                "h3",
                null,
                this.state.count
            ),
            React.createElement("input", { type: "submit", className: "btn btn-primary", value: "aaa", onClick: this.onChange })
        );
    }
});

module.exports = Counter;

}).call(this,typeof global !== "undefined" ? global : typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {})

},{}],9:[function(require,module,exports){
module.exports={
  "components": {
    "Counter": {
      "props": {
        "value" : {
          "type" : "PlainText"
        }
      }
    }
  }
}

},{}],10:[function(require,module,exports){
(function (global){
"use strict";

var React = (typeof window !== "undefined" ? window['React'] : typeof global !== "undefined" ? global['React'] : null);

var DataGrid = React.createClass({
    displayName: "DataGrid",

    render: function () {
        return React.createElement("div", null);
    }
});

var ValueLink = require("../../util/value-link");

var DebounceMixin = require("../../mixin/debounce-mixin");
var ComplexComponent = require("../../mixin/complex-component");
var StatelessComponent = require("../../mixin/stateless-component");

var PagingComponent = require("../../ui/PagingComponent");

var Enum = require("../../util/enum");

var MAX = 1 << 31;

var FilterMode = new Enum({
    CONTAINS: 1,
    EQUALS: 1,
    STARTS_WITH: 1
});

var Column = React.createClass({
    displayName: "Column",

    propTypes: {
        filterMode: React.PropTypes.oneOf(FilterMode.values()).isRequired,
        name: React.PropTypes.string.isRequired,
        value: React.PropTypes.any
    },
    getDefaultProps: function () {
        return {
            filterMode: "CONTAINS"
        };
    },
    render: function () {
        var kids = this.props.children;

        if (React.Children.count(kids) === 0) {
            return React.createElement(
                "td",
                null,
                React.createElement(
                    "p",
                    { className: "form-control-static" },
                    this.props.value
                )
            );
        }
        return React.createElement(
            "td",
            null,
            ri.cloneWithContext(kids, this.props.value)
        );
    }
});

var FilterField = React.createClass({
    displayName: "FilterField",

    mixins: [DebounceMixin],

    handleChange: function (ev) {
        this.debounce(function (value) {
            this.props.valueLink.requestChange(value);
        }, 250, ev.target.value);
    },

    render: function () {
        return React.createElement(
            "th",
            null,
            React.createElement("input", { type: "text", className: "form-control", defaultValue: this.props.valueLink.value, onChange: this.handleChange })
        );
    }
});

var Header = React.createClass({
    displayName: "Header",

    toggle: function (ev) {
        var currentSort = this.props.currentSortLink.value;

        if (currentSort === this.props.sort) {
            this.props.currentSortLink.requestChange("!" + this.props.sort);
        } else {
            this.props.currentSortLink.requestChange(this.props.sort);
        }
        ev.preventDefault();
    },

    render: function () {
        var currentSort = this.props.currentSortLink.value;

        var arrow = false;
        if (currentSort === this.props.sort) {
            arrow = React.createElement(
                "span",
                { className: "sort-indicator" },
                "\u21D3"
            );
        } else if (currentSort === "!" + this.props.sort) {
            arrow = React.createElement(
                "span",
                { className: "sort-indicator" },
                "\u21D1"
            );
        }

        return React.createElement(
            "th",
            null,
            React.createElement(
                "a",
                { className: "header", href: "#sort", onClick: this.toggle },
                i18n(ri.prop("heading"))
            ),
            arrow
        );
    }
});

var DataGrid = React.createClass({
    displayName: "DataGrid",

    //statics: {
    //    vars: {
    //        orderBy: ri.prop("orderBy"),
    //        filter: {},
    //        offset: 0,
    //        limit: ri.prop("limit")
    //    },
    //    queries: {
    //        rows: {
    //            from: ri.prop("type"),
    //            orderBy: ri.var("orderBy"),
    //            filter: ri.var("filter"),
    //            limit: ri.var("limit"),
    //            offset: ri.var("offset")
    //        },
    //        count: {
    //            from: ri.prop("type"),
    //            filter: ri.var("filter"),
    //            count: true
    //        }
    //    }
    //},

    propTypes: {
        type: React.PropTypes.string.isRequired,
        orderBy: React.PropTypes.string,
        limit: React.PropTypes.number,
        rows: React.PropTypes.object
    },

    mixins: [ComplexComponent, StatelessComponent],

    changeSort: function (newValue) {
        this.updateVars({
            orderBy: newValue
        });
    },

    setFilter: function (v) {
        var vars = this.getVars();

        this.setVars(React.addons.update(vars, {
            filter: babelHelpers.defineProperty({}, v.name, { $set: v.value }),
            // restart paging on filter change
            offset: { $set: 0 }
        }));
    },

    setPagingOffset: function (offset) {
        this.updateVars({
            offset: offset
        });
    },

    renderHeader: function () {
        var colCount = 0;

        var type = this.props.type;

        var currentSortLink = new ValueLink(ri.var("orderBy"), this.changeSort);
        return React.createElement(
            "tr",
            null,
            React.Children.map(this.props.children, function (kid) {
                if (kid.type !== Column) {
                    throw new Error("Datagrid component should only have Datagrid.Column children.");
                }
                return React.createElement(Header, {
                    key: colCount++,
                    currentSortLink: currentSortLink,
                    heading: type + "." + kid.props.name,
                    sort: kid.props.name
                });
            }, this)
        );
    },

    renderFilter: function () {
        var colCount = 0;
        var activeFilters = ri.var("filter");

        var dataGridComponent = this;

        return React.createElement(
            "tr",
            null,
            React.Children.map(this.props.children, function (kid) {
                return React.createElement(FilterField, { key: colCount++,
                    name: kid.props.name,
                    valueLink: new ValueLink(activeFilters[kid.props.name], function (value) {
                        dataGridComponent.setFilter({ name: kid.props.name, value: value });
                    })
                });
            }, this)
        );
    },

    render: function () {
        //console.log("COUNT", this.props.count);

        var colCount;
        var rowCount = 0;
        var rows = this.props.rows.map(function (row) {
            rowCount++;
            colCount = 0;

            return React.createElement(
                "tr",
                { key: rowCount },
                React.Children.map(this.props.children, function (kid) {
                    return React.cloneElement(kid, {
                        key: colCount++,
                        value: row[kid.props.name]
                    });
                }, this)
            );
        }, this);

        if (!rows.length) {
            rows = React.createElement(
                "tr",
                null,
                React.createElement(
                    "td",
                    { colSpan: React.Children.count(this.props.children) },
                    i18n("No Rows")
                )
            );
        }

        return React.createElement(
            "div",
            { className: "datagrid" },
            React.createElement(
                "table",
                { className: "table table-striped table-hover table-bordered" },
                React.createElement(
                    "thead",
                    null,
                    this.renderHeader(),
                    this.renderFilter()
                ),
                React.createElement(
                    "tbody",
                    null,
                    rows
                )
            ),
            React.createElement(PagingComponent, {
                offsetLink: new ValueLink(ri.var("offset"), this.setPagingOffset),
                limit: ri.var("limit"),
                rowCount: this.props.count
            })
        );
    }
});

DataGrid.Column = Column;

module.exports = DataGrid;

}).call(this,typeof global !== "undefined" ? global : typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {})

},{"../../mixin/complex-component":18,"../../mixin/debounce-mixin":19,"../../mixin/stateless-component":20,"../../ui/PagingComponent":26,"../../util/enum":28,"../../util/value-link":32}],11:[function(require,module,exports){
module.exports={
    "components": {
        "DataGrid": {
            "vars": {},
            "props": {}
        },
        "DataGrid.Column": {
            "vars": {},
            "props": {
                "name": {
                    "type": "ModelReference",
                    "param": "domain.DomainProperty"
                },
                "filterMode": {
                    "type": "enum",
                    "param": ""
                },
                "value": {
                    "type": "Any"
                }
            }
        }
    }
}

},{}],12:[function(require,module,exports){
(function (global){
"use strict";

var React = (typeof window !== "undefined" ? window['React'] : typeof global !== "undefined" ? global['React'] : null);

var Heading = React.createClass({
    displayName: "Heading",

    render: function () {
        return React.createElement(
            "h2",
            null,
            this.props.value
        );
    }
});

var HR = React.createClass({
    displayName: "HR",

    render: function () {
        return React.createElement("hr", null);
    }
});

Heading.HR = HR;

module.exports = Heading;

}).call(this,typeof global !== "undefined" ? global : typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {})

},{}],13:[function(require,module,exports){
module.exports={
  "components": {
    "Heading": {
      "queries" : {
        "test" : { "from": "Test" }
      },
      "props" : {
        "value" : {
          "type" : "PlainText"
        },
        "size" : {
          "type" : "Integer"
        }
      }
    },
    "Heading.HR": {
      "props" : {
        "value" : {
          "type" : "PlainText"
        }
      }
    }
  }
}

},{}],14:[function(require,module,exports){
(function (global){
"use strict";

var React = (typeof window !== "undefined" ? window['React'] : typeof global !== "undefined" ? global['React'] : null);

var InputElement = React.createClass({
    displayName: "InputElement",

    onChange: function () {
        console.log("change");
    },

    render: function () {
        return React.createElement(
            "form",
            { action: "/app/exceed/" },
            React.createElement("input", { type: "text", name: "test", defaultValue: "Change me", onChange: this.onChange })
        );
    }
});

module.exports = InputElement;

}).call(this,typeof global !== "undefined" ? global : typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {})

},{}],15:[function(require,module,exports){
module.exports={
  "components": {
    "InputElement": {
      "props": {
        "value" : {
          "type" : "PlainText"
        }
      }
    }
  }
}

},{}],16:[function(require,module,exports){
(function (global){
"use strict";

var React = (typeof window !== "undefined" ? window['React'] : typeof global !== "undefined" ? global['React'] : null);
var extend = require("extend");
var ajax = require("./service/ajax");

var uri = require("./util/uri");

var idCount = 0;

if (undefined !== "production") {
    var warnOnce = require("./util/warn-once");
}

var Alert = React.createClass({
    displayName: "Alert",

    render: function () {
        var detail;
        if (this.props.detail) {
            detail = React.createElement(
                "pre",
                { className: "text-danger" },
                this.props.detail
            );
        }

        return React.createElement(
            "div",
            { className: "alert alert-danger" },
            this.props.message,
            detail
        );
    }
});
var missingFunctionValues = {};

var compositors = [];

/**
 *
 * Creates a compositor instance tied to a mount point element in the DOM.
 *
 * @param views     static require map a la bulkify
 * @param elem      DOM element
 * @constructor
 */
function Compositor(views, elem) {

    this.views = views;
    this.elem = elem;

    if (elem.id) {
        this.id = elem.id;
    } else {
        this.id = "id" + ++idCount;
        elem.id = this.id;
    }

    compositors.push(this);
}

extend(Compositor.prototype, {

    /**
     * Creates a view compontent
     *
     * @param viewName      {string|ReactClass} view module name or component class
     * @param props         view component props
     *
     * @returns {ReactElement} view component element
     */
    createView: function (viewName, props) {
        if (!viewName) {
            return React.createElement(Alert, { message: "No viewName", detail: "Props:\n" + JSON.stringify(props, null, "  ") });
        }

        if (typeof viewName === "string") {
            var parts = viewName.split("/");

            var pointer = this.views;
            for (var i = 0; i < parts.length; i++) {
                pointer = pointer[parts[i]];
                if (!pointer) {
                    return React.createElement(Alert, { message: "React view module '" + viewName + "' not found.",
                        detail: "Props:\n" + JSON.stringify(props, null, "  ") });
                }
            }
            return React.createElement(pointer, props);
        } else {
            // non-string viewName assumed to the react component
            return React.createElement(viewName, props);
        }
    },

    /**
     * Renders a view or a component into the given DOM element.
     *
     * @param viewName              {(string|Object)?}view module name or react component
     * @param props                 {Object?} component props. If not given, an initial data lookup is performed
     * @param callback              {function?} callback to call after rendering (gets passed to React.render)
     */
    render: function (viewName, props, callback) {

        if (!props) {
            var script = document.getElementById(this.id + "-data");

            if (script && script.getAttribute("type") === "application/x-initial-data") {
                var data = JSON.parse(script.innerHTML);
                props = data.props;
            } else {
                props = null;
            }
        }

        var compositor = this;

        viewName = viewName || this.elem.dataset.compositor;

        ri.startFunctionTracking(this.id);

        var component = compositor.createView(viewName, props);

        React.render(component, compositor.elem, function () {
            compositor.rootComponent = component;
            compositor.viewName = viewName;
            compositor.props = props;

            var missingCalls = ri.getMissingCalls(compositor.id);
            if (missingCalls.length && Compositor.retryMissingFunctions) {
                compositor.requestMissing(missingCalls).then(function (data) {
                    ri.fillCache(data);

                    ri.startFunctionTracking(compositor.id);

                    React.render(component, compositor.elem, function () {
                        var missingFunctionValues = ri.getMissingCalls(compositor.id);
                        if (missingFunctionValues.length) {
                            console.warn("Still missing function values after requesting them: " + JSON.stringify(missingFunctionValues));
                        }

                        if (callback) {
                            callback(compositor);
                        }
                    });
                }).catch(function (e) {
                    console.error("Error rerendering", e);
                });
            } else {
                if (callback) {
                    callback(compositor);
                }
            }
        });
    },
    updateComponent: function (component, vars) {
        var props = component.props;
        if (!props || !props._injection) {
            throw new Error(component + " is not a injected component");
        }

        var compositor = this;
        var index = props._injection.index;
        ajax({
            url: uri("/inject/update", {
                compositor: compositor.viewName,
                index: index,
                vars: JSON.stringify(vars)
            }),
            method: "GET"
        }).then(function (data) {
            var oldProps = compositor.rootComponent.props;
            var newProps = React.addons.update(oldProps, {
                _injections: { $splice: [[index, 1, data.value]] },
                _functionValues: { $set: data.functionValues }
            });

            compositor.render(compositor.viewName, newProps, Compositor.updateHistory);
        }).catch(function (err) {
            console.log("Error updating component #" + index + " in '" + compositor.viewName + ", vars = " + JSON.stringify(vars) + ":\n", err);
        });
    },

    requestMissing: function (missingFunctionValues) {
        var json = JSON.stringify({
            calls: missingFunctionValues
        });

        console.info("Requesting missing function values: " + json);

        return ajax({
            url: uri("/inject/runtimeCalls", {
                compositor: this.viewName,
                calls: json
            })
        });
    },

    navigateTo: function (url) {
        var compositor = this;

        return ajax({
            url: url
        }).then(function (data) {
            window.history.pushState({
                jsViewName: data.jsViewName,
                props: data.props,
                id: compositor.elem.id
            }, "title", url);

            return new Promise(function (resolve, reject) {
                compositor.render(data.jsViewName, data.props, resolve);
            });
        }).catch(function (e) {
            // if we received a NOT_ACCEPTABLE or there was a JSON parsing error
            if (e.status === 406 || e.error && e.error.indexOf("Error parsing JSON") >= 0) {
                // warn and redirect to the original URL

                if (undefined !== "production") {
                    warnOnce(url, "AJAX request to '" + url + "' failed.\nYou might want to declare the link ajax={ false }");
                }

                window.location.href = url;
            }
        });
    }
});

Compositor.clearCompositorRegistry = function () {
    compositors = [];
};

Compositor.find = function (elem) {
    for (var i = 0; i < compositors.length; i++) {
        var compositor = compositors[i];
        if (compositor === elem || compositor.elem.contains(elem)) {
            return compositor;
        }
    }
    return null;
};

Compositor.updateHistory = function (compositor) {
    if (compositor.id === "root") {
        var state = {
            jsViewName: compositor.viewName,
            props: compositor.props,
            id: compositor.id
        };

        //console.log("UPDATE", state.props._injections);

        window.history.replaceState(state, "title", window.location.href);
    }
};

window.onpopstate = function (ev) {
    var state = ev.state;
    //console.log("POP", state.jsViewName, history.state.jsViewName);

    var elem = document.getElementById(state.id);
    var mp = Compositor.find(elem);

    mp.render(state.jsViewName, state.props);
};

// if we encounter missing functions during a render, we request the missing values via AJAX and rerender
Compositor.retryMissingFunctions = true;

module.exports = Compositor;

}).call(this,typeof global !== "undefined" ? global : typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {})

},{"./service/ajax":21,"./util/uri":31,"./util/warn-once":33,"extend":6}],17:[function(require,module,exports){
(function (global){
"use strict";

var React = (typeof window !== "undefined" ? window['React'] : typeof global !== "undefined" ? global['React'] : null);

var classes = require("classnames");

var EDITOR_STORAGE_KEY = "InPageEditorState";

var InPageEditor = React.createClass({
    displayName: "InPageEditor",

    getInitialState: function () {
        var item = sessionStorage.getItem(EDITOR_STORAGE_KEY);

        return item ? JSON.parse(item) : {
            active: false
        };
    },
    componentWillUpdate: function (nextProps, nextState) {
        sessionStorage.setItem(EDITOR_STORAGE_KEY, JSON.stringify(nextState));
    },
    toggle: function () {
        this.setState({
            active: !this.state.active
        });
    },
    render: function () {
        return React.createElement(
            "div",
            { className: classes("editor", this.state.active ? "active" : "inactive") },
            React.createElement(
                "div",
                { className: "editor-tab", onClick: this.toggle },
                "E"
            ),
            this.state.active && React.createElement(
                "div",
                { className: "container-fluid" },
                React.createElement(
                    "h1",
                    null,
                    "Editor"
                ),
                React.createElement(
                    "pre",
                    null,
                    JSON.stringify(this.props.model, null, "    ")
                )
            )
        );
    }
});

module.exports = InPageEditor;

}).call(this,typeof global !== "undefined" ? global : typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {})

},{"classnames":3}],18:[function(require,module,exports){
(function (global){
"use strict";

var React = (typeof window !== "undefined" ? window['React'] : typeof global !== "undefined" ? global['React'] : null);
var Compositor = require("../compositor");

var ComplexComponent = {

    /**
     * Returns the current vars for this component.
     *
     * @returns {Object} current vars map.
     */
    getVars: function () {
        var injection = this.props._injection;
        return injection && injection.vars;
    },

    /**
     * Finds the compositor instance for the current component.
     *
     * Note that this is *not* the compositor component, but react-inject Compositor instance.
     *
     */
    findCompositor: function () {
        return Compositor.find(React.findDOMNode(this));
    },

    /**
     * Updates the current vars for this component by merging the given new values
     * with the current vars.
     *
     * @param newVars
     */
    updateVars: function (newVars) {
        var compositor = this.findCompositor();
        var vars = React.addons.update(this.getVars(), { $merge: newVars });
        compositor.updateComponent(this, vars);
    },
    /**
     * Replaces all vars for this component with the new values.
     *
     * @param newVars
     */
    setVars: function (newVars) {
        var compositor = this.findCompositor();
        compositor.updateComponent(this, newVars);
    }

};

module.exports = ComplexComponent;

}).call(this,typeof global !== "undefined" ? global : typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {})

},{"../compositor":16}],19:[function(require,module,exports){
"use strict";

var DebounceMixin = {
    debounce: function (func, time) {
        var args = Array.prototype.slice.call(arguments, 2);

        var ctx = this;
        var timeout = ctx.debouncedTimeout;
        if (timeout) {
            clearTimeout(timeout);
        }

        ctx.debouncedTimeout = setTimeout(function () {
            ctx.debouncedTimeout = null;
            func.apply(ctx, args);
        }, time);
    }
};

module.exports = DebounceMixin;

},{}],20:[function(require,module,exports){
"use strict";

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
    shouldComponentUpdate: function (nextProps) {
        if (!this.props._injection) {
            return true;
        }
        var result = this.props._injection.injection !== nextProps._injection.injection;
        console.info("Update %s => %s", this.__proto__.constructor.displayName, result);
        return result;
    }

};

module.exports = StatelessComponent;

},{}],21:[function(require,module,exports){
"use strict";

var Promise = require("es6-promise").Promise;
var extend = require("extend");
var cando = require("../cando");

var Enum = require("./../util/enum");

var createXMLHTTPObject = require("./../util/xhr-factory");

var HttpMethod = new Enum({
    GET: true,
    POST: true
});

var DataType = new Enum({
    JSON: true,
    TEXT: true
});

var defaultOpts = {
    method: "get",
    dataType: DataType.JSON,
    headers: null
};

var contentType = {};
contentType[DataType.JSON] = "application/json";
contentType[DataType.TEXT] = "text/plain";

var csrfToken;
var csrfTokenHeader;

function logError(err) {
    var s = [];
    for (var name in err) {
        if (err.hasOwnProperty(name)) {
            l.push(name + " = " + err[name]);
        }
    }

    console.error("Request failed: %s", l.join(", "));
}

function serialize(data) {
    var s = "";

    for (var k in data) {
        if (data.hasOwnProperty(k)) {
            s += (s.length ? "&" : "") + encodeURIComponent(k) + "=" + encodeURIComponent(data[k]);
        }
    }
    return s;
}

/**
 * Requests an URL from the server via AJAX and returns a promise resolving to the returned data.
 *
 * @param opts              options
 * @param opts.url          [string} url
 * @param opts.method       HTTP method (default = "get")
 * @param opts.data         {string|object} POST data
 * @param opts.dataType     {string} expected data type of the resonse (default = "JSON")
 * @param opts.contentType  [string} POST content type. data should be a string in this case.
 *
 * @returns promise resolving to the requested data in the type corresponding to the dataType option.
 */
module.exports = function (opts) {
    opts = extend({}, defaultOpts, opts);

    if (!csrfToken) {
        csrfToken = document.querySelector("meta[name='token']").getAttribute("content");
        csrfTokenHeader = document.querySelector("meta[name='token-type']").getAttribute("content");
    }

    var promise = new Promise(function (resolve, reject) {
        if (typeof opts.url !== "string") {
            reject({
                status: 0,
                error: "No url",
                xhr: null
            });
        }

        var dataType = opts.dataType.toUpperCase();
        if (!DataType.isValid(dataType)) {
            reject({
                status: 0,
                error: "Invalid dataType: " + opts.dataType,
                xhr: null
            });
            return;
        }

        if (!HttpMethod.isValid(opts.method.toUpperCase())) {
            reject({
                status: 0,
                error: "Invalid method: " + opts.method,
                xhr: null
            });
            return;
        }

        var xhr = createXMLHTTPObject();
        if (!xhr) {
            reject({
                status: 0,
                error: "Could not create XMLHTTPObject",
                xhr: null
            });
            return;
        }
        var method = opts.method.toUpperCase();
        xhr.open(method, opts.url, true);
        xhr.setRequestHeader("X-Requested-With", "XMLHttpRequest");
        xhr.setRequestHeader("Accept", contentType[opts.dataType]);

        var headers = opts.headers;
        if (headers) {
            for (var name in headers) {
                if (headers.hasOwnProperty(name)) {
                    xhr.setRequestHeader(name, headers[name]);
                }
            }
        }

        var data = null;
        if (method === "POST") {
            xhr.setRequestHeader(csrfTokenHeader, csrfToken);
            xhr.setRequestHeader("Content-type", opts.contentType || "application/x-www-form-urlencoded");

            data = opts.data || "";
            if (typeof data !== "string") {
                data = serialize(data);
            }
        }

        xhr.onreadystatechange = function () {
            if (xhr.readyState != 4) {
                return;
            }
            if (xhr.status != 200 && xhr.status != 304) {
                reject({
                    status: xhr.status,
                    xhr: xhr,
                    error: "HTTP error " + xhr.status
                });
                return;
            }

            var responseText = xhr.responseText;

            if (dataType === DataType.JSON) {
                try {
                    var data = JSON.parse(responseText);
                    resolve(data);
                } catch (e) {
                    reject({
                        status: xhr.status,
                        xhr: xhr,
                        error: "Error parsing JSON: " + e.message
                    });
                }
            } else if (dataType === DataType.TEXT) {
                resolve(responseText);
            }
        };

        if (xhr.readyState == 4) {
            return;
        }
        xhr.send(data);
    });

    // install default error logger
    promise.then(null, logError);

    return promise;
};

},{"../cando":7,"./../util/enum":28,"./../util/xhr-factory":34,"es6-promise":5,"extend":6}],22:[function(require,module,exports){
"use strict";

var extend = require("extend");

var components = {};

function registerMapRecursively(map) {
    //console.debug("registerMapRecursively", componentsMap, path);
    for (var name in map) {
        if (map.hasOwnProperty(name)) {
            var value = map[name];
            if (typeof value === "object") {
                if (name === "component") {
                    if (value.components && typeof value.components === "object") {
                        ComponentService.register(map, value);
                    }
                } else {
                    registerMapRecursively(map[name], value);
                }
            }
        }
    }
}

var ComponentService = {
    getComponents: function () {
        return components;
    },

    registerBulk: function (bulkMap) {
        registerMapRecursively(bulkMap);
    },
    register: function (dir, def) {
        //console.log("register def", dir.DataGrid, def);

        var subComponents = def.components;
        for (var name in subComponents) {
            if (subComponents.hasOwnProperty(name)) {
                var componentDef = subComponents[name];

                var parts = name.split(".");

                var component = dir;
                for (var i = 0; i < parts.length; i++) {
                    component = component[parts[i]];
                    if (!component) {
                        throw new Error("Cannot find module for declared component '" + name + "'");
                    }
                }

                components[name] = extend({
                    component: component
                }, componentDef);
            }
        }
    }
};

window.ComponentService = ComponentService;

module.exports = ComponentService;

},{"extend":6}],23:[function(require,module,exports){
"use strict";

var roles = {};

module.exports = {
    init: function () {
        var rolesArray = document.body.dataset.roles.replace(" ", "").split(",");
        for (var i = 0; i < rolesArray.length; i++) {
            roles[rolesArray[i]] = true;
        }
    },
    hasRole: function (role) {
        return !!roles[role];
    }
};

},{}],24:[function(require,module,exports){
(function (global){
"use strict";

var componentService = require("./component");

var React = (typeof window !== "undefined" ? window['React'] : typeof global !== "undefined" ? global['React'] : null);
var extend = require("extend");

var components = componentService.getComponents();

var viewComponents = {};

function indent(buf, depth) {
    for (var i = 0; i < depth; i++) {
        buf.push("    ");
    }
}

var generatedIdRegEx = /^id-[a-z0-5]+$/;

var expressionRegEx = /^\{\s*(.*?)\s*}$/;

window.debug = function (a) {
    console.log("DEBUG", a);
    return a;
};

function renderRecursively(buf, componentModel, depth, usedComponents) {
    var name = componentModel.name;
    var code, value, component, m;

    var componentDescriptor;
    var isComponent = components.hasOwnProperty(name);
    if (isComponent) {
        componentDescriptor = components[name];
        component = name;

        usedComponents[name.split(".")[0]] = true;
    } else {
        component = JSON.stringify(name);
    }

    var attrs = componentModel.attrs;
    var queries = componentDescriptor && componentDescriptor.queries;

    indent(buf, depth);
    buf.push("React.createElement(", component, ", ");

    if (queries) {
        buf.push("extend({\n");
    } else {
        buf.push("{\n");
    }

    var first = true;
    for (var attrName in attrs) {
        if (attrs.hasOwnProperty(attrName)) {
            value = attrs[attrName];

            if (isComponent || attrName !== "id" || !generatedIdRegEx.test(value)) {

                if (!first) {
                    buf.push(",\n");
                }
                first = false;

                var last = value.length - 1;
                if (typeof value === "string" && (m = expressionRegEx.exec(value))) {
                    code = m[1];
                } else {
                    code = JSON.stringify(value);
                }

                indent(buf, depth + 2);
                buf.push(JSON.stringify(attrName), " : ", code);
            }
        }
    }

    if (queries) {
        buf.push("\n");
        indent(buf, depth + 1);
        buf.push("}, this.props.componentData[\"" + attrs.id + "\"])");
    } else {
        buf.push("\n");
        indent(buf, depth + 1);
        buf.push("}");
    }

    var kids = componentModel.kids;
    if (kids) {
        for (var i = 0; i < kids.length; i++) {
            buf.push(",\n");
            var kidModel = kids[i];
            renderRecursively(buf, kidModel, depth + 1, usedComponents);
        }
    }
    buf.push("\n");
    indent(buf, depth);
    buf.push(")");
}

/**
 * Handles converting the view JSON models into js code and caching the React component results.
 *
 * @type {{}}
 */
var ViewService = {

    renderView: function (name, model) {
        var buf = ["\nreturn React.createClass({\n    displayName: \"" + name + "\",\n\n    render: function ()\n    {\n        return (\n\n"];

        var usedComponents = {};

        renderRecursively(buf, model.root, 3, usedComponents);

        for (var componentName in usedComponents) {
            if (usedComponents.hasOwnProperty(componentName)) {
                buf.splice(0, 0, "var " + componentName + " = components[\"" + componentName + "\"].component;\n");
            }
        }

        buf.push("\n        );\n    }\n});");
        return buf.join("");
    },

    getViewComponent: function (name, model, regenerate) {
        var component = viewComponents[name];
        if (!component || regenerate) {
            var code = ViewService.renderView(name, model);

            console.debug(code);

            component = new Function("React", "components", "extend", code)(React, components, extend);

            viewComponents[name] = component;
        }

        return component;
    }
};
module.exports = ViewService;

}).call(this,typeof global !== "undefined" ? global : typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {})

},{"./component":22,"extend":6}],25:[function(require,module,exports){
(function (global){
"use strict";

var React = (typeof window !== "undefined" ? window['React'] : typeof global !== "undefined" ? global['React'] : null);

var Alert = React.createClass({
    displayName: "Alert",

    propTypes: React.PropTypes.string.isRequired,
    render: function () {
        return React.createElement(
            "div",
            { className: "bg-danger" },
            this.props.message
        );
    }
});

module.exports = Alert;

}).call(this,typeof global !== "undefined" ? global : typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {})

},{}],26:[function(require,module,exports){
(function (global){
"use strict";

var React = (typeof window !== "undefined" ? window['React'] : typeof global !== "undefined" ? global['React'] : null);

var hasClass = require("../util/has-class");

var classnames = require("classnames");

var PagingLink = React.createClass({
    displayName: "PagingLink",

    onClick: function (ev) {
        // check disabled class to make sure we're not executing a link in case the CSS pointer event rule
        // doesn't catch
        var classes = ev.target.className;
        if (!hasClass(classes, "disabled")) {
            this.props.ctx.offsetLink.requestChange(this.props.newOffset);
        }
        ev.preventDefault();
    },
    render: function () {

        var newOffset = this.props.newOffset;

        var ctx = this.props.ctx;
        var rowCount = ctx.rowCount;
        var limit = ctx.limit;
        var offsetLink = ctx.offsetLink;
        var max = ctx.maxOffset;

        var disabled = false;
        var isCurrent = newOffset === offsetLink.value;
        if (isCurrent) {
            disabled = true;
        }

        if (newOffset < 0) {
            newOffset = 0;
            disabled = true;
        } else if (newOffset > max) {
            newOffset = max;
            disabled = true;
        }

        if (disabled) {
            return React.createElement(
                "span",
                {
                    className: classnames({
                        "btn": true,
                        "btn-link": true,
                        "disabled": true,
                        "current": isCurrent
                    }) },
                this.props.label
            );
        }

        return React.createElement(
            "a",
            {
                onClick: this.onClick,
                href: "#jump-to-" + newOffset,
                className: classnames({
                    "btn": true,
                    "btn-link": true
                }) },
            this.props.label
        );
    }
});

var PagingComponent = React.createClass({
    displayName: "PagingComponent",

    render: function () {
        var rowCount = this.props.rowCount;
        var limit = this.props.limit;
        var offset = this.props.offsetLink.value;

        var max = Math.floor((rowCount - 1) / limit) * this.props.limit;

        // if we have nothing to page, don't render anything
        if (max === 0) {
            return false;
        }

        var ctx = {
            limit: this.props.limit,
            rowCount: this.props.rowCount,
            offsetLink: this.props.offsetLink,
            maxOffset: max
        };

        var links = [React.createElement(PagingLink, { key: "first", newOffset: 0, label: "|<", ctx: ctx }), React.createElement(PagingLink, { key: "prev", newOffset: offset - limit, label: "<", ctx: ctx })];

        var page,
            currentPage = (offset / limit | 0) + 1;
        for (var i = -2; i <= 2; i++) {
            page = currentPage + i;

            var newOffset = offset + i * limit;

            var label = page > 0 && newOffset >= 0 && newOffset <= max ? page : "\u00a0";

            links.push(React.createElement(PagingLink, { key: i, newOffset: newOffset, label: label, ctx: ctx }));
        }

        links.push(React.createElement(PagingLink, { key: "next", newOffset: offset + limit, label: ">", ctx: ctx }), React.createElement(PagingLink, { key: "last", newOffset: max, label: ">|", ctx: ctx }));

        return React.createElement(
            "div",
            { className: "paging" },
            links
        );
    }
});

module.exports = PagingComponent;

}).call(this,typeof global !== "undefined" ? global : typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {})

},{"../util/has-class":29,"classnames":3}],27:[function(require,module,exports){
(function (global){
"use strict";

var React = (typeof window !== "undefined" ? window['React'] : typeof global !== "undefined" ? global['React'] : null);
var extend = require("extend");

var security = require("../service/security");

var InPageEditor = require("../editor/InPageEditor");

var components = require("../service/component").getComponents();

var expressionRegEx = /^\{\s*(.*?)\s*}$/;

var ViewComponent = React.createClass({
    displayName: "ViewComponent",

    renderComponent: function (model, componentData, key) {
        //console.log("renderComponent", model, componentData, key);

        var component = model.name;

        var modelAttrs = model.attrs;
        var modelId = modelAttrs && model.id;
        if (component <= 'Z') {
            component = components[component].component;
        }

        var m;
        var attrs = {
            key: key,
            id: modelId
        };

        if (modelAttrs) {
            for (var name in modelAttrs) {
                if (modelAttrs.hasOwnProperty(name)) {
                    var value = modelAttrs[name];
                    if (typeof value === "string" && (m = expressionRegEx.exec(value))) {
                        value = eval(value);
                    }
                    attrs[name] = value;
                }
            }
        }
        var modelKids = model.kids;
        var callArgs = [component, extend(attrs, modelId && components[modelId])];

        if (modelKids) {
            for (var i = 0; i < modelKids.length; i++) {
                var kid = modelKids[i];

                callArgs.push(this.renderComponent(kid, componentData, i));
            }
        }

        //console.log("createElement", callArgs, modelKids);
        return React.createElement.apply(React, callArgs);
    },

    render: function () {
        var model = this.props.model;

        return React.createElement(
            "div",
            { className: "container-fluid" },
            React.createElement(
                "div",
                { className: "row" },
                React.createElement(
                    "div",
                    { className: "col-md-12" },
                    this.renderComponent(model.root, this.props.componentData, "root")
                )
            ),
            security.hasRole("ROLE_EDITOR") && React.createElement(InPageEditor, { model: model, activeLink: this.props.activeLink })
        );
    }
});

module.exports = ViewComponent;

}).call(this,typeof global !== "undefined" ? global : typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {})

},{"../editor/InPageEditor":17,"../service/component":22,"../service/security":23,"extend":6}],28:[function(require,module,exports){
"use strict";

function Enum(map) {
    if (!(this instanceof Enum)) {
        return new Enum(map);
    }

    for (var name in map) {
        if (map.hasOwnProperty(name)) {
            this[name] = name;
        }
    }
}

Enum.prototype.values = function () {
    var l = [];
    for (var name in this) {
        if (this.hasOwnProperty(name)) {
            l.push(name);
        }
    }
    return l;
};

Enum.prototype.isValid = function (value) {
    return this.hasOwnProperty(value) && this[value] === value;
};

module.exports = Enum;

},{}],29:[function(require,module,exports){
"use strict";

module.exports = function (classes, cls) {
    var classesLen = classes.length,
        classLen = cls.length,
        pos;

    if (classes) {
        var start = 0;
        while ((pos = classes.indexOf(cls, start)) >= 0) {
            if ((pos == 0 || classes.charAt(pos - 1) == " ") && (pos + classLen == classesLen || classes.charAt(pos + classLen) == " ")) {
                return true;
            }

            start = pos + classLen;
        }
    }
    return false;
};

},{}],30:[function(require,module,exports){
"use strict";

module.exports = (function () {
    var parseXml;

    if (typeof window === "undefined") {
        return null;
    }

    if (typeof window.DOMParser !== "undefined") {
        parseXml = function (xmlStr) {
            return new window.DOMParser().parseFromString(xmlStr, "text/xml");
        };
    } else if (typeof window.ActiveXObject !== "undefined" && new window.ActiveXObject("Microsoft.XMLDOM")) {
        parseXml = function (xmlStr) {
            var xmlDoc = new window.ActiveXObject("Microsoft.XMLDOM");
            xmlDoc.async = "false";
            xmlDoc.loadXML(xmlStr);
            return xmlDoc;
        };
    }
    return parseXml;
})();

},{}],31:[function(require,module,exports){
"use strict";

var contextPath = null;

function evaluateParams(params) {
    var p = "";
    if (params) {
        var sep = "?";
        for (var name in params) {
            if (params.hasOwnProperty(name)) {
                p += sep + encodeURIComponent(name) + "=" + encodeURIComponent(params[name]);
                sep = "&";
            }
        }
    }
    return p;
}

function replacePathVariables(location, params) {
    return location.replace(/{([a-z]+)}/g, function (match, name, offset, str) {
        var value = params[name];
        if (value === undefined) {
            throw new Error("Undefined path variable '" + name + "' in '" + location + "'");
        }
        delete params[name];
        return value;
    });
}
function uri(location, params) {
    location = replacePathVariables(location, params);

    if (contextPath === null) {
        if (typeof document !== "undefined") {
            contextPath = document.body && document.body.dataset && document.body.dataset.contextPath;

            if (typeof contextPath !== "string") {
                throw new Error("Context path not initialized");
            }
            //console.log("context-path from body[data-context-path] = " + contextPath);
        }
    }

    var result = contextPath + location + evaluateParams(params);

    //console.log("URI:", result);

    return result;
}

uri._init_context_path = function (cp) {
    if (!contextPath) {
        contextPath = cp;
    }
};

module.exports = uri;

},{}],32:[function(require,module,exports){
"use strict";

function ValueLink(value, requestChange) {
    this.value = value;
    this.requestChange = requestChange;
}

module.exports = ValueLink;

},{}],33:[function(require,module,exports){
"use strict";

var cando = require("../cando");

var MAP_KEY = "warnOnce:warnings";

/**
 * Warns a user once per session about something with an alert,
 *
 * @param name      {string?} key for this message
 * @param message   {string} message
 */
module.exports = function (name, message) {
    if (cando.sessionStorage) {
        console.log("can do sessionStorage");

        message = message || name;

        var key = "warnOnce:" + name;

        var warnings = JSON.parse(sessionStorage.getItem(MAP_KEY));
        if (warnings) {
            console.log("map present");
            if (warnings[key]) {
                console.log("warning registered");
                return true;
            }
        } else {
            console.log("no warnings");
            // no warnings map -> create one with our entry
            warnings = {};
        }

        warnings[key] = true;

        console.log("store %o", warnings);
        sessionStorage.setItem(MAP_KEY, JSON.stringify(warnings));
    }
    alert(message);
};

},{"../cando":7}],34:[function(require,module,exports){
"use strict";

var XMLHttpFactories = [function () {
    return new XMLHttpRequest();
}, function () {
    return new ActiveXObject("Msxml2.XMLHTTP");
}, function () {
    return new ActiveXObject("Msxml3.XMLHTTP");
}, function () {
    return new ActiveXObject("Microsoft.XMLHTTP");
}];

var startAt = 0;

module.exports = function () {
    var xmlhttp = false;
    for (var i = startAt; i < XMLHttpFactories.length; i++) {
        try {
            xmlhttp = XMLHttpFactories[i]();
        } catch (e) {
            continue;
        }
        // remember succesful index to start with it the next time
        startAt = i;
        break;
    }
    return xmlhttp;
};

},{}]},{},[1])


//# sourceMappingURL=main.js.map
