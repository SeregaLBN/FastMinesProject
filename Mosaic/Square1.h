////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "Square1.h"
//
// �������� ������ CSquare1 - ������� (������������ ������� ����)
////////////////////////////////////////////////////////////////////////////////

#ifndef __FILE__SQUARE1__
#define __FILE__SQUARE1__

#ifndef __AFX_H__
   #include <Windows.h>
#endif
#include "Base.h"

namespace nsCell {

class CSquare1: public CBase {
public:
   static SIZE GetSizeInPixel(const COORD &sizeField, int area);
   static int SizeInscribedSquare(int area, int borderWidth);
public:
   CSquare1(const COORD &Coord, const COORD &sizeField, int area, const CGraphicContext &gContext);
   bool PointInRegion(const POINT &point) const; // ����������� �� ��� �������� ���������� ������
   void SetPoint(int area); // ���������� ���������� ����� �� ������� ������� ������
   void Paint() const;
};

} // namespace nsCell

#endif // __FILE__SQUARE1__
