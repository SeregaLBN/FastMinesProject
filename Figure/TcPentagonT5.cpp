////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "TcPentagonT5.cpp"
//
// Реализация класса TcPentagonT5 - 5-ти угольник, тип №5
////////////////////////////////////////////////////////////////////////////////

#include ".\TcPentagonT5.h"
#include <math.h>

namespace nsFigure {

////////////////////////////////////////////////////////////////////////////////
//                               local init
////////////////////////////////////////////////////////////////////////////////

float TcPentagonT5::h;

////////////////////////////////////////////////////////////////////////////////
//                               static function
////////////////////////////////////////////////////////////////////////////////

POINT TcPentagonT5::GetSizeFieldInPixel(const POINT& sizeField, const int& area) {
   a = sqrt(area/sqrt(147))*2;
   h = a*sqrt(3)/2;
   POINT result = {a*3.5f + a*2.0f*((sizeField.x+13)/14) +
                            a     *((sizeField.x+12)/14) +
                            a*1.5f*((sizeField.x+11)/14) +
                            a*2.0f*((sizeField.x+10)/14) +
                            a     *((sizeField.x+ 9)/14) +
                            a*1.5f*((sizeField.x+ 8)/14) +
                            a*2.0f*((sizeField.x+ 7)/14) +
                            a     *((sizeField.x+ 6)/14) +
                            a*1.5f*((sizeField.x+ 5)/14) +
                            a*2.0f*((sizeField.x+ 4)/14) +
                            a     *((sizeField.x+ 3)/14) +
                            a*2.0f*((sizeField.x+ 2)/14) +
                            a     *((sizeField.x+ 1)/14) +
                            a*1.5f*((sizeField.x+ 0)/14),
                   h*5 +    h*2   *((sizeField.y+ 5)/ 6) +
                            h*2   *((sizeField.y+ 4)/ 6) +
                            h*2   *((sizeField.y+ 3)/ 6) +
                            h*3   *((sizeField.y+ 2)/ 6) +
                            h*2   *((sizeField.y+ 1)/ 6) +
                            h*3   *((sizeField.y+ 0)/ 6) + 1};
   return result;
}

int TcPentagonT5::SizeInscribedSquare(int area) {
   a = sqrt(area/sqrt(147))*2;
   sq = a*2*sqrt(3)/(sqrt(3)+1);
   return sq;
}

float TcPentagonT5::GetPercentMine(TeSkillLevel skill) { // процент мин на заданном уровне сложности
   switch (skill) {
   case skillLevelBeginner    : return 14.5f;
   case skillLevelAmateur     : return 17.5f;
   case skillLevelProfessional: return 20.5f;
   case skillLevelCrazy       : return 24.5f;
   }
   return 1.f;
}

////////////////////////////////////////////////////////////////////////////////
//                               virtual function
////////////////////////////////////////////////////////////////////////////////

TcPentagonT5::TcPentagonT5(const POINT& setCoord, const POINT& sizeField, const int& area) {
   coord = setCoord;
   Reset();

   direction = (coord.y%6)*14 + (coord.x%14);
   SetPoint(area);
   // определяю координаты соседей
   switch (direction) {
   case 0:
      neighbor[0].x = coord.x-2;   neighbor[0].y = coord.y-2;
      neighbor[1].x = coord.x-3;   neighbor[1].y = coord.y-1;
      neighbor[2].x = coord.x-2;   neighbor[2].y = coord.y-1;
      neighbor[3].x = coord.x+1;   neighbor[3].y = coord.y  ;
      neighbor[4].x = coord.x+2;   neighbor[4].y = coord.y  ;
      neighbor[5].x = coord.x  ;   neighbor[5].y = coord.y+1;
      neighbor[6].x = coord.x+1;   neighbor[6].y = coord.y+1;
      neighbor[7].x = coord.x+2;   neighbor[7].y = coord.y+1;
      break;
   case 3: case 6: case 9:
      neighbor[0].x = coord.x-2;   neighbor[0].y = coord.y-1;
      neighbor[1].x = coord.x-1;   neighbor[1].y = coord.y  ;
      neighbor[2].x = coord.x+1;   neighbor[2].y = coord.y  ;
      neighbor[3].x = coord.x+2;   neighbor[3].y = coord.y  ;
      neighbor[4].x = coord.x-1;   neighbor[4].y = coord.y+1;
      neighbor[5].x = coord.x  ;   neighbor[5].y = coord.y+1;  
      neighbor[6].x = coord.x+1;   neighbor[6].y = coord.y+1;
      neighbor[7].x = coord.x+2;   neighbor[7].y = coord.y+1;
      break;
   case 12:
      neighbor[0].x = coord.x-2;   neighbor[0].y = coord.y-1;
      neighbor[1].x = coord.x-1;   neighbor[1].y = coord.y  ;
      neighbor[2].x = coord.x+1;   neighbor[2].y = coord.y  ;
      neighbor[3].x = coord.x-1;   neighbor[3].y = coord.y+1;
      neighbor[4].x = coord.x  ;   neighbor[4].y = coord.y+1;
      neighbor[5].x = coord.x+1;   neighbor[5].y = coord.y+1;
      neighbor[6].x = coord.x  ;   neighbor[6].y = coord.y+2;
      neighbor[7].x = coord.x+1;   neighbor[7].y = coord.y+2;
      break;
   case 28:
      neighbor[0].x = coord.x-1;   neighbor[0].y = coord.y-1;
      neighbor[1].x = coord.x+1;   neighbor[1].y = coord.y-1;
      neighbor[2].x = coord.x-1;   neighbor[2].y = coord.y  ;
      neighbor[3].x = coord.x+1;   neighbor[3].y = coord.y  ;
      neighbor[4].x = coord.x+2;   neighbor[4].y = coord.y  ;
      neighbor[5].x = coord.x  ;   neighbor[5].y = coord.y+1;
      neighbor[6].x = coord.x+1;   neighbor[6].y = coord.y+1;
      neighbor[7].x = coord.x+2;   neighbor[7].y = coord.y+1;
      break;
   case 31: case 34: case 37: case 56: case 59: case 62:
      neighbor[0].x = coord.x+1;   neighbor[0].y = coord.y-1;
      neighbor[1].x = coord.x-1;   neighbor[1].y = coord.y  ;
      neighbor[2].x = coord.x+1;   neighbor[2].y = coord.y  ;
      neighbor[3].x = coord.x+2;   neighbor[3].y = coord.y  ;
      neighbor[4].x = coord.x-1;   neighbor[4].y = coord.y+1;
      neighbor[5].x = coord.x  ;   neighbor[5].y = coord.y+1;
      neighbor[6].x = coord.x+1;   neighbor[6].y = coord.y+1;
      neighbor[7].x = coord.x+2;   neighbor[7].y = coord.y+1;
      break;
   case 54:
      neighbor[0].x = coord.x-1;   neighbor[0].y = coord.y-1;
      neighbor[1].x = coord.x  ;   neighbor[1].y = coord.y-1;
      neighbor[2].x = coord.x-1;   neighbor[2].y = coord.y  ;
      neighbor[3].x = coord.x+1;   neighbor[3].y = coord.y  ;
      neighbor[4].x = coord.x-1;   neighbor[4].y = coord.y+1;
      neighbor[5].x = coord.x  ;   neighbor[5].y = coord.y+1;
      neighbor[6].x = coord.x+1;   neighbor[6].y = coord.y+1;
      neighbor[7].x = coord.x+1;   neighbor[7].y = coord.y+2;
      break;
   case 65:
      neighbor[0].x = coord.x+1;   neighbor[0].y = coord.y-1;
      neighbor[1].x = coord.x-1;   neighbor[1].y = coord.y  ;
      neighbor[2].x = coord.x+1;   neighbor[2].y = coord.y  ;
      neighbor[3].x = coord.x-1;   neighbor[3].y = coord.y+1;
      neighbor[4].x = coord.x  ;   neighbor[4].y = coord.y+1;
      neighbor[5].x = coord.x+1;   neighbor[5].y = coord.y+1;
      neighbor[6].x = coord.x+2;   neighbor[6].y = coord.y+1;
      neighbor[7].x = coord.x+3;   neighbor[7].y = coord.y+1;
      break;
   case 1:
      neighbor[0].x = coord.x-3;   neighbor[0].y = coord.y-2;
      neighbor[1].x = coord.x-2;   neighbor[1].y = coord.y-1;
      neighbor[2].x = coord.x-1;   neighbor[2].y = coord.y-1;
      neighbor[3].x = coord.x-1;   neighbor[3].y = coord.y  ;
      neighbor[4].x = coord.x+1;   neighbor[4].y = coord.y  ;
      neighbor[5].x = coord.x-1;   neighbor[5].y = coord.y+1;
      neighbor[6].x = coord.x  ;   neighbor[6].y = coord.y+1;
      neighbor[7].x = coord.x+1;   neighbor[7].y = coord.y+1;
      break;
   case 4: case 7: case 10:
      neighbor[0].x = coord.x-3;   neighbor[0].y = coord.y-1;
      neighbor[1].x = coord.x-2;   neighbor[1].y = coord.y-1;
      neighbor[2].x = coord.x-1;   neighbor[2].y = coord.y-1;
      neighbor[3].x = coord.x-1;   neighbor[3].y = coord.y  ;
      neighbor[4].x = coord.x+1;   neighbor[4].y = coord.y  ;
      neighbor[5].x = coord.x-1;   neighbor[5].y = coord.y+1;
      neighbor[6].x = coord.x  ;   neighbor[6].y = coord.y+1;
      neighbor[7].x = coord.x+1;   neighbor[7].y = coord.y+1;
      break;
   case 13:
      neighbor[0].x = coord.x-3;   neighbor[0].y = coord.y-1;
      neighbor[1].x = coord.x-2;   neighbor[1].y = coord.y-1;
      neighbor[2].x = coord.x-1;   neighbor[2].y = coord.y  ;
      neighbor[3].x = coord.x-1;   neighbor[3].y = coord.y+1;
      neighbor[4].x = coord.x  ;   neighbor[4].y = coord.y+1;
      neighbor[5].x = coord.x+1;   neighbor[5].y = coord.y+1;
      neighbor[6].x = coord.x-1;   neighbor[6].y = coord.y+2;
      neighbor[7].x = coord.x  ;   neighbor[7].y = coord.y+2;
      break;
   case 29: case 32: case 35: case 38: case 57: case 60: case 63:
      neighbor[0].x = coord.x  ;   neighbor[0].y = coord.y-1;
      neighbor[1].x = coord.x+1;   neighbor[1].y = coord.y-1;
      neighbor[2].x = coord.x+2;   neighbor[2].y = coord.y-1;
      neighbor[3].x = coord.x-1;   neighbor[3].y = coord.y  ;
      neighbor[4].x = coord.x+1;   neighbor[4].y = coord.y  ;
      neighbor[5].x = coord.x-1;   neighbor[5].y = coord.y+1;
      neighbor[6].x = coord.x  ;   neighbor[6].y = coord.y+1;
      neighbor[7].x = coord.x+1;   neighbor[7].y = coord.y+1;
      break;
   case 55:
      neighbor[0].x = coord.x-1;   neighbor[0].y = coord.y-1;
      neighbor[1].x = coord.x  ;   neighbor[1].y = coord.y-1;
      neighbor[2].x = coord.x-1;   neighbor[2].y = coord.y  ;
      neighbor[3].x = coord.x+1;   neighbor[3].y = coord.y  ;
      neighbor[4].x = coord.x-2;   neighbor[4].y = coord.y+1;
      neighbor[5].x = coord.x-1;   neighbor[5].y = coord.y+1;
      neighbor[6].x = coord.x  ;   neighbor[6].y = coord.y+1;
      neighbor[7].x = coord.x  ;   neighbor[7].y = coord.y+2;
      break;
   case 66:
      neighbor[0].x = coord.x  ;   neighbor[0].y = coord.y-1;
      neighbor[1].x = coord.x+1;   neighbor[1].y = coord.y-1;
      neighbor[2].x = coord.x-1;   neighbor[2].y = coord.y  ;
      neighbor[3].x = coord.x+1;   neighbor[3].y = coord.y  ;
      neighbor[4].x = coord.x-1;   neighbor[4].y = coord.y+1;
      neighbor[5].x = coord.x  ;   neighbor[5].y = coord.y+1;
      neighbor[6].x = coord.x+1;   neighbor[6].y = coord.y+1;
      neighbor[7].x = coord.x+2;   neighbor[7].y = coord.y+1;
      break;
   case 2: case 5: case 8: case 11:
      neighbor[0].x = coord.x-2;   neighbor[0].y = coord.y-1;
      neighbor[1].x = coord.x-1;   neighbor[1].y = coord.y-1;
      neighbor[2].x = coord.x-2;   neighbor[2].y = coord.y  ;
      neighbor[3].x = coord.x-1;   neighbor[3].y = coord.y  ;
      neighbor[4].x = coord.x+1;   neighbor[4].y = coord.y  ;
      neighbor[5].x = coord.x-2;   neighbor[5].y = coord.y+1;
      neighbor[6].x = coord.x-1;   neighbor[6].y = coord.y+1;
      neighbor[7].x = coord.x  ;   neighbor[7].y = coord.y+1;
      break;
   case 27:
      neighbor[0].x = coord.x-1;   neighbor[0].y = coord.y-1;
      neighbor[1].x = coord.x  ;   neighbor[1].y = coord.y-1;
      neighbor[2].x = coord.x-1;   neighbor[2].y = coord.y  ;
      neighbor[3].x = coord.x+1;   neighbor[3].y = coord.y  ;
      neighbor[4].x = coord.x+2;   neighbor[4].y = coord.y  ;
      neighbor[5].x = coord.x-1;   neighbor[5].y = coord.y+1;
      neighbor[6].x = coord.x  ;   neighbor[6].y = coord.y+1;
      neighbor[7].x = coord.x+1;   neighbor[7].y = coord.y+1;
      break;
   case 30: case 33: case 36: case 58: case 61: case 64:
      neighbor[0].x = coord.x+1;   neighbor[0].y = coord.y-1;
      neighbor[1].x = coord.x+2;   neighbor[1].y = coord.y-1;
      neighbor[2].x = coord.x-2;   neighbor[2].y = coord.y  ;
      neighbor[3].x = coord.x-1;   neighbor[3].y = coord.y  ;
      neighbor[4].x = coord.x+1;   neighbor[4].y = coord.y  ;
      neighbor[5].x = coord.x-2;   neighbor[5].y = coord.y+1;
      neighbor[6].x = coord.x-1;   neighbor[6].y = coord.y+1;
      neighbor[7].x = coord.x  ;   neighbor[7].y = coord.y+1;
      break;
   case 39:
      neighbor[0].x = coord.x+1;   neighbor[0].y = coord.y-1;
      neighbor[1].x = coord.x-2;   neighbor[1].y = coord.y  ;
      neighbor[2].x = coord.x-1;   neighbor[2].y = coord.y  ;
      neighbor[3].x = coord.x+1;   neighbor[3].y = coord.y  ;
      neighbor[4].x = coord.x-2;   neighbor[4].y = coord.y+1;
      neighbor[5].x = coord.x-1;   neighbor[5].y = coord.y+1;
      neighbor[6].x = coord.x  ;   neighbor[6].y = coord.y+1;
      neighbor[7].x = coord.x+1;   neighbor[7].y = coord.y+1;
      break;
   case 69:
      neighbor[0].x = coord.x-1;   neighbor[0].y = coord.y-1;
      neighbor[1].x = coord.x  ;   neighbor[1].y = coord.y-1;
      neighbor[2].x = coord.x+1;   neighbor[2].y = coord.y-1;
      neighbor[3].x = coord.x+2;   neighbor[3].y = coord.y-1;
      neighbor[4].x = coord.x-2;   neighbor[4].y = coord.y  ;
      neighbor[5].x = coord.x-1;   neighbor[5].y = coord.y  ;
      neighbor[6].x = coord.x+1;   neighbor[6].y = coord.y  ;
      neighbor[7].x = coord.x  ;   neighbor[7].y = coord.y+1;
      break;
   case 82:
      neighbor[0].x = coord.x-3;   neighbor[0].y = coord.y-1;
      neighbor[1].x = coord.x-2;   neighbor[1].y = coord.y-1;
      neighbor[2].x = coord.x-1;   neighbor[2].y = coord.y-1;
      neighbor[3].x = coord.x  ;   neighbor[3].y = coord.y-1;
      neighbor[4].x = coord.x-3;   neighbor[4].y = coord.y  ;
      neighbor[5].x = coord.x-2;   neighbor[5].y = coord.y  ;
      neighbor[6].x = coord.x-1;   neighbor[6].y = coord.y  ;
      neighbor[7].x = coord.x+2;   neighbor[7].y = coord.y+1;
      break;
   case 14:
      neighbor[0].x = coord.x-3;   neighbor[0].y = coord.y-2;
      neighbor[1].x = coord.x-1;   neighbor[1].y = coord.y-1;
      neighbor[2].x = coord.x  ;   neighbor[2].y = coord.y-1;
      neighbor[3].x = coord.x+1;   neighbor[3].y = coord.y-1;
      neighbor[4].x = coord.x+2;   neighbor[4].y = coord.y-1;
      neighbor[5].x = coord.x-1;   neighbor[5].y = coord.y  ;
      neighbor[6].x = coord.x+1;   neighbor[6].y = coord.y  ;
      neighbor[7].x = coord.x+2;   neighbor[7].y = coord.y  ;
      break;
   case 17: case 20: case 23: case 45: case 48: case 51:
      neighbor[0].x = coord.x  ;   neighbor[0].y = coord.y-1;
      neighbor[1].x = coord.x+1;   neighbor[1].y = coord.y-1;
      neighbor[2].x = coord.x+2;   neighbor[2].y = coord.y-1;
      neighbor[3].x = coord.x-1;   neighbor[3].y = coord.y  ;
      neighbor[4].x = coord.x+1;   neighbor[4].y = coord.y  ;
      neighbor[5].x = coord.x+2;   neighbor[5].y = coord.y  ;
      neighbor[6].x = coord.x-2;   neighbor[6].y = coord.y+1;
      neighbor[7].x = coord.x-1;   neighbor[7].y = coord.y+1;
      break;
   case 26:
      neighbor[0].x = coord.x  ;   neighbor[0].y = coord.y-1;
      neighbor[1].x = coord.x+1;   neighbor[1].y = coord.y-1;
      neighbor[2].x = coord.x-1;   neighbor[2].y = coord.y  ;
      neighbor[3].x = coord.x+1;   neighbor[3].y = coord.y  ;
      neighbor[4].x = coord.x-2;   neighbor[4].y = coord.y+1;
      neighbor[5].x = coord.x-1;   neighbor[5].y = coord.y+1;
      neighbor[6].x = coord.x  ;   neighbor[6].y = coord.y+1;
      neighbor[7].x = coord.x+1;   neighbor[7].y = coord.y+1;
      break;
   case 42:
      neighbor[0].x = coord.x-1;   neighbor[0].y = coord.y-1;
      neighbor[1].x = coord.x  ;   neighbor[1].y = coord.y-1;
      neighbor[2].x = coord.x+1;   neighbor[2].y = coord.y-1;
      neighbor[3].x = coord.x+2;   neighbor[3].y = coord.y-1;
      neighbor[4].x = coord.x-1;   neighbor[4].y = coord.y  ;
      neighbor[5].x = coord.x+1;   neighbor[5].y = coord.y  ;
      neighbor[6].x = coord.x+2;   neighbor[6].y = coord.y  ;
      neighbor[7].x = coord.x-1;   neighbor[7].y = coord.y+1;
      break;
   case 67:
   case 70: case 73: case 76:
      neighbor[0].x = coord.x  ;   neighbor[0].y = coord.y-1;
      neighbor[1].x = coord.x+1;   neighbor[1].y = coord.y-1;
      neighbor[2].x = coord.x+2;   neighbor[2].y = coord.y-1;
      neighbor[3].x = coord.x-1;   neighbor[3].y = coord.y  ;
      neighbor[4].x = coord.x+1;   neighbor[4].y = coord.y  ;
      neighbor[5].x = coord.x+2;   neighbor[5].y = coord.y  ;
      neighbor[6].x = coord.x+1;   neighbor[6].y = coord.y+1;
      neighbor[7].x = coord.x+2;   neighbor[7].y = coord.y+1;
      break;
   case 79:
      neighbor[0].x = coord.x  ;   neighbor[0].y = coord.y-1;
      neighbor[1].x = coord.x+1;   neighbor[1].y = coord.y-1;
      neighbor[2].x = coord.x-1;   neighbor[2].y = coord.y  ;
      neighbor[3].x = coord.x+1;   neighbor[3].y = coord.y  ;
      neighbor[4].x = coord.x+2;   neighbor[4].y = coord.y  ;
      neighbor[5].x = coord.x+3;   neighbor[5].y = coord.y  ;
      neighbor[6].x = coord.x+1;   neighbor[6].y = coord.y+1;
      neighbor[7].x = coord.x+2;   neighbor[7].y = coord.y+1;
      break;
   case 15:
      neighbor[0].x = coord.x-1;   neighbor[0].y = coord.y-1;
      neighbor[1].x = coord.x  ;   neighbor[1].y = coord.y-1;
      neighbor[2].x = coord.x+1;   neighbor[2].y = coord.y-1;
      neighbor[3].x = coord.x-2;   neighbor[3].y = coord.y  ;
      neighbor[4].x = coord.x-1;   neighbor[4].y = coord.y  ;
      neighbor[5].x = coord.x+1;   neighbor[5].y = coord.y  ;
      neighbor[6].x = coord.x-1;   neighbor[6].y = coord.y+1;
      neighbor[7].x = coord.x  ;   neighbor[7].y = coord.y+1;
      break;
   case 18: case 21: case 24: case 43: case 46: case 49: case 52:
      neighbor[0].x = coord.x-1;   neighbor[0].y = coord.y-1;
      neighbor[1].x = coord.x  ;   neighbor[1].y = coord.y-1;
      neighbor[2].x = coord.x+1;   neighbor[2].y = coord.y-1;
      neighbor[3].x = coord.x-1;   neighbor[3].y = coord.y  ;
      neighbor[4].x = coord.x+1;   neighbor[4].y = coord.y  ;
      neighbor[5].x = coord.x-2;   neighbor[5].y = coord.y+1;
      neighbor[6].x = coord.x-1;   neighbor[6].y = coord.y+1;
      neighbor[7].x = coord.x  ;   neighbor[7].y = coord.y+1;
      break;
   case 40:
      neighbor[0].x = coord.x  ;   neighbor[0].y = coord.y-2;
      neighbor[1].x = coord.x+1;   neighbor[1].y = coord.y-2;
      neighbor[2].x = coord.x  ;   neighbor[2].y = coord.y-1;
      neighbor[3].x = coord.x+1;   neighbor[3].y = coord.y-1;
      neighbor[4].x = coord.x-1;   neighbor[4].y = coord.y  ;
      neighbor[5].x = coord.x+1;   neighbor[5].y = coord.y  ;
      neighbor[6].x = coord.x  ;   neighbor[6].y = coord.y+1;
      neighbor[7].x = coord.x+1;   neighbor[7].y = coord.y+1;
      break;
   case 68:
      neighbor[0].x = coord.x  ;   neighbor[0].y = coord.y-1;
      neighbor[1].x = coord.x+1;   neighbor[1].y = coord.y-1;
      neighbor[2].x = coord.x-1;   neighbor[2].y = coord.y  ;
      neighbor[3].x = coord.x+1;   neighbor[3].y = coord.y  ;
      neighbor[4].x = coord.x  ;   neighbor[4].y = coord.y+1;
      neighbor[5].x = coord.x+1;   neighbor[5].y = coord.y+1;
      neighbor[6].x = coord.x+2;   neighbor[6].y = coord.y+2;
      neighbor[7].x = coord.x+3;   neighbor[7].y = coord.y+2;
      break;
   case 71: case 74: case 77:
      neighbor[0].x = coord.x-1;   neighbor[0].y = coord.y-1;
      neighbor[1].x = coord.x  ;   neighbor[1].y = coord.y-1;
      neighbor[2].x = coord.x+1;   neighbor[2].y = coord.y-1;
      neighbor[3].x = coord.x-1;   neighbor[3].y = coord.y  ;
      neighbor[4].x = coord.x+1;   neighbor[4].y = coord.y  ;
      neighbor[5].x = coord.x+1;   neighbor[5].y = coord.y+1;
      neighbor[6].x = coord.x+2;   neighbor[6].y = coord.y+1;
      neighbor[7].x = coord.x+3;   neighbor[7].y = coord.y+1;
      break;
   case 80:
      neighbor[0].x = coord.x-1;   neighbor[0].y = coord.y-1;
      neighbor[1].x = coord.x  ;   neighbor[1].y = coord.y-1;
      neighbor[2].x = coord.x-1;   neighbor[2].y = coord.y  ;
      neighbor[3].x = coord.x+1;   neighbor[3].y = coord.y  ;
      neighbor[4].x = coord.x+2;   neighbor[4].y = coord.y  ;
      neighbor[5].x = coord.x+1;   neighbor[5].y = coord.y+1;
      neighbor[6].x = coord.x+2;   neighbor[6].y = coord.y+1;
      neighbor[7].x = coord.x+3;   neighbor[7].y = coord.y+1;
      break;
   case 16: case 19: case 22: case 25: case 44: case 47: case 50:
      neighbor[0].x = coord.x-2;   neighbor[0].y = coord.y-1;
      neighbor[1].x = coord.x-1;   neighbor[1].y = coord.y-1;
      neighbor[2].x = coord.x  ;   neighbor[2].y = coord.y-1;
      neighbor[3].x = coord.x+1;   neighbor[3].y = coord.y-1;
      neighbor[4].x = coord.x-2;   neighbor[4].y = coord.y  ;
      neighbor[5].x = coord.x-1;   neighbor[5].y = coord.y  ;
      neighbor[6].x = coord.x+1;   neighbor[6].y = coord.y  ;
      neighbor[7].x = coord.x-1;   neighbor[7].y = coord.y+1;
      break;
   case 41:
      neighbor[0].x = coord.x-1;   neighbor[0].y = coord.y-2;
      neighbor[1].x = coord.x  ;   neighbor[1].y = coord.y-2;
      neighbor[2].x = coord.x-1;   neighbor[2].y = coord.y-1;
      neighbor[3].x = coord.x  ;   neighbor[3].y = coord.y-1;
      neighbor[4].x = coord.x-1;   neighbor[4].y = coord.y  ;
      neighbor[5].x = coord.x+1;   neighbor[5].y = coord.y  ;
      neighbor[6].x = coord.x  ;   neighbor[6].y = coord.y+1;
      neighbor[7].x = coord.x+1;   neighbor[7].y = coord.y+1;
      break;
   case 53:
      neighbor[0].x = coord.x-2;   neighbor[0].y = coord.y-1;
      neighbor[1].x = coord.x-1;   neighbor[1].y = coord.y-1;
      neighbor[2].x = coord.x  ;   neighbor[2].y = coord.y-1;
      neighbor[3].x = coord.x-2;   neighbor[3].y = coord.y  ;
      neighbor[4].x = coord.x-1;   neighbor[4].y = coord.y  ;
      neighbor[5].x = coord.x+1;   neighbor[5].y = coord.y  ;
      neighbor[6].x = coord.x-1;   neighbor[6].y = coord.y+1;
      neighbor[7].x = coord.x  ;   neighbor[7].y = coord.y+1;
      break;
   case 83:
      neighbor[0].x = coord.x-1;   neighbor[0].y = coord.y-2;
      neighbor[1].x = coord.x  ;   neighbor[1].y = coord.y-2;
      neighbor[2].x = coord.x-2;   neighbor[2].y = coord.y-1;
      neighbor[3].x = coord.x-1;   neighbor[3].y = coord.y-1;
      neighbor[4].x = coord.x  ;   neighbor[4].y = coord.y-1;
      neighbor[5].x = coord.x+1;   neighbor[5].y = coord.y-1;
      neighbor[6].x = coord.x+1;   neighbor[6].y = coord.y  ;
      neighbor[7].x = coord.x+2;   neighbor[7].y = coord.y+1;
      break;
   case 72: case 75: case 78:
      neighbor[0].x = coord.x-2;   neighbor[0].y = coord.y-1;
      neighbor[1].x = coord.x-1;   neighbor[1].y = coord.y-1;
      neighbor[2].x = coord.x  ;   neighbor[2].y = coord.y-1;
      neighbor[3].x = coord.x+1;   neighbor[3].y = coord.y-1;
      neighbor[4].x = coord.x-2;   neighbor[4].y = coord.y  ;
      neighbor[5].x = coord.x-1;   neighbor[5].y = coord.y  ;
      neighbor[6].x = coord.x+1;   neighbor[6].y = coord.y  ;
      neighbor[7].x = coord.x+2;   neighbor[7].y = coord.y+1;
      break;
   case 81:
      neighbor[0].x = coord.x-2;   neighbor[0].y = coord.y-1;
      neighbor[1].x = coord.x-1;   neighbor[1].y = coord.y-1;
      neighbor[2].x = coord.x-2;   neighbor[2].y = coord.y  ;
      neighbor[3].x = coord.x-1;   neighbor[3].y = coord.y  ;
      neighbor[4].x = coord.x+1;   neighbor[4].y = coord.y  ;
      neighbor[5].x = coord.x+2;   neighbor[5].y = coord.y+1;
      neighbor[6].x = coord.x+3;   neighbor[6].y = coord.y+1;
      neighbor[7].x = coord.x+3;   neighbor[7].y = coord.y+2;
      break;
   }
   for (int i=0; i<8; i++)
      if ((neighbor[i].x >= sizeField.x) ||
          (neighbor[i].y >= sizeField.y) ||
          (neighbor[i].x < 0) ||
          (neighbor[i].y < 0)) {
         neighbor[i] = CIncorrectCoord;
      }
}

int TcPentagonT5::GetNeighborNumber() const {
   return 8;
}

POINT TcPentagonT5::GetNeighborCoord(int index) const {
   return neighbor[index];
}

bool TcPentagonT5::ToBelong(int x, int y) { // принадлежат ли эти экранные координаты ячейке
   POINT point = {x, y};
   return PointInPolygon(point, regionOut, 5);
}

void TcPentagonT5::SetPoint(const int& area) {
   if (coord.x==0 && coord.y==0) {
      a = sqrt(area/sqrt(147))*2;
      h = a*sqrt(3)/2;
      sq = a*2*sqrt(3)/(sqrt(3)+1);
      TB::SetPoint(NULL);
   }

   // определение координат точек фигуры
   regionOut[0].x = a*21.0f*(coord.x/14);
   switch (direction) {
   case  0:
      regionOut[0].x += a* 1.5f;   break;
   case 14: case 15:
      regionOut[0].x += a* 2.5f;   break;
   case  1: case 28:
      regionOut[0].x += a* 3.0f;   break;
   case 42: case 43:
      regionOut[0].x += a* 4.0f;   break;
   case  2: case 16: case 29: case 56:
      regionOut[0].x += a* 4.5f;   break;
   case 70: case 71:
      regionOut[0].x += a* 5.5f;   break;
   case  3: case 30: case 44: case 57:
      regionOut[0].x += a* 6.0f;   break;
   case 17: case 18:
      regionOut[0].x += a* 7.0f;   break;
   case  4: case 31: case 58: case 72:
      regionOut[0].x += a* 7.5f;   break;
   case 45: case 46:
      regionOut[0].x += a* 8.5f;   break;
   case  5: case 19: case 32: case 59:
      regionOut[0].x += a* 9.0f;   break;
   case 73: case 74:
      regionOut[0].x += a*10.0f;   break;
   case  6: case 33: case 47: case 60:
      regionOut[0].x += a*10.5f;   break;
   case 20: case 21:
      regionOut[0].x += a*11.5f;   break;
   case  7: case 34:
   case 61: case 75:
      regionOut[0].x += a*12.0f;   break;
   case 48: case 49:
      regionOut[0].x += a*13.0f;   break;
   case  8: case 22: case 35: case 62:
      regionOut[0].x += a*13.5f;   break;
   case 76: case 77:
      regionOut[0].x += a*14.5f;   break;
   case  9: case 36: case 50: case 63:
      regionOut[0].x += a*15.0f;   break;
   case 23: case 24:
      regionOut[0].x += a*16.0f;   break;
   case 10: case 37: case 64: case 78:
      regionOut[0].x += a*16.5f;   break;
   case 51: case 52:
      regionOut[0].x += a*17.5f;   break;
   case 11: case 25: case 38: case 65:
      regionOut[0].x += a*18.0f;   break;
   case 79: case 80:
      regionOut[0].x += a*19.0f;   break;
   case 12: case 39: case 53: case 66:
      regionOut[0].x += a*19.5f;   break;
   case 26: case 40:
      regionOut[0].x += a*20.5f;   break;
   case 13: case 54: case 82: case 81:
      regionOut[0].x += a*21.0f;   break;
   case 67: case 68:
      regionOut[0].x += a*22.0f;   break;
   case 27: case 41: case 55:
      regionOut[0].x += a*22.5f;   break;
   case 69: case 83:
      regionOut[0].x += a*24.0f;   break;
   }
   regionOut[0].y = h*14*(coord.y/6);  
   switch (direction) {
   case  1:
      regionOut[0].y += h* 0;   break;
   case  0: case  2: case  4:
      regionOut[0].y += h* 1;   break;
   case  3: case  5: case  7:
      regionOut[0].y += h* 2;   break;
   case 14: case 15: case 16: case  6: case  8: case 10:
      regionOut[0].y += h* 3;   break;
   case 17: case 18: case 19: case  9: case 11: case 13:
      regionOut[0].y += h* 4;   break;
   case 29: case 20: case 21: case 22: case 12: case 27:
      regionOut[0].y += h* 5;   break;
   case 28: case 30: case 32: case 23: case 24: case 25:
      regionOut[0].y += h* 6;   break;
   case 31: case 33: case 35: case 26: case 40: case 41:
      regionOut[0].y += h* 7;   break;
   case 42: case 43: case 44: case 34: case 36: case 38:
      regionOut[0].y += h* 8;   break;
   case 45: case 46: case 47: case 37: case 39: case 55:
      regionOut[0].y += h* 9;   break;
   case 57: case 48: case 49: case 50: case 54: case 69:
      regionOut[0].y += h*10;   break;
   case 56: case 58: case 60: case 51: case 52: case 53:
      regionOut[0].y += h*11;   break;
   case 59: case 61: case 63: case 67: case 68: case 83:
      regionOut[0].y += h*12;   break;
   case 70: case 71: case 72: case 62: case 64: case 66:
      regionOut[0].y += h*13;   break;
   case 73: case 74: case 75: case 65: case 82:
      regionOut[0].y += h*14;   break;
   case 76: case 77: case 78:
      regionOut[0].y += h*15;   break;
   case 79: case 80: case 81:
      regionOut[0].y += h*16;   break;
   }
   switch (direction) {
   case  0: case  3: case  6: case  9: case 12: case 28: case 31: case 34: case 37: case 54: case 56: case 59: case 62: case 65:
      regionOut[1].x = regionOut[0].x+a    ; regionOut[1].y = regionOut[0].y+h*2;
      regionOut[2].x = regionOut[0].x-a    ; regionOut[2].y = regionOut[1].y    ;
      regionOut[3].x = regionOut[0].x-a*1.5; regionOut[3].y = regionOut[0].y+h  ;
      regionOut[4].x = regionOut[2].x      ; regionOut[4].y = regionOut[0].y    ;
      break;
   case  1: case  4: case  7: case 10: case 13: case 29: case 32: case 35: case 38: case 55: case 57: case 60: case 63: case 66:
      regionOut[1].x = regionOut[0].x+a/2  ; regionOut[1].y = regionOut[0].y+h  ;
      regionOut[2].x = regionOut[0].x-a/2  ; regionOut[2].y = regionOut[0].y+h*3;
      regionOut[3].x = regionOut[0].x-a*1.5; regionOut[3].y = regionOut[1].y    ;
      regionOut[4].x = regionOut[0].x-a    ; regionOut[4].y = regionOut[0].y    ;
      break;
   case  2: case  5: case  8: case 11: case 27: case 30: case 33: case 36: case 39: case 69: case 58: case 61: case 64: case 82:
      regionOut[1].x = regionOut[0].x+a/2  ; regionOut[1].y = regionOut[0].y+h  ;
      regionOut[2].x = regionOut[0].x      ; regionOut[2].y = regionOut[0].y+h*2;
      regionOut[3].x = regionOut[0].x-a*2  ; regionOut[3].y = regionOut[2].y    ;
      regionOut[4].x = regionOut[0].x-a    ; regionOut[4].y = regionOut[0].y    ;
      break;
   case 14: case 17: case 20: case 23: case 26: case 42: case 45: case 48: case 51: case 67: case 70: case 73: case 76: case 79:
      regionOut[1].x = regionOut[0].x-a    ; regionOut[1].y = regionOut[0].y+h*2;
      regionOut[2].x = regionOut[0].x-a*2  ; regionOut[2].y = regionOut[1].y    ;
      regionOut[3].x = regionOut[0].x-a*2.5; regionOut[3].y = regionOut[0].y+h  ;
      regionOut[4].x = regionOut[2].x      ; regionOut[4].y = regionOut[0].y    ;
      break;
   case 15: case 18: case 21: case 24: case 40: case 43: case 46: case 49: case 52: case 68: case 71: case 74: case 77: case 80:
      regionOut[1].x = regionOut[0].x+a    ; regionOut[1].y = regionOut[0].y+h*2;
      regionOut[2].x = regionOut[0].x+a/2  ; regionOut[2].y = regionOut[0].y+h*3;
      regionOut[3].x = regionOut[0].x-a/2  ; regionOut[3].y = regionOut[2].y    ;
      regionOut[4].x = regionOut[0].x-a    ; regionOut[4].y = regionOut[1].y    ;
      break;
   case 16: case 19: case 22: case 25: case 41: case 44: case 47: case 50: case 53: case 83: case 72: case 75: case 78: case 81:
      regionOut[1].x = regionOut[0].x+a/2  ; regionOut[1].y = regionOut[0].y+h  ;
      regionOut[2].x = regionOut[0].x      ; regionOut[2].y = regionOut[0].y+h*2;
      regionOut[3].x = regionOut[0].x-a    ; regionOut[3].y = regionOut[2].y    ;
      regionOut[4].x = regionOut[0].x-a*2  ; regionOut[4].y = regionOut[0].y    ;
      break;
   }

   // определение координат вписанного в фигуру квадрата - область в которую выводится изображение/текст
   POINTFLOAT center;
   switch (direction) {
   case  0: case  3: case  6: case  9: case 12: case 28: case 31: case 34: case 37: case 54: case 56: case 59: case 62: case 65:
   case  1: case  4: case  7: case 10: case 13: case 29: case 32: case 35: case 38: case 55: case 57: case 60: case 63: case 66:
   case 14: case 17: case 20: case 23: case 26: case 42: case 45: case 48: case 51: case 67: case 70: case 73: case 76: case 79:
      center.x = regionOut[3].x+a;
      center.y = regionOut[3].y;
      break;
   case  2: case  5: case  8: case 11: case 27: case 30: case 33: case 36: case 39: case 69: case 58: case 61: case 64: case 82:
   case 16: case 19: case 22: case 25: case 41: case 44: case 47: case 50: case 53: case 83: case 72: case 75: case 78: case 81:
      center.x = regionOut[1].x-a;
      center.y = regionOut[1].y;
      break;
   case 15: case 18: case 21: case 24: case 40: case 43: case 46: case 49: case 52: case 68: case 71: case 74: case 77: case 80:
      center.x = regionOut[4].x+a;
      center.y = regionOut[4].y;
      break;
   }
   regionIn.left   = center.x - sq/2;
   regionIn.right  = center.x + sq/2;
   regionIn.top    = center.y - sq/2;
   regionIn.bottom = center.y + sq/2;
   regionIn.left   += 1;
   regionIn.top    += 1;
   regionIn.right  += 1;
   regionIn.bottom += 1;
}

void TcPentagonT5::Paint() const {
   TB::Paint();

   SelectObject(hDC, down ? hPenWhite : hPenBlack);
   MoveToEx(hDC, regionOut[0].x, regionOut[0].y, NULL);
   LineTo  (hDC, regionOut[1].x, regionOut[1].y);
   LineTo  (hDC, regionOut[2].x, regionOut[2].y);
   switch (direction) {
   case  2: case  5: case  8: case 11: case 27: case 30: case 33: case 36: case 39: case 69: case 58: case 61: case 64: case 82:
   case 15: case 18: case 21: case 24: case 40: case 43: case 46: case 49: case 52: case 68: case 71: case 74: case 77: case 80:
   case 16: case 19: case 22: case 25: case 41: case 44: case 47: case 50: case 53: case 83: case 72: case 75: case 78: case 81:
      LineTo  (hDC, regionOut[3].x  , regionOut[3].y);
   }
   SelectObject(hDC, down ? hPenBlack : hPenWhite);
   switch (direction) {
   case  0: case  3: case  6: case  9: case 12: case 28: case 31: case 34: case 37: case 54: case 56: case 59: case 62: case 65:
   case  1: case  4: case  7: case 10: case 13: case 29: case 32: case 35: case 38: case 55: case 57: case 60: case 63: case 66:
   case 14: case 17: case 20: case 23: case 26: case 42: case 45: case 48: case 51: case 67: case 70: case 73: case 76: case 79:
      MoveToEx(hDC, regionOut[2].x+1, regionOut[2].y  , NULL);
      LineTo  (hDC, regionOut[3].x+1, regionOut[3].y  );
      LineTo  (hDC, regionOut[4].x+1, regionOut[4].y  );
      MoveToEx(hDC, regionOut[4].x  , regionOut[4].y+1, NULL);
      LineTo  (hDC, regionOut[0].x  , regionOut[0].y+1);
      break;
   case 15: case 18: case 21: case 24: case 40: case 43: case 46: case 49: case 52: case 68: case 71: case 74: case 77: case 80:
      MoveToEx(hDC, regionOut[3].x+1, regionOut[3].y  , NULL);
      LineTo  (hDC, regionOut[4].x+1, regionOut[4].y  );
      LineTo  (hDC, regionOut[0].x+1, regionOut[0].y  );
      break;
   case  2: case  5: case  8: case 11: case 27: case 30: case 33: case 36: case 39: case 69: case 58: case 61: case 64: case 82:
   case 16: case 19: case 22: case 25: case 41: case 44: case 47: case 50: case 53: case 83: case 72: case 75: case 78: case 81:
      MoveToEx(hDC, regionOut[3].x+1, regionOut[3].y  , NULL);
      LineTo  (hDC, regionOut[4].x+1, regionOut[4].y  );
      MoveToEx(hDC, regionOut[4].x  , regionOut[4].y+1, NULL);
      LineTo  (hDC, regionOut[0].x  , regionOut[0].y+1);
      break;
   }
}

} // namespace nsFigure
