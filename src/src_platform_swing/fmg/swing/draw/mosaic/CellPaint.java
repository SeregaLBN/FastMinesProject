package fmg.swing.draw.mosaic;

import fmg.core.mosaic.draw.ICellPaint;
import fmg.core.mosaic.draw.IPaintable;
import fmg.core.mosaic.cells.BaseCell;
import fmg.swing.Cast;
import fmg.swing.draw.GraphicContext;

/**
 * Helper class for drawing info
 * @author SeregaLBN
 *
 */
public abstract class CellPaint<TPaintable extends IPaintable> implements ICellPaint<TPaintable> {
	protected GraphicContext gContext;

	public GraphicContext getGraphicContext() {
		return gContext;
	}
	public void setGraphicContext(GraphicContext gContext) {
		this.gContext = gContext;
	}

	protected void repaint(BaseCell cell) {
//    	gContext.getOwner().paintImmediately(Cast.toRect(cell.getRcOuter()));
		gContext.getOwner().repaint(Cast.toRect(cell.getRcOuter()));
	}

	public abstract void paint(BaseCell cell, TPaintable p);
	
	public abstract void paintBorder(BaseCell cell, TPaintable p);

	/** draw border lines */
	public abstract void paintBorderLines(BaseCell cell, TPaintable p);

	public abstract void paintComponent(BaseCell cell, TPaintable p);

	/** залить ячейку нужным цветом */
	public abstract void paintComponentBackground(BaseCell cell, TPaintable p);

}