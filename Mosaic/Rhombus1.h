////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "Rhombus1.h"
//
// Описание класса CRhombus1
////////////////////////////////////////////////////////////////////////////////

#ifndef __FILE__RHOMBUS1__
#define __FILE__RHOMBUS1__

#ifndef __AFX_H__
   #include <Windows.h>
#endif
#include "Base.h"

namespace nsCell {

class CRhombus1: public CBase {
private:
   static float m_r;
   static float m_h;
   static float m_c;
public:
   static SIZE GetSizeInPixel(const COORD &sizeField, int area);
   static int SizeInscribedSquare(int area, int borderWidth);
public:
   CRhombus1(const COORD &Coord, const COORD &sizeField, int area, const CGraphicContext &gContext);
   void SetPoint(int area); // определить координаты точек из которых состоит фигура
   void Paint() const;
};

} // namespace nsCell

#endif // __FILE__RHOMBUS1__
