////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "Parquet1.cpp"
//
// Реализация класса CParquet1 - паркет в елку (herring-bone parquet)
////////////////////////////////////////////////////////////////////////////////

#include "StdAfx.h"
#include "Parquet1.h"

////////////////////////////////////////////////////////////////////////////////
//                               static function
////////////////////////////////////////////////////////////////////////////////

SIZE nsCell::CParquet1::GetSizeInPixel(const COORD& sizeField, int area) {
   m_a = (int)sqrt(area)/2;
   SIZE result = {m_a*(sizeField.X*2+1),
                   m_a*(sizeField.Y*2+2)};
   return result;
}

int nsCell::CParquet1::SizeInscribedSquare(int area, int borderWidth) {
   m_a = (int)sqrt(area)/2;
   m_sq = m_a-borderWidth*SQRT2;
   return m_sq;
}

////////////////////////////////////////////////////////////////////////////////
//                               virtual function
////////////////////////////////////////////////////////////////////////////////

nsCell::CParquet1::CParquet1(const COORD& Coord, const COORD& sizeField, int area, const CGraphicContext& gContext)
   : CBase(Coord, sizeField, area, gContext,
           6, 4,
           Coord.X&1 // 0..1
          )
{
   SetPoint(area);
   // определяю координаты соседей
   m_pNeighbor[0].X = m_iDirection ? m_Coord.X   : m_Coord.X-1;
   m_pNeighbor[0].Y = m_Coord.Y-1;
   m_pNeighbor[1].X = m_iDirection ? m_Coord.X-1 : m_Coord.X  ;
   m_pNeighbor[1].Y = m_iDirection ? m_Coord.Y   : m_Coord.Y-1;
   m_pNeighbor[2].X = m_Coord.X+1;
   m_pNeighbor[2].Y = m_iDirection ? m_Coord.Y   : m_Coord.Y-1;
   m_pNeighbor[3].X = m_Coord.X-1;
   m_pNeighbor[3].Y = m_iDirection ? m_Coord.Y+1 : m_Coord.Y  ;
   m_pNeighbor[4].X = m_iDirection ? m_Coord.X   : m_Coord.X+1;
   m_pNeighbor[4].Y = m_iDirection ? m_Coord.Y+1 : m_Coord.Y  ;
   m_pNeighbor[5].X = m_iDirection ? m_Coord.X+1 : m_Coord.X  ;
   m_pNeighbor[5].Y = m_Coord.Y+1;
   VerifyNeighbor(sizeField);
}

void nsCell::CParquet1::SetPoint(int area) {
   if (m_Coord.X==0 && m_Coord.Y==0) {
      m_a = (int)sqrt(area)/2; // размер стороны вписанного квадрата
      m_sq = m_a-m_GContext.m_Border.m_iWidth*SQRT2;
   }

   // определение координат точек фигуры
   switch (m_iDirection) {
   case 0:
      m_pRegion[0].x = m_a*(2+2*m_Coord.X);  m_pRegion[0].y = m_a*(0+2*m_Coord.Y);
      m_pRegion[1].x = m_a*(0+2*m_Coord.X);  m_pRegion[1].y = m_a*(2+2*m_Coord.Y);
      m_pRegion[2].x = m_a*(1+2*m_Coord.X);  m_pRegion[2].y = m_a*(3+2*m_Coord.Y);
      m_pRegion[3].x = m_a*(3+2*m_Coord.X);  m_pRegion[3].y = m_a*(1+2*m_Coord.Y);
      break;
   case 1:
      m_pRegion[0].x = m_a*(1+2*m_Coord.X);  m_pRegion[0].y = m_a*(1+2*m_Coord.Y);
      m_pRegion[1].x = m_a*(0+2*m_Coord.X);  m_pRegion[1].y = m_a*(2+2*m_Coord.Y);
      m_pRegion[2].x = m_a*(2+2*m_Coord.X);  m_pRegion[2].y = m_a*(4+2*m_Coord.Y);
      m_pRegion[3].x = m_a*(3+2*m_Coord.X);  m_pRegion[3].y = m_a*(3+2*m_Coord.Y);
      break;
   }

   // определение координат вписанного в фигуру квадрата - область в которую выводится изображение/текст
   {
      m_Square.left   = (m_iDirection ? m_pRegion[0].x : m_pRegion[2].x) + m_GContext.m_Border.m_iWidth/SQRT2;
      m_Square.top    = (m_iDirection ? m_pRegion[1].y : m_pRegion[3].y) + m_GContext.m_Border.m_iWidth/SQRT2;
      m_Square.right  = m_Square.left+m_sq;
      m_Square.bottom = m_Square.top +m_sq;
   }

}

void nsCell::CParquet1::Paint() const {
   BitBlt(m_GContext.m_hDCTmp,
      m_pRegion[1].x,
      m_pRegion[0].y,
      m_pRegion[3].x - m_pRegion[1].x,
      m_pRegion[2].y - m_pRegion[0].y,
      m_GContext.m_hDCBck,
      m_pRegion[1].x,
      m_pRegion[0].y,
      SRCCOPY);
   CBase::Paint();

   SelectObject(m_GContext.m_hDCTmp, m_bDown ? m_GContext.m_hPenShadow : m_GContext.m_hPenLight );
   MoveToEx(m_GContext.m_hDCTmp, m_pRegion[1].x, m_pRegion[1].y, NULL);
   LineTo  (m_GContext.m_hDCTmp, m_pRegion[2].x, m_pRegion[2].y);
   SelectObject(m_GContext.m_hDCTmp, m_bDown ? m_GContext.m_hPenLight  : m_GContext.m_hPenShadow);
   LineTo  (m_GContext.m_hDCTmp, m_pRegion[3].x, m_pRegion[3].y);
   LineTo  (m_GContext.m_hDCTmp, m_pRegion[0].x, m_pRegion[0].y);
   SelectObject(m_GContext.m_hDCTmp, m_bDown ? m_GContext.m_hPenShadow : m_GContext.m_hPenLight );
   LineTo  (m_GContext.m_hDCTmp, m_pRegion[1].x, m_pRegion[1].y);

   RegionDraw(
      m_GContext.m_hDCDst, true,
      m_pRegion, 4, NULL, NULL, 0, 0, 0, 0,
      m_GContext.m_hDCTmp, NULL, 0,0);
}
