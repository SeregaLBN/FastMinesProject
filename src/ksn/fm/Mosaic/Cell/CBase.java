package ksn.fm.Mosaic.Cell;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

import ksn.CommonLib.Geometry;
import ksn.fm.Mosaic.Types.Color;
import ksn.fm.Mosaic.Types.HPEN;
import ksn.fm.Mosaic.Types.clickResult.CLeftUpReturn;
import ksn.fm.Mosaic.Types.clickResult.CRightDownReturn;
import ksn.utils.Types.GraphPrimitives.*;

public abstract class CBase {
	public static final CoordEx INCORRECT_COORD = new CoordEx((short)-1, (short)-1);

	public static enum EStatus{_Open, _Close};
	public static enum EClose {_Unknown, _Clear, _Flag};
	public static enum EOpen  {_Nil, _1, _2, _3, _4, _5, _6, _7, _8, _9, _10, _11, _12, _13, _14, _15, _16, _17, _18, _19, _20, _21, _Mine;
		public static EOpen valueOf(int val)
		{
			for(EOpen msg: EOpen.values())
				if(msg.ordinal() == val)
					return msg;
			throw new IllegalArgumentException("enum EOpen("+val+") not found");
		}
	};

	public class CColorText {
		public final Color[] m_colorOpen  = new Color[CBase.EOpen .values().length];
	    public final Color[] m_colorClose = new Color[CBase.EClose.values().length];
	    @Override
	    protected Object clone() {//throws CloneNotSupportedException {
	    	CColorText ct = new CColorText();
	    	for (int i=0; i<CBase.EOpen .values().length; i++)
	    		ct.m_colorOpen [i] = new Color(this.m_colorOpen [i]);
	    	for (int i=0; i<CBase.EClose.values().length; i++)
	    		ct.m_colorClose[i] = new Color(this.m_colorClose[i]);
	    	return ct;
	    }
	}

	public class CBorder{
		public Color m_colorShadow;
		public Color m_colorLight;
		public int m_iWidth;
		public CBorder()
		{
			m_colorShadow = new Color(0x00000000);
			m_colorLight  = new Color(0x00FFFFFF);
			m_iWidth      = 1;
		}
		public CBorder(Color colorShadow,
		               Color colorLight,
		               int iWidth)
		{
			m_colorShadow = new Color(colorShadow);
			m_colorLight  = new Color(colorLight );
			m_iWidth      = iWidth;
		}
	}

	public class CClickReportContext {
		public Set<CBase> m_SetOpenNil; // множество ячеек (нулевых  ) открытых           при последнем клике
		public Set<CBase> m_SetOpen;    // множество ячеек (ненулевых) открытых           при последнем клике
		public Set<CBase> m_SetFlag;    // множество ячеек с флажками  снятых/уставленных при последнем клике
		public CClickReportContext() {
			m_SetOpenNil = new HashSet<CBase>();
			m_SetOpen    = new HashSet<CBase>();
			m_SetFlag    = new HashSet<CBase>();
		}
	}

	public class CGraphicContext {
		public BufferedImage m_hDCDst;
		public BufferedImage m_hDCBck;
		public BufferedImage m_hDCTmp;
		public HPEN       m_hPenShadow;
		public HPEN       m_hPenLight;
		public Image     m_ImgMine;
		public Image     m_ImgFlag;
		public CColorText m_ColorText;
		public CBorder    m_Border;
		public CGraphicContext(CColorText colorText) {
			m_hDCDst     =
			m_hDCBck     =
			m_hDCTmp     = null;
			m_hPenShadow =
			m_hPenLight  = null;
			m_ColorText  = (CColorText) colorText.clone();
		}
	};

	private boolean m_bPresumeFlag;

	protected static float m_a ; // базовая величина фигуры (обычно это размер одной из сторон фигуры)
	protected static float m_sq; // размер квадрата, вписанного в фигуру

	public static Size GetSizeInPixel(Size sizeField, int iArea) {
		return new Size(200, 100);
	}
	public static int SizeInscribedSquare(int iArea, int iBorderWidth) { // по площади ячейки определить размер вписанного в фигуру квадрата
		return 7; // говорят, 7 счастливое число... :)
	}
	public static String GetCaptionOpen(EOpen eOpen) {
		switch (eOpen) {
		case _Nil : return " ";
		case _1   : return "1";
		case _2   : return "2";
		case _3   : return "3";
		case _4   : return "4";
		case _5   : return "5";
		case _6   : return "6";
		case _7   : return "7";
		case _8   : return "8";
		case _9   : return "9";
		case _10  : return "10";
		case _11  : return "11";
		case _12  : return "12";
		case _13  : return "13";
		case _14  : return "14";
		case _15  : return "15";
		case _16  : return "16";
		case _17  : return "17";
		case _18  : return "18";
		case _19  : return "19";
		case _20  : return "20";
		case _21  : return "21";
		case _Mine: return "M";
		}
		throw new java.lang.IllegalArgumentException();
	}
	public static String GetCaptionClose(EClose eClose) {
		switch (eClose) {
		case _Unknown: return "?";
		case _Clear  : return " ";
		case _Flag   : return "F";
		}
		throw new java.lang.IllegalArgumentException();
	}

	protected Coord   m_Coord;
	protected RectEx  m_Square;    // вписанный в фигуру квадрат - область в которую выводится изображение/текст
	protected RectEx  m_rcOuter;
	protected CGraphicContext m_GContext;
	protected boolean m_bDown; // Нажата? Не путать с m_Cell.m_Open! - ячейка может быть нажата, но ещё не открыта. Важно только для ф-ции прорисовки

	protected Coord[] m_pNeighbor;  // массив кординат соседей
	protected Point[] m_pRegion;    // массив координат точек из которых состоит фигура
	protected int     m_iDirection; // направление - "третья координата" ячейки

	final private class Cell{
		public EStatus m_Status;
		public EOpen   m_Open;
		public EClose  m_Close;
	}
	private Cell m_Cell;
	private boolean m_bLockMine; // блокировать ли возможность установки на данную ячейку мины?
	private int m_iNeighborNumber; // количество соседей
	private int m_iVertexNumber;   // количество вершин у фигуры

	private CBase[] m_ppLinkNeighbor;

	protected void VerifyNeighbor(Size sizeField) {
		for (int i=0; i<m_iNeighborNumber; i++)
			if ((m_pNeighbor[i].X >= sizeField.cx) ||
					(m_pNeighbor[i].Y >= sizeField.cy) ||
					(m_pNeighbor[i].X < 0) ||
					(m_pNeighbor[i].Y < 0))
			{
				m_pNeighbor[i] = INCORRECT_COORD;
			}
	}

	private void Lock() {
		m_bLockMine = true;
	}

	public void Cell_SetDown(boolean bDown) {
		m_bDown = bDown;
	}
	public void Cell_SetStatus(EStatus eStatus, CClickReportContext pClickRepContext) {
		if (null != pClickRepContext) {
			if (eStatus == EStatus._Open) {
				if (m_Cell.m_Open == EOpen._Nil)
					pClickRepContext.m_SetOpenNil.add(this);
				else
					pClickRepContext.m_SetOpen.add(this);
			}
		}
		m_Cell.m_Status = eStatus;
	}
	public EStatus Cell_GetStatus() {
		return m_Cell.m_Status;
	}
	public void Cell_DefineValue() {
		if (m_Cell.m_Open == EOpen._Mine) return;
		// подсчитать у соседей число мин и установить значение
		int count = 0;
		for (int i=0; i<m_iNeighborNumber; i++) {
			if (null == m_ppLinkNeighbor[i]) continue; // существует ли сосед?
			if (m_ppLinkNeighbor[i].Cell_GetOpen() == EOpen._Mine) count++;
		}
		m_Cell.m_Open = EOpen.valueOf(count);
	}
	public boolean Cell_SetMine() {
		if (m_bLockMine || (m_Cell.m_Open == EOpen._Mine)) return false;
		m_Cell.m_Open = EOpen._Mine;
		return true;
	}
	public EOpen Cell_GetOpen() {
		return m_Cell.m_Open;
	}
	public void Cell_SetClose(EClose Close, CClickReportContext pClickRepContext) {
		if (null != pClickRepContext) {
			if ((         Close == EClose._Flag) || // если устанавливаю флажок
			    (m_Cell.m_Close == EClose._Flag))   // если снимаю флажок
			{
				pClickRepContext.m_SetFlag.add(this);
			}
		}
		m_Cell.m_Close = Close;

	}
	public EClose Cell_GetClose() {
		return m_Cell.m_Close;
	}

	public CBase(
		Coord coord,
		Size sizeField,
		int iArea,
		CGraphicContext gContext,
		int iNeighborNumber,
		int iVertexNumber,
		int iDirection)
	{
		m_Coord           = coord;
		m_GContext        = gContext;
		m_iNeighborNumber = iNeighborNumber;
		m_iVertexNumber   = iVertexNumber;
		m_iDirection      = iDirection;
		m_pNeighbor       = new Coord[iNeighborNumber];
		m_pRegion         = new Point[iVertexNumber];
		m_ppLinkNeighbor  = null;
		Reset();
	}
	public void LockNeighbor() {
		m_bLockMine = true;
		// запретить установку мин у соседей
		for (int i=0; i<m_iNeighborNumber; i++) {
			if (null == m_ppLinkNeighbor[i]) continue; // существует ли сосед?
			m_ppLinkNeighbor[i].Lock();
		}
	}
	public void SetNeighborLink(CBase [] ppLinkNeighbor, int numberNeighbor) {
		m_ppLinkNeighbor = new CBase[numberNeighbor];
		for (int i=0; i<numberNeighbor; i++)
			m_ppLinkNeighbor[i] = ppLinkNeighbor[i];

	}
	public int GetNeighborNumber() { // определить количество соседей
		return m_iNeighborNumber;
	}
	public Coord GetNeighborCoord(int index) {
		return (index<m_iNeighborNumber) ? m_pNeighbor[index] : INCORRECT_COORD;
	}
	public Coord GetCoord() { // X и Y ячейки
		return m_Coord;
	}
	public Point GetCenterPixel() { // координата центра фигуры (в пикселях)
		return new Point(m_Square.left + (m_Square.right -m_Square.left)/2,
		                 m_Square.top  + (m_Square.bottom-m_Square.top )/2);
	}
	public /*abstract*/ boolean PointInRegion(Point point) { // принадлежат ли эти экранные координаты ячейке
		return Geometry.PointInPolygon(point, m_pRegion, m_iVertexNumber);
	}
	public RectEx GetOuterRect() { return m_rcOuter; } // вернёт прямоугольник в который вписана фигура ячеёки
	public /*abstract*/ void SetPoint(int iArea) { // определить координаты точек из которых состоит фигура
		// Должно вызваться после пересчёта координат точек!
		// пересчитываю координаты описывающего прямоугольника
		if (m_iVertexNumber>0) {
			m_rcOuter.left = m_rcOuter.right  = m_pRegion[0].x;
			m_rcOuter.top  = m_rcOuter.bottom = m_pRegion[0].y;
			for (int i=1; i<m_iVertexNumber; i++) {
				if (m_rcOuter.left   > m_pRegion[i].x) m_rcOuter.left   = m_pRegion[i].x;
				if (m_rcOuter.top    > m_pRegion[i].y) m_rcOuter.top    = m_pRegion[i].y;
				if (m_rcOuter.right  < m_pRegion[i].x) m_rcOuter.right  = m_pRegion[i].x;
				if (m_rcOuter.bottom < m_pRegion[i].y) m_rcOuter.bottom = m_pRegion[i].y;
			}
		}
	}
	public void Reset() {
		m_Cell.m_Status = EStatus._Close;
		m_Cell.m_Open   = EOpen._Nil;
		m_Cell.m_Close  = EClose._Clear;
		m_bLockMine = false;
		m_bDown = false;
	}
	public /*abstract*/ void Paint() {
		Graphics2D g = m_GContext.m_hDCTmp.createGraphics();
		g.drawImage(
			m_GContext.m_hDCBck,
			(int)m_Square.left,
			(int)m_Square.top,
			(int)m_Square.width(),
			(int)m_Square.height(),
			(int)m_Square.left,
			(int)m_Square.top,
			(int)m_Square.width(),
			(int)m_Square.height(), null);

		// output Pictures
		if ((m_Cell.m_Status == EStatus._Close) && /**/(m_Cell.m_Close == EClose._Flag) && (null != m_GContext.m_ImgFlag))
			g.drawImage(
				m_GContext.m_ImgFlag,
				(int)m_Square.left,
				(int)m_Square.top,
				(int)m_Square.width(),
				(int)m_Square.height(), null);
		else
			if ((m_Cell.m_Status == EStatus._Open ) && /**/(m_Cell.m_Open  == EOpen._Mine) && (null != m_GContext.m_ImgMine))
				g.drawImage(
					m_GContext.m_ImgMine,
					(int)m_Square.left,
					(int)m_Square.top,
					(int)m_Square.width(),
					(int)m_Square.height(), null);
			else // output text
			{
				String szCaption;
				if (m_Cell.m_Status == EStatus._Close) {
					g.setPaint(m_GContext.m_ColorText.m_colorClose[m_Cell.m_Close.ordinal()]);
					szCaption = CBase.GetCaptionClose(m_Cell.m_Close);
				} else {
					g.setPaint(m_GContext.m_ColorText.m_colorClose[m_Cell.m_Open.ordinal()]);
					szCaption = CBase.GetCaptionOpen(m_Cell.m_Open);
				}
				RectEx sq_tmp = new RectEx(
					m_Square.left   + (m_bDown ? 1 : 0),
					m_Square.top    + (m_bDown ? 1 : 0),
					m_Square.right  + (m_bDown ? 1 : 0),
					m_Square.bottom + (m_bDown ? 1 : 0));
				Shape shape = g.getClip();
				g.setClip((int)sq_tmp.left, (int)sq_tmp.top, (int)sq_tmp.width(), (int)sq_tmp.height());
				g.drawString(szCaption, sq_tmp.left, sq_tmp.top);
//				DrawText(m_GContext.m_hDCTmp, szCaption,
//						-1, &sq_tmp, DT_CENTER | DT_VCENTER | DT_SINGLELINE);
				g.setClip(shape);
			}
	}
	public void LButtonDown() {
		if (m_Cell.m_Close  == EClose._Flag) return;
		if (m_Cell.m_Status == EStatus._Close) {
			m_bDown = true;
			Paint();
			return;
		}
		// эффект нажатости для неоткрытых соседей
		if ((m_Cell.m_Status == EStatus._Open) && (m_Cell.m_Open != EOpen._Nil))
			for (int i=0; i<m_iNeighborNumber; i++) {
				if (null == m_ppLinkNeighbor[i]) continue; // существует ли сосед?
				if ((m_ppLinkNeighbor[i].Cell_GetStatus() == EStatus._Open) ||
				    (m_ppLinkNeighbor[i].Cell_GetClose()  == EClose._Flag)) continue;
				m_ppLinkNeighbor[i].Cell_SetDown(true);
				m_ppLinkNeighbor[i].Paint();
			}
	}
	public CLeftUpReturn LButtonUp(boolean isMy, CClickReportContext pClickRepContext) {
		CLeftUpReturn result = new CLeftUpReturn(0, 0, 0, false, false);

		if (m_Cell.m_Close == EClose._Flag) return result;
		// избавится от эффекта нажатости
		if ((m_Cell.m_Status == EStatus._Open) && (m_Cell.m_Open != EOpen._Nil))
			for (int i=0; i<m_iNeighborNumber; i++) {
				if (null == m_ppLinkNeighbor[i]) continue; // существует ли сосед?
				if ((m_ppLinkNeighbor[i].Cell_GetStatus() == EStatus._Open) ||
				    (m_ppLinkNeighbor[i].Cell_GetClose()  == EClose._Flag)) continue;
				m_ppLinkNeighbor[i].Cell_SetDown(false);
				m_ppLinkNeighbor[i].Paint();
			}
		// Открыть закрытую ячейку на которой нажали
		if (m_Cell.m_Status == EStatus._Close) {
			if (!isMy) {
				m_bDown = false;
				Paint();
				return result;
			} else {
				result.m_iCountUnknown += (m_Cell.m_Close == EClose._Unknown) ? -1 : 0;
				result.m_iCountOpen++;
				Cell_SetStatus(EStatus._Open, pClickRepContext);
				m_bDown = true;
				Paint();
			}
		}
		// ! В этой точке ячейка уже открыта
		// Подсчитываю кол-во установленных вокруг флагов и не открытых ячеек
		int countFlags = 0;
		int countClear = 0;
		if (m_Cell.m_Open != EOpen._Nil)
			for (int i=0; i<m_iNeighborNumber; i++) {
				if (null == m_ppLinkNeighbor[i]) continue; // существует ли сосед?
				if (m_ppLinkNeighbor[i].Cell_GetStatus() == EStatus._Open) continue;
				if (m_ppLinkNeighbor[i].Cell_GetClose()  == EClose._Flag)
					countFlags++;
				else countClear++;
			}
		// оставшимся установить флаги
		if ((m_Cell.m_Open != EOpen._Nil) && (countFlags+countClear == m_Cell.m_Open.ordinal()))
			for (int i=0; i<m_iNeighborNumber; i++) {
				if (null == m_ppLinkNeighbor[i]) continue; // существует ли сосед?
				if ((m_ppLinkNeighbor[i].Cell_GetStatus() == EStatus._Open) ||
				    (m_ppLinkNeighbor[i].Cell_GetClose()  == EClose._Flag)) continue;
				result.m_iCountUnknown += (m_ppLinkNeighbor[i].Cell_GetClose() == EClose._Unknown) ? -1 : 0;
				result.m_iCountFlag++;
				m_ppLinkNeighbor[i].Cell_SetClose(EClose._Flag, pClickRepContext);
				m_ppLinkNeighbor[i].Paint();
			}
		if (!isMy) return result;
		// открыть оставшиеся
		if ((countFlags+result.m_iCountFlag) == m_Cell.m_Open.ordinal())
			for (int i=0; i<m_iNeighborNumber; i++) {
				if (null == m_ppLinkNeighbor[i]) continue; // существует ли сосед?
				if ((m_ppLinkNeighbor[i].Cell_GetStatus() == EStatus._Open) ||
				    (m_ppLinkNeighbor[i].Cell_GetClose()  == EClose._Flag)) continue;
				result.m_iCountUnknown += (m_ppLinkNeighbor[i].Cell_GetClose() == EClose._Unknown) ? -1 : 0;
				result.m_iCountOpen++;
				m_ppLinkNeighbor[i].Cell_SetDown(true);
				m_ppLinkNeighbor[i].Cell_SetStatus(EStatus._Open, pClickRepContext);
				m_ppLinkNeighbor[i].Paint();
				if (m_ppLinkNeighbor[i].Cell_GetOpen() == EOpen._Nil) {
					CLeftUpReturn result2 = m_ppLinkNeighbor[i].LButtonUp(true, pClickRepContext);
					result.m_iCountFlag    += result2.m_iCountFlag;
					result.m_iCountOpen    += result2.m_iCountOpen;
					result.m_iCountUnknown += result2.m_iCountUnknown;
					if (result.m_bEndGame) {
						result.m_bEndGame = result2.m_bEndGame;
						result.m_bVictory = result2.m_bVictory;
					}
				}
				if (m_ppLinkNeighbor[i].Cell_GetOpen() == EOpen._Mine) {
					result.m_bEndGame = true;
					result.m_bVictory = false;
					return result;
				}
			}
		if (m_Cell.m_Open == EOpen._Mine) {
			result.m_bEndGame = true;
			result.m_bVictory = false;
		}
		return result;
	}
	public CRightDownReturn RButtonDown(EClose eClose, CClickReportContext pClickRepContext) {
		CRightDownReturn result = new CRightDownReturn(0,0);

		if ((m_Cell.m_Status == EStatus._Open) || m_bDown) return result;
		switch (m_Cell.m_Close) {
		case _Clear:
			switch (eClose) {
			case _Flag:    result.m_iCountFlag    = +1;  break;
			case _Unknown: result.m_iCountUnknown = +1;
			}
			if (m_Cell.m_Close != eClose) Cell_SetClose(eClose, pClickRepContext);
			break;
		case _Flag:
			switch (eClose) {
			case _Unknown: result.m_iCountUnknown = +1;
			case _Clear:   result.m_iCountFlag    = -1;
			}
			if (m_Cell.m_Close != eClose) Cell_SetClose(eClose, pClickRepContext);
			break;
		case _Unknown:
			switch (eClose) {
			case _Flag:    result.m_iCountFlag    = +1;
			case _Clear:   result.m_iCountUnknown = -1;
			}
			if (m_Cell.m_Close != eClose) Cell_SetClose(eClose, pClickRepContext);
			break;
		}
		Paint();
		return result;
	}

	public void    SetPresumeFlag(boolean bValue) { m_bPresumeFlag = bValue; }
	public boolean GetPresumeFlag()               { return m_bPresumeFlag; }

	protected static double SQRT2   = Math.sqrt(2.);
	protected static double SQRT3   = Math.sqrt(3.);
	protected static double SQRT27  = Math.sqrt(27.);
	protected static double SQRT48  = Math.sqrt(48.);
	protected static double SQRT147 = Math.sqrt(147.);
	protected static double PI      = Math.PI;//3.14159265358979323;
	protected static double SIN75   = Math.sin(PI/180*75);
	protected static double SIN15   = Math.sin(PI/180*15);
	protected static double TAN15   = Math.tan(PI/180*15);
	protected static double TAN45_2 = Math.tan(PI/180*45/2);
	protected static double SIN135a = Math.sin(PI/180*135-Math.atan(8.f/3));
}