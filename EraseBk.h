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

namespace nsEraseBk {
   HBRUSH OnCtlColor(HWND hwnd, HDC hdc, HWND hwndChild, int type);
#ifdef REPLACEBKCOLORFROMFILLWINDOW
   const RECT errorRect = {0,0,0,0};

   void FillWnd    (HWND hwnd, COLORREF bkColor, bool client = true, const RECT& fillRect = errorRect);
   void FillMenu   (HWND hwnd, COLORREF bkColor);
   void FillCaption(HWND hwnd, COLORREF bkColor);

   BOOL OnEraseBkgnd(HWND hwnd, HDC hdc, COLORREF colorBk);
#endif // REPLACEBKCOLORFROMFILLWINDOW
   BOOL ReplaceFunctions();
}

//#define HANDLE_WM_CTLCOLOR(hwnd)
#define HANDLE_WM_CTLCOLOR(hwnd) \
   HANDLE_MSG(hwnd, WM_CTLCOLORMSGBOX   , nsEraseBk::OnCtlColor);     \
 /*HANDLE_MSG(hwnd, WM_CTLCOLOREDIT     , nsEraseBk::OnCtlColor);/**/ \
 /*HANDLE_MSG(hwnd, WM_CTLCOLORLISTBOX  , nsEraseBk::OnCtlColor);/**/ \
   HANDLE_MSG(hwnd, WM_CTLCOLORBTN      , nsEraseBk::OnCtlColor);     \
   HANDLE_MSG(hwnd, WM_CTLCOLORDLG      , nsEraseBk::OnCtlColor);     \
   HANDLE_MSG(hwnd, WM_CTLCOLORSCROLLBAR, nsEraseBk::OnCtlColor);     \
   HANDLE_MSG(hwnd, WM_CTLCOLORSTATIC   , nsEraseBk::OnCtlColor)

#ifdef REPLACEBKCOLORFROMFILLWINDOW
////////////////////////////////////////////////////////////////////////////////
//                   Overdetermination Windows Procedure
////////////////////////////////////////////////////////////////////////////////

#if (_MSC_VER==1100)
   #define CALL_WND_PROC FARPROC // MsVC++ 6
#else
   #define CALL_WND_PROC WNDPROC // MsVC++ 5
#endif

#define WNDPROC_OVER(name, Skin)   \
WNDPROC defWndProc_##name;  \
LRESULT CALLBACK newWndProc_##name(HWND hwnd, UINT msg, WPARAM wParam, LPARAM lParam) {  \
   /*switch (msg) { \
   case WM_ERASEBKGND:  \
      if (Skin.m_bToAll) { \
         return nsEraseBk::OnEraseBkgnd(hwnd, (HDC)wParam, Skin.m_colorBk);  \
      }  \
   }*/  \
   return CallWindowProc((CALL_WND_PROC)defWndProc_##name, hwnd, msg, wParam, lParam);   \
}

#define WNDPROC_STATIC(name, Skin)   \
WNDPROC defWndProc_##name;  \
LRESULT CALLBACK newWndProc_##name(HWND hwnd, UINT msg, WPARAM wParam, LPARAM lParam) {  \
   switch (msg) { \
   case WM_PAINT: \
      CallWindowProc((CALL_WND_PROC)defWndProc_##name, hwnd, msg, wParam, lParam); \
      if (Skin.m_bToAll)  \
         nsEraseBk::FillWnd(hwnd, Skin.m_colorBk); \
      return 0;   \
   }  \
   return CallWindowProc((CALL_WND_PROC)defWndProc_##name, hwnd, msg, wParam, lParam);   \
}

#define WNDPROC_BUTTON(name, hDlg, Skin)   \
WNDPROC defWndProc_##name; \
LRESULT CALLBACK newWndProc_##name(HWND hwnd, UINT msg, WPARAM wParam, LPARAM lParam) {   \
   switch (msg) { \
   case WM_MOUSEMOVE:   \
      CallWindowProc((CALL_WND_PROC)defWndProc_##name, hwnd, msg, wParam, lParam);   \
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
      CallWindowProc((CALL_WND_PROC)defWndProc_##name, hwnd, msg, wParam, lParam);   \
      if (Skin.m_bToAll)  \
         nsEraseBk::FillWnd(hwnd, Skin.m_colorBk); \
      return 0;   \
   }  \
   return CallWindowProc((CALL_WND_PROC)defWndProc_##name, hwnd, msg, wParam, lParam);  \
}

#define WNDPROC_BUTTON_LOG(name, hDlg, Skin)   \
WNDPROC defWndProc_##name; \
LRESULT CALLBACK newWndProc_##name(HWND hwnd, UINT msg, WPARAM wParam, LPARAM lParam) {   \
   g_Logger.PutMsg(TEXT(""), msg); \
   switch (msg) { \
   case WM_MOUSEMOVE:   \
      CallWindowProc((CALL_WND_PROC)defWndProc_##name, hwnd, msg, wParam, lParam);   \
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
      CallWindowProc((CALL_WND_PROC)defWndProc_##name, hwnd, msg, wParam, lParam);   \
      if (Skin.m_bToAll)  \
         nsEraseBk::FillWnd(hwnd, Skin.m_colorBk); \
      return 0;   \
   }  \
   return CallWindowProc((CALL_WND_PROC)defWndProc_##name, hwnd, msg, wParam, lParam);  \
}

#define SETNEWWNDPROC(hwnd, name) defWndProc_##name = (WNDPROC)SetWindowLong(GetDlgItem(hwnd, name), GWL_WNDPROC, (LONG)newWndProc_##name)

#endif // REPLACEBKCOLORFROMFILLWINDOW

#endif // __FILE__ERASEBK__
