////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "Trapezoid2.h"
//
// Описание класса CTrapezoid2
////////////////////////////////////////////////////////////////////////////////

#ifndef __FILE__TRAPEZOID2__
#define __FILE__TRAPEZOID2__

#ifndef __AFX_H__
   #include <Windows.h>
#endif
#include "Base.h"

namespace nsCell {

class CTrapezoid2: public CBase {
private:
   static float m_b;  // -   большая сторона трапеции (основание)
   static float m_c;  //
   static float m_R;  // -   диагональ трапеции
   static float m_r;  // -   высота трапеции
public:
   static SIZE GetSizeInPixel(const SIZE &sizeField, int iArea);
   static int SizeInscribedSquare(int iArea, int iBorderWidth);
public:
   CTrapezoid2(const COORD &Coord, const SIZE &sizeField, int iArea, const CGraphicContext &gContext);
   void SetPoint(int iArea); // определить координаты точек из которых состоит фигура
   void Paint() const;
};

} // namespace nsCell

#endif // __FILE__TRAPEZOID2__
