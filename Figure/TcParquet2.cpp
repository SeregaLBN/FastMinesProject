////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "TcParquet2.cpp"
//
// Реализация класса TcParquet2 - паркет (тип №2)
////////////////////////////////////////////////////////////////////////////////

#include ".\TcParquet2.h"
#include <math.h>

namespace nsFigure {

////////////////////////////////////////////////////////////////////////////////
//                               static function
////////////////////////////////////////////////////////////////////////////////

POINT TcParquet2::GetSizeFieldInPixel(const POINT& sizeField, const int& area) {
   a = sqrt(area)/2; // размер стороны вписанного квадрата
   POINT result = {(int)a*(sizeField.x*2+2),
                   (int)a*(sizeField.y*2+2)};
   return result;
}

int TcParquet2::SizeInscribedSquare(int area) {
   sq = sqrt(area)/2;
   return sq;
}

float TcParquet2::GetPercentMine(TeSkillLevel skill) { // процент мин на заданном уровне сложности
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

TcParquet2::TcParquet2(const POINT& setCoord, const POINT& sizeField, const int& area) {
   coord = setCoord;
   Reset();

   direction = 2*(coord.y%2) + coord.x%2;
   SetPoint(area);

   // определяю координаты соседей
   switch (direction) {
   case 0:  neighbor[0].x = coord.x-1; neighbor[0].y = coord.y-1;
            neighbor[1].x = coord.x  ; neighbor[1].y = coord.y-1;
            neighbor[2].x = coord.x+1; neighbor[2].y = coord.y-1;
            neighbor[3].x = coord.x-1; neighbor[3].y = coord.y  ;
            neighbor[4].x = coord.x+1; neighbor[4].y = coord.y  ;
            neighbor[5].x = coord.x  ; neighbor[5].y = coord.y+1;
            neighbor[6].x = coord.x+1; neighbor[6].y = coord.y+1; break;
   case 1:  neighbor[0].x = coord.x  ; neighbor[0].y = coord.y-1;
            neighbor[1].x = coord.x+1; neighbor[1].y = coord.y-1;
            neighbor[2].x = coord.x-1; neighbor[2].y = coord.y  ;
            neighbor[3].x = coord.x+1; neighbor[3].y = coord.y  ;
            neighbor[4].x = coord.x-1; neighbor[4].y = coord.y+1;
            neighbor[5].x = coord.x  ; neighbor[5].y = coord.y+1;
            neighbor[6].x = coord.x+1; neighbor[6].y = coord.y+1; break;
   case 2:  neighbor[0].x = coord.x-1; neighbor[0].y = coord.y-1;
            neighbor[1].x = coord.x  ; neighbor[1].y = coord.y-1;
            neighbor[2].x = coord.x+1; neighbor[2].y = coord.y-1;
            neighbor[3].x = coord.x-1; neighbor[3].y = coord.y  ;
            neighbor[4].x = coord.x+1; neighbor[4].y = coord.y  ;
            neighbor[5].x = coord.x-1; neighbor[5].y = coord.y+1;
            neighbor[6].x = coord.x  ; neighbor[6].y = coord.y+1; break;
   case 3:  neighbor[0].x = coord.x-1; neighbor[0].y = coord.y-1;
            neighbor[1].x = coord.x  ; neighbor[1].y = coord.y-1;
            neighbor[2].x = coord.x-1; neighbor[2].y = coord.y  ;
            neighbor[3].x = coord.x+1; neighbor[3].y = coord.y  ;
            neighbor[4].x = coord.x-1; neighbor[4].y = coord.y+1;
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

int TcParquet2::GetNeighborNumber() const {
   return 7;
}

POINT TcParquet2::GetNeighborCoord(int index) const {
   return neighbor[index];
}

bool TcParquet2::ToBelong(int x, int y) { // принадлежат ли эти экранные координаты ячейке
   POINT point = {x, y};
   POINTFLOAT f[4] = {{regionOut[0].x, regionOut[0].y},
                      {regionOut[1].x, regionOut[1].y},
                      {regionOut[2].x, regionOut[2].y},
                      {regionOut[3].x, regionOut[3].y}};
   return PointInPolygon(point, f, 4);
}

void TcParquet2::SetPoint(const int& area) {
   if (coord.x==0 && coord.y==0) {
      sq = a = sqrt(area)/2; // размер стороны вписанного квадрата
      TB::SetPoint(NULL);
   }

   // определение координат точек фигуры
   switch (direction) {
   case 0:
      regionOut[0].x = (2*coord.x+2)*(int)a     ;   regionOut[0].y = (2*coord.y  )*(int)a;
      regionOut[1].x = regionOut[0].x + (int)a*2;   regionOut[1].y = regionOut[0].y + (int)a*2;
      regionOut[2].x = regionOut[0].x + (int)a  ;   regionOut[2].y = regionOut[0].y + (int)a*3;
      regionOut[3].x = regionOut[0].x - (int)a  ;   regionOut[3].y = regionOut[0].y + (int)a;
      break;
   case 1:
      regionOut[0].x = (2*coord.x+3)*(int)a     ;   regionOut[0].y = (2*coord.y+1)*(int)a;
      regionOut[1].x = regionOut[0].x + (int)a  ;   regionOut[1].y = regionOut[0].y + (int)a;
      regionOut[2].x = regionOut[0].x - (int)a  ;   regionOut[2].y = regionOut[0].y + (int)a*3;
      regionOut[3].x = regionOut[0].x - (int)a*2;   regionOut[3].y = regionOut[0].y + (int)a*2;
      break;
   case 2:
      regionOut[0].x = (2*coord.x+2)*(int)a     ;   regionOut[0].y = (2*coord.y  )*(int)a;
      regionOut[1].x = regionOut[0].x + (int)a  ;   regionOut[1].y = regionOut[0].y + (int)a;
      regionOut[2].x = regionOut[0].x - (int)a  ;   regionOut[2].y = regionOut[0].y + (int)a*3;
      regionOut[3].x = regionOut[0].x - (int)a*2;   regionOut[3].y = regionOut[0].y + (int)a*2;
      break;
   case 3:
      regionOut[0].x = (2*coord.x+1)*(int)a     ;   regionOut[0].y = (2*coord.y+1)*(int)a;
      regionOut[1].x = regionOut[0].x + (int)a*2;   regionOut[1].y = regionOut[0].y + (int)a*2;
      regionOut[2].x = regionOut[0].x + (int)a  ;   regionOut[2].y = regionOut[0].y + (int)a*3;
      regionOut[3].x = regionOut[0].x - (int)a  ;   regionOut[3].y = regionOut[0].y + (int)a;
      break;
   }

   // определение координат вписанного в фигуру квадрата - область в которую выводится изображение/текст
   switch (direction) {
   case 0:
   case 3:
      regionIn.left  = regionOut[0].x;   regionIn.top    = regionOut[3].y;
      regionIn.right = regionOut[2].x;   regionIn.bottom = regionOut[1].y;
      break;
   case 1:
   case 2:
      regionIn.left  = regionOut[2].x;   regionIn.top    = regionOut[1].y;
      regionIn.right = regionOut[0].x;   regionIn.bottom = regionOut[3].y;
      break;
   }
   regionIn.left   += 1;
   regionIn.top    += 1;
   regionIn.right  += 1;
   regionIn.bottom += 1;
}

void TcParquet2::Paint() const {
   TB::Paint();

   SelectObject(hDC, down ? hPenWhite : hPenBlack);
   MoveToEx(hDC, regionOut[0].x  , regionOut[0].y, NULL);
   LineTo  (hDC, regionOut[1].x  , regionOut[1].y);
   LineTo  (hDC, regionOut[2].x  , regionOut[2].y);
   SelectObject(hDC, down ? hPenBlack : hPenWhite);
   MoveToEx(hDC, regionOut[2].x  , regionOut[2].y-1, NULL);
   LineTo  (hDC, regionOut[3].x  , regionOut[3].y-1);
   MoveToEx(hDC, regionOut[3].x+1, regionOut[3].y  , NULL);
   LineTo  (hDC, regionOut[0].x+1, regionOut[0].y  );
}

} // namespace nsFigure
