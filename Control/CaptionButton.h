////////////////////////////////////////////////////////////////////////////////
// File name: CaptionButton.h
// Author: Sergey Krivulya (Ceргей Кpивуля) - KSerg
// e-mail: Sergey_Krivulya@UkrPost.Net
// Date: 19 09 2004
//
//   Надо добавить свою кнопку к заголовку окна? Нет ничего проще!
// Всё что надо сделать - это создать экземпляр класса CCaptionButtonText,
// в конструктор которого передаются: хэндл окна, текст на кнопку, ну и обработчик
// нажатия на кнопку вместе со своим параметром. И всё! :)
//   Если такая кнопка не устраивает - можно нарисовать свою. Для этого надо реализовать
// свой класс аналогично CCaptionButtonText, в котором самая важная часть - это реализация
// ф-ции типа CCaptionButton::CREATEBMPBUTTON, отвечающая за внешний вид кнопки в
// (не)нажатом и (не)сфокусированном состояниях.
//
// Недоделки:
// 1. Неуверен что правильно определяю с помощью ф-ции CCaptionButton::GetDefSizeBttn
//    размеры кнопок по умолчанию ('Закрыть', 'Развернуть', 'Свернуть').
//    Если у окна внешний вид стиля XP, то тогда точно неверно.
// 2. С 'tooltip' окнами ещё не разобрался. Незнаю как определить высоту заголовка и,
//    соответственно, размер кнопки 'Закрыть'.
// 3. С MDI окнами ещё не разобрался. Здесь другая проблема - когда окно развёрнуто,
//    то кнопки по умолчанию распологаются на меню главного окна, если оно (меню) вообще есть.
// 4. В WinXP со стилем XP использование этих классов меняет отображение кнопок по умолчанию -
//    кнопки становятся обычными (как у консольных окон). :(
//
// Недостатки:
// 1. К каждому окну можно добавлять только одну кнопку. Просто мне изначально много и не требовалось.
// 2. В WinXP со стилем XP очень плохо с отображением кнопок, как моих так и стандартных...
//
// Достоинства:
// 1. Простота использования.
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
   static  SIZE GetDefSizeBttn(HWND); // размеры кнопок по умолчанию - 'Закрыть', 'Развернуть', 'Свернуть'.
   typedef HBITMAP (*CREATEBMPBUTTON)(HWND, BOOL bDown, BOOL bFocus, LPVOID pParam);
   static  HBITMAP CreateDefBmpButton(HWND, BOOL bDown, BOOL bFocus, LPVOID pParam = NULL); // пример ф-ции CREATEBMPBUTTON - рисует пустую кнопку
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
      // Хэндл окна, к заголовку которого добавляется кнопка.
      HWND hWnd,

      // Указатель на пользовательскую ф-цию, которая возвращает HBITMAP - рисунок кнопки.
      // Если NULL, то рисуется пустая кнопка (с помощью CCaptionButton::CreateDefBmpButton()).
      CREATEBMPBUTTON pCreateBmpFunc,

      // Указатель на данные пользователя. Будет возвращён в ф-цию CREATEBMPBUTTON.
      LPVOID pCreateBmpParam,

      // Удалять ли HBITMAP (полученый из ф-ции CREATEBMPBUTTON) после его использования?
      BOOL bAutoDelBmp,

      // Указатель на пользовательскую ф-цию-обработчик клика.
      ONCLICK pClickFunc,

      // Указатель на данные пользователя. Будет возвращён в ф-цию-обработчик клика.
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
      // Хэндл окна, к заголовку которого добавляется кнопка.
      HWND,

      // Текст на кнопке
      LPCTSTR szTextBttn,

      // Указатель на пользовательскую ф-цию-обработчик клика.
      CCaptionButton::ONCLICK pClickFunc,

      // Указатель на данные пользователя. Будет возвращён в ф-цию-обработчик клика.
      LPVOID pClickParam
   );
  ~CCaptionButtonText();
};

#endif // __FILE__CAPTION_BUTTON__
