////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "Triangle3.h"
//
// Описание класса CTriangle3 - треугольник 45°-90°-45°
////////////////////////////////////////////////////////////////////////////////

#ifndef __FILE__TRIANGLE3__
#define __FILE__TRIANGLE3__

#ifndef __AFX_H__
   #include <Windows.h>
#endif
#include "Base.h"

namespace nsCell {

class CTriangle3: public CBase {
private:
   static float m_b;
public:
   static SIZE GetSizeInPixel(const SIZE &sizeField, int iArea);
   static int SizeInscribedSquare(int iArea, int iBorderWidth);
public:
   CTriangle3(const COORD &Coord, const SIZE &sizeField, int iArea, const CGraphicContext &gContext);
   void SetPoint(int iArea); // определить координаты точек из которых состоит фигура
   void Paint() const;
};

} // namespace nsCell

#endif // __FILE__TRIANGLE3__
