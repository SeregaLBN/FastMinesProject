using System;
using System.Linq;
using System.Collections.Generic;
using Windows.UI.Xaml.Media.Imaging;
using fmg.common;
using fmg.common.geom;
using fmg.core.types;
using fmg.core.img;

namespace fmg.uwp.img.wbmp {

   /// <summary> Representable <see cref="EMosaicSkill"/> as image (<see cref="WriteableBitmap"/> implementation)</summary>
   public class MosaicSkillImg : MosaicSkillOrGroupView<MosaicSkillModel> {

      /// <summary>ctor</summary>
      /// <param name="skill">may be null. if Null - representable image of ESkillLevel.class </param>
      protected MosaicSkillImg(ESkillLevel? skill)
         : base(new MosaicSkillModel(skill))
      { }

      protected override IEnumerable<Tuple<Color, IEnumerable<PointDouble>>> Coords {
         get { return Model.Coords; }
      }

      protected override void Disposing() {
         Model.Dispose();
         base.Disposing();
      }

      /////////////////////////////////////////////////////////////////////////////////////////////////////
      //    custom implementations
      /////////////////////////////////////////////////////////////////////////////////////////////////////

      /// <summary> MosaicsSkill image controller implementation for <see cref="MosaicSkillImg"/> </summary>
      public class Controller : MosaicSkillController<WriteableBitmap, MosaicSkillImg> {

         public Controller(ESkillLevel? skill)
            : base(!skill.HasValue, new MosaicSkillImg(skill))
         { }

         protected override void Disposing() {
            View.Dispose();
            base.Disposing();
         }

      }

   }

}
