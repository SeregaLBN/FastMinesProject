////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "PentagonT24.cpp"
//
// –еализаци€ класса CPentagonT24 - равносторонний 5-ти угольник, тип є2 и є4
////////////////////////////////////////////////////////////////////////////////

#include "StdAfx.h"
#include "PentagonT24.h"

////////////////////////////////////////////////////////////////////////////////
//                               local init
////////////////////////////////////////////////////////////////////////////////

float nsCell::CPentagonT24::m_b;
float nsCell::CPentagonT24::m_c;

////////////////////////////////////////////////////////////////////////////////
//                               static function
////////////////////////////////////////////////////////////////////////////////

SIZE nsCell::CPentagonT24::GetSizeInPixel(const SIZE &sizeField, int iArea) {
   m_a = sqrt(iArea); // базова€ величина п€тиугольника
   m_b = m_a*6/11;
   SIZE result = {m_b + sizeField.cx*m_a,
                  m_b + sizeField.cy*m_a};
   return result;
}

int nsCell::CPentagonT24::SizeInscribedSquare(int iArea, int iBorderWidth) {
   m_sq = sqrt(iArea)*8/11-(iBorderWidth+iBorderWidth/SIN135a)/SQRT2;
   return m_sq;
}

////////////////////////////////////////////////////////////////////////////////
//                               virtual function
////////////////////////////////////////////////////////////////////////////////

nsCell::CPentagonT24::CPentagonT24(const COORD &Coord, const SIZE &sizeField, int iArea, const CGraphicContext &gContext)
   : CBase(Coord, sizeField, iArea, gContext,
           7, 5,
           ((Coord.Y&1)<<1) + (Coord.X&1) // 0..3
          )
{
   SetPoint(iArea);
   // определ€ю координаты соседей
   switch (m_iDirection) {
   case 0:  m_pNeighbor[0].X = m_Coord.X-1; m_pNeighbor[0].Y = m_Coord.Y-1;
            m_pNeighbor[1].X = m_Coord.X  ; m_pNeighbor[1].Y = m_Coord.Y-1;
            m_pNeighbor[2].X = m_Coord.X-1; m_pNeighbor[2].Y = m_Coord.Y  ;
            m_pNeighbor[3].X = m_Coord.X+1; m_pNeighbor[3].Y = m_Coord.Y  ;
            m_pNeighbor[4].X = m_Coord.X-1; m_pNeighbor[4].Y = m_Coord.Y+1;
            m_pNeighbor[5].X = m_Coord.X  ; m_pNeighbor[5].Y = m_Coord.Y+1;
            m_pNeighbor[6].X = m_Coord.X+1; m_pNeighbor[6].Y = m_Coord.Y+1; break;
   case 1:  m_pNeighbor[0].X = m_Coord.X-1; m_pNeighbor[0].Y = m_Coord.Y-1;
            m_pNeighbor[1].X = m_Coord.X  ; m_pNeighbor[1].Y = m_Coord.Y-1;
            m_pNeighbor[2].X = m_Coord.X+1; m_pNeighbor[2].Y = m_Coord.Y-1;
            m_pNeighbor[3].X = m_Coord.X-1; m_pNeighbor[3].Y = m_Coord.Y  ;
            m_pNeighbor[4].X = m_Coord.X+1; m_pNeighbor[4].Y = m_Coord.Y  ;
            m_pNeighbor[5].X = m_Coord.X-1; m_pNeighbor[5].Y = m_Coord.Y+1;
            m_pNeighbor[6].X = m_Coord.X  ; m_pNeighbor[6].Y = m_Coord.Y+1; break;
   case 2:  m_pNeighbor[0].X = m_Coord.X  ; m_pNeighbor[0].Y = m_Coord.Y-1;
            m_pNeighbor[1].X = m_Coord.X+1; m_pNeighbor[1].Y = m_Coord.Y-1;
            m_pNeighbor[2].X = m_Coord.X-1; m_pNeighbor[2].Y = m_Coord.Y  ;
            m_pNeighbor[3].X = m_Coord.X+1; m_pNeighbor[3].Y = m_Coord.Y  ;
            m_pNeighbor[4].X = m_Coord.X-1; m_pNeighbor[4].Y = m_Coord.Y+1;
            m_pNeighbor[5].X = m_Coord.X  ; m_pNeighbor[5].Y = m_Coord.Y+1;
            m_pNeighbor[6].X = m_Coord.X+1; m_pNeighbor[6].Y = m_Coord.Y+1; break;
   case 3:  m_pNeighbor[0].X = m_Coord.X-1; m_pNeighbor[0].Y = m_Coord.Y-1;
            m_pNeighbor[1].X = m_Coord.X  ; m_pNeighbor[1].Y = m_Coord.Y-1;
            m_pNeighbor[2].X = m_Coord.X+1; m_pNeighbor[2].Y = m_Coord.Y-1;
            m_pNeighbor[3].X = m_Coord.X-1; m_pNeighbor[3].Y = m_Coord.Y  ;
            m_pNeighbor[4].X = m_Coord.X+1; m_pNeighbor[4].Y = m_Coord.Y  ;
            m_pNeighbor[5].X = m_Coord.X  ; m_pNeighbor[5].Y = m_Coord.Y+1;
            m_pNeighbor[6].X = m_Coord.X+1; m_pNeighbor[6].Y = m_Coord.Y+1; break;
   }
   VerifyNeighbor(sizeField);
}

void nsCell::CPentagonT24::SetPoint(int iArea) {
   if (m_Coord.X==0 && m_Coord.Y==0) {
      m_a = sqrt(iArea); // базова€ величина п€тиугольника
      m_b = m_a*6/11;
      m_c = m_b/2;
      m_sq = m_a*8/11-(m_GContext.m_Border.m_iWidth+m_GContext.m_Border.m_iWidth/SIN135a)/SQRT2;
   }

   // определение координат точек фигуры
   float oX = m_a*((m_Coord.X>>1)<<1); // offset X
   float oY = m_a*((m_Coord.Y>>1)<<1); // offset Y
   switch (m_iDirection) {
   case 0:
      m_pRegion[0].x = oX +         m_a;   m_pRegion[0].y = oY + m_b        ;
      m_pRegion[1].x = oX + m_c        ;   m_pRegion[1].y = oY + m_c        ;
      m_pRegion[2].x = oX              ;   m_pRegion[2].y = oY +         m_a;
      m_pRegion[4].x = oX + m_c +   m_a;   m_pRegion[4].y = oY + m_c +   m_a;
      m_pRegion[3].x = oX + m_b        ;   m_pRegion[3].y = oY + m_b +   m_a;
      break;
   case 1:
      m_pRegion[3].x = oX + m_c +   m_a;   m_pRegion[3].y = oY + m_c +   m_a;
      m_pRegion[2].x = oX +         m_a;   m_pRegion[2].y = oY + m_b        ;
      m_pRegion[1].x = oX + m_b +   m_a;   m_pRegion[1].y = oY              ;
      m_pRegion[0].x = oX + m_c + 2*m_a;   m_pRegion[0].y = oY + m_c        ;
      m_pRegion[4].x = oX +       2*m_a;   m_pRegion[4].y = oY +         m_a;
      break;
   case 2:
      m_pRegion[0].x = oX + m_c        ;   m_pRegion[0].y = oY + m_c + 2*m_a;
      m_pRegion[1].x = oX +         m_a;   m_pRegion[1].y = oY + m_b + 2*m_a;
      m_pRegion[2].x = oX + m_b +   m_a;   m_pRegion[2].y = oY +       2*m_a;
      m_pRegion[3].x = oX + m_c +   m_a;   m_pRegion[3].y = oY + m_c +   m_a;
      m_pRegion[4].x = oX + m_b        ;   m_pRegion[4].y = oY + m_b +   m_a;
      break;
   case 3:
      m_pRegion[0].x = oX + m_b +   m_a;   m_pRegion[0].y = oY +       2*m_a;
      m_pRegion[1].x = oX + m_c + 2*m_a;   m_pRegion[1].y = oY + m_c + 2*m_a;
      m_pRegion[2].x = oX + m_b + 2*m_a;   m_pRegion[2].y = oY + m_b +   m_a;
      m_pRegion[3].x = oX +       2*m_a;   m_pRegion[3].y = oY +         m_a;
      m_pRegion[4].x = oX + m_c +   m_a;   m_pRegion[4].y = oY + m_c +   m_a;
      break;
   }

   // определение координат вписанного в фигуру квадрата - область в которую выводитс€ изображение/текст
   float w2 = m_GContext.m_Border.m_iWidth/SQRT2;
   switch (m_iDirection) {
   case 0:  m_Square.left   = m_pRegion[1].x+w2;  m_Square.right  = m_pRegion[1].x+w2 + m_sq;
            m_Square.bottom = m_pRegion[4].y-w2;  m_Square.top    = m_pRegion[4].y-w2 - m_sq; break;
   case 1:  m_Square.left   = m_pRegion[3].x+w2;  m_Square.right  = m_pRegion[3].x+w2 + m_sq;
            m_Square.top    = m_pRegion[0].y+w2;  m_Square.bottom = m_pRegion[0].y+w2 + m_sq; break;
   case 2:  m_Square.right  = m_pRegion[3].x-w2;  m_Square.left   = m_pRegion[3].x-w2 - m_sq;
            m_Square.bottom = m_pRegion[0].y-w2;  m_Square.top    = m_pRegion[0].y-w2 - m_sq; break;
   case 3:  m_Square.right  = m_pRegion[1].x-w2;  m_Square.left   = m_pRegion[1].x-w2 - m_sq;
            m_Square.top    = m_pRegion[4].y+w2;  m_Square.bottom = m_pRegion[4].y+w2 + m_sq; break;
   }
}

void nsCell::CPentagonT24::Paint() const {
   CBase::Paint();

   switch (m_iDirection) {
   case 0: case 1:   SelectObject(m_GContext.m_hDCTmp, m_bDown ? m_GContext.m_hPenShadow : m_GContext.m_hPenLight ); break;
   case 2: case 3:   SelectObject(m_GContext.m_hDCTmp, m_bDown ? m_GContext.m_hPenLight  : m_GContext.m_hPenShadow); break;
   }
   MoveToEx(m_GContext.m_hDCTmp, m_pRegion[0].x, m_pRegion[0].y, NULL);
   LineTo  (m_GContext.m_hDCTmp, m_pRegion[1].x, m_pRegion[1].y);
   LineTo  (m_GContext.m_hDCTmp, m_pRegion[2].x, m_pRegion[2].y);
   LineTo  (m_GContext.m_hDCTmp, m_pRegion[3].x, m_pRegion[3].y);
   switch (m_iDirection) {
   case 0: case 1:   SelectObject(m_GContext.m_hDCTmp, m_bDown ? m_GContext.m_hPenLight  : m_GContext.m_hPenShadow); break;
   case 2: case 3:   SelectObject(m_GContext.m_hDCTmp, m_bDown ? m_GContext.m_hPenShadow : m_GContext.m_hPenLight ); break;
   }
   LineTo  (m_GContext.m_hDCTmp, m_pRegion[4].x, m_pRegion[4].y);
   LineTo  (m_GContext.m_hDCTmp, m_pRegion[0].x, m_pRegion[0].y);

   RegionDraw(
      m_GContext.m_hDCDst, true,
      m_pRegion, 5, NULL, NULL, 0, 0, 0, 0,
      m_GContext.m_hDCTmp, NULL, 0,0);
}
