////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "Parquet2.h"
//
// �������� ������ CParquet2
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
   static SIZE GetSizeInPixel(const COORD &sizeField, int area);
   static int SizeInscribedSquare(int area, int borderWidth);
public:
   CParquet2(const COORD &Coord, const COORD &sizeField, int area, const CGraphicContext &gContext);
   void SetPoint(int area); // ���������� ���������� ����� �� ������� ������� ������
   void Paint() const;
};

} // namespace nsCell

#endif // __FILE__PARQUET2__
