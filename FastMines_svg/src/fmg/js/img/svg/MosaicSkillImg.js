/**
 * Representable {@link fmg.core.types.ESkillLevel} as image
 * <br>
 * SVG impl
 *
 * @param <TImage> SVG specific image context
 **/
class MosaicSkillImg extends MosaicSkillOrGroupView {

    /** @param skill - may be null. if Null - representable image of ESkillLevel.class */
    constructor(/*ESkillLevel*/ skill) { super(new MosaicSkillModel(skill)); }

    getCoords() { return this.Model.getCoords(); }

}

/** MosaicsSkill image view implementation over SVG */
class MosaicSkillImgSvg extends MosaicSkillImg {

    /** @param skill - may be null. if Null - representable image of ESkillLevel.class */
    constructor(/*ESkillLevel*/ skill, /* own SVG */ svg, /* SVG document */ svgDoc) {
        super(skill);
        this._drawContext = null;
        this._svg    = svg;
        this._svgDoc = svgDoc;
    }

    createImage() {
        return this._drawContext = {
            // nodes
            background: null, // node example: <rect x='0' y='0' width='100' height='100' fill='hsla(112.71, 50.95%, 82.35%, 1)' />
            shapes    : null, // nodes examples: <path stroke='#400000' stroke-width='0.7' fill-opacity='0.92'> .... </path>
            menus     : null  // TODO...
        };
    }

    drawBody() {
        this.drawIntoContext(this._drawContext, this._svg, this._svgDoc);
    }

    getSvgContent() { return getSvgContent(this._svg); }

}

/** MosaicsSkill image controller implementation for {@link MosaicSkillImgSvg} */
class MosaicSkillImgControllerSvg extends MosaicSkillController {

    constructor(/*ESkillLevel*/ skill, /* own SVG */ svg, /* SVG document */ svgDoc) {
        super(skill == null, new MosaicSkillImgSvg(skill, svg, svgDoc));
    }

    getFileName() {
        var m = this.Model;
        var res = 'MosaicSkillImg.' + ESkillLevel.getName(m.mosaicSkill);
        res += '.' + m.size.width + 'x' + m.size.height;
        if (m.animated) {
            res += '.' + m.totalFrames + 'frames';
            res += '.' + round(m.animatePeriod/1000, 2) + 'sec';
            res += '.' + (m.animeDirection ? 'clockwise' : 'counterclockwise');
        }
        return res;
    }

}
