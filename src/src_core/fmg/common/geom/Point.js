// include after Common.js

class Point {

    constructorXY(x, y) {
        this.x = x;
        this.y = y;
    }
    constructorPoint(pointFrom) {
        this.x = pointFrom.x;
        this.y = pointFrom.y;
    }
    constructor(args/*, ...*/) {
        switch (arguments.length) {
        case 0: return this.constructorXY(0, 0);
        case 1: return this.constructorPoint(arguments[0]);
        case 2: return this.constructorXY(arguments[0], arguments[1]);
        }
        throw new Error();
    }

    toString() {
        return '{x: ' + round(this.x, dpPnt)
            + ', y: ' + round(this.y, dpPnt)
            + '}';
    }

}
