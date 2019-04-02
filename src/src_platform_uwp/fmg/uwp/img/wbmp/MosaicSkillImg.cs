using System;
using System.Collections.Generic;
using Windows.UI.Xaml.Media.Imaging;
using fmg.common;
using fmg.common.geom;
using fmg.core.types;
using fmg.core.img;

namespace fmg.uwp.img.wbmp {

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
                Model.Dispose();
                base.Disposing();
            }

        }

        /// <summary> MosaicsSkill image controller implementation for <see cref="WBmpView"/> </summary>
        public class WBmpController : MosaicSkillController<WriteableBitmap, WBmpView> {

            public WBmpController(ESkillLevel? skill)
                : base(!skill.HasValue, new WBmpView(skill))
            { }

            protected override void Disposing() {
                View.Dispose();
                base.Disposing();
            }

        }

    }

}
