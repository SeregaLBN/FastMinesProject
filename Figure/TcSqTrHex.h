////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "TcSqTrHex.h"
//
// �������� ������ TcSqTrHex - 6Square 4Triangle 2Hexagon
////////////////////////////////////////////////////////////////////////////////

#ifndef FILE_TCSQTRHEX
#define FILE_TCSQTRHEX

#include ".\TcBase.h"

//////////////////////////////////
//
// ���� (�������) ������� �� ���������, ���������� ������������� � ���������� 6������������.
//
// S = (6*Ssquare+4*Striangle+2*Shexagon)/12 =
// = (6*a^2+4*a^2*3^0.5/4+2*a^2*3*3^0.5/2)/12 =
// = (6*a^2+a^2*3^0.5+a^2*3*3^0.5)/12 =
// = a^2*(6+4*3^0.5)/12 = a^2/2+a^2/3^0.5      -   �������
// a = (S/(0.5+1/3^0.5))^0.5                   -   ������� ��������, ������������ � 6�����������
// h = a*3^0.5/2                               -   ������ ������������
// sq = h*2^0.5/3 = a*(8.0/27)^0.5             -   ������ ��������, ���������� � ����������� (������� ������ � ����)
//////////////////////////////////

namespace nsFigure {

class TcSqTrHex: public TB{
private:
   static float h;  // -   ������ ������������
public:
   static POINT GetSizeFieldInPixel(const POINT&, const int&);
   static int SizeInscribedSquare(int);
   static float GetPercentMine(TeSkillLevel); // ������� ��� �� �������� ������ ���������
private:
   POINT neighbor[12]; // ��������� �������; ���� ������ ���, �� ���������� ������������
   POINTFLOAT regionOut[6]; // ���������� 6�� ����� �� ������� ������� ������
   int direction; // 0..11
public:
   TcSqTrHex(const POINT&, const POINT&, const int&);
   int   GetNeighborNumber() const;
   POINT GetNeighborCoord(int) const;
   bool ToBelong(int, int); // ����������� �� ��� �������� ���������� ������
   void SetPoint(const int&); // ���������� ���������� ����� �� ������� ������� ������
   void Paint() const;
};

} // namespace nsFigure

#endif // FILE_TCSQTRHEX
