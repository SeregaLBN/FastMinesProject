/** Main logos image - base Logo image view implementation */
class Logo extends ImageView {

    static get svgNS() { return 'http://www.w3.org/2000/svg'; }

    constructor() {
        super(new LogoModel());
    }

    drawIntoContext(/* DrawContext */ cnxt, /* own SVG */ svg, /* SVG document */ svgDoc) {
        var m = this.Model;
        var keyTime = m.currentFrame / m.totalFrames; // 0..1;
        var duration  = m.animatePeriod;
        var borderWidth = m.borderWidth;
        var backgroundColor = m.backgroundColor;
        var size = m.size;
        var zoomAverage = (m.zoomX + m.zoomY)/2;
        var animated = m.animated;
        var useGradient = m.useGradient;

        var rays = m.rays;
        var inn  = m.inn;
        var oct  = m.oct;
        var center = new Point(size.width/2.0, size.height/2.0);

        var hsvPalette = m.palette;
        var palette = hsvPalette.map(function(hsv) { return hsv.toColor(); });

        if (!cnxt.background && !backgroundColor.isTransparent) { // fill background
            // example: <rect x='0' y='0' width='100' height='100' fill='hsla(112.71, 50.95%, 82.35%, 1)' />
            // один раз (самий перший за всі малювання) створюю ноду для фонової заливки
            var rcNode = cnxt.background = svgDoc.createElementNS(Logo.svgNS, 'rect');
            rcNode.setAttributeNS(null, 'x'     , 0);
            rcNode.setAttributeNS(null, 'y'     , 0);
            rcNode.setAttributeNS(null, 'width' , size.width);
            rcNode.setAttributeNS(null, 'height', size.height);
            var bc = new HSV(backgroundColor);
            rcNode.setAttributeNS(null, 'fill'  , 'hsla(' + round(bc.h, dpClr) + ', ' + round(bc.s, dpClr) + '%, ' + round(bc.v, dpClr) + '%, ' + round(bc.a/255.0, dpClr) + ')');
            svg.appendChild(rcNode);
        }

        const useKeyTimes = false;

        // один раз створюю необхідні ноди
        if (!cnxt.ownRays) {
            var names = ['ownRays', 'innerTr'];
            if (useGradient)
                names = [...names, 'ownRays1', 'ownRays2'];
            for (var name of names) {
                cnxt[name] = [];
                for (var i=0; i<8; ++i) {
                    var pathNode = svgDoc.createElementNS(Logo.svgNS, 'path');
                  //pathNode.setAttributeNS(null, 'fill-opacity'  , round(foregroundColor.a/255.0, dpClr));
                    if (useGradient)
                        pathNode.setAttributeNS(null, 'fill', 'url(#' + name + i + ')');

                    if (animated) {
                        // examples: <animate attributeName='d' dur='2000ms' repeatCount='indefinite' calcMode='linear' ... />
                        pathNode.appendChild(createAnimateNode(svgDoc, 'd', duration, useKeyTimes ? null : 'linear'));

                        if (!useGradient)
                            // examples: <animate attributeName='fill' dur='2000ms' repeatCount='indefinite' calcMode='linear' ...>
                            pathNode.appendChild(createAnimateNode(svgDoc, 'fill', duration, useKeyTimes ? null : 'linear'));
                    }

                    svg.appendChild(pathNode);
                    cnxt[name].push(pathNode);
                }
            }

            if (borderWidth > 0) {
                cnxt.perim = []
                for (var i=0; i<8; ++i) {
                    var lineNode = svgDoc.createElementNS(Logo.svgNS, 'line');
                    lineNode.setAttributeNS(null, 'stroke-width', zoomAverage*borderWidth);
                    lineNode.setAttributeNS(null, 'stroke-linecap', 'round');
                    if (animated) {
                        lineNode.appendChild(createAnimateNode(svgDoc, 'stroke', duration, useKeyTimes ? null : 'linear'));
                        lineNode.appendChild(createAnimateNode(svgDoc, 'x1'    , duration, useKeyTimes ? null : 'linear'));
                        lineNode.appendChild(createAnimateNode(svgDoc, 'y1'    , duration, useKeyTimes ? null : 'linear'));
                        lineNode.appendChild(createAnimateNode(svgDoc, 'x2'    , duration, useKeyTimes ? null : 'linear'));
                        lineNode.appendChild(createAnimateNode(svgDoc, 'y2'    , duration, useKeyTimes ? null : 'linear'));
                    }

                    svg.appendChild(lineNode);
                    cnxt.perim.push(lineNode);
                }
            }

            if (useGradient) {
                var defsNode = svgDoc.createElementNS(Logo.svgNS, 'defs');
                svg.appendChild(defsNode);

                var names = ['ownRays', 'innerTr'];
                if (useGradient)
                    names = [...names, 'ownRays1', 'ownRays2'];
                for (var name of names) {
                    cnxt.gradient[name] = [];
                    for (var i=0; i<8; ++i) {
                        var linearGradientNode = svgDoc.createElementNS(Logo.svgNS, 'linearGradient');
                        linearGradientNode.setAttributeNS(null, 'id', name + i);

                        var stopNodeRun = svgDoc.createElementNS(Logo.svgNS, 'stop');
                        var stopNodeEnd = svgDoc.createElementNS(Logo.svgNS, 'stop');
                        stopNodeRun.setAttributeNS(null, 'offset', '0%');
                        stopNodeEnd.setAttributeNS(null, 'offset', '100%');
                        linearGradientNode.appendChild(stopNodeRun);
                        linearGradientNode.appendChild(stopNodeEnd);
                        linearGradientNode.setAttributeNS(null, 'gradientUnits', 'userSpaceOnUse');

                        if (animated) {
                            linearGradientNode.appendChild(createAnimateNode(svgDoc, 'x1'        , duration, useKeyTimes ? null : 'linear'));
                            linearGradientNode.appendChild(createAnimateNode(svgDoc, 'y1'        , duration, useKeyTimes ? null : 'linear'));
                            linearGradientNode.appendChild(createAnimateNode(svgDoc, 'x2'        , duration, useKeyTimes ? null : 'linear'));
                            linearGradientNode.appendChild(createAnimateNode(svgDoc, 'y2'        , duration, useKeyTimes ? null : 'linear'));
                            stopNodeRun       .appendChild(createAnimateNode(svgDoc, 'stop-color', duration, useKeyTimes ? null : 'linear'));
                            stopNodeEnd       .appendChild(createAnimateNode(svgDoc, 'stop-color', duration, useKeyTimes ? null : 'linear'));
                        }

                        defsNode.appendChild(linearGradientNode);
                        cnxt.gradient[name].push(linearGradientNode);
                    }
                }
            }
        }

        var setFill = function(/*Node*/ pathNode, /*Color*/ fillColor) {
            var pathFillColor = '#' + fillColor.asHexString.substr(2);
            if (!animated) {
                pathNode.setAttributeNS(null, 'fill', pathFillColor);
            } else {
                var animateNodeFill = [...pathNode.childNodes].find(function(x) { return x.getAttribute('attributeName') === 'fill'; });
                addAttributeValue(animateNodeFill, 'values', pathFillColor);
            }
        };
        var setGradientFill = function(/*linearGradient id*/ idName, /* int */ i, /*Point*/ start, /*Color*/ startClr, /*Point*/ end, /*Color*/ endClr) {
            var linearGradientNode = cnxt.gradient[idName][i];
            var childNodes = [...linearGradientNode.childNodes];
            var stopNodeRun = childNodes.find(function(x) { return x.getAttribute('offset') === '0%'; });
            var stopNodeEnd = childNodes.find(function(x) { return x.getAttribute('offset') === '100%'; });
            if (!startClr.isOpaque)
                stopNodeRun.setAttributeNS(null, 'stop-opacity', round(startClr.a/255.0, dpClr));
            if (!endClr.isOpaque)
                stopNodeEnd.setAttributeNS(null, 'stop-opacity', round(endClr.a/255.0, dpClr));
            if (!animated) {
                linearGradientNode.setAttributeNS(null, 'x1', round(start.x, dpPnt));
                linearGradientNode.setAttributeNS(null, 'y1', round(start.y, dpPnt));
                linearGradientNode.setAttributeNS(null, 'x2', round(end.x  , dpPnt));
                linearGradientNode.setAttributeNS(null, 'y2', round(end.y  , dpPnt));

                stopNodeRun.setAttributeNS(null, 'stop-color', '#' + startClr.asHexString.substr(2));
                stopNodeEnd.setAttributeNS(null, 'stop-color', '#' +   endClr.asHexString.substr(2));
            } else {
                var animateNodeX1       =                 childNodes .find(function(x) { return x.getAttribute('attributeName') === 'x1'; });
                var animateNodeY1       =                 childNodes .find(function(x) { return x.getAttribute('attributeName') === 'y1'; });
                var animateNodeX2       =                 childNodes .find(function(x) { return x.getAttribute('attributeName') === 'x2'; });
                var animateNodeY2       =                 childNodes .find(function(x) { return x.getAttribute('attributeName') === 'y2'; });
                var animateNodeRunColor = [...stopNodeRun.childNodes].find(function(x) { return x.getAttribute('attributeName') === 'stop-color'; });
                var animateNodeEndColor = [...stopNodeEnd.childNodes].find(function(x) { return x.getAttribute('attributeName') === 'stop-color'; });

                // shape coordinates
                addAttributeValue(animateNodeX1      , 'values', round(start.x, dpPnt));
                addAttributeValue(animateNodeY1      , 'values', round(start.y, dpPnt));
                addAttributeValue(animateNodeX2      , 'values', round(end.x  , dpPnt));
                addAttributeValue(animateNodeY2      , 'values', round(end.y  , dpPnt));
                addAttributeValue(animateNodeRunColor, 'values', '#' + startClr.asHexString.substr(2));
                addAttributeValue(animateNodeEndColor, 'values', '#' +   endClr.asHexString.substr(2));

                if (useKeyTimes) { // time stamps
                    addAttributeValue(animateNodeX1      , 'keyTimes', round(keyTime, dpTm));
                    addAttributeValue(animateNodeY1      , 'keyTimes', round(keyTime, dpTm));
                    addAttributeValue(animateNodeX2      , 'keyTimes', round(keyTime, dpTm));
                    addAttributeValue(animateNodeY2      , 'keyTimes', round(keyTime, dpTm));
                    addAttributeValue(animateNodeRunColor, 'keyTimes', round(keyTime, dpTm));
                    addAttributeValue(animateNodeEndColor, 'keyTimes', round(keyTime, dpTm));
                }
            }
            //g.setFill(new LinearGradient(start.getX(), start.getY(),
            //                             end  .getX(), end  .getY(),
            //                             false,
            //                             CycleMethod.NO_CYCLE,
            //                             new Stop[] {
            //                                 new Stop(0, startClr),
            //                                 new Stop(1, endClr)
            //                             }));
        };

        var drawPolygon = function(/*string*/ name, /*int*/ i, /*Point[]*/ ...polygon) {
            var pathNode = cnxt[name][i];

            var pathD = 'M' + polygon.map(function(p) { return round(p.x, dpPnt) + ' ' + round(p.y, dpPnt); }).join(' L')  + ' Z';
            if (!animated) {
                pathNode.setAttributeNS(null, 'd', pathD);
            } else {
                var animateNodeD = [...pathNode.childNodes].find(function(x) { return x.getAttribute('attributeName') === 'd'; });
                addAttributeValue(animateNodeD, 'values', pathD); // shape coordinates

                if (useKeyTimes)
                    addAttributeValue(animateNodeD, 'keyTimes', round(keyTime, dpTm)); // time stamps
            }

            return pathNode;
        };

        // paint owner gradient rays
        for (var i=0; i<8; i++) {
            var shape = drawPolygon('ownRays', i, rays[i], oct[i], inn[i], oct[(i+5)%8]);
            if (!useGradient) {
                setFill(shape, hsvPalette[i].toColor().darker());
            } else {
                // emulate triangle gradient (see BmpLogo.cpp C++ source code)
                // over linear gragients

                setGradientFill('ownRays', i, rays[i], palette[(i+1)%8], inn[i], palette[(i+6)%8]);

                var p1 = oct[i];
                var p2 = oct[(i+5)%8];
                var p = new Point((p1.x+p2.x)/2, (p1.y+p2.y)/2); // середина линии oct[i]-oct[(i+5)%8]. По факту - пересечение линий rays[i]-inn[i] и oct[i]-oct[(i+5)%8]

                var clr = Color.Transparent; // new Color(0); //
                if (true) {
                    // var hsv_h = 1 * window.document.getElementById('hsv_h').value;
                    // var hsv_s = 1 * window.document.getElementById('hsv_s').value;
                    // var hsv_v = 1 * window.document.getElementById('hsv_v').value;
                    switch (0) {
                    case 0: {
                           var c1 = hsvPalette[(i+1)%8];
                           var c2 = hsvPalette[(i+6)%8];
                           var diff = c1.h - c2.h;
                           var cP = new HSV(c1.toColor());
                           cP.h += diff/2; // цвет в точке p (пересечений линий...)
                           cP.a = 0;
                           // cP.h += hsv_h;
                           // cP.s = (hsv_s + cP.s) % 255;
                           // cP.v = (hsv_v + cP.v) % 255;
                           clr = cP.toColor();
                        }
                        break;
                    case 1: {
                           var c1 = hsvPalette[(i+1)%8];
                           var c2 = hsvPalette[(i+6)%8];
                           var diff = c1.h - c2.h;

                           var xDeltaInnRays = (inn[i].x + rays[i].x)/2;
                           var yDeltaInnRays = (inn[i].y + rays[i].y)/2;
                           var lenInnRays = Math.sqrt(xDeltaInnRays*xDeltaInnRays + yDeltaInnRays*yDeltaInnRays);

                           var xDeltaInnP = (inn[i].x + p.x)/2;
                           var yDeltaInnP = (inn[i].y + p.y)/2;
                           var lenInnP = Math.sqrt(xDeltaInnP*xDeltaInnP + yDeltaInnP*yDeltaInnP);

                           //   lenInnRays/100%  ==  lenInnP/???%
                           var z = lenInnP/lenInnRays; // 0..1
                           var cZ = new HSV(hsvPalette[(i+6)%8].toColor());
                           cZ.h += diff*z;
                           cZ.a = 0;
                           // cZ.h += hsv_h;
                           // cZ.s = (hsv_s + cZ.s) % 255;
                           // cZ.v = (hsv_v + cZ.v) % 255;

                           clr = cZ.toColor();
                        }
                        break;
                    case 2: {
                            clr = palette[(i+6)%8];
                            clr = new Color(0, clr.r, clr.g, clr.b);
                        }
                        break;
                    }
                }

                drawPolygon('ownRays1', i, rays[i], oct[i], inn[i]);
                setGradientFill('ownRays1', i, oct[i], palette[(i+3)%8], p, clr);

                drawPolygon('ownRays2', i, rays[i], oct[(i+5)%8], inn[i]);
                setGradientFill('ownRays2', i, oct[(i+5)%8], palette[(i+0)%8], p, clr);
            }
        }

        // paint star perimeter
        if (borderWidth > 0) {
            for (var i=0; i<8; i++) {
                var p1 = rays[(i + 7)%8];
                var p2 = rays[i];
                var lineNode = cnxt.perim[i];
                // g.setStroke(palette[i].darker());
                // g.strokeLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
                var strokeColor = '#' + palette[i].darker().asHexString.substr(2);
                if (!animated) {
                    lineNode.setAttributeNS(null, 'stroke', strokeColor);
                    lineNode.setAttributeNS(null, 'x1', p1.x);
                    lineNode.setAttributeNS(null, 'y1', p1.y);
                    lineNode.setAttributeNS(null, 'x2', p2.x);
                    lineNode.setAttributeNS(null, 'y2', p2.y);
                } else {
                    var childNodes = [...lineNode.childNodes];
                    var animateNodeStroke = childNodes.find(function(x) { return x.getAttribute('attributeName') === 'stroke'; });
                    var animateNodeX1     = childNodes.find(function(x) { return x.getAttribute('attributeName') === 'x1'; });
                    var animateNodeY1     = childNodes.find(function(x) { return x.getAttribute('attributeName') === 'y1'; });
                    var animateNodeX2     = childNodes.find(function(x) { return x.getAttribute('attributeName') === 'x2'; });
                    var animateNodeY2     = childNodes.find(function(x) { return x.getAttribute('attributeName') === 'y2'; });
                    addAttributeValue(animateNodeStroke, 'values', strokeColor);
                    addAttributeValue(animateNodeX1    , 'values', p1.x);
                    addAttributeValue(animateNodeY1    , 'values', p1.y);
                    addAttributeValue(animateNodeX2    , 'values', p2.x);
                    addAttributeValue(animateNodeY2    , 'values', p2.y);
                    if (useKeyTimes) {
                        // time stamps
                        addAttributeValue(animateNodeStroke, 'keyTimes', round(keyTime, dpTm));
                        addAttributeValue(animateNodeX1    , 'keyTimes', round(keyTime, dpTm));
                        addAttributeValue(animateNodeY1    , 'keyTimes', round(keyTime, dpTm));
                        addAttributeValue(animateNodeX2    , 'keyTimes', round(keyTime, dpTm));
                        addAttributeValue(animateNodeY2    , 'keyTimes', round(keyTime, dpTm));
                    }
                }
            }
        }

        // paint inner gradient triangles
        for (var i=0; i<8; i++) {
            var shape = drawPolygon('innerTr', i, inn[(i + 0)%8], inn[(i + 3)%8], center);
            if (useGradient) {
                var p1 = inn[(i+0)%8];
                var p2 = inn[(i+3)%8];
                var p = new Point((p1.x+p2.x)/2, (p1.y+p2.y)/2); // center line of p1-p2
                setGradientFill('innerTr', i,
                                p, palette[(i+6)%8],
                                center, ((i & 1) == 1) ? Color.Black : Color.White);
            } else {
                setFill(shape, ((i & 1) == 1)
                                    ? hsvPalette[(i + 6)%8].toColor().brighter()
                                    : hsvPalette[(i + 6)%8].toColor().darker());
            }
        }
   }

}

/** Logo image view implementation over SVG */
class LogoSvg extends Logo {

    /** @param skill - may be null. if Null - representable image of ESkillLevel.class */
    constructor(/* own SVG */ svg, /* SVG document */ svgDoc) {
        super();
        this._drawContext = null;
        this._svg    = svg;
        this._svgDoc = svgDoc;
    }

    createImage() {
        return this._drawContext = {
            // nodes
            background: null, // node example: <rect ...
            ownRays   : null, // nodes examples: <path ...
            ownRays1  : null,
            ownRays2  : null,
            perim     : null, // star lines perimeter
            innerTr   : null, // inner triangles
            gradient: {
                ownRays : null,
                ownRays1: null,
                ownRays2: null,
                innerTr : null
            }
        };
    }

    drawBody() {
        this.drawIntoContext(this._drawContext, this._svg, this._svgDoc);
    }


    getSvgContent() { return getSvgContent(this._svg); }

}

/** Logo image controller implementation for {@link LogoSvg} */
class LogoControllerSvg extends LogoController {

    constructor(/* own SVG */ svg, /* SVG document */ svgDoc) { super(new LogoSvg(svg, svgDoc)); }

    getFileName() {
        var m = this.Model;
        var res = 'Logo';
        res += '.' + m.size.width + 'x' + m.size.height;
        if (m.animated) {
            res += '.' + m.totalFrames + 'frames';
            res += '.' + round(m.animatePeriod/1000, 2) + 'sec';
            res += '.' + (m.animeDirection ? 'clockwise' : 'counterclockwise');
        }
        return res;
    }

}
