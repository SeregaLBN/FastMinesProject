////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "Hexagon1.h"
//
// Описание класса CHexagon1 - правильный 6-ти угольник (сота)
////////////////////////////////////////////////////////////////////////////////

#ifndef __FILE__HEXAGON1__
#define __FILE__HEXAGON1__

#ifndef __AFX_H__
   #include <Windows.h>
#endif
#include "Base.h"

namespace nsCell {

class CHexagon1: public CBase {
private:
   static float m_b; // ширина шестиугольника
   static float m_h; // высота шестиугольника
public:
   static SIZE GetSizeInPixel(const COORD &sizeField, int area);
   static int SizeInscribedSquare(int area, int borderWidth);
public:
   CHexagon1(const COORD &Coord, const COORD &sizeField, int area, const CGraphicContext &gContext);
   void SetPoint(int area); // определить координаты точек из которых состоит фигура
   void Paint() const;
};

} // namespace nsCell

#endif // __FILE__HEXAGON1__
