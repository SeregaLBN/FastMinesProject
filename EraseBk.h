////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "EraseBk.h"
//
////////////////////////////////////////////////////////////////////////////////

#ifndef __FILE__ERASEBK__
#define __FILE__ERASEBK__

#ifndef __AFX_H__
   #include <Windows.h>
#endif
#include "CommonLib.h"
#include "FastMines2.h"

namespace nsEraseBk {
   HBRUSH OnCtlColor(HWND hwnd, HDC hdc, HWND hwndChild, int type, const CSkin &Skin);
#ifdef REPLACEBKCOLORFROMFILLWINDOW
   const RECT errorRect = {0,0,0,0};

   void FillWnd    (HWND hwnd, COLORREF bkColor, bool client = true, const RECT& fillRect = errorRect);
   void FillMenu   (HWND hwnd, COLORREF bkColor);
   void FillCaption(HWND hwnd, COLORREF bkColor);

   BOOL OnEraseBkgnd(HWND hwnd, HDC hdc, COLORREF colorBk);
#endif // REPLACEBKCOLORFROMFILLWINDOW
   BOOL ReplaceFunctions();
}

#define HANDLE_WM_EX_CTLCOLOR(hwnd, skin) \
   HANDLE_MSG_EX(hwnd, WM_CTLCOLORMSGBOX   , nsEraseBk::OnCtlColor, skin );     \
 /*HANDLE_MSG_EX(hwnd, WM_CTLCOLOREDIT     , nsEraseBk::OnCtlColor, skin );/**/ \
 /*HANDLE_MSG_EX(hwnd, WM_CTLCOLORLISTBOX  , nsEraseBk::OnCtlColor, skin );/**/ \
   HANDLE_MSG_EX(hwnd, WM_CTLCOLORBTN      , nsEraseBk::OnCtlColor, skin );     \
   HANDLE_MSG_EX(hwnd, WM_CTLCOLORDLG      , nsEraseBk::OnCtlColor, skin );     \
   HANDLE_MSG_EX(hwnd, WM_CTLCOLORSCROLLBAR, nsEraseBk::OnCtlColor, skin );     \
   HANDLE_MSG_EX(hwnd, WM_CTLCOLORSTATIC   , nsEraseBk::OnCtlColor, skin )

#ifdef REPLACEBKCOLORFROMFILLWINDOW
////////////////////////////////////////////////////////////////////////////////
//                   Overdetermination Windows Procedure
////////////////////////////////////////////////////////////////////////////////

#define WNDPROC_OVER(name, Skin)   \
WNDPROC defWndProc_##name;  \
LRESULT CALLBACK newWndProc_##name(HWND hwnd, UINT msg, WPARAM wParam, LPARAM lParam) {  \
   /*switch (msg) { \
   case WM_ERASEBKGND:  \
      if (Skin.m_bToAll) { \
         return nsEraseBk::OnEraseBkgnd(hwnd, (HDC)wParam, Skin.m_colorBk);  \
      }  \
   }*/  \
   return CallWindowProc((WNDPROC)defWndProc_##name, hwnd, msg, wParam, lParam);   \
}

#define WNDPROC_STATIC(name, Skin)   \
WNDPROC defWndProc_##name;  \
LRESULT CALLBACK newWndProc_##name(HWND hwnd, UINT msg, WPARAM wParam, LPARAM lParam) {  \
   switch (msg) { \
   case WM_PAINT: \
      CallWindowProc((WNDPROC)defWndProc_##name, hwnd, msg, wParam, lParam); \
      if (Skin.m_bToAll)  \
         nsEraseBk::FillWnd(hwnd, Skin.m_colorBk); \
      return 0;   \
   }  \
   return CallWindowProc((WNDPROC)defWndProc_##name, hwnd, msg, wParam, lParam);   \
}

#define WNDPROC_BUTTON(name, hDlg, Skin)   \
WNDPROC defWndProc_##name; \
LRESULT CALLBACK newWndProc_##name(HWND hwnd, UINT msg, WPARAM wParam, LPARAM lParam) {   \
   switch (msg) { \
   case WM_MOUSEMOVE:   \
      CallWindowProc((WNDPROC)defWndProc_##name, hwnd, msg, wParam, lParam);   \
      {  if ((Skin.m_bToAll) && (wParam & MK_LBUTTON)) {  \
            /*POINT cursorPos = {LOWORD(lParam), HIWORD(lParam)}; */ \
            POINTEX cursorPos = ::GetCursorPos();   \
            RECTEX winRect = ::GetWindowRect(hwnd);   \
            BOOL inRegion = ::PointInRect(cursorPos, winRect);  \
            static BOOL oldInRegion = inRegion; \
            if (oldInRegion != inRegion)  \
               nsEraseBk::FillWnd(hwnd, Skin.m_colorBk); \
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
   case WM_SETTEXT: \
      CallWindowProc((WNDPROC)defWndProc_##name, hwnd, msg, wParam, lParam);   \
      if (Skin.m_bToAll)  \
         nsEraseBk::FillWnd(hwnd, Skin.m_colorBk); \
      return 0;   \
   }  \
   return CallWindowProc((WNDPROC)defWndProc_##name, hwnd, msg, wParam, lParam);  \
}

#define WNDPROC_BUTTON_LOG(name, hDlg, Skin)   \
WNDPROC defWndProc_##name; \
LRESULT CALLBACK newWndProc_##name(HWND hwnd, UINT msg, WPARAM wParam, LPARAM lParam) {   \
   g_Logger.PutMsg(TEXT(""), msg); \
   switch (msg) { \
   case WM_MOUSEMOVE:   \
      CallWindowProc((WNDPROC)defWndProc_##name, hwnd, msg, wParam, lParam);   \
      {  if ((Skin.m_bToAll) && (wParam & MK_LBUTTON)) {  \
            /*POINT cursorPos = {LOWORD(lParam), HIWORD(lParam)}; */ \
            POINTEX cursorPos = ::GetCursorPos();   \
            RECTEX winRect = ::GetWindowRect(hwnd);   \
            BOOL inRegion = ::PointInRect(cursorPos, winRect);  \
            static BOOL oldInRegion = inRegion; \
            if (oldInRegion != inRegion)  \
               nsEraseBk::FillWnd(hwnd, Skin.m_colorBk); \
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
      CallWindowProc((WNDPROC)defWndProc_##name, hwnd, msg, wParam, lParam);   \
      if (Skin.m_bToAll)  \
         nsEraseBk::FillWnd(hwnd, Skin.m_colorBk); \
      return 0;   \
   }  \
   return CallWindowProc((WNDPROC)defWndProc_##name, hwnd, msg, wParam, lParam);  \
}

#define SETNEWWNDPROC(hwnd, name) defWndProc_##name = (WNDPROC)SetWindowLong(GetDlgItem(hwnd, name), GWL_WNDPROC, (LONG)newWndProc_##name)

#endif // REPLACEBKCOLORFROMFILLWINDOW

#endif // __FILE__ERASEBK__
