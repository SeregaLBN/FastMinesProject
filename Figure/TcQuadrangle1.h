////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "TcQuadrangle1.h"
//
// �������� ������ TcQuadrangle1 - �������������� 120�-90�-60�-90�
////////////////////////////////////////////////////////////////////////////////

#ifndef FILE_TCQUADRANGLE1
#define FILE_TCQUADRANGLE1

#include ".\TcBase.h"

//////////////////////////////////

namespace nsFigure {

class TcQuadrangle1: public TB{
private:
   static float b;
   static float h;
   static float n;
   static float m;
   static float R;
   static float t;
public:
   static POINT GetSizeFieldInPixel(const POINT&, const int&);
   static int SizeInscribedSquare(int);
   static float GetPercentMine(TeSkillLevel); // ������� ��� �� �������� ������ ���������
private:
   POINT neighbor[9]; // ��������� �������; ���� ������ ���, �� ���������� ������������
   POINTFLOAT regionOut[4]; // ���������� 4x ����� �� ������� ������� ������
   int direction; // 0..11
public:
   TcQuadrangle1(const POINT&, const POINT&, const int&);
   int   GetNeighborNumber() const;
   POINT GetNeighborCoord(int) const;
   bool ToBelong(int, int); // ����������� �� ��� �������� ���������� ������
   void SetPoint(const int&); // ���������� ���������� ����� �� ������� ������� ������
   void Paint() const;
};

} // namespace nsFigure

#endif // FILE_TCQUADRANGLE1
