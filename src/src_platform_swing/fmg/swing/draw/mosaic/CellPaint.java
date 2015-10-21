package fmg.swing.draw.mosaic;

import java.awt.Color;

import javax.swing.UIDefaults;
import javax.swing.UIManager;

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

	private fmg.common.Color _defaultBkColor;
	/** Цвет заливки ячейки по-умолчанию. Зависит от текущего UI манагера */
	public fmg.common.Color getDefaultBackgroundFillColor() {
		if (_defaultBkColor == null) {
			UIDefaults uiDef = UIManager.getDefaults();
	
			if (gContext.isIconicMode()) // когда русуется иконка, а не игровое поле, - делаю попроще...
				_defaultBkColor = Cast.toColor(uiDef.getColor("Panel.background"));
			else {
				String key = "Panel.background"; // "ToggleButton.light"; // "Button.light"; // 
				// ToggleButton.darkShadow : javax.swing.plaf.ColorUIResource[r=105,g=105,b=105]
				// ToggleButton.background : javax.swing.plaf.ColorUIResource[r=240,g=240,b=240]
				// ToggleButton.focus      : javax.swing.plaf.ColorUIResource[r=0,g=0,b=0]
				// ToggleButton.highlight  : javax.swing.plaf.ColorUIResource[r=255,g=255,b=255]
				// ToggleButton.light      : javax.swing.plaf.ColorUIResource[r=227,g=227,b=227]
				// ToggleButton.shadow     : javax.swing.plaf.ColorUIResource[r=160,g=160,b=160]
				// ToggleButton.foreground : javax.swing.plaf.ColorUIResource[r=0,g=0,b=0]
		
		//		if (state.getStatus() == EStatus._Close)
		//			if (state.isDown())
		//				key = "ToggleButton.light";
		//			else
		//				key = "ToggleButton.highlight";
		//		else
		//			key = "ToggleButton.shadow";
		
				Color clr = uiDef.getColor(key);
				if (clr == null) {
					System.out.println("Invalid color key: " + key);
					clr = uiDef.getColor("Panel.background");
				}
				_defaultBkColor = Cast.toColor(clr);
			}
		}
		return _defaultBkColor;
	}

	/** залить ячейку нужным цветом */
	public abstract void paintComponentBackground(BaseCell cell, TPaintable p);

}