////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "TcHexagon.cpp"
//
// Реализация класса TcHexagon - правильный 6-ти угольник (сота)
////////////////////////////////////////////////////////////////////////////////

#include ".\TcHexagon.h"
#include <math.h>

namespace nsFigure {

////////////////////////////////////////////////////////////////////////////////
//                               local init
////////////////////////////////////////////////////////////////////////////////

float TcHexagon::b; // ширина шестиугольника
float TcHexagon::h; // высота шестиугольника

////////////////////////////////////////////////////////////////////////////////
//                               static function
////////////////////////////////////////////////////////////////////////////////

POINT TcHexagon::GetSizeFieldInPixel(const POINT& sizeField, const int& area) {
   a = sqrt(2*area/sqrt(27)); // размер стороны шестиугольника
   POINT result = {((float)sizeField.x + 0.5f) * sqrt(3)*a + 1,
                   ((float)sizeField.y*1.5f + 0.5f) * a};
   return result;
}

int TcHexagon::SizeInscribedSquare(int area) {
   sq = sqrt(area/sqrt(3));
   return sq;
}

float TcHexagon::GetPercentMine(TeSkillLevel skill) { // процент мин на заданном уровне сложности
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

TcHexagon::TcHexagon(const POINT& setCoord, const POINT& sizeField, const int& area) {
   coord = setCoord;
   Reset();

   direction = (coord.y % 2) ? true : false;
   SetPoint(area);

   // определяю координаты соседей
   neighbor[0].x = direction ? coord.x   : coord.x-1;
   neighbor[0].y = coord.y-1;
   neighbor[1].x = direction ? coord.x+1 : coord.x;
   neighbor[1].y = coord.y-1;
   neighbor[2].x = coord.x-1;
   neighbor[2].y = coord.y;
   neighbor[3].x = coord.x+1;
   neighbor[3].y = coord.y;
   neighbor[4].x = direction ? coord.x   : coord.x-1;
   neighbor[4].y = coord.y+1;
   neighbor[5].x = direction ? coord.x+1 : coord.x;
   neighbor[5].y = coord.y+1;
   for (int i=0; i<6; i++)
      if ((neighbor[i].x >= sizeField.x) ||
          (neighbor[i].y >= sizeField.y) ||
          (neighbor[i].x < 0) ||
          (neighbor[i].y < 0)) {
         neighbor[i] = CIncorrectCoord;
      }
}

int TcHexagon::GetNeighborNumber() const {
   return 6;
}

POINT TcHexagon::GetNeighborCoord(int index) const {
   return neighbor[index];
}

bool TcHexagon::ToBelong(int x, int y) { // принадлежат ли эти экранные координаты ячейке
   POINT point = {x, y};
   return PointInPolygon(point, regionOut, 6);
}

void TcHexagon::SetPoint(const int& area) {
   if (coord.x==0 && coord.y==0) {
      a = sqrt(2*area/sqrt(27)); // размер стороны шестиугольника
      b = a*sqrt(3);             // ширина шестиугольника
      h = a*2;                   // высота шестиугольника
      sq = sqrt(area/sqrt(3));   // размер квадрата, вписанного в круг (круг вписан в шестиугольник)
      TB::SetPoint(NULL);
   }

   // определение координат точек фигуры
   float x0 = ((float)coord.x + (direction ? 1 : 0.5 )) * b;
   float y0 =  (float)coord.y * a * 1.5;
   regionOut[0].x = x0;      regionOut[0].y = y0;
   regionOut[1].x = x0+b/2;  regionOut[1].y = y0+h/4;
   regionOut[2].x = x0+b/2;  regionOut[2].y = y0+h*3/4;
   regionOut[3].x = x0;      regionOut[3].y = y0+h;
   regionOut[4].x = x0-b/2;  regionOut[4].y = y0+h*3/4;
   regionOut[5].x = x0-b/2;  regionOut[5].y = y0+h/4;

   // определение координат вписанного в фигуру квадрата - область в которую выводится изображение/текст
   POINTFLOAT center = {regionOut[0].x,// координата центра квадрата
                        regionOut[1].y + a/2};
   regionIn.left   = center.x - sq/2;
   regionIn.top    = center.y - sq/2;
   regionIn.right  = center.x + sq/2;
   regionIn.bottom = center.y + sq/2;
   regionIn.left   += 1;
   regionIn.top    += 1;
   regionIn.right  += 1;
   regionIn.bottom += 1;
}

void TcHexagon::Paint() const {
   TB::Paint();

   SelectObject(hDC, down ? hPenWhite : hPenBlack);
   MoveToEx(hDC, regionOut[1].x  , regionOut[1].y, NULL);
   LineTo  (hDC, regionOut[2].x  , regionOut[2].y);
   LineTo  (hDC, regionOut[3].x  , regionOut[3].y);
   LineTo  (hDC, regionOut[4].x  , regionOut[4].y);
   SelectObject(hDC, down ? hPenBlack : hPenWhite);
   MoveToEx(hDC, regionOut[4].x+1, regionOut[4].y, NULL);
   LineTo  (hDC, regionOut[5].x+1, regionOut[5].y);
   MoveToEx(hDC, regionOut[5].x  , regionOut[5].y+1, NULL);
   LineTo  (hDC, regionOut[0].x  , regionOut[0].y+1);
   LineTo  (hDC, regionOut[1].x  , regionOut[1].y+1);
}

} // namespace nsFigure
