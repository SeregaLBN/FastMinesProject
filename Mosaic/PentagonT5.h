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
   static SIZE GetSizeInPixel(const SIZE &sizeField, int iArea);
   static int SizeInscribedSquare(int iArea, int iBorderWidth);
public:
   CPentagonT5(const COORD& Coord, const SIZE &sizeField, int iArea, const CGraphicContext &gContext);
   void SetPoint(int iArea); // определить координаты точек из которых состоит фигура
   void Paint() const;
};

} // namespace nsCell

#endif // __FILE__PENTAGONT5__
