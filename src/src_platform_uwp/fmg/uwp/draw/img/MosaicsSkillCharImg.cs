using fmg.data.controller.types;

namespace fmg.uwp.draw.img {

   /// <summary> representable <see cref="ESkillLevel"/> as CHAR </summary>
   public class MosaicsSkillCharImg : core.img.StaticImg<string> {

      protected MosaicsSkillCharImg(ESkillLevel skill) {
         _mosaicSkill = skill;
      }

      private ESkillLevel _mosaicSkill;
      public ESkillLevel MosaicSkill {
         get { return _mosaicSkill; }
         set { SetProperty(ref _mosaicSkill, value); }
      }

      protected override void DrawBody() { }

      protected override string CreateImage() {
         return MosaicSkill.UnicodeChar().ToString();
      }

   }
}
