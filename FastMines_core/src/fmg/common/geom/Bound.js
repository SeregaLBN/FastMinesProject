// include after Common.js

class Bound {

    constructor1(/* bound or double */ arg) {
        if (isNumeric(arg)) {
            var bound = arg;
            this.left = this.top = this.right = this.bottom = bound;
        } else {
            var copy = arg;
            this.left = copy.left; this.top = copy.top; this.right = copy.right; this.bottom = copy.bottom;
        }
    }
    constructor4(/*double*/ left, /*double*/ top, /*double*/ right, /*double*/ bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }
    constructor(args/*, ...*/) {
        switch (arguments.length) {
        case 1: return this.constructor1(arguments[0]);
        case 4: return this.constructor4(arguments[0], arguments[1], arguments[2], arguments[3]);
        }
        throw new Error();
    }

    get leftAndRight() { return this.left + this.right; }
    get topAndBottom() { return this.top + this.bottom; }

    toString() {
        return '{left: '   + round(this.left  , dpPnt)
            + ', right: '  + round(this.right , dpPnt)
            + ', top: '    + round(this.top   , dpPnt)
            + ', bottom: ' + round(this.bottom, dpPnt)
            + '}';
    }

}
