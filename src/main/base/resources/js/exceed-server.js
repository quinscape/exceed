var Exceed =
webpackJsonpExceed([1],{

/***/ 216:
/*!************************!*\
  !*** ./server-main.js ***!
  \************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


__webpack_require__(/*! es6-shim */ 124);

var _es6PromisePolyfill = __webpack_require__(/*! es6-promise-polyfill */ 24);

var _bignumber = __webpack_require__(/*! bignumber.js */ 66);

var _bignumber2 = _interopRequireDefault(_bignumber);

var _bindToGlobal = __webpack_require__(/*! ./util/bind-to-global */ 125);

var _bindToGlobal2 = _interopRequireDefault(_bindToGlobal);

var _domain = __webpack_require__(/*! ./service/domain */ 15);

var _domain2 = _interopRequireDefault(_domain);

var _propertyConverter = __webpack_require__(/*! ./service/property-converter */ 31);

var _propertyConverter2 = _interopRequireDefault(_propertyConverter);

var _cast = __webpack_require__(/*! ./util/cast */ 129);

var _cast2 = _interopRequireDefault(_cast);

var _when = __webpack_require__(/*! ./util/when */ 130);

var _when2 = _interopRequireDefault(_when);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

/**
 * All properties of this object are bound to the global object as non-writable property for easy access
 */
// setImmediate from promises is provided on the server-side by a java host object
module.exports = (0, _bindToGlobal2.default)({
    _a: {},
    _v: {
        isNew: function isNew(domainObject) {
            return domainObject && domainObject.id === null;
        },
        cast: _cast2.default,
        when: _when2.default,
        conditional: function conditional(cond) {
            if (cond) {
                return _es6PromisePolyfill.Promise.resolve(true);
            } else {
                return _es6PromisePolyfill.Promise.reject(false);
            }
        },
        debug: function debug(data) {
            var len = data && data.length;
            if (typeof len !== "number" || len < 2) {
                return;
            }

            var txt = data[0] + " = " + data[1];
            for (var i = 2; i < data.length; i += 2) {
                txt += ", " + data[i] + " = " + data[i + 1];
            }
            console.debug(txt);

            return data[1];
        }
    },
    _domainService: _domain2.default,
    _converter: _propertyConverter2.default,
    _now: function _now() {
        return new Date();
    },

    _decimal: function _decimal(val) {
        return new _bignumber2.default(val);
    },

    Promise: _es6PromisePolyfill.Promise

});

/***/ })

},[216]);
//# sourceMappingURL=exceed-server.js.map