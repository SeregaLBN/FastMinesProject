////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "TrSq2.cpp"
//
// Реализация класса CTrSq2 - мозаика из 24х треугольников и 12х квадратов (на 1 квадрат приходится 2 треугольника)
////////////////////////////////////////////////////////////////////////////////

#include "StdAfx.h"
#include "TrSq2.h"
#include "../CommonLib.h"

////////////////////////////////////////////////////////////////////////////////
//                               local init
////////////////////////////////////////////////////////////////////////////////

float nsCell::CTrSq2::m_b;
float nsCell::CTrSq2::m_h;

////////////////////////////////////////////////////////////////////////////////
//                               static function
////////////////////////////////////////////////////////////////////////////////

SIZE nsCell::CTrSq2::GetSizeInPixel(const COORD& sizeField, int area) {
   m_a = sqrt(6*area/(2+SQRT3)); // размер стороны треугольника и квадрата
   m_b = m_a/2;
   m_h = m_b*SQRT3;

   SIZE result = {m_b+m_h*((sizeField.X+2)/3)+
                      m_a*((sizeField.X+1)/3)+
                      m_b*((sizeField.X+0)/3),
                  m_b+m_h*((sizeField.Y+2)/3)+
                      m_a*((sizeField.Y+1)/3)+
                      m_b*((sizeField.Y+0)/3)};
   return result;
}

int nsCell::CTrSq2::SizeInscribedSquare(int area, int borderWidth) {
   m_a = sqrt(6*area/(2+SQRT3)); // размер стороны треугольника и квадрата
   m_sq = (m_a*SQRT3-borderWidth*6)/(SQRT3+2) - 1;
   return m_sq;
}

////////////////////////////////////////////////////////////////////////////////
//                               virtual function
////////////////////////////////////////////////////////////////////////////////

nsCell::CTrSq2::CTrSq2(const COORD& Coord, const COORD& sizeField, int area, const CGraphicContext& gContext)
   : CBase(Coord, sizeField, area, gContext,
           12, 4,
           (Coord.Y%6)*6+(Coord.X%6) // 0..35
          )
{
   SetPoint(area);
   // определяю координаты соседей
   switch (m_iDirection) {
   case 0:
   case 21: m_pNeighbor[ 0].X = m_Coord.X+1;   m_pNeighbor[ 0].Y = m_Coord.Y-2;
            m_pNeighbor[ 1].X = m_Coord.X-1;   m_pNeighbor[ 1].Y = m_Coord.Y-1;
            m_pNeighbor[ 2].X = m_Coord.X  ;   m_pNeighbor[ 2].Y = m_Coord.Y-1;
            m_pNeighbor[ 3].X = m_Coord.X+1;   m_pNeighbor[ 3].Y = m_Coord.Y-1;
            m_pNeighbor[ 4].X = m_Coord.X-1;   m_pNeighbor[ 4].Y = m_Coord.Y  ;
            m_pNeighbor[ 5].X = m_Coord.X+1;   m_pNeighbor[ 5].Y = m_Coord.Y  ;
            m_pNeighbor[ 6].X = m_Coord.X-1;   m_pNeighbor[ 6].Y = m_Coord.Y+1;
            m_pNeighbor[ 7].X = m_Coord.X  ;   m_pNeighbor[ 7].Y = m_Coord.Y+1;
            m_pNeighbor[ 8].X = m_Coord.X+1;   m_pNeighbor[ 8].Y = m_Coord.Y+1;
            m_pNeighbor[ 9].X = m_Coord.X+2;   m_pNeighbor[ 9].Y = m_Coord.Y+1;
            m_pNeighbor[10].X = m_Coord.X-1;   m_pNeighbor[10].Y = m_Coord.Y+2;
            m_pNeighbor[11].X = m_Coord.X  ;   m_pNeighbor[11].Y = m_Coord.Y+2;
            break;
   case 1:
   case 22: m_pNeighbor[ 0].X = m_Coord.X  ;   m_pNeighbor[ 0].Y = m_Coord.Y-2;
            m_pNeighbor[ 1].X = m_Coord.X-1;   m_pNeighbor[ 1].Y = m_Coord.Y-1;
            m_pNeighbor[ 2].X = m_Coord.X  ;   m_pNeighbor[ 2].Y = m_Coord.Y-1;
            m_pNeighbor[ 3].X = m_Coord.X+1;   m_pNeighbor[ 3].Y = m_Coord.Y-1;
            m_pNeighbor[ 4].X = m_Coord.X-1;   m_pNeighbor[ 4].Y = m_Coord.Y  ;
            m_pNeighbor[ 5].X = m_Coord.X+1;   m_pNeighbor[ 5].Y = m_Coord.Y  ;
            m_pNeighbor[ 6].X = m_Coord.X-1;   m_pNeighbor[ 6].Y = m_Coord.Y+1;
            m_pNeighbor[ 7].X = m_Coord.X  ;   m_pNeighbor[ 7].Y = m_Coord.Y+1;
            m_pNeighbor[ 8].X = m_Coord.X+1;   m_pNeighbor[ 8].Y = m_Coord.Y+1;
            m_pNeighbor[ 9] =
            m_pNeighbor[10] =
            m_pNeighbor[11] = INCORRECT_COORD;
            break;
   case 2:
   case 23: m_pNeighbor[ 0].X = m_Coord.X-1;   m_pNeighbor[ 0].Y = m_Coord.Y-2;
            m_pNeighbor[ 1].X = m_Coord.X+1;   m_pNeighbor[ 1].Y = m_Coord.Y-1;
            m_pNeighbor[ 2].X = m_Coord.X  ;   m_pNeighbor[ 2].Y = m_Coord.Y-1;
            m_pNeighbor[ 3].X = m_Coord.X+2;   m_pNeighbor[ 3].Y = m_Coord.Y-1;
            m_pNeighbor[ 4].X = m_Coord.X-1;   m_pNeighbor[ 4].Y = m_Coord.Y  ;
            m_pNeighbor[ 5].X = m_Coord.X+1;   m_pNeighbor[ 5].Y = m_Coord.Y  ;
            m_pNeighbor[ 6].X = m_Coord.X-1;   m_pNeighbor[ 6].Y = m_Coord.Y+1;
            m_pNeighbor[ 7].X = m_Coord.X  ;   m_pNeighbor[ 7].Y = m_Coord.Y+1;
            m_pNeighbor[ 8].X = m_Coord.X+1;   m_pNeighbor[ 8].Y = m_Coord.Y+1;
            m_pNeighbor[ 9] =
            m_pNeighbor[10] =
            m_pNeighbor[11] = INCORRECT_COORD;
            break;
   case 3:
   case 18: m_pNeighbor[ 0].X = m_Coord.X-1;   m_pNeighbor[ 0].Y = m_Coord.Y-1;
            m_pNeighbor[ 1].X = m_Coord.X  ;   m_pNeighbor[ 1].Y = m_Coord.Y-1;
            m_pNeighbor[ 2].X = m_Coord.X+1;   m_pNeighbor[ 2].Y = m_Coord.Y-1;
            m_pNeighbor[ 3].X = m_Coord.X+2;   m_pNeighbor[ 3].Y = m_Coord.Y-1;
            m_pNeighbor[ 4].X = m_Coord.X-1;   m_pNeighbor[ 4].Y = m_Coord.Y  ;
            m_pNeighbor[ 5].X = m_Coord.X+1;   m_pNeighbor[ 5].Y = m_Coord.Y  ;
            m_pNeighbor[ 6].X = m_Coord.X+2;   m_pNeighbor[ 6].Y = m_Coord.Y  ;
            m_pNeighbor[ 7].X = m_Coord.X-2;   m_pNeighbor[ 7].Y = m_Coord.Y+1;
            m_pNeighbor[ 8].X = m_Coord.X-1;   m_pNeighbor[ 8].Y = m_Coord.Y+1;
            m_pNeighbor[ 9].X = m_Coord.X  ;   m_pNeighbor[ 9].Y = m_Coord.Y+1;
            m_pNeighbor[10].X = m_Coord.X+1;   m_pNeighbor[10].Y = m_Coord.Y+1;
            m_pNeighbor[11].X = m_Coord.X+1;   m_pNeighbor[11].Y = m_Coord.Y+2;
            break;
   case 4:
   case 19: m_pNeighbor[ 0].X = m_Coord.X  ;   m_pNeighbor[ 0].Y = m_Coord.Y-1;
            m_pNeighbor[ 1].X = m_Coord.X+1;   m_pNeighbor[ 1].Y = m_Coord.Y-1;
            m_pNeighbor[ 2].X = m_Coord.X-1;   m_pNeighbor[ 2].Y = m_Coord.Y  ;
            m_pNeighbor[ 3].X = m_Coord.X+1;   m_pNeighbor[ 3].Y = m_Coord.Y  ;
            m_pNeighbor[ 4].X = m_Coord.X-1;   m_pNeighbor[ 4].Y = m_Coord.Y+1;
            m_pNeighbor[ 5].X = m_Coord.X  ;   m_pNeighbor[ 5].Y = m_Coord.Y+1;
            m_pNeighbor[ 6].X = m_Coord.X+1;   m_pNeighbor[ 6].Y = m_Coord.Y+1;
            m_pNeighbor[ 7].X = m_Coord.X  ;   m_pNeighbor[ 7].Y = m_Coord.Y+2;
            m_pNeighbor[ 8].X = m_Coord.X+1;   m_pNeighbor[ 8].Y = m_Coord.Y+2;
            m_pNeighbor[ 9] =
            m_pNeighbor[10] =
            m_pNeighbor[11] = INCORRECT_COORD;
            break;
   case 5:
   case 20: m_pNeighbor[ 0].X = m_Coord.X-1;   m_pNeighbor[ 0].Y = m_Coord.Y-1;
            m_pNeighbor[ 1].X = m_Coord.X  ;   m_pNeighbor[ 1].Y = m_Coord.Y-1;
            m_pNeighbor[ 2].X = m_Coord.X+1;   m_pNeighbor[ 2].Y = m_Coord.Y-1;
            m_pNeighbor[ 3].X = m_Coord.X-2;   m_pNeighbor[ 3].Y = m_Coord.Y  ;
            m_pNeighbor[ 4].X = m_Coord.X-1;   m_pNeighbor[ 4].Y = m_Coord.Y  ;
            m_pNeighbor[ 5].X = m_Coord.X+1;   m_pNeighbor[ 5].Y = m_Coord.Y  ;
            m_pNeighbor[ 6].X = m_Coord.X-1;   m_pNeighbor[ 6].Y = m_Coord.Y+1;
            m_pNeighbor[ 7].X = m_Coord.X  ;   m_pNeighbor[ 7].Y = m_Coord.Y+1;
            m_pNeighbor[ 8].X = m_Coord.X  ;   m_pNeighbor[ 8].Y = m_Coord.Y+2;
            m_pNeighbor[ 9] =
            m_pNeighbor[10] =
            m_pNeighbor[11] = INCORRECT_COORD;
            break;
   case 6:
   case 27: m_pNeighbor[ 0].X = m_Coord.X  ;   m_pNeighbor[ 0].Y = m_Coord.Y-1;
            m_pNeighbor[ 1].X = m_Coord.X+1;   m_pNeighbor[ 1].Y = m_Coord.Y-1;
            m_pNeighbor[ 2].X = m_Coord.X-1;   m_pNeighbor[ 2].Y = m_Coord.Y  ;
            m_pNeighbor[ 3].X = m_Coord.X+1;   m_pNeighbor[ 3].Y = m_Coord.Y  ;
            m_pNeighbor[ 4].X = m_Coord.X+2;   m_pNeighbor[ 4].Y = m_Coord.Y  ;
            m_pNeighbor[ 5].X = m_Coord.X-1;   m_pNeighbor[ 5].Y = m_Coord.Y+1;
            m_pNeighbor[ 6].X = m_Coord.X  ;   m_pNeighbor[ 6].Y = m_Coord.Y+1;
            m_pNeighbor[ 7].X = m_Coord.X+1;   m_pNeighbor[ 7].Y = m_Coord.Y+1;
            m_pNeighbor[ 8].X = m_Coord.X+2;   m_pNeighbor[ 8].Y = m_Coord.Y+1;
            m_pNeighbor[ 9] =
            m_pNeighbor[10] =
            m_pNeighbor[11] = INCORRECT_COORD;
            break;
   case 7:
   case 28: m_pNeighbor[ 0].X = m_Coord.X-1;   m_pNeighbor[ 0].Y = m_Coord.Y-1;
            m_pNeighbor[ 1].X = m_Coord.X  ;   m_pNeighbor[ 1].Y = m_Coord.Y-1;
            m_pNeighbor[ 2].X = m_Coord.X+1;   m_pNeighbor[ 2].Y = m_Coord.Y-1;
            m_pNeighbor[ 3].X = m_Coord.X+2;   m_pNeighbor[ 3].Y = m_Coord.Y-1;
            m_pNeighbor[ 4].X = m_Coord.X-1;   m_pNeighbor[ 4].Y = m_Coord.Y  ;
            m_pNeighbor[ 5].X = m_Coord.X+1;   m_pNeighbor[ 5].Y = m_Coord.Y  ;
            m_pNeighbor[ 6].X = m_Coord.X+2;   m_pNeighbor[ 6].Y = m_Coord.Y  ;
            m_pNeighbor[ 7].X = m_Coord.X-1;   m_pNeighbor[ 7].Y = m_Coord.Y+1;
            m_pNeighbor[ 8].X = m_Coord.X  ;   m_pNeighbor[ 8].Y = m_Coord.Y+1;
            m_pNeighbor[ 9].X = m_Coord.X+1;   m_pNeighbor[ 9].Y = m_Coord.Y+1;
            m_pNeighbor[10].X = m_Coord.X+2;   m_pNeighbor[10].Y = m_Coord.Y+1;
            m_pNeighbor[11].X = m_Coord.X+3;   m_pNeighbor[11].Y = m_Coord.Y+1;
            break;
   case 8:
   case 29: m_pNeighbor[ 0].X = m_Coord.X-1;   m_pNeighbor[ 0].Y = m_Coord.Y-3;
            m_pNeighbor[ 1].X = m_Coord.X  ;   m_pNeighbor[ 1].Y = m_Coord.Y-2;
            m_pNeighbor[ 2].X = m_Coord.X-2;   m_pNeighbor[ 2].Y = m_Coord.Y-1;
            m_pNeighbor[ 3].X = m_Coord.X-1;   m_pNeighbor[ 3].Y = m_Coord.Y-1;
            m_pNeighbor[ 4].X = m_Coord.X  ;   m_pNeighbor[ 4].Y = m_Coord.Y-1;
            m_pNeighbor[ 5].X = m_Coord.X+1;   m_pNeighbor[ 5].Y = m_Coord.Y-1;
            m_pNeighbor[ 6].X = m_Coord.X-2;   m_pNeighbor[ 6].Y = m_Coord.Y  ;
            m_pNeighbor[ 7].X = m_Coord.X-1;   m_pNeighbor[ 7].Y = m_Coord.Y  ;
            m_pNeighbor[ 8].X = m_Coord.X+1;   m_pNeighbor[ 8].Y = m_Coord.Y  ;
            m_pNeighbor[ 9] =
            m_pNeighbor[10] =
            m_pNeighbor[11] = INCORRECT_COORD;
            break;
   case 9:
   case 24: m_pNeighbor[ 0].X = m_Coord.X-1;   m_pNeighbor[ 0].Y = m_Coord.Y-1;
            m_pNeighbor[ 1].X = m_Coord.X  ;   m_pNeighbor[ 1].Y = m_Coord.Y-1;
            m_pNeighbor[ 2].X = m_Coord.X+1;   m_pNeighbor[ 2].Y = m_Coord.Y-1;
            m_pNeighbor[ 3].X = m_Coord.X-2;   m_pNeighbor[ 3].Y = m_Coord.Y  ;
            m_pNeighbor[ 4].X = m_Coord.X-1;   m_pNeighbor[ 4].Y = m_Coord.Y  ;
            m_pNeighbor[ 5].X = m_Coord.X+1;   m_pNeighbor[ 5].Y = m_Coord.Y  ;
            m_pNeighbor[ 6].X = m_Coord.X-1;   m_pNeighbor[ 6].Y = m_Coord.Y+1;
            m_pNeighbor[ 7].X = m_Coord.X  ;   m_pNeighbor[ 7].Y = m_Coord.Y+1;
            m_pNeighbor[ 8].X = m_Coord.X+1;   m_pNeighbor[ 8].Y = m_Coord.Y+1;
            m_pNeighbor[ 9] =
            m_pNeighbor[10] =
            m_pNeighbor[11] = INCORRECT_COORD;
            break;
   case 10:
   case 25: m_pNeighbor[ 0].X = m_Coord.X-1;   m_pNeighbor[ 0].Y = m_Coord.Y-1;
            m_pNeighbor[ 1].X = m_Coord.X  ;   m_pNeighbor[ 1].Y = m_Coord.Y-1;
            m_pNeighbor[ 2].X = m_Coord.X+1;   m_pNeighbor[ 2].Y = m_Coord.Y-1;
            m_pNeighbor[ 3].X = m_Coord.X-1;   m_pNeighbor[ 3].Y = m_Coord.Y  ;
            m_pNeighbor[ 4].X = m_Coord.X+1;   m_pNeighbor[ 4].Y = m_Coord.Y  ;
            m_pNeighbor[ 5].X = m_Coord.X-1;   m_pNeighbor[ 5].Y = m_Coord.Y+1;
            m_pNeighbor[ 6].X = m_Coord.X  ;   m_pNeighbor[ 6].Y = m_Coord.Y+1;
            m_pNeighbor[ 7].X = m_Coord.X+1;   m_pNeighbor[ 7].Y = m_Coord.Y+1;
            m_pNeighbor[ 8].X = m_Coord.X-1;   m_pNeighbor[ 8].Y = m_Coord.Y+2;
            m_pNeighbor[ 9].X = m_Coord.X  ;   m_pNeighbor[ 9].Y = m_Coord.Y+2;
            m_pNeighbor[10].X = m_Coord.X+1;   m_pNeighbor[10].Y = m_Coord.Y+2;
            m_pNeighbor[11].X = m_Coord.X+1;   m_pNeighbor[11].Y = m_Coord.Y+3;
            break;
   case 11:
   case 26: m_pNeighbor[ 0].X = m_Coord.X  ;   m_pNeighbor[ 0].Y = m_Coord.Y-2;
            m_pNeighbor[ 1].X = m_Coord.X+1;   m_pNeighbor[ 1].Y = m_Coord.Y-2;
            m_pNeighbor[ 2].X = m_Coord.X-1;   m_pNeighbor[ 2].Y = m_Coord.Y-1;
            m_pNeighbor[ 3].X = m_Coord.X  ;   m_pNeighbor[ 3].Y = m_Coord.Y-1;
            m_pNeighbor[ 4].X = m_Coord.X+1;   m_pNeighbor[ 4].Y = m_Coord.Y-1;
            m_pNeighbor[ 5].X = m_Coord.X-1;   m_pNeighbor[ 5].Y = m_Coord.Y  ;
            m_pNeighbor[ 6].X = m_Coord.X+1;   m_pNeighbor[ 6].Y = m_Coord.Y  ;
            m_pNeighbor[ 7].X = m_Coord.X  ;   m_pNeighbor[ 7].Y = m_Coord.Y+1;
            m_pNeighbor[ 8].X = m_Coord.X+1;   m_pNeighbor[ 8].Y = m_Coord.Y+1;
            m_pNeighbor[ 9] =
            m_pNeighbor[10] =
            m_pNeighbor[11] = INCORRECT_COORD;
            break;
   case 12:
   case 33: m_pNeighbor[ 0].X = m_Coord.X  ;   m_pNeighbor[ 0].Y = m_Coord.Y-2;
            m_pNeighbor[ 1].X = m_Coord.X-1;   m_pNeighbor[ 1].Y = m_Coord.Y-1;
            m_pNeighbor[ 2].X = m_Coord.X  ;   m_pNeighbor[ 2].Y = m_Coord.Y-1;
            m_pNeighbor[ 3].X = m_Coord.X+1;   m_pNeighbor[ 3].Y = m_Coord.Y-1;
            m_pNeighbor[ 4].X = m_Coord.X-1;   m_pNeighbor[ 4].Y = m_Coord.Y  ;
            m_pNeighbor[ 5].X = m_Coord.X+1;   m_pNeighbor[ 5].Y = m_Coord.Y  ;
            m_pNeighbor[ 6].X = m_Coord.X+2;   m_pNeighbor[ 6].Y = m_Coord.Y  ;
            m_pNeighbor[ 7].X = m_Coord.X-1;   m_pNeighbor[ 7].Y = m_Coord.Y+1;
            m_pNeighbor[ 8].X = m_Coord.X  ;   m_pNeighbor[ 8].Y = m_Coord.Y+1;
            m_pNeighbor[ 9] =
            m_pNeighbor[10] =
            m_pNeighbor[11] = INCORRECT_COORD;
            break;
   case 13:
   case 34: m_pNeighbor[ 0].X = m_Coord.X-1;   m_pNeighbor[ 0].Y = m_Coord.Y-1;
            m_pNeighbor[ 1].X = m_Coord.X  ;   m_pNeighbor[ 1].Y = m_Coord.Y-1;
            m_pNeighbor[ 2].X = m_Coord.X-2;   m_pNeighbor[ 2].Y = m_Coord.Y  ;
            m_pNeighbor[ 3].X = m_Coord.X-1;   m_pNeighbor[ 3].Y = m_Coord.Y  ;
            m_pNeighbor[ 4].X = m_Coord.X+1;   m_pNeighbor[ 4].Y = m_Coord.Y  ;
            m_pNeighbor[ 5].X = m_Coord.X-2;   m_pNeighbor[ 5].Y = m_Coord.Y+1;
            m_pNeighbor[ 6].X = m_Coord.X-1;   m_pNeighbor[ 6].Y = m_Coord.Y+1;
            m_pNeighbor[ 7].X = m_Coord.X  ;   m_pNeighbor[ 7].Y = m_Coord.Y+1;
            m_pNeighbor[ 8].X = m_Coord.X+1;   m_pNeighbor[ 8].Y = m_Coord.Y+1;
            m_pNeighbor[ 9] =
            m_pNeighbor[10] =
            m_pNeighbor[11] = INCORRECT_COORD;
            break;
   case 14:
   case 35: m_pNeighbor[ 0].X = m_Coord.X-2;   m_pNeighbor[ 0].Y = m_Coord.Y-1;
            m_pNeighbor[ 1].X = m_Coord.X-1;   m_pNeighbor[ 1].Y = m_Coord.Y-1;
            m_pNeighbor[ 2].X = m_Coord.X+1;   m_pNeighbor[ 2].Y = m_Coord.Y-1;
            m_pNeighbor[ 3].X = m_Coord.X-2;   m_pNeighbor[ 3].Y = m_Coord.Y  ;
            m_pNeighbor[ 4].X = m_Coord.X-1;   m_pNeighbor[ 4].Y = m_Coord.Y  ;
            m_pNeighbor[ 5].X = m_Coord.X+1;   m_pNeighbor[ 5].Y = m_Coord.Y  ;
            m_pNeighbor[ 6].X = m_Coord.X+2;   m_pNeighbor[ 6].Y = m_Coord.Y  ;
            m_pNeighbor[ 7].X = m_Coord.X-2;   m_pNeighbor[ 7].Y = m_Coord.Y+1;
            m_pNeighbor[ 8].X = m_Coord.X-1;   m_pNeighbor[ 8].Y = m_Coord.Y+1;
            m_pNeighbor[ 9].X = m_Coord.X  ;   m_pNeighbor[ 9].Y = m_Coord.Y+1;
            m_pNeighbor[10].X = m_Coord.X+1;   m_pNeighbor[10].Y = m_Coord.Y+1;
            m_pNeighbor[11].X = m_Coord.X  ;   m_pNeighbor[11].Y = m_Coord.Y+2;
            break;
   case 15:
   case 30: m_pNeighbor[ 0].X = m_Coord.X-2;   m_pNeighbor[ 0].Y = m_Coord.Y-1;
            m_pNeighbor[ 1].X = m_Coord.X  ;   m_pNeighbor[ 1].Y = m_Coord.Y-1;
            m_pNeighbor[ 2].X = m_Coord.X+1;   m_pNeighbor[ 2].Y = m_Coord.Y-1;
            m_pNeighbor[ 3].X = m_Coord.X-1;   m_pNeighbor[ 3].Y = m_Coord.Y  ;
            m_pNeighbor[ 4].X = m_Coord.X+1;   m_pNeighbor[ 4].Y = m_Coord.Y  ;
            m_pNeighbor[ 5].X = m_Coord.X-1;   m_pNeighbor[ 5].Y = m_Coord.Y+1;
            m_pNeighbor[ 6].X = m_Coord.X  ;   m_pNeighbor[ 6].Y = m_Coord.Y+1;
            m_pNeighbor[ 7].X = m_Coord.X+1;   m_pNeighbor[ 7].Y = m_Coord.Y+1;
            m_pNeighbor[ 8].X = m_Coord.X-1;   m_pNeighbor[ 8].Y = m_Coord.Y+2;
            m_pNeighbor[ 9] =
            m_pNeighbor[10] =
            m_pNeighbor[11] = INCORRECT_COORD;
            break;
   case 16:
   case 31: m_pNeighbor[ 0].X = m_Coord.X-1;   m_pNeighbor[ 0].Y = m_Coord.Y-2;
            m_pNeighbor[ 1].X = m_Coord.X  ;   m_pNeighbor[ 1].Y = m_Coord.Y-2;
            m_pNeighbor[ 2].X = m_Coord.X-3;   m_pNeighbor[ 2].Y = m_Coord.Y-1;
            m_pNeighbor[ 3].X = m_Coord.X-1;   m_pNeighbor[ 3].Y = m_Coord.Y-1;
            m_pNeighbor[ 4].X = m_Coord.X  ;   m_pNeighbor[ 4].Y = m_Coord.Y-1;
            m_pNeighbor[ 5].X = m_Coord.X-2;   m_pNeighbor[ 5].Y = m_Coord.Y  ;
            m_pNeighbor[ 6].X = m_Coord.X-1;   m_pNeighbor[ 6].Y = m_Coord.Y  ;
            m_pNeighbor[ 7].X = m_Coord.X-1;   m_pNeighbor[ 7].Y = m_Coord.Y+1;
            m_pNeighbor[ 8].X = m_Coord.X  ;   m_pNeighbor[ 8].Y = m_Coord.Y+1;
            m_pNeighbor[ 9] =
            m_pNeighbor[10] =
            m_pNeighbor[11] = INCORRECT_COORD;
            break;
   case 17:
   case 32: m_pNeighbor[ 0].X = m_Coord.X-1;   m_pNeighbor[ 0].Y = m_Coord.Y-2;
            m_pNeighbor[ 1].X = m_Coord.X  ;   m_pNeighbor[ 1].Y = m_Coord.Y-2;
            m_pNeighbor[ 2].X = m_Coord.X+1;   m_pNeighbor[ 2].Y = m_Coord.Y-2;
            m_pNeighbor[ 3].X = m_Coord.X-1;   m_pNeighbor[ 3].Y = m_Coord.Y-1;
            m_pNeighbor[ 4].X = m_Coord.X  ;   m_pNeighbor[ 4].Y = m_Coord.Y-1;
            m_pNeighbor[ 5].X = m_Coord.X+1;   m_pNeighbor[ 5].Y = m_Coord.Y-1;
            m_pNeighbor[ 6].X = m_Coord.X+1;   m_pNeighbor[ 6].Y = m_Coord.Y  ;
            m_pNeighbor[ 7].X = m_Coord.X+2;   m_pNeighbor[ 7].Y = m_Coord.Y  ;
            m_pNeighbor[ 8].X = m_Coord.X-1;   m_pNeighbor[ 8].Y = m_Coord.Y+1;
            m_pNeighbor[ 9].X = m_Coord.X  ;   m_pNeighbor[ 9].Y = m_Coord.Y+1;
            m_pNeighbor[10].X = m_Coord.X+1;   m_pNeighbor[10].Y = m_Coord.Y+1;
            m_pNeighbor[11].X = m_Coord.X  ;   m_pNeighbor[11].Y = m_Coord.Y+2;
            break;
   }
   for (int i=0; i<12; i++)
      if ((m_pNeighbor[i].X >= sizeField.X) ||
          (m_pNeighbor[i].Y >= sizeField.Y) ||
          (m_pNeighbor[i].X < 0) ||
          (m_pNeighbor[i].Y < 0)) {
         m_pNeighbor[i] = INCORRECT_COORD;
      }
}

bool nsCell::CTrSq2::PointInRegion(const POINT& point) const { // принадлежат ли эти экранные координаты ячейке
   switch (m_iDirection) {
   case 0:  case 21:
   case 3:  case 18:
   case 7:  case 28:
   case 10: case 25:
   case 14: case 35:
   case 17: case 32:
      return PointInPolygon(point, m_pRegion, 4);
   case 1:  case 22:
   case 2:  case 23:
   case 4:  case 19:
   case 5:  case 20:
   case 6:  case 27:
   case 8:  case 29:
   case 9:  case 24:
   case 11: case 26:
   case 12: case 33:
   case 13: case 34:
   case 15: case 30:
   case 16: case 31:
      return PointInPolygon(point, m_pRegion, 3);
   }
   return false;
}

void nsCell::CTrSq2::SetPoint(int area) {
   if (m_Coord.X==0 && m_Coord.Y==0) {
      m_a = sqrt(6*area/(2+SQRT3)); // размер стороны треугольника и квадрата
      m_b = m_a/2;
      m_h = m_b*SQRT3;
      m_sq = (m_a*SQRT3-m_GContext.m_Border.m_iWidth*6)/(SQRT3+2) - 1;
   }

   // определение координат точек фигуры
   float oX; // offset X
   float oY; // offset Y
   switch (m_iDirection) {
   case  0: case  1: case  2: case  6: case  7: case  8: case 12: case 13: case 14: oX = (m_h*2+m_a*3)*(m_Coord.X/6) + m_h+m_b;
                                                                                    oY = (m_h*2+m_a*3)*(m_Coord.Y/6) + m_h;            break;
   case  3: case  4: case  5: case  9: case 10: case 11: case 15: case 16: case 17: oX = (m_h*2+m_a*3)*(m_Coord.X/6) + m_h*2+m_a+m_b;
                                                                                    oY = (m_h*2+m_a*3)*(m_Coord.Y/6) + m_h+m_b;        break;
   case 18: case 19: case 20: case 24: case 25: case 26: case 30: case 31: case 32: oX = (m_h*2+m_a*3)*(m_Coord.X/6) + m_h;
                                                                                    oY = (m_h*2+m_a*3)*(m_Coord.Y/6) + m_h*2+m_a*2;    break;
   case 21: case 22: case 23: case 27: case 28: case 29: case 33: case 34: case 35: oX = (m_h*2+m_a*3)*(m_Coord.X/6) + m_h*2+m_a*2;
                                                                                    oY = (m_h*2+m_a*3)*(m_Coord.Y/6) + m_h*2+m_a+m_b;  break;
   }
   switch (m_iDirection) {
   case 0: case 21:
      m_pRegion[0].x = oX - m_b;     m_pRegion[0].y = oY - m_h;
      m_pRegion[1].x = oX - m_b-m_h; m_pRegion[1].y = oY + m_b-m_h;
      m_pRegion[2].x = oX - m_h;     m_pRegion[2].y = oY + m_b;
      m_pRegion[3].x = oX;           m_pRegion[3].y = oY;
      break;
   case 1: case 22:
      m_pRegion[0].x = oX + m_b;     m_pRegion[0].y = oY - m_h;
      m_pRegion[1].x = oX - m_b;     m_pRegion[1].y = oY - m_h;
      m_pRegion[2].x = oX;           m_pRegion[2].y = oY;
      break;
   case 2: case 23:
      m_pRegion[0].x = oX + m_a+m_b; m_pRegion[0].y = oY - m_h;
      m_pRegion[1].x = oX + m_b;     m_pRegion[1].y = oY - m_h;
      m_pRegion[2].x = oX + m_a;     m_pRegion[2].y = oY;
      break;
   case 3: case 18:
      m_pRegion[0].x = oX - m_h+m_b; m_pRegion[0].y = oY - m_b-m_h;
      m_pRegion[1].x = oX - m_h;     m_pRegion[1].y = oY - m_b;
      m_pRegion[2].x = oX;           m_pRegion[2].y = oY;
      m_pRegion[3].x = oX + m_b;     m_pRegion[3].y = oY - m_h;
      break;
   case 4: case 19:
      m_pRegion[0].x = oX + m_b;     m_pRegion[0].y = oY - m_h;
      m_pRegion[1].x = oX + m_a;     m_pRegion[1].y = oY;
      m_pRegion[2].x = oX;           m_pRegion[2].y = oY;
      break;
   case 5: case 20:
      m_pRegion[0].x = oX + m_a+m_b; m_pRegion[0].y = oY - m_h;
      m_pRegion[1].x = oX + m_b;     m_pRegion[1].y = oY - m_h;
      m_pRegion[2].x = oX + m_a;     m_pRegion[2].y = oY;
      break;
   case 6: case 27:
      m_pRegion[0].x = oX;           m_pRegion[0].y = oY;
      m_pRegion[1].x = oX - m_h;     m_pRegion[1].y = oY + m_b;
      m_pRegion[2].x = oX;           m_pRegion[2].y = oY + m_a;
      break;
   case 7: case 28:
      m_pRegion[0].x = oX + m_a;     m_pRegion[0].y = oY;
      m_pRegion[1].x = oX;           m_pRegion[1].y = oY;
      m_pRegion[2].x = oX;           m_pRegion[2].y = oY + m_a;
      m_pRegion[3].x = oX + m_a;     m_pRegion[3].y = oY + m_a;
      break;
   case 8: case 29:
      m_pRegion[0].x = oX + m_b;     m_pRegion[0].y = oY - m_h;
      m_pRegion[1].x = oX + m_a;     m_pRegion[1].y = oY;
      m_pRegion[2].x = oX;           m_pRegion[2].y = oY;
      break;
   case 9: case 24:
      m_pRegion[0].x = oX - m_h;     m_pRegion[0].y = oY - m_b;
      m_pRegion[1].x = oX;           m_pRegion[1].y = oY;
      m_pRegion[2].x = oX - m_h;     m_pRegion[2].y = oY + m_b;
      break;
   case 10: case 25:
      m_pRegion[0].x = oX + m_a;     m_pRegion[0].y = oY;
      m_pRegion[1].x = oX;           m_pRegion[1].y = oY;
      m_pRegion[2].x = oX;           m_pRegion[2].y = oY + m_a;
      m_pRegion[3].x = oX + m_a;     m_pRegion[3].y = oY + m_a;
      break;
   case 11: case 26:
      m_pRegion[0].x = oX + m_a+m_b; m_pRegion[0].y = oY - m_h;
      m_pRegion[1].x = oX + m_a+m_a; m_pRegion[1].y = oY;
      m_pRegion[2].x = oX + m_a;     m_pRegion[2].y = oY;
      break;
   case 12: case 33:
      m_pRegion[0].x = oX - m_h;     m_pRegion[0].y = oY + m_b;
      m_pRegion[1].x = oX;           m_pRegion[1].y = oY + m_a;
      m_pRegion[2].x = oX - m_h;     m_pRegion[2].y = oY + m_a+m_b;
      break;
   case 13: case 34:
      m_pRegion[0].x = oX;           m_pRegion[0].y = oY + m_a;
      m_pRegion[1].x = oX - m_h;     m_pRegion[1].y = oY + m_a+m_b;
      m_pRegion[2].x = oX;           m_pRegion[2].y = oY + m_a+m_a;
      break;
   case 14: case 35:
      m_pRegion[0].x = oX + m_a;     m_pRegion[0].y = oY + m_a;
      m_pRegion[1].x = oX;           m_pRegion[1].y = oY + m_a;
      m_pRegion[2].x = oX;           m_pRegion[2].y = oY + m_a+m_a;
      m_pRegion[3].x = oX + m_a;     m_pRegion[3].y = oY + m_a+m_a;
      break;
   case 15: case 30:
      m_pRegion[0].x = oX - m_h;     m_pRegion[0].y = oY + m_b;
      m_pRegion[1].x = oX;           m_pRegion[1].y = oY + m_a;
      m_pRegion[2].x = oX - m_h;     m_pRegion[2].y = oY + m_a+m_b;
      break;
   case 16: case 31:
      m_pRegion[0].x = oX;           m_pRegion[0].y = oY;
      m_pRegion[1].x = oX - m_h;     m_pRegion[1].y = oY + m_b;
      m_pRegion[2].x = oX;           m_pRegion[2].y = oY + m_a;
      break;
   case 17: case 32:
      m_pRegion[0].x = oX + m_a+m_a; m_pRegion[0].y = oY;
      m_pRegion[1].x = oX + m_a;     m_pRegion[1].y = oY;
      m_pRegion[2].x = oX + m_a;     m_pRegion[2].y = oY + m_a;
      m_pRegion[3].x = oX + m_a+m_a; m_pRegion[3].y = oY + m_a;
      break;
   }

   // определение координат вписанного в фигуру квадрата - область в которую выводится изображение/текст
   {
      POINTFLOAT center;
      float wsq2 = m_GContext.m_Border.m_iWidth+m_sq/2;
      switch (m_iDirection) {
      case  0: case 21: center.x = oX - (m_b+m_h)/2;  center.y = oY + (m_b-m_h)/2;  break;
      case  1: case 22: center.x = oX;                center.y = oY - m_h+wsq2;     break;
      case  2: case 23: center.x = oX + m_a;          center.y = oY - m_h+wsq2;     break;
      case  3: case 18: center.x = oX + (m_b-m_h)/2;  center.y = oY - (m_b+m_h)/2;  break;
      case  4: case 19: center.x = oX + m_b;          center.y = oY - wsq2;         break;
      case  5: case 20: center.x = oX + m_a;          center.y = oY - m_h+wsq2;     break;
      case  6: case 27: center.x = oX - wsq2;         center.y = oY + m_b;          break;
      case  7: case 28: center.x = oX + m_b;          center.y = oY + m_b;          break;
      case  8: case 29: center.x = oX + m_b;          center.y = oY - wsq2;         break;
      case  9: case 24: center.x = oX - m_h+wsq2;     center.y = oY;                break;
      case 10: case 25: center.x = oX + m_b;          center.y = oY + m_b;          break;
      case 11: case 26: center.x = oX + m_a+m_b;      center.y = oY - wsq2;         break;
      case 12: case 33: center.x = oX - m_h+wsq2;     center.y = oY + m_a;          break;
      case 13: case 34: center.x = oX - wsq2;         center.y = oY + m_a+m_b;      break;
      case 14: case 35: center.x = oX + m_b;          center.y = oY + m_a+m_b;      break;
      case 15: case 30: center.x = oX - m_h+wsq2;     center.y = oY + m_a;          break;
      case 16: case 31: center.x = oX - wsq2;         center.y = oY + m_b;          break; 
      case 17: case 32: center.x = oX + m_a+m_b;      center.y = oY + m_b;          break;
      }
      m_Square.left   = center.x - m_sq/2;
      m_Square.top    = center.y - m_sq/2;
      m_Square.right  = m_Square.left + m_sq;
      m_Square.bottom = m_Square.top  + m_sq;
   }
}

void nsCell::CTrSq2::Paint() const {
/**
   int minX = min(m_pRegion[1].x, min(m_pRegion[2].x, m_pRegion[3].x));
   int maxX = max(m_pRegion[0].x, m_pRegion[1].x);
   int minY = m_pRegion[0].y;
   int maxY = m_pRegion[2].y;
   BitBlt(m_GContext.m_hDCTmp, minX, minY, maxX - minX, maxY - minY,
          hDCBck, minX, minY, SRCCOPY);
/**/
   CBase::Paint();

   switch (m_iDirection) {
   case 4: case  8: case 11: case 19: case 26: case 29:
   case 9: case 12: case 15: case 24: case 30: case 33:
      SelectObject(m_GContext.m_hDCTmp, m_bDown ? m_GContext.m_hPenLight  : m_GContext.m_hPenShadow);
      MoveToEx(m_GContext.m_hDCTmp, m_pRegion[0].x, m_pRegion[0].y, NULL);
      LineTo  (m_GContext.m_hDCTmp, m_pRegion[1].x, m_pRegion[1].y);           break;
   case 0: case 21:
   case 3: case 18:
   case 7: case 10: case 14: case 17: case 25: case 28: case 32: case 35:
      SelectObject(m_GContext.m_hDCTmp, m_bDown ? m_GContext.m_hPenShadow : m_GContext.m_hPenLight );
      MoveToEx(m_GContext.m_hDCTmp, m_pRegion[1].x, m_pRegion[1].y, NULL);     break;
   case 1: case  2: case  5: case 20: case 22: case 23:
   case 6: case 13: case 16: case 27: case 31: case 34:
      SelectObject(m_GContext.m_hDCTmp, m_bDown ? m_GContext.m_hPenShadow : m_GContext.m_hPenLight );
      MoveToEx(m_GContext.m_hDCTmp, m_pRegion[0].x, m_pRegion[0].y, NULL);
      LineTo  (m_GContext.m_hDCTmp, m_pRegion[1].x, m_pRegion[1].y);           break;
   }
   LineTo  (m_GContext.m_hDCTmp, m_pRegion[2].x, m_pRegion[2].y);
   switch (m_iDirection) {
   case 4: case  8: case 11: case 19: case 26: case 29:
   case 9: case 12: case 15: case 24: case 30: case 33:
      SelectObject(m_GContext.m_hDCTmp, m_bDown ? m_GContext.m_hPenShadow : m_GContext.m_hPenLight ); break;
   case 0: case 21:
   case 3: case 18:
   case 7: case 10: case 14: case 17: case 25: case 28: case 32: case 35:
   case 1: case  2: case  5: case 20: case 22: case 23:
   case 6: case 13: case 16: case 27: case 31: case 34:
      SelectObject(m_GContext.m_hDCTmp, m_bDown ? m_GContext.m_hPenLight  : m_GContext.m_hPenShadow); break;
   }
   switch (m_iDirection) {
   case 0: case 21:
   case 3: case 18:
   case 7: case 10: case 14: case 17: case 25: case 28: case 32: case 35:
      LineTo  (m_GContext.m_hDCTmp, m_pRegion[3].x, m_pRegion[3].y);
      LineTo  (m_GContext.m_hDCTmp, m_pRegion[0].x, m_pRegion[0].y);
      SelectObject(m_GContext.m_hDCTmp, m_bDown ? m_GContext.m_hPenShadow : m_GContext.m_hPenLight );
      LineTo  (m_GContext.m_hDCTmp, m_pRegion[1].x, m_pRegion[1].y);
      RegionDraw(
         m_GContext.m_hDCDst, true,
         m_pRegion, 4, NULL, NULL, 0, 0, 0, 0,
         m_GContext.m_hDCTmp, NULL, 0,0);
      break;
   case 1: case  2: case  5: case 20: case 22: case 23:
   case 4: case  8: case 11: case 19: case 26: case 29:
   case 6: case 13: case 16: case 27: case 31: case 34:
   case 9: case 12: case 15: case 24: case 30: case 33:
      LineTo  (m_GContext.m_hDCTmp, m_pRegion[0].x, m_pRegion[0].y);
      RegionDraw(
         m_GContext.m_hDCDst, true,
         m_pRegion, 3, NULL, NULL, 0, 0, 0, 0,
         m_GContext.m_hDCTmp, NULL, 0,0);
      break;
   }
}
