////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "TcRhombus.h"
//
// Описание класса TcRhombus
////////////////////////////////////////////////////////////////////////////////

#ifndef FILE_TCRHOMBUS
#define FILE_TCRHOMBUS

#include ".\TcBase.h"

//////////////////////////////////
//
// Поле (мозаика) состоит из одинаковых ромбов.
// Ромб можно получить путём объединения 2х равносторонних треугольников.
// 3 ромба сложенные определённым образом составляют правильный 6тиугольник.
// Из этого следует:
// Sромба = 2*Sтреугольника = a^2*3^0.5/2      -   площадь ромба
// a = (S*2/3^0.5)^0.5                         -   сторона ромба
// r = a*3^0.5/2                               -   радиус вписанного в 6тиугольник круга
// h = r*2                                     -   высота 6тиугольника (больщая диагональ ромба)
// c = a/2
// sq = (3/2)^0.5*a/2/sin75                    -   размер квадрата, вписанного в ромб
//////////////////////////////////

namespace nsFigure {

class TcRhombus: public TB{
private:
   static float r;
   static float h;
   static float c;
public:
   static POINT GetSizeFieldInPixel(const POINT&, const int&);
   static int SizeInscribedSquare(int);
   static float GetPercentMine(TeSkillLevel); // процент мин на заданном уровне сложности
private:
   POINT neighbor[10]; // кординаты соседей; если соседа нет, то координаты отрицательны
   POINTFLOAT regionOut[4]; // координаты 4x точек из которых состоит фигура
   int direction; // 0..5
public:
   TcRhombus(const POINT&, const POINT&, const int&);
   int   GetNeighborNumber() const;
   POINT GetNeighborCoord(int) const;
   bool ToBelong(int, int); // принадлежат ли эти экранные координаты ячейке
   void SetPoint(const int&); // определить координаты точек из которых состоит фигура
   void Paint() const;
};

} // namespace nsFigure

#endif // FILE_TCRHOMBUS
