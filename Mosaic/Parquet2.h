////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "Parquet2.h"
//
// Описание класса CParquet2
////////////////////////////////////////////////////////////////////////////////

#ifndef __FILE__PARQUET2__
#define __FILE__PARQUET2__

#ifndef __AFX_H__
   #include <Windows.h>
#endif
#include "Base.h"

namespace nsCell {

class CParquet2: public CBase {
public:
   static SIZE GetSizeInPixel(const SIZE &sizeField, int iArea);
   static int SizeInscribedSquare(int iArea, int iBorderWidth);
public:
   CParquet2(const COORD &Coord, const SIZE &sizeField, int iArea, const CGraphicContext &gContext);
   void SetPoint(int iArea); // определить координаты точек из которых состоит фигура
   void Paint() const;
};

} // namespace nsCell

#endif // __FILE__PARQUET2__
