////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "TcPentagonT5.h"
//
// �������� ������ TcPentagonT5 - 5-�� ��������, ��� �5
////////////////////////////////////////////////////////////////////////////////

#ifndef FILE_TCPENTAGONT5
#define FILE_TCPENTAGONT5

#include ".\TcBase.h"

//////////////////////////////////

namespace nsFigure {

class TcPentagonT5: public TB{
private:
   static float h;
public:
   static POINT GetSizeFieldInPixel(const POINT&, const int&);
   static int SizeInscribedSquare(int);
   static float GetPercentMine(TeSkillLevel); // ������� ��� �� �������� ������ ���������
private:
   POINT neighbor[8]; // ��������� �������; ���� ������ ���, �� ���������� ������������
   POINTFLOAT regionOut[5]; // ���������� 5�� ����� �� ������� ������� ������
   int direction; // 0..83
public:
   TcPentagonT5(const POINT&, const POINT&, const int&);
   int   GetNeighborNumber() const;
   POINT GetNeighborCoord(int) const;
   bool ToBelong(int, int); // ����������� �� ��� �������� ���������� ������
   void SetPoint(const int&); // ���������� ���������� ����� �� ������� ������� ������
   void Paint() const;
};

} // namespace nsFigure

#endif // FILE_TCPENTAGONT5
