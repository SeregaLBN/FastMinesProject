/** MVC: model for FastMines logo image */
class LogoModel extends AnimatedImageModel {

    static get /* enum */ ERotateMode() {
        return {
            /** rotate image */
            classic: { ordinal: 0 },
            /** rotate color Palette */
            color  : { ordinal: 1 },
            /** {@link #color} + {@link #classic} */
            combi  : { ordinal: 2 }
        };
    }


    constructor() {
        super();
        this._palette = [ new HSV(  0, 100, 100), new HSV( 45, 100, 100), new HSV( 90, 100, 100), new HSV(135, 100, 100),
                            new HSV(180, 100, 100), new HSV(225, 100, 100), new HSV(270, 100, 100), new HSV(315, 100, 100) ];
        this._useGradient = false;
        this._rotateMode = LogoModel.ERotateMode.combi;
        /** owner rays points */
        this._rays = [];
        /** inner octahedron */
        this._inn = [];
        /** central octahedron */
        this._oct = [];

        this._backgroundColor = Color.Transparent;
    }


    get palette() { return this._palette; }

    static toMineModel(/*LogoModel*/ m) {
        m.useGradient = false;
        m.palette.forEach(function(item) {
            item.grayscale();
        });

    }

    get useGradient() { return this._useGradient; }
    set useGradient(/*bool*/ value) { this._useGradient = value; }

    get rotateMode() { return this._rotateMode; }
    set rotateMode(/*ERotateMode*/ value) { this._rotateMode = value; }

    get zoomX() { return (this.size.width  - this.padding.leftAndRight) / 200.0; }
    get zoomY() { return (this.size.height - this.padding.topAndBottom) / 200.0; }

    get rays() {
        if (this._rays.length == 0) {
            var pl = this.padding.left;
            var pt = this.padding.top;
            var zx = this.zoomX;
            var zy = this.zoomY;

            this._rays.push(new Point(pl + 100.0000*zx, pt + 200.0000*zy));
            this._rays.push(new Point(pl + 170.7107*zx, pt +  29.2893*zy));
            this._rays.push(new Point(pl +   0.0000*zx, pt + 100.0000*zy));
            this._rays.push(new Point(pl + 170.7107*zx, pt + 170.7107*zy));
            this._rays.push(new Point(pl + 100.0000*zx, pt +   0.0000*zy));
            this._rays.push(new Point(pl +  29.2893*zx, pt + 170.7107*zy));
            this._rays.push(new Point(pl + 200.0000*zx, pt + 100.0000*zy));
            this._rays.push(new Point(pl +  29.2893*zx, pt +  29.2893*zy));
        }
        return this._rays;
    }

    get inn() {
        if (this._inn.length == 0) {
            var pl = this.padding.left;
            var pt = this.padding.top;
            var zx = this.zoomX;
            var zy = this.zoomY;

            this._inn.push(new Point(pl + 100.0346*zx, pt + 141.4070*zy));
            this._inn.push(new Point(pl + 129.3408*zx, pt +  70.7320*zy));
            this._inn.push(new Point(pl +  58.5800*zx, pt + 100.0000*zy));
            this._inn.push(new Point(pl + 129.2500*zx, pt + 129.2500*zy));
            this._inn.push(new Point(pl +  99.9011*zx, pt +  58.5377*zy));
            this._inn.push(new Point(pl +  70.7233*zx, pt + 129.3198*zy));
            this._inn.push(new Point(pl + 141.4167*zx, pt + 100.0000*zy));
            this._inn.push(new Point(pl +  70.7500*zx, pt +  70.7500*zy));
        }
        return this._inn;
    }

    get oct() {
        if (this._oct.length == 0) {
            var pl = this.padding.left;
            var pt = this.padding.top;
            var zx = this.zoomX;
            var zy = this.zoomY;

            this._oct.push(new Point(pl + 120.7053*zx, pt + 149.9897*zy));
            this._oct.push(new Point(pl + 120.7269*zx, pt +  50.0007*zy));
            this._oct.push(new Point(pl +  50.0034*zx, pt + 120.7137*zy));
            this._oct.push(new Point(pl + 150.0000*zx, pt + 120.6950*zy));
            this._oct.push(new Point(pl +  79.3120*zx, pt +  50.0007*zy));
            this._oct.push(new Point(pl +  79.2624*zx, pt + 149.9727*zy));
            this._oct.push(new Point(pl + 150.0000*zx, pt +  79.2737*zy));
            this._oct.push(new Point(pl +  50.0034*zx, pt +  79.3093*zy));
        }
        return this._oct;
    }

}
