////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "TcTriangle4.h"
//
// �������� ������ TcTriangle4 - ����������� 30�-30�-120�
////////////////////////////////////////////////////////////////////////////////

#ifndef FILE_TCTRIANGLE4
#define FILE_TCTRIANGLE4

#include ".\TcBase.h"

//////////////////////////////////

namespace nsFigure {

class TcTriangle4: public TB{
private:
   static float b;  // -   �������� ������� ������� a (b=a/2)
   static float R;  // -   ������ (������) �������
   static float r;  // -   ������
public:
   static POINT GetSizeFieldInPixel(const POINT&, const int&);
   static int SizeInscribedSquare(int);
   static float GetPercentMine(TeSkillLevel); // ������� ��� �� �������� ������ ���������
private:
   POINT neighbor[21]; // ��������� �������; ���� ������ ���, �� ���������� ������������
   POINTFLOAT regionOut[3]; // ���������� 3x ����� �� ������� ������� ������
   int direction; // 0..11
public:
   TcTriangle4(const POINT&, const POINT&, const int&);
   int   GetNeighborNumber() const;
   POINT GetNeighborCoord(int) const;
   bool ToBelong(int, int); // ����������� �� ��� �������� ���������� ������
   void SetPoint(const int&); // ���������� ���������� ����� �� ������� ������� ������
   void Paint() const;
};

} // namespace nsFigure

#endif // FILE_TCTRIANGLE4
