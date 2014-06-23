package ua.ksn.fmg.controller.swing;

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

import ua.ksn.fmg.controller.Mosaic;
import ua.ksn.fmg.model.mosaics.CellFactory;
import ua.ksn.fmg.model.mosaics.EMosaic;
import ua.ksn.fmg.model.mosaics.cell.BaseCell;
import ua.ksn.fmg.view.draw.PenBorder;
import ua.ksn.fmg.view.swing.draw.GraphicContext;
import ua.ksn.fmg.view.swing.draw.mosaics.CellPaint;
import ua.ksn.fmg.view.swing.draw.mosaics.MosaicGraphicContext;
import ua.ksn.geom.Coord;
import ua.ksn.geom.Size;
import ua.ksn.swing.geom.Cast;

public class MosaicExt extends Mosaic implements PropertyChangeListener {
	private MosaicGraphicContext _gContext;
	private CellPaint _cellPaint;
	private JPanel _container;
	private MosaicMouseListeners _mosaicMouseListener;
	private static final boolean _DEBUG = true;

	public MosaicExt() {
		super();
	}
	public MosaicExt(Size sizeField, EMosaic mosaicType, int minesCount, int area) {
		super(sizeField, mosaicType, minesCount, area);
	}

	public JPanel getContainer() {
		if (_container == null) {
			_container = new JPanel() {
				private static final long serialVersionUID = 1L;
				
				@Override
				protected void paintComponent(Graphics g) {
//					super.paintComponent(g); // ::DefWindowProc(hwnd, WM_PAINT, 0L, 0L); // ��� ����� �� ������ ���������� WM_PAINT ��� ������� - BeginPaint ... EndPaint
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
					for (BaseCell cell: _matrix)
						if (cell.getRcOuter().Intersects(Cast.toRect(g.getClipBounds()))) // redraw only when needed - when the cells and update region intersect
							getCellPaint().paint(cell, g);
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
	public void setParams(Size newSizeField, EMosaic newMosaicType, Integer newMinesCount) {
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
			_gContext.addPropertyChangeListener(this); // ��������� ��������� -> ����������� �������
		}
		return _gContext;
	}

	public CellPaint getCellPaint() {
		if (_cellPaint == null)
			_cellPaint = new CellPaint(getGraphicContext());
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
						CellFactory.createAttributeInstance(getMosaicType(), getArea()).getMaxBackgroundFillModeValue()));
		super.GameNew();
		getContainer().repaint();
	}

	@Override
	protected void GameBegin(Coord firstClick) {
		getGraphicContext().getBackgroundFill().setMode(0);
		super.GameBegin(firstClick);
	}

	/** ������������� �������� ���������� � ���������� mosaic'a */
	private Coord CursorPointToMosaicCoord(Point point) {
//		long l1 = System.currentTimeMillis();
//		try {
			for (BaseCell cell: _matrix)
				//if (cell.getRcOuter().contains(point)) // ���.. - �������� ���..  (�������� ����� �� ���� �������� ����...) � ��������, �������� �� ������...
					if (cell.PointInRegion(Cast.toPoint(point)))
						return cell.getCoord();
			return Coord.INCORRECT_COORD;
//		} finally {
//			System.out.println("Mosaic::CursorPointToMosaicCoord: find cell: " + (System.currentTimeMillis()-l1) + "ms.");
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
			// ��. ������ - ������ 1
			changeFontSize(getGraphicContext().getPenBorder(), newArea);
	//		getContainer().repaint(); // ��������� ������: area->gContext.font->this.repaint
			getContainer().revalidate();
		}
	}

	private class MosaicMouseListeners implements MouseInputListener, FocusListener {
		@Override
	    public void mouseClicked(MouseEvent e) {}

		@Override
	    public void mousePressed(MouseEvent e) {
			if (SwingUtilities.isLeftMouseButton(e))
				OnLeftButtonDown(CursorPointToMosaicCoord(e.getPoint()));
			else
			if (SwingUtilities.isRightMouseButton(e))
				OnRightButtonDown(CursorPointToMosaicCoord(e.getPoint()));
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (SwingUtilities.isLeftMouseButton(e)) {
	    		// ������� ���� ����� �� ���������� ������� ���� �����, ����� ���� ����� ���������..
	    		// ������� ������������ onClick'a
	    		Component rootFrame = SwingUtilities.getRoot((Component) e.getSource());
	    		boolean rootFrameActive = true;
	    		if (rootFrame instanceof Window)
	    			rootFrameActive = ((Window)rootFrame).isActive(); 

	    		if (rootFrameActive)
	    			OnLeftButtonUp(CursorPointToMosaicCoord(e.getPoint()));
			} else
			if (SwingUtilities.isRightMouseButton(e))
	    		OnRightButtonUp(/*CursorPointToMosaicCoord(e.getPoint())*/);
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
			if (!MosaicExt.this.getCoordDown().equals(Coord.INCORRECT_COORD))
				MosaicExt.this.OnLeftButtonUp(Coord.INCORRECT_COORD);
		}
		@Override
		public void focusGained(FocusEvent e) {}
	}

	public MosaicMouseListeners getMosaicMouseListeners() {
		if (_mosaicMouseListener == null)
			_mosaicMouseListener = new MosaicMouseListeners();
		return _mosaicMouseListener;
	}

	protected void initialize(Size sizeField, EMosaic mosaicType, int minesCount, int area) {
		this.getContainer().setFocusable(true); // ����� �� ����� ����������� FocusListener

		this.getContainer().addMouseListener(getMosaicMouseListeners());
		this.getContainer().addMouseMotionListener(getMosaicMouseListeners());
		this.getContainer().addFocusListener(getMosaicMouseListeners());
		
		super.initialize(sizeField, mosaicType, minesCount, area);

		getContainer().setSize(getContainer().getPreferredSize()); // for run as java been
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if ("GraphicContext_penBorder".equals(evt.getPropertyName())) {
			// ��. ������ - ������ 1
			PenBorder penBorder = (PenBorder) evt.getNewValue();
			changeFontSize(penBorder, getArea());
		}

		if (evt.getSource().getClass() == GraphicContext.class)
			getContainer().repaint();
		if (evt.getSource().getClass() == MosaicGraphicContext.class)
			getContainer().repaint();
	}

    /** ����������� � ���������� ����� ������ ������ */
    public void changeFontSize() { changeFontSize(getGraphicContext().getPenBorder(), getArea()); }
    /** ����������� � ���������� ����� ������ ������ */
    private void changeFontSize(PenBorder penBorder, int area) {
		getGraphicContext().setFontSize(
				(int) getCellAttr().CalcSq(
						area, penBorder.getWidth()));
    }

	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.add((new MosaicExt()).getContainer());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}
}
/**
 * ������
 * ======
 *
 *
 *  1. ����������� ��������:
 *  ------------------------
 *    Mosaic:
 *      * ����� area (������� ������):
 *        - ���������� ������� (������� �������, ������, etc) ������ (�� @BaseCell$BaseAttribute)
 *          ( @BaseCell$BaseAttribute ������� Mosaic_area)
 *        - ���������� ������ ������ - ���������� @BaseCell$BaseAttribute ������ ���������� ��������, � ����� SetSizeFont ��� @MosaicGraphicContext
 *          (�� @Mosaic.setArea ����� @BaseCell$BaseAttribute.InSquare � ������������ ����� ������ ������)
 *        - ������ ����������� �������
 *      * ����� ��� �������:
 *        - �.�. �������� ������ ���������� � ������ ��������, �� ����� ������� fireOnChangeMosaicType ����� � @Main ����� (� ��������� @MosaicListener.OnChangeMosaicType) :
 *           - ���������� ������ ������
 *           - ���������� ������ �������� ���� � �����
 *
 *    ���� ��������:
 *      * ����� ����� ��������:
 *        - ������ ����������� �������
 *          ( @Mosaic ������� @MosaicGraphicContext)
 *      * ����� ������ ����:
 *        - ���������� ������ ������ - �� ������� area ���������� @BaseCell$BaseAttribute ������ ���������� ��������, � ����� SetSizeFont ��� @MosaicGraphicContext
 *           ( @Mosaic ������� @MosaicGraphicContext.������_����)
 *          (���, ��������� ������ ����, �������������� (����� �������), ������ �� �������� @MosaicGraphicContext - �����)
 *
 *    BaseAttr:
 *      * ����� ����� �������� (������� �������, ������, etc) ������
 *        - ���������� ��������� ���� ����� ������
 *          ( @BaseCell ������� @BaseCell$BaseAttribute)
 *
 *   �.�., � �����, ���� ���� ����������:
 *     - @Mosaic ������� @MosaicGraphicContext
 *     - @BaseCell$BaseAttribute ������� @Mosaic - �������� Mosaic_area
 *     - @BaseCell ������� @BaseCell$BaseAttribute
 *
 *     - �� � ��������� ��� @Main - @MosaicListener 
 *
 **/