////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "TrSq2.h"
//
// �������� ������ CTrSq2 - ������� �� 24� ������������� � 12� ��������� (�� 1 ������� ���������� 2 ������������)
////////////////////////////////////////////////////////////////////////////////

#ifndef __FILE__TRSQ2_
#define __FILE__TRSQ2_

#ifndef __AFX_H__
   #include <Windows.h>
#endif
#include "Base.h"

namespace nsCell {

class CTrSq2: public CBase {
private:
   static float m_b;
   static float m_h;
public:
   static SIZE GetSizeInPixel(const COORD &sizeField, int area);
   static int SizeInscribedSquare(int area, int borderWidth);
public:
   CTrSq2(const COORD &Coord, const COORD &sizeField, int area, const CGraphicContext &gContext);
   bool PointInRegion(const POINT &point) const; // ����������� �� ��� �������� ���������� ������
   void SetPoint(int area); // ���������� ���������� ����� �� ������� ������� ������
   void Paint() const;
};

} // namespace nsCell

#endif // __FILE__TRSQ2_
