////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "TcTrapezoid1.cpp"
//
// Реализация класса TcTrapezoid1 - 3 трапеции, составляющие равносторонний треугольник
////////////////////////////////////////////////////////////////////////////////

#include ".\TcTrapezoid1.h"
#include <math.h>

namespace nsFigure {

////////////////////////////////////////////////////////////////////////////////
//                               local init
////////////////////////////////////////////////////////////////////////////////

float TcTrapezoid1::b; // большая сторона трапеции (основание)
float TcTrapezoid1::c;
float TcTrapezoid1::R; // диагональ трапеции
float TcTrapezoid1::r; // высота трапеции

////////////////////////////////////////////////////////////////////////////////
//                               static function
////////////////////////////////////////////////////////////////////////////////

POINT TcTrapezoid1::GetSizeFieldInPixel(const POINT& sizeField, const int& area) {
   a = sqrt(area/sqrt(27))*2; // меньшая сторона трапеции (верх и стороны)
 //b = a*2;
   c = a/2;
   R = a*sqrt(3);
   r = R/2;

   POINT result = {c + a*(sizeField.x+1),
                     R *((sizeField.y+1)/2)+
                     r *((sizeField.y+0)/2) + 1};
   return result;
}

int TcTrapezoid1::SizeInscribedSquare(int area) {
   a = sqrt(area/sqrt(27))*2;       // меньшая сторона трапеции (верх и стороны)
   sq = sqrt(0.375f)*a/sin(pi/180*75); // размер квадрата, вписанного в трапецию
   return sq;
}

float TcTrapezoid1::GetPercentMine(TeSkillLevel skill) { // процент мин на заданном уровне сложности
   switch (skill) {
   case skillLevelBeginner    : return 14.5f;
   case skillLevelAmateur     : return 17.5f;
   case skillLevelProfessional: return 20.5f;
   case skillLevelCrazy       : return 24.5f;
   }
   return 1.f;
}

////////////////////////////////////////////////////////////////////////////////
//                               virtual function
////////////////////////////////////////////////////////////////////////////////

TcTrapezoid1::TcTrapezoid1(const POINT& setCoord, const POINT& sizeField, const int& area) {
   coord = setCoord;
   Reset();

   direction = (coord.y%4)*3+(coord.x%3); // 0..11
   SetPoint(area);

   // определяю координаты соседей
   switch (direction) {
   case  0:
      neighbor[0].x = coord.x-1;   neighbor[0].y = coord.y-1;
      neighbor[1].x = coord.x  ;   neighbor[1].y = coord.y-1;
      neighbor[2].x = coord.x+1;   neighbor[2].y = coord.y-1;
      neighbor[3].x = coord.x-1;   neighbor[3].y = coord.y  ;
      neighbor[4].x = coord.x+1;   neighbor[4].y = coord.y  ;
      neighbor[5].x = coord.x-1;   neighbor[5].y = coord.y+1;
      neighbor[6].x = coord.x  ;   neighbor[6].y = coord.y+1;
      neighbor[7].x = coord.x+1;   neighbor[7].y = coord.y+1;
      break;
   case  1:
      neighbor[0].x = coord.x-2;   neighbor[0].y = coord.y-1;
      neighbor[1].x = coord.x-1;   neighbor[1].y = coord.y-1;
      neighbor[2].x = coord.x  ;   neighbor[2].y = coord.y-1;
      neighbor[3].x = coord.x-2;   neighbor[3].y = coord.y  ;
      neighbor[4].x = coord.x-1;   neighbor[4].y = coord.y  ;
      neighbor[5].x = coord.x+1;   neighbor[5].y = coord.y  ;  
      neighbor[6].x = coord.x  ;   neighbor[6].y = coord.y+1;
      neighbor[7].x = coord.x+1;   neighbor[7].y = coord.y+1;
      break;
   case  2:
      neighbor[0].x = coord.x-1;   neighbor[0].y = coord.y-1;
      neighbor[1].x = coord.x  ;   neighbor[1].y = coord.y-1;
      neighbor[2].x = coord.x+1;   neighbor[2].y = coord.y-1;
      neighbor[3].x = coord.x+2;   neighbor[3].y = coord.y-1;
      neighbor[4].x = coord.x-1;   neighbor[4].y = coord.y  ;
      neighbor[5].x = coord.x+1;   neighbor[5].y = coord.y  ;  
      neighbor[6].x = coord.x+2;   neighbor[6].y = coord.y  ;
      neighbor[7].x = coord.x  ;   neighbor[7].y = coord.y+1;
      break;
   case  3:
      neighbor[0].x = coord.x  ;   neighbor[0].y = coord.y-1;
      neighbor[1].x = coord.x-2;   neighbor[1].y = coord.y  ;
      neighbor[2].x = coord.x-1;   neighbor[2].y = coord.y  ;
      neighbor[3].x = coord.x+1;   neighbor[3].y = coord.y  ;
      neighbor[4].x = coord.x-2;   neighbor[4].y = coord.y+1;
      neighbor[5].x = coord.x-1;   neighbor[5].y = coord.y+1;
      neighbor[6].x = coord.x  ;   neighbor[6].y = coord.y+1;
      neighbor[7].x = coord.x+1;   neighbor[7].y = coord.y+1;
      break;
   case  4:
      neighbor[0].x = coord.x-1;   neighbor[0].y = coord.y-1;
      neighbor[1].x = coord.x  ;   neighbor[1].y = coord.y-1;
      neighbor[2].x = coord.x-1;   neighbor[2].y = coord.y  ;
      neighbor[3].x = coord.x+1;   neighbor[3].y = coord.y  ;
      neighbor[4].x = coord.x+2;   neighbor[4].y = coord.y  ;
      neighbor[5].x = coord.x  ;   neighbor[5].y = coord.y+1;
      neighbor[6].x = coord.x+1;   neighbor[6].y = coord.y+1;
      neighbor[7].x = coord.x+2;   neighbor[7].y = coord.y+1;
      break;
   case  5:
      neighbor[0].x = coord.x-1;   neighbor[0].y = coord.y-1;
      neighbor[1].x = coord.x  ;   neighbor[1].y = coord.y-1;
      neighbor[2].x = coord.x+1;   neighbor[2].y = coord.y-1;
      neighbor[3].x = coord.x-1;   neighbor[3].y = coord.y  ;
      neighbor[4].x = coord.x+1;   neighbor[4].y = coord.y  ;
      neighbor[5].x = coord.x-1;   neighbor[5].y = coord.y+1;
      neighbor[6].x = coord.x  ;   neighbor[6].y = coord.y+1;
      neighbor[7].x = coord.x+1;   neighbor[7].y = coord.y+1;
      break;
   case  6:
      neighbor[0].x = coord.x-2;   neighbor[0].y = coord.y-1;
      neighbor[1].x = coord.x-1;   neighbor[1].y = coord.y-1;
      neighbor[2].x = coord.x  ;   neighbor[2].y = coord.y-1;
      neighbor[3].x = coord.x-2;   neighbor[3].y = coord.y  ;
      neighbor[4].x = coord.x-1;   neighbor[4].y = coord.y  ;
      neighbor[5].x = coord.x+1;   neighbor[5].y = coord.y  ;
      neighbor[6].x = coord.x-1;   neighbor[6].y = coord.y+1;
      neighbor[7].x = coord.x  ;   neighbor[7].y = coord.y+1;
      break;
   case  7:
      neighbor[0].x = coord.x-1;   neighbor[0].y = coord.y-1;
      neighbor[1].x = coord.x  ;   neighbor[1].y = coord.y-1;
      neighbor[2].x = coord.x+1;   neighbor[2].y = coord.y-1;
      neighbor[3].x = coord.x+2;   neighbor[3].y = coord.y-1;
      neighbor[4].x = coord.x-1;   neighbor[4].y = coord.y  ;
      neighbor[5].x = coord.x+1;   neighbor[5].y = coord.y  ;
      neighbor[6].x = coord.x+2;   neighbor[6].y = coord.y  ;
      neighbor[7].x = coord.x-1;   neighbor[7].y = coord.y+1;
      break;
   case  8:
      neighbor[0].x = coord.x-1;   neighbor[0].y = coord.y-1;
      neighbor[1].x = coord.x  ;   neighbor[1].y = coord.y-1;
      neighbor[2].x = coord.x+1;   neighbor[2].y = coord.y-1;
      neighbor[3].x = coord.x-1;   neighbor[3].y = coord.y  ;
      neighbor[4].x = coord.x+1;   neighbor[4].y = coord.y  ;
      neighbor[5].x = coord.x-2;   neighbor[5].y = coord.y+1;
      neighbor[6].x = coord.x-1;   neighbor[6].y = coord.y+1;
      neighbor[7].x = coord.x  ;   neighbor[7].y = coord.y+1;
      break;
   case  9:
      neighbor[0].x = coord.x  ;   neighbor[0].y = coord.y-1;
      neighbor[1].x = coord.x+1;   neighbor[1].y = coord.y-1;
      neighbor[2].x = coord.x+2;   neighbor[2].y = coord.y-1;
      neighbor[3].x = coord.x-1;   neighbor[3].y = coord.y  ;
      neighbor[4].x = coord.x+1;   neighbor[4].y = coord.y  ;
      neighbor[5].x = coord.x-1;   neighbor[5].y = coord.y+1;
      neighbor[6].x = coord.x  ;   neighbor[6].y = coord.y+1;
      neighbor[7].x = coord.x+1;   neighbor[7].y = coord.y+1;
      break;
   case 10:
      neighbor[0].x = coord.x+1;   neighbor[0].y = coord.y-1;
      neighbor[1].x = coord.x-2;   neighbor[1].y = coord.y  ;
      neighbor[2].x = coord.x-1;   neighbor[2].y = coord.y  ;
      neighbor[3].x = coord.x+1;   neighbor[3].y = coord.y  ;
      neighbor[4].x = coord.x-2;   neighbor[4].y = coord.y+1;
      neighbor[5].x = coord.x-1;   neighbor[5].y = coord.y+1;
      neighbor[6].x = coord.x  ;   neighbor[6].y = coord.y+1;
      neighbor[7].x = coord.x+1;   neighbor[7].y = coord.y+1;
      break;
   case 11:
      neighbor[0].x = coord.x  ;   neighbor[0].y = coord.y-1;
      neighbor[1].x = coord.x+1;   neighbor[1].y = coord.y-1;
      neighbor[2].x = coord.x-1;   neighbor[2].y = coord.y  ;
      neighbor[3].x = coord.x+1;   neighbor[3].y = coord.y  ;
      neighbor[4].x = coord.x+2;   neighbor[4].y = coord.y  ;
      neighbor[5].x = coord.x  ;   neighbor[5].y = coord.y+1;
      neighbor[6].x = coord.x+1;   neighbor[6].y = coord.y+1;
      neighbor[7].x = coord.x+2;   neighbor[7].y = coord.y+1;
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

int TcTrapezoid1::GetNeighborNumber() const {
   return 8;
}

POINT TcTrapezoid1::GetNeighborCoord(int index) const {
   return neighbor[index];
}

bool TcTrapezoid1::ToBelong(int x, int y) { // принадлежат ли эти экранные координаты ячейке
   POINT point = {x, y};
   return PointInPolygon(point, regionOut, 4);
}

void TcTrapezoid1::SetPoint(const int& area) {
   if (coord.x==0 && coord.y==0) {
      a = sqrt(area/sqrt(27))*2;
      b = a*2;
      c = a/2;
      R = a*sqrt(3);
      r = R/2;
      sq = sqrt(0.375f)*a/sin(pi/180*75); // размер квадрата, вписанного в трапецию
      TB::SetPoint(NULL);
   }

   // определение координат точек фигуры
   switch (direction) {
   case 6:
      regionOut[0].x = (a+b)*(coord.x/3) + a;     break;
   case 0: case 3:
      regionOut[0].x = (a+b)*(coord.x/3) + a+c;   break;
   case 4:
      regionOut[0].x = (a+b)*(coord.x/3) + b;     break;
   case 1: case 9:
      regionOut[0].x = (a+b)*(coord.x/3) + b+c;   break;
   case 7: case 8: case 10:
      regionOut[0].x = (a+b)*(coord.x/3) + a+b;   break;
   case 11:
      regionOut[0].x = (a+b)*(coord.x/3) + a+b+c; break;
   case 5:
      regionOut[0].x = (a+b)*(coord.x/3) + b*2;   break;
   case 2:
      regionOut[0].x = (a+b)*(coord.x/3) + b*2+c; break;
   }
   switch (direction) {
   case 0: case 1: case 2:
      regionOut[0].y = (R+r)*(coord.y/4*2);          break;
   case 4: case 5:
      regionOut[0].y = (R+r)*(coord.y/4*2) + r;      break;
   case 3:
      regionOut[0].y = (R+r)*(coord.y/4*2) + R;      break;
   case 6: case 7: case 8:
      regionOut[0].y = (R+r)*(coord.y/4*2) + R+r;    break;
   case 9: case 11:
      regionOut[0].y = (R+r)*(coord.y/4*2) + R*2;    break;
   case 10:
      regionOut[0].y = (R+r)*(coord.y/4*2) + R*2+r;  break;
   }
   switch (direction) {
   case 0: case 8:
      regionOut[1].x = regionOut[0].x+c;   regionOut[1].y = regionOut[0].y+r;
      regionOut[2].x = regionOut[0].x  ;   regionOut[2].y = regionOut[0].y+R;
      regionOut[3].x = regionOut[0].x-a;   regionOut[3].y = regionOut[2].y  ;
      break;
   case 1: case 6:
      regionOut[1].x = regionOut[0].x+c;   regionOut[1].y = regionOut[0].y+r;
      regionOut[2].x = regionOut[0].x  ;   regionOut[2].y = regionOut[0].y+R;
      regionOut[3].x = regionOut[0].x-a;   regionOut[3].y = regionOut[0].y  ;
      break;
   case 2: case 7:
      regionOut[1].x = regionOut[0].x-c;   regionOut[1].y = regionOut[0].y+r;
      regionOut[2].x = regionOut[1].x-a;   regionOut[2].y = regionOut[1].y  ;
      regionOut[3].x = regionOut[0].x-b;   regionOut[3].y = regionOut[0].y  ;
      break;
   case 3: case 10:
      regionOut[1].x = regionOut[0].x+c;   regionOut[1].y = regionOut[0].y+r;
      regionOut[2].x = regionOut[1].x-b;   regionOut[2].y = regionOut[1].y  ;
      regionOut[3].x = regionOut[0].x-a;   regionOut[3].y = regionOut[0].y  ;
      break;
   case 4: case 11:
      regionOut[1].x = regionOut[0].x+a;   regionOut[1].y = regionOut[0].y+R;
      regionOut[2].x = regionOut[0].x  ;   regionOut[2].y = regionOut[1].y  ;
      regionOut[3].x = regionOut[0].x-c;   regionOut[3].y = regionOut[0].y+r;
      break;
   case 5: case 9:
      regionOut[1].x = regionOut[0].x-a;   regionOut[1].y = regionOut[0].y+R;
      regionOut[2].x = regionOut[1].x-c;   regionOut[2].y = regionOut[0].y+r;
      regionOut[3].x = regionOut[1].x  ;   regionOut[3].y = regionOut[0].y  ;
      break;
   }

   // определение координат вписанного в фигуру квадрата - область в которую выводится изображение/текст
   switch (direction) {
   case 0: case 8:
      regionIn.right  = regionOut[2].x  + c/2;
      regionIn.bottom = regionOut[2].y  - r/2;
      regionIn.left   = regionIn.right  - sq;
      regionIn.top    = regionIn.bottom - sq;
      break;
   case 1: case 6:
      regionIn.right  = regionOut[1].x  - c/2;
      regionIn.top    = regionOut[1].y  - r/2;
      regionIn.left   = regionIn.right  - sq;
      regionIn.bottom = regionIn.top    + sq;
      break;
   case 2: case 7:
      regionIn.left   = regionOut[2].x  - sq/2 + c   ;
      regionIn.bottom = regionOut[2].y  + sq/2 - r/2 ;
      regionIn.right  = regionIn.left   + sq;
      regionIn.top    = regionIn.bottom - sq;
      break;
   case 3: case 10:
      regionIn.left   = regionOut[0].x  - sq/2 - c   ;
      regionIn.top    = regionOut[0].y  - sq/2 + r/2 ;
      regionIn.right  = regionIn.left   + sq;
      regionIn.bottom = regionIn.top    + sq;
      break;
   case 4: case 11:
      regionIn.left   = regionOut[3].x  + c/2;
      regionIn.bottom = regionOut[3].y  + r/2;
      regionIn.right  = regionIn.left   + sq;
      regionIn.top    = regionIn.bottom - sq;
      break;
   case 5: case 9:
      regionIn.left   = regionOut[3].x  - c/2;
      regionIn.top    = regionOut[3].y  + r/2;
      regionIn.right  = regionIn.left   + sq;
      regionIn.bottom = regionIn.top    + sq;
      break;
   }
   regionIn.left   += 1;
   regionIn.top    += 1;
   regionIn.right  += 1;
   regionIn.bottom += 1;
}

void TcTrapezoid1::Paint() const {
   TB::Paint();

   SelectObject(hDC, down ? hPenWhite : hPenBlack);
   MoveToEx(hDC, regionOut[0].x, regionOut[0].y, NULL);
   LineTo  (hDC, regionOut[1].x, regionOut[1].y);
   switch (direction) {
   case 0: case 1: case 2: case 3:  case 4:
   case 8: case 6: case 7: case 10: case 11:
      LineTo  (hDC, regionOut[2].x, regionOut[2].y);
   }
   switch (direction) {
   case 0: case 8:
      LineTo  (hDC, regionOut[3].x, regionOut[3].y);
   }

   SelectObject(hDC, down ? hPenBlack : hPenWhite);
   switch (direction) {
   case 4: case 11:
      MoveToEx(hDC, regionOut[2].x+1, regionOut[2].y  , NULL);
      LineTo  (hDC, regionOut[3].x+1, regionOut[3].y  );
   case 0: case 8:
      MoveToEx(hDC, regionOut[3].x+1, regionOut[3].y  , NULL);
      LineTo  (hDC, regionOut[0].x+1, regionOut[0].y  );
      break;
   case 5: case 9:
      MoveToEx(hDC, regionOut[1].x+1, regionOut[1].y  , NULL);
      LineTo  (hDC, regionOut[2].x+1, regionOut[2].y  );
   case 1: case 6:
   case 2: case 7:
   case 3: case 10:
      MoveToEx(hDC, regionOut[2].x+1, regionOut[2].y  , NULL);
      LineTo  (hDC, regionOut[3].x+1, regionOut[3].y  );
      MoveToEx(hDC, regionOut[3].x  , regionOut[3].y+1, NULL);
      LineTo  (hDC, regionOut[0].x  , regionOut[0].y+1);
      break;
   }
}

} // namespace nsFigure
