////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "PentagonT5.cpp"
//
// Реализация класса CPentagonT5 - 5-ти угольник, тип №5
////////////////////////////////////////////////////////////////////////////////

#include "StdAfx.h"
#include "PentagonT5.h"

////////////////////////////////////////////////////////////////////////////////
//                               local init
////////////////////////////////////////////////////////////////////////////////

float nsCell::CPentagonT5::m_h;

////////////////////////////////////////////////////////////////////////////////
//                               static function
////////////////////////////////////////////////////////////////////////////////

SIZE nsCell::CPentagonT5::GetSizeInPixel(const SIZE &sizeField, int iArea) {
   m_a = 2*sqrt(iArea/SQRT147);
   m_h = m_a*SQRT3/2;
   SIZE result = {m_a*3.5f + m_a*2.0f*((sizeField.cx+13)/14) +
                             m_a     *((sizeField.cx+12)/14) +
                             m_a*1.5f*((sizeField.cx+11)/14) +
                             m_a*2.0f*((sizeField.cx+10)/14) +
                             m_a     *((sizeField.cx+ 9)/14) +
                             m_a*1.5f*((sizeField.cx+ 8)/14) +
                             m_a*2.0f*((sizeField.cx+ 7)/14) +
                             m_a     *((sizeField.cx+ 6)/14) +
                             m_a*1.5f*((sizeField.cx+ 5)/14) +
                             m_a*2.0f*((sizeField.cx+ 4)/14) +
                             m_a     *((sizeField.cx+ 3)/14) +
                             m_a*2.0f*((sizeField.cx+ 2)/14) +
                             m_a     *((sizeField.cx+ 1)/14) +
                             m_a*1.5f*((sizeField.cx+ 0)/14),
                  m_h*5 +    m_h*2   *((sizeField.cy+ 5)/ 6) +
                             m_h*2   *((sizeField.cy+ 4)/ 6) +
                             m_h*2   *((sizeField.cy+ 3)/ 6) +
                             m_h*3   *((sizeField.cy+ 2)/ 6) +
                             m_h*2   *((sizeField.cy+ 1)/ 6) +
                             m_h*3   *((sizeField.cy+ 0)/ 6)};
   return result;
}

int nsCell::CPentagonT5::SizeInscribedSquare(int iArea, int iBorderWidth) {
   m_a = 2*sqrt(iArea/SQRT147);
   m_sq = (m_a*2*SQRT3-4*iBorderWidth)/(SQRT3+1);
   return m_sq;
}

////////////////////////////////////////////////////////////////////////////////
//                               virtual function
////////////////////////////////////////////////////////////////////////////////

nsCell::CPentagonT5::CPentagonT5(const COORD &Coord, const SIZE &sizeField, int iArea, const CGraphicContext &gContext)
   : CBase(Coord, sizeField, iArea, gContext,
           8, 5,
           (Coord.Y%6)*14 + (Coord.X%14) // 0..83
          )
{
   SetPoint(iArea);
   // определяю координаты соседей
   switch (m_iDirection) {
   case 0:
      m_pNeighbor[0].X = m_Coord.X-2;   m_pNeighbor[0].Y = m_Coord.Y-2;
      m_pNeighbor[1].X = m_Coord.X-3;   m_pNeighbor[1].Y = m_Coord.Y-1;
      m_pNeighbor[2].X = m_Coord.X-2;   m_pNeighbor[2].Y = m_Coord.Y-1;
      m_pNeighbor[3].X = m_Coord.X+1;   m_pNeighbor[3].Y = m_Coord.Y  ;
      m_pNeighbor[4].X = m_Coord.X+2;   m_pNeighbor[4].Y = m_Coord.Y  ;
      m_pNeighbor[5].X = m_Coord.X  ;   m_pNeighbor[5].Y = m_Coord.Y+1;
      m_pNeighbor[6].X = m_Coord.X+1;   m_pNeighbor[6].Y = m_Coord.Y+1;
      m_pNeighbor[7].X = m_Coord.X+2;   m_pNeighbor[7].Y = m_Coord.Y+1;
      break;
   case 3: case 6: case 9:
      m_pNeighbor[0].X = m_Coord.X-2;   m_pNeighbor[0].Y = m_Coord.Y-1;
      m_pNeighbor[1].X = m_Coord.X-1;   m_pNeighbor[1].Y = m_Coord.Y  ;
      m_pNeighbor[2].X = m_Coord.X+1;   m_pNeighbor[2].Y = m_Coord.Y  ;
      m_pNeighbor[3].X = m_Coord.X+2;   m_pNeighbor[3].Y = m_Coord.Y  ;
      m_pNeighbor[4].X = m_Coord.X-1;   m_pNeighbor[4].Y = m_Coord.Y+1;
      m_pNeighbor[5].X = m_Coord.X  ;   m_pNeighbor[5].Y = m_Coord.Y+1;  
      m_pNeighbor[6].X = m_Coord.X+1;   m_pNeighbor[6].Y = m_Coord.Y+1;
      m_pNeighbor[7].X = m_Coord.X+2;   m_pNeighbor[7].Y = m_Coord.Y+1;
      break;
   case 12:
      m_pNeighbor[0].X = m_Coord.X-2;   m_pNeighbor[0].Y = m_Coord.Y-1;
      m_pNeighbor[1].X = m_Coord.X-1;   m_pNeighbor[1].Y = m_Coord.Y  ;
      m_pNeighbor[2].X = m_Coord.X+1;   m_pNeighbor[2].Y = m_Coord.Y  ;
      m_pNeighbor[3].X = m_Coord.X-1;   m_pNeighbor[3].Y = m_Coord.Y+1;
      m_pNeighbor[4].X = m_Coord.X  ;   m_pNeighbor[4].Y = m_Coord.Y+1;
      m_pNeighbor[5].X = m_Coord.X+1;   m_pNeighbor[5].Y = m_Coord.Y+1;
      m_pNeighbor[6].X = m_Coord.X  ;   m_pNeighbor[6].Y = m_Coord.Y+2;
      m_pNeighbor[7].X = m_Coord.X+1;   m_pNeighbor[7].Y = m_Coord.Y+2;
      break;
   case 28:
      m_pNeighbor[0].X = m_Coord.X-1;   m_pNeighbor[0].Y = m_Coord.Y-1;
      m_pNeighbor[1].X = m_Coord.X+1;   m_pNeighbor[1].Y = m_Coord.Y-1;
      m_pNeighbor[2].X = m_Coord.X-1;   m_pNeighbor[2].Y = m_Coord.Y  ;
      m_pNeighbor[3].X = m_Coord.X+1;   m_pNeighbor[3].Y = m_Coord.Y  ;
      m_pNeighbor[4].X = m_Coord.X+2;   m_pNeighbor[4].Y = m_Coord.Y  ;
      m_pNeighbor[5].X = m_Coord.X  ;   m_pNeighbor[5].Y = m_Coord.Y+1;
      m_pNeighbor[6].X = m_Coord.X+1;   m_pNeighbor[6].Y = m_Coord.Y+1;
      m_pNeighbor[7].X = m_Coord.X+2;   m_pNeighbor[7].Y = m_Coord.Y+1;
      break;
   case 31: case 34: case 37: case 56: case 59: case 62:
      m_pNeighbor[0].X = m_Coord.X+1;   m_pNeighbor[0].Y = m_Coord.Y-1;
      m_pNeighbor[1].X = m_Coord.X-1;   m_pNeighbor[1].Y = m_Coord.Y  ;
      m_pNeighbor[2].X = m_Coord.X+1;   m_pNeighbor[2].Y = m_Coord.Y  ;
      m_pNeighbor[3].X = m_Coord.X+2;   m_pNeighbor[3].Y = m_Coord.Y  ;
      m_pNeighbor[4].X = m_Coord.X-1;   m_pNeighbor[4].Y = m_Coord.Y+1;
      m_pNeighbor[5].X = m_Coord.X  ;   m_pNeighbor[5].Y = m_Coord.Y+1;
      m_pNeighbor[6].X = m_Coord.X+1;   m_pNeighbor[6].Y = m_Coord.Y+1;
      m_pNeighbor[7].X = m_Coord.X+2;   m_pNeighbor[7].Y = m_Coord.Y+1;
      break;
   case 54:
      m_pNeighbor[0].X = m_Coord.X-1;   m_pNeighbor[0].Y = m_Coord.Y-1;
      m_pNeighbor[1].X = m_Coord.X  ;   m_pNeighbor[1].Y = m_Coord.Y-1;
      m_pNeighbor[2].X = m_Coord.X-1;   m_pNeighbor[2].Y = m_Coord.Y  ;
      m_pNeighbor[3].X = m_Coord.X+1;   m_pNeighbor[3].Y = m_Coord.Y  ;
      m_pNeighbor[4].X = m_Coord.X-1;   m_pNeighbor[4].Y = m_Coord.Y+1;
      m_pNeighbor[5].X = m_Coord.X  ;   m_pNeighbor[5].Y = m_Coord.Y+1;
      m_pNeighbor[6].X = m_Coord.X+1;   m_pNeighbor[6].Y = m_Coord.Y+1;
      m_pNeighbor[7].X = m_Coord.X+1;   m_pNeighbor[7].Y = m_Coord.Y+2;
      break;
   case 65:
      m_pNeighbor[0].X = m_Coord.X+1;   m_pNeighbor[0].Y = m_Coord.Y-1;
      m_pNeighbor[1].X = m_Coord.X-1;   m_pNeighbor[1].Y = m_Coord.Y  ;
      m_pNeighbor[2].X = m_Coord.X+1;   m_pNeighbor[2].Y = m_Coord.Y  ;
      m_pNeighbor[3].X = m_Coord.X-1;   m_pNeighbor[3].Y = m_Coord.Y+1;
      m_pNeighbor[4].X = m_Coord.X  ;   m_pNeighbor[4].Y = m_Coord.Y+1;
      m_pNeighbor[5].X = m_Coord.X+1;   m_pNeighbor[5].Y = m_Coord.Y+1;
      m_pNeighbor[6].X = m_Coord.X+2;   m_pNeighbor[6].Y = m_Coord.Y+1;
      m_pNeighbor[7].X = m_Coord.X+3;   m_pNeighbor[7].Y = m_Coord.Y+1;
      break;
   case 1:
      m_pNeighbor[0].X = m_Coord.X-3;   m_pNeighbor[0].Y = m_Coord.Y-2;
      m_pNeighbor[1].X = m_Coord.X-2;   m_pNeighbor[1].Y = m_Coord.Y-1;
      m_pNeighbor[2].X = m_Coord.X-1;   m_pNeighbor[2].Y = m_Coord.Y-1;
      m_pNeighbor[3].X = m_Coord.X-1;   m_pNeighbor[3].Y = m_Coord.Y  ;
      m_pNeighbor[4].X = m_Coord.X+1;   m_pNeighbor[4].Y = m_Coord.Y  ;
      m_pNeighbor[5].X = m_Coord.X-1;   m_pNeighbor[5].Y = m_Coord.Y+1;
      m_pNeighbor[6].X = m_Coord.X  ;   m_pNeighbor[6].Y = m_Coord.Y+1;
      m_pNeighbor[7].X = m_Coord.X+1;   m_pNeighbor[7].Y = m_Coord.Y+1;
      break;
   case 4: case 7: case 10:
      m_pNeighbor[0].X = m_Coord.X-3;   m_pNeighbor[0].Y = m_Coord.Y-1;
      m_pNeighbor[1].X = m_Coord.X-2;   m_pNeighbor[1].Y = m_Coord.Y-1;
      m_pNeighbor[2].X = m_Coord.X-1;   m_pNeighbor[2].Y = m_Coord.Y-1;
      m_pNeighbor[3].X = m_Coord.X-1;   m_pNeighbor[3].Y = m_Coord.Y  ;
      m_pNeighbor[4].X = m_Coord.X+1;   m_pNeighbor[4].Y = m_Coord.Y  ;
      m_pNeighbor[5].X = m_Coord.X-1;   m_pNeighbor[5].Y = m_Coord.Y+1;
      m_pNeighbor[6].X = m_Coord.X  ;   m_pNeighbor[6].Y = m_Coord.Y+1;
      m_pNeighbor[7].X = m_Coord.X+1;   m_pNeighbor[7].Y = m_Coord.Y+1;
      break;
   case 13:
      m_pNeighbor[0].X = m_Coord.X-3;   m_pNeighbor[0].Y = m_Coord.Y-1;
      m_pNeighbor[1].X = m_Coord.X-2;   m_pNeighbor[1].Y = m_Coord.Y-1;
      m_pNeighbor[2].X = m_Coord.X-1;   m_pNeighbor[2].Y = m_Coord.Y  ;
      m_pNeighbor[3].X = m_Coord.X-1;   m_pNeighbor[3].Y = m_Coord.Y+1;
      m_pNeighbor[4].X = m_Coord.X  ;   m_pNeighbor[4].Y = m_Coord.Y+1;
      m_pNeighbor[5].X = m_Coord.X+1;   m_pNeighbor[5].Y = m_Coord.Y+1;
      m_pNeighbor[6].X = m_Coord.X-1;   m_pNeighbor[6].Y = m_Coord.Y+2;
      m_pNeighbor[7].X = m_Coord.X  ;   m_pNeighbor[7].Y = m_Coord.Y+2;
      break;
   case 29: case 32: case 35: case 38: case 57: case 60: case 63:
      m_pNeighbor[0].X = m_Coord.X  ;   m_pNeighbor[0].Y = m_Coord.Y-1;
      m_pNeighbor[1].X = m_Coord.X+1;   m_pNeighbor[1].Y = m_Coord.Y-1;
      m_pNeighbor[2].X = m_Coord.X+2;   m_pNeighbor[2].Y = m_Coord.Y-1;
      m_pNeighbor[3].X = m_Coord.X-1;   m_pNeighbor[3].Y = m_Coord.Y  ;
      m_pNeighbor[4].X = m_Coord.X+1;   m_pNeighbor[4].Y = m_Coord.Y  ;
      m_pNeighbor[5].X = m_Coord.X-1;   m_pNeighbor[5].Y = m_Coord.Y+1;
      m_pNeighbor[6].X = m_Coord.X  ;   m_pNeighbor[6].Y = m_Coord.Y+1;
      m_pNeighbor[7].X = m_Coord.X+1;   m_pNeighbor[7].Y = m_Coord.Y+1;
      break;
   case 55:
      m_pNeighbor[0].X = m_Coord.X-1;   m_pNeighbor[0].Y = m_Coord.Y-1;
      m_pNeighbor[1].X = m_Coord.X  ;   m_pNeighbor[1].Y = m_Coord.Y-1;
      m_pNeighbor[2].X = m_Coord.X-1;   m_pNeighbor[2].Y = m_Coord.Y  ;
      m_pNeighbor[3].X = m_Coord.X+1;   m_pNeighbor[3].Y = m_Coord.Y  ;
      m_pNeighbor[4].X = m_Coord.X-2;   m_pNeighbor[4].Y = m_Coord.Y+1;
      m_pNeighbor[5].X = m_Coord.X-1;   m_pNeighbor[5].Y = m_Coord.Y+1;
      m_pNeighbor[6].X = m_Coord.X  ;   m_pNeighbor[6].Y = m_Coord.Y+1;
      m_pNeighbor[7].X = m_Coord.X  ;   m_pNeighbor[7].Y = m_Coord.Y+2;
      break;
   case 66:
      m_pNeighbor[0].X = m_Coord.X  ;   m_pNeighbor[0].Y = m_Coord.Y-1;
      m_pNeighbor[1].X = m_Coord.X+1;   m_pNeighbor[1].Y = m_Coord.Y-1;
      m_pNeighbor[2].X = m_Coord.X-1;   m_pNeighbor[2].Y = m_Coord.Y  ;
      m_pNeighbor[3].X = m_Coord.X+1;   m_pNeighbor[3].Y = m_Coord.Y  ;
      m_pNeighbor[4].X = m_Coord.X-1;   m_pNeighbor[4].Y = m_Coord.Y+1;
      m_pNeighbor[5].X = m_Coord.X  ;   m_pNeighbor[5].Y = m_Coord.Y+1;
      m_pNeighbor[6].X = m_Coord.X+1;   m_pNeighbor[6].Y = m_Coord.Y+1;
      m_pNeighbor[7].X = m_Coord.X+2;   m_pNeighbor[7].Y = m_Coord.Y+1;
      break;
   case 2: case 5: case 8: case 11:
      m_pNeighbor[0].X = m_Coord.X-2;   m_pNeighbor[0].Y = m_Coord.Y-1;
      m_pNeighbor[1].X = m_Coord.X-1;   m_pNeighbor[1].Y = m_Coord.Y-1;
      m_pNeighbor[2].X = m_Coord.X-2;   m_pNeighbor[2].Y = m_Coord.Y  ;
      m_pNeighbor[3].X = m_Coord.X-1;   m_pNeighbor[3].Y = m_Coord.Y  ;
      m_pNeighbor[4].X = m_Coord.X+1;   m_pNeighbor[4].Y = m_Coord.Y  ;
      m_pNeighbor[5].X = m_Coord.X-2;   m_pNeighbor[5].Y = m_Coord.Y+1;
      m_pNeighbor[6].X = m_Coord.X-1;   m_pNeighbor[6].Y = m_Coord.Y+1;
      m_pNeighbor[7].X = m_Coord.X  ;   m_pNeighbor[7].Y = m_Coord.Y+1;
      break;
   case 27:
      m_pNeighbor[0].X = m_Coord.X-1;   m_pNeighbor[0].Y = m_Coord.Y-1;
      m_pNeighbor[1].X = m_Coord.X  ;   m_pNeighbor[1].Y = m_Coord.Y-1;
      m_pNeighbor[2].X = m_Coord.X-1;   m_pNeighbor[2].Y = m_Coord.Y  ;
      m_pNeighbor[3].X = m_Coord.X+1;   m_pNeighbor[3].Y = m_Coord.Y  ;
      m_pNeighbor[4].X = m_Coord.X+2;   m_pNeighbor[4].Y = m_Coord.Y  ;
      m_pNeighbor[5].X = m_Coord.X-1;   m_pNeighbor[5].Y = m_Coord.Y+1;
      m_pNeighbor[6].X = m_Coord.X  ;   m_pNeighbor[6].Y = m_Coord.Y+1;
      m_pNeighbor[7].X = m_Coord.X+1;   m_pNeighbor[7].Y = m_Coord.Y+1;
      break;
   case 30: case 33: case 36: case 58: case 61: case 64:
      m_pNeighbor[0].X = m_Coord.X+1;   m_pNeighbor[0].Y = m_Coord.Y-1;
      m_pNeighbor[1].X = m_Coord.X+2;   m_pNeighbor[1].Y = m_Coord.Y-1;
      m_pNeighbor[2].X = m_Coord.X-2;   m_pNeighbor[2].Y = m_Coord.Y  ;
      m_pNeighbor[3].X = m_Coord.X-1;   m_pNeighbor[3].Y = m_Coord.Y  ;
      m_pNeighbor[4].X = m_Coord.X+1;   m_pNeighbor[4].Y = m_Coord.Y  ;
      m_pNeighbor[5].X = m_Coord.X-2;   m_pNeighbor[5].Y = m_Coord.Y+1;
      m_pNeighbor[6].X = m_Coord.X-1;   m_pNeighbor[6].Y = m_Coord.Y+1;
      m_pNeighbor[7].X = m_Coord.X  ;   m_pNeighbor[7].Y = m_Coord.Y+1;
      break;
   case 39:
      m_pNeighbor[0].X = m_Coord.X+1;   m_pNeighbor[0].Y = m_Coord.Y-1;
      m_pNeighbor[1].X = m_Coord.X-2;   m_pNeighbor[1].Y = m_Coord.Y  ;
      m_pNeighbor[2].X = m_Coord.X-1;   m_pNeighbor[2].Y = m_Coord.Y  ;
      m_pNeighbor[3].X = m_Coord.X+1;   m_pNeighbor[3].Y = m_Coord.Y  ;
      m_pNeighbor[4].X = m_Coord.X-2;   m_pNeighbor[4].Y = m_Coord.Y+1;
      m_pNeighbor[5].X = m_Coord.X-1;   m_pNeighbor[5].Y = m_Coord.Y+1;
      m_pNeighbor[6].X = m_Coord.X  ;   m_pNeighbor[6].Y = m_Coord.Y+1;
      m_pNeighbor[7].X = m_Coord.X+1;   m_pNeighbor[7].Y = m_Coord.Y+1;
      break;
   case 69:
      m_pNeighbor[0].X = m_Coord.X-1;   m_pNeighbor[0].Y = m_Coord.Y-1;
      m_pNeighbor[1].X = m_Coord.X  ;   m_pNeighbor[1].Y = m_Coord.Y-1;
      m_pNeighbor[2].X = m_Coord.X+1;   m_pNeighbor[2].Y = m_Coord.Y-1;
      m_pNeighbor[3].X = m_Coord.X+2;   m_pNeighbor[3].Y = m_Coord.Y-1;
      m_pNeighbor[4].X = m_Coord.X-2;   m_pNeighbor[4].Y = m_Coord.Y  ;
      m_pNeighbor[5].X = m_Coord.X-1;   m_pNeighbor[5].Y = m_Coord.Y  ;
      m_pNeighbor[6].X = m_Coord.X+1;   m_pNeighbor[6].Y = m_Coord.Y  ;
      m_pNeighbor[7].X = m_Coord.X  ;   m_pNeighbor[7].Y = m_Coord.Y+1;
      break;
   case 82:
      m_pNeighbor[0].X = m_Coord.X-3;   m_pNeighbor[0].Y = m_Coord.Y-1;
      m_pNeighbor[1].X = m_Coord.X-2;   m_pNeighbor[1].Y = m_Coord.Y-1;
      m_pNeighbor[2].X = m_Coord.X-1;   m_pNeighbor[2].Y = m_Coord.Y-1;
      m_pNeighbor[3].X = m_Coord.X  ;   m_pNeighbor[3].Y = m_Coord.Y-1;
      m_pNeighbor[4].X = m_Coord.X-3;   m_pNeighbor[4].Y = m_Coord.Y  ;
      m_pNeighbor[5].X = m_Coord.X-2;   m_pNeighbor[5].Y = m_Coord.Y  ;
      m_pNeighbor[6].X = m_Coord.X-1;   m_pNeighbor[6].Y = m_Coord.Y  ;
      m_pNeighbor[7].X = m_Coord.X+2;   m_pNeighbor[7].Y = m_Coord.Y+1;
      break;
   case 14:
      m_pNeighbor[0].X = m_Coord.X-3;   m_pNeighbor[0].Y = m_Coord.Y-2;
      m_pNeighbor[1].X = m_Coord.X-1;   m_pNeighbor[1].Y = m_Coord.Y-1;
      m_pNeighbor[2].X = m_Coord.X  ;   m_pNeighbor[2].Y = m_Coord.Y-1;
      m_pNeighbor[3].X = m_Coord.X+1;   m_pNeighbor[3].Y = m_Coord.Y-1;
      m_pNeighbor[4].X = m_Coord.X+2;   m_pNeighbor[4].Y = m_Coord.Y-1;
      m_pNeighbor[5].X = m_Coord.X-1;   m_pNeighbor[5].Y = m_Coord.Y  ;
      m_pNeighbor[6].X = m_Coord.X+1;   m_pNeighbor[6].Y = m_Coord.Y  ;
      m_pNeighbor[7].X = m_Coord.X+2;   m_pNeighbor[7].Y = m_Coord.Y  ;
      break;
   case 17: case 20: case 23: case 45: case 48: case 51:
      m_pNeighbor[0].X = m_Coord.X  ;   m_pNeighbor[0].Y = m_Coord.Y-1;
      m_pNeighbor[1].X = m_Coord.X+1;   m_pNeighbor[1].Y = m_Coord.Y-1;
      m_pNeighbor[2].X = m_Coord.X+2;   m_pNeighbor[2].Y = m_Coord.Y-1;
      m_pNeighbor[3].X = m_Coord.X-1;   m_pNeighbor[3].Y = m_Coord.Y  ;
      m_pNeighbor[4].X = m_Coord.X+1;   m_pNeighbor[4].Y = m_Coord.Y  ;
      m_pNeighbor[5].X = m_Coord.X+2;   m_pNeighbor[5].Y = m_Coord.Y  ;
      m_pNeighbor[6].X = m_Coord.X-2;   m_pNeighbor[6].Y = m_Coord.Y+1;
      m_pNeighbor[7].X = m_Coord.X-1;   m_pNeighbor[7].Y = m_Coord.Y+1;
      break;
   case 26:
      m_pNeighbor[0].X = m_Coord.X  ;   m_pNeighbor[0].Y = m_Coord.Y-1;
      m_pNeighbor[1].X = m_Coord.X+1;   m_pNeighbor[1].Y = m_Coord.Y-1;
      m_pNeighbor[2].X = m_Coord.X-1;   m_pNeighbor[2].Y = m_Coord.Y  ;
      m_pNeighbor[3].X = m_Coord.X+1;   m_pNeighbor[3].Y = m_Coord.Y  ;
      m_pNeighbor[4].X = m_Coord.X-2;   m_pNeighbor[4].Y = m_Coord.Y+1;
      m_pNeighbor[5].X = m_Coord.X-1;   m_pNeighbor[5].Y = m_Coord.Y+1;
      m_pNeighbor[6].X = m_Coord.X  ;   m_pNeighbor[6].Y = m_Coord.Y+1;
      m_pNeighbor[7].X = m_Coord.X+1;   m_pNeighbor[7].Y = m_Coord.Y+1;
      break;
   case 42:
      m_pNeighbor[0].X = m_Coord.X-1;   m_pNeighbor[0].Y = m_Coord.Y-1;
      m_pNeighbor[1].X = m_Coord.X  ;   m_pNeighbor[1].Y = m_Coord.Y-1;
      m_pNeighbor[2].X = m_Coord.X+1;   m_pNeighbor[2].Y = m_Coord.Y-1;
      m_pNeighbor[3].X = m_Coord.X+2;   m_pNeighbor[3].Y = m_Coord.Y-1;
      m_pNeighbor[4].X = m_Coord.X-1;   m_pNeighbor[4].Y = m_Coord.Y  ;
      m_pNeighbor[5].X = m_Coord.X+1;   m_pNeighbor[5].Y = m_Coord.Y  ;
      m_pNeighbor[6].X = m_Coord.X+2;   m_pNeighbor[6].Y = m_Coord.Y  ;
      m_pNeighbor[7].X = m_Coord.X-1;   m_pNeighbor[7].Y = m_Coord.Y+1;
      break;
   case 67:
   case 70: case 73: case 76:
      m_pNeighbor[0].X = m_Coord.X  ;   m_pNeighbor[0].Y = m_Coord.Y-1;
      m_pNeighbor[1].X = m_Coord.X+1;   m_pNeighbor[1].Y = m_Coord.Y-1;
      m_pNeighbor[2].X = m_Coord.X+2;   m_pNeighbor[2].Y = m_Coord.Y-1;
      m_pNeighbor[3].X = m_Coord.X-1;   m_pNeighbor[3].Y = m_Coord.Y  ;
      m_pNeighbor[4].X = m_Coord.X+1;   m_pNeighbor[4].Y = m_Coord.Y  ;
      m_pNeighbor[5].X = m_Coord.X+2;   m_pNeighbor[5].Y = m_Coord.Y  ;
      m_pNeighbor[6].X = m_Coord.X+1;   m_pNeighbor[6].Y = m_Coord.Y+1;
      m_pNeighbor[7].X = m_Coord.X+2;   m_pNeighbor[7].Y = m_Coord.Y+1;
      break;
   case 79:
      m_pNeighbor[0].X = m_Coord.X  ;   m_pNeighbor[0].Y = m_Coord.Y-1;
      m_pNeighbor[1].X = m_Coord.X+1;   m_pNeighbor[1].Y = m_Coord.Y-1;
      m_pNeighbor[2].X = m_Coord.X-1;   m_pNeighbor[2].Y = m_Coord.Y  ;
      m_pNeighbor[3].X = m_Coord.X+1;   m_pNeighbor[3].Y = m_Coord.Y  ;
      m_pNeighbor[4].X = m_Coord.X+2;   m_pNeighbor[4].Y = m_Coord.Y  ;
      m_pNeighbor[5].X = m_Coord.X+3;   m_pNeighbor[5].Y = m_Coord.Y  ;
      m_pNeighbor[6].X = m_Coord.X+1;   m_pNeighbor[6].Y = m_Coord.Y+1;
      m_pNeighbor[7].X = m_Coord.X+2;   m_pNeighbor[7].Y = m_Coord.Y+1;
      break;
   case 15:
      m_pNeighbor[0].X = m_Coord.X-1;   m_pNeighbor[0].Y = m_Coord.Y-1;
      m_pNeighbor[1].X = m_Coord.X  ;   m_pNeighbor[1].Y = m_Coord.Y-1;
      m_pNeighbor[2].X = m_Coord.X+1;   m_pNeighbor[2].Y = m_Coord.Y-1;
      m_pNeighbor[3].X = m_Coord.X-2;   m_pNeighbor[3].Y = m_Coord.Y  ;
      m_pNeighbor[4].X = m_Coord.X-1;   m_pNeighbor[4].Y = m_Coord.Y  ;
      m_pNeighbor[5].X = m_Coord.X+1;   m_pNeighbor[5].Y = m_Coord.Y  ;
      m_pNeighbor[6].X = m_Coord.X-1;   m_pNeighbor[6].Y = m_Coord.Y+1;
      m_pNeighbor[7].X = m_Coord.X  ;   m_pNeighbor[7].Y = m_Coord.Y+1;
      break;
   case 18: case 21: case 24: case 43: case 46: case 49: case 52:
      m_pNeighbor[0].X = m_Coord.X-1;   m_pNeighbor[0].Y = m_Coord.Y-1;
      m_pNeighbor[1].X = m_Coord.X  ;   m_pNeighbor[1].Y = m_Coord.Y-1;
      m_pNeighbor[2].X = m_Coord.X+1;   m_pNeighbor[2].Y = m_Coord.Y-1;
      m_pNeighbor[3].X = m_Coord.X-1;   m_pNeighbor[3].Y = m_Coord.Y  ;
      m_pNeighbor[4].X = m_Coord.X+1;   m_pNeighbor[4].Y = m_Coord.Y  ;
      m_pNeighbor[5].X = m_Coord.X-2;   m_pNeighbor[5].Y = m_Coord.Y+1;
      m_pNeighbor[6].X = m_Coord.X-1;   m_pNeighbor[6].Y = m_Coord.Y+1;
      m_pNeighbor[7].X = m_Coord.X  ;   m_pNeighbor[7].Y = m_Coord.Y+1;
      break;
   case 40:
      m_pNeighbor[0].X = m_Coord.X  ;   m_pNeighbor[0].Y = m_Coord.Y-2;
      m_pNeighbor[1].X = m_Coord.X+1;   m_pNeighbor[1].Y = m_Coord.Y-2;
      m_pNeighbor[2].X = m_Coord.X  ;   m_pNeighbor[2].Y = m_Coord.Y-1;
      m_pNeighbor[3].X = m_Coord.X+1;   m_pNeighbor[3].Y = m_Coord.Y-1;
      m_pNeighbor[4].X = m_Coord.X-1;   m_pNeighbor[4].Y = m_Coord.Y  ;
      m_pNeighbor[5].X = m_Coord.X+1;   m_pNeighbor[5].Y = m_Coord.Y  ;
      m_pNeighbor[6].X = m_Coord.X  ;   m_pNeighbor[6].Y = m_Coord.Y+1;
      m_pNeighbor[7].X = m_Coord.X+1;   m_pNeighbor[7].Y = m_Coord.Y+1;
      break;
   case 68:
      m_pNeighbor[0].X = m_Coord.X  ;   m_pNeighbor[0].Y = m_Coord.Y-1;
      m_pNeighbor[1].X = m_Coord.X+1;   m_pNeighbor[1].Y = m_Coord.Y-1;
      m_pNeighbor[2].X = m_Coord.X-1;   m_pNeighbor[2].Y = m_Coord.Y  ;
      m_pNeighbor[3].X = m_Coord.X+1;   m_pNeighbor[3].Y = m_Coord.Y  ;
      m_pNeighbor[4].X = m_Coord.X  ;   m_pNeighbor[4].Y = m_Coord.Y+1;
      m_pNeighbor[5].X = m_Coord.X+1;   m_pNeighbor[5].Y = m_Coord.Y+1;
      m_pNeighbor[6].X = m_Coord.X+2;   m_pNeighbor[6].Y = m_Coord.Y+2;
      m_pNeighbor[7].X = m_Coord.X+3;   m_pNeighbor[7].Y = m_Coord.Y+2;
      break;
   case 71: case 74: case 77:
      m_pNeighbor[0].X = m_Coord.X-1;   m_pNeighbor[0].Y = m_Coord.Y-1;
      m_pNeighbor[1].X = m_Coord.X  ;   m_pNeighbor[1].Y = m_Coord.Y-1;
      m_pNeighbor[2].X = m_Coord.X+1;   m_pNeighbor[2].Y = m_Coord.Y-1;
      m_pNeighbor[3].X = m_Coord.X-1;   m_pNeighbor[3].Y = m_Coord.Y  ;
      m_pNeighbor[4].X = m_Coord.X+1;   m_pNeighbor[4].Y = m_Coord.Y  ;
      m_pNeighbor[5].X = m_Coord.X+1;   m_pNeighbor[5].Y = m_Coord.Y+1;
      m_pNeighbor[6].X = m_Coord.X+2;   m_pNeighbor[6].Y = m_Coord.Y+1;
      m_pNeighbor[7].X = m_Coord.X+3;   m_pNeighbor[7].Y = m_Coord.Y+1;
      break;
   case 80:
      m_pNeighbor[0].X = m_Coord.X-1;   m_pNeighbor[0].Y = m_Coord.Y-1;
      m_pNeighbor[1].X = m_Coord.X  ;   m_pNeighbor[1].Y = m_Coord.Y-1;
      m_pNeighbor[2].X = m_Coord.X-1;   m_pNeighbor[2].Y = m_Coord.Y  ;
      m_pNeighbor[3].X = m_Coord.X+1;   m_pNeighbor[3].Y = m_Coord.Y  ;
      m_pNeighbor[4].X = m_Coord.X+2;   m_pNeighbor[4].Y = m_Coord.Y  ;
      m_pNeighbor[5].X = m_Coord.X+1;   m_pNeighbor[5].Y = m_Coord.Y+1;
      m_pNeighbor[6].X = m_Coord.X+2;   m_pNeighbor[6].Y = m_Coord.Y+1;
      m_pNeighbor[7].X = m_Coord.X+3;   m_pNeighbor[7].Y = m_Coord.Y+1;
      break;
   case 16: case 19: case 22: case 25: case 44: case 47: case 50:
      m_pNeighbor[0].X = m_Coord.X-2;   m_pNeighbor[0].Y = m_Coord.Y-1;
      m_pNeighbor[1].X = m_Coord.X-1;   m_pNeighbor[1].Y = m_Coord.Y-1;
      m_pNeighbor[2].X = m_Coord.X  ;   m_pNeighbor[2].Y = m_Coord.Y-1;
      m_pNeighbor[3].X = m_Coord.X+1;   m_pNeighbor[3].Y = m_Coord.Y-1;
      m_pNeighbor[4].X = m_Coord.X-2;   m_pNeighbor[4].Y = m_Coord.Y  ;
      m_pNeighbor[5].X = m_Coord.X-1;   m_pNeighbor[5].Y = m_Coord.Y  ;
      m_pNeighbor[6].X = m_Coord.X+1;   m_pNeighbor[6].Y = m_Coord.Y  ;
      m_pNeighbor[7].X = m_Coord.X-1;   m_pNeighbor[7].Y = m_Coord.Y+1;
      break;
   case 41:
      m_pNeighbor[0].X = m_Coord.X-1;   m_pNeighbor[0].Y = m_Coord.Y-2;
      m_pNeighbor[1].X = m_Coord.X  ;   m_pNeighbor[1].Y = m_Coord.Y-2;
      m_pNeighbor[2].X = m_Coord.X-1;   m_pNeighbor[2].Y = m_Coord.Y-1;
      m_pNeighbor[3].X = m_Coord.X  ;   m_pNeighbor[3].Y = m_Coord.Y-1;
      m_pNeighbor[4].X = m_Coord.X-1;   m_pNeighbor[4].Y = m_Coord.Y  ;
      m_pNeighbor[5].X = m_Coord.X+1;   m_pNeighbor[5].Y = m_Coord.Y  ;
      m_pNeighbor[6].X = m_Coord.X  ;   m_pNeighbor[6].Y = m_Coord.Y+1;
      m_pNeighbor[7].X = m_Coord.X+1;   m_pNeighbor[7].Y = m_Coord.Y+1;
      break;
   case 53:
      m_pNeighbor[0].X = m_Coord.X-2;   m_pNeighbor[0].Y = m_Coord.Y-1;
      m_pNeighbor[1].X = m_Coord.X-1;   m_pNeighbor[1].Y = m_Coord.Y-1;
      m_pNeighbor[2].X = m_Coord.X  ;   m_pNeighbor[2].Y = m_Coord.Y-1;
      m_pNeighbor[3].X = m_Coord.X-2;   m_pNeighbor[3].Y = m_Coord.Y  ;
      m_pNeighbor[4].X = m_Coord.X-1;   m_pNeighbor[4].Y = m_Coord.Y  ;
      m_pNeighbor[5].X = m_Coord.X+1;   m_pNeighbor[5].Y = m_Coord.Y  ;
      m_pNeighbor[6].X = m_Coord.X-1;   m_pNeighbor[6].Y = m_Coord.Y+1;
      m_pNeighbor[7].X = m_Coord.X  ;   m_pNeighbor[7].Y = m_Coord.Y+1;
      break;
   case 83:
      m_pNeighbor[0].X = m_Coord.X-1;   m_pNeighbor[0].Y = m_Coord.Y-2;
      m_pNeighbor[1].X = m_Coord.X  ;   m_pNeighbor[1].Y = m_Coord.Y-2;
      m_pNeighbor[2].X = m_Coord.X-2;   m_pNeighbor[2].Y = m_Coord.Y-1;
      m_pNeighbor[3].X = m_Coord.X-1;   m_pNeighbor[3].Y = m_Coord.Y-1;
      m_pNeighbor[4].X = m_Coord.X  ;   m_pNeighbor[4].Y = m_Coord.Y-1;
      m_pNeighbor[5].X = m_Coord.X+1;   m_pNeighbor[5].Y = m_Coord.Y-1;
      m_pNeighbor[6].X = m_Coord.X+1;   m_pNeighbor[6].Y = m_Coord.Y  ;
      m_pNeighbor[7].X = m_Coord.X+2;   m_pNeighbor[7].Y = m_Coord.Y+1;
      break;
   case 72: case 75: case 78:
      m_pNeighbor[0].X = m_Coord.X-2;   m_pNeighbor[0].Y = m_Coord.Y-1;
      m_pNeighbor[1].X = m_Coord.X-1;   m_pNeighbor[1].Y = m_Coord.Y-1;
      m_pNeighbor[2].X = m_Coord.X  ;   m_pNeighbor[2].Y = m_Coord.Y-1;
      m_pNeighbor[3].X = m_Coord.X+1;   m_pNeighbor[3].Y = m_Coord.Y-1;
      m_pNeighbor[4].X = m_Coord.X-2;   m_pNeighbor[4].Y = m_Coord.Y  ;
      m_pNeighbor[5].X = m_Coord.X-1;   m_pNeighbor[5].Y = m_Coord.Y  ;
      m_pNeighbor[6].X = m_Coord.X+1;   m_pNeighbor[6].Y = m_Coord.Y  ;
      m_pNeighbor[7].X = m_Coord.X+2;   m_pNeighbor[7].Y = m_Coord.Y+1;
      break;
   case 81:
      m_pNeighbor[0].X = m_Coord.X-2;   m_pNeighbor[0].Y = m_Coord.Y-1;
      m_pNeighbor[1].X = m_Coord.X-1;   m_pNeighbor[1].Y = m_Coord.Y-1;
      m_pNeighbor[2].X = m_Coord.X-2;   m_pNeighbor[2].Y = m_Coord.Y  ;
      m_pNeighbor[3].X = m_Coord.X-1;   m_pNeighbor[3].Y = m_Coord.Y  ;
      m_pNeighbor[4].X = m_Coord.X+1;   m_pNeighbor[4].Y = m_Coord.Y  ;
      m_pNeighbor[5].X = m_Coord.X+2;   m_pNeighbor[5].Y = m_Coord.Y+1;
      m_pNeighbor[6].X = m_Coord.X+3;   m_pNeighbor[6].Y = m_Coord.Y+1;
      m_pNeighbor[7].X = m_Coord.X+3;   m_pNeighbor[7].Y = m_Coord.Y+2;
      break;
   }
   VerifyNeighbor(sizeField);
}

void nsCell::CPentagonT5::SetPoint(int iArea) {
   if (m_Coord.X==0 && m_Coord.Y==0) {
      m_a = 2*sqrt(iArea/SQRT147);
      m_h = m_a*SQRT3/2;
      m_sq = (m_a*2*SQRT3-4*m_GContext.m_Border.m_iWidth)/(SQRT3+1);
   }

   // определение координат точек фигуры
   float oX = m_a*21*(m_Coord.X/14); // offset X
   float oY = m_h*14*(m_Coord.Y/6);  // offset Y
   switch (m_iDirection) {
   case  0: case  1: case  2: case 14: case 15: case 16: oX += m_a* 2.5f; oY += m_h* 3; break;
   case  3: case  4: case  5: case 17: case 18: case 19: oX += m_a* 7.0f; oY += m_h* 4; break;
   case  6: case  7: case  8: case 20: case 21: case 22: oX += m_a*11.5f; oY += m_h* 5; break;
   case  9: case 10: case 11: case 23: case 24: case 25: oX += m_a*16.0f; oY += m_h* 6; break;
   case 12: case 13: case 27: case 26: case 40: case 41: oX += m_a*20.5f; oY += m_h* 7; break;
   case 28: case 29: case 30: case 42: case 43: case 44: oX += m_a* 4.0f; oY += m_h* 8; break;
   case 31: case 32: case 33: case 45: case 46: case 47: oX += m_a* 8.5f; oY += m_h* 9; break;
   case 34: case 35: case 36: case 48: case 49: case 50: oX += m_a*13.0f; oY += m_h*10; break;
   case 37: case 38: case 39: case 51: case 52: case 53: oX += m_a*17.5f; oY += m_h*11; break;
   case 54: case 55: case 69: case 67: case 68: case 83: oX += m_a*22.0f; oY += m_h*12; break;
   case 56: case 57: case 58: case 70: case 71: case 72: oX += m_a* 5.5f; oY += m_h*13; break;
   case 59: case 60: case 61: case 73: case 74: case 75: oX += m_a*10.0f; oY += m_h*14; break;
   case 62: case 63: case 64: case 76: case 77: case 78: oX += m_a*14.5f; oY += m_h*15; break;
   case 65: case 66: case 82: case 79: case 80: case 81: oX += m_a*19.0f; oY += m_h*16; break;
   }
   switch (m_iDirection) {
   case  0: case  3: case  6: case  9: case 12: case 28: case 31: case 34: case 37: case 54: case 56: case 59: case 62: case 65:
      m_pRegion[0].x = oX - m_a;       m_pRegion[0].y = oY - m_h*2;
      m_pRegion[1].x = oX;             m_pRegion[1].y = oY;
      m_pRegion[2].x = oX - m_a*2;     m_pRegion[2].y = oY;
      m_pRegion[3].x = oX - m_a*2.5f;  m_pRegion[3].y = oY - m_h;
      m_pRegion[4].x = oX - m_a*2;     m_pRegion[4].y = oY - m_h*2;
      break;
   case  1: case  4: case  7: case 10: case 13: case 29: case 32: case 35: case 38: case 55: case 57: case 60: case 63: case 66:
      m_pRegion[0].x = oX + m_a*0.5f;  m_pRegion[0].y = oY - m_h*3;
      m_pRegion[1].x = oX - m_a*0.5f;  m_pRegion[1].y = oY - m_h*3;
      m_pRegion[2].x = oX - m_a;       m_pRegion[2].y = oY - m_h*2;
      m_pRegion[3].x = oX;             m_pRegion[3].y = oY;
      m_pRegion[4].x = oX + m_a;       m_pRegion[4].y = oY - m_h*2;
      break;
   case  2: case  5: case  8: case 11: case 27: case 30: case 33: case 36: case 39: case 69: case 58: case 61: case 64: case 82:
      m_pRegion[0].x = oX + m_a*2;     m_pRegion[0].y = oY - m_h*2;
      m_pRegion[1].x = oX + m_a*2.5f;  m_pRegion[1].y = oY - m_h;
      m_pRegion[2].x = oX + m_a*2;     m_pRegion[2].y = oY;
      m_pRegion[3].x = oX;             m_pRegion[3].y = oY;
      m_pRegion[4].x = oX + m_a;       m_pRegion[4].y = oY - m_h*2;
      break;
   case 14: case 17: case 20: case 23: case 26: case 42: case 45: case 48: case 51: case 67: case 70: case 73: case 76: case 79:
      m_pRegion[0].x = oX;             m_pRegion[0].y = oY;
      m_pRegion[1].x = oX - m_a*2;     m_pRegion[1].y = oY;
      m_pRegion[2].x = oX - m_a*2.5f;  m_pRegion[2].y = oY + m_h;
      m_pRegion[3].x = oX - m_a*2;     m_pRegion[3].y = oY + m_h*2;
      m_pRegion[4].x = oX - m_a;       m_pRegion[4].y = oY + m_h*2;
      break;
   case 15: case 18: case 21: case 24: case 40: case 43: case 46: case 49: case 52: case 68: case 71: case 74: case 77: case 80:
      m_pRegion[0].x = oX;             m_pRegion[0].y = oY;
      m_pRegion[1].x = oX + m_a;       m_pRegion[1].y = oY + m_h*2;
      m_pRegion[2].x = oX + m_a*0.5f;  m_pRegion[2].y = oY + m_h*3;
      m_pRegion[3].x = oX - m_a*0.5f;  m_pRegion[3].y = oY + m_h*3;
      m_pRegion[4].x = oX - m_a;       m_pRegion[4].y = oY + m_h*2;
      break;
   case 16: case 19: case 22: case 25: case 41: case 44: case 47: case 50: case 53: case 83: case 72: case 75: case 78: case 81:
      m_pRegion[0].x = oX + m_a*2;     m_pRegion[0].y = oY;
      m_pRegion[1].x = oX;             m_pRegion[1].y = oY;
      m_pRegion[2].x = oX + m_a;       m_pRegion[2].y = oY + m_h*2;
      m_pRegion[3].x = oX + m_a*2;     m_pRegion[3].y = oY + m_h*2;
      m_pRegion[4].x = oX + m_a*2.5f;  m_pRegion[4].y = oY + m_h;
      break;
   }

   // определение координат вписанного в фигуру квадрата - область в которую выводится изображение/текст
   {
      POINTFLOAT center;
      switch (m_iDirection) {
      case  0: case  3: case  6: case  9: case 12: case 28: case 31:
      case 34: case 37: case 54: case 56: case 59: case 62: case 65: center.x = oX - m_a*1.5f;  center.y = oY - m_h;   break;
      case  1: case  4: case  7: case 10: case 13: case 29: case 32:
      case 35: case 38: case 55: case 57: case 60: case 63: case 66: center.x = oX;             center.y = oY - m_h*2; break;
      case  2: case  5: case  8: case 11: case 27: case 30: case 33:
      case 36: case 39: case 69: case 58: case 61: case 64: case 82: center.x = oX + m_a*1.5f;  center.y = oY - m_h;   break;
      case 14: case 17: case 20: case 23: case 26: case 42: case 45:
      case 48: case 51: case 67: case 70: case 73: case 76: case 79: center.x = oX - m_a*1.5f;  center.y = oY + m_h;   break;
      case 15: case 18: case 21: case 24: case 40: case 43: case 46:
      case 49: case 52: case 68: case 71: case 74: case 77: case 80: center.x = oX;             center.y = oY + m_h*2; break;
      case 16: case 19: case 22: case 25: case 41: case 44: case 47:
      case 50: case 53: case 83: case 72: case 75: case 78: case 81: center.x = oX + m_a*1.5f;  center.y = oY + m_h;   break;
      }
      m_Square.left   = center.x - m_sq/2;
      m_Square.top    = center.y - m_sq/2;
      m_Square.right  = m_Square.left + m_sq;
      m_Square.bottom = m_Square.top  + m_sq;
   }
}

void nsCell::CPentagonT5::Paint() const {
   CBase::Paint();

   switch (m_iDirection) {
   case  0: case  3: case  6: case  9: case 12: case 28: case 31: case 34: case 37: case 54: case 56: case 59: case 62: case 65:
   case  2: case  5: case  8: case 11: case 27: case 30: case 33: case 36: case 39: case 69: case 58: case 61: case 64: case 82:
   case 15: case 18: case 21: case 24: case 40: case 43: case 46: case 49: case 52: case 68: case 71: case 74: case 77: case 80:
      SelectObject(m_GContext.m_hDCTmp, m_bDown ? m_GContext.m_hPenLight  : m_GContext.m_hPenShadow); break;
   case  1: case  4: case  7: case 10: case 13: case 29: case 32: case 35: case 38: case 55: case 57: case 60: case 63: case 66:
   case 14: case 17: case 20: case 23: case 26: case 42: case 45: case 48: case 51: case 67: case 70: case 73: case 76: case 79:
   case 16: case 19: case 22: case 25: case 41: case 44: case 47: case 50: case 53: case 83: case 72: case 75: case 78: case 81:
      SelectObject(m_GContext.m_hDCTmp, m_bDown ? m_GContext.m_hPenShadow : m_GContext.m_hPenLight ); break;
   }
   MoveToEx(m_GContext.m_hDCTmp, m_pRegion[0].x, m_pRegion[0].y, NULL);
   LineTo  (m_GContext.m_hDCTmp, m_pRegion[1].x, m_pRegion[1].y);
   LineTo  (m_GContext.m_hDCTmp, m_pRegion[2].x, m_pRegion[2].y);
   switch (m_iDirection) {
   case  0: case  3: case  6: case  9: case 12: case 28: case 31: case 34: case 37: case 54: case 56: case 59: case 62: case 65:
      SelectObject(m_GContext.m_hDCTmp, m_bDown ? m_GContext.m_hPenShadow : m_GContext.m_hPenLight ); break;
   case 16: case 19: case 22: case 25: case 41: case 44: case 47: case 50: case 53: case 83: case 72: case 75: case 78: case 81:
      SelectObject(m_GContext.m_hDCTmp, m_bDown ? m_GContext.m_hPenLight  : m_GContext.m_hPenShadow); break;
   }
   LineTo  (m_GContext.m_hDCTmp, m_pRegion[3].x, m_pRegion[3].y);
   switch (m_iDirection) {
   case  2: case  5: case  8: case 11: case 27: case 30: case 33: case 36: case 39: case 69: case 58: case 61: case 64: case 82:
   case 15: case 18: case 21: case 24: case 40: case 43: case 46: case 49: case 52: case 68: case 71: case 74: case 77: case 80:
      SelectObject(m_GContext.m_hDCTmp, m_bDown ? m_GContext.m_hPenShadow : m_GContext.m_hPenLight ); break;
   case  1: case  4: case  7: case 10: case 13: case 29: case 32: case 35: case 38: case 55: case 57: case 60: case 63: case 66:
   case 14: case 17: case 20: case 23: case 26: case 42: case 45: case 48: case 51: case 67: case 70: case 73: case 76: case 79:
      SelectObject(m_GContext.m_hDCTmp, m_bDown ? m_GContext.m_hPenLight  : m_GContext.m_hPenShadow); break;
   }
   LineTo  (m_GContext.m_hDCTmp, m_pRegion[4].x, m_pRegion[4].y);
   LineTo  (m_GContext.m_hDCTmp, m_pRegion[0].x, m_pRegion[0].y);

   RegionDraw(
      m_GContext.m_hDCDst, true,
      m_pRegion, 5, NULL, NULL, 0, 0, 0, 0,
      m_GContext.m_hDCTmp, NULL, 0,0);
}
