////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "SqTrHex.h"
//
// ќписание класса CSqTrHex - 6Square 4Triangle 2Hexagon
////////////////////////////////////////////////////////////////////////////////

#ifndef __FILE__SQTRHEX__
#define __FILE__SQTRHEX__

#ifndef __AFX_H__
   #include <Windows.h>
#endif
#include "Base.h"

namespace nsCell {

class CSqTrHex: public CBase {
private:
   static float m_h;  // -   высота треугольника
public:
   static SIZE GetSizeInPixel(const COORD& sizeField, int area);
   static int SizeInscribedSquare(int area, int borderWidth);
public:
   CSqTrHex(const COORD& Coord, const COORD& sizeField, int area, const CGraphicContext& gContext);
   bool PointInRegion(const POINT& point) const; // принадлежат ли эти экранные координаты €чейке
   void SetPoint(int area); // определить координаты точек из которых состоит фигура
   void Paint() const;
};

} // namespace nsCell

#endif // __FILE__SQTRHEX__
