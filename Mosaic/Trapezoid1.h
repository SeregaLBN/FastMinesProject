////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "Trapezoid1.h"
//
// �������� ������ CTrapezoid1
////////////////////////////////////////////////////////////////////////////////

#ifndef __FILE__TRAPEZOID1__
#define __FILE__TRAPEZOID1__

#ifndef __AFX_H__
   #include <Windows.h>
#endif
#include "Base.h"

namespace nsCell {

class CTrapezoid1: public CBase {
private:
   static float m_b;  // -   ������� ������� �������� (���������)
   static float m_c;  //
   static float m_R;  // -   ��������� ��������
   static float m_r;  // -   ������ ��������
public:
   static SIZE GetSizeInPixel(const COORD &sizeField, int area);
   static int SizeInscribedSquare(int area, int borderWidth);
public:
   CTrapezoid1(const COORD &Coord, const COORD &sizeField, int area, const CGraphicContext &gContext);
   void SetPoint(int area); // ���������� ���������� ����� �� ������� ������� ������
   void Paint() const;
};

} // namespace nsCell

#endif // __FILE__TRAPEZOID1__
