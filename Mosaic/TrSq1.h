////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "TrSq1.h"
//
// ќписание класса CTrSq1 - мозаика из 4х треугольников и 2х квадратов
////////////////////////////////////////////////////////////////////////////////

#ifndef __FILE__TRSQ1__
#define __FILE__TRSQ1__

#ifndef __AFX_H__
   #include <Windows.h>
#endif
#include "Base.h"

namespace nsCell {

class CTrSq1: public CBase {
private:
   static float m_n;
   static float m_m;
   static float m_b;
   static float m_k;
public:
   static SIZE GetSizeInPixel(const SIZE &sizeField, int iArea);
   static int SizeInscribedSquare(int iArea, int iBorderWidth);
public:
   CTrSq1(const COORD &Coord, const SIZE &sizeField, int iArea, const CGraphicContext &gContext);
   bool PointInRegion(const POINT &point) const; // принадлежат ли эти экранные координаты €чейке
   void SetPoint(int iArea); // определить координаты точек из которых состоит фигура
   void Paint() const;
};

} // namespace nsCell

#endif // __FILE__TRSQ1__
