////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "TcTriangle2.cpp"
//
// –еализаци€ класса TcTriangle2 - равносторонний треугольник (вариант пол€ є2 - Єлочкой)
////////////////////////////////////////////////////////////////////////////////

#include ".\TcTriangle2.h"
#include <math.h>

namespace nsFigure {

////////////////////////////////////////////////////////////////////////////////
//                               local init
////////////////////////////////////////////////////////////////////////////////

float TcTriangle2::b;
float TcTriangle2::h;

////////////////////////////////////////////////////////////////////////////////
//                               static function
////////////////////////////////////////////////////////////////////////////////

POINT TcTriangle2::GetSizeFieldInPixel(const POINT& sizeField, const int& area) {
   a = sqrt(area/sqrt(3))*2;
   b = a/2;
   h = a*sqrt(3)/2;
   POINT result = {b*(sizeField.x +1),
                   h* sizeField.y     + 1 };
   return result;
}

int TcTriangle2::SizeInscribedSquare(int area) {
   a = sqrt(area/sqrt(3))*2; // размер стороны треугольника
   sq = a*sqrt(3)/(sqrt(3)+2);
   return sq;
}

float TcTriangle2::GetPercentMine(TeSkillLevel skill) { // процент мин на заданном уровне сложности
   switch (skill) {
   case skillLevelBeginner    : return 15.f;
   case skillLevelAmateur     : return 18.f;
   case skillLevelProfessional: return 21.f;
   case skillLevelCrazy       : return 25.f;
   }
   return 1.f;
}

////////////////////////////////////////////////////////////////////////////////
//                               virtual function
////////////////////////////////////////////////////////////////////////////////

TcTriangle2::TcTriangle2(const POINT& setCoord, const POINT& sizeField, const int& area) {
   coord = setCoord;
   Reset();

   direction = coord.x & 1;
   SetPoint(area);

   // определ€ю координаты соседей
   switch (direction) {
   case 0:
      neighbor[0].x = coord.x  ; neighbor[0].y = coord.y-1;
      neighbor[1].x = coord.x-2; neighbor[1].y = coord.y  ;
      neighbor[2].x = coord.x-1; neighbor[2].y = coord.y  ;
      neighbor[3].x = coord.x+1; neighbor[3].y = coord.y  ;
      neighbor[4].x = coord.x+2; neighbor[4].y = coord.y  ;
      neighbor[5].x = coord.x-1; neighbor[5].y = coord.y+1;
      neighbor[6].x = coord.x  ; neighbor[6].y = coord.y+1;
      neighbor[7].x = coord.x+1; neighbor[7].y = coord.y+1;
      break;
   case 1:
      neighbor[0].x = coord.x-1; neighbor[0].y = coord.y-1;
      neighbor[1].x = coord.x  ; neighbor[1].y = coord.y-1;
      neighbor[2].x = coord.x+1; neighbor[2].y = coord.y-1;
      neighbor[3].x = coord.x-2; neighbor[3].y = coord.y  ;
      neighbor[4].x = coord.x-1; neighbor[4].y = coord.y  ;
      neighbor[5].x = coord.x+1; neighbor[5].y = coord.y  ;
      neighbor[6].x = coord.x+2; neighbor[6].y = coord.y  ;
      neighbor[7].x = coord.x  ; neighbor[7].y = coord.y+1;
      break;
   }
   for (int i=0; i<8; i++)
      if ((neighbor[i].x >= sizeField.x) ||
          (neighbor[i].y >= sizeField.y) ||
          (neighbor[i].x < 0) ||
          (neighbor[i].y < 0)) {
         neighbor[i] = CIncorrectCoord;
      }
}

int TcTriangle2::GetNeighborNumber() const {
   return 8;
}

POINT TcTriangle2::GetNeighborCoord(int index) const {
   return neighbor[index];
}

bool TcTriangle2::ToBelong(int x, int y) { // принадлежат ли эти экранные координаты €чейке
   POINT point = {x, y};
   return PointInPolygon(point, regionOut, 3);
}

void TcTriangle2::SetPoint(const int& area) {
   if (coord.x==0 && coord.y==0) {
      a = sqrt(area/sqrt(3))*2;
      b = a/2;
      h = a*sqrt(3)/2;
      sq = a*sqrt(3)/(sqrt(3)+2);
      TB::SetPoint(NULL);
   }

   // определение координат точек фигуры
   switch (direction) {
   case 0:
      regionOut[0].x = a*(coord.x/2) + b;
      break;
   case 1:
      regionOut[0].x = a*(coord.x/2) + a+b;
      break;
   }
   regionOut[0].y = h*coord.y;
   switch (direction) {
   case 0:
      regionOut[1].x = regionOut[0].x + b;   regionOut[1].y = regionOut[0].y + h;
      regionOut[2].x = regionOut[0].x - b;   regionOut[2].y = regionOut[1].y;
      break;
   case 1:
      regionOut[1].x = regionOut[0].x - b;   regionOut[1].y = regionOut[0].y + h;
      regionOut[2].x = regionOut[0].x - a;   regionOut[2].y = regionOut[0].y;
      break;
   }

   // определение координат вписанного в фигуру квадрата - область в которую выводитс€ изображение/текст
   POINTFLOAT center; // координата центра квадрата
   switch (direction) {
   case 0:
      center.x = regionOut[0].x;
      center.y = regionOut[1].y - sq/2;
      center.y--;
      break;
   case 1:
      center.x = regionOut[1].x;
      center.y = regionOut[0].y + sq/2;
      center.y++;
      break;
   }
   
   regionIn.left   = center.x - sq/2;
   regionIn.top    = center.y - sq/2;
   regionIn.right  = center.x + sq/2;
   regionIn.bottom = center.y + sq/2;
   regionIn.left   += 1;
   regionIn.top    += 1;
   regionIn.right  += 1;
   regionIn.bottom += 1;
}

void TcTriangle2::Paint() const {
   TB::Paint();

   SelectObject(hDC, down ? hPenWhite : hPenBlack);
   MoveToEx(hDC, regionOut[0].x, regionOut[0].y, NULL);
   LineTo  (hDC, regionOut[1].x, regionOut[1].y);
   if (!direction)
      LineTo  (hDC, regionOut[2].x, regionOut[2].y);

   SelectObject(hDC, down ? hPenBlack : hPenWhite);
   switch (direction) {
   case 0:
      MoveToEx(hDC, regionOut[2].x+1, regionOut[2].y  , NULL);
      LineTo  (hDC, regionOut[0].x+1, regionOut[0].y  );
      break;
   case 1:
      MoveToEx(hDC, regionOut[1].x+1, regionOut[1].y  , NULL);
      LineTo  (hDC, regionOut[2].x+1, regionOut[2].y  );
      MoveToEx(hDC, regionOut[2].x  , regionOut[2].y+1, NULL);
      LineTo  (hDC, regionOut[0].x  , regionOut[0].y+1);
      break;
   }
}

} // namespace nsFigure
