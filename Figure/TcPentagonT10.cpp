////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "TcPentagonT10.cpp"
//
// Реализация класса TcPentagonT10 - 5-ти угольник, тип №10
////////////////////////////////////////////////////////////////////////////////

#include ".\TcPentagonT10.h"
#include <math.h>

namespace nsFigure {

////////////////////////////////////////////////////////////////////////////////
//                               static function
////////////////////////////////////////////////////////////////////////////////

POINT TcPentagonT10::GetSizeFieldInPixel(const POINT& sizeField, const int& area) {
   a = sqrt(area/7);
   POINT result = {a*2 +
                     a*5*((sizeField.x+1)/2) +
                       a*((sizeField.x+0)/2),
                   a*2 +
                     a*3*((sizeField.y+2)/3) +
                     a*3*((sizeField.y+1)/3) +
                       a*((sizeField.y+0)/3) };
   return result;
}

int TcPentagonT10::SizeInscribedSquare(int area) {
   sq = sqrt(area/7)*2;
   return sq;
}

float TcPentagonT10::GetPercentMine(TeSkillLevel skill) { // процент мин на заданном уровне сложности
   switch (skill) {
   case skillLevelBeginner    : return 14.151f;
   case skillLevelAmateur     : return 17.151f;
   case skillLevelProfessional: return 20.151f;
   case skillLevelCrazy       : return 24.151f;
   }
   return 1.f;
}

////////////////////////////////////////////////////////////////////////////////
//                               virtual function
////////////////////////////////////////////////////////////////////////////////

TcPentagonT10::TcPentagonT10(const POINT& setCoord, const POINT& sizeField, const int& area) {
   coord = setCoord;
   Reset();

   direction = (coord.y%6)*2 + (coord.x&1);
   SetPoint(area);
   // определяю координаты соседей
   switch (direction) {
   case  0:
      neighbor[0].x = coord.x  ;   neighbor[0].y = coord.y-1;
      neighbor[1].x = coord.x-1;   neighbor[1].y = coord.y  ;
      neighbor[2].x = coord.x+1;   neighbor[2].y = coord.y  ;
      neighbor[3].x = coord.x-1;   neighbor[3].y = coord.y+1;
      neighbor[4].x = coord.x  ;   neighbor[4].y = coord.y+1;
      neighbor[5].x = coord.x+1;   neighbor[5].y = coord.y+1;
      neighbor[6].x = coord.x-1;   neighbor[6].y = coord.y+2;
      break;
   case  1:
      neighbor[0].x = coord.x  ;   neighbor[0].y = coord.y-2;
      neighbor[1].x = coord.x-1;   neighbor[1].y = coord.y-1;
      neighbor[2].x = coord.x  ;   neighbor[2].y = coord.y-1;
      neighbor[3].x = coord.x+1;   neighbor[3].y = coord.y-1;
      neighbor[4].x = coord.x-1;   neighbor[4].y = coord.y  ;
      neighbor[5].x = coord.x+1;   neighbor[5].y = coord.y  ;  
      neighbor[6].x = coord.x  ;   neighbor[6].y = coord.y+1;
      break;
   case  2:
      neighbor[0].x = coord.x  ;   neighbor[0].y = coord.y-1;
      neighbor[1].x = coord.x+1;   neighbor[1].y = coord.y  ;
      neighbor[2].x = coord.x-1;   neighbor[2].y = coord.y+1;
      neighbor[3].x = coord.x  ;   neighbor[3].y = coord.y+1;
      neighbor[4].x = coord.x+1;   neighbor[4].y = coord.y+1;
      neighbor[5].x = coord.x  ;   neighbor[5].y = coord.y+2;  
      neighbor[6].x = -1       ;   neighbor[6].y = -1       ;
      break;
   case  3:
      neighbor[0].x = coord.x-1;   neighbor[0].y = coord.y-1;
      neighbor[1].x = coord.x  ;   neighbor[1].y = coord.y-1;
      neighbor[2].x = coord.x+1;   neighbor[2].y = coord.y-1;
      neighbor[3].x = coord.x-1;   neighbor[3].y = coord.y  ;
      neighbor[4].x = coord.x-1;   neighbor[4].y = coord.y+1;
      neighbor[5].x = coord.x  ;   neighbor[5].y = coord.y+1;
      neighbor[6].x = -1       ;   neighbor[6].y = -1       ;
      break;
   case  4:
      neighbor[0].x = coord.x  ;   neighbor[0].y = coord.y-1;
      neighbor[1].x = coord.x+1;   neighbor[1].y = coord.y-1;
      neighbor[2].x = coord.x+1;   neighbor[2].y = coord.y  ;
      neighbor[3].x = coord.x  ;   neighbor[3].y = coord.y+1;
      neighbor[4].x = coord.x+1;   neighbor[4].y = coord.y+1;
      neighbor[5].x = coord.x+2;   neighbor[5].y = coord.y+1;
      neighbor[6].x = -1       ;   neighbor[6].y = -1       ;
      break;
   case  5:
      neighbor[0].x = coord.x+1;   neighbor[0].y = coord.y-2;
      neighbor[1].x = coord.x-1;   neighbor[1].y = coord.y-1;
      neighbor[2].x = coord.x  ;   neighbor[2].y = coord.y-1;
      neighbor[3].x = coord.x+1;   neighbor[3].y = coord.y-1;
      neighbor[4].x = coord.x-1;   neighbor[4].y = coord.y  ;
      neighbor[5].x = coord.x+1;   neighbor[5].y = coord.y+1;
      neighbor[6].x = -1       ;   neighbor[6].y = -1       ;
      break;
   case  6:
      neighbor[0].x = coord.x  ;   neighbor[0].y = coord.y-2;
      neighbor[1].x = coord.x-2;   neighbor[1].y = coord.y-1;
      neighbor[2].x = coord.x-1;   neighbor[2].y = coord.y-1;
      neighbor[3].x = coord.x  ;   neighbor[3].y = coord.y-1;
      neighbor[4].x = coord.x-1;   neighbor[4].y = coord.y  ;
      neighbor[5].x = coord.x+1;   neighbor[5].y = coord.y  ;
      neighbor[6].x = coord.x  ;   neighbor[6].y = coord.y+1;
      break;
   case  7:
      neighbor[0].x = coord.x-1;   neighbor[0].y = coord.y-1;
      neighbor[1].x = coord.x-1;   neighbor[1].y = coord.y  ;
      neighbor[2].x = coord.x+1;   neighbor[2].y = coord.y  ;
      neighbor[3].x = coord.x-1;   neighbor[3].y = coord.y+1;
      neighbor[4].x = coord.x  ;   neighbor[4].y = coord.y+1;
      neighbor[5].x = coord.x+1;   neighbor[5].y = coord.y+1;
      neighbor[6].x = coord.x  ;   neighbor[6].y = coord.y+2;
      break;
   case  8:
      neighbor[0].x = coord.x-1;   neighbor[0].y = coord.y-1;
      neighbor[1].x = coord.x  ;   neighbor[1].y = coord.y-1;
      neighbor[2].x = coord.x+1;   neighbor[2].y = coord.y-1;
      neighbor[3].x = coord.x+1;   neighbor[3].y = coord.y  ;
      neighbor[4].x = coord.x-1;   neighbor[4].y = coord.y+1;
      neighbor[5].x = coord.x  ;   neighbor[5].y = coord.y+1;
      neighbor[6].x = -1       ;   neighbor[6].y = -1       ;
      break;
   case  9:
      neighbor[0].x = coord.x  ;   neighbor[0].y = coord.y-1;
      neighbor[1].x = coord.x-1;   neighbor[1].y = coord.y  ;
      neighbor[2].x = coord.x-2;   neighbor[2].y = coord.y+1;
      neighbor[3].x = coord.x-1;   neighbor[3].y = coord.y+1;
      neighbor[4].x = coord.x  ;   neighbor[4].y = coord.y+1;
      neighbor[5].x = coord.x  ;   neighbor[5].y = coord.y+2;
      neighbor[6].x = -1       ;   neighbor[6].y = -1       ;
      break;
   case 10:
      neighbor[0].x = coord.x  ;   neighbor[0].y = coord.y-1;
      neighbor[1].x = coord.x+1;   neighbor[1].y = coord.y-1;
      neighbor[2].x = coord.x-1;   neighbor[2].y = coord.y  ;
      neighbor[3].x = coord.x-1;   neighbor[3].y = coord.y+1;
      neighbor[4].x = coord.x  ;   neighbor[4].y = coord.y+1;
      neighbor[5].x = coord.x+1;   neighbor[5].y = coord.y+1;
      neighbor[6].x = -1       ;   neighbor[6].y = -1       ;
      break;
   case 11:
      neighbor[0].x = coord.x  ;   neighbor[0].y = coord.y-2;
      neighbor[1].x = coord.x  ;   neighbor[1].y = coord.y-1;
      neighbor[2].x = coord.x+1;   neighbor[2].y = coord.y-1;
      neighbor[3].x = coord.x+2;   neighbor[3].y = coord.y-1;
      neighbor[4].x = coord.x+1;   neighbor[4].y = coord.y  ;
      neighbor[5].x = coord.x  ;   neighbor[5].y = coord.y+1;
      neighbor[6].x = -1       ;   neighbor[6].y = -1       ;
      break;
   }
   for (int i=0; i<7; i++)
      if ((neighbor[i].x >= sizeField.x) ||
          (neighbor[i].y >= sizeField.y) ||
          (neighbor[i].x < 0) ||
          (neighbor[i].y < 0)) {
         neighbor[i] = CIncorrectCoord;
      }
}

int TcPentagonT10::GetNeighborNumber() const {
   return 7;
}

POINT TcPentagonT10::GetNeighborCoord(int index) const {
   return neighbor[index];
}

bool TcPentagonT10::ToBelong(int x, int y) { // принадлежат ли эти экранные координаты ячейке
   POINT point = {x, y};
   return PointInPolygon(point, regionOut, 5);
}

void TcPentagonT10::SetPoint(const int& area) {
   if (coord.x==0 && coord.y==0) {
      a = sqrt(area/7);
      sq = a*2;
      TB::SetPoint(NULL);
   }

   // определение координат точек фигуры
   switch (direction) {
   case 0:
   case 6:
   case 8:
   case 9:
   case 10: regionOut[0].x = a*2+a*6*((coord.x+0)/2);   break;
   case 1:
   case 2:
   case 3:
   case 4:
   case 5:
   case 7:  regionOut[0].x = a*5+a*6*((coord.x+0)/2);   break;
   case 11: regionOut[0].x = a*2+a*6*((coord.x+1)/2);   break;
   }
   switch (direction) {
   case 0:  regionOut[0].y = a*5 +a*14*(coord.y/6);  break;
   case 1:  regionOut[0].y =      a*14*(coord.y/6);  break;
   case 2:
   case 3:
   case 4:
   case 5:  regionOut[0].y = a*6 +a*14*(coord.y/6);  break;
   case 6:  regionOut[0].y = a*7 +a*14*(coord.y/6);  break;
   case 7:  regionOut[0].y = a*12+a*14*(coord.y/6);  break;
   case 8:
   case 9:
   case 10:
   case 11: regionOut[0].y = a*13+a*14*(coord.y/6);  break;
   }
   switch (direction) {
   case 0:
   case 3:
   case 7:
   case 8:
      regionOut[1].x = regionOut[0].x-a*2; regionOut[1].y = regionOut[0].y-a*2;
      regionOut[2].x = regionOut[0].x-a  ; regionOut[2].y = regionOut[0].y-a*3;
      regionOut[3].x = regionOut[0].x+a  ; regionOut[3].y = regionOut[2].y    ;
      regionOut[4].x = regionOut[0].x+a*2; regionOut[4].y = regionOut[1].y    ;
      break;
   case 1:
   case 4:
   case 6:
   case 10:
      regionOut[1].x = regionOut[0].x+a*2; regionOut[1].y = regionOut[0].y+a*2;
      regionOut[2].x = regionOut[0].x+a  ; regionOut[2].y = regionOut[0].y+a*3;
      regionOut[3].x = regionOut[0].x-a  ; regionOut[3].y = regionOut[2].y    ;
      regionOut[4].x = regionOut[0].x-a*2; regionOut[4].y = regionOut[1].y    ;
      break;
   case 2:
   case 11:
      regionOut[1].x = regionOut[0].x-a*2; regionOut[1].y = regionOut[0].y+a*2;
      regionOut[2].x = regionOut[0].x-a*3; regionOut[2].y = regionOut[0].y+a  ;
      regionOut[3].x = regionOut[2].x    ; regionOut[3].y = regionOut[0].y-a  ;
      regionOut[4].x = regionOut[1].x    ; regionOut[4].y = regionOut[0].y-a*2;
      break;
   case 5:
   case 9:
      regionOut[1].x = regionOut[0].x+a*2; regionOut[1].y = regionOut[0].y-a*2;
      regionOut[2].x = regionOut[0].x+a*3; regionOut[2].y = regionOut[0].y-a  ;
      regionOut[3].x = regionOut[2].x    ; regionOut[3].y = regionOut[0].y+a  ;
      regionOut[4].x = regionOut[1].x    ; regionOut[4].y = regionOut[0].y+a*2;
      break;
   }

   // определение координат вписанного в фигуру квадрата - область в которую выводится изображение/текст
   switch (direction) {
   case 0: case 3: case 7: case 8:
      regionIn.left   = regionOut[2].x;
      regionIn.top    = regionOut[2].y;
      regionIn.right  = regionIn.left + sq;
      regionIn.bottom = regionIn.top  + sq;
      regionIn.left   += 1;
      regionIn.top    += 1;
      regionIn.right  += 1;
      regionIn.bottom += 1;
      break;
   case 1: case 4: case 6: case 10:
      regionIn.right  = regionOut[2].x;
      regionIn.bottom = regionOut[2].y;
      regionIn.left   = regionIn.right  - sq;
      regionIn.top    = regionIn.bottom - sq;
      break;
   case 2: case 11:
      regionIn.left   = regionOut[3].x;
      regionIn.top    = regionOut[3].y;
      regionIn.right  = regionIn.left + sq;
      regionIn.bottom = regionIn.top  + sq;
      regionIn.left   += 1;
      regionIn.top    += 1;
      regionIn.right  += 1;
      regionIn.bottom += 1;
      break;
   case 5: case 9:
      regionIn.right  = regionOut[3].x;
      regionIn.bottom = regionOut[3].y;
      regionIn.left   = regionIn.right  - sq;
      regionIn.top    = regionIn.bottom - sq;
      break;
   }
}

void TcPentagonT10::Paint() const {
   TB::Paint();

   SelectObject(hDC, down ? hPenBlack : hPenWhite);
   switch (direction) {
   case 0:
   case 3:
   case 7:
   case 8:
      MoveToEx(hDC, regionOut[0].x  , regionOut[0].y, NULL);
      LineTo  (hDC, regionOut[1].x  , regionOut[1].y);
      LineTo  (hDC, regionOut[2].x  , regionOut[2].y);
      LineTo  (hDC, regionOut[3].x  , regionOut[3].y);
      break;
   case 1:
   case 4:
   case 6:
   case 10:
      MoveToEx(hDC, regionOut[3].x  , regionOut[3].y, NULL);
      LineTo  (hDC, regionOut[4].x  , regionOut[4].y);
      LineTo  (hDC, regionOut[0].x  , regionOut[0].y);
      break;
   case 2:
   case 11:
      MoveToEx(hDC, regionOut[1].x  , regionOut[1].y, NULL);
      LineTo  (hDC, regionOut[2].x  , regionOut[2].y);
      LineTo  (hDC, regionOut[3].x  , regionOut[3].y);
      LineTo  (hDC, regionOut[4].x  , regionOut[4].y);
      break;
   case 5:
   case 9:
      MoveToEx(hDC, regionOut[4].x  , regionOut[4].y, NULL);
      LineTo  (hDC, regionOut[0].x  , regionOut[0].y);
      LineTo  (hDC, regionOut[1].x  , regionOut[1].y);
      break;
   }
   SelectObject(hDC, down ? hPenWhite : hPenBlack);
   switch (direction) {
   case 0:
   case 3:
   case 7:
   case 8:
      MoveToEx(hDC, regionOut[3].x-1, regionOut[3].y  , NULL);
      LineTo  (hDC, regionOut[4].x-1, regionOut[4].y  );
    //MoveToEx(hDC, regionOut[4].x-1, regionOut[4].y  , NULL);
      LineTo  (hDC, regionOut[0].x-1, regionOut[0].y  );
      break;
   case 1:
   case 4:
   case 6:
   case 10:
      MoveToEx(hDC, regionOut[0].x-1, regionOut[0].y  , NULL);
      LineTo  (hDC, regionOut[1].x-1, regionOut[1].y  );
    //MoveToEx(hDC, regionOut[1].x-1, regionOut[1].y  , NULL);
      LineTo  (hDC, regionOut[2].x-1, regionOut[2].y  );
      MoveToEx(hDC, regionOut[2].x  , regionOut[2].y-1, NULL);
      LineTo  (hDC, regionOut[3].x  , regionOut[3].y-1);
      break;
   case 2:
   case 11:
      MoveToEx(hDC, regionOut[4].x-1, regionOut[4].y  , NULL);
      LineTo  (hDC, regionOut[0].x-1, regionOut[0].y  );
    //MoveToEx(hDC, regionOut[0].x-1, regionOut[0].y  , NULL);
      LineTo  (hDC, regionOut[1].x-1, regionOut[1].y  );
      break;
   case 5:
   case 9:
      MoveToEx(hDC, regionOut[1].x-1, regionOut[1].y  , NULL);
      LineTo  (hDC, regionOut[2].x-1, regionOut[2].y  );
    //MoveToEx(hDC, regionOut[2].x-1, regionOut[2].y  , NULL);
      LineTo  (hDC, regionOut[3].x-1, regionOut[3].y  );
    //MoveToEx(hDC, regionOut[3].x-1, regionOut[3].y+1, NULL);
      LineTo  (hDC, regionOut[4].x-1, regionOut[4].y+1);
      break;
   }
}

} // namespace nsFigure
