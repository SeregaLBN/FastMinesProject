using Fmg.Core.Img;
using Fmg.Core.Types;

namespace Fmg.Uwp.Img {

    /// <summary> DEMO. Representable <see cref="ESkillLevel"/> as CHAR </summary>
    public static class MosaicSkillCharImg {

        /////////////////////////////////////////////////////////////////////////////////////////////////////
        //    custom implementations
        /////////////////////////////////////////////////////////////////////////////////////////////////////

        /// <summary> DEMO. Representable <see cref="ESkillLevel"/> as CHAR </summary>
        public class CharView : WithBurgerMenuView<string, MosaicSkillModel> {

            public CharView(ESkillLevel skill)
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

        }

        /// <summary> MosaicsSkill image controller implementation for <see cref="CharView"/> </summary>
        public class CharController : MosaicSkillController<string, CharView> {

            public CharController(ESkillLevel skill)
                : base(false, new CharView(skill))
            { }

            protected override void Disposing() {
                View.Dispose();
                base.Disposing();
            }

        }

    }

}
