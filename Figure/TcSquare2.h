////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "TcSquare2.h"
//
// Описание класса TcSquare2 - квадрат (перекошенный вариант поля)
////////////////////////////////////////////////////////////////////////////////

#ifndef FILE_TCSQUARE2
#define FILE_TCSQUARE2

#include ".\TcBase.h"

//////////////////////////////////
//    _____
//   |_|_|_|  -   вид поля
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
   static float GetPercentMine(TeSkillLevel); // процент мин на заданном уровне сложности
private:
   POINT neighbor[6]; // кординаты соседей; если соседа нет, то координаты отрицательны
   POINT regionOut[2]; // координаты 2x точек из которых состоит квадрат
public:
   TcSquare2(const POINT&, const POINT&, const int&);
   int   GetNeighborNumber() const;
   POINT GetNeighborCoord(int) const;
   bool ToBelong(int, int); // принадлежат ли эти экранные координаты ячейке
   void SetPoint(const int&); // определить координаты точек из которых состоит фигура
   void Paint() const;
};

} // namespace nsFigure

#endif // FILE_TCSQUARE2
