////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "TcTrapezoid2.h"
//
// Описание класса TcTrapezoid2
////////////////////////////////////////////////////////////////////////////////

#ifndef FILE_TCTRAPEZOID2
#define FILE_TCTRAPEZOID2

#include ".\TcBase.h"

//////////////////////////////////
//
// Поле (мозаика) состоит из одинаковых трапеций.
// Трапеция равносторонняя; верх равен сторонам; основание в 2 раза больше верха.
// Т.е. трапецию можно получить путём объединения 3х равносторонних треугольников.
// 3 трапеции сложенные определённым образом составляют равносторонний треугольник.
// Из этого следует:
// Sтрапеции = 3*Sтреугольника = 3*a^2*3^0.5/4      -   площадь
// a = (S/27^0.5)^0.5*2                             -   меньшая сторона трапеции (верх и стороны)
// b = a*2                                          -   большая сторона трапеции (основание)
// c = a/2
// R = a*3^0.5                                      -   диагональ трапеции
// r = R/2 = a/2*3^0.5                              -   высота трапеции
// sq = (3/2)^0.5*a/2/sin75 = a*(3/8)^0.5/sin75     -   размер квадрата, вписанного в трапецию
//////////////////////////////////

namespace nsFigure {

class TcTrapezoid2: public TB{
private:
   static float b;  // -   большая сторона трапеции (основание)
   static float c;  //
   static float R;  // -   диагональ трапеции
   static float r;  // -   высота трапеции
public:
   static POINT GetSizeFieldInPixel(const POINT&, const int&);
   static int SizeInscribedSquare(int);
   static float GetPercentMine(TeSkillLevel); // процент мин на заданном уровне сложности
private:
   POINT neighbor[9]; // кординаты соседей; если соседа нет, то координаты отрицательны
   POINTFLOAT regionOut[4]; // координаты 4x точек из которых состоит фигура
   int direction; // 0..11
public:
   TcTrapezoid2(const POINT&, const POINT&, const int&);
   int   GetNeighborNumber() const;
   POINT GetNeighborCoord(int) const;
   bool ToBelong(int, int); // принадлежат ли эти экранные координаты ячейке
   void SetPoint(const int&); // определить координаты точек из которых состоит фигура
   void Paint() const;
};

} // namespace nsFigure

#endif // FILE_TCTRAPEZOID2
