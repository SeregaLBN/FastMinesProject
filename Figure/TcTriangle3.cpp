////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "TcTriangle3.cpp"
//
// –еализаци€ класса TcTriangle3 - треугольник 45∞-90∞-45∞
////////////////////////////////////////////////////////////////////////////////

#include ".\TcTriangle3.h"
#include <math.h>

namespace nsFigure {

////////////////////////////////////////////////////////////////////////////////
//                               local init
////////////////////////////////////////////////////////////////////////////////

float TcTriangle3::b;

#ifdef SQUARE
#undef SQUARE
#endif // SQUARE

#define SQUARE (a/3 - 1)
////////////////////////////////////////////////////////////////////////////////
//                               static function
////////////////////////////////////////////////////////////////////////////////

POINT TcTriangle3::GetSizeFieldInPixel(const POINT& sizeField, const int& area) {
   a = sqrt(area)*2;
 //b = a/2;

   POINT result = {a*((sizeField.x+1)/2) + 1,
                   a*((sizeField.y+1)/2) + 1};
   return result;
}

int TcTriangle3::SizeInscribedSquare(int area) {
   a = sqrt(area)*2;
   sq = SQUARE;
   return sq;
}

float TcTriangle3::GetPercentMine(TeSkillLevel skill) { // процент мин на заданном уровне сложности
   switch (skill) {
   case skillLevelBeginner    : return 17.143f;
   case skillLevelAmateur     : return 20.143f;
   case skillLevelProfessional: return 23.143f;
   case skillLevelCrazy       : return 27.143f;
   }
   return 1.f;
}

////////////////////////////////////////////////////////////////////////////////
//                               virtual function
////////////////////////////////////////////////////////////////////////////////

TcTriangle3::TcTriangle3(const POINT& setCoord, const POINT& sizeField, const int& area) {
   coord = setCoord;
   Reset();

   direction = (coord.y%2)*2+(coord.x%2); // 0..3
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
      neighbor[ 6].x = coord.x+3;   neighbor[ 6].y = coord.y-1;
      neighbor[ 7].x = coord.x-2;   neighbor[ 7].y = coord.y  ;
      neighbor[ 8].x = coord.x-1;   neighbor[ 8].y = coord.y  ;
      neighbor[ 9].x = coord.x+1;   neighbor[ 9].y = coord.y  ;
      neighbor[10].x = coord.x+2;   neighbor[10].y = coord.y  ;
      neighbor[11].x = coord.x  ;   neighbor[11].y = coord.y+1;
      neighbor[12].x = coord.x+1;   neighbor[12].y = coord.y+1;
      neighbor[13].x = coord.x+2;   neighbor[13].y = coord.y+1;
      break;
   case  1:
      neighbor[ 0].x = coord.x  ;   neighbor[ 0].y = coord.y-2;
      neighbor[ 1].x = coord.x  ;   neighbor[ 1].y = coord.y-1;
      neighbor[ 2].x = coord.x+1;   neighbor[ 2].y = coord.y-1;
      neighbor[ 3].x = coord.x+2;   neighbor[ 3].y = coord.y-1;
      neighbor[ 4].x = coord.x-1;   neighbor[ 4].y = coord.y  ;
      neighbor[ 5].x = coord.x+1;   neighbor[ 5].y = coord.y  ;
      neighbor[ 6].x = coord.x-1;   neighbor[ 6].y = coord.y+1;
      neighbor[ 7].x = coord.x  ;   neighbor[ 7].y = coord.y+1;
      neighbor[ 8].x = coord.x+1;   neighbor[ 8].y = coord.y+1;
      neighbor[ 9].x = coord.x+2;   neighbor[ 9].y = coord.y+1;
      neighbor[10].x = coord.x-1;   neighbor[10].y = coord.y+2;
      neighbor[11].x = coord.x  ;   neighbor[11].y = coord.y+2;
      neighbor[12].x = coord.x+1;   neighbor[12].y = coord.y+2;
      neighbor[13].x = coord.x+1;   neighbor[13].y = coord.y+3;
      break;
   case  2:
      neighbor[ 0].x = coord.x-1;   neighbor[ 0].y = coord.y-3;
      neighbor[ 1].x = coord.x-1;   neighbor[ 1].y = coord.y-2;
      neighbor[ 2].x = coord.x  ;   neighbor[ 2].y = coord.y-2;
      neighbor[ 3].x = coord.x+1;   neighbor[ 3].y = coord.y-2;
      neighbor[ 4].x = coord.x-2;   neighbor[ 4].y = coord.y-1;
      neighbor[ 5].x = coord.x-1;   neighbor[ 5].y = coord.y-1;
      neighbor[ 6].x = coord.x  ;   neighbor[ 6].y = coord.y-1;
      neighbor[ 7].x = coord.x+1;   neighbor[ 7].y = coord.y-1;
      neighbor[ 8].x = coord.x-1;   neighbor[ 8].y = coord.y  ;
      neighbor[ 9].x = coord.x+1;   neighbor[ 9].y = coord.y  ;
      neighbor[10].x = coord.x-2;   neighbor[10].y = coord.y+1;
      neighbor[11].x = coord.x-1;   neighbor[11].y = coord.y+1;
      neighbor[12].x = coord.x  ;   neighbor[12].y = coord.y+1;
      neighbor[13].x = coord.x  ;   neighbor[13].y = coord.y+2;
      break;
   case  3:
      neighbor[ 0].x = coord.x-2;   neighbor[ 0].y = coord.y-1;
      neighbor[ 1].x = coord.x-1;   neighbor[ 1].y = coord.y-1;
      neighbor[ 2].x = coord.x  ;   neighbor[ 2].y = coord.y-1;
      neighbor[ 3].x = coord.x-2;   neighbor[ 3].y = coord.y  ;
      neighbor[ 4].x = coord.x-1;   neighbor[ 4].y = coord.y  ;
      neighbor[ 5].x = coord.x+1;   neighbor[ 5].y = coord.y  ;
      neighbor[ 6].x = coord.x+2;   neighbor[ 6].y = coord.y  ;
      neighbor[ 7].x = coord.x-3;   neighbor[ 7].y = coord.y+1;
      neighbor[ 8].x = coord.x-2;   neighbor[ 8].y = coord.y+1;
      neighbor[ 9].x = coord.x-1;   neighbor[ 9].y = coord.y+1;
      neighbor[10].x = coord.x  ;   neighbor[10].y = coord.y+1;
      neighbor[11].x = coord.x+1;   neighbor[11].y = coord.y+1;
      neighbor[12].x = coord.x-1;   neighbor[12].y = coord.y+2;
      neighbor[13].x = coord.x+1;   neighbor[13].y = coord.y+2;
      break;
   }
   for (int i=0; i<14; i++)
      if ((neighbor[i].x >= sizeField.x) ||
          (neighbor[i].y >= sizeField.y) ||
          (neighbor[i].x < 0) ||
          (neighbor[i].y < 0)) {
         neighbor[i] = CIncorrectCoord;
      }
}

int TcTriangle3::GetNeighborNumber() const {
   return 14;
}

POINT TcTriangle3::GetNeighborCoord(int index) const {
   return neighbor[index];
}

bool TcTriangle3::ToBelong(int x, int y) { // принадлежат ли эти экранные координаты €чейке
   POINT point = {x, y};
   return PointInPolygon(point, regionOut, 3);
}

void TcTriangle3::SetPoint(const int& area) {
   if (coord.x==0 && coord.y==0) {
      a = sqrt(area)*2;
      b = a/2;
      sq = SQUARE;
      TB::SetPoint(NULL);
   }

   // определение координат точек фигуры
   switch (direction) {
   case 0: case 1:
      regionOut[0].x = a*(coord.x/2) + a;   break;
   case 2:
      regionOut[0].x = a*(coord.x/2);       break;
   case 3:
      regionOut[0].x = a*(coord.x/2) + b;   break;
   }
   switch (direction) {
   case 0: case 1: case 2:
      regionOut[0].y = a*(coord.y/2);       break;
   case 3:
      regionOut[0].y = a*(coord.y/2) + b;   break;
   }
   switch (direction) {
   case 0:
      regionOut[1].x = regionOut[0].x-b;   regionOut[1].y = regionOut[0].y+b;
      regionOut[2].x = regionOut[0].x-a;   regionOut[2].y = regionOut[0].y  ;
      break;
   case 1:
      regionOut[1].x = regionOut[0].x  ;   regionOut[1].y = regionOut[0].y+a;
      regionOut[2].x = regionOut[0].x-b;   regionOut[2].y = regionOut[0].y+b;
      break;
   case 2:
      regionOut[1].x = regionOut[0].x+b;   regionOut[1].y = regionOut[0].y+b;
      regionOut[2].x = regionOut[0].x  ;   regionOut[2].y = regionOut[0].y+a;
      break;
   case 3:
      regionOut[1].x = regionOut[0].x+b;   regionOut[1].y = regionOut[0].y+b;
      regionOut[2].x = regionOut[0].x-b;   regionOut[2].y = regionOut[1].y  ;
      break;
   }

   // определение координат вписанного в фигуру квадрата - область в которую выводитс€ изображение/текст
   POINTFLOAT center; // координата центра квадрата
   switch (direction) {
   case 0:
      center.x = regionOut[1].x;
      center.y = regionOut[0].y + sq/2;
      center.x++;
      center.y += 2;
      break;
   case 1:
      center.x = regionOut[0].x - sq/2;
      center.y = regionOut[2].y;
      break;
   case 2:
      center.x = regionOut[0].x + sq/2;
      center.y = regionOut[1].y;
      center.x += 2;
      break;
   case 3:
      center.x = regionOut[0].x;
      center.y = regionOut[1].y - sq/2;
      center.x++;
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

void TcTriangle3::Paint() const {
   TB::Paint();

   SelectObject(hDC, down ? hPenWhite : hPenBlack);
   MoveToEx(hDC, regionOut[0].x, regionOut[0].y, NULL);
   LineTo  (hDC, regionOut[1].x, regionOut[1].y);
   switch (direction) {
   case  2: case 3:
      LineTo  (hDC, regionOut[2].x, regionOut[2].y);
   }

   SelectObject(hDC, down ? hPenBlack : hPenWhite);
   switch (direction) {
   case 0:
      MoveToEx(hDC, regionOut[1].x  , regionOut[1].y-1, NULL);
      LineTo  (hDC, regionOut[2].x  , regionOut[2].y-1);
      MoveToEx(hDC, regionOut[2].x  , regionOut[2].y+1, NULL);
      LineTo  (hDC, regionOut[0].x  , regionOut[0].y+1);
      break;
   case 1:
      MoveToEx(hDC, regionOut[1].x+1, regionOut[1].y  , NULL);
      LineTo  (hDC, regionOut[2].x+1, regionOut[2].y  );
      LineTo  (hDC, regionOut[0].x+1, regionOut[0].y  );
      break;
   case 2:
      MoveToEx(hDC, regionOut[2].x+1, regionOut[2].y  , NULL);
      LineTo  (hDC, regionOut[0].x+1, regionOut[0].y  );
      break;
   case 3:
      MoveToEx(hDC, regionOut[2].x  , regionOut[2].y+1, NULL);
      LineTo  (hDC, regionOut[0].x  , regionOut[0].y+1);
      break;
   }
}

} // namespace nsFigure
