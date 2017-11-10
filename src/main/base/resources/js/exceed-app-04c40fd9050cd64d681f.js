var Exceed =
webpackJsonpExceed([1],{

/***/ 144:
/*!****************************!*\
  !*** ./service/process.js ***!
  \****************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
    value: true
});

var _scope = __webpack_require__(/*! ./scope */ 107);

var _scope2 = _interopRequireDefault(_scope);

var _uri = __webpack_require__(/*! ../util/uri */ 15);

var _uri2 = _interopRequireDefault(_uri);

var _sys = __webpack_require__(/*! ../sys */ 12);

var _sys2 = _interopRequireDefault(_sys);

var _store = __webpack_require__(/*! ./store */ 3);

var _store2 = _interopRequireDefault(_store);

var _view = __webpack_require__(/*! ../actions/view */ 24);

var _reducers = __webpack_require__(/*! ../reducers */ 23);

var _dialog = __webpack_require__(/*! ../util/dialog */ 120);

var _dialog2 = _interopRequireDefault(_dialog);

var _meta = __webpack_require__(/*! ../reducers/meta */ 14);

var _i18n = __webpack_require__(/*! ../service/i18n */ 9);

var _i18n2 = _interopRequireDefault(_i18n);

var _runtimeViewApi = __webpack_require__(/*! ./runtime-view-api */ 78);

var _runtimeViewApi2 = _interopRequireDefault(_runtimeViewApi);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function renderURI(locInfo, transition) {
    var params = {
        stateId: locInfo.params.stateId,
        _trans: transition
    };

    return (0, _uri2.default)("/app/" + _sys2.default.appName + locInfo.routingTemplate, params);
}

var processService = {
    transition: function transition(transitionName, context) {
        var transitionModel = (0, _meta.getViewStateTransition)(_store2.default.getState(), transitionName);
        if (!transitionModel) {
            throw new Error("No transition '" + transitionName + "' exists");
        }

        var confirmation = transitionModel.confirmation;


        var promise = void 0;
        if (confirmation) {
            promise = _dialog2.default.prompt({
                title: confirmation.title,
                text: confirmation.message,
                choices: [(0, _i18n2.default)("Cancel"), confirmation.okLabel],
                properties: []
            });
        } else {
            promise = Promise.resolve();
        }

        return promise.then(function (result) {
            //console.log("CONFIRMATION", result);

            if (result && result.choice !== 1) {
                return;
            }

            var state = _store2.default.getState();
            var locInfo = (0, _reducers.getLocation)(state);

            var scopeDeclarations = (0, _reducers.getScopeDeclarations)(state);
            var contextUpdate = _scope2.default.getScopeDelta();

            if (context) {
                for (var name in context) {
                    if (context.hasOwnProperty(name)) {
                        var obj = context[name];

                        var decl = scopeDeclarations[name];
                        if (decl) {
                            var model = decl.model;
                            if (model.type === "DomainType") {
                                if (model.typeParam === null || model.typeParam === obj._type) {
                                    contextUpdate[name] = obj;
                                    delete context[name];
                                }
                            }
                        }
                    }
                }
            }

            return _store2.default.dispatch((0, _view.executeTransition)({
                url: renderURI(locInfo, transitionName),
                data: {
                    context: context,
                    contextUpdate: contextUpdate
                },
                urlProvider: function urlProvider(xhr, data) {
                    // render without transition parameter
                    return renderURI((0, _reducers.getLocation)(data));
                }
            }));
        });
    },
    scope: _runtimeViewApi2.default.prototype.scope
};

exports.default = processService;

/***/ }),

/***/ 223:
/*!************************************!*\
  !*** ./editor/gui/GUIContainer.js ***!
  \************************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
    value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _guiContext = __webpack_require__(/*! ./gui-context */ 96);

var _guiContext2 = _interopRequireDefault(_guiContext);

var _containerContext = __webpack_require__(/*! ./container-context */ 224);

var _containerContext2 = _interopRequireDefault(_containerContext);

var _event = __webpack_require__(/*! ../../util/event */ 60);

var _event2 = _interopRequireDefault(_event);

var _globalDrag = __webpack_require__(/*! ../../util/global-drag */ 225);

var _globalDrag2 = _interopRequireDefault(_globalDrag);

var _propTypes = __webpack_require__(/*! prop-types */ 2);

var _propTypes2 = _interopRequireDefault(_propTypes);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var React = __webpack_require__(/*! react */ 0);


var requestAnimationFrame = __webpack_require__(/*! raf */ 409);

var MIN_ZOOM = 1;
var MAX_ZOOM = 16;

var ZOOM_SPEED = 1.5;
var ZOOM_ACCELERATION = 0.01;

var ANIMATION_STEP = 1 / 100 * 1000;

function ViewBox(centerX, centerY, height) {
    var size = _guiContext2.default.applyZoom(height);
    var hh = size / 2;
    // we use height for both dimensions since we have a locked aspect ratio anyway and the width
    // most often is given as "100%"
    this.x = centerX - hh;
    this.y = centerY - hh;
    this.size = size;
}

ViewBox.prototype.render = function () {
    return this.x + " " + this.y + " " + this.size + " " + this.size;
};

/**
 * Contains SVG based GUIElement and provides a GUIContext context object to GUIElement instances to manage SVG
 * component focus and keyboard interaction via focus proxy elements.
 */

var GUIContainer = function (_React$Component) {
    _inherits(GUIContainer, _React$Component);

    function GUIContainer() {
        var _ref;

        var _temp, _this, _ret;

        _classCallCheck(this, GUIContainer);

        for (var _len = arguments.length, args = Array(_len), _key = 0; _key < _len; _key++) {
            args[_key] = arguments[_key];
        }

        return _ret = (_temp = (_this = _possibleConstructorReturn(this, (_ref = GUIContainer.__proto__ || Object.getPrototypeOf(GUIContainer)).call.apply(_ref, [this].concat(args))), _this), _this.onMouseDown = function (ev) {
            _this.dragLocked = true;

            if (_this.props.pan) {
                var x = _guiContext2.default.applyZoom(ev.pageX);
                var y = _guiContext2.default.applyZoom(ev.pageY);

                _this.offsetX = _this.centerX + x;
                _this.offsetY = _this.centerY + y;
            }

            _globalDrag2.default.setActiveDrag(_this);

            return _event2.default.preventDefault(ev);
        }, _temp), _possibleConstructorReturn(_this, _ret);
    }

    _createClass(GUIContainer, [{
        key: "getChildContext",
        value: function getChildContext() {
            //console.log("GUIContainer.getChildContext");

            this.ctx = new _containerContext2.default(this);

            return {
                containerContext: this.ctx
            };
        }
    }, {
        key: "componentDidMount",
        value: function componentDidMount() {
            //console.log("GUIContainer mount");

            _guiContext2.default.update();

            this.centerX = this.props.centerX;
            this.centerY = this.props.centerY;
            this.targetZoom = _guiContext2.default.getZoom();
            this.dz = 0;
            this.animating = false;

            if (this.props.zoom && this.ctx.svgElem) {
                _event2.default.add(this.ctx.svgElem, "mousewheel", this.onMouseWheel, false);
            }

            _globalDrag2.default.init();
        }
    }, {
        key: "componentWillUnmount",
        value: function componentWillUnmount() {
            if (this.props.zoom && this.ctx.svgElem) {
                _event2.default.remove(this.ctx.svgElem, "mousewheel", this.onMouseWheel, false);
            }

            _globalDrag2.default.destroy();
        }
    }, {
        key: "animateZoom",
        value: function animateZoom() {
            var now = Date.now();
            var finished = false;

            var delta = now - this.lastFrame;

            this.lastFrame = now;
            //console.log("animate");

            // We consume the time that has passed in discrete steps
            while (delta > 0) {
                //console.log("animate", this.targetZoom, GUIContext.getZoom());
                var d = this.targetZoom - _guiContext2.default.getZoom();
                //console.log("update", d);

                // as soon as we reach our destination or overshoot in the zoom direction we were heading..
                if (this.dz < 0 && d > this.dz || this.dz > 0 && d < this.dz) {
                    // we set the final targetZoom..
                    _guiContext2.default.setZoom(this.targetZoom);
                    // reset our speed
                    this.dz = 0;
                    // and stop animating
                    this.animating = false;
                    break;
                } else {
                    // accelerate towards target zoom
                    if (d < 0) {
                        if (this.dz > -ZOOM_SPEED) {
                            this.dz = Math.min(0, this.dz) - ZOOM_ACCELERATION;
                        }
                    } else {
                        if (this.dz < ZOOM_SPEED) {
                            this.dz = Math.max(0, this.dz) + ZOOM_ACCELERATION;
                        }
                    }
                }

                delta -= ANIMATION_STEP;
            }

            // set current zoom ..
            _guiContext2.default.setZoom(_guiContext2.default.getZoom() + this.dz);

            if (this.animating) {
                // .. and request the next animation step.
                requestAnimationFrame(this.animateZoom);
            }

            this.updateViewBox();
        }
    }, {
        key: "updateViewBox",
        value: function updateViewBox() {
            if (this.ctx.svgElem) {
                this.ctx.svgElem.setAttribute("viewBox", new ViewBox(this.centerX, this.centerY, this.props.height).render());
            }
        }
    }, {
        key: "onMouseWheel",
        value: function onMouseWheel(ev) {
            if (_globalDrag2.default.isActiveDrag(this)) {
                return;
            }

            // cross-browser wheel delta
            var event = window.event || ev;

            this.targetZoom -= Math.max(-1, Math.min(1, event.wheelDelta || -event.detail));

            if (this.targetZoom < MIN_ZOOM) {
                this.targetZoom = MIN_ZOOM;
            }

            if (this.targetZoom > MAX_ZOOM) {
                this.targetZoom = MAX_ZOOM;
            }

            //console.log("TARGET: ", this.targetZoom);

            if (!this.animating) {
                //console.log("start animation");
                this.animating = true;
                requestAnimationFrame(this.animateZoom);
            }

            return _event2.default.preventDefault(ev);
        }
    }, {
        key: "onMouseMove",
        value: function onMouseMove(x, y) {
            if (_globalDrag2.default.isActiveDrag(this)) {

                x = this.offsetX - _guiContext2.default.applyZoom(x);
                y = this.offsetY - _guiContext2.default.applyZoom(y);

                if (this.dragLocked) {
                    var dx = this.centerX - x;
                    var dy = this.centerY - y;

                    var dist = Math.sqrt(dx * dx + dy * dy);

                    //console.log("LOCKED MOVE", dist);

                    if (dist > 2) {
                        this.dragLocked = false;
                        //console.log("ACTIVATE DRAG");
                    }
                }

                if (!this.dragLocked) {
                    //console.log("CONTAINER MOVE", x,y);

                    this.centerX = x;
                    this.centerY = y;

                    this.updateViewBox();
                }
            }
        }
    }, {
        key: "onMouseUp",
        value: function onMouseUp(x, y) {
            if (_globalDrag2.default.isActiveDrag(this) || !this.props.pan) {
                x = this.offsetX - _guiContext2.default.applyZoom(x);
                y = this.offsetY - _guiContext2.default.applyZoom(y);

                _globalDrag2.default.setActiveDrag(null);

                if (this.dragLocked) {
                    var fn = this.props.onInteraction;
                    typeof fn == "function" && fn();
                } else {
                    this.centerX = x;
                    this.centerY = y;

                    this.updateViewBox();
                }
            }
        }
    }, {
        key: "render",
        value: function render() {
            var _this2 = this;

            var _props = this.props,
                width = _props.width,
                height = _props.height,
                centerX = _props.centerX,
                centerY = _props.centerY,
                style = _props.style,
                children = _props.children,
                pan = _props.pan;


            var viewBox = void 0;

            if (typeof this.centerX === "number") {
                viewBox = new ViewBox(this.centerX, this.centerY, height);
            } else {
                viewBox = new ViewBox(centerX, centerY, height);
            }

            return React.createElement(
                "div",
                { className: "gui-container" },
                React.createElement(
                    "svg",
                    {
                        ref: function ref(elem) {
                            return _this2.ctx.init(elem);
                        },
                        width: width,
                        height: height,
                        style: style,
                        viewBox: viewBox.render(),
                        preserveAspectRatio: "xMidYMid meet"
                    },
                    React.createElement("rect", {
                        x: viewBox.x - viewBox.size,
                        y: viewBox.y - viewBox.size,
                        width: viewBox.size * 3,
                        height: viewBox.size * 3,
                        fill: "transparent",
                        onMouseDown: this.onMouseDown,
                        onTouchStart: this.onMouseDown
                    }),
                    children
                )
            );
        }
    }]);

    return GUIContainer;
}(React.Component);

GUIContainer.propTypes = {
    centerX: _propTypes2.default.number,
    centerY: _propTypes2.default.number,
    width: _propTypes2.default.oneOfType([_propTypes2.default.number, _propTypes2.default.string]),
    height: _propTypes2.default.number,
    style: _propTypes2.default.object,
    zoom: _propTypes2.default.bool,
    pan: _propTypes2.default.bool,
    onInteraction: _propTypes2.default.func
};
GUIContainer.childContextTypes = {
    containerContext: _propTypes2.default.instanceOf(_containerContext2.default)
};
GUIContainer.defaultProps = {
    centerX: 0,
    centerY: 0,
    width: "100%",
    height: 600,
    style: null,
    zoom: true,
    pan: true,
    onInteraction: null
};
exports.default = GUIContainer;

/***/ }),

/***/ 224:
/*!*****************************************!*\
  !*** ./editor/gui/container-context.js ***!
  \*****************************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
    value: true
});
function ContainerContext(containerComponent) {
    this.containerComponent = containerComponent;
    this.svgElem = null;
}

ContainerContext.prototype.init = function (svgElem) {
    //console.log("INIT SVG ELEM", svgElem);
    this.svgElem = svgElem;
};

ContainerContext.prototype.toObjectCoordinates = function (x, y) {
    var svg = this.svgElem;
    var pt = svg.createSVGPoint();
    pt.x = x;
    pt.y = y;
    pt = pt.matrixTransform(svg.getScreenCTM().inverse());

    return {
        x: pt.x,
        y: pt.y
    };
};

exports.default = ContainerContext;

/***/ }),

/***/ 225:
/*!*****************************!*\
  !*** ./util/global-drag.js ***!
  \*****************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
    value: true
});

var _event = __webpack_require__(/*! ./event */ 60);

var _event2 = _interopRequireDefault(_event);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var activeDragObject;

var initCount = 0;

var eventPos = { x: 0, y: 0 };

function globalMouseMove(ev) {
    if (activeDragObject) {
        _event2.default.position(ev, eventPos);

        activeDragObject.onMouseMove(eventPos.x, eventPos.y);

        return _event2.default.preventDefault(ev);
    }
}

function globalMouseUp(ev) {
    if (activeDragObject) {
        _event2.default.position(ev, eventPos);

        activeDragObject.onMouseUp(eventPos.x, eventPos.y);
        activeDragObject = null;
        return _event2.default.preventDefault(ev);
    }
}

/**
 * Helper module for drag and drop operations and the fact that we register mousemove and mouseup on a different target
 * than mousedown usually. mousedown needs to be on the element to be dragged to initiate the drag, once the dragging has
 * started, it is better to register mousemove and mouseup globally so that moving the mouse out of the container does
 * not as easily mess up interaction when we miss a mouse up. It also allows us to intuitively move objects out of the
 * screen and not have it stuck at the border.
 *
 * @type {{init: GlobalDrag.init, destroy: GlobalDrag.destroy, setActiveDrag: GlobalDrag.setActiveDrag}}
 */
var GlobalDrag = {
    init: function init() {
        if (initCount === 0) {
            //console.log("INIT GLOBAL DRAG");

            _event2.default.add(window, "mousemove", globalMouseMove, false);
            _event2.default.add(window, "mouseup", globalMouseUp, false);
        }

        initCount++;
    },
    destroy: function destroy() {
        if (--initCount === 0) {
            //console.log("DESTROY GLOBAL DRAG");

            _event2.default.remove(window, "mousemove", globalMouseMove, false);
            _event2.default.remove(window, "mouseup", globalMouseUp, false);
        }
    },
    setActiveDrag: function setActiveDrag(dragObject) {
        //console.log("ACTIVE DRAG", dragObject);

        activeDragObject = dragObject;
    },
    isActiveDrag: function isActiveDrag(dragObject) {
        return activeDragObject === dragObject;
    }

};

exports.default = GlobalDrag;

/***/ }),

/***/ 226:
/*!**********************************************************************************!*\
  !*** /home/sven/ideaprojects/exceed/node_modules/react-bootstrap/lib/Popover.js ***!
  \**********************************************************************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


exports.__esModule = true;

var _extends3 = __webpack_require__(/*! babel-runtime/helpers/extends */ 8);

var _extends4 = _interopRequireDefault(_extends3);

var _objectWithoutProperties2 = __webpack_require__(/*! babel-runtime/helpers/objectWithoutProperties */ 10);

var _objectWithoutProperties3 = _interopRequireDefault(_objectWithoutProperties2);

var _classCallCheck2 = __webpack_require__(/*! babel-runtime/helpers/classCallCheck */ 5);

var _classCallCheck3 = _interopRequireDefault(_classCallCheck2);

var _possibleConstructorReturn2 = __webpack_require__(/*! babel-runtime/helpers/possibleConstructorReturn */ 6);

var _possibleConstructorReturn3 = _interopRequireDefault(_possibleConstructorReturn2);

var _inherits2 = __webpack_require__(/*! babel-runtime/helpers/inherits */ 7);

var _inherits3 = _interopRequireDefault(_inherits2);

var _classnames = __webpack_require__(/*! classnames */ 1);

var _classnames2 = _interopRequireDefault(_classnames);

var _react = __webpack_require__(/*! react */ 0);

var _react2 = _interopRequireDefault(_react);

var _propTypes = __webpack_require__(/*! prop-types */ 2);

var _propTypes2 = _interopRequireDefault(_propTypes);

var _isRequiredForA11y = __webpack_require__(/*! prop-types-extra/lib/isRequiredForA11y */ 227);

var _isRequiredForA11y2 = _interopRequireDefault(_isRequiredForA11y);

var _bootstrapUtils = __webpack_require__(/*! ./utils/bootstrapUtils */ 11);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { 'default': obj }; }

var propTypes = {
  /**
   * An html id attribute, necessary for accessibility
   * @type {string}
   * @required
   */
  id: (0, _isRequiredForA11y2['default'])(_propTypes2['default'].oneOfType([_propTypes2['default'].string, _propTypes2['default'].number])),

  /**
   * Sets the direction the Popover is positioned towards.
   */
  placement: _propTypes2['default'].oneOf(['top', 'right', 'bottom', 'left']),

  /**
   * The "top" position value for the Popover.
   */
  positionTop: _propTypes2['default'].oneOfType([_propTypes2['default'].number, _propTypes2['default'].string]),
  /**
   * The "left" position value for the Popover.
   */
  positionLeft: _propTypes2['default'].oneOfType([_propTypes2['default'].number, _propTypes2['default'].string]),

  /**
   * The "top" position value for the Popover arrow.
   */
  arrowOffsetTop: _propTypes2['default'].oneOfType([_propTypes2['default'].number, _propTypes2['default'].string]),
  /**
   * The "left" position value for the Popover arrow.
   */
  arrowOffsetLeft: _propTypes2['default'].oneOfType([_propTypes2['default'].number, _propTypes2['default'].string]),

  /**
   * Title content
   */
  title: _propTypes2['default'].node
};

var defaultProps = {
  placement: 'right'
};

var Popover = function (_React$Component) {
  (0, _inherits3['default'])(Popover, _React$Component);

  function Popover() {
    (0, _classCallCheck3['default'])(this, Popover);
    return (0, _possibleConstructorReturn3['default'])(this, _React$Component.apply(this, arguments));
  }

  Popover.prototype.render = function render() {
    var _extends2;

    var _props = this.props,
        placement = _props.placement,
        positionTop = _props.positionTop,
        positionLeft = _props.positionLeft,
        arrowOffsetTop = _props.arrowOffsetTop,
        arrowOffsetLeft = _props.arrowOffsetLeft,
        title = _props.title,
        className = _props.className,
        style = _props.style,
        children = _props.children,
        props = (0, _objectWithoutProperties3['default'])(_props, ['placement', 'positionTop', 'positionLeft', 'arrowOffsetTop', 'arrowOffsetLeft', 'title', 'className', 'style', 'children']);

    var _splitBsProps = (0, _bootstrapUtils.splitBsProps)(props),
        bsProps = _splitBsProps[0],
        elementProps = _splitBsProps[1];

    var classes = (0, _extends4['default'])({}, (0, _bootstrapUtils.getClassSet)(bsProps), (_extends2 = {}, _extends2[placement] = true, _extends2));

    var outerStyle = (0, _extends4['default'])({
      display: 'block',
      top: positionTop,
      left: positionLeft
    }, style);

    var arrowStyle = {
      top: arrowOffsetTop,
      left: arrowOffsetLeft
    };

    return _react2['default'].createElement(
      'div',
      (0, _extends4['default'])({}, elementProps, {
        role: 'tooltip',
        className: (0, _classnames2['default'])(className, classes),
        style: outerStyle
      }),
      _react2['default'].createElement('div', { className: 'arrow', style: arrowStyle }),
      title && _react2['default'].createElement(
        'h3',
        { className: (0, _bootstrapUtils.prefix)(bsProps, 'title') },
        title
      ),
      _react2['default'].createElement(
        'div',
        { className: (0, _bootstrapUtils.prefix)(bsProps, 'content') },
        children
      )
    );
  };

  return Popover;
}(_react2['default'].Component);

Popover.propTypes = propTypes;
Popover.defaultProps = defaultProps;

exports['default'] = (0, _bootstrapUtils.bsClass)('popover', Popover);
module.exports = exports['default'];

/***/ }),

/***/ 227:
/*!*********************************************************************************************!*\
  !*** /home/sven/ideaprojects/exceed/node_modules/prop-types-extra/lib/isRequiredForA11y.js ***!
  \*********************************************************************************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = isRequiredForA11y;
function isRequiredForA11y(validator) {
  return function validate(props, propName, componentName, location, propFullName) {
    var componentNameSafe = componentName || '<<anonymous>>';
    var propFullNameSafe = propFullName || propName;

    if (props[propName] == null) {
      return new Error('The ' + location + ' `' + propFullNameSafe + '` is required to make ' + ('`' + componentNameSafe + '` accessible for users of assistive ') + 'technologies such as screen readers.');
    }

    for (var _len = arguments.length, args = Array(_len > 5 ? _len - 5 : 0), _key = 5; _key < _len; _key++) {
      args[_key - 5] = arguments[_key];
    }

    return validator.apply(undefined, [props, propName, componentName, location, propFullName].concat(args));
  };
}
module.exports = exports['default'];

/***/ }),

/***/ 228:
/*!**********************************!*\
  !*** ./editor/gui/GUIElement.js ***!
  \**********************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
    value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = __webpack_require__(/*! react */ 0);

var _react2 = _interopRequireDefault(_react);

var _containerContext = __webpack_require__(/*! ./container-context */ 224);

var _containerContext2 = _interopRequireDefault(_containerContext);

var _guiContext = __webpack_require__(/*! ./gui-context */ 96);

var _guiContext2 = _interopRequireDefault(_guiContext);

var _uiState = __webpack_require__(/*! ./ui-state */ 97);

var _uiState2 = _interopRequireDefault(_uiState);

var _globalDrag = __webpack_require__(/*! ../../util/global-drag */ 225);

var _globalDrag2 = _interopRequireDefault(_globalDrag);

var _propTypes = __webpack_require__(/*! prop-types */ 2);

var _propTypes2 = _interopRequireDefault(_propTypes);

var _objectAssign = __webpack_require__(/*! object-assign */ 4);

var _objectAssign2 = _interopRequireDefault(_objectAssign);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

/**
 * SVGElement is a helper element for implementing SVG based user interface elements that can be interacted with by mouse and keyboard.
 *
 * It acts as a grouping wrapper for the graphical elements given as children and has no graphical representation of its own.
 */
var GUIElement = function (_React$Component) {
    _inherits(GUIElement, _React$Component);

    function GUIElement() {
        var _ref;

        var _temp, _this, _ret;

        _classCallCheck(this, GUIElement);

        for (var _len = arguments.length, args = Array(_len), _key = 0; _key < _len; _key++) {
            args[_key] = arguments[_key];
        }

        return _ret = (_temp = (_this = _possibleConstructorReturn(this, (_ref = GUIElement.__proto__ || Object.getPrototypeOf(GUIElement)).call.apply(_ref, [this].concat(args))), _this), _this.onMouseDown = function (ev) {
            var layout = _this.props.position;

            var x = _guiContext2.default.applyZoom(ev.pageX);
            var y = _guiContext2.default.applyZoom(ev.pageY);

            //console.log("START:" , layout.x, layout.y, "EVENT", x, y);

            // we use a simple offset system for dragging and dropping, we don't figure out the exact position of the mouse
            // cursor in object space, we just remember the offset after accounting for zoom and apply it later.
            _this.offsetX = x - layout.x;
            _this.offsetY = y - layout.y;

            _this.dragLocked = true;
            _globalDrag2.default.setActiveDrag(_this);

            ev.preventDefault();
            return false;
        }, _this.onMouseMove = function (x, y) {
            if (_globalDrag2.default.isActiveDrag(_this)) {
                var layout = _this.props.position;

                x = _guiContext2.default.applyZoom(x) - _this.offsetX;
                y = _guiContext2.default.applyZoom(y) - _this.offsetY;

                if (_this.dragLocked) {
                    var dx = layout.x - x;
                    var dy = layout.y - y;

                    if (Math.sqrt(dx * dx + dy * dy) > _this.props.dragThreshold) {
                        //console.log("ACTIVATE DRAG");
                        _this.dragLocked = false;
                    }
                }

                if (!_this.dragLocked && _this.props.draggable) {
                    //console.log("GUIElement move");
                    var copy = (0, _objectAssign2.default)({}, layout);
                    copy.x = x;
                    copy.y = y;
                    _this.props.updatePosition(copy, false);
                }
            }
        }, _temp), _possibleConstructorReturn(_this, _ret);
    }

    _createClass(GUIElement, [{
        key: "componentDidMount",
        value: function componentDidMount() {
            _guiContext2.default._register(this);
            // we use a global move listener, so that our dragged object doesn't act strange when the user managed to
            // move the cursor outside of the svg element or svg container.
            if (this.props.draggable) {
                _globalDrag2.default.init();
            }
        }
    }, {
        key: "componentWillUnmount",
        value: function componentWillUnmount() {
            _guiContext2.default._deregister(this);
            if (this.props.draggable) {
                _globalDrag2.default.destroy();
            }
        }
    }, {
        key: "componentWillUpdate",
        value: function componentWillUpdate(nextProps, nextState) {
            if (nextProps.uiState !== this.props.uiState) {
                _guiContext2.default._setElementState(this.props.id, nextProps.uiState, true);
            }
        }
    }, {
        key: "onMouseUp",


        /**
         * Called by GlobalDrag for every update in position for this GUIElement by dragging.
         *
         * @param screenX     x-coordinate in screen space
         * @param screenY     y-coordinate in screen space
         */
        value: function onMouseUp(screenX, screenY) {
            if (_globalDrag2.default.isActiveDrag(this)) {
                // uncorrected object space values
                var x = _guiContext2.default.applyZoom(screenX) - this.offsetX;
                var y = _guiContext2.default.applyZoom(screenY) - this.offsetY;

                _globalDrag2.default.setActiveDrag(null);

                //console.log("onMouseUp", link);
                var pos = this.props.position;

                if (this.dragLocked) {
                    var fn = this.props.onInteraction;
                    if (typeof fn == "function") {
                        var clickPos;
                        // does the onInteraction method expect a position argument?
                        if (fn.length === 1) {
                            // yep -> figure out the actual object space position of the click relative to
                            //        the current position
                            clickPos = this.context.containerContext.toObjectCoordinates(screenX, screenY);
                            clickPos.x -= pos.x;
                            clickPos.y -= pos.y;
                        }
                        fn(clickPos);
                    }
                } else if (this.props.draggable) {
                    // copy pos structure and update the coordinates
                    var copy = (0, _objectAssign2.default)({}, pos);
                    copy.x = x;
                    copy.y = y;
                    this.props.updatePosition(copy, true);
                }
            }
        }
    }, {
        key: "render",
        value: function render() {
            //console.log("Render GUI element " + this.props.id);
            return _react2.default.createElement(
                "g",
                {
                    onMouseOver: this.props.onMouseOver,
                    onMouseOut: this.props.onMouseOut,
                    onMouseDown: this.onMouseDown,
                    className: this.props.className,
                    style: this.props.style
                },
                this.props.children
            );
        }
    }]);

    return GUIElement;
}(_react2.default.Component);

GUIElement.defaultProps = {
    uiState: _uiState2.default.NORMAL,
    dragThreshold: 2,
    draggable: true
};
GUIElement.propTypes = {
    id: _propTypes2.default.string.isRequired,
    className: _propTypes2.default.string,
    style: _propTypes2.default.object,
    uiState: _propTypes2.default.oneOf(_uiState2.default.values()),
    onInteraction: _propTypes2.default.func,
    onUpdate: _propTypes2.default.func,
    draggable: _propTypes2.default.bool,
    onFocus: _propTypes2.default.func,
    data: _propTypes2.default.any,
    dragThreshold: _propTypes2.default.number
};
GUIElement.contextTypes = {
    containerContext: _propTypes2.default.instanceOf(_containerContext2.default)
};
exports.default = GUIElement;

/***/ }),

/***/ 229:
/*!*******************************!*\
  !*** ./util/DocumentClick.js ***!
  \*******************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
    value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

exports.default = function (Component) {
    return function (_React$Component) {
        _inherits(_class2, _React$Component);

        function _class2() {
            var _ref;

            var _temp, _this, _ret;

            _classCallCheck(this, _class2);

            for (var _len = arguments.length, args = Array(_len), _key = 0; _key < _len; _key++) {
                args[_key] = arguments[_key];
            }

            return _ret = (_temp = (_this = _possibleConstructorReturn(this, (_ref = _class2.__proto__ || Object.getPrototypeOf(_class2)).call.apply(_ref, [this].concat(args))), _this), _this.onDocumentClick = function (ev) {
                _this._component && _this._component.onDocumentClick();
            }, _this.assignComponent = function (c) {
                return _this._component = c;
            }, _temp), _possibleConstructorReturn(_this, _ret);
        }

        _createClass(_class2, [{
            key: "componentDidMount",
            value: function componentDidMount() {
                document.addEventListener("mousedown", this.onDocumentClick);
            }
        }, {
            key: "componentWillUnmount",
            value: function componentWillUnmount() {
                document.removeEventListener("mousedown", this.onDocumentClick);
            }
        }, {
            key: "render",
            value: function render() {
                return _react2.default.createElement(Component, _extends({
                    ref: this.assignComponent
                }, this.props));
            }
        }]);

        return _class2;
    }(_react2.default.Component);
};

var _react = __webpack_require__(/*! react */ 0);

var _react2 = _interopRequireDefault(_react);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

/***/ }),

/***/ 231:
/*!*******************************!*\
  !*** ./ui/PagingComponent.js ***!
  \*******************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
    value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = __webpack_require__(/*! react */ 0);

var _react2 = _interopRequireDefault(_react);

var _hasClass = __webpack_require__(/*! ../util/has-class */ 446);

var _hasClass2 = _interopRequireDefault(_hasClass);

var _classnames = __webpack_require__(/*! classnames */ 1);

var _classnames2 = _interopRequireDefault(_classnames);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var PagingLink = function (_React$Component) {
    _inherits(PagingLink, _React$Component);

    function PagingLink() {
        _classCallCheck(this, PagingLink);

        return _possibleConstructorReturn(this, (PagingLink.__proto__ || Object.getPrototypeOf(PagingLink)).apply(this, arguments));
    }

    _createClass(PagingLink, [{
        key: "onClick",
        value: function onClick(ev) {
            // check disabled class to make sure we're not executing a link in case the CSS pointer event rule
            // doesn't catch
            var classes = ev.target.className;

            if (!(0, _hasClass2.default)(classes, "disabled")) {
                //console.log("offsetLink", this.props.ctx.offsetLink, this.props.newOffset);
                this.props.ctx.offsetLink.requestChange(this.props.newOffset);
            }
            ev.preventDefault();
        }
    }, {
        key: "render",
        value: function render() {
            var _this2 = this;

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
                return _react2.default.createElement(
                    "span",
                    {
                        className: (0, _classnames2.default)({
                            "btn": true,
                            "btn-link": true,
                            "disabled": true,
                            "current": isCurrent
                        }) },
                    this.props.label
                );
            }

            return _react2.default.createElement(
                "a",
                {
                    onClick: function onClick(e) {
                        return _this2.onClick(e);
                    },
                    href: "#jump-to-" + newOffset,
                    className: (0, _classnames2.default)({
                        "btn": true,
                        "btn-link": true
                    }) },
                this.props.label
            );
        }
    }]);

    return PagingLink;
}(_react2.default.Component);

;

var PagingComponent = function (_React$Component2) {
    _inherits(PagingComponent, _React$Component2);

    function PagingComponent() {
        _classCallCheck(this, PagingComponent);

        return _possibleConstructorReturn(this, (PagingComponent.__proto__ || Object.getPrototypeOf(PagingComponent)).apply(this, arguments));
    }

    _createClass(PagingComponent, [{
        key: "render",
        value: function render() {
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

            var links = [_react2.default.createElement(PagingLink, { key: "first", newOffset: 0, label: "|<", ctx: ctx }), _react2.default.createElement(PagingLink, { key: "prev", newOffset: offset - limit, label: "<", ctx: ctx })];

            var page = void 0;
            var currentPage = (offset / limit | 0) + 1;
            for (var i = -2; i <= 2; i++) {
                page = currentPage + i;

                var newOffset = offset + i * limit;

                var label = page > 0 && newOffset >= 0 && newOffset <= max ? page : "\xA0";

                links.push(_react2.default.createElement(PagingLink, { key: i, newOffset: newOffset, label: label, ctx: ctx }));
            }

            links.push(_react2.default.createElement(PagingLink, { key: "next", newOffset: offset + limit, label: ">", ctx: ctx }), _react2.default.createElement(PagingLink, { key: "last", newOffset: max, label: ">|", ctx: ctx }));

            return _react2.default.createElement(
                "div",
                { className: "paging" },
                links
            );
        }
    }]);

    return PagingComponent;
}(_react2.default.Component);

exports.default = PagingComponent;

/***/ }),

/***/ 236:
/*!*********************!*\
  !*** ./app-main.js ***!
  \*********************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


var _reactDom = __webpack_require__(/*! react-dom */ 13);

var _reactDom2 = _interopRequireDefault(_reactDom);

var _es6PromisePolyfill = __webpack_require__(/*! es6-promise-polyfill */ 19);

var _startup = __webpack_require__(/*! ./util/startup */ 103);

var _reset = __webpack_require__(/*! ./actions/reset */ 75);

var _index = __webpack_require__(/*! ./reducers/index */ 23);

var _index2 = _interopRequireDefault(_index);

var _domready = __webpack_require__(/*! domready */ 173);

var _domready2 = _interopRequireDefault(_domready);

var _createStore = __webpack_require__(/*! ./create-store */ 174);

var _createStore2 = _interopRequireDefault(_createStore);

var _store = __webpack_require__(/*! ./service/store */ 3);

var _store2 = _interopRequireDefault(_store);

var _reducers = __webpack_require__(/*! ./reducers */ 23);

var _security = __webpack_require__(/*! ./service/security */ 62);

var _security2 = _interopRequireDefault(_security);

var _services = __webpack_require__(/*! ./services */ 176);

var _services2 = _interopRequireDefault(_services);

var _svgLayout = __webpack_require__(/*! ./gfx/svg-layout */ 95);

var _svgLayout2 = _interopRequireDefault(_svgLayout);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

//
// load our global-undo to patch MouseTrap
//
//noinspection JSUnusedLocalSymbols
var initialState = (0, _startup.evaluateEmbedded)("root-data", "x-ceed/view-data");
_store2.default._init(null, initialState);

//console.log("Initial state", initialState);

var store = (0, _createStore2.default)(_index2.default, initialState);
_store2.default._init(store, null);

var state = _store2.default.getState();

var sys = __webpack_require__(/*! ./sys */ 12);
var domainService = __webpack_require__(/*! ./service/domain */ 16);
var actionService = __webpack_require__(/*! ./service/action */ 82);

sys.init((0, _reducers.getContextPath)(state), (0, _reducers.getAppName)(state));
domainService.init((0, _reducers.getDomainData)(state));

var actions = (0, _reducers.getActionNames)(state);
//console.info("Server actions", actions);
actionService.initServerActions(actions);
actionService.registerFromRequireContext(__webpack_require__(/*! ./action/ */ 391));

var componentService = __webpack_require__(/*! ./service/component */ 48);
componentService.registerFromRequireContext(__webpack_require__(/*! ./components/std/ */ 400));

store.dispatch((0, _reset.hydrateAppState)(initialState));

var auth = (0, _reducers.getAuthentication)(initialState);
_security2.default.init(auth.userName, auth.roles);

var viewService = __webpack_require__(/*! ./service/view */ 177).default;

var appNavHistory = __webpack_require__(/*! ./service/app-history */ 106).default;

(0, _domready2.default)(function () {

    //console.log("DOMREADY");


    // set correct public path for dynamic module loading.
    var scriptResourcePath = sys.contextPath + "/res/" + sys.appName + "/js/";
    // noinspection JSUndeclaredVariable
    __webpack_require__.p = scriptResourcePath;

    // async setup
    _es6PromisePolyfill.Promise.all([_svgLayout2.default.init()]).then(function () {
        appNavHistory.init();
        appNavHistory.update(store.getState());

        var rootElem = document.getElementById("root");
        if (!rootElem) {
            throw new Error("Missing #root DOM element");
        }
        return new _es6PromisePolyfill.Promise(function (resolve, reject) {
            _reactDom2.default.hydrate(viewService.render(store), rootElem, resolve);
        });
    }).then(function () {
        var scripts = (0, _startup.findBundles)(scriptResourcePath);
        console.info("READY: Loaded " + scripts.join(", "), "( " + new Date().toISOString() + " )");
    });
    // .catch(function (e)
    // {
    //     console.error(e);
    // });
});

// This export will be available as "Exceed" in the browser environment of the runnign application
// traditional export to not have a .default in the browser env
// noinspection JSUnusedGlobalSymbols
module.exports = _services2.default;

/***/ }),

/***/ 391:
/*!**********************!*\
  !*** ./action \.js$ ***!
  \**********************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

var map = {
	"./alert.js": 392,
	"./navigateTo.js": 393,
	"./ping.js": 394,
	"./sleep.js": 395,
	"./store.js": 396,
	"./syslog.js": 397,
	"./transition.js": 398,
	"./when.js": 399
};
function webpackContext(req) {
	return __webpack_require__(webpackContextResolve(req));
};
function webpackContextResolve(req) {
	var id = map[req];
	if(!(id + 1)) // check for number or string
		throw new Error("Cannot find module '" + req + "'.");
	return id;
};
webpackContext.keys = function webpackContextKeys() {
	return Object.keys(map);
};
webpackContext.resolve = webpackContextResolve;
module.exports = webpackContext;
webpackContext.id = 391;

/***/ }),

/***/ 392:
/*!*************************!*\
  !*** ./action/alert.js ***!
  \*************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
    value: true
});

var _typeof = typeof Symbol === "function" && typeof Symbol.iterator === "symbol" ? function (obj) { return typeof obj; } : function (obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; };

exports.default = function (text) {
    if (text instanceof _cursor2.default) {
        text = text.get();
    }

    if ((typeof text === "undefined" ? "undefined" : _typeof(text)) === "object") {
        text = JSON.stringify(text, null, 4);
    }

    return _dialog2.default.alert(String(text));
};

var _dialog = __webpack_require__(/*! ../util/dialog */ 120);

var _dialog2 = _interopRequireDefault(_dialog);

var _cursor = __webpack_require__(/*! ../domain/cursor */ 27);

var _cursor2 = _interopRequireDefault(_cursor);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

/***/ }),

/***/ 393:
/*!******************************!*\
  !*** ./action/navigateTo.js ***!
  \******************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
    value: true
});

exports.default = function (url) {
    _store2.default.dispatch((0, _view.navigateView)({
        url: (0, _uri2.default)(url)
    }));
};

var _store = __webpack_require__(/*! ../service/store */ 3);

var _store2 = _interopRequireDefault(_store);

var _view = __webpack_require__(/*! ../actions/view */ 24);

var _uri = __webpack_require__(/*! ../util/uri */ 15);

var _uri2 = _interopRequireDefault(_uri);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

/***/ }),

/***/ 394:
/*!************************!*\
  !*** ./action/ping.js ***!
  \************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
    value: true
});
var _exports = function _exports(model, data) {
    //    console.log("ping", model, data);
    return data;
};

_exports.catch = function (e, actionModel, data) {
    //console.log("client-side error handling", e, actionModel, data);
    return Promise.resolve({ value: -100 });
};

exports.default = _exports;

/***/ }),

/***/ 395:
/*!*************************!*\
  !*** ./action/sleep.js ***!
  \*************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
    value: true
});

exports.default = function (model) {
    return new Promise(function (resolve, reject) {
        //        console.log("SLEEP", model);

        window.setTimeout(resolve, (model.time || 3) * 1000);
    });
};

var Promise = __webpack_require__(/*! es6-promise-polyfill */ 19).Promise;

/***/ }),

/***/ 396:
/*!*************************!*\
  !*** ./action/store.js ***!
  \*************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
    value: true
});

exports.default = function (model) {
    //    console.log("STORE", model);

    if (!model) {
        throw new Error("No model");
    }

    if (!model.object) {
        throw new Error("No data");
    }

    var data = model.object;

    if (data instanceof _cursor2.default) {
        model.object = data.extractObjects();
    } else if (data._type) {
        model.object = data;
    } else {
        throw new Error("Cannot store unknown object" + model);
    }

    return _action2.default.execute(model, true);
};

var _cursor = __webpack_require__(/*! ../domain/cursor */ 27);

var _cursor2 = _interopRequireDefault(_cursor);

var _action = __webpack_require__(/*! ../service/action */ 82);

var _action2 = _interopRequireDefault(_action);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

/***/ }),

/***/ 397:
/*!**************************!*\
  !*** ./action/syslog.js ***!
  \**************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
    value: true
});

exports.default = function () {
    console.info.apply(console, arguments);
};

/***/ }),

/***/ 398:
/*!******************************!*\
  !*** ./action/transition.js ***!
  \******************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
    value: true
});

exports.default = function (name, context) {
    var objects = null;
    if (context) {
        if (context.isProperty()) {
            context = context.pop();
        }
        objects = context.extractObjects();
    }

    return _process2.default.transition(name, objects).catch(function (err) {
        console.error(err);
    });
};

var _process = __webpack_require__(/*! ../service/process */ 144);

var _process2 = _interopRequireDefault(_process);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

/***/ }),

/***/ 399:
/*!************************!*\
  !*** ./action/when.js ***!
  \************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
  value: true
});

exports.default = function (cond) {
  return cond ? _es6PromisePolyfill.Promise.resolve(true) : _es6PromisePolyfill.Promise.reject(false);
};

var _es6PromisePolyfill = __webpack_require__(/*! es6-promise-polyfill */ 19);

/***/ }),

/***/ 400:
/*!***********************************!*\
  !*** ./components/std \.js(on)?$ ***!
  \***********************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

var map = {
	"./bootstrap/Bootstrap.js": 401,
	"./bootstrap/Col.js": 402,
	"./bootstrap/Grid.js": 404,
	"./bootstrap/Row.js": 405,
	"./bootstrap/components.json": 407,
	"./chart/BarChart.js": 408,
	"./chart/RadarChart.js": 411,
	"./chart/components.json": 412,
	"./common/Content.js": 413,
	"./common/Dialog.js": 414,
	"./common/Error.js": 415,
	"./common/Footer.js": 416,
	"./common/GlyphButton.js": 216,
	"./common/Heading.js": 417,
	"./common/InfoBlock.js": 418,
	"./common/Link.js": 419,
	"./common/ResourceImage.js": 420,
	"./common/StandardNav.js": 421,
	"./common/Test.js": 441,
	"./common/components.json": 442,
	"./counter/Counter.js": 443,
	"./counter/components.json": 444,
	"./datagrid/DataGrid.js": 445,
	"./datagrid/DataGridColumnWizard.js": 447,
	"./datagrid/DataGridTypeWizard.js": 448,
	"./datagrid/components.json": 449,
	"./form/Button.js": 450,
	"./form/CalendarField.js": 215,
	"./form/Checkbox.js": 214,
	"./form/ErrorMessages.js": 451,
	"./form/FKSelect.js": 452,
	"./form/Field.js": 137,
	"./form/Form.js": 232,
	"./form/FormBlock.js": 453,
	"./form/FormElement.js": 55,
	"./form/ListEditor.js": 454,
	"./form/ManyToMany.js": 455,
	"./form/Options.js": 456,
	"./form/PropertySelect.js": 138,
	"./form/SelectField.js": 210,
	"./form/StateMachineButtons.js": 457,
	"./form/StaticText.js": 233,
	"./form/TButton.js": 458,
	"./form/Toolbar.js": 89,
	"./form/components.json": 459,
	"./login/LoginForm.js": 460,
	"./login/components.json": 461,
	"./navigation/Navbar.js": 462,
	"./navigation/components.json": 463,
	"./shim/ShopAddress.js": 464,
	"./shim/ShopNav.js": 465,
	"./shim/components.json": 466
};
function webpackContext(req) {
	return __webpack_require__(webpackContextResolve(req));
};
function webpackContextResolve(req) {
	var id = map[req];
	if(!(id + 1)) // check for number or string
		throw new Error("Cannot find module '" + req + "'.");
	return id;
};
webpackContext.keys = function webpackContextKeys() {
	return Object.keys(map);
};
webpackContext.resolve = webpackContextResolve;
module.exports = webpackContext;
webpackContext.id = 400;

/***/ }),

/***/ 401:
/*!***********************************************!*\
  !*** ./components/std/bootstrap/Bootstrap.js ***!
  \***********************************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


/**
 * Module to draw in the components of react-bootstrap into our component system.
 *
 * @type {{Accordion: (exports|module.exports), Alert: (exports|module.exports), Badge: (exports|module.exports), BreadcrumbItem: (exports|module.exports), Breadcrumb: (exports|module.exports), ButtonGroup: (exports|module.exports), ButtonInput: (exports|module.exports), Button: (exports|module.exports), ButtonToolbar: (exports|module.exports), CarouselItem: (exports|module.exports), Carousel: (exports|module.exports), Col: (exports|module.exports), Collapse: (exports|module.exports), CollapsibleNav: (exports|module.exports), DropdownButton: (exports|module.exports), Dropdown: (exports|module.exports), DropdownMenu: (exports|module.exports), DropdownToggle: (exports|module.exports), Fade: (exports|module.exports), FormGroup: (exports|module.exports), Glyphicon: (exports|module.exports), Grid: (exports|module.exports), Image: (exports|module.exports), index: (exports|module.exports), InputBase: (exports|module.exports), Input: (exports|module.exports), Interpolate: (exports|module.exports), Jumbotron: (exports|module.exports), Label: (exports|module.exports), ListGroupItem: (exports|module.exports), ListGroup: (exports|module.exports), MenuItem: (exports|module.exports), ModalBody: (exports|module.exports), ModalDialog: (exports|module.exports), ModalFooter: (exports|module.exports), ModalHeader: (exports|module.exports), Modal: (exports|module.exports), ModalTitle: (exports|module.exports), NavbarBrand: (exports|module.exports), NavbarCollapse: (exports|module.exports), NavbarHeader: (exports|module.exports), Navbar: (exports|module.exports), NavbarToggle: (exports|module.exports), NavBrand: (exports|module.exports), NavDropdown: (exports|module.exports), NavItem: (exports|module.exports), Nav: (exports|module.exports), Overlay: (exports|module.exports), OverlayTrigger: (exports|module.exports), PageHeader: (exports|module.exports), PageItem: (exports|module.exports), Pager: (exports|module.exports), PaginationButton: (exports|module.exports), Pagination: (exports|module.exports), PanelGroup: (exports|module.exports), Panel: (exports|module.exports), Popover: (exports|module.exports), ProgressBar: (exports|module.exports), ResponsiveEmbed: (exports|module.exports), Row: (exports|module.exports), SafeAnchor: (exports|module.exports), SplitButton: (exports|module.exports), SplitToggle: (exports|module.exports), styleMaps: (exports|module.exports), Tab: (exports|module.exports), Table: (exports|module.exports), Tabs: (exports|module.exports), Thumbnail: (exports|module.exports), Tooltip: (exports|module.exports), Well: (exports|module.exports)}}
 */
module.exports = {
    /*
    Accordion: require('react-bootstrap/lib/Accordion'),
    Alert: require('react-bootstrap/lib/Alert'),
    Badge: require('react-bootstrap/lib/Badge'),
    BreadcrumbItem: require('react-bootstrap/lib/BreadcrumbItem'),
    Breadcrumb: require('react-bootstrap/lib/Breadcrumb'),
    ButtonGroup: require('react-bootstrap/lib/ButtonGroup'),
    ButtonInput: require('react-bootstrap/lib/ButtonInput'),
    Button: require('react-bootstrap/lib/Button'),
    ButtonToolbar: require('react-bootstrap/lib/ButtonToolbar'),
    CarouselItem: require('react-bootstrap/lib/CarouselItem'),
    Carousel: require('react-bootstrap/lib/Carousel'),
    Col: require('react-bootstrap/lib/Col'),
    Collapse: require('react-bootstrap/lib/Collapse'),
    CollapsibleNav: require('react-bootstrap/lib/CollapsibleNav'),
    DropdownButton: require('react-bootstrap/lib/DropdownButton'),
    Dropdown: require('react-bootstrap/lib/Dropdown'),
    DropdownMenu: require('react-bootstrap/lib/DropdownMenu'),
    DropdownToggle: require('react-bootstrap/lib/DropdownToggle'),
    Fade: require('react-bootstrap/lib/Fade'),
    FormGroup: require('react-bootstrap/lib/FormGroup'),
    Glyphicon: require('react-bootstrap/lib/Glyphicon'),
    Grid: require('react-bootstrap/lib/Grid'),
    Image: require('react-bootstrap/lib/Image'),
    index: require('react-bootstrap/lib/index'),
    InputBase: require('react-bootstrap/lib/InputBase'),
    Input: require('react-bootstrap/lib/Input'),
    Interpolate: require('react-bootstrap/lib/Interpolate'),
    Jumbotron: require('react-bootstrap/lib/Jumbotron'),
    Label: require('react-bootstrap/lib/Label'),
    ListGroupItem: require('react-bootstrap/lib/ListGroupItem'),
    ListGroup: require('react-bootstrap/lib/ListGroup'),
    MenuItem: require('react-bootstrap/lib/MenuItem'),
    ModalBody: require('react-bootstrap/lib/ModalBody'),
    ModalDialog: require('react-bootstrap/lib/ModalDialog'),
    ModalFooter: require('react-bootstrap/lib/ModalFooter'),
    ModalHeader: require('react-bootstrap/lib/ModalHeader'),
    Modal: require('react-bootstrap/lib/Modal'),
    ModalTitle: require('react-bootstrap/lib/ModalTitle'),
    NavbarBrand: require('react-bootstrap/lib/NavbarBrand'),
    NavbarCollapse: require('react-bootstrap/lib/NavbarCollapse'),
    NavbarHeader: require('react-bootstrap/lib/NavbarHeader'),
    Navbar: require('react-bootstrap/lib/Navbar'),
    NavbarToggle: require('react-bootstrap/lib/NavbarToggle'),
    NavBrand: require('react-bootstrap/lib/NavBrand'),
    NavDropdown: require('react-bootstrap/lib/NavDropdown'),
    NavItem: require('react-bootstrap/lib/NavItem'),
    Nav: require('react-bootstrap/lib/Nav'),
    Overlay: require('react-bootstrap/lib/Overlay'),
    OverlayTrigger: require('react-bootstrap/lib/OverlayTrigger'),
    PageHeader: require('react-bootstrap/lib/PageHeader'),
    PageItem: require('react-bootstrap/lib/PageItem'),
    Pager: require('react-bootstrap/lib/Pager'),
    PaginationButton: require('react-bootstrap/lib/PaginationButton'),
    Pagination: require('react-bootstrap/lib/Pagination'),
    PanelGroup: require('react-bootstrap/lib/PanelGroup'),
    Panel: require('react-bootstrap/lib/Panel'),
    Popover: require('react-bootstrap/lib/Popover'),
    ProgressBar: require('react-bootstrap/lib/ProgressBar'),
    ResponsiveEmbed: require('react-bootstrap/lib/ResponsiveEmbed'),
    Row: require('react-bootstrap/lib/Row'),
    SafeAnchor: require('react-bootstrap/lib/SafeAnchor'),
    SplitButton: require('react-bootstrap/lib/SplitButton'),
    SplitToggle: require('react-bootstrap/lib/SplitToggle'),
    styleMaps: require('react-bootstrap/lib/styleMaps'),
    Tab: require('react-bootstrap/lib/Tab'),
    Table: require('react-bootstrap/lib/Table'),
    Tabs: require('react-bootstrap/lib/Tabs'),
    Thumbnail: require('react-bootstrap/lib/Thumbnail'),
    Tooltip: require('react-bootstrap/lib/Tooltip'),
    Well: require('react-bootstrap/lib/Well')
    */
};

/***/ }),

/***/ 402:
/*!*****************************************!*\
  !*** ./components/std/bootstrap/Col.js ***!
  \*****************************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


// integration module
module.exports = __webpack_require__(/*! react-bootstrap/lib/Col */ 403);

/***/ }),

/***/ 403:
/*!******************************************************************************!*\
  !*** /home/sven/ideaprojects/exceed/node_modules/react-bootstrap/lib/Col.js ***!
  \******************************************************************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


exports.__esModule = true;

var _extends2 = __webpack_require__(/*! babel-runtime/helpers/extends */ 8);

var _extends3 = _interopRequireDefault(_extends2);

var _objectWithoutProperties2 = __webpack_require__(/*! babel-runtime/helpers/objectWithoutProperties */ 10);

var _objectWithoutProperties3 = _interopRequireDefault(_objectWithoutProperties2);

var _classCallCheck2 = __webpack_require__(/*! babel-runtime/helpers/classCallCheck */ 5);

var _classCallCheck3 = _interopRequireDefault(_classCallCheck2);

var _possibleConstructorReturn2 = __webpack_require__(/*! babel-runtime/helpers/possibleConstructorReturn */ 6);

var _possibleConstructorReturn3 = _interopRequireDefault(_possibleConstructorReturn2);

var _inherits2 = __webpack_require__(/*! babel-runtime/helpers/inherits */ 7);

var _inherits3 = _interopRequireDefault(_inherits2);

var _classnames = __webpack_require__(/*! classnames */ 1);

var _classnames2 = _interopRequireDefault(_classnames);

var _react = __webpack_require__(/*! react */ 0);

var _react2 = _interopRequireDefault(_react);

var _propTypes = __webpack_require__(/*! prop-types */ 2);

var _propTypes2 = _interopRequireDefault(_propTypes);

var _elementType = __webpack_require__(/*! prop-types-extra/lib/elementType */ 17);

var _elementType2 = _interopRequireDefault(_elementType);

var _bootstrapUtils = __webpack_require__(/*! ./utils/bootstrapUtils */ 11);

var _StyleConfig = __webpack_require__(/*! ./utils/StyleConfig */ 54);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { 'default': obj }; }

var propTypes = {
  componentClass: _elementType2['default'],

  /**
   * The number of columns you wish to span
   *
   * for Extra small devices Phones (<768px)
   *
   * class-prefix `col-xs-`
   */
  xs: _propTypes2['default'].number,
  /**
   * The number of columns you wish to span
   *
   * for Small devices Tablets (≥768px)
   *
   * class-prefix `col-sm-`
   */
  sm: _propTypes2['default'].number,
  /**
   * The number of columns you wish to span
   *
   * for Medium devices Desktops (≥992px)
   *
   * class-prefix `col-md-`
   */
  md: _propTypes2['default'].number,
  /**
   * The number of columns you wish to span
   *
   * for Large devices Desktops (≥1200px)
   *
   * class-prefix `col-lg-`
   */
  lg: _propTypes2['default'].number,
  /**
   * Hide column
   *
   * on Extra small devices Phones
   *
   * adds class `hidden-xs`
   */
  xsHidden: _propTypes2['default'].bool,
  /**
   * Hide column
   *
   * on Small devices Tablets
   *
   * adds class `hidden-sm`
   */
  smHidden: _propTypes2['default'].bool,
  /**
   * Hide column
   *
   * on Medium devices Desktops
   *
   * adds class `hidden-md`
   */
  mdHidden: _propTypes2['default'].bool,
  /**
   * Hide column
   *
   * on Large devices Desktops
   *
   * adds class `hidden-lg`
   */
  lgHidden: _propTypes2['default'].bool,
  /**
   * Move columns to the right
   *
   * for Extra small devices Phones
   *
   * class-prefix `col-xs-offset-`
   */
  xsOffset: _propTypes2['default'].number,
  /**
   * Move columns to the right
   *
   * for Small devices Tablets
   *
   * class-prefix `col-sm-offset-`
   */
  smOffset: _propTypes2['default'].number,
  /**
   * Move columns to the right
   *
   * for Medium devices Desktops
   *
   * class-prefix `col-md-offset-`
   */
  mdOffset: _propTypes2['default'].number,
  /**
   * Move columns to the right
   *
   * for Large devices Desktops
   *
   * class-prefix `col-lg-offset-`
   */
  lgOffset: _propTypes2['default'].number,
  /**
   * Change the order of grid columns to the right
   *
   * for Extra small devices Phones
   *
   * class-prefix `col-xs-push-`
   */
  xsPush: _propTypes2['default'].number,
  /**
   * Change the order of grid columns to the right
   *
   * for Small devices Tablets
   *
   * class-prefix `col-sm-push-`
   */
  smPush: _propTypes2['default'].number,
  /**
   * Change the order of grid columns to the right
   *
   * for Medium devices Desktops
   *
   * class-prefix `col-md-push-`
   */
  mdPush: _propTypes2['default'].number,
  /**
   * Change the order of grid columns to the right
   *
   * for Large devices Desktops
   *
   * class-prefix `col-lg-push-`
   */
  lgPush: _propTypes2['default'].number,
  /**
   * Change the order of grid columns to the left
   *
   * for Extra small devices Phones
   *
   * class-prefix `col-xs-pull-`
   */
  xsPull: _propTypes2['default'].number,
  /**
   * Change the order of grid columns to the left
   *
   * for Small devices Tablets
   *
   * class-prefix `col-sm-pull-`
   */
  smPull: _propTypes2['default'].number,
  /**
   * Change the order of grid columns to the left
   *
   * for Medium devices Desktops
   *
   * class-prefix `col-md-pull-`
   */
  mdPull: _propTypes2['default'].number,
  /**
   * Change the order of grid columns to the left
   *
   * for Large devices Desktops
   *
   * class-prefix `col-lg-pull-`
   */
  lgPull: _propTypes2['default'].number
};

var defaultProps = {
  componentClass: 'div'
};

var Col = function (_React$Component) {
  (0, _inherits3['default'])(Col, _React$Component);

  function Col() {
    (0, _classCallCheck3['default'])(this, Col);
    return (0, _possibleConstructorReturn3['default'])(this, _React$Component.apply(this, arguments));
  }

  Col.prototype.render = function render() {
    var _props = this.props,
        Component = _props.componentClass,
        className = _props.className,
        props = (0, _objectWithoutProperties3['default'])(_props, ['componentClass', 'className']);

    var _splitBsProps = (0, _bootstrapUtils.splitBsProps)(props),
        bsProps = _splitBsProps[0],
        elementProps = _splitBsProps[1];

    var classes = [];

    _StyleConfig.DEVICE_SIZES.forEach(function (size) {
      function popProp(propSuffix, modifier) {
        var propName = '' + size + propSuffix;
        var propValue = elementProps[propName];

        if (propValue != null) {
          classes.push((0, _bootstrapUtils.prefix)(bsProps, '' + size + modifier + '-' + propValue));
        }

        delete elementProps[propName];
      }

      popProp('', '');
      popProp('Offset', '-offset');
      popProp('Push', '-push');
      popProp('Pull', '-pull');

      var hiddenPropName = size + 'Hidden';
      if (elementProps[hiddenPropName]) {
        classes.push('hidden-' + size);
      }
      delete elementProps[hiddenPropName];
    });

    return _react2['default'].createElement(Component, (0, _extends3['default'])({}, elementProps, {
      className: (0, _classnames2['default'])(className, classes)
    }));
  };

  return Col;
}(_react2['default'].Component);

Col.propTypes = propTypes;
Col.defaultProps = defaultProps;

exports['default'] = (0, _bootstrapUtils.bsClass)('col', Col);
module.exports = exports['default'];

/***/ }),

/***/ 404:
/*!******************************************!*\
  !*** ./components/std/bootstrap/Grid.js ***!
  \******************************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


// integration module
module.exports = __webpack_require__(/*! react-bootstrap/lib/Grid */ 221);

/***/ }),

/***/ 405:
/*!*****************************************!*\
  !*** ./components/std/bootstrap/Row.js ***!
  \*****************************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


// integration module
module.exports = __webpack_require__(/*! react-bootstrap/lib/Row */ 406);

/***/ }),

/***/ 406:
/*!******************************************************************************!*\
  !*** /home/sven/ideaprojects/exceed/node_modules/react-bootstrap/lib/Row.js ***!
  \******************************************************************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


exports.__esModule = true;

var _extends2 = __webpack_require__(/*! babel-runtime/helpers/extends */ 8);

var _extends3 = _interopRequireDefault(_extends2);

var _objectWithoutProperties2 = __webpack_require__(/*! babel-runtime/helpers/objectWithoutProperties */ 10);

var _objectWithoutProperties3 = _interopRequireDefault(_objectWithoutProperties2);

var _classCallCheck2 = __webpack_require__(/*! babel-runtime/helpers/classCallCheck */ 5);

var _classCallCheck3 = _interopRequireDefault(_classCallCheck2);

var _possibleConstructorReturn2 = __webpack_require__(/*! babel-runtime/helpers/possibleConstructorReturn */ 6);

var _possibleConstructorReturn3 = _interopRequireDefault(_possibleConstructorReturn2);

var _inherits2 = __webpack_require__(/*! babel-runtime/helpers/inherits */ 7);

var _inherits3 = _interopRequireDefault(_inherits2);

var _classnames = __webpack_require__(/*! classnames */ 1);

var _classnames2 = _interopRequireDefault(_classnames);

var _react = __webpack_require__(/*! react */ 0);

var _react2 = _interopRequireDefault(_react);

var _elementType = __webpack_require__(/*! prop-types-extra/lib/elementType */ 17);

var _elementType2 = _interopRequireDefault(_elementType);

var _bootstrapUtils = __webpack_require__(/*! ./utils/bootstrapUtils */ 11);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { 'default': obj }; }

var propTypes = {
  componentClass: _elementType2['default']
};

var defaultProps = {
  componentClass: 'div'
};

var Row = function (_React$Component) {
  (0, _inherits3['default'])(Row, _React$Component);

  function Row() {
    (0, _classCallCheck3['default'])(this, Row);
    return (0, _possibleConstructorReturn3['default'])(this, _React$Component.apply(this, arguments));
  }

  Row.prototype.render = function render() {
    var _props = this.props,
        Component = _props.componentClass,
        className = _props.className,
        props = (0, _objectWithoutProperties3['default'])(_props, ['componentClass', 'className']);

    var _splitBsProps = (0, _bootstrapUtils.splitBsProps)(props),
        bsProps = _splitBsProps[0],
        elementProps = _splitBsProps[1];

    var classes = (0, _bootstrapUtils.getClassSet)(bsProps);

    return _react2['default'].createElement(Component, (0, _extends3['default'])({}, elementProps, {
      className: (0, _classnames2['default'])(className, classes)
    }));
  };

  return Row;
}(_react2['default'].Component);

Row.propTypes = propTypes;
Row.defaultProps = defaultProps;

exports['default'] = (0, _bootstrapUtils.bsClass)('row', Row);
module.exports = exports['default'];

/***/ }),

/***/ 407:
/*!**************************************************!*\
  !*** ./components/std/bootstrap/components.json ***!
  \**************************************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports) {

module.exports = {"components":{"Grid":{"description":"React-bootstrap Grid component. See <a href=\"https://react-bootstrap.github.io/components.html#grid\">react-bootstrap documentation</a>","classes":["view-child"],"childRule":"component('Row')","templates":[{"desc":"Default Grid","model":{"name":"Grid","attrs":{"fluid":"{ true }"},"kids":[{"name":"Row","kids":[{"name":"Col","attrs":{"md":"{ 12 }"}}]}]}}]},"Row":{"description":"React-bootstrap Row component. See <a href=\"https://react-bootstrap.github.io/components.html#grid\">react-bootstrap documentation</a>","childRule":"component('Col')"},"Col":{"description":"React-bootstrap Col component. See <a href=\"https://react-bootstrap.github.io/components.html#grid\">react-bootstrap documentation</a>","childRule":"hasClass('element')"}}}

/***/ }),

/***/ 408:
/*!******************************************!*\
  !*** ./components/std/chart/BarChart.js ***!
  \******************************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
    value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = __webpack_require__(/*! react */ 0);

var _react2 = _interopRequireDefault(_react);

var _classnames = __webpack_require__(/*! classnames */ 1);

var _classnames2 = _interopRequireDefault(_classnames);

var _propertyConverter = __webpack_require__(/*! ../../../service/property-converter */ 46);

var _propertyConverter2 = _interopRequireDefault(_propertyConverter);

var _propertyRenderer = __webpack_require__(/*! ../../../service/property-renderer */ 61);

var _propertyRenderer2 = _interopRequireDefault(_propertyRenderer);

var _svgLayout = __webpack_require__(/*! ../../../gfx/svg-layout */ 95);

var _svgLayout2 = _interopRequireDefault(_svgLayout);

var _GUIContainer = __webpack_require__(/*! ../../../editor/gui/GUIContainer */ 223);

var _GUIContainer2 = _interopRequireDefault(_GUIContainer);

var _guiContext = __webpack_require__(/*! ../../../editor/gui/gui-context */ 96);

var _guiContext2 = _interopRequireDefault(_guiContext);

var _Popover = __webpack_require__(/*! react-bootstrap/lib/Popover */ 226);

var _Popover2 = _interopRequireDefault(_Popover);

var _GUIElement = __webpack_require__(/*! ../../../editor/gui/GUIElement */ 228);

var _GUIElement2 = _interopRequireDefault(_GUIElement);

var _uiState = __webpack_require__(/*! ../../../editor/gui/ui-state */ 97);

var _uiState2 = _interopRequireDefault(_uiState);

var _DocumentClick = __webpack_require__(/*! ../../../util/DocumentClick */ 229);

var _DocumentClick2 = _interopRequireDefault(_DocumentClick);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var TextSize = _svgLayout2.default.TextSize;


var SIZE_LABEL = TextSize.NORMAL;

// lazy initialized from constants above
var LAYOUT_LABEL = void 0;
var FONT_SIZE_LABEL = void 0;

var TAU = Math.PI * 2;
var TO_DEGREE_FACTOR = 360 / TAU;

function initLayout() {
    var exampleTextSizes = _svgLayout2.default.getExampleTextSizes();

    LAYOUT_LABEL = exampleTextSizes[SIZE_LABEL];

    FONT_SIZE_LABEL = _svgLayout2.default.getFontSize(SIZE_LABEL);
}

function CartesianGrid(props) {
    var chartConfig = props.chartConfig;
    var x = chartConfig.x,
        y = chartConfig.y,
        width = chartConfig.width,
        height = chartConfig.height,
        max = chartConfig.max,
        propertyType = chartConfig.propertyType,
        round = chartConfig.round;


    var scale = height / max;

    var rounded = Math.floor(max / round) * round;

    var maxLabel = _propertyConverter2.default.toUser(rounded, propertyType);

    var x2 = x + width;
    var y2 = y + height;

    var scaleMarkers = [];

    var current = rounded;
    while (current > 0) {
        var markerY = y + height - current * scale;
        scaleMarkers.push(_react2.default.createElement("path", { key: current, d: "M" + x + "," + markerY + " L" + x2 + "," + markerY }));

        if (current === rounded) {
            scaleMarkers.push(_react2.default.createElement(
                "text",
                { key: "label", x: x + 8, y: markerY + LAYOUT_LABEL.height / 5, fontSize: FONT_SIZE_LABEL },
                maxLabel
            ));
        }

        current -= round;
    }

    return _react2.default.createElement(
        "g",
        { className: "chart-grid" },
        _react2.default.createElement("path", { d: "M" + x + "," + y2 + " L" + (x2 + 25) + "," + y2 }),
        _react2.default.createElement("path", { d: "M" + x + "," + (y - 25) + " L" + x + "," + y2 }),
        scaleMarkers
    );
}

var Series = function (_React$Component) {
    _inherits(Series, _React$Component);

    function Series() {
        var _ref;

        var _temp, _this, _ret;

        _classCallCheck(this, Series);

        for (var _len = arguments.length, args = Array(_len), _key = 0; _key < _len; _key++) {
            args[_key] = arguments[_key];
        }

        return _ret = (_temp = (_this = _possibleConstructorReturn(this, (_ref = Series.__proto__ || Object.getPrototypeOf(Series)).call.apply(_ref, [this].concat(args))), _this), _this.onUpdate = function () {
            return _this.forceUpdate();
        }, _temp), _possibleConstructorReturn(_this, _ret);
    }

    _createClass(Series, [{
        key: "render",
        value: function render() {
            var _this2 = this;

            var _props = this.props,
                chartConfig = _props.chartConfig,
                query = _props.query,
                onFocus = _props.onFocus;
            var x = chartConfig.x,
                y = chartConfig.y,
                width = chartConfig.width,
                height = chartConfig.height,
                max = chartConfig.max;
            var data = query.data;


            var step = width / data.rootObject.length;
            var barWidth = step * 0.95 | 0;

            var array = query.data.rootObject;

            var scale = height / max;

            return _react2.default.createElement(
                "g",
                { className: "graph-series" },
                array.map(function (row, idx) {

                    var v = row.value * scale;

                    var bx = x + idx * step;
                    var by = y + height - v;

                    var id = query.className + "-" + idx;

                    var uiState = _guiContext2.default.getElementState(id, _uiState2.default.NORMAL);

                    return _react2.default.createElement(
                        _GUIElement2.default,
                        {
                            key: id,
                            id: id,
                            position: { x: bx, y: by },
                            className: (0, _classnames2.default)("bar", uiState.toLowerCase()),
                            draggable: false,
                            onUpdate: _this2.onUpdate,
                            onInteraction: function onInteraction() {
                                return onFocus(query, idx);
                            }
                        },
                        _react2.default.createElement("rect", {
                            x: bx,
                            y: by,
                            width: barWidth,
                            height: v
                        }),
                        _react2.default.createElement(
                            "text",
                            { x: bx, y: y + height + LAYOUT_LABEL.height, fontSize: FONT_SIZE_LABEL },
                            row.label
                        )
                    );
                })
            );
        }
    }]);

    return Series;
}(_react2.default.Component);

function findMaximum(queries) {
    var max = -Infinity;
    for (var i = 0; i < queries.length; i++) {
        var query = queries[i];
        var array = query.data.rootObject;

        for (var _i = 0; _i < array.length; _i++) {
            var row = array[_i];
            max = Math.max(row.value, max);
        }
    }

    return max;
}

function InfoWindow(props) {
    var query = props.query,
        index = props.index,
        chartConfig = props.chartConfig;
    var x = chartConfig.x,
        y = chartConfig.y,
        width = chartConfig.width,
        height = chartConfig.height,
        max = chartConfig.max,
        propertyType = chartConfig.propertyType,
        chartId = chartConfig.chartId;
    var data = query.data;

    var step = width / data.rootObject.length;

    var scale = height / max;

    var row = data.rootObject[index];
    var label = row.label,
        value = row.value;

    return _react2.default.createElement(
        _Popover2.default,
        {
            id: chartId + "-info",
            placement: "top",
            positionLeft: x + step * index - 44,
            positionTop: height - value * scale,
            title: query.name
        },
        label,
        " ",
        _react2.default.createElement("br", null),
        "Value: ",
        _propertyRenderer2.default.renderStatic(value, propertyType)
    );
}

var BarChart = (0, _DocumentClick2.default)(function (_React$Component2) {
    _inherits(_class2, _React$Component2);

    function _class2() {
        var _ref2;

        var _temp2, _this3, _ret2;

        _classCallCheck(this, _class2);

        for (var _len2 = arguments.length, args = Array(_len2), _key2 = 0; _key2 < _len2; _key2++) {
            args[_key2] = arguments[_key2];
        }

        return _ret2 = (_temp2 = (_this3 = _possibleConstructorReturn(this, (_ref2 = _class2.__proto__ || Object.getPrototypeOf(_class2)).call.apply(_ref2, [this].concat(args))), _this3), _this3.state = {
            info: null
        }, _this3.onDocumentClick = function () {
            return _this3.setState({
                info: null
            });
        }, _this3.updatePopover = function (query, index) {
            return _this3.setState({
                info: {
                    query: query,
                    index: index
                }
            });
        }, _temp2), _possibleConstructorReturn(_this3, _ret2);
    }

    _createClass(_class2, [{
        key: "render",
        value: function render() {
            var _this4 = this;

            if (!LAYOUT_LABEL) {
                initLayout();
            }

            var _props2 = this.props,
                id = _props2.id,
                width = _props2.width,
                height = _props2.height,
                queries = _props2.queries,
                round = _props2.round;
            var info = this.state.info;


            var size = 0.9;
            var graphWidth = width * size;
            var graphHeight = height * size;

            var max = findMaximum(queries);

            var chartConfig = {
                x: width / 2 - graphWidth / 2,
                y: height / 2 - graphHeight / 2,
                width: graphWidth,
                height: graphHeight,
                max: max,
                round: round,
                propertyType: queries[0].data.columns.value,
                chartId: id
            };

            return _react2.default.createElement(
                "div",
                { id: id, className: "bar-chart" },
                _react2.default.createElement(
                    _GUIContainer2.default,
                    {
                        width: width,
                        height: height,
                        centerX: width / 2,
                        centerY: height / 2,
                        zoom: false,
                        pan: false
                    },
                    _react2.default.createElement(CartesianGrid, {
                        chartConfig: chartConfig
                    }),
                    queries.map(function (query) {
                        return _react2.default.createElement(Series, {
                            key: query.className,
                            chartConfig: chartConfig,
                            query: query,
                            onFocus: _this4.updatePopover
                        });
                    })
                ),
                info && _react2.default.createElement(InfoWindow, _extends({
                    chartConfig: chartConfig
                }, info))
            );
        }
    }]);

    return _class2;
}(_react2.default.Component));

exports.default = BarChart;

/***/ }),

/***/ 409:
/*!****************************************************************!*\
  !*** /home/sven/ideaprojects/exceed/node_modules/raf/index.js ***!
  \****************************************************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

/* WEBPACK VAR INJECTION */(function(global) {var now = __webpack_require__(/*! performance-now */ 410)
  , root = typeof window === 'undefined' ? global : window
  , vendors = ['moz', 'webkit']
  , suffix = 'AnimationFrame'
  , raf = root['request' + suffix]
  , caf = root['cancel' + suffix] || root['cancelRequest' + suffix]

for(var i = 0; !raf && i < vendors.length; i++) {
  raf = root[vendors[i] + 'Request' + suffix]
  caf = root[vendors[i] + 'Cancel' + suffix]
      || root[vendors[i] + 'CancelRequest' + suffix]
}

// Some versions of FF have rAF but not cAF
if(!raf || !caf) {
  var last = 0
    , id = 0
    , queue = []
    , frameDuration = 1000 / 60

  raf = function(callback) {
    if(queue.length === 0) {
      var _now = now()
        , next = Math.max(0, frameDuration - (_now - last))
      last = next + _now
      setTimeout(function() {
        var cp = queue.slice(0)
        // Clear queue here to prevent
        // callbacks from appending listeners
        // to the current frame's queue
        queue.length = 0
        for(var i = 0; i < cp.length; i++) {
          if(!cp[i].cancelled) {
            try{
              cp[i].callback(last)
            } catch(e) {
              setTimeout(function() { throw e }, 0)
            }
          }
        }
      }, Math.round(next))
    }
    queue.push({
      handle: ++id,
      callback: callback,
      cancelled: false
    })
    return id
  }

  caf = function(handle) {
    for(var i = 0; i < queue.length; i++) {
      if(queue[i].handle === handle) {
        queue[i].cancelled = true
      }
    }
  }
}

module.exports = function(fn) {
  // Wrap in a new function to prevent
  // `cancel` potentially being assigned
  // to the native rAF function
  return raf.call(root, fn)
}
module.exports.cancel = function() {
  caf.apply(root, arguments)
}
module.exports.polyfill = function(object) {
  if (!object) {
    object = root;
  }
  object.requestAnimationFrame = raf
  object.cancelAnimationFrame = caf
}

/* WEBPACK VAR INJECTION */}.call(exports, __webpack_require__(/*! ./../webpack/buildin/global.js */ 35)))

/***/ }),

/***/ 410:
/*!******************************************************************************************!*\
  !*** /home/sven/ideaprojects/exceed/node_modules/performance-now/lib/performance-now.js ***!
  \******************************************************************************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

/* WEBPACK VAR INJECTION */(function(process) {// Generated by CoffeeScript 1.12.2
(function() {
  var getNanoSeconds, hrtime, loadTime, moduleLoadTime, nodeLoadTime, upTime;

  if ((typeof performance !== "undefined" && performance !== null) && performance.now) {
    module.exports = function() {
      return performance.now();
    };
  } else if ((typeof process !== "undefined" && process !== null) && process.hrtime) {
    module.exports = function() {
      return (getNanoSeconds() - nodeLoadTime) / 1e6;
    };
    hrtime = process.hrtime;
    getNanoSeconds = function() {
      var hr;
      hr = hrtime();
      return hr[0] * 1e9 + hr[1];
    };
    moduleLoadTime = getNanoSeconds();
    upTime = process.uptime() * 1e9;
    nodeLoadTime = moduleLoadTime - upTime;
  } else if (Date.now) {
    module.exports = function() {
      return Date.now() - loadTime;
    };
    loadTime = Date.now();
  } else {
    module.exports = function() {
      return new Date().getTime() - loadTime;
    };
    loadTime = new Date().getTime();
  }

}).call(this);

//# sourceMappingURL=performance-now.js.map

/* WEBPACK VAR INJECTION */}.call(exports, __webpack_require__(/*! ./../../process/browser.js */ 153)))

/***/ }),

/***/ 411:
/*!********************************************!*\
  !*** ./components/std/chart/RadarChart.js ***!
  \********************************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
    value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = __webpack_require__(/*! react */ 0);

var _react2 = _interopRequireDefault(_react);

var _classnames = __webpack_require__(/*! classnames */ 1);

var _classnames2 = _interopRequireDefault(_classnames);

var _keys = __webpack_require__(/*! ../../../util/keys */ 25);

var _keys2 = _interopRequireDefault(_keys);

var _DocumentClick = __webpack_require__(/*! ../../../util/DocumentClick */ 229);

var _DocumentClick2 = _interopRequireDefault(_DocumentClick);

var _svgLayout = __webpack_require__(/*! ../../../gfx/svg-layout */ 95);

var _svgLayout2 = _interopRequireDefault(_svgLayout);

var _guiContext = __webpack_require__(/*! ../../../editor/gui/gui-context */ 96);

var _guiContext2 = _interopRequireDefault(_guiContext);

var _GUIContainer = __webpack_require__(/*! ../../../editor/gui/GUIContainer */ 223);

var _GUIContainer2 = _interopRequireDefault(_GUIContainer);

var _GUIElement = __webpack_require__(/*! ../../../editor/gui/GUIElement */ 228);

var _GUIElement2 = _interopRequireDefault(_GUIElement);

var _uiState = __webpack_require__(/*! ../../../editor/gui/ui-state */ 97);

var _uiState2 = _interopRequireDefault(_uiState);

var _Popover = __webpack_require__(/*! react-bootstrap/lib/Popover */ 226);

var _Popover2 = _interopRequireDefault(_Popover);

var _propertyRenderer = __webpack_require__(/*! ../../../service/property-renderer */ 61);

var _propertyRenderer2 = _interopRequireDefault(_propertyRenderer);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var TextSize = _svgLayout2.default.TextSize;


var SIZE_LABEL = TextSize.NORMAL;
var SIZE_INFO_TITLE = TextSize.LARGER;
var SIZE_INFO_TEXT = TextSize.SMALL;

// lazy initialized from constants above
var LAYOUT_LABEL = void 0;
var LAYOUT_INFO_TITLE = void 0;
var LAYOUT_INFO_TEXT = void 0;
var FONT_SIZE_LABEL = void 0;
var FONT_SIZE_INFO_TITLE = void 0;
var FONT_SIZE_INFO_TEXT = void 0;

var TAU = Math.PI * 2;
var TO_DEGREE_FACTOR = 360 / TAU;

function initLayout() {
    var exampleTextSizes = _svgLayout2.default.getExampleTextSizes();

    LAYOUT_LABEL = exampleTextSizes[SIZE_LABEL];
    LAYOUT_INFO_TITLE = exampleTextSizes[SIZE_INFO_TITLE];
    LAYOUT_INFO_TEXT = exampleTextSizes[SIZE_INFO_TEXT];

    FONT_SIZE_LABEL = _svgLayout2.default.getFontSize(SIZE_LABEL);
    FONT_SIZE_INFO_TITLE = _svgLayout2.default.getFontSize(SIZE_INFO_TITLE);
    FONT_SIZE_INFO_TEXT = _svgLayout2.default.getFontSize(SIZE_INFO_TEXT);
}

function buildData(queries) {
    var labels = {};
    var names = [];
    var titles = [];
    for (var i = 0; i < queries.length; i++) {
        var query = queries[i];
        var rootObject = query.data.rootObject;


        titles.push(query.name);
        names.push(query.className);

        for (var j = 0; j < rootObject.length; j++) {
            labels[rootObject[j].label] = true;
        }
    }

    var sorted = (0, _keys2.default)(labels).sort();

    var series = [];

    var max = 0;
    for (var _i = 0; _i < queries.length; _i++) {
        var _query = queries[_i];
        var rootObject = _query.data.rootObject;


        var data = new Array(sorted.length);
        for (var _j = 0; _j < sorted.length; _j++) {
            var label = sorted[_j];

            var value = 0;
            for (var k = 0; k < rootObject.length; k++) {
                var row = rootObject[k];
                if (row.label === label) {
                    value = row.value;
                }
            }

            data[_j] = value;
            if (value > max) {
                max = value;
            }
        }

        series.push(data);
    }

    return { names: names, titles: titles, labels: sorted, series: series, max: max };
}

function Labels(props) {
    var labels = props.labels;
    var _props$chartConfig = props.chartConfig,
        halfWidth = _props$chartConfig.halfWidth,
        halfHeight = _props$chartConfig.halfHeight;


    var angleStep = TAU / labels.length;

    var labelRadius = halfHeight * 0.95;
    var invLabelRadius = halfHeight * 0.975;

    return _react2.default.createElement(
        "g",
        null,
        labels.map(function (label, idx) {
            var radians = idx * angleStep;
            var degrees = radians * TO_DEGREE_FACTOR;

            var labelWidth = LAYOUT_LABEL.estimateWidth(label);

            var shouldFlip = degrees > 90 && degrees < 270;
            var r = shouldFlip ? invLabelRadius : labelRadius;

            var x = Math.sin(radians) * r;
            var y = -Math.cos(radians) * r;

            var transform = "translate(" + x + ", " + y + ") rotate(" + (shouldFlip ? degrees + 180 : degrees) + "," + halfWidth + ", " + halfHeight + ")";

            return _react2.default.createElement(
                "g",
                {
                    key: label,
                    transform: transform
                },
                _react2.default.createElement(
                    "text",
                    {
                        x: halfWidth - labelWidth / 2,
                        y: halfHeight,
                        fontSize: FONT_SIZE_LABEL
                    },
                    label
                )
            );
        })
    );
}

function DataSet(props) {
    var name = props.name,
        title = props.title,
        dataSet = props.dataSet,
        max = props.max,
        labels = props.labels,
        onInteraction = props.onInteraction;
    var _props$chartConfig2 = props.chartConfig,
        width = _props$chartConfig2.width,
        height = _props$chartConfig2.height,
        halfWidth = _props$chartConfig2.halfWidth,
        halfHeight = _props$chartConfig2.halfHeight;


    var angleStep = TAU / dataSet.length;

    var scale = halfHeight * 0.9 / max;

    var infos = [];

    var path = "";

    for (var i = 0, angle = 0; i < dataSet.length; i++, angle += angleStep) {
        var value = dataSet[i];

        // angle 0 is -radius, 0
        var x = halfWidth + Math.sin(angle) * value * scale;
        var y = halfHeight - Math.cos(angle) * value * scale;

        path += (i === 0 ? "M" : "L") + x + " " + y;

        var id = name + "-" + labels[i];

        infos.push(_react2.default.createElement(PointInfo, {
            key: id,
            id: id,
            position: { x: x, y: y },
            name: name,
            title: title,
            label: labels[i],
            value: value,
            onInteraction: onInteraction
        }));
    }

    path += "Z";

    return _react2.default.createElement(
        "g",
        null,
        _react2.default.createElement("path", { className: "radar-" + name, d: path }),
        infos
    );
}

var Grid = function (_React$Component) {
    _inherits(Grid, _React$Component);

    function Grid() {
        var _ref;

        var _temp, _this, _ret;

        _classCallCheck(this, Grid);

        for (var _len = arguments.length, args = Array(_len), _key = 0; _key < _len; _key++) {
            args[_key] = arguments[_key];
        }

        return _ret = (_temp = (_this = _possibleConstructorReturn(this, (_ref = Grid.__proto__ || Object.getPrototypeOf(Grid)).call.apply(_ref, [this].concat(args))), _this), _this.onUpdate = function () {
            _this.forceUpdate();
        }, _temp), _possibleConstructorReturn(_this, _ret);
    }

    _createClass(Grid, [{
        key: "render",
        value: function render() {
            var _props = this.props,
                count = _props.count,
                close = _props.close;
            var _props$chartConfig3 = this.props.chartConfig,
                width = _props$chartConfig3.width,
                height = _props$chartConfig3.height,
                halfWidth = _props$chartConfig3.halfWidth,
                halfHeight = _props$chartConfig3.halfHeight;


            var angleStep = TAU / count;

            var radius = halfHeight * 0.9;

            var maxValueRing = "";
            var halfValueRing = "";
            var spokes = "";

            for (var i = 0, angle = 0; i < count; i++, angle += angleStep) {

                // angle 0 is -radius, 0
                var sx = Math.sin(angle) * radius;
                var sy = -Math.cos(angle) * radius;

                var x = halfWidth + sx;
                var y = halfHeight + sy;
                var x2 = halfWidth + sx / 2;
                var y2 = halfHeight + sy / 2;

                maxValueRing += (i === 0 ? "M" : "L") + x + " " + y;
                halfValueRing += (i === 0 ? "M" : "L") + x2 + " " + y2;
                spokes += "M" + halfWidth + " " + halfHeight + " L" + x + " " + y;
            }

            maxValueRing += "Z";
            halfValueRing += "Z";

            return _react2.default.createElement(
                "g",
                { className: "chart-grid" },
                _react2.default.createElement("path", { d: spokes }),
                _react2.default.createElement("path", { d: maxValueRing }),
                _react2.default.createElement("path", { d: halfValueRing })
            );
        }
    }]);

    return Grid;
}(_react2.default.Component);

var PointInfo = function (_React$Component2) {
    _inherits(PointInfo, _React$Component2);

    function PointInfo() {
        var _ref2;

        var _temp2, _this2, _ret2;

        _classCallCheck(this, PointInfo);

        for (var _len2 = arguments.length, args = Array(_len2), _key2 = 0; _key2 < _len2; _key2++) {
            args[_key2] = arguments[_key2];
        }

        return _ret2 = (_temp2 = (_this2 = _possibleConstructorReturn(this, (_ref2 = PointInfo.__proto__ || Object.getPrototypeOf(PointInfo)).call.apply(_ref2, [this].concat(args))), _this2), _this2.onUpdate = function () {
            _this2.forceUpdate();
        }, _this2.onInteraction = function () {
            var _this2$props = _this2.props,
                title = _this2$props.title,
                label = _this2$props.label,
                value = _this2$props.value,
                position = _this2$props.position;


            _this2.props.onInteraction(title, label, value, position);
        }, _temp2), _possibleConstructorReturn(_this2, _ret2);
    }

    _createClass(PointInfo, [{
        key: "render",
        value: function render() {
            var _props2 = this.props,
                id = _props2.id,
                position = _props2.position;


            var uiState = _guiContext2.default.getElementState(id, _uiState2.default.NORMAL);

            return _react2.default.createElement(
                _GUIElement2.default,
                {
                    id: id,
                    className: (0, _classnames2.default)("point-info", uiState.toLowerCase()),
                    position: position,
                    onUpdate: this.onUpdate,
                    onInteraction: this.onInteraction,
                    draggable: false
                },
                _react2.default.createElement("circle", { r: "10", cx: position.x, cy: position.y })
            );
        }
    }]);

    return PointInfo;
}(_react2.default.Component);

var InfoWindow = function (_React$Component3) {
    _inherits(InfoWindow, _React$Component3);

    function InfoWindow() {
        _classCallCheck(this, InfoWindow);

        return _possibleConstructorReturn(this, (InfoWindow.__proto__ || Object.getPrototypeOf(InfoWindow)).apply(this, arguments));
    }

    _createClass(InfoWindow, [{
        key: "render",
        value: function render() {
            var _props3 = this.props,
                name = _props3.name,
                label = _props3.label,
                value = _props3.value,
                chartConfig = _props3.chartConfig,
                position = _props3.position;
            var chartId = chartConfig.chartId,
                propertyType = chartConfig.propertyType;


            return _react2.default.createElement(
                _Popover2.default,
                {
                    id: chartId + "-info",
                    placement: "right",
                    positionLeft: position.x + 14,
                    positionTop: position.y + 22,
                    title: name
                },
                label,
                " ",
                _react2.default.createElement("br", null),
                "Value: ",
                _propertyRenderer2.default.renderStatic(value, propertyType)
            );
        }
    }]);

    return InfoWindow;
}(_react2.default.Component);

function Legend(props) {
    var names = props.names,
        titles = props.titles;
    var _props$chartConfig4 = props.chartConfig,
        width = _props$chartConfig4.width,
        height = _props$chartConfig4.height,
        halfWidth = _props$chartConfig4.halfWidth,
        halfHeight = _props$chartConfig4.halfHeight;


    return _react2.default.createElement(
        "g",
        { className: "legend" },
        names.map(function (name, idx) {

            var yPos = height - 10 - (names.length - idx) * LAYOUT_INFO_TEXT.height;

            return _react2.default.createElement(
                "g",
                { key: name,
                    className: "legend-" + name
                },
                _react2.default.createElement("rect", {
                    x: 10,
                    y: yPos - LAYOUT_INFO_TEXT.height + 5,
                    width: 10,
                    height: 10
                }),
                _react2.default.createElement(
                    "text",
                    {
                        x: 30,
                        y: yPos,
                        fontSize: SIZE_INFO_TEXT
                    },
                    titles[idx]
                )
            );
        })
    );
}

var RadarChart = (0, _DocumentClick2.default)(function (_React$Component4) {
    _inherits(_class2, _React$Component4);

    function _class2() {
        var _ref3;

        var _temp3, _this4, _ret3;

        _classCallCheck(this, _class2);

        for (var _len3 = arguments.length, args = Array(_len3), _key3 = 0; _key3 < _len3; _key3++) {
            args[_key3] = arguments[_key3];
        }

        return _ret3 = (_temp3 = (_this4 = _possibleConstructorReturn(this, (_ref3 = _class2.__proto__ || Object.getPrototypeOf(_class2)).call.apply(_ref3, [this].concat(args))), _this4), _this4.state = {
            currentInfo: null
        }, _this4.onInteraction = function (name, label, value, position) {

            _this4.setState({
                currentInfo: {
                    name: name,
                    label: label,
                    value: value,
                    position: position
                }
            });
        }, _this4.onDocumentClick = function () {

            _this4.setState({
                currentInfo: null
            });
        }, _temp3), _possibleConstructorReturn(_this4, _ret3);
    }

    _createClass(_class2, [{
        key: "render",
        value: function render() {
            var _this5 = this;

            if (!LAYOUT_LABEL) {
                initLayout();
            }

            var _props4 = this.props,
                id = _props4.id,
                width = _props4.width,
                height = _props4.height,
                queries = _props4.queries;


            var chartConfig = {
                width: width,
                height: height,
                halfWidth: width / 2,
                halfHeight: height / 2,
                chartId: id,
                propertyType: queries[0].data.columns.value
            };

            var currentInfo = this.state.currentInfo;


            var data = buildData(queries);

            var names = data.names,
                titles = data.titles;


            return _react2.default.createElement(
                "div",
                { id: id, className: "radar-chart" },
                _react2.default.createElement(
                    _GUIContainer2.default,
                    {
                        chartConfig: chartConfig,
                        width: width,
                        height: height,
                        centerX: width / 2,
                        centerY: height / 2,
                        zoom: false,
                        pan: false
                    },
                    _react2.default.createElement(Legend, {
                        chartConfig: chartConfig,
                        names: names,
                        titles: titles
                    }),
                    _react2.default.createElement(Grid, {
                        chartConfig: chartConfig,
                        count: data.labels.length
                    }),
                    data.series.map(function (dataSet, idx) {
                        var name = names[idx];
                        var title = titles[idx];
                        return _react2.default.createElement(DataSet, {
                            key: name,
                            name: name,
                            title: title,
                            chartConfig: chartConfig,
                            dataSet: dataSet,
                            labels: data.labels,
                            titles: data.labels,
                            max: data.max,
                            onInteraction: _this5.onInteraction
                        });
                    }),
                    _react2.default.createElement(Labels, {
                        chartConfig: chartConfig,
                        labels: data.labels
                    })
                ),
                currentInfo && _react2.default.createElement(InfoWindow, _extends({
                    chartConfig: chartConfig
                }, currentInfo))
            );
        }
    }]);

    return _class2;
}(_react2.default.Component));

exports.default = RadarChart;

/***/ }),

/***/ 412:
/*!**********************************************!*\
  !*** ./components/std/chart/components.json ***!
  \**********************************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports) {

module.exports = {"components":{"RadarChart":{"description":"Radar chart component.","classes":["element"],"queries":{"queries":"prop('queries')"},"propTypes":{"queries":{"description":"Complex query expression.","client":false,"type":"QUERY_EXPRESSION"}}},"BarChart":{"description":"Bar chart component.","classes":["element"],"queries":{"queries":"prop('queries')"},"propTypes":{"queries":{"description":"Complex query expression.","client":false,"type":"QUERY_EXPRESSION"}}}}}

/***/ }),

/***/ 413:
/*!******************************************!*\
  !*** ./components/std/common/Content.js ***!
  \******************************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
  value: true
});
/**
 * Content is a formal component important content roots but is not itself being rendered.
 */
exports.default = "*CONTENT";

/***/ }),

/***/ 414:
/*!*****************************************!*\
  !*** ./components/std/common/Dialog.js ***!
  \*****************************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
    value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = __webpack_require__(/*! react */ 0);

var _react2 = _interopRequireDefault(_react);

var _store = __webpack_require__(/*! ../../../service/store */ 3);

var _store2 = _interopRequireDefault(_store);

var _scope = __webpack_require__(/*! ../../../service/scope */ 107);

var _scope2 = _interopRequireDefault(_scope);

var _i18n = __webpack_require__(/*! ../../../service/i18n */ 9);

var _i18n2 = _interopRequireDefault(_i18n);

var _scope3 = __webpack_require__(/*! ../../../actions/scope */ 26);

var _Modal = __webpack_require__(/*! react-bootstrap/lib/Modal */ 50);

var _Modal2 = _interopRequireDefault(_Modal);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

// constants for "DialogState" enum
var DIALOG_OPEN = 1;
var DIALOG_CLOSED = 0;

var Dialog = function (_React$Component) {
    _inherits(Dialog, _React$Component);

    function Dialog() {
        var _ref;

        var _temp, _this, _ret;

        _classCallCheck(this, Dialog);

        for (var _len = arguments.length, args = Array(_len), _key = 0; _key < _len; _key++) {
            args[_key] = arguments[_key];
        }

        return _ret = (_temp = (_this = _possibleConstructorReturn(this, (_ref = Dialog.__proto__ || Object.getPrototypeOf(Dialog)).call.apply(_ref, [this].concat(args))), _this), _this.close = function () {
            var id = _this.props.id;

            _store2.default.dispatch((0, _scope3.updateScope)([id], DIALOG_CLOSED));
        }, _temp), _possibleConstructorReturn(_this, _ret);
    }

    _createClass(Dialog, [{
        key: "componentDidMount",
        value: function componentDidMount() {
            var _props = this.props,
                id = _props.id,
                isOpen = _props.isOpen;

            if (typeof isOpen !== "undefined") {
                var currentState = _scope2.default.property(this.props.id);

                if (currentState !== isOpen) {
                    _store2.default.dispatch((0, _scope3.updateScope)([id], isOpen ? DIALOG_OPEN : DIALOG_CLOSED));
                }
            }
        }
    }, {
        key: "render",
        value: function render() {
            var _props2 = this.props,
                title = _props2.title,
                children = _props2.children;

            var isOpen = _scope2.default.property(this.props.id) !== DIALOG_CLOSED;

            return _react2.default.createElement(
                _Modal2.default,
                { show: isOpen, onHide: this.close },
                _react2.default.createElement(
                    _Modal2.default.Header,
                    { closeButton: true },
                    _react2.default.createElement(
                        _Modal2.default.Title,
                        null,
                        title || (0, _i18n2.default)("Dialog")
                    )
                ),
                _react2.default.createElement(
                    _Modal2.default.Body,
                    null,
                    children
                )
            );
        }
    }]);

    return Dialog;
}(_react2.default.Component);

exports.default = Dialog;

/***/ }),

/***/ 415:
/*!****************************************!*\
  !*** ./components/std/common/Error.js ***!
  \****************************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
    value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = __webpack_require__(/*! react */ 0);

var _react2 = _interopRequireDefault(_react);

var _ErrorReport = __webpack_require__(/*! ../../../ui/ErrorReport.es5 */ 218);

var _ErrorReport2 = _interopRequireDefault(_ErrorReport);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var Error = function (_Component) {
    _inherits(Error, _Component);

    function Error() {
        _classCallCheck(this, Error);

        return _possibleConstructorReturn(this, (Error.__proto__ || Object.getPrototypeOf(Error)).apply(this, arguments));
    }

    _createClass(Error, [{
        key: "render",
        value: function render() {
            return _react2.default.createElement(_ErrorReport2.default, { error: this.props.error });
        }
    }]);

    return Error;
}(_react.Component);

exports.default = Error;

/***/ }),

/***/ 416:
/*!*****************************************!*\
  !*** ./components/std/common/Footer.js ***!
  \*****************************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
    value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = __webpack_require__(/*! react */ 0);

var _react2 = _interopRequireDefault(_react);

var _security = __webpack_require__(/*! ../../../service/security */ 62);

var _security2 = _interopRequireDefault(_security);

var _csfr = __webpack_require__(/*! ../../../service/csfr */ 105);

var _csfr2 = _interopRequireDefault(_csfr);

var _i18n = __webpack_require__(/*! ../../../service/i18n */ 9);

var _i18n2 = _interopRequireDefault(_i18n);

var _uri = __webpack_require__(/*! ../../../util/uri */ 15);

var _uri2 = _interopRequireDefault(_uri);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var Footer = function (_React$Component) {
    _inherits(Footer, _React$Component);

    function Footer() {
        _classCallCheck(this, Footer);

        return _possibleConstructorReturn(this, (Footer.__proto__ || Object.getPrototypeOf(Footer)).apply(this, arguments));
    }

    _createClass(Footer, [{
        key: "render",
        value: function render() {
            var login = _security2.default.getLogin();

            return _react2.default.createElement(
                "div",
                { className: "container-fluid page-decoration" },
                _react2.default.createElement("hr", null),
                _react2.default.createElement(
                    "div",
                    { className: "row" },
                    _react2.default.createElement(
                        "div",
                        { className: "col-md-8" },
                        _react2.default.createElement(
                            "small",
                            null,
                            (0, _i18n2.default)('Footer Text')
                        )
                    ),
                    _react2.default.createElement(
                        "div",
                        { className: "col-md-4" },
                        login === "Anonymous" ? _react2.default.createElement(
                            "a",
                            { className: "btn btn-link pull-right", href: (0, _uri2.default)("/login") },
                            (0, _i18n2.default)('Login')
                        ) : _react2.default.createElement(
                            "form",
                            { className: "form-inline", action: (0, _uri2.default)('/logout'), method: "POST" },
                            _react2.default.createElement("input", { type: "submit", className: "btn btn-link pull-right",
                                value: (0, _i18n2.default)('Logout {0}', login) }),
                            _react2.default.createElement("input", { type: "hidden", name: _csfr2.default.tokenParam(), value: _csfr2.default.token() })
                        )
                    )
                )
            );
        }
    }]);

    return Footer;
}(_react2.default.Component);

;

exports.default = Footer;

/***/ }),

/***/ 417:
/*!******************************************!*\
  !*** ./components/std/common/Heading.js ***!
  \******************************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
    value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = __webpack_require__(/*! react */ 0);

var _react2 = _interopRequireDefault(_react);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var Heading = function (_React$Component) {
    _inherits(Heading, _React$Component);

    function Heading() {
        _classCallCheck(this, Heading);

        return _possibleConstructorReturn(this, (Heading.__proto__ || Object.getPrototypeOf(Heading)).apply(this, arguments));
    }

    _createClass(Heading, [{
        key: "render",
        value: function render() {
            var icon = this.props.icon;


            return _react2.default.createElement(
                "h2",
                null,
                icon && _react2.default.createElement("span", { className: "text-info glyphicon glyphicon-" + icon }),
                this.props.value
            );
        }
    }]);

    return Heading;
}(_react2.default.Component);

;

exports.default = Heading;

/***/ }),

/***/ 418:
/*!********************************************!*\
  !*** ./components/std/common/InfoBlock.js ***!
  \********************************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
    value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = __webpack_require__(/*! react */ 0);

var _react2 = _interopRequireDefault(_react);

var _Icon = __webpack_require__(/*! ../../../ui/Icon */ 91);

var _Icon2 = _interopRequireDefault(_Icon);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var InfoBlock = function (_React$Component) {
    _inherits(InfoBlock, _React$Component);

    function InfoBlock() {
        _classCallCheck(this, InfoBlock);

        return _possibleConstructorReturn(this, (InfoBlock.__proto__ || Object.getPrototypeOf(InfoBlock)).apply(this, arguments));
    }

    _createClass(InfoBlock, [{
        key: "render",
        value: function render() {
            var _props = this.props,
                heading = _props.heading,
                text = _props.text,
                children = _props.children;


            return _react2.default.createElement(
                "div",
                { className: "info-block" },
                _react2.default.createElement(
                    "h5",
                    { className: "text-info" },
                    _react2.default.createElement(_Icon2.default, { className: "glyphicon-info-sign" }),
                    " " + heading
                ),
                _react2.default.createElement(
                    "p",
                    null,
                    text || children
                )
            );
        }
    }]);

    return InfoBlock;
}(_react2.default.Component);

exports.default = InfoBlock;

/***/ }),

/***/ 419:
/*!***************************************!*\
  !*** ./components/std/common/Link.js ***!
  \***************************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
    value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

exports.Param = Param;

var _sys = __webpack_require__(/*! ../../../sys */ 12);

var _sys2 = _interopRequireDefault(_sys);

var _uri = __webpack_require__(/*! ../../../util/uri */ 15);

var _uri2 = _interopRequireDefault(_uri);

var _react = __webpack_require__(/*! react */ 0);

var _react2 = _interopRequireDefault(_react);

var _view = __webpack_require__(/*! ../../../actions/view */ 24);

var _store = __webpack_require__(/*! ../../../service/store */ 3);

var _store2 = _interopRequireDefault(_store);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var Link = function (_React$Component) {
    _inherits(Link, _React$Component);

    function Link() {
        _classCallCheck(this, Link);

        return _possibleConstructorReturn(this, (Link.__proto__ || Object.getPrototypeOf(Link)).apply(this, arguments));
    }

    _createClass(Link, [{
        key: "render",
        value: function render() {
            var params = {};

            _react2.default.Children.forEach(this.props.children, function (kid) {
                if (kid.type === Param) {
                    params[kid.props.name] = kid.props.value;
                }
            });

            //console.log("params", params);

            var target = (0, _uri2.default)("/app/" + _sys2.default.appName + this.props.location, params);

            return _react2.default.createElement(
                "a",
                { href: target,
                    target: this.props.target,
                    className: "btn btn-link",
                    onClick: function onClick(ev) {

                        try {
                            _store2.default.dispatch((0, _view.navigateView)({
                                url: ev.target.href
                            }));
                        } catch (e) {
                            console.error(e);
                        }

                        ev.preventDefault();
                    } },
                this.props.text
            );
        }
    }]);

    return Link;
}(_react2.default.Component);

;

function Param(props) {}

exports.default = Link;

/***/ }),

/***/ 420:
/*!************************************************!*\
  !*** ./components/std/common/ResourceImage.js ***!
  \************************************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
    value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = __webpack_require__(/*! react */ 0);

var _react2 = _interopRequireDefault(_react);

var _uri = __webpack_require__(/*! ../../../util/uri */ 15);

var _uri2 = _interopRequireDefault(_uri);

var _sys = __webpack_require__(/*! ../../../sys */ 12);

var _sys2 = _interopRequireDefault(_sys);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var ResourceImage = function (_React$Component) {
    _inherits(ResourceImage, _React$Component);

    function ResourceImage() {
        _classCallCheck(this, ResourceImage);

        return _possibleConstructorReturn(this, (ResourceImage.__proto__ || Object.getPrototypeOf(ResourceImage)).apply(this, arguments));
    }

    _createClass(ResourceImage, [{
        key: "render",
        value: function render() {
            return _react2.default.createElement("img", _extends({}, this.props, {
                resource: null,
                src: (0, _uri2.default)("/res/" + _sys2.default.appName + this.props.resource)
            }));
        }
    }]);

    return ResourceImage;
}(_react2.default.Component);

exports.default = ResourceImage;

/***/ }),

/***/ 421:
/*!**********************************************!*\
  !*** ./components/std/common/StandardNav.js ***!
  \**********************************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
    value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = __webpack_require__(/*! react */ 0);

var _react2 = _interopRequireDefault(_react);

var _Nav = __webpack_require__(/*! react-bootstrap/lib/Nav */ 141);

var _Nav2 = _interopRequireDefault(_Nav);

var _Navbar = __webpack_require__(/*! react-bootstrap/lib/Navbar */ 92);

var _Navbar2 = _interopRequireDefault(_Navbar);

var _NavItem = __webpack_require__(/*! react-bootstrap/lib/NavItem */ 143);

var _NavItem2 = _interopRequireDefault(_NavItem);

var _NavDropdown = __webpack_require__(/*! react-bootstrap/lib/NavDropdown */ 422);

var _NavDropdown2 = _interopRequireDefault(_NavDropdown);

var _MenuItem = __webpack_require__(/*! react-bootstrap/lib/MenuItem */ 440);

var _MenuItem2 = _interopRequireDefault(_MenuItem);

var _store = __webpack_require__(/*! ../../../service/store */ 3);

var _store2 = _interopRequireDefault(_store);

var _meta = __webpack_require__(/*! ../../../reducers/meta */ 14);

var _uri = __webpack_require__(/*! ../../../util/uri */ 15);

var _uri2 = _interopRequireDefault(_uri);

var _sys = __webpack_require__(/*! ../../../sys */ 12);

var _sys2 = _interopRequireDefault(_sys);

var _i18n = __webpack_require__(/*! ../../../service/i18n */ 9);

var _i18n2 = _interopRequireDefault(_i18n);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var CLASS_TOP = "top";
var CLASS_ADMIN = "admin";
var CLASS_HIDDEN = "hidden";

var StandardNav = function (_React$Component) {
    _inherits(StandardNav, _React$Component);

    function StandardNav() {
        _classCallCheck(this, StandardNav);

        return _possibleConstructorReturn(this, (StandardNav.__proto__ || Object.getPrototypeOf(StandardNav)).apply(this, arguments));
    }

    _createClass(StandardNav, [{
        key: "render",
        value: function render() {
            var _props = this.props,
                showBranding = _props.showBranding,
                showNormal = _props.showNormal,
                showAdmin = _props.showAdmin,
                children = _props.children;


            var state = _store2.default.getState();

            var routingTable = (0, _meta.getRoutingTable)(state);
            var mappings = routingTable.mappings;


            var applications = [];
            var topLevelApplications = [];
            var adminApplications = [];

            for (var location in mappings) {

                if (mappings.hasOwnProperty(location)) {
                    var m = mappings[location];
                    var classes = m.classes;


                    if (classes && classes.indexOf(CLASS_HIDDEN) >= 0) {
                        continue;
                    }

                    var id = m.viewName || m.processName;

                    if (classes && classes.indexOf(CLASS_TOP) >= 0) {
                        topLevelApplications.push({
                            id: id,
                            location: location,
                            name: m.title || id
                        });
                    } else if (classes && classes.indexOf(CLASS_ADMIN) >= 0) {
                        adminApplications.push({
                            id: id,
                            location: location,
                            name: m.title || id
                        });
                    } else {
                        applications.push({
                            id: id,
                            location: location,
                            name: m.title || id
                        });
                    }
                }
            }

            return _react2.default.createElement(
                _Navbar2.default,
                null,
                _react2.default.createElement(
                    _Navbar2.default.Header,
                    null,
                    showBranding && _react2.default.createElement(
                        _Navbar2.default.Brand,
                        null,
                        _react2.default.Children.count(children) === 0 ? _react2.default.createElement(
                            "a",
                            { href: (0, _uri2.default)("/app/" + _sys2.default.appName) },
                            " ",
                            _sys2.default.appName
                        ) : { children: children }
                    ),
                    _react2.default.createElement(_Navbar2.default.Toggle, null)
                ),
                _react2.default.createElement(
                    _Nav2.default,
                    null,
                    showNormal && applications.length > 0 && _react2.default.createElement(
                        _NavDropdown2.default,
                        { id: "applicationDropdown", title: (0, _i18n2.default)("App Nav") },
                        applications.map(function (_ref) {
                            var id = _ref.id,
                                name = _ref.name,
                                location = _ref.location;
                            return _react2.default.createElement(
                                _MenuItem2.default,
                                { key: id, href: (0, _uri2.default)("/app/" + _sys2.default.appName + location) },
                                name
                            );
                        })
                    ),
                    topLevelApplications.length > 0 && topLevelApplications.map(function (_ref2) {
                        var id = _ref2.id,
                            name = _ref2.name,
                            location = _ref2.location;
                        return _react2.default.createElement(
                            _NavItem2.default,
                            { key: id, href: (0, _uri2.default)("/app/" + _sys2.default.appName + location) },
                            name
                        );
                    })
                ),
                _react2.default.createElement(
                    _Nav2.default,
                    { pullRight: true },
                    showAdmin && !!adminApplications.length && _react2.default.createElement(
                        _NavDropdown2.default,
                        { id: "adminApplicationDropdown", title: (0, _i18n2.default)("Admin Nav") },
                        adminApplications.map(function (_ref3) {
                            var id = _ref3.id,
                                name = _ref3.name,
                                location = _ref3.location;
                            return _react2.default.createElement(
                                _MenuItem2.default,
                                { key: id, href: (0, _uri2.default)("/app/" + _sys2.default.appName + location) },
                                name
                            );
                        })
                    )
                )
            );
        }
    }]);

    return StandardNav;
}(_react2.default.Component);

exports.default = StandardNav;

/***/ }),

/***/ 422:
/*!**************************************************************************************!*\
  !*** /home/sven/ideaprojects/exceed/node_modules/react-bootstrap/lib/NavDropdown.js ***!
  \**************************************************************************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


exports.__esModule = true;

var _objectWithoutProperties2 = __webpack_require__(/*! babel-runtime/helpers/objectWithoutProperties */ 10);

var _objectWithoutProperties3 = _interopRequireDefault(_objectWithoutProperties2);

var _classCallCheck2 = __webpack_require__(/*! babel-runtime/helpers/classCallCheck */ 5);

var _classCallCheck3 = _interopRequireDefault(_classCallCheck2);

var _possibleConstructorReturn2 = __webpack_require__(/*! babel-runtime/helpers/possibleConstructorReturn */ 6);

var _possibleConstructorReturn3 = _interopRequireDefault(_possibleConstructorReturn2);

var _inherits2 = __webpack_require__(/*! babel-runtime/helpers/inherits */ 7);

var _inherits3 = _interopRequireDefault(_inherits2);

var _extends2 = __webpack_require__(/*! babel-runtime/helpers/extends */ 8);

var _extends3 = _interopRequireDefault(_extends2);

var _classnames = __webpack_require__(/*! classnames */ 1);

var _classnames2 = _interopRequireDefault(_classnames);

var _react = __webpack_require__(/*! react */ 0);

var _react2 = _interopRequireDefault(_react);

var _propTypes = __webpack_require__(/*! prop-types */ 2);

var _propTypes2 = _interopRequireDefault(_propTypes);

var _Dropdown = __webpack_require__(/*! ./Dropdown */ 423);

var _Dropdown2 = _interopRequireDefault(_Dropdown);

var _splitComponentProps2 = __webpack_require__(/*! ./utils/splitComponentProps */ 209);

var _splitComponentProps3 = _interopRequireDefault(_splitComponentProps2);

var _ValidComponentChildren = __webpack_require__(/*! ./utils/ValidComponentChildren */ 74);

var _ValidComponentChildren2 = _interopRequireDefault(_ValidComponentChildren);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { 'default': obj }; }

var propTypes = (0, _extends3['default'])({}, _Dropdown2['default'].propTypes, {

  // Toggle props.
  title: _propTypes2['default'].node.isRequired,
  noCaret: _propTypes2['default'].bool,
  active: _propTypes2['default'].bool,

  // Override generated docs from <Dropdown>.
  /**
   * @private
   */
  children: _propTypes2['default'].node
});

var NavDropdown = function (_React$Component) {
  (0, _inherits3['default'])(NavDropdown, _React$Component);

  function NavDropdown() {
    (0, _classCallCheck3['default'])(this, NavDropdown);
    return (0, _possibleConstructorReturn3['default'])(this, _React$Component.apply(this, arguments));
  }

  NavDropdown.prototype.isActive = function isActive(_ref, activeKey, activeHref) {
    var props = _ref.props;

    var _this2 = this;

    if (props.active || activeKey != null && props.eventKey === activeKey || activeHref && props.href === activeHref) {
      return true;
    }

    if (_ValidComponentChildren2['default'].some(props.children, function (child) {
      return _this2.isActive(child, activeKey, activeHref);
    })) {
      return true;
    }

    return props.active;
  };

  NavDropdown.prototype.render = function render() {
    var _this3 = this;

    var _props = this.props,
        title = _props.title,
        activeKey = _props.activeKey,
        activeHref = _props.activeHref,
        className = _props.className,
        style = _props.style,
        children = _props.children,
        props = (0, _objectWithoutProperties3['default'])(_props, ['title', 'activeKey', 'activeHref', 'className', 'style', 'children']);


    var active = this.isActive(this, activeKey, activeHref);
    delete props.active; // Accessed via this.isActive().
    delete props.eventKey; // Accessed via this.isActive().

    var _splitComponentProps = (0, _splitComponentProps3['default'])(props, _Dropdown2['default'].ControlledComponent),
        dropdownProps = _splitComponentProps[0],
        toggleProps = _splitComponentProps[1];

    // Unlike for the other dropdowns, styling needs to go to the `<Dropdown>`
    // rather than the `<Dropdown.Toggle>`.

    return _react2['default'].createElement(
      _Dropdown2['default'],
      (0, _extends3['default'])({}, dropdownProps, {
        componentClass: 'li',
        className: (0, _classnames2['default'])(className, { active: active }),
        style: style
      }),
      _react2['default'].createElement(
        _Dropdown2['default'].Toggle,
        (0, _extends3['default'])({}, toggleProps, { useAnchor: true }),
        title
      ),
      _react2['default'].createElement(
        _Dropdown2['default'].Menu,
        null,
        _ValidComponentChildren2['default'].map(children, function (child) {
          return _react2['default'].cloneElement(child, {
            active: _this3.isActive(child, activeKey, activeHref)
          });
        })
      )
    );
  };

  return NavDropdown;
}(_react2['default'].Component);

NavDropdown.propTypes = propTypes;

exports['default'] = NavDropdown;
module.exports = exports['default'];

/***/ }),

/***/ 423:
/*!***********************************************************************************!*\
  !*** /home/sven/ideaprojects/exceed/node_modules/react-bootstrap/lib/Dropdown.js ***!
  \***********************************************************************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


exports.__esModule = true;

var _objectWithoutProperties2 = __webpack_require__(/*! babel-runtime/helpers/objectWithoutProperties */ 10);

var _objectWithoutProperties3 = _interopRequireDefault(_objectWithoutProperties2);

var _extends2 = __webpack_require__(/*! babel-runtime/helpers/extends */ 8);

var _extends3 = _interopRequireDefault(_extends2);

var _classCallCheck2 = __webpack_require__(/*! babel-runtime/helpers/classCallCheck */ 5);

var _classCallCheck3 = _interopRequireDefault(_classCallCheck2);

var _possibleConstructorReturn2 = __webpack_require__(/*! babel-runtime/helpers/possibleConstructorReturn */ 6);

var _possibleConstructorReturn3 = _interopRequireDefault(_possibleConstructorReturn2);

var _inherits2 = __webpack_require__(/*! babel-runtime/helpers/inherits */ 7);

var _inherits3 = _interopRequireDefault(_inherits2);

var _classnames = __webpack_require__(/*! classnames */ 1);

var _classnames2 = _interopRequireDefault(_classnames);

var _activeElement = __webpack_require__(/*! dom-helpers/activeElement */ 191);

var _activeElement2 = _interopRequireDefault(_activeElement);

var _contains = __webpack_require__(/*! dom-helpers/query/contains */ 64);

var _contains2 = _interopRequireDefault(_contains);

var _keycode = __webpack_require__(/*! keycode */ 142);

var _keycode2 = _interopRequireDefault(_keycode);

var _react = __webpack_require__(/*! react */ 0);

var _react2 = _interopRequireDefault(_react);

var _propTypes = __webpack_require__(/*! prop-types */ 2);

var _propTypes2 = _interopRequireDefault(_propTypes);

var _reactDom = __webpack_require__(/*! react-dom */ 13);

var _reactDom2 = _interopRequireDefault(_reactDom);

var _all = __webpack_require__(/*! prop-types-extra/lib/all */ 93);

var _all2 = _interopRequireDefault(_all);

var _elementType = __webpack_require__(/*! prop-types-extra/lib/elementType */ 17);

var _elementType2 = _interopRequireDefault(_elementType);

var _isRequiredForA11y = __webpack_require__(/*! prop-types-extra/lib/isRequiredForA11y */ 227);

var _isRequiredForA11y2 = _interopRequireDefault(_isRequiredForA11y);

var _uncontrollable = __webpack_require__(/*! uncontrollable */ 220);

var _uncontrollable2 = _interopRequireDefault(_uncontrollable);

var _warning = __webpack_require__(/*! warning */ 87);

var _warning2 = _interopRequireDefault(_warning);

var _ButtonGroup = __webpack_require__(/*! ./ButtonGroup */ 424);

var _ButtonGroup2 = _interopRequireDefault(_ButtonGroup);

var _DropdownMenu = __webpack_require__(/*! ./DropdownMenu */ 428);

var _DropdownMenu2 = _interopRequireDefault(_DropdownMenu);

var _DropdownToggle = __webpack_require__(/*! ./DropdownToggle */ 438);

var _DropdownToggle2 = _interopRequireDefault(_DropdownToggle);

var _bootstrapUtils = __webpack_require__(/*! ./utils/bootstrapUtils */ 11);

var _createChainedFunction = __webpack_require__(/*! ./utils/createChainedFunction */ 18);

var _createChainedFunction2 = _interopRequireDefault(_createChainedFunction);

var _PropTypes = __webpack_require__(/*! ./utils/PropTypes */ 439);

var _ValidComponentChildren = __webpack_require__(/*! ./utils/ValidComponentChildren */ 74);

var _ValidComponentChildren2 = _interopRequireDefault(_ValidComponentChildren);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { 'default': obj }; }

var TOGGLE_ROLE = _DropdownToggle2['default'].defaultProps.bsRole;
var MENU_ROLE = _DropdownMenu2['default'].defaultProps.bsRole;

var propTypes = {
  /**
   * The menu will open above the dropdown button, instead of below it.
   */
  dropup: _propTypes2['default'].bool,

  /**
   * An html id attribute, necessary for assistive technologies, such as screen readers.
   * @type {string|number}
   * @required
   */
  id: (0, _isRequiredForA11y2['default'])(_propTypes2['default'].oneOfType([_propTypes2['default'].string, _propTypes2['default'].number])),

  componentClass: _elementType2['default'],

  /**
   * The children of a Dropdown may be a `<Dropdown.Toggle>` or a `<Dropdown.Menu>`.
   * @type {node}
   */
  children: (0, _all2['default'])((0, _PropTypes.requiredRoles)(TOGGLE_ROLE, MENU_ROLE), (0, _PropTypes.exclusiveRoles)(MENU_ROLE)),

  /**
   * Whether or not component is disabled.
   */
  disabled: _propTypes2['default'].bool,

  /**
   * Align the menu to the right side of the Dropdown toggle
   */
  pullRight: _propTypes2['default'].bool,

  /**
   * Whether or not the Dropdown is visible.
   *
   * @controllable onToggle
   */
  open: _propTypes2['default'].bool,

  defaultOpen: _propTypes2['default'].bool,

  /**
   * A callback fired when the Dropdown wishes to change visibility. Called with the requested
   * `open` value, the DOM event, and the source that fired it: `'click'`,`'keydown'`,`'rootClose'`, or `'select'`.
   *
   * ```js
   * function(Boolean isOpen, Object event, { String source }) {}
   * ```
   * @controllable open
   */
  onToggle: _propTypes2['default'].func,

  /**
   * A callback fired when a menu item is selected.
   *
   * ```js
   * (eventKey: any, event: Object) => any
   * ```
   */
  onSelect: _propTypes2['default'].func,

  /**
   * If `'menuitem'`, causes the dropdown to behave like a menu item rather than
   * a menu button.
   */
  role: _propTypes2['default'].string,

  /**
   * Which event when fired outside the component will cause it to be closed
   */
  rootCloseEvent: _propTypes2['default'].oneOf(['click', 'mousedown']),

  /**
   * @private
   */
  onMouseEnter: _propTypes2['default'].func,
  /**
   * @private
   */
  onMouseLeave: _propTypes2['default'].func
};

var defaultProps = {
  componentClass: _ButtonGroup2['default']
};

var Dropdown = function (_React$Component) {
  (0, _inherits3['default'])(Dropdown, _React$Component);

  function Dropdown(props, context) {
    (0, _classCallCheck3['default'])(this, Dropdown);

    var _this = (0, _possibleConstructorReturn3['default'])(this, _React$Component.call(this, props, context));

    _this.handleClick = _this.handleClick.bind(_this);
    _this.handleKeyDown = _this.handleKeyDown.bind(_this);
    _this.handleClose = _this.handleClose.bind(_this);

    _this._focusInDropdown = false;
    _this.lastOpenEventType = null;
    return _this;
  }

  Dropdown.prototype.componentDidMount = function componentDidMount() {
    this.focusNextOnOpen();
  };

  Dropdown.prototype.componentWillUpdate = function componentWillUpdate(nextProps) {
    if (!nextProps.open && this.props.open) {
      this._focusInDropdown = (0, _contains2['default'])(_reactDom2['default'].findDOMNode(this.menu), (0, _activeElement2['default'])(document));
    }
  };

  Dropdown.prototype.componentDidUpdate = function componentDidUpdate(prevProps) {
    var open = this.props.open;

    var prevOpen = prevProps.open;

    if (open && !prevOpen) {
      this.focusNextOnOpen();
    }

    if (!open && prevOpen) {
      // if focus hasn't already moved from the menu let's return it
      // to the toggle
      if (this._focusInDropdown) {
        this._focusInDropdown = false;
        this.focus();
      }
    }
  };

  Dropdown.prototype.handleClick = function handleClick(event) {
    if (this.props.disabled) {
      return;
    }

    this.toggleOpen(event, { source: 'click' });
  };

  Dropdown.prototype.handleKeyDown = function handleKeyDown(event) {
    if (this.props.disabled) {
      return;
    }

    switch (event.keyCode) {
      case _keycode2['default'].codes.down:
        if (!this.props.open) {
          this.toggleOpen(event, { source: 'keydown' });
        } else if (this.menu.focusNext) {
          this.menu.focusNext();
        }
        event.preventDefault();
        break;
      case _keycode2['default'].codes.esc:
      case _keycode2['default'].codes.tab:
        this.handleClose(event, { source: 'keydown' });
        break;
      default:
    }
  };

  Dropdown.prototype.toggleOpen = function toggleOpen(event, eventDetails) {
    var open = !this.props.open;

    if (open) {
      this.lastOpenEventType = eventDetails.source;
    }

    if (this.props.onToggle) {
      this.props.onToggle(open, event, eventDetails);
    }
  };

  Dropdown.prototype.handleClose = function handleClose(event, eventDetails) {
    if (!this.props.open) {
      return;
    }

    this.toggleOpen(event, eventDetails);
  };

  Dropdown.prototype.focusNextOnOpen = function focusNextOnOpen() {
    var menu = this.menu;

    if (!menu.focusNext) {
      return;
    }

    if (this.lastOpenEventType === 'keydown' || this.props.role === 'menuitem') {
      menu.focusNext();
    }
  };

  Dropdown.prototype.focus = function focus() {
    var toggle = _reactDom2['default'].findDOMNode(this.toggle);

    if (toggle && toggle.focus) {
      toggle.focus();
    }
  };

  Dropdown.prototype.renderToggle = function renderToggle(child, props) {
    var _this2 = this;

    var ref = function ref(c) {
      _this2.toggle = c;
    };

    if (typeof child.ref === 'string') {
      Object({"USE_EDITOR":true}).NODE_ENV !== 'production' ? (0, _warning2['default'])(false, 'String refs are not supported on `<Dropdown.Toggle>` components. ' + 'To apply a ref to the component use the callback signature:\n\n ' + 'https://facebook.github.io/react/docs/more-about-refs.html#the-ref-callback-attribute') : void 0;
    } else {
      ref = (0, _createChainedFunction2['default'])(child.ref, ref);
    }

    return (0, _react.cloneElement)(child, (0, _extends3['default'])({}, props, {
      ref: ref,
      bsClass: (0, _bootstrapUtils.prefix)(props, 'toggle'),
      onClick: (0, _createChainedFunction2['default'])(child.props.onClick, this.handleClick),
      onKeyDown: (0, _createChainedFunction2['default'])(child.props.onKeyDown, this.handleKeyDown)
    }));
  };

  Dropdown.prototype.renderMenu = function renderMenu(child, _ref) {
    var _this3 = this;

    var id = _ref.id,
        onSelect = _ref.onSelect,
        rootCloseEvent = _ref.rootCloseEvent,
        props = (0, _objectWithoutProperties3['default'])(_ref, ['id', 'onSelect', 'rootCloseEvent']);

    var ref = function ref(c) {
      _this3.menu = c;
    };

    if (typeof child.ref === 'string') {
      Object({"USE_EDITOR":true}).NODE_ENV !== 'production' ? (0, _warning2['default'])(false, 'String refs are not supported on `<Dropdown.Menu>` components. ' + 'To apply a ref to the component use the callback signature:\n\n ' + 'https://facebook.github.io/react/docs/more-about-refs.html#the-ref-callback-attribute') : void 0;
    } else {
      ref = (0, _createChainedFunction2['default'])(child.ref, ref);
    }

    return (0, _react.cloneElement)(child, (0, _extends3['default'])({}, props, {
      ref: ref,
      labelledBy: id,
      bsClass: (0, _bootstrapUtils.prefix)(props, 'menu'),
      onClose: (0, _createChainedFunction2['default'])(child.props.onClose, this.handleClose),
      onSelect: (0, _createChainedFunction2['default'])(child.props.onSelect, onSelect, function (key, event) {
        return _this3.handleClose(event, { source: 'select' });
      }),
      rootCloseEvent: rootCloseEvent
    }));
  };

  Dropdown.prototype.render = function render() {
    var _classes,
        _this4 = this;

    var _props = this.props,
        Component = _props.componentClass,
        id = _props.id,
        dropup = _props.dropup,
        disabled = _props.disabled,
        pullRight = _props.pullRight,
        open = _props.open,
        onSelect = _props.onSelect,
        role = _props.role,
        bsClass = _props.bsClass,
        className = _props.className,
        rootCloseEvent = _props.rootCloseEvent,
        children = _props.children,
        props = (0, _objectWithoutProperties3['default'])(_props, ['componentClass', 'id', 'dropup', 'disabled', 'pullRight', 'open', 'onSelect', 'role', 'bsClass', 'className', 'rootCloseEvent', 'children']);


    delete props.onToggle;

    var classes = (_classes = {}, _classes[bsClass] = true, _classes.open = open, _classes.disabled = disabled, _classes);

    if (dropup) {
      classes[bsClass] = false;
      classes.dropup = true;
    }

    // This intentionally forwards bsSize and bsStyle (if set) to the
    // underlying component, to allow it to render size and style variants.

    return _react2['default'].createElement(
      Component,
      (0, _extends3['default'])({}, props, {
        className: (0, _classnames2['default'])(className, classes)
      }),
      _ValidComponentChildren2['default'].map(children, function (child) {
        switch (child.props.bsRole) {
          case TOGGLE_ROLE:
            return _this4.renderToggle(child, {
              id: id, disabled: disabled, open: open, role: role, bsClass: bsClass
            });
          case MENU_ROLE:
            return _this4.renderMenu(child, {
              id: id, open: open, pullRight: pullRight, bsClass: bsClass, onSelect: onSelect, rootCloseEvent: rootCloseEvent
            });
          default:
            return child;
        }
      })
    );
  };

  return Dropdown;
}(_react2['default'].Component);

Dropdown.propTypes = propTypes;
Dropdown.defaultProps = defaultProps;

(0, _bootstrapUtils.bsClass)('dropdown', Dropdown);

var UncontrolledDropdown = (0, _uncontrollable2['default'])(Dropdown, { open: 'onToggle' });

UncontrolledDropdown.Toggle = _DropdownToggle2['default'];
UncontrolledDropdown.Menu = _DropdownMenu2['default'];

exports['default'] = UncontrolledDropdown;
module.exports = exports['default'];

/***/ }),

/***/ 424:
/*!**************************************************************************************!*\
  !*** /home/sven/ideaprojects/exceed/node_modules/react-bootstrap/lib/ButtonGroup.js ***!
  \**************************************************************************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


exports.__esModule = true;

var _extends3 = __webpack_require__(/*! babel-runtime/helpers/extends */ 8);

var _extends4 = _interopRequireDefault(_extends3);

var _objectWithoutProperties2 = __webpack_require__(/*! babel-runtime/helpers/objectWithoutProperties */ 10);

var _objectWithoutProperties3 = _interopRequireDefault(_objectWithoutProperties2);

var _classCallCheck2 = __webpack_require__(/*! babel-runtime/helpers/classCallCheck */ 5);

var _classCallCheck3 = _interopRequireDefault(_classCallCheck2);

var _possibleConstructorReturn2 = __webpack_require__(/*! babel-runtime/helpers/possibleConstructorReturn */ 6);

var _possibleConstructorReturn3 = _interopRequireDefault(_possibleConstructorReturn2);

var _inherits2 = __webpack_require__(/*! babel-runtime/helpers/inherits */ 7);

var _inherits3 = _interopRequireDefault(_inherits2);

var _classnames = __webpack_require__(/*! classnames */ 1);

var _classnames2 = _interopRequireDefault(_classnames);

var _react = __webpack_require__(/*! react */ 0);

var _react2 = _interopRequireDefault(_react);

var _propTypes = __webpack_require__(/*! prop-types */ 2);

var _propTypes2 = _interopRequireDefault(_propTypes);

var _all = __webpack_require__(/*! prop-types-extra/lib/all */ 93);

var _all2 = _interopRequireDefault(_all);

var _Button = __webpack_require__(/*! ./Button */ 145);

var _Button2 = _interopRequireDefault(_Button);

var _bootstrapUtils = __webpack_require__(/*! ./utils/bootstrapUtils */ 11);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { 'default': obj }; }

var propTypes = {
  vertical: _propTypes2['default'].bool,
  justified: _propTypes2['default'].bool,

  /**
   * Display block buttons; only useful when used with the "vertical" prop.
   * @type {bool}
   */
  block: (0, _all2['default'])(_propTypes2['default'].bool, function (_ref) {
    var block = _ref.block,
        vertical = _ref.vertical;
    return block && !vertical ? new Error('`block` requires `vertical` to be set to have any effect') : null;
  })
};

var defaultProps = {
  block: false,
  justified: false,
  vertical: false
};

var ButtonGroup = function (_React$Component) {
  (0, _inherits3['default'])(ButtonGroup, _React$Component);

  function ButtonGroup() {
    (0, _classCallCheck3['default'])(this, ButtonGroup);
    return (0, _possibleConstructorReturn3['default'])(this, _React$Component.apply(this, arguments));
  }

  ButtonGroup.prototype.render = function render() {
    var _extends2;

    var _props = this.props,
        block = _props.block,
        justified = _props.justified,
        vertical = _props.vertical,
        className = _props.className,
        props = (0, _objectWithoutProperties3['default'])(_props, ['block', 'justified', 'vertical', 'className']);

    var _splitBsProps = (0, _bootstrapUtils.splitBsProps)(props),
        bsProps = _splitBsProps[0],
        elementProps = _splitBsProps[1];

    var classes = (0, _extends4['default'])({}, (0, _bootstrapUtils.getClassSet)(bsProps), (_extends2 = {}, _extends2[(0, _bootstrapUtils.prefix)(bsProps)] = !vertical, _extends2[(0, _bootstrapUtils.prefix)(bsProps, 'vertical')] = vertical, _extends2[(0, _bootstrapUtils.prefix)(bsProps, 'justified')] = justified, _extends2[(0, _bootstrapUtils.prefix)(_Button2['default'].defaultProps, 'block')] = block, _extends2));

    return _react2['default'].createElement('div', (0, _extends4['default'])({}, elementProps, {
      className: (0, _classnames2['default'])(className, classes)
    }));
  };

  return ButtonGroup;
}(_react2['default'].Component);

ButtonGroup.propTypes = propTypes;
ButtonGroup.defaultProps = defaultProps;

exports['default'] = (0, _bootstrapUtils.bsClass)('btn-group', ButtonGroup);
module.exports = exports['default'];

/***/ }),

/***/ 428:
/*!***************************************************************************************!*\
  !*** /home/sven/ideaprojects/exceed/node_modules/react-bootstrap/lib/DropdownMenu.js ***!
  \***************************************************************************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


exports.__esModule = true;

var _extends3 = __webpack_require__(/*! babel-runtime/helpers/extends */ 8);

var _extends4 = _interopRequireDefault(_extends3);

var _objectWithoutProperties2 = __webpack_require__(/*! babel-runtime/helpers/objectWithoutProperties */ 10);

var _objectWithoutProperties3 = _interopRequireDefault(_objectWithoutProperties2);

var _from = __webpack_require__(/*! babel-runtime/core-js/array/from */ 429);

var _from2 = _interopRequireDefault(_from);

var _classCallCheck2 = __webpack_require__(/*! babel-runtime/helpers/classCallCheck */ 5);

var _classCallCheck3 = _interopRequireDefault(_classCallCheck2);

var _possibleConstructorReturn2 = __webpack_require__(/*! babel-runtime/helpers/possibleConstructorReturn */ 6);

var _possibleConstructorReturn3 = _interopRequireDefault(_possibleConstructorReturn2);

var _inherits2 = __webpack_require__(/*! babel-runtime/helpers/inherits */ 7);

var _inherits3 = _interopRequireDefault(_inherits2);

var _classnames = __webpack_require__(/*! classnames */ 1);

var _classnames2 = _interopRequireDefault(_classnames);

var _keycode = __webpack_require__(/*! keycode */ 142);

var _keycode2 = _interopRequireDefault(_keycode);

var _react = __webpack_require__(/*! react */ 0);

var _react2 = _interopRequireDefault(_react);

var _propTypes = __webpack_require__(/*! prop-types */ 2);

var _propTypes2 = _interopRequireDefault(_propTypes);

var _reactDom = __webpack_require__(/*! react-dom */ 13);

var _reactDom2 = _interopRequireDefault(_reactDom);

var _RootCloseWrapper = __webpack_require__(/*! react-overlays/lib/RootCloseWrapper */ 118);

var _RootCloseWrapper2 = _interopRequireDefault(_RootCloseWrapper);

var _bootstrapUtils = __webpack_require__(/*! ./utils/bootstrapUtils */ 11);

var _createChainedFunction = __webpack_require__(/*! ./utils/createChainedFunction */ 18);

var _createChainedFunction2 = _interopRequireDefault(_createChainedFunction);

var _ValidComponentChildren = __webpack_require__(/*! ./utils/ValidComponentChildren */ 74);

var _ValidComponentChildren2 = _interopRequireDefault(_ValidComponentChildren);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { 'default': obj }; }

var propTypes = {
  open: _propTypes2['default'].bool,
  pullRight: _propTypes2['default'].bool,
  onClose: _propTypes2['default'].func,
  labelledBy: _propTypes2['default'].oneOfType([_propTypes2['default'].string, _propTypes2['default'].number]),
  onSelect: _propTypes2['default'].func,
  rootCloseEvent: _propTypes2['default'].oneOf(['click', 'mousedown'])
};

var defaultProps = {
  bsRole: 'menu',
  pullRight: false
};

var DropdownMenu = function (_React$Component) {
  (0, _inherits3['default'])(DropdownMenu, _React$Component);

  function DropdownMenu(props) {
    (0, _classCallCheck3['default'])(this, DropdownMenu);

    var _this = (0, _possibleConstructorReturn3['default'])(this, _React$Component.call(this, props));

    _this.handleRootClose = _this.handleRootClose.bind(_this);
    _this.handleKeyDown = _this.handleKeyDown.bind(_this);
    return _this;
  }

  DropdownMenu.prototype.handleRootClose = function handleRootClose(event) {
    this.props.onClose(event, { source: 'rootClose' });
  };

  DropdownMenu.prototype.handleKeyDown = function handleKeyDown(event) {
    switch (event.keyCode) {
      case _keycode2['default'].codes.down:
        this.focusNext();
        event.preventDefault();
        break;
      case _keycode2['default'].codes.up:
        this.focusPrevious();
        event.preventDefault();
        break;
      case _keycode2['default'].codes.esc:
      case _keycode2['default'].codes.tab:
        this.props.onClose(event, { source: 'keydown' });
        break;
      default:
    }
  };

  DropdownMenu.prototype.getItemsAndActiveIndex = function getItemsAndActiveIndex() {
    var items = this.getFocusableMenuItems();
    var activeIndex = items.indexOf(document.activeElement);

    return { items: items, activeIndex: activeIndex };
  };

  DropdownMenu.prototype.getFocusableMenuItems = function getFocusableMenuItems() {
    var node = _reactDom2['default'].findDOMNode(this);
    if (!node) {
      return [];
    }

    return (0, _from2['default'])(node.querySelectorAll('[tabIndex="-1"]'));
  };

  DropdownMenu.prototype.focusNext = function focusNext() {
    var _getItemsAndActiveInd = this.getItemsAndActiveIndex(),
        items = _getItemsAndActiveInd.items,
        activeIndex = _getItemsAndActiveInd.activeIndex;

    if (items.length === 0) {
      return;
    }

    var nextIndex = activeIndex === items.length - 1 ? 0 : activeIndex + 1;
    items[nextIndex].focus();
  };

  DropdownMenu.prototype.focusPrevious = function focusPrevious() {
    var _getItemsAndActiveInd2 = this.getItemsAndActiveIndex(),
        items = _getItemsAndActiveInd2.items,
        activeIndex = _getItemsAndActiveInd2.activeIndex;

    if (items.length === 0) {
      return;
    }

    var prevIndex = activeIndex === 0 ? items.length - 1 : activeIndex - 1;
    items[prevIndex].focus();
  };

  DropdownMenu.prototype.render = function render() {
    var _extends2,
        _this2 = this;

    var _props = this.props,
        open = _props.open,
        pullRight = _props.pullRight,
        labelledBy = _props.labelledBy,
        onSelect = _props.onSelect,
        className = _props.className,
        rootCloseEvent = _props.rootCloseEvent,
        children = _props.children,
        props = (0, _objectWithoutProperties3['default'])(_props, ['open', 'pullRight', 'labelledBy', 'onSelect', 'className', 'rootCloseEvent', 'children']);

    var _splitBsPropsAndOmit = (0, _bootstrapUtils.splitBsPropsAndOmit)(props, ['onClose']),
        bsProps = _splitBsPropsAndOmit[0],
        elementProps = _splitBsPropsAndOmit[1];

    var classes = (0, _extends4['default'])({}, (0, _bootstrapUtils.getClassSet)(bsProps), (_extends2 = {}, _extends2[(0, _bootstrapUtils.prefix)(bsProps, 'right')] = pullRight, _extends2));

    return _react2['default'].createElement(
      _RootCloseWrapper2['default'],
      {
        disabled: !open,
        onRootClose: this.handleRootClose,
        event: rootCloseEvent
      },
      _react2['default'].createElement(
        'ul',
        (0, _extends4['default'])({}, elementProps, {
          role: 'menu',
          className: (0, _classnames2['default'])(className, classes),
          'aria-labelledby': labelledBy
        }),
        _ValidComponentChildren2['default'].map(children, function (child) {
          return _react2['default'].cloneElement(child, {
            onKeyDown: (0, _createChainedFunction2['default'])(child.props.onKeyDown, _this2.handleKeyDown),
            onSelect: (0, _createChainedFunction2['default'])(child.props.onSelect, onSelect)
          });
        })
      )
    );
  };

  return DropdownMenu;
}(_react2['default'].Component);

DropdownMenu.propTypes = propTypes;
DropdownMenu.defaultProps = defaultProps;

exports['default'] = (0, _bootstrapUtils.bsClass)('dropdown-menu', DropdownMenu);
module.exports = exports['default'];

/***/ }),

/***/ 429:
/*!***************************************************************************************!*\
  !*** /home/sven/ideaprojects/exceed/node_modules/babel-runtime/core-js/array/from.js ***!
  \***************************************************************************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

module.exports = { "default": __webpack_require__(/*! core-js/library/fn/array/from */ 430), __esModule: true };

/***/ }),

/***/ 430:
/*!************************************************************************************!*\
  !*** /home/sven/ideaprojects/exceed/node_modules/core-js/library/fn/array/from.js ***!
  \************************************************************************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

__webpack_require__(/*! ../../modules/es6.string.iterator */ 197);
__webpack_require__(/*! ../../modules/es6.array.from */ 431);
module.exports = __webpack_require__(/*! ../../modules/_core */ 21).Array.from;


/***/ }),

/***/ 431:
/*!*********************************************************************************************!*\
  !*** /home/sven/ideaprojects/exceed/node_modules/core-js/library/modules/es6.array.from.js ***!
  \*********************************************************************************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

"use strict";

var ctx = __webpack_require__(/*! ./_ctx */ 124);
var $export = __webpack_require__(/*! ./_export */ 33);
var toObject = __webpack_require__(/*! ./_to-object */ 132);
var call = __webpack_require__(/*! ./_iter-call */ 432);
var isArrayIter = __webpack_require__(/*! ./_is-array-iter */ 433);
var toLength = __webpack_require__(/*! ./_to-length */ 204);
var createProperty = __webpack_require__(/*! ./_create-property */ 434);
var getIterFn = __webpack_require__(/*! ./core.get-iterator-method */ 435);

$export($export.S + $export.F * !__webpack_require__(/*! ./_iter-detect */ 437)(function (iter) { Array.from(iter); }), 'Array', {
  // 22.1.2.1 Array.from(arrayLike, mapfn = undefined, thisArg = undefined)
  from: function from(arrayLike /* , mapfn = undefined, thisArg = undefined */) {
    var O = toObject(arrayLike);
    var C = typeof this == 'function' ? this : Array;
    var aLen = arguments.length;
    var mapfn = aLen > 1 ? arguments[1] : undefined;
    var mapping = mapfn !== undefined;
    var index = 0;
    var iterFn = getIterFn(O);
    var length, result, step, iterator;
    if (mapping) mapfn = ctx(mapfn, aLen > 2 ? arguments[2] : undefined, 2);
    // if object isn't iterable or it's array with default iterator - use simple case
    if (iterFn != undefined && !(C == Array && isArrayIter(iterFn))) {
      for (iterator = iterFn.call(O), result = new C(); !(step = iterator.next()).done; index++) {
        createProperty(result, index, mapping ? call(iterator, mapfn, [step.value, index], true) : step.value);
      }
    } else {
      length = toLength(O.length);
      for (result = new C(length); length > index; index++) {
        createProperty(result, index, mapping ? mapfn(O[index], index) : O[index]);
      }
    }
    result.length = index;
    return result;
  }
});


/***/ }),

/***/ 432:
/*!*****************************************************************************************!*\
  !*** /home/sven/ideaprojects/exceed/node_modules/core-js/library/modules/_iter-call.js ***!
  \*****************************************************************************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

// call something on iterator step with safe closing on error
var anObject = __webpack_require__(/*! ./_an-object */ 52);
module.exports = function (iterator, fn, value, entries) {
  try {
    return entries ? fn(anObject(value)[0], value[1]) : fn(value);
  // 7.4.6 IteratorClose(iterator, completion)
  } catch (e) {
    var ret = iterator['return'];
    if (ret !== undefined) anObject(ret.call(iterator));
    throw e;
  }
};


/***/ }),

/***/ 433:
/*!*********************************************************************************************!*\
  !*** /home/sven/ideaprojects/exceed/node_modules/core-js/library/modules/_is-array-iter.js ***!
  \*********************************************************************************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

// check on default Array iterator
var Iterators = __webpack_require__(/*! ./_iterators */ 71);
var ITERATOR = __webpack_require__(/*! ./_wks */ 22)('iterator');
var ArrayProto = Array.prototype;

module.exports = function (it) {
  return it !== undefined && (Iterators.Array === it || ArrayProto[ITERATOR] === it);
};


/***/ }),

/***/ 434:
/*!***********************************************************************************************!*\
  !*** /home/sven/ideaprojects/exceed/node_modules/core-js/library/modules/_create-property.js ***!
  \***********************************************************************************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

"use strict";

var $defineProperty = __webpack_require__(/*! ./_object-dp */ 37);
var createDesc = __webpack_require__(/*! ./_property-desc */ 70);

module.exports = function (object, index, value) {
  if (index in object) $defineProperty.f(object, index, createDesc(0, value));
  else object[index] = value;
};


/***/ }),

/***/ 435:
/*!*******************************************************************************************************!*\
  !*** /home/sven/ideaprojects/exceed/node_modules/core-js/library/modules/core.get-iterator-method.js ***!
  \*******************************************************************************************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

var classof = __webpack_require__(/*! ./_classof */ 436);
var ITERATOR = __webpack_require__(/*! ./_wks */ 22)('iterator');
var Iterators = __webpack_require__(/*! ./_iterators */ 71);
module.exports = __webpack_require__(/*! ./_core */ 21).getIteratorMethod = function (it) {
  if (it != undefined) return it[ITERATOR]
    || it['@@iterator']
    || Iterators[classof(it)];
};


/***/ }),

/***/ 436:
/*!***************************************************************************************!*\
  !*** /home/sven/ideaprojects/exceed/node_modules/core-js/library/modules/_classof.js ***!
  \***************************************************************************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

// getting tag from 19.1.3.6 Object.prototype.toString()
var cof = __webpack_require__(/*! ./_cof */ 127);
var TAG = __webpack_require__(/*! ./_wks */ 22)('toStringTag');
// ES3 wrong here
var ARG = cof(function () { return arguments; }()) == 'Arguments';

// fallback for IE11 Script Access Denied error
var tryGet = function (it, key) {
  try {
    return it[key];
  } catch (e) { /* empty */ }
};

module.exports = function (it) {
  var O, T, B;
  return it === undefined ? 'Undefined' : it === null ? 'Null'
    // @@toStringTag case
    : typeof (T = tryGet(O = Object(it), TAG)) == 'string' ? T
    // builtinTag case
    : ARG ? cof(O)
    // ES3 arguments fallback
    : (B = cof(O)) == 'Object' && typeof O.callee == 'function' ? 'Arguments' : B;
};


/***/ }),

/***/ 437:
/*!*******************************************************************************************!*\
  !*** /home/sven/ideaprojects/exceed/node_modules/core-js/library/modules/_iter-detect.js ***!
  \*******************************************************************************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

var ITERATOR = __webpack_require__(/*! ./_wks */ 22)('iterator');
var SAFE_CLOSING = false;

try {
  var riter = [7][ITERATOR]();
  riter['return'] = function () { SAFE_CLOSING = true; };
  // eslint-disable-next-line no-throw-literal
  Array.from(riter, function () { throw 2; });
} catch (e) { /* empty */ }

module.exports = function (exec, skipClosing) {
  if (!skipClosing && !SAFE_CLOSING) return false;
  var safe = false;
  try {
    var arr = [7];
    var iter = arr[ITERATOR]();
    iter.next = function () { return { done: safe = true }; };
    arr[ITERATOR] = function () { return iter; };
    exec(arr);
  } catch (e) { /* empty */ }
  return safe;
};


/***/ }),

/***/ 438:
/*!*****************************************************************************************!*\
  !*** /home/sven/ideaprojects/exceed/node_modules/react-bootstrap/lib/DropdownToggle.js ***!
  \*****************************************************************************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


exports.__esModule = true;

var _extends2 = __webpack_require__(/*! babel-runtime/helpers/extends */ 8);

var _extends3 = _interopRequireDefault(_extends2);

var _objectWithoutProperties2 = __webpack_require__(/*! babel-runtime/helpers/objectWithoutProperties */ 10);

var _objectWithoutProperties3 = _interopRequireDefault(_objectWithoutProperties2);

var _classCallCheck2 = __webpack_require__(/*! babel-runtime/helpers/classCallCheck */ 5);

var _classCallCheck3 = _interopRequireDefault(_classCallCheck2);

var _possibleConstructorReturn2 = __webpack_require__(/*! babel-runtime/helpers/possibleConstructorReturn */ 6);

var _possibleConstructorReturn3 = _interopRequireDefault(_possibleConstructorReturn2);

var _inherits2 = __webpack_require__(/*! babel-runtime/helpers/inherits */ 7);

var _inherits3 = _interopRequireDefault(_inherits2);

var _react = __webpack_require__(/*! react */ 0);

var _react2 = _interopRequireDefault(_react);

var _propTypes = __webpack_require__(/*! prop-types */ 2);

var _propTypes2 = _interopRequireDefault(_propTypes);

var _classnames = __webpack_require__(/*! classnames */ 1);

var _classnames2 = _interopRequireDefault(_classnames);

var _Button = __webpack_require__(/*! ./Button */ 145);

var _Button2 = _interopRequireDefault(_Button);

var _SafeAnchor = __webpack_require__(/*! ./SafeAnchor */ 94);

var _SafeAnchor2 = _interopRequireDefault(_SafeAnchor);

var _bootstrapUtils = __webpack_require__(/*! ./utils/bootstrapUtils */ 11);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { 'default': obj }; }

var propTypes = {
  noCaret: _propTypes2['default'].bool,
  open: _propTypes2['default'].bool,
  title: _propTypes2['default'].string,
  useAnchor: _propTypes2['default'].bool
};

var defaultProps = {
  open: false,
  useAnchor: false,
  bsRole: 'toggle'
};

var DropdownToggle = function (_React$Component) {
  (0, _inherits3['default'])(DropdownToggle, _React$Component);

  function DropdownToggle() {
    (0, _classCallCheck3['default'])(this, DropdownToggle);
    return (0, _possibleConstructorReturn3['default'])(this, _React$Component.apply(this, arguments));
  }

  DropdownToggle.prototype.render = function render() {
    var _props = this.props,
        noCaret = _props.noCaret,
        open = _props.open,
        useAnchor = _props.useAnchor,
        bsClass = _props.bsClass,
        className = _props.className,
        children = _props.children,
        props = (0, _objectWithoutProperties3['default'])(_props, ['noCaret', 'open', 'useAnchor', 'bsClass', 'className', 'children']);


    delete props.bsRole;

    var Component = useAnchor ? _SafeAnchor2['default'] : _Button2['default'];
    var useCaret = !noCaret;

    // This intentionally forwards bsSize and bsStyle (if set) to the
    // underlying component, to allow it to render size and style variants.

    // FIXME: Should this really fall back to `title` as children?

    return _react2['default'].createElement(
      Component,
      (0, _extends3['default'])({}, props, {
        role: 'button',
        className: (0, _classnames2['default'])(className, bsClass),
        'aria-haspopup': true,
        'aria-expanded': open
      }),
      children || props.title,
      useCaret && ' ',
      useCaret && _react2['default'].createElement('span', { className: 'caret' })
    );
  };

  return DropdownToggle;
}(_react2['default'].Component);

DropdownToggle.propTypes = propTypes;
DropdownToggle.defaultProps = defaultProps;

exports['default'] = (0, _bootstrapUtils.bsClass)('dropdown-toggle', DropdownToggle);
module.exports = exports['default'];

/***/ }),

/***/ 439:
/*!******************************************************************************************!*\
  !*** /home/sven/ideaprojects/exceed/node_modules/react-bootstrap/lib/utils/PropTypes.js ***!
  \******************************************************************************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


exports.__esModule = true;
exports.requiredRoles = requiredRoles;
exports.exclusiveRoles = exclusiveRoles;

var _createChainableTypeChecker = __webpack_require__(/*! prop-types-extra/lib/utils/createChainableTypeChecker */ 86);

var _createChainableTypeChecker2 = _interopRequireDefault(_createChainableTypeChecker);

var _ValidComponentChildren = __webpack_require__(/*! ./ValidComponentChildren */ 74);

var _ValidComponentChildren2 = _interopRequireDefault(_ValidComponentChildren);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { 'default': obj }; }

function requiredRoles() {
  for (var _len = arguments.length, roles = Array(_len), _key = 0; _key < _len; _key++) {
    roles[_key] = arguments[_key];
  }

  return (0, _createChainableTypeChecker2['default'])(function (props, propName, component) {
    var missing = void 0;

    roles.every(function (role) {
      if (!_ValidComponentChildren2['default'].some(props.children, function (child) {
        return child.props.bsRole === role;
      })) {
        missing = role;
        return false;
      }

      return true;
    });

    if (missing) {
      return new Error('(children) ' + component + ' - Missing a required child with bsRole: ' + (missing + '. ' + component + ' must have at least one child of each of ') + ('the following bsRoles: ' + roles.join(', ')));
    }

    return null;
  });
}

function exclusiveRoles() {
  for (var _len2 = arguments.length, roles = Array(_len2), _key2 = 0; _key2 < _len2; _key2++) {
    roles[_key2] = arguments[_key2];
  }

  return (0, _createChainableTypeChecker2['default'])(function (props, propName, component) {
    var duplicate = void 0;

    roles.every(function (role) {
      var childrenWithRole = _ValidComponentChildren2['default'].filter(props.children, function (child) {
        return child.props.bsRole === role;
      });

      if (childrenWithRole.length > 1) {
        duplicate = role;
        return false;
      }

      return true;
    });

    if (duplicate) {
      return new Error('(children) ' + component + ' - Duplicate children detected of bsRole: ' + (duplicate + '. Only one child each allowed with the following ') + ('bsRoles: ' + roles.join(', ')));
    }

    return null;
  });
}

/***/ }),

/***/ 440:
/*!***********************************************************************************!*\
  !*** /home/sven/ideaprojects/exceed/node_modules/react-bootstrap/lib/MenuItem.js ***!
  \***********************************************************************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


exports.__esModule = true;

var _extends2 = __webpack_require__(/*! babel-runtime/helpers/extends */ 8);

var _extends3 = _interopRequireDefault(_extends2);

var _objectWithoutProperties2 = __webpack_require__(/*! babel-runtime/helpers/objectWithoutProperties */ 10);

var _objectWithoutProperties3 = _interopRequireDefault(_objectWithoutProperties2);

var _classCallCheck2 = __webpack_require__(/*! babel-runtime/helpers/classCallCheck */ 5);

var _classCallCheck3 = _interopRequireDefault(_classCallCheck2);

var _possibleConstructorReturn2 = __webpack_require__(/*! babel-runtime/helpers/possibleConstructorReturn */ 6);

var _possibleConstructorReturn3 = _interopRequireDefault(_possibleConstructorReturn2);

var _inherits2 = __webpack_require__(/*! babel-runtime/helpers/inherits */ 7);

var _inherits3 = _interopRequireDefault(_inherits2);

var _classnames = __webpack_require__(/*! classnames */ 1);

var _classnames2 = _interopRequireDefault(_classnames);

var _react = __webpack_require__(/*! react */ 0);

var _react2 = _interopRequireDefault(_react);

var _propTypes = __webpack_require__(/*! prop-types */ 2);

var _propTypes2 = _interopRequireDefault(_propTypes);

var _all = __webpack_require__(/*! prop-types-extra/lib/all */ 93);

var _all2 = _interopRequireDefault(_all);

var _SafeAnchor = __webpack_require__(/*! ./SafeAnchor */ 94);

var _SafeAnchor2 = _interopRequireDefault(_SafeAnchor);

var _bootstrapUtils = __webpack_require__(/*! ./utils/bootstrapUtils */ 11);

var _createChainedFunction = __webpack_require__(/*! ./utils/createChainedFunction */ 18);

var _createChainedFunction2 = _interopRequireDefault(_createChainedFunction);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { 'default': obj }; }

var propTypes = {
  /**
   * Highlight the menu item as active.
   */
  active: _propTypes2['default'].bool,

  /**
   * Disable the menu item, making it unselectable.
   */
  disabled: _propTypes2['default'].bool,

  /**
   * Styles the menu item as a horizontal rule, providing visual separation between
   * groups of menu items.
   */
  divider: (0, _all2['default'])(_propTypes2['default'].bool, function (_ref) {
    var divider = _ref.divider,
        children = _ref.children;
    return divider && children ? new Error('Children will not be rendered for dividers') : null;
  }),

  /**
   * Value passed to the `onSelect` handler, useful for identifying the selected menu item.
   */
  eventKey: _propTypes2['default'].any,

  /**
   * Styles the menu item as a header label, useful for describing a group of menu items.
   */
  header: _propTypes2['default'].bool,

  /**
   * HTML `href` attribute corresponding to `a.href`.
   */
  href: _propTypes2['default'].string,

  /**
   * Callback fired when the menu item is clicked.
   */
  onClick: _propTypes2['default'].func,

  /**
   * Callback fired when the menu item is selected.
   *
   * ```js
   * (eventKey: any, event: Object) => any
   * ```
   */
  onSelect: _propTypes2['default'].func
};

var defaultProps = {
  divider: false,
  disabled: false,
  header: false
};

var MenuItem = function (_React$Component) {
  (0, _inherits3['default'])(MenuItem, _React$Component);

  function MenuItem(props, context) {
    (0, _classCallCheck3['default'])(this, MenuItem);

    var _this = (0, _possibleConstructorReturn3['default'])(this, _React$Component.call(this, props, context));

    _this.handleClick = _this.handleClick.bind(_this);
    return _this;
  }

  MenuItem.prototype.handleClick = function handleClick(event) {
    var _props = this.props,
        href = _props.href,
        disabled = _props.disabled,
        onSelect = _props.onSelect,
        eventKey = _props.eventKey;


    if (!href || disabled) {
      event.preventDefault();
    }

    if (disabled) {
      return;
    }

    if (onSelect) {
      onSelect(eventKey, event);
    }
  };

  MenuItem.prototype.render = function render() {
    var _props2 = this.props,
        active = _props2.active,
        disabled = _props2.disabled,
        divider = _props2.divider,
        header = _props2.header,
        onClick = _props2.onClick,
        className = _props2.className,
        style = _props2.style,
        props = (0, _objectWithoutProperties3['default'])(_props2, ['active', 'disabled', 'divider', 'header', 'onClick', 'className', 'style']);

    var _splitBsPropsAndOmit = (0, _bootstrapUtils.splitBsPropsAndOmit)(props, ['eventKey', 'onSelect']),
        bsProps = _splitBsPropsAndOmit[0],
        elementProps = _splitBsPropsAndOmit[1];

    if (divider) {
      // Forcibly blank out the children; separators shouldn't render any.
      elementProps.children = undefined;

      return _react2['default'].createElement('li', (0, _extends3['default'])({}, elementProps, {
        role: 'separator',
        className: (0, _classnames2['default'])(className, 'divider'),
        style: style
      }));
    }

    if (header) {
      return _react2['default'].createElement('li', (0, _extends3['default'])({}, elementProps, {
        role: 'heading',
        className: (0, _classnames2['default'])(className, (0, _bootstrapUtils.prefix)(bsProps, 'header')),
        style: style
      }));
    }

    return _react2['default'].createElement(
      'li',
      {
        role: 'presentation',
        className: (0, _classnames2['default'])(className, { active: active, disabled: disabled }),
        style: style
      },
      _react2['default'].createElement(_SafeAnchor2['default'], (0, _extends3['default'])({}, elementProps, {
        role: 'menuitem',
        tabIndex: '-1',
        onClick: (0, _createChainedFunction2['default'])(onClick, this.handleClick)
      }))
    );
  };

  return MenuItem;
}(_react2['default'].Component);

MenuItem.propTypes = propTypes;
MenuItem.defaultProps = defaultProps;

exports['default'] = (0, _bootstrapUtils.bsClass)('dropdown', MenuItem);
module.exports = exports['default'];

/***/ }),

/***/ 441:
/*!***************************************!*\
  !*** ./components/std/common/Test.js ***!
  \***************************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
    value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = __webpack_require__(/*! react */ 0);

var _react2 = _interopRequireDefault(_react);

var _objectAssign = __webpack_require__(/*! object-assign */ 4);

var _objectAssign2 = _interopRequireDefault(_objectAssign);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var View = function (_React$Component) {
    _inherits(View, _React$Component);

    function View() {
        _classCallCheck(this, View);

        return _possibleConstructorReturn(this, (View.__proto__ || Object.getPrototypeOf(View)).apply(this, arguments));
    }

    _createClass(View, [{
        key: "render",
        value: function render() {
            var props = (0, _objectAssign2.default)({}, this.props);

            delete props.children;

            return _react2.default.createElement(
                "div",
                null,
                _react2.default.createElement(
                    "p",
                    null,
                    "Props:"
                ),
                _react2.default.createElement(
                    "pre",
                    null,
                    JSON.stringify(props, null, 4)
                ),
                _react2.default.createElement(
                    "p",
                    null,
                    "Children: ",
                    this.props.children
                )
            );
        }
    }]);

    return View;
}(_react2.default.Component);

exports.default = View;

/***/ }),

/***/ 442:
/*!***********************************************!*\
  !*** ./components/std/common/components.json ***!
  \***********************************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports) {

module.exports = {"components":{"Heading":{"description":"Heading element","classes":["element"],"propTypes":{"value":{"description":"Heading text","type":"PLAINTEXT"}}},"ResourceImage":{"description":"Displays an exceed resource as image","classes":["element"],"propTypes":{"resource":{"type":"PLAINTEXT"},"alt":{"type":"PLAINTEXT","required":true}},"childRule":"component('Link.Param')"},"Link":{"description":"Application link to a standalone-view or process start","classes":["element","toolbar-child"],"propTypes":{"location":{"rule":"location()","required":true},"text":{"type":"PLAINTEXT","required":true}},"childRule":"component('Link.Param')"},"Link.Param":{"description":"Parameter for a Link element","propTypes":{"name":{"type":"PLAINTEXT","rule":"locationParams()","required":true},"value":{"required":true}}},"Footer":{"description":"Footer element for layouts","classes":["top-level","element"]},"Error":{"description":"Component wrapper for the error component.","classes":["top-level","element"],"propTypes":{"error":{"description":"Error value","type":"VALUE_EXPRESSION","required":true}}},"InfoBlock":{"description":"Informational text block","classes":["top-level","element"],"propTypes":{"heading":{"description":"Error value","type":"VALUE_EXPRESSION","defaultValue":"'Info'","required":true},"text":{"description":"Error value","type":"VALUE_EXPRESSION","required":true}}},"Content":{"description":"Component used in templates to import content areas from the view using that template.","classes":["element"],"propTypes":{"name":{"description":"Name of the content area to import.","type":"PLAINTEXT","required":true,"defaultValue":"'main'"}}},"Dialog":{"description":"Modal dialog to be displayed in the view. Can be controlled by actions (e.g. '{ myDialog = DialogState.OPEN }') ","classes":["element"],"propTypes":{"isOpen":{"description":"If set to true, the dialog will be open by default. Useful sometimes.","type":"VALUE_EXPRESSION"},"title":{"description":"Title of the dialog","type":"PLAINTEXT"}}},"StandardNav":{"description":"Default implementation of an application navigation. Its children will be displayed in the branding part.","classes":["element"],"propTypes":{"showBranding":{"description":"If false, don't display the branding part of the navbar.","type":"BOOLEAN","defaultValue":"true"},"showNormal":{"description":"If false, don't display the normal app dropdown, even if there are normal entries.","type":"BOOLEAN","defaultValue":"true"},"showAdmin":{"description":"If false, don't display the admin dropdown, even if there are admin entries.","type":"BOOLEAN","defaultValue":"true"}}}}}

/***/ }),

/***/ 443:
/*!*******************************************!*\
  !*** ./components/std/counter/Counter.js ***!
  \*******************************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
    value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = __webpack_require__(/*! react */ 0);

var _react2 = _interopRequireDefault(_react);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var Counter = function (_React$Component) {
    _inherits(Counter, _React$Component);

    function Counter() {
        var _ref;

        var _temp, _this, _ret;

        _classCallCheck(this, Counter);

        for (var _len = arguments.length, args = Array(_len), _key = 0; _key < _len; _key++) {
            args[_key] = arguments[_key];
        }

        return _ret = (_temp = (_this = _possibleConstructorReturn(this, (_ref = Counter.__proto__ || Object.getPrototypeOf(Counter)).call.apply(_ref, [this].concat(args))), _this), _this.state = { count: _this.props.value || 0 }, _temp), _possibleConstructorReturn(_this, _ret);
    }

    _createClass(Counter, [{
        key: "render",
        value: function render() {
            var _this2 = this;

            return _react2.default.createElement(
                "div",
                { className: "counter" },
                _react2.default.createElement(
                    "h3",
                    null,
                    this.state.count
                ),
                _react2.default.createElement("input", { type: "submit", className: "btn btn-primary", value: "++", onClick: function onClick() {
                        _this2.setState({
                            count: _this2.state.count + 1
                        });
                    } })
            );
        }
    }]);

    return Counter;
}(_react2.default.Component);

;

exports.default = Counter;

/***/ }),

/***/ 444:
/*!************************************************!*\
  !*** ./components/std/counter/components.json ***!
  \************************************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports) {

module.exports = {"components":{"Counter":{"description":"Simple test component offering a counter based on local component state","classes":["element"],"propTypes":{"value":{"description":"Initial value","type":"INTEGER","rule":"integer()"}}}}}

/***/ }),

/***/ 445:
/*!*********************************************!*\
  !*** ./components/std/datagrid/DataGrid.js ***!
  \*********************************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
    value: true
});
exports.Column = undefined;

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _component = __webpack_require__(/*! ../../../actions/component */ 45);

var _store = __webpack_require__(/*! ../../../service/store */ 3);

var _store2 = _interopRequireDefault(_store);

var _react = __webpack_require__(/*! react */ 0);

var _react2 = _interopRequireDefault(_react);

var _valueLink = __webpack_require__(/*! ../../../util/value-link */ 139);

var _valueLink2 = _interopRequireDefault(_valueLink);

var _graph = __webpack_require__(/*! ../../../domain/graph */ 31);

var _graph2 = _interopRequireDefault(_graph);

var _cursor = __webpack_require__(/*! ../../../domain/cursor */ 27);

var _cursor2 = _interopRequireDefault(_cursor);

var _immutabilityHelper = __webpack_require__(/*! immutability-helper */ 43);

var _immutabilityHelper2 = _interopRequireDefault(_immutabilityHelper);

var _domain = __webpack_require__(/*! ../../../service/domain */ 16);

var _domain2 = _interopRequireDefault(_domain);

var _propertyRenderer = __webpack_require__(/*! ../../../service/property-renderer */ 61);

var _propertyRenderer2 = _interopRequireDefault(_propertyRenderer);

var _debounce = __webpack_require__(/*! ../../../util/debounce */ 230);

var _debounce2 = _interopRequireDefault(_debounce);

var _PagingComponent = __webpack_require__(/*! ../../../ui/PagingComponent */ 231);

var _PagingComponent2 = _interopRequireDefault(_PagingComponent);

var _renderWithContext = __webpack_require__(/*! ../../../util/render-with-context */ 98);

var _renderWithContext2 = _interopRequireDefault(_renderWithContext);

var _classnames = __webpack_require__(/*! classnames */ 1);

var _classnames2 = _interopRequireDefault(_classnames);

var _i18n = __webpack_require__(/*! ../../../service/i18n */ 9);

var _i18n2 = _interopRequireDefault(_i18n);

var _propTypes = __webpack_require__(/*! prop-types */ 2);

var _propTypes2 = _interopRequireDefault(_propTypes);

var _PropertySelect = __webpack_require__(/*! ../form/PropertySelect */ 138);

var _PropertySelect2 = _interopRequireDefault(_PropertySelect);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _defineProperty(obj, key, value) { if (key in obj) { Object.defineProperty(obj, key, { value: value, enumerable: true, configurable: true, writable: true }); } else { obj[key] = value; } return obj; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

/**
 * Additional classes per property type, currently used for text-alignment. The visualization of properties is
 * controlled mostly by propertyContext.renderStatic() 
 */
var COLUMN_ALIGNMENT = {
    "Currency": "text-right",
    "Integer": "text-right",
    "Long": "text-right",
    "Decimal": "text-right",
    "Boolean": "text-center"
};

function supplyBooleanValues() {
    return [_react2.default.createElement(
        "option",
        { key: 'none', value: "" },
        (0, _i18n2.default)('---')
    ), _react2.default.createElement(
        "option",
        { key: 'true', value: true },
        (0, _i18n2.default)('True')
    ), _react2.default.createElement(
        "option",
        { key: 'false', value: false },
        (0, _i18n2.default)('False')
    )];
}

function supplyStateMachineValues(_ref) {
    var propertyType = _ref.propertyType;

    var stateMachineModel = _domain2.default.getStateMachine(propertyType.typeParam);

    var options = [_react2.default.createElement(
        "option",
        { key: 'none', value: "" },
        (0, _i18n2.default)('---')
    )];
    var states = stateMachineModel.states;

    for (var name in states) {
        if (states.hasOwnProperty(name)) {
            options.push(_react2.default.createElement(
                "option",
                { key: name, value: name },
                (0, _i18n2.default)(stateMachineModel.name + " " + name)
            ));
        }
    }

    return options;
}

function supplyEnumValues(_ref2) {
    var propertyType = _ref2.propertyType;

    var enumModel = _domain2.default.getEnumType(propertyType.typeParam);

    var options = enumModel.values.map(function (name, idx) {
        return _react2.default.createElement(
            "option",
            { key: name, value: idx },
            (0, _i18n2.default)(enumModel.name + " " + name)
        );
    });

    options.unshift(_react2.default.createElement(
        "option",
        { key: 'none', value: "" },
        (0, _i18n2.default)('---')
    ));

    return options;
}

var Column = exports.Column = function (_React$Component) {
    _inherits(Column, _React$Component);

    function Column() {
        _classCallCheck(this, Column);

        return _possibleConstructorReturn(this, (Column.__proto__ || Object.getPrototypeOf(Column)).apply(this, arguments));
    }

    _createClass(Column, [{
        key: "render",
        value: function render() {
            if (typeof this.props.children === "function") {
                return _react2.default.createElement(
                    "td",
                    null,
                    this.props.children(this.props.context)
                );
            } else {
                var cursor = this.props.context;
                var propertyType = cursor.getPropertyType(null);

                var value = this.props.context.get();

                return _react2.default.createElement(
                    "td",
                    null,
                    _react2.default.createElement(
                        "p",
                        { className: (0, _classnames2.default)("form-control-static", COLUMN_ALIGNMENT[propertyType.type]) },
                        _propertyRenderer2.default.renderStatic(value, propertyType)
                    )
                );
            }
        }
    }]);

    return Column;
}(_react2.default.Component);

Column.propTypes = {
    name: _propTypes2.default.string.isRequired
};

var FilterField = function (_React$Component2) {
    _inherits(FilterField, _React$Component2);

    function FilterField() {
        var _ref3;

        var _temp, _this2, _ret;

        _classCallCheck(this, FilterField);

        for (var _len = arguments.length, args = Array(_len), _key = 0; _key < _len; _key++) {
            args[_key] = arguments[_key];
        }

        return _ret = (_temp = (_this2 = _possibleConstructorReturn(this, (_ref3 = FilterField.__proto__ || Object.getPrototypeOf(FilterField)).call.apply(_ref3, [this].concat(args))), _this2), _this2.handleChange = function (value) {
            var _this2$props = _this2.props,
                name = _this2$props.name,
                setFilter = _this2$props.setFilter;


            setFilter({
                name: name,
                value: value
            });
        }, _this2.debouncedChange = (0, _debounce2.default)(_this2.handleChange, 250), _temp), _possibleConstructorReturn(_this2, _ret);
    }

    _createClass(FilterField, [{
        key: "render",
        value: function render() {
            var _this3 = this;

            var _props = this.props,
                value = _props.value,
                propertyType = _props.propertyType;


            if (propertyType.type === "State") {
                return _react2.default.createElement(
                    "td",
                    null,
                    _react2.default.createElement(_PropertySelect2.default, {
                        value: value,
                        disabled: false,
                        className: "",
                        propertyType: propertyType,
                        supplier: supplyStateMachineValues,
                        onChange: this.handleChange
                    })
                );
            }

            if (propertyType.type === "Enum") {
                return _react2.default.createElement(
                    "td",
                    null,
                    _react2.default.createElement(_PropertySelect2.default, {
                        value: value,
                        disabled: false,
                        className: "",
                        propertyType: propertyType,
                        supplier: supplyEnumValues,
                        onChange: this.handleChange
                    })
                );
            }

            if (propertyType.type === "Boolean") {
                return _react2.default.createElement(
                    "td",
                    null,
                    _react2.default.createElement(_PropertySelect2.default, {
                        value: value,
                        disabled: false,
                        className: "",
                        propertyType: propertyType,
                        supplier: supplyBooleanValues,
                        onChange: this.handleChange
                    })
                );
            }

            return _react2.default.createElement(
                "td",
                null,
                _react2.default.createElement("input", { type: "text", className: "form-control", defaultValue: value,
                    onChange: function onChange(ev) {
                        _this3.debouncedChange(ev.target.value);
                    } })
            );
        }
    }]);

    return FilterField;
}(_react2.default.Component);

var Header = function (_React$Component3) {
    _inherits(Header, _React$Component3);

    function Header() {
        var _ref4;

        var _temp2, _this4, _ret2;

        _classCallCheck(this, Header);

        for (var _len2 = arguments.length, args = Array(_len2), _key2 = 0; _key2 < _len2; _key2++) {
            args[_key2] = arguments[_key2];
        }

        return _ret2 = (_temp2 = (_this4 = _possibleConstructorReturn(this, (_ref4 = Header.__proto__ || Object.getPrototypeOf(Header)).call.apply(_ref4, [this].concat(args))), _this4), _this4.toggle = function (ev) {
            var _this4$props = _this4.props,
                currentSortLink = _this4$props.currentSortLink,
                sort = _this4$props.sort;


            var currentSort = currentSortLink.value;

            if (currentSort && currentSort.length === 1 && currentSort[0] === sort) {
                currentSortLink.requestChange("!" + sort);
            } else {
                currentSortLink.requestChange(sort);
            }
            ev.preventDefault();
        }, _temp2), _possibleConstructorReturn(_this4, _ret2);
    }

    _createClass(Header, [{
        key: "render",
        value: function render() {
            var _props2 = this.props,
                currentSortLink = _props2.currentSortLink,
                sort = _props2.sort,
                heading = _props2.heading;

            var currentSort = currentSortLink.value;

            //console.log("SORT", currentSort, sort);

            var arrow = false;
            if (currentSort && currentSort.length === 1) {
                if (currentSort[0] === sort) {
                    arrow = _react2.default.createElement(
                        "span",
                        { className: "sort-indicator" },
                        _react2.default.createElement("span", { className: "glyphicon glyphicon-sort-by-attributes" })
                    );
                } else if (currentSort[0] === "!" + sort) {
                    arrow = _react2.default.createElement(
                        "span",
                        { className: "sort-indicator" },
                        _react2.default.createElement("span", { className: "glyphicon glyphicon-sort-by-attributes-alt" })
                    );
                }
            }

            return _react2.default.createElement(
                "th",
                null,
                _react2.default.createElement(
                    "a",
                    { className: "header", href: "#sort", onClick: this.toggle },
                    heading
                ),
                arrow
            );
        }
    }]);

    return Header;
}(_react2.default.Component);

var DataGrid = function (_React$Component4) {
    _inherits(DataGrid, _React$Component4);

    function DataGrid() {
        var _ref5;

        var _temp3, _this5, _ret3;

        _classCallCheck(this, DataGrid);

        for (var _len3 = arguments.length, args = Array(_len3), _key3 = 0; _key3 < _len3; _key3++) {
            args[_key3] = arguments[_key3];
        }

        return _ret3 = (_temp3 = (_this5 = _possibleConstructorReturn(this, (_ref5 = DataGrid.__proto__ || Object.getPrototypeOf(DataGrid)).call.apply(_ref5, [this].concat(args))), _this5), _this5.changeSort = function (newValue) {
            _store2.default.dispatch((0, _component.updateComponent)(_this5.props.id, {
                orderBy: [newValue],
                // restart paging on sort change
                offset: 0
            }));
        }, _this5.setFilter = function (v) {
            var _this5$props = _this5.props,
                id = _this5$props.id,
                vars = _this5$props.vars;

            _store2.default.dispatch((0, _component.updateComponent)(id, {
                filter: (0, _immutabilityHelper2.default)(vars.filter, _defineProperty({}, v.name, { $set: v.value })),
                // restart paging on filter change
                offset: 0
            }));
        }, _this5.setPagingOffset = function (offset) {
            _store2.default.dispatch((0, _component.updateComponent)(_this5.props.id, { offset: offset }));
        }, _temp3), _possibleConstructorReturn(_this5, _ret3);
    }

    _createClass(DataGrid, [{
        key: "renderHeader",
        value: function renderHeader() {
            var _props3 = this.props,
                vars = _props3.vars,
                model = _props3.model,
                result = _props3.result;

            var currentSortLink = new _valueLink2.default(vars.orderBy, this.changeSort);

            var kids = model.kids;
            var headers = [];
            for (var i = 0; i < kids.length; i++) {
                var kid = kids[i];
                if (kid.name !== "DataGrid.Column") {
                    throw new Error("Datagrid component should only have Datagrid.Column children.");
                }

                var name = kid.attrs.name;

                //console.log("kid", name, kid);

                var column = result.columns[name];
                headers.push(_react2.default.createElement(Header, {
                    key: i,
                    currentSortLink: currentSortLink,
                    heading: (0, _i18n2.default)(kid.attrs.heading || (column.domainType ? column.domainType : column.type) + ":" + column.name),
                    sort: name
                }));
            }

            return _react2.default.createElement(
                "tr",
                null,
                headers
            );
        }
    }, {
        key: "renderFilter",
        value: function renderFilter(cursor) {
            var _this6 = this;

            var _props4 = this.props,
                vars = _props4.vars,
                model = _props4.model;


            var activeFilters = vars.filter || {};

            var colCount = 0;

            return _react2.default.createElement(
                "tr",
                null,
                model.kids.map(function (kidModel) {
                    var name = kidModel.attrs.name;

                    var columnCursor = cursor.getCursor([0, name]);

                    return _react2.default.createElement(FilterField, {
                        key: colCount++,
                        name: name,
                        propertyType: columnCursor.getPropertyType(),
                        value: activeFilters[name],
                        setFilter: _this6.setFilter
                    });
                })
            );
        }
    }, {
        key: "render",
        value: function render() {
            var _props5 = this.props,
                result = _props5.result,
                children = _props5.children,
                vars = _props5.vars;


            var childCount = _react2.default.Children.count(children);

            var cursor = DataGrid.cursorFromData(result);
            //console.log("DATAGRID", cursor);

            var count = cursor.get().length;
            var rows = void 0;
            if (count === 0) {
                rows = _react2.default.createElement(
                    "tr",
                    null,
                    _react2.default.createElement(
                        "td",
                        { colSpan: childCount },
                        (0, _i18n2.default)("No Rows")
                    )
                );
            } else {
                rows = [];

                for (var i = 0; i < count; i++) {
                    var context = cursor.getCursor([i]);

                    //console.log("CONTEXT", context);

                    rows[i] = _react2.default.createElement(
                        "tr",
                        { key: i },
                        (0, _renderWithContext2.default)(children, context)
                    );
                }
            }

            return _react2.default.createElement(
                "div",
                { className: "datagrid" },
                _react2.default.createElement(
                    "table",
                    { className: "table table-striped table-hover table-bordered" },
                    _react2.default.createElement(
                        "thead",
                        null,
                        this.renderHeader(),
                        this.renderFilter(cursor)
                    ),
                    _react2.default.createElement(
                        "tbody",
                        null,
                        rows
                    )
                ),
                _react2.default.createElement(_PagingComponent2.default, {
                    offsetLink: new _valueLink2.default(vars.offset, this.setPagingOffset),
                    limit: vars.limit,
                    rowCount: result.count
                })
            );
        }
    }], [{
        key: "cursorFromData",
        value: function cursorFromData(data) {
            if (!data) {
                throw new Error("No data");
            }

            if (data instanceof _cursor2.default) {
                return data;
            } else if ((0, _graph.validateDataGraph)(data)) {
                return new _cursor2.default(_domain2.default.getDomainData(), data, []);
            } else {
                throw new Error("Cannot handle data", data);
            }
        }
    }]);

    return DataGrid;
}(_react2.default.Component);

DataGrid.propTypes = {
    orderBy: _propTypes2.default.string,
    limit: _propTypes2.default.number,
    result: _propTypes2.default.object
};
exports.default = DataGrid;

/***/ }),

/***/ 446:
/*!***************************!*\
  !*** ./util/has-class.js ***!
  \***************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
    value: true
});

exports.default = function (classes, cls) {
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

/***/ }),

/***/ 447:
/*!*********************************************************!*\
  !*** ./components/std/datagrid/DataGridColumnWizard.js ***!
  \*********************************************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


module.exports = function (editor, completion) {
    editor.getSession().suppressPreviewOnce = true;

    var pos = editor.getCursorPosition();
    editor.insert("<DataGrid.Column name=\"\" />\r\n");
    editor.moveCursorTo(pos.row, pos.column + 23);

    window.setTimeout(function () {
        editor.execCommand("startAutocomplete");
    }, 10);
};

/***/ }),

/***/ 448:
/*!*******************************************************!*\
  !*** ./components/std/datagrid/DataGridTypeWizard.js ***!
  \*******************************************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
    value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = __webpack_require__(/*! react */ 0);

var _react2 = _interopRequireDefault(_react);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var DataGridTypeWizard = function (_React$Component) {
    _inherits(DataGridTypeWizard, _React$Component);

    function DataGridTypeWizard() {
        _classCallCheck(this, DataGridTypeWizard);

        return _possibleConstructorReturn(this, (DataGridTypeWizard.__proto__ || Object.getPrototypeOf(DataGridTypeWizard)).apply(this, arguments));
    }

    _createClass(DataGridTypeWizard, [{
        key: "render",
        value: function render() {
            return _react2.default.createElement(
                "div",
                null,
                "DataGridTypeWizard"
            );
        }
    }]);

    return DataGridTypeWizard;
}(_react2.default.Component);

;

exports.default = DataGridTypeWizard;

/***/ }),

/***/ 449:
/*!*************************************************!*\
  !*** ./components/std/datagrid/components.json ***!
  \*************************************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports) {

module.exports = {"components":{"DataGrid":{"description":"Datagrid element","classes":["element","iterative-context","model-aware"],"vars":{"orderBy":"props.orderBy","filter":"{}","offset":"0","limit":"props.limit","params":"props.params"},"queries":{"result":"query( prop('type').fields( childFieldRefs()) ).filter( prop('filter') && combineChildFilters() ).orderBy( var('orderBy') ).limit( var('limit') ).offset( var('offset')).params(var('params'))"},"propTypes":{"type":{"description":"Type expression. Usually just a type identifier '{ MyType }'","client":false,"type":"QUERY_EXPRESSION","rule":"domainType()"},"orderBy":{"description":"Field name to order the datagrid by default.","client":false,"rule":"fieldListOf(query(prop('type')))"},"filter":{"description":"Filter expression","type":"FILTER_EXPRESSION","client":false,"rule":"filterExpr(query(prop('type')))"},"limit":{"description":"Maximum number of rows to display per page.","client":false,"type":"INTEGER","defaultValue":"10"},"params":{"description":"SQL Parameters for query type queries","client":false,"type":"MAP","defaultValue":"{}"}},"propWizards":{"type":{"wizard":"DataGridTypeWizard","title":"Configure Query","description":"Open Query join dialog for complex queries"}},"providesContext":"DataCursor","childRule":"component('DataGrid.Column')","parentRule":"!parentHasClass('iterative-context')"},"DataGrid.Column":{"providesContext":"DataCursor","propTypes":{"name":{"description":"Field name for this column","type":"FIELD_REFERENCE","rule":"fieldOf(parent.prop('type'))","required":true},"filterTemplate":{"description":"Filter template to use for the user-defined filtering. <code>field()</code> is the current field value, <code>filterValue()</code> the current user input into the filter field.","type":"FILTER_EXPRESSION","defaultValue":"field().contains(filterValue())","client":false},"heading":{"description":"Heading to use for this column. Default is something like <code>{ i18n('MyType.myField') }</code>","type":"PLAINTEXT"},"context":{"type":"CURSOR_EXPRESSION","defaultValue":"context[props.name]","contextType":"DataCursor"}},"templates":[{"wizard":"DataGridColumnWizard","title":"configure..."}],"childRule":"hasClass('element')"}}}

/***/ }),

/***/ 450:
/*!***************************************!*\
  !*** ./components/std/form/Button.js ***!
  \***************************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
    value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _classnames = __webpack_require__(/*! classnames */ 1);

var _classnames2 = _interopRequireDefault(_classnames);

var _react = __webpack_require__(/*! react */ 0);

var _react2 = _interopRequireDefault(_react);

var _formState = __webpack_require__(/*! ../../../reducers/form-state */ 20);

var _store = __webpack_require__(/*! ../../../service/store */ 3);

var _store2 = _interopRequireDefault(_store);

var _fieldState = __webpack_require__(/*! ../../../form/field-state */ 47);

var _fieldState2 = _interopRequireDefault(_fieldState);

var _propTypes = __webpack_require__(/*! prop-types */ 2);

var _propTypes2 = _interopRequireDefault(_propTypes);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; } /**
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                * Action executing button
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                */


var Button = function (_React$Component) {
    _inherits(Button, _React$Component);

    function Button() {
        var _ref;

        var _temp, _this, _ret;

        _classCallCheck(this, Button);

        for (var _len = arguments.length, args = Array(_len), _key = 0; _key < _len; _key++) {
            args[_key] = arguments[_key];
        }

        return _ret = (_temp = (_this = _possibleConstructorReturn(this, (_ref = Button.__proto__ || Object.getPrototypeOf(Button)).call.apply(_ref, [this].concat(args))), _this), _this.onClick = function (ev) {
            var action = _this.props.action;


            if (!_this.isDisabled()) {
                action();
            }
        }, _temp), _possibleConstructorReturn(_this, _ret);
    }

    _createClass(Button, [{
        key: "isDisabled",
        value: function isDisabled() {
            var _props = this.props,
                id = _props.id,
                discard = _props.discard;


            if (discard) {
                return false;
            }

            var state = _store2.default.getState();

            var hasError = (0, _formState.formHasError)(state, id);
            var isDisabledByExpr = (0, _formState.getFieldState)(state, id) !== _fieldState2.default.NORMAL;

            return hasError || isDisabledByExpr;
        }
    }, {
        key: "render",
        value: function render() {
            var _props2 = this.props,
                id = _props2.id,
                className = _props2.className,
                title = _props2.title,
                icon = _props2.icon,
                text = _props2.text;

            var isDisabled = this.isDisabled();

            return _react2.default.createElement(
                "button",
                {
                    type: "button",
                    id: id,
                    name: id,
                    className: (0, _classnames2.default)("btn", isDisabled && "disabled", className || "btn-default"),
                    title: title,
                    disabled: isDisabled,
                    onClick: this.onClick
                },
                icon && _react2.default.createElement("span", { className: "glyphicon glyphicon-" + icon }),
                " " + text
            );
        }
    }]);

    return Button;
}(_react2.default.Component);

Button.propTypes = {
    action: _propTypes2.default.func.isRequired,
    discard: _propTypes2.default.bool,
    className: _propTypes2.default.string,
    text: _propTypes2.default.string.isRequired
};
exports.default = Button;

/***/ }),

/***/ 451:
/*!**********************************************!*\
  !*** ./components/std/form/ErrorMessages.js ***!
  \**********************************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
    value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _i18n = __webpack_require__(/*! ../../../service/i18n */ 9);

var _i18n2 = _interopRequireDefault(_i18n);

var _store = __webpack_require__(/*! ../../../service/store */ 3);

var _store2 = _interopRequireDefault(_store);

var _react = __webpack_require__(/*! react */ 0);

var _react2 = _interopRequireDefault(_react);

var _formState = __webpack_require__(/*! ../../../reducers/form-state */ 20);

var _componentUtil = __webpack_require__(/*! ../../../util/component-util */ 111);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var ErrorMessages = function (_React$Component) {
    _inherits(ErrorMessages, _React$Component);

    function ErrorMessages() {
        _classCallCheck(this, ErrorMessages);

        return _possibleConstructorReturn(this, (ErrorMessages.__proto__ || Object.getPrototypeOf(ErrorMessages)).apply(this, arguments));
    }

    _createClass(ErrorMessages, [{
        key: "render",
        value: function render() {
            var state = _store2.default.getState();
            var formComponent = (0, _componentUtil.findParent)(this.props.model, _componentUtil.isFormComponent);

            var errors = (0, _formState.getFormErrors)(state, formComponent ? formComponent.attrs.id : null);
            //        console.log("ERRORS", errors);

            return !!errors.length && _react2.default.createElement(
                "div",
                { className: "error-messages form-group has-error" },
                _react2.default.createElement(
                    "h4",
                    null,
                    (0, _i18n2.default)("Errors")
                ),
                _react2.default.createElement(
                    "ul",
                    { className: "errors" },
                    errors.map(function (error, index) {
                        return error && _react2.default.createElement(
                            "li",
                            { key: index },
                            _react2.default.createElement(
                                "label",
                                { className: "control-label", htmlFor: error.fieldId },
                                error.message
                            )
                        );
                    })
                )
            );
        }
    }]);

    return ErrorMessages;
}(_react2.default.Component);

exports.default = ErrorMessages;

/***/ }),

/***/ 452:
/*!*****************************************!*\
  !*** ./components/std/form/FKSelect.js ***!
  \*****************************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
    value: true
});

var _class, _temp4;

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

exports.Property = Property;

var _react = __webpack_require__(/*! react */ 0);

var _react2 = _interopRequireDefault(_react);

var _classnames = __webpack_require__(/*! classnames */ 1);

var _classnames2 = _interopRequireDefault(_classnames);

var _FormElement = __webpack_require__(/*! ./FormElement */ 55);

var _FormElement2 = _interopRequireDefault(_FormElement);

var _Modal = __webpack_require__(/*! react-bootstrap/lib/Modal */ 50);

var _Modal2 = _interopRequireDefault(_Modal);

var _PagingComponent = __webpack_require__(/*! ../../../ui/PagingComponent */ 231);

var _PagingComponent2 = _interopRequireDefault(_PagingComponent);

var _Toolbar = __webpack_require__(/*! ./Toolbar */ 89);

var _Toolbar2 = _interopRequireDefault(_Toolbar);

var _valueLink = __webpack_require__(/*! ../../../util/value-link */ 139);

var _valueLink2 = _interopRequireDefault(_valueLink);

var _i18n = __webpack_require__(/*! ../../../service/i18n */ 9);

var _i18n2 = _interopRequireDefault(_i18n);

var _store = __webpack_require__(/*! ../../../service/store */ 3);

var _store2 = _interopRequireDefault(_store);

var _propertyRenderer = __webpack_require__(/*! ../../../service/property-renderer */ 61);

var _propertyRenderer2 = _interopRequireDefault(_propertyRenderer);

var _component = __webpack_require__(/*! ../../../actions/component */ 45);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var domainService = __webpack_require__(/*! ../../../service/domain */ 16);

function Property(props) {
    return false;
}

function renderTargetLabel(graph, target, propModels) {
    var columns = graph.columns;


    if (!target) {
        return _react2.default.createElement(
            "em",
            null,
            "\xA0" + (0, _i18n2.default)("---")
        );
    } else {
        //console.log("TARGET", target);


        var l = ["\xA0"];
        for (var i = 0; i < propModels.length; i++) {
            var propModel = propModels[i];

            if (propModel.name === "[String]") {
                l.push(propModel.attrs.value + " ");
            } else {
                var attrName = propModel.attrs.name;

                var component = _propertyRenderer2.default.renderStatic(target[attrName], columns[attrName]);
                l.push(_react2.default.cloneElement(component, { key: i }), " ");
            }
        }
        return l;
    }
}

/**
 * Form field part of the input
 */

var TargetField = function (_React$PureComponent) {
    _inherits(TargetField, _React$PureComponent);

    function TargetField() {
        _classCallCheck(this, TargetField);

        return _possibleConstructorReturn(this, (TargetField.__proto__ || Object.getPrototypeOf(TargetField)).apply(this, arguments));
    }

    _createClass(TargetField, [{
        key: "render",
        value: function render() {
            var _props = this.props,
                id = _props.id,
                icon = _props.icon,
                disabled = _props.disabled,
                target = _props.target,
                propModels = _props.propModels,
                candidates = _props.candidates;


            return _react2.default.createElement(
                "div",
                { className: "input-group" },
                icon && _react2.default.createElement(
                    "div",
                    { className: "input-group-addon" },
                    _react2.default.createElement("span", { className: (0, _classnames2.default)("glyphicon glyphicon-" + icon, "text-info") })
                ),
                _react2.default.createElement(
                    "p",
                    {
                        className: (0, _classnames2.default)("form-control-static target", disabled && "disabled"),
                        onClick: !disabled ? this.props.openModal : null
                    },
                    renderTargetLabel(candidates, target, propModels)
                ),
                _react2.default.createElement(
                    "span",
                    { className: "input-group-btn" },
                    _react2.default.createElement(
                        "button",
                        {
                            className: "btn btn-default",
                            type: "button",
                            disabled: disabled,
                            onClick: this.props.openModal
                        },
                        "\u2026"
                    )
                )
            );
        }
    }]);

    return TargetField;
}(_react2.default.PureComponent);

var Target = function (_React$PureComponent2) {
    _inherits(Target, _React$PureComponent2);

    function Target() {
        var _ref;

        var _temp, _this2, _ret;

        _classCallCheck(this, Target);

        for (var _len = arguments.length, args = Array(_len), _key = 0; _key < _len; _key++) {
            args[_key] = arguments[_key];
        }

        return _ret = (_temp = (_this2 = _possibleConstructorReturn(this, (_ref = Target.__proto__ || Object.getPrototypeOf(Target)).call.apply(_ref, [this].concat(args))), _this2), _this2.onSelect = function (ev) {
            _this2.props.onSelect(_this2.props.target.id);
            ev.preventDefault();
        }, _temp), _possibleConstructorReturn(_this2, _ret);
    }

    _createClass(Target, [{
        key: "render",
        value: function render() {
            var _props2 = this.props,
                icon = _props2.icon,
                target = _props2.target,
                candidates = _props2.candidates,
                propModels = _props2.propModels;

            //console.log({target});

            return _react2.default.createElement(
                "tr",
                null,
                _react2.default.createElement(
                    "td",
                    null,
                    _react2.default.createElement(
                        "a",
                        {
                            href: "#select",
                            className: "btn btn-link",
                            onClick: this.onSelect
                        },
                        _react2.default.createElement("span", { className: "glyphicon glyphicon-circle-arrow-down" }),
                        "  ",
                        _react2.default.createElement(
                            "span",
                            { className: "fk-target" },
                            icon && _react2.default.createElement("span", { className: "glyphicon glyphicon-" + icon }),
                            "\xA0",
                            renderTargetLabel(candidates, target, propModels)
                        )
                    )
                )
            );
        }
    }]);

    return Target;
}(_react2.default.PureComponent);

var TargetList = function (_React$PureComponent3) {
    _inherits(TargetList, _React$PureComponent3);

    function TargetList() {
        var _ref2;

        var _temp2, _this3, _ret2;

        _classCallCheck(this, TargetList);

        for (var _len2 = arguments.length, args = Array(_len2), _key2 = 0; _key2 < _len2; _key2++) {
            args[_key2] = arguments[_key2];
        }

        return _ret2 = (_temp2 = (_this3 = _possibleConstructorReturn(this, (_ref2 = TargetList.__proto__ || Object.getPrototypeOf(TargetList)).call.apply(_ref2, [this].concat(args))), _this3), _this3.onFilterChange = function (ev) {
            return _this3.props.onFilterChange(ev.target.value);
        }, _this3.onSubmit = function (ev) {
            var onSubmit = _this3.props.onSubmit;


            if (onSubmit) {
                onSubmit(ev.target.value);
            }
            ev.preventDefault();
        }, _temp2), _possibleConstructorReturn(_this3, _ret2);
    }

    _createClass(TargetList, [{
        key: "render",
        value: function render() {
            var _props3 = this.props,
                candidates = _props3.candidates,
                icon = _props3.icon,
                propModels = _props3.propModels,
                filter = _props3.filter,
                onSelect = _props3.onSelect;


            return _react2.default.createElement(
                "div",
                { className: "fk-targets" },
                _react2.default.createElement(
                    "form",
                    { className: "form-inline", onSubmit: this.onSubmit },
                    _react2.default.createElement("input", {
                        type: "text",
                        className: "form-control",
                        placeholder: (0, _i18n2.default)("Filter Targets"),
                        defaultValue: filter,
                        autoFocus: true,
                        onChange: this.onFilterChange
                    })
                ),
                _react2.default.createElement(
                    "table",
                    { className: "table table-striped table-hover table-bordered" },
                    _react2.default.createElement(
                        "tbody",
                        null,
                        candidates.rootObject.map(function (target, idx) {
                            return _react2.default.createElement(Target, {
                                key: idx,
                                icon: icon,
                                target: target,
                                propModels: propModels,
                                candidates: candidates,
                                onSelect: onSelect
                            });
                        })
                    )
                )
            );
        }
    }]);

    return TargetList;
}(_react2.default.PureComponent);

var FKSelect = (0, _FormElement2.default)((_temp4 = _class = function (_React$PureComponent4) {
    _inherits(_class, _React$PureComponent4);

    function _class() {
        var _ref3;

        var _temp3, _this4, _ret3;

        _classCallCheck(this, _class);

        for (var _len3 = arguments.length, args = Array(_len3), _key3 = 0; _key3 < _len3; _key3++) {
            args[_key3] = arguments[_key3];
        }

        return _ret3 = (_temp3 = (_this4 = _possibleConstructorReturn(this, (_ref3 = _class.__proto__ || Object.getPrototypeOf(_class)).call.apply(_ref3, [this].concat(args))), _this4), _this4.state = {
            modalOpen: false
        }, _this4.onChange = function (ev) {
            return _this4.props.onChange(ev.target.value);
        }, _this4.openModal = function (ev) {
            var _this4$props = _this4.props,
                id = _this4$props.id,
                vars = _this4$props.vars,
                propertyType = _this4$props.propertyType;
            //console.log({id, vars, propertyType});

            var foreignKey = propertyType.foreignKey;


            if (!foreignKey) {
                throw new Error("Target property has no foreign key");
            }

            _this4.setState({ modalOpen: true });
        }, _this4.closeModal = function (ev) {
            return _this4.setState({ modalOpen: false });
        }, _this4.selectNone = function () {
            return _this4.onSelect(null);
        }, _this4.onSelect = function (targetId) {

            _this4.setState({
                modalOpen: false
            }, function () {
                _this4.props.onChange(targetId);

                return _store2.default.dispatch((0, _component.updateComponent)(_this4.props.modelId, {}));
            });
        }, _this4.onFilterChange = function (filter) {
            var modelId = _this4.props.modelId;


            return _store2.default.dispatch((0, _component.updateComponent)(modelId, {
                filter: filter
            }));
        }, _this4.setPagingOffset = function (offset) {
            var modelId = _this4.props.modelId;


            return _store2.default.dispatch((0, _component.updateComponent)(modelId, {
                offset: offset
            }));
        }, _temp3), _possibleConstructorReturn(_this4, _ret3);
    }

    _createClass(_class, [{
        key: "getInputField",
        value: function getInputField() {
            return this._input;
        }
    }, {
        key: "render",
        value: function render() {
            var _this5 = this;

            var _props4 = this.props,
                candidates = _props4.candidates,
                disabled = _props4.disabled,
                icon = _props4.icon,
                id = _props4.id,
                limit = _props4.limit,
                model = _props4.model,
                propertyType = _props4.propertyType,
                title = _props4.title,
                value = _props4.value,
                vars = _props4.vars;
            //console.log("RENDER FKSelect", { candidates, icon, id, limit, model, propertyType, title, value, vars });

            var target = candidates && candidates.rootObject.filter(function (t) {
                return t.id === value;
            })[0];

            var propModels = model.kids;

            return _react2.default.createElement(
                "div",
                { className: "fk-select" },
                _react2.default.createElement(TargetField, {
                    ref: function ref(elem) {
                        return _this5._input = elem;
                    },
                    id: id,
                    icon: icon,
                    disabled: disabled,
                    target: target,
                    type: propertyType.foreignKey.type,
                    propModels: propModels,
                    candidates: candidates,
                    openModal: this.openModal
                }),
                _react2.default.createElement(
                    _Modal2.default,
                    {
                        show: this.state.modalOpen,
                        onHide: this.closeModal
                    },
                    _react2.default.createElement(
                        _Modal2.default.Header,
                        { closeButton: true },
                        _react2.default.createElement(
                            _Modal2.default.Title,
                            null,
                            title || (0, _i18n2.default)("Select FK Target")
                        )
                    ),
                    _react2.default.createElement(
                        _Modal2.default.Body,
                        null,
                        this.state.modalOpen && _react2.default.createElement(TargetList, {
                            icon: icon,
                            candidates: candidates,
                            propModels: propModels,
                            componentId: id,
                            onSelect: this.onSelect,
                            filter: vars.filter,
                            onFilterChange: this.onFilterChange,
                            onSubmit: this.onSubmit
                        }),
                        _react2.default.createElement(_PagingComponent2.default, {
                            offsetLink: new _valueLink2.default(vars.offset, this.setPagingOffset),
                            limit: limit,
                            rowCount: candidates.count
                        }),
                        _react2.default.createElement(
                            _Toolbar2.default,
                            null,
                            !propertyType.required && _react2.default.createElement(
                                "button",
                                { type: "button", className: "btn btn-default", onClick: this.selectNone },
                                (0, _i18n2.default)("Select None")
                            ),
                            _react2.default.createElement(
                                "button",
                                { type: "button", className: "btn btn-default", onClick: this.closeModal },
                                (0, _i18n2.default)("Close")
                            )
                        )
                    )
                )
            );
        }
    }]);

    return _class;
}(_react2.default.PureComponent), _class.displayName = "FKSelect", _temp4));

exports.default = FKSelect;

/***/ }),

/***/ 453:
/*!******************************************!*\
  !*** ./components/std/form/FormBlock.js ***!
  \******************************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
    value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _renderWithContext = __webpack_require__(/*! ../../../util/render-with-context */ 98);

var _renderWithContext2 = _interopRequireDefault(_renderWithContext);

var _react = __webpack_require__(/*! react */ 0);

var _react2 = _interopRequireDefault(_react);

var _propTypes = __webpack_require__(/*! prop-types */ 2);

var _propTypes2 = _interopRequireDefault(_propTypes);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var FormBlock = function (_React$Component) {
    _inherits(FormBlock, _React$Component);

    function FormBlock() {
        _classCallCheck(this, FormBlock);

        return _possibleConstructorReturn(this, (FormBlock.__proto__ || Object.getPrototypeOf(FormBlock)).apply(this, arguments));
    }

    _createClass(FormBlock, [{
        key: "render",
        value: function render() {
            var _props = this.props,
                data = _props.data,
                value = _props.value,
                children = _props.children;

            //        console.log({ data, value, children});

            return _react2.default.createElement(
                "div",
                { className: "form-block" },
                (0, _renderWithContext2.default)(children, data || value)
            );
        }
    }]);

    return FormBlock;
}(_react2.default.Component);

FormBlock.propTypes = {
    horizontal: _propTypes2.default.bool,
    labelClass: _propTypes2.default.string,
    wrapperClass: _propTypes2.default.string
};
exports.default = FormBlock;

/***/ }),

/***/ 454:
/*!*******************************************!*\
  !*** ./components/std/form/ListEditor.js ***!
  \*******************************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
    value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = __webpack_require__(/*! react */ 0);

var _react2 = _interopRequireDefault(_react);

var _store = __webpack_require__(/*! ../../../service/store */ 3);

var _store2 = _interopRequireDefault(_store);

var _i18n = __webpack_require__(/*! ../../../service/i18n */ 9);

var _i18n2 = _interopRequireDefault(_i18n);

var _renderWithContext = __webpack_require__(/*! ../../../util/render-with-context */ 98);

var _renderWithContext2 = _interopRequireDefault(_renderWithContext);

var _scope = __webpack_require__(/*! ../../../actions/scope */ 26);

var _formState = __webpack_require__(/*! ../../../reducers/form-state */ 20);

var _Toolbar = __webpack_require__(/*! ./Toolbar */ 89);

var _Toolbar2 = _interopRequireDefault(_Toolbar);

var _fieldState = __webpack_require__(/*! ../../../form/field-state */ 47);

var _fieldState2 = _interopRequireDefault(_fieldState);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var componentService = __webpack_require__(/*! ../../../service/component */ 48);
var domainService = __webpack_require__(/*! ../../../service/domain */ 16);

var ListEditorControl = function (_React$Component) {
    _inherits(ListEditorControl, _React$Component);

    function ListEditorControl() {
        var _ref;

        var _temp, _this, _ret;

        _classCallCheck(this, ListEditorControl);

        for (var _len = arguments.length, args = Array(_len), _key = 0; _key < _len; _key++) {
            args[_key] = arguments[_key];
        }

        return _ret = (_temp = (_this = _possibleConstructorReturn(this, (_ref = ListEditorControl.__proto__ || Object.getPrototypeOf(ListEditorControl)).call.apply(_ref, [this].concat(args))), _this), _this.removeItem = function (ev) {
            return _this.props.removeItem(_this.props.index);
        }, _this.moveItemUp = function (ev) {
            return _this.props.moveItemUp(_this.props.index);
        }, _this.moveItemDown = function (ev) {
            return _this.props.moveItemDown(_this.props.index);
        }, _temp), _possibleConstructorReturn(_this, _ret);
    }

    _createClass(ListEditorControl, [{
        key: "render",
        value: function render() {
            var _props = this.props,
                canReorder = _props.canReorder,
                removeLabel = _props.removeLabel,
                removeItem = _props.removeItem,
                moveItemUp = _props.moveItemUp,
                moveItemDown = _props.moveItemDown;


            return _react2.default.createElement(
                "div",
                { className: "list-editor-control" },
                canReorder && _react2.default.createElement(
                    "button",
                    { type: "button", className: "btn btn-default", disabled: !moveItemUp, onClick: this.moveItemUp },
                    _react2.default.createElement("span", { className: "glyphicon glyphicon-arrow-up" })
                ),
                canReorder && _react2.default.createElement(
                    "button",
                    { type: "button", className: "btn btn-default", disabled: !moveItemDown, onClick: this.moveItemDown },
                    _react2.default.createElement("span", { className: "glyphicon glyphicon-arrow-down" })
                ),
                removeItem && _react2.default.createElement(
                    "button",
                    { type: "button", className: "btn btn-default", onClick: this.removeItem, title: removeLabel },
                    _react2.default.createElement("span", { className: "glyphicon glyphicon-remove" })
                )
            );
        }
    }]);

    return ListEditorControl;
}(_react2.default.Component);

var ListEditor = function (_React$Component2) {
    _inherits(ListEditor, _React$Component2);

    function ListEditor() {
        var _ref2;

        var _temp2, _this2, _ret2;

        _classCallCheck(this, ListEditor);

        for (var _len2 = arguments.length, args = Array(_len2), _key2 = 0; _key2 < _len2; _key2++) {
            args[_key2] = arguments[_key2];
        }

        return _ret2 = (_temp2 = (_this2 = _possibleConstructorReturn(this, (_ref2 = ListEditor.__proto__ || Object.getPrototypeOf(ListEditor)).call.apply(_ref2, [this].concat(args))), _this2), _this2.newItem = function (ev) {
            var data = _this2.props.data;


            var newValue = domainService.create(data.getPropertyType().typeParam);
            _store2.default.dispatch((0, _scope.updateScope)(data.getPath(), data.get().concat(newValue)));

            //console.log("after NEW-ITEM", store.getState());

            if (_this2.props.onNewObject) {
                // we delay the action to have it executed in the new state after updateScope
                window.setTimeout(_this2.props.onNewObject, 1);
            }
        }, _this2.moveItemUp = function (index) {
            _this2.swapItems(index, index - 1);
        }, _this2.moveItemDown = function (index) {
            _this2.swapItems(index, index + 1);
        }, _this2.removeItem = function (index) {
            var data = _this2.props.data;

            //console.log("REMOVE", index);

            var array = data.get().slice();

            array.splice(index, 1);

            _store2.default.dispatch((0, _scope.updateScope)(data.getPath(), array));
        }, _temp2), _possibleConstructorReturn(_this2, _ret2);
    }

    _createClass(ListEditor, [{
        key: "swapItems",
        value: function swapItems(index, other) {
            var data = this.props.data;


            var array = data.get().slice();

            var h = array[other];
            array[other] = array[index];
            array[index] = h;

            //console.log("SWAP", index, "before",  data.get(), "after", array);

            _store2.default.dispatch((0, _scope.updateScope)(data.getPath(), array));
        }
    }, {
        key: "render",
        value: function render() {
            var _props2 = this.props,
                id = _props2.id,
                children = _props2.children,
                data = _props2.data,
                canReorder = _props2.canReorder,
                canRemove = _props2.canRemove,
                canAdd = _props2.canAdd,
                addLabel = _props2.addLabel,
                removeLabel = _props2.removeLabel,
                minItems = _props2.minItems,
                maxItems = _props2.maxItems;


            var state = _store2.default.getState();

            var disabled = (0, _formState.getFieldState)(state, id) !== _fieldState2.default.NORMAL;

            var value = data.get();

            var rows = false;
            var len = 0;
            if (value) {
                len = value.length;
                var last = len - 1;
                rows = new Array(len);

                for (var i = 0; i < len; i++) {
                    rows[i] = _react2.default.createElement(
                        "div",
                        { key: i, className: "list-editor-row" },
                        _react2.default.createElement(ListEditorControl, {
                            index: i,
                            removeItem: !disabled && canRemove && len > minItems && this.removeItem,
                            moveItemUp: !disabled && i > 0 && canReorder && this.moveItemUp,
                            moveItemDown: !disabled && i < last && canReorder && this.moveItemDown,
                            canReorder: !disabled && canReorder,
                            removeLabel: removeLabel
                        }),
                        _react2.default.createElement(
                            "div",
                            { className: "list-editor-field" },
                            (0, _renderWithContext2.default)(children, data.getCursor([i]))
                        )
                    );
                }
            }

            return _react2.default.createElement(
                "div",
                { className: "list-editor" },
                !disabled && canAdd && (maxItems < 0 || len < maxItems) && _react2.default.createElement(
                    _Toolbar2.default,
                    null,
                    _react2.default.createElement(
                        "button",
                        { type: "button", className: "btn btn-default", onClick: this.newItem },
                        _react2.default.createElement("span", { className: "glyphicon glyphicon-plus" }),
                        " " + this.props.addLabel
                    )
                ),
                rows
            );
        }
    }]);

    return ListEditor;
}(_react2.default.Component);

ListEditor.defaultProps = {
    canAdd: true,
    canRemove: true,
    canReorder: true,
    addLabel: (0, _i18n2.default)("Add"),
    removeLabel: (0, _i18n2.default)("Remove"),
    minItems: 0,
    maxItems: -1
};
exports.default = ListEditor;

/***/ }),

/***/ 455:
/*!*******************************************!*\
  !*** ./components/std/form/ManyToMany.js ***!
  \*******************************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
    value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = __webpack_require__(/*! react */ 0);

var _react2 = _interopRequireDefault(_react);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var ManyToMany = function (_React$Component) {
    _inherits(ManyToMany, _React$Component);

    function ManyToMany() {
        _classCallCheck(this, ManyToMany);

        return _possibleConstructorReturn(this, (ManyToMany.__proto__ || Object.getPrototypeOf(ManyToMany)).apply(this, arguments));
    }

    _createClass(ManyToMany, [{
        key: "render",
        value: function render() {
            return _react2.default.createElement(
                "div",
                null,
                _react2.default.createElement(
                    "h1",
                    null,
                    " ManyToMany "
                )
            );
        }
    }]);

    return ManyToMany;
}(_react2.default.Component);

exports.default = ManyToMany;

/***/ }),

/***/ 456:
/*!****************************************!*\
  !*** ./components/std/form/Options.js ***!
  \****************************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


/***/ }),

/***/ 457:
/*!****************************************************!*\
  !*** ./components/std/form/StateMachineButtons.js ***!
  \****************************************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
    value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = __webpack_require__(/*! react */ 0);

var _react2 = _interopRequireDefault(_react);

var _classnames = __webpack_require__(/*! classnames */ 1);

var _classnames2 = _interopRequireDefault(_classnames);

var _store = __webpack_require__(/*! ../../../service/store */ 3);

var _store2 = _interopRequireDefault(_store);

var _process = __webpack_require__(/*! ../../../service/process */ 144);

var _process2 = _interopRequireDefault(_process);

var _i18n = __webpack_require__(/*! ../../../service/i18n */ 9);

var _i18n2 = _interopRequireDefault(_i18n);

var _meta = __webpack_require__(/*! ../../../reducers/meta */ 14);

var _FormElement = __webpack_require__(/*! ./FormElement */ 55);

var _FormElement2 = _interopRequireDefault(_FormElement);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var StateMachineButton = function (_React$Component) {
    _inherits(StateMachineButton, _React$Component);

    function StateMachineButton() {
        var _ref;

        var _temp, _this, _ret;

        _classCallCheck(this, StateMachineButton);

        for (var _len = arguments.length, args = Array(_len), _key = 0; _key < _len; _key++) {
            args[_key] = arguments[_key];
        }

        return _ret = (_temp = (_this = _possibleConstructorReturn(this, (_ref = StateMachineButton.__proto__ || Object.getPrototypeOf(StateMachineButton)).call.apply(_ref, [this].concat(args))), _this), _this.changeValue = function (ev) {
            var _this$props = _this.props,
                stateName = _this$props.stateName,
                changeValue = _this$props.changeValue;


            changeValue(stateName);
        }, _temp), _possibleConstructorReturn(_this, _ret);
    }

    _createClass(StateMachineButton, [{
        key: "render",
        value: function render() {
            var _props = this.props,
                stateName = _props.stateName,
                className = _props.className,
                icon = _props.icon,
                stateMachineName = _props.stateMachineName;

            return _react2.default.createElement(
                "button",
                { type: "button", className: (0, _classnames2.default)("btn", className || "btn-default"), onClick: this.changeValue },
                icon && _react2.default.createElement("span", { className: "glyphicon glyphicon-" + icon }),
                " " + (0, _i18n2.default)("Set " + stateMachineName + " " + stateName)
            );
        }
    }]);

    return StateMachineButton;
}(_react2.default.Component);

var StateMachineButtons = (0, _FormElement2.default)(function (_React$Component2) {
    _inherits(_class2, _React$Component2);

    function _class2() {
        var _ref2;

        var _temp2, _this2, _ret2;

        _classCallCheck(this, _class2);

        for (var _len2 = arguments.length, args = Array(_len2), _key2 = 0; _key2 < _len2; _key2++) {
            args[_key2] = arguments[_key2];
        }

        return _ret2 = (_temp2 = (_this2 = _possibleConstructorReturn(this, (_ref2 = _class2.__proto__ || Object.getPrototypeOf(_class2)).call.apply(_ref2, [this].concat(args))), _this2), _this2.changeValue = function (name) {
            var _this2$props = _this2.props,
                onChange = _this2$props.onChange,
                transition = _this2$props.transition;


            onChange(name);

            return _process2.default.transition(transition);
        }, _temp2), _possibleConstructorReturn(_this2, _ret2);
    }

    _createClass(_class2, [{
        key: "render",
        value: function render() {
            var state = _store2.default.getState();

            var _props2 = this.props,
                value = _props2.value,
                propertyType = _props2.propertyType,
                stateClasses = _props2.stateClasses,
                stateIcons = _props2.stateIcons,
                stateMachine = _props2.stateMachine;


            var typeFromCursor = propertyType.typeParam;
            if (stateMachine !== typeFromCursor) {
                throw new Error("State machine mismatch, static type is " + stateMachine + ", but cursor type is " + typeFromCursor);
            }

            var stateMachineModel = (0, _meta.getStateMachine)(state, stateMachine);

            var buttons = [];

            var states = stateMachineModel.states;


            var validTransitions = states[value];

            for (var i = 0; i < validTransitions.length; i++) {
                var name = validTransitions[i];

                buttons.push(_react2.default.createElement(StateMachineButton, {
                    key: name,
                    value: value,
                    stateName: name,
                    changeValue: this.changeValue,
                    className: stateClasses[name],
                    icon: stateIcons[name],
                    stateMachineName: stateMachine
                }));
            }

            return _react2.default.createElement(
                "div",
                { className: "btn-group", role: "group" },
                buttons
            );
        }
    }]);

    return _class2;
}(_react2.default.Component),
// opts
{
    decorate: false
});

exports.default = StateMachineButtons;

/***/ }),

/***/ 458:
/*!****************************************!*\
  !*** ./components/std/form/TButton.js ***!
  \****************************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
    value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _process = __webpack_require__(/*! ../../../service/process */ 144);

var _process2 = _interopRequireDefault(_process);

var _store = __webpack_require__(/*! ../../../service/store */ 3);

var _store2 = _interopRequireDefault(_store);

var _classnames = __webpack_require__(/*! classnames */ 1);

var _classnames2 = _interopRequireDefault(_classnames);

var _react = __webpack_require__(/*! react */ 0);

var _react2 = _interopRequireDefault(_react);

var _formState = __webpack_require__(/*! ../../../reducers/form-state */ 20);

var _fieldState = __webpack_require__(/*! ../../../form/field-state */ 47);

var _fieldState2 = _interopRequireDefault(_fieldState);

var _propTypes = __webpack_require__(/*! prop-types */ 2);

var _propTypes2 = _interopRequireDefault(_propTypes);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; } /**
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                * Process transition executing button.
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                */


var TButton = function (_React$Component) {
    _inherits(TButton, _React$Component);

    function TButton() {
        var _ref;

        var _temp, _this, _ret;

        _classCallCheck(this, TButton);

        for (var _len = arguments.length, args = Array(_len), _key = 0; _key < _len; _key++) {
            args[_key] = arguments[_key];
        }

        return _ret = (_temp = (_this = _possibleConstructorReturn(this, (_ref = TButton.__proto__ || Object.getPrototypeOf(TButton)).call.apply(_ref, [this].concat(args))), _this), _this.onClick = function (ev) {
            var _this$props = _this.props,
                context = _this$props.context,
                discard = _this$props.discard,
                transition = _this$props.transition;
            //console.log({ context, discard, transition });

            if (!_this.isDisabled()) {

                var objects = null;

                if (!discard && context && context.getGraph().qualifier === "QUERY") {
                    objects = context.extractObjects();
                }

                _process2.default.transition(transition, objects);
                // .catch(function(err)
                // {
                //     console.log("TRANSITION FAIL");
                //     console.error(err);
                // });
            }
        }, _temp), _possibleConstructorReturn(_this, _ret);
    }

    _createClass(TButton, [{
        key: "isDisabled",
        value: function isDisabled() {
            var _props = this.props,
                id = _props.id,
                discard = _props.discard;


            if (discard) {
                return false;
            }

            var state = _store2.default.getState();

            var hasError = (0, _formState.formHasError)(state, id);
            var isDisabledByExpr = (0, _formState.getFieldState)(state, id) !== _fieldState2.default.NORMAL;

            return hasError || isDisabledByExpr;
        }
    }, {
        key: "render",
        value: function render() {
            //        console.log("RENDER TBUTTON", this.props.context && this.props.context.graph.id);
            var _props2 = this.props,
                className = _props2.className,
                text = _props2.text,
                icon = _props2.icon;


            var isDisabled = this.isDisabled();
            return _react2.default.createElement(
                "button",
                {
                    type: "button",
                    className: (0, _classnames2.default)("btn", isDisabled && "disabled", className || "btn-default"),
                    value: text,
                    disabled: isDisabled,
                    onClick: this.onClick
                },
                icon && _react2.default.createElement("span", { className: "glyphicon glyphicon-" + icon }),
                " " + text
            );
        }
    }]);

    return TButton;
}(_react2.default.Component);

TButton.propTypes = {
    /** transition to execute */
    transition: _propTypes2.default.string.isRequired,
    /** true if the transition execution discards all user changes / does not depend on field validation */
    discard: _propTypes2.default.bool,
    /** HTML classes */
    className: _propTypes2.default.string,
    /** Text for the button */
    text: _propTypes2.default.string.isRequired,

    mapping: _propTypes2.default.object
};
TButton.defaultProps = {
    // default: map any unambiguous object of the target type or throw an error
    mapping: { "?": "current" }
};
exports.default = TButton;

/***/ }),

/***/ 459:
/*!*********************************************!*\
  !*** ./components/std/form/components.json ***!
  \*********************************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports) {

module.exports = {"components":{"Form":{"description":"Object form. Can either query a filtered list or work based off a cursor expression like '{ current }'.","classes":["element","form-container","form-config-provider"],"propTypes":{"data":{"description":"Cursor expression to provide data","type":"CURSOR_EXPRESSION"},"type":{"description":"Type to query (Query only)","client":false,"type":"QUERY_EXPRESSION","rule":"domainType()","required":true},"filter":{"description":"Filter expression (Query only)","client":false,"type":"FILTER_EXPRESSION","rule":"filterExpr(query(prop('type')))","required":true},"horizontal":{"description":"Horizontal form mode","type":"BOOLEAN"},"labelClass":{"description":"HTML classes to apply to the label element","type":"CLASSES"},"wrapperClass":{"description":"HTML classes to apply to the field wrapper element in horizontal mode","type":"CLASSES"},"disabledIf":{"description":"If true, form block is rendered as disabled.","type":"CONTEXT_EXPRESSION"},"readOnlyIf":{"description":"If true, form block is rendered as static text field.","type":"CONTEXT_EXPRESSION"}},"providesContext":"DataCursor","childRule":"hasClass('element') || hasClass('field')","parentRule":"!parentHasClass('iterative-context')"},"FormBlock":{"description":"Block of form elements","classes":["element","needs-id","form-container","form-config-provider"],"propTypes":{"value":{"description":"cursor expression for the form block. Is ignored if the data attribute is given","rule":"fieldOf(parent.prop('type'))","type":"CURSOR_EXPRESSION","contextType":"DataCursor","required":true},"horizontal":{"type":"BOOLEAN"},"labelClass":{"type":"CLASSES","description":"HTML classes to apply to the label element"},"wrapperClass":{"type":"CLASSES","description":"HTML classes to apply to the field wrapper element in horizontal mode"},"data":{"description":"Cursor expression to provide data","type":"CURSOR_EXPRESSION","defaultValue":"context"},"disabledIf":{"description":"If true, form block is rendered as disabled.","type":"CONTEXT_EXPRESSION"},"readOnlyIf":{"description":"If true, form block is rendered as static text field.","type":"CONTEXT_EXPRESSION"}},"providesContext":"DataCursor","childRule":"hasClass('element') || hasClass('field')","parentRule":"!parentHasClass('iterative-context')"},"StaticText":{"description":"Static text field that formats its output to fit in with other form elements.","classes":["field","model-aware","element"],"propTypes":{"value":{"description":"cursor expression for the value","rule":"fieldOf(parent.prop('type'))","type":"CURSOR_EXPRESSION","contextType":"DataCursor","required":true},"wrapperClass":{"type":"CLASSES"},"labelClass":{"type":"CLASSES"}},"providesContext":"DataCursor"},"Field":{"description":"General purpose form field adapting to the property type of its target where possible","classes":["element","field","model-aware"],"propTypes":{"value":{"description":"cursor expression for the value","rule":"fieldOf(parent.prop('type'))","type":"CURSOR_EXPRESSION","contextType":"DataCursor","required":true},"wrapperClass":{"description":"HTML classes for the field wrapper.","type":"CLASSES"},"labelClass":{"description":"HTML classes for the label element.","type":"CLASSES"},"disabledIf":{"description":"If true, field is rendered as disabled.","type":"CONTEXT_EXPRESSION"},"readOnlyIf":{"description":"If true, field is rendered as static text field.","type":"CONTEXT_EXPRESSION"},"propagate":{"description":"Controls the propagation behavior for text inputs. { false } makes the field on propagate its value on blur. Default value is controlled by component config 'instantTextFields'","type":"BOOLEAN"}}},"SelectField":{"description":"Select element whose options are based on a query. WIP","classes":["element","field","model-aware"],"queries":{"data":"query( prop('type') )"},"propTypes":{"value":{"description":"cursor expression for the value","rule":"fieldOf(parent.prop('type'))","type":"CURSOR_EXPRESSION","contextType":"DataCursor","required":true},"data":{"type":"VALUE_EXPRESSION"},"wrapperClass":{"description":"HTML classes for the field wrapper.","type":"CLASSES"},"labelClass":{"description":"HTML classes for the label element.","type":"CLASSES"},"type":{"description":"Type to query for the select field contents","client":false,"type":"QUERY_EXPRESSION","rule":"domainType()","required":true},"optionDisplay":{"description":"Cursor expression to resolve the display value for an option. If not given only optionValue is used.","rule":"fieldOf(prop('type'))","type":"CURSOR_EXPRESSION"},"optionValue":{"description":"Cursor expression to resolve the value for an option","rule":"fieldOf(prop('type'))","type":"CURSOR_EXPRESSION","required":true},"disabledIf":{"description":"If true, select field is rendered as disabled.","type":"CONTEXT_EXPRESSION"},"readOnlyIf":{"description":"If true, select field is rendered as static text field.","type":"CONTEXT_EXPRESSION"}}},"Button":{"description":"Button to execute actions in standalone-views","classes":["element","model-aware","toolbar-child","field"],"parentRule":"!viewInProcess()","propTypes":{"context":{"description":"context for the button","defaultValue":"context","type":"CURSOR_EXPRESSION","contextType":"DataCursor","required":false},"action":{"description":"Action (sequence)","type":"ACTION_EXPRESSION","required":true},"text":{"description":"Button text","required":true,"type":"PLAINTEXT"},"icon":{"description":"Icon to display for button","type":"GLYPH_ICON"},"disabledIf":{"description":"If true, button is rendered as disabled.","type":"CONTEXT_EXPRESSION"}}},"TButton":{"description":"Button to execute process transitions","classes":["element","model-aware","toolbar-child","field"],"parentRule":"viewInProcess()","propTypes":{"context":{"description":"context for the button","defaultValue":"context","type":"CURSOR_EXPRESSION","contextType":"DataCursor","required":false},"transition":{"description":"Name of the transition to be execute from this button.","rule":"transitionNames()","type":"TRANSITION","required":true},"domainType":{"description":"Defines the domain type to use for this button when it is ambiguous","type":"DOMAIN_TYPE_REFERENCE","required":true},"discard":{"description":"Allows user input to be discarded if true. (Think 'cancel' transition)","type":"BOOLEAN","defaultValue":"transitionModel(props.transition).discard"},"title":{"description":"The title for the TButton. Default is the description of the transition model.","type":"PLAINTEXT","defaultValue":"transitionModel(props.transition).description"},"text":{"description":"Button text","required":true,"type":"PLAINTEXT"},"icon":{"description":"Icon to display for transition button","type":"GLYPH_ICON"},"disabledIf":{"description":"If true, button is rendered as disabled.","type":"CONTEXT_EXPRESSION"}}},"Toolbar":{"description":"Toolbar grouping multiple toolbar-child classified elements, providing the necessary padding rules","classes":["element"],"childRule":"hasClass('toolbar-child')"},"Toolbar.Separator":{"description":"Separator in a toolbar","classes":["toolbar-child"]},"ErrorMessages":{"description":"Displays error messages","classes":["elem","model-aware"]},"FKSelect":{"description":"Selects the target for a foreign key property from a popup","classes":["element","model-aware","field","query-ids"],"vars":{"filter":"''","offset":"0"},"queries":{"candidates":"query(domainType(formFieldType().foreignKey.type).fields(childFieldRefs())).filter( prop('filter') && combineChildFilters() ).orderBy( prop('orderBy') ).limit( prop('limit') ).offset( var('offset'))"},"propTypes":{"value":{"description":"cursor expression for foreign key property","type":"CURSOR_EXPRESSION","contextType":"DataCursor","required":true},"orderBy":{"description":"Field name to order the FK target list by","client":false},"filter":{"description":"Filter expression to filter the applicable FK targets","type":"FILTER_EXPRESSION","client":false},"limit":{"description":"Maximum number of targets to display per page.","type":"INTEGER","defaultValue":"10"},"icon":{"description":"Icon to display for the FK targets","type":"GLYPH_ICON"}},"childRule":"component('FKSelect.Property')","parentRule":"!parentHasClass('iterative-context')"},"FKSelect.Property":{"propTypes":{"name":{"description":"Field name","type":"FIELD_REFERENCE","required":true},"filterTemplate":{"description":"Filter template to use for the user-defined filtering. <code>field()</code> is the current field value, <code>filterValue()</code> the current user input into the filter field.","type":"FILTER_EXPRESSION","defaultValue":"field().contains(var('filter'))","client":false}}},"ListEditor":{"description":"Editor for a list of domain objects","classes":["element","iterative-context","model-aware","form-container"],"propTypes":{"data":{"description":"Cursor expression to provide the list of domain objects","type":"CURSOR_EXPRESSION","defaultValue":"context"},"canAdd":{"description":"if set to false, don't allow addition of new rows (default is true)","type":"BOOLEAN"},"canRemove":{"description":"if set to false, don't allow removal of rows (default is true)","type":"BOOLEAN"},"canReorder":{"description":"if set to false, don't allow reordering of rows (default is true)","type":"BOOLEAN"},"addLabel":{"description":"label for the add button (default is i18n('Add'))","type":"PLAINTEXT"},"removeLabel":{"description":"label for the remove button (default is i18n('Remove'))","type":"PLAINTEXT"},"onNewObject":{"description":"Action expression to execute when adding a new object","type":"ACTION_EXPRESSION"},"disabledIf":{"description":"If true, list editor is rendered as disabled.","type":"CONTEXT_EXPRESSION"},"readOnlyIf":{"description":"If true, list editor is rendered as static text field.","type":"CONTEXT_EXPRESSION"}},"providesContext":"DataCursor","childRule":"hasClass('field')"},"StateMachineButtons":{"description":"Renders a button for every possible state machine transition from a current state","classes":["field","model-aware","element"],"propTypes":{"stateMachine":{"description":"Name of the state machine these buttons control","type":"STATE_MACHINE_REFERENCE","required":true},"value":{"description":"cursor expression for current state machine value","type":"CURSOR_EXPRESSION","contextType":"DataCursor","required":true},"wrapperClass":{"type":"CLASSES"},"labelClass":{"type":"CLASSES"},"transition":{"description":"Name of the transition to change the state machine value","rule":"transitionNames()","type":"TRANSITION","required":true},"stateClasses":{"description":"Map of extra classes per state machine state","type":"MAP","defaultValue":"{}"},"stateIcons":{"description":"Map of icons per state machine state","type":"MAP","defaultValue":"{}"}}}}}

/***/ }),

/***/ 460:
/*!*******************************************!*\
  !*** ./components/std/login/LoginForm.js ***!
  \*******************************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
    value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = __webpack_require__(/*! react */ 0);

var _react2 = _interopRequireDefault(_react);

var _uri = __webpack_require__(/*! ../../../util/uri */ 15);

var _uri2 = _interopRequireDefault(_uri);

var _csfr = __webpack_require__(/*! ../../../service/csfr */ 105);

var _csfr2 = _interopRequireDefault(_csfr);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; } /**
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                * Special form to be used in login views.
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                *
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                * Renders the spring security login check URI as action and provides the correct
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                * CSFR-token via hidden field.
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                */


var LoginForm = function (_React$Component) {
    _inherits(LoginForm, _React$Component);

    function LoginForm() {
        _classCallCheck(this, LoginForm);

        return _possibleConstructorReturn(this, (LoginForm.__proto__ || Object.getPrototypeOf(LoginForm)).apply(this, arguments));
    }

    _createClass(LoginForm, [{
        key: "render",
        value: function render() {
            return _react2.default.createElement(
                "form",
                { action: (0, _uri2.default)("/login_check"), method: "POST" },
                this.props.children,
                _react2.default.createElement("input", { type: "hidden", name: _csfr2.default.tokenParam(), value: _csfr2.default.token() })
            );
        }
    }]);

    return LoginForm;
}(_react2.default.Component);

;

exports.default = LoginForm;

/***/ }),

/***/ 461:
/*!**********************************************!*\
  !*** ./components/std/login/components.json ***!
  \**********************************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports) {

module.exports = {"components":{"LoginForm":{"classes":["element","internal"]}}}

/***/ }),

/***/ 462:
/*!*********************************************!*\
  !*** ./components/std/navigation/Navbar.js ***!
  \*********************************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


// integration module
module.exports = __webpack_require__(/*! react-bootstrap/lib/Navbar */ 92);

/***/ }),

/***/ 463:
/*!***************************************************!*\
  !*** ./components/std/navigation/components.json ***!
  \***************************************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports) {

module.exports = {"components":{"Navbar":{"classes":["nav-child"],"propTypes":{"bsStyle":{"type":"PLAINTEXT","rule":"oneOf('default', 'inverse')"}}}}}

/***/ }),

/***/ 464:
/*!********************************************!*\
  !*** ./components/std/shim/ShopAddress.js ***!
  \********************************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
    value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = __webpack_require__(/*! react */ 0);

var _react2 = _interopRequireDefault(_react);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var ShopAddress = function (_React$Component) {
    _inherits(ShopAddress, _React$Component);

    function ShopAddress() {
        _classCallCheck(this, ShopAddress);

        return _possibleConstructorReturn(this, (ShopAddress.__proto__ || Object.getPrototypeOf(ShopAddress)).apply(this, arguments));
    }

    _createClass(ShopAddress, [{
        key: "render",
        value: function render() {
            var value = this.props.value;


            var obj = value.get();

            return _react2.default.createElement(
                "pre",
                { className: "small" },
                obj.recipient + "\n",
                obj.addressLine + "\n",
                obj.addressExtra + "\n",
                obj.postal + "\n",
                obj.country + "\n"
            );
        }
    }]);

    return ShopAddress;
}(_react2.default.Component);

exports.default = ShopAddress;

/***/ }),

/***/ 465:
/*!****************************************!*\
  !*** ./components/std/shim/ShopNav.js ***!
  \****************************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
    value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = __webpack_require__(/*! react */ 0);

var _react2 = _interopRequireDefault(_react);

var _Nav = __webpack_require__(/*! react-bootstrap/lib/Nav */ 141);

var _Nav2 = _interopRequireDefault(_Nav);

var _Navbar = __webpack_require__(/*! react-bootstrap/lib/Navbar */ 92);

var _Navbar2 = _interopRequireDefault(_Navbar);

var _NavItem = __webpack_require__(/*! react-bootstrap/lib/NavItem */ 143);

var _NavItem2 = _interopRequireDefault(_NavItem);

var _uri = __webpack_require__(/*! ../../../util/uri */ 15);

var _uri2 = _interopRequireDefault(_uri);

var _sys = __webpack_require__(/*! ../../../sys */ 12);

var _sys2 = _interopRequireDefault(_sys);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var ShopNav = function (_React$Component) {
    _inherits(ShopNav, _React$Component);

    function ShopNav() {
        _classCallCheck(this, ShopNav);

        return _possibleConstructorReturn(this, (ShopNav.__proto__ || Object.getPrototypeOf(ShopNav)).apply(this, arguments));
    }

    _createClass(ShopNav, [{
        key: "render",
        value: function render() {
            return _react2.default.createElement(
                _Navbar2.default,
                null,
                _react2.default.createElement(
                    _Navbar2.default.Header,
                    null,
                    _react2.default.createElement(
                        _Navbar2.default.Brand,
                        null,
                        _react2.default.createElement(
                            "a",
                            { href: (0, _uri2.default)("/") },
                            "ACME Shipping"
                        )
                    ),
                    _react2.default.createElement(_Navbar2.default.Toggle, null)
                ),
                _react2.default.createElement(
                    _Nav2.default,
                    null,
                    _react2.default.createElement(
                        _NavItem2.default,
                        { href: (0, _uri2.default)("/app/" + _sys2.default.appName + "/customers") },
                        "Customers"
                    ),
                    _react2.default.createElement(
                        _NavItem2.default,
                        { href: (0, _uri2.default)("/app/" + _sys2.default.appName + "/products") },
                        "Products"
                    ),
                    _react2.default.createElement(
                        _NavItem2.default,
                        { href: (0, _uri2.default)("/app/" + _sys2.default.appName + "/orders") },
                        "Orders"
                    )
                ),
                _react2.default.createElement(
                    _Nav2.default,
                    { pullRight: true },
                    _react2.default.createElement(
                        _NavItem2.default,
                        { href: (0, _uri2.default)("/app/" + _sys2.default.appName + "/admin") },
                        "Administration"
                    )
                )
            );
        }
    }]);

    return ShopNav;
}(_react2.default.Component);

exports.default = ShopNav;

/***/ }),

/***/ 466:
/*!*********************************************!*\
  !*** ./components/std/shim/components.json ***!
  \*********************************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports) {

module.exports = {"components":{"ShopNav":{"description":"Fixed Shop navigation component, currently lacking real nav components","classes":["elem"]},"ShopAddress":{"description":"Display component for address objects","classes":["elem"],"propTypes":{"value":{"type":"CURSOR_EXPRESSION"}}}}}

/***/ }),

/***/ 96:
/*!***********************************!*\
  !*** ./editor/gui/gui-context.js ***!
  \***********************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
    value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = __webpack_require__(/*! react */ 0);

var _react2 = _interopRequireDefault(_react);

var _reactDom = __webpack_require__(/*! react-dom */ 13);

var _reactDom2 = _interopRequireDefault(_reactDom);

var _uiState = __webpack_require__(/*! ./ui-state */ 97);

var _uiState2 = _interopRequireDefault(_uiState);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var elements = {};

var CONTAINER_ID = "focus-proxy-container";
var ZOOM_FACTOR = 8;

var zoom = ZOOM_FACTOR;

var updateTimerId = void 0;
var proxyContainer = void 0;

var GUIContext = {
    UIState: _uiState2.default,
    update: function update() {
        if (updateTimerId) {
            clearTimeout(updateTimerId);
        }

        updateTimerId = setTimeout(renderProxies, 10);
    },
    _register: function _register(guiElem) {
        var id = guiElem.props.id;

        //console.log("_register", id);

        var elem = elements[id];
        if (!elem) {
            elem = {
                id: guiElem.props.id,
                uiState: guiElem.props.uiState,
                onUpdate: guiElem.props.onUpdate,
                onFocus: guiElem.props.onFocus,
                onInteraction: guiElem.props.onInteraction,
                data: guiElem.props.data
            };
            elements[id] = elem;

            //console.log("NEW ELEMENT", elem);
        }
        GUIContext.update();

        return elem;
    },
    _deregister: function _deregister(guiElem) {
        var id = guiElem.props.id;

        //console.log("_deregister", id);

        var elem = elements[id];
        if (!elem) {
            throw new Error("Id '" + id + "' is not registered");
        }
        delete elements[id];

        GUIContext.update();
    },
    _getElements: function _getElements(id) {
        return elements;
    },
    getElementState: function getElementState(id, defaultUiState) {
        var elem = elements[id];
        if (!elem) {
            return defaultUiState;
        }

        return elem.uiState;
    },
    focus: function focus(id) {
        var elem = proxyContainer.firstChild;
        while (elem) {
            if (elem.getAttribute("data-id") === id) {
                elem.focus();
            }
            elem = elem.nextSibling;
        }
    },
    _setElementState: function _setElementState(id, uiState, noUpdate) {
        //console.log("Set %s to %s", id, uiState);

        if (!_uiState2.default.isValid(uiState)) {
            throw new Error("Invalid ui state " + uiState);
        }

        var elem = elements[id];
        if (!elem) {
            throw new Error("Id '" + id + "' is not registered");
        }

        if (uiState === _uiState2.default.FOCUSED && elem.onFocus) {
            elem.onFocus();
        }

        elem.uiState = uiState;

        if (!noUpdate) {
            elem.onUpdate && elem.onUpdate.call(null);
        }
    },
    setZoom: function setZoom(newZoom) {
        zoom = newZoom;
    },
    getZoom: function getZoom() {
        return zoom;
    },
    applyZoom: function applyZoom(v) {
        return v * zoom / ZOOM_FACTOR;
    }

};

var FocusProxies = function (_React$Component) {
    _inherits(FocusProxies, _React$Component);

    function FocusProxies() {
        _classCallCheck(this, FocusProxies);

        return _possibleConstructorReturn(this, (FocusProxies.__proto__ || Object.getPrototypeOf(FocusProxies)).apply(this, arguments));
    }

    _createClass(FocusProxies, [{
        key: "onFocus",
        value: function onFocus(ev) {
            var id = ev.target.dataset.id;
            GUIContext._setElementState(id, _uiState2.default.FOCUSED);
        }
    }, {
        key: "onBlur",
        value: function onBlur(ev) {
            var id = ev.target.dataset.id;
            GUIContext._setElementState(id, _uiState2.default.NORMAL);
        }
    }, {
        key: "onUpdate",
        value: function onUpdate() {
            this.forceUpdate();
        }
    }, {
        key: "render",
        value: function render() {
            //console.log("render proxies", this.props.elements);
            var proxies = [];
            var elements = this.props.elements;
            for (var id in elements) {
                if (elements.hasOwnProperty(id)) {
                    var elem = elements[id];
                    var isDisabled = elem.uiState === _uiState2.default.DISABLED;
                    //console.log("render proxy", elem, isDisabled);
                    proxies.push(_react2.default.createElement(isDisabled ? "span" : "a", {
                        key: id,
                        href: "#",
                        "data-id": id,
                        onClick: isDisabled ? null : elem.onInteraction
                    }, id));
                }
            }

            return _react2.default.createElement(
                "div",
                { onFocusCapture: this.onFocus, onBlurCapture: this.onBlur },
                proxies
            );
        }
    }]);

    return FocusProxies;
}(_react2.default.Component);

function getProxyContainer() {
    if (!proxyContainer) {
        proxyContainer = document.createElement("div");
        proxyContainer.setAttribute("id", CONTAINER_ID);
        document.body.appendChild(proxyContainer);
    }
    return proxyContainer;
}

function renderProxies() {
    updateTimerId = null;
    var proxyContainer = getProxyContainer();

    _reactDom2.default.render(_react2.default.createElement(FocusProxies, {
        elements: elements
    }), proxyContainer /*, function ()
                       {
                       //        console.log("proxies updated");
                       }*/);
}

exports.default = GUIContext;

/***/ }),

/***/ 97:
/*!********************************!*\
  !*** ./editor/gui/ui-state.js ***!
  \********************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
  value: true
});

var _enum = __webpack_require__(/*! ../../util/enum */ 44);

var _enum2 = _interopRequireDefault(_enum);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

/**
 * @type UIState
 */
var UIState = new _enum2.default({
  NORMAL: 1,
  DISABLED: 1,
  FOCUSED: 1
});

exports.default = UIState;

/***/ })

},[236]);
//# sourceMappingURL=exceed-app-04c40fd9050cd64d681f.js.map