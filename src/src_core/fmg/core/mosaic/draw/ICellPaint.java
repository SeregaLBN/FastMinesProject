package fmg.core.mosaic.draw;

import fmg.core.mosaic.draw.IPaintable;
import fmg.core.mosaic.cells.BaseCell;

/**
 * Interface for drawing
 * @author SeregaLBN
 *
 */
public interface ICellPaint<TPaintable extends IPaintable> {
	void paint(BaseCell cell, TPaintable p);
	
	void paintBorder(BaseCell cell, TPaintable p);

	/** draw border lines */
	void paintBorderLines(BaseCell cell, TPaintable p);

	void paintComponent(BaseCell cell, TPaintable p);

	/** Цвет заливки ячейки по-умолчанию. Зависит от текущего UI манагера */
	fmg.common.Color getDefaultBackgroundFillColor();

	/** залить ячейку нужным цветом */
	void paintComponentBackground(BaseCell cell, TPaintable p);
}