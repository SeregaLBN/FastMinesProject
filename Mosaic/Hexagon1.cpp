////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "Hexagon1.cpp"
//
// Реализация класса CHexagon1 - правильный 6-ти угольник (сота)
////////////////////////////////////////////////////////////////////////////////

#include "StdAfx.h"
#include "Hexagon1.h"

////////////////////////////////////////////////////////////////////////////////
//                               local init
////////////////////////////////////////////////////////////////////////////////

float nsCell::CHexagon1::m_b; // ширина шестиугольника
float nsCell::CHexagon1::m_h; // высота шестиугольника

////////////////////////////////////////////////////////////////////////////////
//                               static function
////////////////////////////////////////////////////////////////////////////////

SIZE nsCell::CHexagon1::GetSizeInPixel(const SIZE &sizeField, int iArea) {
   m_a = sqrt(2*iArea/SQRT27); // размер стороны шестиугольника
   SIZE result = {(sizeField.cx      + 0.5f)*m_a*SQRT3,
                  (sizeField.cy*1.5f + 0.5f)*m_a};
   return result;
}

int nsCell::CHexagon1::SizeInscribedSquare(int iArea, int iBorderWidth) {
   m_a = sqrt(2*iArea/SQRT27); // размер стороны шестиугольника
   m_sq = 2*(m_a*SQRT3-iBorderWidth*2)/(1+SQRT3);
   return m_sq;
}

////////////////////////////////////////////////////////////////////////////////
//                               virtual function
////////////////////////////////////////////////////////////////////////////////

nsCell::CHexagon1::CHexagon1(const COORD &Coord, const SIZE &sizeField, int iArea, const CGraphicContext &gContext)
   : CBase(Coord, sizeField, iArea, gContext,
           6, 6,
           Coord.Y&1 // 0..1
          )
{
   SetPoint(iArea);
   // определяю координаты соседей
   m_pNeighbor[0].X = m_iDirection ? m_Coord.X   : m_Coord.X-1;
   m_pNeighbor[0].Y = m_Coord.Y-1;
   m_pNeighbor[1].X = m_iDirection ? m_Coord.X+1 : m_Coord.X;
   m_pNeighbor[1].Y = m_Coord.Y-1;
   m_pNeighbor[2].X = m_Coord.X-1;
   m_pNeighbor[2].Y = m_Coord.Y;
   m_pNeighbor[3].X = m_Coord.X+1;
   m_pNeighbor[3].Y = m_Coord.Y;
   m_pNeighbor[4].X = m_iDirection ? m_Coord.X   : m_Coord.X-1;
   m_pNeighbor[4].Y = m_Coord.Y+1;
   m_pNeighbor[5].X = m_iDirection ? m_Coord.X+1 : m_Coord.X;
   m_pNeighbor[5].Y = m_Coord.Y+1;
   VerifyNeighbor(sizeField);
}

void nsCell::CHexagon1::SetPoint(int iArea) {
   if (m_Coord.X==0 && m_Coord.Y==0) {
      m_a = sqrt(2*iArea/SQRT27);                        // размер стороны шестиугольника
      m_b = m_a*SQRT3;                                  // ширина шестиугольника
      m_h = m_a*2;                                      // высота шестиугольника
      m_sq = 2*(m_a*SQRT3-m_GContext.m_Border.m_iWidth*2)/(1+SQRT3); // размер квадрата, вписанного в шестиугольник
   }

   // определение координат точек фигуры
   float oX = (m_Coord.X+1)*m_b;      // offset X
   float oY = (m_Coord.Y+!m_iDirection)*m_a*1.5f; // offset Y
   switch (m_iDirection) {
   case 0:
      m_pRegion[0].x = oX;         m_pRegion[0].y = oY - m_a;
      m_pRegion[1].x = oX;         m_pRegion[1].y = oY;
      m_pRegion[2].x = oX - m_b/2; m_pRegion[2].y = oY + m_a/2;
      m_pRegion[3].x = oX - m_b;   m_pRegion[3].y = oY;
      m_pRegion[4].x = oX - m_b;   m_pRegion[4].y = oY - m_a;
      m_pRegion[5].x = oX - m_b/2; m_pRegion[5].y = oY - m_a*1.5f;
      break;
   case 1:
      m_pRegion[0].x = oX + m_b/2; m_pRegion[0].y = oY + m_a/2;
      m_pRegion[1].x = oX + m_b/2; m_pRegion[1].y = oY + m_a*1.5f;
      m_pRegion[2].x = oX;         m_pRegion[2].y = oY + m_a*2;
      m_pRegion[3].x = oX - m_b/2; m_pRegion[3].y = oY + m_a*1.5f;
      m_pRegion[4].x = oX - m_b/2; m_pRegion[4].y = oY + m_a/2;
      m_pRegion[5].x = oX;         m_pRegion[5].y = oY;
      break;
   }

   // определение координат вписанного в фигуру квадрата - область в которую выводится изображение/текст
   {
      POINTFLOAT center; // координата центра квадрата
      switch (m_iDirection) {
      case 0: center.x = oX - m_b/2; center.y = oY - m_a/2; break;
      case 1: center.x = oX;         center.y = oY + m_a;   break;
      }
      m_Square.left   = center.x - m_sq/2;
      m_Square.top    = center.y - m_sq/2;
      m_Square.right  = m_Square.left + m_sq;
      m_Square.bottom = m_Square.top  + m_sq;
   }
}

void nsCell::CHexagon1::Paint() const {
   /**
   int minX = m_pRegion[4].x;
   int maxX = m_pRegion[0].x;
   int minY = m_pRegion[5].y;
   int maxY = m_pRegion[2].y;
   BitBlt(m_GContext.m_hDCTmp, minX, minY, maxX - minX, maxY - minY,
          hDCBck, minX, minY, SRCCOPY);
   /**/
   CBase::Paint();

   SelectObject(m_GContext.m_hDCTmp, m_bDown ? m_GContext.m_hPenLight  : m_GContext.m_hPenShadow);
   MoveToEx(m_GContext.m_hDCTmp, m_pRegion[1].x, m_pRegion[1].y, NULL);
   LineTo  (m_GContext.m_hDCTmp, m_pRegion[2].x, m_pRegion[2].y);
   LineTo  (m_GContext.m_hDCTmp, m_pRegion[3].x, m_pRegion[3].y);
   SelectObject(m_GContext.m_hDCTmp, m_bDown ? m_GContext.m_hPenShadow : m_GContext.m_hPenLight );
   LineTo  (m_GContext.m_hDCTmp, m_pRegion[4].x, m_pRegion[4].y);
   LineTo  (m_GContext.m_hDCTmp, m_pRegion[5].x, m_pRegion[5].y);
   LineTo  (m_GContext.m_hDCTmp, m_pRegion[0].x, m_pRegion[0].y);
   SelectObject(m_GContext.m_hDCTmp, m_bDown ? m_GContext.m_hPenLight  : m_GContext.m_hPenShadow);
   LineTo  (m_GContext.m_hDCTmp, m_pRegion[1].x, m_pRegion[1].y);

   RegionDraw(
      m_GContext.m_hDCDst, true,
      m_pRegion, 6, NULL, NULL, 0, 0, 0, 0,
      m_GContext.m_hDCTmp, NULL, 0,0);
}
