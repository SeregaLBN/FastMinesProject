////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "TcParquet1.cpp"
//
// Реализация класса TcParquet1 - паркет в елку (herring-bone parquet)
////////////////////////////////////////////////////////////////////////////////

#include ".\TcParquet1.h"
#include <math.h>

namespace nsFigure {

////////////////////////////////////////////////////////////////////////////////
//                               static function
////////////////////////////////////////////////////////////////////////////////

POINT TcParquet1::GetSizeFieldInPixel(const POINT& sizeField, const int& area) {
   a = sqrt(area)/2;
   POINT result = {(int)a*(sizeField.x*2+1),
                   (int)a*(sizeField.y*2+2)};
   return result;
}

int TcParquet1::SizeInscribedSquare(int area) {
   sq = sqrt(area)/2;
   return sq;
}

float TcParquet1::GetPercentMine(TeSkillLevel skill) { // процент мин на заданном уровне сложности
   switch (skill) {
   case skillLevelBeginner    : return 14.f;
   case skillLevelAmateur     : return 17.f;
   case skillLevelProfessional: return 20.f;
   case skillLevelCrazy       : return 24.f;
   }
   return 1.f;
}

////////////////////////////////////////////////////////////////////////////////
//                               virtual function
////////////////////////////////////////////////////////////////////////////////

TcParquet1::TcParquet1(const POINT& setCoord, const POINT& sizeField, const int& area) {
   coord = setCoord;
   Reset();

   direction = (coord.x % 2) ? true : false;
   SetPoint(area);

   // определяю координаты соседей
   neighbor[0].x = direction ? coord.x   : coord.x-1;
   neighbor[0].y = coord.y-1;
   neighbor[1].x = direction ? coord.x-1 : coord.x  ;
   neighbor[1].y = direction ? coord.y   : coord.y-1;
   neighbor[2].x = coord.x+1;
   neighbor[2].y = direction ? coord.y   : coord.y-1;
   neighbor[3].x = coord.x-1;
   neighbor[3].y = direction ? coord.y+1 : coord.y  ;
   neighbor[4].x = direction ? coord.x   : coord.x+1;
   neighbor[4].y = direction ? coord.y+1 : coord.y  ;
   neighbor[5].x = direction ? coord.x+1 : coord.x  ;
   neighbor[5].y = coord.y+1;
   for (int i=0; i<6; i++)
      if ((neighbor[i].x >= sizeField.x) ||
          (neighbor[i].y >= sizeField.y) ||
          (neighbor[i].x < 0) ||
          (neighbor[i].y < 0)) {
         neighbor[i] = CIncorrectCoord;
      }
}

int TcParquet1::GetNeighborNumber() const {
   return 6;
}

POINT TcParquet1::GetNeighborCoord(int index) const {
   return neighbor[index];
}

bool TcParquet1::ToBelong(int x, int y) { // принадлежат ли эти экранные координаты ячейке
   POINT point = {x, y};
   POINTFLOAT f[4] = {{regionOut[0].x, regionOut[0].y},
                      {regionOut[1].x, regionOut[1].y},
                      {regionOut[2].x, regionOut[2].y},
                      {regionOut[3].x, regionOut[3].y}};
   return PointInPolygon(point, f, 4);
}

void TcParquet1::SetPoint(const int& area) {
   if (coord.x==0 && coord.y==0) {
      sq = a = sqrt(area)/2; // размер стороны вписанного квадрата
      TB::SetPoint(NULL);
   }

   // определение координат точек фигуры
   int x0 = (int)a*(2*(coord.x+1)-direction);
   int y0 = (int)a*(2* coord.y   +direction);
   regionOut[0].x = x0;
   regionOut[1].x = direction ? x0+(int)a*2 : x0+(int)a;
   regionOut[2].x = direction ? x0+(int)a   : x0-(int)a;
   regionOut[3].x = direction ? x0-(int)a   : x0-(int)a*2;
   regionOut[0].y = y0;
   regionOut[1].y = direction ? y0+(int)a*2 : y0+(int)a;
   regionOut[2].y = y0+(int)a*3;
   regionOut[3].y = direction ? y0+(int)a   : y0+(int)a*2;

   // определение координат вписанного в фигуру квадрата - область в которую выводится изображение/текст
   regionIn.left   = direction ? regionOut[0].x : regionOut[2].x;
   regionIn.top    = direction ? regionOut[3].y : regionOut[1].y;
   regionIn.right  = direction ? regionOut[2].x : regionOut[0].x;
   regionIn.bottom = direction ? regionOut[1].y : regionOut[3].y;
   regionIn.left   += 1;
   regionIn.top    += 1;
   regionIn.right  += 1;
   regionIn.bottom += 1;
}

void TcParquet1::Paint() const {
   TB::Paint();

   SelectObject(hDC, down ? hPenWhite : hPenBlack);
   MoveToEx(hDC, regionOut[0].x  , regionOut[0].y, NULL);
   LineTo  (hDC, regionOut[1].x  , regionOut[1].y);
   LineTo  (hDC, regionOut[2].x  , regionOut[2].y);
   SelectObject(hDC, down ? hPenBlack : hPenWhite);
   MoveToEx(hDC, regionOut[2].x  , regionOut[2].y-1, NULL);
   LineTo  (hDC, regionOut[3].x  , regionOut[3].y-1);
   MoveToEx(hDC, regionOut[3].x+1, regionOut[3].y  , NULL);
   LineTo  (hDC, regionOut[0].x+1, regionOut[0].y  );
}

} // namespace nsFigure
