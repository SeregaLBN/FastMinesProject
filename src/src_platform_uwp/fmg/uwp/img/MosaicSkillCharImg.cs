using fmg.core.img;
using fmg.core.types;

namespace fmg.uwp.img {

   /// <summary> DEMO. Representable <see cref="ESkillLevel"/> as CHAR </summary>
   public class MosaicSkillCharImg : WithBurgerMenuView<string, MosaicSkillModel> {

      protected MosaicSkillCharImg(ESkillLevel skill)
         : base(new MosaicSkillModel(skill))
      { }

      protected override void DrawBody() { }

      protected override string CreateImage() {
         return Model.MosaicSkill.Value.UnicodeChar().ToString();
      }

      protected override void Disposing() {
         Model.Dispose();
         base.Disposing();
      }

      /////////////////////////////////////////////////////////////////////////////////////////////////////
      //    custom implementations
      /////////////////////////////////////////////////////////////////////////////////////////////////////

      /// <summary> MosaicsSkill image controller implementation for <see cref="MosaicSkillCharImg"/> </summary>
      public class Controller : MosaicSkillController<string, MosaicSkillCharImg> {

         public Controller(ESkillLevel skill)
            : base(false, new MosaicSkillCharImg(skill)) { }

         protected override void Disposing() {
            View.Dispose();
            base.Disposing();
         }

      }

   }

}
