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
   static SIZE GetSizeInPixel(const SIZE &sizeField, int iArea);
   static int SizeInscribedSquare(int iArea, int iBorderWidth);
public:
   CTriangle2(const COORD &Coord, const SIZE &sizeField, int iArea, const CGraphicContext &gContext);
   void SetPoint(int iArea); // ���������� ���������� ����� �� ������� ������� ������
   void Paint() const;
};

} // namespace nsCell

#endif // __FILE__TRIANGLE2__
