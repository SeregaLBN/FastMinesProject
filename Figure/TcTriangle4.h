////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "TcTriangle4.h"
//
// ќписание класса TcTriangle4 - треугольник 30∞-30∞-120∞
////////////////////////////////////////////////////////////////////////////////

#ifndef FILE_TCTRIANGLE4
#define FILE_TCTRIANGLE4

#include ".\TcBase.h"

//////////////////////////////////

namespace nsFigure {

class TcTriangle4: public TB{
private:
   static float b;  // -   половина большей стороны a (b=a/2)
   static float R;  // -   втора€ (треть€) сторона
   static float r;  // -   высота
public:
   static POINT GetSizeFieldInPixel(const POINT&, const int&);
   static int SizeInscribedSquare(int);
   static float GetPercentMine(TeSkillLevel); // процент мин на заданном уровне сложности
private:
   POINT neighbor[21]; // кординаты соседей; если соседа нет, то координаты отрицательны
   POINTFLOAT regionOut[3]; // координаты 3x точек из которых состоит фигура
   int direction; // 0..11
public:
   TcTriangle4(const POINT&, const POINT&, const int&);
   int   GetNeighborNumber() const;
   POINT GetNeighborCoord(int) const;
   bool ToBelong(int, int); // принадлежат ли эти экранные координаты €чейке
   void SetPoint(const int&); // определить координаты точек из которых состоит фигура
   void Paint() const;
};

} // namespace nsFigure

#endif // FILE_TCTRIANGLE4
