////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "SqTrHex.cpp"
//
// Реализация класса CSqTrHex - 6Square 4Triangle 2Hexagon
////////////////////////////////////////////////////////////////////////////////

#include "StdAfx.h"
#include "SqTrHex.h"
#include "../CommonLib.h"

////////////////////////////////////////////////////////////////////////////////
//                               local init
////////////////////////////////////////////////////////////////////////////////

float nsCell::CSqTrHex::m_h; // высота треугольника

////////////////////////////////////////////////////////////////////////////////
//                               static function
////////////////////////////////////////////////////////////////////////////////

SIZE nsCell::CSqTrHex::GetSizeInPixel(const COORD &sizeField, int area) {
   m_a = sqrt(area/(0.5f+1/SQRT3));
   m_h = m_a*SQRT3/2;

   SIZE result = {m_a/2+m_h + m_a/2*((sizeField.X+2)/3) +
                                m_h*((sizeField.X+1)/3) +
                        (m_a/2+m_h)*((sizeField.X+0)/3),
                  m_a/2       + m_h*((sizeField.Y+1)/2)+
                            m_a*3/2*((sizeField.Y+0)/2)};
   return result;
}

int nsCell::CSqTrHex::SizeInscribedSquare(int area, int borderWidth) {
   m_a = sqrt(area/(0.5f+1/SQRT3));
   m_sq = (m_a*SQRT3-borderWidth*6)/(SQRT3+2);
   return m_sq;
}

////////////////////////////////////////////////////////////////////////////////
//                               virtual function
////////////////////////////////////////////////////////////////////////////////

nsCell::CSqTrHex::CSqTrHex(const COORD &Coord, const COORD &sizeField, int area, const CGraphicContext &gContext)
   : CBase(Coord, sizeField, area, gContext,
           12, 6,
           (Coord.Y&3)*3+(Coord.X%3) // 0..11
          )
{
   SetPoint(area);
   // определяю координаты соседей
   switch (m_iDirection) {
   case  0:
      m_pNeighbor[ 0].X = m_Coord.X-1;   m_pNeighbor[ 0].Y = m_Coord.Y-1;
      m_pNeighbor[ 1].X = m_Coord.X+1;   m_pNeighbor[ 1].Y = m_Coord.Y  ;
      m_pNeighbor[ 2].X = m_Coord.X-2;   m_pNeighbor[ 2].Y = m_Coord.Y+1;
      m_pNeighbor[ 3].X = m_Coord.X-1;   m_pNeighbor[ 3].Y = m_Coord.Y+1;
      m_pNeighbor[ 4].X = m_Coord.X  ;   m_pNeighbor[ 4].Y = m_Coord.Y+1;
      m_pNeighbor[ 5].X = m_Coord.X+1;   m_pNeighbor[ 5].Y = m_Coord.Y+1;
      m_pNeighbor[ 6] =
      m_pNeighbor[ 7] =
      m_pNeighbor[ 8] =
      m_pNeighbor[ 9] =
      m_pNeighbor[10] =
      m_pNeighbor[11] = INCORRECT_COORD;
      break;
   case  1:
      m_pNeighbor[ 0].X = m_Coord.X-2;   m_pNeighbor[ 0].Y = m_Coord.Y-1;
      m_pNeighbor[ 1].X = m_Coord.X  ;   m_pNeighbor[ 1].Y = m_Coord.Y-1;
      m_pNeighbor[ 2].X = m_Coord.X-1;   m_pNeighbor[ 2].Y = m_Coord.Y  ;
      m_pNeighbor[ 3].X = m_Coord.X+1;   m_pNeighbor[ 3].Y = m_Coord.Y  ;
      m_pNeighbor[ 4].X = m_Coord.X-2;   m_pNeighbor[ 4].Y = m_Coord.Y+1;
      m_pNeighbor[ 5].X = m_Coord.X-1;   m_pNeighbor[ 5].Y = m_Coord.Y+1;
      m_pNeighbor[ 6].X = m_Coord.X  ;   m_pNeighbor[ 6].Y = m_Coord.Y+1;
      m_pNeighbor[ 7].X = m_Coord.X+1;   m_pNeighbor[ 7].Y = m_Coord.Y+1;
      m_pNeighbor[ 8] =
      m_pNeighbor[ 9] =
      m_pNeighbor[10] =
      m_pNeighbor[11] = INCORRECT_COORD;
      break;
   case  2:
      m_pNeighbor[ 0].X = m_Coord.X-3;   m_pNeighbor[ 0].Y = m_Coord.Y-1;
      m_pNeighbor[ 1].X = m_Coord.X-1;   m_pNeighbor[ 1].Y = m_Coord.Y-1;
      m_pNeighbor[ 2].X = m_Coord.X  ;   m_pNeighbor[ 2].Y = m_Coord.Y-1;
      m_pNeighbor[ 3].X = m_Coord.X-1;   m_pNeighbor[ 3].Y = m_Coord.Y  ;
      m_pNeighbor[ 4].X = m_Coord.X-1;   m_pNeighbor[ 4].Y = m_Coord.Y+1;
      m_pNeighbor[ 5].X = m_Coord.X  ;   m_pNeighbor[ 5].Y = m_Coord.Y+1;
      m_pNeighbor[ 6] =
      m_pNeighbor[ 7] =
      m_pNeighbor[ 8] =
      m_pNeighbor[ 9] =
      m_pNeighbor[10] =
      m_pNeighbor[11] = INCORRECT_COORD;
      break;
   case  3:
      m_pNeighbor[ 0].X = m_Coord.X  ;   m_pNeighbor[ 0].Y = m_Coord.Y-1;
      m_pNeighbor[ 1].X = m_Coord.X+1;   m_pNeighbor[ 1].Y = m_Coord.Y-1;
      m_pNeighbor[ 2].X = m_Coord.X-2;   m_pNeighbor[ 2].Y = m_Coord.Y  ;
      m_pNeighbor[ 3].X = m_Coord.X-1;   m_pNeighbor[ 3].Y = m_Coord.Y  ;
      m_pNeighbor[ 4].X = m_Coord.X+1;   m_pNeighbor[ 4].Y = m_Coord.Y  ;
      m_pNeighbor[ 5].X = m_Coord.X-1;   m_pNeighbor[ 5].Y = m_Coord.Y+1;
      m_pNeighbor[ 6].X = m_Coord.X  ;   m_pNeighbor[ 6].Y = m_Coord.Y+1;
      m_pNeighbor[ 7].X = m_Coord.X  ;   m_pNeighbor[ 7].Y = m_Coord.Y+2;
      m_pNeighbor[ 8] =
      m_pNeighbor[ 9] =
      m_pNeighbor[10] =
      m_pNeighbor[11] = INCORRECT_COORD;
      break;
   case  4:
      m_pNeighbor[ 0].X = m_Coord.X-1;   m_pNeighbor[ 0].Y = m_Coord.Y-1;
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
      m_pNeighbor[11].X = m_Coord.X-1;   m_pNeighbor[11].Y = m_Coord.Y+2;
      break;
   case  5:
      m_pNeighbor[ 0].X = m_Coord.X-1;   m_pNeighbor[ 0].Y = m_Coord.Y-2;
      m_pNeighbor[ 1].X = m_Coord.X  ;   m_pNeighbor[ 1].Y = m_Coord.Y-2;
      m_pNeighbor[ 2].X = m_Coord.X-1;   m_pNeighbor[ 2].Y = m_Coord.Y-1;
      m_pNeighbor[ 3].X = m_Coord.X  ;   m_pNeighbor[ 3].Y = m_Coord.Y-1;
      m_pNeighbor[ 4].X = m_Coord.X+1;   m_pNeighbor[ 4].Y = m_Coord.Y-1;
      m_pNeighbor[ 5].X = m_Coord.X+2;   m_pNeighbor[ 5].Y = m_Coord.Y-1;
      m_pNeighbor[ 6].X = m_Coord.X-1;   m_pNeighbor[ 6].Y = m_Coord.Y  ;
      m_pNeighbor[ 7].X = m_Coord.X+1;   m_pNeighbor[ 7].Y = m_Coord.Y  ;
      m_pNeighbor[ 8] =
      m_pNeighbor[ 9] =
      m_pNeighbor[10] =
      m_pNeighbor[11] = INCORRECT_COORD;
      break;
   case  6:
      m_pNeighbor[ 0].X = m_Coord.X-2;   m_pNeighbor[ 0].Y = m_Coord.Y-1;
      m_pNeighbor[ 1].X = m_Coord.X  ;   m_pNeighbor[ 1].Y = m_Coord.Y-1;
      m_pNeighbor[ 2].X = m_Coord.X+1;   m_pNeighbor[ 2].Y = m_Coord.Y-1;
      m_pNeighbor[ 3].X = m_Coord.X-1;   m_pNeighbor[ 3].Y = m_Coord.Y  ;
      m_pNeighbor[ 4].X = m_Coord.X-1;   m_pNeighbor[ 4].Y = m_Coord.Y+1;
      m_pNeighbor[ 5].X = m_Coord.X  ;   m_pNeighbor[ 5].Y = m_Coord.Y+1;
      m_pNeighbor[ 6] =
      m_pNeighbor[ 7] =
      m_pNeighbor[ 8] =
      m_pNeighbor[ 9] =
      m_pNeighbor[10] =
      m_pNeighbor[11] = INCORRECT_COORD;
      break;
   case  7:
      m_pNeighbor[ 0].X = m_Coord.X  ;   m_pNeighbor[ 0].Y = m_Coord.Y-1;
      m_pNeighbor[ 1].X = m_Coord.X+1;   m_pNeighbor[ 1].Y = m_Coord.Y  ;
      m_pNeighbor[ 2].X = m_Coord.X-2;   m_pNeighbor[ 2].Y = m_Coord.Y+1;
      m_pNeighbor[ 3].X = m_Coord.X-1;   m_pNeighbor[ 3].Y = m_Coord.Y+1;
      m_pNeighbor[ 4].X = m_Coord.X  ;   m_pNeighbor[ 4].Y = m_Coord.Y+1;
      m_pNeighbor[ 5].X = m_Coord.X+1;   m_pNeighbor[ 5].Y = m_Coord.Y+1;
      m_pNeighbor[ 6] =
      m_pNeighbor[ 7] =
      m_pNeighbor[ 8] =
      m_pNeighbor[ 9] =
      m_pNeighbor[10] =
      m_pNeighbor[11] = INCORRECT_COORD;
      break;
   case  8:
      m_pNeighbor[ 0].X = m_Coord.X-1;   m_pNeighbor[ 0].Y = m_Coord.Y-1;
      m_pNeighbor[ 1].X = m_Coord.X+1;   m_pNeighbor[ 1].Y = m_Coord.Y-1;
      m_pNeighbor[ 2].X = m_Coord.X-1;   m_pNeighbor[ 2].Y = m_Coord.Y  ;
      m_pNeighbor[ 3].X = m_Coord.X+1;   m_pNeighbor[ 3].Y = m_Coord.Y  ;
      m_pNeighbor[ 4].X = m_Coord.X-2;   m_pNeighbor[ 4].Y = m_Coord.Y+1;
      m_pNeighbor[ 5].X = m_Coord.X-1;   m_pNeighbor[ 5].Y = m_Coord.Y+1;
      m_pNeighbor[ 6].X = m_Coord.X  ;   m_pNeighbor[ 6].Y = m_Coord.Y+1;
      m_pNeighbor[ 7].X = m_Coord.X+1;   m_pNeighbor[ 7].Y = m_Coord.Y+1;
      m_pNeighbor[ 8] =
      m_pNeighbor[ 9] =
      m_pNeighbor[10] =
      m_pNeighbor[11] = INCORRECT_COORD;
      break;
   case  9:
      m_pNeighbor[ 0].X = m_Coord.X  ;   m_pNeighbor[ 0].Y = m_Coord.Y-2;
      m_pNeighbor[ 1].X = m_Coord.X+1;   m_pNeighbor[ 1].Y = m_Coord.Y-2;
      m_pNeighbor[ 2].X = m_Coord.X-1;   m_pNeighbor[ 2].Y = m_Coord.Y-1;
      m_pNeighbor[ 3].X = m_Coord.X  ;   m_pNeighbor[ 3].Y = m_Coord.Y-1;
      m_pNeighbor[ 4].X = m_Coord.X+1;   m_pNeighbor[ 4].Y = m_Coord.Y-1;
      m_pNeighbor[ 5].X = m_Coord.X+2;   m_pNeighbor[ 5].Y = m_Coord.Y-1;
      m_pNeighbor[ 6].X = m_Coord.X-1;   m_pNeighbor[ 6].Y = m_Coord.Y  ;
      m_pNeighbor[ 7].X = m_Coord.X+1;   m_pNeighbor[ 7].Y = m_Coord.Y  ;
      m_pNeighbor[ 8] =
      m_pNeighbor[ 9] =
      m_pNeighbor[10] =
      m_pNeighbor[11] = INCORRECT_COORD;
      break;
   case 10:
      m_pNeighbor[ 0].X = m_Coord.X  ;   m_pNeighbor[ 0].Y = m_Coord.Y-1;
      m_pNeighbor[ 1].X = m_Coord.X+1;   m_pNeighbor[ 1].Y = m_Coord.Y-1;
      m_pNeighbor[ 2].X = m_Coord.X-2;   m_pNeighbor[ 2].Y = m_Coord.Y  ;
      m_pNeighbor[ 3].X = m_Coord.X-1;   m_pNeighbor[ 3].Y = m_Coord.Y  ;
      m_pNeighbor[ 4].X = m_Coord.X+1;   m_pNeighbor[ 4].Y = m_Coord.Y  ;
      m_pNeighbor[ 5].X = m_Coord.X  ;   m_pNeighbor[ 5].Y = m_Coord.Y+1;
      m_pNeighbor[ 6].X = m_Coord.X+1;   m_pNeighbor[ 6].Y = m_Coord.Y+1;
      m_pNeighbor[ 7].X = m_Coord.X+1;   m_pNeighbor[ 7].Y = m_Coord.Y+2;
      m_pNeighbor[ 8] =
      m_pNeighbor[ 9] =
      m_pNeighbor[10] =
      m_pNeighbor[11] = INCORRECT_COORD;
      break;
   case 11:
      m_pNeighbor[ 0].X = m_Coord.X-1;   m_pNeighbor[ 0].Y = m_Coord.Y-1;
      m_pNeighbor[ 1].X = m_Coord.X  ;   m_pNeighbor[ 1].Y = m_Coord.Y-1;
      m_pNeighbor[ 2].X = m_Coord.X+1;   m_pNeighbor[ 2].Y = m_Coord.Y-1;
      m_pNeighbor[ 3].X = m_Coord.X+2;   m_pNeighbor[ 3].Y = m_Coord.Y-1;
      m_pNeighbor[ 4].X = m_Coord.X-1;   m_pNeighbor[ 4].Y = m_Coord.Y  ;
      m_pNeighbor[ 5].X = m_Coord.X+1;   m_pNeighbor[ 5].Y = m_Coord.Y  ;
      m_pNeighbor[ 6].X = m_Coord.X+2;   m_pNeighbor[ 6].Y = m_Coord.Y  ;
      m_pNeighbor[ 7].X = m_Coord.X  ;   m_pNeighbor[ 7].Y = m_Coord.Y+1;
      m_pNeighbor[ 8].X = m_Coord.X+1;   m_pNeighbor[ 8].Y = m_Coord.Y+1;
      m_pNeighbor[ 9].X = m_Coord.X+2;   m_pNeighbor[ 9].Y = m_Coord.Y+1;
      m_pNeighbor[10].X = m_Coord.X+3;   m_pNeighbor[10].Y = m_Coord.Y+1;
      m_pNeighbor[11].X = m_Coord.X  ;   m_pNeighbor[11].Y = m_Coord.Y+2;
      break;
   }
   VerifyNeighbor(sizeField);
}

bool nsCell::CSqTrHex::PointInRegion(const POINT &point) const { // принадлежат ли эти экранные координаты ячейке
   switch (m_iDirection) {
   case  0:
   case  2:
   case  6:
   case  7: return PointInPolygon(point, m_pRegion, 3);
   case  1:
   case  3:
   case  5:
   case  8:
   case  9:
   case 10: return PointInPolygon(point, m_pRegion, 4);
   case  4:
   case 11: return PointInPolygon(point, m_pRegion, 6);
   }
   return false;
}

void nsCell::CSqTrHex::SetPoint(int area) {
   if (m_Coord.X==0 && m_Coord.Y==0) {
      m_a = sqrt(area/(0.5f+1/SQRT3));
      m_h = m_a*SQRT3/2;
      m_sq = (m_a*SQRT3-m_GContext.m_Border.m_iWidth*6)/(SQRT3+2);
   }

   // определение координат точек фигуры
   float b = m_a/2;
   float oX = (m_h*2+m_a  )*(m_Coord.X/3) + m_a+m_h;   // offset X
   float oY = (m_h*2+m_a*3)*(m_Coord.Y/4) + m_a*2+m_h; // offset Y
   switch (m_iDirection) {
   case 0:
      m_pRegion[0].x = oX - b-m_h;      m_pRegion[0].y = oY - m_a-b-m_h;
      m_pRegion[1].x = oX - m_h;        m_pRegion[1].y = oY - m_a-b;
      m_pRegion[2].x = oX - m_h-m_a;    m_pRegion[2].y = oY - m_a-b;
      break;
   case 1:
      m_pRegion[0].x = oX - b;          m_pRegion[0].y = oY - m_a-m_a-m_h;
      m_pRegion[1].x = oX - b-m_h;      m_pRegion[1].y = oY - m_a-b-m_h;
      m_pRegion[2].x = oX - m_h;        m_pRegion[2].y = oY - m_a-b;
      m_pRegion[3].x = oX;              m_pRegion[3].y = oY - m_a-m_a;
      break;
   case 2:
      m_pRegion[0].x = oX + b;          m_pRegion[0].y = oY - m_a-m_a-m_h;
      m_pRegion[1].x = oX - b;          m_pRegion[1].y = oY - m_a-m_a-m_h;
      m_pRegion[2].x = oX;              m_pRegion[2].y = oY - m_a-m_a;
      break;
   case 3:
      m_pRegion[0].x = oX - m_h;        m_pRegion[0].y = oY - m_a-b;
      m_pRegion[1].x = oX - m_a-m_h;    m_pRegion[1].y = oY - m_a-b;
      m_pRegion[2].x = oX - m_a-m_h;    m_pRegion[2].y = oY - b;
      m_pRegion[3].x = oX - m_h;        m_pRegion[3].y = oY - b;
      break;
   case 4:
      m_pRegion[0].x = oX;              m_pRegion[0].y = oY - m_a-m_a;
      m_pRegion[1].x = oX - m_h;        m_pRegion[1].y = oY - m_a-b;
      m_pRegion[2].x = oX - m_h;        m_pRegion[2].y = oY - b;
      m_pRegion[3].x = oX;              m_pRegion[3].y = oY;
      m_pRegion[4].x = oX + m_h;        m_pRegion[4].y = oY - b;
      m_pRegion[5].x = oX + m_h;        m_pRegion[5].y = oY - m_a-b;
      break;
   case 5:
      m_pRegion[0].x = oX + b;          m_pRegion[0].y = oY - m_a-m_a-m_h;
      m_pRegion[1].x = oX;              m_pRegion[1].y = oY - m_a-m_a;
      m_pRegion[2].x = oX + m_h;        m_pRegion[2].y = oY - m_a-b;
      m_pRegion[3].x = oX + b+m_h;      m_pRegion[3].y = oY - m_a-b-m_h;
      break;
   case 6:
      m_pRegion[0].x = oX - m_h;        m_pRegion[0].y = oY - b;
      m_pRegion[1].x = oX - m_a-m_h;    m_pRegion[1].y = oY - b;
      m_pRegion[2].x = oX - b-m_h;      m_pRegion[2].y = oY - b+m_h;
      break;
   case 7:
      m_pRegion[0].x = oX;              m_pRegion[0].y = oY;
      m_pRegion[1].x = oX + b;          m_pRegion[1].y = oY + m_h;
      m_pRegion[2].x = oX - b;          m_pRegion[2].y = oY + m_h;
      break;
   case 8:
      m_pRegion[0].x = oX + m_h;        m_pRegion[0].y = oY - b;
      m_pRegion[1].x = oX;              m_pRegion[1].y = oY;
      m_pRegion[2].x = oX + b;          m_pRegion[2].y = oY + m_h;
      m_pRegion[3].x = oX + m_h+b;      m_pRegion[3].y = oY - b+m_h;
      break;
   case 9:
      m_pRegion[0].x = oX - m_h;        m_pRegion[0].y = oY - b;
      m_pRegion[1].x = oX - b-m_h;      m_pRegion[1].y = oY - b+m_h;
      m_pRegion[2].x = oX - b;          m_pRegion[2].y = oY + m_h;
      m_pRegion[3].x = oX;              m_pRegion[3].y = oY;
      break;
   case 10:
      m_pRegion[0].x = oX + b;          m_pRegion[0].y = oY + m_h;
      m_pRegion[1].x = oX - b;          m_pRegion[1].y = oY + m_h;
      m_pRegion[2].x = oX - b;          m_pRegion[2].y = oY + m_a+m_h;
      m_pRegion[3].x = oX + b;          m_pRegion[3].y = oY + m_a+m_h;
      break;
   case 11:
      m_pRegion[0].x = oX + b+m_h;      m_pRegion[0].y = oY + m_h-b;
      m_pRegion[1].x = oX + b;          m_pRegion[1].y = oY + m_h;
      m_pRegion[2].x = oX + b;          m_pRegion[2].y = oY + m_a+m_h;
      m_pRegion[3].x = oX + b+m_h;      m_pRegion[3].y = oY + m_a+b+m_h;
      m_pRegion[4].x = oX + b+m_h+m_h;  m_pRegion[4].y = oY + m_a+m_h;
      m_pRegion[5].x = oX + b+m_h+m_h;  m_pRegion[5].y = oY + m_h;
      break;
   }

   // определение координат вписанного в фигуру квадрата - область в которую выводится изображение/текст
   {
      POINTFLOAT center; // координата вписанного в фигуру квадрата
      switch (m_iDirection) {
      case  0:  center.x = oX -  b-m_h;      center.y = oY - m_a-b-m_GContext.m_Border.m_iWidth-m_sq/2;        break;
      case  1:  center.x = oX - (b+m_h)/2;   center.y = oY - m_a-b-(b+m_h)/2;                                  break;
      case  2:  center.x = oX;               center.y = oY - m_a-m_a-m_h+m_GContext.m_Border.m_iWidth+m_sq/2;  break;
      case  3:  center.x = oX -  b-m_h;      center.y = oY - m_a;                                              break;
      case  4:  center.x = oX;               center.y = oY - m_a;                                              break;
      case  5:  center.x = oX + (b+m_h)/2;   center.y = oY - m_a-b-(b+m_h)/2;                                  break;
      case  6:  center.x = oX -  b-m_h;      center.y = oY - b  +m_GContext.m_Border.m_iWidth+m_sq/2;          break;
      case  7:  center.x = oX;               center.y = oY + m_h-m_GContext.m_Border.m_iWidth-m_sq/2;          break;
      case  8:  center.x = oX + (b+m_h)/2;   center.y = oY - b+(b+m_h)/2;                                      break;
      case  9:  center.x = oX - (b+m_h)/2;   center.y = oY - b+(b+m_h)/2;                                      break;
      case 10:  center.x = oX;               center.y = oY + b+m_h;                                            break;
      case 11:  center.x = oX +  b+m_h;      center.y = oY + b+m_h;                                            break;
      }
      m_Square.left   = center.x - m_sq/2;
      m_Square.top    = center.y - m_sq/2;
      m_Square.right  = m_Square.left + m_sq;
      m_Square.bottom = m_Square.top  + m_sq;
   }
}

void nsCell::CSqTrHex::Paint() const {
   CBase::Paint();

   switch (m_iDirection) {
   case 0: case  7:
      SelectObject(m_GContext.m_hDCTmp, m_bDown ? m_GContext.m_hPenLight  : m_GContext.m_hPenShadow);
      MoveToEx(m_GContext.m_hDCTmp, m_pRegion[0].x, m_pRegion[0].y, NULL);
      LineTo  (m_GContext.m_hDCTmp, m_pRegion[1].x, m_pRegion[1].y);
      break;
   case 2: case  6:
      SelectObject(m_GContext.m_hDCTmp, m_bDown ? m_GContext.m_hPenShadow : m_GContext.m_hPenLight );
      MoveToEx(m_GContext.m_hDCTmp, m_pRegion[0].x, m_pRegion[0].y, NULL);
      LineTo  (m_GContext.m_hDCTmp, m_pRegion[1].x, m_pRegion[1].y);
      break;
   case 1: case  8:
   case 3: case 10:
   case 4: case 11:
   case 5: case  9:
      SelectObject(m_GContext.m_hDCTmp, m_bDown ? m_GContext.m_hPenShadow : m_GContext.m_hPenLight );
      MoveToEx(m_GContext.m_hDCTmp, m_pRegion[1].x, m_pRegion[1].y, NULL);
      break;
   }
   LineTo  (m_GContext.m_hDCTmp, m_pRegion[2].x, m_pRegion[2].y);
   switch (m_iDirection) {
   case 4: case 11:
      LineTo  (m_GContext.m_hDCTmp, m_pRegion[3].x, m_pRegion[3].y);  break;
   }
   switch (m_iDirection) {
   case 0: case  7:
      SelectObject(m_GContext.m_hDCTmp, m_bDown ? m_GContext.m_hPenShadow : m_GContext.m_hPenLight ); break;
   case 1: case  8:
   case 3: case 10:
   case 4: case 11:
   case 5: case  9:
   case 2: case  6:
      SelectObject(m_GContext.m_hDCTmp, m_bDown ? m_GContext.m_hPenLight  : m_GContext.m_hPenShadow); break;
   }
   switch (m_iDirection) {
   case 0: case  7:
   case 2: case  6:
      LineTo  (m_GContext.m_hDCTmp, m_pRegion[0].x, m_pRegion[0].y);
      RegionDraw(
         m_GContext.m_hDCDst, true,
         m_pRegion, 3, NULL, NULL, 0, 0, 0, 0,
         m_GContext.m_hDCTmp, NULL, 0,0);
      break;
   case 1: case  8:
   case 3: case 10:
   case 5: case  9:
      LineTo  (m_GContext.m_hDCTmp, m_pRegion[3].x, m_pRegion[3].y);
      LineTo  (m_GContext.m_hDCTmp, m_pRegion[0].x, m_pRegion[0].y);
      SelectObject(m_GContext.m_hDCTmp, m_bDown ? m_GContext.m_hPenShadow : m_GContext.m_hPenLight );
      LineTo  (m_GContext.m_hDCTmp, m_pRegion[1].x, m_pRegion[1].y);
      RegionDraw(
         m_GContext.m_hDCDst, true,
         m_pRegion, 4, NULL, NULL, 0, 0, 0, 0,
         m_GContext.m_hDCTmp, NULL, 0,0);
      break;
   case 4: case 11:
      LineTo  (m_GContext.m_hDCTmp, m_pRegion[4].x, m_pRegion[4].y);
      LineTo  (m_GContext.m_hDCTmp, m_pRegion[5].x, m_pRegion[5].y);
      LineTo  (m_GContext.m_hDCTmp, m_pRegion[0].x, m_pRegion[0].y);
      SelectObject(m_GContext.m_hDCTmp, m_bDown ? m_GContext.m_hPenShadow : m_GContext.m_hPenLight );
      LineTo  (m_GContext.m_hDCTmp, m_pRegion[1].x, m_pRegion[1].y);
      RegionDraw(
         m_GContext.m_hDCDst, true,
         m_pRegion, 6, NULL, NULL, 0, 0, 0, 0,
         m_GContext.m_hDCTmp, NULL, 0,0);
      break;
   }
}
