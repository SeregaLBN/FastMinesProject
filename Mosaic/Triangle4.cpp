////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "Triangle4.cpp"
//
// –еализаци€ класса CTriangle4 - треугольник 30∞-30∞-120∞
////////////////////////////////////////////////////////////////////////////////

#include "StdAfx.h"
#include "Triangle4.h"

////////////////////////////////////////////////////////////////////////////////
//                               local init
////////////////////////////////////////////////////////////////////////////////

float nsCell::CTriangle4::m_b;
float nsCell::CTriangle4::m_R;
float nsCell::CTriangle4::m_r;

////////////////////////////////////////////////////////////////////////////////
//                               static function
////////////////////////////////////////////////////////////////////////////////

SIZE nsCell::CTriangle4::GetSizeInPixel(const COORD &sizeField, int area) {
   m_a = sqrt(area*SQRT48);
   m_b = m_a/2;
   m_R = m_a/SQRT3;
   m_r = m_R/2;

   SIZE result = {m_b + m_b*((sizeField.X+2)/3)+
                        m_b*((sizeField.X+0)/3),
                  (m_R+m_r)*((sizeField.Y+1)/2)};
   return result;
}

int nsCell::CTriangle4::SizeInscribedSquare(int area, int borderWidth) {
   m_a = sqrt(area*SQRT48);
   m_sq = (m_a-borderWidth*2/TAN15)/(SQRT3+3);
   return m_sq;
}

////////////////////////////////////////////////////////////////////////////////
//                               virtual function
////////////////////////////////////////////////////////////////////////////////

nsCell::CTriangle4::CTriangle4(const COORD &Coord, const COORD &sizeField, int area, const CGraphicContext &gContext)
   : CBase(Coord, sizeField, area, gContext,
           21, 3,
           (Coord.Y&3)*3+(Coord.X%3) // 0..11
          )
{
   SetPoint(area);
   // определ€ю координаты соседей
   switch (m_iDirection) {
   case  0:
      m_pNeighbor[ 0].X = m_Coord.X-1;   m_pNeighbor[ 0].Y = m_Coord.Y-2;
      m_pNeighbor[ 1].X = m_Coord.X+1;   m_pNeighbor[ 1].Y = m_Coord.Y-2;
      m_pNeighbor[ 2].X = m_Coord.X-1;   m_pNeighbor[ 2].Y = m_Coord.Y-1;
      m_pNeighbor[ 3].X = m_Coord.X  ;   m_pNeighbor[ 3].Y = m_Coord.Y-1;
      m_pNeighbor[ 4].X = m_Coord.X+1;   m_pNeighbor[ 4].Y = m_Coord.Y-1;
      m_pNeighbor[ 5].X = m_Coord.X+2;   m_pNeighbor[ 5].Y = m_Coord.Y-1;
      m_pNeighbor[ 6].X = m_Coord.X-2;   m_pNeighbor[ 6].Y = m_Coord.Y  ;
      m_pNeighbor[ 7].X = m_Coord.X-1;   m_pNeighbor[ 7].Y = m_Coord.Y  ;
      m_pNeighbor[ 8].X = m_Coord.X+1;   m_pNeighbor[ 8].Y = m_Coord.Y  ;
      m_pNeighbor[ 9].X = m_Coord.X+2;   m_pNeighbor[ 9].Y = m_Coord.Y  ;
      m_pNeighbor[10].X = m_Coord.X-3;   m_pNeighbor[10].Y = m_Coord.Y+1;
      m_pNeighbor[11].X = m_Coord.X-2;   m_pNeighbor[11].Y = m_Coord.Y+1;
      m_pNeighbor[12].X = m_Coord.X-1;   m_pNeighbor[12].Y = m_Coord.Y+1;
      m_pNeighbor[13].X = m_Coord.X  ;   m_pNeighbor[13].Y = m_Coord.Y+1;
      m_pNeighbor[14].X = m_Coord.X+1;   m_pNeighbor[14].Y = m_Coord.Y+1;
      m_pNeighbor[15].X = m_Coord.X-3;   m_pNeighbor[15].Y = m_Coord.Y+2;
      m_pNeighbor[16].X = m_Coord.X-2;   m_pNeighbor[16].Y = m_Coord.Y+2;
      m_pNeighbor[17].X = m_Coord.X-1;   m_pNeighbor[17].Y = m_Coord.Y+2;
      m_pNeighbor[18].X = m_Coord.X  ;   m_pNeighbor[18].Y = m_Coord.Y+2;
      m_pNeighbor[19].X = m_Coord.X-2;   m_pNeighbor[19].Y = m_Coord.Y+3;
      m_pNeighbor[20].X = m_Coord.X  ;   m_pNeighbor[20].Y = m_Coord.Y+3;
      break;
   case  1:
      m_pNeighbor[ 0].X = m_Coord.X-2;   m_pNeighbor[ 0].Y = m_Coord.Y-2;
      m_pNeighbor[ 1].X = m_Coord.X  ;   m_pNeighbor[ 1].Y = m_Coord.Y-2;
      m_pNeighbor[ 2].X = m_Coord.X-2;   m_pNeighbor[ 2].Y = m_Coord.Y-1;
      m_pNeighbor[ 3].X = m_Coord.X-1;   m_pNeighbor[ 3].Y = m_Coord.Y-1;
      m_pNeighbor[ 4].X = m_Coord.X  ;   m_pNeighbor[ 4].Y = m_Coord.Y-1;
      m_pNeighbor[ 5].X = m_Coord.X+1;   m_pNeighbor[ 5].Y = m_Coord.Y-1;
      m_pNeighbor[ 6].X = m_Coord.X-2;   m_pNeighbor[ 6].Y = m_Coord.Y  ;
      m_pNeighbor[ 7].X = m_Coord.X-1;   m_pNeighbor[ 7].Y = m_Coord.Y  ;
      m_pNeighbor[ 8].X = m_Coord.X+1;   m_pNeighbor[ 8].Y = m_Coord.Y  ;
      m_pNeighbor[ 9].X = m_Coord.X+2;   m_pNeighbor[ 9].Y = m_Coord.Y  ;
      m_pNeighbor[10].X = m_Coord.X-2;   m_pNeighbor[10].Y = m_Coord.Y+1;
      m_pNeighbor[11].X = m_Coord.X-1;   m_pNeighbor[11].Y = m_Coord.Y+1;
      m_pNeighbor[12].X = m_Coord.X  ;   m_pNeighbor[12].Y = m_Coord.Y+1;
      m_pNeighbor[13].X = m_Coord.X+1;   m_pNeighbor[13].Y = m_Coord.Y+1;
      m_pNeighbor[14].X = m_Coord.X+2;   m_pNeighbor[14].Y = m_Coord.Y+1;
      m_pNeighbor[15].X = m_Coord.X-1;   m_pNeighbor[15].Y = m_Coord.Y+2;
      m_pNeighbor[16].X = m_Coord.X  ;   m_pNeighbor[16].Y = m_Coord.Y+2;
      m_pNeighbor[17].X = m_Coord.X+1;   m_pNeighbor[17].Y = m_Coord.Y+2;
      m_pNeighbor[18].X = m_Coord.X+2;   m_pNeighbor[18].Y = m_Coord.Y+2;
      m_pNeighbor[19].X = m_Coord.X  ;   m_pNeighbor[19].Y = m_Coord.Y+3;
      m_pNeighbor[20].X = m_Coord.X+2;   m_pNeighbor[20].Y = m_Coord.Y+3;
      break;
   case  2:
      m_pNeighbor[ 0].X = m_Coord.X-3;   m_pNeighbor[ 0].Y = m_Coord.Y-2;
      m_pNeighbor[ 1].X = m_Coord.X-1;   m_pNeighbor[ 1].Y = m_Coord.Y-2;
      m_pNeighbor[ 2].X = m_Coord.X  ;   m_pNeighbor[ 2].Y = m_Coord.Y-2;
      m_pNeighbor[ 3].X = m_Coord.X+2;   m_pNeighbor[ 3].Y = m_Coord.Y-2;
      m_pNeighbor[ 4].X = m_Coord.X-3;   m_pNeighbor[ 4].Y = m_Coord.Y-1;
      m_pNeighbor[ 5].X = m_Coord.X-2;   m_pNeighbor[ 5].Y = m_Coord.Y-1;
      m_pNeighbor[ 6].X = m_Coord.X-1;   m_pNeighbor[ 6].Y = m_Coord.Y-1;
      m_pNeighbor[ 7].X = m_Coord.X  ;   m_pNeighbor[ 7].Y = m_Coord.Y-1;
      m_pNeighbor[ 8].X = m_Coord.X+1;   m_pNeighbor[ 8].Y = m_Coord.Y-1;
      m_pNeighbor[ 9].X = m_Coord.X+2;   m_pNeighbor[ 9].Y = m_Coord.Y-1;
      m_pNeighbor[10].X = m_Coord.X+3;   m_pNeighbor[10].Y = m_Coord.Y-1;
      m_pNeighbor[11].X = m_Coord.X-3;   m_pNeighbor[11].Y = m_Coord.Y  ;
      m_pNeighbor[12].X = m_Coord.X-2;   m_pNeighbor[12].Y = m_Coord.Y  ;
      m_pNeighbor[13].X = m_Coord.X-1;   m_pNeighbor[13].Y = m_Coord.Y  ;
      m_pNeighbor[14].X = m_Coord.X+1;   m_pNeighbor[14].Y = m_Coord.Y  ;
      m_pNeighbor[15].X = m_Coord.X+2;   m_pNeighbor[15].Y = m_Coord.Y  ;
      m_pNeighbor[16].X = m_Coord.X+3;   m_pNeighbor[16].Y = m_Coord.Y  ;
      m_pNeighbor[17].X = m_Coord.X-3;   m_pNeighbor[17].Y = m_Coord.Y+1;
      m_pNeighbor[18].X = m_Coord.X-1;   m_pNeighbor[18].Y = m_Coord.Y+1;
      m_pNeighbor[19].X = m_Coord.X  ;   m_pNeighbor[19].Y = m_Coord.Y+1;
      m_pNeighbor[20].X = m_Coord.X+2;   m_pNeighbor[20].Y = m_Coord.Y+1;
      break;
   case  3:
      m_pNeighbor[ 0].X = m_Coord.X-2;   m_pNeighbor[ 0].Y = m_Coord.Y-1;
      m_pNeighbor[ 1].X = m_Coord.X  ;   m_pNeighbor[ 1].Y = m_Coord.Y-1;
      m_pNeighbor[ 2].X = m_Coord.X+1;   m_pNeighbor[ 2].Y = m_Coord.Y-1;
      m_pNeighbor[ 3].X = m_Coord.X+3;   m_pNeighbor[ 3].Y = m_Coord.Y-1;
      m_pNeighbor[ 4].X = m_Coord.X-3;   m_pNeighbor[ 4].Y = m_Coord.Y  ;
      m_pNeighbor[ 5].X = m_Coord.X-2;   m_pNeighbor[ 5].Y = m_Coord.Y  ;
      m_pNeighbor[ 6].X = m_Coord.X-1;   m_pNeighbor[ 6].Y = m_Coord.Y  ;
      m_pNeighbor[ 7].X = m_Coord.X+1;   m_pNeighbor[ 7].Y = m_Coord.Y  ;
      m_pNeighbor[ 8].X = m_Coord.X+2;   m_pNeighbor[ 8].Y = m_Coord.Y  ;
      m_pNeighbor[ 9].X = m_Coord.X+3;   m_pNeighbor[ 9].Y = m_Coord.Y  ;
      m_pNeighbor[10].X = m_Coord.X-3;   m_pNeighbor[10].Y = m_Coord.Y+1;
      m_pNeighbor[11].X = m_Coord.X-2;   m_pNeighbor[11].Y = m_Coord.Y+1;
      m_pNeighbor[12].X = m_Coord.X-1;   m_pNeighbor[12].Y = m_Coord.Y+1;
      m_pNeighbor[13].X = m_Coord.X  ;   m_pNeighbor[13].Y = m_Coord.Y+1;
      m_pNeighbor[14].X = m_Coord.X+1;   m_pNeighbor[14].Y = m_Coord.Y+1;
      m_pNeighbor[15].X = m_Coord.X+2;   m_pNeighbor[15].Y = m_Coord.Y+1;
      m_pNeighbor[16].X = m_Coord.X+3;   m_pNeighbor[16].Y = m_Coord.Y+1;
      m_pNeighbor[17].X = m_Coord.X-2;   m_pNeighbor[17].Y = m_Coord.Y+2;
      m_pNeighbor[18].X = m_Coord.X  ;   m_pNeighbor[18].Y = m_Coord.Y+2;
      m_pNeighbor[19].X = m_Coord.X+1;   m_pNeighbor[19].Y = m_Coord.Y+2;
      m_pNeighbor[20].X = m_Coord.X+3;   m_pNeighbor[20].Y = m_Coord.Y+2;
      break;
   case  4:
      m_pNeighbor[ 0].X = m_Coord.X-2;   m_pNeighbor[ 0].Y = m_Coord.Y-3;
      m_pNeighbor[ 1].X = m_Coord.X  ;   m_pNeighbor[ 1].Y = m_Coord.Y-3;
      m_pNeighbor[ 2].X = m_Coord.X-2;   m_pNeighbor[ 2].Y = m_Coord.Y-2;
      m_pNeighbor[ 3].X = m_Coord.X-1;   m_pNeighbor[ 3].Y = m_Coord.Y-2;
      m_pNeighbor[ 4].X = m_Coord.X  ;   m_pNeighbor[ 4].Y = m_Coord.Y-2;
      m_pNeighbor[ 5].X = m_Coord.X+1;   m_pNeighbor[ 5].Y = m_Coord.Y-2;
      m_pNeighbor[ 6].X = m_Coord.X-2;   m_pNeighbor[ 6].Y = m_Coord.Y-1;
      m_pNeighbor[ 7].X = m_Coord.X-1;   m_pNeighbor[ 7].Y = m_Coord.Y-1;
      m_pNeighbor[ 8].X = m_Coord.X  ;   m_pNeighbor[ 8].Y = m_Coord.Y-1;
      m_pNeighbor[ 9].X = m_Coord.X+1;   m_pNeighbor[ 9].Y = m_Coord.Y-1;
      m_pNeighbor[10].X = m_Coord.X+2;   m_pNeighbor[10].Y = m_Coord.Y-1;
      m_pNeighbor[11].X = m_Coord.X-2;   m_pNeighbor[11].Y = m_Coord.Y  ;
      m_pNeighbor[12].X = m_Coord.X-1;   m_pNeighbor[12].Y = m_Coord.Y  ;
      m_pNeighbor[13].X = m_Coord.X+1;   m_pNeighbor[13].Y = m_Coord.Y  ;
      m_pNeighbor[14].X = m_Coord.X+2;   m_pNeighbor[14].Y = m_Coord.Y  ;
      m_pNeighbor[15].X = m_Coord.X-1;   m_pNeighbor[15].Y = m_Coord.Y+1;
      m_pNeighbor[16].X = m_Coord.X  ;   m_pNeighbor[16].Y = m_Coord.Y+1;
      m_pNeighbor[17].X = m_Coord.X+1;   m_pNeighbor[17].Y = m_Coord.Y+1;
      m_pNeighbor[18].X = m_Coord.X+2;   m_pNeighbor[18].Y = m_Coord.Y+1;
      m_pNeighbor[19].X = m_Coord.X  ;   m_pNeighbor[19].Y = m_Coord.Y+2;
      m_pNeighbor[20].X = m_Coord.X+2;   m_pNeighbor[20].Y = m_Coord.Y+2;
      break;
   case  5:
      m_pNeighbor[ 0].X = m_Coord.X  ;   m_pNeighbor[ 0].Y = m_Coord.Y-3;
      m_pNeighbor[ 1].X = m_Coord.X+2;   m_pNeighbor[ 1].Y = m_Coord.Y-3;
      m_pNeighbor[ 2].X = m_Coord.X  ;   m_pNeighbor[ 2].Y = m_Coord.Y-2;
      m_pNeighbor[ 3].X = m_Coord.X+1;   m_pNeighbor[ 3].Y = m_Coord.Y-2;
      m_pNeighbor[ 4].X = m_Coord.X+2;   m_pNeighbor[ 4].Y = m_Coord.Y-2;
      m_pNeighbor[ 5].X = m_Coord.X+3;   m_pNeighbor[ 5].Y = m_Coord.Y-2;
      m_pNeighbor[ 6].X = m_Coord.X-1;   m_pNeighbor[ 6].Y = m_Coord.Y-1;
      m_pNeighbor[ 7].X = m_Coord.X  ;   m_pNeighbor[ 7].Y = m_Coord.Y-1;
      m_pNeighbor[ 8].X = m_Coord.X+1;   m_pNeighbor[ 8].Y = m_Coord.Y-1;
      m_pNeighbor[ 9].X = m_Coord.X+2;   m_pNeighbor[ 9].Y = m_Coord.Y-1;
      m_pNeighbor[10].X = m_Coord.X+3;   m_pNeighbor[10].Y = m_Coord.Y-1;
      m_pNeighbor[11].X = m_Coord.X-2;   m_pNeighbor[11].Y = m_Coord.Y  ;
      m_pNeighbor[12].X = m_Coord.X-1;   m_pNeighbor[12].Y = m_Coord.Y  ;
      m_pNeighbor[13].X = m_Coord.X+1;   m_pNeighbor[13].Y = m_Coord.Y  ;
      m_pNeighbor[14].X = m_Coord.X+2;   m_pNeighbor[14].Y = m_Coord.Y  ;
      m_pNeighbor[15].X = m_Coord.X-2;   m_pNeighbor[15].Y = m_Coord.Y+1;
      m_pNeighbor[16].X = m_Coord.X-1;   m_pNeighbor[16].Y = m_Coord.Y+1;
      m_pNeighbor[17].X = m_Coord.X  ;   m_pNeighbor[17].Y = m_Coord.Y+1;
      m_pNeighbor[18].X = m_Coord.X+1;   m_pNeighbor[18].Y = m_Coord.Y+1;
      m_pNeighbor[19].X = m_Coord.X-1;   m_pNeighbor[19].Y = m_Coord.Y+2;
      m_pNeighbor[20].X = m_Coord.X+1;   m_pNeighbor[20].Y = m_Coord.Y+2;
      break;
   case  6:
      m_pNeighbor[ 0].X = m_Coord.X-2;   m_pNeighbor[ 0].Y = m_Coord.Y-2;
      m_pNeighbor[ 1].X = m_Coord.X  ;   m_pNeighbor[ 1].Y = m_Coord.Y-2;
      m_pNeighbor[ 2].X = m_Coord.X+1;   m_pNeighbor[ 2].Y = m_Coord.Y-2;
      m_pNeighbor[ 3].X = m_Coord.X+3;   m_pNeighbor[ 3].Y = m_Coord.Y-2;
      m_pNeighbor[ 4].X = m_Coord.X-3;   m_pNeighbor[ 4].Y = m_Coord.Y-1;
      m_pNeighbor[ 5].X = m_Coord.X-2;   m_pNeighbor[ 5].Y = m_Coord.Y-1;
      m_pNeighbor[ 6].X = m_Coord.X-1;   m_pNeighbor[ 6].Y = m_Coord.Y-1;
      m_pNeighbor[ 7].X = m_Coord.X  ;   m_pNeighbor[ 7].Y = m_Coord.Y-1;
      m_pNeighbor[ 8].X = m_Coord.X+1;   m_pNeighbor[ 8].Y = m_Coord.Y-1;
      m_pNeighbor[ 9].X = m_Coord.X+2;   m_pNeighbor[ 9].Y = m_Coord.Y-1;
      m_pNeighbor[10].X = m_Coord.X+3;   m_pNeighbor[10].Y = m_Coord.Y-1;
      m_pNeighbor[11].X = m_Coord.X-3;   m_pNeighbor[11].Y = m_Coord.Y  ;
      m_pNeighbor[12].X = m_Coord.X-2;   m_pNeighbor[12].Y = m_Coord.Y  ;
      m_pNeighbor[13].X = m_Coord.X-1;   m_pNeighbor[13].Y = m_Coord.Y  ;
      m_pNeighbor[14].X = m_Coord.X+1;   m_pNeighbor[14].Y = m_Coord.Y  ;
      m_pNeighbor[15].X = m_Coord.X+2;   m_pNeighbor[15].Y = m_Coord.Y  ;
      m_pNeighbor[16].X = m_Coord.X+3;   m_pNeighbor[16].Y = m_Coord.Y  ;
      m_pNeighbor[17].X = m_Coord.X-2;   m_pNeighbor[17].Y = m_Coord.Y+1;
      m_pNeighbor[18].X = m_Coord.X  ;   m_pNeighbor[18].Y = m_Coord.Y+1;
      m_pNeighbor[19].X = m_Coord.X+1;   m_pNeighbor[19].Y = m_Coord.Y+1;
      m_pNeighbor[20].X = m_Coord.X+3;   m_pNeighbor[20].Y = m_Coord.Y+1;
      break;
   case  7:
      m_pNeighbor[ 0].X = m_Coord.X  ;   m_pNeighbor[ 0].Y = m_Coord.Y-2;
      m_pNeighbor[ 1].X = m_Coord.X+2;   m_pNeighbor[ 1].Y = m_Coord.Y-2;
      m_pNeighbor[ 2].X = m_Coord.X-1;   m_pNeighbor[ 2].Y = m_Coord.Y-1;
      m_pNeighbor[ 3].X = m_Coord.X  ;   m_pNeighbor[ 3].Y = m_Coord.Y-1;
      m_pNeighbor[ 4].X = m_Coord.X+1;   m_pNeighbor[ 4].Y = m_Coord.Y-1;
      m_pNeighbor[ 5].X = m_Coord.X+2;   m_pNeighbor[ 5].Y = m_Coord.Y-1;
      m_pNeighbor[ 6].X = m_Coord.X-2;   m_pNeighbor[ 6].Y = m_Coord.Y  ;
      m_pNeighbor[ 7].X = m_Coord.X-1;   m_pNeighbor[ 7].Y = m_Coord.Y  ;
      m_pNeighbor[ 8].X = m_Coord.X+1;   m_pNeighbor[ 8].Y = m_Coord.Y  ;
      m_pNeighbor[ 9].X = m_Coord.X+2;   m_pNeighbor[ 9].Y = m_Coord.Y  ;
      m_pNeighbor[10].X = m_Coord.X-2;   m_pNeighbor[10].Y = m_Coord.Y+1;
      m_pNeighbor[11].X = m_Coord.X-1;   m_pNeighbor[11].Y = m_Coord.Y+1;
      m_pNeighbor[12].X = m_Coord.X  ;   m_pNeighbor[12].Y = m_Coord.Y+1;
      m_pNeighbor[13].X = m_Coord.X+1;   m_pNeighbor[13].Y = m_Coord.Y+1;
      m_pNeighbor[14].X = m_Coord.X+2;   m_pNeighbor[14].Y = m_Coord.Y+1;
      m_pNeighbor[15].X = m_Coord.X-2;   m_pNeighbor[15].Y = m_Coord.Y+2;
      m_pNeighbor[16].X = m_Coord.X-1;   m_pNeighbor[16].Y = m_Coord.Y+2;
      m_pNeighbor[17].X = m_Coord.X  ;   m_pNeighbor[17].Y = m_Coord.Y+2;
      m_pNeighbor[18].X = m_Coord.X+1;   m_pNeighbor[18].Y = m_Coord.Y+2;
      m_pNeighbor[19].X = m_Coord.X-2;   m_pNeighbor[19].Y = m_Coord.Y+3;
      m_pNeighbor[20].X = m_Coord.X  ;   m_pNeighbor[20].Y = m_Coord.Y+3;
      break;
   case  8:
      m_pNeighbor[ 0].X = m_Coord.X-1;   m_pNeighbor[ 0].Y = m_Coord.Y-2;
      m_pNeighbor[ 1].X = m_Coord.X+1;   m_pNeighbor[ 1].Y = m_Coord.Y-2;
      m_pNeighbor[ 2].X = m_Coord.X-2;   m_pNeighbor[ 2].Y = m_Coord.Y-1;
      m_pNeighbor[ 3].X = m_Coord.X-1;   m_pNeighbor[ 3].Y = m_Coord.Y-1;
      m_pNeighbor[ 4].X = m_Coord.X  ;   m_pNeighbor[ 4].Y = m_Coord.Y-1;
      m_pNeighbor[ 5].X = m_Coord.X+1;   m_pNeighbor[ 5].Y = m_Coord.Y-1;
      m_pNeighbor[ 6].X = m_Coord.X-2;   m_pNeighbor[ 6].Y = m_Coord.Y  ;
      m_pNeighbor[ 7].X = m_Coord.X-1;   m_pNeighbor[ 7].Y = m_Coord.Y  ;
      m_pNeighbor[ 8].X = m_Coord.X+1;   m_pNeighbor[ 8].Y = m_Coord.Y  ;
      m_pNeighbor[ 9].X = m_Coord.X+2;   m_pNeighbor[ 9].Y = m_Coord.Y  ;
      m_pNeighbor[10].X = m_Coord.X-1;   m_pNeighbor[10].Y = m_Coord.Y+1;
      m_pNeighbor[11].X = m_Coord.X  ;   m_pNeighbor[11].Y = m_Coord.Y+1;
      m_pNeighbor[12].X = m_Coord.X+1;   m_pNeighbor[12].Y = m_Coord.Y+1;
      m_pNeighbor[13].X = m_Coord.X+2;   m_pNeighbor[13].Y = m_Coord.Y+1;
      m_pNeighbor[14].X = m_Coord.X+3;   m_pNeighbor[14].Y = m_Coord.Y+1;
      m_pNeighbor[15].X = m_Coord.X  ;   m_pNeighbor[15].Y = m_Coord.Y+2;
      m_pNeighbor[16].X = m_Coord.X+1;   m_pNeighbor[16].Y = m_Coord.Y+2;
      m_pNeighbor[17].X = m_Coord.X+2;   m_pNeighbor[17].Y = m_Coord.Y+2;
      m_pNeighbor[18].X = m_Coord.X+3;   m_pNeighbor[18].Y = m_Coord.Y+2;
      m_pNeighbor[19].X = m_Coord.X  ;   m_pNeighbor[19].Y = m_Coord.Y+3;
      m_pNeighbor[20].X = m_Coord.X+2;   m_pNeighbor[20].Y = m_Coord.Y+3;
      break;
   case  9:
      m_pNeighbor[ 0].X = m_Coord.X-2;   m_pNeighbor[ 0].Y = m_Coord.Y-3;
      m_pNeighbor[ 1].X = m_Coord.X  ;   m_pNeighbor[ 1].Y = m_Coord.Y-3;
      m_pNeighbor[ 2].X = m_Coord.X-3;   m_pNeighbor[ 2].Y = m_Coord.Y-2;
      m_pNeighbor[ 3].X = m_Coord.X-2;   m_pNeighbor[ 3].Y = m_Coord.Y-2;
      m_pNeighbor[ 4].X = m_Coord.X-1;   m_pNeighbor[ 4].Y = m_Coord.Y-2;
      m_pNeighbor[ 5].X = m_Coord.X  ;   m_pNeighbor[ 5].Y = m_Coord.Y-2;
      m_pNeighbor[ 6].X = m_Coord.X-3;   m_pNeighbor[ 6].Y = m_Coord.Y-1;
      m_pNeighbor[ 7].X = m_Coord.X-2;   m_pNeighbor[ 7].Y = m_Coord.Y-1;
      m_pNeighbor[ 8].X = m_Coord.X-1;   m_pNeighbor[ 8].Y = m_Coord.Y-1;
      m_pNeighbor[ 9].X = m_Coord.X  ;   m_pNeighbor[ 9].Y = m_Coord.Y-1;
      m_pNeighbor[10].X = m_Coord.X+1;   m_pNeighbor[10].Y = m_Coord.Y-1;
      m_pNeighbor[11].X = m_Coord.X-2;   m_pNeighbor[11].Y = m_Coord.Y  ;
      m_pNeighbor[12].X = m_Coord.X-1;   m_pNeighbor[12].Y = m_Coord.Y  ;
      m_pNeighbor[13].X = m_Coord.X+1;   m_pNeighbor[13].Y = m_Coord.Y  ;
      m_pNeighbor[14].X = m_Coord.X+2;   m_pNeighbor[14].Y = m_Coord.Y  ;
      m_pNeighbor[15].X = m_Coord.X-1;   m_pNeighbor[15].Y = m_Coord.Y+1;
      m_pNeighbor[16].X = m_Coord.X  ;   m_pNeighbor[16].Y = m_Coord.Y+1;
      m_pNeighbor[17].X = m_Coord.X+1;   m_pNeighbor[17].Y = m_Coord.Y+1;
      m_pNeighbor[18].X = m_Coord.X+2;   m_pNeighbor[18].Y = m_Coord.Y+1;
      m_pNeighbor[19].X = m_Coord.X-1;   m_pNeighbor[19].Y = m_Coord.Y+2;
      m_pNeighbor[20].X = m_Coord.X+1;   m_pNeighbor[20].Y = m_Coord.Y+2;
      break;
   case 10:
      m_pNeighbor[ 0].X = m_Coord.X  ;   m_pNeighbor[ 0].Y = m_Coord.Y-3;
      m_pNeighbor[ 1].X = m_Coord.X+2;   m_pNeighbor[ 1].Y = m_Coord.Y-3;
      m_pNeighbor[ 2].X = m_Coord.X-1;   m_pNeighbor[ 2].Y = m_Coord.Y-2;
      m_pNeighbor[ 3].X = m_Coord.X  ;   m_pNeighbor[ 3].Y = m_Coord.Y-2;
      m_pNeighbor[ 4].X = m_Coord.X+1;   m_pNeighbor[ 4].Y = m_Coord.Y-2;
      m_pNeighbor[ 5].X = m_Coord.X+2;   m_pNeighbor[ 5].Y = m_Coord.Y-2;
      m_pNeighbor[ 6].X = m_Coord.X-2;   m_pNeighbor[ 6].Y = m_Coord.Y-1;
      m_pNeighbor[ 7].X = m_Coord.X-1;   m_pNeighbor[ 7].Y = m_Coord.Y-1;
      m_pNeighbor[ 8].X = m_Coord.X  ;   m_pNeighbor[ 8].Y = m_Coord.Y-1;
      m_pNeighbor[ 9].X = m_Coord.X+1;   m_pNeighbor[ 9].Y = m_Coord.Y-1;
      m_pNeighbor[10].X = m_Coord.X+2;   m_pNeighbor[10].Y = m_Coord.Y-1;
      m_pNeighbor[11].X = m_Coord.X-2;   m_pNeighbor[11].Y = m_Coord.Y  ;
      m_pNeighbor[12].X = m_Coord.X-1;   m_pNeighbor[12].Y = m_Coord.Y  ;
      m_pNeighbor[13].X = m_Coord.X+1;   m_pNeighbor[13].Y = m_Coord.Y  ;
      m_pNeighbor[14].X = m_Coord.X+2;   m_pNeighbor[14].Y = m_Coord.Y  ;
      m_pNeighbor[15].X = m_Coord.X-2;   m_pNeighbor[15].Y = m_Coord.Y+1;
      m_pNeighbor[16].X = m_Coord.X-1;   m_pNeighbor[16].Y = m_Coord.Y+1;
      m_pNeighbor[17].X = m_Coord.X  ;   m_pNeighbor[17].Y = m_Coord.Y+1;
      m_pNeighbor[18].X = m_Coord.X+1;   m_pNeighbor[18].Y = m_Coord.Y+1;
      m_pNeighbor[19].X = m_Coord.X-2;   m_pNeighbor[19].Y = m_Coord.Y+2;
      m_pNeighbor[20].X = m_Coord.X  ;   m_pNeighbor[20].Y = m_Coord.Y+2;
      break;
   case 11:
      m_pNeighbor[ 0].X = m_Coord.X-3;   m_pNeighbor[ 0].Y = m_Coord.Y-1;
      m_pNeighbor[ 1].X = m_Coord.X-1;   m_pNeighbor[ 1].Y = m_Coord.Y-1;
      m_pNeighbor[ 2].X = m_Coord.X  ;   m_pNeighbor[ 2].Y = m_Coord.Y-1;
      m_pNeighbor[ 3].X = m_Coord.X+2;   m_pNeighbor[ 3].Y = m_Coord.Y-1;
      m_pNeighbor[ 4].X = m_Coord.X-3;   m_pNeighbor[ 4].Y = m_Coord.Y  ;
      m_pNeighbor[ 5].X = m_Coord.X-2;   m_pNeighbor[ 5].Y = m_Coord.Y  ;
      m_pNeighbor[ 6].X = m_Coord.X-1;   m_pNeighbor[ 6].Y = m_Coord.Y  ;
      m_pNeighbor[ 7].X = m_Coord.X+1;   m_pNeighbor[ 7].Y = m_Coord.Y  ;
      m_pNeighbor[ 8].X = m_Coord.X+2;   m_pNeighbor[ 8].Y = m_Coord.Y  ;
      m_pNeighbor[ 9].X = m_Coord.X+3;   m_pNeighbor[ 9].Y = m_Coord.Y  ;
      m_pNeighbor[10].X = m_Coord.X-3;   m_pNeighbor[10].Y = m_Coord.Y+1;
      m_pNeighbor[11].X = m_Coord.X-2;   m_pNeighbor[11].Y = m_Coord.Y+1;
      m_pNeighbor[12].X = m_Coord.X-1;   m_pNeighbor[12].Y = m_Coord.Y+1;
      m_pNeighbor[13].X = m_Coord.X  ;   m_pNeighbor[13].Y = m_Coord.Y+1;
      m_pNeighbor[14].X = m_Coord.X+1;   m_pNeighbor[14].Y = m_Coord.Y+1;
      m_pNeighbor[15].X = m_Coord.X+2;   m_pNeighbor[15].Y = m_Coord.Y+1;
      m_pNeighbor[16].X = m_Coord.X+3;   m_pNeighbor[16].Y = m_Coord.Y+1;
      m_pNeighbor[17].X = m_Coord.X-3;   m_pNeighbor[17].Y = m_Coord.Y+2;
      m_pNeighbor[18].X = m_Coord.X-1;   m_pNeighbor[18].Y = m_Coord.Y+2;
      m_pNeighbor[19].X = m_Coord.X  ;   m_pNeighbor[19].Y = m_Coord.Y+2;
      m_pNeighbor[20].X = m_Coord.X+2;   m_pNeighbor[20].Y = m_Coord.Y+2;
      break;
   }
   VerifyNeighbor(sizeField);
}

void nsCell::CTriangle4::SetPoint(int area) {
   if (m_Coord.X==0 && m_Coord.Y==0) {
      m_a = sqrt(area*SQRT48);
      m_b = m_a/2;
      m_R = m_a/SQRT3;
      m_r = m_R/2;
      m_sq = ((m_a-m_GContext.m_Border.m_iWidth*2/TAN15)/(SQRT3+3));
   }

   // определение координат точек фигуры
   float oX =  (m_Coord.X/3)*m_a + m_b;      // offset X
   float oY = ((m_Coord.Y/4)*2+1)*(m_R+m_r); // offset Y
   switch (m_iDirection) {
   case 0:
      m_pRegion[0].x = oX - m_b;   m_pRegion[0].y = oY;
      m_pRegion[1].x = oX      ;   m_pRegion[1].y = oY - m_r;
      m_pRegion[2].x = oX      ;   m_pRegion[2].y = oY - m_R-m_r;
      break;
   case 1:
      m_pRegion[0].x = oX + m_b;   m_pRegion[0].y = oY - m_R;
      m_pRegion[1].x = oX      ;   m_pRegion[1].y = oY - m_R-m_r;
      m_pRegion[2].x = oX + m_b;   m_pRegion[2].y = oY;
      break;
   case 2:
      m_pRegion[0].x = oX      ;   m_pRegion[0].y = oY - m_R-m_r;
      m_pRegion[1].x = oX + m_b;   m_pRegion[1].y = oY - m_R;
      m_pRegion[2].x = oX + m_a;   m_pRegion[2].y = oY - m_R-m_r;
      break;
   case 3:
      m_pRegion[0].x = oX + m_b;   m_pRegion[0].y = oY;
      m_pRegion[1].x = oX      ;   m_pRegion[1].y = oY - m_r;
      m_pRegion[2].x = oX - m_b;   m_pRegion[2].y = oY;
      break;
   case 4:
      m_pRegion[0].x = oX      ;   m_pRegion[0].y = oY - m_r;
      m_pRegion[1].x = oX + m_b;   m_pRegion[1].y = oY;
      m_pRegion[2].x = oX      ;   m_pRegion[2].y = oY - m_R-m_r;
      break;
   case 5:
      m_pRegion[0].x = oX + m_a;   m_pRegion[0].y = oY - m_R-m_r;
      m_pRegion[1].x = oX + m_b;   m_pRegion[1].y = oY - m_R;
      m_pRegion[2].x = oX + m_b;   m_pRegion[2].y = oY;
      break;
   case 6:
      m_pRegion[0].x = oX - m_b;   m_pRegion[0].y = oY;
      m_pRegion[1].x = oX      ;   m_pRegion[1].y = oY + m_r;
      m_pRegion[2].x = oX + m_b;   m_pRegion[2].y = oY;
      break;
   case 7:
      m_pRegion[0].x = oX + m_b;   m_pRegion[0].y = oY;
      m_pRegion[1].x = oX      ;   m_pRegion[1].y = oY + m_r;
      m_pRegion[2].x = oX      ;   m_pRegion[2].y = oY + m_R+m_r;
      break;
   case 8:
      m_pRegion[0].x = oX + m_b;   m_pRegion[0].y = oY + m_R;
      m_pRegion[1].x = oX + m_a;   m_pRegion[1].y = oY + m_R+m_r;
      m_pRegion[2].x = oX + m_b;   m_pRegion[2].y = oY;
      break;
   case 9:
      m_pRegion[0].x = oX      ;   m_pRegion[0].y = oY + m_r;
      m_pRegion[1].x = oX - m_b;   m_pRegion[1].y = oY;
      m_pRegion[2].x = oX      ;   m_pRegion[2].y = oY + m_R+m_r;
      break;
   case 10:
      m_pRegion[0].x = oX      ;   m_pRegion[0].y = oY + m_R+m_r;
      m_pRegion[1].x = oX + m_b;   m_pRegion[1].y = oY + m_R;
      m_pRegion[2].x = oX + m_b;   m_pRegion[2].y = oY;
      break;
   case 11:
      m_pRegion[0].x = oX + m_a;   m_pRegion[0].y = oY + m_R+m_r;
      m_pRegion[1].x = oX + m_b;   m_pRegion[1].y = oY + m_R;
      m_pRegion[2].x = oX      ;   m_pRegion[2].y = oY + m_R+m_r;
      break;
   }

   // определение координат вписанного в фигуру квадрата - область в которую выводитс€ изображение/текст
   {
      POINTFLOAT center; // координата центра квадрата
      float sq2   = m_sq/2;
      float sq2w  = m_sq/2+m_GContext.m_Border.m_iWidth;
      float sq2w3 = m_sq/2+m_GContext.m_Border.m_iWidth/SQRT3;
      switch (m_iDirection) {
      case 0: case 10:
         center.x = m_pRegion[1].x - sq2w;
         center.y = m_pRegion[1].y - sq2w3;
         break;
      case 1: case 9:
         center.x = m_pRegion[0].x - sq2w;
         center.y = m_pRegion[0].y + sq2w3;
         break;
      case 2: case 6:
         center.x = m_pRegion[1].x;
         center.y = m_pRegion[2].y + sq2w;
         break;
      case 3: case 11:
         center.x = m_pRegion[1].x;
         center.y = m_pRegion[0].y - sq2w;
         break;
      case 4: case 8:
         center.x = m_pRegion[0].x + sq2w;
         center.y = m_pRegion[0].y - sq2w3;
         break;
      case 5: case 7:
         center.x = m_pRegion[1].x + sq2w;
         center.y = m_pRegion[1].y + sq2w3;
         break;
      }
      m_Square.left   = center.x - sq2;
      m_Square.top    = center.y - sq2;
      m_Square.right  = m_Square.left + m_sq;
      m_Square.bottom = m_Square.top  + m_sq;
   }

}

void nsCell::CTriangle4::Paint() const {
   CBase::Paint();

   switch (m_iDirection) {
   case 0: case 2: case 4: case 6: case 8: case 10:   SelectObject(m_GContext.m_hDCTmp, m_bDown ? m_GContext.m_hPenLight  : m_GContext.m_hPenShadow); break;
   case 1: case 3: case 5: case 7: case 9: case 11:   SelectObject(m_GContext.m_hDCTmp, m_bDown ? m_GContext.m_hPenShadow : m_GContext.m_hPenLight ); break;
   }
   MoveToEx(m_GContext.m_hDCTmp, m_pRegion[0].x, m_pRegion[0].y, NULL);
   LineTo  (m_GContext.m_hDCTmp, m_pRegion[1].x, m_pRegion[1].y);
   LineTo  (m_GContext.m_hDCTmp, m_pRegion[2].x, m_pRegion[2].y);
   switch (m_iDirection) {
   case 0: case 2: case 4: case 6: case 8: case 10:   SelectObject(m_GContext.m_hDCTmp, m_bDown ? m_GContext.m_hPenShadow : m_GContext.m_hPenLight ); break;
   case 1: case 3: case 5: case 7: case 9: case 11:   SelectObject(m_GContext.m_hDCTmp, m_bDown ? m_GContext.m_hPenLight  : m_GContext.m_hPenShadow); break;
   }
   LineTo  (m_GContext.m_hDCTmp, m_pRegion[0].x, m_pRegion[0].y);

   RegionDraw(
      m_GContext.m_hDCDst, true,
      m_pRegion, 3, NULL, NULL, 0, 0, 0, 0,
      m_GContext.m_hDCTmp, NULL, 0,0);
}
