////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "PentagonT24.h"
//
// �������� ������ CPentagonT24 - �������������� 5-�� ��������, ��� �2 � �4
////////////////////////////////////////////////////////////////////////////////

#ifndef __FILE__PENTAGONT24__
#define __FILE__PENTAGONT24__

#ifndef __AFX_H__
   #include <Windows.h>
#endif
#include "Base.h"

namespace nsCell {

class CPentagonT24: public CBase {
private:
   static float m_b;
   static float m_c;
public:
   static SIZE GetSizeInPixel(const COORD &sizeField, int area);
   static int SizeInscribedSquare(int area, int borderWidth);
public:
   CPentagonT24(const COORD &Coord, const COORD &sizeField, int area, const CGraphicContext &gContext);
   void SetPoint(int area); // ���������� ���������� ����� �� ������� ������� ������
   void Paint() const;
};

} // namespace nsCell

#endif // __FILE__PENTAGONT24__
