#if !defined(AFX_FADINGBUTTON_H__52AF83EF_3954_11D8_934D_444553540000__INCLUDED_)
#define AFX_FADINGBUTTON_H__52AF83EF_3954_11D8_934D_444553540000__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

/////////////////////////////////////////////////////////////////////////////
// CFadingButton window

class CFadingButton : public CButton
{
public:
   COLORREF m_colorActive;
   COLORREF m_colorInactive;
   int  m_nStepsColorChange;
   int  m_nOneStepMillicecs;

private:
   int  m_iCurrStep;
   bool  m_bActive;
   bool  m_bInitialized;

   // Construction
public:
   CFadingButton();

   // Operations
public:
   void ReinitFade(int nOneStepMillisecs, int nStepsColorChange);

   // Overrides
   // ClassWizard generated virtual function overrides
   //{{AFX_VIRTUAL(CFadingButton)
   public:
   virtual void DrawItem(LPDRAWITEMSTRUCT lpDrawItemStruct);
   //}}AFX_VIRTUAL

   // Implementation
public:
   virtual ~CFadingButton();

   // Generated message map functions
protected:
   //{{AFX_MSG(CFadingButton)
   afx_msg void OnTimer(UINT nIDEvent);
   afx_msg void OnMouseMove(UINT nFlags, CPoint point);
   afx_msg BOOL OnEraseBkgnd(CDC* pDC);
   //}}AFX_MSG

   DECLARE_MESSAGE_MAP()
};

/////////////////////////////////////////////////////////////////////////////

//{{AFX_INSERT_LOCATION}}
// Microsoft Visual C++ will insert additional declarations immediately before the previous line.

#endif // !defined(AFX_FADINGBUTTON_H__52AF83EF_3954_11D8_934D_444553540000__INCLUDED_)
