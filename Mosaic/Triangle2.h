////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "Triangle2.h"
//
// �������� ������ CTriangle2 - �������������� ����������� (������� ���� �2 - �������)
////////////////////////////////////////////////////////////////////////////////

#ifndef __FILE__TRIANGLE2__
#define __FILE__TRIANGLE2__

#ifndef __AFX_H__
   #include <Windows.h>
#endif
#include "Base.h"

namespace nsCell {

class CTriangle2: public CBase {
private:
   static float m_h;
   static float m_b;
public:
   static SIZE GetSizeInPixel(const COORD &sizeField, int area);
   static int SizeInscribedSquare(int area, int borderWidth);
public:
   CTriangle2(const COORD &Coord, const COORD &sizeField, int area, const CGraphicContext &gContext);
   void SetPoint(int area); // ���������� ���������� ����� �� ������� ������� ������
   void Paint() const;
};

} // namespace nsCell

#endif // __FILE__TRIANGLE2__
