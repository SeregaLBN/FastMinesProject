package fmg.core.img;

import fmg.core.types.ESkillLevel;

/** MVC model of {@link ESkillLevel} representable as image */
public class MosaicsSkillModel extends AnimatedImageModel {

   public static final String PROPERTY_MOSAIC_SKILL = "MosaicSkill";

   public MosaicsSkillModel() {}
   public MosaicsSkillModel(ESkillLevel mosaicSkill) { _mosaicSkill = mosaicSkill; }

   private ESkillLevel _mosaicSkill;
   public ESkillLevel getMosaicSkill() { return _mosaicSkill; }
   public void setMosaicSkill(ESkillLevel value) { setProperty(_mosaicSkill, value, PROPERTY_MOSAIC_SKILL); }

}
