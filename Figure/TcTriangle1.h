////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "TcTriangle1.h"
//
// �������� ������ TcTriangle1 - �������������� ����������� (������� ���� �1)
////////////////////////////////////////////////////////////////////////////////

#ifndef FILE_TCTRIANGLE1
#define FILE_TCTRIANGLE1

#include ".\TcBase.h"

//////////////////////////////////
//     ________________________
//    /\    /\    /\    /\    /
// 0 /  \  /  \  /  \  /  \  /     -   ��� ����, �.�.
//  /____\/____\/____\/____\/
//  \    /\    /\    /\    /\
// 1 \  /  \  /  \  /  \  /  \
//    \/____\/____\/____\/____\
//    /\    /\    /\    /\    / 
// 2 /  \  /  \  /  \  /  \  /
//  /____\/____\/____\/____\/
//  \    /\    /\    /\    /\ 
// 3 \  /  \  /  \  /  \  /  \ 
//    \/____\/____\/____\/____\
//    /\    /\    /\    /\    /
// 4 /  \  /  \  /  \  /  \  /
//  /____\/____\/____\/____\/
//  \    /\    /\    /\    /\
// 5 \  /  \  /  \  /  \  /  \
//    \/____\/____\/____\/____\   
//    0  1  2  3  4  5  6  7
//////////////////////////////////
//    0        0     1
//    *        *-----*    POINT regionOut[3] 
//   / \        \   /
//  /   \   or   \ /
// *-----*        *
// 2     1        2
//////////////////////////////////
//           ______
//         /\      /\
//        /  \ 1  /  \
//       / 0  \  / 2  \    POINT neighbor[12]
//      /______\/______\
//     /\      /\      /\
//    /  \ 4  /  \ 5  /  \
//   / 3  \  /this\  / 6  \  this->direction == false // ����������� �����
//  /______\/______\/______\
//  \      /\      /\      /
//   \ 7  /  \ 9  /  \ 11 /
//    \  / 8  \  / 10 \  /
//     \/______\/______\/
//
//             or
//      ________________
//     /\      /\      /\
//    /  \ 1  /  \ 3  /  \
//   / 0  \  / 2  \  / 4  \
//  /______\/______\/______\
//  \      /\      /\      /
//   \ 5  /  \this/  \ 8  /  this->direction == true // ����������� ����
//    \  / 6  \  / 7  \  /
//     \/______\/______\/
//      \      /\      /
//       \ 9  /  \ 12 /
//        \  / 10 \  /
//         \/______\/
//
//////////////////////////////////

namespace nsFigure {

class TcTriangle1: public TB{
private:
   static float h;
   static float b;
public:
   static POINT GetSizeFieldInPixel(const POINT&, const int&);
   static int SizeInscribedSquare(int);
   static float GetPercentMine(TeSkillLevel); // ������� ��� �� �������� ������ ���������
private:
   POINT neighbor[12]; // ��������� �������; ���� ������ ���, �� ���������� ������������
   POINTFLOAT regionOut[3]; // ���������� 3x ����� �� ������� ������� �����������
   int direction; // 0..3
public:
   TcTriangle1(const POINT&, const POINT&, const int&);
   int   GetNeighborNumber() const;
   POINT GetNeighborCoord(int) const;
   bool ToBelong(int, int); // ����������� �� ��� �������� ���������� ������
   void SetPoint(const int&); // ���������� ���������� ����� �� ������� ������� ������
   void Paint() const;
};

} // namespace nsFigure

#endif // FILE_TCTRIANGLE1
