package ksn.fm.Mosaic;

import java.awt.Font;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import ksn.fm.Mosaic.Cell.CBase;
import ksn.fm.Mosaic.Cell.CBase.EClose;
import ksn.fm.Mosaic.Cell.CBase.EOpen;
import ksn.fm.Mosaic.Types.Color;

/**
 * класс скина. только для мозаики
 */
public class CSkinMosaic implements Externalizable {
	public static final Font DefaultFont = new Font(Font.SANS_SERIF, Font.BOLD, 12);

	public static final CBase.CColorText DefaultTextColor;
	
	{
		DefaultTextColor = new CBase.CColorText();
		                                                                 // RRGGBB
		DefaultTextColor.m_colorOpen[EOpen._Nil .ordinal()] = new Color(0x00000000); // Black
		DefaultTextColor.m_colorOpen[EOpen._1   .ordinal()] = new Color(0x00000080); // Navy
		DefaultTextColor.m_colorOpen[EOpen._2   .ordinal()] = new Color(0x00008000); // Green
		DefaultTextColor.m_colorOpen[EOpen._3   .ordinal()] = new Color(0x00FF0000); // Red
		DefaultTextColor.m_colorOpen[EOpen._4   .ordinal()] = new Color(0x00800000); // Maroon
		DefaultTextColor.m_colorOpen[EOpen._5   .ordinal()] = new Color(0x000000FF); // Blue
		DefaultTextColor.m_colorOpen[EOpen._6   .ordinal()] = new Color(0x00000000); // Black
		DefaultTextColor.m_colorOpen[EOpen._7   .ordinal()] = new Color(0x00808000); // Olive
		DefaultTextColor.m_colorOpen[EOpen._8   .ordinal()] = new Color(0x0000FFFF); // Aqua
		DefaultTextColor.m_colorOpen[EOpen._9   .ordinal()] = new Color(0x00000080); // Navy
		DefaultTextColor.m_colorOpen[EOpen._10  .ordinal()] = new Color(0x00008000); // Green
		DefaultTextColor.m_colorOpen[EOpen._11  .ordinal()] = new Color(0x00FF0000); // Red
		DefaultTextColor.m_colorOpen[EOpen._12  .ordinal()] = new Color(0x00800000); // Maroon
		DefaultTextColor.m_colorOpen[EOpen._13  .ordinal()] = new Color(0x00000080); // Navy
		DefaultTextColor.m_colorOpen[EOpen._14  .ordinal()] = new Color(0x00008000); // Green
		DefaultTextColor.m_colorOpen[EOpen._15  .ordinal()] = new Color(0x00FF0000); // Red
		DefaultTextColor.m_colorOpen[EOpen._16  .ordinal()] = new Color(0x00800000); // Maroon
		DefaultTextColor.m_colorOpen[EOpen._17  .ordinal()] = new Color(0x000000FF); // Blue
		DefaultTextColor.m_colorOpen[EOpen._18  .ordinal()] = new Color(0x00000000); // Black
		DefaultTextColor.m_colorOpen[EOpen._19  .ordinal()] = new Color(0x00808000); // Olive
		DefaultTextColor.m_colorOpen[EOpen._20  .ordinal()] = new Color(0x0000FFFF); // Aqua
		DefaultTextColor.m_colorOpen[EOpen._21  .ordinal()] = new Color(0x00000080); // Navy
		DefaultTextColor.m_colorOpen[EOpen._Mine.ordinal()] = new Color(0x00000000); // Black
		                                                                      // RRGGBB
		DefaultTextColor.m_colorClose[EClose._Unknown.ordinal()] = new Color(0x00008080); // Teal
		DefaultTextColor.m_colorClose[EClose._Clear  .ordinal()] = new Color(0x00000000); // Black
		DefaultTextColor.m_colorClose[EClose._Flag   .ordinal()] = new Color(0x00FF0000); // Red
	}
	public:
	   CImgInfo          m_ImgMine, m_ImgFlag, m_ImgBckgrnd;
	   LOGFONT           m_Font;
	   CCell::CColorText m_ColorText;
	   CCell::CBorder    m_Border;
	private:
	   COLORREF          m_colorBk;
	private:
	   void copy(const CSkinMosaic& skin, bool ignore);
	public:
	   CSkinMosaic();
	   COLORREF GetBkColor() const { return (m_colorBk==CLR_INVALID) ? ::GetSysColor(COLOR_BTNFACE) : m_colorBk; }
	   void     SetBkColor(COLORREF clr) { m_colorBk = (clr==::GetSysColor(COLOR_BTNFACE)) ? CLR_INVALID : clr; }

	   const CSkinMosaic& operator=(const CSkinMosaic&);

	   // implement ISerialize
	   BOOL SerializeRead(HANDLE hFile);
	   BOOL SerializeWrite(HANDLE hFile) const;

	   BOOL IsDefaultFont() const;

	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		// TODO Auto-generated method stub
	}
	public void writeExternal(ObjectOutput out) throws IOException {
		// TODO Auto-generated method stub
	}
}
