////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "TcTriangle3.h"
//
// �������� ������ TcTriangle3 - ����������� 45�-90�-45�
////////////////////////////////////////////////////////////////////////////////

#ifndef FILE_TCTRIANGLE3
#define FILE_TCTRIANGLE3

#include ".\TcBase.h"

//////////////////////////////////

namespace nsFigure {

class TcTriangle3: public TB{
private:
   static float b;  // -   �������� ������� ������� a (b=a/2)
public:
   static POINT GetSizeFieldInPixel(const POINT&, const int&);
   static int SizeInscribedSquare(int);
   static float GetPercentMine(TeSkillLevel); // ������� ��� �� �������� ������ ���������
private:
   POINT neighbor[14]; // ��������� �������; ���� ������ ���, �� ���������� ������������
   POINTFLOAT regionOut[3]; // ���������� 3x ����� �� ������� ������� ������
   int direction; // 0..3
public:
   TcTriangle3(const POINT&, const POINT&, const int&);
   int   GetNeighborNumber() const;
   POINT GetNeighborCoord(int) const;
   bool ToBelong(int, int); // ����������� �� ��� �������� ���������� ������
   void SetPoint(const int&); // ���������� ���������� ����� �� ������� ������� ������
   void Paint() const;
};

} // namespace nsFigure

#endif // FILE_TCTRIANGLE3
