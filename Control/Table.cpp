////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "Table.cpp"
////////////////////////////////////////////////////////////////////////////////

#include "StdAfx.h"
#include <WindowsX.h>
#include "Table.h"
#include "../CommonLib.h"
#include "../EraseBk.h"

////////////////////////////////////////////////////////////////////////////////
//                             global variables
////////////////////////////////////////////////////////////////////////////////
extern HINSTANCE ghInstance;

const TCHAR CTable::SZ_CLASS_WND[] = TEXT("ClassWndFastMinesTable");
const POINT CTable::INVALID_CELL = {-1,-1};

LRESULT CALLBACK CTable::WndProc(HWND hWnd, UINT msg, WPARAM wParam, LPARAM lParam) {
   CTable *const This = (CTable*)GetWindowLong(hWnd, GWL_USERDATA);
   if (This)
      switch(msg){
      case WM_GETDLGCODE:
         //g_Logger.Put(CLogger::LL_DEBUG, "msg WM_GETDLGCODE: keyState = 0x%08X", keyState, 2);
         if (GetKeyState(VK_SPACE) & 0x80000000) {
            static int count = 0;
            if (++count == 10) FORWARD_WM_KEYUP(hWnd, VK_SPACE, count = 0, 0, SendMessage);
         }
         if (GetKeyState(VK_UP   ) & 0x80000000) {
            static int count = 0;
            if (++count == 10) FORWARD_WM_KEYUP(hWnd, VK_UP   , count = 0, 0, SendMessage);
         }
         if (GetKeyState(VK_DOWN ) & 0x80000000) {
            static int count = 0;
            if (++count == 10) FORWARD_WM_KEYUP(hWnd, VK_DOWN , count = 0, 0, SendMessage);
         }
         if (GetKeyState(VK_LEFT ) & 0x80000000) {
            static int count = 0;
            if (++count == 10) FORWARD_WM_KEYUP(hWnd, VK_LEFT , count = 0, 0, SendMessage);
         }
         if (GetKeyState(VK_RIGHT) & 0x80000000) {
            static int count = 0;
            if (++count == 10) FORWARD_WM_KEYUP(hWnd, VK_RIGHT, count = 0, 0, SendMessage);
         }
         break;
      HANDLE_MSG(hWnd, WM_ERASEBKGND   , This->OnEraseBkgnd);
      HANDLE_MSG(hWnd, WM_PAINT        , This->OnPaint);
      HANDLE_MSG(hWnd, WM_MOUSEMOVE    , This->OnMouseMove);
      HANDLE_MSG(hWnd, WM_LBUTTONDOWN  , This->OnLButtonDown);
      HANDLE_MSG(hWnd, WM_LBUTTONDBLCLK, This->OnLButtonDown);
      HANDLE_MSG(hWnd, WM_RBUTTONDOWN  , This->OnRButtonDown);
      HANDLE_MSG(hWnd, WM_LBUTTONUP    , This->OnLButtonUp);
      HANDLE_MSG(hWnd, WM_SIZE         , This->OnSize);
      HANDLE_MSG(hWnd, WM_HSCROLL      , This->OnHScroll);
      HANDLE_MSG(hWnd, WM_VSCROLL      , This->OnVScroll);
      HANDLE_MSG(hWnd, WM_KEYUP        , This->OnKey);
    //HANDLE_WM_CTLCOLOR(hWnd);
/*
      case WM_USER+1: // ~ WM_SIZE
         {
            if (This->wm_size) {
               PostMessage(hWnd, WM_USER, wParam, lParam);
               break;
            }
            const int cx = wParam;
            const int cy = lParam;
            static POINT oldSize = {0,0};
            const RECT invalidateRect1 = {min(oldSize.x, cx), 0                 , cx, cy};
            const RECT invalidateRect2 = {0                 , min(oldSize.y, cy), cx, cy};
            InvalidateRect(hWnd, &invalidateRect1, FALSE);
            InvalidateRect(hWnd, &invalidateRect2, FALSE);
            oldSize.x = cx;
            oldSize.y = cy;
         }
         break;
/**/
   }
   return DefWindowProc(hWnd, msg, wParam, lParam);
}

CTable::~CTable() {
   DestroyWindow(m_hWnd);
   for (int i=0; i<m_ColW.size(); i++)
      for (int j=0; j<m_RowH.size(); j++)
         delete m_Data[i][j];
}

CTable::CTable():
   m_colorBk    (CLR_INVALID),
   m_hWnd       (NULL),
   m_hWndParent (NULL),
   m_iIdControl (0),
   m_iDefH      (27), // высота подобрана так, чтобы рисунок робота отображался без искажений
   m_iDefW      (100),
   m_CurrentCell(INVALID_CELL),
   m_bSelect    (false),
   m_iStaticCol (0),
   m_iStaticRow (1)//,
 //wm_size     (false)
{
   static BOOL bInit =
   RegClass(
      CS_DBLCLKS,                      // UINT    style
      WndProc,                         // WNDPROC lpfnWndProc
      0,                               // int     cbClsExtra
      0,                               // int     cbWndExtra
      ghInstance,                      // HANDLE  ghInstance
      (HICON)0,                        // HICON   hIcon
      LoadCursor(NULL, IDC_ARROW),     // HCURSOR hCursor
      GetSysColorBrush(COLOR_BTNFACE), // HBRUSH  hbrBackground
      NULL,                            // LPCTSTR lpszMenuName
      SZ_CLASS_WND      // LPCTSTR lpszClassName
   );
}

void CTable::Create(const HWND hParent, const int id) {
   m_hWndParent = hParent;
   m_iIdControl = id;
   m_hWnd = CreateWindowEx(
      WS_EX_CLIENTEDGE,
      SZ_CLASS_WND, NULL,
      WS_GROUP | WS_CHILD | WS_VISIBLE | WS_TABSTOP | WS_VSCROLL | WS_HSCROLL,// | WS_DLGFRAME,
      CW_USEDEFAULT, CW_USEDEFAULT, CW_USEDEFAULT, CW_USEDEFAULT,
      m_hWndParent, HMENU(id), ghInstance, NULL);
   SetWindowLong(m_hWnd, GWL_USERDATA, (LONG)this);
   SetRowNumber(11);
   SetColNumber(6);

   RECTEX clientRect = GetClientRect(m_hWnd);
   const SCROLLINFO scInfoHorz = {
      sizeof(SCROLLINFO),
      SIF_RANGE | SIF_PAGE | SIF_POS,
      0, Width(m_ColW.size())+1,
      min(clientRect.width(), Width(m_ColW.size())),
      0, 0
   };
   SetScrollInfo(m_hWnd, SB_HORZ, &scInfoHorz, TRUE);
   const SCROLLINFO scInfoVert = {
      sizeof(SCROLLINFO),
      SIF_RANGE | SIF_PAGE | SIF_POS,
      0, Height(m_RowH.size())+1,
      min(clientRect.height(), Height(m_RowH.size())),
      0, 0
   };
   SetScrollInfo(m_hWnd, SB_VERT, &scInfoVert, TRUE);
}

void CTable::SetBkColor(COLORREF colorBk) {
   m_colorBk = colorBk;
   ::InvalidateRect(m_hWnd, NULL, TRUE);
}

void CTable::SetColNumber(const int nCol) {
   if (nCol < 0) return;
   const int oldCol = m_ColW.size();
   if (nCol == oldCol) return;
   { // таблица уменьшается - удалить данные
      for (int i=nCol; i<oldCol; i++)
         for (int j=0; j<m_RowH.size(); j++)
            delete m_Data[i][j];
   }
   const int oldMaxW = Width(oldCol);
   { // изменить размер таблицы
      m_ColW.resize(nCol);
      m_Data.resize(nCol);
      for (int i=oldCol; i<nCol; i++) {
         m_ColW[i] = m_iDefW;
         m_Data[i].resize(m_RowH.size());
      }
   }
   { // таблица расширяется - добавить данные
      for (int i=oldCol; i<nCol; i++)
         for (int j=0; j<m_RowH.size(); j++)
            m_Data[i][j] = new CTableData;
   }

   if (m_CurrentCell.x > nCol-1) SetCurrentCell(nCol-1, m_CurrentCell.y, m_bSelect);

   RecountScrollInfo();
   const RECT invalidateRect = {-GetScrollPos(m_hWnd, SB_HORZ)+min(oldMaxW, Width(m_ColW.size())),
                                -GetScrollPos(m_hWnd, SB_VERT),
                                -GetScrollPos(m_hWnd, SB_HORZ)+max(oldMaxW, Width(m_ColW.size()))+1,
                                -GetScrollPos(m_hWnd, SB_VERT)+Height(m_RowH.size())+1};
   InvalidateRect(m_hWnd, &invalidateRect, FALSE);
}

void CTable::SetRowNumber(const int nRow) {
   if (nRow < 0) return;
   const int oldRow = m_RowH.size();
   if (nRow == oldRow) return;
   { // таблица уменьшается - удалить данные
      for (int i=0; i<m_ColW.size(); i++)
         for (int j=nRow; j<oldRow; j++)
            delete m_Data[i][j];
   }
   const int oldMaxH = Height(oldRow);
   { // изменить размер таблицы
      m_RowH.resize(nRow);
      for (int i=0; i<m_ColW.size(); i++)
         m_Data[i].resize(nRow);
      for (int j=oldRow; j<nRow; j++)
         m_RowH[j] = m_iDefH;
   }
   { // таблица расширяется - добавить данные
      for (int i=0; i<m_ColW.size(); i++)
         for (int j=oldRow; j<nRow; j++)
            m_Data[i][j] = new CTableData;
   }

   if (m_CurrentCell.y > nRow-1) SetCurrentCell(m_CurrentCell.x, nRow-1, m_bSelect);

   RecountScrollInfo();
   const RECT invalidateRect = {-GetScrollPos(m_hWnd, SB_HORZ),
                                -GetScrollPos(m_hWnd, SB_VERT)+min(oldMaxH, Height(m_RowH.size())),
                                -GetScrollPos(m_hWnd, SB_HORZ)+Width(m_ColW.size())+1,
                                -GetScrollPos(m_hWnd, SB_VERT)+max(oldMaxH, Height(m_RowH.size()))+1};
   InvalidateRect(m_hWnd, &invalidateRect, FALSE);
}

void CTable::SetColWidth(const int indexCol, const int width) {
   if ((indexCol < 0) || (indexCol >= m_ColW.size())) return;
   const int oldMaxW = Width(m_ColW.size());
   m_ColW[indexCol] = width;
   RecountScrollInfo();
   const RECT invalidateRect = {-GetScrollPos(m_hWnd, SB_HORZ)+Width(indexCol),
                                -GetScrollPos(m_hWnd, SB_VERT),
                                -GetScrollPos(m_hWnd, SB_HORZ)+max(oldMaxW, Width(m_ColW.size()))+1,
                                -GetScrollPos(m_hWnd, SB_VERT)+Height(m_RowH.size())+1};
   InvalidateRect(m_hWnd, &invalidateRect, FALSE);
}

void CTable::SetRowHeight(const int indexRow, const int height) {
   if ((indexRow < 0) || (indexRow >= m_RowH.size())) return;
   const int oldMaxH = Height(m_RowH.size());
   m_RowH[indexRow] = height;
   RecountScrollInfo();
   const RECT invalidateRect = {-GetScrollPos(m_hWnd, SB_HORZ),
                                -GetScrollPos(m_hWnd, SB_VERT)+Height(indexRow),
                                -GetScrollPos(m_hWnd, SB_HORZ)+Width(m_ColW.size())+1,
                                -GetScrollPos(m_hWnd, SB_VERT)+max(oldMaxH, Height(m_RowH.size()))+1};
   InvalidateRect(m_hWnd, &invalidateRect, FALSE);
}

inline int CTable::Width(const int col) const { // ширина первых col колонок
   int result = 0;
   for (int i=0; i<min(m_ColW.size(), col); i++)
      result += m_ColW[i];
   return result;
}

inline int CTable::Height(const int row) const { // высота первых row строк
   int result = 0;
   for (int j=0; j<min(m_RowH.size(), row); j++)
      result += m_RowH[j];
   return result;
}

void CTable::SetText(const int indexCol, const int indexRow, LPCTSTR str) {
   if ((indexCol < 0) || (indexCol >= m_ColW.size()) ||
       (indexRow < 0) || (indexRow >= m_RowH.size())) return;
   m_Data[indexCol][indexRow]->m_strText = str;
   const x = (indexCol < m_iStaticCol) ? 0 : GetScrollPos(m_hWnd, SB_HORZ);
   const y = (indexRow < m_iStaticRow) ? 0 : GetScrollPos(m_hWnd, SB_VERT);
   const RECT invalidateRect = {-x+Width(indexCol  ), -y+Height(indexRow  ),
                                -x+Width(indexCol+1), -y+Height(indexRow+1)};
   InvalidateRect(m_hWnd, &invalidateRect, FALSE);
}

void CTable::SetFormatText(const int indexCol, const int indexRow, UINT m_uFormatText) {
   if ((indexCol < 0) || (indexCol >= m_ColW.size()) ||
       (indexRow < 0) || (indexRow >= m_RowH.size())) return;
   m_Data[indexCol][indexRow]->m_uFormatText = m_uFormatText;
   const x = (indexCol < m_iStaticCol) ? 0 : GetScrollPos(m_hWnd, SB_HORZ);
   const y = (indexRow < m_iStaticRow) ? 0 : GetScrollPos(m_hWnd, SB_VERT);
   const RECT invalidateRect = {-x+Width(indexCol  ), -y+Height(indexRow  ),
                                -x+Width(indexCol+1), -y+Height(indexRow+1)};
   InvalidateRect(m_hWnd, &invalidateRect, FALSE);
}

void CTable::SetImage(const int indexCol, const int indexRow, const CImage* pImage) {
   if ((indexCol < 0) || (indexCol >= m_ColW.size()) ||
       (indexRow < 0) || (indexRow >= m_RowH.size())) return;
   m_Data[indexCol][indexRow]->m_pImage = pImage;
   const x = (indexCol < m_iStaticCol) ? 0 : GetScrollPos(m_hWnd, SB_HORZ);
   const y = (indexRow < m_iStaticRow) ? 0 : GetScrollPos(m_hWnd, SB_VERT);
   const RECT invalidateRect = {-x+Width(indexCol  ), -y+Height(indexRow  ),
                                -x+Width(indexCol+1), -y+Height(indexRow+1)};
   InvalidateRect(m_hWnd, &invalidateRect, FALSE);
}

void CTable::SetCurrentCell(const int indexCol, const int indexRow, const bool isSelect) {
   if ((indexCol < m_iStaticCol) || (indexCol >= m_ColW.size()) ||
       (indexRow < m_iStaticRow) || (indexRow >= m_RowH.size())) return;
   const POINT newCell = {indexCol, indexRow};
   if ((m_CurrentCell  == newCell) && (m_bSelect == isSelect)) return;
   const RECT invalidateRectOld = {Width (m_CurrentCell.x  ), Height(m_CurrentCell.y  ),
                                   Width (m_CurrentCell.x+1), Height(m_CurrentCell.y+1)};
   m_bSelect = isSelect;
   m_CurrentCell = newCell;
   const RECT invalidateRectNew = {Width (m_CurrentCell.x  ), Height(m_CurrentCell.y  ),
                                   Width (m_CurrentCell.x+1), Height(m_CurrentCell.y+1)};
   const RECT invalidateRect = {min(invalidateRectOld.left  ,invalidateRectNew.left  )-GetScrollPos(m_hWnd, SB_HORZ),
                                min(invalidateRectOld.top   ,invalidateRectNew.top   )-GetScrollPos(m_hWnd, SB_VERT),
                                max(invalidateRectOld.right ,invalidateRectNew.right )-GetScrollPos(m_hWnd, SB_HORZ),
                                max(invalidateRectOld.bottom,invalidateRectNew.bottom)-GetScrollPos(m_hWnd, SB_VERT)};
   InvalidateRect(m_hWnd, &invalidateRect, FALSE);
   SetVisibleCell(indexCol, indexRow);
}

LPCTSTR CTable::GetCurrentText() const {
   if (m_CurrentCell == INVALID_CELL) {
      static const TCHAR szNull[] = TEXT("");
      return szNull;
   }
   return m_Data[m_CurrentCell.x][m_CurrentCell.y]->m_strText;
}

void CTable::UnselectCell() {
   if (m_CurrentCell == INVALID_CELL) return;
   const RECT invalidateRect = {Width (m_CurrentCell.x  ), Height(m_CurrentCell.y  ),
                                Width (m_CurrentCell.x+1), Height(m_CurrentCell.y+1)};
   m_CurrentCell = INVALID_CELL;
   InvalidateRect(m_hWnd, &invalidateRect, FALSE);
}

POINT CTable::FindCell(const int xWin, const int yWin) const {
   const int x = xWin+GetScrollPos(m_hWnd, SB_HORZ);
   const int y = yWin+GetScrollPos(m_hWnd, SB_VERT);
   POINT resultCell = INVALID_CELL;
   int result = 0;
   for (int i=0; i<m_ColW.size(); i++) {
      if ((x >= result) && (x < result+m_ColW[i])) {
         resultCell.x = i;
         break;
      }
      result += m_ColW[i];
   }
   result = 0;
   for (i=0; i<m_RowH.size(); i++) {
      if ((y >= result) && (y < result+m_RowH[i])) {
         resultCell.y = i;
         break;
      }
      result += m_RowH[i];
   }
   return ((resultCell.x == INVALID_CELL.x) ||
           (resultCell.y == INVALID_CELL.y)) ?
           INVALID_CELL :
           resultCell;
}


inline void CTable::DrawHScroll() const {
   if ((m_colorBk == CLR_INVALID) || (m_colorBk == ::GetSysColor(COLOR_BTNFACE))) return;
   RECT wndRect = ::GetWindowRect(m_hWnd);
   const RECT scrollRect = {
      +::GetSystemMetrics(SM_CXBORDER)+wndRect.left,
      -::GetSystemMetrics(SM_CYBORDER)+wndRect.bottom-GetSystemMetrics(SM_CYHSCROLL),
      -::GetSystemMetrics(SM_CXBORDER)+wndRect.right,
      -::GetSystemMetrics(SM_CYBORDER)+wndRect.bottom};
   nsEraseBk::FillWnd(m_hWnd, m_colorBk, false, scrollRect);
}

inline void CTable::DrawVScroll() const {
   if ((m_colorBk == CLR_INVALID) || (m_colorBk == ::GetSysColor(COLOR_BTNFACE))) return;
   RECT wndRect = ::GetWindowRect(m_hWnd);
   const RECT scrollRect = {
      -GetSystemMetrics(SM_CXBORDER)+wndRect.right-GetSystemMetrics(SM_CXVSCROLL),
      +GetSystemMetrics(SM_CYBORDER)+wndRect.top,
      -GetSystemMetrics(SM_CXBORDER)+wndRect.right,
      -GetSystemMetrics(SM_CYBORDER)+wndRect.bottom};
   nsEraseBk::FillWnd(m_hWnd, m_colorBk, false, scrollRect);
}

inline void CTable::RecountScrollInfo() {
   RECT clientRect = GetClientRect(m_hWnd);
   const int x = Width (m_ColW.size());
   const int y = Height(m_RowH.size());
   const int h = GetScrollPos(m_hWnd, SB_HORZ);
   const int v = GetScrollPos(m_hWnd, SB_VERT);
   const SCROLLINFO scInfoHorz = {
      sizeof(SCROLLINFO),
      SIF_RANGE | SIF_PAGE | SIF_POS,
      0,  x-1,
      min(x,     clientRect.right),
      min(x, min(clientRect.right, h)),
   };
   SetScrollInfo(m_hWnd, SB_HORZ, &scInfoHorz, TRUE);
   static bool oldShowSBHorz = (clientRect.right < x);
          bool newShowSBHorz = (clientRect.right < x);
   if (oldShowSBHorz != newShowSBHorz) {
      oldShowSBHorz = newShowSBHorz;
      ShowScrollBar(m_hWnd, SB_HORZ, newShowSBHorz); // изменяю размер клиентской области на экране, а это значит...
      RecountScrollInfo();                         // ... что надо заново пересчитать скролинг
   }
   //EnableScrollBar(m_hWnd, SB_HORZ, (clientRect.right < x) ? ESB_ENABLE_BOTH : ESB_DISABLE_BOTH);
   const SCROLLINFO scInfoVert = {
      sizeof(SCROLLINFO),
      SIF_RANGE | SIF_PAGE | SIF_POS,
      0,  y-1,
      min(y,     clientRect.bottom),
      min(y, min(clientRect.bottom, v)),
   };
   SetScrollInfo(m_hWnd, SB_VERT, &scInfoVert, TRUE);
   static bool oldShowSBVert = (clientRect.bottom < y);
          bool newShowSBVert = (clientRect.bottom < y);
   if (oldShowSBVert != newShowSBVert) {
      oldShowSBVert = newShowSBVert;
      ShowScrollBar(m_hWnd, SB_VERT, newShowSBVert); // изменяю размер клиентской области на экране, а это значит...
      RecountScrollInfo();                         // ... что надо заново пересчитать скролинг
   }
   //EnableScrollBar(m_hWnd, SB_VERT, (clientRect.bottom < y) ? ESB_ENABLE_BOTH : ESB_DISABLE_BOTH);
}

bool CTable::VisibleCell(const int indexCol, const int indexRow) const { // видимa ли ячейка? (отображается на экране?)
   if ((indexCol < 0) || (indexCol >= m_ColW.size()) ||
       (indexRow < 0) || (indexRow >= m_RowH.size())) return false; // неверный индекс
   RECT clientRect = GetClientRect(m_hWnd);
   clientRect.left += Width (m_iStaticCol);
   clientRect.top  += Height(m_iStaticRow);
   if ((clientRect.left   <= Width (indexCol  )-GetScrollPos(m_hWnd, SB_HORZ)) &&
       (clientRect.right  >= Width (indexCol+1)-GetScrollPos(m_hWnd, SB_HORZ)) &&
       (clientRect.top    <= Height(indexRow  )-GetScrollPos(m_hWnd, SB_VERT)) &&
       (clientRect.bottom >= Height(indexRow+1)-GetScrollPos(m_hWnd, SB_VERT)))
      return true;
   else
      return false;
}

void CTable::SetVisibleCell(const int indexCol, const int indexRow) { // сделать ячейку видимой (отображаемой) на экране
   if ((indexCol < 0) || (indexCol >= m_ColW.size()) ||
       (indexRow < 0) || (indexRow >= m_RowH.size())) return;
   if (VisibleCell(indexCol, indexRow)) {
      return; // ячейка и так уже полностью видима
   }
   RECT clientRect = GetClientRect(m_hWnd);
   if(GetScrollPos(m_hWnd, SB_HORZ)<(Width (indexCol+1)-clientRect.right))
      SetScrollPos(m_hWnd, SB_HORZ,  Width (indexCol+1)-clientRect.right,  TRUE);
   if(GetScrollPos(m_hWnd, SB_VERT)<(Height(indexRow+1)-clientRect.bottom))
      SetScrollPos(m_hWnd, SB_VERT,  Height(indexRow+1)-clientRect.bottom, TRUE);
   if(GetScrollPos(m_hWnd, SB_HORZ)>(Width (indexCol  )-Width (m_iStaticCol)))
      SetScrollPos(m_hWnd, SB_HORZ,  Width (indexCol  )-Width (m_iStaticCol), TRUE);
   if(GetScrollPos(m_hWnd, SB_VERT)>(Height(indexRow  )-Height(m_iStaticRow)))
      SetScrollPos(m_hWnd, SB_VERT,  Height(indexRow  )-Height(m_iStaticRow), TRUE);

   InvalidateRect(m_hWnd, NULL, FALSE);
}

////////////////////////////////////////////////////////////////////////////////
//                         обработчики сообщений
////////////////////////////////////////////////////////////////////////////////
// WM_PAINT
#define DrawCell { \
   const POINT cellPoint = {i,j}; \
   MoveToEx(hCDC, cellRect.left , cellRect.bottom-1, NULL); \
   LineTo  (hCDC, cellRect.left , cellRect.top); \
   LineTo  (hCDC, cellRect.right, cellRect.top); \
   RECT rectText = {cellRect.left+2+4, cellRect.top+2, cellRect.right-1, cellRect.bottom-1}; \
   if (m_Data[i][j]->m_pImage) { \
      const int m = min(rectText.right-rectText.left, rectText.bottom-rectText.top); \
      const RECT rectImage = {rectText.left, rectText.top, rectText.left+m, rectText.top+m}; \
      m_Data[i][j]->m_pImage->Draw(hCDC, &rectImage); \
      rectText.left += m+4; \
   } \
   if (!m_Data[i][j]->m_strText.IsEmpty()) { \
      COLORREF oldTextColor; \
      if ((cellPoint == m_CurrentCell) && m_bSelect) { \
         oldTextColor = SetTextColor(hCDC, GetSysColor(COLOR_HIGHLIGHTTEXT)); \
      } \
      DrawText(hCDC, m_Data[i][j]->m_strText, \
          -1, &rectText, m_Data[i][j]->m_uFormatText); \
      if ((cellPoint == m_CurrentCell) && m_bSelect) { \
         SetTextColor(hCDC, oldTextColor); \
      } \
   } \
}

#define DrawCellField { \
   if (cellPoint == m_CurrentCell) { \
      if (m_bSelect) { \
         const HBRUSH hBrushNew = CreateSolidBrush(GetSysColor(COLOR_HIGHLIGHT)); \
         const HBRUSH hBrushOld = (HBRUSH)SelectObject(hCDC, hBrushNew); \
         Rectangle(hCDC, cellRect.left, cellRect.top, cellRect.right+1, cellRect.bottom+1); \
         SelectObject(hCDC, hBrushOld); \
         DeleteObject(hBrushNew); \
      } else { \
         const HPEN hPenNew = ::CreatePen(PS_DOT, 1, (m_colorBk == CLR_INVALID) ? ::GetSysColor(COLOR_BTNFACE) : m_colorBk); \
         const HPEN hPenOld = (HPEN)SelectObject(hCDC, hPenNew); \
         MoveToEx(hCDC, cellRect.left +1, cellRect.top   +1, NULL); \
         LineTo  (hCDC, cellRect.left +1, cellRect.bottom-1); \
         LineTo  (hCDC, cellRect.right-1, cellRect.bottom-1); \
         LineTo  (hCDC, cellRect.right-1, cellRect.top   +1); \
         LineTo  (hCDC, cellRect.left +1, cellRect.top   +1); \
         SelectObject(hCDC, hPenOld); \
         DeleteObject(hPenNew); \
      } \
   } \
   DrawCell; \
}

#define DrawCellCaption { \
   Rectangle(hCDC, cellRect.left, cellRect.top, cellRect.right+1, cellRect.bottom+1); \
   { \
      const HPEN hPenB = CreatePen(PS_SOLID, 1, 0); \
      const HPEN hPenW = CreatePen(PS_SOLID, 1, 0xFFFFFF); \
      const HBRUSH hPenOld = (HBRUSH)SelectObject(hCDC, hPenW); \
      MoveToEx(hCDC, cellRect.left +1, cellRect.bottom-1, NULL); \
      LineTo  (hCDC, cellRect.left +1, cellRect.top   +1); \
      LineTo  (hCDC, cellRect.right-1, cellRect.top   +1); \
      SelectObject(hCDC, hPenB); \
      LineTo  (hCDC, cellRect.right-1, cellRect.bottom-1); \
      LineTo  (hCDC, cellRect.left +1, cellRect.bottom-1); \
      SelectObject(hCDC, hPenOld); \
      DeleteObject(hPenB); \
      DeleteObject(hPenW); \
   } \
   DrawCell;\
}

void CTable::OnPaint(HWND hWnd) const {
/*   if (wm_size) {
      wm_size = false;
      PAINTSTRUCT PaintStruct;
      BeginPaint(hWnd, &PaintStruct);
      PaintStruct.rcPaint.left   =
      PaintStruct.rcPaint.top    =
      PaintStruct.rcPaint.right  =
      PaintStruct.rcPaint.bottom = 0;
      EndPaint  (hWnd, &PaintStruct);
      return;
   }
/**/
   PAINTSTRUCT PaintStruct;
   const HDC hODC = BeginPaint(hWnd, &PaintStruct); // original DC
   const HDC hCDC = CreateCompatibleDC(hODC);
   RECT clientRect = GetClientRect(hWnd);
   const HBITMAP hBmpNew = CreateCompatibleBitmap(hODC, clientRect.right, clientRect.bottom);
   const HBITMAP hBmpOld = (HBITMAP)SelectObject(hCDC, hBmpNew);
   const x = GetScrollPos(hWnd, SB_HORZ);
   const y = GetScrollPos(hWnd, SB_VERT);
   { // фон
      const HBRUSH hBrushNew = CreateSolidBrush(GetSysColor(COLOR_WINDOW));
      const HBRUSH hBrushOld = (HBRUSH)SelectObject(hCDC, hBrushNew);
      PatBlt(hCDC, 0,0, clientRect.right, clientRect.bottom, PATCOPY);
      SelectObject(hCDC, hBrushOld);
      DeleteObject(hBrushNew);
   }
   { // вывод данных на ...
      const HPEN hPenNew = ::CreatePen(PS_SOLID, 1, (m_colorBk == CLR_INVALID) ? ::GetSysColor(COLOR_BTNFACE) : m_colorBk);
      const HPEN hPenOld = (HPEN)SelectObject(hCDC, hPenNew);
      const int oldBkMode = SetBkMode(hCDC, TRANSPARENT); // вывод текста на прозрачном фоне
      const LOGFONT Font = {8,0,0,0,
                            FW_NORMAL,//FW_BOLD,//
                            0,0,0,
                            DEFAULT_CHARSET,
                            OUT_DEFAULT_PRECIS,
                            CLIP_DEFAULT_PRECIS,
                            DEFAULT_QUALITY,
                            DEFAULT_PITCH | FF_DONTCARE,
                            TEXT("MS Sans Serif")};// TEXT("Times New Roman")
      const HFONT hFont = CreateFontIndirect(&Font);
      const HFONT hFontOld = (HFONT)SelectObject(hCDC, hFont);
      { // ... на нестaтичном поле
         for (int i=m_iStaticCol; i<m_ColW.size(); i++)
            for (int j=m_iStaticRow; j<m_RowH.size(); j++) {
               const POINT cellPoint = {i,j};
               const RECT cellRect = {-x+Width(i),-y+Height(j),-x+Width(i+1),-y+Height(j+1)};
               DrawCellField;
         }
      }
      { // ... на стaтичном поле
         const HBRUSH hBrushNew = CreateSolidBrush((m_colorBk == CLR_INVALID) ? ::GetSysColor(COLOR_BTNFACE) : m_colorBk);
         const HBRUSH hBrushOld = (HBRUSH)SelectObject(hCDC, hBrushNew);
         { // верхний заголовок
            for (int i=m_iStaticCol; i<m_ColW.size(); i++)
               for (int j=0; j<min(m_iStaticRow, m_RowH.size()); j++) {
                  const RECT cellRect = {-x+Width(i),Height(j),-x+Width(i+1),Height(j+1)};
                  DrawCellCaption;
               }
         }
         { // левый заголовок
            for (int i=0; i<min(m_iStaticCol, m_ColW.size()); i++)
               for (int j=m_iStaticRow; j<m_RowH.size(); j++) {
                  const RECT cellRect = {Width(i),-y+Height(j),Width(i+1),-y+Height(j+1)};
                  DrawCellCaption;
               }
         }
         { // верхний левый заголовок
            for (int i=0; i<min(m_iStaticCol, m_ColW.size()); i++)
               for (int j=0; j<min(m_iStaticRow, m_RowH.size()); j++) {
                  const RECT cellRect = {Width(i),Height(j),Width(i+1),Height(j+1)};
                  DrawCellCaption;
               }
         }/**/
         SelectObject(hCDC, hBrushOld);
         DeleteObject(hBrushNew);
      }
      MoveToEx(hCDC, -x+Width(m_ColW.size()), -y+0, NULL);
      LineTo  (hCDC, -x+Width(m_ColW.size()), -y+Height(m_RowH.size()));
      LineTo  (hCDC, -x-1                   , -y+Height(m_RowH.size()));
      SelectObject(hCDC, hPenOld);
      DeleteObject(hPenNew);
      SetBkMode(hCDC, oldBkMode);
      SelectObject(hCDC, hFontOld);
      DeleteObject(hFont);
   }
   BitBlt(hODC, PaintStruct.rcPaint.left,
                PaintStruct.rcPaint.top,
                PaintStruct.rcPaint.right -PaintStruct.rcPaint.left,
                PaintStruct.rcPaint.bottom-PaintStruct.rcPaint.top,
          hCDC, PaintStruct.rcPaint.left,
                PaintStruct.rcPaint.top,
          SRCCOPY);
   SelectObject(hCDC, hBmpOld);
   DeleteObject(hBmpNew);
   DeleteDC(hCDC);
   EndPaint(hWnd, &PaintStruct);
   DrawHScroll();
   DrawVScroll();
}

// WM_MOUSEMOVE
void CTable::OnMouseMove(HWND hWnd, int x, int y, UINT keyFlags) {
   if (!(keyFlags & MK_LBUTTON)) return;
   const POINT cell = FindCell(x, y);
   if (cell == INVALID_CELL) return;
   SetCurrentCell(cell.x, cell.y);
}

// WM_LBUTTONDOWN & WM_LBUTTONDBLCLK
void CTable::OnLButtonDown(HWND hWnd, BOOL fDoubleClick, int x, int y, UINT keyFlags) {
   SetFocus(m_hWnd);
   SetCapture(m_hWnd);
   const POINT cell = FindCell(x, y);
   if (cell == INVALID_CELL) return;
   SetCurrentCell(cell.x, cell.y, !!fDoubleClick);
   if (fDoubleClick)
      FORWARD_WM_COMMAND(m_hWndParent, m_iIdControl, m_hWnd, NC_TABLE_SELECT_CELL, PostMessage);
}

// WM_LBUTTONUP
void CTable::OnLButtonUp(HWND hWnd, int x, int y, UINT keyFlags) {
   ReleaseCapture();
}

// WM_RBUTTONDOWN & WM_RBUTTONDBLCLK
void CTable::OnRButtonDown(HWND hWnd, BOOL fDoubleClick, int x, int y, UINT keyFlags) {
   SetFocus(m_hWnd);
}

// WM_SIZE
void CTable::OnSize(HWND hWnd, UINT state, int cx, int cy) {
   RecountScrollInfo();
//   InvalidateRect(m_hWnd, NULL, FALSE);
 //wm_size = true;
 //PostMessage(hWnd, WM_USER+1, cx, cy);
}

// WM_HSCROLL
void CTable::OnHScroll(HWND hWnd, HWND hwndCtl, UINT code, int pos) {
   SCROLLINFO scInfo = {sizeof(SCROLLINFO), SIF_RANGE | SIF_PAGE | SIF_POS};
   GetScrollInfo(hWnd, SB_HORZ, &scInfo);
   switch (code) {
   case SB_TOP          : scInfo.nPos = scInfo.nMin; break;
   case SB_BOTTOM       : scInfo.nPos = scInfo.nMax; break;
   case SB_LINELEFT     : scInfo.nPos = max(scInfo.nMin, scInfo.nPos-scInfo.nPage/10); break;
   case SB_LINERIGHT    : scInfo.nPos = min(scInfo.nMax, scInfo.nPos+scInfo.nPage/10); break;
   case SB_PAGELEFT     : scInfo.nPos = max(scInfo.nMin, scInfo.nPos-scInfo.nPage); break;
   case SB_PAGERIGHT    : scInfo.nPos = min(scInfo.nMax, scInfo.nPos+scInfo.nPage); break;
   case SB_THUMBPOSITION:
   case SB_THUMBTRACK   : scInfo.nPos = pos; break;
   case SB_ENDSCROLL    : break;
   }
   scInfo.fMask = SIF_POS;
   SetScrollInfo(hWnd, SB_HORZ, &scInfo, TRUE);
   RECT clientRect = GetClientRect(hWnd);
   const RECT invalidateRect = {Width(m_iStaticCol), 0, clientRect.right, clientRect.bottom};
   InvalidateRect(m_hWnd, &invalidateRect, FALSE);
}

// WM_VSCROLL
void CTable::OnVScroll(HWND hWnd, HWND hwndCtl, UINT code, int pos) {
   SCROLLINFO scInfo = {sizeof(SCROLLINFO), SIF_RANGE | SIF_PAGE | SIF_POS};
   GetScrollInfo(hWnd, SB_VERT, &scInfo);
   switch (code) {
   case SB_TOP          : scInfo.nPos = scInfo.nMin; break;
   case SB_BOTTOM       : scInfo.nPos = scInfo.nMax; break;
   case SB_LINELEFT     : scInfo.nPos = max(scInfo.nMin, scInfo.nPos-scInfo.nPage/10); break;
   case SB_LINERIGHT    : scInfo.nPos = min(scInfo.nMax, scInfo.nPos+scInfo.nPage/10); break;
   case SB_PAGELEFT     : scInfo.nPos = max(scInfo.nMin, scInfo.nPos-scInfo.nPage); break;
   case SB_PAGERIGHT    : scInfo.nPos = min(scInfo.nMax, scInfo.nPos+scInfo.nPage); break;
   case SB_THUMBPOSITION:
   case SB_THUMBTRACK   : scInfo.nPos = pos; break;
   case SB_ENDSCROLL    : break;
   }
   scInfo.fMask = SIF_POS;
   SetScrollInfo(hWnd, SB_VERT, &scInfo, TRUE);
   RECT clientRect = GetClientRect(hWnd);
   const RECT invalidateRect = {0, Height(m_iStaticRow), clientRect.right, clientRect.bottom};
   InvalidateRect(m_hWnd, &invalidateRect, FALSE);
}

// WM_ERASEBKGND
BOOL CTable::OnEraseBkgnd(HWND hWnd, HDC hdc) {
   return TRUE;
   if ((m_colorBk == CLR_INVALID) || (m_colorBk == ::GetSysColor(COLOR_BTNFACE))) return FALSE;
   return nsEraseBk::OnEraseBkgnd(hWnd, hdc, m_colorBk);
}

// WM_KEYDOWN & WM_KEYUP
void CTable::OnKey(HWND hWnd, UINT vk, BOOL fDown, int cRepeat, UINT flags) {
   //g_Logger.Put(CLogger::LL_DEBUG, "vk = 0x%08X", vk);
   if (fDown) return;
   switch (vk) {
   case VK_LEFT  : SetCurrentCell(m_CurrentCell.x-1, m_CurrentCell.y  ); break;
   case VK_UP    : SetCurrentCell(m_CurrentCell.x  , m_CurrentCell.y-1); break;
   case VK_RIGHT : SetCurrentCell(m_CurrentCell.x+1, m_CurrentCell.y  ); break;
   case VK_DOWN  : SetCurrentCell(m_CurrentCell.x  , m_CurrentCell.y+1); break;
   case VK_HOME  : SetCurrentCell(m_CurrentCell.x  , m_iStaticRow     ); break;
   case VK_END   : SetCurrentCell(m_CurrentCell.x  , m_RowH.size()  -1); break;
   case VK_PRIOR : SetCurrentCell(m_CurrentCell.x  , max(m_iStaticRow   , m_CurrentCell.y-7)); break;
   case VK_NEXT  : SetCurrentCell(m_CurrentCell.x  , min(m_RowH.size()-1, m_CurrentCell.y+7)); break;
   case VK_SPACE : SetCurrentCell(m_CurrentCell.x  , m_CurrentCell.y, !m_bSelect); break;
   case VK_DELETE: FORWARD_WM_COMMAND(m_hWndParent, m_iIdControl, m_hWnd, NC_TABLE_DELETE_CELL, PostMessage); break;
   }
}
