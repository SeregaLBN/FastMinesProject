////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "TcParquet1.h"
//
// Описание класса TcParquet1 - паркет в елку (herring-bone parquet)
////////////////////////////////////////////////////////////////////////////////

#ifndef FILE_TCPARQUET1
#define FILE_TCPARQUET1

#include ".\TcBase.h"

//////////////////////////////////
//        /\      /\      / \      -   вид поля
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
   static float GetPercentMine(TeSkillLevel); // процент мин на заданном уровне сложности
private:
   POINT neighbor[6]; // кординаты соседей; если соседа нет, то координаты отрицательны
   POINT regionOut[4]; // координаты 4x точек из которых состоит фигура
   bool direction;
public:
   TcParquet1(const POINT&, const POINT&, const int&);
   int   GetNeighborNumber() const;
   POINT GetNeighborCoord(int) const;
   bool ToBelong(int, int); // принадлежат ли эти экранные координаты ячейке
   void SetPoint(const int&); // определить координаты точек из которых состоит фигура
   void Paint() const;
};

} // namespace nsFigure

#endif // FILE_TCPARQUET1
