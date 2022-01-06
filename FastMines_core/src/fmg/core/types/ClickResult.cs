using Fmg.Core.Mosaic.Cells;

namespace Fmg.Core.Types {

    public class ClickResult : ClickCellResult {

        public ClickResult(BaseCell cellDown, bool left, bool down) {
            CellDown = cellDown;
            IsLeft = left;
            IsDown = down;
        }

        /// <summary> cell which was pressed (down click), but not the fact that the released (click released) </summary>
        public BaseCell CellDown { get; }

        /// <summary> its left click </summary>
        public bool IsLeft { get; }

        /// <summary> its down click </summary>
        public bool IsDown { get; }

        protected override string ToStringInternal() {
            return "CellDown=" + CellDown
                 + ", IsLeft=" + IsLeft
                 + ", IsDown=" + IsDown
                 + ", " + base.ToStringInternal();
        }

    }

}
