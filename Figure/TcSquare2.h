////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "TcSquare2.h"
//
// �������� ������ TcSquare2 - ������� (������������ ������� ����)
////////////////////////////////////////////////////////////////////////////////

#ifndef FILE_TCSQUARE2
#define FILE_TCSQUARE2

#include ".\TcBase.h"

//////////////////////////////////
//    _____
//   |_|_|_|  -   ��� ����
//  |_|_|_|
//   |_|_|_|
//
//////////////////////////////////
//  0
//  *----*  POINT regionOut[2] 
//  |    |
//  |    |
//  *----*
//       1
//////////////////////////////////
//      ___________
//     |     |     |     POINT neighbor[6]
//     |  0  |  1  |
//   __|_____|_____|__
//  |     |     |     |
//  |  2  |this |  3  |
//  |_____|_____|_____|
//     |     |     |
//     |  4  |  5  |
//     |_____|_____|
//
//////////////////////////////////

namespace nsFigure {

class TcSquare2: public TB{
public:
   static POINT GetSizeFieldInPixel(const POINT&, const int&);
   static int SizeInscribedSquare(int);
   static float GetPercentMine(TeSkillLevel); // ������� ��� �� �������� ������ ���������
private:
   POINT neighbor[6]; // ��������� �������; ���� ������ ���, �� ���������� ������������
   POINT regionOut[2]; // ���������� 2x ����� �� ������� ������� �������
public:
   TcSquare2(const POINT&, const POINT&, const int&);
   int   GetNeighborNumber() const;
   POINT GetNeighborCoord(int) const;
   bool ToBelong(int, int); // ����������� �� ��� �������� ���������� ������
   void SetPoint(const int&); // ���������� ���������� ����� �� ������� ������� ������
   void Paint() const;
};

} // namespace nsFigure

#endif // FILE_TCSQUARE2
