using fmg.data.controller.types;

namespace fmg.uwp.res.img {

   /// <summary> representable fmg.data.controller.types.ESkillLevel as CHAR </summary>
   public class MosaicsSkillCharImg : StaticImg<ESkillLevel, string> {

      public MosaicsSkillCharImg(ESkillLevel group, int widthAndHeight = DefaultImageSize, int? padding = null)
         : base(group, widthAndHeight, padding) {}

      public ESkillLevel MosaicSkill => Entity;

      protected override void DrawBody() { }

      protected override string CreateImage() {
         return MosaicSkill.UnicodeChar().ToString();
      }

   }
}
