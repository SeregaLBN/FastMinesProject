////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "PentagonT24.h"
//
// Описание класса CPentagonT24 - равносторонний 5-ти угольник, тип №2 и №4
////////////////////////////////////////////////////////////////////////////////

#ifndef __FILE__PENTAGONT24__
#define __FILE__PENTAGONT24__

#ifndef __AFX_H__
   #include <Windows.h>
#endif
#include "Base.h"

namespace nsCell {

class CPentagonT24: public CBase {
private:
   static float m_b;
   static float m_c;
public:
   static SIZE GetSizeInPixel(const SIZE &sizeField, int iArea);
   static int SizeInscribedSquare(int iArea, int iBorderWidth);
public:
   CPentagonT24(const COORD &Coord, const SIZE &sizeField, int iArea, const CGraphicContext &gContext);
   void SetPoint(int iArea); // определить координаты точек из которых состоит фигура
   void Paint() const;
};

} // namespace nsCell

#endif // __FILE__PENTAGONT24__
