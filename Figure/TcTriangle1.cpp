////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "TcTriangle1.cpp"
//
// Реализация класса TcTriangle1 - равносторонний треугольник (вариант поля №1)
////////////////////////////////////////////////////////////////////////////////

#include ".\TcTriangle1.h"
#include <math.h>

namespace nsFigure {

////////////////////////////////////////////////////////////////////////////////
//                               local init
////////////////////////////////////////////////////////////////////////////////

float TcTriangle1::b;
float TcTriangle1::h;

////////////////////////////////////////////////////////////////////////////////
//                               static function
////////////////////////////////////////////////////////////////////////////////

POINT TcTriangle1::GetSizeFieldInPixel(const POINT& sizeField, const int& area) {
   a = sqrt(area/sqrt(3))*2;
   b = a/2;
   h = a*sqrt(3)/2;
   POINT result = {b*(sizeField.x +1),
                   h* sizeField.y     + 1 };
   return result;
}

int TcTriangle1::SizeInscribedSquare(int area) {
   a = sqrt(area/sqrt(3))*2; // размер стороны треугольника
   sq = a*sqrt(3)/(sqrt(3)+2);
   return sq;
}

float TcTriangle1::GetPercentMine(TeSkillLevel skill) { // процент мин на заданном уровне сложности
   switch (skill) {
   case skillLevelBeginner    : return 16.5f;
   case skillLevelAmateur     : return 19.5f;
   case skillLevelProfessional: return 22.5f;
   case skillLevelCrazy       : return 26.5f;
   }
   return 1.f;
}

////////////////////////////////////////////////////////////////////////////////
//                               virtual function
////////////////////////////////////////////////////////////////////////////////

TcTriangle1::TcTriangle1(const POINT& setCoord, const POINT& sizeField, const int& area) {
   coord = setCoord;
   Reset();

   direction = (coord.y%2)*2+(coord.x%2); // 0..3
   SetPoint(area);

   // определяю координаты соседей
   switch (direction) {
   case 0: case 3:
      neighbor[ 0].x = coord.x-1;   neighbor[ 0].y = coord.y-1;
      neighbor[ 1].x = coord.x  ;   neighbor[ 1].y = coord.y-1;
      neighbor[ 2].x = coord.x+1;   neighbor[ 2].y = coord.y-1;
      neighbor[ 3].x = coord.x-2;   neighbor[ 3].y = coord.y  ;
      neighbor[ 4].x = coord.x-1;   neighbor[ 4].y = coord.y  ;
      neighbor[ 5].x = coord.x+1;   neighbor[ 5].y = coord.y  ;
      neighbor[ 6].x = coord.x+2;   neighbor[ 6].y = coord.y  ;
      neighbor[ 7].x = coord.x-2;   neighbor[ 7].y = coord.y+1;
      neighbor[ 8].x = coord.x-1;   neighbor[ 8].y = coord.y+1;
      neighbor[ 9].x = coord.x  ;   neighbor[ 9].y = coord.y+1;
      neighbor[10].x = coord.x+1;   neighbor[10].y = coord.y+1;
      neighbor[11].x = coord.x+2;   neighbor[11].y = coord.y+1;
      break;
   case 1: case 2:
      neighbor[ 0].x = coord.x-2;   neighbor[ 0].y = coord.y-1;
      neighbor[ 1].x = coord.x-1;   neighbor[ 1].y = coord.y-1;
      neighbor[ 2].x = coord.x  ;   neighbor[ 2].y = coord.y-1;
      neighbor[ 3].x = coord.x+1;   neighbor[ 3].y = coord.y-1;
      neighbor[ 4].x = coord.x+2;   neighbor[ 4].y = coord.y-1;
      neighbor[ 5].x = coord.x-2;   neighbor[ 5].y = coord.y  ;
      neighbor[ 6].x = coord.x-1;   neighbor[ 6].y = coord.y  ;
      neighbor[ 7].x = coord.x+1;   neighbor[ 7].y = coord.y  ;
      neighbor[ 8].x = coord.x+2;   neighbor[ 8].y = coord.y  ;
      neighbor[ 9].x = coord.x-1;   neighbor[ 9].y = coord.y+1;
      neighbor[10].x = coord.x  ;   neighbor[10].y = coord.y+1;
      neighbor[11].x = coord.x+1;   neighbor[11].y = coord.y+1;
      break;
   }
   for (int i=0; i<12; i++)
      if ((neighbor[i].x >= sizeField.x) ||
          (neighbor[i].y >= sizeField.y) ||
          (neighbor[i].x < 0) ||
          (neighbor[i].y < 0)) {
         neighbor[i] = CIncorrectCoord;
      }
}

int TcTriangle1::GetNeighborNumber() const {
   return 12;
}

POINT TcTriangle1::GetNeighborCoord(int index) const {
   return neighbor[index];
}

bool TcTriangle1::ToBelong(int x, int y) { // принадлежат ли эти экранные координаты ячейке
   POINT point = {x, y};
   return PointInPolygon(point, regionOut, 3);
}

void TcTriangle1::SetPoint(const int& area) {
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
   case 2: case 3:
      regionOut[0].x = a*(coord.x/2) + a;
      break;
   }
   regionOut[0].y = h*coord.y;
   switch (direction) {
   case 0: case 3:
      regionOut[1].x = regionOut[0].x + b;   regionOut[1].y = regionOut[0].y + h;
      regionOut[2].x = regionOut[0].x - b;   regionOut[2].y = regionOut[1].y;
      break;
   case 1: case 2:
      regionOut[1].x = regionOut[0].x - b;   regionOut[1].y = regionOut[0].y + h;
      regionOut[2].x = regionOut[0].x - a;   regionOut[2].y = regionOut[0].y;
      break;
   }

   // определение координат вписанного в фигуру квадрата - область в которую выводится изображение/текст
   POINTFLOAT center; // координата центра квадрата
   switch (direction) {
   case 0: case 3:
      center.x = regionOut[0].x;
      center.y = regionOut[1].y - sq/2;
      center.y--;
      break;
   case 1: case 2:
      center.x = regionOut[1].x;
      center.y = regionOut[0].y + sq/2;
      center.y++;
      break;
   }
   
   regionIn.left   = center.x - sq/2;
   regionIn.top    = center.y - sq/2;
   regionIn.right  = center.x + sq/2;
   regionIn.bottom = center.y + sq/2;
   regionIn.left   += 2;
   regionIn.top    += 1;
   regionIn.right  += 1;
   regionIn.bottom += 1;
}

void TcTriangle1::Paint() const {
   TB::Paint();

   SelectObject(hDC, down ? hPenWhite : hPenBlack);
   MoveToEx(hDC, regionOut[0].x, regionOut[0].y, NULL);
   LineTo  (hDC, regionOut[1].x, regionOut[1].y);
   switch (direction) {
   case 0: case 3:
      LineTo  (hDC, regionOut[2].x, regionOut[2].y);
   }

   SelectObject(hDC, down ? hPenBlack : hPenWhite);
   switch (direction) {
   case 0: case 3:
      MoveToEx(hDC, regionOut[2].x+1, regionOut[2].y  , NULL);
      LineTo  (hDC, regionOut[0].x+1, regionOut[0].y  );
      break;
   case 1: case 2:
      MoveToEx(hDC, regionOut[1].x+1, regionOut[1].y  , NULL);
      LineTo  (hDC, regionOut[2].x+1, regionOut[2].y  );
      MoveToEx(hDC, regionOut[2].x  , regionOut[2].y+1, NULL);
      LineTo  (hDC, regionOut[0].x  , regionOut[0].y+1);
      break;
   }
}

} // namespace nsFigure
