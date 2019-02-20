/** MVC model of {@link ESkillLevel} representable as image */
class MosaicSkillModel extends AnimatedImageModel {

    //private ESkillLevel _mosaicSkill;

    constructor(/*ESkillLevel*/ mosaicSkill) { super(); this._mosaicSkill = mosaicSkill; }

    get mosaicSkill() { return this._mosaicSkill; }
    set mosaicSkill(/*ESkillLevel*/ value) { this._mosaicSkill = value; }


    getCoords() {
        return (this.mosaicSkill == null)
                ? this.getCoords_SkillLevelAsType()
                : this.getCoords_SkillLevelAsValue();
    }

    getCoords_SkillLevelAsType() {
        const bigMaxStar = !true; // true - большая звезда - вне картинки; false - большая звезда - внутри картинки.
        const accelerateRevert = !true; // ускорение под конец анимации, иначе - в начале...

        var rays = 5;
        var stars = bigMaxStar ? 6 : 8;
        var angle = this.rotateAngle;

        var size = this.size;
        var pad = this.padding;
        var sqMax = Math.min( // размер квадрата куда будет вписана звезда при 0°
                            size.width  - pad.leftAndRight,
                            size.height - pad.topAndBottom);
        var sqMin = 1;//sqMax / (bigMaxStar ? 17 : 7); // размер квадрата куда будет вписана звезда при 360°
        var sqExt = sqMax * 3;

        var centerMax = new Point(pad.left + (size.width  - pad.leftAndRight) / 2.0,
                                  pad.top  + (size.height - pad.topAndBottom) / 2.0);
        var centerMin = new Point(pad.left + sqMin/2, pad.top + sqMin/2);
        var centerExt = new Point(size.width * 1.5, size.height * 1.5);

        return this.getCoords_SkillLevelAsType_2(true , bigMaxStar, accelerateRevert, rays, stars/2, angle, sqMin, sqMax, centerMin, centerMax)
            .concat(
                this.getCoords_SkillLevelAsType_2(false, bigMaxStar, accelerateRevert, rays, stars/2, angle, sqMax, sqExt, centerMax, centerExt));
        // return this.getCoords_SkillLevelAsType_2(false, bigMaxStar, accelerateRevert, rays, stars/2, angle, sqMin, sqMax, centerMin, centerMax); // old
    }
    getCoords_SkillLevelAsType_2(
        /* bool */ accumulative,
        /* bool */ bigMaxStar,
        /* bool */ accelerateRevert,
        /* int */ rays,
        /* int */ stars,
        /* double */ angle,
        /* double */ sqMin,
        /* double */ sqMax,
        /* Point */ centerMin,
        /* Point */ centerMax
    ) {
        var angleAccumulative = angle;
        var anglePart = 360.0 / stars;
        var sqDiff = sqMax - sqMin;
        var centerDiff = new Point(centerMax.x - centerMin.x, centerMax.y - centerMin.y);
        var fgClr = this.foregroundColor;
        var pl = this.polarLights;

        var res = []; // array of Pair<Color bkColor, Point[] polygonCoords >
        for (var starNum=0; starNum < stars; ++starNum) {
            var angleStar = AnimatedImageModel.fixAngle(angle + starNum * anglePart);
            if (accumulative)
                angleAccumulative = Math.sin(FigureHelper.toRadian(angle/4)) * angleAccumulative; // accelerate / ускоряшка..

            var sq = angleStar * sqDiff / 360;
            // (un)comment next line to view result changes...
            sq = Math.sin(FigureHelper.toRadian(angleStar/4))*sq; // accelerate / ускоряшка..
            sq = accelerateRevert
                    ? sqMin + sq
                    : sqMax - sq;

            var r1 = bigMaxStar ? sq*2.2 : sq/2; // external radius
            var r2 = r1/2.6; // internal radius

            var centerStar = new Point(angleStar * centerDiff.x / 360,
                                       angleStar * centerDiff.y / 360);
            // (un)comment next 2 lines to view result changes...
            centerStar.x = Math.sin(FigureHelper.toRadian(angleStar/4))*centerStar.x; // accelerate / ускоряшка..
            centerStar.y = Math.sin(FigureHelper.toRadian(angleStar/4))*centerStar.y; // accelerate / ускоряшка..
            centerStar.x = accelerateRevert
                    ? centerMin.x + centerStar.x
                    : centerMax.x - centerStar.x;
            centerStar.y = accelerateRevert
                    ? centerMin.y + centerStar.y
                    : centerMax.y - centerStar.y;

            var clr = !pl
                    ? fgClr
                    : new HSV(fgClr).addHue(+angleStar).toColor();// try: -angleStar

            res.push( { priority: sq,
                        clrAndPoly: {
                          color: clr,
                          polygon: FigureHelper.getRegularStarCoords(rays,
                                                                     r1, r2,
                                                                     bigMaxStar ? centerMax : centerStar,
                                                                     accumulative ? angleAccumulative : 0
                                                                   )} } );
        }
        var saveColorSequence = res.map(function(x) { return x.clrAndPoly.color; });
        res.sort(function(o1, o2) {
            if (o1.priority < o2.priority) return bigMaxStar ?  1 : -1;
            if (o1.priority > o2.priority) return bigMaxStar ? -1 :  1;
            return 0;
        });
        for (var i=0; i<res.length; ++i)
            res[i].clrAndPoly.color = saveColorSequence[i]; // restore color sequence; only for SVG
        return res.map(function(x) { return x.clrAndPoly; });
    }

    getCoords_SkillLevelAsValue() {
        var size = this.size;
        var pad = this.padding;
        var sq = Math.min( // size inner square
                          size.width  - pad.leftAndRight,
                          size.height - pad.topAndBottom);
        var r1 = sq/7; // external radius
        var r2 = sq/12; // internal radius

        var skill = this.mosaicSkill;
        var ordinal = skill.ordinal;
        var rays = 5 + ordinal; // rays count
        var stars = 4 + ordinal; // number of stars on the perimeter of the circle

        var angle = this.rotateAngle;
        var angleAccumulative = angle;
        var anglePart = 360.0/stars;

        var center = new Point(size.width / 2.0, size.height / 2.0);
        var zero = new Point(0, 0);
        var fgClr = this.foregroundColor;
        var pl = this.polarLights;
        var res = []; // array of Pair<Color bkColor, Point[] polygonCoords >
        for (var starNum=0; starNum < stars; ++starNum) {
            // (un)comment next line to view result changes...
            angleAccumulative = Math.sin(FigureHelper.toRadian(angleAccumulative/4))*angleAccumulative; // accelerate / ускоряшка..

            // adding offset
            var offset = FigureHelper.getPointOnCircle(sq / 3, angleAccumulative + starNum * anglePart, zero);
            var centerStar = new Point(center.x + offset.x, center.y + offset.y);

            var clr = !pl
                    ? fgClr
                    : new HSV(fgClr).addHue(starNum * anglePart).toColor();

            res.push({ color: clr,
                       polygon: (skill == ESkillLevel.eCustom)
                                    ? FigureHelper.getRegularPolygonCoords(3 + starNum % 4, r1, centerStar, -angleAccumulative)
                                    : FigureHelper.getRegularStarCoords(rays, r1, r2, centerStar, -angleAccumulative) });
        }
        res.reverse(); // reverse stars, to draw the first star of the latter. (pseudo Z-order). (un)comment line to view result changes...
        return res;
    }

}
