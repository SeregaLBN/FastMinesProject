////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "TrSq1.h"
//
// �������� ������ CTrSq1 - ������� �� 4� ������������� � 2� ���������
////////////////////////////////////////////////////////////////////////////////

#ifndef __FILE__TRSQ1__
#define __FILE__TRSQ1__

#ifndef __AFX_H__
   #include <Windows.h>
#endif
#include "Base.h"

namespace nsCell {

class CTrSq1: public CBase {
private:
   static float m_n;
   static float m_m;
   static float m_b;
   static float m_k;
public:
   static SIZE GetSizeInPixel(const COORD &sizeField, int area);
   static int SizeInscribedSquare(int area, int borderWidth);
public:
   CTrSq1(const COORD &Coord, const COORD &sizeField, int area, const CGraphicContext &gContext);
   bool PointInRegion(const POINT &point) const; // ����������� �� ��� �������� ���������� ������
   void SetPoint(int area); // ���������� ���������� ����� �� ������� ������� ������
   void Paint() const;
};

} // namespace nsCell

#endif // __FILE__TRSQ1__
