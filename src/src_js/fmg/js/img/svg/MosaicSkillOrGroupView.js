/**
 * MVC: view. Abstract SVG representable {@link fmg.core.types.ESkillLevel} or {@link fmg.core.types.EMosaicGroup} as image
 * @param <TImage> plaform specific view/image/picture or other display context/canvas/window/panel
 * @param <TImageModel> {@link MosaicsSkillModel} or {@link MosaicsGroupModel}
 */
class MosaicSkillOrGroupView extends WithBurgerMenuView {

    static get svgNS() { return 'http://www.w3.org/2000/svg'; }

    constructor(/*TImageModel*/ imageModel) {
        super(imageModel);
    }

    /** get paint information of drawing basic image model */
    getCoords() { throw new Error('Abstract method'); }

    drawIntoContext(/* DrawContext */ cnxt, /* own SVG */ svg, /* SVG document */ svgDoc) {
        var m = this.Model;
        var keyTime = m.currentFrame / m.totalFrames; // 0..1;
        var duration  = m.animatePeriod;
        var borderWidth = m.borderWidth;
        var borderColor = m.borderColor;
        var backgroundColor = m.backgroundColor;
        var foregroundColor = m.foregroundColor;
        var skill = m.mosaicSkill;
        var size = m.size;
        var animated = m.animated;

        if (!cnxt.background && !backgroundColor.isTransparent) { // fill background
            // example: <rect x='0' y='0' width='100' height='100' fill='hsla(112.71, 50.95%, 82.35%, 1)' />
            // один раз (самий перший за всі малювання) створюю ноду для фонової заливки
            var rcNode = cnxt.background = svgDoc.createElementNS(MosaicSkillOrGroupView.svgNS, 'rect');
            rcNode.setAttributeNS(null, 'x'     , 0);
            rcNode.setAttributeNS(null, 'y'     , 0);
            rcNode.setAttributeNS(null, 'width' , size.width);
            rcNode.setAttributeNS(null, 'height', size.height);
            var bc = new HSV(backgroundColor);
            rcNode.setAttributeNS(null, 'fill'  , 'hsla(' + round(bc.h, dpClr) + ', ' + round(bc.s, dpClr) + '%, ' + round(bc.v, dpClr) + '%, ' + round(bc.a/255.0, dpClr) + ')');
            svg.appendChild(rcNode);
        }

        var shapes = this.getCoords();

        const useKeyTimes = false;

        if (!cnxt.shapes) {
            // один раз (самий перший за всі малювання) створюю ноди для фігур з піднодами анімацій
            cnxt.shapes = []; // examples: <path stroke='#400000' stroke-width='0.7' fill-opacity='0.92'> .... </path>
            shapes.forEach(function(_) {
                var pathNode = svgDoc.createElementNS(MosaicSkillOrGroupView.svgNS, 'path');
                pathNode.setAttributeNS(null, 'stroke'        , '#' + borderColor.asHexString.substr(2));
                pathNode.setAttributeNS(null, 'stroke-width'  , borderWidth);
            //pathNode.setAttributeNS(null, 'stroke-opacity', round(borderColor.a/255.0, dpClr));
                pathNode.setAttributeNS(null, 'fill-opacity'  , round(foregroundColor.a/255.0, dpClr));

                if (animated) {
                    // examples: <animate attributeName='d' dur='2000ms' repeatCount='indefinite' calcMode='linear' ... />
                    pathNode.appendChild(createAnimateNode(svgDoc, 'd', duration, useKeyTimes ? null : (skill==null) ? 'discrete' : 'linear'));

                    // examples: <animate attributeName='fill' dur='2000ms' repeatCount='indefinite' calcMode='linear' ...>
                    pathNode.appendChild(createAnimateNode(svgDoc, 'fill', duration, useKeyTimes ? null : (skill==null) ? 'discrete' : 'linear'));
                }

                svg.appendChild(pathNode);
                cnxt.shapes.push(pathNode);
            });
        }

        // set values of shapes - coordinates and colors
        for (var i=0; i<shapes.length; ++i) {
            var pathNode = cnxt.shapes[i];
            var polygon = shapes[i].polygon;
            var color = shapes[i].color;
            var pathD = 'M' + polygon.map(function(p) { return round(p.x, dpPnt) + ' ' + round(p.y, dpPnt); }).join(' L')  + ' Z';
            var pathFillColor = '#' + color.asHexString.substr(2);
            if (!animated) {
                pathNode.setAttributeNS(null, 'd', pathD);
                pathNode.setAttributeNS(null, 'fill', pathFillColor);
            } else {
                var childNodes = [...pathNode.childNodes];
                var animateNodeD    = childNodes.find(function(x) { return x.getAttribute('attributeName') === 'd'; });
                var animateNodeFill = childNodes.find(function(x) { return x.getAttribute('attributeName') === 'fill'; });

                addAttributeValue(animateNodeD   , 'values', pathD); // shape coordinates
                addAttributeValue(animateNodeFill, 'values', pathFillColor); // shape colors

                // time stamps
                if (useKeyTimes) {
                    addAttributeValue(animateNodeD   , 'keyTimes', round(keyTime, dpTm));
                    addAttributeValue(animateNodeFill, 'keyTimes', round(keyTime, dpTm));
                }
            }
        }

        var menuShapes = this.burgerMenuModel.getCoords()
        if (menuShapes.length && !cnxt.menus) { // fill background
            cnxt.menus = [];
            menuShapes.forEach(function(li) {
                var pathNode = svgDoc.createElementNS(MosaicSkillOrGroupView.svgNS, 'path');
                pathNode.setAttributeNS(null, 'stroke'        , '#' + li.clr.asHexString.substr(2));
                pathNode.setAttributeNS(null, 'stroke-width'  , li.penWidht);
                //pathNode.setAttributeNS(null, 'stroke-opacity', round(li.clr.a/255.0, dpClr));
                //pathNode.setAttributeNS(null, 'fill-opacity'  , round(li.clr.a/255.0, dpClr));

                if (animated) {
                    // examples: <animate attributeName='d' dur='2000ms' repeatCount='indefinite' calcMode='discrete' ... />
                    pathNode.appendChild(createAnimateNode(svgDoc, 'd', duration, useKeyTimes ? null : 'discrete'));
                }

                svg.appendChild(pathNode);
                cnxt.menus.push(pathNode);
            });
        }
        // set values of shapes - coordinates and colors
        for (var i=0; i<menuShapes.length; ++i) {
            var pathNode = cnxt.menus[i];
            var from = menuShapes[i].from;
            var to   = menuShapes[i].to;
            var pathD = 'M ' + round(from.x, dpPnt) + ' ' + round(from.y, dpPnt) + ' L ' + round(to.x, dpPnt) + ' ' + round(to.y, dpPnt);
            if (!animated) {
                pathNode.setAttributeNS(null, 'd', pathD);
            } else {
                var animateNodeD = [...pathNode.childNodes].find(function(x) { return x.getAttribute('attributeName') === 'd'; });
                addAttributeValue(animateNodeD, 'values', pathD); // shape coordinates
                if (useKeyTimes)
                    addAttributeValue(animateNodeD, 'keyTimes', round(keyTime, dpTm)); // time stamps
            }
        }

    }

}
