////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "Triangle2.cpp"
//
// ���������� ������ CTriangle2 - �������������� ����������� (������� ���� �2 - �������)
////////////////////////////////////////////////////////////////////////////////

#include "StdAfx.h"
#include "Triangle2.h"

////////////////////////////////////////////////////////////////////////////////
//                               local init
////////////////////////////////////////////////////////////////////////////////

float nsCell::CTriangle2::m_b;
float nsCell::CTriangle2::m_h;

////////////////////////////////////////////////////////////////////////////////
//                               static function
////////////////////////////////////////////////////////////////////////////////

SIZE nsCell::CTriangle2::GetSizeInPixel(const SIZE &sizeField, int iArea) {
   m_b = sqrt(iArea/SQRT3);
   m_h = m_b*SQRT3;
   SIZE result = {m_b*(sizeField.cx+1),
                  m_h*(sizeField.cy+0)};
   return result;
}

int nsCell::CTriangle2::SizeInscribedSquare(int iArea, int iBorderWidth) {
   m_a = 2*sqrt(iArea/SQRT3); // ������ ������� ������������
   m_sq = (m_a*SQRT3-6*iBorderWidth)/(SQRT3+2);
   return m_sq;
}

////////////////////////////////////////////////////////////////////////////////
//                               virtual function
////////////////////////////////////////////////////////////////////////////////

nsCell::CTriangle2::CTriangle2(const COORD &Coord, const SIZE &sizeField, int iArea, const CGraphicContext &gContext)
   : CBase(Coord, sizeField, iArea, gContext,
           8, 3,
           Coord.X & 1 // 0..1
          )
{
   SetPoint(iArea);
   // ��������� ���������� �������
   switch (m_iDirection) {
   case 0:
      m_pNeighbor[0].X = m_Coord.X  ; m_pNeighbor[0].Y = m_Coord.Y-1;
      m_pNeighbor[1].X = m_Coord.X-2; m_pNeighbor[1].Y = m_Coord.Y  ;
      m_pNeighbor[2].X = m_Coord.X-1; m_pNeighbor[2].Y = m_Coord.Y  ;
      m_pNeighbor[3].X = m_Coord.X+1; m_pNeighbor[3].Y = m_Coord.Y  ;
      m_pNeighbor[4].X = m_Coord.X+2; m_pNeighbor[4].Y = m_Coord.Y  ;
      m_pNeighbor[5].X = m_Coord.X-1; m_pNeighbor[5].Y = m_Coord.Y+1;
      m_pNeighbor[6].X = m_Coord.X  ; m_pNeighbor[6].Y = m_Coord.Y+1;
      m_pNeighbor[7].X = m_Coord.X+1; m_pNeighbor[7].Y = m_Coord.Y+1;
      break;
   case 1:
      m_pNeighbor[0].X = m_Coord.X-1; m_pNeighbor[0].Y = m_Coord.Y-1;
      m_pNeighbor[1].X = m_Coord.X  ; m_pNeighbor[1].Y = m_Coord.Y-1;
      m_pNeighbor[2].X = m_Coord.X+1; m_pNeighbor[2].Y = m_Coord.Y-1;
      m_pNeighbor[3].X = m_Coord.X-2; m_pNeighbor[3].Y = m_Coord.Y  ;
      m_pNeighbor[4].X = m_Coord.X-1; m_pNeighbor[4].Y = m_Coord.Y  ;
      m_pNeighbor[5].X = m_Coord.X+1; m_pNeighbor[5].Y = m_Coord.Y  ;
      m_pNeighbor[6].X = m_Coord.X+2; m_pNeighbor[6].Y = m_Coord.Y  ;
      m_pNeighbor[7].X = m_Coord.X  ; m_pNeighbor[7].Y = m_Coord.Y+1;
      break;
   }
   VerifyNeighbor(sizeField);
}

void nsCell::CTriangle2::SetPoint(int iArea) {
   if (m_Coord.X==0 && m_Coord.Y==0) {
      m_a = 2*sqrt(iArea/SQRT3);
      m_b = m_a/2;
      m_h = m_b*SQRT3;
      m_sq = (m_h*2-6*m_GContext.m_Border.m_iWidth)/(SQRT3+2);
   }

   // ����������� ��������� ����� ������
   float oX = m_a*(m_Coord.X>>1); // offset X
   float oY = m_h* m_Coord.Y;     // offset Y
   switch (m_iDirection) {
   case 0:  m_pRegion[0].x = oX      ; m_pRegion[0].y = oY + m_h;
            m_pRegion[1].x = oX + m_a  ; m_pRegion[1].y = oY + m_h;
            m_pRegion[2].x = oX +   m_b; m_pRegion[2].y = oY    ;   break;
   case 1:  m_pRegion[0].x = oX + m_a+m_b; m_pRegion[0].y = oY    ;
            m_pRegion[1].x = oX +   m_b; m_pRegion[1].y = oY    ;
            m_pRegion[2].x = oX + m_a  ; m_pRegion[2].y = oY + m_h;   break;
   }

   // ����������� ��������� ���������� � ������ �������� - ������� � ������� ��������� �����������/�����
   {
      POINTFLOAT center; // ���������� ���������� � ������ �������� (�� ��������� � ������� ������)
      switch (m_iDirection) {
      case 0: center.x = m_pRegion[0].x + m_b;
              center.y = m_pRegion[0].y - m_sq/2 - m_GContext.m_Border.m_iWidth;   break;
      case 1: center.x = m_pRegion[1].x + m_b;
              center.y = m_pRegion[1].y + m_sq/2 + m_GContext.m_Border.m_iWidth;   break;
      }
      m_Square.left   = center.x - m_sq/2;
      m_Square.top    = center.y - m_sq/2;
      m_Square.right  = m_Square.left + m_sq;
      m_Square.bottom = m_Square.top  + m_sq;
   }
}

void nsCell::CTriangle2::Paint() const {
   CBase::Paint();

   switch (m_iDirection) {
   case 0: case 3:   SelectObject(m_GContext.m_hDCTmp, m_bDown ? m_GContext.m_hPenLight  : m_GContext.m_hPenShadow); break;
   case 1: case 2:   SelectObject(m_GContext.m_hDCTmp, m_bDown ? m_GContext.m_hPenShadow : m_GContext.m_hPenLight ); break;
   }
   MoveToEx(m_GContext.m_hDCTmp, m_pRegion[0].x, m_pRegion[0].y, NULL);
   LineTo  (m_GContext.m_hDCTmp, m_pRegion[1].x, m_pRegion[1].y);
   LineTo  (m_GContext.m_hDCTmp, m_pRegion[2].x, m_pRegion[2].y);
   switch (m_iDirection) {
   case 0: case 3:   SelectObject(m_GContext.m_hDCTmp, m_bDown ? m_GContext.m_hPenShadow : m_GContext.m_hPenLight ); break;
   case 1: case 2:   SelectObject(m_GContext.m_hDCTmp, m_bDown ? m_GContext.m_hPenLight  : m_GContext.m_hPenShadow); break;
   }
   LineTo  (m_GContext.m_hDCTmp, m_pRegion[0].x, m_pRegion[0].y);

   RegionDraw(
      m_GContext.m_hDCDst, true,
      m_pRegion, 3, NULL, NULL, 0, 0, 0, 0,
      m_GContext.m_hDCTmp, NULL, 0,0);
}
