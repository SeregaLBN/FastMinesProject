////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "ButtonImage.h"
////////////////////////////////////////////////////////////////////////////////

#ifndef __FILE__BUTTONIMAGE__
#define __FILE__BUTTONIMAGE__

#ifndef __AFX_H__
   #include <Windows.h>
#endif
#include "../Image.h"

class CButtonImage {
private:
   const static TCHAR SZ_CLASS_WND[];
protected:
   COLORREF m_colorBk;
   HBRUSH   m_hBrush;
   HWND m_hWnd, m_hWndParent;
   bool m_bDown;
   CImage* m_pImage;
   int m_iIdControl;
protected:
   void OnPaint      (HWND) const;                 // WM_PAINT
   BOOL OnEraseBkgnd (HWND, HDC);                  // WM_ERASEBKGND
   void OnSetImage   (HWND, CImage*);              // BM_SETIMAGE
   void OnLButtonUp  (HWND, int , int, UINT);      // WM_LBUTTONUP
   void OnLButtonDown(HWND, BOOL, int, int, UINT); // WM_LBUTTONDOWN & WM_LBUTTONDBLCLK
   void OnMouseMove  (HWND, int , int, UINT);      // WM_MOUSEMOVE
   static LRESULT CALLBACK WndProc(HWND, UINT, WPARAM, LPARAM);
public:
   CButtonImage();
  ~CButtonImage();
   HWND GetHandle() const {return m_hWnd;}
   virtual void Create(HWND, int);
   void SetBkColor(COLORREF);
};

#endif // __FILE__BUTTONIMAGE__
