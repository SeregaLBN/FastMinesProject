using fmg.data.controller.types;

namespace fmg.uwp.res.img {

   /// <summary> representable <see cref="ESkillLevel"/> as CHAR </summary>
   public class MosaicsSkillCharImg : core.img.StaticImg<ESkillLevel, string> {

      public MosaicsSkillCharImg(ESkillLevel group)
         : base(group) {}

      public ESkillLevel MosaicSkill => Entity;

      protected override void DrawBody() { }

      protected override string CreateImage() {
         return MosaicSkill.UnicodeChar().ToString();
      }

   }
}
