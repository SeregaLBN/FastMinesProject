////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "TcTriangle4.cpp"
//
// –еализаци€ класса TcTriangle4 - треугольник 30∞-30∞-120∞
////////////////////////////////////////////////////////////////////////////////

#include ".\TcTriangle4.h"
#include <math.h>

namespace nsFigure {

////////////////////////////////////////////////////////////////////////////////
//                               local init
////////////////////////////////////////////////////////////////////////////////

float TcTriangle4::b;
float TcTriangle4::R;
float TcTriangle4::r;

#ifdef SQUARE
#undef SQUARE
#endif // SQUARE

#define SQUARE (a/(sqrt(3)+3) - 1)
////////////////////////////////////////////////////////////////////////////////
//                               static function
////////////////////////////////////////////////////////////////////////////////

POINT TcTriangle4::GetSizeFieldInPixel(const POINT& sizeField, const int& area) {
   a = sqrt(area*sqrt(48));
   b = a/2;
   R = a/sqrt(3);
   r = R/2;

   POINT result = {b + b*((sizeField.x+2)/3)+
                       b*((sizeField.x+0)/3) + 1,
                   (R+r)*((sizeField.y+1)/2) + 1};
   return result;
}

int TcTriangle4::SizeInscribedSquare(int area) {
   a = sqrt(area*sqrt(48));
   sq = SQUARE;
   return sq;
}

float TcTriangle4::GetPercentMine(TeSkillLevel skill) { // процент мин на заданном уровне сложности
   switch (skill) {
   case skillLevelBeginner    : return 19.762f;
   case skillLevelAmateur     : return 22.762f;
   case skillLevelProfessional: return 25.762f;
   case skillLevelCrazy       : return 29.762f;
   }
   return 1.f;
}

////////////////////////////////////////////////////////////////////////////////
//                               virtual function
////////////////////////////////////////////////////////////////////////////////

TcTriangle4::TcTriangle4(const POINT& setCoord, const POINT& sizeField, const int& area) {
   coord = setCoord;
   Reset();

   direction = (coord.y%4)*3+(coord.x%3); // 0..11
   SetPoint(area);

   // определ€ю координаты соседей
   switch (direction) {
   case  0:
      neighbor[ 0].x = coord.x-1;   neighbor[ 0].y = coord.y-2;
      neighbor[ 1].x = coord.x+1;   neighbor[ 1].y = coord.y-2;
      neighbor[ 2].x = coord.x-1;   neighbor[ 2].y = coord.y-1;
      neighbor[ 3].x = coord.x  ;   neighbor[ 3].y = coord.y-1;
      neighbor[ 4].x = coord.x+1;   neighbor[ 4].y = coord.y-1;
      neighbor[ 5].x = coord.x+2;   neighbor[ 5].y = coord.y-1;
      neighbor[ 6].x = coord.x-2;   neighbor[ 6].y = coord.y  ;
      neighbor[ 7].x = coord.x-1;   neighbor[ 7].y = coord.y  ;
      neighbor[ 8].x = coord.x+1;   neighbor[ 8].y = coord.y  ;
      neighbor[ 9].x = coord.x+2;   neighbor[ 9].y = coord.y  ;
      neighbor[10].x = coord.x-3;   neighbor[10].y = coord.y+1;
      neighbor[11].x = coord.x-2;   neighbor[11].y = coord.y+1;
      neighbor[12].x = coord.x-1;   neighbor[12].y = coord.y+1;
      neighbor[13].x = coord.x  ;   neighbor[13].y = coord.y+1;
      neighbor[14].x = coord.x+1;   neighbor[14].y = coord.y+1;
      neighbor[15].x = coord.x-3;   neighbor[15].y = coord.y+2;
      neighbor[16].x = coord.x-2;   neighbor[16].y = coord.y+2;
      neighbor[17].x = coord.x-1;   neighbor[17].y = coord.y+2;
      neighbor[18].x = coord.x  ;   neighbor[18].y = coord.y+2;
      neighbor[19].x = coord.x-2;   neighbor[19].y = coord.y+3;
      neighbor[20].x = coord.x  ;   neighbor[20].y = coord.y+3;
      break;
   case  1:
      neighbor[ 0].x = coord.x-2;   neighbor[ 0].y = coord.y-2;
      neighbor[ 1].x = coord.x  ;   neighbor[ 1].y = coord.y-2;
      neighbor[ 2].x = coord.x-2;   neighbor[ 2].y = coord.y-1;
      neighbor[ 3].x = coord.x-1;   neighbor[ 3].y = coord.y-1;
      neighbor[ 4].x = coord.x  ;   neighbor[ 4].y = coord.y-1;
      neighbor[ 5].x = coord.x+1;   neighbor[ 5].y = coord.y-1;
      neighbor[ 6].x = coord.x-2;   neighbor[ 6].y = coord.y  ;
      neighbor[ 7].x = coord.x-1;   neighbor[ 7].y = coord.y  ;
      neighbor[ 8].x = coord.x+1;   neighbor[ 8].y = coord.y  ;
      neighbor[ 9].x = coord.x+2;   neighbor[ 9].y = coord.y  ;
      neighbor[10].x = coord.x-2;   neighbor[10].y = coord.y+1;
      neighbor[11].x = coord.x-1;   neighbor[11].y = coord.y+1;
      neighbor[12].x = coord.x  ;   neighbor[12].y = coord.y+1;
      neighbor[13].x = coord.x+1;   neighbor[13].y = coord.y+1;
      neighbor[14].x = coord.x+2;   neighbor[14].y = coord.y+1;
      neighbor[15].x = coord.x-1;   neighbor[15].y = coord.y+2;
      neighbor[16].x = coord.x  ;   neighbor[16].y = coord.y+2;
      neighbor[17].x = coord.x+1;   neighbor[17].y = coord.y+2;
      neighbor[18].x = coord.x+2;   neighbor[18].y = coord.y+2;
      neighbor[19].x = coord.x  ;   neighbor[19].y = coord.y+3;
      neighbor[20].x = coord.x+2;   neighbor[20].y = coord.y+3;
      break;
   case  2:
      neighbor[ 0].x = coord.x-3;   neighbor[ 0].y = coord.y-2;
      neighbor[ 1].x = coord.x-1;   neighbor[ 1].y = coord.y-2;
      neighbor[ 2].x = coord.x  ;   neighbor[ 2].y = coord.y-2;
      neighbor[ 3].x = coord.x+2;   neighbor[ 3].y = coord.y-2;
      neighbor[ 4].x = coord.x-3;   neighbor[ 4].y = coord.y-1;
      neighbor[ 5].x = coord.x-2;   neighbor[ 5].y = coord.y-1;
      neighbor[ 6].x = coord.x-1;   neighbor[ 6].y = coord.y-1;
      neighbor[ 7].x = coord.x  ;   neighbor[ 7].y = coord.y-1;
      neighbor[ 8].x = coord.x+1;   neighbor[ 8].y = coord.y-1;
      neighbor[ 9].x = coord.x+2;   neighbor[ 9].y = coord.y-1;
      neighbor[10].x = coord.x+3;   neighbor[10].y = coord.y-1;
      neighbor[11].x = coord.x-3;   neighbor[11].y = coord.y  ;
      neighbor[12].x = coord.x-2;   neighbor[12].y = coord.y  ;
      neighbor[13].x = coord.x-1;   neighbor[13].y = coord.y  ;
      neighbor[14].x = coord.x+1;   neighbor[14].y = coord.y  ;
      neighbor[15].x = coord.x+2;   neighbor[15].y = coord.y  ;
      neighbor[16].x = coord.x+3;   neighbor[16].y = coord.y  ;
      neighbor[17].x = coord.x-3;   neighbor[17].y = coord.y+1;
      neighbor[18].x = coord.x-1;   neighbor[18].y = coord.y+1;
      neighbor[19].x = coord.x  ;   neighbor[19].y = coord.y+1;
      neighbor[20].x = coord.x+2;   neighbor[20].y = coord.y+1;
      break;
   case  3:
      neighbor[ 0].x = coord.x-2;   neighbor[ 0].y = coord.y-1;
      neighbor[ 1].x = coord.x  ;   neighbor[ 1].y = coord.y-1;
      neighbor[ 2].x = coord.x+1;   neighbor[ 2].y = coord.y-1;
      neighbor[ 3].x = coord.x+3;   neighbor[ 3].y = coord.y-1;
      neighbor[ 4].x = coord.x-3;   neighbor[ 4].y = coord.y  ;
      neighbor[ 5].x = coord.x-2;   neighbor[ 5].y = coord.y  ;
      neighbor[ 6].x = coord.x-1;   neighbor[ 6].y = coord.y  ;
      neighbor[ 7].x = coord.x+1;   neighbor[ 7].y = coord.y  ;
      neighbor[ 8].x = coord.x+2;   neighbor[ 8].y = coord.y  ;
      neighbor[ 9].x = coord.x+3;   neighbor[ 9].y = coord.y  ;
      neighbor[10].x = coord.x-3;   neighbor[10].y = coord.y+1;
      neighbor[11].x = coord.x-2;   neighbor[11].y = coord.y+1;
      neighbor[12].x = coord.x-1;   neighbor[12].y = coord.y+1;
      neighbor[13].x = coord.x  ;   neighbor[13].y = coord.y+1;
      neighbor[14].x = coord.x+1;   neighbor[14].y = coord.y+1;
      neighbor[15].x = coord.x+2;   neighbor[15].y = coord.y+1;
      neighbor[16].x = coord.x+3;   neighbor[16].y = coord.y+1;
      neighbor[17].x = coord.x-2;   neighbor[17].y = coord.y+2;
      neighbor[18].x = coord.x  ;   neighbor[18].y = coord.y+2;
      neighbor[19].x = coord.x+1;   neighbor[19].y = coord.y+2;
      neighbor[20].x = coord.x+3;   neighbor[20].y = coord.y+2;
      break;
   case  4:
      neighbor[ 0].x = coord.x-2;   neighbor[ 0].y = coord.y-3;
      neighbor[ 1].x = coord.x  ;   neighbor[ 1].y = coord.y-3;
      neighbor[ 2].x = coord.x-2;   neighbor[ 2].y = coord.y-2;
      neighbor[ 3].x = coord.x-1;   neighbor[ 3].y = coord.y-2;
      neighbor[ 4].x = coord.x  ;   neighbor[ 4].y = coord.y-2;
      neighbor[ 5].x = coord.x+1;   neighbor[ 5].y = coord.y-2;
      neighbor[ 6].x = coord.x-2;   neighbor[ 6].y = coord.y-1;
      neighbor[ 7].x = coord.x-1;   neighbor[ 7].y = coord.y-1;
      neighbor[ 8].x = coord.x  ;   neighbor[ 8].y = coord.y-1;
      neighbor[ 9].x = coord.x+1;   neighbor[ 9].y = coord.y-1;
      neighbor[10].x = coord.x+2;   neighbor[10].y = coord.y-1;
      neighbor[11].x = coord.x-2;   neighbor[11].y = coord.y  ;
      neighbor[12].x = coord.x-1;   neighbor[12].y = coord.y  ;
      neighbor[13].x = coord.x+1;   neighbor[13].y = coord.y  ;
      neighbor[14].x = coord.x+2;   neighbor[14].y = coord.y  ;
      neighbor[15].x = coord.x-1;   neighbor[15].y = coord.y+1;
      neighbor[16].x = coord.x  ;   neighbor[16].y = coord.y+1;
      neighbor[17].x = coord.x+1;   neighbor[17].y = coord.y+1;
      neighbor[18].x = coord.x+2;   neighbor[18].y = coord.y+1;
      neighbor[19].x = coord.x  ;   neighbor[19].y = coord.y+2;
      neighbor[20].x = coord.x+2;   neighbor[20].y = coord.y+2;
      break;
   case  5:
      neighbor[ 0].x = coord.x  ;   neighbor[ 0].y = coord.y-3;
      neighbor[ 1].x = coord.x+2;   neighbor[ 1].y = coord.y-3;
      neighbor[ 2].x = coord.x  ;   neighbor[ 2].y = coord.y-2;
      neighbor[ 3].x = coord.x+1;   neighbor[ 3].y = coord.y-2;
      neighbor[ 4].x = coord.x+2;   neighbor[ 4].y = coord.y-2;
      neighbor[ 5].x = coord.x+3;   neighbor[ 5].y = coord.y-2;
      neighbor[ 6].x = coord.x-1;   neighbor[ 6].y = coord.y-1;
      neighbor[ 7].x = coord.x  ;   neighbor[ 7].y = coord.y-1;
      neighbor[ 8].x = coord.x+1;   neighbor[ 8].y = coord.y-1;
      neighbor[ 9].x = coord.x+2;   neighbor[ 9].y = coord.y-1;
      neighbor[10].x = coord.x+3;   neighbor[10].y = coord.y-1;
      neighbor[11].x = coord.x-2;   neighbor[11].y = coord.y  ;
      neighbor[12].x = coord.x-1;   neighbor[12].y = coord.y  ;
      neighbor[13].x = coord.x+1;   neighbor[13].y = coord.y  ;
      neighbor[14].x = coord.x+2;   neighbor[14].y = coord.y  ;
      neighbor[15].x = coord.x-2;   neighbor[15].y = coord.y+1;
      neighbor[16].x = coord.x-1;   neighbor[16].y = coord.y+1;
      neighbor[17].x = coord.x  ;   neighbor[17].y = coord.y+1;
      neighbor[18].x = coord.x+1;   neighbor[18].y = coord.y+1;
      neighbor[19].x = coord.x-1;   neighbor[19].y = coord.y+2;
      neighbor[20].x = coord.x+1;   neighbor[20].y = coord.y+2;
      break;
   case  6:
      neighbor[ 0].x = coord.x-2;   neighbor[ 0].y = coord.y-2;
      neighbor[ 1].x = coord.x  ;   neighbor[ 1].y = coord.y-2;
      neighbor[ 2].x = coord.x+1;   neighbor[ 2].y = coord.y-2;
      neighbor[ 3].x = coord.x+3;   neighbor[ 3].y = coord.y-2;
      neighbor[ 4].x = coord.x-3;   neighbor[ 4].y = coord.y-1;
      neighbor[ 5].x = coord.x-2;   neighbor[ 5].y = coord.y-1;
      neighbor[ 6].x = coord.x-1;   neighbor[ 6].y = coord.y-1;
      neighbor[ 7].x = coord.x  ;   neighbor[ 7].y = coord.y-1;
      neighbor[ 8].x = coord.x+1;   neighbor[ 8].y = coord.y-1;
      neighbor[ 9].x = coord.x+2;   neighbor[ 9].y = coord.y-1;
      neighbor[10].x = coord.x+3;   neighbor[10].y = coord.y-1;
      neighbor[11].x = coord.x-3;   neighbor[11].y = coord.y  ;
      neighbor[12].x = coord.x-2;   neighbor[12].y = coord.y  ;
      neighbor[13].x = coord.x-1;   neighbor[13].y = coord.y  ;
      neighbor[14].x = coord.x+1;   neighbor[14].y = coord.y  ;
      neighbor[15].x = coord.x+2;   neighbor[15].y = coord.y  ;
      neighbor[16].x = coord.x+3;   neighbor[16].y = coord.y  ;
      neighbor[17].x = coord.x-2;   neighbor[17].y = coord.y+1;
      neighbor[18].x = coord.x  ;   neighbor[18].y = coord.y+1;
      neighbor[19].x = coord.x+1;   neighbor[19].y = coord.y+1;
      neighbor[20].x = coord.x+3;   neighbor[20].y = coord.y+1;
      break;
   case  7:
      neighbor[ 0].x = coord.x  ;   neighbor[ 0].y = coord.y-2;
      neighbor[ 1].x = coord.x+2;   neighbor[ 1].y = coord.y-2;
      neighbor[ 2].x = coord.x-1;   neighbor[ 2].y = coord.y-1;
      neighbor[ 3].x = coord.x  ;   neighbor[ 3].y = coord.y-1;
      neighbor[ 4].x = coord.x+1;   neighbor[ 4].y = coord.y-1;
      neighbor[ 5].x = coord.x+2;   neighbor[ 5].y = coord.y-1;
      neighbor[ 6].x = coord.x-2;   neighbor[ 6].y = coord.y  ;
      neighbor[ 7].x = coord.x-1;   neighbor[ 7].y = coord.y  ;
      neighbor[ 8].x = coord.x+1;   neighbor[ 8].y = coord.y  ;
      neighbor[ 9].x = coord.x+2;   neighbor[ 9].y = coord.y  ;
      neighbor[10].x = coord.x-2;   neighbor[10].y = coord.y+1;
      neighbor[11].x = coord.x-1;   neighbor[11].y = coord.y+1;
      neighbor[12].x = coord.x  ;   neighbor[12].y = coord.y+1;
      neighbor[13].x = coord.x+1;   neighbor[13].y = coord.y+1;
      neighbor[14].x = coord.x+2;   neighbor[14].y = coord.y+1;
      neighbor[15].x = coord.x-2;   neighbor[15].y = coord.y+2;
      neighbor[16].x = coord.x-1;   neighbor[16].y = coord.y+2;
      neighbor[17].x = coord.x  ;   neighbor[17].y = coord.y+2;
      neighbor[18].x = coord.x+1;   neighbor[18].y = coord.y+2;
      neighbor[19].x = coord.x-2;   neighbor[19].y = coord.y+3;
      neighbor[20].x = coord.x  ;   neighbor[20].y = coord.y+3;
      break;
   case  8:
      neighbor[ 0].x = coord.x-1;   neighbor[ 0].y = coord.y-2;
      neighbor[ 1].x = coord.x+1;   neighbor[ 1].y = coord.y-2;
      neighbor[ 2].x = coord.x-2;   neighbor[ 2].y = coord.y-1;
      neighbor[ 3].x = coord.x-1;   neighbor[ 3].y = coord.y-1;
      neighbor[ 4].x = coord.x  ;   neighbor[ 4].y = coord.y-1;
      neighbor[ 5].x = coord.x+1;   neighbor[ 5].y = coord.y-1;
      neighbor[ 6].x = coord.x-2;   neighbor[ 6].y = coord.y  ;
      neighbor[ 7].x = coord.x-1;   neighbor[ 7].y = coord.y  ;
      neighbor[ 8].x = coord.x+1;   neighbor[ 8].y = coord.y  ;
      neighbor[ 9].x = coord.x+2;   neighbor[ 9].y = coord.y  ;
      neighbor[10].x = coord.x-1;   neighbor[10].y = coord.y+1;
      neighbor[11].x = coord.x  ;   neighbor[11].y = coord.y+1;
      neighbor[12].x = coord.x+1;   neighbor[12].y = coord.y+1;
      neighbor[13].x = coord.x+2;   neighbor[13].y = coord.y+1;
      neighbor[14].x = coord.x+3;   neighbor[14].y = coord.y+1;
      neighbor[15].x = coord.x  ;   neighbor[15].y = coord.y+2;
      neighbor[16].x = coord.x+1;   neighbor[16].y = coord.y+2;
      neighbor[17].x = coord.x+2;   neighbor[17].y = coord.y+2;
      neighbor[18].x = coord.x+3;   neighbor[18].y = coord.y+2;
      neighbor[19].x = coord.x  ;   neighbor[19].y = coord.y+3;
      neighbor[20].x = coord.x+2;   neighbor[20].y = coord.y+3;
      break;
   case  9:
      neighbor[ 0].x = coord.x-2;   neighbor[ 0].y = coord.y-3;
      neighbor[ 1].x = coord.x  ;   neighbor[ 1].y = coord.y-3;
      neighbor[ 2].x = coord.x-3;   neighbor[ 2].y = coord.y-2;
      neighbor[ 3].x = coord.x-2;   neighbor[ 3].y = coord.y-2;
      neighbor[ 4].x = coord.x-1;   neighbor[ 4].y = coord.y-2;
      neighbor[ 5].x = coord.x  ;   neighbor[ 5].y = coord.y-2;
      neighbor[ 6].x = coord.x-3;   neighbor[ 6].y = coord.y-1;
      neighbor[ 7].x = coord.x-2;   neighbor[ 7].y = coord.y-1;
      neighbor[ 8].x = coord.x-1;   neighbor[ 8].y = coord.y-1;
      neighbor[ 9].x = coord.x  ;   neighbor[ 9].y = coord.y-1;
      neighbor[10].x = coord.x+1;   neighbor[10].y = coord.y-1;
      neighbor[11].x = coord.x-2;   neighbor[11].y = coord.y  ;
      neighbor[12].x = coord.x-1;   neighbor[12].y = coord.y  ;
      neighbor[13].x = coord.x+1;   neighbor[13].y = coord.y  ;
      neighbor[14].x = coord.x+2;   neighbor[14].y = coord.y  ;
      neighbor[15].x = coord.x-1;   neighbor[15].y = coord.y+1;
      neighbor[16].x = coord.x  ;   neighbor[16].y = coord.y+1;
      neighbor[17].x = coord.x+1;   neighbor[17].y = coord.y+1;
      neighbor[18].x = coord.x+2;   neighbor[18].y = coord.y+1;
      neighbor[19].x = coord.x-1;   neighbor[19].y = coord.y+2;
      neighbor[20].x = coord.x+1;   neighbor[20].y = coord.y+2;
      break;
   case 10:
      neighbor[ 0].x = coord.x  ;   neighbor[ 0].y = coord.y-3;
      neighbor[ 1].x = coord.x+2;   neighbor[ 1].y = coord.y-3;
      neighbor[ 2].x = coord.x-1;   neighbor[ 2].y = coord.y-2;
      neighbor[ 3].x = coord.x  ;   neighbor[ 3].y = coord.y-2;
      neighbor[ 4].x = coord.x+1;   neighbor[ 4].y = coord.y-2;
      neighbor[ 5].x = coord.x+2;   neighbor[ 5].y = coord.y-2;
      neighbor[ 6].x = coord.x-2;   neighbor[ 6].y = coord.y-1;
      neighbor[ 7].x = coord.x-1;   neighbor[ 7].y = coord.y-1;
      neighbor[ 8].x = coord.x  ;   neighbor[ 8].y = coord.y-1;
      neighbor[ 9].x = coord.x+1;   neighbor[ 9].y = coord.y-1;
      neighbor[10].x = coord.x+2;   neighbor[10].y = coord.y-1;
      neighbor[11].x = coord.x-2;   neighbor[11].y = coord.y  ;
      neighbor[12].x = coord.x-1;   neighbor[12].y = coord.y  ;
      neighbor[13].x = coord.x+1;   neighbor[13].y = coord.y  ;
      neighbor[14].x = coord.x+2;   neighbor[14].y = coord.y  ;
      neighbor[15].x = coord.x-2;   neighbor[15].y = coord.y+1;
      neighbor[16].x = coord.x-1;   neighbor[16].y = coord.y+1;
      neighbor[17].x = coord.x  ;   neighbor[17].y = coord.y+1;
      neighbor[18].x = coord.x+1;   neighbor[18].y = coord.y+1;
      neighbor[19].x = coord.x-2;   neighbor[19].y = coord.y+2;
      neighbor[20].x = coord.x  ;   neighbor[20].y = coord.y+2;
      break;
   case 11:
      neighbor[ 0].x = coord.x-3;   neighbor[ 0].y = coord.y-1;
      neighbor[ 1].x = coord.x-1;   neighbor[ 1].y = coord.y-1;
      neighbor[ 2].x = coord.x  ;   neighbor[ 2].y = coord.y-1;
      neighbor[ 3].x = coord.x+2;   neighbor[ 3].y = coord.y-1;
      neighbor[ 4].x = coord.x-3;   neighbor[ 4].y = coord.y  ;
      neighbor[ 5].x = coord.x-2;   neighbor[ 5].y = coord.y  ;
      neighbor[ 6].x = coord.x-1;   neighbor[ 6].y = coord.y  ;
      neighbor[ 7].x = coord.x+1;   neighbor[ 7].y = coord.y  ;
      neighbor[ 8].x = coord.x+2;   neighbor[ 8].y = coord.y  ;
      neighbor[ 9].x = coord.x+3;   neighbor[ 9].y = coord.y  ;
      neighbor[10].x = coord.x-3;   neighbor[10].y = coord.y+1;
      neighbor[11].x = coord.x-2;   neighbor[11].y = coord.y+1;
      neighbor[12].x = coord.x-1;   neighbor[12].y = coord.y+1;
      neighbor[13].x = coord.x  ;   neighbor[13].y = coord.y+1;
      neighbor[14].x = coord.x+1;   neighbor[14].y = coord.y+1;
      neighbor[15].x = coord.x+2;   neighbor[15].y = coord.y+1;
      neighbor[16].x = coord.x+3;   neighbor[16].y = coord.y+1;
      neighbor[17].x = coord.x-3;   neighbor[17].y = coord.y+2;
      neighbor[18].x = coord.x-1;   neighbor[18].y = coord.y+2;
      neighbor[19].x = coord.x  ;   neighbor[19].y = coord.y+2;
      neighbor[20].x = coord.x+2;   neighbor[20].y = coord.y+2;
      break;
   }
   for (int i=0; i<21; i++)
      if ((neighbor[i].x >= sizeField.x) ||
          (neighbor[i].y >= sizeField.y) ||
          (neighbor[i].x < 0) ||
          (neighbor[i].y < 0)) {
         neighbor[i] = CIncorrectCoord;
      }
}

int TcTriangle4::GetNeighborNumber() const {
   return 21;
}

POINT TcTriangle4::GetNeighborCoord(int index) const {
   return neighbor[index];
}

bool TcTriangle4::ToBelong(int x, int y) { // принадлежат ли эти экранные координаты €чейке
   POINT point = {x, y};
   return PointInPolygon(point, regionOut, 3);
}

void TcTriangle4::SetPoint(const int& area) {
   if (coord.x==0 && coord.y==0) {
      a = sqrt(area*sqrt(48));
      b = a/2;
      R = a/sqrt(3);
      r = R/2;
      sq = SQUARE;
      TB::SetPoint(NULL);
   }

   // определение координат точек фигуры
   switch (direction) {
   case 0: case 4: case 9:
      regionOut[0].x = a*(coord.x/3) + b;   break;
   case 1: case 3: case 6: case 7: case 8: case 10:
      regionOut[0].x = a*(coord.x/3) + a;   break;
   case 2: case 5: case 11:
      regionOut[0].x = a*(coord.x/3) + a+b; break;
   }
   switch (direction) {
   case 0: case 2: case 4: case 5:
      regionOut[0].y = (R+r)*(coord.y/4*2);             break;
   case 1:
      regionOut[0].y = (R+r)*(coord.y/4*2) + r;         break;
   case 3: case 6: case 7: case 8: case 10:
      regionOut[0].y = (R+r)*(coord.y/4*2) + r+R;       break;
   case 9:
      regionOut[0].y = (R+r)*(coord.y/4*2) + r*2+R;     break;
   case 11:
      regionOut[0].y = (R+r)*(coord.y/4*2) + (r+R)*2;   break;
   }
   switch (direction) {
   case 0: case 10:
      regionOut[1].x = regionOut[0].x  ;   regionOut[1].y = regionOut[0].y+R;
      regionOut[2].x = regionOut[0].x-b;   regionOut[2].y = regionOut[1].y+r;
      break;
   case 1: case 9:
      regionOut[1].x = regionOut[0].x  ;   regionOut[1].y = regionOut[0].y+R;
      regionOut[2].x = regionOut[0].x-b;   regionOut[2].y = regionOut[0].y-r;
      break;
   case 2: case 6:
      regionOut[1].x = regionOut[0].x-b;   regionOut[1].y = regionOut[0].y+r;
      regionOut[2].x = regionOut[0].x-a;   regionOut[2].y = regionOut[0].y  ;
      break;
   case 3: case 11:
      regionOut[1].x = regionOut[0].x-a;   regionOut[1].y = regionOut[0].y  ;
      regionOut[2].x = regionOut[0].x-b;   regionOut[2].y = regionOut[0].y-r;
      break;
   case 4: case 8:
      regionOut[1].x = regionOut[0].x+b;   regionOut[2].y = regionOut[0].y+R;
      regionOut[2].x = regionOut[0].x  ;   regionOut[1].y = regionOut[2].y+r;
      break;
   case 5: case 7:
      regionOut[1].x = regionOut[0].x-b;   regionOut[2].y = regionOut[0].y+r;
      regionOut[2].x = regionOut[1].x  ;   regionOut[1].y = regionOut[2].y+R;
      break;
   }

   // определение координат вписанного в фигуру квадрата - область в которую выводитс€ изображение/текст
   POINTFLOAT center; // координата центра квадрата
   switch (direction) {
   case 0: case 10:
      center.x = regionOut[1].x - sq/2;
      center.y = regionOut[1].y - sq/2;
      break;
   case 1: case 9:
      center.x = regionOut[0].x - sq/2;
      center.y = regionOut[0].y + sq/2;
      break;
   case 2: case 6:
      center.x = regionOut[1].x;
      center.y = regionOut[2].y + sq/2;
      center.y += 2;
      break;
   case 3: case 11:
      center.x = regionOut[2].x;
      center.y = regionOut[0].y - sq/2;
      break;
   case 4: case 8:
      center.x = regionOut[2].x + sq/2;
      center.y = regionOut[2].y - sq/2;
      center.x += 2;
      break;
   case 5: case 7:
      center.x = regionOut[2].x + sq/2;
      center.y = regionOut[2].y + sq/2;
      center.x += 2;
      break;
   }
   regionIn.left   = center.x - sq/2;
   regionIn.top    = center.y - sq/2;
   regionIn.right  = center.x + sq/2;
   regionIn.bottom = center.y + sq/2;
   regionIn.left   += 0;
   regionIn.top    += 0;
   regionIn.right  += 0;
   regionIn.bottom += 0;
}

void TcTriangle4::Paint() const {
   TB::Paint();

   SelectObject(hDC, down ? hPenWhite : hPenBlack);
   MoveToEx(hDC, regionOut[0].x, regionOut[0].y, NULL);
   LineTo  (hDC, regionOut[1].x, regionOut[1].y);
   switch (direction) {
   case  0: case 2: case 4:
   case 10: case 6: case 8:
      LineTo  (hDC, regionOut[2].x, regionOut[2].y);
   }

   SelectObject(hDC, down ? hPenBlack : hPenWhite);
   switch (direction) {
   case  0: case 4:
   case 10: case 8:
      MoveToEx(hDC, regionOut[2].x+1, regionOut[2].y  , NULL);
      LineTo  (hDC, regionOut[0].x+1, regionOut[0].y  );
      break;
   case 1: case 5:
   case 9: case 7:
      MoveToEx(hDC, regionOut[1].x+1, regionOut[1].y  , NULL);
      LineTo  (hDC, regionOut[2].x+1, regionOut[2].y  );
      MoveToEx(hDC, regionOut[2].x  , regionOut[2].y+1, NULL);
      LineTo  (hDC, regionOut[0].x  , regionOut[0].y+1);
      break;
   case 3: case 11:
      MoveToEx(hDC, regionOut[1].x  , regionOut[1].y+1, NULL);
      LineTo  (hDC, regionOut[2].x  , regionOut[2].y+1);
   case 2: case 6:
      MoveToEx(hDC, regionOut[2].x  , regionOut[2].y+1, NULL);
      LineTo  (hDC, regionOut[0].x  , regionOut[0].y+1);
      break;
   }
}

} // namespace nsFigure
