////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "TcTriangle1.h"
//
// ќписание класса TcTriangle1 - равносторонний треугольник (вариант пол€ є1)
////////////////////////////////////////////////////////////////////////////////

#ifndef FILE_TCTRIANGLE1
#define FILE_TCTRIANGLE1

#include ".\TcBase.h"

//////////////////////////////////
//     ________________________
//    /\    /\    /\    /\    /
// 0 /  \  /  \  /  \  /  \  /     -   вид пол€, т.е.
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
//   / 3  \  /this\  / 6  \  this->direction == false // направление вверх
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
//   \ 5  /  \this/  \ 8  /  this->direction == true // направление вниз
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
   static float GetPercentMine(TeSkillLevel); // процент мин на заданном уровне сложности
private:
   POINT neighbor[12]; // кординаты соседей; если соседа нет, то координаты отрицательны
   POINTFLOAT regionOut[3]; // координаты 3x точек из которых состоит треугольник
   int direction; // 0..3
public:
   TcTriangle1(const POINT&, const POINT&, const int&);
   int   GetNeighborNumber() const;
   POINT GetNeighborCoord(int) const;
   bool ToBelong(int, int); // принадлежат ли эти экранные координаты €чейке
   void SetPoint(const int&); // определить координаты точек из которых состоит фигура
   void Paint() const;
};

} // namespace nsFigure

#endif // FILE_TCTRIANGLE1
