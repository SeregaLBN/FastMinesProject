package fmg.core.types;

import fmg.core.mosaic.cells.BaseCell;

public class ClickResult extends ClickCellResult {

    public final BaseCell cellDown;
    public final boolean isLeft;
    public final boolean isDown;

    public ClickResult(BaseCell cellDown, boolean left, boolean down) {
        this.cellDown = cellDown;
        this.isLeft = left;
        this.isDown = down;
    }

    @Override
    protected String toStringInternal() {
        return "cellDown=" + cellDown
             + ", isLeft=" + isLeft
             + ", isDown=" + isDown
             + ", "  + super.toStringInternal();
    }

}
