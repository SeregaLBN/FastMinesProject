////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "Triangle1.h"
//
// Описание класса CTriangle1 - равносторонний треугольник (вариант поля №1)
////////////////////////////////////////////////////////////////////////////////

#ifndef __FILE__TRIANGLE1__
#define __FILE__TRIANGLE1__

#ifndef __AFX_H__
   #include <Windows.h>
#endif
#include "Base.h"

namespace nsCell {

class CTriangle1: public CBase {
private:
   static float m_h;
   static float m_b;
public:
   static SIZE GetSizeInPixel(const SIZE &sizeField, int iArea);
   static int SizeInscribedSquare(int iArea, int iBorderWidth);
public:
   CTriangle1(const COORD &Coord, const SIZE &sizeField, int iArea, const CGraphicContext &gContext);
   void SetPoint(int iArea); // определить координаты точек из которых состоит фигура
   void Paint() const;
};

} // namespace nsCell

#endif // __FILE__TRIANGLE1__
