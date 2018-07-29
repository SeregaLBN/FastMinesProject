using System;
using System.Linq;
using System.Collections.Generic;
using Windows.UI.Xaml.Media.Imaging;
using fmg.common;
using fmg.common.geom;
using fmg.core.types;
using fmg.core.img;

namespace fmg.uwp.img.wbmp {

   /// <summary> Representable <see cref="EMosaicGroup"/> as image.
   /// <br/>
   /// WriteableBitmap impl
   /// </summary>
   public class MosaicGroupImg : MosaicSkillOrGroupView<MosaicGroupModel> {

      /// <summary>ctor</summary>
      /// <param name="group">may be null. if Null - representable image of EMosaicGroup.class</param>
      protected MosaicGroupImg(EMosaicGroup? group)
         : base(new MosaicGroupModel(group))
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

      /// <summary> MosaicsGroup image controller implementation for <see cref="MosaicGroupImg"/> </summary>
      public class Controller : MosaicGroupController<WriteableBitmap, MosaicGroupImg> {

         public Controller(EMosaicGroup? group)
            : base(!group.HasValue, new MosaicGroupImg(group))
         { }

         protected override void Disposing() {
            View.Dispose();
            base.Disposing();
         }

      }

      ////////////// TEST //////////////
      public static IEnumerable<Controller> GetTestData() {
         return (new Controller[] { new Controller(null) })
               .Concat(EMosaicGroupEx.GetValues()
                                     .Select(e => new Controller[] { new Controller(e) })
                                     .SelectMany(m => m));
      }
      //////////////////////////////////

   }

}
