var Exceed =
webpackJsonpExceed([2],{

/***/ 234:
/*!*******************************!*\
  !*** ./editor/UndoManager.js ***!
  \*******************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
    value: true
});
exports.UndoManager = exports.MAX_UNDO = undefined;

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _keys = __webpack_require__(/*! ../util/keys */ 25);

var _keys2 = _interopRequireDefault(_keys);

var _store = __webpack_require__(/*! ../service/store */ 3);

var _store2 = _interopRequireDefault(_store);

var _editor = __webpack_require__(/*! ../actions/editor */ 40);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

// last valid index in the undo buffer
var MAX_UNDO = exports.MAX_UNDO = 63;

var UndoManager = exports.UndoManager = function () {
    function UndoManager(store, nonUndoable) {
        _classCallCheck(this, UndoManager);

        _initialiseProps.call(this);

        this.store = store;
        this.data = new Array(MAX_UNDO + 1);

        this.pos = 0;
        this.last = 0;
        this.clean = 0;

        this.nonUndoable = nonUndoable;
    }

    _createClass(UndoManager, [{
        key: "canUndo",
        value: function canUndo() {
            return this.pos > 0;
        }
    }, {
        key: "isClean",
        value: function isClean() {
            return this.pos === this.clean;
        }
    }, {
        key: "markClean",
        value: function markClean() {
            this.clean = this.pos;
        }
    }, {
        key: "canRedo",
        value: function canRedo() {
            return this.pos < this.last;
        }
    }, {
        key: "getCleanState",
        value: function getCleanState() {
            return this.data[this.clean];
        }
    }, {
        key: "update",
        value: function update(state) {
            this.data[this.pos] = state;
        }
    }, {
        key: "insert",
        value: function insert(state) {

            if (this.pos === MAX_UNDO) {
                for (var i = 1; i <= MAX_UNDO; i++) {
                    this.data[i - 1] = this.data[i];
                }
                this.data[this.pos] = state;
            } else {
                this.data[++this.pos] = state;
                this.last = this.pos;
            }
        }
    }]);

    return UndoManager;
}();

var _initialiseProps = function _initialiseProps() {
    var _this = this;

    this.undo = function () {
        if (_this.canUndo()) {
            _this.store.dispatch((0, _editor.restoreEditorState)(_this.data[--_this.pos]));
        }
    };

    this.redo = function () {
        if (_this.canRedo()) {
            _this.store.dispatch((0, _editor.restoreEditorState)(_this.data[++_this.pos]));
        }
    };

    this.middleWare = function (store) {
        var sliceNames = (0, _keys2.default)(store.getState()).filter(function (name) {
            return name !== _this.nonUndoable;
        });

        //console.log("Undoable slices: ", sliceNames);

        return function (next) {
            return function (action) {

                var oldState = store.getState();
                var result = next(action);
                var newState = store.getState();

                if (action.type !== _editor.EDITOR_STATE_RESTORE) {
                    for (var i = 0; i < sliceNames.length; i++) {
                        var name = sliceNames[i];
                        if (oldState[name] !== newState[name]) {
                            //console.log("Insert undo");
                            _this.insert(newState);
                            return result;
                        }
                    }
                }

                //console.log("Update undo");
                _this.update(newState);
                return result;
            };
        };
    };
};

exports.default = new UndoManager(_store2.default, "editorView");

/***/ }),

/***/ 235:
/*!*****************************!*\
  !*** ./editor/ModelLink.js ***!
  \*****************************/
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

var _store = __webpack_require__(/*! ../service/store */ 3);

var _store2 = _interopRequireDefault(_store);

var _uri = __webpack_require__(/*! ../util/uri */ 15);

var _uri2 = _interopRequireDefault(_uri);

var _sys = __webpack_require__(/*! ../sys */ 12);

var _sys2 = _interopRequireDefault(_sys);

var _editorView = __webpack_require__(/*! ../actions/editor/editorView */ 136);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var ModelLink = function (_React$Component) {
    _inherits(ModelLink, _React$Component);

    function ModelLink() {
        var _ref;

        var _temp, _this, _ret;

        _classCallCheck(this, ModelLink);

        for (var _len = arguments.length, args = Array(_len), _key = 0; _key < _len; _key++) {
            args[_key] = arguments[_key];
        }

        return _ret = (_temp = (_this = _possibleConstructorReturn(this, (_ref = ModelLink.__proto__ || Object.getPrototypeOf(ModelLink)).call.apply(_ref, [this].concat(args))), _this), _this.navigate = function (ev) {

            ev.preventDefault();

            try {
                var _this$props = _this.props,
                    type = _this$props.type,
                    name = _this$props.name,
                    params = _this$props.params;


                _store2.default.dispatch((0, _editorView.navigateEditor)({
                    type: type,
                    name: name,
                    detail: params && params.detail,
                    resultType: params && params.resultType,
                    uri: _this.renderURI()
                }));
            } catch (e) {
                console.error(e);
            }
        }, _temp), _possibleConstructorReturn(_this, _ret);
    }

    _createClass(ModelLink, [{
        key: "render",
        value: function render() {
            var _props = this.props,
                type = _props.type,
                name = _props.name,
                children = _props.children,
                currentLocation = _props.currentLocation,
                filter = _props.filter;


            var haveName = !!name;
            var isActive = currentLocation.type === type && (!name && !currentLocation.name || currentLocation.name === name);

            var tag = isActive ? "span" : "a";

            if (filter && (haveName ? name : type).indexOf(filter) < 0) {
                return false;
            }

            //console.log(type, name, currentLocation, isActive);

            return _react2.default.createElement(tag, {
                className: "model-link",
                href: isActive ? null : this.renderURI(),
                onClick: this.navigate
            }, children);
        }
    }, {
        key: "renderURI",
        value: function renderURI() {
            var _props2 = this.props,
                type = _props2.type,
                name = _props2.name,
                params = _props2.params,
                hash = _props2.hash;


            var haveName = !!name;
            return (0, _uri2.default)("/editor/{app}/{type}{name}", {
                app: _sys2.default.appName,
                type: type,
                name: haveName ? "/" + name : "",
                detail: params && JSON.stringify(params.detail),
                resultType: params && params.resultType
            }) + (hash ? "#" + hash : "");
        }
    }]);

    return ModelLink;
}(_react2.default.Component);

exports.default = ModelLink;

/***/ }),

/***/ 467:
/*!************************!*\
  !*** ./editor-main.js ***!
  \************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
    value: true
});

var _slicedToArray = function () { function sliceIterator(arr, i) { var _arr = []; var _n = true; var _d = false; var _e = undefined; try { for (var _i = arr[Symbol.iterator](), _s; !(_n = (_s = _i.next()).done); _n = true) { _arr.push(_s.value); if (i && _arr.length === i) break; } } catch (err) { _d = true; _e = err; } finally { try { if (!_n && _i["return"]) _i["return"](); } finally { if (_d) throw _e; } } return _arr; } return function (arr, i) { if (Array.isArray(arr)) { return arr; } else if (Symbol.iterator in Object(arr)) { return sliceIterator(arr, i); } else { throw new TypeError("Invalid attempt to destructure non-iterable instance"); } }; }(); //
// load our global-undo to patch MouseTrap
//
//noinspection JSUnusedLocalSymbols


var _aceLoader = __webpack_require__(/*! ./editor/ace-loader */ 81);

var _aceLoader2 = _interopRequireDefault(_aceLoader);

var _event = __webpack_require__(/*! ./util/event */ 60);

var _event2 = _interopRequireDefault(_event);

var _es6PromisePolyfill = __webpack_require__(/*! es6-promise-polyfill */ 19);

var _editorView = __webpack_require__(/*! ./reducers/editor/editorView */ 90);

var _react = __webpack_require__(/*! react */ 0);

var _react2 = _interopRequireDefault(_react);

var _reactRedux = __webpack_require__(/*! react-redux */ 113);

var _reactDom = __webpack_require__(/*! react-dom */ 13);

var _editor = __webpack_require__(/*! ./reducers/editor */ 99);

var _editor2 = _interopRequireDefault(_editor);

var _startup = __webpack_require__(/*! ./util/startup */ 103);

var _domready = __webpack_require__(/*! domready */ 173);

var _domready2 = _interopRequireDefault(_domready);

var _createStore = __webpack_require__(/*! ./create-store */ 174);

var _createStore2 = _interopRequireDefault(_createStore);

var _store = __webpack_require__(/*! ./service/store */ 3);

var _store2 = _interopRequireDefault(_store);

var _reducers = __webpack_require__(/*! ./reducers */ 23);

var _editorView2 = __webpack_require__(/*! ./actions/editor/editorView */ 136);

var _security = __webpack_require__(/*! ./service/security */ 62);

var _security2 = _interopRequireDefault(_security);

var _UndoManager = __webpack_require__(/*! ./editor/UndoManager */ 234);

var _UndoManager2 = _interopRequireDefault(_UndoManager);

var _services = __webpack_require__(/*! ./services */ 176);

var _services2 = _interopRequireDefault(_services);

var _sys = __webpack_require__(/*! ./sys */ 12);

var _sys2 = _interopRequireDefault(_sys);

var _domain = __webpack_require__(/*! ./service/domain */ 16);

var _domain2 = _interopRequireDefault(_domain);

var _i18n = __webpack_require__(/*! ./service/i18n */ 9);

var _i18n2 = _interopRequireDefault(_i18n);

var _svgLayout = __webpack_require__(/*! ./gfx/svg-layout */ 95);

var _svgLayout2 = _interopRequireDefault(_svgLayout);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var initialState = (0, _startup.evaluateEmbedded)("root-data", "x-ceed/view-data");
_store2.default._init(null, initialState);

//console.log("Initial state", initialState);

var store = (0, _createStore2.default)(_editor2.default, initialState, [_UndoManager2.default.middleWare]);
_store2.default._init(store, null);

var auth = (0, _reducers.getAuthentication)(initialState);
_security2.default.init(auth.userName, auth.roles);

var Editor = __webpack_require__(/*! ./editor/Editor */ 470).default;

var hub = __webpack_require__(/*! ./service/hub */ 49);
var editorNavHistory = __webpack_require__(/*! ./editor/nav-history */ 212).default;

(0, _domready2.default)(function () {
    //console.log("DOMREADY");

    var state = _store2.default.getState();

    _sys2.default.init((0, _reducers.getContextPath)(state), (0, _reducers.getAppName)(state));
    _domain2.default.init((0, _reducers.getDomainData)(state));

    // set correct public path for dynamic module loading.
    var scriptResourcePath = _sys2.default.contextPath + "/res/" + _sys2.default.appName + "/js/";
    // noinspection JSUndeclaredVariable
    __webpack_require__.p = scriptResourcePath;

    // async setup
    _es6PromisePolyfill.Promise.all([_aceLoader2.default.load(), _svgLayout2.default.init(), hub.init((0, _reducers.getConnectionId)(state))]).then(function (_ref) {
        var _ref2 = _slicedToArray(_ref, 1),
            ace = _ref2[0];

        //        console.log(ace);

        var state = store.getState();
        editorNavHistory.init();
        editorNavHistory.update((0, _editorView.getEditorView)(state));

        _UndoManager2.default.update(state);

        if (typeof window !== "undefined") {
            _event2.default.add(window, "beforeunload", function (ev) {
                if (!_UndoManager2.default.isClean()) {
                    var message = 'WARNING: Lose unsaved changes in Editor?';
                    ev.returnValue = message;
                    return message;
                }
            }, true);
        }

        store.dispatch((0, _editorView2.syncEditor)());

        return new _es6PromisePolyfill.Promise(function (resolve, reject) {
            return (0, _reactDom.render)(_react2.default.createElement(
                _reactRedux.Provider,
                { store: store },
                _react2.default.createElement(Editor, null)
            ), document.getElementById("root"), resolve);
        });
    }).then(function () {
        var scripts = (0, _startup.findBundles)(scriptResourcePath);
        console.info((0, _i18n2.default)("READY: Loaded {0} ( {1} )", scripts.join(", "), new Date().toISOString()));
    });
    // .catch(function (e)
    // {
    //     console.error(e);
    // });
});

exports.default = _services2.default;

/***/ }),

/***/ 468:
/*!***********************************!*\
  !*** ./reducers/editor/editor.js ***!
  \***********************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
    value: true
});

exports.default = function () {
    var editorState = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : {};
    var action = arguments[1];

    if (action.type === _config.EDITOR_CONFIG_UPDATE) {
        return action.graph;
    }

    return editorState;
};

exports.getEditorData = getEditorData;
exports.getAppConfig = getAppConfig;
exports.getViews = getViews;
exports.getView = getView;
exports.getRoutingTable = getRoutingTable;
exports.getProcesses = getProcesses;
exports.getProcess = getProcess;
exports.getLayouts = getLayouts;
exports.getLayout = getLayout;
exports.getEnumTypes = getEnumTypes;
exports.getEnumType = getEnumType;
exports.getDomainTypes = getDomainTypes;
exports.getDomainType = getDomainType;
exports.getTranslations = getTranslations;

var _config = __webpack_require__(/*! ../../actions/editor/config */ 213);

function getEditorData(state) {
    return state.editor;
}

function getAppConfig(state) {
    return getEditorData(state).rootObject.config;
}

function getViews(state) {
    return getEditorData(state).rootObject.view;
}

function getView(state, name) {
    var view = getViews(state)[name];
    //console.log("VIEW", name, "=>", view, getViews(state));

    return view;
}

function getRoutingTable(state) {
    return getEditorData(state).rootObject.routing;
}

function getProcesses(state) {
    return getEditorData(state).rootObject.process;
}

function getProcess(state, name) {
    return getProcesses(state)[name];
}

function getLayouts(state) {
    return getEditorData(state).rootObject.layout;
}

function getLayout(state, name) {
    return getLayouts(state)[name];
}

function getEnumTypes(state) {
    return getEditorData(state).rootObject.enumType;
}

function getEnumType(state, name) {
    return getEnumTypes(state)[name];
}

function getDomainTypes(state) {
    return getEditorData(state).rootObject.domainType;
}

function getDomainType(state, name) {
    return getDomainTypes(state)[name];
}

function getTranslations(state) {
    return getEditorData(state).rootObject.translation;
}

/***/ }),

/***/ 469:
/*!*********************************!*\
  !*** ./reducers/editor/meta.js ***!
  \*********************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
    value: true
});
exports.getResourceLocations = getResourceLocations;
exports.getModelDocs = getModelDocs;
exports.getModelLocations = getModelLocations;

var _meta = __webpack_require__(/*! ../meta */ 14);

function getResourceLocations(state) {
    return (0, _meta.getMeta)(state).editor.resources;
}

function getModelDocs(state) {
    return (0, _meta.getMeta)(state).modelDocs;
}

function getModelLocations(state) {
    return (0, _meta.getMeta)(state).modelLocations;
}

/***/ }),

/***/ 470:
/*!**************************!*\
  !*** ./editor/Editor.js ***!
  \**************************/
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

var _ComponentSubscription = __webpack_require__(/*! ../util/ComponentSubscription */ 140);

var _ComponentSubscription2 = _interopRequireDefault(_ComponentSubscription);

var _UndoManager = __webpack_require__(/*! ../editor/UndoManager */ 234);

var _UndoManager2 = _interopRequireDefault(_UndoManager);

var _ModelSelector = __webpack_require__(/*! ./ModelSelector */ 471);

var _ModelSelector2 = _interopRequireDefault(_ModelSelector);

var _ConfigEditor = __webpack_require__(/*! ./ConfigEditor */ 473);

var _ConfigEditor2 = _interopRequireDefault(_ConfigEditor);

var _AutoHeight = __webpack_require__(/*! ../ui/AutoHeight */ 475);

var _AutoHeight2 = _interopRequireDefault(_AutoHeight);

var _Button = __webpack_require__(/*! react-bootstrap/lib/Button */ 145);

var _Button2 = _interopRequireDefault(_Button);

var _editorView = __webpack_require__(/*! ../reducers/editor/editorView */ 90);

var _editor = __webpack_require__(/*! ../reducers/editor */ 99);

var _i18n = __webpack_require__(/*! ../service/i18n */ 9);

var _i18n2 = _interopRequireDefault(_i18n);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var MODEL_SELECTORS = {
    config: _editor.getAppConfig,
    routing: _editor.getRoutingTable,
    domainType: _editor.getDomainType,
    enumType: _editor.getEnumType,
    process: _editor.getProcess,
    view: _editor.getView,
    layout: _editor.getLayout,
    translation: _editor.getTranslations
};

function JSONDump(props) {
    var model = props.model;


    return _react2.default.createElement(
        "pre",
        null,
        JSON.stringify(model, null, 4)
    );
}

function getModelSelector(type) {
    var selector = MODEL_SELECTORS[type];

    if (!selector) {
        throw new Error("No selector for type '" + type + "'");
    }

    return selector;
}

/**
 *
 * @name EditorLocation
 * @type {{
 *     type: string,
 *     name: string?,
 *     resultType: string?
 *     detail: string|object
 * }}
 */

/**
 * Editor main component
 *
 * @type {ReactComponent}
 */
var Editor = (0, _ComponentSubscription2.default)((0, _AutoHeight2.default)(function (_React$Component) {
    _inherits(_class2, _React$Component);

    function _class2() {
        var _ref;

        var _temp, _this, _ret;

        _classCallCheck(this, _class2);

        for (var _len = arguments.length, args = Array(_len), _key = 0; _key < _len; _key++) {
            args[_key] = arguments[_key];
        }

        return _ret = (_temp = (_this = _possibleConstructorReturn(this, (_ref = _class2.__proto__ || Object.getPrototypeOf(_class2)).call.apply(_ref, [this].concat(args))), _this), _this.save = function (ev) {
            //            console.log("SAVE");
        }, _temp), _possibleConstructorReturn(_this, _ret);
    }

    _createClass(_class2, [{
        key: "open",
        value: function open(type, path) {
            //            console.log({type,path});
        }
    }, {
        key: "render",
        value: function render() {
            var _props = this.props,
                height = _props.height,
                store = _props.store;


            var state = store.getState();
            var currentLocation = (0, _editorView.getCurrentEditorLocation)(state);
            var currentModel = getModelSelector(currentLocation.type)(state, currentLocation.name);

            var editorPaneComponent = MODEL_EDITORS[currentLocation.type] || JSONDump;

            return _react2.default.createElement(
                "div",
                { className: "main-editor container-fluid" },
                _react2.default.createElement(
                    "div",
                    { className: "row" },
                    _react2.default.createElement(_ModelSelector2.default, { store: this.props.store, currentLocation: currentLocation, filter: (0, _editorView.getFilter)(state), height: height }),
                    _react2.default.createElement(
                        "div",
                        { className: "model-container col-md-10", style: { height: height } },
                        _react2.default.createElement(
                            "div",
                            { className: "btn-toolbar", style: { padding: "5px 0" } },
                            _react2.default.createElement(
                                _Button2.default,
                                {
                                    disabled: !_UndoManager2.default.canUndo(),
                                    onClick: _UndoManager2.default.undo },
                                (0, _i18n2.default)("Undo")
                            ),
                            _react2.default.createElement(
                                _Button2.default,
                                {
                                    disabled: !_UndoManager2.default.canRedo(),
                                    onClick: _UndoManager2.default.redo },
                                (0, _i18n2.default)("Redo")
                            ),
                            _react2.default.createElement(
                                _Button2.default,
                                {
                                    disabled: _UndoManager2.default.isClean(),
                                    bsStyle: "primary",
                                    onClick: this.save },
                                ">",
                                (0, _i18n2.default)("Save")
                            )
                        ),
                        _react2.default.createElement(editorPaneComponent, {
                            store: store,
                            location: currentLocation,
                            model: currentModel
                        })
                    )
                )
            );
        }
    }]);

    return _class2;
}(_react2.default.Component)));

var MODEL_EDITORS = {
    "config": _ConfigEditor2.default
};

exports.default = Editor;

/***/ }),

/***/ 471:
/*!*********************************!*\
  !*** ./editor/ModelSelector.js ***!
  \*********************************/
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

var _ModelLink = __webpack_require__(/*! ./ModelLink */ 235);

var _ModelLink2 = _interopRequireDefault(_ModelLink);

var _NamedGroup = __webpack_require__(/*! ./NamedGroup */ 472);

var _NamedGroup2 = _interopRequireDefault(_NamedGroup);

var _memoizer = __webpack_require__(/*! ../util/memoizer */ 80);

var _memoizer2 = _interopRequireDefault(_memoizer);

var _sys = __webpack_require__(/*! ../sys */ 12);

var _sys2 = _interopRequireDefault(_sys);

var _uri = __webpack_require__(/*! ../util/uri */ 15);

var _uri2 = _interopRequireDefault(_uri);

var _debounce = __webpack_require__(/*! ../util/debounce */ 230);

var _debounce2 = _interopRequireDefault(_debounce);

var _i18n = __webpack_require__(/*! ../service/i18n */ 9);

var _i18n2 = _interopRequireDefault(_i18n);

var _Icon = __webpack_require__(/*! ../ui/Icon */ 91);

var _Icon2 = _interopRequireDefault(_Icon);

var _editor = __webpack_require__(/*! ../actions/editor */ 40);

var _editor2 = __webpack_require__(/*! ../reducers/editor */ 99);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var RESULT_TYPE_LABELS = {
    NAME: (0, _i18n2.default)("SearchResult:Name"),
    REFERENCE: (0, _i18n2.default)("SearchResult:Reference"),
    TITLE: (0, _i18n2.default)("SearchResult:Title"),
    COMPONENT: (0, _i18n2.default)("SearchResult:Component"),
    ATTRIBUTE: (0, _i18n2.default)("SearchResult:Attribute"),
    STATE: (0, _i18n2.default)("SearchResult:State"),
    PROPERTY: (0, _i18n2.default)("SearchResult:Property"),
    ROUTE: (0, _i18n2.default)("SearchResult:route"),
    TRANSITION: (0, _i18n2.default)("SearchResult:Transition"),
    ACTION: (0, _i18n2.default)("SearchResult:Action"),
    EXPRESSION: (0, _i18n2.default)("SearchResult:Expression"),
    CONTEXT: (0, _i18n2.default)("SearchResult:Context"),
    SCOPE: (0, _i18n2.default)("SearchResult:Scope"),
    DESCRIPTION: (0, _i18n2.default)("SearchResult:Description"),
    DEFAULT_VALUE: (0, _i18n2.default)("SearchResult:Default Value"),
    COMPONENT_ID: (0, _i18n2.default)("SearchResult:Component Id"),
    QUERY: (0, _i18n2.default)("SearchResult:Query")
};

var MODEL_TYPE_TO_EDITOR_TYPE = {
    "xcd.ApplicationConfig": "config",
    "xcd.routing.RoutingTable": "routing",
    "xcd.domain.DomainType": "domainType",
    "xcd.domain.EnumType": "enumType",
    "xcd.process.Process": "process",
    "xcd.view.View": "view",
    "xcd.view.LayoutModel": "layout"
};

function getViewSelector(process) {
    return function (views) {

        var filtered = {};

        for (var name in views) {
            if (views.hasOwnProperty(name)) {
                var view = views[name];
                if (!!view.processName === process) {
                    filtered[name] = name;
                }
            }
        }

        return filtered;
    };
}

function getEditorType(modelType) {
    var editorType = MODEL_TYPE_TO_EDITOR_TYPE[modelType];
    if (!editorType) {
        throw new Error("Unhandled model type: " + modelType);
    }
    return editorType;
}

var getStandaloneViews = (0, _memoizer2.default)(getViewSelector(false));
var getProcessViews = (0, _memoizer2.default)(getViewSelector(true));

function SearchResults(props) {
    var results = props.results,
        currentLocation = props.currentLocation;


    return !!results.length && _react2.default.createElement(
        "div",
        { className: "search-results" },
        _react2.default.createElement(
            "h5",
            null,
            (0, _i18n2.default)("Search Results")
        ),
        results.map(function (result, idx) {
            return _react2.default.createElement(
                _ModelLink2.default,
                {
                    key: idx,
                    type: getEditorType(result.type),
                    name: result.name,
                    params: {
                        resultType: result.resultType,
                        detail: result.detail
                    },
                    currentLocation: currentLocation
                },
                _react2.default.createElement(
                    "span",
                    { className: "text-info pull-right" },
                    RESULT_TYPE_LABELS[result.resultType]
                ),
                result.name
            );
        })
    );
}

var ModelSelector = function (_React$Component) {
    _inherits(ModelSelector, _React$Component);

    function ModelSelector() {
        var _ref;

        var _temp, _this, _ret;

        _classCallCheck(this, ModelSelector);

        for (var _len = arguments.length, args = Array(_len), _key = 0; _key < _len; _key++) {
            args[_key] = arguments[_key];
        }

        return _ret = (_temp = (_this = _possibleConstructorReturn(this, (_ref = ModelSelector.__proto__ || Object.getPrototypeOf(ModelSelector)).call.apply(_ref, [this].concat(args))), _this), _this.onFilterChange = (0, _debounce2.default)(function (ev) {

            var searchTerm = _this._searchField.value || "";
            _this.search(searchTerm);
        }, 250), _this.clearFilter = function (ev) {

            _this._searchField.value = "";
            _this.search("");
        }, _temp), _possibleConstructorReturn(_this, _ret);
    }

    _createClass(ModelSelector, [{
        key: "search",
        value: function search(searchTerm) {
            this.props.store.dispatch((0, _editor.searchModel)(searchTerm.toLowerCase()));
        }
    }, {
        key: "render",
        value: function render() {
            var _this2 = this;

            var state = this.props.store.getState();
            var currentLocation = this.props.currentLocation;


            var currentFilter = (0, _editor2.getFilter)(state);

            var searchResults = (0, _editor2.getSearchResults)(state);

            return _react2.default.createElement(
                "div",
                null,
                _react2.default.createElement(
                    "div",
                    { className: "model-selector col-md-2", style: { height: this.props.height, background: "#f0f0f0", borderRight: "1px solid #ccc" } },
                    _react2.default.createElement(
                        "h4",
                        null,
                        _react2.default.createElement(
                            "a",
                            {
                                className: "pull-right",
                                title: (0, _i18n2.default)("Go To App {0}", _sys2.default.appName),
                                href: (0, _uri2.default)("/app/" + _sys2.default.appName)
                            },
                            _react2.default.createElement(_Icon2.default, { className: "glyphicon-link" })
                        ),
                        (0, _i18n2.default)("App {0}", _sys2.default.appName)
                    ),
                    _react2.default.createElement(
                        "form",
                        {
                            className: "form",
                            action: (0, _uri2.default)("/editor/" + _sys2.default.appName + "/search/"),
                            method: "GET",
                            onSubmit: this.onFilterChange },
                        _react2.default.createElement(
                            "div",
                            { className: "form-group" },
                            _react2.default.createElement(
                                "label",
                                { htmlFor: "model-search", className: "sr-only" },
                                (0, _i18n2.default)("Filter")
                            ),
                            _react2.default.createElement(
                                "div",
                                { className: "input-group" },
                                _react2.default.createElement("input", {
                                    ref: function ref(elem) {
                                        return _this2._searchField = elem;
                                    },
                                    id: "model-search",
                                    type: "text",
                                    className: "form-control",
                                    defaultValue: currentFilter,
                                    onChange: this.onFilterChange,
                                    placeholder: (0, _i18n2.default)("Partial model name")
                                }),
                                _react2.default.createElement(
                                    "span",
                                    { className: "input-group-btn" },
                                    _react2.default.createElement(
                                        "button",
                                        {
                                            className: "btn btn-default",
                                            disabled: !currentFilter,
                                            type: "button",
                                            onClick: this.clearFilter
                                        },
                                        _react2.default.createElement(_Icon2.default, { className: "glyphicon-erase" })
                                    )
                                )
                            )
                        )
                    ),
                    _react2.default.createElement(
                        _ModelLink2.default,
                        { type: "config", currentLocation: currentLocation, filter: currentFilter },
                        _react2.default.createElement(
                            "h5",
                            null,
                            _react2.default.createElement(_Icon2.default, { className: "glyphicon-cog text-info" }),
                            " " + (0, _i18n2.default)("Config")
                        )
                    ),
                    _react2.default.createElement(
                        _ModelLink2.default,
                        { type: "routing", currentLocation: currentLocation, filter: currentFilter },
                        _react2.default.createElement(
                            "h5",
                            null,
                            _react2.default.createElement(_Icon2.default, { className: "glyphicon-road text-info" }),
                            " " + (0, _i18n2.default)("Routing")
                        )
                    ),
                    _react2.default.createElement(
                        _ModelLink2.default,
                        { type: "translation", currentLocation: currentLocation, filter: currentFilter },
                        _react2.default.createElement(
                            "h5",
                            null,
                            _react2.default.createElement(_Icon2.default, { className: "glyphicon-globe text-info" }),
                            " " + (0, _i18n2.default)("Translations")
                        )
                    ),
                    _react2.default.createElement(
                        _NamedGroup2.default,
                        { type: "domainType", map: (0, _editor2.getDomainTypes)(state), currentLocation: currentLocation, filter: currentFilter },
                        (0, _i18n2.default)("Domain Types")
                    ),
                    _react2.default.createElement(
                        _NamedGroup2.default,
                        { type: "enumType", map: (0, _editor2.getEnumTypes)(state), currentLocation: currentLocation, filter: currentFilter },
                        (0, _i18n2.default)("Enum Types")
                    ),
                    _react2.default.createElement(
                        _NamedGroup2.default,
                        { type: "process", map: (0, _editor2.getProcesses)(state), currentLocation: currentLocation, filter: currentFilter, subType: "view", subItems: getProcessViews((0, _editor2.getViews)(state)), icon: "chevron-down", subIcon: "space" },
                        (0, _i18n2.default)("Processes")
                    ),
                    _react2.default.createElement(
                        _NamedGroup2.default,
                        { type: "view", map: getStandaloneViews((0, _editor2.getViews)(state)), currentLocation: currentLocation, filter: currentFilter },
                        (0, _i18n2.default)("Views")
                    ),
                    _react2.default.createElement(
                        _NamedGroup2.default,
                        { type: "layout", map: (0, _editor2.getLayouts)(state), currentLocation: currentLocation, filter: currentFilter },
                        (0, _i18n2.default)("Layouts")
                    ),
                    _react2.default.createElement(SearchResults, { results: searchResults, currentLocation: currentLocation }),
                    _react2.default.createElement("hr", null)
                )
            );
        }
    }]);

    return ModelSelector;
}(_react2.default.Component);

exports.default = ModelSelector;

/***/ }),

/***/ 472:
/*!******************************!*\
  !*** ./editor/NamedGroup.js ***!
  \******************************/
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

var _memoizer = __webpack_require__(/*! ../util/memoizer */ 80);

var _memoizer2 = _interopRequireDefault(_memoizer);

var _Icon = __webpack_require__(/*! ../ui/Icon */ 91);

var _Icon2 = _interopRequireDefault(_Icon);

var _keys = __webpack_require__(/*! ../util/keys */ 25);

var _keys2 = _interopRequireDefault(_keys);

var _ModelLink = __webpack_require__(/*! ./ModelLink */ 235);

var _ModelLink2 = _interopRequireDefault(_ModelLink);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

function sortEntries(a, b) {
    return a.name.localeCompare(b.name);
}

var getSortedModels = (0, _memoizer2.default)(function (map, type, icon) {

    return (0, _keys2.default)(map).map(function (name) {
        return {
            name: name,
            type: type,
            icon: icon
        };
    }).sort(sortEntries);
});

var getSortedModelsAndSubs = (0, _memoizer2.default)(function (map, type, icon, subType, subItems, subIcon) {

    //    console.log({type,subType,subItems});

    var entries = (0, _keys2.default)(map).map(function (name) {
        return {
            name: name,
            type: type,
            icon: icon
        };
    });

    if (subType) {
        entries = entries.concat((0, _keys2.default)(subItems).map(function (name) {
            return {
                name: name,
                type: subType,
                icon: subIcon
            };
        }));
    }

    return entries.sort(sortEntries);
});

var NamedGroup = function (_React$Component) {
    _inherits(NamedGroup, _React$Component);

    function NamedGroup() {
        var _ref;

        var _temp, _this, _ret;

        _classCallCheck(this, NamedGroup);

        for (var _len = arguments.length, args = Array(_len), _key = 0; _key < _len; _key++) {
            args[_key] = arguments[_key];
        }

        return _ret = (_temp = (_this = _possibleConstructorReturn(this, (_ref = NamedGroup.__proto__ || Object.getPrototypeOf(NamedGroup)).call.apply(_ref, [this].concat(args))), _this), _this.state = {
            open: true
        }, _this.toggle = function (ev) {

            _this.setState({
                open: !_this.state.open
            });

            ev.preventDefault();
        }, _temp), _possibleConstructorReturn(_this, _ret);
    }

    _createClass(NamedGroup, [{
        key: "render",
        value: function render() {
            var _props = this.props,
                map = _props.map,
                type = _props.type,
                currentLocation = _props.currentLocation,
                filter = _props.filter,
                icon = _props.icon,
                subIcon = _props.subIcon,
                subType = _props.subType,
                subItems = _props.subItems,
                children = _props.children;
            var open = this.state.open;


            var entries = subType ? getSortedModelsAndSubs(map, type, icon, subType, subItems, subIcon) : getSortedModels(map, type, icon);

            if (filter) {
                entries = entries.filter(function (entry) {
                    return entry.name.toLowerCase().indexOf(filter) >= 0;
                });
            }

            if (!entries.length) {
                return false;
            }

            return _react2.default.createElement(
                "div",
                { className: "group" },
                _react2.default.createElement(
                    "div",
                    { className: "header" },
                    _react2.default.createElement(
                        "h5",
                        null,
                        _react2.default.createElement(
                            "a",
                            { href: "#toggle", onClick: this.toggle },
                            _react2.default.createElement(_Icon2.default, { className: open ? "glyphicon-chevron-down" : "glyphicon-chevron-right" })
                        ),
                        " ",
                        children
                    )
                ),
                open && entries.map(function (_ref2) {
                    var name = _ref2.name,
                        type = _ref2.type,
                        icon = _ref2.icon;
                    return _react2.default.createElement(
                        _ModelLink2.default,
                        { key: name, type: type, name: name, currentLocation: currentLocation },
                        icon && _react2.default.createElement(_Icon2.default, { className: "glyphicon-" + icon }),
                        icon && " ",
                        name
                    );
                })
            );
        }
    }]);

    return NamedGroup;
}(_react2.default.Component);

exports.default = NamedGroup;

/***/ }),

/***/ 473:
/*!********************************!*\
  !*** ./editor/ConfigEditor.js ***!
  \********************************/
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

var _sys = __webpack_require__(/*! ../sys */ 12);

var _sys2 = _interopRequireDefault(_sys);

var _ScopeEditor = __webpack_require__(/*! ./code/ScopeEditor */ 222);

var _ScopeEditor2 = _interopRequireDefault(_ScopeEditor);

var _i18n = __webpack_require__(/*! ../service/i18n */ 9);

var _i18n2 = _interopRequireDefault(_i18n);

var _editor = __webpack_require__(/*! ../reducers/editor */ 99);

var _editor2 = __webpack_require__(/*! ../actions/editor */ 40);

var _Form = __webpack_require__(/*! ../components/std/form/Form */ 232);

var _Form2 = _interopRequireDefault(_Form);

var _Field = __webpack_require__(/*! ../components/std/form/Field */ 137);

var _Field2 = _interopRequireDefault(_Field);

var _StaticText = __webpack_require__(/*! ../components/std/form/StaticText */ 233);

var _StaticText2 = _interopRequireDefault(_StaticText);

var _FormProvider = __webpack_require__(/*! ../ui/FormProvider */ 474);

var _FormProvider2 = _interopRequireDefault(_FormProvider);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var ConfigEditor = function (_React$Component) {
    _inherits(ConfigEditor, _React$Component);

    function ConfigEditor() {
        _classCallCheck(this, ConfigEditor);

        return _possibleConstructorReturn(this, (ConfigEditor.__proto__ || Object.getPrototypeOf(ConfigEditor)).apply(this, arguments));
    }

    _createClass(ConfigEditor, [{
        key: "render",
        value: function render() {
            var _props = this.props,
                location = _props.location,
                model = _props.model,
                store = _props.store;


            var state = store.getState();
            return _react2.default.createElement(
                _FormProvider2.default,
                { update: _editor2.updateConfig },
                _react2.default.createElement(
                    "h4",
                    null,
                    " ",
                    (0, _i18n2.default)('{0} configuration', _sys2.default.appName),
                    " "
                ),
                _react2.default.createElement(
                    _Form2.default,
                    { data: (0, _editor.getEditorData)(state), path: ["config"] },
                    function (context) {
                        return _react2.default.createElement(_Field2.default, { value: "schema", context: context });
                    }
                )
            );
        }
    }]);

    return ConfigEditor;
}(_react2.default.Component);

exports.default = ConfigEditor;

/***/ }),

/***/ 474:
/*!****************************!*\
  !*** ./ui/FormProvider.js ***!
  \****************************/
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

var FormProvider = function (_React$Component) {
    _inherits(FormProvider, _React$Component);

    function FormProvider() {
        _classCallCheck(this, FormProvider);

        return _possibleConstructorReturn(this, (FormProvider.__proto__ || Object.getPrototypeOf(FormProvider)).apply(this, arguments));
    }

    _createClass(FormProvider, [{
        key: "render",
        value: function render() {
            var children = this.props.children;


            return _react2.default.createElement(
                "div",
                null,
                children
            );
        }
    }]);

    return FormProvider;
}(_react2.default.Component);

exports.default = FormProvider;

/***/ }),

/***/ 475:
/*!**************************!*\
  !*** ./ui/AutoHeight.js ***!
  \**************************/
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
        _inherits(AutoHeight, _React$Component);

        function AutoHeight() {
            var _ref;

            var _temp, _this, _ret;

            _classCallCheck(this, AutoHeight);

            for (var _len = arguments.length, args = Array(_len), _key = 0; _key < _len; _key++) {
                args[_key] = arguments[_key];
            }

            return _ret = (_temp = (_this = _possibleConstructorReturn(this, (_ref = AutoHeight.__proto__ || Object.getPrototypeOf(AutoHeight)).call.apply(_ref, [this].concat(args))), _this), _this.timerId = null, _this.state = {
                height: calculateHeight(Component.calculateHeight)
            }, _this.onResize = function (ev) {
                if (_this.timerId) {
                    window.clearTimeout(_this.timerId);
                }

                _this.timerId = window.setTimeout(function () {
                    _this.timerId = null;

                    _this.setState({
                        height: calculateHeight(Component.calculateHeight)
                    });
                }, 100);
                //console.log("RESIZE");
            }, _temp), _possibleConstructorReturn(_this, _ret);
        }

        _createClass(AutoHeight, [{
            key: "componentDidMount",
            value: function componentDidMount() {
                window && _event2.default.add(window, "resize", this.onResize, false);
            }
        }, {
            key: "componentWillUnmount",
            value: function componentWillUnmount() {
                window && _event2.default.remove(window, "resize", this.onResize, false);
            }
        }, {
            key: "render",
            value: function render() {
                return _react2.default.createElement(Component, _extends({
                    height: this.state.height
                }, this.props));
            }
        }]);

        return AutoHeight;
    }(_react2.default.Component);
};

var _event = __webpack_require__(/*! ../util/event */ 60);

var _event2 = _interopRequireDefault(_event);

var _react = __webpack_require__(/*! react */ 0);

var _react2 = _interopRequireDefault(_react);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

function calculateHeight(customFn) {
    if (customFn) {
        return customFn();
    } else {
        return window ? window.innerHeight : 500;
    }
}

/**
 * High level component to provide automatic vertical scaling to components. It calculates the height based on
 * the current available window height and updates on window resize.
 *
 * @param Component
 * @returns {*}
 */

/***/ }),

/***/ 99:
/*!**********************************!*\
  !*** ./reducers/editor/index.js ***!
  \**********************************/
/*! dynamic exports provided */
/*! all exports used */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
    value: true
});

var _editorView = __webpack_require__(/*! ./editorView */ 90);

Object.keys(_editorView).forEach(function (key) {
    if (key === "default" || key === "__esModule") return;
    Object.defineProperty(exports, key, {
        enumerable: true,
        get: function get() {
            return _editorView[key];
        }
    });
});

var _editor = __webpack_require__(/*! ./editor */ 468);

Object.keys(_editor).forEach(function (key) {
    if (key === "default" || key === "__esModule") return;
    Object.defineProperty(exports, key, {
        enumerable: true,
        get: function get() {
            return _editor[key];
        }
    });
});

var _meta = __webpack_require__(/*! ./meta */ 469);

Object.keys(_meta).forEach(function (key) {
    if (key === "default" || key === "__esModule") return;
    Object.defineProperty(exports, key, {
        enumerable: true,
        get: function get() {
            return _meta[key];
        }
    });
});

exports.default = function (state, action) {
    if (action.type === _editor3.EDITOR_STATE_RESTORE) {
        return action.state;
    }

    if (action.type === _editor3.EDITOR_UNDO_GROUP) {
        var actions = action.actions;


        for (var i = 0; i < actions.length; i++) {
            var _action = actions[i];
            state = editorReducers(state, _action);
        }

        return state;
    }

    return editorReducers(state, action);
};

var _redux = __webpack_require__(/*! redux */ 79);

var _meta2 = __webpack_require__(/*! ../meta */ 14);

var _meta3 = _interopRequireDefault(_meta2);

var _editor2 = _interopRequireDefault(_editor);

var _editorView2 = _interopRequireDefault(_editorView);

var _editor3 = __webpack_require__(/*! ../../actions/editor */ 40);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var editorReducers = (0, _redux.combineReducers)({

    // current editor view state (what object is selected, which editor shown etc)
    editorView: _editorView2.default,
    // complex editor data graph state
    editor: _editor2.default,
    // meta block
    meta: _meta3.default
});

/***/ })

},[467]);
//# sourceMappingURL=exceed-editor-9cc34e5272550ed412f8.js.map