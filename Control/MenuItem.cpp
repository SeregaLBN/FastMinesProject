////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "MenuItem.cpp"
////////////////////////////////////////////////////////////////////////////////

#include "StdAfx.h"
#include <TChar.h>
#ifndef __AFX_H__
   #include "../CStringKS.h"
#endif
#include "MenuItem.h"
#include "../CommonLib.h"

UINT CMenuItem::m_uMaxWidthText = 0;

CMenuItem::CMenuItem(const TCHAR  *szText,
                           UINT    width_Img,
                           UINT    width_Text,
                           UINT    height_Item,
                     const CImage *pImg,
                           UINT    format):
   m_szText     (NULL),
   m_pImg       (pImg),
   m_uFormat    (format),
   m_uWidthImg  (width_Img),
   m_uWidthText (width_Text),
   m_uHeightItem(height_Item)
{
   SetText(szText);
}

void CMenuItem::SetText(const TCHAR *szText) {
   if (m_szText) {
      delete [] m_szText;
      m_szText = NULL;
   }
   if (szText && *szText) {
      m_szText = new TCHAR[lstrlen(szText)+1];
      lstrcpy(m_szText, szText);

      m_uMaxWidthText = max(m_uMaxWidthText, m_uWidthText);
   }
}

CMenuItem::~CMenuItem()
{
   if (m_szText)
      delete [] m_szText;
}

void CMenuItem::Draw (const DRAWITEMSTRUCT &dis) const
{
   if (dis.CtlType != ODT_MENU) // error
      return;

   const int oxTxt = CMenuItem::offsetText(); // offset X text - отступ текста

   // –исунок в меню будет отображатьс€ внутри кнопки.
   //  нопка будет нажата если данный пункт меню имеет статус ODS_CHECKED.

   // rcBtn - пр€моугольник кнопки
   RECTEX rcBtn(dis.rcItem.left,
                dis.rcItem.top,
                dis.rcItem.left+m_uWidthImg,
                dis.rcItem.bottom);

   // rcImg - пр€моугольник рисунка внутри кнопки
   RECTEX rcImg(+1+rcBtn.left,
                +1+rcBtn.top,
                -1+rcBtn.right,
                -1+rcBtn.bottom);

   // rcTxt - пр€моугольник в котором будет отображатьс€ текст меню
   RECTEX rcTxt(rcBtn.right+oxTxt,
                dis.rcItem.top,
                min(rcBtn.right+(int)m_uMaxWidthText,dis.rcItem.right),
                dis.rcItem.bottom);

   // rcFill - пр€моугольние заливки
   RECTEX rcFill(m_pImg ? rcBtn.right : rcBtn.left,
                 dis.rcItem.top,
                 dis.rcItem.right,
                 dis.rcItem.bottom);

   bool isCheck  = !!(dis.itemState & ODS_CHECKED); // отмечен ли пункт меню?
   bool isBullet = false; // отмечен ли пункт меню точкой "Bullet'ом" ?
   if (isCheck) {
      // если пункт меню отмечен, то узнаю чем - галочкой "Check mark'ом" или точкой "Bullet'ом" ?
      MENUITEMINFO mii = {sizeof(MENUITEMINFO), MIIM_TYPE};
      BOOL bRes = ::GetMenuItemInfo((HMENU)dis.hwndItem, dis.itemID, FALSE, &mii);
      isBullet = !!(mii.fType & MFT_RADIOCHECK);
   }
   bool isSelect = !!(dis.itemState & ODS_SELECTED);
   bool isGrayed = !!(dis.itemState & ODS_GRAYED);
   //bool isArrow = ???; // Submenu arrow ?

   // fill menu item
   if (isSelect)
      SelectObject(dis.hDC, GetSysColorBrush(COLOR_HIGHLIGHT));
   else
      SelectObject(dis.hDC, GetSysColorBrush(COLOR_MENU));
   PatBlt(dis.hDC, rcFill.left, rcFill.top, rcFill.width(), rcFill.height(), PATCOPY);
   if (m_pImg) {
      SelectObject(dis.hDC, GetSysColorBrush(COLOR_MENU));
      PatBlt(dis.hDC, rcBtn.left, rcBtn.top, rcBtn.width(), rcBtn.height(), PATCOPY);
   }

   // draw image & check
   if (m_pImg) {
      // button
      if (isSelect || isCheck) {
         SelectObject(dis.hDC, GetStockObject(isCheck ? BLACK_PEN : WHITE_PEN));
         MoveToEx(dis.hDC, rcBtn.left   , rcBtn.bottom-1, NULL);
         LineTo  (dis.hDC, rcBtn.left   , rcBtn.top     );
         LineTo  (dis.hDC, rcBtn.right-1, rcBtn.top     );
         SelectObject(dis.hDC, GetStockObject(isCheck ? WHITE_PEN : BLACK_PEN));
         LineTo  (dis.hDC, rcBtn.right-1, rcBtn.bottom-1);
         LineTo  (dis.hDC, rcBtn.left   , rcBtn.bottom-1);
      }
      // image
      if (isSelect) {
         if (isCheck)
            rcImg.move(+1,+1);
         else
            rcImg.move(-1,-1);
      }
      m_pImg->Draw(dis.hDC, &rcImg);
   } else {
      if (isCheck) {
         /**
         RECTEX rc(dis.rcItem);
         BOOL bRes = DrawFrameControl( // попытка нарисовать средствами винды
            dis.hDC,
            &rc.width(rc.height()),
            DFC_MENU,
            isBullet ? DFCS_MENUBULLET : DFCS_MENUCHECK
         );
         /**/
         SelectObject(dis.hDC, GetStockObject(isSelect ? WHITE_BRUSH : BLACK_BRUSH));
         SelectObject(dis.hDC, GetStockObject(NULL_PEN));
         Ellipse(dis.hDC, rcImg.center().x-4, rcImg.center().y-4, rcImg.center().x+4, rcImg.center().y+4);
         /**/
      }
   }

   // draw text
   if (m_szText) {
      TCHAR *pTab = _tcsrchr(m_szText, TEXT('\t'));
      if (pTab) {
         *pTab = 0;
      }

      SetBkMode(dis.hDC, TRANSPARENT);
      if (isGrayed) {
         if (isSelect){
            SetTextColor(dis.hDC, GetSysColor(COLOR_GRAYTEXT));
            DrawText(dis.hDC, m_szText, lstrlen(m_szText), &rcTxt, m_uFormat);
            if (pTab)
            DrawText(dis.hDC, pTab+1, lstrlen(pTab+1), &rcTxt, m_uFormat | DT_RIGHT);
         } else {
            SetTextColor(dis.hDC, 0xFFFFFF);
            DrawText(dis.hDC, m_szText, lstrlen(m_szText), &rcTxt.move(+1,+1), m_uFormat);
            if (pTab)
            DrawText(dis.hDC, pTab+1, lstrlen(pTab+1), &rcTxt.move(+1,+1), m_uFormat | DT_RIGHT);
            SetTextColor(dis.hDC, GetSysColor(COLOR_GRAYTEXT));
            DrawText(dis.hDC, m_szText, lstrlen(m_szText), &rcTxt.move(-1,-1), m_uFormat);
            if (pTab)
            DrawText(dis.hDC, pTab+1, lstrlen(pTab+1), &rcTxt.move(-1,-1), m_uFormat | DT_RIGHT);
         }
      } else {
         SetTextColor(dis.hDC, GetSysColor(isSelect ? COLOR_HIGHLIGHTTEXT : COLOR_MENUTEXT));
         DrawText(dis.hDC, m_szText, lstrlen(m_szText), &rcTxt, m_uFormat);
         if (pTab)
         DrawText(dis.hDC, pTab+1, lstrlen(pTab+1), &rcTxt, m_uFormat | DT_RIGHT);
      }

      if (pTab) {
         *pTab = TEXT('\t');
      }
   }
   return;

   static const DWORD dwROP=0x00A000C9L;
   static const WORD  bmpMASK[] = {0x55, 0xaa, 0x55, 0xaa, 0x55, 0xaa, 0x55, 0xaa};

   static HBITMAP hBMP   = CreateBitmap(8, 8, 1, 1, bmpMASK);
   static HBRUSH  hBR    = CreatePatternBrush(hBMP);
   HBRUSH  oldhBR = (HBRUSH)SelectObject(dis.hDC, hBR);

   COLORREF oldclrText = SetTextColor(dis.hDC, RGB(190, 190, 190));
   COLORREF oldclrBkgr = SetBkColor(dis.hDC, RGB(190, 190, 190));

   if (dis.rcItem.top==0)
      PatBlt(dis.hDC, dis.rcItem.left, dis.rcItem.top, RECTEX(dis.rcItem).width(), RECTEX(dis.rcItem).height(), dwROP);
}

//static
CMenuItem* CMenuItem::GetInstance(HMENU hMenu, UINT uItem, BOOL bByPosition) {
   CMenuItem *pMenuItem = NULL;
   MENUITEMINFO mii = {sizeof(MENUITEMINFO), MIIM_TYPE | MIIM_DATA};
   BOOL bRes = ::GetMenuItemInfo(hMenu, uItem, bByPosition, &mii);
   if (bRes) {
      switch (mii.fType) {
      case MFT_OWNERDRAW | MFT_RADIOCHECK:
      case MFT_OWNERDRAW:
         pMenuItem = (CMenuItem *)mii.dwItemData;
         break;
      }
   }
   return pMenuItem;
}

//static
// »змен€ю состо€ние обычного пункта меню на MFT_OWNERDRAW,
// т.е. оно теперь будет отрисовыватьс€ классом CMenuItem.
// Ќе измен€€ текст этого пункта меню (изменить можно потом
// с помощью CMenuItem::SetMenuText()).
// @TODO: класс CImage должен существовать
// всЄ врем€, пока есь меню!!!
void CMenuItem::SetMenuOwnerDraw(HMENU hMenu, UINT item_ID, const CImage *pImg) {
   CString strMenu(GetMenuString(hMenu, item_ID, FALSE));

   MENUITEMINFO mii = {sizeof(MENUITEMINFO), MIIM_TYPE | MIIM_DATA, MFT_OWNERDRAW,
      0,0,0,0,0,
      (ULONG) new CMenuItem(strMenu, 0,0,0, pImg), // @TODO: забиваю на освобождение пам€ти при выходе из программы
      0,0
   };
   BOOL res = SetMenuItemInfo(
      hMenu,
      item_ID,
      FALSE,
     &mii);
}

//static
BOOL CMenuItem::SetMenuText(HMENU hMenu, UINT uItem, BOOL bByPosition, LPCTSTR szNewText) {
   MENUITEMINFO mii = {sizeof(MENUITEMINFO), MIIM_TYPE | MIIM_DATA};
   BOOL bRes = ::GetMenuItemInfo(hMenu, uItem, bByPosition, &mii);
   if (bRes) {
      switch (mii.fType) {
      case MFT_STRING | MFT_RADIOCHECK:
      case MFT_STRING:
         bRes = ::SetMenuText(hMenu, uItem, bByPosition, szNewText);
         break;
      case MFT_OWNERDRAW | MFT_RADIOCHECK:
      case MFT_OWNERDRAW:
         {
            CMenuItem *pMenuItem = (CMenuItem *)mii.dwItemData;
            pMenuItem->SetText(szNewText);
            bRes = TRUE;
            /**
            //bRes = ::DeleteMenu(hMenu, uItem, bByPosition);
            //if (!bRes) return FALSE;
            delete pMenuItem;
            pMenuItem = new CMenuItem(szNewText, 0,0,0, CFastMines2Project::GetImageMosaic((nsMosaic::EMosaic)(uItem-ID_MENU_MOSAIC_TRIANGLE1)));
            mii.dwItemData = (ULONG)pMenuItem;
            bRes = ::SetMenuItemInfo(hMenu, uItem, bByPosition, &mii);
            /**/
         }
         break;
      }
   }
   return bRes;
}

// WM_MEASUREITEM
//static
void CMenuItem::OnMeasureItem(MEASUREITEMSTRUCT *lpMeasureItem) {
   CMenuItem *pMenuItem = (CMenuItem*)lpMeasureItem->itemData;
   if (pMenuItem)
   {

      NONCLIENTMETRICS ncm = {sizeof(ncm)};
      BOOL bResult = SystemParametersInfo(SPI_GETNONCLIENTMETRICS, ncm.cbSize, (PVOID)&ncm, 0);
      if (!bResult) return;

      HFONT hFontMenu = CreateFontIndirect(&ncm.lfMenuFont);
      if (!hFontMenu) return;

      HDC hCDC = ::CreateCompatibleDC(NULL);
      if (!hCDC) return;

      HFONT hFontOld = (HFONT)SelectObject(hCDC, hFontMenu);

      SIZE size;
      bResult = GetTextExtentPoint32(hCDC, pMenuItem->GetText(), lstrlen(pMenuItem->GetText()), &size);
      if (!bResult) return;

      POINTEX sizeItem(
         lpMeasureItem->itemWidth  = size.cx+15,
         lpMeasureItem->itemHeight = ncm.iMenuHeight//GetSystemMetrics(SM_CYMENU)//2+16+2//GetSystemMetrics(SM_CYMENUCHECK)+5//**/abs(fontData.lfHeight)*/
      );

      pMenuItem->SetSize(
         ncm.iMenuWidth, // Specifies the width, in pixels, of menu-bar buttons
         sizeItem.x, sizeItem.y
      );

      SelectObject(hCDC, hFontOld);
      DeleteObject(hFontMenu);
      DeleteDC(hCDC);
   }
}

// WM_DRAWITEM
// static
void CMenuItem::OnDrawItem(const DRAWITEMSTRUCT *lpDrawItem) {
   CMenuItem *pMenuItem = (CMenuItem*)lpDrawItem->itemData;
   if (pMenuItem)
      pMenuItem->Draw(*lpDrawItem);
}

