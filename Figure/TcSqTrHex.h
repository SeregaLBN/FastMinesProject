////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "TcSqTrHex.h"
//
// Описание класса TcSqTrHex - 6Square 4Triangle 2Hexagon
////////////////////////////////////////////////////////////////////////////////

#ifndef FILE_TCSQTRHEX
#define FILE_TCSQTRHEX

#include ".\TcBase.h"

//////////////////////////////////
//
// Поле (мозаика) состоит из квадратов, правильных треугольников и правильных 6тиугольников.
//
// S = (6*Ssquare+4*Striangle+2*Shexagon)/12 =
// = (6*a^2+4*a^2*3^0.5/4+2*a^2*3*3^0.5/2)/12 =
// = (6*a^2+a^2*3^0.5+a^2*3*3^0.5)/12 =
// = a^2*(6+4*3^0.5)/12 = a^2/2+a^2/3^0.5      -   площадь
// a = (S/(0.5+1/3^0.5))^0.5                   -   сторона квадрата, треугольника и 6тиугольника
// h = a*3^0.5/2                               -   высота треугольника
// sq = h*2^0.5/3 = a*(8.0/27)^0.5             -   размер квадрата, вписанного в треугольник (квадрат вписан в круг)
//////////////////////////////////

namespace nsFigure {

class TcSqTrHex: public TB{
private:
   static float h;  // -   высота треугольника
public:
   static POINT GetSizeFieldInPixel(const POINT&, const int&);
   static int SizeInscribedSquare(int);
   static float GetPercentMine(TeSkillLevel); // процент мин на заданном уровне сложности
private:
   POINT neighbor[12]; // кординаты соседей; если соседа нет, то координаты отрицательны
   POINTFLOAT regionOut[6]; // координаты 6ти точек из которых состоит фигура
   int direction; // 0..11
public:
   TcSqTrHex(const POINT&, const POINT&, const int&);
   int   GetNeighborNumber() const;
   POINT GetNeighborCoord(int) const;
   bool ToBelong(int, int); // принадлежат ли эти экранные координаты ячейке
   void SetPoint(const int&); // определить координаты точек из которых состоит фигура
   void Paint() const;
};

} // namespace nsFigure

#endif // FILE_TCSQTRHEX
