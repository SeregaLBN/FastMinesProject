////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "TcHexagon.h"
//
// �������� ������ TcHexagon - ���������� 6-�� �������� (����)
////////////////////////////////////////////////////////////////////////////////

#ifndef FILE_TCHEXAGON
#define FILE_TCHEXAGON

#include ".\TcBase.h"

//////////////////////////////////
//      
//       / \       / \       / \       -   ��� ����
//    /       \ /       \ /       \
//   |         |         |         |
// 0 |         |         |         |
//   |         |         |         |
//    \       / \       / \       / \
//       \ /       \ /       \ /       \
//        |         |         |         |
// 1      |         |         |         |
//        |         |         |         |
//       / \       / \       / \       /
//    /       \ /       \ /       \ / 
//   |         |         |         |
// 2 |         |         |         |
//   |         |         |         |
//    \       / \       / \       /
//       \ /       \ /       \ /
//           0         1         2    
//////////////////////////////////
//        0
//       / \        POINTFLOAT regionOut[6]
//  5 /       \ 1
//   |         |
//   |         |
//   |         |
//  4 \       / 2
//       \ / 
//        3
//////////////////////////////////
//      
//           / \       / \       POINT neighbor[6]
//        /       \ /       \
//       |         |         |
//       |    0    |    1    |
//       |         |         |
//      / \       / \       / \
//   /       \ /       \ /       \
//  |         |         |         |
//  |    2    |  this   |    3    |
//  |         |         |         |
//   \       / \       / \       /
//      \ /       \ /       \ / 
//       |         |         |
//       |    4    |    5    |
//       |         |         |
//        \       / \       /
//           \ /       \ /
//
//////////////////////////////////

namespace nsFigure {

class TcHexagon: public TcBase{
private:
   static float b; // ������ ��������������
   static float h; // ������ ��������������
public:
   static POINT GetSizeFieldInPixel(const POINT&, const int&);
   static int SizeInscribedSquare(int);
   static float GetPercentMine(TeSkillLevel skill); // ������� ��� �� �������� ������ ���������
private:
   POINT neighbor[6]; // ��������� �������; ���� ������ ���, �� ���������� ������������
   POINTFLOAT regionOut[6]; // ���������� 6�� ����� �� ������� ������� ������
   bool direction;
public:
   TcHexagon(const POINT&, const POINT&, const int&);
   int   GetNeighborNumber() const;
   POINT GetNeighborCoord(int) const;
   bool ToBelong(int, int); // ����������� �� ��� �������� ���������� ������
   void SetPoint(const int&); // ���������� ���������� ����� �� ������� ������� ������
   void Paint() const;
};

} // namespace nsFigure

#endif // FILE_TCHEXAGON
