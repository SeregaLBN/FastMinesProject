// include after Common.js

class Size {

    constructorWH(w, h) {
        this.width  = w;
        this.height = h;
    }
    constructorSize(sizeFrom) {
        this.width  = sizeFrom.width;
        this.height = sizeFrom.height;
    }
    constructor(args/*, ...*/) {
        switch (arguments.length) {
        case 0: return this.constructorWH(0, 0);
        case 1: return this.constructorSize(arguments[0]);
        case 2: return this.constructorWH(arguments[0], arguments[1]);
        }
        throw new Error();
    }

    toString() {
        return '{w: ' + round(this.width , dpPnt)
            + ', h: ' + round(this.height, dpPnt)
            + '}';
    }

}
