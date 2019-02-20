/** RGBA color */
class Color {

    static get Black      () { return new Color(0xFF000000); }
    static get White      () { return new Color(0xFFFFFFFF); }
    static get Transparent() { return new Color(0x00FFFFFF); }
    static get Orchid     () { return new Color(0xFFDA70D6); }
    static get DarkOrange () { return new Color(0xFFFF8C00); }
    static get Maroon     () { return new Color(0xFF800000); }
    static get Gray       () { return new Color(0xFF808080); }

    static /*int*/ check(/*int*/ v, /*String*/ name) {
        if (v<0 || v>255)
            throw new Error('Bad '+name+' argument');
        return v;
    }
    static /*int*/ checkA(/*int*/ a) { return Color.check(a, 'ALPHA'); }
    static /*int*/ checkR(/*int*/ r) { return Color.check(r, 'RED'); }
    static /*int*/ checkG(/*int*/ g) { return Color.check(g, 'GREEN'); }
    static /*int*/ checkB(/*int*/ b) { return Color.check(b, 'BLUE'); }

    constructor4(a, r, g, b) {
        this.a = Color.checkA(a);
        this.r = Color.checkR(r);
        this.g = Color.checkG(g);
        this.b = Color.checkB(b);
    }
    constructor3(r, g, b) {
        this.constructor4(255, r, g, b);
    }
    constructor1(OxAARRGGBB__or__colorCopy) {
        if (OxAARRGGBB__or__colorCopy instanceof Color) {
            var copy = OxAARRGGBB__or__colorCopy;
            this.constructor4(copy.a, copy.r, copy.g, copy.b);
            return;
        }
        var OxAARRGGBB = OxAARRGGBB__or__colorCopy;
        this.constructor4((OxAARRGGBB >> 24) & 0xFF,
                          (OxAARRGGBB >> 16) & 0xFF,
                          (OxAARRGGBB >>  8) & 0xFF,
                          (OxAARRGGBB >>  0) & 0xFF);
    }
    constructor(args/*, ...*/) {
        switch (arguments.length) {
        case 1: return this.constructor1(arguments[0]);
        case 3: return this.constructor3(arguments[0], arguments[1], arguments[2]);
        case 4: return this.constructor4(arguments[0], arguments[1], arguments[2], arguments[3]);
        }
        throw new Error();
    }

    equals(obj) {
        if (!(obj instanceof Color))
            return false;
        return (obj.a==this.a) && (obj.r==this.r) && (obj.g==this.g) && (obj.b==this.b);
    }

    toString() {
        return 'argb[' + this.asHexString + ']';
    }

    get asNumber() {
        return (this.a << 24) & 0xFF000000
            |  (this.r << 16) & 0x00FF0000
            |  (this.g <<  8) & 0x0000FF00
            |  (this.b <<  0) & 0x000000FF;
    }

    get asHexString() {
        var h = function(v) {
            var s = (+v).toString(16);
            if (s.length < 2) s = '0' + s;
            return s.toUpperCase();
        };
        return h(this.a) + h(this.r) + h(this.g) + h(this.b);
    }

    clone() { return new Color(this.a, this.r, this.g, this.b); }

    get isOpaque()      { return this.a == 255; }
    get isTransparent() { return this.a == 0; }

    static get RandomColor() {
        return new Color(random(256), random(256), random(256));
    }

    // Creates grayscale version of this Color
    grayscale() { return new Color(this.a, toInt(this.r * 0.2126), toInt(this.g * 0.7152), toInt(this.b * 0.0722)); }

    /**
     * Creates brighter version of this Color
     * @param percent - 0.0 - as is; 1 - WHITE
     * @return
     */
    brighter(/*double*/ percent) {
        if (arguments.length == 0)
            percent = 0.7;
        if ((percent < 0) || (percent > 1))
            throw new Error('Bad \'percent\' argument');
        var tmp = new Color(this.a, 0xFF - this.r, 0xFF - this.g, 0xFF - this.b);
        tmp = tmp.darker(percent);
        return new Color(tmp.a, 0xFF - tmp.r, 0xFF - tmp.g, 0xFF - tmp.b);
        //HSV hsv = new HSV(this);
        //hsv.s *= 1 - percent;
        //hsv.v = 100 - hsv.v * ( 1 - percent);
        //return hsv.toColor();
    }

    /**
     * Creates darker version of this Color
     * @param percent - 0.0 - as is; 1 - BLACK
     * @return
     */
    darker(/*double*/ percent) {
        if (arguments.length == 0)
            percent = 0.7;
        if ((percent < 0) || (percent > 1))
            throw new Error('Bad \'percent\' argument');
        var tmp = 1 - Math.min(1.0, Math.max(0, percent));
        return new Color(this.a,
            toInt(this.r * tmp),
            toInt(this.g * tmp),
            toInt(this.b * tmp));
        //HSV hsv = new HSV(this);
        //hsv.v *= 1 - percent;
        //return hsv.toColor();
    }

}
