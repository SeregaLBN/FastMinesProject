////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "Triangle4.h"
//
// �������� ������ CTriangle4 - ����������� 30�-30�-120�
////////////////////////////////////////////////////////////////////////////////

#ifndef __FILE__TRIANGLE4__
#define __FILE__TRIANGLE4__

#ifndef __AFX_H__
   #include <Windows.h>
#endif
#include "Base.h"

namespace nsCell {

class CTriangle4: public CBase {
private:
   static float m_b;  // -   �������� ������� ������� a (b=a/2)
   static float m_R;  // -   ������ (������) �������
   static float m_r;  // -   ������
public:
   static SIZE GetSizeInPixel(const SIZE &sizeField, int iArea);
   static int SizeInscribedSquare(int iArea, int iBorderWidth);
public:
   CTriangle4(const COORD &Coord, const SIZE &sizeField, int iArea, const CGraphicContext &gContext);
   void SetPoint(int iArea); // ���������� ���������� ����� �� ������� ������� ������
   void Paint() const;
};

} // namespace nsCell

#endif // __FILE__TRIANGLE4__
