////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "TcParquet1.h"
//
// �������� ������ TcParquet1 - ������ � ���� (herring-bone parquet)
////////////////////////////////////////////////////////////////////////////////

#ifndef FILE_TCPARQUET1
#define FILE_TCPARQUET1

#include ".\TcBase.h"

//////////////////////////////////
//        /\      /\      / \      -   ��� ����
//    0 /   / \ /   / \ /   / \
//      \ / \   \ / \   \ / \   \
//    1 /   / \ /   / \ /   / \ /
//      \ / \   \ / \   \ / \   \
//    2 /   / \ /   / \ /   / \ /
//      \ / \   \ / \   \ / \   \
//    3 /   / \ /   / \ /   / \ /
//      \ / \   \ / \   \ / \   \
//    4 /   / \ /   / \ /   / \ /
//      \ / \   \ / \   \ / \   \
//    5 /   / \ /   / \ /   / \ /
//      \ / \   \ / \   \ / \   \
//            \ /     \ /     \ /
//        0   1   2   3   4   5
//////////////////////////////////
//                       POINT regionOut[4]
//               0
//              / \
//            /     \
//          /       / 1
//      3 /       /
//        \     /     this->direction == 0
//          \ /
//           2
//          0
//         / \ 
//       /     \      this->direction == 1
//     3 \       \
//         \       \ 1
//           \     /
//             \ /
//              2
//
//////////////////////////////////
//
//                / \
//              /  1  \      POINT neighbor[6]
//    / \     /       / \
//  /  0  \ /  x,   /  2  \
//  \       \  y-1/ \       \
//    \   x-1,\ /this \   x+1,\
//    / \ y-1 /       / \ y-1 /     this->direction == 0
//  /  3  \ /  x, y /  4  \ /
//  \       \     / \       \
//    \   x-1,\ /  5  \   x+1,\
//      \ y   /       / \ y   /
//        \ /  x,   /     \ /
//          \  y+1/
//            \ /
//
//            / \
//          /  0  \
//        / \       \     / \
//      /  1  \   x,  \ /  2  \
//    /       / \ y-1 /       /    
//  /  x-1, / this\ /  x+1, /
//  \  y  / \       \  y  / \       this->direction == 1
//    \ /  3  \  x, y \ /  5  \
//    /       / \     /       /
//  /  x-1, /  4  \ /  x+1, /
//  \  y+1/ \       \  y+1/
//    \ /     \   x,  \ /
//              \ y+1 /
//                \ /
//
//
////////////////////////////////////

namespace nsFigure {

class TcParquet1: public TB{
public:
   static POINT GetSizeFieldInPixel(const POINT&, const int&);
   static int SizeInscribedSquare(int);
   static float GetPercentMine(TeSkillLevel); // ������� ��� �� �������� ������ ���������
private:
   POINT neighbor[6]; // ��������� �������; ���� ������ ���, �� ���������� ������������
   POINT regionOut[4]; // ���������� 4x ����� �� ������� ������� ������
   bool direction;
public:
   TcParquet1(const POINT&, const POINT&, const int&);
   int   GetNeighborNumber() const;
   POINT GetNeighborCoord(int) const;
   bool ToBelong(int, int); // ����������� �� ��� �������� ���������� ������
   void SetPoint(const int&); // ���������� ���������� ����� �� ������� ������� ������
   void Paint() const;
};

} // namespace nsFigure

#endif // FILE_TCPARQUET1
