////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "TcTriangle2.h"
//
// ќписание класса TcTriangle2 - равносторонний треугольник (вариант пол€ є2 - Єлочкой)
////////////////////////////////////////////////////////////////////////////////

#ifndef FILE_TCTRIANGLE2
#define FILE_TCTRIANGLE2

#include ".\TcBase.h"

//////////////////////////////////
//     ________________________
//    /\    /\    /\    /\    /
// 0 /  \  /  \  /  \  /  \  /     -   вид пол€, т.е.
//  /____\/____\/____\/____\/__       ____
//    /\    /\    /\    /\    /       \  /  -  нечЄтные: 1==(coord.x % 2)
// 1 /  \  /  \  /  \  /  \  /         \/
//  /____\/____\/____\/____\/__ 
//    /\    /\    /\    /\    /        /\   -  чЄтные  : 0==(coord.x % 2)
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
//   / 1  \  /this\  / 4  \  this->direction == false // направление вверх
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
//   \ 3  /  \this/  \ 6  /  this->direction == true // направление вниз
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
   static float GetPercentMine(TeSkillLevel); // процент мин на заданном уровне сложности
private:
   POINT neighbor[8]; // кординаты соседей; если соседа нет, то координаты отрицательны
   POINTFLOAT regionOut[3]; // координаты 3x точек из которых состоит треугольник
   int direction; // 0..1
public:
   TcTriangle2(const POINT&, const POINT&, const int&);
   int   GetNeighborNumber() const;
   POINT GetNeighborCoord(int) const;
   bool ToBelong(int, int); // принадлежат ли эти экранные координаты €чейке
   void SetPoint(const int&); // определить координаты точек из которых состоит фигура
   void Paint() const;
};

} // namespace nsFigure

#endif // FILE_TCTRIANGLE2
