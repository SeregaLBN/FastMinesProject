/** MVC: inner model. Animated image characteristics. */
class AnimatedInnerModel {

    constructor() {
        /** Image is animated? */
        this._animated = null;
        /** Overall animation period (in milliseconds) */
        this._animatePeriod = 3000;
        /** Total frames of the animated period */
        this._totalFrames = 30;
        this._currentFrame = 0;
    }

    /** Image is animated? */
    get animated() { return this._animated; }
    set animated(/*boolean*/ value) { this._animated = value; }

    /** Overall animation period (in milliseconds) */
    get animatePeriod() { return this._animatePeriod; }
    /** Overall animation period (in milliseconds) */
    set animatePeriod(/* long */ value) { this._animatePeriod = value; }

    /** Total frames of the animated period */
    get totalFrames() { return this._totalFrames; }
    set totalFrames(/*int*/ value) {
        this._totalFrames = value;
        this.currentFrame = 0;
    }

    get currentFrame() { return this._currentFrame; }
    set currentFrame(/*int*/ value) { this._currentFrame = value; }

}
