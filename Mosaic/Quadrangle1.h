////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "Quadrangle1.h"
//
// Описание класса CQuadrangle1 - четырёхугольник 120°-90°-60°-90°
////////////////////////////////////////////////////////////////////////////////

#ifndef __FILE__QUADRANGLE1__
#define __FILE__QUADRANGLE1__

#ifndef __AFX_H__
   #include <Windows.h>
#endif
#include "Base.h"

namespace nsCell {

class CQuadrangle1: public CBase {
private:
   static float m_b;
   static float m_h;
   static float m_n;
   static float m_m;
   static float m_Z;
   static float m_Zx;
   static float m_Zy;
public:
   static SIZE GetSizeInPixel(const COORD &sizeField, int area);
   static int SizeInscribedSquare(int area, int borderWidth);
public:
   CQuadrangle1(const COORD &Coord, const COORD &sizeField, int area, const CGraphicContext &gContext);
   void SetPoint(int area); // определить координаты точек из которых состоит фигура
   void Paint() const;
};

} // namespace nsCell

#endif // __FILE__QUADRANGLE1__
