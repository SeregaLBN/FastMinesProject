////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "Info.cpp"
// обработка диалогового окна "Info"
////////////////////////////////////////////////////////////////////////////////
#include ".\Info.h"
#include <windowsx.h>
#include <fstream>
#include <tchar.h>
#include "..\ID_resource.h"

#ifdef USE_INFO_DIALOG

////////////////////////////////////////////////////////////////////////////////
//                          implementation namespace
////////////////////////////////////////////////////////////////////////////////
namespace nsInfo {

HWND hDlg;

#define sizebuff 700//2000//0x4000//
#define GOTOENDLINE FORWARD_WM_VSCROLL(GetDlgItem(hDlg, ID_DIALOG_INFO_EDIT), NULL, SB_BOTTOM, 0, SendMessage)

TCHAR str_edit[sizebuff];
const TCHAR endline[2] = {13, 10};

#ifdef  UNICODE
   std::ofstream  logFile(     "log_file.txt");
#else
   std::wofstream logFile(TEXT("log_file.txt"));
#endif

void LogFilePut(LPCTSTR addStr) {
   for (int i=0; addStr[i]; i++)
      logFile.put(addStr[i]);
   logFile.put(endline[0]);
}

void LogFilePut(const bool* vec, const int size) {
   LPTSTR str = new TCHAR[size+1];
   for (int i=0; i<size; i++)
      str[i] = vec[i] ? TEXT('1') : TEXT('0');
   str[size]=TEXT('\0');
   LogFilePut(str);
   delete [] str;
}

void AddString(LPCTSTR prefix, LPCTSTR str, LPCTSTR sufix) {
   if ((_tcslen(str_edit) + 
        _tcslen(prefix  ) +
        _tcslen(str     ) +
        _tcslen(sufix   ) +
        _tcslen(endline ) +
        1) >= sizebuff) str_edit[0] = TEXT('\0');
   _tcscat(str_edit, prefix);
   _tcscat(str_edit, str);
   _tcscat(str_edit, sufix);
   _tcscat(str_edit, endline);
   SendMessage(GetDlgItem(hDlg, ID_DIALOG_INFO_EDIT), WM_SETTEXT, 0L, (LPARAM)str_edit);
   GOTOENDLINE;
}

void AddValue(LPCTSTR str, const int value, const int radix) {
   TCHAR strbuf[64] = {TEXT('\0')};
   _itot(value, strbuf, radix);
   if ((_tcslen(str_edit) + 
        _tcslen(str     ) +
        _tcslen(strbuf  ) +
        _tcslen(endline ) +
        1) >= sizebuff) str_edit[0] = TEXT('\0');
   _tcscat(str_edit, str);
   _tcscat(str_edit, strbuf);
   _tcscat(str_edit, endline);
   SendMessage(GetDlgItem(hDlg, ID_DIALOG_INFO_EDIT), WM_SETTEXT, 0L, (LPARAM)str_edit);
   GOTOENDLINE;
}

void AddValue(LPCTSTR str, const POINT value, const int radix) {
   TCHAR strbufX[64] = {TEXT('\0')};
   TCHAR strbufY[64] = {TEXT('\0')};
   _itot(value.x, strbufX, radix);
   _itot(value.y, strbufY, radix);
   if ((_tcslen(str_edit ) + 
        _tcslen(str      ) +
        _tcslen(strbufX  ) +
        _tcslen(TEXT(",")) +
        _tcslen(strbufY  ) +
        _tcslen(endline  ) +
        1) >= sizebuff) str_edit[0] = TEXT('\0');
   _tcscat(str_edit, str);
   _tcscat(str_edit, strbufX);
   _tcscat(str_edit, TEXT(","));
   _tcscat(str_edit, strbufY);
   _tcscat(str_edit, endline);
   SendMessage(GetDlgItem(hDlg, ID_DIALOG_INFO_EDIT), WM_SETTEXT, 0L, (LPARAM)str_edit);
   GOTOENDLINE;
}

void AddValue(LPCTSTR str, const POINT value1, const POINT value2, const int radix) {
   TCHAR strbufX1[64] = {TEXT('\0')};
   TCHAR strbufY1[64] = {TEXT('\0')};
   TCHAR strbufX2[64] = {TEXT('\0')};
   TCHAR strbufY2[64] = {TEXT('\0')};
   _itot(value1.x, strbufX1, radix);
   _itot(value1.y, strbufY1, radix);
   _itot(value2.x, strbufX2, radix);
   _itot(value2.y, strbufY2, radix);
   if ((_tcslen(str_edit  ) + 
        _tcslen(str       ) +
        _tcslen(strbufX1  ) +
        _tcslen(TEXT(",") ) +
        _tcslen(strbufY1  ) +
        _tcslen(TEXT("  ")) +
        _tcslen(strbufX2  ) +
        _tcslen(TEXT(",") ) +
        _tcslen(strbufY2  ) +
        _tcslen(endline   ) +
        1) >= sizebuff) str_edit[0] = TEXT('\0');
   _tcscat(str_edit, str);
   _tcscat(str_edit, strbufX1);
   _tcscat(str_edit, TEXT(","));
   _tcscat(str_edit, strbufY1);
   _tcscat(str_edit, TEXT("  "));
   _tcscat(str_edit, strbufX2);
   _tcscat(str_edit, TEXT(","));
   _tcscat(str_edit, strbufY2);
   _tcscat(str_edit, endline);
   SendMessage(GetDlgItem(hDlg, ID_DIALOG_INFO_EDIT), WM_SETTEXT, 0L, (LPARAM)str_edit);
   GOTOENDLINE;
}

void AddValue(LPCTSTR str, const double value) {
   TCHAR strbuf[32] = {TEXT('\0')};
   _stprintf(strbuf, TEXT("%.7f\0"), value); // _gcvt(value, 7, strbuf);
   if ((_tcslen(str_edit) + 
        _tcslen(str     ) +
        _tcslen(strbuf  ) +
        _tcslen(endline ) +
        1) >= sizebuff) str_edit[0] = TEXT('\0');
   _tcscat(str_edit, str);
   _tcscat(str_edit, strbuf);
   _tcscat(str_edit, endline);
   SendMessage(GetDlgItem(hDlg, ID_DIALOG_INFO_EDIT), WM_SETTEXT, 0L, (LPARAM)str_edit);
   GOTOENDLINE;
}

void AddValue(LPCTSTR str, const bool* value, const int sizeArr) {
   if ((_tcslen(str_edit) + 
        sizeArr*sizeof(TCHAR) +
        _tcslen(endline) +
        1) >= sizebuff) str_edit[0] = TEXT('\0');
   _tcscat(str_edit, str);
   TCHAR *strVal = new TCHAR[sizeArr+1]; strVal[sizeArr] = TEXT('\0');
   for (int i=0; i<sizeArr; i++)
      strVal[i] = value[i] ? TEXT('1') : TEXT('0');
 //LogFilePut(strVal);
   _tcscat(str_edit, strVal);
   _tcscat(str_edit, endline);
   delete [] strVal;
   SendMessage(GetDlgItem(hDlg, ID_DIALOG_INFO_EDIT), WM_SETTEXT, 0L, (LPARAM)str_edit);
   GOTOENDLINE;
}

void ClearEdit() {
   str_edit[0] = TEXT('\0');
   SendMessage(GetDlgItem(hDlg, ID_DIALOG_INFO_EDIT), WM_SETTEXT, 0L, (LPARAM)str_edit);
}

const LOGFONT CLogFont =
  {15, //10, //12, //
   0,0,0,
   FW_REGULAR,//FW_BOLD,
   0,
   0,0,
   DEFAULT_CHARSET,
   OUT_DEFAULT_PRECIS,
   CLIP_DEFAULT_PRECIS,
   DEFAULT_QUALITY,
   DEFAULT_PITCH | FF_DONTCARE,
   TEXT("Courier")};

// WM_INITDIALOG
BOOL Cls_OnInitDialog(HWND hwnd, HWND hwndFocus, LPARAM lParam) {
   MoveWindow(hwnd, 0,GetSystemMetrics(SM_CYSCREEN)-10,200,10, FALSE);
   //MoveWindow(hwnd, 0,0,200,GetSystemMetrics(SM_CYSCREEN), FALSE);
   hDlg = hwnd;
   {
      HWND hEdit= GetDlgItem(hwnd, ID_DIALOG_INFO_EDIT);
      FORWARD_WM_SETFONT(hEdit, CreateFontIndirect(&CLogFont), TRUE, SendMessage);
      FORWARD_WM_SETFONT(hEdit, CreateFontIndirect(&CLogFont), TRUE, SendMessage);
   }
   return TRUE;
}

// WM_SIZE
void Cls_OnSize(HWND hwnd, UINT state, int cx, int cy){
   MoveWindow(GetDlgItem(hwnd, ID_DIALOG_INFO_EDIT),
      0, 0, cx-1, cy-1, TRUE);
}

// WM_CLOSE
void Cls_OnClose(HWND hwnd){
   EndDialog(hwnd, 0);
}

BOOL CALLBACK DialogProc(HWND hDlg, UINT msg, WPARAM wParam, LPARAM lParam){
   switch (msg){
   HANDLE_MSG(hDlg, WM_INITDIALOG, Cls_OnInitDialog);
   HANDLE_MSG(hDlg, WM_SIZE      , Cls_OnSize);
   //HANDLE_MSG(hDlg, WM_CLOSE     , Cls_OnClose);
   }
   return FALSE;
}

HWND GetHandle(int id) {
   if (id == 0) return hDlg;
   return GetDlgItem(hDlg, id);
}

} // namespace nsInfo

#endif // USE_INFO_DIALOG
