////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "TcQuadrangle1.h"
//
// ќписание класса TcQuadrangle1 - четырЄхугольник 120∞-90∞-60∞-90∞
////////////////////////////////////////////////////////////////////////////////

#ifndef FILE_TCQUADRANGLE1
#define FILE_TCQUADRANGLE1

#include ".\TcBase.h"

//////////////////////////////////

namespace nsFigure {

class TcQuadrangle1: public TB{
private:
   static float b;
   static float h;
   static float n;
   static float m;
   static float R;
   static float t;
public:
   static POINT GetSizeFieldInPixel(const POINT&, const int&);
   static int SizeInscribedSquare(int);
   static float GetPercentMine(TeSkillLevel); // процент мин на заданном уровне сложности
private:
   POINT neighbor[9]; // кординаты соседей; если соседа нет, то координаты отрицательны
   POINTFLOAT regionOut[4]; // координаты 4x точек из которых состоит фигура
   int direction; // 0..11
public:
   TcQuadrangle1(const POINT&, const POINT&, const int&);
   int   GetNeighborNumber() const;
   POINT GetNeighborCoord(int) const;
   bool ToBelong(int, int); // принадлежат ли эти экранные координаты €чейке
   void SetPoint(const int&); // определить координаты точек из которых состоит фигура
   void Paint() const;
};

} // namespace nsFigure

#endif // FILE_TCQUADRANGLE1
