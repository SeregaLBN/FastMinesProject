////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "TcSquare1.h"
//
// Описание класса TcSquare1 - квадрат (классический вариант поля)
////////////////////////////////////////////////////////////////////////////////

#ifndef FILE_TCSQUARE1
#define FILE_TCSQUARE1

#include ".\TcBase.h"

//////////////////////////////////
//   _____
//  |_|_|_|   -   вид поля
//  |_|_|_|
//  |_|_|_|
//
//////////////////////////////////
//  0
//  *----*  POINT regionOut[2] 
//  |    |
//  |    |
//  *----*
//       1
//////////////////////////////////
//   _________________
//  |     |     |     |  POINT neighbor[8]
//  |  0  |  1  |  2  |
//  |_____|_____|_____|
//  |     |     |     |
//  |  3  |this |  4  |
//  |_____|_____|_____|
//  |     |     |     |
//  |  5  |  6  |  7  |
//  |_____|_____|_____|
//
//////////////////////////////////

namespace nsFigure {

class TcSquare1: public TB{
public:
   static POINT GetSizeFieldInPixel(const POINT&, const int&);
   static int SizeInscribedSquare(int);
   static float GetPercentMine(TeSkillLevel); // процент мин на заданном уровне сложности
private:
   POINT neighbor[8]; // кординаты соседей; если соседа нет, то координаты отрицательны
   POINT regionOut[2]; // координаты 2x точек из которых состоит квадрат
public:
   TcSquare1(const POINT&, const POINT&, const int&);
   int   GetNeighborNumber() const;
   POINT GetNeighborCoord(int) const;
   bool ToBelong(int, int); // принадлежат ли эти экранные координаты ячейке
   void SetPoint(const int&); // определить координаты точек из которых состоит фигура
   void Paint() const;
};

} // namespace nsFigure

#endif // FILE_TCSQUARE1
