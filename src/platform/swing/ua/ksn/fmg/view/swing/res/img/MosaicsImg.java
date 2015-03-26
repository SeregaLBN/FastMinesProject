package ua.ksn.fmg.view.swing.res.img;
import java.awt.Component;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;

import ua.ksn.fmg.model.mosaics.CellFactory;
import ua.ksn.fmg.model.mosaics.EMosaic;
import ua.ksn.fmg.model.mosaics.cell.BaseCell;
import ua.ksn.fmg.view.swing.draw.GraphicContext;
import ua.ksn.fmg.view.swing.draw.mosaics.CellPaint;
import ua.ksn.geom.Coord;
import ua.ksn.geom.Size;

/** картинка поля конкретной мозаики. Используется для меню, кнопок, etc... */
public class MosaicsImg implements Icon {
	private final BaseCell.BaseAttribute attr;
	private final CellPaint gInfo;
	private final List<BaseCell> arrCell;
	private final static GraphicContext gContext;
	private final Size sizeField;

	static {
		gContext = new GraphicContext(null, true, new Size(0,0));
		gContext.getPenBorder().setWidth(2);
		gContext.getPenBorder().setColorLight(gContext.getPenBorder().getColorShadow());
	}

	public MosaicsImg(EMosaic mosaicType, boolean smallIco) { this(mosaicType, smallIco, 300); }
	public MosaicsImg(EMosaic mosaicType, boolean smallIco, int area) {
		attr = CellFactory.createAttributeInstance(mosaicType, area);
		arrCell = new ArrayList<BaseCell>();
		gInfo = new CellPaint(gContext);
		sizeField = mosaicType.sizeIcoField(smallIco);
		for (int i=0; i<sizeField.width; i++)
			for (int j=0; j<sizeField.height; j++)
				arrCell.add(CellFactory.createCellInstance(attr, mosaicType, new Coord(i,j)));
	}

	@Override
	public int getIconWidth() {
		return attr.CalcOwnerSize(sizeField, attr.getArea()).width+gContext.getBound().width*2;
	}

	@Override
	public int getIconHeight() {
		return attr.CalcOwnerSize(sizeField, attr.getArea()).height+gContext.getBound().height*2;
	}

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
//		if (false) {
//			Size pixelSize = attr.CalcOwnerSize(sizeField, area);
//			g.setColor(java.awt.Color.ORANGE);
//			g.fillRect(0, 0, pixelSize.width+gContext.getBound().width*2, pixelSize.height+gContext.getBound().height*2);
//		}
		for (BaseCell cell: arrCell)
			gInfo.paint(cell, g);
	}
}