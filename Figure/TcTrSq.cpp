////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "TcTrSq.cpp"
//
// Реализация класса TcTrSq - мозаика из 4х треугольников и 2х квадратов
////////////////////////////////////////////////////////////////////////////////

#include ".\TcTrSq.h"
#include <math.h>

namespace nsFigure {

////////////////////////////////////////////////////////////////////////////////
//                               local init
////////////////////////////////////////////////////////////////////////////////

float TcTrSq::n;
float TcTrSq::m;
float TcTrSq::b;
float TcTrSq::k;

#ifdef SQUARE
#undef SQUARE
#endif // SQUARE

#define SQUARE (a*sqrt(3)/4/sin(pi/180*75) - 1)
////////////////////////////////////////////////////////////////////////////////
//                               static function
////////////////////////////////////////////////////////////////////////////////

POINT TcTrSq::GetSizeFieldInPixel(const POINT& sizeField, const int& area) {
   a = sqrt(3*area/(1+sqrt(3)/2)); // размер стороны треугольника и квадрата
   n = a*cos(pi/180*15);
   m = a*cos(pi/180*75);
   b = n+m;
   k = n-m;

   POINT result = {b+n*((sizeField.x-1+2)/3)+
                     k*((sizeField.x-1+1)/3)+
                     m*((sizeField.x-1+0)/3),
                   b+n* (sizeField.y-1)};
   return result;
}

int TcTrSq::SizeInscribedSquare(int area) {
   a = sqrt(3*area/(1+sqrt(3)/2)); // размер стороны треугольника и квадрата
   sq = SQUARE;
   return sq;
}

float TcTrSq::GetPercentMine(TeSkillLevel skill) { // процент мин на заданном уровне сложности
   switch (skill) {
   case skillLevelBeginner    : return 15.852f;
   case skillLevelAmateur     : return 18.852f;
   case skillLevelProfessional: return 21.852f;
   case skillLevelCrazy       : return 25.852f;
   }
   return 1.f;
}

////////////////////////////////////////////////////////////////////////////////
//                               virtual function
////////////////////////////////////////////////////////////////////////////////

TcTrSq::TcTrSq(const POINT& setCoord, const POINT& sizeField, const int& area) {
   coord = setCoord;
   Reset();

   direction = (coord.y%2)*3+(coord.x%3); // 0..5
   SetPoint(area);

   // определяю координаты соседей
   switch (direction) {
   case 0:  neighbor[ 0].x = coord.x-2;   neighbor[ 0].y = coord.y-1;
            neighbor[ 1].x = coord.x-1;   neighbor[ 1].y = coord.y-1;
            neighbor[ 2].x = coord.x  ;   neighbor[ 2].y = coord.y-1;
            neighbor[ 3].x = coord.x+1;   neighbor[ 3].y = coord.y-1;
            neighbor[ 4].x = coord.x-2;   neighbor[ 4].y = coord.y  ;
            neighbor[ 5].x = coord.x-1;   neighbor[ 5].y = coord.y  ;
            neighbor[ 6].x = coord.x+1;   neighbor[ 6].y = coord.y  ;
            neighbor[ 7].x = coord.x+2;   neighbor[ 7].y = coord.y  ;
            neighbor[ 8].x = coord.x-2;   neighbor[ 8].y = coord.y+1;
            neighbor[ 9].x = coord.x-1;   neighbor[ 9].y = coord.y+1;
            neighbor[10].x = coord.x  ;   neighbor[10].y = coord.y+1;
            neighbor[11].x = coord.x+1;   neighbor[11].y = coord.y+1; break;
   case 1:  neighbor[ 0].x = coord.x-2;   neighbor[ 0].y = coord.y-1;
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
   case 2:  neighbor[ 0].x = coord.x-1;   neighbor[ 0].y = coord.y-1;
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
   case 3:  neighbor[ 0].x = coord.x-1;   neighbor[ 0].y = coord.y-1;
            neighbor[ 1].x = coord.x  ;   neighbor[ 1].y = coord.y-1;
            neighbor[ 2].x = coord.x+1;   neighbor[ 2].y = coord.y-1;
            neighbor[ 3].x = coord.x+2;   neighbor[ 3].y = coord.y-1;
            neighbor[ 4].x = coord.x-2;   neighbor[ 4].y = coord.y  ;
            neighbor[ 5].x = coord.x-1;   neighbor[ 5].y = coord.y  ;
            neighbor[ 6].x = coord.x+1;   neighbor[ 6].y = coord.y  ;
            neighbor[ 7].x = coord.x  ;   neighbor[ 7].y = coord.y+1;
            neighbor[ 8].x = coord.x+1;   neighbor[ 8].y = coord.y+1;
            neighbor[ 9].x = -1;          neighbor[ 9].y = -1       ;
            neighbor[10].x = -1;          neighbor[10].y = -1       ;
            neighbor[11].x = -1;          neighbor[11].y = -1       ; break;
   case 4:  neighbor[ 0].x = coord.x-1;   neighbor[ 0].y = coord.y-1;
            neighbor[ 1].x = coord.x  ;   neighbor[ 1].y = coord.y-1;
            neighbor[ 2].x = coord.x+1;   neighbor[ 2].y = coord.y-1;
            neighbor[ 3].x = coord.x+2;   neighbor[ 3].y = coord.y-1;
            neighbor[ 4].x = coord.x-2;   neighbor[ 4].y = coord.y  ;
            neighbor[ 5].x = coord.x-1;   neighbor[ 5].y = coord.y  ;
            neighbor[ 6].x = coord.x+1;   neighbor[ 6].y = coord.y  ;
            neighbor[ 7].x = coord.x+2;   neighbor[ 7].y = coord.y  ;
            neighbor[ 8].x = coord.x-1;   neighbor[ 8].y = coord.y+1;
            neighbor[ 9].x = coord.x  ;   neighbor[ 9].y = coord.y+1;
            neighbor[10].x = coord.x+1;   neighbor[10].y = coord.y+1;
            neighbor[11].x = coord.x+2;   neighbor[11].y = coord.y+1; break;
   case 5:  neighbor[ 0].x = coord.x  ;   neighbor[ 0].y = coord.y-1;
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
   }
   for (int i=0; i<12; i++)
      if ((neighbor[i].x >= sizeField.x) ||
          (neighbor[i].y >= sizeField.y) ||
          (neighbor[i].x < 0) ||
          (neighbor[i].y < 0)) {
         neighbor[i] = CIncorrectCoord;
      }
}

int TcTrSq::GetNeighborNumber() const {
   return 12;
}

POINT TcTrSq::GetNeighborCoord(int index) const {
   return neighbor[index];
}

bool TcTrSq::ToBelong(int x, int y) { // принадлежат ли эти экранные координаты ячейке
   POINT point = {x, y};
   if ((direction == 0) || (direction == 4))
      return PointInPolygon(point, regionOut, 4);
   else
      return PointInPolygon(point, regionOut, 3);
}

void TcTrSq::SetPoint(const int& area) {
   if (coord.x==0 && coord.y==0) {
      a = sqrt(3*area/(1+sqrt(3)/2)); // размер стороны треугольника и квадрата
      n = a*cos(pi/180*15);
      m = a*cos(pi/180*75);
      b = n+m;
      k = n-m;
      sq = SQUARE;
      TB::SetPoint(NULL);
   }

   // определение координат точек фигуры
   POINTFLOAT point;
   switch (direction) {
   case 0:
   case 1:
   case 2:
   case 3:
   case 4:
      point.x = b + n * (coord.x/3*2);
      point.y = n + n*2*(coord.y/2);
      break;
   case 5:
      point.x = b + n * (coord.x/3*2+1);
      point.y = b + n*2*(coord.y/2);
      break;
   }
   switch (direction) {
   case 0:
      regionOut[1].x = point.x;             regionOut[1].y = point.y;
      regionOut[0].x = regionOut[1].x-m;   regionOut[0].y = regionOut[1].y-n;
      regionOut[2].x = regionOut[1].x-n;   regionOut[2].y = regionOut[1].y+m;
      regionOut[3].x = regionOut[1].x-b;   regionOut[3].y = regionOut[1].y-k;
      break;
   case 1:
      regionOut[1].x = point.x;             regionOut[1].y = point.y;
      regionOut[0].x = regionOut[1].x+k;   regionOut[0].y = regionOut[1].y-k;
      regionOut[2].x = regionOut[1].x-m;   regionOut[2].y = regionOut[1].y-n;
      break;
   case 2:
      regionOut[2].x = point.x;             regionOut[2].y = point.y;
      regionOut[0].x = regionOut[2].x+k;   regionOut[0].y = regionOut[2].y-k;
      regionOut[1].x = regionOut[2].x+n;   regionOut[1].y = regionOut[2].y+m;
      break;
   case 3:
      regionOut[0].x = point.x;             regionOut[0].y = point.y;
      regionOut[1].x = regionOut[0].x-m;   regionOut[1].y = regionOut[0].y+n;
      regionOut[2].x = regionOut[0].x-n;   regionOut[2].y = regionOut[0].y+m;
      break;
   case 4:
      regionOut[3].x = point.x;             regionOut[3].y = point.y;
      regionOut[0].x = regionOut[3].x+n;   regionOut[0].y = regionOut[3].y+m;
      regionOut[1].x = regionOut[3].x+k;   regionOut[1].y = regionOut[3].y+b;
      regionOut[2].x = regionOut[3].x-m;   regionOut[2].y = regionOut[3].y+n;
      break;
   case 5:
      regionOut[0].x = point.x;             regionOut[0].y = point.y;
      regionOut[1].x = regionOut[0].x+k;   regionOut[1].y = regionOut[0].y+k;
      regionOut[2].x = regionOut[0].x-m;   regionOut[2].y = regionOut[0].y+n;
      break;
   }

   // определение координат вписанного в фигуру квадрата - область в которую выводится изображение/текст
   float&c = sq;
   switch (direction) {
   case 0:
      regionIn.left   = regionOut[3].x + (b-c)/2;
      regionIn.top    = regionOut[0].y + (b-c)/2;
      regionIn.right  = regionOut[1].x - (b-c)/2;
      regionIn.bottom = regionOut[2].y - (b-c)/2;
      break;
   case 4:
      regionIn.left   = regionOut[2].x + (b-c)/2;
      regionIn.top    = regionOut[3].y + (b-c)/2;
      regionIn.right  = regionOut[0].x - (b-c)/2;
      regionIn.bottom = regionOut[1].y - (b-c)/2;
      break;
   case 1:
      regionIn.right  = regionOut[0].x - k/2;
      regionIn.bottom = regionOut[0].y + k/2;
      regionIn.left   = regionIn.right  - c;
      regionIn.top    = regionIn.bottom - c;
      break;
   case 2:
      regionIn.left   = regionOut[0].x - k/2;
      regionIn.top    = regionOut[0].y + k/2;
      regionIn.right  = regionIn.left   + c;
      regionIn.bottom = regionIn.top    + c;
      break;
   case 3:
      regionIn.left   = regionOut[1].x - k/2;
      regionIn.top    = regionOut[1].y - k/2 - c;
      regionIn.right  = regionIn.left   + c;
      regionIn.bottom = regionIn.top    + c;
      break;
   case 5:
      regionIn.left   = regionOut[0].x + k/2 - c;
      regionIn.top    = regionOut[0].y + k/2;
      regionIn.right  = regionIn.left   + c;
      regionIn.bottom = regionIn.top    + c;
      break;
   }
   regionIn.left   += 1;
   regionIn.top    += 1;
   regionIn.right  += 1;
   regionIn.bottom += 1;
}

void TcTrSq::Paint() const {
   TB::Paint();

   SelectObject(hDC, down ? hPenWhite : hPenBlack);
   MoveToEx(hDC, regionOut[0].x, regionOut[0].y, NULL);
   LineTo  (hDC, regionOut[1].x, regionOut[1].y);
   if ((direction != 1) && (direction != 3))
     LineTo(hDC, regionOut[2].x, regionOut[2].y);
   SelectObject(hDC, down ? hPenBlack : hPenWhite);
   switch (direction) {
   case 0:
   case 4:
      MoveToEx(hDC, regionOut[2].x+1, regionOut[2].y  , NULL);
      LineTo  (hDC, regionOut[3].x+1, regionOut[3].y  );
      MoveToEx(hDC, regionOut[3].x  , regionOut[3].y+1, NULL);
      LineTo  (hDC, regionOut[0].x  , regionOut[0].y+1);
      break;
   case 1:
      MoveToEx(hDC, regionOut[1].x+1, regionOut[1].y  , NULL);
      LineTo  (hDC, regionOut[2].x+1, regionOut[2].y  );
      MoveToEx(hDC, regionOut[2].x  , regionOut[2].y+1, NULL);
      LineTo  (hDC, regionOut[0].x  , regionOut[0].y+1);
      break;
   case 2:
      MoveToEx(hDC, regionOut[2].x+1, regionOut[2].y  , NULL);
      LineTo  (hDC, regionOut[0].x  , regionOut[0].y+1);
      break;
   case 3:
      MoveToEx(hDC, regionOut[1].x  , regionOut[1].y-1, NULL);
      LineTo  (hDC, regionOut[2].x+1, regionOut[2].y  );
      MoveToEx(hDC, regionOut[2].x  , regionOut[2].y+1, NULL);
      LineTo  (hDC, regionOut[0].x  , regionOut[0].y+1);
      break;
   case 5:
      MoveToEx(hDC, regionOut[2].x+1, regionOut[2].y  , NULL);
      LineTo  (hDC, regionOut[0].x+1, regionOut[0].y  );
      break;
   }
}

} // namespace nsFigure
