////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "PentagonT10.cpp"
//
// Реализация класса CPentagonT10 - 5-ти угольник, тип №10
////////////////////////////////////////////////////////////////////////////////

#include "StdAfx.h"
#include "PentagonT10.h"

////////////////////////////////////////////////////////////////////////////////
//                               static function
////////////////////////////////////////////////////////////////////////////////

SIZE nsCell::CPentagonT10::GetSizeInPixel(const COORD &sizeField, int area) {
   m_a = sqrt(area/7.f);
   SIZE result = {2*m_a +
                  5*m_a*((sizeField.X+1)/2) +
                    m_a*((sizeField.X+0)/2),
                  2*m_a +
                  3*m_a*((sizeField.Y+2)/3) +
                  3*m_a*((sizeField.Y+1)/3) +
                    m_a*((sizeField.Y+0)/3)};
   return result;
}

int nsCell::CPentagonT10::SizeInscribedSquare(int area, int borderWidth) {
   m_sq = 2*(sqrt(area/7.f)-borderWidth);
   return m_sq;
}

////////////////////////////////////////////////////////////////////////////////
//                               virtual function
////////////////////////////////////////////////////////////////////////////////

nsCell::CPentagonT10::CPentagonT10(const COORD &Coord, const COORD &sizeField, int area, const CGraphicContext &gContext)
   : CBase(Coord, sizeField, area, gContext, 7, 5,
           ((Coord.Y%6)<<1) + (Coord.X&1) // 0..11
          )
{
   SetPoint(area);
   // определяю координаты соседей
   switch (m_iDirection) {
   case  0:
      m_pNeighbor[0].X = m_Coord.X  ;   m_pNeighbor[0].Y = m_Coord.Y-1;
      m_pNeighbor[1].X = m_Coord.X-1;   m_pNeighbor[1].Y = m_Coord.Y  ;
      m_pNeighbor[2].X = m_Coord.X+1;   m_pNeighbor[2].Y = m_Coord.Y  ;
      m_pNeighbor[3].X = m_Coord.X-1;   m_pNeighbor[3].Y = m_Coord.Y+1;
      m_pNeighbor[4].X = m_Coord.X  ;   m_pNeighbor[4].Y = m_Coord.Y+1;
      m_pNeighbor[5].X = m_Coord.X+1;   m_pNeighbor[5].Y = m_Coord.Y+1;
      m_pNeighbor[6].X = m_Coord.X-1;   m_pNeighbor[6].Y = m_Coord.Y+2;
      break;
   case  1:
      m_pNeighbor[0].X = m_Coord.X  ;   m_pNeighbor[0].Y = m_Coord.Y-2;
      m_pNeighbor[1].X = m_Coord.X-1;   m_pNeighbor[1].Y = m_Coord.Y-1;
      m_pNeighbor[2].X = m_Coord.X  ;   m_pNeighbor[2].Y = m_Coord.Y-1;
      m_pNeighbor[3].X = m_Coord.X+1;   m_pNeighbor[3].Y = m_Coord.Y-1;
      m_pNeighbor[4].X = m_Coord.X-1;   m_pNeighbor[4].Y = m_Coord.Y  ;
      m_pNeighbor[5].X = m_Coord.X+1;   m_pNeighbor[5].Y = m_Coord.Y  ;  
      m_pNeighbor[6].X = m_Coord.X  ;   m_pNeighbor[6].Y = m_Coord.Y+1;
      break;
   case  2:
      m_pNeighbor[0].X = m_Coord.X  ;   m_pNeighbor[0].Y = m_Coord.Y-1;
      m_pNeighbor[1].X = m_Coord.X+1;   m_pNeighbor[1].Y = m_Coord.Y  ;
      m_pNeighbor[2].X = m_Coord.X-1;   m_pNeighbor[2].Y = m_Coord.Y+1;
      m_pNeighbor[3].X = m_Coord.X  ;   m_pNeighbor[3].Y = m_Coord.Y+1;
      m_pNeighbor[4].X = m_Coord.X+1;   m_pNeighbor[4].Y = m_Coord.Y+1;
      m_pNeighbor[5].X = m_Coord.X  ;   m_pNeighbor[5].Y = m_Coord.Y+2;  
      m_pNeighbor[6] = INCORRECT_COORD;
      break;
   case  3:
      m_pNeighbor[0].X = m_Coord.X-1;   m_pNeighbor[0].Y = m_Coord.Y-1;
      m_pNeighbor[1].X = m_Coord.X  ;   m_pNeighbor[1].Y = m_Coord.Y-1;
      m_pNeighbor[2].X = m_Coord.X+1;   m_pNeighbor[2].Y = m_Coord.Y-1;
      m_pNeighbor[3].X = m_Coord.X-1;   m_pNeighbor[3].Y = m_Coord.Y  ;
      m_pNeighbor[4].X = m_Coord.X-1;   m_pNeighbor[4].Y = m_Coord.Y+1;
      m_pNeighbor[5].X = m_Coord.X  ;   m_pNeighbor[5].Y = m_Coord.Y+1;
      m_pNeighbor[6] = INCORRECT_COORD;
      break;
   case  4:
      m_pNeighbor[0].X = m_Coord.X  ;   m_pNeighbor[0].Y = m_Coord.Y-1;
      m_pNeighbor[1].X = m_Coord.X+1;   m_pNeighbor[1].Y = m_Coord.Y-1;
      m_pNeighbor[2].X = m_Coord.X+1;   m_pNeighbor[2].Y = m_Coord.Y  ;
      m_pNeighbor[3].X = m_Coord.X  ;   m_pNeighbor[3].Y = m_Coord.Y+1;
      m_pNeighbor[4].X = m_Coord.X+1;   m_pNeighbor[4].Y = m_Coord.Y+1;
      m_pNeighbor[5].X = m_Coord.X+2;   m_pNeighbor[5].Y = m_Coord.Y+1;
      m_pNeighbor[6] = INCORRECT_COORD;
      break;
   case  5:
      m_pNeighbor[0].X = m_Coord.X+1;   m_pNeighbor[0].Y = m_Coord.Y-2;
      m_pNeighbor[1].X = m_Coord.X-1;   m_pNeighbor[1].Y = m_Coord.Y-1;
      m_pNeighbor[2].X = m_Coord.X  ;   m_pNeighbor[2].Y = m_Coord.Y-1;
      m_pNeighbor[3].X = m_Coord.X+1;   m_pNeighbor[3].Y = m_Coord.Y-1;
      m_pNeighbor[4].X = m_Coord.X-1;   m_pNeighbor[4].Y = m_Coord.Y  ;
      m_pNeighbor[5].X = m_Coord.X+1;   m_pNeighbor[5].Y = m_Coord.Y+1;
      m_pNeighbor[6] = INCORRECT_COORD;
      break;
   case  6:
      m_pNeighbor[0].X = m_Coord.X  ;   m_pNeighbor[0].Y = m_Coord.Y-2;
      m_pNeighbor[1].X = m_Coord.X-2;   m_pNeighbor[1].Y = m_Coord.Y-1;
      m_pNeighbor[2].X = m_Coord.X-1;   m_pNeighbor[2].Y = m_Coord.Y-1;
      m_pNeighbor[3].X = m_Coord.X  ;   m_pNeighbor[3].Y = m_Coord.Y-1;
      m_pNeighbor[4].X = m_Coord.X-1;   m_pNeighbor[4].Y = m_Coord.Y  ;
      m_pNeighbor[5].X = m_Coord.X+1;   m_pNeighbor[5].Y = m_Coord.Y  ;
      m_pNeighbor[6].X = m_Coord.X  ;   m_pNeighbor[6].Y = m_Coord.Y+1;
      break;
   case  7:
      m_pNeighbor[0].X = m_Coord.X-1;   m_pNeighbor[0].Y = m_Coord.Y-1;
      m_pNeighbor[1].X = m_Coord.X-1;   m_pNeighbor[1].Y = m_Coord.Y  ;
      m_pNeighbor[2].X = m_Coord.X+1;   m_pNeighbor[2].Y = m_Coord.Y  ;
      m_pNeighbor[3].X = m_Coord.X-1;   m_pNeighbor[3].Y = m_Coord.Y+1;
      m_pNeighbor[4].X = m_Coord.X  ;   m_pNeighbor[4].Y = m_Coord.Y+1;
      m_pNeighbor[5].X = m_Coord.X+1;   m_pNeighbor[5].Y = m_Coord.Y+1;
      m_pNeighbor[6].X = m_Coord.X  ;   m_pNeighbor[6].Y = m_Coord.Y+2;
      break;
   case  8:
      m_pNeighbor[0].X = m_Coord.X-1;   m_pNeighbor[0].Y = m_Coord.Y-1;
      m_pNeighbor[1].X = m_Coord.X  ;   m_pNeighbor[1].Y = m_Coord.Y-1;
      m_pNeighbor[2].X = m_Coord.X+1;   m_pNeighbor[2].Y = m_Coord.Y-1;
      m_pNeighbor[3].X = m_Coord.X+1;   m_pNeighbor[3].Y = m_Coord.Y  ;
      m_pNeighbor[4].X = m_Coord.X-1;   m_pNeighbor[4].Y = m_Coord.Y+1;
      m_pNeighbor[5].X = m_Coord.X  ;   m_pNeighbor[5].Y = m_Coord.Y+1;
      m_pNeighbor[6] = INCORRECT_COORD;
      break;
   case  9:
      m_pNeighbor[0].X = m_Coord.X  ;   m_pNeighbor[0].Y = m_Coord.Y-1;
      m_pNeighbor[1].X = m_Coord.X-1;   m_pNeighbor[1].Y = m_Coord.Y  ;
      m_pNeighbor[2].X = m_Coord.X-2;   m_pNeighbor[2].Y = m_Coord.Y+1;
      m_pNeighbor[3].X = m_Coord.X-1;   m_pNeighbor[3].Y = m_Coord.Y+1;
      m_pNeighbor[4].X = m_Coord.X  ;   m_pNeighbor[4].Y = m_Coord.Y+1;
      m_pNeighbor[5].X = m_Coord.X  ;   m_pNeighbor[5].Y = m_Coord.Y+2;
      m_pNeighbor[6] = INCORRECT_COORD;
      break;
   case 10:
      m_pNeighbor[0].X = m_Coord.X  ;   m_pNeighbor[0].Y = m_Coord.Y-1;
      m_pNeighbor[1].X = m_Coord.X+1;   m_pNeighbor[1].Y = m_Coord.Y-1;
      m_pNeighbor[2].X = m_Coord.X-1;   m_pNeighbor[2].Y = m_Coord.Y  ;
      m_pNeighbor[3].X = m_Coord.X-1;   m_pNeighbor[3].Y = m_Coord.Y+1;
      m_pNeighbor[4].X = m_Coord.X  ;   m_pNeighbor[4].Y = m_Coord.Y+1;
      m_pNeighbor[5].X = m_Coord.X+1;   m_pNeighbor[5].Y = m_Coord.Y+1;
      m_pNeighbor[6] = INCORRECT_COORD;
      break;
   case 11:
      m_pNeighbor[0].X = m_Coord.X  ;   m_pNeighbor[0].Y = m_Coord.Y-2;
      m_pNeighbor[1].X = m_Coord.X  ;   m_pNeighbor[1].Y = m_Coord.Y-1;
      m_pNeighbor[2].X = m_Coord.X+1;   m_pNeighbor[2].Y = m_Coord.Y-1;
      m_pNeighbor[3].X = m_Coord.X+2;   m_pNeighbor[3].Y = m_Coord.Y-1;
      m_pNeighbor[4].X = m_Coord.X+1;   m_pNeighbor[4].Y = m_Coord.Y  ;
      m_pNeighbor[5].X = m_Coord.X  ;   m_pNeighbor[5].Y = m_Coord.Y+1;
      m_pNeighbor[6] = INCORRECT_COORD;
      break;
   }
   VerifyNeighbor(sizeField);
}

void nsCell::CPentagonT10::SetPoint(int area) {
   if (m_Coord.X==0 && m_Coord.Y==0) {
      m_a = sqrt(area/7.f);
      m_sq = 2*(m_a-m_GContext.m_Border.m_iWidth);
   }

   // определение координат точек фигуры
   float oX; // offset X
   float oY;  // offset Y
   switch (m_iDirection) {
   case 0: case 6: case  8: case 9: case 10:          oX = m_a*2+m_a*6*((m_Coord.X+0)/2); break;
   case 1: case 2: case  3: case 4: case 5: case 7:   oX = m_a*5+m_a*6*((m_Coord.X+0)/2); break;
   case 11:                                           oX = m_a*2+m_a*6*((m_Coord.X+1)/2); break;
   }
   switch (m_iDirection) {
   case 0:                                            oY = m_a*5 +m_a*14*(m_Coord.Y/6);   break;
   case 1:                                            oY =      m_a*14*(m_Coord.Y/6);   break;
   case 2: case 3: case  4: case 5:                   oY = m_a*6 +m_a*14*(m_Coord.Y/6);   break;
   case 6:                                            oY = m_a*7 +m_a*14*(m_Coord.Y/6);   break;
   case 7:                                            oY = m_a*12+m_a*14*(m_Coord.Y/6);   break;
   case 8: case 9: case 10: case 11:                  oY = m_a*13+m_a*14*(m_Coord.Y/6);   break;
   }
   switch (m_iDirection) {
   case 0: case 3: case 7: case 8:
      m_pRegion[0].x = oX      ; m_pRegion[0].y = oY      ;
      m_pRegion[1].x = oX - m_a*2; m_pRegion[1].y = oY - m_a*2;
      m_pRegion[2].x = oX - m_a  ; m_pRegion[2].y = oY - m_a*3;
      m_pRegion[3].x = oX + m_a  ; m_pRegion[3].y = oY - m_a*3;
      m_pRegion[4].x = oX + m_a*2; m_pRegion[4].y = oY - m_a*2;
      break;
   case 1: case 4: case 6: case 10:
      m_pRegion[0].x = oX      ; m_pRegion[0].y = oY      ;
      m_pRegion[1].x = oX + m_a*2; m_pRegion[1].y = oY + m_a*2;
      m_pRegion[2].x = oX + m_a  ; m_pRegion[2].y = oY + m_a*3;
      m_pRegion[3].x = oX - m_a  ; m_pRegion[3].y = oY + m_a*3;
      m_pRegion[4].x = oX - m_a*2; m_pRegion[4].y = oY + m_a*2;
      break;
   case 2: case 11:
      m_pRegion[0].x = oX - m_a*2; m_pRegion[0].y = oY + m_a*2;
      m_pRegion[1].x = oX - m_a*3; m_pRegion[1].y = oY + m_a  ;
      m_pRegion[2].x = oX - m_a*3; m_pRegion[2].y = oY - m_a  ;
      m_pRegion[3].x = oX - m_a*2; m_pRegion[3].y = oY - m_a*2;
      m_pRegion[4].x = oX      ; m_pRegion[4].y = oY      ;
      break;
   case 5: case 9:
      m_pRegion[0].x = oX + m_a*2; m_pRegion[0].y = oY - m_a*2;
      m_pRegion[1].x = oX + m_a*3; m_pRegion[1].y = oY - m_a  ;
      m_pRegion[2].x = oX + m_a*3; m_pRegion[2].y = oY + m_a  ;
      m_pRegion[3].x = oX + m_a*2; m_pRegion[3].y = oY + m_a*2;
      m_pRegion[4].x = oX      ; m_pRegion[4].y = oY;
      break;
   }

   // определение координат вписанного в фигуру квадрата - область в которую выводится изображение/текст
   {
      POINTFLOAT center;
      switch (m_iDirection) {
      case 0: case 3: case 7: case 8:  center.x = oX;       center.y = oY - m_a*2; break;
      case 1: case 4: case 6: case 10: center.x = oX;       center.y = oY + m_a*2; break;
      case 2: case 11:                 center.x = oX - m_a*2; center.y = oY;       break;
      case 5: case 9:                  center.x = oX + m_a*2; center.y = oY;       break;
      }
      m_Square.left   = center.x - m_sq/2;
      m_Square.top    = center.y - m_sq/2;
      m_Square.right  = m_Square.left + m_sq;
      m_Square.bottom = m_Square.top  + m_sq;
   }
}

void nsCell::CPentagonT10::Paint() const {
   int minX = min(m_pRegion[1].x, m_pRegion[4].x);
   int maxX = max(m_pRegion[1].x, m_pRegion[4].x);
   int minY = min(m_pRegion[0].y, m_pRegion[3].y);
   int maxY = max(m_pRegion[0].y, m_pRegion[3].y);
   BitBlt(m_GContext.m_hDCTmp, minX, minY, maxX - minX, maxY - minY,
          m_GContext.m_hDCBck, minX, minY, SRCCOPY);

   CBase::Paint();

   switch (m_iDirection) {
   case 0: case 3: case 7: case 8:
   case 2: case 11:                 SelectObject(m_GContext.m_hDCTmp, m_bDown ? m_GContext.m_hPenShadow : m_GContext.m_hPenLight ); break;
   case 1: case 4: case 6: case 10:
   case 5: case 9:                  SelectObject(m_GContext.m_hDCTmp, m_bDown ? m_GContext.m_hPenLight  : m_GContext.m_hPenShadow); break;
   }
   MoveToEx(m_GContext.m_hDCTmp, m_pRegion[0].x, m_pRegion[0].y, NULL);
   LineTo  (m_GContext.m_hDCTmp, m_pRegion[1].x, m_pRegion[1].y);
   LineTo  (m_GContext.m_hDCTmp, m_pRegion[2].x, m_pRegion[2].y);
   LineTo  (m_GContext.m_hDCTmp, m_pRegion[3].x, m_pRegion[3].y);
   switch (m_iDirection) {
   case 0: case 3: case 7: case 8:
   case 2: case 11:                 SelectObject(m_GContext.m_hDCTmp, m_bDown ? m_GContext.m_hPenLight  : m_GContext.m_hPenShadow); break;
   case 1: case 4: case 6: case 10:
   case 5: case 9:                  SelectObject(m_GContext.m_hDCTmp, m_bDown ? m_GContext.m_hPenShadow : m_GContext.m_hPenLight ); break;
   }
   LineTo  (m_GContext.m_hDCTmp, m_pRegion[4].x, m_pRegion[4].y);
   LineTo  (m_GContext.m_hDCTmp, m_pRegion[0].x, m_pRegion[0].y);

   RegionDraw(
      m_GContext.m_hDCDst, true,
      m_pRegion, 5, NULL, NULL, 0, 0, 0, 0,
      m_GContext.m_hDCTmp, NULL, 0,0);
}

