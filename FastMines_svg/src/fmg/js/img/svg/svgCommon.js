function createAnimateNode(svgDoc, attributeName, duration, calcMode) {
    var animateNodeD = svgDoc.createElementNS(MosaicSkillOrGroupView.svgNS, 'animate');
    animateNodeD.setAttributeNS(null, 'attributeName', attributeName);
    animateNodeD.setAttributeNS(null, 'dur'          , duration + 'ms');
    animateNodeD.setAttributeNS(null, 'repeatCount'  , 'indefinite');
    if (calcMode)
        animateNodeD.setAttributeNS(null, 'calcMode'  , calcMode); // discrete linear paced spline
    return animateNodeD;
}

function addAttributeValue(node, attributeName, addonValue) {
    var addAttributeValue = node.getAttribute(attributeName);
    if (!addAttributeValue)
        addAttributeValue = addonValue;
    else
        addAttributeValue += ';' + addonValue;
    node.setAttributeNS(null, attributeName, addAttributeValue);
}

function getSvgContent(svg) {
    var clean = function() {
        svg.removeAttribute('onload');
        svg.removeAttribute('style');
        return svg;
    }

    var format = function(node, value) {
        var indentBefore = new Array(value++ + 1).join('  '),
            indentAfter  = new Array(value - 1).join('  '),
            textNode, childNode;
        for (var i = 0; i < node.children.length; i++) {
            textNode = document.createTextNode('\n' + indentBefore);
            childNode = node.children[i];
            node.insertBefore(textNode, childNode);
            format(childNode, value);
            if (node.lastElementChild == childNode) {
                textNode = document.createTextNode('\n' + indentAfter);
                node.appendChild(textNode);
            }
        }
        textNode = childNode = null;
        return node;
    }

    return format(clean(), 1).outerHTML;
}
