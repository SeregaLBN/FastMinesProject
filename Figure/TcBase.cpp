////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "TcBase.cpp"
//
// –еализаци€ базового класса TcBase
////////////////////////////////////////////////////////////////////////////////

#include "TcBase.h"
#include "..\Dialog\Info.h"
#include "..\Preproc.h"

namespace nsFigure {

////////////////////////////////////////////////////////////////////////////////
//                               local init
////////////////////////////////////////////////////////////////////////////////

float     TcBase::a; // размер одной из сторон фигуры
float     TcBase::sq;
TcImage * TcBase::pImageFlag;
TcImage * TcBase::pImageMine;
COLORREF* TcBase::pColorClose;
COLORREF* TcBase::pColorOpen;
HANDLE    TcBase::hDC;
HANDLE    TcBase::hCDC;
HANDLE    TcBase::hPenBlack;
HANDLE    TcBase::hPenWhite;
//int     TcBase::w;
SET_cpTB  TcBase::setOpenNil;
SET_cpTB  TcBase::setOpen;
SET_cpTB  TcBase::setFlag;

////////////////////////////////////////////////////////////////////////////////
//                               static function
////////////////////////////////////////////////////////////////////////////////

POINT TcBase::GetSizeFieldInPixel(const POINT&, const int&) {
   POINT result = {200, 100};
   return result;
}

int TcBase::SizeInscribedSquare(int) {
   return 7; // говор€т, 7 счастливое число... :)
}

float TcBase::GetPercentMine(TeSkillLevel) { // процент мин на заданном уровне сложности
   return 1.f;
}

bool TcBase::PointInPolygon(const POINT& pnt, const POINTFLOAT* const plgn, int n) {
   POINTFLOAT a = {pnt.x+(float)0.01, pnt.y+(float)0.01};
   int count = 0;
   for (int i=0; i<n; i++) {
      int j = (i+1)%n;
      if (plgn[i].y == plgn[j].y) continue;
      if (plgn[i].y > a.y && plgn[j].y > a.y) continue;
      if (plgn[i].y < a.y && plgn[j].y < a.y) continue;
      if (max(plgn[i].y, plgn[j].y) == a.y) count++;
      else
         if (min(plgn[i].y, plgn[j].y) == a.y) continue;
         else {
            float t = (float)(a.y-plgn[i].y)/(plgn[j].y-plgn[i].y);
            if (t>0 && t<1 && plgn[i].x+t*(plgn[j].x-plgn[i].x) >= a.x) count++;
         }
   }
   return count & 1;
}

////////////////////////////////////////////////////////////////////////////////
//                               virtual function
////////////////////////////////////////////////////////////////////////////////

void TcBase::Cell_SetDown(bool setDown) {
   down = setDown;
}

void TcBase::Cell_SetStatus(TeStatus setStatus) {
   if (setStatus == _Open){
      if (Cell.Open == _Nil)
         setOpenNil.insert(this);
      else
         setOpen.insert(this);
   }
   Cell.Status = setStatus;
}

TeStatus TcBase::Cell_GetStatus() const {
   return Cell.Status;
}

void TcBase::Cell_DefineValue() {
   if (Cell.Open == _Mine) return;
   // подсчитать у соседей число мин и установить значение
   int count = 0;
   for (int i=0; i<GetNeighborNumber(); i++) {
      if(!ppLinkNeighbor[i]) continue; // существует ли сосед?
      if (ppLinkNeighbor[i]->Cell_GetOpen() == _Mine) count++;
   }
   Cell.Open = TeOpen(count);
}

bool TcBase::Cell_SetMine() {
   if (lockMine || (Cell.Open == _Mine)) return false;
   Cell.Open = _Mine;
   return true;
}

TeOpen TcBase::Cell_GetOpen() const {
   return Cell.Open;
}

void TcBase::Cell_SetClose(TeClose setClose) {
   if ((setClose   == _Flag) || // если устанавливаю флажок
       (Cell.Close == _Flag)) { // если снимаю флажок
      setFlag.insert(this);
   }
   Cell.Close = setClose;
}

TeClose TcBase::Cell_GetClose() const {
   return Cell.Close;
}

TcBase::~TcBase() {
   //delete [] ppLinkNeighbor;
}

TcBase::TcBase(const POINT& setCoord, const POINT&, const int&) {
   coord = setCoord;
   Reset();
   // потомки должны определить массив соседей
}

void TcBase::Lock() {
   lockMine = true;
}

void TcBase::LockNeighbor() {
   lockMine = true;
   // запретить установку мин у соседей,
   for (int i=0; i<GetNeighborNumber(); i++) {
      if (!ppLinkNeighbor[i]) continue; // существует ли сосед?
      ppLinkNeighbor[i]->Lock();
   }
}

void TcBase::SetNeighborLink(TcBase*const*const pArray, int numberNeighbor) {
   ppLinkNeighbor = new TcBase*[numberNeighbor];
   for (int i=0; i<numberNeighbor; i++)
      ppLinkNeighbor[i] = pArray[i];
}

int TcBase::GetNeighborNumber() const {
   return 0;
}

POINT TcBase::GetNeighborCoord(int) const {
   POINT result = CIncorrectCoord;
   return result;
}

POINT TcBase::GetCoord() const {
   return coord;
}

POINT TcBase::GetCenterPixel() const {
   POINT result = {regionIn.left + (regionIn.right -regionIn.left)/2,
                   regionIn.top  + (regionIn.bottom-regionIn.top )/2};
   return result;
}

bool TcBase::ToBelong(int, int) { // принадлежат ли эти экранные координаты €чейке
   return false;
}

void TcBase::SetPoint(const int&) {
/*   pImageFlag->ZoomImage(sq, sq);
   pImageMine->ZoomImage(sq, sq);
   */
}

void TcBase::Reset() {
   Cell.Status = _Close;
   Cell.Open   = _Nil;
   Cell.Close  = _Clear;
   lockMine = false;
   down = false;
}

void TcBase::Paint() const {
   BitBlt(hDC,
      regionIn.left,
      regionIn.top,
      regionIn.right  - regionIn.left,
      regionIn.bottom - regionIn.top,
      hCDC,
      regionIn.left,
      regionIn.top,
      SRCCOPY);

   // output Pictures
   if ((Cell.Status == _Close) && (Cell.Close == _Flag))
      pImageFlag->DrawImage(hDC, &regionIn);
   else
   if ((Cell.Status == _Open ) && (Cell.Open  == _Mine))
      pImageMine->DrawImage(hDC, &regionIn);
   else
   // output text
   {
      TCHAR szCaption[3] = {TEXT('\0'), TEXT('\0'), TEXT('\0')};
      if (Cell.Status == _Close) {
         SetTextColor(hDC, pColorClose[Cell.Close]);
         szCaption[0] = CCaptionClose[Cell.Close];
      } else {
         SetTextColor(hDC, pColorOpen[Cell.Open]);
         szCaption[0] = CCaptionOpen[Cell.Open][0];
         szCaption[1] = CCaptionOpen[Cell.Open][1];
      }
      RECT rectan = {regionIn.left   + !!down,
                     regionIn.top    + !!down,
                     regionIn.right  + !!down,
                     regionIn.bottom + !!down};
      DrawText(hDC, szCaption,
               -1, &rectan, DT_CENTER | DT_VCENTER | DT_SINGLELINE);
   }
}

void TcBase::LButtonDown() {
   if (Cell.Close  == _Flag) return;
   if (Cell.Status == _Close) {
      down = true;
      Paint();
      return;
   }
   // эффект нажатости дл€ неоткрытых соседей
   if ((Cell.Status == _Open) && (Cell.Open != _Nil))
      for (int i=0; i<GetNeighborNumber(); i++) {
         if (!ppLinkNeighbor[i]) continue; // существует ли сосед?
         if ((ppLinkNeighbor[i]->Cell_GetStatus() == _Open) ||
             (ppLinkNeighbor[i]->Cell_GetClose()  == _Flag)) continue;
         ppLinkNeighbor[i]->Cell_SetDown(true);
         ppLinkNeighbor[i]->Paint();
      }
}

TsLUpReturn TcBase::LButtonUp(bool isMy) {
   TsLUpReturn result = {0, 0, 0, false, false};

   if (Cell.Close == _Flag) return result;
   // избавитс€ от эффекта нажатости
   if ((Cell.Status == _Open) && (Cell.Open != _Nil))
      for (int i=0; i<GetNeighborNumber(); i++) {
         if (!ppLinkNeighbor[i]) continue; // существует ли сосед?
         if ((ppLinkNeighbor[i]->Cell_GetStatus() == _Open) ||
             (ppLinkNeighbor[i]->Cell_GetClose()  == _Flag)) continue;
         ppLinkNeighbor[i]->Cell_SetDown(false);
         ppLinkNeighbor[i]->Paint();
      }
   // ќткрыть закрытую €чейку на которой нажали
   if (Cell.Status == _Close) {
      if (!isMy){
         down = false;
         Paint();
         return result;
      } else {
         result.countUnknown += (Cell.Close == _Unknown) ? -1 : 0;
         result.countOpen++;
         Cell_SetStatus(_Open);
         down = true;
         Paint();
      }
   }
   // ! ¬ этой точке €чейка уже открыта
   // ѕодсчитываю кол-во установленных вокруг флагов и не открытых €чеек
   int countFlags = 0;
   int countClear = 0;
   if (Cell.Open != _Nil)
      for (int i=0; i<GetNeighborNumber(); i++) {
         if(!ppLinkNeighbor[i]) continue; // существует ли сосед?
         if (ppLinkNeighbor[i]->Cell_GetStatus() == _Open) continue;
         if (ppLinkNeighbor[i]->Cell_GetClose()  == _Flag)
            countFlags++;
         else countClear++;
      }
   // оставшимс€ установить флаги
   if ((Cell.Open != _Nil) && (countFlags+countClear == Cell.Open))
      for (int i=0; i<GetNeighborNumber(); i++) {
         if(!ppLinkNeighbor[i]) continue; // существует ли сосед?
         if ((ppLinkNeighbor[i]->Cell_GetStatus() == _Open) ||
             (ppLinkNeighbor[i]->Cell_GetClose()  == _Flag)) continue;
         result.countUnknown += (ppLinkNeighbor[i]->Cell_GetClose() == _Unknown) ? -1 : 0;
         result.countFlag++;
         ppLinkNeighbor[i]->Cell_SetClose(_Flag);
         ppLinkNeighbor[i]->Paint();
      }
   if (!isMy) return result;
   // открыть оставшиес€
   if (countFlags == Cell.Open)
      for (int i=0; i<GetNeighborNumber(); i++) {
         if (!ppLinkNeighbor[i]) continue; // существует ли сосед?
         if ((ppLinkNeighbor[i]->Cell_GetStatus() == _Open) ||
             (ppLinkNeighbor[i]->Cell_GetClose()  == _Flag)) continue;
         result.countUnknown += (ppLinkNeighbor[i]->Cell_GetClose() == _Unknown) ? -1 : 0;
         result.countOpen++;
         ppLinkNeighbor[i]->Cell_SetDown(true);
         ppLinkNeighbor[i]->Cell_SetStatus(_Open);
         ppLinkNeighbor[i]->Paint();
         if (ppLinkNeighbor[i]->Cell_GetOpen() == _Nil) {
            const TsLUpReturn result2 = ppLinkNeighbor[i]->LButtonUp(true);
            result.countFlag    += result2.countFlag;
            result.countOpen    += result2.countOpen;
            result.countUnknown += result2.countUnknown;
            if (result.endGame) {
               result.endGame = result2.endGame;
               result.victory = result2.victory;
            }
         }
         if (ppLinkNeighbor[i]->Cell_GetOpen() == _Mine) {
            result.endGame = true;
            result.victory = false;
            return result;
         }
      }
   if (Cell.Open == _Mine) {
      result.endGame = true;
      result.victory = false;
   }
   return result;
}

TsRDownReturn TcBase::RButtonDown(TeClose setClose) {
   TsRDownReturn result = {0,0};

   if ((Cell.Status == _Open) || down) return result;
   switch (Cell.Close) {
   case _Clear:
      switch (setClose) {
         case _Flag:    result.countFlag    = +1;  break;
         case _Unknown: result.countUnknown = +1;
      }
      if (Cell.Close != setClose) Cell_SetClose(setClose);
      break;
   case _Flag:
      switch (setClose) {
         case _Unknown: result.countUnknown = +1;
         case _Clear:   result.countFlag    = -1;
      }
      if (Cell.Close != setClose) Cell_SetClose(setClose);
      break;
   case _Unknown:
      switch (setClose) {
         case _Flag:    result.countFlag    = +1;
         case _Clear:   result.countUnknown = -1;
      }
      if (Cell.Close != setClose) Cell_SetClose(setClose);
   }
   Paint();
   return result;
}

} // namespace nsFigure
