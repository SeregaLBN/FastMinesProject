////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "TcSqTrHex.cpp"
//
// Реализация класса TcSqTrHex - 6Square 4Triangle 2Hexagon
////////////////////////////////////////////////////////////////////////////////

#include ".\TcSqTrHex.h"
#include <math.h>

namespace nsFigure {

////////////////////////////////////////////////////////////////////////////////
//                               local init
////////////////////////////////////////////////////////////////////////////////

float TcSqTrHex::h; // высота треугольника

////////////////////////////////////////////////////////////////////////////////
//                               static function
////////////////////////////////////////////////////////////////////////////////

POINT TcSqTrHex::GetSizeFieldInPixel(const POINT& sizeField, const int& area) {
   a = sqrt(area/(0.5f+1.0f/sqrt(3)));
   h = a*sqrt(3)/2;

   POINT result = {a/2+h + a/2*((sizeField.x+2)/3) +
                             h*((sizeField.x+1)/3) +
                       (a/2+h)*((sizeField.x+0)/3),
                   a/2   +   h*((sizeField.y+1)/2)+
                         a*3/2*((sizeField.y+0)/2)};
   return result;
}

int TcSqTrHex::SizeInscribedSquare(int area) {
   a = sqrt(area/(0.5f+1.0f/sqrt(3)));
   sq = a*sqrt(3)/(sqrt(3)+2);
   return sq;
}

float TcSqTrHex::GetPercentMine(TeSkillLevel skill) { // процент мин на заданном уровне сложности
   switch (skill) {
   case skillLevelBeginner    : return 15.5f;
   case skillLevelAmateur     : return 18.5f;
   case skillLevelProfessional: return 21.5f;
   case skillLevelCrazy       : return 25.5f;
   }
   return 1.f;
}

////////////////////////////////////////////////////////////////////////////////
//                               virtual function
////////////////////////////////////////////////////////////////////////////////

TcSqTrHex::TcSqTrHex(const POINT& setCoord, const POINT& sizeField, const int& area) {
   coord = setCoord;
   Reset();

   direction = (coord.y%4)*3+(coord.x%3); // 0..11
   SetPoint(area);

   // определяю координаты соседей
   switch (direction) {
   case  0:
      neighbor[ 0].x = coord.x-1;   neighbor[ 0].y = coord.y-1;
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
      neighbor[11].x = -1       ;   neighbor[11].y = -1       ;
      break;
   case  1:
      neighbor[ 0].x = coord.x-2;   neighbor[ 0].y = coord.y-1;
      neighbor[ 1].x = coord.x  ;   neighbor[ 1].y = coord.y-1;
      neighbor[ 2].x = coord.x-1;   neighbor[ 2].y = coord.y  ;
      neighbor[ 3].x = coord.x+1;   neighbor[ 3].y = coord.y  ;
      neighbor[ 4].x = coord.x-2;   neighbor[ 4].y = coord.y+1;
      neighbor[ 5].x = coord.x-1;   neighbor[ 5].y = coord.y+1;
      neighbor[ 6].x = coord.x  ;   neighbor[ 6].y = coord.y+1;
      neighbor[ 7].x = coord.x+1;   neighbor[ 7].y = coord.y+1;
      neighbor[ 8].x = -1       ;   neighbor[ 8].y = -1       ;
      neighbor[ 9].x = -1       ;   neighbor[ 9].y = -1       ;
      neighbor[10].x = -1       ;   neighbor[10].y = -1       ;
      neighbor[11].x = -1       ;   neighbor[11].y = -1       ;
      break;
   case  2:
      neighbor[ 0].x = coord.x-3;   neighbor[ 0].y = coord.y-1;
      neighbor[ 1].x = coord.x-1;   neighbor[ 1].y = coord.y-1;
      neighbor[ 2].x = coord.x  ;   neighbor[ 2].y = coord.y-1;
      neighbor[ 3].x = coord.x-1;   neighbor[ 3].y = coord.y  ;
      neighbor[ 4].x = coord.x  ;   neighbor[ 4].y = coord.y+1;
      neighbor[ 5].x = coord.x-1;   neighbor[ 5].y = coord.y+1;
      neighbor[ 6].x = -1       ;   neighbor[ 6].y = -1       ;
      neighbor[ 7].x = -1       ;   neighbor[ 7].y = -1       ;
      neighbor[ 8].x = -1       ;   neighbor[ 8].y = -1       ;
      neighbor[ 9].x = -1       ;   neighbor[ 9].y = -1       ;
      neighbor[10].x = -1       ;   neighbor[10].y = -1       ;
      neighbor[11].x = -1       ;   neighbor[11].y = -1       ;
      break;
   case  3:
      neighbor[ 0].x = coord.x  ;   neighbor[ 0].y = coord.y-1;
      neighbor[ 1].x = coord.x+1;   neighbor[ 1].y = coord.y-1;
      neighbor[ 2].x = coord.x-2;   neighbor[ 2].y = coord.y  ;
      neighbor[ 3].x = coord.x-1;   neighbor[ 3].y = coord.y  ;
      neighbor[ 4].x = coord.x+1;   neighbor[ 4].y = coord.y  ;
      neighbor[ 5].x = coord.x-1;   neighbor[ 5].y = coord.y+1;
      neighbor[ 6].x = coord.x  ;   neighbor[ 6].y = coord.y+1;
      neighbor[ 7].x = coord.x  ;   neighbor[ 7].y = coord.y+2;
      neighbor[ 8].x = -1       ;   neighbor[ 8].y = -1       ;
      neighbor[ 9].x = -1       ;   neighbor[ 9].y = -1       ;
      neighbor[10].x = -1       ;   neighbor[10].y = -1       ;
      neighbor[11].x = -1       ;   neighbor[11].y = -1       ;
      break;
   case  4:
      neighbor[ 0].x = coord.x-1;   neighbor[ 0].y = coord.y-1;
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
      neighbor[11].x = coord.x-1;   neighbor[11].y = coord.y+2;
      break;
   case  5:
      neighbor[ 0].x = coord.x-1;   neighbor[ 0].y = coord.y-2;
      neighbor[ 1].x = coord.x  ;   neighbor[ 1].y = coord.y-2;
      neighbor[ 2].x = coord.x-1;   neighbor[ 2].y = coord.y-1;
      neighbor[ 3].x = coord.x  ;   neighbor[ 3].y = coord.y-1;
      neighbor[ 4].x = coord.x+1;   neighbor[ 4].y = coord.y-1;
      neighbor[ 5].x = coord.x+2;   neighbor[ 5].y = coord.y-1;
      neighbor[ 6].x = coord.x-1;   neighbor[ 6].y = coord.y  ;
      neighbor[ 7].x = coord.x+1;   neighbor[ 7].y = coord.y  ;
      neighbor[ 8].x = -1       ;   neighbor[ 8].y = -1       ;
      neighbor[ 9].x = -1       ;   neighbor[ 9].y = -1       ;
      neighbor[10].x = -1       ;   neighbor[10].y = -1       ;
      neighbor[11].x = -1       ;   neighbor[11].y = -1       ;
      break;
   case  6:
      neighbor[ 0].x = coord.x-2;   neighbor[ 0].y = coord.y-1;
      neighbor[ 1].x = coord.x  ;   neighbor[ 1].y = coord.y-1;
      neighbor[ 2].x = coord.x+1;   neighbor[ 2].y = coord.y-1;
      neighbor[ 3].x = coord.x-1;   neighbor[ 3].y = coord.y  ;
      neighbor[ 4].x = coord.x-1;   neighbor[ 4].y = coord.y+1;
      neighbor[ 5].x = coord.x  ;   neighbor[ 5].y = coord.y+1;
      neighbor[ 6].x = -1       ;   neighbor[ 6].y = -1       ;
      neighbor[ 7].x = -1       ;   neighbor[ 7].y = -1       ;
      neighbor[ 8].x = -1       ;   neighbor[ 8].y = -1       ;
      neighbor[ 9].x = -1       ;   neighbor[ 9].y = -1       ;
      neighbor[10].x = -1       ;   neighbor[10].y = -1       ;
      neighbor[11].x = -1       ;   neighbor[11].y = -1       ;
      break;
   case  7:
      neighbor[ 0].x = coord.x  ;   neighbor[ 0].y = coord.y-1;
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
      neighbor[11].x = -1       ;   neighbor[11].y = -1       ;
      break;
   case  8:
      neighbor[ 0].x = coord.x-1;   neighbor[ 0].y = coord.y-1;
      neighbor[ 1].x = coord.x+1;   neighbor[ 1].y = coord.y-1;
      neighbor[ 2].x = coord.x-1;   neighbor[ 2].y = coord.y  ;
      neighbor[ 3].x = coord.x+1;   neighbor[ 3].y = coord.y  ;
      neighbor[ 4].x = coord.x-2;   neighbor[ 4].y = coord.y+1;
      neighbor[ 5].x = coord.x-1;   neighbor[ 5].y = coord.y+1;
      neighbor[ 6].x = coord.x  ;   neighbor[ 6].y = coord.y+1;
      neighbor[ 7].x = coord.x+1;   neighbor[ 7].y = coord.y+1;
      neighbor[ 8].x = -1       ;   neighbor[ 8].y = -1       ;
      neighbor[ 9].x = -1       ;   neighbor[ 9].y = -1       ;
      neighbor[10].x = -1       ;   neighbor[10].y = -1       ;
      neighbor[11].x = -1       ;   neighbor[11].y = -1       ;
      break;
   case  9:
      neighbor[ 0].x = coord.x  ;   neighbor[ 0].y = coord.y-2;
      neighbor[ 1].x = coord.x+1;   neighbor[ 1].y = coord.y-2;
      neighbor[ 2].x = coord.x-1;   neighbor[ 2].y = coord.y-1;
      neighbor[ 3].x = coord.x  ;   neighbor[ 3].y = coord.y-1;
      neighbor[ 4].x = coord.x+1;   neighbor[ 4].y = coord.y-1;
      neighbor[ 5].x = coord.x+2;   neighbor[ 5].y = coord.y-1;
      neighbor[ 6].x = coord.x-1;   neighbor[ 6].y = coord.y  ;
      neighbor[ 7].x = coord.x+1;   neighbor[ 7].y = coord.y  ;
      neighbor[ 8].x = -1       ;   neighbor[ 8].y = -1       ;
      neighbor[ 9].x = -1       ;   neighbor[ 9].y = -1       ;
      neighbor[10].x = -1       ;   neighbor[10].y = -1       ;
      neighbor[11].x = -1       ;   neighbor[11].y = -1       ;
      break;
   case 10:
      neighbor[ 0].x = coord.x  ;   neighbor[ 0].y = coord.y-1;
      neighbor[ 1].x = coord.x+1;   neighbor[ 1].y = coord.y-1;
      neighbor[ 2].x = coord.x-2;   neighbor[ 2].y = coord.y  ;
      neighbor[ 3].x = coord.x-1;   neighbor[ 3].y = coord.y  ;
      neighbor[ 4].x = coord.x+1;   neighbor[ 4].y = coord.y  ;
      neighbor[ 5].x = coord.x  ;   neighbor[ 5].y = coord.y+1;
      neighbor[ 6].x = coord.x+1;   neighbor[ 6].y = coord.y+1;
      neighbor[ 7].x = coord.x+1;   neighbor[ 7].y = coord.y+2;
      neighbor[ 8].x = -1       ;   neighbor[ 8].y = -1       ;
      neighbor[ 9].x = -1       ;   neighbor[ 9].y = -1       ;
      neighbor[10].x = -1       ;   neighbor[10].y = -1       ;
      neighbor[11].x = -1       ;   neighbor[11].y = -1       ;
      break;
   case 11:
      neighbor[ 0].x = coord.x-1;   neighbor[ 0].y = coord.y-1;
      neighbor[ 1].x = coord.x  ;   neighbor[ 1].y = coord.y-1;
      neighbor[ 2].x = coord.x+1;   neighbor[ 2].y = coord.y-1;
      neighbor[ 3].x = coord.x+2;   neighbor[ 3].y = coord.y-1;
      neighbor[ 4].x = coord.x-1;   neighbor[ 4].y = coord.y  ;
      neighbor[ 5].x = coord.x+1;   neighbor[ 5].y = coord.y  ;
      neighbor[ 6].x = coord.x+2;   neighbor[ 6].y = coord.y  ;
      neighbor[ 7].x = coord.x  ;   neighbor[ 7].y = coord.y+1;
      neighbor[ 8].x = coord.x+1;   neighbor[ 8].y = coord.y+1;
      neighbor[ 9].x = coord.x+2;   neighbor[ 9].y = coord.y+1;
      neighbor[10].x = coord.x+3;   neighbor[10].y = coord.y+1;
      neighbor[11].x = coord.x  ;   neighbor[11].y = coord.y+2;
      break;
   }
   for (int i=0; i<12; i++)
      if ((neighbor[i].x >= sizeField.x) ||
          (neighbor[i].y >= sizeField.y) ||
          (neighbor[i].x < 0) ||
          (neighbor[i].y < 0)) {
         neighbor[i] = CIncorrectCoord;
      }
}

int TcSqTrHex::GetNeighborNumber() const {
   return 12;
}

POINT TcSqTrHex::GetNeighborCoord(int index) const {
   return neighbor[index];
}

bool TcSqTrHex::ToBelong(int x, int y) { // принадлежат ли эти экранные координаты ячейке
   POINT point = {x, y};
   switch (direction) {
   case  0:
   case  2:
   case  6:
   case  7: return PointInPolygon(point, regionOut, 3);
   case  1:
   case  3:
   case  5:
   case  8:
   case  9:
   case 10: return PointInPolygon(point, regionOut, 4);
   case  4:
   case 11: return PointInPolygon(point, regionOut, 6);
   }
   return false;
}

void TcSqTrHex::SetPoint(const int& area) {
   if (coord.x==0 && coord.y==0) {
      a = sqrt(area/(0.5f+1.0f/sqrt(3)));
      h = a*sqrt(3)/2;
      sq = a*sqrt(3)/(sqrt(3)+2);
      TB::SetPoint(NULL);
   }

   // определение координат точек фигуры
   float b = a/2;
   switch (direction) {
   case  0:
   case  3: regionOut[0].x = a      +(h*2+a)*(coord.x/3);  regionOut[0].y =   b+h  +(h*2+a*3)*(coord.y/4);   break;
   case  1:
   case  4: regionOut[0].x = a  +h  +(h*2+a)*(coord.x/3);  regionOut[0].y =     h  +(h*2+a*3)*(coord.y/4);   break;
   case  2:
   case  5: regionOut[0].x = a+b+h  +(h*2+a)*(coord.x/3);  regionOut[0].y =         (h*2+a*3)*(coord.y/4);   break;
   case  6:
   case  9: regionOut[0].x = a      +(h*2+a)*(coord.x/3);  regionOut[0].y = a+b+h  +(h*2+a*3)*(coord.y/4);   break;
   case  7:
   case 10: regionOut[0].x = a+b+h  +(h*2+a)*(coord.x/3);  regionOut[0].y = a*2+h*2+(h*2+a*3)*(coord.y/4);   break;
   case  8:
   case 11: regionOut[0].x = a+b+h*2+(h*2+a)*(coord.x/3);  regionOut[0].y = a+b+h*2+(h*2+a*3)*(coord.y/4);   break;
   }
   switch (direction) {
   case 0:
   case 7:
      regionOut[1].x = regionOut[0].x-a;   regionOut[1].y = regionOut[0].y  ;
      regionOut[2].x = regionOut[0].x-b;   regionOut[2].y = regionOut[0].y-h;
      break;
   case 1:
   case 8:
      regionOut[1].x = regionOut[0].x-h;   regionOut[1].y = regionOut[0].y+b;
      regionOut[2].x = regionOut[1].x-b;   regionOut[2].y = regionOut[1].y-h;
      regionOut[3].x = regionOut[0].x-b;   regionOut[3].y = regionOut[0].y-h;
      break;
   case 2:
   case 6:
      regionOut[1].x = regionOut[0].x-b;   regionOut[1].y = regionOut[0].y+h;
      regionOut[2].x = regionOut[0].x-a;   regionOut[2].y = regionOut[0].y  ;
      break;
   case 3:
   case 10:
      regionOut[1].x = regionOut[0].x  ;   regionOut[1].y = regionOut[0].y+a;
      regionOut[2].x = regionOut[0].x-a;   regionOut[2].y = regionOut[1].y  ;
      regionOut[3].x = regionOut[2].x  ;   regionOut[3].y = regionOut[0].y  ;
      break;
   case 4:
   case 11:
      regionOut[1].x = regionOut[0].x+h;   regionOut[1].y = regionOut[0].y+b;
      regionOut[2].x = regionOut[1].x  ;   regionOut[2].y = regionOut[1].y+a;
      regionOut[3].x = regionOut[0].x  ;   regionOut[3].y = regionOut[2].y+b;
      regionOut[4].x = regionOut[0].x-h;   regionOut[4].y = regionOut[2].y  ;
      regionOut[5].x = regionOut[4].x  ;   regionOut[5].y = regionOut[1].y  ;
      break;
   case 5:
   case 9:
      regionOut[1].x = regionOut[0].x+h;   regionOut[1].y = regionOut[0].y+b;
      regionOut[2].x = regionOut[1].x-b;   regionOut[2].y = regionOut[1].y+h;
      regionOut[3].x = regionOut[0].x-b;   regionOut[3].y = regionOut[0].y+h;
      break;
   }

   // определение координат вписанного в фигуру квадрата - область в которую выводится изображение/текст
   POINTFLOAT center; // координата центра фигуры
   float R = h*2/3;
   float q = (h+b)/2;
   switch (direction) {
   case 0: case 7:
      center.x = regionOut[2].x  ; center.y = regionOut[0].y-sq/2;
      center.y--;
      break;
   case 1: case 8:
      center.x = regionOut[0].x-q; center.y = regionOut[3].y+q;
      break;
   case 2: case 6:
      center.x = regionOut[1].x  ; center.y = regionOut[0].y+sq/2;
      center.y++;
      break;
   case 3: case 10:
      center.x = regionOut[0].x-b; center.y = regionOut[0].y+b;
      break;
   case 4: case 11:
      center.x = regionOut[0].x  ; center.y = regionOut[0].y+a;
      break;
   case 5: case 9:
      center.x = regionOut[1].x-q; center.y = regionOut[0].y+q;
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

void TcSqTrHex::Paint() const {
   TB::Paint();

   SelectObject(hDC, down ? hPenBlack : hPenWhite);
   switch (direction) {
   case 0:
   case 7:
      MoveToEx(hDC, regionOut[1].x, regionOut[1].y, NULL);
      LineTo  (hDC, regionOut[2].x, regionOut[2].y);
      break;
   case 1:
   case 8:
      MoveToEx(hDC, regionOut[1].x, regionOut[1].y, NULL);
      LineTo  (hDC, regionOut[2].x, regionOut[2].y);
      LineTo  (hDC, regionOut[3].x, regionOut[3].y);
      break;
   case 2:
   case 6:
      MoveToEx(hDC, regionOut[1].x, regionOut[1].y, NULL);
      LineTo  (hDC, regionOut[2].x, regionOut[2].y);
      LineTo  (hDC, regionOut[0].x, regionOut[0].y);
      break;
   case 3:
   case 10:
      MoveToEx(hDC, regionOut[2].x, regionOut[2].y, NULL);
      LineTo  (hDC, regionOut[3].x, regionOut[3].y);
      LineTo  (hDC, regionOut[0].x, regionOut[0].y);
      break;
   case 4:
   case 11:
      MoveToEx(hDC, regionOut[3].x, regionOut[3].y, NULL);
      LineTo  (hDC, regionOut[4].x, regionOut[4].y);
      LineTo  (hDC, regionOut[5].x, regionOut[5].y);
      LineTo  (hDC, regionOut[0].x, regionOut[0].y);
      break;
   case 5:
   case 9:
      MoveToEx(hDC, regionOut[2].x, regionOut[2].y, NULL);
      LineTo  (hDC, regionOut[3].x, regionOut[3].y);
      LineTo  (hDC, regionOut[0].x, regionOut[0].y);
      break;
   }

   SelectObject(hDC, down ? hPenWhite : hPenBlack);
   switch (direction) {
   case 0:
   case 7:
      MoveToEx(hDC, regionOut[2].x-1, regionOut[2].y  , NULL);
      LineTo  (hDC, regionOut[0].x-1, regionOut[0].y  );
      MoveToEx(hDC, regionOut[0].x  , regionOut[0].y-1, NULL);
      LineTo  (hDC, regionOut[1].x  , regionOut[1].y-1);
      break;
   case 1:
   case 8:
      MoveToEx(hDC, regionOut[3].x-1, regionOut[3].y  , NULL);
      LineTo  (hDC, regionOut[0].x-1, regionOut[0].y  );
    //MoveToEx(hDC, regionOut[0].x-1, regionOut[0].y  , NULL);
      LineTo  (hDC, regionOut[1].x-1, regionOut[1].y  );
      break;
   case 2:
   case 6:
      MoveToEx(hDC, regionOut[0].x-1, regionOut[0].y  , NULL);
      LineTo  (hDC, regionOut[1].x-1, regionOut[1].y  );
      break;
   case 3:
   case 10:
      MoveToEx(hDC, regionOut[0].x-1, regionOut[0].y  , NULL);
      LineTo  (hDC, regionOut[1].x-1, regionOut[1].y  );
      MoveToEx(hDC, regionOut[1].x  , regionOut[1].y-1, NULL);
      LineTo  (hDC, regionOut[2].x  , regionOut[2].y-1);
      break;
   case 4:
   case 11:
      MoveToEx(hDC, regionOut[0].x-1, regionOut[0].y  , NULL);
      LineTo  (hDC, regionOut[1].x-1, regionOut[1].y  );
    //MoveToEx(hDC, regionOut[1].x-1, regionOut[1].y  , NULL);
      LineTo  (hDC, regionOut[2].x-1, regionOut[2].y  );
    //MoveToEx(hDC, regionOut[2].x-1, regionOut[2].y  , NULL);
      LineTo  (hDC, regionOut[3].x-1, regionOut[3].y  );
      break;
   case 5:
   case 9:
      MoveToEx(hDC, regionOut[0].x-1, regionOut[0].y  , NULL);
      LineTo  (hDC, regionOut[1].x-1, regionOut[1].y  );
    //MoveToEx(hDC, regionOut[1].x-1, regionOut[1].y  , NULL);
      LineTo  (hDC, regionOut[2].x-1, regionOut[2].y  );
      break;
   }
}

} // namespace nsFigure
