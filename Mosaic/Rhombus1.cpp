////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "Rhombus1.cpp"
//
// Реализация класса CRhombus1 - 3 ромба, составляющие равносторонний шестиугольник
////////////////////////////////////////////////////////////////////////////////

#include "StdAfx.h"
#include "Rhombus1.h"

////////////////////////////////////////////////////////////////////////////////
//                               local init
////////////////////////////////////////////////////////////////////////////////

float nsCell::CRhombus1::m_r;
float nsCell::CRhombus1::m_h;
float nsCell::CRhombus1::m_c;

////////////////////////////////////////////////////////////////////////////////
//                               static function
////////////////////////////////////////////////////////////////////////////////

SIZE nsCell::CRhombus1::GetSizeInPixel(const SIZE &sizeField, int iArea) {
   m_a = sqrt(iArea*2/SQRT3);
   m_r = m_a*SQRT3/2;
   m_c = m_a/2;

   SIZE result = {m_c +  m_a     *((sizeField.cx+2)/3) +
                        (m_a+m_c)*((sizeField.cx+1)/3) +
                             m_c *((sizeField.cx+0)/3),
                             m_r * (sizeField.cy+1)};
   return result;
}

int nsCell::CRhombus1::SizeInscribedSquare(int iArea, int iBorderWidth) {
   m_a  = sqrt(iArea*2/SQRT3);
   m_sq = (m_a*SQRT3-iBorderWidth*4)/(1+SQRT3);
   return m_sq;
}

////////////////////////////////////////////////////////////////////////////////
//                               virtual function
////////////////////////////////////////////////////////////////////////////////

nsCell::CRhombus1::CRhombus1(const COORD &Coord, const SIZE &sizeField, int iArea, const CGraphicContext &gContext)
   : CBase(Coord, sizeField, iArea, gContext,
           10, 4,
           (Coord.Y&1)*3+(Coord.X%3) // 0..5
          )
{
   SetPoint(iArea);
   // определяю координаты соседей
   switch (m_iDirection) {
   case  0:
      m_pNeighbor[0].X = m_Coord.X+1;   m_pNeighbor[0].Y = m_Coord.Y-2;
      m_pNeighbor[1].X = m_Coord.X+2;   m_pNeighbor[1].Y = m_Coord.Y-2;
      m_pNeighbor[2].X = m_Coord.X-2;   m_pNeighbor[2].Y = m_Coord.Y-1;
      m_pNeighbor[3].X = m_Coord.X-1;   m_pNeighbor[3].Y = m_Coord.Y-1;
      m_pNeighbor[4].X = m_Coord.X  ;   m_pNeighbor[4].Y = m_Coord.Y-1;
      m_pNeighbor[5].X = m_Coord.X+1;   m_pNeighbor[5].Y = m_Coord.Y-1;
      m_pNeighbor[6].X = m_Coord.X-1;   m_pNeighbor[6].Y = m_Coord.Y  ;
      m_pNeighbor[7].X = m_Coord.X+1;   m_pNeighbor[7].Y = m_Coord.Y  ;
      m_pNeighbor[8].X = m_Coord.X-1;   m_pNeighbor[8].Y = m_Coord.Y+1;
      m_pNeighbor[9].X = m_Coord.X  ;   m_pNeighbor[9].Y = m_Coord.Y+1;
      break;
   case  1:
      m_pNeighbor[0].X = m_Coord.X  ;   m_pNeighbor[0].Y = m_Coord.Y-2;
      m_pNeighbor[1].X = m_Coord.X+1;   m_pNeighbor[1].Y = m_Coord.Y-2;
      m_pNeighbor[2].X = m_Coord.X-1;   m_pNeighbor[2].Y = m_Coord.Y-1;
      m_pNeighbor[3].X = m_Coord.X  ;   m_pNeighbor[3].Y = m_Coord.Y-1;
      m_pNeighbor[4].X = m_Coord.X-1;   m_pNeighbor[4].Y = m_Coord.Y  ;
      m_pNeighbor[5].X = m_Coord.X+1;   m_pNeighbor[5].Y = m_Coord.Y  ;  
      m_pNeighbor[6].X = m_Coord.X-1;   m_pNeighbor[6].Y = m_Coord.Y+1;
      m_pNeighbor[7].X = m_Coord.X  ;   m_pNeighbor[7].Y = m_Coord.Y+1;
      m_pNeighbor[8].X = m_Coord.X-1;   m_pNeighbor[8].Y = m_Coord.Y+2;
      m_pNeighbor[9].X = m_Coord.X  ;   m_pNeighbor[9].Y = m_Coord.Y+2;
      break;
   case  2:
      m_pNeighbor[0].X = m_Coord.X-1;   m_pNeighbor[0].Y = m_Coord.Y-1;
      m_pNeighbor[1].X = m_Coord.X  ;   m_pNeighbor[1].Y = m_Coord.Y-1;
      m_pNeighbor[2].X = m_Coord.X-1;   m_pNeighbor[2].Y = m_Coord.Y  ;
      m_pNeighbor[3].X = m_Coord.X+1;   m_pNeighbor[3].Y = m_Coord.Y  ;
      m_pNeighbor[4].X = m_Coord.X-2;   m_pNeighbor[4].Y = m_Coord.Y+1;
      m_pNeighbor[5].X = m_Coord.X-1;   m_pNeighbor[5].Y = m_Coord.Y+1;  
      m_pNeighbor[6].X = m_Coord.X  ;   m_pNeighbor[6].Y = m_Coord.Y+1;
      m_pNeighbor[7].X = m_Coord.X+1;   m_pNeighbor[7].Y = m_Coord.Y+1;
      m_pNeighbor[8].X = m_Coord.X-2;   m_pNeighbor[8].Y = m_Coord.Y+2;
      m_pNeighbor[9].X = m_Coord.X-1;   m_pNeighbor[9].Y = m_Coord.Y+2;
      break;
   case  3:
      m_pNeighbor[0].X = m_Coord.X-2;   m_pNeighbor[0].Y = m_Coord.Y-2;
      m_pNeighbor[1].X = m_Coord.X-1;   m_pNeighbor[1].Y = m_Coord.Y-2;
      m_pNeighbor[2].X = m_Coord.X-1;   m_pNeighbor[2].Y = m_Coord.Y-1;
      m_pNeighbor[3].X = m_Coord.X  ;   m_pNeighbor[3].Y = m_Coord.Y-1;
      m_pNeighbor[4].X = m_Coord.X+1;   m_pNeighbor[4].Y = m_Coord.Y-1;
      m_pNeighbor[5].X = m_Coord.X+2;   m_pNeighbor[5].Y = m_Coord.Y-1;
      m_pNeighbor[6].X = m_Coord.X-1;   m_pNeighbor[6].Y = m_Coord.Y  ;
      m_pNeighbor[7].X = m_Coord.X+1;   m_pNeighbor[7].Y = m_Coord.Y  ;
      m_pNeighbor[8].X = m_Coord.X  ;   m_pNeighbor[8].Y = m_Coord.Y+1;
      m_pNeighbor[9].X = m_Coord.X+1;   m_pNeighbor[9].Y = m_Coord.Y+1;
      break;
   case  4:
      m_pNeighbor[0].X = m_Coord.X  ;   m_pNeighbor[0].Y = m_Coord.Y-1;
      m_pNeighbor[1].X = m_Coord.X+1;   m_pNeighbor[1].Y = m_Coord.Y-1;
      m_pNeighbor[2].X = m_Coord.X-1;   m_pNeighbor[2].Y = m_Coord.Y  ;
      m_pNeighbor[3].X = m_Coord.X+1;   m_pNeighbor[3].Y = m_Coord.Y  ;
      m_pNeighbor[4].X = m_Coord.X-1;   m_pNeighbor[4].Y = m_Coord.Y+1;
      m_pNeighbor[5].X = m_Coord.X  ;   m_pNeighbor[5].Y = m_Coord.Y+1;
      m_pNeighbor[6].X = m_Coord.X+1;   m_pNeighbor[6].Y = m_Coord.Y+1;
      m_pNeighbor[7].X = m_Coord.X+2;   m_pNeighbor[7].Y = m_Coord.Y+1;
      m_pNeighbor[8].X = m_Coord.X+1;   m_pNeighbor[8].Y = m_Coord.Y+2;
      m_pNeighbor[9].X = m_Coord.X+2;   m_pNeighbor[9].Y = m_Coord.Y+2;
      break;
   case  5:
      m_pNeighbor[0].X = m_Coord.X-1;   m_pNeighbor[0].Y = m_Coord.Y-2;
      m_pNeighbor[1].X = m_Coord.X  ;   m_pNeighbor[1].Y = m_Coord.Y-2;
      m_pNeighbor[2].X = m_Coord.X  ;   m_pNeighbor[2].Y = m_Coord.Y-1;
      m_pNeighbor[3].X = m_Coord.X+1;   m_pNeighbor[3].Y = m_Coord.Y-1;
      m_pNeighbor[4].X = m_Coord.X-1;   m_pNeighbor[4].Y = m_Coord.Y  ;
      m_pNeighbor[5].X = m_Coord.X+1;   m_pNeighbor[5].Y = m_Coord.Y  ;
      m_pNeighbor[6].X = m_Coord.X  ;   m_pNeighbor[6].Y = m_Coord.Y+1;
      m_pNeighbor[7].X = m_Coord.X+1;   m_pNeighbor[7].Y = m_Coord.Y+1;
      m_pNeighbor[8].X = m_Coord.X  ;   m_pNeighbor[8].Y = m_Coord.Y+2;
      m_pNeighbor[9].X = m_Coord.X+1;   m_pNeighbor[9].Y = m_Coord.Y+2;
      break;
   }
   VerifyNeighbor(sizeField);
}

void nsCell::CRhombus1::SetPoint(int iArea) {
   if (m_Coord.X==0 && m_Coord.Y==0) {
      m_a = sqrt(iArea*2/SQRT3);
      m_h = m_a*SQRT3;
      m_r = m_h/2;
      m_c = m_a/2;
      m_sq = (m_a*SQRT3-m_GContext.m_Border.m_iWidth*4)/(1+SQRT3);
   }

   // определение координат точек фигуры
   float oX = m_a*(m_Coord.X/3*3+1)+m_c; // offset X
   float oY = m_h*(m_Coord.Y/2)    +m_h; // offset Y
   switch (m_iDirection) {
   case 0:
      m_pRegion[0].x = oX;           m_pRegion[0].y = oY - m_h;
      m_pRegion[1].x = oX - m_a;     m_pRegion[1].y = oY - m_h;
      m_pRegion[2].x = oX - m_a-m_c; m_pRegion[2].y = oY - m_r;
      m_pRegion[3].x = oX - m_c;     m_pRegion[3].y = oY - m_r;
      break;
   case 1:
      m_pRegion[0].x = oX;           m_pRegion[0].y = oY - m_h;
      m_pRegion[1].x = oX - m_c;     m_pRegion[1].y = oY - m_r;
      m_pRegion[2].x = oX;           m_pRegion[2].y = oY;
      m_pRegion[3].x = oX + m_c;     m_pRegion[3].y = oY - m_r;
      break;
   case 2:
      m_pRegion[0].x = oX + m_a+m_c; m_pRegion[0].y = oY - m_r;
      m_pRegion[1].x = oX + m_c;     m_pRegion[1].y = oY - m_r;
      m_pRegion[2].x = oX;           m_pRegion[2].y = oY;
      m_pRegion[3].x = oX + m_a;     m_pRegion[3].y = oY;
      break;
   case 3:
      m_pRegion[0].x = oX - m_c;     m_pRegion[0].y = oY - m_r;
      m_pRegion[1].x = oX - m_a-m_c; m_pRegion[1].y = oY - m_r;
      m_pRegion[2].x = oX - m_a;     m_pRegion[2].y = oY;
      m_pRegion[3].x = oX;           m_pRegion[3].y = oY;
      break;
   case 4:
      m_pRegion[0].x = oX + m_a;     m_pRegion[0].y = oY;
      m_pRegion[1].x = oX;           m_pRegion[1].y = oY;
      m_pRegion[2].x = oX + m_c;     m_pRegion[2].y = oY + m_r;
      m_pRegion[3].x = oX + m_a+m_c; m_pRegion[3].y = oY + m_r;
      break;
   case 5:
      m_pRegion[0].x = oX + m_a+m_c; m_pRegion[0].y = oY - m_r;
      m_pRegion[1].x = oX + m_a;     m_pRegion[1].y = oY;
      m_pRegion[2].x = oX + m_a+m_c; m_pRegion[2].y = oY + m_r;
      m_pRegion[3].x = oX + m_a+m_a; m_pRegion[3].y = oY;
      break;
   }

   // определение координат вписанного в фигуру квадрата - область в которую выводится изображение/текст
   {
      POINTFLOAT center; // координата вписанного в фигуру квадрата
      switch (m_iDirection) {
      case 0:  center.x = oX - m_c*1.5f;  center.y = oY - m_r*1.5f;  break;
      case 1:  center.x = m_pRegion[0].x; center.y = m_pRegion[1].y; break;
      case 2:  center.x = oX + m_c*1.5f;  center.y = oY - m_r*0.5f;  break;
      case 3:  center.x = oX - m_c*1.5f;  center.y = oY - m_r*0.5f;  break;
      case 4:  center.x = oX + m_c*1.5f;  center.y = oY + m_r*0.5f;  break;
      case 5:  center.x = m_pRegion[0].x; center.y = m_pRegion[1].y; break;
      }
      m_Square.left   = center.x - m_sq/2;
      m_Square.top    = center.y - m_sq/2;
      m_Square.right  = m_Square.left + m_sq;
      m_Square.bottom = m_Square.top  + m_sq;
   }
}

void nsCell::CRhombus1::Paint() const {
   /**
   int minX = min(m_pRegion[1].x, m_pRegion[2].x);
   int maxX = max(m_pRegion[0].x, m_pRegion[3].x);
   int minY = m_pRegion[0].y;
   int maxY = m_pRegion[2].y;
   BitBlt(m_GContext.m_hDCTmp, minX, minY, maxX - minX, maxY - minY,
          hDCBck, minX, minY, SRCCOPY);
   /**/

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
