const FigureHelper = {
    toRadian: function(/*double*/ degreesAngle) {
        return (degreesAngle * Math.PI) / 180; // to radians
    },
    toDegrees: function(/*double*/ radianAngle) {
        return radianAngle * 180 / Math.PI;
    },
    getPointOnCircleRadian: function(/*double*/ radius, /*double*/ radAngle, /*Point{x,y}*/ center) {
        return { x: radius * Math.sin(radAngle) + center.x, y: -radius * Math.cos(radAngle) + center.y };
    },
    getPointOnCircle(/*double*/ radius, /*double*/ degreesAngle, /*Point*/ center) {
        return FigureHelper.getPointOnCircleRadian(radius, FigureHelper.toRadian(degreesAngle), center);
    },
    getRegularPolygonCoords: function(/*int*/ n, /*double*/ radius, /*Point{x,y}*/ center, /*double*/ offsetAngle) {
        var angle = (2 * Math.PI)/n; // 360° / n
        var offsetAngle2 = FigureHelper.toRadian(offsetAngle);
        var polygon = [];
        for (var i=0; i<n; ++i) {
            var a = (i * angle) + offsetAngle2;
            polygon[i] = FigureHelper.getPointOnCircleRadian(radius, a, center);
        }
        return polygon;
    },
    getRegularStarCoords: function(/*int*/ rays, /*double*/ radiusOut, /*double*/ radiusIn, /*Point*/ center, /*double*/ offsetAngle) {
        var pointsExternal = FigureHelper.getRegularPolygonCoords(rays, radiusOut, center, offsetAngle);
        var pointsInternal = FigureHelper.getRegularPolygonCoords(rays, radiusIn, center, offsetAngle + (180.0/rays));
        var polygon = [];
        for (var i=0; i<rays; ++i) {
            polygon[i*2+0] = pointsExternal[i];
            polygon[i*2+1] = pointsInternal[i];
        }
        return polygon;
    },

    /** rotate around the center coordinates. !!Modify existed collection!!
     * @param coords coordinates for transformation
     * @param angle angle of rotation: -360° .. 0° .. +360°
     * @param center центр фигуры
     */
    rotateCollection(/*TCollection*/ coords, /*double*/ angle, /*Point*/ center) {
        angle = FigureHelper.toRadian(angle);
        var cos = Math.cos(angle);
        var sin = Math.sin(angle);
        coords.forEach(function(p) {
            p.x -= center.x;
            p.y -= center.y;
            var x = (p.x * cos) - (p.y * sin);
            var y = (p.x * sin) + (p.y * cos);
            p.x = x + center.x;
            p.y = y + center.y;
        });
        return coords;
    }

};
