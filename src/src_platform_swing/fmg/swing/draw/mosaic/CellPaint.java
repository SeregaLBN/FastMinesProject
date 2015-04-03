package fmg.swing.draw.mosaic;

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

import javax.swing.UIDefaults;
import javax.swing.UIManager;

import fmg.common.geom.Point;
import fmg.common.geom.Rect;
import fmg.common.geom.Region;
import fmg.common.geom.Size;
import fmg.core.model.mosaics.EClose;
import fmg.core.model.mosaics.EOpen;
import fmg.core.model.mosaics.EState;
import fmg.core.model.mosaics.cell.BaseCell;
import fmg.swing.draw.GraphicContext;
import fmg.swing.geom.Cast;

/**
 * Helper class for drawing info
 * @author SeregaLBN
 *
 */
public class CellPaint {
	protected GraphicContext gContext;
	
	public CellPaint(GraphicContext gContext) {
		this.gContext = gContext;
	}

	protected void repaint(BaseCell cell) {
//    	gContext.getOwner().paintImmediately(Cast.toRect(cell.getRcOuter()));
		gContext.getOwner().repaint(Cast.toRect(cell.getRcOuter()));
	}

	/** @see javax.swing.JComponent.paint */
	public void paint(BaseCell cell, Graphics g) {
//		Object obj = this;
//		if (obj instanceof JComponent) {
//			JComponent This = (JComponent)obj;
//			This.paint(g);
//		} else
		{
			Graphics2D g2d = (Graphics2D) g;

			// save
			Shape shapeOld = g2d.getClip();

			// ограничиваю рисование только границами своей фигуры
			g2d.setClip(Cast.toPolygon(Region.moveXY(cell.getRegion(), gContext.getBound())));

			// all paint
			this.paintComponent(cell, g);
			this.paintBorder(cell, g);

			// restore
			g2d.setClip(shapeOld);
		}
	}

	/** @see javax.swing.JComponent.paintBorder */
	public void paintBorder(BaseCell cell, Graphics g) {
//		Object obj = this;
//		if (obj instanceof JComponent) {
//			JComponent This = (JComponent)obj;
//			This.paintBorder(g);
//			super.paintBorder(g);
//			return;
//		}

		Graphics2D g2 = (Graphics2D) g;
		// save
		Stroke strokeOld = g2.getStroke();
		Object oldValAntialiasing = g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);

		// set my custom params
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // TODO для релиза сменить на VALUE_ANTIALIAS_ON 
		g2.setStroke(new BasicStroke(gContext.getPenBorder().getWidth())); // TODO глянуть расширенные параметры конструктора пера

		// draw lines
		paintBorderLines(cell, g);

		// debug - визуально проверяю верность вписанного квадрата (проверять при ширине пера около 21)
//		Rect rcInner = cell.getRcInner(gContext.getPenBorder().getWidth());
//		g.setColor(Color.MAGENTA);
//		g.drawRect(rcInner.x, rcInner.y, rcInner.width, rcInner.height);

		// restore
		g2.setStroke(strokeOld);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldValAntialiasing);
	}

	/** draw border lines */
	public void paintBorderLines(BaseCell cell, Graphics g) {
		Size bound = gContext.getBound();
		boolean down = cell.getState().isDown() || (cell.getState().getStatus() == EState._Open);
		if (gContext.isIconicMode()) {
			g.setColor(Cast.toColor(down ? gContext.getPenBorder().getColorLight() : gContext.getPenBorder().getColorShadow()));
			g.drawPolygon(Cast.toPolygon(Region.moveXY(cell.getRegion(), bound)));
		} else {
			g.setColor(Cast.toColor(down ? gContext.getPenBorder().getColorLight()  : gContext.getPenBorder().getColorShadow()));
			int s = cell.getShiftPointBorderIndex();
			int v = cell.getAttr().getVertexNumber(cell.getDirection());
			for (int i=0; i<v; i++) {
				Point p1 = cell.getRegion().getPoint(i);
				Point p2 = (i != (v-1)) ? cell.getRegion().getPoint(i+1) : cell.getRegion().getPoint(0);
				if (i==s)
					g.setColor(Cast.toColor(down ? gContext.getPenBorder().getColorShadow(): gContext.getPenBorder().getColorLight()));
				g.drawLine(p1.x+bound.width, p1.y+bound.height, p2.x+bound.width, p2.y+bound.height);
			}
		}
	}

	/** @see javax.swing.JComponent.paintComponent */
	public void paintComponent(BaseCell cell, Graphics g) {
		Color colorOld = g.getColor();
		Size bound = gContext.getBound();

		paintComponentBackground(cell, g);

		Rect rcInner = cell.getRcInner(gContext.getPenBorder().getWidth());
//		g.setColor(Color.MAGENTA);
//		g.drawRect(rcInner.x, rcInner.y, rcInner.width, rcInner.height);

		// output Pictures
		if ((gContext.getImgFlag() != null) &&
			(cell.getState().getStatus() == EState._Close) &&
			(cell.getState().getClose() == EClose._Flag))
		{
			gContext.getImgFlag().paintIcon(gContext.getOwner(), g, rcInner.x+bound.width, rcInner.y+bound.height);
		} else
		if ((gContext.getImgMine() != null) &&
			(cell.getState().getStatus() == EState._Open ) &&
			(cell.getState().getOpen() == EOpen._Mine))
		{
			gContext.getImgMine().paintIcon(gContext.getOwner(), g, rcInner.x+bound.width, rcInner.y+bound.height);
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
				rcInner.moveXY(bound);
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

	private fmg.common.Color _defaultBkColor;
	/** Цвет заливки ячейки по-умолчанию. Зависит от текущего UI манагера */
	private fmg.common.Color getDefaultBackgroundFillColor() {
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
	protected void paintComponentBackground(BaseCell cell, Graphics g) {
//		if (gContext.isIconicMode()) // когда русуется иконка, а не игровое поле, - делаю попроще...
//			return;
		g.setColor(Cast.toColor(cell.getBackgroundFillColor(
				gContext.getBackgroundFill().getMode(),
				getDefaultBackgroundFillColor(),
				gContext.getBackgroundFill().getColors()
				)));
		g.fillPolygon(Cast.toPolygon(Region.moveXY(cell.getRegion(), gContext.getBound())));
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