////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "TcTrapezoid3.cpp"
//
// Реализация класса TcTrapezoid3 - 3 трапеции, составляющие равносторонний треугольник
////////////////////////////////////////////////////////////////////////////////

#include ".\TcTrapezoid3.h"
#include <math.h>

namespace nsFigure {

////////////////////////////////////////////////////////////////////////////////
//                               local init
////////////////////////////////////////////////////////////////////////////////

float TcTrapezoid3::b; // большая сторона трапеции (основание)
float TcTrapezoid3::c;
float TcTrapezoid3::R; // диагональ трапеции
float TcTrapezoid3::r; // высота трапеции

////////////////////////////////////////////////////////////////////////////////
//                               static function
////////////////////////////////////////////////////////////////////////////////

POINT TcTrapezoid3::GetSizeFieldInPixel(const POINT& sizeField, const int& area) {
   a = sqrt(area/sqrt(27))*2; // меньшая сторона трапеции (верх и стороны)
   b = a*2;
   c = a/2;
   R = a*sqrt(3);
   r = R/2;

   POINT result = {    R*((sizeField.x+1)/2) + 1,
                   a + b*((sizeField.y+1)/2)+
                       a*((sizeField.y+0)/2)};
   return result;
}

int TcTrapezoid3::SizeInscribedSquare(int area) {
   a = sqrt(area/sqrt(27))*2;       // меньшая сторона трапеции (верх и стороны)
   sq = sqrt(0.375f)*a/sin(pi/180*75); // размер квадрата, вписанного в трапецию
   return sq;
}

float TcTrapezoid3::GetPercentMine(TeSkillLevel skill) { // процент мин на заданном уровне сложности
   switch (skill) {
   case skillLevelBeginner    : return 16.015f;
   case skillLevelAmateur     : return 19.015f;
   case skillLevelProfessional: return 22.015f;
   case skillLevelCrazy       : return 26.015f;
   }
   return 1.f;
}

////////////////////////////////////////////////////////////////////////////////
//                               virtual function
////////////////////////////////////////////////////////////////////////////////

TcTrapezoid3::TcTrapezoid3(const POINT& setCoord, const POINT& sizeField, const int& area) {
   coord = setCoord;
   Reset();

   direction = (coord.y%4)*4+(coord.x%4); // 0..15
   SetPoint(area);

   // определяю координаты соседей
   switch (direction) {
   case  0:
      neighbor[ 0].x = coord.x-1;   neighbor[ 0].y = coord.y-1;
      neighbor[ 1].x = coord.x+1;   neighbor[ 1].y = coord.y-1;
      neighbor[ 2].x = coord.x-1;   neighbor[ 2].y = coord.y  ;
      neighbor[ 3].x = coord.x+1;   neighbor[ 3].y = coord.y  ;
      neighbor[ 4].x = coord.x-2;   neighbor[ 4].y = coord.y+1;
      neighbor[ 5].x = coord.x-1;   neighbor[ 5].y = coord.y+1;
      neighbor[ 6].x = coord.x  ;   neighbor[ 6].y = coord.y+1;
      neighbor[ 7].x = coord.x+1;   neighbor[ 7].y = coord.y+1;
      neighbor[ 8].x = coord.x-2;   neighbor[ 8].y = coord.y+2;
      neighbor[ 9].x = coord.x  ;   neighbor[ 9].y = coord.y+2;
      neighbor[10].x = -1       ;   neighbor[10].y = -1       ;
      break;
   case  1:
      neighbor[ 0].x = coord.x  ;   neighbor[ 0].y = coord.y-2;
      neighbor[ 1].x = coord.x-2;   neighbor[ 1].y = coord.y-1;
      neighbor[ 2].x = coord.x  ;   neighbor[ 2].y = coord.y-1;
      neighbor[ 3].x = coord.x+1;   neighbor[ 3].y = coord.y-1;
      neighbor[ 4].x = coord.x+2;   neighbor[ 4].y = coord.y-1;
      neighbor[ 5].x = coord.x-2;   neighbor[ 5].y = coord.y  ;
      neighbor[ 6].x = coord.x-1;   neighbor[ 6].y = coord.y  ;
      neighbor[ 7].x = coord.x+1;   neighbor[ 7].y = coord.y  ;
      neighbor[ 8].x = coord.x+2;   neighbor[ 8].y = coord.y  ;
      neighbor[ 9].x = coord.x-2;   neighbor[ 9].y = coord.y+1;
      neighbor[10].x = coord.x  ;   neighbor[10].y = coord.y+1;
      break;
   case  2:
      neighbor[ 0].x = coord.x-1;   neighbor[ 0].y = coord.y  ;
      neighbor[ 1].x = coord.x+1;   neighbor[ 1].y = coord.y  ;
      neighbor[ 2].x = coord.x-2;   neighbor[ 2].y = coord.y+1;
      neighbor[ 3].x = coord.x-1;   neighbor[ 3].y = coord.y+1;
      neighbor[ 4].x = coord.x  ;   neighbor[ 4].y = coord.y+1;
      neighbor[ 5].x = coord.x+1;   neighbor[ 5].y = coord.y+1;
      neighbor[ 6].x = -1       ;   neighbor[ 6].y = -1       ;
      neighbor[ 7].x = -1       ;   neighbor[ 7].y = -1       ;
      neighbor[ 8].x = -1       ;   neighbor[ 8].y = -1       ;
      neighbor[ 9].x = -1       ;   neighbor[ 9].y = -1       ;
      neighbor[10].x = -1       ;   neighbor[10].y = -1       ;
      break;
   case  3:
      neighbor[ 0].x = coord.x-2;   neighbor[ 0].y = coord.y-2;
      neighbor[ 1].x = coord.x-2;   neighbor[ 1].y = coord.y-1;
      neighbor[ 2].x = coord.x-1;   neighbor[ 2].y = coord.y-1;
      neighbor[ 3].x = coord.x  ;   neighbor[ 3].y = coord.y-1;
      neighbor[ 4].x = coord.x+2;   neighbor[ 4].y = coord.y-1;
      neighbor[ 5].x = coord.x-2;   neighbor[ 5].y = coord.y  ;
      neighbor[ 6].x = coord.x-1;   neighbor[ 6].y = coord.y  ;
      neighbor[ 7].x = coord.x+1;   neighbor[ 7].y = coord.y  ;
      neighbor[ 8].x = coord.x+2;   neighbor[ 8].y = coord.y  ;
      neighbor[ 9].x = coord.x-2;   neighbor[ 9].y = coord.y+1;
      neighbor[10].x = coord.x  ;   neighbor[10].y = coord.y+1;
      break;
   case  4:
      neighbor[ 0].x = coord.x  ;   neighbor[ 0].y = coord.y-1;
      neighbor[ 1].x = coord.x+2;   neighbor[ 1].y = coord.y-1;
      neighbor[ 2].x = coord.x-2;   neighbor[ 2].y = coord.y  ;
      neighbor[ 3].x = coord.x-1;   neighbor[ 3].y = coord.y  ;
      neighbor[ 4].x = coord.x+1;   neighbor[ 4].y = coord.y  ;
      neighbor[ 5].x = coord.x+2;   neighbor[ 5].y = coord.y  ;
      neighbor[ 6].x = coord.x-2;   neighbor[ 6].y = coord.y+1;
      neighbor[ 7].x = coord.x  ;   neighbor[ 7].y = coord.y+1;
      neighbor[ 8].x = coord.x+1;   neighbor[ 8].y = coord.y+1;
      neighbor[ 9].x = coord.x+2;   neighbor[ 9].y = coord.y+1;
      neighbor[10].x = coord.x+2;   neighbor[10].y = coord.y+2;
      break;
   case  5:
      neighbor[ 0].x = coord.x-1;   neighbor[ 0].y = coord.y-1;
      neighbor[ 1].x = coord.x  ;   neighbor[ 1].y = coord.y-1;
      neighbor[ 2].x = coord.x+1;   neighbor[ 2].y = coord.y-1;
      neighbor[ 3].x = coord.x+2;   neighbor[ 3].y = coord.y-1;
      neighbor[ 4].x = coord.x-1;   neighbor[ 4].y = coord.y  ;
      neighbor[ 5].x = coord.x+1;   neighbor[ 5].y = coord.y  ;
      neighbor[ 6].x = -1       ;   neighbor[ 6].y = -1       ;
      neighbor[ 7].x = -1       ;   neighbor[ 7].y = -1       ;
      neighbor[ 8].x = -1       ;   neighbor[ 8].y = -1       ;
      neighbor[ 9].x = -1       ;   neighbor[ 9].y = -1       ;
      neighbor[10].x = -1       ;   neighbor[10].y = -1       ;
      break;
   case  6:
      neighbor[ 0].x = coord.x  ;   neighbor[ 0].y = coord.y-1;
      neighbor[ 1].x = coord.x+2;   neighbor[ 1].y = coord.y-1;
      neighbor[ 2].x = coord.x-2;   neighbor[ 2].y = coord.y  ;
      neighbor[ 3].x = coord.x-1;   neighbor[ 3].y = coord.y  ;
      neighbor[ 4].x = coord.x+1;   neighbor[ 4].y = coord.y  ;
      neighbor[ 5].x = coord.x+2;   neighbor[ 5].y = coord.y  ;
      neighbor[ 6].x = coord.x-2;   neighbor[ 6].y = coord.y+1;
      neighbor[ 7].x = coord.x-1;   neighbor[ 7].y = coord.y+1;
      neighbor[ 8].x = coord.x  ;   neighbor[ 8].y = coord.y+1;
      neighbor[ 9].x = coord.x+2;   neighbor[ 9].y = coord.y+1;
      neighbor[10].x = coord.x  ;   neighbor[10].y = coord.y+2;
      break;
   case  7:
      neighbor[ 0].x = coord.x  ;   neighbor[ 0].y = coord.y-2;
      neighbor[ 1].x = coord.x+2;   neighbor[ 1].y = coord.y-2;
      neighbor[ 2].x = coord.x-1;   neighbor[ 2].y = coord.y-1;
      neighbor[ 3].x = coord.x  ;   neighbor[ 3].y = coord.y-1;
      neighbor[ 4].x = coord.x+1;   neighbor[ 4].y = coord.y-1;
      neighbor[ 5].x = coord.x+2;   neighbor[ 5].y = coord.y-1;
      neighbor[ 6].x = coord.x-1;   neighbor[ 6].y = coord.y  ;
      neighbor[ 7].x = coord.x+1;   neighbor[ 7].y = coord.y  ;
      neighbor[ 8].x = coord.x-1;   neighbor[ 8].y = coord.y+1;
      neighbor[ 9].x = coord.x+1;   neighbor[ 9].y = coord.y+1;
      neighbor[10].x = -1       ;   neighbor[10].y = -1       ;
      break;
   case  8:
      neighbor[ 0].x = coord.x  ;   neighbor[ 0].y = coord.y-2;
      neighbor[ 1].x = coord.x-2;   neighbor[ 1].y = coord.y-1;
      neighbor[ 2].x = coord.x-1;   neighbor[ 2].y = coord.y-1;
      neighbor[ 3].x = coord.x  ;   neighbor[ 3].y = coord.y-1;
      neighbor[ 4].x = coord.x+2;   neighbor[ 4].y = coord.y-1;
      neighbor[ 5].x = coord.x-2;   neighbor[ 5].y = coord.y  ;
      neighbor[ 6].x = coord.x-1;   neighbor[ 6].y = coord.y  ;
      neighbor[ 7].x = coord.x+1;   neighbor[ 7].y = coord.y  ;
      neighbor[ 8].x = coord.x+2;   neighbor[ 8].y = coord.y  ;
      neighbor[ 9].x = coord.x  ;   neighbor[ 9].y = coord.y+1;
      neighbor[10].x = coord.x+2;   neighbor[10].y = coord.y+1;
      break;
   case  9:
      neighbor[ 0].x = coord.x-1;   neighbor[ 0].y = coord.y-1;
      neighbor[ 1].x = coord.x+1;   neighbor[ 1].y = coord.y-1;
      neighbor[ 2].x = coord.x-1;   neighbor[ 2].y = coord.y  ;
      neighbor[ 3].x = coord.x+1;   neighbor[ 3].y = coord.y  ;
      neighbor[ 4].x = coord.x-1;   neighbor[ 4].y = coord.y+1;
      neighbor[ 5].x = coord.x  ;   neighbor[ 5].y = coord.y+1;
      neighbor[ 6].x = coord.x+1;   neighbor[ 6].y = coord.y+1;
      neighbor[ 7].x = coord.x+2;   neighbor[ 7].y = coord.y+1;
      neighbor[ 8].x = coord.x  ;   neighbor[ 8].y = coord.y+2;
      neighbor[ 9].x = coord.x+2;   neighbor[ 9].y = coord.y+2;
      neighbor[10].x = -1       ;   neighbor[10].y = -1       ;
      break;
   case 10:
      neighbor[ 0].x = coord.x+2;   neighbor[ 0].y = coord.y-2;
      neighbor[ 1].x = coord.x-2;   neighbor[ 1].y = coord.y-1;
      neighbor[ 2].x = coord.x  ;   neighbor[ 2].y = coord.y-1;
      neighbor[ 3].x = coord.x+1;   neighbor[ 3].y = coord.y-1;
      neighbor[ 4].x = coord.x+2;   neighbor[ 4].y = coord.y-1;
      neighbor[ 5].x = coord.x-2;   neighbor[ 5].y = coord.y  ;
      neighbor[ 6].x = coord.x-1;   neighbor[ 6].y = coord.y  ;
      neighbor[ 7].x = coord.x+1;   neighbor[ 7].y = coord.y  ;
      neighbor[ 8].x = coord.x+2;   neighbor[ 8].y = coord.y  ;
      neighbor[ 9].x = coord.x  ;   neighbor[ 9].y = coord.y+1;
      neighbor[10].x = coord.x+2;   neighbor[10].y = coord.y+1;
      break;
   case 11:
      neighbor[ 0].x = coord.x-1;   neighbor[ 0].y = coord.y  ;
      neighbor[ 1].x = coord.x+1;   neighbor[ 1].y = coord.y  ;
      neighbor[ 2].x = coord.x-1;   neighbor[ 2].y = coord.y+1;
      neighbor[ 3].x = coord.x  ;   neighbor[ 3].y = coord.y+1;
      neighbor[ 4].x = coord.x+1;   neighbor[ 4].y = coord.y+1;
      neighbor[ 5].x = coord.x+2;   neighbor[ 5].y = coord.y+1;
      neighbor[ 6].x = -1       ;   neighbor[ 6].y = -1       ;
      neighbor[ 7].x = -1       ;   neighbor[ 7].y = -1       ;
      neighbor[ 8].x = -1       ;   neighbor[ 8].y = -1       ;
      neighbor[ 9].x = -1       ;   neighbor[ 9].y = -1       ;
      neighbor[10].x = -1       ;   neighbor[10].y = -1       ;
      break;
   case 12:
      neighbor[ 0].x = coord.x-2;   neighbor[ 0].y = coord.y-1;
      neighbor[ 1].x = coord.x-1;   neighbor[ 1].y = coord.y-1;
      neighbor[ 2].x = coord.x  ;   neighbor[ 2].y = coord.y-1;
      neighbor[ 3].x = coord.x+1;   neighbor[ 3].y = coord.y-1;
      neighbor[ 4].x = coord.x-1;   neighbor[ 4].y = coord.y  ;
      neighbor[ 5].x = coord.x+1;   neighbor[ 5].y = coord.y  ;
      neighbor[ 6].x = -1       ;   neighbor[ 6].y = -1       ;
      neighbor[ 7].x = -1       ;   neighbor[ 7].y = -1       ;
      neighbor[ 8].x = -1       ;   neighbor[ 8].y = -1       ;
      neighbor[ 9].x = -1       ;   neighbor[ 9].y = -1       ;
      neighbor[10].x = -1       ;   neighbor[10].y = -1       ;
      break;
   case 13:
      neighbor[ 0].x = coord.x-2;   neighbor[ 0].y = coord.y-1;
      neighbor[ 1].x = coord.x  ;   neighbor[ 1].y = coord.y-1;
      neighbor[ 2].x = coord.x-2;   neighbor[ 2].y = coord.y  ;
      neighbor[ 3].x = coord.x-1;   neighbor[ 3].y = coord.y  ;
      neighbor[ 4].x = coord.x+1;   neighbor[ 4].y = coord.y  ;
      neighbor[ 5].x = coord.x+2;   neighbor[ 5].y = coord.y  ;
      neighbor[ 6].x = coord.x-2;   neighbor[ 6].y = coord.y+1;
      neighbor[ 7].x = coord.x-1;   neighbor[ 7].y = coord.y+1;
      neighbor[ 8].x = coord.x  ;   neighbor[ 8].y = coord.y+1;
      neighbor[ 9].x = coord.x+2;   neighbor[ 9].y = coord.y+1;
      neighbor[10].x = coord.x-2;   neighbor[10].y = coord.y+2;
      break;
   case 14:
      neighbor[ 0].x = coord.x-2;   neighbor[ 0].y = coord.y-2;
      neighbor[ 1].x = coord.x  ;   neighbor[ 1].y = coord.y-2;
      neighbor[ 2].x = coord.x-2;   neighbor[ 2].y = coord.y-1;
      neighbor[ 3].x = coord.x-1;   neighbor[ 3].y = coord.y-1;
      neighbor[ 4].x = coord.x  ;   neighbor[ 4].y = coord.y-1;
      neighbor[ 5].x = coord.x+1;   neighbor[ 5].y = coord.y-1;
      neighbor[ 6].x = coord.x-1;   neighbor[ 6].y = coord.y  ;
      neighbor[ 7].x = coord.x+1;   neighbor[ 7].y = coord.y  ;
      neighbor[ 8].x = coord.x-1;   neighbor[ 8].y = coord.y+1;
      neighbor[ 9].x = coord.x+1;   neighbor[ 9].y = coord.y+1;
      neighbor[10].x = -1       ;   neighbor[10].y = -1       ;
      break;
   case 15:
      neighbor[ 0].x = coord.x-2;   neighbor[ 0].y = coord.y-1;
      neighbor[ 1].x = coord.x  ;   neighbor[ 1].y = coord.y-1;
      neighbor[ 2].x = coord.x-2;   neighbor[ 2].y = coord.y  ;
      neighbor[ 3].x = coord.x-1;   neighbor[ 3].y = coord.y  ;
      neighbor[ 4].x = coord.x+1;   neighbor[ 4].y = coord.y  ;
      neighbor[ 5].x = coord.x+2;   neighbor[ 5].y = coord.y  ;
      neighbor[ 6].x = coord.x-2;   neighbor[ 6].y = coord.y+1;
      neighbor[ 7].x = coord.x  ;   neighbor[ 7].y = coord.y+1;
      neighbor[ 8].x = coord.x+1;   neighbor[ 8].y = coord.y+1;
      neighbor[ 9].x = coord.x+2;   neighbor[ 9].y = coord.y+1;
      neighbor[10].x = coord.x  ;   neighbor[10].y = coord.y+2;
      break;
   }
   for (int i=0; i<11; i++)
      if ((neighbor[i].x >= sizeField.x) ||
          (neighbor[i].y >= sizeField.y) ||
          (neighbor[i].x < 0) ||
          (neighbor[i].y < 0)) {
         neighbor[i] = CIncorrectCoord;
      }
}

int TcTrapezoid3::GetNeighborNumber() const {
   return 11;
}

POINT TcTrapezoid3::GetNeighborCoord(int index) const {
   return neighbor[index];
}

bool TcTrapezoid3::ToBelong(int x, int y) { // принадлежат ли эти экранные координаты ячейке
   POINT point = {x, y};
   return PointInPolygon(point, regionOut, 4);
}

void TcTrapezoid3::SetPoint(const int& area) {
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
   case 0: case 12:
      regionOut[0].x = (R*2)*(coord.x/4) + r;     break;
   case 1: case 4: case 5: case 8: case 9: case 13:
      regionOut[0].x = (R*2)*(coord.x/4) + R;     break;
   case 2: case 14:
      regionOut[0].x = (R*2)*(coord.x/4) + R+r;   break;
   case 3: case 7: case 6: case 10: case 11: case 15:
      regionOut[0].x = (R*2)*(coord.x/4) + R*2;   break;
   }
   switch (direction) {
   case 1:
      regionOut[0].y = (a*6)*(coord.y/4);         break;
   case 3: case 5: case 7:
      regionOut[0].y = (a*6)*(coord.y/4) + a;     break;
   case 0: case 2:
      regionOut[0].y = (a*6)*(coord.y/4) + a+c;   break;
   case 4: case 6: case 10:
      regionOut[0].y = (a*6)*(coord.y/4) + a+b;   break;
   case 8: case 9: case 11:
      regionOut[0].y = (a*6)*(coord.y/4) + b*2;   break;
   case 12: case 14:
      regionOut[0].y = (a*6)*(coord.y/4) + b*2+c; break;
   case 13: case 15:
      regionOut[0].y = (a*6)*(coord.y/4) + b*3;   break;
   }
   switch (direction) {
   case 0: case 2: case 12: case 14:
      regionOut[1].x = regionOut[0].x  ;   regionOut[1].y = regionOut[0].y+a;
      regionOut[2].x = regionOut[0].x-r;   regionOut[2].y = regionOut[1].y+c;
      regionOut[3].x = regionOut[2].x  ;   regionOut[3].y = regionOut[0].y-c;
      break;
   case 1: case 10:
      regionOut[1].x = regionOut[0].x  ;   regionOut[1].y = regionOut[0].y+a;
      regionOut[2].x = regionOut[0].x-r;   regionOut[2].y = regionOut[1].y+c;
      regionOut[3].x = regionOut[0].x-R;   regionOut[3].y = regionOut[1].y  ;
      break;
   case 3: case 8:
      regionOut[1].x = regionOut[0].x-r;   regionOut[1].y = regionOut[0].y+c;
      regionOut[2].x = regionOut[0].x-R;   regionOut[2].y = regionOut[0].y  ;
      regionOut[3].x = regionOut[2].x  ;   regionOut[3].y = regionOut[0].y-a;
      break;
   case 4: case 15:
      regionOut[1].x = regionOut[0].x  ;   regionOut[1].y = regionOut[0].y+a;
      regionOut[2].x = regionOut[0].x-R;   regionOut[2].y = regionOut[0].y  ;
      regionOut[3].x = regionOut[0].x-r;   regionOut[3].y = regionOut[0].y-c;
      break;
   case 5: case 7: case 9: case 11:
      regionOut[1].x = regionOut[0].x  ;   regionOut[1].y = regionOut[0].y+b;
      regionOut[2].x = regionOut[0].x-r;   regionOut[2].y = regionOut[1].y-c;
      regionOut[3].x = regionOut[2].x  ;   regionOut[3].y = regionOut[0].y+c;
      break;
   case 6: case 13:
      regionOut[1].x = regionOut[0].x-R;   regionOut[1].y = regionOut[0].y+a;
      regionOut[2].x = regionOut[1].x  ;   regionOut[2].y = regionOut[0].y  ;
      regionOut[3].x = regionOut[0].x-r;   regionOut[3].y = regionOut[0].y-c;
      break;
   }

   // определение координат вписанного в фигуру квадрата - область в которую выводится изображение/текст
   switch (direction) {
   case 0: case 2: case 12: case 14:
      regionIn.left   = regionOut[0].x - sq/2 - r/2;
      regionIn.top    = regionOut[0].y - sq/2 + c;
      regionIn.right  = regionIn.left   + sq;
      regionIn.bottom = regionIn.top    + sq;
      break;
   case 1: case 10:
      regionIn.left   = regionOut[2].x;
      regionIn.top    = regionOut[2].y - a;
      regionIn.right  = regionIn.left   + sq;
      regionIn.bottom = regionIn.top    + sq;
      break;
   case 3: case 8:
      regionIn.right  = regionOut[1].x;
      regionIn.top    = regionOut[1].y - a;
      regionIn.left   = regionIn.right  - sq;
      regionIn.bottom = regionIn.top    + sq;
      break;
   case 4: case 15:
      regionIn.left   = regionOut[3].x;
      regionIn.bottom = regionOut[3].y + a;
      regionIn.right  = regionIn.left   + sq;
      regionIn.top    = regionIn.bottom - sq;
      break;
   case 5: case 7: case 9: case 11:
      regionIn.left   = regionOut[3].x - sq/2 + r/2;
      regionIn.top    = regionOut[3].y - sq/2 + c;
      regionIn.right  = regionIn.left   + sq;
      regionIn.bottom = regionIn.top    + sq;
      break;
   case 6: case 13:
      regionIn.right  = regionOut[3].x;
      regionIn.bottom = regionOut[3].y + a;
      regionIn.left   = regionIn.right  - sq;
      regionIn.top    = regionIn.bottom - sq;
   }
   regionIn.left   += 1;
   regionIn.top    += 1;
   regionIn.right  += 1;
   regionIn.bottom += 1;
}

void TcTrapezoid3::Paint() const {
   TB::Paint();

   SelectObject(hDC, down ? hPenWhite : hPenBlack);
   MoveToEx(hDC, regionOut[0].x, regionOut[0].y, NULL);
   LineTo  (hDC, regionOut[1].x, regionOut[1].y);
   switch (direction) {
   case 0: case 1: case  2: case  3: case 4:  case  5: case  7:
   case 8: case 9: case 10: case 11: case 12: case 14: case 15:
      LineTo  (hDC, regionOut[2].x, regionOut[2].y);
   }
   switch (direction) {
   case 1: case 10:
      LineTo  (hDC, regionOut[3].x, regionOut[3].y);
   }

   SelectObject(hDC, down ? hPenBlack : hPenWhite);
   switch (direction) {
   case 0: case 2: case 12: case 14:
   case 3: case 8:
   case 5: case 7: case 9: case 11:
      MoveToEx(hDC, regionOut[2].x+1, regionOut[2].y  , NULL);
      LineTo  (hDC, regionOut[3].x+1, regionOut[3].y  );
      MoveToEx(hDC, regionOut[3].x  , regionOut[3].y+1, NULL);
      LineTo  (hDC, regionOut[0].x  , regionOut[0].y+1);
      break;
   case 1: case 10:
      MoveToEx(hDC, regionOut[3].x  , regionOut[3].y+1, NULL);
      LineTo  (hDC, regionOut[0].x  , regionOut[0].y+1);
      break;
   case 6: case 13:
      MoveToEx(hDC, regionOut[1].x+1, regionOut[1].y  , NULL);
      LineTo  (hDC, regionOut[2].x+1, regionOut[2].y  );
   case 4: case 15:
      MoveToEx(hDC, regionOut[2].x  , regionOut[2].y+1, NULL);
      LineTo  (hDC, regionOut[3].x  , regionOut[3].y+1);
      LineTo  (hDC, regionOut[0].x  , regionOut[0].y+1);
      break;
   }
}

} // namespace nsFigure
