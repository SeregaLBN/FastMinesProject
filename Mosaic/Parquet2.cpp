////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "Parquet2.cpp"
//
// Реализация класса CParquet2 - паркет (тип №2)
////////////////////////////////////////////////////////////////////////////////

#include "StdAfx.h"
#include "Parquet2.h"

////////////////////////////////////////////////////////////////////////////////
//                               static function
////////////////////////////////////////////////////////////////////////////////

SIZE nsCell::CParquet2::GetSizeInPixel(const COORD &sizeField, int area) {
   m_a = (int)sqrt(area)/2; // размер стороны вписанного квадрата
   SIZE result = {m_a*(sizeField.X*2+2),
                  m_a*(sizeField.Y*2+2)};
   return result;
}

int nsCell::CParquet2::SizeInscribedSquare(int area, int borderWidth) {
   m_a = (int)sqrt(area)/2;
   m_sq = (m_a-borderWidth*SQRT2);
   return m_sq;
}

////////////////////////////////////////////////////////////////////////////////
//                               virtual function
////////////////////////////////////////////////////////////////////////////////

nsCell::CParquet2::CParquet2(const COORD &Coord, const COORD &sizeField, int area, const CGraphicContext &gContext)
   : CBase(Coord, sizeField, area, gContext, 7, 4,
           ((Coord.Y&1)<<1) + (Coord.X&1) // 0..3
          )
{
   SetPoint(area);
   // определяю координаты соседей
   switch (m_iDirection) {
   case 0:  m_pNeighbor[0].X = m_Coord.X-1; m_pNeighbor[0].Y = m_Coord.Y-1;
            m_pNeighbor[1].X = m_Coord.X  ; m_pNeighbor[1].Y = m_Coord.Y-1;
            m_pNeighbor[2].X = m_Coord.X+1; m_pNeighbor[2].Y = m_Coord.Y-1;
            m_pNeighbor[3].X = m_Coord.X-1; m_pNeighbor[3].Y = m_Coord.Y  ;
            m_pNeighbor[4].X = m_Coord.X+1; m_pNeighbor[4].Y = m_Coord.Y  ;
            m_pNeighbor[5].X = m_Coord.X  ; m_pNeighbor[5].Y = m_Coord.Y+1;
            m_pNeighbor[6].X = m_Coord.X+1; m_pNeighbor[6].Y = m_Coord.Y+1; break;
   case 1:  m_pNeighbor[0].X = m_Coord.X  ; m_pNeighbor[0].Y = m_Coord.Y-1;
            m_pNeighbor[1].X = m_Coord.X+1; m_pNeighbor[1].Y = m_Coord.Y-1;
            m_pNeighbor[2].X = m_Coord.X-1; m_pNeighbor[2].Y = m_Coord.Y  ;
            m_pNeighbor[3].X = m_Coord.X+1; m_pNeighbor[3].Y = m_Coord.Y  ;
            m_pNeighbor[4].X = m_Coord.X-1; m_pNeighbor[4].Y = m_Coord.Y+1;
            m_pNeighbor[5].X = m_Coord.X  ; m_pNeighbor[5].Y = m_Coord.Y+1;
            m_pNeighbor[6].X = m_Coord.X+1; m_pNeighbor[6].Y = m_Coord.Y+1; break;
   case 2:  m_pNeighbor[0].X = m_Coord.X-1; m_pNeighbor[0].Y = m_Coord.Y-1;
            m_pNeighbor[1].X = m_Coord.X  ; m_pNeighbor[1].Y = m_Coord.Y-1;
            m_pNeighbor[2].X = m_Coord.X+1; m_pNeighbor[2].Y = m_Coord.Y-1;
            m_pNeighbor[3].X = m_Coord.X-1; m_pNeighbor[3].Y = m_Coord.Y  ;
            m_pNeighbor[4].X = m_Coord.X+1; m_pNeighbor[4].Y = m_Coord.Y  ;
            m_pNeighbor[5].X = m_Coord.X-1; m_pNeighbor[5].Y = m_Coord.Y+1;
            m_pNeighbor[6].X = m_Coord.X  ; m_pNeighbor[6].Y = m_Coord.Y+1; break;
   case 3:  m_pNeighbor[0].X = m_Coord.X-1; m_pNeighbor[0].Y = m_Coord.Y-1;
            m_pNeighbor[1].X = m_Coord.X  ; m_pNeighbor[1].Y = m_Coord.Y-1;
            m_pNeighbor[2].X = m_Coord.X-1; m_pNeighbor[2].Y = m_Coord.Y  ;
            m_pNeighbor[3].X = m_Coord.X+1; m_pNeighbor[3].Y = m_Coord.Y  ;
            m_pNeighbor[4].X = m_Coord.X-1; m_pNeighbor[4].Y = m_Coord.Y+1;
            m_pNeighbor[5].X = m_Coord.X  ; m_pNeighbor[5].Y = m_Coord.Y+1;
            m_pNeighbor[6].X = m_Coord.X+1; m_pNeighbor[6].Y = m_Coord.Y+1; break;
   }
   VerifyNeighbor(sizeField);
}

void nsCell::CParquet2::SetPoint(int area) {
   if (m_Coord.X==0 && m_Coord.Y==0) {
      m_a = (int)sqrt(area)/2; // размер стороны вписанного квадрата
      m_sq = (m_a-m_GContext.m_Border.m_iWidth*SQRT2);
   }

   // определение координат точек фигуры
   switch (m_iDirection) {
   case 0:
      m_pRegion[0].x = (2*m_Coord.X+2)*m_a;   m_pRegion[0].y = (2*m_Coord.Y+0)*m_a;
      m_pRegion[1].x = (2*m_Coord.X+1)*m_a;   m_pRegion[1].y = (2*m_Coord.Y+1)*m_a;
      m_pRegion[2].x = (2*m_Coord.X+3)*m_a;   m_pRegion[2].y = (2*m_Coord.Y+3)*m_a;
      m_pRegion[3].x = (2*m_Coord.X+4)*m_a;   m_pRegion[3].y = (2*m_Coord.Y+2)*m_a;
      break;
   case 1:
      m_pRegion[0].x = (2*m_Coord.X+3)*m_a;   m_pRegion[0].y = (2*m_Coord.Y+1)*m_a;
      m_pRegion[1].x = (2*m_Coord.X+1)*m_a;   m_pRegion[1].y = (2*m_Coord.Y+3)*m_a;
      m_pRegion[2].x = (2*m_Coord.X+2)*m_a;   m_pRegion[2].y = (2*m_Coord.Y+4)*m_a;
      m_pRegion[3].x = (2*m_Coord.X+4)*m_a;   m_pRegion[3].y = (2*m_Coord.Y+2)*m_a;
      break;
   case 2:
      m_pRegion[0].x = (2*m_Coord.X+2)*m_a;   m_pRegion[0].y = (2*m_Coord.Y+0)*m_a;
      m_pRegion[1].x = (2*m_Coord.X+0)*m_a;   m_pRegion[1].y = (2*m_Coord.Y+2)*m_a;
      m_pRegion[2].x = (2*m_Coord.X+1)*m_a;   m_pRegion[2].y = (2*m_Coord.Y+3)*m_a;
      m_pRegion[3].x = (2*m_Coord.X+3)*m_a;   m_pRegion[3].y = (2*m_Coord.Y+1)*m_a;
      break;
   case 3:
      m_pRegion[0].x = (2*m_Coord.X+1)*m_a;   m_pRegion[0].y = (2*m_Coord.Y+1)*m_a;
      m_pRegion[1].x = (2*m_Coord.X+0)*m_a;   m_pRegion[1].y = (2*m_Coord.Y+2)*m_a;
      m_pRegion[2].x = (2*m_Coord.X+2)*m_a;   m_pRegion[2].y = (2*m_Coord.Y+4)*m_a;
      m_pRegion[3].x = (2*m_Coord.X+3)*m_a;   m_pRegion[3].y = (2*m_Coord.Y+3)*m_a;
      break;
   }

   // определение координат вписанного в фигуру квадрата - область в которую выводится изображение/текст
   {
      switch (m_iDirection) {
      case 0: case 3:
         m_Square.left   = m_pRegion[0].x + m_GContext.m_Border.m_iWidth/SQRT2;
         m_Square.top    = m_pRegion[1].y + m_GContext.m_Border.m_iWidth/SQRT2;
         break;
      case 1: case 2:
         m_Square.left   = m_pRegion[2].x + m_GContext.m_Border.m_iWidth/SQRT2;
         m_Square.top    = m_pRegion[3].y + m_GContext.m_Border.m_iWidth/SQRT2;
         break;
      }
      m_Square.right  = m_Square.left+m_sq;
      m_Square.bottom = m_Square.top +m_sq;
   }
}

void nsCell::CParquet2::Paint() const {
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
