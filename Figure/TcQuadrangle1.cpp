////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "TcQuadrangle1.cpp"
//
// –еализаци€ класса TcQuadrangle1 - четырЄхугольник 120∞-90∞-60∞-90∞
////////////////////////////////////////////////////////////////////////////////

#include ".\TcQuadrangle1.h"
#include <math.h>

namespace nsFigure {

////////////////////////////////////////////////////////////////////////////////
//                               local init
////////////////////////////////////////////////////////////////////////////////

float TcQuadrangle1::b;
float TcQuadrangle1::h;
float TcQuadrangle1::n;
float TcQuadrangle1::m;
float TcQuadrangle1::R;
float TcQuadrangle1::t;

////////////////////////////////////////////////////////////////////////////////
//                               static function
////////////////////////////////////////////////////////////////////////////////

POINT TcQuadrangle1::GetSizeFieldInPixel(const POINT& sizeField, const int& area) {
   a = sqrt(area/sqrt(3))*2;
   b = a/2;
   h = b*sqrt(3);
 //n = a*0.75f;
   m = h/2;

   POINT result = {m + m*((sizeField.x+2)/3)+
                       h*((sizeField.x+1)/3)+
                       m*((sizeField.x+0)/3) + 1,
                   b + b*((sizeField.y+1)/2)+
                       a*((sizeField.y+0)/2) + 1};
   return result;
}

int TcQuadrangle1::SizeInscribedSquare(int area) {
   a = sqrt(area/sqrt(3))*2;
   sq = a*sqrt(3)/(sqrt(3)+2); // размер квадрата, вписанного в трапецию
   return sq;
}

float TcQuadrangle1::GetPercentMine(TeSkillLevel skill) { // процент мин на заданном уровне сложности
   switch (skill) {
   case skillLevelBeginner    : return 15.222f;
   case skillLevelAmateur     : return 18.222f;
   case skillLevelProfessional: return 21.222f;
   case skillLevelCrazy       : return 25.222f;
   }
   return 1.f;
}

////////////////////////////////////////////////////////////////////////////////
//                               virtual function
////////////////////////////////////////////////////////////////////////////////

TcQuadrangle1::TcQuadrangle1(const POINT& setCoord, const POINT& sizeField, const int& area) {
   coord = setCoord;
   Reset();

   direction = (coord.y%4)*3+(coord.x%3); // 0..11
   SetPoint(area);

   // определ€ю координаты соседей
   switch (direction) {
   case  0:
      neighbor[0].x = coord.x-1;   neighbor[0].y = coord.y-1;
      neighbor[1].x = coord.x  ;   neighbor[1].y = coord.y-1;
      neighbor[2].x = coord.x-1;   neighbor[2].y = coord.y  ;
      neighbor[3].x = coord.x+1;   neighbor[3].y = coord.y  ;
      neighbor[4].x = coord.x+2;   neighbor[4].y = coord.y  ;
      neighbor[5].x = coord.x-1;   neighbor[5].y = coord.y+1;
      neighbor[6].x = coord.x  ;   neighbor[6].y = coord.y+1;
      neighbor[7].x = coord.x+1;   neighbor[7].y = coord.y+1;
      neighbor[8].x = coord.x+2;   neighbor[8].y = coord.y+1;
      break;
   case  1:
      neighbor[0].x = coord.x-2;   neighbor[0].y = coord.y-1;
      neighbor[1].x = coord.x-1;   neighbor[1].y = coord.y-1;
      neighbor[2].x = coord.x  ;   neighbor[2].y = coord.y-1;
      neighbor[3].x = coord.x+1;   neighbor[3].y = coord.y-1;
      neighbor[4].x = coord.x-1;   neighbor[4].y = coord.y  ;
      neighbor[5].x = coord.x+1;   neighbor[5].y = coord.y  ;  
      neighbor[6].x = coord.x-1;   neighbor[6].y = coord.y+1;
      neighbor[7].x = coord.x  ;   neighbor[7].y = coord.y+1;
      neighbor[8].x = coord.x+1;   neighbor[8].y = coord.y+1;
      break;
   case  2:
      neighbor[0].x = coord.x-1;   neighbor[0].y = coord.y-1;
      neighbor[1].x = coord.x  ;   neighbor[1].y = coord.y-1;
      neighbor[2].x = coord.x-2;   neighbor[2].y = coord.y  ;
      neighbor[3].x = coord.x-1;   neighbor[3].y = coord.y  ;
      neighbor[4].x = coord.x+1;   neighbor[4].y = coord.y  ;
      neighbor[5].x = coord.x-2;   neighbor[5].y = coord.y+1;  
      neighbor[6].x = coord.x-1;   neighbor[6].y = coord.y+1;
      neighbor[7].x = coord.x  ;   neighbor[7].y = coord.y+1;
      neighbor[8].x = coord.x+1;   neighbor[8].y = coord.y+1;
      break;
   case  3:
      neighbor[0].x = coord.x-1;   neighbor[0].y = coord.y-1;
      neighbor[1].x = coord.x  ;   neighbor[1].y = coord.y-1;
      neighbor[2].x = coord.x+1;   neighbor[2].y = coord.y-1;
      neighbor[3].x = coord.x+2;   neighbor[3].y = coord.y-1;
      neighbor[4].x = coord.x-1;   neighbor[4].y = coord.y  ;
      neighbor[5].x = coord.x+1;   neighbor[5].y = coord.y  ;
      neighbor[6].x = coord.x+2;   neighbor[6].y = coord.y  ;
      neighbor[7].x = coord.x-1;   neighbor[7].y = coord.y+1;
      neighbor[8].x = coord.x  ;   neighbor[8].y = coord.y+1;
      break;
   case  4:
      neighbor[0].x = coord.x-1;   neighbor[0].y = coord.y-1;
      neighbor[1].x = coord.x  ;   neighbor[1].y = coord.y-1;
      neighbor[2].x = coord.x+1;   neighbor[2].y = coord.y-1;
      neighbor[3].x = coord.x-1;   neighbor[3].y = coord.y  ;
      neighbor[4].x = coord.x+1;   neighbor[4].y = coord.y  ;
      neighbor[5].x = coord.x-2;   neighbor[5].y = coord.y+1;
      neighbor[6].x = coord.x-1;   neighbor[6].y = coord.y+1;
      neighbor[7].x = coord.x  ;   neighbor[7].y = coord.y+1;
      neighbor[8].x = coord.x+1;   neighbor[8].y = coord.y+1;
      break;
   case  5:
      neighbor[0].x = coord.x-2;   neighbor[0].y = coord.y-1;
      neighbor[1].x = coord.x-1;   neighbor[1].y = coord.y-1;
      neighbor[2].x = coord.x  ;   neighbor[2].y = coord.y-1;
      neighbor[3].x = coord.x+1;   neighbor[3].y = coord.y-1;
      neighbor[4].x = coord.x-2;   neighbor[4].y = coord.y  ;
      neighbor[5].x = coord.x-1;   neighbor[5].y = coord.y  ;
      neighbor[6].x = coord.x+1;   neighbor[6].y = coord.y  ;
      neighbor[7].x = coord.x-1;   neighbor[7].y = coord.y+1;
      neighbor[8].x = coord.x  ;   neighbor[8].y = coord.y+1;
      break;
   case  6:
      neighbor[0].x = coord.x  ;   neighbor[0].y = coord.y-1;
      neighbor[1].x = coord.x+1;   neighbor[1].y = coord.y-1;
      neighbor[2].x = coord.x-2;   neighbor[2].y = coord.y  ;
      neighbor[3].x = coord.x-1;   neighbor[3].y = coord.y  ;
      neighbor[4].x = coord.x+1;   neighbor[4].y = coord.y  ;
      neighbor[5].x = coord.x-2;   neighbor[5].y = coord.y+1;
      neighbor[6].x = coord.x-1;   neighbor[6].y = coord.y+1;
      neighbor[7].x = coord.x  ;   neighbor[7].y = coord.y+1;
      neighbor[8].x = coord.x+1;   neighbor[8].y = coord.y+1;
      break;
   case  7:
      neighbor[0].x = coord.x  ;   neighbor[0].y = coord.y-1;
      neighbor[1].x = coord.x+1;   neighbor[1].y = coord.y-1;
      neighbor[2].x = coord.x-1;   neighbor[2].y = coord.y  ;
      neighbor[3].x = coord.x+1;   neighbor[3].y = coord.y  ;
      neighbor[4].x = coord.x+2;   neighbor[4].y = coord.y  ;
      neighbor[5].x = coord.x-1;   neighbor[5].y = coord.y+1;
      neighbor[6].x = coord.x  ;   neighbor[6].y = coord.y+1;
      neighbor[7].x = coord.x+1;   neighbor[7].y = coord.y+1;
      neighbor[8].x = coord.x+2;   neighbor[8].y = coord.y+1;
      break;
   case  8:
      neighbor[0].x = coord.x-1;   neighbor[0].y = coord.y-1;
      neighbor[1].x = coord.x  ;   neighbor[1].y = coord.y-1;
      neighbor[2].x = coord.x+1;   neighbor[2].y = coord.y-1;
      neighbor[3].x = coord.x+2;   neighbor[3].y = coord.y-1;
      neighbor[4].x = coord.x-1;   neighbor[4].y = coord.y  ;
      neighbor[5].x = coord.x+1;   neighbor[5].y = coord.y  ;
      neighbor[6].x = coord.x-1;   neighbor[6].y = coord.y+1;
      neighbor[7].x = coord.x  ;   neighbor[7].y = coord.y+1;
      neighbor[8].x = coord.x+1;   neighbor[8].y = coord.y+1;
      break;
   case  9:
      neighbor[0].x = coord.x-2;   neighbor[0].y = coord.y-1;
      neighbor[1].x = coord.x-1;   neighbor[1].y = coord.y-1;
      neighbor[2].x = coord.x  ;   neighbor[2].y = coord.y-1;
      neighbor[3].x = coord.x+1;   neighbor[3].y = coord.y-1;
      neighbor[4].x = coord.x-2;   neighbor[4].y = coord.y  ;
      neighbor[5].x = coord.x-1;   neighbor[5].y = coord.y  ;
      neighbor[6].x = coord.x+1;   neighbor[6].y = coord.y  ;
      neighbor[7].x = coord.x  ;   neighbor[7].y = coord.y+1;
      neighbor[8].x = coord.x+1;   neighbor[8].y = coord.y+1;
      break;
   case 10:
      neighbor[0].x = coord.x-1;   neighbor[0].y = coord.y-1;
      neighbor[1].x = coord.x  ;   neighbor[1].y = coord.y-1;
      neighbor[2].x = coord.x+1;   neighbor[2].y = coord.y-1;
      neighbor[3].x = coord.x+2;   neighbor[3].y = coord.y-1;
      neighbor[4].x = coord.x-1;   neighbor[4].y = coord.y  ;
      neighbor[5].x = coord.x+1;   neighbor[5].y = coord.y  ;
      neighbor[6].x = coord.x+2;   neighbor[6].y = coord.y  ;
      neighbor[7].x = coord.x  ;   neighbor[7].y = coord.y+1;
      neighbor[8].x = coord.x+1;   neighbor[8].y = coord.y+1;
      break;
   case 11:
      neighbor[0].x = coord.x-1;   neighbor[0].y = coord.y-1;
      neighbor[1].x = coord.x  ;   neighbor[1].y = coord.y-1;
      neighbor[2].x = coord.x+1;   neighbor[2].y = coord.y-1;
      neighbor[3].x = coord.x-1;   neighbor[3].y = coord.y  ;
      neighbor[4].x = coord.x+1;   neighbor[4].y = coord.y  ;
      neighbor[5].x = coord.x-1;   neighbor[5].y = coord.y+1;
      neighbor[6].x = coord.x  ;   neighbor[6].y = coord.y+1;
      neighbor[7].x = coord.x+1;   neighbor[7].y = coord.y+1;
      neighbor[8].x = coord.x+2;   neighbor[8].y = coord.y+1;
      break;
   }
   for (int i=0; i<9; i++)
      if ((neighbor[i].x >= sizeField.x) ||
          (neighbor[i].y >= sizeField.y) ||
          (neighbor[i].x < 0) ||
          (neighbor[i].y < 0)) {
         neighbor[i] = CIncorrectCoord;
      }
}

int TcQuadrangle1::GetNeighborNumber() const {
   return 9;
}

POINT TcQuadrangle1::GetNeighborCoord(int index) const {
   return neighbor[index];
}

bool TcQuadrangle1::ToBelong(int x, int y) { // принадлежат ли эти экранные координаты €чейке
   POINT point = {x, y};
   return PointInPolygon(point, regionOut, 4);
}

void TcQuadrangle1::SetPoint(const int& area) {
   if (coord.x==0 && coord.y==0) {
      a = sqrt(area/sqrt(3))*2;
      b = a/2;
      h = b*sqrt(3);
      n = a*0.75f;
      m = h/2;
      sq = a*sqrt(3)/(sqrt(3)+2); // размер квадрата, вписанного в трапецию
      R = h*2/3;
      t = sq*sqrt(3)/2;
      TB::SetPoint(NULL);
   }

   // определение координат точек фигуры
   switch (direction) {
   case 0:  regionOut[0].x = (h*2)*(coord.x/3) + m;     break;
   case 3:
   case 4:
   case 6:
   case 9:  regionOut[0].x = (h*2)*(coord.x/3) + h;     break;
   case 1:
   case 7:  regionOut[0].x = (h*2)*(coord.x/3) + h+m;   break;
   case 2:
   case 5:
   case 10:
   case 11: regionOut[0].x = (h*2)*(coord.x/3) + h*2;   break;
   case 8:  regionOut[0].x = (h*2)*(coord.x/3) + h*2+m; break;
   }
   switch (direction) {
   case 0:
   case 1:  regionOut[0].y = (a*3)*(coord.y/4) + a-n;   break;
   case 2:  regionOut[0].y = (a*3)*(coord.y/4) + b;     break;
   case 3:
   case 4:
   case 5:  regionOut[0].y = (a*3)*(coord.y/4) + a;     break;
   case 7:
   case 8:  regionOut[0].y = (a*3)*(coord.y/4) + a+n;   break;
   case 6:  regionOut[0].y = (a*3)*(coord.y/4) + a*2;   break;
   case 9:
   case 10:
   case 11: regionOut[0].y = (a*3)*(coord.y/4) + a*2+b; break;
   }
   switch (direction) {
   case 0: case 7:
      regionOut[1].x = regionOut[0].x+m;   regionOut[1].y = regionOut[0].y+n;
      regionOut[2].x = regionOut[0].x-m;   regionOut[2].y = regionOut[1].y  ;
      regionOut[3].x = regionOut[2].x  ;   regionOut[3].y = regionOut[1].y-b;
      break;
   case 1: case 8:
      regionOut[1].x = regionOut[0].x-m;   regionOut[1].y = regionOut[0].y+n;
      regionOut[2].x = regionOut[0].x-h;   regionOut[2].y = regionOut[0].y  ;
      regionOut[3].x = regionOut[1].x  ;   regionOut[3].y = regionOut[1].y-a;
      break;
   case 2: case 6:
      regionOut[1].x = regionOut[0].x  ;   regionOut[1].y = regionOut[0].y+b;
      regionOut[2].x = regionOut[0].x-h;   regionOut[2].y = regionOut[1].y  ;
      regionOut[3].x = regionOut[0].x-m;   regionOut[3].y = regionOut[1].y-n;
      break;
   case 3: case 10:
      regionOut[1].x = regionOut[0].x-m;   regionOut[1].y = regionOut[0].y+n;
      regionOut[2].x = regionOut[0].x-h;   regionOut[2].y = regionOut[0].y+b;
      regionOut[3].x = regionOut[2].x  ;   regionOut[3].y = regionOut[0].y  ;
      break;
   case 4: case 11:
      regionOut[1].x = regionOut[0].x+m;   regionOut[1].y = regionOut[0].y+n;
      regionOut[2].x = regionOut[0].x  ;   regionOut[2].y = regionOut[0].y+a;
      regionOut[3].x = regionOut[0].x-m;   regionOut[3].y = regionOut[1].y  ;
      break;
   case 5: case 9:
      regionOut[1].x = regionOut[0].x  ;   regionOut[1].y = regionOut[0].y+b;
      regionOut[2].x = regionOut[0].x-m;   regionOut[2].y = regionOut[0].y+n;
      regionOut[3].x = regionOut[0].x-h;   regionOut[3].y = regionOut[0].y  ;
      break;
   }

   // определение координат вписанного в фигуру квадрата - область в которую выводитс€ изображение/текст
   switch (direction) {
   case 0: case 7:
      regionIn.right  = regionOut[3].x + R;
      regionIn.top    = regionOut[3].y;
      regionIn.left   = regionIn.right  - sq;
      regionIn.bottom = regionIn.top    + sq;
      break;
   case 1: case 8:
      regionIn.left   = regionOut[3].x - sq/2;
      regionIn.top    = regionOut[1].y - sq - t;
      regionIn.right  = regionIn.left   + sq;
      regionIn.bottom = regionIn.top    + sq;
      regionIn.left   += 1;
      regionIn.right  += 1;
      break;
   case 2: case 6:
      regionIn.left   = regionOut[0].x - R;
      regionIn.top    = regionOut[0].y;
      regionIn.right  = regionIn.left   + sq;
      regionIn.bottom = regionIn.top    + sq;
      break;
   case 3: case 10:
      regionIn.right  = regionOut[2].x + R;
      regionIn.bottom = regionOut[2].y;
      regionIn.left   = regionIn.right  - sq;
      regionIn.top    = regionIn.bottom - sq;
      break;
   case 4: case 11:
      regionIn.left   = regionOut[0].x - sq/2;
      regionIn.top    = regionOut[0].y + t;
      regionIn.right  = regionIn.left   + sq;
      regionIn.bottom = regionIn.top    + sq;
      regionIn.left   += 1;
      regionIn.right  += 1;
      break;
   case 5: case 9:
      regionIn.left   = regionOut[1].x - R;
      regionIn.bottom = regionOut[1].y;
      regionIn.right  = regionIn.left   + sq;
      regionIn.top    = regionIn.bottom - sq;
      regionIn.left   += 1;
      regionIn.right  += 1;
      break;
   }
   regionIn.left   += 0;
   regionIn.top    += 1;
   regionIn.right  += 0;
   regionIn.bottom += 1;
}

void TcQuadrangle1::Paint() const {
   TB::Paint();

   SelectObject(hDC, down ? hPenWhite : hPenBlack);
   MoveToEx(hDC, regionOut[0].x, regionOut[0].y, NULL);
   LineTo  (hDC, regionOut[1].x, regionOut[1].y);
   switch (direction) {
   case 0: case 2: case  3: case  4: case 5:
   case 7: case 6: case 10: case 11: case 9:
      LineTo(hDC, regionOut[2].x, regionOut[2].y);
   }
   switch (direction) {
   case 4: case 11:
      LineTo(hDC, regionOut[3].x, regionOut[3].y);
   }

   SelectObject(hDC, down ? hPenBlack : hPenWhite);
   switch (direction) {
   case 0: case 7:
   case 2: case 6:
   case 3: case 10:
   case 5: case 9:
      MoveToEx(hDC, regionOut[2].x+1, regionOut[2].y  , NULL);
      LineTo  (hDC, regionOut[3].x+1, regionOut[3].y  );
      MoveToEx(hDC, regionOut[3].x  , regionOut[3].y+1, NULL);
      LineTo  (hDC, regionOut[0].x  , regionOut[0].y+1);
      break;
   case 1: case 8:
      MoveToEx(hDC, regionOut[1].x+1, regionOut[1].y  , NULL);
      LineTo  (hDC, regionOut[2].x+1, regionOut[2].y  );
      MoveToEx(hDC, regionOut[2].x  , regionOut[2].y+1, NULL);
      LineTo  (hDC, regionOut[3].x  , regionOut[3].y+1);
      LineTo  (hDC, regionOut[0].x  , regionOut[0].y+1);
      break;
   case 4: case 11:
      MoveToEx(hDC, regionOut[3].x+1, regionOut[3].y  , NULL);
      LineTo  (hDC, regionOut[0].x+1, regionOut[0].y  );
      break;
   }
}

} // namespace nsFigure
