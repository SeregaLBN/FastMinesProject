////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "TcTriangle3.h"
//
// ќписание класса TcTriangle3 - треугольник 45∞-90∞-45∞
////////////////////////////////////////////////////////////////////////////////

#ifndef FILE_TCTRIANGLE3
#define FILE_TCTRIANGLE3

#include ".\TcBase.h"

//////////////////////////////////

namespace nsFigure {

class TcTriangle3: public TB{
private:
   static float b;  // -   половина большей стороны a (b=a/2)
public:
   static POINT GetSizeFieldInPixel(const POINT&, const int&);
   static int SizeInscribedSquare(int);
   static float GetPercentMine(TeSkillLevel); // процент мин на заданном уровне сложности
private:
   POINT neighbor[14]; // кординаты соседей; если соседа нет, то координаты отрицательны
   POINTFLOAT regionOut[3]; // координаты 3x точек из которых состоит фигура
   int direction; // 0..3
public:
   TcTriangle3(const POINT&, const POINT&, const int&);
   int   GetNeighborNumber() const;
   POINT GetNeighborCoord(int) const;
   bool ToBelong(int, int); // принадлежат ли эти экранные координаты €чейке
   void SetPoint(const int&); // определить координаты точек из которых состоит фигура
   void Paint() const;
};

} // namespace nsFigure

#endif // FILE_TCTRIANGLE3
