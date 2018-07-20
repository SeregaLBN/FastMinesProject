using fmg.core.types;

namespace fmg.uwp.img {

#if false
   /// <summary> representable <see cref="ESkillLevel"/> as CHAR </summary>
   public class MosaicSkillCharImg : core.img.ImageModel<string> {

      protected MosaicSkillCharImg(ESkillLevel skill) {
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
