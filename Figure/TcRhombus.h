////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "TcRhombus.h"
//
// �������� ������ TcRhombus
////////////////////////////////////////////////////////////////////////////////

#ifndef FILE_TCRHOMBUS
#define FILE_TCRHOMBUS

#include ".\TcBase.h"

//////////////////////////////////
//
// ���� (�������) ������� �� ���������� ������.
// ���� ����� �������� ���� ����������� 2� �������������� �������������.
// 3 ����� ��������� ����������� ������� ���������� ���������� 6����������.
// �� ����� �������:
// S����� = 2*S������������ = a^2*3^0.5/2      -   ������� �����
// a = (S*2/3^0.5)^0.5                         -   ������� �����
// r = a*3^0.5/2                               -   ������ ���������� � 6���������� �����
// h = r*2                                     -   ������ 6����������� (������� ��������� �����)
// c = a/2
// sq = (3/2)^0.5*a/2/sin75                    -   ������ ��������, ���������� � ����
//////////////////////////////////

namespace nsFigure {

class TcRhombus: public TB{
private:
   static float r;
   static float h;
   static float c;
public:
   static POINT GetSizeFieldInPixel(const POINT&, const int&);
   static int SizeInscribedSquare(int);
   static float GetPercentMine(TeSkillLevel); // ������� ��� �� �������� ������ ���������
private:
   POINT neighbor[10]; // ��������� �������; ���� ������ ���, �� ���������� ������������
   POINTFLOAT regionOut[4]; // ���������� 4x ����� �� ������� ������� ������
   int direction; // 0..5
public:
   TcRhombus(const POINT&, const POINT&, const int&);
   int   GetNeighborNumber() const;
   POINT GetNeighborCoord(int) const;
   bool ToBelong(int, int); // ����������� �� ��� �������� ���������� ������
   void SetPoint(const int&); // ���������� ���������� ����� �� ������� ������� ������
   void Paint() const;
};

} // namespace nsFigure

#endif // FILE_TCRHOMBUS
