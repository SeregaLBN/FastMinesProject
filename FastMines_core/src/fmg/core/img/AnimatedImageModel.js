/** MVC: model. Common animated image characteristics. */
class AnimatedImageModel {

    constructor() {
        var defaultImageSize = 100;
        /** width and height in pixel */
        this._size = new Size(defaultImageSize, defaultImageSize);

        var defaultPadding = defaultImageSize * 0.05; // 5%
        /** inside padding. Автоматически пропорционально регулирую при измениях размеров */
        this._padding = new Bound(defaultPadding);
        this._foregroundColor = Color.Orchid;
        /** background fill color */
        this._backgroundColor = Color.DarkOrange;
        this._borderColor = Color.Maroon.darker(0.5);
        this._borderWidth = 3;
        /** 0° .. +360° */
        this._rotateAngle = 0;
        /** animation of polar lights */
        this._polarLights = true;
        /** animation direction (example: clockwise or counterclockwise for simple rotation) */
        this._animeDirection = true;
        this._innerModel = new AnimatedInnerModel();
    }

    get size() { return this._size; }
    set size(value) {
        var old = this._size;
        if (isNumeric(value))
            this._size = new Size(value, value);
        else
            this._size = new Size(value);
        this.recalcPadding(old);
    }

    /** inside padding */
    get padding() { return this._padding; }
    set padding(/*double or Bound*/ value) {
        if (isNumeric(value))
            value = new Bound(value);
        if (value.leftAndRight >= this.size.width)
            throw new Error('IllegalArgumentException: Padding size is very large. Should be less than Width.');
        if (value.topAndBottom >= this.size.height)
            throw new Error('IllegalArgumentException: Padding size is very large. Should be less than Height.');
        var paddingNew = new Bound(value.left, value.top, value.right, value.bottom);
        this._padding = paddingNew;
    }
    static recalcPadding(/*Bound*/ padding, /*Size*/ current, /*Size*/ old) {
        return new Bound(padding.left   * current.width  / old.width,
                         padding.top    * current.height / old.height,
                         padding.right  * current.width  / old.width,
                         padding.bottom * current.height / old.height);
    }
    recalcPadding(/*Size*/ old) {
        var paddingNew = ImageModel.recalcPadding(_padding, _size, old);
        this._padding = paddingNew;
    }

    get foregroundColor() { return this._foregroundColor; }
    set foregroundColor(/*Color*/ value) {
        this._foregroundColor = value;
    }

    /** background fill color */
    get backgroundColor() { return this._backgroundColor; }
    set backgroundColor(/*Color*/ value) {
        this._backgroundColor = value;
    }

    get borderColor() { return this._borderColor; }
    set borderColor(/*Color*/ value) {
        this._borderColor = value;
    }

    get borderWidth() { return this._borderWidth; }
    set borderWidth(/*double*/ value) {
        this._borderWidth = value;
    }

    /** 0° .. +360° */
    get rotateAngle() { return this._rotateAngle; }
    set rotateAngle(/*double*/ value) {
        this._rotateAngle = AnimatedImageModel.fixAngle(value);
    }

    /** to diapason (0° .. +360°] */
    static /* double */ fixAngle(/* double */ value) {
        return (value >= 360)
            ?              (value % 360)
            :  (value < 0)
                ?          (value % 360) + 360
                :           value;
    }

    /** Image is animated? */
    get animated() { return this._innerModel.animated; }
    set animated(/*boolean*/ value) { this._innerModel.animated = value; }

    /** Overall animation period (in milliseconds) */
    get animatePeriod() { return this._innerModel.animatePeriod; }
    set animatePeriod(/*long*/ value) { this._innerModel.animatePeriod = value; }

    /** Total frames of the animated period */
    get totalFrames() { return this._innerModel.totalFrames; }
    set totalFrames(/*int*/ value) { this._innerModel.totalFrames = value; }

    get currentFrame() { return this._innerModel.currentFrame; }
    set currentFrame(/*int*/ value) { this._innerModel.currentFrame = value; }

    get polarLights() { return this._polarLights; }
    set polarLights(/*boolean*/ polarLights) { this._polarLights = polarLights; }

    get animeDirection() { return this._animeDirection; }
    set animeDirection(/*boolean*/ animeDirection) { this._animeDirection = animeDirection; }

}
