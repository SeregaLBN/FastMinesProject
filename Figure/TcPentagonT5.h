////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "TcPentagonT5.h"
//
// Описание класса TcPentagonT5 - 5-ти угольник, тип №5
////////////////////////////////////////////////////////////////////////////////

#ifndef FILE_TCPENTAGONT5
#define FILE_TCPENTAGONT5

#include ".\TcBase.h"

//////////////////////////////////

namespace nsFigure {

class TcPentagonT5: public TB{
private:
   static float h;
public:
   static POINT GetSizeFieldInPixel(const POINT&, const int&);
   static int SizeInscribedSquare(int);
   static float GetPercentMine(TeSkillLevel); // процент мин на заданном уровне сложности
private:
   POINT neighbor[8]; // кординаты соседей; если соседа нет, то координаты отрицательны
   POINTFLOAT regionOut[5]; // координаты 5ти точек из которых состоит фигура
   int direction; // 0..83
public:
   TcPentagonT5(const POINT&, const POINT&, const int&);
   int   GetNeighborNumber() const;
   POINT GetNeighborCoord(int) const;
   bool ToBelong(int, int); // принадлежат ли эти экранные координаты ячейке
   void SetPoint(const int&); // определить координаты точек из которых состоит фигура
   void Paint() const;
};

} // namespace nsFigure

#endif // FILE_TCPENTAGONT5
