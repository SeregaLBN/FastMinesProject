////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "TcRhombus.cpp"
//
// Реализация класса TcRhombus - 3 трапеции, составляющие равносторонний треугольник
////////////////////////////////////////////////////////////////////////////////

#include ".\TcRhombus.h"
#include <math.h>

namespace nsFigure {

////////////////////////////////////////////////////////////////////////////////
//                               local init
////////////////////////////////////////////////////////////////////////////////

float TcRhombus::r;
float TcRhombus::h;
float TcRhombus::c;

////////////////////////////////////////////////////////////////////////////////
//                               static function
////////////////////////////////////////////////////////////////////////////////

POINT TcRhombus::GetSizeFieldInPixel(const POINT& sizeField, const int& area) {
   a = sqrt(area*2/sqrt(3));
   r = a*sqrt(3)/2;
 //h = r*2;
   c = a/2;

   POINT result = {c +  a   *((sizeField.x+2)/3) +
                       (a+c)*((sizeField.x+1)/3) +
                          c *((sizeField.x+0)/3),
                          r * (sizeField.y+1)};
   return result;
}

int TcRhombus::SizeInscribedSquare(int area) {
   a  = sqrt(area*2/sqrt(3));
   sq = sqrt(3/2)*a/2/sin(pi/180*75);
   return sq;
}

float TcRhombus::GetPercentMine(TeSkillLevel skill) { // процент мин на заданном уровне сложности
   switch (skill) {
   case skillLevelBeginner    : return 15.6f;
   case skillLevelAmateur     : return 18.6f;
   case skillLevelProfessional: return 21.6f;
   case skillLevelCrazy       : return 25.6f;
   }
   return 1.f;
}

////////////////////////////////////////////////////////////////////////////////
//                               virtual function
////////////////////////////////////////////////////////////////////////////////

TcRhombus::TcRhombus(const POINT& setCoord, const POINT& sizeField, const int& area) {
   coord = setCoord;
   Reset();

   direction = (coord.y%2)*3+(coord.x%3); // 0..5
   SetPoint(area);

   // определяю координаты соседей
   switch (direction) {
   case  0:
      neighbor[0].x = coord.x+1;   neighbor[0].y = coord.y-2;
      neighbor[1].x = coord.x+2;   neighbor[1].y = coord.y-2;
      neighbor[2].x = coord.x-2;   neighbor[2].y = coord.y-1;
      neighbor[3].x = coord.x-1;   neighbor[3].y = coord.y-1;
      neighbor[4].x = coord.x  ;   neighbor[4].y = coord.y-1;
      neighbor[5].x = coord.x+1;   neighbor[5].y = coord.y-1;
      neighbor[6].x = coord.x-1;   neighbor[6].y = coord.y  ;
      neighbor[7].x = coord.x+1;   neighbor[7].y = coord.y  ;
      neighbor[8].x = coord.x-1;   neighbor[8].y = coord.y+1;
      neighbor[9].x = coord.x  ;   neighbor[9].y = coord.y+1;
      break;
   case  1:
      neighbor[0].x = coord.x  ;   neighbor[0].y = coord.y-2;
      neighbor[1].x = coord.x+1;   neighbor[1].y = coord.y-2;
      neighbor[2].x = coord.x-1;   neighbor[2].y = coord.y-1;
      neighbor[3].x = coord.x  ;   neighbor[3].y = coord.y-1;
      neighbor[4].x = coord.x-1;   neighbor[4].y = coord.y  ;
      neighbor[5].x = coord.x+1;   neighbor[5].y = coord.y  ;  
      neighbor[6].x = coord.x-1;   neighbor[6].y = coord.y+1;
      neighbor[7].x = coord.x  ;   neighbor[7].y = coord.y+1;
      neighbor[8].x = coord.x-1;   neighbor[8].y = coord.y+2;
      neighbor[9].x = coord.x  ;   neighbor[9].y = coord.y+2;
      break;
   case  2:
      neighbor[0].x = coord.x-1;   neighbor[0].y = coord.y-1;
      neighbor[1].x = coord.x  ;   neighbor[1].y = coord.y-1;
      neighbor[2].x = coord.x-1;   neighbor[2].y = coord.y  ;
      neighbor[3].x = coord.x+1;   neighbor[3].y = coord.y  ;
      neighbor[4].x = coord.x-2;   neighbor[4].y = coord.y+1;
      neighbor[5].x = coord.x-1;   neighbor[5].y = coord.y+1;  
      neighbor[6].x = coord.x  ;   neighbor[6].y = coord.y+1;
      neighbor[7].x = coord.x+1;   neighbor[7].y = coord.y+1;
      neighbor[8].x = coord.x-2;   neighbor[8].y = coord.y+2;
      neighbor[9].x = coord.x-1;   neighbor[9].y = coord.y+2;
      break;
   case  3:
      neighbor[0].x = coord.x-2;   neighbor[0].y = coord.y-2;
      neighbor[1].x = coord.x-1;   neighbor[1].y = coord.y-2;
      neighbor[2].x = coord.x-1;   neighbor[2].y = coord.y-1;
      neighbor[3].x = coord.x  ;   neighbor[3].y = coord.y-1;
      neighbor[4].x = coord.x+1;   neighbor[4].y = coord.y-1;
      neighbor[5].x = coord.x+2;   neighbor[5].y = coord.y-1;
      neighbor[6].x = coord.x-1;   neighbor[6].y = coord.y  ;
      neighbor[7].x = coord.x+1;   neighbor[7].y = coord.y  ;
      neighbor[8].x = coord.x  ;   neighbor[8].y = coord.y+1;
      neighbor[9].x = coord.x+1;   neighbor[9].y = coord.y+1;
      break;
   case  4:
      neighbor[0].x = coord.x  ;   neighbor[0].y = coord.y-1;
      neighbor[1].x = coord.x+1;   neighbor[1].y = coord.y-1;
      neighbor[2].x = coord.x-1;   neighbor[2].y = coord.y  ;
      neighbor[3].x = coord.x+1;   neighbor[3].y = coord.y  ;
      neighbor[4].x = coord.x-1;   neighbor[4].y = coord.y+1;
      neighbor[5].x = coord.x  ;   neighbor[5].y = coord.y+1;
      neighbor[6].x = coord.x+1;   neighbor[6].y = coord.y+1;
      neighbor[7].x = coord.x+2;   neighbor[7].y = coord.y+1;
      neighbor[8].x = coord.x+1;   neighbor[8].y = coord.y+2;
      neighbor[9].x = coord.x+2;   neighbor[9].y = coord.y+2;
      break;
   case  5:
      neighbor[0].x = coord.x-1;   neighbor[0].y = coord.y-2;
      neighbor[1].x = coord.x  ;   neighbor[1].y = coord.y-2;
      neighbor[2].x = coord.x  ;   neighbor[2].y = coord.y-1;
      neighbor[3].x = coord.x+1;   neighbor[3].y = coord.y-1;
      neighbor[4].x = coord.x-1;   neighbor[4].y = coord.y  ;
      neighbor[5].x = coord.x+1;   neighbor[5].y = coord.y  ;
      neighbor[6].x = coord.x  ;   neighbor[6].y = coord.y+1;
      neighbor[7].x = coord.x+1;   neighbor[7].y = coord.y+1;
      neighbor[8].x = coord.x  ;   neighbor[8].y = coord.y+2;
      neighbor[9].x = coord.x+1;   neighbor[9].y = coord.y+2;
      break;
   }
   for (int i=0; i<10; i++)
      if ((neighbor[i].x >= sizeField.x) ||
          (neighbor[i].y >= sizeField.y) ||
          (neighbor[i].x < 0) ||
          (neighbor[i].y < 0)) {
         neighbor[i] = CIncorrectCoord;
      }
}

int TcRhombus::GetNeighborNumber() const {
   return 10;
}

POINT TcRhombus::GetNeighborCoord(int index) const {
   return neighbor[index];
}

bool TcRhombus::ToBelong(int x, int y) { // принадлежат ли эти экранные координаты ячейке
   POINT point = {x, y};
   return PointInPolygon(point, regionOut, 4);
}

void TcRhombus::SetPoint(const int& area) {
   if (coord.x==0 && coord.y==0) {
      a = sqrt(area*2/sqrt(3));
      r = a*sqrt(3)/2;
      h = r*2;
      c = a/2;
      sq = sqrt(3/2)*a/2/sin(pi/180*75);
      TB::SetPoint(NULL);
   }

   // определение координат точек фигуры
   switch (direction) {
   case 0:
   case 1:
   case 3:  regionOut[0].x = a     + a*(coord.x/3*3);   regionOut[0].y = r + h*(coord.y/2);   break;
   case 2:
   case 4:
   case 5:  regionOut[0].x = a*2+c + a*(coord.x/3*3);   regionOut[0].y = h + h*(coord.y/2);   break;
   }
   switch (direction) {
   case 0:
   case 2:
      regionOut[1].x = regionOut[0].x-a;   regionOut[1].y = regionOut[0].y  ;
      regionOut[2].x = regionOut[0].x-c;   regionOut[2].y = regionOut[0].y-r;
      regionOut[3].x = regionOut[0].x+c;   regionOut[3].y = regionOut[2].y  ;
      break;
   case 1:
   case 5:
      regionOut[1].x = regionOut[0].x+c;   regionOut[1].y = regionOut[0].y-r;
      regionOut[2].x = regionOut[0].x+a;   regionOut[2].y = regionOut[0].y  ;
      regionOut[3].x = regionOut[1].x  ;   regionOut[3].y = regionOut[0].y+r;
      break;
   case 3:
   case 4:
      regionOut[1].x = regionOut[0].x+c;   regionOut[1].y = regionOut[0].y+r;
      regionOut[2].x = regionOut[0].x-c;   regionOut[2].y = regionOut[1].y  ;
      regionOut[3].x = regionOut[0].x-a;   regionOut[3].y = regionOut[0].y  ;
      break;
   }

   // определение координат вписанного в фигуру квадрата - область в которую выводится изображение/текст
   switch (direction) {
   case 0:
   case 2:
      regionIn.left   = regionOut[2].x + c/2 - sq/2;
      regionIn.top    = regionOut[2].y + r/2 - sq/2;
      regionIn.right  = regionIn.left + sq;
      regionIn.bottom = regionIn.top  + sq;
      break;
   case 1:
   case 5:
      regionIn.left   = regionOut[1].x - sq/2;
      regionIn.top    = regionOut[0].y - sq/2;
      regionIn.right  = regionIn.left + sq;
      regionIn.bottom = regionIn.top  + sq;
      break;
   case 3:
   case 4:
      regionIn.left   = regionOut[2].x + c/2 - sq/2;
      regionIn.top    = regionOut[3].y + r/2 - sq/2;
      regionIn.right  = regionIn.left + sq;
      regionIn.bottom = regionIn.top  + sq;
      break;
   }
   regionIn.left   += 0;
   regionIn.top    += 0;
   regionIn.right  += 0;
   regionIn.bottom += 0;
}

void TcRhombus::Paint() const {
   TB::Paint();

   SelectObject(hDC, down ? hPenBlack : hPenWhite);
   switch (direction) {
   case 0:
   case 2:
      MoveToEx(hDC, regionOut[1].x, regionOut[1].y, NULL);
      LineTo  (hDC, regionOut[2].x, regionOut[2].y);
      LineTo  (hDC, regionOut[3].x, regionOut[3].y);
      break;
   case 1:
   case 5:
      MoveToEx(hDC, regionOut[3].x, regionOut[3].y, NULL);
      LineTo  (hDC, regionOut[0].x, regionOut[0].y);
      LineTo  (hDC, regionOut[1].x, regionOut[1].y);
      break;
   case 3:
   case 4:
      MoveToEx(hDC, regionOut[2].x, regionOut[2].y, NULL);
      LineTo  (hDC, regionOut[3].x, regionOut[3].y);
      LineTo  (hDC, regionOut[0].x, regionOut[0].y);
      break;
   }
   SelectObject(hDC, down ? hPenWhite : hPenBlack);
   switch (direction) {
   case 0:
   case 2:
      MoveToEx(hDC, regionOut[3].x-1, regionOut[3].y  , NULL);
      LineTo  (hDC, regionOut[0].x-1, regionOut[0].y  );
      MoveToEx(hDC, regionOut[0].x  , regionOut[0].y-1, NULL);
      LineTo  (hDC, regionOut[1].x  , regionOut[1].y-1);
      break;
   case 1:
   case 5:
      MoveToEx(hDC, regionOut[1].x-1, regionOut[1].y  , NULL);
      LineTo  (hDC, regionOut[2].x-1, regionOut[2].y  );
      MoveToEx(hDC, regionOut[2].x-1, regionOut[2].y  , NULL);
      LineTo  (hDC, regionOut[3].x-1, regionOut[3].y  );
      break;
   case 3:
   case 4:
      MoveToEx(hDC, regionOut[0].x-1, regionOut[0].y  , NULL);
      LineTo  (hDC, regionOut[1].x-1, regionOut[1].y  );
      MoveToEx(hDC, regionOut[1].x  , regionOut[1].y-1, NULL);
      LineTo  (hDC, regionOut[2].x  , regionOut[2].y-1);
      break;
   }
}

} // namespace nsFigure
