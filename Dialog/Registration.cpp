////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "Registration.cpp"
// обработка диалогового окна "Registration Form"
////////////////////////////////////////////////////////////////////////////////
#include ".\Registration.h"
#include <windowsx.h>
#include <commctrl.h>
#include <tchar.h>
#include "..\ID_resource.h"
#include "..\Lib.h"
#include "..\TcMosaic.h"
#include "..\EraseBk.h"

////////////////////////////////////////////////////////////////////////////////
//                             global variables
////////////////////////////////////////////////////////////////////////////////
extern TcMosaic* gpMosaic;
extern HINSTANCE ghInstance;

////////////////////////////////////////////////////////////////////////////////
//                          implementation namespace
////////////////////////////////////////////////////////////////////////////////
namespace nsRegistration {
////////////////////////////////////////////////////////////////////////////////
//                            const & types & variables
////////////////////////////////////////////////////////////////////////////////
HWND hDlg;

struct TsPassword;

#define LENGTH_LOGIN 40                       // с учётом последнего нуль символа!
#define LENGTH_PSWRD (2*sizeof(TsPassword)+1) // с учётом последнего нуль символа!

#define LENGTH_USER_NAME 30
#define LENGTH_COMP_NAME (MAX_COMPUTERNAME_LENGTH+1)

#define VERIFY_MASK(mask) while(!mask){mask=0xFF&rand();} // нужна ненулевая маска

#pragma pack(push, 1)

struct TsInfo{ // общая информация о компе
   SYSTEMTIME      stTime; // время сохранения данных
   SYSTEM_INFO     siInfo; // информация о машине на которой был введён данных
   TCHAR           szCompName[LENGTH_COMP_NAME]; // имя компа
   TCHAR           szUserName[LENGTH_USER_NAME]; // user name
   OSVERSIONINFO   OSVerInfo;
 //HW_PROFILE_INFO hardwareProfile; // information about the current hardware profile for the local computer (NT only)
};

struct TsPassword{
   BYTE login[LENGTH_LOGIN];
   BYTE mask;
   BYTE crc;
};

struct TsDataRegister{
   char   login[LENGTH_LOGIN];
   char   pswrd[LENGTH_PSWRD];
   TsInfo info;
   BYTE   mask; // random
   BYTE   crc;  // контрольная сумма
};

#pragma pack(pop)

#ifdef DEBUG_REGISTRATION
   const char CFreeLogin[LENGTH_LOGIN] = {'q'-0xF, '\0'};
   const char CFreePswrd[LENGTH_PSWRD] = {'q'-0xF, '\0'};
#else // DEBUG_REGISTRATION
   const char CFreeLogin[LENGTH_LOGIN] = { // Гражданин страны бывшего СССР
      'Г'-0xF, 'р'-0xF, 'а'-0xF, 'ж'-0xF, 'д'-0xF, 'а'-0xF, 'н'-0xF, 'и'-0xF, 'н'-0xF, ' '-0xF,
      'с'-0xF, 'т'-0xF, 'р'-0xF, 'а'-0xF, 'н'-0xF, 'ы'-0xF, ' '-0xF,
      'б'-0xF, 'ы'-0xF, 'в'-0xF, 'ш'-0xF, 'е'-0xF, 'г'-0xF, 'о'-0xF, ' '-0xF,
      'С'-0xF, 'С'-0xF, 'С'-0xF, 'Р'-0xF, '\0'};
   const char CFreePswrd[LENGTH_PSWRD] = { // Регистрация Фастминёра версии 2тчк10
      'Р'-0xF, 'е'-0xF, 'г'-0xF, 'и'-0xF, 'с'-0xF, 'т'-0xF, 'р'-0xF, 'а'-0xF, 'ц'-0xF, 'и'-0xF, 'я'-0xF, ' '-0xF,
      'Ф'-0xF, 'а'-0xF, 'с'-0xF, 'т'-0xF, 'м'-0xF, 'и'-0xF, 'н'-0xF, 'ё'-0xF, 'р'-0xF, 'а'-0xF, ' '-0xF,
      'в'-0xF, 'е'-0xF, 'р'-0xF, 'с'-0xF, 'и'-0xF, 'и'-0xF, ' '-0xF,
      '2'-0xF, 'т'-0xF, 'ч'-0xF, 'к'-0xF, '1'-0xF, '0'-0xF,
      '\0'};
#endif // DEBUG_REGISTRATION

const TCHAR CNameFileInSystemDirectory[] = TEXT("\\fmsys.sys");
const TCHAR CNameFileInFMinesDirectory[] = TEXT("register.lck");

////////////////////////////////////////////////////////////////////////////////
//                           forward declaration
////////////////////////////////////////////////////////////////////////////////
bool RegistrationNew  ();
bool RegistrationFunc (const char* const, const char* const);
bool RegistrationFree (const char* const, const char* const);
void RegistrationSave (const char* const, const char* const);
void RegDataLock      (      TsDataRegister&);
bool RegDataUnLock    (      TsDataRegister&);
void RegDataMasked    (      TsDataRegister&);
void RegDataUnmasked  (      TsDataRegister&);
bool RegDataSaveInFile(const TsDataRegister&, LPCTSTR const,              bool captionFile);
bool RegDataLoadFromFile(    TsDataRegister&, LPCTSTR const, SYSTEMTIME&, bool captionFile);
#ifdef DEBUG_REGISTRATION
   void FindPassword();
   void FindLogin();
#endif // DEBUG_REGISTRATION
BYTE CreateMask();

BOOL Cls_OnInitDialog(HWND, HWND, LPARAM);    // WM_INITDIALOG
void Cls_OnCommand   (HWND, int, HWND, UINT); // WM_COMMAND
void Cls_OnClose     (HWND);                  // WM_CLOSE
#ifdef REPLACEBKCOLORFROMFILLWINDOW
void Cls_OnPaint     (HWND);                  // WM_PAINT
BOOL Cls_OnEraseBkgnd(HWND, HDC);             // WM_ERASEBKGND
#endif // REPLACEBKCOLORFROMFILLWINDOW

////////////////////////////////////////////////////////////////////////////////
//                              implementation
////////////////////////////////////////////////////////////////////////////////
#ifdef REPLACEBKCOLORFROMFILLWINDOW
WNDPROC_STATIC(ID_DIALOG_REGFORM_STATIC_LOGIN   ,       gpMosaic->GetSkin())
WNDPROC_STATIC(ID_DIALOG_REGFORM_STATIC_PASSWORD,       gpMosaic->GetSkin())
WNDPROC_STATIC(ID_DIALOG_REGFORM_EDIT_LOGIN     ,       gpMosaic->GetSkin())
WNDPROC_STATIC(ID_DIALOG_REGFORM_EDIT_PASSWORD  ,       gpMosaic->GetSkin())
WNDPROC_BUTTON(IDOK                             , hDlg, gpMosaic->GetSkin())
WNDPROC_BUTTON(IDCANCEL                         , hDlg, gpMosaic->GetSkin())
#endif // REPLACEBKCOLORFROMFILLWINDOW

////////////////////////////////////////////////////////////////////////////////
//                              dialog procedure
////////////////////////////////////////////////////////////////////////////////
BOOL CALLBACK DialogProc(HWND hDlg, UINT msg, WPARAM wParam, LPARAM lParam){
   switch (msg){
   HANDLE_MSG(hDlg, WM_INITDIALOG, Cls_OnInitDialog);
   HANDLE_MSG(hDlg, WM_COMMAND   , Cls_OnCommand);
   HANDLE_MSG(hDlg, WM_CLOSE     , Cls_OnClose);
#ifdef REPLACEBKCOLORFROMFILLWINDOW
 //HANDLE_MSG(hDlg, WM_PAINT     , Cls_OnPaint);
   HANDLE_MSG(hDlg, WM_ERASEBKGND, Cls_OnEraseBkgnd);
#endif // REPLACEBKCOLORFROMFILLWINDOW
   HANDLE_WM_CTLCOLOR(hDlg);
   }
   return FALSE;
}

////////////////////////////////////////////////////////////////////////////////
//                           обработчики сообщений 
////////////////////////////////////////////////////////////////////////////////
// WM_INITDIALOG
BOOL Cls_OnInitDialog(HWND hwnd, HWND hwndFocus, LPARAM lParam) {
   hDlg = hwnd;

#ifdef REPLACEBKCOLORFROMFILLWINDOW
   SETNEWWNDPROC(hwnd, ID_DIALOG_REGFORM_STATIC_LOGIN   );
   SETNEWWNDPROC(hwnd, ID_DIALOG_REGFORM_STATIC_PASSWORD);
 //SETNEWWNDPROC(hwnd, ID_DIALOG_REGFORM_EDIT_LOGIN     );
 //SETNEWWNDPROC(hwnd, ID_DIALOG_REGFORM_EDIT_PASSWORD  );
   SETNEWWNDPROC(hwnd, IDCANCEL                         );
   SETNEWWNDPROC(hwnd, IDOK                             );
#endif // REPLACEBKCOLORFROMFILLWINDOW

   SetWindowText(GetDlgItem(hwnd, ID_DIALOG_REGFORM_EDIT_PASSWORD), TEXT(""));
   char login[LENGTH_LOGIN];
   char pswrd[LENGTH_PSWRD];
   if (isRegister(login, pswrd)) {
      SetWindowTextA(GetDlgItem(hwnd, ID_DIALOG_REGFORM_EDIT_LOGIN   ), login);
      for (int i=0; pswrd[i]; i++) pswrd[i] = '*';
      SetWindowTextA(GetDlgItem(hwnd, ID_DIALOG_REGFORM_EDIT_PASSWORD), pswrd);
      EnableWindow(GetDlgItem(hwnd, ID_DIALOG_REGFORM_EDIT_LOGIN   ), FALSE);
      EnableWindow(GetDlgItem(hwnd, ID_DIALOG_REGFORM_EDIT_PASSWORD), FALSE);
   }

   return TRUE;
}

// WM_COMMAND
void Cls_OnCommand(HWND hwnd, int id, HWND hwndCtl, UINT codeNotify) {
   switch (id) {
#ifdef DEBUG_REGISTRATION
   case ID_DIALOG_REGFORM_BUTTON_CREATELOGIN:
      FindLogin();
      return;
   case ID_DIALOG_REGFORM_BUTTON_CREATEPASSWORD:
      FindPassword();
      return;
#endif // DEBUG_REGISTRATION
   case IDOK:
      if (!IsWindowEnabled(GetDlgItem(hwnd, ID_DIALOG_REGFORM_EDIT_LOGIN))) { // isRegister(NULL, NULL)
         SendMessage(hwnd, WM_CLOSE, 0L, 0L);
         return;
      }
      if (RegistrationNew()) {
         MessageBox(hwnd, TEXT("Registration completed successfully"), TEXT("Registration result"), MB_OK | MB_ICONINFORMATION);
         SendMessage(hwnd, WM_CLOSE, 0L, 0L);
      } else
         MessageBox(hwnd, TEXT("Sorry. Not registered"), TEXT("Registration result"), MB_OK | MB_ICONERROR);
      return;
   case IDCANCEL:
      SendMessage(hwnd, WM_CLOSE, 0L, 0L);
   }
}

#ifdef REPLACEBKCOLORFROMFILLWINDOW
// WM_PAINT
void Cls_OnPaint(HWND hwnd){
   DefWindowProc(hwnd, WM_PAINT, 0L, 0L);
   if (gpMosaic->GetSkin().toAll)
      nsEraseBk::FillWnd(hwnd, gpMosaic->GetSkin().colorBk, false);
}

// WM_ERASEBKGND
BOOL Cls_OnEraseBkgnd(HWND hwnd, HDC hdc) {
   if (!gpMosaic->GetSkin().toAll)
      return FALSE; // DefWindowProc(hwnd, WM_ERASEBKGND, (WPARAM)hdc, 0L);
   return nsEraseBk::Cls_OnEraseBkgnd(hwnd, hdc, gpMosaic->GetSkin().colorBk);
}
#endif // REPLACEBKCOLORFROMFILLWINDOW

// WM_CLOSE
void Cls_OnClose(HWND hwnd){
   EndDialog(hwnd, 0);
}

////////////////////////////////////////////////////////////////////////////////
//                             other function
////////////////////////////////////////////////////////////////////////////////
char* ValToStr(BYTE *array, int size) { // value to string
   static char buff[1024];
   char etalon[] = "0123456789ABCDEF";
   char *p = buff;

   while (size--) {
      *p++ = etalon[(*array  ) >> 4];
      *p++ = etalon[(*array++) &  0xF];
   }
   *p = 0;
   return buff;
}

BYTE* StrToVal(const char* array, int size) { // string to value
   static BYTE buff[1024];
   BYTE *p = buff;

   while (size--) {
      *p    = (*array>64 ? *array-55 : *array-48) << 4;
      *array++;
      *p++ |=  *array>64 ? *array-55 : *array-48;
      *array++;
   }
   return buff;
}

#ifdef DEBUG_REGISTRATION
void FindPassword() {
   char login[LENGTH_LOGIN];
   GetWindowTextA(GetDlgItem(hDlg, ID_DIALOG_REGFORM_EDIT_LOGIN), login, LENGTH_LOGIN);
   { // записываю случайные (ненужные) данные после нулевого символа
      int i;
      for (i=0; login[i]      ; i++);
      for (i++; i<LENGTH_LOGIN; i++)
         login[i] = 0xFF & rand();
   }
   TsPassword sPassword;
   { // создаю маску, маскирую пароль, подсчитываю crc
      sPassword.mask = 0xFF & rand();
      VERIFY_MASK(sPassword.mask)
      sPassword.crc = 0;
      for (int i=0; i<LENGTH_LOGIN; i++) {
         sPassword.login[i] = login[i] ^ sPassword.mask;
         sPassword.crc ^= sPassword.login[i];
      }
      sPassword.crc ^= sPassword.mask;
   }
   SetWindowTextA(GetDlgItem(hDlg, ID_DIALOG_REGFORM_EDIT_PASSWORD), ValToStr((BYTE*)&sPassword, sizeof(TsPassword)));
}

void FindLogin() {
   char pswrd[LENGTH_PSWRD];
   GetWindowTextA(GetDlgItem(hDlg, ID_DIALOG_REGFORM_EDIT_PASSWORD), pswrd, LENGTH_PSWRD);
   TsPassword sPassword;
   memcpy(&sPassword, StrToVal(pswrd, LENGTH_PSWRD), sizeof(TsPassword));
   char login[LENGTH_LOGIN];
   { // размаскирование пароля, проверка crc
      BYTE crc = sPassword.mask;
      for (int i=0; i<LENGTH_LOGIN; i++) {
         login[i] = sPassword.login[i] ^ sPassword.mask;
         crc ^= sPassword.login[i];
      }
      if (crc != sPassword.crc) {
         SetWindowText(GetDlgItem(hDlg, ID_DIALOG_REGFORM_EDIT_LOGIN), TEXT("CRC Error"));
         return;
      }
   }
   SetWindowTextA(GetDlgItem(hDlg, ID_DIALOG_REGFORM_EDIT_LOGIN), login);
}
#endif // DEBUG_REGISTRATION

bool RegistrationFunc(const char* const cLogin, const char* const cPswrd) {
   if (RegistrationFree(cLogin, cPswrd))
      return true;

   // проверяю - соответствует ли дaнному паролю логин
   TsPassword sPassword;
   memcpy(&sPassword, StrToVal(cPswrd, LENGTH_PSWRD), sizeof(TsPassword));
   char login[LENGTH_LOGIN];
   { // размаскирование пароля, проверка crc
      BYTE crc = sPassword.mask;
      for (int i=0; i<LENGTH_LOGIN; i++) {
         login[i] = sPassword.login[i] ^ sPassword.mask;
         crc ^= sPassword.login[i];
      }
      if (crc != sPassword.crc) return false;
   }

   return !strcmp(cLogin, login);
}

bool RegistrationFree(const char* const cLogin, const char* const cPswrd) {
   char login[LENGTH_LOGIN];   strcpy(login, cLogin);
   char pswrd[LENGTH_PSWRD];   strcpy(pswrd, cPswrd); 
   int i, j;
   for (i=0; login[i]; i++) {
      login[i] -= 0xF;
   }
   for (i=0; pswrd[i]; i++) {
      pswrd[i] -= 0xF;
   }
   i = strcmp(CFreeLogin, login);
   j = strcmp(CFreePswrd, pswrd); 
   return (!i && !j);
}

void RegDataMasked(TsDataRegister& dataRegister) { // маскирование
   *((BYTE*)&dataRegister) ^= dataRegister.mask;
   for (int i=1; i<sizeof(TsDataRegister)-2; i++)
      *((BYTE*)&dataRegister + i) ^= *((BYTE*)&dataRegister + i-1);
}

void RegDataUnmasked(TsDataRegister& dataRegister) { // размаскирование
   for (int i=sizeof(TsDataRegister)-2-1; i>0; i--)
      *((BYTE*)&dataRegister + i) ^= *((BYTE*)&dataRegister + i-1);
   *((BYTE*)&dataRegister) ^= dataRegister.mask;
}

bool RegDataUnLock(TsDataRegister& dataRegister) {
   // подсчёт и сравнение crc
   BYTE crc = 0;
   for (int i=0; i<sizeof(TsDataRegister)-1; i++)
      crc ^= *((BYTE*)&dataRegister+i);
   if (crc != dataRegister.crc) return false; // crc неверен

   RegDataUnmasked(dataRegister); // размаскирование
   return true; // crc верен
}

void RegDataLock(TsDataRegister& dataRegister) {
   dataRegister.mask = CreateMask();
   RegDataMasked(dataRegister); // маскирование
   { // подсчёт crc
      dataRegister.crc = 0;
      for (int i=0; i<sizeof(TsDataRegister)-1; i++)
         dataRegister.crc ^= *((BYTE*)&dataRegister+i);
   }
   dataRegister.crc ^= 0;
}

bool RegDataSaveInFile(const TsDataRegister& dataRegister, LPCTSTR const szFullFileName, bool captionFile) {
   bool result = false;
/*   if (!SetFileAttributes(szFullFileName, FILE_ATTRIBUTE_NORMAL)) {
      DWORD errCode = GetLastError();
   }/**/
   if (!DeleteFile(szFullFileName)) {
      DWORD errCode = GetLastError();
   }
   HANDLE hFile = CreateFile(
      szFullFileName,
      GENERIC_WRITE,
      0,
      NULL,
      CREATE_ALWAYS,
      FILE_ATTRIBUTE_HIDDEN,
      NULL
   );
   DWORD errCode = GetLastError();
   if (hFile != INVALID_HANDLE_VALUE) {
      DWORD dwNOBW;
      if (captionFile) WriteFile(hFile, &TEXT(ID_VERSIONINFO_VERSION3), sizeof(TEXT(ID_VERSIONINFO_VERSION3)), &dwNOBW, NULL);
      result = !!WriteFile(hFile, &dataRegister,
          sizeof(dataRegister), &dwNOBW, NULL);
      if (result && (sizeof(TsDataRegister) != dwNOBW)) result = false;
      CloseHandle(hFile);
   }
   return result;
}

bool RegDataLoadFromFile(TsDataRegister& dataRegister, LPCTSTR const szFullFileName, SYSTEMTIME& stCreationTime, bool captionFile) {
   bool result = false;

   HANDLE hFile = CreateFile(
      szFullFileName,
      GENERIC_READ,
      0,
      NULL,
      OPEN_EXISTING,
      FILE_ATTRIBUTE_HIDDEN,
      NULL
   );
   if (hFile != INVALID_HANDLE_VALUE) {
      DWORD dwNOBR;
      if (captionFile) {
         TCHAR version[chDIMOF(TEXT(ID_VERSIONINFO_VERSION3))];
         result = !!ReadFile(hFile, &version, sizeof(TEXT(ID_VERSIONINFO_VERSION3)), &dwNOBR, NULL);
         if (result && (sizeof(TEXT(ID_VERSIONINFO_VERSION3)) != dwNOBR)) {
            CloseHandle(hFile);
            return false;
         }
         if (_tcscmp(TEXT(ID_VERSIONINFO_VERSION3), version)) {
            CloseHandle(hFile);
            return false;
         }
      }
      result = !!ReadFile(hFile, &dataRegister, sizeof(TsDataRegister), &dwNOBR, NULL);
      if (result && (sizeof(TsDataRegister) != dwNOBR)) result = false;
      if (result) {
         FILETIME ftCreationTime, ftLastAccessTime, ftLastWriteTime;
         result = !!GetFileTime(hFile, &ftCreationTime, &ftLastAccessTime, &ftLastWriteTime);
         CloseHandle(hFile);
         if(result) 
            result = !!FileTimeToSystemTime(&ftCreationTime, &stCreationTime);
      }
   } else return false;
   return result;
}

void RegistrationSave(const char* const cLogin, const char* const cPswrd) {
   TsInfo info;
   {
      DWORD dwCompLen = LENGTH_COMP_NAME;
      DWORD dwUserLen = LENGTH_USER_NAME;
      GetSystemTime      (&info.stTime);                 // время сохранения данных
      GetSystemInfo      (&info.siInfo);                 // информация о машине на которой был введён данных
      GetComputerName    ( info.szCompName, &dwCompLen); // имя компа
      GetUserName        ( info.szUserName, &dwUserLen); // user name
      { // записываю случайные (ненужные) данные после нулевого символа
         int i;
         for (i=0; info.szCompName[i]; i++);
         for (i++; i<LENGTH_COMP_NAME; i++)
            info.szCompName[i] = 0xFF & rand();
         for (i=0; info.szUserName[i]; i++);
         for (i++; i<LENGTH_USER_NAME; i++)
            info.szUserName[i] = 0xFF & rand();
         for (i=0; i<info.OSVerInfo.szCSDVersion[i]; i++)
         for (i++; i<128; i++)
            info.OSVerInfo.szCSDVersion[i] = 0xFF & rand();
      }
      GetVersionEx       (&info.OSVerInfo);
      //GetCurrentHwProfile(&info.hardwareProfile);        // information about the current hardware profile for the local computer (NT only)
   }
   TsDataRegister dataRegister;
   memcpy( dataRegister.login, cLogin, LENGTH_LOGIN);
   memcpy( dataRegister.pswrd, cPswrd, LENGTH_PSWRD);
   memcpy(&dataRegister.info    , &info    , sizeof(TsInfo));
   { // записываю случайные (ненужные) данные после нулевого символа
      int i;
      for (i=0; dataRegister.login[i]; i++);
      for (i++; i<LENGTH_LOGIN; i++)
         dataRegister.login[i] = 0xFF & rand();
      for (i=0; dataRegister.pswrd[i]; i++);
      for (i++; i<LENGTH_PSWRD; i++)
         dataRegister.pswrd[i] = 0xFF & rand();
   }
   { // файл в системной директории
      TCHAR szPath[MAX_PATH];
      GetSystemDirectory(szPath, MAX_PATH);
      _tcscat(szPath, CNameFileInSystemDirectory);

      TsDataRegister dataSystemDir;
      memcpy(&dataSystemDir, &dataRegister, sizeof(TsDataRegister));
      RegDataLock(dataSystemDir);

      RegDataSaveInFile(dataSystemDir, szPath, false);
   }
   { // файл в стартовой директории
      TCHAR szPath[MAX_PATH];
      GetModuleFileName(ghInstance, szPath, MAX_PATH);
      DelFileFromFullPath(szPath);
      _tcscat(szPath, CNameFileInFMinesDirectory);

      TsDataRegister dataFMinesDir;
      memcpy(&dataFMinesDir, &dataRegister, sizeof(TsDataRegister));
      RegDataLock(dataFMinesDir);

      RegDataSaveInFile(dataFMinesDir, szPath, true);
   }
   { // реестр
      HKEY hKeySoft, hKeyFMines;
      DWORD disposition;
      long res;
      res = RegOpenKeyEx(HKEY_CURRENT_USER, TEXT("SOFTWARE"), 0, KEY_WRITE, &hKeySoft);
      if (res != ERROR_SUCCESS)
         MessageBeep(0);
      RegDeleteKey(hKeySoft, TEXT("FastMines"));
      res = RegCreateKeyEx(hKeySoft, TEXT("FastMines"), 0, TEXT("FastMinesRegClass"), REG_OPTION_NON_VOLATILE, KEY_WRITE, NULL, &hKeyFMines, &disposition);
      if (res != ERROR_SUCCESS)
         MessageBeep(0);

      LPTSTR szVersion = TEXT(ID_VERSIONINFO_VERSION2);
      res = RegSetValueEx(hKeyFMines, TEXT("Version"), 0, REG_SZ, (BYTE * const)szVersion, sizeof(TEXT(ID_VERSIONINFO_VERSION2)));
      if (res != ERROR_SUCCESS)
         MessageBeep(0);

      TsDataRegister dataReestr;
      memcpy(&dataReestr, &dataRegister, sizeof(TsDataRegister));
      RegDataLock(dataReestr);

      res = RegSetValueEx(hKeyFMines, TEXT("Registration"), 0, REG_BINARY, (BYTE * const)&dataReestr, sizeof(TsDataRegister));
      if (res != ERROR_SUCCESS)
         MessageBeep(0);

      res = RegCloseKey(hKeyFMines);
      if (res != ERROR_SUCCESS)
         MessageBeep(0);
      res = RegCloseKey(hKeySoft);
      if (res != ERROR_SUCCESS)
         MessageBeep(0);
   }
}

bool RegistrationNew() {
   char login[LENGTH_LOGIN];
   char pswrd[LENGTH_PSWRD];
   GetWindowTextA(GetDlgItem(hDlg, ID_DIALOG_REGFORM_EDIT_LOGIN   ), login, LENGTH_LOGIN);
   GetWindowTextA(GetDlgItem(hDlg, ID_DIALOG_REGFORM_EDIT_PASSWORD), pswrd, LENGTH_PSWRD);

   Sleep(1000); // задержка против взлома... 1 sec
   bool result = RegistrationFunc(login, pswrd);
   if  (result)  RegistrationSave(login, pswrd);
   return result;
}

bool isRegister(char* inLogin, char* inPswrd) {
   TsDataRegister dataSystemDir;
   TsDataRegister dataFMinesDir;
   TsDataRegister dataReestr;
   SYSTEMTIME stCreateTimeSystemDir;
   SYSTEMTIME stCreateTimeFMinesDir;

   { // файл в системной директории
      TCHAR szPath[MAX_PATH];
      GetSystemDirectory(szPath, MAX_PATH);
      _tcscat(szPath, CNameFileInSystemDirectory);

      if (!RegDataLoadFromFile(dataSystemDir,
                               szPath,
                               stCreateTimeSystemDir, false))
         return false;
      if (!RegDataUnLock(dataSystemDir)) // размаскирование данных
         return false; // crc неверен
   }
   { // файл в стартовой директории
      TCHAR szPath[MAX_PATH];
      GetModuleFileName(ghInstance, szPath, MAX_PATH);
      DelFileFromFullPath(szPath);
      _tcscat(szPath, CNameFileInFMinesDirectory);

      if (!RegDataLoadFromFile(dataFMinesDir,
                               szPath,
                               stCreateTimeFMinesDir, true))
         return false;
      if (!RegDataUnLock(dataFMinesDir)) // размаскирование данных
         return false; // crc неверен
   }
   { // реестр
      HKEY hKeySoft, hKeyFMines;
      long res;
      res = RegOpenKeyEx(HKEY_CURRENT_USER, TEXT("SOFTWARE"), 0, KEY_READ, &hKeySoft);
      if (res != ERROR_SUCCESS) return false;
      res = RegOpenKeyEx(hKeySoft, TEXT("FastMines"), 0, KEY_READ, &hKeyFMines);
      if (res != ERROR_SUCCESS) return false;
      {
         TCHAR szValueName[20]; // "Version"
         DWORD dwSizeValueName=20, dwType=REG_SZ, dwSizeDataReestr=sizeof(TEXT(ID_VERSIONINFO_VERSION2));
         TCHAR szVersion[sizeof(TEXT(ID_VERSIONINFO_VERSION2)) / sizeof(TCHAR)];
         res = RegEnumValue(hKeyFMines, 0, szValueName, &dwSizeValueName, NULL, &dwType, (BYTE * const)&szVersion, &dwSizeDataReestr);
         if (res != ERROR_SUCCESS) return false;
         if (_tcscmp(TEXT(ID_VERSIONINFO_VERSION2), szVersion)) return false;
      }
      {
         TCHAR szValueName[20]; // "Registration"
         DWORD dwSizeValueName=20, dwType=REG_BINARY, dwSizeDataReestr=sizeof(TsDataRegister);
         res = RegEnumValue(hKeyFMines, 1, szValueName, &dwSizeValueName, NULL, &dwType, (BYTE * const)&dataReestr, &dwSizeDataReestr);
         if (res != ERROR_SUCCESS) return false;
      }
      res = RegCloseKey(hKeyFMines);
      if (res != ERROR_SUCCESS)
         MessageBeep(0);
      res = RegCloseKey(hKeySoft);
      if (res != ERROR_SUCCESS)
         MessageBeep(0);
      if (!RegDataUnLock(dataReestr)) // размаскирование данных
         return false; // crc неверен
   }
   { // сравнение данных
/**/
      if (!!memcmp( dataSystemDir.login      ,  dataFMinesDir.login  , LENGTH_LOGIN) ||
          !!memcmp( dataSystemDir.login      ,  dataReestr   .login  , LENGTH_LOGIN) ||
          !!memcmp( dataSystemDir.pswrd      ,  dataFMinesDir.pswrd  , LENGTH_PSWRD) ||
          !!memcmp( dataSystemDir.pswrd      ,  dataReestr   .pswrd  , LENGTH_PSWRD) ||
          !!memcmp(&dataSystemDir.info       , &dataFMinesDir.info   , sizeof(SYSTEMTIME)) ||
          !!memcmp(&dataSystemDir.info       , &dataReestr   .info   , sizeof(SYSTEMTIME)) ||
          !!memcmp(&dataSystemDir.info.stTime, &stCreateTimeSystemDir, sizeof(SYSTEMTIME)-3*sizeof(DWORD)) ||
          !!memcmp(&dataFMinesDir.info.stTime, &stCreateTimeFMinesDir, sizeof(SYSTEMTIME)-3*sizeof(DWORD)) ||
          !RegistrationFunc(dataFMinesDir.login, dataFMinesDir.pswrd)
      ) return false;
/**
      bool r1 = !!memcmp( dataSystemDir.login      ,  dataFMinesDir.login  , LENGTH_LOGIN);
      bool r2 = !!memcmp( dataSystemDir.login      ,  dataReestr   .login  , LENGTH_LOGIN);
      bool r3 = !!memcmp( dataSystemDir.pswrd      ,  dataFMinesDir.pswrd  , LENGTH_PSWRD);
      bool r4 = !!memcmp( dataSystemDir.pswrd      ,  dataReestr   .pswrd  , LENGTH_PSWRD);
      bool r5 = !!memcmp(&dataSystemDir.info       , &dataFMinesDir.info   , sizeof(SYSTEMTIME));
      bool r6 = !!memcmp(&dataSystemDir.info       , &dataReestr   .info   , sizeof(SYSTEMTIME));
      bool r7 = !!memcmp(&dataSystemDir.info.stTime, &stCreateTimeSystemDir, sizeof(SYSTEMTIME)-3*sizeof(DWORD));
      bool r8 = !!memcmp(&dataFMinesDir.info.stTime, &stCreateTimeFMinesDir, sizeof(SYSTEMTIME)-3*sizeof(DWORD));
      bool r9 = !RegistrationFunc(dataFMinesDir.login, dataFMinesDir.pswrd);
      if (r1 ||
          r2 ||
          r3 ||
          r4 ||
          r5 ||
          r6 ||
          r7 ||
          r8 ||
          r9
      ) return false;
/**/
   }
   if (inLogin) strcpy(inLogin, dataSystemDir.login);
   if (inPswrd) strcpy(inPswrd, dataSystemDir.pswrd); 
   return true;
}

BYTE CreateMask() { // нужны неповторяющиеся маски
   #define MAX_MASK 3
   static BYTE mask[MAX_MASK];
   static int index = 0;
   BYTE result = 0xFF & rand();
   VERIFY_MASK(result)
   bool f;
   do {
      f = false;
      for (int i=0; i<=index; i++)
         if (result == mask[i]) {
            f = true;
            result = 0xFF & rand();
            VERIFY_MASK(result)
            continue;
         }
   } while (f);
   index = ++index % MAX_MASK;
   return result;
}

} // namespace nsRegistration
