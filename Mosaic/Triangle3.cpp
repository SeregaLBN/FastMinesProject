////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "Triangle3.cpp"
//
// –еализаци€ класса CTriangle3 - треугольник 45∞-90∞-45∞
////////////////////////////////////////////////////////////////////////////////

#include "StdAfx.h"
#include "Triangle3.h"

////////////////////////////////////////////////////////////////////////////////
//                               local init
////////////////////////////////////////////////////////////////////////////////

float nsCell::CTriangle3::m_b;

////////////////////////////////////////////////////////////////////////////////
//                               static function
////////////////////////////////////////////////////////////////////////////////

SIZE nsCell::CTriangle3::GetSizeInPixel(const SIZE &sizeField, int iArea) {
   m_a = 2*sqrt(iArea);
   SIZE result = {m_a*((sizeField.cx+1)>>1),
                  m_a*((sizeField.cy+1)>>1)};
   return result;
}

int nsCell::CTriangle3::SizeInscribedSquare(int iArea, int iBorderWidth) {
   m_a = 2*sqrt(iArea);
   m_sq = (m_a-iBorderWidth*2/TAN45_2)/3;
   return m_sq;
}

////////////////////////////////////////////////////////////////////////////////
//                               virtual function
////////////////////////////////////////////////////////////////////////////////

nsCell::CTriangle3::CTriangle3(const COORD &Coord, const SIZE &sizeField, int iArea, const CGraphicContext &gContext)
   : CBase(Coord, sizeField, iArea, gContext,
           14, 3,
           ((Coord.Y&1)<<1)+(Coord.X&1) // 0..3
          )
{
   SetPoint(iArea);
   // определ€ю координаты соседей
   switch (m_iDirection) {
   case  0:
      m_pNeighbor[ 0].X = m_Coord.X-1;   m_pNeighbor[ 0].Y = m_Coord.Y-2;
      m_pNeighbor[ 1].X = m_Coord.X+1;   m_pNeighbor[ 1].Y = m_Coord.Y-2;
      m_pNeighbor[ 2].X = m_Coord.X-1;   m_pNeighbor[ 2].Y = m_Coord.Y-1;
      m_pNeighbor[ 3].X = m_Coord.X  ;   m_pNeighbor[ 3].Y = m_Coord.Y-1;
      m_pNeighbor[ 4].X = m_Coord.X+1;   m_pNeighbor[ 4].Y = m_Coord.Y-1;
      m_pNeighbor[ 5].X = m_Coord.X+2;   m_pNeighbor[ 5].Y = m_Coord.Y-1;
      m_pNeighbor[ 6].X = m_Coord.X+3;   m_pNeighbor[ 6].Y = m_Coord.Y-1;
      m_pNeighbor[ 7].X = m_Coord.X-2;   m_pNeighbor[ 7].Y = m_Coord.Y  ;
      m_pNeighbor[ 8].X = m_Coord.X-1;   m_pNeighbor[ 8].Y = m_Coord.Y  ;
      m_pNeighbor[ 9].X = m_Coord.X+1;   m_pNeighbor[ 9].Y = m_Coord.Y  ;
      m_pNeighbor[10].X = m_Coord.X+2;   m_pNeighbor[10].Y = m_Coord.Y  ;
      m_pNeighbor[11].X = m_Coord.X  ;   m_pNeighbor[11].Y = m_Coord.Y+1;
      m_pNeighbor[12].X = m_Coord.X+1;   m_pNeighbor[12].Y = m_Coord.Y+1;
      m_pNeighbor[13].X = m_Coord.X+2;   m_pNeighbor[13].Y = m_Coord.Y+1;
      break;
   case  1:
      m_pNeighbor[ 0].X = m_Coord.X  ;   m_pNeighbor[ 0].Y = m_Coord.Y-2;
      m_pNeighbor[ 1].X = m_Coord.X  ;   m_pNeighbor[ 1].Y = m_Coord.Y-1;
      m_pNeighbor[ 2].X = m_Coord.X+1;   m_pNeighbor[ 2].Y = m_Coord.Y-1;
      m_pNeighbor[ 3].X = m_Coord.X+2;   m_pNeighbor[ 3].Y = m_Coord.Y-1;
      m_pNeighbor[ 4].X = m_Coord.X-1;   m_pNeighbor[ 4].Y = m_Coord.Y  ;
      m_pNeighbor[ 5].X = m_Coord.X+1;   m_pNeighbor[ 5].Y = m_Coord.Y  ;
      m_pNeighbor[ 6].X = m_Coord.X-1;   m_pNeighbor[ 6].Y = m_Coord.Y+1;
      m_pNeighbor[ 7].X = m_Coord.X  ;   m_pNeighbor[ 7].Y = m_Coord.Y+1;
      m_pNeighbor[ 8].X = m_Coord.X+1;   m_pNeighbor[ 8].Y = m_Coord.Y+1;
      m_pNeighbor[ 9].X = m_Coord.X+2;   m_pNeighbor[ 9].Y = m_Coord.Y+1;
      m_pNeighbor[10].X = m_Coord.X-1;   m_pNeighbor[10].Y = m_Coord.Y+2;
      m_pNeighbor[11].X = m_Coord.X  ;   m_pNeighbor[11].Y = m_Coord.Y+2;
      m_pNeighbor[12].X = m_Coord.X+1;   m_pNeighbor[12].Y = m_Coord.Y+2;
      m_pNeighbor[13].X = m_Coord.X+1;   m_pNeighbor[13].Y = m_Coord.Y+3;
      break;
   case  2:
      m_pNeighbor[ 0].X = m_Coord.X-1;   m_pNeighbor[ 0].Y = m_Coord.Y-3;
      m_pNeighbor[ 1].X = m_Coord.X-1;   m_pNeighbor[ 1].Y = m_Coord.Y-2;
      m_pNeighbor[ 2].X = m_Coord.X  ;   m_pNeighbor[ 2].Y = m_Coord.Y-2;
      m_pNeighbor[ 3].X = m_Coord.X+1;   m_pNeighbor[ 3].Y = m_Coord.Y-2;
      m_pNeighbor[ 4].X = m_Coord.X-2;   m_pNeighbor[ 4].Y = m_Coord.Y-1;
      m_pNeighbor[ 5].X = m_Coord.X-1;   m_pNeighbor[ 5].Y = m_Coord.Y-1;
      m_pNeighbor[ 6].X = m_Coord.X  ;   m_pNeighbor[ 6].Y = m_Coord.Y-1;
      m_pNeighbor[ 7].X = m_Coord.X+1;   m_pNeighbor[ 7].Y = m_Coord.Y-1;
      m_pNeighbor[ 8].X = m_Coord.X-1;   m_pNeighbor[ 8].Y = m_Coord.Y  ;
      m_pNeighbor[ 9].X = m_Coord.X+1;   m_pNeighbor[ 9].Y = m_Coord.Y  ;
      m_pNeighbor[10].X = m_Coord.X-2;   m_pNeighbor[10].Y = m_Coord.Y+1;
      m_pNeighbor[11].X = m_Coord.X-1;   m_pNeighbor[11].Y = m_Coord.Y+1;
      m_pNeighbor[12].X = m_Coord.X  ;   m_pNeighbor[12].Y = m_Coord.Y+1;
      m_pNeighbor[13].X = m_Coord.X  ;   m_pNeighbor[13].Y = m_Coord.Y+2;
      break;
   case  3:
      m_pNeighbor[ 0].X = m_Coord.X-2;   m_pNeighbor[ 0].Y = m_Coord.Y-1;
      m_pNeighbor[ 1].X = m_Coord.X-1;   m_pNeighbor[ 1].Y = m_Coord.Y-1;
      m_pNeighbor[ 2].X = m_Coord.X  ;   m_pNeighbor[ 2].Y = m_Coord.Y-1;
      m_pNeighbor[ 3].X = m_Coord.X-2;   m_pNeighbor[ 3].Y = m_Coord.Y  ;
      m_pNeighbor[ 4].X = m_Coord.X-1;   m_pNeighbor[ 4].Y = m_Coord.Y  ;
      m_pNeighbor[ 5].X = m_Coord.X+1;   m_pNeighbor[ 5].Y = m_Coord.Y  ;
      m_pNeighbor[ 6].X = m_Coord.X+2;   m_pNeighbor[ 6].Y = m_Coord.Y  ;
      m_pNeighbor[ 7].X = m_Coord.X-3;   m_pNeighbor[ 7].Y = m_Coord.Y+1;
      m_pNeighbor[ 8].X = m_Coord.X-2;   m_pNeighbor[ 8].Y = m_Coord.Y+1;
      m_pNeighbor[ 9].X = m_Coord.X-1;   m_pNeighbor[ 9].Y = m_Coord.Y+1;
      m_pNeighbor[10].X = m_Coord.X  ;   m_pNeighbor[10].Y = m_Coord.Y+1;
      m_pNeighbor[11].X = m_Coord.X+1;   m_pNeighbor[11].Y = m_Coord.Y+1;
      m_pNeighbor[12].X = m_Coord.X-1;   m_pNeighbor[12].Y = m_Coord.Y+2;
      m_pNeighbor[13].X = m_Coord.X+1;   m_pNeighbor[13].Y = m_Coord.Y+2;
      break;
   }
   VerifyNeighbor(sizeField);
}

void nsCell::CTriangle3::SetPoint(int iArea) {
   if (m_Coord.X==0 && m_Coord.Y==0) {
      m_a = sqrt(iArea)*2;
      m_b = m_a/2;
      m_sq = (m_a-m_GContext.m_Border.m_iWidth*2/TAN45_2)/3;
   }

   // определение координат точек фигуры
   float oX = m_a*(m_Coord.X>>1); // offset X
   float oY = m_a*(m_Coord.Y>>1); // offset Y
   switch (m_iDirection) {
   case 0:
      m_pRegion[0].x = oX + m_a; m_pRegion[0].y = oY;
      m_pRegion[1].x = oX      ; m_pRegion[1].y = oY;
      m_pRegion[2].x = oX + m_b; m_pRegion[2].y = oY + m_b;
      break;
   case 1:
      m_pRegion[0].x = oX + m_a; m_pRegion[0].y = oY;
      m_pRegion[1].x = oX + m_b; m_pRegion[1].y = oY + m_b;
      m_pRegion[2].x = oX + m_a; m_pRegion[2].y = oY + m_a;
      break;
   case 2:
      m_pRegion[0].x = oX      ; m_pRegion[0].y = oY + m_a;
      m_pRegion[1].x = oX + m_b; m_pRegion[1].y = oY + m_b;
      m_pRegion[2].x = oX      ; m_pRegion[2].y = oY;
      break;
   case 3:
      m_pRegion[0].x = oX      ; m_pRegion[0].y = oY + m_a;
      m_pRegion[1].x = oX + m_a; m_pRegion[1].y = oY + m_a;
      m_pRegion[2].x = oX + m_b; m_pRegion[2].y = oY + m_b;
      break;
   }

   // определение координат вписанного в фигуру квадрата - область в которую выводитс€ изображение/текст
   {
      POINTFLOAT center; // координата центра квадрата
      switch (m_iDirection) {
      case 0:  center.x = m_pRegion[2].x;
               center.y = m_pRegion[0].y + m_a/6; break;
      case 1:  center.x = m_pRegion[0].x - m_a/6;
               center.y = m_pRegion[1].y;         break;
      case 2:  center.x = m_pRegion[2].x + m_a/6;
               center.y = m_pRegion[1].y;         break;
      case 3:  center.x = m_pRegion[2].x;
               center.y = m_pRegion[1].y - m_a/6; break;
      }
      m_Square.left   = center.x - m_sq/2;
      m_Square.top    = center.y - m_sq/2;
      m_Square.right  = m_Square.left + m_sq;
      m_Square.bottom = m_Square.top  + m_sq;
   }
}

void nsCell::CTriangle3::Paint() const {
   CBase::Paint();

   switch (m_iDirection) {
   case 0: case 1:   SelectObject(m_GContext.m_hDCTmp, m_bDown ? m_GContext.m_hPenShadow : m_GContext.m_hPenLight ); break;
   case 2: case 3:   SelectObject(m_GContext.m_hDCTmp, m_bDown ? m_GContext.m_hPenLight  : m_GContext.m_hPenShadow); break;
   }
   MoveToEx(m_GContext.m_hDCTmp, m_pRegion[0].x, m_pRegion[0].y, NULL);
   LineTo  (m_GContext.m_hDCTmp, m_pRegion[1].x, m_pRegion[1].y);
   LineTo  (m_GContext.m_hDCTmp, m_pRegion[2].x, m_pRegion[2].y);
   switch (m_iDirection) {
   case 0: case 1:   SelectObject(m_GContext.m_hDCTmp, m_bDown ? m_GContext.m_hPenLight  : m_GContext.m_hPenShadow); break;
   case 2: case 3:   SelectObject(m_GContext.m_hDCTmp, m_bDown ? m_GContext.m_hPenShadow : m_GContext.m_hPenLight ); break;
   }
   LineTo  (m_GContext.m_hDCTmp, m_pRegion[0].x, m_pRegion[0].y);

   RegionDraw(
      m_GContext.m_hDCDst, true,
      m_pRegion, 3, NULL, NULL, 0, 0, 0, 0,
      m_GContext.m_hDCTmp, NULL, 0,0);
}
