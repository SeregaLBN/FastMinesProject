////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "Trapezoid3.h"
//
// �������� ������ CTrapezoid3
////////////////////////////////////////////////////////////////////////////////

#ifndef __FILE__TRAPEZOID3__
#define __FILE__TRAPEZOID3__

#ifndef __AFX_H__
   #include <Windows.h>
#endif
#include "Base.h"

namespace nsCell {

class CTrapezoid3: public CBase {
private:
   static float m_b;  // -   ������� ������� �������� (���������)
   static float m_c;  //
   static float m_R;  // -   ��������� ��������
   static float m_r;  // -   ������ ��������
public:
   static SIZE GetSizeInPixel(const SIZE &sizeField, int iArea);
   static int SizeInscribedSquare(int iArea, int iBorderWidth);
public:
   CTrapezoid3(const COORD &Coord, const SIZE &sizeField, int iArea, const CGraphicContext &gContext);
   void SetPoint(int iArea); // ���������� ���������� ����� �� ������� ������� ������
   void Paint() const;
};

} // namespace nsCell

#endif // __FILE__TRAPEZOID3__
