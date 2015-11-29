using fmg.data.controller.types;

namespace fmg.uwp.res.img
{
   /// <summary> representable fmg.data.controller.types.ESkillLevel as image </summary>
   public class MosaicsSkillImg : RotatedImg<ESkillLevel, string>
   {

      public MosaicsSkillImg(ESkillLevel skill, int widthAndHeight = DefaultImageSize, int? padding = null)
         : base(skill, widthAndHeight, padding)
      {
      }

      public ESkillLevel SkillLevel => Entity;

      protected override void MakeCoords() {
         base.MakeCoords(); // => Draw();
      }

      protected override void DrawBody()
      {
         //System.Diagnostics.Debug.WriteLine("DrawBody: " + SkillLevel);
         if (Image ==  null)
            Image = SkillLevel.UnicodeChar().ToString();
      }

   }
}