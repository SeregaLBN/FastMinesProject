////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "TrSq1.cpp"
//
// Реализация класса CTrSq1 - мозаика из 4х треугольников и 2х квадратов
////////////////////////////////////////////////////////////////////////////////

#include "StdAfx.h"
#include "TrSq1.h"
#include "../CommonLib.h"

////////////////////////////////////////////////////////////////////////////////
//                               local init
////////////////////////////////////////////////////////////////////////////////

float nsCell::CTrSq1::m_n;
float nsCell::CTrSq1::m_m;
float nsCell::CTrSq1::m_b;
float nsCell::CTrSq1::m_k;

////////////////////////////////////////////////////////////////////////////////
//                               static function
////////////////////////////////////////////////////////////////////////////////

SIZE nsCell::CTrSq1::GetSizeInPixel(const COORD &sizeField, int area) {
   m_a = sqrt(3*area/(1+SQRT3/2)); // размер стороны треугольника и квадрата
   m_n = m_a*SIN75;
   m_m = m_a*SIN15;
   m_b = m_n+m_m;
   m_k = m_n-m_m;

   SIZE result = {m_b+m_n*((sizeField.X-1+2)/3)+
                      m_k*((sizeField.X-1+1)/3)+
                      m_m*((sizeField.X-1+0)/3),
                  m_b+m_n* (sizeField.Y-1)};
   return result;
}

int nsCell::CTrSq1::SizeInscribedSquare(int area, int borderWidth) {
   m_a = sqrt(3*area/(1+SQRT3/2)); // размер стороны треугольника и квадрата
   m_sq = (m_a*SQRT3-borderWidth*6)/(4*SIN75);
   return m_sq;
}

////////////////////////////////////////////////////////////////////////////////
//                               virtual function
////////////////////////////////////////////////////////////////////////////////

nsCell::CTrSq1::CTrSq1(const COORD &Coord, const COORD &sizeField, int area, const CGraphicContext &gContext)
   : CBase(Coord, sizeField, area, gContext,
           12, 4,
           (Coord.Y&1)*3+(Coord.X%3) // 0..5
          )
{
   SetPoint(area);
   // определяю координаты соседей
   switch (m_iDirection) {
   case 0:  m_pNeighbor[ 0].X = m_Coord.X-2;   m_pNeighbor[ 0].Y = m_Coord.Y-1;
            m_pNeighbor[ 1].X = m_Coord.X-1;   m_pNeighbor[ 1].Y = m_Coord.Y-1;
            m_pNeighbor[ 2].X = m_Coord.X  ;   m_pNeighbor[ 2].Y = m_Coord.Y-1;
            m_pNeighbor[ 3].X = m_Coord.X+1;   m_pNeighbor[ 3].Y = m_Coord.Y-1;
            m_pNeighbor[ 4].X = m_Coord.X-2;   m_pNeighbor[ 4].Y = m_Coord.Y  ;
            m_pNeighbor[ 5].X = m_Coord.X-1;   m_pNeighbor[ 5].Y = m_Coord.Y  ;
            m_pNeighbor[ 6].X = m_Coord.X+1;   m_pNeighbor[ 6].Y = m_Coord.Y  ;
            m_pNeighbor[ 7].X = m_Coord.X+2;   m_pNeighbor[ 7].Y = m_Coord.Y  ;
            m_pNeighbor[ 8].X = m_Coord.X-2;   m_pNeighbor[ 8].Y = m_Coord.Y+1;
            m_pNeighbor[ 9].X = m_Coord.X-1;   m_pNeighbor[ 9].Y = m_Coord.Y+1;
            m_pNeighbor[10].X = m_Coord.X  ;   m_pNeighbor[10].Y = m_Coord.Y+1;
            m_pNeighbor[11].X = m_Coord.X+1;   m_pNeighbor[11].Y = m_Coord.Y+1;
            break;
   case 1:  m_pNeighbor[ 0].X = m_Coord.X-2;   m_pNeighbor[ 0].Y = m_Coord.Y-1;
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
   case 2:  m_pNeighbor[ 0].X = m_Coord.X-1;   m_pNeighbor[ 0].Y = m_Coord.Y-1;
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
   case 3:  m_pNeighbor[ 0].X = m_Coord.X-1;   m_pNeighbor[ 0].Y = m_Coord.Y-1;
            m_pNeighbor[ 1].X = m_Coord.X  ;   m_pNeighbor[ 1].Y = m_Coord.Y-1;
            m_pNeighbor[ 2].X = m_Coord.X+1;   m_pNeighbor[ 2].Y = m_Coord.Y-1;
            m_pNeighbor[ 3].X = m_Coord.X+2;   m_pNeighbor[ 3].Y = m_Coord.Y-1;
            m_pNeighbor[ 4].X = m_Coord.X-2;   m_pNeighbor[ 4].Y = m_Coord.Y  ;
            m_pNeighbor[ 5].X = m_Coord.X-1;   m_pNeighbor[ 5].Y = m_Coord.Y  ;
            m_pNeighbor[ 6].X = m_Coord.X+1;   m_pNeighbor[ 6].Y = m_Coord.Y  ;
            m_pNeighbor[ 7].X = m_Coord.X  ;   m_pNeighbor[ 7].Y = m_Coord.Y+1;
            m_pNeighbor[ 8].X = m_Coord.X+1;   m_pNeighbor[ 8].Y = m_Coord.Y+1;
            m_pNeighbor[ 9] =
            m_pNeighbor[10] =
            m_pNeighbor[11] = INCORRECT_COORD;
            break;
   case 4:  m_pNeighbor[ 0].X = m_Coord.X-1;   m_pNeighbor[ 0].Y = m_Coord.Y-1;
            m_pNeighbor[ 1].X = m_Coord.X  ;   m_pNeighbor[ 1].Y = m_Coord.Y-1;
            m_pNeighbor[ 2].X = m_Coord.X+1;   m_pNeighbor[ 2].Y = m_Coord.Y-1;
            m_pNeighbor[ 3].X = m_Coord.X+2;   m_pNeighbor[ 3].Y = m_Coord.Y-1;
            m_pNeighbor[ 4].X = m_Coord.X-2;   m_pNeighbor[ 4].Y = m_Coord.Y  ;
            m_pNeighbor[ 5].X = m_Coord.X-1;   m_pNeighbor[ 5].Y = m_Coord.Y  ;
            m_pNeighbor[ 6].X = m_Coord.X+1;   m_pNeighbor[ 6].Y = m_Coord.Y  ;
            m_pNeighbor[ 7].X = m_Coord.X+2;   m_pNeighbor[ 7].Y = m_Coord.Y  ;
            m_pNeighbor[ 8].X = m_Coord.X-1;   m_pNeighbor[ 8].Y = m_Coord.Y+1;
            m_pNeighbor[ 9].X = m_Coord.X  ;   m_pNeighbor[ 9].Y = m_Coord.Y+1;
            m_pNeighbor[10].X = m_Coord.X+1;   m_pNeighbor[10].Y = m_Coord.Y+1;
            m_pNeighbor[11].X = m_Coord.X+2;   m_pNeighbor[11].Y = m_Coord.Y+1;
            break;
   case 5:  m_pNeighbor[ 0].X = m_Coord.X  ;   m_pNeighbor[ 0].Y = m_Coord.Y-1;
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
   }
   VerifyNeighbor(sizeField);
}

bool nsCell::CTrSq1::PointInRegion(const POINT &point) const { // принадлежат ли эти экранные координаты ячейке
   if ((m_iDirection == 0) || (m_iDirection == 4))
      return PointInPolygon(point, m_pRegion, 4);
   else
      return PointInPolygon(point, m_pRegion, 3);
}

void nsCell::CTrSq1::SetPoint(int area) {
   if (m_Coord.X==0 && m_Coord.Y==0) {
      m_a = sqrt(3*area/(1+SQRT3/2)); // размер стороны треугольника и квадрата
      m_n = m_a*SIN75;
      m_m = m_a*SIN15;
      m_b = m_n+m_m;
      m_k = m_n-m_m;
      m_sq = (m_a*SQRT3-m_GContext.m_Border.m_iWidth*6)/(4*SIN75);
   }

   // определение координат точек фигуры
   float oX = m_b + m_n * (m_Coord.X/3*2); // offset X
   float oY = m_n + m_n*2*(m_Coord.Y/2); // offset Y
   switch (m_iDirection) {
   case 0:
      m_pRegion[0].x = oX - m_m;     m_pRegion[0].y = oY - m_n;
      m_pRegion[1].x = oX;           m_pRegion[1].y = oY;
      m_pRegion[2].x = oX - m_n;     m_pRegion[2].y = oY + m_m;
      m_pRegion[3].x = oX - m_b;     m_pRegion[3].y = oY - m_k;
      break;
   case 1:
      m_pRegion[0].x = oX;           m_pRegion[0].y = oY;
      m_pRegion[1].x = oX - m_m;     m_pRegion[1].y = oY - m_n;
      m_pRegion[2].x = oX + m_k;     m_pRegion[2].y = oY - m_k;
      break;
   case 2:
      m_pRegion[0].x = oX + m_k;     m_pRegion[0].y = oY - m_k;
      m_pRegion[1].x = oX + m_n;     m_pRegion[1].y = oY + m_m;
      m_pRegion[2].x = oX;           m_pRegion[2].y = oY;
      break;
   case 3:
      m_pRegion[0].x = oX - m_m;     m_pRegion[0].y = oY + m_n;
      m_pRegion[1].x = oX - m_n;     m_pRegion[1].y = oY + m_m;
      m_pRegion[2].x = oX;           m_pRegion[2].y = oY;
      break;
   case 4:
      m_pRegion[0].x = oX + m_n;     m_pRegion[0].y = oY + m_m;
      m_pRegion[1].x = oX;           m_pRegion[1].y = oY;
      m_pRegion[2].x = oX - m_m;     m_pRegion[2].y = oY + m_n;
      m_pRegion[3].x = oX + m_k;     m_pRegion[3].y = oY + m_b;
      break;
   case 5:
      m_pRegion[0].x = oX + m_n;     m_pRegion[0].y = oY + m_m;
      m_pRegion[1].x = oX + m_n+m_k; m_pRegion[1].y = oY + m_n;
      m_pRegion[2].x = oX + m_k;     m_pRegion[2].y = oY + m_b;
      break;
   }

   // определение координат вписанного в фигуру квадрата - область в которую выводится изображение/текст
   {
      POINTFLOAT center;
      float ksw1 = m_k/2-m_sq/2-m_GContext.m_Border.m_iWidth/SQRT2;
      float ksw2 = m_k/2+m_sq/2+m_GContext.m_Border.m_iWidth/SQRT2;
      switch (m_iDirection) {
      case 0:  center.x = oX - m_b/2;     center.y = oY - m_k/2;     break;
      case 1:  center.x = oX + ksw1;      center.y = oY - ksw2;      break;
      case 2:  center.x = oX + ksw2;      center.y = oY - ksw1;      break;
      case 3:  center.x = oX + ksw2-m_n;  center.y = oY - ksw2+m_n;  break;
      case 4:  center.x = oX + m_k/2;     center.y = oY + m_b/2;     break;
      case 5:  center.x = oX + ksw1+m_n;  center.y = oY + ksw2+m_m;  break;
      }
      m_Square.left   = center.x - m_sq/2;
      m_Square.top    = center.y - m_sq/2;
      m_Square.right  = m_Square.left + m_sq;
      m_Square.bottom = m_Square.top  + m_sq;
   }
}

void nsCell::CTrSq1::Paint() const {
/**
   int minX = min(m_pRegion[1].x, min(m_pRegion[2].x, m_pRegion[3].x));
   int maxX = max(m_pRegion[0].x, max(m_pRegion[1].x, m_pRegion[2].x));
   int minY = min(m_pRegion[0].y, min(m_pRegion[1].y, m_pRegion[2].y));
   int maxY = max(m_pRegion[0].y, max(m_pRegion[1].y, max(m_pRegion[2].y, m_pRegion[3].y)));
   BitBlt(m_GContext.m_hDCTmp, minX, minY, maxX - minX, maxY - minY,
          m_GContext.m_hDCBck, minX, minY, SRCCOPY);
/**/
   CBase::Paint();

   switch (m_iDirection) {
   case 0: case 2: case 5: SelectObject(m_GContext.m_hDCTmp, m_bDown ? m_GContext.m_hPenLight  : m_GContext.m_hPenShadow); break;
   case 1: case 3: case 4: SelectObject(m_GContext.m_hDCTmp, m_bDown ? m_GContext.m_hPenShadow : m_GContext.m_hPenLight ); break;
   }
   MoveToEx(m_GContext.m_hDCTmp, m_pRegion[0].x, m_pRegion[0].y, NULL);
   LineTo  (m_GContext.m_hDCTmp, m_pRegion[1].x, m_pRegion[1].y);
   LineTo  (m_GContext.m_hDCTmp, m_pRegion[2].x, m_pRegion[2].y);
   switch (m_iDirection) {
   case 0: case 2: case 5: SelectObject(m_GContext.m_hDCTmp, m_bDown ? m_GContext.m_hPenShadow : m_GContext.m_hPenLight ); break;
   case 1: case 3: case 4: SelectObject(m_GContext.m_hDCTmp, m_bDown ? m_GContext.m_hPenLight  : m_GContext.m_hPenShadow); break;
   }
   switch (m_iDirection) {
   case 0: case 4:
      LineTo  (m_GContext.m_hDCTmp, m_pRegion[3].x, m_pRegion[3].y);
      LineTo  (m_GContext.m_hDCTmp, m_pRegion[0].x, m_pRegion[0].y);
      RegionDraw(
         m_GContext.m_hDCDst, true,
         m_pRegion, 4, NULL, NULL, 0, 0, 0, 0,
         m_GContext.m_hDCTmp, NULL, 0,0);
      break;
   case 1: case 2: case 3: case 5:
      LineTo  (m_GContext.m_hDCTmp, m_pRegion[0].x, m_pRegion[0].y);
      RegionDraw(
         m_GContext.m_hDCDst, true,
         m_pRegion, 3, NULL, NULL, 0, 0, 0, 0,
         m_GContext.m_hDCTmp, NULL, 0,0);
      break;
   }
}
