////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "TcTrapezoid1.h"
//
// �������� ������ TcTrapezoid1
////////////////////////////////////////////////////////////////////////////////

#ifndef FILE_TCTRAPEZOID1
#define FILE_TCTRAPEZOID1

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

class TcTrapezoid1: public TB{
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
   POINT neighbor[8]; // ��������� �������; ���� ������ ���, �� ���������� ������������
   POINTFLOAT regionOut[4]; // ���������� 4x ����� �� ������� ������� ������
   int direction; // 0..11
public:
   TcTrapezoid1(const POINT&, const POINT&, const int&);
   int   GetNeighborNumber() const;
   POINT GetNeighborCoord(int) const;
   bool ToBelong(int, int); // ����������� �� ��� �������� ���������� ������
   void SetPoint(const int&); // ���������� ���������� ����� �� ������� ������� ������
   void Paint() const;
};

} // namespace nsFigure

#endif // FILE_TCTRAPEZOID1
