/** MVC: model of representable menu as horizontal or vertical lines */
class BurgerMenuModel {

    /**
     * @param generalModel another basic model
     */
    constructor(/*AnimatedImageModel*/ generalModel) {
        this._generalModel = generalModel;
        this._show = true;
        this._horizontal = true;
        this._layers = 3;
        this._padding;
        // _generalModelListener = event -> {
        //    assert event.getSource() == _generalModel; // by reference
        //    if (IImageModel.PROPERTY_SIZE.equals(event.getPropertyName()))
        //       recalcPadding((SizeDouble)event.getOldValue());
        // };
    }

    /** image width and height in pixel */
    get size() { return this._generalModel.size; }
    set size(value) { this._generalModel.size = value; }

    get show() { return this._show; }
    set show(/*boolean*/ value) { this._show = value; }

    get horizontal() { return this._horizontal; }
    set horizontal(/*boolean*/ value) { this._horizontal = value; }

    get layers() { return this._layers; }
    set layers(/*int*/ value) { this._layers = value; }

    /** inside padding */
    get padding() {
        if (this._padding == null)
            this.recalcPadding(null);
        return this._padding;
    }
    set padding(/*Bound*/ value) {
        if (value.leftAndRight >= this.size.width)
            throw new Error('IllegalArgumentException: Padding size is very large. Should be less than Width.');
        if (value.topAndBottom >= this.size.height)
            throw new Error('IllegalArgumentException: Padding size is very large. Should be less than Height.');
        var paddingNew = new Bound(value.left, value.top, value.right, value.bottom);
        this._padding = paddingNew;
    }
    recalcPadding(/*Size*/ old) {
        var size = this.size;
        var paddingNew = (this._padding == null)
                ? new Bound(size.width / 2,
                            size.height / 2,
                            this._generalModel.padding.right,
                            this._generalModel.padding.bottom)
                : AnimatedImageModel.recalcPadding(this._padding, size, old);
        this._padding = paddingNew;
    }

    //public static class LineInfo {
    //    public Color clr;
    //    public double penWidht;
    //    public PointDouble from; // start coord
    //    public PointDouble to;   // end   coord
    //}

    /** get paint information of drawing burger menu model image */
    getCoords() {
        if (!this.show)
            return [];

        var horizontal = this.horizontal;
        var layers = this.layers;
        var pad = this.padding;
        var rc = new Rect(pad.left,
                          pad.top,
                          this.size.width  - pad.leftAndRight,
                          this.size.height - pad.topAndBottom);
        var penWidth = Math.max(1, (horizontal ? rc.height : rc.width) / (2.0 * layers));
        var rotateAngle = this._generalModel.rotateAngle;
        var stepAngle = 360.0 / layers;

        var res = [];
        for (var layerNum=0; layerNum<layers; ++layerNum) {
            var layerAlignmentAngle = AnimatedImageModel.fixAngle(layerNum*stepAngle + rotateAngle);
            var offsetTop  = !horizontal ? 0 : layerAlignmentAngle*rc.height/360;
            var offsetLeft =  horizontal ? 0 : layerAlignmentAngle*rc.width /360;
            var start = new Point(rc.left + offsetLeft,
                                  rc.top  + offsetTop);
            var end   = new Point((horizontal ? rc.right : rc.left) + offsetLeft,
                                  (horizontal ? rc.top : rc.bottom) + offsetTop);

            var hsv = new HSV(Color.Gray);
            hsv.v *= Math.sin(layerNum*stepAngle / layers);

            var li = { // new LineInfo();
                clr: hsv.toColor(),
                penWidht: penWidth,
                from: start,
                to: end
            };
            res.push(li);
        }
        return res;
    }

}
