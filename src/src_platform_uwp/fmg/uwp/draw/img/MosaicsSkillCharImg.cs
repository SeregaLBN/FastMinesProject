using fmg.core.types;

namespace fmg.uwp.draw.img {

#if false
   /// <summary> representable <see cref="ESkillLevel"/> as CHAR </summary>
   public class MosaicsSkillCharImg : core.img.ImageModel<string> {

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
#endif
}
