#include "stdafx.h"
#include "FadeBtn.h"

#ifdef _DEBUG
#define new DEBUG_NEW
#undef THIS_FILE
static char THIS_FILE[] = __FILE__;
#endif

class CColor
{
public:
   union
   {
      struct
      {
         BYTE b0;
         BYTE b1;
         BYTE b2;
         BYTE b3;
      } bytes;
      COLORREF col;
   } m_color;

   CColor(COLORREF col)
   {
      m_color.col = col;
   }
};


/////////////////////////////////////////////////////////////////////////////
// CFadingButton

CFadingButton::CFadingButton()
{
   m_colorInactive = GetSysColor(COLOR_BTNFACE);
   m_colorActive = GetSysColor(COLOR_BTNFACE);

   CColor colI(m_colorInactive);
   CColor colA(m_colorActive);

   colA.m_color.bytes.b0 = BYTE(float(colI.m_color.bytes.b0) * float(1.2));
   colA.m_color.bytes.b1 = BYTE(float(colI.m_color.bytes.b1) * float(1.2));
   colA.m_color.bytes.b2 = BYTE(float(colI.m_color.bytes.b2) * float(1.2));
   colA.m_color.bytes.b3 = BYTE(float(colI.m_color.bytes.b3) * float(1.2));

   m_colorActive = colA.m_color.col;
 
   m_nStepsColorChange = 10;
   m_nOneStepMillicecs = 5;
   m_iCurrStep = 0;
   m_bActive = false;
   m_bInitialized = false;
}

CFadingButton::~CFadingButton()
{
}


BEGIN_MESSAGE_MAP(CFadingButton, CButton)
//{{AFX_MSG_MAP(CFadingButton)
ON_WM_TIMER()
ON_WM_MOUSEMOVE()
ON_WM_ERASEBKGND()
//}}AFX_MSG_MAP
END_MESSAGE_MAP()

/////////////////////////////////////////////////////////////////////////////
// CFadingButton message handlers

void CFadingButton::OnTimer(UINT nIDEvent)
{
   if(nIDEvent == 0)
   {
      CPoint pt;
      GetCursorPos(&pt);
      CRect rect;
      GetWindowRect(&rect);
      if(!rect.PtInRect(pt))
         m_bActive = false;
      if(m_bActive && m_iCurrStep < m_nStepsColorChange)
      {
         m_iCurrStep++;
         RedrawWindow();
      }
      if(!m_bActive && m_iCurrStep)
      {
         m_iCurrStep--;
         RedrawWindow();
      }
   }
   CButton::OnTimer(nIDEvent);
}

void CFadingButton::OnMouseMove(UINT nFlags, CPoint point)
{
   m_bActive = true;
   CButton::OnMouseMove(nFlags, point);
}

void CFadingButton::DrawItem(LPDRAWITEMSTRUCT lpDrawItemStruct)
{
   if(!m_bInitialized)
   {
      SetTimer(0, m_nOneStepMillicecs, NULL);
      m_bInitialized = true;
   }

   CDC dc;
   dc.Attach(lpDrawItemStruct->hDC);

   CColor colI(m_colorInactive);
   CColor colA(m_colorActive);
   CColor col(m_colorInactive);

   BYTE &bI0 = colI.m_color.bytes.b0;
   BYTE &bA0 = colA.m_color.bytes.b0;
   BYTE &bI1 = colI.m_color.bytes.b1;
   BYTE &bA1 = colA.m_color.bytes.b1;
   BYTE &bI2 = colI.m_color.bytes.b2;
   BYTE &bA2 = colA.m_color.bytes.b2;
   BYTE &bI3 = colI.m_color.bytes.b3;
   BYTE &bA3 = colA.m_color.bytes.b3;
   col.m_color.bytes.b0 = BYTE(float(bI0) + float(bA0 - bI0) * (float(m_iCurrStep)/float(m_nStepsColorChange)));
   col.m_color.bytes.b1 = BYTE(float(bI1) + float(bA1 - bI1) * (float(m_iCurrStep)/float(m_nStepsColorChange)));
   col.m_color.bytes.b2 = BYTE(float(bI2) + float(bA2 - bI2) * (float(m_iCurrStep)/float(m_nStepsColorChange)));
   col.m_color.bytes.b3 = BYTE(float(bI3) + float(bA3 - bI3) * (float(m_iCurrStep)/float(m_nStepsColorChange)));

   dc.SetBkMode(TRANSPARENT);
   dc.SetTextColor(GetSysColor(COLOR_WINDOWTEXT));
   CRect rect = lpDrawItemStruct->rcItem;
   CString text; GetWindowText(text);
   dc.DrawFrameControl(&rect, DFC_BUTTON, (GetState() & 4) ? DFCS_PUSHED|DFCS_BUTTONPUSH : DFCS_BUTTONPUSH);
   rect.DeflateRect(2,2,2,2);
   dc.FillSolidRect(&rect, (GetState() & 4) ? m_colorActive : col.m_color.col);
   dc.DrawText(text, &rect, DT_SINGLELINE | DT_CENTER | DT_VCENTER);
   dc.Detach();
}

void CFadingButton::ReinitFade(int nOneStepMillisecs, int nStepsColorChange)
{
   KillTimer(0);
   m_iCurrStep = 1;
   m_nOneStepMillicecs = nOneStepMillisecs;
   m_nStepsColorChange = nStepsColorChange;
   SetTimer(0, m_nOneStepMillicecs, NULL);
   RedrawWindow();
}

BOOL CFadingButton::OnEraseBkgnd(CDC* pDC)
{
   return FALSE;
}
