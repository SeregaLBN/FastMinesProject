////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "TcPentagon.cpp"
//
// Реализация класса TcPentagon - равносторонний 5-ти угольник
////////////////////////////////////////////////////////////////////////////////

#include ".\TcPentagon.h"
#include <math.h>

namespace nsFigure {

////////////////////////////////////////////////////////////////////////////////
//                               local init
////////////////////////////////////////////////////////////////////////////////

float TcPentagon::b;
float TcPentagon::L;

////////////////////////////////////////////////////////////////////////////////
//                               static function
////////////////////////////////////////////////////////////////////////////////

POINT TcPentagon::GetSizeFieldInPixel(const POINT& sizeField, const int& area) {
   a = 2*sqrt(area)/sqrt(4+sqrt(7)); // размер стороны пятиугольника
   b = a/sqrt(2);
   L = a/2*sqrt(4+sqrt(7));
   POINT result = {b + (float)sizeField.x*L + 1,
                   b + (float)sizeField.y*L + 1};
   return result;
}

int TcPentagon::SizeInscribedSquare(int area) {
   sq = sqrt(area*7)/(1+sqrt(7));
   return sq;
}

float TcPentagon::GetPercentMine(TeSkillLevel skill) { // процент мин на заданном уровне сложности
   switch (skill) {
   case skillLevelBeginner    : return 14.429f;
   case skillLevelAmateur     : return 17.429f;
   case skillLevelProfessional: return 20.429f;
   case skillLevelCrazy       : return 24.429f;
   }
   return 1.f;
}

////////////////////////////////////////////////////////////////////////////////
//                               virtual function
////////////////////////////////////////////////////////////////////////////////

TcPentagon::TcPentagon(const POINT& setCoord, const POINT& sizeField, const int& area) {
   coord = setCoord;
   Reset();

   direction = 2*(coord.y%2) + coord.x%2;
   SetPoint(area);
   // определяю координаты соседей
   switch (direction) {
   case 0:  neighbor[0].x = coord.x-1; neighbor[0].y = coord.y-1;
            neighbor[1].x = coord.x  ; neighbor[1].y = coord.y-1;
            neighbor[2].x = coord.x-1; neighbor[2].y = coord.y  ;
            neighbor[3].x = coord.x+1; neighbor[3].y = coord.y  ;
            neighbor[4].x = coord.x-1; neighbor[4].y = coord.y+1;
            neighbor[5].x = coord.x  ; neighbor[5].y = coord.y+1;
            neighbor[6].x = coord.x+1; neighbor[6].y = coord.y+1; break;
   case 1:  neighbor[0].x = coord.x-1; neighbor[0].y = coord.y-1;
            neighbor[1].x = coord.x  ; neighbor[1].y = coord.y-1;
            neighbor[2].x = coord.x+1; neighbor[2].y = coord.y-1;
            neighbor[3].x = coord.x-1; neighbor[3].y = coord.y  ;
            neighbor[4].x = coord.x+1; neighbor[4].y = coord.y  ;
            neighbor[5].x = coord.x-1; neighbor[5].y = coord.y+1;
            neighbor[6].x = coord.x  ; neighbor[6].y = coord.y+1; break;
   case 2:  neighbor[0].x = coord.x  ; neighbor[0].y = coord.y-1;
            neighbor[1].x = coord.x+1; neighbor[1].y = coord.y-1;
            neighbor[2].x = coord.x-1; neighbor[2].y = coord.y  ;
            neighbor[3].x = coord.x+1; neighbor[3].y = coord.y  ;
            neighbor[4].x = coord.x-1; neighbor[4].y = coord.y+1;
            neighbor[5].x = coord.x  ; neighbor[5].y = coord.y+1;
            neighbor[6].x = coord.x+1; neighbor[6].y = coord.y+1; break;
   case 3:  neighbor[0].x = coord.x-1; neighbor[0].y = coord.y-1;
            neighbor[1].x = coord.x  ; neighbor[1].y = coord.y-1;
            neighbor[2].x = coord.x+1; neighbor[2].y = coord.y-1;
            neighbor[3].x = coord.x-1; neighbor[3].y = coord.y  ;
            neighbor[4].x = coord.x+1; neighbor[4].y = coord.y  ;
            neighbor[5].x = coord.x  ; neighbor[5].y = coord.y+1;
            neighbor[6].x = coord.x+1; neighbor[6].y = coord.y+1; break;
   }
   for (int i=0; i<7; i++)
      if ((neighbor[i].x >= sizeField.x) ||
          (neighbor[i].y >= sizeField.y) ||
          (neighbor[i].x < 0) ||
          (neighbor[i].y < 0)) {
         neighbor[i] = CIncorrectCoord;
      }
}

int TcPentagon::GetNeighborNumber() const {
   return 7;
}

POINT TcPentagon::GetNeighborCoord(int index) const {
   return neighbor[index];
}

bool TcPentagon::ToBelong(int x, int y) { // принадлежат ли эти экранные координаты ячейке
   POINT point = {x, y};
   return PointInPolygon(point, regionOut, 5);
}

void TcPentagon::SetPoint(const int& area) {
   if (coord.x==0 && coord.y==0) {
      a = 2*sqrt(area)/sqrt(4+sqrt(7)); // размер стороны пятиугольника
      b = a/sqrt(2);
      L = a/2*sqrt(4+sqrt(7));
      sq = sqrt(area*7)/(1+sqrt(7));
      TB::SetPoint(NULL);
   }

   // определение координат точек фигуры
   switch (direction) {
   case 0:
      regionOut[0].x = b/2 +   L;   regionOut[0].y = b/2 +   L;
      regionOut[1].x = b        ;   regionOut[1].y = b   +   L;
      regionOut[2].x = 0        ;   regionOut[2].y =         L;
      regionOut[3].x = b/2      ;   regionOut[3].y = b/2      ;
      regionOut[4].x =         L;   regionOut[4].y = b        ;
      break;
   case 1:
      regionOut[0].x = b/2 +   L;   regionOut[0].y = b/2 +   L;
      regionOut[1].x =         L;   regionOut[1].y = b        ;
      regionOut[2].x = b   +   L;   regionOut[2].y = 0        ;
      regionOut[3].x = b/2 + 2*L;   regionOut[3].y = b/2      ;
      regionOut[4].x =       2*L;   regionOut[4].y =         L;
      break;
   case 2:
      regionOut[0].x = b/2 +   L;   regionOut[0].y = b/2 +   L;
      regionOut[1].x = b   +   L;   regionOut[1].y =       2*L;
      regionOut[2].x =         L;   regionOut[2].y = b   + 2*L;
      regionOut[3].x = b/2      ;   regionOut[3].y = b/2 + 2*L;
      regionOut[4].x = b        ;   regionOut[4].y = b   +   L;
      break;
   case 3:
      regionOut[0].x = b/2 +   L;   regionOut[0].y = b/2 +   L;
      regionOut[1].x =       2*L;   regionOut[1].y =         L;
      regionOut[2].x = b   + 2*L;   regionOut[2].y = b   +   L;
      regionOut[3].x = b/2 + 2*L;   regionOut[3].y = b/2 + 2*L;
      regionOut[4].x = b   +   L;   regionOut[4].y =       2*L;
      break;
   }
   regionOut[0].x += L*2*(coord.x/2);  regionOut[0].y += L*2*(coord.y/2);
   regionOut[1].x += L*2*(coord.x/2);  regionOut[1].y += L*2*(coord.y/2);
   regionOut[2].x += L*2*(coord.x/2);  regionOut[2].y += L*2*(coord.y/2);
   regionOut[3].x += L*2*(coord.x/2);  regionOut[3].y += L*2*(coord.y/2);
   regionOut[4].x += L*2*(coord.x/2);  regionOut[4].y += L*2*(coord.y/2);

   // определение координат вписанного в фигуру квадрата - область в которую выводится изображение/текст
   switch (direction) {
   case 0:  regionIn.left  = regionOut[3].x;   regionIn.top    = regionOut[4].y;
            regionIn.right = regionOut[4].x;   regionIn.bottom = regionOut[0].y;  break;
   case 1:  regionIn.left  = regionOut[0].x;   regionIn.top    = regionOut[3].y;
            regionIn.right = regionOut[4].x;   regionIn.bottom = regionOut[4].y;  break;
   case 2:  regionIn.left  = regionOut[4].x;   regionIn.top    = regionOut[4].y;
            regionIn.right = regionOut[0].x;   regionIn.bottom = regionOut[3].y;  break;
   case 3:  regionIn.left  = regionOut[4].x;   regionIn.top    = regionOut[0].y;
            regionIn.right = regionOut[3].x;   regionIn.bottom = regionOut[4].y;  break;
   }
   regionIn.left   += 1;
   regionIn.top    += 1;
   regionIn.right  += 1;
   regionIn.bottom += 1;
}

void TcPentagon::Paint() const {
   TB::Paint();

   SelectObject(hDC, down ? hPenWhite : hPenBlack);
   switch (direction) {
   case 0:
      MoveToEx(hDC, regionOut[4].x  , regionOut[4].y, NULL);
      LineTo  (hDC, regionOut[0].x  , regionOut[0].y);
      LineTo  (hDC, regionOut[1].x  , regionOut[1].y);
      LineTo  (hDC, regionOut[2].x  , regionOut[2].y);
      break;
   case 1:
      MoveToEx(hDC, regionOut[3].x  , regionOut[3].y, NULL);
      LineTo  (hDC, regionOut[4].x  , regionOut[4].y);
      LineTo  (hDC, regionOut[0].x  , regionOut[0].y);
      break;
   case 2:
      MoveToEx(hDC, regionOut[0].x  , regionOut[0].y, NULL);
      LineTo  (hDC, regionOut[1].x  , regionOut[1].y);
      LineTo  (hDC, regionOut[2].x  , regionOut[2].y);
      LineTo  (hDC, regionOut[3].x  , regionOut[3].y);
      break;
   case 3:
      MoveToEx(hDC, regionOut[2].x  , regionOut[2].y, NULL);
      LineTo  (hDC, regionOut[3].x  , regionOut[3].y);
      LineTo  (hDC, regionOut[4].x  , regionOut[4].y);
      break;
   }
   SelectObject(hDC, down ? hPenBlack : hPenWhite);
   switch (direction) {
   case 0:
      MoveToEx(hDC, regionOut[2].x+1, regionOut[2].y  , NULL);
      LineTo  (hDC, regionOut[3].x+1, regionOut[3].y  );
      MoveToEx(hDC, regionOut[3].x  , regionOut[3].y+1, NULL);
      LineTo  (hDC, regionOut[4].x  , regionOut[4].y+1);
      break;
   case 1:
      MoveToEx(hDC, regionOut[0].x+1, regionOut[0].y  , NULL);
      LineTo  (hDC, regionOut[1].x+1, regionOut[1].y  );
      MoveToEx(hDC, regionOut[1].x  , regionOut[1].y+1, NULL);
      LineTo  (hDC, regionOut[2].x  , regionOut[2].y+1);
    //MoveToEx(hDC, regionOut[2].x  , regionOut[2].y+1, NULL);
      LineTo  (hDC, regionOut[3].x  , regionOut[3].y+1);
      break;
   case 2:
      MoveToEx(hDC, regionOut[3].x+1, regionOut[3].y  , NULL);
      LineTo  (hDC, regionOut[4].x+1, regionOut[4].y  );
      MoveToEx(hDC, regionOut[4].x  , regionOut[4].y+1, NULL);
      LineTo  (hDC, regionOut[0].x  , regionOut[0].y+1);
      break;
   case 3:
      MoveToEx(hDC, regionOut[4].x+1, regionOut[4].y  , NULL);
      LineTo  (hDC, regionOut[0].x+1, regionOut[0].y  );
      MoveToEx(hDC, regionOut[0].x  , regionOut[0].y+1, NULL);
      LineTo  (hDC, regionOut[1].x  , regionOut[1].y+1);
    //MoveToEx(hDC, regionOut[1].x  , regionOut[1].y+1, NULL);
      LineTo  (hDC, regionOut[2].x  , regionOut[2].y+1);
      break;
   }
}

} // namespace nsFigure
