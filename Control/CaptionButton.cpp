////////////////////////////////////////////////////////////////////////////////
// File name: CaptionButton.cpp
// Author: Sergey Krivulya (Ceргей Кpивуля) - KSerg
// e-mail: Sergey_Krivulya@UkrPost.Net
// Date: 19 09 2004
////////////////////////////////////////////////////////////////////////////////

#include "StdAfx.h"
#include "CaptionButton.h"
#include "../CommonLib.h"
//#include "../Logger.h"

#ifdef __AFX_H__
   #ifdef _DEBUG
   #undef THIS_FILE
   static char THIS_FILE[]=__FILE__;
   #define new DEBUG_NEW
   #endif
#endif

CCaptionButton::MAP_Bttn CCaptionButton::m_Map;

POINT GetOffsetBttn(HWND hWnd) {
   // Возвращает координату точки (относительно всего окна), которая является правым-верхним углом новой кнопки.
   // Фактически узнаю смещение справа-сверху кнопки.
   POINT pointRes = {0, 0};

   DWORD lStyle   = GetWindowStyle  (hWnd);
   DWORD lStyleEx = GetWindowStyleEx(hWnd);

   BOOL bToolWin     = !!(lStyleEx & WS_EX_TOOLWINDOW);
   BOOL bResizingWin = !!(lStyle   & WS_THICKFRAME);
   BOOL bBtnClose    = !!(lStyle   & WS_SYSMENU);
   BOOL bBtnMinMax   =  ((lStyle   & WS_MAXIMIZEBOX) || (lStyle & WS_MINIMIZEBOX)) && bBtnClose && !bToolWin;
   BOOL bBtnHelp     =   (lStyleEx & WS_EX_CONTEXTHELP)             && !bBtnMinMax && bBtnClose && !bToolWin;

   int iDefBttnWidth = CCaptionButton::GetDefSizeBttn(hWnd).cx;
   int iOffsetLeft = 0;
   iOffsetLeft += bResizingWin ? ::GetSystemMetrics(SM_CXSIZEFRAME) : ::GetSystemMetrics(SM_CXFIXEDFRAME); // размер каймы
   iOffsetLeft += 2; // отступ от края заголовка к кнопке
   iOffsetLeft += bBtnClose  ?  iDefBttnWidth    +2 : 0; // размер  кнопки 'Закрыть'                с отступом к след. кнопке
   iOffsetLeft += bBtnMinMax ? (iDefBttnWidth<<1)+2 : 0; // размеры кнопок 'Развернуть', 'Свернуть' с отступом к след. кнопке
   iOffsetLeft += bBtnHelp   ?  iDefBttnWidth    +2 : 0; // размеры кнопки '?'                      с отступом к новой кнопке
   int iOffsetTop = 0;
   iOffsetTop += bResizingWin ? ::GetSystemMetrics(SM_CYSIZEFRAME) : ::GetSystemMetrics(SM_CYFIXEDFRAME); // размер каймы
   iOffsetTop += 2; // отступ от края заголовка к кнопке

   pointRes.x = iOffsetLeft;
   pointRes.y = iOffsetTop;
   return pointRes;
}

RECT CCaptionButton::GetBttnRect(HWND hWnd) {
   SIZE sizeBttn = {0,0};
   {
      HBITMAP hBmp = CCaptionButton::m_Map[hWnd]->m_pCreateBmpFunc(hWnd, FALSE, FALSE, CCaptionButton::m_Map[hWnd]->m_pCreateBmpParam);
      sizeBttn = ::SizeBitmap(hBmp);
      if (CCaptionButton::m_Map[hWnd]->m_bAutoDelBmp)
         ::DeleteObject(hBmp);
   }
   RECT rectWnd = ::GetWindowRect(hWnd);
   POINT pointOffset = GetOffsetBttn(hWnd);
   return RECTEX(
      rectWnd.right-pointOffset.x-sizeBttn.cx,
      rectWnd.top  +pointOffset.y,
      rectWnd.right-pointOffset.x,
      rectWnd.top  +pointOffset.y+sizeBttn.cy
   );
}

BOOL CCaptionButton::CursorInBttn(HWND hWnd) {
   return ::PointInRect(::GetCursorPos(), GetBttnRect(hWnd));
}

SIZE CCaptionButton::GetDefSizeBttn(HWND hWnd) {
   // возвращает размеры кнопок по умолчанию ('Закрыть', 'Развернуть', 'Свернуть', '?').
   SIZE sizeRes = {0, 0};

   DWORD lStyle   = GetWindowStyle  (hWnd);
   DWORD lStyleEx = GetWindowStyleEx(hWnd);

   BOOL bMdiWin  = FALSE; // @TODO: MDI окно? Ещё не знаю как узанть...
   BOOL bToolWin = (lStyleEx & WS_EX_TOOLWINDOW);

   sizeRes.cx = bToolWin ? 13 : ::GetSystemMetrics(bMdiWin ? SM_CYMENUSIZE : SM_CXSIZE)-2;
   sizeRes.cy = bToolWin ? 11 : ::GetSystemMetrics(bMdiWin ? SM_CYMENUSIZE : SM_CYSIZE)-4;

   return sizeRes;
}

LRESULT CALLBACK CCaptionButton::WndProc(HWND hWnd, UINT msg, WPARAM wParam, LPARAM lParam) {
   CCaptionButton::CContext *pCntx = CCaptionButton::m_Map[hWnd];

   if (!pCntx->m_bCaption && (msg != WM_STYLECHANGED))
      return ::CallWindowProc(pCntx->m_pWndProc, hWnd, msg, wParam, lParam);

   BOOL bCall = !pCntx->m_bCapture;//TRUE;// вызывать ли оригинальный обработчик сообщений?
   LRESULT lRes = 0L;

   //static CLogger Log(CLogger::LL_DEBUG, NULL, true, false, false);

   switch (msg) {
   default:
      if (bCall) {
         lRes = ::CallWindowProc(pCntx->m_pWndProc, hWnd, msg, wParam, lParam);
      }
      break;
   case WM_NCLBUTTONDBLCLK:
      {
         BOOL bFocus = CursorInBttn(hWnd);
         CCaptionButton::DrawButton(hWnd, FALSE, bFocus);
         if (!bFocus) {
            lRes = ::CallWindowProc(pCntx->m_pWndProc, hWnd, msg, wParam, lParam);
            bCall = TRUE;
         }
         else {
            ::SendMessage(hWnd, WM_NCLBUTTONDOWN, wParam, lParam);
         }
      }
      break;
   case WM_LBUTTONUP:
      if (bCall) {
         lRes = ::CallWindowProc(pCntx->m_pWndProc, hWnd, msg, wParam, lParam);
      }
      if (pCntx->m_bCapture) {
         pCntx->m_bCapture = FALSE;
         ::ReleaseCapture();
         BOOL bFocus = CursorInBttn(hWnd);
         CCaptionButton::DrawButton(hWnd, FALSE, bFocus);
         if (bFocus) {
            ONCLICK pOnClick = pCntx->m_pClickFunc;
            if (pOnClick) {
               pOnClick(pCntx->m_pClickParam);
               ::DrawMenuBar(hWnd);
            }
         }
      }
      break;
   case WM_NCPAINT:
      {
         lRes = ::CallWindowProc(pCntx->m_pWndProc, hWnd, msg, wParam, lParam);
         bCall = TRUE;

         HRGN hRgn = (HRGN)wParam; // handle of update region
         RECTEX rectRegion;
         int iRegion = ::GetRgnBox(hRgn, &rectRegion);
         if (
             ((iRegion == SIMPLEREGION) ||
              (iRegion == COMPLEXREGION)) &&
              (::IntersectRect(RECTEX(GetBttnRect(hWnd)), rectRegion))
            )
         {
            BOOL bDown = FALSE;
            BOOL bFocus = CursorInBttn(hWnd);
            if (pCntx->m_bCapture) {
               bDown = bFocus;
            }
            CCaptionButton::DrawButton(hWnd, bDown, bFocus);
         }
      }
      break;
   case WM_SETTEXT:
      bCall = TRUE;
   case WM_NCACTIVATE:
   case WM_MOUSEMOVE:
   case WM_NCMOUSEMOVE:
 //case WM_AFXFIRST+10:
      {
         if (bCall) {
            lRes = ::CallWindowProc(pCntx->m_pWndProc, hWnd, msg, wParam, lParam);
         }
         BOOL bDown = FALSE;
         BOOL bFocus = CursorInBttn(hWnd);
         if (pCntx->m_bCapture) {
            bDown = bFocus;
         }
         CCaptionButton::DrawButton(hWnd, bDown, bFocus);
      }
      break;
   case WM_NCLBUTTONDOWN:
      {
         BOOL bFocus = CursorInBttn(hWnd);
         BOOL bDown = bFocus;
         CCaptionButton::DrawButton(hWnd, bDown, bFocus);
         if (bDown) {
            ::SetCapture(hWnd);
            pCntx->m_bCapture = TRUE;
         } else {
            if (bCall) {
               lRes = ::CallWindowProc(pCntx->m_pWndProc, hWnd, msg, wParam, lParam);
            }
         }
      }
      break;
   case WM_CAPTURECHANGED:
   case WM_CANCELMODE:
      lRes = ::CallWindowProc(pCntx->m_pWndProc, hWnd, msg, wParam, lParam);
      bCall = TRUE;
      if (pCntx->m_bCapture) {
         pCntx->m_bCapture = FALSE;
      }
      break;
   //case WM_STYLECHANGING:
   case WM_STYLECHANGED:
      {
         pCntx->m_bCaption = !!(WS_DLGFRAME & ((LPSTYLESTRUCT)lParam)->styleNew);
         lRes = ::CallWindowProc(pCntx->m_pWndProc, hWnd, msg, wParam, lParam);
         bCall = TRUE;
      }
      break;
   }

   if (bCall)
   {
      //Log.PutMsg(CLogger::LL_DEBUG, "", msg);
   }
   return lRes;
}

void CCaptionButton::DrawButton(HWND hWnd, BOOL bDown, BOOL bFocus) {
   HDC hDC  = ::GetWindowDC(hWnd);
   HDC hCDC = ::CreateCompatibleDC(hDC);
   HBITMAP hBmp = CCaptionButton::m_Map[hWnd]->m_pCreateBmpFunc(hWnd, bDown, bFocus, CCaptionButton::m_Map[hWnd]->m_pCreateBmpParam);
   HBITMAP hBmpOld = (HBITMAP)::SelectObject(hCDC, hBmp);

   SIZE sizeBmp = ::SizeBitmap(hBmp);
   RECTEX rectWnd = ::GetWindowRect(hWnd);
   POINT pointOffset = GetOffsetBttn(hWnd);
   ::BitBlt(
      hDC,
      rectWnd.width()-pointOffset.x-sizeBmp.cx,
      pointOffset.y,
      sizeBmp.cx,
      sizeBmp.cy,
      hCDC,
      0, 0,
      SRCCOPY
   );

   ::SelectObject(hCDC, hBmpOld);
   ::DeleteDC(hCDC);
   if (CCaptionButton::m_Map[hWnd]->m_bAutoDelBmp)
      ::DeleteObject(hBmp);
   ::ReleaseDC(hWnd, hDC);
}

HBITMAP CCaptionButton::CreateDefBmpButton(HWND hWnd, BOOL bDown, BOOL bFocus, LPVOID) {
   DWORD lStyle   = GetWindowStyle  (hWnd);
   DWORD lStyleEx = GetWindowStyleEx(hWnd);

   SIZE sizeBmp = CCaptionButton::GetDefSizeBttn(hWnd);

   HDC hDC = ::GetWindowDC(::GetDesktopWindow());
   HDC hCDC = ::CreateCompatibleDC(hDC);
   HBITMAP hBmpNew = ::CreateCompatibleBitmap(hDC, sizeBmp.cx, sizeBmp.cy);
   HBITMAP hBmpOld = (HBITMAP)::SelectObject(hCDC, hBmpNew);

   ::DrawFrameControl(hCDC, &RECTEX(sizeBmp), DFC_BUTTON, DFCS_BUTTONPUSH | (bDown ? DFCS_PUSHED : 0));

   ::SelectObject(hCDC, hBmpOld);
   ::DeleteDC(hCDC);
   ::ReleaseDC(::GetDesktopWindow(), hDC);
   return hBmpNew;
}

BOOL CCaptionButton::Create(
   HWND hWnd,
   CREATEBMPBUTTON pCreateBmpFunc,
   LPVOID pCreateBmpParam,
   BOOL bAutoDelBmp,
   ONCLICK pClickFunc,
   LPVOID pClickParam
) {
   BOOL bRes = (m_Map.find(hWnd) == m_Map.end());
   if (bRes) {
      m_Map[hWnd] = new CCaptionButton::CContext(
         ::SetWindowProc(hWnd, CCaptionButton::WndProc),
         pCreateBmpFunc,
         pCreateBmpParam,
         bAutoDelBmp,
         pClickFunc,
         pClickParam,
         !!(WS_DLGFRAME & GetWindowStyle(hWnd))
      );
      ::DrawMenuBar(hWnd);
   }
   return bRes;
}

BOOL CCaptionButton::Delete(HWND hWnd) {
   MAP_Bttn::iterator I = m_Map.find(hWnd);
   BOOL bRes = (I != m_Map.end());
   if (bRes) {
      ::SetWindowProc(hWnd, m_Map[hWnd]->m_pWndProc);
      delete I->second;
      m_Map.erase(I);
      ::DrawMenuBar(hWnd);
   }
   return bRes;
}

/////////////////////////////////////////////////////////////////////

CCaptionButtonText::CCaptionButtonText(
   HWND hWnd,
   LPCTSTR szText,
   CCaptionButton::ONCLICK pClickFunc,
   LPVOID pClickParam
) :
   m_hWnd  (hWnd),
   m_szText(NULL)
{
   if (szText) {
      int iLen = lstrlen(szText);
      m_szText = new TCHAR[iLen+1];
      lstrcpy(m_szText, szText);
   }
   CCaptionButton::Create(m_hWnd, CCaptionButtonText::CreateBmpButtonText, this, true, pClickFunc, pClickParam);
}

CCaptionButtonText::~CCaptionButtonText() {
   if (m_szText) {
      delete [] m_szText;
      m_szText = NULL;
   }
   CCaptionButton::Delete(m_hWnd);
}

HBITMAP CCaptionButtonText::CreateBmpButtonText(HWND hWnd, BOOL bDown, BOOL bFocus, LPVOID pParam) {
   // Высота будет как у кнопок по умолчанию ('Закрыть', 'Развернуть', 'Свернуть', '?').
   // Ширина определяется текстом.
   SIZE sizeBmp = {0, 0};
   { // Определяю высоту кнопок по умолчанию
      HBITMAP hBmpDef = CCaptionButton::CreateDefBmpButton(hWnd, bDown, bFocus, pParam);
      sizeBmp = ::SizeBitmap(hBmpDef);
      ::DeleteObject(hBmpDef);
   }
   LOGFONT Font = {0,0,0,0, FW_BOLD, 0,0,0, DEFAULT_CHARSET, OUT_DEFAULT_PRECIS,
                   CLIP_DEFAULT_PRECIS, DEFAULT_QUALITY, DEFAULT_PITCH | FF_DONTCARE,
                   TEXT("MS Sans Serif")};//TEXT("Times New Roman")};//
   Font.lfHeight = sizeBmp.cy;
   HDC hDC = ::GetWindowDC(hWnd);
   HDC hCDC = ::CreateCompatibleDC(hDC);
   CCaptionButtonText *This = (CCaptionButtonText*)pParam;
   HFONT hFontOld = (HFONT)::SelectObject(hCDC, ::CreateFontIndirect(&Font));

   // Узнаю ширину текста
   SIZE sizeText = {0, 0};
   ::GetTextExtentPoint32(hCDC, This->m_szText, lstrlen(This->m_szText), &sizeText);
   sizeBmp.cx = max(sizeBmp.cx, sizeText.cx+2+2);

   if (!bFocus) {
      Font.lfWeight = FW_NORMAL;
      ::DeleteObject(::SelectObject(hCDC, ::CreateFontIndirect(&Font)));
   }

   // Создаю битмап, рисую на нём кнопку, и вывожу текст.
   HBITMAP hBmp = ::CreateCompatibleBitmap(hDC, sizeBmp.cx, sizeBmp.cy);
   HBITMAP hBmpOld = (HBITMAP)::SelectObject(hCDC, hBmp);

   RECTEX rect(sizeBmp);
   ::DrawFrameControl(hCDC, &rect, DFC_BUTTON, DFCS_BUTTONPUSH | (bDown ? DFCS_PUSHED : 0));
   int iBkModeOld = ::SetBkMode(hCDC, TRANSPARENT);
   if (bDown) {
      rect.move(1, 1);
   }
   ::DrawText(hCDC, This->m_szText, -1, &rect, DT_CENTER); // ExtTextOut(hCDC, 0,0, ETO_CLIPPED, &rect, This->m_szText, strlen(This->m_szText), NULL);

   ::SetBkMode(hCDC, iBkModeOld);
   ::SelectObject(hCDC, hBmpOld);
   ::DeleteObject(::SelectObject(hCDC, hFontOld));
   ::DeleteDC(hCDC);
   ::ReleaseDC(hWnd, hDC);
   return hBmp;
}
