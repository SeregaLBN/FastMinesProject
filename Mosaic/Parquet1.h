////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "Parquet1.h"
//
// �������� ������ CParquet1 - ������ � ���� (herring-bone parquet)
////////////////////////////////////////////////////////////////////////////////

#ifndef __FILE__PARQUET1__
#define __FILE__PARQUET1__

#ifndef __AFX_H__
   #include <Windows.h>
#endif
#include "Base.h"

namespace nsCell {

class CParquet1: public CBase {
public:
   static SIZE GetSizeInPixel(const COORD &sizeField, int area);
   static int SizeInscribedSquare(int area, int borderWidth);
public:
   CParquet1(const COORD &Coord, const COORD &sizeField, int area, const CGraphicContext &gContext);
   void SetPoint(int area); // ���������� ���������� ����� �� ������� ������� ������
   void Paint() const;
};

} // namespace nsCell

#endif // __FILE__PARQUET1__
