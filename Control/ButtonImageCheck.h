////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "ButtonImageCheck.h"
////////////////////////////////////////////////////////////////////////////////

#ifndef __FILE__BUTTONIMAGECHECK__
#define __FILE__BUTTONIMAGECHECK__

#ifndef __AFX_H__
   #include <Windows.h>
#endif
#include "ButtonImage.h"

class CButtonImageCheck: public CButtonImage {
private:
   const static TCHAR SZ_CLASS_WND[];
protected:
   bool m_bCheck;
protected:
   void OnPaint   (HWND) const; // WM_PAINT
   void OnSetCheck(HWND, UINT); // BM_SETCHECK
   static LRESULT CALLBACK WndProc(HWND, UINT, WPARAM, LPARAM);
public:
   CButtonImageCheck();
   void Create(HWND, int);
   bool IsChecked() const {return m_bCheck;};
};

#endif // __FILE__BUTTONIMAGECHECK__
