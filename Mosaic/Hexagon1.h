////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "Hexagon1.h"
//
// �������� ������ CHexagon1 - ���������� 6-�� �������� (����)
////////////////////////////////////////////////////////////////////////////////

#ifndef __FILE__HEXAGON1__
#define __FILE__HEXAGON1__

#ifndef __AFX_H__
   #include <Windows.h>
#endif
#include "Base.h"

namespace nsCell {

class CHexagon1: public CBase {
private:
   static float m_b; // ������ ��������������
   static float m_h; // ������ ��������������
public:
   static SIZE GetSizeInPixel(const SIZE &sizeField, int iArea);
   static int SizeInscribedSquare(int iArea, int iBorderWidth);
public:
   CHexagon1(const COORD &Coord, const SIZE &sizeField, int iArea, const CGraphicContext &gContext);
   void SetPoint(int iArea); // ���������� ���������� ����� �� ������� ������� ������
   void Paint() const;
};

} // namespace nsCell

#endif // __FILE__HEXAGON1__
