/**
 * SVG loader / merger method.
 *
 * Loads an SVG file and matches SVG element with a given map function or a built-in one.
 *
 * loadSymbols(path, mapFn)
 *
 * The map fn must return the group to which the element belongs or null if the element
 * is not supposed to be included in any group either because it is not important or
 * because a parent already is part of a group.
 *
 * All elements of the same group will be joined in a new group element that is
 * translated so that the AABB of all elements lies at the origin.
 *
 * The default impl matches all elements with an id matching "symbol:name:id" where name
 * is the group name and "id" a element-specific id.
 *
 * The methods returns a promise resolving to an object mapping all group names to a
 * Symbol instance.
 *
 *
 */
        var allSymbols = {};

        var symbolCount = 0;

        function mapSymbolColon(id) {

            var result = null;
            var m = /^symbol:(.*):/.exec(id);
            if (m) {
                result = m[1];
            }

            //console.debug("%s => %s", id, result);

            return result;
        }

        function outer(elem)
        {
            var $elem = $(elem).clone();

            var $container = $("<div></div>");
            $container.append($elem);

            return $container.html();
        }

        var SymbolHelper = React.createClass({
            registerBox:
                function(name, cb, html)
                {
                    allSymbols[this.props.path][name] = new Symbol(name, cb, html);
                },
            getSymbolMap:
                function()
                {
                    return {};
                },
            render: function () {

                var svg = $('<div></div>');
                svg.html(this.props.rawSVG);

                var styles = {
                    visibility: "hidden"
                };

                var data = "";

                var groups = {};
                var groupsArray = [];

                var elems = svg.find("*");
                var commonStyles = {};
                for (var i = 0; i < elems.length; i++)
                {
                    var elem = elems[i];
                    var grpName;
                    if (elem.id && (grpName = this.props.map(elem.id)))
                    {
                        var data = outer(elem);

                        var grpAttrs = groups[grpName];
                        if (grpAttrs)
                        {
                            grpAttrs.html += data;
                        }
                        else
                        {
                            var ticket = ++symbolCount;

                            var attrs = {
                                key: "grp-" + ticket,
                                ticket: ticket,
                                name: grpName,
                                html: data,
                                registerBox: this.registerBox
                            };

                            groups[grpName] = attrs;

                            groupsArray.push(Group(attrs));
                        }
                    }
                }

                allSymbols[this.props.path] = {};

                var b = React.DOM;
                return b.svg({
                        version:"1.1",
                        width:"1",
                        height:"1"
                    },
                    groupsArray
                );
            }
        });

        var Group = React.createClass({

            componentDidMount:
                function()
                {
                    var rootNode = this.getDOMNode();
                    var cb = rootNode.getBBox();

//                    console.debug("cb for %s = %o", this.props.name, cb);

                    // innerHTML on SVG requires the innerSVG shim in some browsers
                    this.props.registerBox(this.props.name, cb,
                        rootNode.innerHTML
                            .replace(/ data-reactid="[^"]+"/g, "")
                            .replace(/symbol:/g, "symbol" + this.props.ticket + ":")
                    );
                },
            render: function () {

                var inner = { __html: this.props.html };

                var b = React.DOM;
                return b.g({
                        dangerouslySetInnerHTML: inner
                    }
                );
            }
        });

        var loadSymbols= function(path, map)
        {
            if (path === "clear")
            {
                allSymbols = {};
                return;
            }
            else if (map === "clear")
            {
                delete allSymbols[path];
                return;
            }

            var symbolsDef = $.Deferred();

            var cachedSymbols = allSymbols[path];
            if (cachedSymbols)
            {
                symbolsDef.resolveWith(this,[cachedSymbols]);
            }
            else
            {
                $.ajax({
                    url: path,
                    dataType: "text"
                }).then(function(txt){

                    // cleanup unneeded namespaces
                    txt = txt.replace(/(xmlns:sodipodi|xmlns:inkscape|inkscape:[^=]+|sodipodi:[^=]+)="[^"]*"\s*/g,"");

                    //console.debug("filtered: %s", txt);


                    var $anchor = $("<div></div>", {
                        position: "absolute",
                        left: -10000,
                        top: 0
                    });

                    $(document.body).append($anchor);

                    var helper = SymbolHelper({
                        path: path,
                        rawSVG: txt,
                        map: map || mapSymbolColon
                    });

                    React.renderComponent(helper, $anchor[0], function(){

                        var symbols = allSymbols[path];
                        symbolsDef.resolveWith(this, [symbols])

                        React.unmountComponentAtNode(helper);
                        $anchor.remove();
                    });

                });
            };

            return symbolsDef.promise();
        };

        var Symbol = common.Class.extend({
            init:
                function(name,cb,html)
                {
                    this.name = name;
                    this.width  = +cb.width;
                    this.height = +cb.height;
                    this.offX   = -cb.x;
                    this.offY   = -cb.y;
                    this.raw = html;
                },
            create:
                function( attrs)
                {

                    if (attrs.scale)
                    {
                        var scale = attrs.scale;
                        delete attrs.scale;

                        attrs.transform =  "translate(" + ( ( this.offX * scale ) + attrs.x ) + "," + ( (this.offY * scale ) + attrs.y ) +") scale(" + scale + ")";
                    }
                    else
                    {
                        attrs.transform =  "translate(" + (this.offX + attrs.x) + "," + ( this.offY + attrs.y ) +")";
                    }

                    delete attrs.x;
                    delete attrs.y;

                    attrs.dangerouslySetInnerHTML = {__html: this.raw};

                    return React.DOM.g(attrs);
                }
        });

module.exports =  loadSymbols;
