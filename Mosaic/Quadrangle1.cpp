////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "Quadrangle1.cpp"
//
// –еализаци€ класса CQuadrangle1 - четырЄхугольник 120∞-90∞-60∞-90∞
////////////////////////////////////////////////////////////////////////////////

#include "StdAfx.h"
#include "Quadrangle1.h"

////////////////////////////////////////////////////////////////////////////////
//                               local init
////////////////////////////////////////////////////////////////////////////////

float nsCell::CQuadrangle1::m_b;
float nsCell::CQuadrangle1::m_h;
float nsCell::CQuadrangle1::m_n;
float nsCell::CQuadrangle1::m_m;
float nsCell::CQuadrangle1::m_Z;
float nsCell::CQuadrangle1::m_Zx;
float nsCell::CQuadrangle1::m_Zy;

////////////////////////////////////////////////////////////////////////////////
//                               static function
////////////////////////////////////////////////////////////////////////////////

SIZE nsCell::CQuadrangle1::GetSizeInPixel(const COORD &sizeField, int area) {
   m_a = sqrt(area/SQRT3)*2;
   m_b = m_a/2;
   m_h = m_b*SQRT3;
 //m_n = m_a*0.75f;
   m_m = m_h/2;

   SIZE result = {m_m + m_m*((sizeField.X+2)/3)+
                        m_h*((sizeField.X+1)/3)+
                        m_m*((sizeField.X+0)/3),
                  m_b + m_b*((sizeField.Y+1)/2)+
                        m_a*((sizeField.Y+0)/2)};
   return result;
}

int nsCell::CQuadrangle1::SizeInscribedSquare(int area, int borderWidth) {
   m_a = sqrt(area/SQRT3)*2;
   m_sq = (m_a*SQRT3-2*borderWidth*(1+SQRT3))/(SQRT3+2); // размер квадрата, вписанного в трапецию
   return m_sq;
}

////////////////////////////////////////////////////////////////////////////////
//                               virtual function
////////////////////////////////////////////////////////////////////////////////

nsCell::CQuadrangle1::CQuadrangle1(const COORD &Coord, const COORD &sizeField, int area, const CGraphicContext &gContext)
   : CBase(Coord, sizeField, area, gContext,
           9, 4,
           (Coord.Y&3)*3+(Coord.X%3) // 0..11
          )
{
   SetPoint(area);
   // определ€ю координаты соседей
   switch (m_iDirection) {
   case  0:
      m_pNeighbor[0].X = m_Coord.X-1;   m_pNeighbor[0].Y = m_Coord.Y-1;
      m_pNeighbor[1].X = m_Coord.X  ;   m_pNeighbor[1].Y = m_Coord.Y-1;
      m_pNeighbor[2].X = m_Coord.X-1;   m_pNeighbor[2].Y = m_Coord.Y  ;
      m_pNeighbor[3].X = m_Coord.X+1;   m_pNeighbor[3].Y = m_Coord.Y  ;
      m_pNeighbor[4].X = m_Coord.X+2;   m_pNeighbor[4].Y = m_Coord.Y  ;
      m_pNeighbor[5].X = m_Coord.X-1;   m_pNeighbor[5].Y = m_Coord.Y+1;
      m_pNeighbor[6].X = m_Coord.X  ;   m_pNeighbor[6].Y = m_Coord.Y+1;
      m_pNeighbor[7].X = m_Coord.X+1;   m_pNeighbor[7].Y = m_Coord.Y+1;
      m_pNeighbor[8].X = m_Coord.X+2;   m_pNeighbor[8].Y = m_Coord.Y+1;
      break;
   case  1:
      m_pNeighbor[0].X = m_Coord.X-2;   m_pNeighbor[0].Y = m_Coord.Y-1;
      m_pNeighbor[1].X = m_Coord.X-1;   m_pNeighbor[1].Y = m_Coord.Y-1;
      m_pNeighbor[2].X = m_Coord.X  ;   m_pNeighbor[2].Y = m_Coord.Y-1;
      m_pNeighbor[3].X = m_Coord.X+1;   m_pNeighbor[3].Y = m_Coord.Y-1;
      m_pNeighbor[4].X = m_Coord.X-1;   m_pNeighbor[4].Y = m_Coord.Y  ;
      m_pNeighbor[5].X = m_Coord.X+1;   m_pNeighbor[5].Y = m_Coord.Y  ;  
      m_pNeighbor[6].X = m_Coord.X-1;   m_pNeighbor[6].Y = m_Coord.Y+1;
      m_pNeighbor[7].X = m_Coord.X  ;   m_pNeighbor[7].Y = m_Coord.Y+1;
      m_pNeighbor[8].X = m_Coord.X+1;   m_pNeighbor[8].Y = m_Coord.Y+1;
      break;
   case  2:
      m_pNeighbor[0].X = m_Coord.X-1;   m_pNeighbor[0].Y = m_Coord.Y-1;
      m_pNeighbor[1].X = m_Coord.X  ;   m_pNeighbor[1].Y = m_Coord.Y-1;
      m_pNeighbor[2].X = m_Coord.X-2;   m_pNeighbor[2].Y = m_Coord.Y  ;
      m_pNeighbor[3].X = m_Coord.X-1;   m_pNeighbor[3].Y = m_Coord.Y  ;
      m_pNeighbor[4].X = m_Coord.X+1;   m_pNeighbor[4].Y = m_Coord.Y  ;
      m_pNeighbor[5].X = m_Coord.X-2;   m_pNeighbor[5].Y = m_Coord.Y+1;  
      m_pNeighbor[6].X = m_Coord.X-1;   m_pNeighbor[6].Y = m_Coord.Y+1;
      m_pNeighbor[7].X = m_Coord.X  ;   m_pNeighbor[7].Y = m_Coord.Y+1;
      m_pNeighbor[8].X = m_Coord.X+1;   m_pNeighbor[8].Y = m_Coord.Y+1;
      break;
   case  3:
      m_pNeighbor[0].X = m_Coord.X-1;   m_pNeighbor[0].Y = m_Coord.Y-1;
      m_pNeighbor[1].X = m_Coord.X  ;   m_pNeighbor[1].Y = m_Coord.Y-1;
      m_pNeighbor[2].X = m_Coord.X+1;   m_pNeighbor[2].Y = m_Coord.Y-1;
      m_pNeighbor[3].X = m_Coord.X+2;   m_pNeighbor[3].Y = m_Coord.Y-1;
      m_pNeighbor[4].X = m_Coord.X-1;   m_pNeighbor[4].Y = m_Coord.Y  ;
      m_pNeighbor[5].X = m_Coord.X+1;   m_pNeighbor[5].Y = m_Coord.Y  ;
      m_pNeighbor[6].X = m_Coord.X+2;   m_pNeighbor[6].Y = m_Coord.Y  ;
      m_pNeighbor[7].X = m_Coord.X-1;   m_pNeighbor[7].Y = m_Coord.Y+1;
      m_pNeighbor[8].X = m_Coord.X  ;   m_pNeighbor[8].Y = m_Coord.Y+1;
      break;
   case  4:
      m_pNeighbor[0].X = m_Coord.X-1;   m_pNeighbor[0].Y = m_Coord.Y-1;
      m_pNeighbor[1].X = m_Coord.X  ;   m_pNeighbor[1].Y = m_Coord.Y-1;
      m_pNeighbor[2].X = m_Coord.X+1;   m_pNeighbor[2].Y = m_Coord.Y-1;
      m_pNeighbor[3].X = m_Coord.X-1;   m_pNeighbor[3].Y = m_Coord.Y  ;
      m_pNeighbor[4].X = m_Coord.X+1;   m_pNeighbor[4].Y = m_Coord.Y  ;
      m_pNeighbor[5].X = m_Coord.X-2;   m_pNeighbor[5].Y = m_Coord.Y+1;
      m_pNeighbor[6].X = m_Coord.X-1;   m_pNeighbor[6].Y = m_Coord.Y+1;
      m_pNeighbor[7].X = m_Coord.X  ;   m_pNeighbor[7].Y = m_Coord.Y+1;
      m_pNeighbor[8].X = m_Coord.X+1;   m_pNeighbor[8].Y = m_Coord.Y+1;
      break;
   case  5:
      m_pNeighbor[0].X = m_Coord.X-2;   m_pNeighbor[0].Y = m_Coord.Y-1;
      m_pNeighbor[1].X = m_Coord.X-1;   m_pNeighbor[1].Y = m_Coord.Y-1;
      m_pNeighbor[2].X = m_Coord.X  ;   m_pNeighbor[2].Y = m_Coord.Y-1;
      m_pNeighbor[3].X = m_Coord.X+1;   m_pNeighbor[3].Y = m_Coord.Y-1;
      m_pNeighbor[4].X = m_Coord.X-2;   m_pNeighbor[4].Y = m_Coord.Y  ;
      m_pNeighbor[5].X = m_Coord.X-1;   m_pNeighbor[5].Y = m_Coord.Y  ;
      m_pNeighbor[6].X = m_Coord.X+1;   m_pNeighbor[6].Y = m_Coord.Y  ;
      m_pNeighbor[7].X = m_Coord.X-1;   m_pNeighbor[7].Y = m_Coord.Y+1;
      m_pNeighbor[8].X = m_Coord.X  ;   m_pNeighbor[8].Y = m_Coord.Y+1;
      break;
   case  6:
      m_pNeighbor[0].X = m_Coord.X  ;   m_pNeighbor[0].Y = m_Coord.Y-1;
      m_pNeighbor[1].X = m_Coord.X+1;   m_pNeighbor[1].Y = m_Coord.Y-1;
      m_pNeighbor[2].X = m_Coord.X-2;   m_pNeighbor[2].Y = m_Coord.Y  ;
      m_pNeighbor[3].X = m_Coord.X-1;   m_pNeighbor[3].Y = m_Coord.Y  ;
      m_pNeighbor[4].X = m_Coord.X+1;   m_pNeighbor[4].Y = m_Coord.Y  ;
      m_pNeighbor[5].X = m_Coord.X-2;   m_pNeighbor[5].Y = m_Coord.Y+1;
      m_pNeighbor[6].X = m_Coord.X-1;   m_pNeighbor[6].Y = m_Coord.Y+1;
      m_pNeighbor[7].X = m_Coord.X  ;   m_pNeighbor[7].Y = m_Coord.Y+1;
      m_pNeighbor[8].X = m_Coord.X+1;   m_pNeighbor[8].Y = m_Coord.Y+1;
      break;
   case  7:
      m_pNeighbor[0].X = m_Coord.X  ;   m_pNeighbor[0].Y = m_Coord.Y-1;
      m_pNeighbor[1].X = m_Coord.X+1;   m_pNeighbor[1].Y = m_Coord.Y-1;
      m_pNeighbor[2].X = m_Coord.X-1;   m_pNeighbor[2].Y = m_Coord.Y  ;
      m_pNeighbor[3].X = m_Coord.X+1;   m_pNeighbor[3].Y = m_Coord.Y  ;
      m_pNeighbor[4].X = m_Coord.X+2;   m_pNeighbor[4].Y = m_Coord.Y  ;
      m_pNeighbor[5].X = m_Coord.X-1;   m_pNeighbor[5].Y = m_Coord.Y+1;
      m_pNeighbor[6].X = m_Coord.X  ;   m_pNeighbor[6].Y = m_Coord.Y+1;
      m_pNeighbor[7].X = m_Coord.X+1;   m_pNeighbor[7].Y = m_Coord.Y+1;
      m_pNeighbor[8].X = m_Coord.X+2;   m_pNeighbor[8].Y = m_Coord.Y+1;
      break;
   case  8:
      m_pNeighbor[0].X = m_Coord.X-1;   m_pNeighbor[0].Y = m_Coord.Y-1;
      m_pNeighbor[1].X = m_Coord.X  ;   m_pNeighbor[1].Y = m_Coord.Y-1;
      m_pNeighbor[2].X = m_Coord.X+1;   m_pNeighbor[2].Y = m_Coord.Y-1;
      m_pNeighbor[3].X = m_Coord.X+2;   m_pNeighbor[3].Y = m_Coord.Y-1;
      m_pNeighbor[4].X = m_Coord.X-1;   m_pNeighbor[4].Y = m_Coord.Y  ;
      m_pNeighbor[5].X = m_Coord.X+1;   m_pNeighbor[5].Y = m_Coord.Y  ;
      m_pNeighbor[6].X = m_Coord.X-1;   m_pNeighbor[6].Y = m_Coord.Y+1;
      m_pNeighbor[7].X = m_Coord.X  ;   m_pNeighbor[7].Y = m_Coord.Y+1;
      m_pNeighbor[8].X = m_Coord.X+1;   m_pNeighbor[8].Y = m_Coord.Y+1;
      break;
   case  9:
      m_pNeighbor[0].X = m_Coord.X-2;   m_pNeighbor[0].Y = m_Coord.Y-1;
      m_pNeighbor[1].X = m_Coord.X-1;   m_pNeighbor[1].Y = m_Coord.Y-1;
      m_pNeighbor[2].X = m_Coord.X  ;   m_pNeighbor[2].Y = m_Coord.Y-1;
      m_pNeighbor[3].X = m_Coord.X+1;   m_pNeighbor[3].Y = m_Coord.Y-1;
      m_pNeighbor[4].X = m_Coord.X-2;   m_pNeighbor[4].Y = m_Coord.Y  ;
      m_pNeighbor[5].X = m_Coord.X-1;   m_pNeighbor[5].Y = m_Coord.Y  ;
      m_pNeighbor[6].X = m_Coord.X+1;   m_pNeighbor[6].Y = m_Coord.Y  ;
      m_pNeighbor[7].X = m_Coord.X  ;   m_pNeighbor[7].Y = m_Coord.Y+1;
      m_pNeighbor[8].X = m_Coord.X+1;   m_pNeighbor[8].Y = m_Coord.Y+1;
      break;
   case 10:
      m_pNeighbor[0].X = m_Coord.X-1;   m_pNeighbor[0].Y = m_Coord.Y-1;
      m_pNeighbor[1].X = m_Coord.X  ;   m_pNeighbor[1].Y = m_Coord.Y-1;
      m_pNeighbor[2].X = m_Coord.X+1;   m_pNeighbor[2].Y = m_Coord.Y-1;
      m_pNeighbor[3].X = m_Coord.X+2;   m_pNeighbor[3].Y = m_Coord.Y-1;
      m_pNeighbor[4].X = m_Coord.X-1;   m_pNeighbor[4].Y = m_Coord.Y  ;
      m_pNeighbor[5].X = m_Coord.X+1;   m_pNeighbor[5].Y = m_Coord.Y  ;
      m_pNeighbor[6].X = m_Coord.X+2;   m_pNeighbor[6].Y = m_Coord.Y  ;
      m_pNeighbor[7].X = m_Coord.X  ;   m_pNeighbor[7].Y = m_Coord.Y+1;
      m_pNeighbor[8].X = m_Coord.X+1;   m_pNeighbor[8].Y = m_Coord.Y+1;
      break;
   case 11:
      m_pNeighbor[0].X = m_Coord.X-1;   m_pNeighbor[0].Y = m_Coord.Y-1;
      m_pNeighbor[1].X = m_Coord.X  ;   m_pNeighbor[1].Y = m_Coord.Y-1;
      m_pNeighbor[2].X = m_Coord.X+1;   m_pNeighbor[2].Y = m_Coord.Y-1;
      m_pNeighbor[3].X = m_Coord.X-1;   m_pNeighbor[3].Y = m_Coord.Y  ;
      m_pNeighbor[4].X = m_Coord.X+1;   m_pNeighbor[4].Y = m_Coord.Y  ;
      m_pNeighbor[5].X = m_Coord.X-1;   m_pNeighbor[5].Y = m_Coord.Y+1;
      m_pNeighbor[6].X = m_Coord.X  ;   m_pNeighbor[6].Y = m_Coord.Y+1;
      m_pNeighbor[7].X = m_Coord.X+1;   m_pNeighbor[7].Y = m_Coord.Y+1;
      m_pNeighbor[8].X = m_Coord.X+2;   m_pNeighbor[8].Y = m_Coord.Y+1;
      break;
   }
   VerifyNeighbor(sizeField);
}

void nsCell::CQuadrangle1::SetPoint(int area) {
   if (m_Coord.X==0 && m_Coord.Y==0) {
      m_a = sqrt(area/SQRT3)*2;
      m_b = m_a/2;
      m_h = m_b*SQRT3;
      m_n = m_a*0.75f;
      m_m = m_h/2;
      m_sq = (m_a*SQRT3-2*m_GContext.m_Border.m_iWidth*(1+SQRT3))/(SQRT3+2); // размер квадрата, вписанного в трапецию
      m_Z  = m_a/(1+SQRT3);
      m_Zx = m_Z*SQRT3/2;
      m_Zy = m_Z/2; 
   }

   // определение координат точек фигуры
   float oX = (m_h*2)*(m_Coord.X/3) + m_h+m_m; // offset X
   float oY = (m_a*3)*(m_Coord.Y/4) + m_a+m_n; // offset Y
   switch (m_iDirection) {
   case 0:
      m_pRegion[0].x = oX - m_h;     m_pRegion[0].y = oY - m_n-m_n;
      m_pRegion[1].x = oX - m_m;     m_pRegion[1].y = oY - m_n;
      m_pRegion[2].x = oX - m_h-m_m; m_pRegion[2].y = oY - m_n;
      m_pRegion[3].x = oX - m_h-m_m; m_pRegion[3].y = oY - m_n-m_b;
      break;
   case 1:
      m_pRegion[0].x = oX;           m_pRegion[0].y = oY - m_n-m_n;
      m_pRegion[1].x = oX - m_m;     m_pRegion[1].y = oY - m_n-m_a;
      m_pRegion[2].x = oX - m_h;     m_pRegion[2].y = oY - m_n-m_n;
      m_pRegion[3].x = oX - m_m;     m_pRegion[3].y = oY - m_n;
      break;
   case 2:
      m_pRegion[0].x = oX + m_m;     m_pRegion[0].y = oY - m_n-m_b;
      m_pRegion[1].x = oX;           m_pRegion[1].y = oY - m_n-m_n;
      m_pRegion[2].x = oX - m_m;     m_pRegion[2].y = oY - m_n;
      m_pRegion[3].x = oX + m_m;     m_pRegion[3].y = oY - m_n;
      break;
   case 3:
      m_pRegion[0].x = oX - m_m;     m_pRegion[0].y = oY - m_n;
      m_pRegion[1].x = oX - m_h;     m_pRegion[1].y = oY;
      m_pRegion[2].x = oX - m_h-m_m; m_pRegion[2].y = oY - m_n+m_b;
      m_pRegion[3].x = oX - m_h-m_m; m_pRegion[3].y = oY - m_n;
      break;
   case 4:
      m_pRegion[0].x = oX - m_m;     m_pRegion[0].y = oY - m_n;
      m_pRegion[1].x = oX;           m_pRegion[1].y = oY;
      m_pRegion[2].x = oX - m_m;     m_pRegion[2].y = oY - m_n+m_a;
      m_pRegion[3].x = oX - m_h;     m_pRegion[3].y = oY;
      break;
   case 5:
      m_pRegion[0].x = oX + m_m;     m_pRegion[0].y = oY - m_n;
      m_pRegion[1].x = oX - m_m;     m_pRegion[1].y = oY - m_n;
      m_pRegion[2].x = oX;           m_pRegion[2].y = oY;
      m_pRegion[3].x = oX + m_m;     m_pRegion[3].y = oY - m_n+m_b;
      break;
   case 6:
      m_pRegion[0].x = oX - m_m;     m_pRegion[0].y = oY + m_n-m_b;
      m_pRegion[1].x = oX - m_h;     m_pRegion[1].y = oY;
      m_pRegion[2].x = oX - m_h-m_m; m_pRegion[2].y = oY + m_n;
      m_pRegion[3].x = oX - m_m;     m_pRegion[3].y = oY + m_n;
      break;
   case 7:
      m_pRegion[0].x = oX;           m_pRegion[0].y = oY;
      m_pRegion[1].x = oX + m_m;     m_pRegion[1].y = oY + m_n;
      m_pRegion[2].x = oX - m_m;     m_pRegion[2].y = oY + m_n;
      m_pRegion[3].x = oX - m_m;     m_pRegion[3].y = oY + m_n-m_b;
      break;
   case 8:
      m_pRegion[0].x = oX + m_h;     m_pRegion[0].y = oY;
      m_pRegion[1].x = oX + m_m;     m_pRegion[1].y = oY + m_n-m_a;
      m_pRegion[2].x = oX;           m_pRegion[2].y = oY;
      m_pRegion[3].x = oX + m_m;     m_pRegion[3].y = oY + m_n;
      break;
   case 9:
      m_pRegion[0].x = oX - m_m;     m_pRegion[0].y = oY + m_n;
      m_pRegion[1].x = oX - m_h-m_m; m_pRegion[1].y = oY + m_n;
      m_pRegion[2].x = oX - m_h;     m_pRegion[2].y = oY + m_n+m_n;
      m_pRegion[3].x = oX - m_m;     m_pRegion[3].y = oY + m_n+m_b;
      break;
   case 10:
      m_pRegion[0].x = oX + m_m;     m_pRegion[0].y = oY + m_n;
      m_pRegion[1].x = oX;           m_pRegion[1].y = oY + m_n+m_n;
      m_pRegion[2].x = oX - m_m;     m_pRegion[2].y = oY + m_n+m_b;
      m_pRegion[3].x = oX - m_m;     m_pRegion[3].y = oY + m_n;
      break;
   case 11:
      m_pRegion[0].x = oX + m_m;     m_pRegion[0].y = oY + m_n;
      m_pRegion[1].x = oX + m_h;     m_pRegion[1].y = oY + m_n+m_n;
      m_pRegion[2].x = oX + m_m;     m_pRegion[2].y = oY + m_n+m_a;
      m_pRegion[3].x = oX;           m_pRegion[3].y = oY + m_n+m_n;
      break;
   }

   // определение координат вписанного в фигуру квадрата - область в которую выводитс€ изображение/текст
   {
      POINTFLOAT center; // координата центра вписанного в фигуру квадрата
      switch (m_iDirection) {
      case 0:  center.x = oX - m_h-m_m+m_Zx; center.y = oY - m_n-m_b+m_Zy; break;
      case 1:  center.x = oX - m_m;          center.y = oY - m_n-m_a+m_Z;  break;
      case 2:  center.x = oX + m_m    -m_Zx; center.y = oY - m_n-m_b+m_Zy; break;
      case 3:  center.x = oX - m_h-m_m+m_Zx; center.y = oY - m_n+m_b-m_Zy; break;
      case 4:  center.x = oX - m_m;          center.y = oY - m_n+m_a-m_Z;  break;
      case 5:  center.x = oX + m_m    -m_Zx; center.y = oY - m_n+m_b-m_Zy; break;
      case 6:  center.x = oX - m_m    -m_Zx; center.y = oY + m_n-m_b+m_Zy; break;
      case 7:  center.x = oX - m_m    +m_Zx; center.y = oY + m_n-m_b+m_Zy; break;
      case 8:  center.x = oX + m_m;          center.y = oY + m_n-m_a+m_Z;  break;
      case 9:  center.x = oX - m_m    -m_Zx; center.y = oY + m_n+m_b-m_Zy; break;
      case 10: center.x = oX - m_m    +m_Zx; center.y = oY + m_n+m_b-m_Zy; break;
      case 11: center.x = oX + m_m;          center.y = oY + m_n+m_a-m_Z;  break;
      }
      m_Square.left   = center.x - m_sq/2;
      m_Square.top    = center.y - m_sq/2;
      m_Square.right  = m_Square.left + m_sq;
      m_Square.bottom = m_Square.top  + m_sq;
   }
}

void nsCell::CQuadrangle1::Paint() const {
   /**/
   int minX = min(m_pRegion[1].x, min(m_pRegion[2].x, m_pRegion[3].x));
   int maxX = max(m_pRegion[0].x, m_pRegion[1].x);
   int minY = min(m_pRegion[0].y, min(m_pRegion[1].y, m_pRegion[3].y));
   int maxY = max(m_pRegion[1].y, max(m_pRegion[2].y, m_pRegion[3].y));
   BitBlt(m_GContext.m_hDCTmp, minX, minY, maxX - minX, maxY - minY,
          m_GContext.m_hDCBck, minX, minY, SRCCOPY);
   /**/
   CBase::Paint();

   switch (m_iDirection) {
   case 0: case 7:
      SelectObject(m_GContext.m_hDCTmp, m_bDown ? m_GContext.m_hPenLight  : m_GContext.m_hPenShadow);
      MoveToEx(m_GContext.m_hDCTmp, m_pRegion[0].x, m_pRegion[0].y, NULL);
      LineTo  (m_GContext.m_hDCTmp, m_pRegion[1].x, m_pRegion[1].y);
      LineTo  (m_GContext.m_hDCTmp, m_pRegion[2].x, m_pRegion[2].y);
      SelectObject(m_GContext.m_hDCTmp, m_bDown ? m_GContext.m_hPenShadow : m_GContext.m_hPenLight );
      LineTo  (m_GContext.m_hDCTmp, m_pRegion[3].x, m_pRegion[3].y);
      LineTo  (m_GContext.m_hDCTmp, m_pRegion[0].x, m_pRegion[0].y);
      break;
   case 1: case 8:
      SelectObject(m_GContext.m_hDCTmp, m_bDown ? m_GContext.m_hPenShadow : m_GContext.m_hPenLight );
      MoveToEx(m_GContext.m_hDCTmp, m_pRegion[0].x, m_pRegion[0].y, NULL);
      LineTo  (m_GContext.m_hDCTmp, m_pRegion[1].x, m_pRegion[1].y);
      LineTo  (m_GContext.m_hDCTmp, m_pRegion[2].x, m_pRegion[2].y);
      LineTo  (m_GContext.m_hDCTmp, m_pRegion[3].x, m_pRegion[3].y);
      SelectObject(m_GContext.m_hDCTmp, m_bDown ? m_GContext.m_hPenLight  : m_GContext.m_hPenShadow);
      LineTo  (m_GContext.m_hDCTmp, m_pRegion[0].x, m_pRegion[0].y);
      break;
   case 2: case 6:
      SelectObject(m_GContext.m_hDCTmp, m_bDown ? m_GContext.m_hPenShadow : m_GContext.m_hPenLight );
      MoveToEx(m_GContext.m_hDCTmp, m_pRegion[0].x, m_pRegion[0].y, NULL);
      LineTo  (m_GContext.m_hDCTmp, m_pRegion[1].x, m_pRegion[1].y);
      LineTo  (m_GContext.m_hDCTmp, m_pRegion[2].x, m_pRegion[2].y);
      SelectObject(m_GContext.m_hDCTmp, m_bDown ? m_GContext.m_hPenLight  : m_GContext.m_hPenShadow);
      LineTo  (m_GContext.m_hDCTmp, m_pRegion[3].x, m_pRegion[3].y);
      LineTo  (m_GContext.m_hDCTmp, m_pRegion[0].x, m_pRegion[0].y);
      break;
   case 3: case 10:
      SelectObject(m_GContext.m_hDCTmp, m_bDown ? m_GContext.m_hPenLight  : m_GContext.m_hPenShadow);
      MoveToEx(m_GContext.m_hDCTmp, m_pRegion[0].x, m_pRegion[0].y, NULL);
      LineTo  (m_GContext.m_hDCTmp, m_pRegion[1].x, m_pRegion[1].y);
      LineTo  (m_GContext.m_hDCTmp, m_pRegion[2].x, m_pRegion[2].y);
      SelectObject(m_GContext.m_hDCTmp, m_bDown ? m_GContext.m_hPenShadow : m_GContext.m_hPenLight );
      LineTo  (m_GContext.m_hDCTmp, m_pRegion[3].x, m_pRegion[3].y);
      LineTo  (m_GContext.m_hDCTmp, m_pRegion[0].x, m_pRegion[0].y);
      break;
   case 4: case 11:
      SelectObject(m_GContext.m_hDCTmp, m_bDown ? m_GContext.m_hPenLight  : m_GContext.m_hPenShadow);
      MoveToEx(m_GContext.m_hDCTmp, m_pRegion[0].x, m_pRegion[0].y, NULL);
      LineTo  (m_GContext.m_hDCTmp, m_pRegion[1].x, m_pRegion[1].y);
      LineTo  (m_GContext.m_hDCTmp, m_pRegion[2].x, m_pRegion[2].y);
      LineTo  (m_GContext.m_hDCTmp, m_pRegion[3].x, m_pRegion[3].y);
      SelectObject(m_GContext.m_hDCTmp, m_bDown ? m_GContext.m_hPenShadow : m_GContext.m_hPenLight );
      LineTo  (m_GContext.m_hDCTmp, m_pRegion[0].x, m_pRegion[0].y);
      break;
   case 5: case 9:
      SelectObject(m_GContext.m_hDCTmp, m_bDown ? m_GContext.m_hPenShadow : m_GContext.m_hPenLight );
      MoveToEx(m_GContext.m_hDCTmp, m_pRegion[0].x, m_pRegion[0].y, NULL);
      LineTo  (m_GContext.m_hDCTmp, m_pRegion[1].x, m_pRegion[1].y);
      LineTo  (m_GContext.m_hDCTmp, m_pRegion[2].x, m_pRegion[2].y);
      SelectObject(m_GContext.m_hDCTmp, m_bDown ? m_GContext.m_hPenLight  : m_GContext.m_hPenShadow);
      LineTo  (m_GContext.m_hDCTmp, m_pRegion[3].x, m_pRegion[3].y);
      LineTo  (m_GContext.m_hDCTmp, m_pRegion[0].x, m_pRegion[0].y);
      break;
   }

   RegionDraw(
      m_GContext.m_hDCDst, true,
      m_pRegion, 4, NULL, NULL, 0, 0, 0, 0,
      m_GContext.m_hDCTmp, NULL, 0,0);
}
