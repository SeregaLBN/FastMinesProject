////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "TcButtonImage.cpp"
////////////////////////////////////////////////////////////////////////////////
#include ".\TcButtonImage.h"
#include <windowsx.h>
#include "..\Lib.h"

////////////////////////////////////////////////////////////////////////////////
//                             global variables
////////////////////////////////////////////////////////////////////////////////
extern HINSTANCE ghInstance;

namespace nsControlButtonImage {
   const TCHAR szCClassWnd[] = TEXT("ClassWndFastMinesButtonImage");

   LRESULT CALLBACK WndProc(HWND hwnd, UINT msg, WPARAM wParam, LPARAM lParam) {
      TcButtonImage * const pBtn = (TcButtonImage*)GetWindowLong(hwnd, GWL_USERDATA);
      switch(msg){
      HANDLE_MSG(hwnd, WM_PAINT      , pBtn->Cls_OnPaint);
      HANDLE_MSG(hwnd, WM_LBUTTONUP  , pBtn->Cls_OnLButtonUp);
      HANDLE_MSG(hwnd, WM_LBUTTONDOWN, pBtn->Cls_OnLButtonDown);
      HANDLE_MSG(hwnd, WM_MOUSEMOVE  , pBtn->Cls_OnMouseMove);
      case BM_SETIMAGE:
         return (pBtn->Cls_OnSetImage(hwnd, (TcImage*)lParam), 0L);
      }
      return DefWindowProc(hwnd, msg, wParam, lParam);
   }
} // namespace nsButtonImage

TcButtonImage::TcButtonImage():
#ifdef REPLACEBKCOLORFROMFILLWINDOW
   pToAll    (NULL),
   pColorBk  (NULL),
#endif // REPLACEBKCOLORFROMFILLWINDOW
   down      (false),
   pImage    (NULL),
   hWnd      (NULL),
   hWndParent(NULL),
   idControl (0)
{
   RegClass(
      0,                                // UINT    style
      nsControlButtonImage::WndProc,    // WNDPROC lpfnWndProc
      0,                                // int     cbClsExtra
      0,                                // int     cbWndExtra
      ghInstance,                       // HANDLE  ghInstance
      (HICON)0,                         // HICON   hIcon
      LoadCursor(NULL, IDC_ARROW),      // HCURSOR hCursor
      GetSysColorBrush(COLOR_BTNFACE),  // HBRUSH  hbrBackground
      NULL,                             // LPCTSTR lpszMenuName
      nsControlButtonImage::szCClassWnd // LPCTSTR lpszClassName
   );
}

void TcButtonImage::Create(const HWND hParent, const int id) {
   hWndParent = hParent;
   idControl = id;
   hWnd = CreateWindow(nsControlButtonImage::szCClassWnd, NULL,
      WS_CHILD | WS_VISIBLE,
      CW_USEDEFAULT, CW_USEDEFAULT, CW_USEDEFAULT, CW_USEDEFAULT,
      hWndParent, HMENU(id), ghInstance, NULL);
   SetWindowLong(hWnd, GWL_USERDATA, (LONG)this);
}

#ifdef REPLACEBKCOLORFROMFILLWINDOW
void TcButtonImage::SetColor(const bool& setColorToAll, const COLORREF& setColorBk) {
   pToAll   = &setColorToAll;
   pColorBk = &setColorBk;
}
#endif // REPLACEBKCOLORFROMFILLWINDOW

////////////////////////////////////////////////////////////////////////////////
//                         обработчики сообщений
////////////////////////////////////////////////////////////////////////////////
// WM_PAINT
void TcButtonImage::Cls_OnPaint(HWND hwnd) const {
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
      HBRUSH hBrushOld = SelectObject(hDC, hBrushNew);
      PatBlt(hDC, 0,0, Rect.right, Rect.bottom, PATCOPY);
      /**/
      Rect.left   += 2+!!down;
      Rect.top    += 2+!!down;
      Rect.right  -= 2-!!down;
      Rect.bottom -= 2-!!down;
      if (pImage)
         pImage->DrawImage(hDC, &Rect);

      GetClientRect(hwnd, &Rect);

      HPEN hPenNew1 = CreatePen(PS_SOLID, 2, down ? 0x00FFFFFF : 0);
      HPEN hPenOld = SelectObject(hDC, hPenNew1);
      MoveToEx(hDC, Rect.right-1, Rect.top   +1, NULL);
      LineTo  (hDC, Rect.right-1, Rect.bottom-1);
      LineTo  (hDC, Rect.left +1, Rect.bottom-1);

      HPEN hPenNew2 = CreatePen(PS_SOLID, 2, down ? 0 : 0x00FFFFFF);
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

// BM_SETIMAGE
void TcButtonImage::Cls_OnSetImage(HWND hwnd, TcImage* pNewImage) {
   pImage = pNewImage;
   InvalidateRect(hwnd, NULL, TRUE);
}

// WM_LBUTTONDOWN & WM_LBUTTONDBLCLK
void TcButtonImage::Cls_OnLButtonDown(HWND hwnd, BOOL fDoubleClick, int x, int y, UINT keyFlags) {
   SetCapture(hwnd);
   down = true;
   InvalidateRect(hwnd, NULL, TRUE);
}

// WM_LBUTTONUP
void TcButtonImage::Cls_OnLButtonUp(HWND hwnd, int x, int y, UINT keyFlags) {
   ReleaseCapture();
   down = false;
   InvalidateRect(hwnd, NULL, TRUE);
   RECT Rect; GetClientRect(hwnd, &Rect);
   if ((x <  Rect.left) || (x >= Rect.right ) ||
       (y <  Rect.top ) || (y >= Rect.bottom)) return;
   FORWARD_WM_COMMAND(hWndParent, idControl, hwnd, 0, SendMessage);
}

// WM_MOUSEMOVE
void TcButtonImage::Cls_OnMouseMove(HWND hwnd, int x, int y, UINT keyFlags) {
   FORWARD_WM_MOUSEMOVE(hwnd, x, y, keyFlags, DefWindowProc);
   if (keyFlags == MK_LBUTTON) {
      RECT Rect;
      GetClientRect(hwnd, &Rect);
      bool inRegion = true;
      if ((x < Rect.left) || (x >=Rect.right ) ||
          (y < Rect.top ) || (y >=Rect.bottom)) inRegion = false;
      static bool oldInRegion = inRegion;
      if (oldInRegion != inRegion) {
         down = inRegion;
         InvalidateRect(hwnd, NULL, TRUE);
      }
      oldInRegion = inRegion;
   }
}
