////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "TcTrapezoid2.h"
//
// �������� ������ TcTrapezoid2
////////////////////////////////////////////////////////////////////////////////

#ifndef FILE_TCTRAPEZOID2
#define FILE_TCTRAPEZOID2

#include ".\TcBase.h"

//////////////////////////////////
//
// ���� (�������) ������� �� ���������� ��������.
// �������� ��������������; ���� ����� ��������; ��������� � 2 ���� ������ �����.
// �.�. �������� ����� �������� ���� ����������� 3� �������������� �������������.
// 3 �������� ��������� ����������� ������� ���������� �������������� �����������.
// �� ����� �������:
// S�������� = 3*S������������ = 3*a^2*3^0.5/4      -   �������
// a = (S/27^0.5)^0.5*2                             -   ������� ������� �������� (���� � �������)
// b = a*2                                          -   ������� ������� �������� (���������)
// c = a/2
// R = a*3^0.5                                      -   ��������� ��������
// r = R/2 = a/2*3^0.5                              -   ������ ��������
// sq = (3/2)^0.5*a/2/sin75 = a*(3/8)^0.5/sin75     -   ������ ��������, ���������� � ��������
//////////////////////////////////

namespace nsFigure {

class TcTrapezoid2: public TB{
private:
   static float b;  // -   ������� ������� �������� (���������)
   static float c;  //
   static float R;  // -   ��������� ��������
   static float r;  // -   ������ ��������
public:
   static POINT GetSizeFieldInPixel(const POINT&, const int&);
   static int SizeInscribedSquare(int);
   static float GetPercentMine(TeSkillLevel); // ������� ��� �� �������� ������ ���������
private:
   POINT neighbor[9]; // ��������� �������; ���� ������ ���, �� ���������� ������������
   POINTFLOAT regionOut[4]; // ���������� 4x ����� �� ������� ������� ������
   int direction; // 0..11
public:
   TcTrapezoid2(const POINT&, const POINT&, const int&);
   int   GetNeighborNumber() const;
   POINT GetNeighborCoord(int) const;
   bool ToBelong(int, int); // ����������� �� ��� �������� ���������� ������
   void SetPoint(const int&); // ���������� ���������� ����� �� ������� ������� ������
   void Paint() const;
};

} // namespace nsFigure

#endif // FILE_TCTRAPEZOID2
