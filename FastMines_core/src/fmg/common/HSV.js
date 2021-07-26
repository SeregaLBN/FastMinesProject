// include after Common.js

/**
 * Сylindrical-coordinate representations of points in an RGB color model
 * <p>
 *  HSV - Hue, Saturation, Value (тон, насыщенность, значение) <br>
 *    it also <br>
 *  HSB - Hue, Saturation, Brightness (тон, насыщенность, яркость)
 * </p>
 *
 * White: h=any; s=0; v=100 <br>
 * Black: h=any; s=any; v=0
 **/
class HSV {

//    /** Hue — цветовой тон (цвет/оттенок): <ul> 0°..360°
//     * <li> 0° - red
//     * <li> 60° - yellow
//     * <li> 120° - green
//     * <li> 180° - aqua
//     * <li> 240° - blue
//     * <li> 300° - fuchsia
//     * <li> 360° - red
//     **/
//    h: 0

//    /** Saturation — насыщенность: 0%..100% (white(gray)..color) */
//    s: 100,

//    /** Value or Brightness — значение цвета или яркость: 0%..100% (black..color) */
//    v: 100,

//    /** Alpha chanel: 0..255 */
//    a: 0xFF,

    constructor0() {
        this.h = 0;
        this.s = 100;
        this.v = 100;
        this.a = 0xFF;
    }
    constructor3(/*int*/ h, /*int*/ s, /*int*/ v) {
        this.h = h;
        this.s = s;
        this.v = v;
        this.a = 0xFF;

        this.fix();
    }
    constructor4(/*int*/ h, /*int*/ s, /*int*/ v, /*int*/ a) {
        this.h = h;
        this.s = s;
        this.v = v;
        this.a = Color.checkA(a);

        this.fix();
    }
    constructor1(/*Color*/ clr) {
        this.a = clr.a;
        this.fromColorDouble(clr.r, clr.g, clr.b);
    }
    constructor(args/*, ...*/) {
        switch (arguments.length) {
        case 0: return this.constructor0();
        case 1: return this.constructor1(arguments[0]);
        case 3: return this.constructor3(arguments[0], arguments[1], arguments[2]);
        case 4: return this.constructor4(arguments[0], arguments[1], arguments[2], arguments[3]);
        }
        throw new Error();
    }

    addHue(/*double*/ addonH) {
        this.h += addonH;
        this.fix();
        return this;
    }

    fromColorDouble(/*double*/ r, /*double*/ g, /*double*/ b) {
        var max = Math.max(Math.max(r, g), b);
        var min = Math.min(Math.min(r, g), b);

        { // calc H
            if (hasMinDiff(max, min))
                this.h = 0;
            else if (hasMinDiff(max, r))
                this.h = 60 * (g - b) / (max - min) + ((g < b) ? 360 : 0);
            else if (hasMinDiff(max, g))
                this.h = 60 * (b - r) / (max - min) + 120;
            else if (hasMinDiff(max, b))
                this.h = 60 * (r - g) / (max - min) + 240;
            else
                throw new Error(';(');
        }
        this.s = hasMinDiff(max, 0) ? 0 : 100*(1 - min/max);
        this.v = max*100/255;

        this.fix();
    }

    toColor() {
        var rgb = this.toColorDouble();
        var r = rgb[0];
        var g = rgb[1];
        var b = rgb[2];
        return new Color(this.a, toInt(r*255/100), toInt(g*255/100), toInt(b*255/100));
    }

    toColorDouble() {
        this.fix();

        var vMin = (100 - this.s) * this.v / 100;
        var delta = (this.v - vMin) * ((this.h % 60) / 60.0);
        var vInc = vMin + delta;
        var vDec = this.v - delta;

        var r, g, b;
        switch (toInt(this.h / 60) % 6) {
        case 0:
            r = this.v; g = vInc; b = vMin;
            break;
        case 1:
            r = vDec; g = this.v; b = vMin;
            break;
        case 2:
            r = vMin; g = this.v; b = vInc;
            break;
        case 3:
            r = vMin; g = vDec; b = this.v;
            break;
        case 4:
            r = vInc; g = vMin; b = this.v;
            break;
        case 5:
            r = this.v; g = vMin; b = vDec;
            break;
        default:
            throw new Error(':( to Color double');
        }
        return [ r, g, b ];
    }

    /** Update HSV to grayscale */
    grayscale() {
        var rgb = this.toColorDouble();
        var r = rgb[0] * 0.2126;
        var g = rgb[1] * 0.7152;
        var b = rgb[2] * 0.0722;
        this.fromColorDouble(r, g, b);
    }

    fix() {
        if (this.h < 0) {
            this.h %= 360;
            this.h += 360;
        } else {
            if (this.h >= 360)
                this.h %= 360;
        }

        if (this.s < 0) {
            this.s = 0;
        } else {
            if (this.s > 100)
                this.s = 100;
        }

        if (this.v < 0) {
            this.v = 0;
        } else {
            if (this.v >= 100)
                this.v = 100;
        }

        if (this.a < 0) {
            this.a = 0;
        } else {
            if (this.a > 255)
                this.a = 255;
        }
    }

    toString() {
        //return 'HSV[h=' + h + ', s=' + s + '%, v=' + v + '%, a=' + a + ']';
        return (this.a == 255)
            ? 'HSV[h=' + round(this.h, dpClr) + ', s=' + round(this.s, dpClr) + '%, v=' + round(this.v, dpClr) + '%]'
            : 'HSV[h=' + round(this.h, dpClr) + ', s=' + round(this.s, dpClr) + '%, v=' + round(this.v, dpClr) + '%, a=' + this.a + ']';
    }
}
