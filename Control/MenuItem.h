////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "MenuItem.h"
////////////////////////////////////////////////////////////////////////////////

#ifndef __FILE__CMENUITEM__
#define __FILE__CMENUITEM__

#ifndef __AFX_H__
   #include <Windows.h>
#endif
#include "../Image.h"

class CMenuItem {
private:
   //   widthImg            widthText          ???
   //  <--------><-------------------------><------>
   //   ____________________...____________________
   //  |         |                 |        |      | /|\
   //  |         |                 |        | |\   |  |
   //  |  pImg   |     szText      | szAcel | |::\ |  | heightItem
   //  |    &    |                 |        | |::/ |  |
   //  |  check  |                 |        | |/   |  |
   //  |_________|__________...____|________|______| \|/

   UINT m_uWidthImg, m_uWidthText, m_uHeightItem;
   const CImage *m_pImg;
   TCHAR *m_szText; // menu text
   UINT m_uFormat; // DrawText() format flags

   static UINT m_uMaxWidthText;
public:
   static UINT offsetText() {return 2;}
  ~CMenuItem();
   CMenuItem(){}
   CMenuItem(const TCHAR *szText,
             UINT widht_Img, UINT widht_Text, UINT height_Item,
             const CImage *pImg = NULL,
             UINT format = DT_LEFT | DT_VCENTER | DT_SINGLELINE | DT_EXPANDTABS);
   void SetSize(UINT width_Img, UINT width_Text, UINT height_Item) {
      m_uWidthImg     = width_Img;
      m_uWidthText    = width_Text;
      m_uHeightItem   = height_Item;
      m_uMaxWidthText = max(m_uMaxWidthText, m_uWidthText);
   }
   void Draw (const DRAWITEMSTRUCT &dis) const;
   const TCHAR* GetText() const {return m_szText;}
   void SetText(const TCHAR *szText);

   static CMenuItem* GetInstance(HMENU hMenu, UINT uItem, BOOL bByPosition);

   static void SetMenuOwnerDraw(HMENU hMenu, UINT item_ID, const CImage *pImg); // ������� ��������� �������� ���� �� MFT_OWNERDRAW, �.�. ��� ������ ����� �������������� ��������� ������� CMenuItem
   static BOOL SetMenuText(HMENU hMenu, UINT uItem, BOOL bByPosition, LPCTSTR szNewText);
   static void OnMeasureItem(MEASUREITEMSTRUCT *lpMeasureItem); // WM_MEASUREITEM
   static void OnDrawItem(const DRAWITEMSTRUCT *lpDrawItem);    // WM_DRAWITEM
};

/*
��� ��������:
1. ���������� ���� �������� ���������.
2. ������ ����� ���� ������� a �-��� SetMenuOwnerDraw()
3. ��������� �� ������������ ����� (�-��� SetMenuText()).
   ���������� �� ������� ������� WM_MEASUREITEM.
4. ��������� ������� WM_MEASUREITEM � WM_DRAWITEM ���������������
   �� OnMeasureItem � �� OnDrawItem ��������������.
/**/
#endif // __FILE__TMENUITEM__
