////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "TcButtonImageCheck.cpp"
////////////////////////////////////////////////////////////////////////////////
#include ".\TcButtonImageCheck.h"
#include <windowsx.h>
#include "..\Lib.h"

////////////////////////////////////////////////////////////////////////////////
//                             global variables
////////////////////////////////////////////////////////////////////////////////
extern HINSTANCE ghInstance;

namespace nsControlButtonImageCheck {
   const TCHAR szCClassWnd[] = TEXT("ClassWndFastMinesButtonImageCheck");

   LRESULT CALLBACK WndProc(HWND hwnd, UINT msg, WPARAM wParam, LPARAM lParam) {
      TcButtonImageCheck* const pBtn = (TcButtonImageCheck*)GetWindowLong(hwnd, GWL_USERDATA);
      switch(msg){
      HANDLE_MSG(hwnd, WM_PAINT      , pBtn->Cls_OnPaint);
      HANDLE_MSG(hwnd, WM_LBUTTONUP  , pBtn->Cls_OnLButtonUp);
      HANDLE_MSG(hwnd, WM_LBUTTONDOWN, pBtn->Cls_OnLButtonDown);
      HANDLE_MSG(hwnd, WM_MOUSEMOVE  , pBtn->Cls_OnMouseMove);
      case BM_SETIMAGE:
         return (pBtn->Cls_OnSetImage(hwnd, (TcImage*)lParam), 0L);
      case BM_SETCHECK:
         return (pBtn->Cls_OnSetCheck(hwnd, wParam), 0L);
      }
      return DefWindowProc(hwnd, msg, wParam, lParam);
   }
} // namespace nsControlButtonImageCheck

TcButtonImageCheck::TcButtonImageCheck():
   check(false)
{
   RegClass(
      0,                                     // UINT    style
      nsControlButtonImageCheck::WndProc,    // WNDPROC lpfnWndProc
      0,                                     // int     cbClsExtra
      0,                                     // int     cbWndExtra
      ghInstance,                            // HANDLE  ghInstance
      (HICON)0,                              // HICON   hIcon
      LoadCursor(NULL, IDC_ARROW),           // HCURSOR hCursor
      GetSysColorBrush(COLOR_BTNFACE),       // HBRUSH  hbrBackground
      NULL,                                  // LPCTSTR lpszMenuName
      nsControlButtonImageCheck::szCClassWnd // LPCTSTR lpszClassName
   );
}

void TcButtonImageCheck::Create(const HWND hParent, const int id) {
   hWndParent = hParent;
   idControl = id;
   hWnd = CreateWindow(nsControlButtonImageCheck::szCClassWnd, NULL,
      WS_CHILD | WS_VISIBLE,
      CW_USEDEFAULT, CW_USEDEFAULT, CW_USEDEFAULT, CW_USEDEFAULT,
      hWndParent, HMENU(id), ghInstance, NULL);
   SetWindowLong(hWnd, GWL_USERDATA, (LONG)this);
}

////////////////////////////////////////////////////////////////////////////////
//                         обработчики сообщений
////////////////////////////////////////////////////////////////////////////////
// WM_PAINT
void TcButtonImageCheck::Cls_OnPaint(HWND hwnd) const {
   PAINTSTRUCT PaintStruct;
   HDC hDC = BeginPaint(hwnd, &PaintStruct);
 //ValidateRect(hwnd, NULL);
 //SendMessage(hwnd, WM_ERASEBKGND, (WPARAM)hDC, 0L);
   {
      RECT Rect;
      GetClientRect(hwnd, &Rect);
      /**/
      HBRUSH hBrushNew = CreateSolidBrush(
#ifdef REPLACEBKCOLORFROMFILLWINDOW
         *pToAll ? *pColorBk :
#endif // REPLACEBKCOLORFROMFILLWINDOW
         GetSysColor(COLOR_BTNFACE)
      );
      HGDIOBJ hBrushOld = SelectObject(hDC, hBrushNew);
      PatBlt(hDC, 0,0, Rect.right, Rect.bottom, PATCOPY);
      /**/
      Rect.left   += 2+!!down;
      Rect.top    += 2+!!down;
      Rect.right  -= 2-!!down;
      Rect.bottom -= 2-!!down;
      if (pImage) pImage->DrawImage(hDC, &Rect);

      GetClientRect(hwnd, &Rect);

      HPEN hPenNew1 = CreatePen(PS_SOLID, 2, (down || check) ? 0x00FFFFFF : 0);
      HGDIOBJ hPenOld = SelectObject(hDC, hPenNew1);
      MoveToEx(hDC, Rect.right-1, Rect.top   +1, NULL);
      LineTo  (hDC, Rect.right-1, Rect.bottom-1);
      LineTo  (hDC, Rect.left +1, Rect.bottom-1);

      HPEN hPenNew2 = CreatePen(PS_SOLID, 2, (down || check) ? 0 : 0x00FFFFFF);
      SelectObject(hDC, hPenNew2);
      MoveToEx(hDC, Rect.left +1, Rect.bottom-2, NULL);
      LineTo  (hDC, Rect.left +1, Rect.top   +1);
      LineTo  (hDC, Rect.right-2, Rect.top   +1);

      SelectObject(hDC, hBrushOld);
      SelectObject(hDC, hPenOld);
      DeleteObject(hBrushNew);
      DeleteObject(hPenNew1);
      DeleteObject(hPenNew2);
   }
   EndPaint(hwnd, &PaintStruct);
}

// BM_SETCHECK
void TcButtonImageCheck::Cls_OnSetCheck(HWND hwnd, UINT fCheck) {
   check = (fCheck == BST_CHECKED);
   InvalidateRect(hwnd, NULL, TRUE);
}
