////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "Trapezoid1.cpp"
//
// Реализация класса CTrapezoid1 - 3 трапеции, составляющие равносторонний треугольник
////////////////////////////////////////////////////////////////////////////////

#include "StdAfx.h"
#include "Trapezoid1.h"

////////////////////////////////////////////////////////////////////////////////
//                               local init
////////////////////////////////////////////////////////////////////////////////

float nsCell::CTrapezoid1::m_b; // большая сторона трапеции (основание)
float nsCell::CTrapezoid1::m_c;
float nsCell::CTrapezoid1::m_R; // диагональ трапеции
float nsCell::CTrapezoid1::m_r; // высота трапеции

////////////////////////////////////////////////////////////////////////////////
//                               static function
////////////////////////////////////////////////////////////////////////////////

SIZE nsCell::CTrapezoid1::GetSizeInPixel(const COORD &sizeField, int area) {
   m_a = sqrt(area/SQRT27)*2; // меньшая сторона трапеции (верх и стороны)
 //m_b = m_a*2;
   m_c = m_a/2;
   m_R = m_a*SQRT3;
   m_r = m_R/2;

   SIZE result = {m_c + m_a* (sizeField.X+1),
                     m_R   *((sizeField.Y+1)/2)+
                     m_r   *((sizeField.Y+0)/2)};
   return result;
}

int nsCell::CTrapezoid1::SizeInscribedSquare(int area, int borderWidth) {
   m_a = 2*sqrt(area/SQRT27);       // меньшая сторона трапеции (верх и стороны)
   m_sq = (m_a*SQRT3-4*borderWidth)/(1+SQRT3); // размер квадрата, вписанного в трапецию
   return m_sq;
}

////////////////////////////////////////////////////////////////////////////////
//                               virtual function
////////////////////////////////////////////////////////////////////////////////

nsCell::CTrapezoid1::CTrapezoid1(const COORD &Coord, const COORD &sizeField, int area, const CGraphicContext &gContext)
   : CBase(Coord, sizeField, area, gContext,
           8, 4,
           (Coord.Y&3)*3+(Coord.X%3) // 0..11
          )
{
   SetPoint(area);
   // определяю координаты соседей
   switch (m_iDirection) {
   case  0:
      m_pNeighbor[0].X = m_Coord.X-1;   m_pNeighbor[0].Y = m_Coord.Y-1;
      m_pNeighbor[1].X = m_Coord.X  ;   m_pNeighbor[1].Y = m_Coord.Y-1;
      m_pNeighbor[2].X = m_Coord.X+1;   m_pNeighbor[2].Y = m_Coord.Y-1;
      m_pNeighbor[3].X = m_Coord.X-1;   m_pNeighbor[3].Y = m_Coord.Y  ;
      m_pNeighbor[4].X = m_Coord.X+1;   m_pNeighbor[4].Y = m_Coord.Y  ;
      m_pNeighbor[5].X = m_Coord.X-1;   m_pNeighbor[5].Y = m_Coord.Y+1;
      m_pNeighbor[6].X = m_Coord.X  ;   m_pNeighbor[6].Y = m_Coord.Y+1;
      m_pNeighbor[7].X = m_Coord.X+1;   m_pNeighbor[7].Y = m_Coord.Y+1;
      break;
   case  1:
      m_pNeighbor[0].X = m_Coord.X-2;   m_pNeighbor[0].Y = m_Coord.Y-1;
      m_pNeighbor[1].X = m_Coord.X-1;   m_pNeighbor[1].Y = m_Coord.Y-1;
      m_pNeighbor[2].X = m_Coord.X  ;   m_pNeighbor[2].Y = m_Coord.Y-1;
      m_pNeighbor[3].X = m_Coord.X-2;   m_pNeighbor[3].Y = m_Coord.Y  ;
      m_pNeighbor[4].X = m_Coord.X-1;   m_pNeighbor[4].Y = m_Coord.Y  ;
      m_pNeighbor[5].X = m_Coord.X+1;   m_pNeighbor[5].Y = m_Coord.Y  ;  
      m_pNeighbor[6].X = m_Coord.X  ;   m_pNeighbor[6].Y = m_Coord.Y+1;
      m_pNeighbor[7].X = m_Coord.X+1;   m_pNeighbor[7].Y = m_Coord.Y+1;
      break;
   case  2:
      m_pNeighbor[0].X = m_Coord.X-1;   m_pNeighbor[0].Y = m_Coord.Y-1;
      m_pNeighbor[1].X = m_Coord.X  ;   m_pNeighbor[1].Y = m_Coord.Y-1;
      m_pNeighbor[2].X = m_Coord.X+1;   m_pNeighbor[2].Y = m_Coord.Y-1;
      m_pNeighbor[3].X = m_Coord.X+2;   m_pNeighbor[3].Y = m_Coord.Y-1;
      m_pNeighbor[4].X = m_Coord.X-1;   m_pNeighbor[4].Y = m_Coord.Y  ;
      m_pNeighbor[5].X = m_Coord.X+1;   m_pNeighbor[5].Y = m_Coord.Y  ;  
      m_pNeighbor[6].X = m_Coord.X+2;   m_pNeighbor[6].Y = m_Coord.Y  ;
      m_pNeighbor[7].X = m_Coord.X  ;   m_pNeighbor[7].Y = m_Coord.Y+1;
      break;
   case  3:
      m_pNeighbor[0].X = m_Coord.X  ;   m_pNeighbor[0].Y = m_Coord.Y-1;
      m_pNeighbor[1].X = m_Coord.X-2;   m_pNeighbor[1].Y = m_Coord.Y  ;
      m_pNeighbor[2].X = m_Coord.X-1;   m_pNeighbor[2].Y = m_Coord.Y  ;
      m_pNeighbor[3].X = m_Coord.X+1;   m_pNeighbor[3].Y = m_Coord.Y  ;
      m_pNeighbor[4].X = m_Coord.X-2;   m_pNeighbor[4].Y = m_Coord.Y+1;
      m_pNeighbor[5].X = m_Coord.X-1;   m_pNeighbor[5].Y = m_Coord.Y+1;
      m_pNeighbor[6].X = m_Coord.X  ;   m_pNeighbor[6].Y = m_Coord.Y+1;
      m_pNeighbor[7].X = m_Coord.X+1;   m_pNeighbor[7].Y = m_Coord.Y+1;
      break;
   case  4:
      m_pNeighbor[0].X = m_Coord.X-1;   m_pNeighbor[0].Y = m_Coord.Y-1;
      m_pNeighbor[1].X = m_Coord.X  ;   m_pNeighbor[1].Y = m_Coord.Y-1;
      m_pNeighbor[2].X = m_Coord.X-1;   m_pNeighbor[2].Y = m_Coord.Y  ;
      m_pNeighbor[3].X = m_Coord.X+1;   m_pNeighbor[3].Y = m_Coord.Y  ;
      m_pNeighbor[4].X = m_Coord.X+2;   m_pNeighbor[4].Y = m_Coord.Y  ;
      m_pNeighbor[5].X = m_Coord.X  ;   m_pNeighbor[5].Y = m_Coord.Y+1;
      m_pNeighbor[6].X = m_Coord.X+1;   m_pNeighbor[6].Y = m_Coord.Y+1;
      m_pNeighbor[7].X = m_Coord.X+2;   m_pNeighbor[7].Y = m_Coord.Y+1;
      break;
   case  5:
      m_pNeighbor[0].X = m_Coord.X-1;   m_pNeighbor[0].Y = m_Coord.Y-1;
      m_pNeighbor[1].X = m_Coord.X  ;   m_pNeighbor[1].Y = m_Coord.Y-1;
      m_pNeighbor[2].X = m_Coord.X+1;   m_pNeighbor[2].Y = m_Coord.Y-1;
      m_pNeighbor[3].X = m_Coord.X-1;   m_pNeighbor[3].Y = m_Coord.Y  ;
      m_pNeighbor[4].X = m_Coord.X+1;   m_pNeighbor[4].Y = m_Coord.Y  ;
      m_pNeighbor[5].X = m_Coord.X-1;   m_pNeighbor[5].Y = m_Coord.Y+1;
      m_pNeighbor[6].X = m_Coord.X  ;   m_pNeighbor[6].Y = m_Coord.Y+1;
      m_pNeighbor[7].X = m_Coord.X+1;   m_pNeighbor[7].Y = m_Coord.Y+1;
      break;
   case  6:
      m_pNeighbor[0].X = m_Coord.X-2;   m_pNeighbor[0].Y = m_Coord.Y-1;
      m_pNeighbor[1].X = m_Coord.X-1;   m_pNeighbor[1].Y = m_Coord.Y-1;
      m_pNeighbor[2].X = m_Coord.X  ;   m_pNeighbor[2].Y = m_Coord.Y-1;
      m_pNeighbor[3].X = m_Coord.X-2;   m_pNeighbor[3].Y = m_Coord.Y  ;
      m_pNeighbor[4].X = m_Coord.X-1;   m_pNeighbor[4].Y = m_Coord.Y  ;
      m_pNeighbor[5].X = m_Coord.X+1;   m_pNeighbor[5].Y = m_Coord.Y  ;
      m_pNeighbor[6].X = m_Coord.X-1;   m_pNeighbor[6].Y = m_Coord.Y+1;
      m_pNeighbor[7].X = m_Coord.X  ;   m_pNeighbor[7].Y = m_Coord.Y+1;
      break;
   case  7:
      m_pNeighbor[0].X = m_Coord.X-1;   m_pNeighbor[0].Y = m_Coord.Y-1;
      m_pNeighbor[1].X = m_Coord.X  ;   m_pNeighbor[1].Y = m_Coord.Y-1;
      m_pNeighbor[2].X = m_Coord.X+1;   m_pNeighbor[2].Y = m_Coord.Y-1;
      m_pNeighbor[3].X = m_Coord.X+2;   m_pNeighbor[3].Y = m_Coord.Y-1;
      m_pNeighbor[4].X = m_Coord.X-1;   m_pNeighbor[4].Y = m_Coord.Y  ;
      m_pNeighbor[5].X = m_Coord.X+1;   m_pNeighbor[5].Y = m_Coord.Y  ;
      m_pNeighbor[6].X = m_Coord.X+2;   m_pNeighbor[6].Y = m_Coord.Y  ;
      m_pNeighbor[7].X = m_Coord.X-1;   m_pNeighbor[7].Y = m_Coord.Y+1;
      break;
   case  8:
      m_pNeighbor[0].X = m_Coord.X-1;   m_pNeighbor[0].Y = m_Coord.Y-1;
      m_pNeighbor[1].X = m_Coord.X  ;   m_pNeighbor[1].Y = m_Coord.Y-1;
      m_pNeighbor[2].X = m_Coord.X+1;   m_pNeighbor[2].Y = m_Coord.Y-1;
      m_pNeighbor[3].X = m_Coord.X-1;   m_pNeighbor[3].Y = m_Coord.Y  ;
      m_pNeighbor[4].X = m_Coord.X+1;   m_pNeighbor[4].Y = m_Coord.Y  ;
      m_pNeighbor[5].X = m_Coord.X-2;   m_pNeighbor[5].Y = m_Coord.Y+1;
      m_pNeighbor[6].X = m_Coord.X-1;   m_pNeighbor[6].Y = m_Coord.Y+1;
      m_pNeighbor[7].X = m_Coord.X  ;   m_pNeighbor[7].Y = m_Coord.Y+1;
      break;
   case  9:
      m_pNeighbor[0].X = m_Coord.X  ;   m_pNeighbor[0].Y = m_Coord.Y-1;
      m_pNeighbor[1].X = m_Coord.X+1;   m_pNeighbor[1].Y = m_Coord.Y-1;
      m_pNeighbor[2].X = m_Coord.X+2;   m_pNeighbor[2].Y = m_Coord.Y-1;
      m_pNeighbor[3].X = m_Coord.X-1;   m_pNeighbor[3].Y = m_Coord.Y  ;
      m_pNeighbor[4].X = m_Coord.X+1;   m_pNeighbor[4].Y = m_Coord.Y  ;
      m_pNeighbor[5].X = m_Coord.X-1;   m_pNeighbor[5].Y = m_Coord.Y+1;
      m_pNeighbor[6].X = m_Coord.X  ;   m_pNeighbor[6].Y = m_Coord.Y+1;
      m_pNeighbor[7].X = m_Coord.X+1;   m_pNeighbor[7].Y = m_Coord.Y+1;
      break;
   case 10:
      m_pNeighbor[0].X = m_Coord.X+1;   m_pNeighbor[0].Y = m_Coord.Y-1;
      m_pNeighbor[1].X = m_Coord.X-2;   m_pNeighbor[1].Y = m_Coord.Y  ;
      m_pNeighbor[2].X = m_Coord.X-1;   m_pNeighbor[2].Y = m_Coord.Y  ;
      m_pNeighbor[3].X = m_Coord.X+1;   m_pNeighbor[3].Y = m_Coord.Y  ;
      m_pNeighbor[4].X = m_Coord.X-2;   m_pNeighbor[4].Y = m_Coord.Y+1;
      m_pNeighbor[5].X = m_Coord.X-1;   m_pNeighbor[5].Y = m_Coord.Y+1;
      m_pNeighbor[6].X = m_Coord.X  ;   m_pNeighbor[6].Y = m_Coord.Y+1;
      m_pNeighbor[7].X = m_Coord.X+1;   m_pNeighbor[7].Y = m_Coord.Y+1;
      break;
   case 11:
      m_pNeighbor[0].X = m_Coord.X  ;   m_pNeighbor[0].Y = m_Coord.Y-1;
      m_pNeighbor[1].X = m_Coord.X+1;   m_pNeighbor[1].Y = m_Coord.Y-1;
      m_pNeighbor[2].X = m_Coord.X-1;   m_pNeighbor[2].Y = m_Coord.Y  ;
      m_pNeighbor[3].X = m_Coord.X+1;   m_pNeighbor[3].Y = m_Coord.Y  ;
      m_pNeighbor[4].X = m_Coord.X+2;   m_pNeighbor[4].Y = m_Coord.Y  ;
      m_pNeighbor[5].X = m_Coord.X  ;   m_pNeighbor[5].Y = m_Coord.Y+1;
      m_pNeighbor[6].X = m_Coord.X+1;   m_pNeighbor[6].Y = m_Coord.Y+1;
      m_pNeighbor[7].X = m_Coord.X+2;   m_pNeighbor[7].Y = m_Coord.Y+1;
      break;
   }
   VerifyNeighbor(sizeField);
}

void nsCell::CTrapezoid1::SetPoint(int area) {
   if (m_Coord.X==0 && m_Coord.Y==0) {
      m_a = sqrt(area/SQRT27)*2;
      m_b = m_a*2;
      m_c = m_a/2;
      m_R = m_a*SQRT3;
      m_r = m_R/2;
      m_sq = (m_a*SQRT3-4*m_GContext.m_Border.m_iWidth)/(1+SQRT3); // размер квадрата, вписанного в трапецию
   }

   // определение координат точек фигуры
   float oX = (m_a+m_b)*(m_Coord.X/3) + m_b; // offset X
   float oY = (m_R+m_r)*(m_Coord.Y/4*2+1); // offset Y
   switch (m_iDirection) {
   case 0:
      m_pRegion[0].x = oX - m_c;     m_pRegion[0].y = oY - m_R-m_r;
      m_pRegion[1].x = oX;           m_pRegion[1].y = oY - m_R;
      m_pRegion[2].x = oX - m_c;     m_pRegion[2].y = oY - m_r;
      m_pRegion[3].x = oX - m_c-m_a; m_pRegion[3].y = oY - m_r;
      break;
   case 1:
      m_pRegion[0].x = oX + m_c;     m_pRegion[0].y = oY - m_R-m_r;
      m_pRegion[1].x = oX - m_c;     m_pRegion[1].y = oY - m_R-m_r;
      m_pRegion[2].x = oX + m_c;     m_pRegion[2].y = oY - m_r;
      m_pRegion[3].x = oX + m_a;     m_pRegion[3].y = oY - m_R;
      break;
   case 2:
      m_pRegion[0].x = oX + m_b+m_c; m_pRegion[0].y = oY - m_R-m_r;
      m_pRegion[1].x = oX + m_c;     m_pRegion[1].y = oY - m_R-m_r;
      m_pRegion[2].x = oX + m_a;     m_pRegion[2].y = oY - m_R;
      m_pRegion[3].x = oX + m_b;     m_pRegion[3].y = oY - m_R;
      break;
   case 3:
      m_pRegion[0].x = oX - m_c;     m_pRegion[0].y = oY - m_r;
      m_pRegion[1].x = oX;           m_pRegion[1].y = oY;
      m_pRegion[2].x = oX - m_b;     m_pRegion[2].y = oY;
      m_pRegion[3].x = oX - m_a-m_c; m_pRegion[3].y = oY - m_r;
      break;
   case 4:
      m_pRegion[0].x = oX;           m_pRegion[0].y = oY - m_R;
      m_pRegion[1].x = oX + m_a;     m_pRegion[1].y = oY;
      m_pRegion[2].x = oX;           m_pRegion[2].y = oY;
      m_pRegion[3].x = oX - m_c;     m_pRegion[3].y = oY - m_r;
      break;
   case 5:
      m_pRegion[0].x = oX + m_b;     m_pRegion[0].y = oY - m_R;
      m_pRegion[1].x = oX + m_a;     m_pRegion[1].y = oY - m_R;
      m_pRegion[2].x = oX + m_c;     m_pRegion[2].y = oY - m_r;
      m_pRegion[3].x = oX + m_a;     m_pRegion[3].y = oY;
      break;
   case 6:
      m_pRegion[0].x = oX - m_a;     m_pRegion[0].y = oY;
      m_pRegion[1].x = oX - m_b;     m_pRegion[1].y = oY;
      m_pRegion[2].x = oX - m_a;     m_pRegion[2].y = oY + m_R;
      m_pRegion[3].x = oX - m_c;     m_pRegion[3].y = oY + m_r;
      break;
   case 7:
      m_pRegion[0].x = oX + m_a;     m_pRegion[0].y = oY;
      m_pRegion[1].x = oX - m_a;     m_pRegion[1].y = oY;
      m_pRegion[2].x = oX - m_c;     m_pRegion[2].y = oY + m_r;
      m_pRegion[3].x = oX + m_c;     m_pRegion[3].y = oY + m_r;
      break;
   case 8:
      m_pRegion[0].x = oX + m_a;     m_pRegion[0].y = oY;
      m_pRegion[1].x = oX + m_a+m_c; m_pRegion[1].y = oY + m_r;
      m_pRegion[2].x = oX + m_a;     m_pRegion[2].y = oY + m_R;
      m_pRegion[3].x = oX;           m_pRegion[3].y = oY + m_R;
      break;
   case 9:
      m_pRegion[0].x = oX + m_c;     m_pRegion[0].y = oY + m_r;
      m_pRegion[1].x = oX - m_c;     m_pRegion[1].y = oY + m_r;
      m_pRegion[2].x = oX - m_a;     m_pRegion[2].y = oY + m_R;
      m_pRegion[3].x = oX - m_c;     m_pRegion[3].y = oY + m_R+m_r;
      break;
   case 10:
      m_pRegion[0].x = oX + m_a;     m_pRegion[0].y = oY + m_R;
      m_pRegion[1].x = oX + m_a+m_c; m_pRegion[1].y = oY + m_R+m_r;
      m_pRegion[2].x = oX - m_c;     m_pRegion[2].y = oY + m_R+m_r;
      m_pRegion[3].x = oX;           m_pRegion[3].y = oY + m_R;
      break;
   case 11:
      m_pRegion[0].x = oX + m_a+m_c; m_pRegion[0].y = oY + m_r;
      m_pRegion[1].x = oX + m_b+m_c; m_pRegion[1].y = oY + m_R+m_r;
      m_pRegion[2].x = oX + m_a+m_c; m_pRegion[2].y = oY + m_R+m_r;
      m_pRegion[3].x = oX + m_a;     m_pRegion[3].y = oY + m_R;
      break;
   }
      
   // определение координат вписанного в фигуру квадрата - область в которую выводится изображение/текст
   {
      POINTFLOAT center; // координата вписанного в фигуру квадрата
      switch (m_iDirection) {
      case 0:  center.x = oX - m_c*1.25f; center.y = oY - m_r*1.75f; break;
      case 1:  center.x = oX + m_c*0.75f; center.y = oY - m_r*2.25f; break;
      case 2:  center.x = oX + m_c*3;     center.y = oY - m_r*2.50f; break;
      case 3:  center.x = oX - m_a;       center.y = oY - m_r*0.50f; break;
      case 4:  center.x = oX + m_c*0.25f; center.y = oY - m_r*0.75f; break;
      case 5:  center.x = oX + m_c*2.25f; center.y = oY - m_r*1.25f; break;
      case 6:  center.x = oX - m_c*2.25f; center.y = oY + m_r*0.75f; break;
      case 7:  center.x = oX;             center.y = oY + m_r*0.50f; break;
      case 8:  center.x = oX + m_c*1.75f; center.y = oY + m_r*1.25f; break;
      case 9:  center.x = oX - m_c*0.75f; center.y = oY + m_r*1.75f; break;
      case 10: center.x = oX + m_c;       center.y = oY + m_r*2.50f; break;
      case 11: center.x = oX + m_c*3.25f; center.y = oY + m_r*2.25f; break;
      }
      m_Square.left   = center.x - m_sq/2;
      m_Square.top    = center.y - m_sq/2;
      m_Square.right  = m_Square.left + m_sq;
      m_Square.bottom = m_Square.top  + m_sq;
   }
}

void nsCell::CTrapezoid1::Paint() const {
   int minX = min(m_pRegion[1].x, min(m_pRegion[2].x, m_pRegion[3].x));
   int maxX = max(m_pRegion[0].x, max(m_pRegion[1].x, m_pRegion[3].x));
   int minY = min(m_pRegion[0].y, min(m_pRegion[1].y, m_pRegion[3].y));
   int maxY = max(m_pRegion[1].y, max(m_pRegion[2].y, m_pRegion[3].y));
   BitBlt(m_GContext.m_hDCTmp, minX, minY, maxX - minX, maxY - minY,
          m_GContext.m_hDCBck, minX, minY, SRCCOPY);

   CBase::Paint();

   switch (m_iDirection) {
   case 0: case 3: case 4: case 8: case 10: case 11:  SelectObject(m_GContext.m_hDCTmp, m_bDown ? m_GContext.m_hPenLight  : m_GContext.m_hPenShadow); break;
   case 1: case 2: case 5: case 6: case 7 : case 9 :  SelectObject(m_GContext.m_hDCTmp, m_bDown ? m_GContext.m_hPenShadow : m_GContext.m_hPenLight ); break;
   }
   MoveToEx(m_GContext.m_hDCTmp, m_pRegion[0].x, m_pRegion[0].y, NULL);
   LineTo  (m_GContext.m_hDCTmp, m_pRegion[1].x, m_pRegion[1].y);
   LineTo  (m_GContext.m_hDCTmp, m_pRegion[2].x, m_pRegion[2].y);
   switch (m_iDirection) {
   case 3: case 4: case 10: case 11:   SelectObject(m_GContext.m_hDCTmp, m_bDown ? m_GContext.m_hPenShadow : m_GContext.m_hPenLight ); break;
   case 1: case 2: case 6 : case 7 :   SelectObject(m_GContext.m_hDCTmp, m_bDown ? m_GContext.m_hPenLight  : m_GContext.m_hPenShadow); break;
   }
   LineTo  (m_GContext.m_hDCTmp, m_pRegion[3].x, m_pRegion[3].y);
   switch (m_iDirection) {
   case 0: case 8:   SelectObject(m_GContext.m_hDCTmp, m_bDown ? m_GContext.m_hPenShadow : m_GContext.m_hPenLight ); break;
   case 5: case 9:   SelectObject(m_GContext.m_hDCTmp, m_bDown ? m_GContext.m_hPenLight  : m_GContext.m_hPenShadow); break;
   }
   LineTo  (m_GContext.m_hDCTmp, m_pRegion[0].x, m_pRegion[0].y);

   RegionDraw(
      m_GContext.m_hDCDst, true,
      m_pRegion, 4, NULL, NULL, 0, 0, 0, 0,
      m_GContext.m_hDCTmp, NULL, 0,0);
}
