////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "TcTrSq2.h"
//
// Описание класса TcTrSq2 - мозаика из 24х треугольников и 12х квадратов (на 1 квадрат приходится 2 треугольника)
////////////////////////////////////////////////////////////////////////////////

#ifndef FILE_TCTRSQ2
#define FILE_TCTRSQ2

#include ".\TcBase.h"

//////////////////////////////////
// Поле (мозаика) состоит из квадратов и правильных треугольников.
// S = (2*S_triangle + S_square)/3 =
//   = (a^2*3^0.5/2 + a^2)/3 = a^2*(3^0.5+2)/6      -   площадь
// a = (s*6/(3^0.5+2))^0.5                          -   сторона квадратa и треугольникa
// b = a/2
// h = a*3^0.5/2                                    -   высота треугольникa
// sq = a*3^0.5/(3^0.5+2)                           -   размер квадрата, вписанного в треугольник
//////////////////////////////////

namespace nsFigure {

class TcTrSq2: public TB{
private:
   static float b;
   static float h;
public:
   static POINT GetSizeFieldInPixel(const POINT&, const int&);
   static int SizeInscribedSquare(int);
   static float GetPercentMine(TeSkillLevel); // процент мин на заданном уровне сложности
private:
   POINT neighbor[12]; // кординаты соседей; если соседа нет, то координаты отрицательны
   POINTFLOAT regionOut[4]; // координаты 4x точек из которых состоит фигура
   int direction;
public:
   TcTrSq2(const POINT&, const POINT&, const int&);
   int   GetNeighborNumber() const;
   POINT GetNeighborCoord(int) const;
   bool ToBelong(int, int); // принадлежат ли эти экранные координаты ячейке
   void SetPoint(const int&); // определить координаты точек из которых состоит фигура
   void Paint() const;
};

} // namespace nsFigure

#endif // FILE_TCTRSQ2
