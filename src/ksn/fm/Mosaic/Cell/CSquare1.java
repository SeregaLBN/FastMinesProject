package ksn.fm.Mosaic.Cell;

import java.awt.Graphics2D;

import ksn.utils.Types.GraphPrimitives.Coord;
import ksn.utils.Types.GraphPrimitives.Point;
import ksn.utils.Types.GraphPrimitives.Size;

public class CSquare1 extends CBase {
	public static Size GetSizeInPixel(Size sizeField, int iArea) {
		m_a = (float)Math.sqrt(iArea); // размер стороны квадрата
		Size result = new Size((long)(sizeField.cx * m_a),
		                       (long)(sizeField.cy * m_a));
		return result;
	}
	public static int SizeInscribedSquare(int iArea, int iBorderWidth) {
		m_sq = (float) (Math.sqrt((float)iArea)-2*iBorderWidth);
		return (int) m_sq;
	}

	public CSquare1(Coord coord, Size sizeField, int iArea, CGraphicContext gContext) {
		super(coord, sizeField, iArea, gContext,
				8, 4,
				0);
		SetPoint(iArea);
		// определяю координаты соседей
		m_pNeighbor[0].X = (short) (m_Coord.X-1); m_pNeighbor[0].Y = (short) (m_Coord.Y-1);
		m_pNeighbor[1].X = (short) (m_Coord.X  ); m_pNeighbor[1].Y = (short) (m_Coord.Y-1);
		m_pNeighbor[2].X = (short) (m_Coord.X+1); m_pNeighbor[2].Y = (short) (m_Coord.Y-1);
		m_pNeighbor[3].X = (short) (m_Coord.X-1); m_pNeighbor[3].Y = (short) (m_Coord.Y  );
		m_pNeighbor[4].X = (short) (m_Coord.X+1); m_pNeighbor[4].Y = (short) (m_Coord.Y  );
		m_pNeighbor[5].X = (short) (m_Coord.X-1); m_pNeighbor[5].Y = (short) (m_Coord.Y+1);
		m_pNeighbor[6].X = (short) (m_Coord.X  ); m_pNeighbor[6].Y = (short) (m_Coord.Y+1);
		m_pNeighbor[7].X = (short) (m_Coord.X+1); m_pNeighbor[7].Y = (short) (m_Coord.Y+1);
		VerifyNeighbor(sizeField);
	}
	public boolean PointInRegion(Point point) { // принадлежат ли эти экранные координаты ячейке
		if ((point.x < m_pRegion[1].x) || (point.x >= m_pRegion[0].x) ||
		    (point.y < m_pRegion[0].y) || (point.y >= m_pRegion[2].y))
			return false;
		return true;
	}
	public void SetPoint(int iArea) { // определить координаты точек из которых состоит фигура
		if (m_Coord.X==0 && m_Coord.Y==0) {
			m_a  = (float)Math.sqrt(iArea); // размер стороны квадрата
			m_sq = m_a-2*m_GContext.m_Border.m_iWidth; // размер квадрата, вписанного в квадрат
		}

		// определение координат точек фигуры
		m_pRegion[1].x = m_pRegion[2].x = (long)(m_a*(m_Coord.X+0));
		m_pRegion[0].x = m_pRegion[3].x = (long)(m_a*(m_Coord.X+1));

		m_pRegion[0].y = m_pRegion[1].y = (long)(m_a*(m_Coord.Y+0));
		m_pRegion[2].y = m_pRegion[3].y = (long)(m_a*(m_Coord.Y+1));

		// определение координат вписанного в фигуру квадрата - область в которую выводится изображение/текст
		m_Square.left   = (long)(m_pRegion[1].x + m_GContext.m_Border.m_iWidth);
		m_Square.top    = (long)(m_pRegion[1].y + m_GContext.m_Border.m_iWidth);
		m_Square.right  = (long)(m_Square.left + m_sq);
		m_Square.bottom = (long)(m_Square.top  + m_sq);

		super.SetPoint(iArea);
	}
	public void Paint() {
		super.Paint();

		Graphics2D g = m_GContext.m_hDCTmp.createGraphics();
		g.setColor(m_bDown ? m_GContext.m_hPenShadow.color : m_GContext.m_hPenLight.color); // SelectObject(m_GContext.m_hDCTmp, m_bDown ? m_GContext.m_hPenShadow : m_GContext.m_hPenLight );
		g.drawLine((int)m_pRegion[1].x, (int)m_pRegion[1].y, (int)m_pRegion[2].x, (int)m_pRegion[2].y);
		g.setColor(m_bDown ? m_GContext.m_hPenLight.color  : m_GContext.m_hPenShadow.color); // SelectObject(m_GContext.m_hDCTmp, m_bDown ? m_GContext.m_hPenLight  : m_GContext.m_hPenShadow);
		g.drawLine((int)m_pRegion[2].x, (int)m_pRegion[2].y, (int)m_pRegion[3].x, (int)m_pRegion[3].y);
		g.drawLine((int)m_pRegion[3].x, (int)m_pRegion[3].y, (int)m_pRegion[0].x, (int)m_pRegion[0].y);
		g.setColor(m_bDown ? m_GContext.m_hPenShadow.color : m_GContext.m_hPenLight.color); // SelectObject(m_GContext.m_hDCTmp, m_bDown ? m_GContext.m_hPenShadow : m_GContext.m_hPenLight );
		g.drawLine((int)m_pRegion[0].x, (int)m_pRegion[0].y, (int)m_pRegion[1].x, (int)m_pRegion[1].y);

		//TODO доделать
//		::BitBltMasked(
//				m_GContext.m_hDCDst, GetOuterRect(),
//				m_GContext.m_hDCTmp, GetOuterRect().pointLT(),
//				m_pRegion, 4, GetOuterRect().pointLT());
	}
}
