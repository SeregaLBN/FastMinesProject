////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "EraseBk.h"
//
////////////////////////////////////////////////////////////////////////////////

#ifndef FILE_ERASEBK
#define FILE_ERASEBK

#include ".\Preproc.h"
#include <windows.h>

namespace nsEraseBk {
   HBRUSH Cls_OnCtlColor(HWND hwnd, HDC hdc, HWND hwndChild, int type);
#ifdef REPLACEBKCOLORFROMFILLWINDOW
   const RECT errorRect = {0,0,0,0};

   void FillWnd    (HWND hwnd, COLORREF bkColor, bool client = true, RECT fillRect = errorRect);
   void FillMenu   (HWND hwnd, COLORREF bkColor);
   void FillCaption(HWND hwnd, COLORREF bkColor);

   BOOL Cls_OnEraseBkgnd(HWND hwnd, HDC hdc, COLORREF colorBk);
#endif // REPLACEBKCOLORFROMFILLWINDOW
}

#define HANDLE_WM_CTLCOLOR(hwnd)
#define HANDLE_WM_CTLCOLOR_(hwnd) \
   HANDLE_MSG(hwnd, WM_CTLCOLORMSGBOX   , nsEraseBk::Cls_OnCtlColor);     \
 /*HANDLE_MSG(hwnd, WM_CTLCOLOREDIT     , nsEraseBk::Cls_OnCtlColor);/**/ \
 /*HANDLE_MSG(hwnd, WM_CTLCOLORLISTBOX  , nsEraseBk::Cls_OnCtlColor);/**/ \
   HANDLE_MSG(hwnd, WM_CTLCOLORBTN      , nsEraseBk::Cls_OnCtlColor);     \
   HANDLE_MSG(hwnd, WM_CTLCOLORDLG      , nsEraseBk::Cls_OnCtlColor);     \
   HANDLE_MSG(hwnd, WM_CTLCOLORSCROLLBAR, nsEraseBk::Cls_OnCtlColor);     \
   HANDLE_MSG(hwnd, WM_CTLCOLORSTATIC   , nsEraseBk::Cls_OnCtlColor)

#ifdef REPLACEBKCOLORFROMFILLWINDOW
////////////////////////////////////////////////////////////////////////////////
//                   Overdetermination Windows Procedure
////////////////////////////////////////////////////////////////////////////////

#define WNDPROC_OVER(name, Skin)   \
WNDPROC defWndProc_##name;  \
LRESULT CALLBACK newWndProc_##name(HWND hwnd, UINT msg, WPARAM wParam, LPARAM lParam) {  \
   /*switch (msg) { \
   case WM_ERASEBKGND:  \
      if (Skin.toAll) { \
         return nsEraseBk::Cls_OnEraseBkgnd(hwnd, (HDC)wParam, Skin.colorBk);  \
      }  \
   }*/  \
   return CallWindowProc(defWndProc_##name, hwnd, msg, wParam, lParam);   \
}

#define WNDPROC_STATIC(name, Skin)   \
WNDPROC defWndProc_##name;  \
LRESULT CALLBACK newWndProc_##name(HWND hwnd, UINT msg, WPARAM wParam, LPARAM lParam) {  \
   switch (msg) { \
   case WM_PAINT: \
      CallWindowProc(defWndProc_##name, hwnd, msg, wParam, lParam); \
      if (Skin.toAll)  \
         nsEraseBk::FillWnd(hwnd, Skin.colorBk); \
      return 0;   \
   }  \
   return CallWindowProc(defWndProc_##name, hwnd, msg, wParam, lParam);   \
}

#define WNDPROC_BUTTON(name, hDlg, Skin)   \
WNDPROC defWndProc_##name; \
LRESULT CALLBACK newWndProc_##name(HWND hwnd, UINT msg, WPARAM wParam, LPARAM lParam) {   \
   switch (msg) { \
   case WM_MOUSEMOVE:   \
      CallWindowProc(defWndProc_##name, hwnd, msg, wParam, lParam);   \
      {  if ((Skin.toAll) && (wParam & MK_LBUTTON)) {  \
            POINT cursorPos = {LOWORD(lParam), HIWORD(lParam)};  \
            GetCursorPos(&cursorPos);  \
            ScreenToClient(hDlg, &cursorPos);   \
            RECT winRect;  \
            GetWindowRect(hwnd, &winRect);   \
            POINT buttonLT = {winRect.left , winRect.top};  \
            ScreenToClient(hDlg, &buttonLT); \
            POINT buttonRB = {winRect.right, winRect.bottom};  \
            ScreenToClient(hDlg, &buttonRB); \
            bool inRegion = true;   \
            if ((cursorPos.x < buttonLT.x) ||   \
                (cursorPos.x >=buttonRB.x) ||   \
                (cursorPos.y < buttonLT.y) ||   \
                (cursorPos.y >=buttonRB.y)) inRegion = false;  \
            static bool oldInRegion = inRegion; \
            if (oldInRegion != inRegion)  \
               nsEraseBk::FillWnd(hwnd, Skin.colorBk); \
            oldInRegion = inRegion; \
         }  \
      }  \
      return 0;   \
   case WM_KEYDOWN:  \
   case WM_ENABLE:   \
   case WM_PAINT: \
   case WM_LBUTTONDBLCLK: \
   case WM_LBUTTONDOWN: \
   case WM_LBUTTONUP:   \
   case BM_SETCHECK: \
      CallWindowProc(defWndProc_##name, hwnd, msg, wParam, lParam);   \
      if (Skin.toAll)  \
         nsEraseBk::FillWnd(hwnd, Skin.colorBk); \
      return 0;   \
   }  \
   return CallWindowProc(defWndProc_##name, hwnd, msg, wParam, lParam);  \
}

#define SETNEWWNDPROC(hwnd, name) defWndProc_##name = (WNDPROC)SetWindowLong(GetDlgItem(hwnd, name), GWL_WNDPROC, (LONG)newWndProc_##name)

#endif // REPLACEBKCOLORFROMFILLWINDOW

#endif // FILE_ERASEBK
