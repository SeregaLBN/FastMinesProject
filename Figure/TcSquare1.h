////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "TcSquare1.h"
//
// �������� ������ TcSquare1 - ������� (������������ ������� ����)
////////////////////////////////////////////////////////////////////////////////

#ifndef FILE_TCSQUARE1
#define FILE_TCSQUARE1

#include ".\TcBase.h"

//////////////////////////////////
//   _____
//  |_|_|_|   -   ��� ����
//  |_|_|_|
//  |_|_|_|
//
//////////////////////////////////
//  0
//  *----*  POINT regionOut[2] 
//  |    |
//  |    |
//  *----*
//       1
//////////////////////////////////
//   _________________
//  |     |     |     |  POINT neighbor[8]
//  |  0  |  1  |  2  |
//  |_____|_____|_____|
//  |     |     |     |
//  |  3  |this |  4  |
//  |_____|_____|_____|
//  |     |     |     |
//  |  5  |  6  |  7  |
//  |_____|_____|_____|
//
//////////////////////////////////

namespace nsFigure {

class TcSquare1: public TB{
public:
   static POINT GetSizeFieldInPixel(const POINT&, const int&);
   static int SizeInscribedSquare(int);
   static float GetPercentMine(TeSkillLevel); // ������� ��� �� �������� ������ ���������
private:
   POINT neighbor[8]; // ��������� �������; ���� ������ ���, �� ���������� ������������
   POINT regionOut[2]; // ���������� 2x ����� �� ������� ������� �������
public:
   TcSquare1(const POINT&, const POINT&, const int&);
   int   GetNeighborNumber() const;
   POINT GetNeighborCoord(int) const;
   bool ToBelong(int, int); // ����������� �� ��� �������� ���������� ������
   void SetPoint(const int&); // ���������� ���������� ����� �� ������� ������� ������
   void Paint() const;
};

} // namespace nsFigure

#endif // FILE_TCSQUARE1
