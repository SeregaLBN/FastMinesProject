////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "Square2.h"
//
// Описание класса CSquare2 - квадрат (перекошенный вариант поля)
////////////////////////////////////////////////////////////////////////////////

#ifndef __FILE__SQUARE2__
#define __FILE__SQUARE2__

#ifndef __AFX_H__
   #include <Windows.h>
#endif
#include "Base.h"

namespace nsCell {

class CSquare2: public CBase {
public:
   static SIZE GetSizeInPixel(const SIZE &sizeField, int iArea);
   static int SizeInscribedSquare(int iArea, int iBorderWidth);
public:
   CSquare2(const COORD &Coord, const SIZE &sizeField, int iArea, const CGraphicContext &gContext);
   bool PointInRegion(const POINT &point) const; // принадлежат ли эти экранные координаты ячейке
   void SetPoint(int iArea); // определить координаты точек из которых состоит фигура
   void Paint() const;
};

} // namespace nsCell

#endif // __FILE__SQUARE2__
