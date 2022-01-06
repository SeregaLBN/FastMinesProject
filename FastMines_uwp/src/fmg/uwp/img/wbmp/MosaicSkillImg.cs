using System;
using System.Collections.Generic;
using Windows.UI.Xaml.Media.Imaging;
using Fmg.Common;
using Fmg.Common.Geom;
using Fmg.Core.Types;
using Fmg.Core.Img;

namespace Fmg.Uwp.Img.Wbmp {

    /// <summary> Representable <see cref="EMosaicSkill"/> as image (<see cref="WriteableBitmap"/> implementation)</summary>
    public static class MosaicSkillImg {

        /////////////////////////////////////////////////////////////////////////////////////////////////////
        //    custom implementations
        /////////////////////////////////////////////////////////////////////////////////////////////////////

        /// <summary> Representable <see cref="EMosaicSkill"/> as image (<see cref="WriteableBitmap"/> implementation)</summary>
        public class WBmpView : MosaicSkillOrGroupView<MosaicSkillModel> {

            /// <summary>ctor</summary>
            /// <param name="skill">may be null. if Null - representable image of ESkillLevel.class </param>
            public WBmpView(ESkillLevel? skill)
                : base(new MosaicSkillModel(skill))
            { }

            protected override IEnumerable<Tuple<Color, IEnumerable<PointDouble>>> Coords {
                get { return Model.Coords; }
            }

            protected override void Disposing() {
                base.Disposing();
                Model.Dispose();
            }

        }

        /// <summary> MosaicsSkill image controller implementation for <see cref="WBmpView"/> </summary>
        public class WBmpController : MosaicSkillController<WriteableBitmap, WBmpView> {

            public WBmpController(ESkillLevel? skill)
                : base(!skill.HasValue, new WBmpView(skill))
            { }

            protected override void Disposing() {
                base.Disposing();
                View.Dispose();
            }

        }

    }

}
