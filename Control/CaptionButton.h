////////////////////////////////////////////////////////////////////////////////
// File name: CaptionButton.h
// Author: Sergey Krivulya (Ce���� �p�����) - KSerg
// e-mail: Sergey_Krivulya@UkrPost.Net
// Date: 19 09 2004
//
//   ���� �������� ���� ������ � ��������� ����? ��� ������ �����!
// �� ��� ���� ������� - ��� ������� ��������� ������ CCaptionButtonText,
// � ����������� �������� ����������: ����� ����, ����� �� ������, �� � ����������
// ������� �� ������ ������ �� ����� ����������. � ��! :)
//   ���� ����� ������ �� ���������� - ����� ���������� ����. ��� ����� ���� �����������
// ���� ����� ���������� CCaptionButtonText, � ������� ����� ������ ����� - ��� ����������
// �-��� ���� CCaptionButton::CREATEBMPBUTTON, ���������� �� ������� ��� ������ �
// (��)������� � (��)��������������� ����������.
//
// ���������:
// 1. �������� ��� ��������� ��������� � ������� �-��� CCaptionButton::GetDefSizeBttn
//    ������� ������ �� ��������� ('�������', '����������', '��������').
//    ���� � ���� ������� ��� ����� XP, �� ����� ����� �������.
// 2. � 'tooltip' ������ ��� �� ����������. ������ ��� ���������� ������ ��������� �,
//    ��������������, ������ ������ '�������'.
// 3. � MDI ������ ��� �� ����������. ����� ������ �������� - ����� ���� ���������,
//    �� ������ �� ��������� ������������� �� ���� �������� ����, ���� ��� (����) ������ ����.
// 4. � WinXP �� ������ XP ������������� ���� ������� ������ ����������� ������ �� ��������� -
//    ������ ���������� �������� (��� � ���������� ����). :(
//
// ����������:
// 1. � ������� ���� ����� ��������� ������ ���� ������. ������ ��� ���������� ����� � �� �����������.
// 2. � WinXP �� ������ XP ����� ����� � ������������ ������, ��� ���� ��� � �����������...
//
// �����������:
// 1. �������� �������������.
//
////////////////////////////////////////////////////////////////////////////////

#ifndef __FILE__CAPTION_BUTTON__
#define __FILE__CAPTION_BUTTON__

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

#pragma warning(disable:4786) // identifier was truncated to '255' characters in the debug information
#include <map>
#ifndef __AFX_H__
   #include <Windows.h>
#endif

class CCaptionButton
{
public:
   static  SIZE GetDefSizeBttn(HWND); // ������� ������ �� ��������� - '�������', '����������', '��������'.
   typedef HBITMAP (*CREATEBMPBUTTON)(HWND, BOOL bDown, BOOL bFocus, LPVOID pParam);
   static  HBITMAP CreateDefBmpButton(HWND, BOOL bDown, BOOL bFocus, LPVOID pParam = NULL); // ������ �-��� CREATEBMPBUTTON - ������ ������ ������
   typedef VOID (*ONCLICK)(LPVOID pParam);
private:
   static LRESULT CALLBACK WndProc(HWND, UINT, WPARAM, LPARAM);
   static void DrawButton(HWND, BOOL bDown, BOOL bFocus);
   struct CContext {
      WNDPROC         m_pWndProc;
      CREATEBMPBUTTON m_pCreateBmpFunc;
      LPVOID          m_pCreateBmpParam;
      BOOL            m_bAutoDelBmp;
      ONCLICK         m_pClickFunc;
      LPVOID          m_pClickParam;
      BOOL            m_bCapture;
      BOOL            m_bCaption;
      CContext(WNDPROC pWndProc, CREATEBMPBUTTON pCreateBmpFunc, LPVOID pCreateBmpParam, BOOL bAutoDelBmp, ONCLICK pClickFunc, LPVOID pClickParam, BOOL bCaption):
         m_pWndProc       (pWndProc),
         m_pCreateBmpFunc (pCreateBmpFunc ? pCreateBmpFunc : CreateDefBmpButton),
         m_pCreateBmpParam(pCreateBmpParam),
         m_bAutoDelBmp    (bAutoDelBmp),
         m_pClickFunc     (pClickFunc),
         m_pClickParam    (pClickParam),
         m_bCapture       (FALSE),
         m_bCaption       (bCaption) {}
   };
   typedef std::map<HWND, CContext*> MAP_Bttn;
   static MAP_Bttn m_Map;
   static RECT GetBttnRect (HWND hWnd);
   static BOOL CursorInBttn(HWND hWnd);
public:
   static BOOL Create(
      // ����� ����, � ��������� �������� ����������� ������.
      HWND hWnd,

      // ��������� �� ���������������� �-���, ������� ���������� HBITMAP - ������� ������.
      // ���� NULL, �� �������� ������ ������ (� ������� CCaptionButton::CreateDefBmpButton()).
      CREATEBMPBUTTON pCreateBmpFunc,

      // ��������� �� ������ ������������. ����� ��������� � �-��� CREATEBMPBUTTON.
      LPVOID pCreateBmpParam,

      // ������� �� HBITMAP (��������� �� �-��� CREATEBMPBUTTON) ����� ��� �������������?
      BOOL bAutoDelBmp,

      // ��������� �� ���������������� �-���-���������� �����.
      ONCLICK pClickFunc,

      // ��������� �� ������ ������������. ����� ��������� � �-���-���������� �����.
      LPVOID pClickParam
   );
   static BOOL Delete(HWND hWnd);
};

class CCaptionButtonText {
private:
   HWND m_hWnd;
   PTSTR m_szText;
   static HBITMAP CreateBmpButtonText(HWND, BOOL bDown, BOOL bFocus, LPVOID pParam);
public:
   CCaptionButtonText(
      // ����� ����, � ��������� �������� ����������� ������.
      HWND,

      // ����� �� ������
      LPCTSTR szTextBttn,

      // ��������� �� ���������������� �-���-���������� �����.
      CCaptionButton::ONCLICK pClickFunc,

      // ��������� �� ������ ������������. ����� ��������� � �-���-���������� �����.
      LPVOID pClickParam
   );
  ~CCaptionButtonText();
};

#endif // __FILE__CAPTION_BUTTON__
