using System;
using System.Collections.Generic;
using Windows.UI.Xaml.Media.Imaging;
using Fmg.Common;
using Fmg.Common.Geom;
using Fmg.Core.Types;
using Fmg.Core.Img;

namespace Fmg.Uwp.Img.Wbmp {

    /// <summary> Representable <see cref="EMosaicGroup"/> as image</summary>
    public static class MosaicGroupImg {

        /////////////////////////////////////////////////////////////////////////////////////////////////////
        //    custom implementations
        /////////////////////////////////////////////////////////////////////////////////////////////////////

        /// <summary> Representable <see cref="EMosaicGroup"/> as image view UWP implementation over (<see cref="WriteableBitmap"/> implementation)</summary>
        public class WBmpView : MosaicSkillOrGroupView<MosaicGroupModel> {

            /// <summary>ctor</summary>
            /// <param name="group">may be null. if Null - representable image of EMosaicGroup.class</param>
            public WBmpView(EMosaicGroup? group)
                : base(new MosaicGroupModel(group))
            { }

            protected override IEnumerable<Tuple<Color, IEnumerable<PointDouble>>> Coords {
                get { return Model.Coords; }
            }

            protected override void Disposing() {
                base.Disposing();
                Model.Dispose();
            }

        }

        /// <summary> MosaicsGroup image controller UWP implementation for <see cref="WBmpView"/> </summary>
        public class WBmpController : MosaicGroupController<WriteableBitmap, WBmpView> {

            public WBmpController(EMosaicGroup? group)
                : base(!group.HasValue, new WBmpView(group))
            { }

            protected override void Disposing() {
                base.Disposing();
                View.Dispose();
            }

        }

    }

}
