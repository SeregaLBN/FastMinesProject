////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "PentagonT5.h"
//
// Описание класса CPentagonT5 - 5-ти угольник, тип №5
////////////////////////////////////////////////////////////////////////////////

#ifndef __FILE__PENTAGONT5__
#define __FILE__PENTAGONT5__

#ifndef __AFX_H__
   #include <Windows.h>
#endif
#include "Base.h"

namespace nsCell {

class CPentagonT5: public CBase {
private:
   static float m_h;
public:
   static SIZE GetSizeInPixel(const COORD& sizeField, int area);
   static int SizeInscribedSquare(int area, int borderWidth);
public:
   CPentagonT5(const COORD& Coord, const COORD& sizeField, int area, const CGraphicContext& gContext);
   void SetPoint(int area); // определить координаты точек из которых состоит фигура
   void Paint() const;
};

} // namespace nsCell

#endif // __FILE__PENTAGONT5__
