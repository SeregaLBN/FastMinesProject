package fmg.core.types;

import fmg.core.mosaic.cells.BaseCell;

public class ClickResult extends ClickCellResult {

    private final BaseCell cellDown;
    private final boolean left, down;

    public ClickResult(BaseCell cellDown, boolean left, boolean down) {
        this.cellDown = cellDown;
        this.left = left;
        this.down = down;
    }

    public BaseCell getCellDown() { return cellDown; }

    /** its left click */
    public boolean isLeft() { return left; }

    /** its down click */
    public boolean isDown() { return down; }

}
