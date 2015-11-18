using System;
using Windows.UI.Xaml;
using fmg.data.controller.types;

namespace fmg.uwp.res.img
{
   /// <summary> representable fmg.data.controller.types.ESkillLevel as image </summary>
   public class MosaicsSkillImg : RotatedImg<ESkillLevel, string>
   {

      public MosaicsSkillImg(ESkillLevel skill, int widthAndHeight = DefaultImageSize, int? padding = null)
         : base(skill, widthAndHeight, padding)
      {
         Image = skill.UnicodeChar().ToString();
      }

      public ESkillLevel SkillLevel => Entity;

      protected override void MakeCoords()
      {
         base.MakeCoords(); // => Draw();
      }

      protected override void DrawSync()
      {
         if (Rotate)
         {
            if (_timer == null)
            {
               _timer = new DispatcherTimer { Interval = TimeSpan.FromMilliseconds(RedrawInterval) };
               _timer.Tick += delegate { Draw(); };
            }
            _timer.Start();
         }
         else
         {
            _timer?.Stop();
         }
      }

   }
}