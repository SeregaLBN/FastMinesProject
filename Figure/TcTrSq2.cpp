////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "TcTrSq2.cpp"
//
// Реализация класса TcTrSq2 - мозаика из 24х треугольников и 12х квадратов (на 1 квадрат приходится 2 треугольника)
////////////////////////////////////////////////////////////////////////////////

#include ".\TcTrSq2.h"
#include <math.h>

namespace nsFigure {

////////////////////////////////////////////////////////////////////////////////
//                               local init
////////////////////////////////////////////////////////////////////////////////

float TcTrSq2::b;
float TcTrSq2::h;

#ifdef SQUARE
#undef SQUARE
#endif // SQUARE

#define SQUARE (a*sqrt(3)/(sqrt(3)+2) - 1)
////////////////////////////////////////////////////////////////////////////////
//                               static function
////////////////////////////////////////////////////////////////////////////////

POINT TcTrSq2::GetSizeFieldInPixel(const POINT& sizeField, const int& area) {
   a = sqrt(6*area/(2+sqrt(3))); // размер стороны треугольника и квадрата
   b = a/2;
   h = a*sqrt(3)/2;

   POINT result = {b+h*((sizeField.x+2)/3)+
                     a*((sizeField.x+1)/3)+
                     b*((sizeField.x+0)/3) + 1,
                   b+h*((sizeField.y+2)/3)+
                     a*((sizeField.y+1)/3)+
                     b*((sizeField.y+0)/3) + 1};
   return result;
}

int TcTrSq2::SizeInscribedSquare(int area) {
   a = sqrt(6*area/(2+sqrt(3))); // размер стороны треугольника и квадрата
   sq = SQUARE;
   return sq;
}

float TcTrSq2::GetPercentMine(TeSkillLevel skill) { // процент мин на заданном уровне сложности
   switch (skill) {
   case skillLevelBeginner    : return 15.809f;
   case skillLevelAmateur     : return 18.809f;
   case skillLevelProfessional: return 21.809f;
   case skillLevelCrazy       : return 25.809f;
   }
   return 1.f;
}

////////////////////////////////////////////////////////////////////////////////
//                               virtual function
////////////////////////////////////////////////////////////////////////////////

TcTrSq2::TcTrSq2(const POINT& setCoord, const POINT& sizeField, const int& area) {
   coord = setCoord;
   Reset();

   direction = (coord.y%6)*6+(coord.x%6); // 0..35
   SetPoint(area);

   // определяю координаты соседей
   switch (direction) {
   case 0:
   case 21: neighbor[ 0].x = coord.x+1;   neighbor[ 0].y = coord.y-2;
            neighbor[ 1].x = coord.x-1;   neighbor[ 1].y = coord.y-1;
            neighbor[ 2].x = coord.x  ;   neighbor[ 2].y = coord.y-1;
            neighbor[ 3].x = coord.x+1;   neighbor[ 3].y = coord.y-1;
            neighbor[ 4].x = coord.x-1;   neighbor[ 4].y = coord.y  ;
            neighbor[ 5].x = coord.x+1;   neighbor[ 5].y = coord.y  ;
            neighbor[ 6].x = coord.x-1;   neighbor[ 6].y = coord.y+1;
            neighbor[ 7].x = coord.x  ;   neighbor[ 7].y = coord.y+1;
            neighbor[ 8].x = coord.x+1;   neighbor[ 8].y = coord.y+1;
            neighbor[ 9].x = coord.x+2;   neighbor[ 9].y = coord.y+1;
            neighbor[10].x = coord.x-1;   neighbor[10].y = coord.y+2;
            neighbor[11].x = coord.x  ;   neighbor[11].y = coord.y+2; break;
   case 1:
   case 22: neighbor[ 0].x = coord.x  ;   neighbor[ 0].y = coord.y-2;
            neighbor[ 1].x = coord.x-1;   neighbor[ 1].y = coord.y-1;
            neighbor[ 2].x = coord.x  ;   neighbor[ 2].y = coord.y-1;
            neighbor[ 3].x = coord.x+1;   neighbor[ 3].y = coord.y-1;
            neighbor[ 4].x = coord.x-1;   neighbor[ 4].y = coord.y  ;
            neighbor[ 5].x = coord.x+1;   neighbor[ 5].y = coord.y  ;
            neighbor[ 6].x = coord.x-1;   neighbor[ 6].y = coord.y+1;
            neighbor[ 7].x = coord.x  ;   neighbor[ 7].y = coord.y+1;
            neighbor[ 8].x = coord.x+1;   neighbor[ 8].y = coord.y+1;
            neighbor[ 9].x = -1;          neighbor[ 9].y = -1       ;
            neighbor[10].x = -1;          neighbor[10].y = -1       ;
            neighbor[11].x = -1;          neighbor[11].y = -1       ; break;
   case 2:
   case 23: neighbor[ 0].x = coord.x-1;   neighbor[ 0].y = coord.y-2;
            neighbor[ 1].x = coord.x+1;   neighbor[ 1].y = coord.y-1;
            neighbor[ 2].x = coord.x  ;   neighbor[ 2].y = coord.y-1;
            neighbor[ 3].x = coord.x+2;   neighbor[ 3].y = coord.y-1;
            neighbor[ 4].x = coord.x-1;   neighbor[ 4].y = coord.y  ;
            neighbor[ 5].x = coord.x+1;   neighbor[ 5].y = coord.y  ;
            neighbor[ 6].x = coord.x-1;   neighbor[ 6].y = coord.y+1;
            neighbor[ 7].x = coord.x  ;   neighbor[ 7].y = coord.y+1;
            neighbor[ 8].x = coord.x+1;   neighbor[ 8].y = coord.y+1;
            neighbor[ 9].x = -1;          neighbor[ 9].y = -1       ;
            neighbor[10].x = -1;          neighbor[10].y = -1       ;
            neighbor[11].x = -1;          neighbor[11].y = -1       ; break;
   case 3:
   case 18: neighbor[ 0].x = coord.x-1;   neighbor[ 0].y = coord.y-1;
            neighbor[ 1].x = coord.x  ;   neighbor[ 1].y = coord.y-1;
            neighbor[ 2].x = coord.x+1;   neighbor[ 2].y = coord.y-1;
            neighbor[ 3].x = coord.x+2;   neighbor[ 3].y = coord.y-1;
            neighbor[ 4].x = coord.x-1;   neighbor[ 4].y = coord.y  ;
            neighbor[ 5].x = coord.x+1;   neighbor[ 5].y = coord.y  ;
            neighbor[ 6].x = coord.x+2;   neighbor[ 6].y = coord.y  ;
            neighbor[ 7].x = coord.x-2;   neighbor[ 7].y = coord.y+1;
            neighbor[ 8].x = coord.x-1;   neighbor[ 8].y = coord.y+1;
            neighbor[ 9].x = coord.x  ;   neighbor[ 9].y = coord.y+1;
            neighbor[10].x = coord.x+1;   neighbor[10].y = coord.y+1;
            neighbor[11].x = coord.x+1;   neighbor[11].y = coord.y+2; break;
   case 4:
   case 19: neighbor[ 0].x = coord.x  ;   neighbor[ 0].y = coord.y-1;
            neighbor[ 1].x = coord.x+1;   neighbor[ 1].y = coord.y-1;
            neighbor[ 2].x = coord.x-1;   neighbor[ 2].y = coord.y  ;
            neighbor[ 3].x = coord.x+1;   neighbor[ 3].y = coord.y  ;
            neighbor[ 4].x = coord.x-1;   neighbor[ 4].y = coord.y+1;
            neighbor[ 5].x = coord.x  ;   neighbor[ 5].y = coord.y+1;
            neighbor[ 6].x = coord.x+1;   neighbor[ 6].y = coord.y+1;
            neighbor[ 7].x = coord.x  ;   neighbor[ 7].y = coord.y+2;
            neighbor[ 8].x = coord.x+1;   neighbor[ 8].y = coord.y+2;
            neighbor[ 9].x = -1;          neighbor[ 9].y = -1       ;
            neighbor[10].x = -1;          neighbor[10].y = -1       ;
            neighbor[11].x = -1;          neighbor[11].y = -1       ; break;
   case 5:
   case 20: neighbor[ 0].x = coord.x-1;   neighbor[ 0].y = coord.y-1;
            neighbor[ 1].x = coord.x  ;   neighbor[ 1].y = coord.y-1;
            neighbor[ 2].x = coord.x+1;   neighbor[ 2].y = coord.y-1;
            neighbor[ 3].x = coord.x-2;   neighbor[ 3].y = coord.y  ;
            neighbor[ 4].x = coord.x-1;   neighbor[ 4].y = coord.y  ;
            neighbor[ 5].x = coord.x+1;   neighbor[ 5].y = coord.y  ;
            neighbor[ 6].x = coord.x-1;   neighbor[ 6].y = coord.y+1;
            neighbor[ 7].x = coord.x  ;   neighbor[ 7].y = coord.y+1;
            neighbor[ 8].x = coord.x  ;   neighbor[ 8].y = coord.y+2;
            neighbor[ 9].x = -1;          neighbor[ 9].y = -1       ;
            neighbor[10].x = -1;          neighbor[10].y = -1       ;
            neighbor[11].x = -1;          neighbor[11].y = -1       ; break;
   case 6:
   case 27: neighbor[ 0].x = coord.x  ;   neighbor[ 0].y = coord.y-1;
            neighbor[ 1].x = coord.x+1;   neighbor[ 1].y = coord.y-1;
            neighbor[ 2].x = coord.x-1;   neighbor[ 2].y = coord.y  ;
            neighbor[ 3].x = coord.x+1;   neighbor[ 3].y = coord.y  ;
            neighbor[ 4].x = coord.x+2;   neighbor[ 4].y = coord.y  ;
            neighbor[ 5].x = coord.x-1;   neighbor[ 5].y = coord.y+1;
            neighbor[ 6].x = coord.x  ;   neighbor[ 6].y = coord.y+1;
            neighbor[ 7].x = coord.x+1;   neighbor[ 7].y = coord.y+1;
            neighbor[ 8].x = coord.x+2;   neighbor[ 8].y = coord.y+1;
            neighbor[ 9].x = -1;          neighbor[ 9].y = -1       ;
            neighbor[10].x = -1;          neighbor[10].y = -1       ;
            neighbor[11].x = -1;          neighbor[11].y = -1       ; break;
   case 7:
   case 28: neighbor[ 0].x = coord.x-1;   neighbor[ 0].y = coord.y-1;
            neighbor[ 1].x = coord.x  ;   neighbor[ 1].y = coord.y-1;
            neighbor[ 2].x = coord.x+1;   neighbor[ 2].y = coord.y-1;
            neighbor[ 3].x = coord.x+2;   neighbor[ 3].y = coord.y-1;
            neighbor[ 4].x = coord.x-1;   neighbor[ 4].y = coord.y  ;
            neighbor[ 5].x = coord.x+1;   neighbor[ 5].y = coord.y  ;
            neighbor[ 6].x = coord.x+2;   neighbor[ 6].y = coord.y  ;
            neighbor[ 7].x = coord.x-1;   neighbor[ 7].y = coord.y+1;
            neighbor[ 8].x = coord.x  ;   neighbor[ 8].y = coord.y+1;
            neighbor[ 9].x = coord.x+1;   neighbor[ 9].y = coord.y+1;
            neighbor[10].x = coord.x+2;   neighbor[10].y = coord.y+1;
            neighbor[11].x = coord.x+3;   neighbor[11].y = coord.y+1; break;
   case 8:
   case 29: neighbor[ 0].x = coord.x-1;   neighbor[ 0].y = coord.y-3;
            neighbor[ 1].x = coord.x  ;   neighbor[ 1].y = coord.y-2;
            neighbor[ 2].x = coord.x-2;   neighbor[ 2].y = coord.y-1;
            neighbor[ 3].x = coord.x-1;   neighbor[ 3].y = coord.y-1;
            neighbor[ 4].x = coord.x  ;   neighbor[ 4].y = coord.y-1;
            neighbor[ 5].x = coord.x+1;   neighbor[ 5].y = coord.y-1;
            neighbor[ 6].x = coord.x-2;   neighbor[ 6].y = coord.y  ;
            neighbor[ 7].x = coord.x-1;   neighbor[ 7].y = coord.y  ;
            neighbor[ 8].x = coord.x+1;   neighbor[ 8].y = coord.y  ;
            neighbor[ 9].x = -1;          neighbor[ 9].y = -1       ;
            neighbor[10].x = -1;          neighbor[10].y = -1       ;
            neighbor[11].x = -1;          neighbor[11].y = -1       ; break;
   case 9:
   case 24: neighbor[ 0].x = coord.x-1;   neighbor[ 0].y = coord.y-1;
            neighbor[ 1].x = coord.x  ;   neighbor[ 1].y = coord.y-1;
            neighbor[ 2].x = coord.x+1;   neighbor[ 2].y = coord.y-1;
            neighbor[ 3].x = coord.x-2;   neighbor[ 3].y = coord.y  ;
            neighbor[ 4].x = coord.x-1;   neighbor[ 4].y = coord.y  ;
            neighbor[ 5].x = coord.x+1;   neighbor[ 5].y = coord.y  ;
            neighbor[ 6].x = coord.x-1;   neighbor[ 6].y = coord.y+1;
            neighbor[ 7].x = coord.x  ;   neighbor[ 7].y = coord.y+1;
            neighbor[ 8].x = coord.x+1;   neighbor[ 8].y = coord.y+1;
            neighbor[ 9].x = -1;          neighbor[ 9].y = -1       ;
            neighbor[10].x = -1;          neighbor[10].y = -1       ;
            neighbor[11].x = -1;          neighbor[11].y = -1       ; break;
   case 10:
   case 25: neighbor[ 0].x = coord.x-1;   neighbor[ 0].y = coord.y-1;
            neighbor[ 1].x = coord.x  ;   neighbor[ 1].y = coord.y-1;
            neighbor[ 2].x = coord.x+1;   neighbor[ 2].y = coord.y-1;
            neighbor[ 3].x = coord.x-1;   neighbor[ 3].y = coord.y  ;
            neighbor[ 4].x = coord.x+1;   neighbor[ 4].y = coord.y  ;
            neighbor[ 5].x = coord.x-1;   neighbor[ 5].y = coord.y+1;
            neighbor[ 6].x = coord.x  ;   neighbor[ 6].y = coord.y+1;
            neighbor[ 7].x = coord.x+1;   neighbor[ 7].y = coord.y+1;
            neighbor[ 8].x = coord.x-1;   neighbor[ 8].y = coord.y+2;
            neighbor[ 9].x = coord.x  ;   neighbor[ 9].y = coord.y+2;
            neighbor[10].x = coord.x+1;   neighbor[10].y = coord.y+2;
            neighbor[11].x = coord.x+1;   neighbor[11].y = coord.y+3; break;
   case 11:
   case 26: neighbor[ 0].x = coord.x  ;   neighbor[ 0].y = coord.y-2;
            neighbor[ 1].x = coord.x+1;   neighbor[ 1].y = coord.y-2;
            neighbor[ 2].x = coord.x-1;   neighbor[ 2].y = coord.y-1;
            neighbor[ 3].x = coord.x  ;   neighbor[ 3].y = coord.y-1;
            neighbor[ 4].x = coord.x+1;   neighbor[ 4].y = coord.y-1;
            neighbor[ 5].x = coord.x-1;   neighbor[ 5].y = coord.y  ;
            neighbor[ 6].x = coord.x+1;   neighbor[ 6].y = coord.y  ;
            neighbor[ 7].x = coord.x  ;   neighbor[ 7].y = coord.y+1;
            neighbor[ 8].x = coord.x+1;   neighbor[ 8].y = coord.y+1;
            neighbor[ 9].x = -1;          neighbor[ 9].y = -1       ;
            neighbor[10].x = -1;          neighbor[10].y = -1       ;
            neighbor[11].x = -1;          neighbor[11].y = -1       ; break;
   case 12:
   case 33: neighbor[ 0].x = coord.x  ;   neighbor[ 0].y = coord.y-2;
            neighbor[ 1].x = coord.x-1;   neighbor[ 1].y = coord.y-1;
            neighbor[ 2].x = coord.x  ;   neighbor[ 2].y = coord.y-1;
            neighbor[ 3].x = coord.x+1;   neighbor[ 3].y = coord.y-1;
            neighbor[ 4].x = coord.x-1;   neighbor[ 4].y = coord.y  ;
            neighbor[ 5].x = coord.x+1;   neighbor[ 5].y = coord.y  ;
            neighbor[ 6].x = coord.x+2;   neighbor[ 6].y = coord.y  ;
            neighbor[ 7].x = coord.x-1;   neighbor[ 7].y = coord.y+1;
            neighbor[ 8].x = coord.x  ;   neighbor[ 8].y = coord.y+1;
            neighbor[ 9].x = -1;          neighbor[ 9].y = -1       ;
            neighbor[10].x = -1;          neighbor[10].y = -1       ;
            neighbor[11].x = -1;          neighbor[11].y = -1       ; break;
   case 13:
   case 34: neighbor[ 0].x = coord.x-1;   neighbor[ 0].y = coord.y-1;
            neighbor[ 1].x = coord.x  ;   neighbor[ 1].y = coord.y-1;
            neighbor[ 2].x = coord.x-2;   neighbor[ 2].y = coord.y  ;
            neighbor[ 3].x = coord.x-1;   neighbor[ 3].y = coord.y  ;
            neighbor[ 4].x = coord.x+1;   neighbor[ 4].y = coord.y  ;
            neighbor[ 5].x = coord.x-2;   neighbor[ 5].y = coord.y+1;
            neighbor[ 6].x = coord.x-1;   neighbor[ 6].y = coord.y+1;
            neighbor[ 7].x = coord.x  ;   neighbor[ 7].y = coord.y+1;
            neighbor[ 8].x = coord.x+1;   neighbor[ 8].y = coord.y+1;
            neighbor[ 9].x = -1;          neighbor[ 9].y = -1       ;
            neighbor[10].x = -1;          neighbor[10].y = -1       ;
            neighbor[11].x = -1;          neighbor[11].y = -1       ; break;
   case 14:
   case 35: neighbor[ 0].x = coord.x-2;   neighbor[ 0].y = coord.y-1;
            neighbor[ 1].x = coord.x-1;   neighbor[ 1].y = coord.y-1;
            neighbor[ 2].x = coord.x+1;   neighbor[ 2].y = coord.y-1;
            neighbor[ 3].x = coord.x-2;   neighbor[ 3].y = coord.y  ;
            neighbor[ 4].x = coord.x-1;   neighbor[ 4].y = coord.y  ;
            neighbor[ 5].x = coord.x+1;   neighbor[ 5].y = coord.y  ;
            neighbor[ 6].x = coord.x+2;   neighbor[ 6].y = coord.y  ;
            neighbor[ 7].x = coord.x-2;   neighbor[ 7].y = coord.y+1;
            neighbor[ 8].x = coord.x-1;   neighbor[ 8].y = coord.y+1;
            neighbor[ 9].x = coord.x  ;   neighbor[ 9].y = coord.y+1;
            neighbor[10].x = coord.x+1;   neighbor[10].y = coord.y+1;
            neighbor[11].x = coord.x  ;   neighbor[11].y = coord.y+2; break;
   case 15:
   case 30: neighbor[ 0].x = coord.x-2;   neighbor[ 0].y = coord.y-1;
            neighbor[ 1].x = coord.x  ;   neighbor[ 1].y = coord.y-1;
            neighbor[ 2].x = coord.x+1;   neighbor[ 2].y = coord.y-1;
            neighbor[ 3].x = coord.x-1;   neighbor[ 3].y = coord.y  ;
            neighbor[ 4].x = coord.x+1;   neighbor[ 4].y = coord.y  ;
            neighbor[ 5].x = coord.x-1;   neighbor[ 5].y = coord.y+1;
            neighbor[ 6].x = coord.x  ;   neighbor[ 6].y = coord.y+1;
            neighbor[ 7].x = coord.x+1;   neighbor[ 7].y = coord.y+1;
            neighbor[ 8].x = coord.x-1;   neighbor[ 8].y = coord.y+2;
            neighbor[ 9].x = -1;          neighbor[ 9].y = -1       ;
            neighbor[10].x = -1;          neighbor[10].y = -1       ;
            neighbor[11].x = -1;          neighbor[11].y = -1       ; break;
   case 16:
   case 31: neighbor[ 0].x = coord.x-1;   neighbor[ 0].y = coord.y-2;
            neighbor[ 1].x = coord.x  ;   neighbor[ 1].y = coord.y-2;
            neighbor[ 2].x = coord.x-3;   neighbor[ 2].y = coord.y-1;
            neighbor[ 3].x = coord.x-1;   neighbor[ 3].y = coord.y-1;
            neighbor[ 4].x = coord.x  ;   neighbor[ 4].y = coord.y-1;
            neighbor[ 5].x = coord.x-2;   neighbor[ 5].y = coord.y  ;
            neighbor[ 6].x = coord.x-1;   neighbor[ 6].y = coord.y  ;
            neighbor[ 7].x = coord.x-1;   neighbor[ 7].y = coord.y+1;
            neighbor[ 8].x = coord.x  ;   neighbor[ 8].y = coord.y+1;
            neighbor[ 9].x = -1;          neighbor[ 9].y = -1       ;
            neighbor[10].x = -1;          neighbor[10].y = -1       ;
            neighbor[11].x = -1;          neighbor[11].y = -1       ; break;
   case 17:
   case 32: neighbor[ 0].x = coord.x-1;   neighbor[ 0].y = coord.y-2;
            neighbor[ 1].x = coord.x  ;   neighbor[ 1].y = coord.y-2;
            neighbor[ 2].x = coord.x+1;   neighbor[ 2].y = coord.y-2;
            neighbor[ 3].x = coord.x-1;   neighbor[ 3].y = coord.y-1;
            neighbor[ 4].x = coord.x  ;   neighbor[ 4].y = coord.y-1;
            neighbor[ 5].x = coord.x+1;   neighbor[ 5].y = coord.y-1;
            neighbor[ 6].x = coord.x+1;   neighbor[ 6].y = coord.y  ;
            neighbor[ 7].x = coord.x+2;   neighbor[ 7].y = coord.y  ;
            neighbor[ 8].x = coord.x-1;   neighbor[ 8].y = coord.y+1;
            neighbor[ 9].x = coord.x  ;   neighbor[ 9].y = coord.y+1;
            neighbor[10].x = coord.x+1;   neighbor[10].y = coord.y+1;
            neighbor[11].x = coord.x  ;   neighbor[11].y = coord.y+2; break;
   }
   for (int i=0; i<12; i++)
      if ((neighbor[i].x >= sizeField.x) ||
          (neighbor[i].y >= sizeField.y) ||
          (neighbor[i].x < 0) ||
          (neighbor[i].y < 0)) {
         neighbor[i] = CIncorrectCoord;
      }
}

int TcTrSq2::GetNeighborNumber() const {
   return 12;
}

POINT TcTrSq2::GetNeighborCoord(int index) const {
   return neighbor[index];
}

bool TcTrSq2::ToBelong(int x, int y) { // принадлежат ли эти экранные координаты ячейке
   POINT point = {x, y};
   switch (direction) {
   case 0:  case 21:
   case 3:  case 18:
   case 7:  case 28:
   case 10: case 25:
   case 14: case 35:
   case 17: case 32:
      return PointInPolygon(point, regionOut, 4);
   case 1:  case 22:
   case 2:  case 23:
   case 4:  case 19:
   case 5:  case 20:
   case 6:  case 27:
   case 8:  case 29:
   case 9:  case 24:
   case 11: case 26:
   case 12: case 33:
   case 13: case 34:
   case 15: case 30:
   case 16: case 31:
      return PointInPolygon(point, regionOut, 3);
   }
   return false;
}

void TcTrSq2::SetPoint(const int& area) {
   if (coord.x==0 && coord.y==0) {
      a = sqrt(6*area/(2+sqrt(3))); // размер стороны треугольника и квадрата
      b = a/2;
      h = a*sqrt(3)/2;
      sq = SQUARE;
      TB::SetPoint(NULL);
   }

   // определение координат точек фигуры
   switch (direction) {
   case 24: case 30:
      regionOut[0].x =             (h*2+a*3)*(coord.x/6); break;
   case 12: case 18:
      regionOut[0].x = b         + (h*2+a*3)*(coord.x/6); break;
   case  0: case 31:
      regionOut[0].x = h         + (h*2+a*3)*(coord.x/6); break;
   case  6: case 13: case 19:
      regionOut[0].x = h+b       + (h*2+a*3)*(coord.x/6); break;
   case  1: case  8: case 25:
      regionOut[0].x = h+a       + (h*2+a*3)*(coord.x/6); break;
   case  7: case  9: case 14: case 15: case 20: case 26:
      regionOut[0].x = b+h+a     + (h*2+a*3)*(coord.x/6); break;
   case  2: case  3: case 32: case 33:
      regionOut[0].x = h+a*2     + (h*2+a*3)*(coord.x/6); break;
   case 16: case 21:
      regionOut[0].x = b+a+h*2   + (h*2+a*3)*(coord.x/6); break;
   case  4: case 27: case 34:
      regionOut[0].x = h*2+a*2   + (h*2+a*3)*(coord.x/6); break;
   case 10: case 22: case 29:
      regionOut[0].x = b+h*2+a*2 + (h*2+a*3)*(coord.x/6); break;
   case  5: case 11: case 28: case 35:
      regionOut[0].x = h*2+a*3   + (h*2+a*3)*(coord.x/6); break;
   case 17: case 23:
      regionOut[0].x = b+h*2+a*3 + (h*2+a*3)*(coord.x/6); break;
   }
   switch (direction) {
   case  0: case  1: case  2: case  3: case  8:
      regionOut[0].y =             (h*2+a*3)*(coord.y/6); break;
   case  4: case  5: case 11:
      regionOut[0].y = b         + (h*2+a*3)*(coord.y/6); break;
   case  6: case  7: case  9:
      regionOut[0].y = h         + (h*2+a*3)*(coord.y/6); break;
   case 10: case 12: case 16: case 17:
      regionOut[0].y = h+b       + (h*2+a*3)*(coord.y/6); break;
   case 13: case 14: case 15:
      regionOut[0].y = h+a       + (h*2+a*3)*(coord.y/6); break;
   case 18: case 21: case 22: case 23: case 29:
      regionOut[0].y = b+h+a     + (h*2+a*3)*(coord.y/6); break;
   case 19: case 20: case 26:
      regionOut[0].y = h+a*2     + (h*2+a*3)*(coord.y/6); break;
   case 24: case 27: case 28:
      regionOut[0].y = b+a+h*2   + (h*2+a*3)*(coord.y/6); break;
   case 25: case 31: case 32: case 33:
      regionOut[0].y = h*2+a*2   + (h*2+a*3)*(coord.y/6); break;
   case 30: case 34: case 35:
      regionOut[0].y = b+h*2+a*2 + (h*2+a*3)*(coord.y/6); break;
   }
   switch (direction) {
   case 0: case 21:
      regionOut[1].x = regionOut[0].x+b;   regionOut[1].y = regionOut[0].y+h;
      regionOut[2].x = regionOut[1].x-h;   regionOut[2].y = regionOut[1].y+b;
      regionOut[3].x = regionOut[0].x-h;   regionOut[3].y = regionOut[2].y-h;
      break;
   case 1: case  2: case  5: case 20: case 22: case 23:
      regionOut[1].x = regionOut[0].x-b;   regionOut[1].y = regionOut[0].y+h;
      regionOut[2].x = regionOut[0].x-a;   regionOut[2].y = regionOut[0].y  ;
      break;
   case 3: case 18:
      regionOut[1].x = regionOut[0].x+h;   regionOut[1].y = regionOut[0].y+b;
      regionOut[3].x = regionOut[0].x-b;   regionOut[2].y = regionOut[1].y+h;
      regionOut[2].x = regionOut[3].x+h;   regionOut[3].y = regionOut[2].y-b;
      break;
   case 4: case  8: case 11: case 19: case 26: case 29:
      regionOut[1].x = regionOut[0].x+b;   regionOut[1].y = regionOut[0].y+h;
      regionOut[2].x = regionOut[0].x-b;   regionOut[2].y = regionOut[1].y  ;
      break;
   case 6: case 13: case 16: case 27: case 31: case 34:
      regionOut[1].x = regionOut[0].x  ;   regionOut[1].y = regionOut[0].y+a;
      regionOut[2].x = regionOut[0].x-h;   regionOut[2].y = regionOut[0].y+b;
      break;
   case 7: case 10: case 14: case 17: case 25: case 28: case 32: case 35:
      regionOut[1].x = regionOut[0].x  ;   regionOut[1].y = regionOut[0].y+a;
      regionOut[2].x = regionOut[0].x-a;   regionOut[2].y = regionOut[1].y  ;
      regionOut[3].x = regionOut[2].x  ;   regionOut[3].y = regionOut[0].y  ;
      break;
   case 9: case 12: case 15: case 24: case 30: case 33:
      regionOut[1].x = regionOut[0].x+h;   regionOut[1].y = regionOut[0].y+b;
      regionOut[2].x = regionOut[0].x  ;   regionOut[2].y = regionOut[0].y+a;
      break;
   }
   // определение координат вписанного в фигуру квадрата - область в которую выводится изображение/текст
   switch (direction) {
   case 0: case 21:
   case 3: case 18:
      regionIn.left   = regionOut[1].x - (b+h)/2 - sq/2;
      regionIn.top    = regionOut[0].y + (b+h)/2 - sq/2;
      break;
   case 1: case  2: case  5: case 20: case 22: case 23:
      regionIn.left   = regionOut[1].x - sq/2;
      regionIn.top    = regionOut[0].y;
      regionIn.top++;
      break;
   case 4: case  8: case 11: case 19: case 26: case 29:
      regionIn.left   = regionOut[0].x - sq/2;
      regionIn.top    = regionOut[2].y - sq;
      break;
   case 6: case 13: case 16: case 27: case 31: case 34:
      regionIn.left   = regionOut[0].x - sq;
      regionIn.top    = regionOut[2].y - sq/2;
      break;
   case 7: case 10: case 14: case 17: case 25: case 28: case 32: case 35:
      regionIn.left   = regionOut[0].x - b - sq/2;
      regionIn.top    = regionOut[0].y + b - sq/2;
      break;
   case 9: case 12: case 15: case 24: case 30: case 33:
      regionIn.left   = regionOut[0].x;
      regionIn.top    = regionOut[1].y - sq/2;
      regionIn.left++;
      break;
   }
   regionIn.right  = regionIn.left+sq;
   regionIn.bottom = regionIn.top +sq;

   regionIn.left   += 1;
   regionIn.top    += 1;
   regionIn.right  += 1;
   regionIn.bottom += 1;
}

void TcTrSq2::Paint() const {
   TB::Paint();

   SelectObject(hDC, down ? hPenWhite : hPenBlack);
   MoveToEx(hDC, regionOut[0].x, regionOut[0].y, NULL);
   LineTo  (hDC, regionOut[1].x, regionOut[1].y);
   switch (direction) {
   case 0: case 21:
   case 3: case 18:
   case 7: case 10: case 14: case 17: case 25: case 28: case 32: case 35:
   case 4: case  8: case 11: case 19: case 26: case 29:
   case 9: case 12: case 15: case 24: case 30: case 33:
      LineTo(hDC, regionOut[2].x, regionOut[2].y);
   }
   SelectObject(hDC, down ? hPenBlack : hPenWhite);
   switch (direction) {
   case 0: case 21:
      MoveToEx(hDC, regionOut[2].x+1, regionOut[2].y  , NULL);
      LineTo  (hDC, regionOut[3].x+1, regionOut[3].y  );
      MoveToEx(hDC, regionOut[3].x  , regionOut[3].y+1, NULL);
      LineTo  (hDC, regionOut[0].x  , regionOut[0].y+1);
      break;
   case 1: case  2: case  5: case 20: case 22: case 23:
      MoveToEx(hDC, regionOut[1].x+1, regionOut[1].y  , NULL);
      LineTo  (hDC, regionOut[2].x+1, regionOut[2].y  );
      MoveToEx(hDC, regionOut[2].x  , regionOut[2].y+1, NULL);
      LineTo  (hDC, regionOut[0].x  , regionOut[0].y+1);
      break;
   case 3: case 18:
      MoveToEx(hDC, regionOut[2].x  , regionOut[2].y-1, NULL);
      LineTo  (hDC, regionOut[3].x  , regionOut[3].y-1);
      MoveToEx(hDC, regionOut[3].x+1, regionOut[3].y  , NULL);
      LineTo  (hDC, regionOut[0].x+1, regionOut[0].y  );
      break;
   case 4: case  8: case 11: case 19: case 26: case 29:
      MoveToEx(hDC, regionOut[2].x+1, regionOut[2].y  , NULL);
      LineTo  (hDC, regionOut[0].x+1, regionOut[0].y  );
      break;
   case 6: case 13: case 16: case 27: case 31: case 34:
      MoveToEx(hDC, regionOut[1].x  , regionOut[1].y-1, NULL);
      LineTo  (hDC, regionOut[2].x  , regionOut[2].y-1);
      MoveToEx(hDC, regionOut[2].x  , regionOut[2].y+1, NULL);
      LineTo  (hDC, regionOut[0].x  , regionOut[0].y+1);
      break;
   case 7: case 10: case 14: case 17: case 25: case 28: case 32: case 35:
      MoveToEx(hDC, regionOut[2].x+1, regionOut[2].y  , NULL);
      LineTo  (hDC, regionOut[3].x+1, regionOut[3].y  );
      MoveToEx(hDC, regionOut[3].x  , regionOut[3].y+1, NULL);
      LineTo  (hDC, regionOut[0].x  , regionOut[0].y+1);
      break;
   case 9: case 12: case 15: case 24: case 30: case 33:
      MoveToEx(hDC, regionOut[2].x+1, regionOut[2].y  , NULL);
      LineTo  (hDC, regionOut[0].x+1, regionOut[0].y  );
      break;
   }
}

} // namespace nsFigure
