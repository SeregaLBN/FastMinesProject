////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "PentagonT10.h"
//
// Описание класса CPentagonT10 - 5-ти угольник, тип №10
////////////////////////////////////////////////////////////////////////////////

#ifndef __FILE__PENTAGONT10__
#define __FILE__PENTAGONT10__

#ifndef __AFX_H__
   #include <Windows.h>
#endif
#include "Base.h"

namespace nsCell {

class CPentagonT10: public CBase {
public:
   static SIZE GetSizeInPixel(const SIZE &sizeField, int iArea);
   static int SizeInscribedSquare(int iArea, int iBorderWidth);
public:
   CPentagonT10(const COORD &Coord, const SIZE &sizeField, int iArea, const CGraphicContext &gContext);
   void SetPoint(int iArea); // определить координаты точек из которых состоит фигура
   void Paint() const;
};

} // namespace nsCell

#endif // __FILE__PENTAGONT10__
