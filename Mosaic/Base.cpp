////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "Base.cpp"
//
// –еализаци€ базового класса CBase
////////////////////////////////////////////////////////////////////////////////

#include "StdAfx.h"
#include "Base.h"
#include "../CommonLib.h"

////////////////////////////////////////////////////////////////////////////////
//                               local init
////////////////////////////////////////////////////////////////////////////////

float nsCell::CBase::m_a; // размер одной из сторон фигуры
float nsCell::CBase::m_sq;

////////////////////////////////////////////////////////////////////////////////
//                               static function
////////////////////////////////////////////////////////////////////////////////

SIZE nsCell::CBase::GetSizeInPixel(const SIZE &sizeField, int iArea) {
   SIZE result = {200, 100};
   return result;
}

int nsCell::CBase::SizeInscribedSquare(int iArea, int iBorderWidth) {
   return 7; // говор€т, 7 счастливое число... :)
}

////////////////////////////////////////////////////////////////////////////////
//                               virtual function
////////////////////////////////////////////////////////////////////////////////

void nsCell::CBase::Cell_SetDown(bool bDown) {
   m_bDown = bDown;
}

void nsCell::CBase::Cell_SetStatus(EStatus Status, CClickReportContext *pClickRepContext) {
   if (pClickRepContext) {
      if (Status == _Open) {
         if (m_Cell.m_Open == _Nil)
            pClickRepContext->m_SetOpenNil.insert(this);
         else
            pClickRepContext->m_SetOpen.insert(this);
      }
   }
   m_Cell.m_Status = Status;
}

nsCell::EStatus nsCell::CBase::Cell_GetStatus() const {
   return m_Cell.m_Status;
}

void nsCell::CBase::Cell_DefineValue() {
   if (m_Cell.m_Open == _Mine) return;
   // подсчитать у соседей число мин и установить значение
   int count = 0;
   for (int i=0; i<m_iNeighborNumber; i++) {
      if(!m_ppLinkNeighbor[i]) continue; // существует ли сосед?
      if (m_ppLinkNeighbor[i]->Cell_GetOpen() == _Mine) count++;
   }
   m_Cell.m_Open = EOpen(count);
}

bool nsCell::CBase::Cell_SetMine() {
   if (m_bLockMine || (m_Cell.m_Open == _Mine)) return false;
   m_Cell.m_Open = _Mine;
   return true;
}

nsCell::EOpen nsCell::CBase::Cell_GetOpen() const {
   return m_Cell.m_Open;
}

void nsCell::CBase::Cell_SetClose(EClose Close, CClickReportContext *pClickRepContext) {
   if (pClickRepContext) {
      if ((         Close == _Flag) || // если устанавливаю флажок
          (m_Cell.m_Close == _Flag))   // если снимаю флажок
      {
         pClickRepContext->m_SetFlag.insert(this);
      }
   }
   m_Cell.m_Close = Close;
}

nsCell::EClose nsCell::CBase::Cell_GetClose() const {
   return m_Cell.m_Close;
}

nsCell::CBase::~CBase() {
   delete [] m_ppLinkNeighbor;
   delete [] m_pNeighbor;
   delete [] m_pRegion;
}

void nsCell::CBase::VerifyNeighbor(const SIZE &sizeField) {
   for (int i=0; i<m_iNeighborNumber; i++)
      if ((m_pNeighbor[i].X >= sizeField.cx) ||
          (m_pNeighbor[i].Y >= sizeField.cy) ||
          (m_pNeighbor[i].X < 0) ||
          (m_pNeighbor[i].Y < 0)) {
         m_pNeighbor[i] = INCORRECT_COORD;
      }
}

nsCell::CBase::CBase(
   const COORD &Coord,
   const SIZE &sizeField,
   int iArea,
   const CGraphicContext &gContext,
   int iNeighborNumber,
   int iVertexNumber,
   int iDirection
):
   m_Coord           (Coord),
   m_GContext        (gContext),
   m_iNeighborNumber (iNeighborNumber),
   m_iVertexNumber   (iVertexNumber),
   m_iDirection      (iDirection),
   m_pNeighbor       (new COORD[iNeighborNumber]),
   m_pRegion         (new POINT[iVertexNumber])
{
   Reset();
   // потомки должны определить массив соседей
}

inline void nsCell::CBase::Lock() {
   m_bLockMine = true;
}

void nsCell::CBase::LockNeighbor() {
   m_bLockMine = true;
   // запретить установку мин у соседей,
   for (int i=0; i<m_iNeighborNumber; i++) {
      if (!m_ppLinkNeighbor[i]) continue; // существует ли сосед?
      m_ppLinkNeighbor[i]->Lock();
   }
}

void nsCell::CBase::SetNeighborLink(CBase *const *const ppLinkNeighbor, int numberNeighbor) {
   m_ppLinkNeighbor = new CBase*[numberNeighbor];
   for (int i=0; i<numberNeighbor; i++)
      m_ppLinkNeighbor[i] = ppLinkNeighbor[i];
}

COORD nsCell::CBase::GetNeighborCoord(int index) const {
   return (index<m_iNeighborNumber) ? m_pNeighbor[index] : INCORRECT_COORD;
}

COORD nsCell::CBase::GetCoord() const {
   return m_Coord;
}

int nsCell::CBase::GetNeighborNumber() const { // определить количество соседей
   return m_iNeighborNumber;
}

POINT nsCell::CBase::GetCenterPixel() const {
   POINT result = {m_Square.left + (m_Square.right -m_Square.left)/2,
                   m_Square.top  + (m_Square.bottom-m_Square.top )/2};
   return result;
}

inline bool nsCell::CBase::PointInRegion(const POINT &point) const { // принадлежат ли эти экранные координаты €чейке
   return PointInPolygon(point, m_pRegion, m_iVertexNumber);
}

void nsCell::CBase::SetPoint(int iArea) {
}

void nsCell::CBase::Reset() {
   m_Cell.m_Status = _Close;
   m_Cell.m_Open   = _Nil;
   m_Cell.m_Close  = _Clear;
   m_bLockMine = false;
   m_bDown = false;
}

void nsCell::CBase::Paint() const {
   /**/
   BitBlt(m_GContext.m_hDCTmp,
      m_Square.left,
      m_Square.top,
      m_Square.right -m_Square.left,
      m_Square.bottom-m_Square.top,
      m_GContext.m_hDCBck,
      m_Square.left,
      m_Square.top,
      SRCCOPY);
   /**
   BitBlt(m_hDCTmp,
      rectangle.left,
      rectangle.top,
      rectangle.width (),
      rectangle.height()
      m_hDCBck,
      rectangle.left,
      rectangle.top,
      SRCCOPY);
   /**/

   // output Pictures
   if ((m_Cell.m_Status == _Close) && /**/(m_Cell.m_Close == _Flag))
      m_GContext.m_ImgFlag.Draw(m_GContext.m_hDCTmp, &m_Square);
   else
   if ((m_Cell.m_Status == _Open ) && /**/(m_Cell.m_Open  == _Mine))
      m_GContext.m_ImgMine.Draw(m_GContext.m_hDCTmp, &m_Square);
   else
   // output text
   {
      TCHAR const *szCaption;
      if (m_Cell.m_Status == _Close) {
         SetTextColor(m_GContext.m_hDCTmp, m_GContext.m_ColorText.m_colorClose[m_Cell.m_Close]);
         szCaption = SZ_CAPTION_CLOSE[m_Cell.m_Close];
      } else {
         SetTextColor(m_GContext.m_hDCTmp, m_GContext.m_ColorText.m_colorOpen[m_Cell.m_Open]);
         szCaption = SZ_CAPTION_OPEN[m_Cell.m_Open];
      }
      RECT sq_tmp = {m_Square.left   + !!m_bDown,
                     m_Square.top    + !!m_bDown,
                     m_Square.right  + !!m_bDown,
                     m_Square.bottom + !!m_bDown};
      DrawText(m_GContext.m_hDCTmp, szCaption,
               -1, &sq_tmp, DT_CENTER | DT_VCENTER | DT_SINGLELINE);
   }
}

void nsCell::CBase::LButtonDown() {
   if (m_Cell.m_Close  == _Flag) return;
   if (m_Cell.m_Status == _Close) {
      m_bDown = true;
      Paint();
      return;
   }
   // эффект нажатости дл€ неоткрытых соседей
   if ((m_Cell.m_Status == _Open) && (m_Cell.m_Open != _Nil))
      for (int i=0; i<m_iNeighborNumber; i++) {
         if (!m_ppLinkNeighbor[i]) continue; // существует ли сосед?
         if ((m_ppLinkNeighbor[i]->Cell_GetStatus() == _Open) ||
             (m_ppLinkNeighbor[i]->Cell_GetClose()  == _Flag)) continue;
         m_ppLinkNeighbor[i]->Cell_SetDown(true);
         m_ppLinkNeighbor[i]->Paint();
      }
}

CLeftUpReturn nsCell::CBase::LButtonUp(bool isMy, CClickReportContext *pClickRepContext) {
   CLeftUpReturn result = {0, 0, 0, false, false};

   if (m_Cell.m_Close == _Flag) return result;
   // избавитс€ от эффекта нажатости
   if ((m_Cell.m_Status == _Open) && (m_Cell.m_Open != _Nil))
      for (int i=0; i<m_iNeighborNumber; i++) {
         if (!m_ppLinkNeighbor[i]) continue; // существует ли сосед?
         if ((m_ppLinkNeighbor[i]->Cell_GetStatus() == _Open) ||
             (m_ppLinkNeighbor[i]->Cell_GetClose()  == _Flag)) continue;
         m_ppLinkNeighbor[i]->Cell_SetDown(false);
         m_ppLinkNeighbor[i]->Paint();
      }
   // ќткрыть закрытую €чейку на которой нажали
   if (m_Cell.m_Status == _Close) {
      if (!isMy){
         m_bDown = false;
         Paint();
         return result;
      } else {
         result.m_iCountUnknown += (m_Cell.m_Close == _Unknown) ? -1 : 0;
         result.m_iCountOpen++;
         Cell_SetStatus(_Open, pClickRepContext);
         m_bDown = true;
         Paint();
      }
   }
   // ! ¬ этой точке €чейка уже открыта
   // ѕодсчитываю кол-во установленных вокруг флагов и не открытых €чеек
   int countFlags = 0;
   int countClear = 0;
   if (m_Cell.m_Open != _Nil)
      for (int i=0; i<m_iNeighborNumber; i++) {
         if(!m_ppLinkNeighbor[i]) continue; // существует ли сосед?
         if (m_ppLinkNeighbor[i]->Cell_GetStatus() == _Open) continue;
         if (m_ppLinkNeighbor[i]->Cell_GetClose()  == _Flag)
            countFlags++;
         else countClear++;
      }
   // оставшимс€ установить флаги
   if ((m_Cell.m_Open != _Nil) && (countFlags+countClear == m_Cell.m_Open))
      for (int i=0; i<m_iNeighborNumber; i++) {
         if(!m_ppLinkNeighbor[i]) continue; // существует ли сосед?
         if ((m_ppLinkNeighbor[i]->Cell_GetStatus() == _Open) ||
             (m_ppLinkNeighbor[i]->Cell_GetClose()  == _Flag)) continue;
         result.m_iCountUnknown += (m_ppLinkNeighbor[i]->Cell_GetClose() == _Unknown) ? -1 : 0;
         result.m_iCountFlag++;
         m_ppLinkNeighbor[i]->Cell_SetClose(_Flag, pClickRepContext);
         m_ppLinkNeighbor[i]->Paint();
      }
   if (!isMy) return result;
   // открыть оставшиес€
   if ((countFlags+result.m_iCountFlag) == m_Cell.m_Open)
      for (int i=0; i<m_iNeighborNumber; i++) {
         if (!m_ppLinkNeighbor[i]) continue; // существует ли сосед?
         if ((m_ppLinkNeighbor[i]->Cell_GetStatus() == _Open) ||
             (m_ppLinkNeighbor[i]->Cell_GetClose()  == _Flag)) continue;
         result.m_iCountUnknown += (m_ppLinkNeighbor[i]->Cell_GetClose() == _Unknown) ? -1 : 0;
         result.m_iCountOpen++;
         m_ppLinkNeighbor[i]->Cell_SetDown(true);
         m_ppLinkNeighbor[i]->Cell_SetStatus(_Open, pClickRepContext);
         m_ppLinkNeighbor[i]->Paint();
         if (m_ppLinkNeighbor[i]->Cell_GetOpen() == _Nil) {
            const CLeftUpReturn result2 = m_ppLinkNeighbor[i]->LButtonUp(true, pClickRepContext);
            result.m_iCountFlag    += result2.m_iCountFlag;
            result.m_iCountOpen    += result2.m_iCountOpen;
            result.m_iCountUnknown += result2.m_iCountUnknown;
            if (result.m_bEndGame) {
               result.m_bEndGame = result2.m_bEndGame;
               result.m_bVictory = result2.m_bVictory;
            }
         }
         if (m_ppLinkNeighbor[i]->Cell_GetOpen() == _Mine) {
            result.m_bEndGame = true;
            result.m_bVictory = false;
            return result;
         }
      }
   if (m_Cell.m_Open == _Mine) {
      result.m_bEndGame = true;
      result.m_bVictory = false;
   }
   return result;
}

CRightDownReturn nsCell::CBase::RButtonDown(EClose Close, CClickReportContext *pClickRepContext) {
   CRightDownReturn result = {0,0};

   if ((m_Cell.m_Status == _Open) || m_bDown) return result;
   switch (m_Cell.m_Close) {
   case _Clear:
      switch (Close) {
         case _Flag:    result.m_iCountFlag    = +1;  break;
         case _Unknown: result.m_iCountUnknown = +1;
      }
      if (m_Cell.m_Close != Close) Cell_SetClose(Close, pClickRepContext);
      break;
   case _Flag:
      switch (Close) {
         case _Unknown: result.m_iCountUnknown = +1;
         case _Clear:   result.m_iCountFlag    = -1;
      }
      if (m_Cell.m_Close != Close) Cell_SetClose(Close, pClickRepContext);
      break;
   case _Unknown:
      switch (Close) {
         case _Flag:    result.m_iCountFlag    = +1;
         case _Clear:   result.m_iCountUnknown = -1;
      }
      if (m_Cell.m_Close != Close) Cell_SetClose(Close, pClickRepContext);
   }
   Paint();
   return result;
}
