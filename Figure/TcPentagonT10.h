////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "TcPentagonT10.h"
//
// �������� ������ TcPentagonT10 - 5-�� ��������, ��� �10
////////////////////////////////////////////////////////////////////////////////

#ifndef FILE_TCPENTAGONT10
#define FILE_TCPENTAGONT10

#include ".\TcBase.h"

//////////////////////////////////
//
// ���� (�������) ������� �� ���������� �������������� 10��� ����.
//
// S  = a^2*7     -   ������� �������������
// a  = (S/7)^0.5 -   ������� �������� ������
// sq = a*2       -   ������ ��������, ���������� � ������������
//////////////////////////////////

namespace nsFigure {

class TcPentagonT10: public TB{
public:
   static POINT GetSizeFieldInPixel(const POINT&, const int&);
   static int SizeInscribedSquare(int);
   static float GetPercentMine(TeSkillLevel); // ������� ��� �� �������� ������ ���������
private:
   POINT neighbor[7]; // ��������� �������; ���� ������ ���, �� ���������� ������������
   POINTFLOAT regionOut[5]; // ���������� 5�� ����� �� ������� ������� ������
   int direction; // 0..11
public:
   TcPentagonT10(const POINT&, const POINT&, const int&);
   int   GetNeighborNumber() const;
   POINT GetNeighborCoord(int) const;
   bool ToBelong(int, int); // ����������� �� ��� �������� ���������� ������
   void SetPoint(const int&); // ���������� ���������� ����� �� ������� ������� ������
   void Paint() const;
};

} // namespace nsFigure

#endif // FILE_TCPENTAGONT10
