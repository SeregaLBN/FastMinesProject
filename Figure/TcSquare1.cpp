////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "TcSquare1.cpp"
//
// Реализация класса TcSquare1 - квадрат (классический вариант поля)
////////////////////////////////////////////////////////////////////////////////

#include ".\TcSquare1.h"
#include <math.h>

namespace nsFigure {

////////////////////////////////////////////////////////////////////////////////
//                               static function
////////////////////////////////////////////////////////////////////////////////

POINT TcSquare1::GetSizeFieldInPixel(const POINT& sizeField, const int& area) {
   a = sqrt(area); // размер стороны квадрата
   POINT result = {sizeField.x * (int)a + 1,
                   sizeField.y * (int)a + 1};
   return result;
}

int TcSquare1::SizeInscribedSquare(int area) {
   sq = sqrt(area);
   return sq;
}

float TcSquare1::GetPercentMine(TeSkillLevel skill) { // процент мин на заданном уровне сложности
   switch (skill) {
   case skillLevelBeginner    : return 15.f;
   case skillLevelAmateur     : return 18.f;
   case skillLevelProfessional: return 21.f;
   case skillLevelCrazy       : return 25.f;
   }
   return 1.f;
}

////////////////////////////////////////////////////////////////////////////////
//                               virtual function
////////////////////////////////////////////////////////////////////////////////

TcSquare1::TcSquare1(const POINT& setCoord, const POINT& sizeField, const int& area) {
   coord = setCoord;
   Reset();
   SetPoint(area);

   // определяю координаты соседей
   neighbor[0].x = coord.x-1; neighbor[0].y = coord.y-1;
   neighbor[1].x = coord.x;   neighbor[1].y = coord.y-1;
   neighbor[2].x = coord.x+1; neighbor[2].y = coord.y-1;
   neighbor[3].x = coord.x-1; neighbor[3].y = coord.y;
   neighbor[4].x = coord.x+1; neighbor[4].y = coord.y;
   neighbor[5].x = coord.x-1; neighbor[5].y = coord.y+1;
   neighbor[6].x = coord.x;   neighbor[6].y = coord.y+1;
   neighbor[7].x = coord.x+1; neighbor[7].y = coord.y+1;
   for (int i=0; i<8; i++)
      if ((neighbor[i].x >= sizeField.x) ||
          (neighbor[i].y >= sizeField.y) ||
          (neighbor[i].x < 0) ||
          (neighbor[i].y < 0)) {
         neighbor[i] = CIncorrectCoord;
      }
}

int TcSquare1::GetNeighborNumber() const {
   return 8;
}

POINT TcSquare1::GetNeighborCoord(int index) const {
   return neighbor[index];
}

bool TcSquare1::ToBelong(int x, int y) {
   if ((x < regionOut[0].x) || (x > regionOut[1].x) ||
       (y < regionOut[0].y) || (y > regionOut[1].y))
      return false;
   return true;
}

void TcSquare1::SetPoint(const int& area) {
   if (coord.x==0 && coord.y==0) {
      a  = sqrt(area); // размер стороны квадрата
      sq = a;          // размер квадрата, вписанного в квадрат
      TB::SetPoint(NULL);
   }

   // определение координат точек фигуры
   regionOut[0].x = (int)a * coord.x;
   regionOut[0].y = (int)a * coord.y;
   regionOut[1].x = (int)a + regionOut[0].x;
   regionOut[1].y = (int)a + regionOut[0].y;

   // определение координат вписанного в фигуру квадрата - область в которую выводится изображение/текст
   regionIn.left   = regionOut[0].x;
   regionIn.top    = regionOut[0].y;
   regionIn.right  = regionOut[1].x;
   regionIn.bottom = regionOut[1].y;
   regionIn.left   += 2;
   regionIn.top    += 2;
   regionIn.right  += 0;
   regionIn.bottom += 0;
}

void TcSquare1::Paint() const {
   TB::Paint();

   SelectObject(hDC, down ? hPenWhite : hPenBlack);
   MoveToEx(hDC, regionOut[1].x  , regionOut[0].y, NULL);
   LineTo  (hDC, regionOut[1].x  , regionOut[1].y  );
   LineTo  (hDC, regionOut[0].x  , regionOut[1].y  );
   SelectObject(hDC, down ? hPenBlack : hPenWhite);
   MoveToEx(hDC, regionOut[0].x+1, regionOut[1].y, NULL);
   LineTo  (hDC, regionOut[0].x+1, regionOut[0].y+1);
   LineTo  (hDC, regionOut[1].x  , regionOut[0].y+1);
}

} // namespace nsFigure
