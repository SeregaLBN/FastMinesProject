////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "TcHexagon.h"
//
// Описание класса TcHexagon - правильный 6-ти угольник (сота)
////////////////////////////////////////////////////////////////////////////////

#ifndef FILE_TCHEXAGON
#define FILE_TCHEXAGON

#include ".\TcBase.h"

//////////////////////////////////
//      
//       / \       / \       / \       -   вид поля
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
   static float b; // ширина шестиугольника
   static float h; // высота шестиугольника
public:
   static POINT GetSizeFieldInPixel(const POINT&, const int&);
   static int SizeInscribedSquare(int);
   static float GetPercentMine(TeSkillLevel skill); // процент мин на заданном уровне сложности
private:
   POINT neighbor[6]; // кординаты соседей; если соседа нет, то координаты отрицательны
   POINTFLOAT regionOut[6]; // координаты 6ти точек из которых состоит фигура
   bool direction;
public:
   TcHexagon(const POINT&, const POINT&, const int&);
   int   GetNeighborNumber() const;
   POINT GetNeighborCoord(int) const;
   bool ToBelong(int, int); // принадлежат ли эти экранные координаты ячейке
   void SetPoint(const int&); // определить координаты точек из которых состоит фигура
   void Paint() const;
};

} // namespace nsFigure

#endif // FILE_TCHEXAGON
