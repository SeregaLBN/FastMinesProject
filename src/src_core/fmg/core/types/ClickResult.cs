using fmg.core.mosaic.cells;

namespace fmg.core.types.click {

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

    }

}
