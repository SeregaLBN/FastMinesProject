////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "Square2.cpp"
//
// Реализация класса CSquare2 - квадрат (перекошенный вариант поля)
////////////////////////////////////////////////////////////////////////////////

#include "StdAfx.h"
#include "Square2.h"

////////////////////////////////////////////////////////////////////////////////
//                               static function
////////////////////////////////////////////////////////////////////////////////

SIZE nsCell::CSquare2::GetSizeInPixel(const SIZE &sizeField, int iArea) {
   m_a = sqrt(iArea); // размер стороны квадрата
   SIZE result = {sizeField.cx * m_a + m_a/2,
                  sizeField.cy * m_a        };
   return result;
}

int nsCell::CSquare2::SizeInscribedSquare(int iArea, int iBorderWidth) {
   m_sq = sqrt(iArea)-2*iBorderWidth;
   return m_sq;
}

////////////////////////////////////////////////////////////////////////////////
//                               virtual function
////////////////////////////////////////////////////////////////////////////////

nsCell::CSquare2::CSquare2(const COORD &Coord, const SIZE &sizeField, int iArea, const CGraphicContext &gContext)
   : CBase(Coord, sizeField, iArea, gContext,
           6, 4,
           Coord.Y&1 // 0..1
          )
{
   SetPoint(iArea);
   // определяю координаты соседей
   m_pNeighbor[0].X = m_Coord.X-(m_iDirection?1:0); m_pNeighbor[0].Y = m_Coord.Y-1;
   m_pNeighbor[1].X = m_Coord.X+(m_iDirection?0:1); m_pNeighbor[1].Y = m_Coord.Y-1;
   m_pNeighbor[2].X = m_Coord.X-1;                  m_pNeighbor[2].Y = m_Coord.Y;
   m_pNeighbor[3].X = m_Coord.X+1;                  m_pNeighbor[3].Y = m_Coord.Y;
   m_pNeighbor[4].X = m_Coord.X-(m_iDirection?1:0); m_pNeighbor[4].Y = m_Coord.Y+1;
   m_pNeighbor[5].X = m_Coord.X+(m_iDirection?0:1); m_pNeighbor[5].Y = m_Coord.Y+1;
   VerifyNeighbor(sizeField);
}

bool nsCell::CSquare2::PointInRegion(const POINT &point) const {
   if ((point.x < m_pRegion[1].x) || (point.x >= m_pRegion[0].x) ||
       (point.y < m_pRegion[0].y) || (point.y >= m_pRegion[2].y))
      return false;
   return true;
}

void nsCell::CSquare2::SetPoint(int iArea) {
   if (m_Coord.X==0 && m_Coord.Y==0) {
      m_a  = sqrt(iArea); // размер стороны квадрата
      m_sq = m_a-2*m_GContext.m_Border.m_iWidth;      // размер квадрата, вписанного в квадрат
   }

   // определение координат точек фигуры
   m_pRegion[1].x = m_pRegion[2].x = m_a*(m_Coord.X+0) + (m_iDirection ? 0 : m_a/2);
   m_pRegion[0].x = m_pRegion[3].x = m_a*(m_Coord.X+1) + (m_iDirection ? 0 : m_a/2);

   m_pRegion[0].y = m_pRegion[1].y = m_a*(m_Coord.Y+0);
   m_pRegion[2].y = m_pRegion[3].y = m_a*(m_Coord.Y+1);

   // определение координат вписанного в фигуру квадрата - область в которую выводится изображение/текст
   m_Square.left   = m_pRegion[1].x + m_GContext.m_Border.m_iWidth;
   m_Square.top    = m_pRegion[1].y + m_GContext.m_Border.m_iWidth;
   m_Square.right  = m_Square.left + m_sq;
   m_Square.bottom = m_Square.top  + m_sq;
}

void nsCell::CSquare2::Paint() const {
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
