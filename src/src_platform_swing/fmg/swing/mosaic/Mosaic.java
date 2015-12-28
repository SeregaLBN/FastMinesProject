package fmg.swing.mosaic;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputListener;

import fmg.common.geom.Matrisize;
import fmg.common.geom.Size;
import fmg.core.mosaic.MosaicHelper;
import fmg.core.mosaic.MosaicBase;
import fmg.core.mosaic.cells.BaseCell;
import fmg.core.types.EMosaic;
import fmg.data.view.draw.PenBorder;
import fmg.swing.Cast;
import fmg.swing.draw.GraphicContext;
import fmg.swing.draw.mosaic.MosaicGraphicContext;
import fmg.swing.draw.mosaic.graphics.CellPaintGraphics;
import fmg.swing.draw.mosaic.graphics.PaintableGraphics;

public class Mosaic extends MosaicBase implements PropertyChangeListener {
	private MosaicGraphicContext _gContext;
	private CellPaintGraphics _cellPaint;
	private JPanel _container;
	private MosaicMouseListeners _mosaicMouseListener;
	private static final boolean _DEBUG = true;

	public Mosaic() {
		super();
	}
	public Mosaic(Matrisize sizeField, EMosaic mosaicType, int minesCount, int area) {
		super(sizeField, mosaicType, minesCount, area);
	}

	public JPanel getContainer() {
		if (_container == null) {
			_container = new JPanel() {
				private static final long serialVersionUID = 1L;
				
				@Override
				protected void paintComponent(Graphics g) {
//					super.paintComponent(g); // ::DefWindowProc(hwnd, WM_PAINT, 0L, 0L); // это чтобы не писать обработчик WM_PAINT как принято - BeginPaint ... EndPaint
					{
						Graphics2D g2d = (Graphics2D) g;
						g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					}

					// background color
					Rectangle rcFill = g.getClipBounds();
					g.setColor(getGraphicContext().getColorBk());
					g.fillRect(rcFill.x, rcFill.y, rcFill.width, rcFill.height);

					// paint cells
					g.setFont(getGraphicContext().getFont());
					PaintableGraphics p = new PaintableGraphics(g);
					for (BaseCell cell: _matrix)
						if (cell.getRcOuter().Intersects(Cast.toRect(g.getClipBounds()))) // redraw only when needed - when the cells and update region intersect
							getCellPaint().paint(cell, p);
				}

			    @Override
			    public Dimension getPreferredSize() {
			    	Size size = getWindowSize();
			    	size.height++;
			    	size.width++;
//			    	System.out.println("Mosaic::getPreferredSize: size="+size);
			    	return Cast.toSize(size);
			    }
			    @Override
			    public Dimension getMinimumSize() {
			    	return getPreferredSize();
			    }

			};
		}
		return _container;
	}

	@Override
	protected void OnError(String msg) {
		if (_DEBUG)
			JOptionPane.showMessageDialog(getContainer(), msg, "Error", JOptionPane.QUESTION_MESSAGE, null);
		else
			super.OnError(msg);
	}

	@Override
	public void setParams(Matrisize newSizeField, EMosaic newMosaicType, Integer newMinesCount) {
		if (this._mosaicType != newMosaicType)
			_cellPaint = null;
		
		super.setParams(newSizeField, newMosaicType, newMinesCount);

		getContainer().repaint();
		getContainer().revalidate();
	}

	public MosaicGraphicContext getGraphicContext() {
		if (_gContext == null) {
			_gContext = new MosaicGraphicContext(getContainer());
//			changeFontSize(_gContext.getPenBorder(), getArea());
			_gContext.addPropertyChangeListener(this); // изменение контекста -> перерисовка мозаики
		}
		return _gContext;
	}

	public CellPaintGraphics getCellPaint() {
		if (_cellPaint == null) {
			_cellPaint = new CellPaintGraphics();
			_cellPaint.setGraphicContext(getGraphicContext());
		}
		return _cellPaint;
	}

	@Override
	protected void Repaint(BaseCell cell) {
		if (cell == null)
			getContainer().repaint();
		else
			//getCellPaint().paint(cell, getContainer().getGraphics());
			getContainer().repaint(Cast.toRect(cell.getRcOuter()));
	}
	
	@Override
	public void GameNew() {
		getGraphicContext().getBackgroundFill().setMode(
				1 + new Random().nextInt(
						MosaicHelper.createAttributeInstance(getMosaicType(), getArea()).getMaxBackgroundFillModeValue()));
		super.GameNew();
		getContainer().repaint();
	}

	@Override
	protected void GameBegin(BaseCell firstClickCell) {
		getGraphicContext().getBackgroundFill().setMode(0);
		super.GameBegin(firstClickCell);
	}

	/** преобразовать экранные координаты в ячейку поля мозаики */
	private BaseCell CursorPointToCell(Point point) {
//		long l1 = System.currentTimeMillis();
//		try {
		    fmg.common.geom.Point p = Cast.toPoint(point);
			for (BaseCell cell: _matrix)
				//if (cell.getRcOuter().contains(point)) // пох.. - тормозов нет..  (измерить время на макс размерах поля...) в принципе, проверка не нужная...
					if (cell.PointInRegion(p))
						return cell;
			return null;
//		} finally {
//			System.out.println("Mosaic::CursorPointToCell: find cell: " + (System.currentTimeMillis()-l1) + "ms.");
//		}
	}

	@Override
	protected boolean RequestToUser_RestoreLastGame() {
		int iRes = JOptionPane.showOptionDialog(getContainer(), "Restore last game?", "Question", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
		return (iRes == JOptionPane.NO_OPTION);
	}

	@Override
	public void setArea(int newArea)  {
		int oldVal = this.getArea();
		super.setArea(newArea);
		int newVal = this.getArea();
		if (oldVal != newVal) {
			// см. комент - сноску 1
			changeFontSize(getGraphicContext().getPenBorder(), newArea);
	//		getContainer().repaint(); // вызовится неявно: area->gContext.font->this.repaint
			getContainer().revalidate();
		}
	}

	private class MosaicMouseListeners implements MouseInputListener, FocusListener {
		@Override
	    public void mouseClicked(MouseEvent e) {}

		@Override
	    public void mousePressed(MouseEvent e) {
			if (SwingUtilities.isLeftMouseButton(e))
				OnLeftButtonDown(CursorPointToCell(e.getPoint()));
			else
			if (SwingUtilities.isRightMouseButton(e))
				OnRightButtonDown(CursorPointToCell(e.getPoint()));
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (SwingUtilities.isLeftMouseButton(e)) {
	    		// Получаю этот эвент на отпускание клавиши даже тогда, когда окно проги неактивно..
	    		// Избегаю срабатывания onClick'a
	    		Component rootFrame = SwingUtilities.getRoot((Component) e.getSource());
	    		boolean rootFrameActive = true;
	    		if (rootFrame instanceof Window)
	    			rootFrameActive = ((Window)rootFrame).isActive(); 

	    		if (rootFrameActive)
	    			OnLeftButtonUp(CursorPointToCell(e.getPoint()));
			} else
			if (SwingUtilities.isRightMouseButton(e))
	    		OnRightButtonUp(/*CursorPointToCell(e.getPoint())*/);
	    }

		@Override
	    public void mouseEntered(MouseEvent e) {}
	    @Override
	    public void mouseExited(MouseEvent e) {}
	    @Override
	    public void mouseDragged(MouseEvent e) {}
	    @Override
	    public void mouseMoved(MouseEvent e) {}
		@Override
		public void focusLost(FocusEvent e) {
//			System.out.println("Mosaic::MosaicMouseListeners::focusLost: " + e);
			BaseCell cell = Mosaic.this.getCellDown();
			if (cell == null)
				return;
			if (cell.getState().isDown())
				Mosaic.this.OnLeftButtonUp(null);
			else
				Mosaic.this.OnRightButtonUp();
		}
		@Override
		public void focusGained(FocusEvent e) {}
	}

	public MosaicMouseListeners getMosaicMouseListeners() {
		if (_mosaicMouseListener == null)
			_mosaicMouseListener = new MosaicMouseListeners();
		return _mosaicMouseListener;
	}

	protected void initialize(Matrisize sizeField, EMosaic mosaicType, int minesCount, int area) {
		this.getContainer().setFocusable(true); // иначе не будет срабатывать FocusListener

		this.getContainer().addMouseListener(getMosaicMouseListeners());
		this.getContainer().addMouseMotionListener(getMosaicMouseListeners());
		this.getContainer().addFocusListener(getMosaicMouseListeners());
		
		super.initialize(sizeField, mosaicType, minesCount, area);

		getContainer().setSize(getContainer().getPreferredSize()); // for run as java been
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if ("GraphicContext_penBorder".equals(evt.getPropertyName())) {
			// см. комент - сноску 1
			PenBorder penBorder = (PenBorder) evt.getNewValue();
			changeFontSize(penBorder, getArea());
		}

		if (evt.getSource().getClass() == GraphicContext.class)
			getContainer().repaint();
		if (evt.getSource().getClass() == MosaicGraphicContext.class)
			getContainer().repaint();
	}

    /** пересчитать и установить новую высоту шрифта */
    public void changeFontSize() { changeFontSize(getGraphicContext().getPenBorder(), getArea()); }
    /** пересчитать и установить новую высоту шрифта */
    private void changeFontSize(PenBorder penBorder, int area) {
		getGraphicContext().setFontSize(
				(int) getCellAttr().getSq(penBorder.getWidth()));
    }

	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.add((new Mosaic()).getContainer());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}
}
/**
 * Сноски
 * ======
 *
 *
 *  1. Зависимости объектов:
 *  ------------------------
 *    Mosaic:
 *      * меняю area (площать ячейки):
 *        - перерасчёт величин (базовая сторона, высота, etc) фигуры (из @BaseCell$BaseAttribute)
 *          ( @BaseCell$BaseAttribute слушает Mosaic_area)
 *        - перерасчёт высоты шрифта - запрашиваю @BaseCell$BaseAttribute размер вписанного квадрата, а далее SetSizeFont для @MosaicGraphicContext
 *          (из @Mosaic.setArea узнаю @BaseCell$BaseAttribute.InSquare и устанавливаю новую высоту шрифта)
 *        - полная перерисовка мозаики
 *      * меняю тип мозаики:
 *        - т.к. меняется размер вписанного в ячейку квадрата, то явным вызовом fireOnChangeMosaicType меняю в @Main класе (в слушателе @MosaicListener.OnChangeMosaicType) :
 *           - перерасчёт высоту шрифта
 *           - перерасчёт размер картинок мины и флага
 *
 *    Граф контекст:
 *      * меняю любой параметр:
 *        - полная перерисовка мозаики
 *          ( @Mosaic слушает @MosaicGraphicContext)
 *      * меняю ширину пера:
 *        - перерасчёт высоты шрифта - по текущей area запрашиваю @BaseCell$BaseAttribute размер вписанного квадрата, а далее SetSizeFont для @MosaicGraphicContext
 *           ( @Mosaic слушает @MosaicGraphicContext.ширина_пера)
 *          (тут, изменение ширины пера, опосредственно (через мозаику), меняет др параметр @MosaicGraphicContext - шрифт)
 *
 *    BaseAttr:
 *      * меняю любую величину (базовая сторона, высота, etc) фигуры
 *        - перерасчёт координат всех ячеек фигуры
 *          ( @BaseCell слушает @BaseCell$BaseAttribute)
 *
 *   Т.е., в целом, имею след слушателей:
 *     - @Mosaic слушает @MosaicGraphicContext
 *     - @BaseCell$BaseAttribute слушает @Mosaic - свойство Mosaic_area
 *     - @BaseCell слушает @BaseCell$BaseAttribute
 *
 *     - ну и отдельный для @Main - @MosaicListener 
 *
 **/