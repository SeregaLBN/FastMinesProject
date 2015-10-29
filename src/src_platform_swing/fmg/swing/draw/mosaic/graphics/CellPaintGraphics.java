package fmg.swing.draw.mosaic.graphics;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;

import fmg.common.geom.Point;
import fmg.common.geom.Rect;
import fmg.common.geom.Region;
import fmg.common.geom.Bound;
import fmg.core.mosaic.cells.BaseCell;
import fmg.core.types.EClose;
import fmg.core.types.EOpen;
import fmg.core.types.EState;
import fmg.swing.Cast;
import fmg.swing.draw.GraphicContext;
import fmg.swing.draw.mosaic.CellPaint;

/**
 * Helper class for drawing info
 * @author SeregaLBN
 *
 */
public class CellPaintGraphics extends CellPaint<PaintableGraphics> {
	/** @see javax.swing.JComponent.paint */
	@Override
	public void paint(BaseCell cell, PaintableGraphics p) {
//		Object obj = this;
//		if (obj instanceof JComponent) {
//			JComponent This = (JComponent)obj;
//			This.paint(g);
//		} else
		{
			Graphics2D g2d = (Graphics2D)p.getGraphics();;

			// save
			Shape shapeOld = g2d.getClip();

			// ограничиваю рисование только границами своей фигуры
			g2d.setClip(Cast.toPolygon(Region.moveXY(cell.getRegion(), gContext.getPadding())));

			// all paint
			this.paintComponent(cell, p);
			this.paintBorder(cell, p);

			// restore
			g2d.setClip(shapeOld);
		}
	}

	/** @see javax.swing.JComponent.paintBorder */
	@Override
	public void paintBorder(BaseCell cell, PaintableGraphics p) {
//		Object obj = this;
//		if (obj instanceof JComponent) {
//			JComponent This = (JComponent)obj;
//			This.paintBorder(g);
//			super.paintBorder(g);
//			return;
//		}

		Graphics2D g2 = (Graphics2D) p.getGraphics();
		// save
		Stroke strokeOld = g2.getStroke();
		Object oldValAntialiasing = g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);

		// set my custom params
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // TODO для релиза сменить на VALUE_ANTIALIAS_ON 
		g2.setStroke(new BasicStroke(gContext.getPenBorder().getWidth())); // TODO глянуть расширенные параметры конструктора пера

		// draw lines
		paintBorderLines(cell, p);

		// debug - визуально проверяю верность вписанного квадрата (проверять при ширине пера около 21)
//		Rect rcInner = cell.getRcInner(gContext.getPenBorder().getWidth());
//		g.setColor(Color.MAGENTA);
//		g.drawRect(rcInner.x, rcInner.y, rcInner.width, rcInner.height);

		// restore
		g2.setStroke(strokeOld);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldValAntialiasing);
	}

	/** draw border lines */
	@Override
	public void paintBorderLines(BaseCell cell, PaintableGraphics p) {
		Bound padding = gContext.getPadding();
		boolean down = cell.getState().isDown() || (cell.getState().getStatus() == EState._Open);
		Graphics g = p.getGraphics();
		if (gContext.isIconicMode()) {
			g.setColor(Cast.toColor(down ? gContext.getPenBorder().getColorLight() : gContext.getPenBorder().getColorShadow()));
			g.drawPolygon(Cast.toPolygon(Region.moveXY(cell.getRegion(), padding)));
		} else {
			g.setColor(Cast.toColor(down ? gContext.getPenBorder().getColorLight()  : gContext.getPenBorder().getColorShadow()));
			int s = cell.getShiftPointBorderIndex();
			int v = cell.getAttr().getVertexNumber(cell.getDirection());
			for (int i=0; i<v; i++) {
				Point p1 = cell.getRegion().getPoint(i);
				Point p2 = (i != (v-1)) ? cell.getRegion().getPoint(i+1) : cell.getRegion().getPoint(0);
				if (i==s)
					g.setColor(Cast.toColor(down ? gContext.getPenBorder().getColorShadow(): gContext.getPenBorder().getColorLight()));
				g.drawLine(p1.x+padding.getLeft(), p1.y+padding.getTop(), p2.x+padding.getLeft(), p2.y+padding.getTop());
			}
		}
	}

	/** @see javax.swing.JComponent.paintComponent */
	@Override
	public void paintComponent(BaseCell cell, PaintableGraphics p) {
		Graphics g = p.getGraphics();
		Color colorOld = g.getColor();
		Bound padding = gContext.getPadding();

		paintComponentBackground(cell, p);

		Rect rcInner = cell.getRcInner(gContext.getPenBorder().getWidth());
//		g.setColor(Color.MAGENTA);
//		g.drawRect(rcInner.x, rcInner.y, rcInner.width, rcInner.height);

		// output Pictures
		if ((gContext.getImgFlag() != null) &&
			(cell.getState().getStatus() == EState._Close) &&
			(cell.getState().getClose() == EClose._Flag))
		{
			gContext.getImgFlag().paintIcon(gContext.getOwner(), g, rcInner.x+padding.getLeft(), rcInner.y+padding.getTop());
		} else
		if ((gContext.getImgMine() != null) &&
			(cell.getState().getStatus() == EState._Open ) &&
			(cell.getState().getOpen() == EOpen._Mine))
		{
			gContext.getImgMine().paintIcon(gContext.getOwner(), g, rcInner.x+padding.getLeft(), rcInner.y+padding.getTop());
		} else
		// output text
		{
			String szCaption;
			if (cell.getState().getStatus() == EState._Close) {
				g.setColor(Cast.toColor(gContext.getColorText().getColorClose(cell.getState().getClose().ordinal())));
				szCaption = cell.getState().getClose().toCaption();
//				szCaption = cell.getCoord().x + ";" + cell.getCoord().y; // debug
//				szCaption = ""+cell.getDirection(); // debug
			} else {
				g.setColor(Cast.toColor(gContext.getColorText().getColorOpen(cell.getState().getOpen().ordinal())));
				szCaption = cell.getState().getOpen().toCaption();
			}
			if ((szCaption != null) && (szCaption.length() > 0))
			{
				rcInner.moveXY(padding.getLeft(), padding.getTop());
				if (cell.getState().isDown())
					rcInner.moveXY(1, 1);
				DrawText(g, szCaption, Cast.toRect(rcInner));
//				{ // test
//					Color clrOld = g.getColor(); // test
//					g.setColor(Color.red);
//					g.drawRect(rcInner.x, rcInner.y, rcInner.width, rcInner.height);
//					g.setColor(clrOld);
//				}
			}
		}

		// restore
		g.setColor(colorOld);
	}

	/** залить ячейку нужным цветом */
	@Override
	public void paintComponentBackground(BaseCell cell, PaintableGraphics p) {
		Graphics g = p.getGraphics();
//		if (gContext.isIconicMode()) // когда русуется иконка, а не игровое поле, - делаю попроще...
//			return;
		g.setColor(Cast.toColor(cell.getBackgroundFillColor(
				gContext.getBackgroundFill().getMode(),
				GraphicContext.getDefaultBackgroundFillColor(),
				gContext.getBackgroundFill().getColors()
				)));
		g.fillPolygon(Cast.toPolygon(Region.moveXY(cell.getRegion(), gContext.getPadding())));
	}

	private static Rectangle2D getStringBounds(String text, Font font) {
		TextLayout tl = new TextLayout(text, font, new FontRenderContext(null, true, true));
		return tl.getBounds();
//		return font.getStringBounds(text, new FontRenderContext(null, true, true));
	}
	public static void DrawText(Graphics g, String text, Rectangle rc) {
		if ((text == null) || text.trim().isEmpty())
			return;
		//DrawText(m_GContext.m_hDCTmp, szCaption, -1, &sq_tmp, DT_CENTER | DT_VCENTER | DT_SINGLELINE);
		Rectangle2D bnd = getStringBounds(text, g.getFont());
//		{ // test
//			Color clrOld = g.getColor();
//			g.setColor(Color.BLUE);
//			g.fillRect(rc.x, rc.y, rc.width, rc.height);
//			g.setColor(clrOld);
//		}
		g.drawString(text,
				rc.x          +(int)((rc.width -bnd.getWidth ())/2.),
				rc.y+rc.height-(int)((rc.height-bnd.getHeight())/2.));
	}

}