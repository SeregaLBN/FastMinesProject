////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "TcTrSq2.h"
//
// �������� ������ TcTrSq2 - ������� �� 24� ������������� � 12� ��������� (�� 1 ������� ���������� 2 ������������)
////////////////////////////////////////////////////////////////////////////////

#ifndef FILE_TCTRSQ2
#define FILE_TCTRSQ2

#include ".\TcBase.h"

//////////////////////////////////
// ���� (�������) ������� �� ��������� � ���������� �������������.
// S = (2*S_triangle + S_square)/3 =
//   = (a^2*3^0.5/2 + a^2)/3 = a^2*(3^0.5+2)/6      -   �������
// a = (s*6/(3^0.5+2))^0.5                          -   ������� �������a � �����������a
// b = a/2
// h = a*3^0.5/2                                    -   ������ �����������a
// sq = a*3^0.5/(3^0.5+2)                           -   ������ ��������, ���������� � �����������
//////////////////////////////////

namespace nsFigure {

class TcTrSq2: public TB{
private:
   static float b;
   static float h;
public:
   static POINT GetSizeFieldInPixel(const POINT&, const int&);
   static int SizeInscribedSquare(int);
   static float GetPercentMine(TeSkillLevel); // ������� ��� �� �������� ������ ���������
private:
   POINT neighbor[12]; // ��������� �������; ���� ������ ���, �� ���������� ������������
   POINTFLOAT regionOut[4]; // ���������� 4x ����� �� ������� ������� ������
   int direction;
public:
   TcTrSq2(const POINT&, const POINT&, const int&);
   int   GetNeighborNumber() const;
   POINT GetNeighborCoord(int) const;
   bool ToBelong(int, int); // ����������� �� ��� �������� ���������� ������
   void SetPoint(const int&); // ���������� ���������� ����� �� ������� ������� ������
   void Paint() const;
};

} // namespace nsFigure

#endif // FILE_TCTRSQ2
