////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "TcTriangle2.h"
//
// �������� ������ TcTriangle2 - �������������� ����������� (������� ���� �2 - �������)
////////////////////////////////////////////////////////////////////////////////

#ifndef FILE_TCTRIANGLE2
#define FILE_TCTRIANGLE2

#include ".\TcBase.h"

//////////////////////////////////
//     ________________________
//    /\    /\    /\    /\    /
// 0 /  \  /  \  /  \  /  \  /     -   ��� ����, �.�.
//  /____\/____\/____\/____\/__       ____
//    /\    /\    /\    /\    /       \  /  -  ��������: 1==(coord.x % 2)
// 1 /  \  /  \  /  \  /  \  /         \/
//  /____\/____\/____\/____\/__ 
//    /\    /\    /\    /\    /        /\   -  ������  : 0==(coord.x % 2)
// 2 /  \  /  \  /  \  /  \  /        /__\
//  /____\/____\/____\/____\/__  
//    /\    /\    /\    /\    /
// 3 /  \  /  \  /  \  /  \  /
//  /____\/____\/____\/____\/__  
//    /\    /\    /\    /\    /
// 4 /  \  /  \  /  \  /  \  /
//  /____\/____\/____\/____\/__
//    /\    /\    /\    /\     /
// 5 /  \  /  \  /  \  /  \   /
//  /____\/____\/____\/____\ /
//    0  1  2  3  4  5  6  7
//////////////////////////////////
//    0        0     1
//    *        *-----*    POINT regionOut[3] 
//   / \        \   /
//  /   \   or   \ /
// *-----*        *
// 2     1        2
//////////////////////////////////
// 
//             /\ 
//            /  \ 
//           / 0  \        POINT neighbor[8]
//       ___/______\____
//     /\      /\      /\
//    /  \ 2  /  \ 3  /  \
//   / 1  \  /this\  / 4  \  this->direction == false // ����������� �����
//  /______\/______\/______\
//      \      /\      /
//       \ 5  /  \ 7  /
//        \  / 6  \  /
//         \/______\/
//
//             or
//          ________
//         /\      /\
//        /  \ 1  /  \
//       / 0  \  / 2  \
//  ____/______\/______\____
//  \      /\      /\      /
//   \ 3  /  \this/  \ 6  /  this->direction == true // ����������� ����
//    \  / 4  \  / 5  \  /
//     \/______\/______\/
//          \      /
//           \ 7  /
//            \  /
//             \/
//
//////////////////////////////////

namespace nsFigure {

class TcTriangle2: public TB{
private:
   static float h;
   static float b;
public:
   static POINT GetSizeFieldInPixel(const POINT&, const int&);
   static int SizeInscribedSquare(int);
   static float GetPercentMine(TeSkillLevel); // ������� ��� �� �������� ������ ���������
private:
   POINT neighbor[8]; // ��������� �������; ���� ������ ���, �� ���������� ������������
   POINTFLOAT regionOut[3]; // ���������� 3x ����� �� ������� ������� �����������
   int direction; // 0..1
public:
   TcTriangle2(const POINT&, const POINT&, const int&);
   int   GetNeighborNumber() const;
   POINT GetNeighborCoord(int) const;
   bool ToBelong(int, int); // ����������� �� ��� �������� ���������� ������
   void SetPoint(const int&); // ���������� ���������� ����� �� ������� ������� ������
   void Paint() const;
};

} // namespace nsFigure

#endif // FILE_TCTRIANGLE2
