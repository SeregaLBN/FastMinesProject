// include after Common.js

class Rect {

    constructor(/* double */ x, /* double */ y, /* double */ w, /* double */ h) {
        this._x = x;
        this._y = y;
        this._width = w;
        this._height = h;
    }

    get left  () { return this._x; }
    get right () { return this._x + this._width; }
    get top   () { return this._y; }
    get bottom() { return this._y + this._height; }
    set left  (/* double */ v) { this._x      = v; }
    set right (/* double */ v) { this._width  = v - this._x; }
    set top   (/* double */ v) { this._y      = v; }
    set bottom(/* double */ v) { this._height = v - this._y; }

    get width () { return this._width; }
    set width (/* double */ w) { this._width  = w; }
    get height() { return this._height; }
    set height(/* double */ h) { this._height = h; }

    toString() {
        return '{x: ' + round(this._x, dpPnt)
            + ', y: ' + round(this._y, dpPnt)
            + ', w: ' + round(this._width, dpPnt)
            + ', h: ' + round(this._height, dpPnt)
            + '}';
    }

}
