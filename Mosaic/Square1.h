////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "Square1.h"
//
// Описание класса CSquare1 - квадрат (классический вариант поля)
////////////////////////////////////////////////////////////////////////////////

#ifndef __FILE__SQUARE1__
#define __FILE__SQUARE1__

#ifndef __AFX_H__
   #include <Windows.h>
#endif
#include "Base.h"

namespace nsCell {

class CSquare1: public CBase {
public:
   static SIZE GetSizeInPixel(const SIZE &sizeField, int iArea);
   static int SizeInscribedSquare(int iArea, int iBorderWidth);
public:
   CSquare1(const COORD &Coord, const SIZE &sizeField, int iArea, const CGraphicContext &gContext);
   bool PointInRegion(const POINT &point) const; // принадлежат ли эти экранные координаты ячейке
   void SetPoint(int iArea); // определить координаты точек из которых состоит фигура
   void Paint() const;
};

} // namespace nsCell

#endif // __FILE__SQUARE1__
