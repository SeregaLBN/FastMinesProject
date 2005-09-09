////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "ButtonImage.cpp"
////////////////////////////////////////////////////////////////////////////////

#include "StdAfx.h"
#include <WindowsX.h>
#include "ButtonImage.h"
#include "CommonLib.h"

////////////////////////////////////////////////////////////////////////////////
//                             global variables
////////////////////////////////////////////////////////////////////////////////
extern HINSTANCE ghInstance;

const TCHAR CButtonImage::SZ_CLASS_WND[] = TEXT("ClassWndFastMinesButtonImage");

LRESULT CALLBACK CButtonImage::WndProc(HWND hWnd, UINT msg, WPARAM wParam, LPARAM lParam) {
   CButtonImage *const This = (CButtonImage*)::GetWindowUserData(hWnd);
   if (This) {
      switch(msg){
      HANDLE_MSG(hWnd, WM_PAINT      , This->OnPaint);
      HANDLE_MSG(hWnd, WM_ERASEBKGND , This->OnEraseBkgnd);
      HANDLE_MSG(hWnd, WM_LBUTTONUP  , This->OnLButtonUp);
      HANDLE_MSG(hWnd, WM_LBUTTONDOWN, This->OnLButtonDown);
      HANDLE_MSG(hWnd, WM_MOUSEMOVE  , This->OnMouseMove);
      case BM_SETIMAGE:
         return (This->OnSetImage(hWnd, (CImage*)lParam), 0L);
      }
   }
   return DefWindowProc(hWnd, msg, wParam, lParam);
}

CButtonImage::CButtonImage():
   m_colorBk   (CLR_INVALID),
   m_hBrush    (NULL),
   m_bDown     (false),
   m_pImage    (NULL),
   m_hWnd      (NULL),
   m_hWndParent(NULL),
   m_iIdControl(0)
{
   SetBkColor(m_colorBk);
   static BOOL bInit =
   ::RegClass(
      0,                               // UINT    style
      WndProc,                         // WNDPROC lpfnWndProc
      0,                               // int     cbClsExtra
      0,                               // int     cbWndExtra
      ghInstance,                      // HANDLE  ghInstance
      (HICON)0,                        // HICON   hIcon
      LoadCursor(NULL, IDC_ARROW),     // HCURSOR hCursor
      GetSysColorBrush(COLOR_BTNFACE), // HBRUSH  hbrBackground
      NULL,                            // LPCTSTR lpszMenuName
      SZ_CLASS_WND                     // LPCTSTR lpszClassName
   );
}

CButtonImage::~CButtonImage() {
   ::DeleteObject(m_hBrush);
}

void CButtonImage::Create(HWND hParent, int id) {
   m_hWndParent = hParent;
   m_iIdControl = id;
   m_hWnd = CreateWindow(SZ_CLASS_WND, NULL,
      WS_CHILD | WS_VISIBLE,
      CW_USEDEFAULT, CW_USEDEFAULT, CW_USEDEFAULT, CW_USEDEFAULT,
      m_hWndParent, HMENU(id), ghInstance, NULL);
   ::SetWindowUserData(m_hWnd, (LONG)this);
}

void CButtonImage::SetBkColor(COLORREF colorBk) {
   m_colorBk = colorBk;

   ::DeleteObject(m_hBrush);
   if ((m_colorBk == CLR_INVALID) ||
       (m_colorBk == ::GetSysColor(COLOR_BTNFACE))
      )
   {
      m_hBrush = ::GetSysColorBrush(COLOR_BTNFACE);
   } else {
      m_hBrush = ::CreateSolidBrush(m_colorBk );
   }

   ::InvalidateRect(m_hWnd, NULL, TRUE);
}

////////////////////////////////////////////////////////////////////////////////
//                         обработчики сообщений
////////////////////////////////////////////////////////////////////////////////

// WM_ERASEBKGND
BOOL CButtonImage::OnEraseBkgnd(HWND hWnd, HDC hDC) {
   RECTEX Rect = ::GetClientRect(hWnd);
   HBRUSH hBrushOld = (HBRUSH)::SelectObject(hDC, m_hBrush);
   ::PatBlt(hDC, 0,0, Rect.right, Rect.bottom, PATCOPY);
   ::SelectObject(hDC, hBrushOld);
   return TRUE;
}

// WM_PAINT
void CButtonImage::OnPaint(HWND hWnd) const {
   PAINTSTRUCT PaintStruct;
   HDC hDC = BeginPaint(hWnd, &PaintStruct);
   {
      RECTEX Rect = ::GetClientRect(hWnd);
      Rect.left   += 2+!!m_bDown;
      Rect.top    += 2+!!m_bDown;
      Rect.right  -= 2-!!m_bDown;
      Rect.bottom -= 2-!!m_bDown;
      if (m_pImage)
         m_pImage->Draw(hDC, &Rect);

      Rect = ::GetClientRect(hWnd);

      HPEN hPenNew1 = CreatePen(PS_SOLID, 2, m_bDown ? 0x00FFFFFF : 0);
      HPEN hPenOld = (HPEN)SelectObject(hDC, hPenNew1);
      MoveToEx(hDC, Rect.right-1, Rect.top   +1, NULL);
      LineTo  (hDC, Rect.right-1, Rect.bottom-1);
      LineTo  (hDC, Rect.left +1, Rect.bottom-1);

      HPEN hPenNew2 = CreatePen(PS_SOLID, 2, m_bDown ? 0 : 0x00FFFFFF);
      SelectObject(hDC, hPenNew2);
      MoveToEx(hDC, Rect.left +1, Rect.bottom-2, NULL);
      LineTo  (hDC, Rect.left +1, Rect.top   +1);
      LineTo  (hDC, Rect.right-2, Rect.top   +1);

      SelectObject(hDC, hPenOld);
      DeleteObject(hPenNew1);
      DeleteObject(hPenNew2);
   }
   ::EndPaint(hWnd, &PaintStruct);
}

// BM_SETIMAGE
void CButtonImage::OnSetImage(HWND hWnd, CImage* pNewImage) {
   m_pImage = pNewImage;
   InvalidateRect(hWnd, NULL, TRUE);
}

// WM_LBUTTONDOWN & WM_LBUTTONDBLCLK
void CButtonImage::OnLButtonDown(HWND hWnd, BOOL fDoubleClick, int x, int y, UINT keyFlags) {
   SetCapture(hWnd);
   m_bDown = true;
   InvalidateRect(hWnd, NULL, TRUE);
}

// WM_LBUTTONUP
void CButtonImage::OnLButtonUp(HWND hWnd, int x, int y, UINT keyFlags) {
   ReleaseCapture();
   m_bDown = false;
   InvalidateRect(hWnd, NULL, TRUE);
   RECT Rect; GetClientRect(hWnd, &Rect);
   if ((x <  Rect.left) || (x >= Rect.right ) ||
       (y <  Rect.top ) || (y >= Rect.bottom)) return;
   FORWARD_WM_COMMAND(m_hWndParent, m_iIdControl, hWnd, 0, SendMessage);
}

// WM_MOUSEMOVE
void CButtonImage::OnMouseMove(HWND hWnd, int x, int y, UINT keyFlags) {
   FORWARD_WM_MOUSEMOVE(hWnd, x, y, keyFlags, DefWindowProc);
   if (keyFlags == MK_LBUTTON) {
      RECT Rect;
      GetClientRect(hWnd, &Rect);
      bool inRegion = true;
      if ((x < Rect.left) || (x >=Rect.right ) ||
          (y < Rect.top ) || (y >=Rect.bottom)) inRegion = false;
      static bool oldInRegion = inRegion;
      if (oldInRegion != inRegion) {
         m_bDown = inRegion;
         InvalidateRect(hWnd, NULL, TRUE);
      }
      oldInRegion = inRegion;
   }
}
