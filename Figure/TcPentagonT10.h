////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "TcPentagonT10.h"
//
// Описание класса TcPentagonT10 - 5-ти угольник, тип №10
////////////////////////////////////////////////////////////////////////////////

#ifndef FILE_TCPENTAGONT10
#define FILE_TCPENTAGONT10

#include ".\TcBase.h"

//////////////////////////////////
//
// Поле (мозаика) состоит из одинаковых пятиугольников 10ого типа.
//
// S  = a^2*7     -   площадь пятиугольника
// a  = (S/7)^0.5 -   базовая величина фигуры
// sq = a*2       -   размер квадрата, вписанного в пятиугольник
//////////////////////////////////

namespace nsFigure {

class TcPentagonT10: public TB{
public:
   static POINT GetSizeFieldInPixel(const POINT&, const int&);
   static int SizeInscribedSquare(int);
   static float GetPercentMine(TeSkillLevel); // процент мин на заданном уровне сложности
private:
   POINT neighbor[7]; // кординаты соседей; если соседа нет, то координаты отрицательны
   POINTFLOAT regionOut[5]; // координаты 5ти точек из которых состоит фигура
   int direction; // 0..11
public:
   TcPentagonT10(const POINT&, const POINT&, const int&);
   int   GetNeighborNumber() const;
   POINT GetNeighborCoord(int) const;
   bool ToBelong(int, int); // принадлежат ли эти экранные координаты ячейке
   void SetPoint(const int&); // определить координаты точек из которых состоит фигура
   void Paint() const;
};

} // namespace nsFigure

#endif // FILE_TCPENTAGONT10
