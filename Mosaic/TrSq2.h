////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "TrSq2.h"
//
// ќписание класса CTrSq2 - мозаика из 24х треугольников и 12х квадратов (на 1 квадрат приходитс€ 2 треугольника)
////////////////////////////////////////////////////////////////////////////////

#ifndef __FILE__TRSQ2_
#define __FILE__TRSQ2_

#ifndef __AFX_H__
   #include <Windows.h>
#endif
#include "Base.h"

namespace nsCell {

class CTrSq2: public CBase {
private:
   static float m_b;
   static float m_h;
public:
   static SIZE GetSizeInPixel(const SIZE &sizeField, int iArea);
   static int SizeInscribedSquare(int iArea, int iBorderWidth);
public:
   CTrSq2(const COORD &Coord, const SIZE &sizeField, int iArea, const CGraphicContext &gContext);
   bool PointInRegion(const POINT &point) const; // принадлежат ли эти экранные координаты €чейке
   void SetPoint(int iArea); // определить координаты точек из которых состоит фигура
   void Paint() const;
};

} // namespace nsCell

#endif // __FILE__TRSQ2_
