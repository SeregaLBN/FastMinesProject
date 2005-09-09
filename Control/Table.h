////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "Table.h"
////////////////////////////////////////////////////////////////////////////////

#ifndef __FILE__CTABLE__
#define __FILE__CTABLE__

#pragma warning(disable:4786) // identifier was truncated to '255' characters in the debug information
#include <vector>
#ifndef __AFX_H__
   #include <Windows.h>
   #include "CStringKS.h"
#endif
#include "../Image.h"

#define NC_TABLE_SELECT_CELL 0 // notification code
#define NC_TABLE_DELETE_CELL 1 // notification code

struct CTableData {
   CString m_strText;
   UINT m_uFormatText;
   const CImage* m_pImage;
   CTableData(): m_pImage(NULL), m_uFormatText(DT_CENTER | DT_VCENTER | DT_SINGLELINE) {};
};

class CTable {
private:
   static const TCHAR SZ_CLASS_WND[];
   static const POINT INVALID_CELL;
protected:
   COLORREF m_colorBk;
   HWND m_hWnd, m_hWndParent;
   int m_iIdControl;
   int m_iDefW; // default width  cell
   int m_iDefH; // default height cell
   int m_iStaticCol; // кол-во статичных колонок
   int m_iStaticRow; // кол-во статичных строк
   POINT m_CurrentCell;  // текущая (выделенная рамкой)  ячейка (не может быть статичной) POINT.y - строка
   bool m_bSelect; // залита ли текущая ячейка
   std::vector<int> m_ColW; // набор широт колонок
   std::vector<int> m_RowH; // набор высот строк
   std::vector<std::vector<CTableData*> > m_Data; // [колонка][строка]
   std::vector<bool> m_Image;
   //mutable bool wm_size;
protected:
   void OnPaint      (HWND) const;                  // WM_PAINT
   void OnLButtonDown(HWND, BOOL, int, int, UINT);  // WM_LBUTTONDOWN & WM_LBUTTONDBLCLK
   void OnRButtonDown(HWND, BOOL, int, int, UINT);  // WM_RBUTTONDOWN & WM_RBUTTONDBLCLK
   void OnLButtonUp  (HWND, int, int, UINT);        // WM_LBUTTONUP
   void OnMouseMove  (HWND, int, int, UINT);        // WM_MOUSEMOVE
   void OnSize       (HWND, UINT, int, int);        // WM_SIZE
   void OnHScroll    (HWND, HWND, UINT, int);       // WM_HSCROLL
   void OnVScroll    (HWND, HWND, UINT, int);       // WM_VSCROLL
   BOOL OnEraseBkgnd (HWND, HDC);                   // WM_ERASEBKGND
   void OnKey        (HWND, UINT, BOOL, int, UINT); // WM_KEYDOWN & WM_KEYUP
   static LRESULT CALLBACK WndProc(HWND, UINT, WPARAM, LPARAM);
   POINT FindCell(const int xWin, const int yWin) const;
   void DrawHScroll() const;
   void DrawVScroll() const;
   void RecountScrollInfo();
   void SetVisibleCell(const int indexCol, const int indexRow); // сделать ячейку видимой (отображаемой) на экране
   bool    VisibleCell(const int indexCol, const int indexRow) const; // видимa ли ячейка? (отображается на экране?)
public:
   int Width (const int col) const; // ширина первых col колонок
   int Height(const int row) const; // высота первых row строк
   int  GetColNumber() const {return m_ColW.size();}; // узнать число колонок
   int  GetRowNumber() const {return m_RowH.size();}; // узнать число строк
   void SetColNumber(const int nCol); // установить заданное число колонок
   void SetRowNumber(const int nRow); // установить заданное число строк
   void SetColWidth (const int indexCol, const int width); // установить ширину заданной колонки
   void SetRowHeight(const int indexRow, const int height); // установить высоту заданной строки
   CTable();
  ~CTable();
   void Create(const HWND hParent, const int id);
   HWND    GetHandle() const {return m_hWnd;}
   void    SetBkColor(COLORREF);
   void    SetText       (const int indexCol, const int indexRow, LPCTSTR);
   void    SetFormatText (const int indexCol, const int indexRow, UINT uFormatText);
   void    SetImage      (const int indexCol, const int indexRow, const CImage*);
   void    SetCurrentCell(const int indexCol, const int indexRow, const bool isSelect = false);
   POINT   GetCurrentCell() const {return m_CurrentCell;};
   LPCTSTR GetCurrentText() const;
   int  GetDefaultWidth () const {return m_iDefW;};
   int  GetDefaultHeight() const {return m_iDefH;};
   void SetDefaultWidth (const int newDefW) {m_iDefW = newDefW;};
   void SetDefaultHeight(const int newDefH) {m_iDefH = newDefH;};
   void UnselectCell();
};

#endif // __FILE__CTABLE__
