////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "TcMosaic.cpp"
//
// реализация алгоритма
////////////////////////////////////////////////////////////////////////////////

#include ".\TcMosaic.h"
#include <windowsx.h>
#include <time.h>
#include <tchar.h>
#include ".\TcRobot.h"
#include ".\Figure\TcBase.h"
#include ".\Figure\TcTriangle1.h"
#include ".\Figure\TcTriangle2.h"
#include ".\Figure\TcTriangle3.h"
#include ".\Figure\TcTriangle4.h"
#include ".\Figure\TcSquare1.h"
#include ".\Figure\TcSquare2.h"
#include ".\Figure\TcParquet1.h"
#include ".\Figure\TcParquet2.h"
#include ".\Figure\TcTrapezoid1.h"
#include ".\Figure\TcTrapezoid2.h"
#include ".\Figure\TcTrapezoid3.h"
#include ".\Figure\TcRhombus.h"
#include ".\Figure\TcQuadrangle1.h"
#include ".\Figure\TcPentagon.h"
#include ".\Figure\TcPentagonT5.h"
#include ".\Figure\TcPentagonT10.h"
#include ".\Figure\TcHexagon.h"
#include ".\Figure\TcTrSq.h"
#include ".\Figure\TcTrSq2.h"
#include ".\Figure\TcSqTrHex.h"
#include ".\Dialog\CustomSkill.h"
#include ".\Dialog\Champions.h"
#include ".\Dialog\Registration.h"
#include ".\Dialog\PlayerName.h"
#include ".\Dialog\SelectFigure.h"
#include ".\Dialog\Statistics.h"
#include ".\Dialog\Info.h"

////////////////////////////////////////////////////////////////////////////////
//                            types & constants
////////////////////////////////////////////////////////////////////////////////
const TCHAR szCFileNameInit[] = TEXT("Mines.ini");

const TsSkin CSkinDefault = {{TEXT(""),true,placeStretch},     // Mine
                             {TEXT(""),true,placeStretch},     // Flag
                             {TEXT(""),true,placeCenter},      // Pause
                             {TEXT(""),true,placeStretch},     // Background image
                            {{TEXT(""),true,placeStretch},     // BtnNew[0]
                             {TEXT(""),true,placeStretch},     // BtnNew[1]
                             {TEXT(""),true,placeStretch},     // BtnNew[2]
                             {TEXT(""),true,placeStretch}},    // BtnNew[3]
                            {{TEXT(""),true,placeStretch},     // BtnPause[0]
                             {TEXT(""),true,placeStretch},     // BtnPause[1]
                             {TEXT(""),true,placeStretch},     // BtnPause[2]
                             {TEXT(""),true,placeStretch}},    // BtnPause[3]
                             GetSysColor(COLOR_BTNFACE), // Background color
                             false,                      // toAll
                             {0,0,0,0,                   // Font
                              FW_BOLD,0,
                              0,0,
                              DEFAULT_CHARSET,
                              OUT_DEFAULT_PRECIS,
                              CLIP_DEFAULT_PRECIS,
                              DEFAULT_QUALITY,
                              DEFAULT_PITCH | FF_DONTCARE,
                              TEXT("MS Sans Serif")},// TEXT("Times New Roman")
                             {{nsFigure::CColorOpen[_Nil],
                               nsFigure::CColorOpen[_1],   nsFigure::CColorOpen[_2],   nsFigure::CColorOpen[_3],
                               nsFigure::CColorOpen[_4],   nsFigure::CColorOpen[_5],   nsFigure::CColorOpen[_6],
                               nsFigure::CColorOpen[_7],   nsFigure::CColorOpen[_8],   nsFigure::CColorOpen[_9],
                               nsFigure::CColorOpen[_10],  nsFigure::CColorOpen[_11],  nsFigure::CColorOpen[_12],
                               nsFigure::CColorOpen[_13],  nsFigure::CColorOpen[_14],  nsFigure::CColorOpen[_15],
                               nsFigure::CColorOpen[_16],  nsFigure::CColorOpen[_17],  nsFigure::CColorOpen[_18],
                               nsFigure::CColorOpen[_19],  nsFigure::CColorOpen[_20],  nsFigure::CColorOpen[_21]},
                              {nsFigure::CColorClose[_Unknown], nsFigure::CColorClose[_Clear]}},/**/
                             {RGB(0,0,0), RGB(255,255,255), 1} }; // Border

const TsAssistant CAssistantDefault = {
   true,  //bool use;            // on/off Assistant
   10000, //int timeoutUnactive; // таймаут срабатывания при бездействии в миллисекундах
   1000,  //int timeoutJob;      // frequency work assistant
   true,  //bool autoStart;      // autostart new game ?
   false, //bool stopJob;        // останавливать когда нет однозначного следующего хода ?
   true,  //bool ignorePause;    // ignore Pause in game ?
   true   //bool beep;           // MessageBeep by virtual click ?
};

#define WM_HOOKMOUSEMOVE (WM_USER+2)
#define PLAYER_UNKNOWN 0
#define PLAYER_USER    1
#define PLAYER_ROBOT   2

////////////////////////////////////////////////////////////////////////////////
//                             global variables
////////////////////////////////////////////////////////////////////////////////
extern TcRobot gRobot;

////////////////////////////////////////////////////////////////////////////////
//                                   other
////////////////////////////////////////////////////////////////////////////////

void LoadDefaultImageMine(HINSTANCE hInstance, TcImage& ImageMine) {
  #ifdef MINE_IS_BITMAP
   ImageMine.LoadResource(hInstance, TEXT("Mine"), imageBitmap);
  #endif // MINE_IS_BITMAP
  #ifdef MINE_IS_ENHMETAFILE
   ImageMine.LoadResource(hInstance, TEXT("Mine"), imageEnhMetafile);
  #endif // MINE_IS_ENHMETAFILE
}

void LoadDefaultImageFlag(HINSTANCE hInstance, TcImage& ImageFlag) {
  #ifdef FLAG_IS_BITMAP
   ImageFlag.LoadResource(hInstance, TEXT("Flag"), imageBitmap);
  #endif // FLAG_IS_BITMAP
  #ifdef FLAG_IS_ENHMETAFILE
   ImageFlag.LoadResource(hInstance, TEXT("Flag"), imageEnhMetafile);
  #endif // FLAG_IS_ENHMETAFILE
}

void LoadDefaultImagePause(HINSTANCE hInstance, TcImage& ImagePause) {
  #ifdef PAUSE_IS_BITMAP
   ImagePause.LoadResource(hInstance, TEXT("Pause"), imageBitmap);
  #endif // PAUSE_IS_BITMAP
  #ifdef PAUSE_IS_ENHMETAFILE
   ImagePause.LoadResource(hInstance, TEXT("Pause"), imageEnhMetafile);
  #endif // PAUSE_IS_ENHMETAFILE
}

void LoadDefaultImageBckgrnd(HINSTANCE hInstance, TcImage& ImageBckgrnd) {
   ImageBckgrnd.LoadResource(hInstance, NULL, imageUnknown);
}

void LoadDefaultImageBtnNew0(HINSTANCE hInstance, TcImage& ImageBtnNew0) {
   ImageBtnNew0.LoadResource(hInstance, TEXT("New0"), imageIcon);
}

void LoadDefaultImageBtnNew1(HINSTANCE hInstance, TcImage& ImageBtnNew1) {
   ImageBtnNew1.LoadResource(hInstance, TEXT("New1"), imageIcon);
}

void LoadDefaultImageBtnNew2(HINSTANCE hInstance, TcImage& ImageBtnNew2) {
   ImageBtnNew2.LoadResource(hInstance, TEXT("New2"), imageIcon);
}

void LoadDefaultImageBtnNew3(HINSTANCE hInstance, TcImage& ImageBtnNew3) {
   ImageBtnNew3.LoadResource(hInstance, TEXT("New3"), imageIcon);
}

void LoadDefaultImageBtnPause0(HINSTANCE hInstance, TcImage& ImageBtnPause0) {
   ImageBtnPause0.LoadResource(hInstance, TEXT("Pause0"), imageIcon);
}

void LoadDefaultImageBtnPause1(HINSTANCE hInstance, TcImage& ImageBtnPause1) {
   ImageBtnPause1.LoadResource(hInstance, TEXT("Pause1"), imageIcon);
}

void LoadDefaultImageBtnPause2(HINSTANCE hInstance, TcImage& ImageBtnPause2) {
   ImageBtnPause2.LoadResource(hInstance, TEXT("Pause2"), imageIcon);
}

void LoadDefaultImageBtnPause3(HINSTANCE hInstance, TcImage& ImageBtnPause3) {
   ImageBtnPause3.LoadResource(hInstance, TEXT("Pause3"), imageIcon);
}

inline void CALLBACK TimerProc(HWND hwnd, UINT uMsg, UINT idEvent, DWORD dwTime) {
   ((TcMosaic*)idEvent)->TimerProc(hwnd, uMsg, idEvent, dwTime);
}

////////////////////////////////////////////////////////////////////////////////
//                              implementation
////////////////////////////////////////////////////////////////////////////////
LRESULT TcMosaic::WndProcField(HWND hwnd, UINT msg, WPARAM wParam, LPARAM lParam) {
#ifdef USE_INFO_DIALOG
   //nsInfo::AddValue(TEXT("msg (field) = "), msg, 16);
#endif // USE_INFO_DIALOG
   switch(msg){
   HANDLE_MSG(hwnd, WM_PAINT        , Cls_FieldOnPaint);
   HANDLE_MSG(hwnd, WM_LBUTTONUP    , Cls_FieldOnLButtonUp);
   HANDLE_MSG(hwnd, WM_LBUTTONDOWN  , Cls_FieldOnLButtonDown);
   HANDLE_MSG(hwnd, WM_LBUTTONDBLCLK, Cls_FieldOnLButtonDown);
   HANDLE_MSG(hwnd, WM_RBUTTONDOWN  , Cls_FieldOnRButtonDown);
   HANDLE_MSG(hwnd, WM_RBUTTONDBLCLK, Cls_FieldOnRButtonDown);
   }
   return DefWindowProc(hwnd, msg, wParam, lParam);
}

// constructor
TcMosaic::TcMosaic(const HINSTANCE     hInstance,
                   const HWND          hWndProject,
                   const HWND          hWndBtnNew,
                   const HWND          hWndBtnPause,
                   const HWND          hWndEdtCount,
                   const HWND          hWndEdtTimer,
                   const TCHAR * const szCaptionProject,
                   const TCHAR * const szClassWndField,
                   const RECT  * const pBorder) :
   c_hInstance       (hInstance       ),
   c_hWndProject     (hWndProject     ),
   c_hWndBtnNew      (hWndBtnNew      ),
   c_hWndBtnPause    (hWndBtnPause    ),
   c_hWndEdtCount    (hWndEdtCount    ),
   c_hWndEdtTimer    (hWndEdtTimer    ),
   c_szCaptionProject(szCaptionProject),
   c_szClassWndField (szClassWndField ),
   c_pBorder         (pBorder         )
{
   hWndField = CreateWindow( c_szClassWndField, NULL,
      WS_CHILD | WS_VISIBLE,
      CW_USEDEFAULT, CW_USEDEFAULT, CW_USEDEFAULT, CW_USEDEFAULT,
      c_hWndProject, (HMENU)0, c_hInstance, NULL );

   srand((unsigned)time(NULL));
   { // read init file
      TCHAR szPath[MAX_PATH];
      GetModuleFileName(hInstance, szPath, MAX_PATH);
      DelFileFromFullPath(szPath);
      _tcscat(szPath, szCFileNameInit);
      HANDLE hFileInit = CreateFile(
         szPath,
         GENERIC_READ,
         0,
         NULL,
         OPEN_EXISTING,
         FILE_ATTRIBUTE_NORMAL,
         NULL
      );
      BOOL result = FALSE;
      if (hFileInit != INVALID_HANDLE_VALUE) {
         DWORD dwNOBR;
         result = ReadFile(hFileInit, &Settings, sizeof(Settings), &dwNOBR, NULL);
         if (sizeof(Settings) != dwNOBR) result = FALSE;
         if (result) {
            if (_tcscmp(Settings.szVersion, TEXT(ID_VERSIONINFO_VERSION3))) {
               MessageBox(NULL, TEXT("INI file - version error"), TEXT("Information"), MB_ICONINFORMATION | MB_OK);
               result = FALSE;
            }
         } else
            MessageBox(NULL, TEXT("Can't read INI file"), TEXT("Error"), MB_ICONSTOP | MB_OK);
         CloseHandle(hFileInit);
      }
      if (!result) {
         _tcscpy(Settings.szVersion, TEXT(ID_VERSIONINFO_VERSION3));
         Settings.SizeField     = CSizeField[skillLevelBeginner]; // размер поля
         Settings.area          = 2*CMinArea;
         Settings.Figure        = figureSquare1;
         Settings.mines         = DefineNumberMines(skillLevelBeginner, Settings.Figure, Settings.SizeField); // кол-во мин на поле
         Settings.useUnknown    = true;
         Settings.Assistant     = CAssistantDefault;
         Settings.showToolbar   = true;
         Settings.showMenu      = true;
         Settings.showCaption   = true;
         Settings.toTray        = false;
         Settings.autoloadAdmin = true;
         Settings.Skin          = CSkinDefault;
         _tcscpy(Settings.playerName, TEXT("Anonymous"));
         PostMessage(c_hWndProject, WM_COMMAND, MAKEWPARAM(ID_MENU_HELP_ABOUT,0), 0L);
      }
   }
   SetClasureToFigure();

   if (Settings.autoloadAdmin) {
      nsPlayerName::firstLoad = true;
      PostMessage(c_hWndProject, WM_COMMAND, MAKEWPARAM(ID_MENU_GAME_SELECTPLAYER,0), 0L);
   }

   //
   hDCField0 = GetWindowDC(hWndField);
   hDCField1 = CreateCompatibleDC(hDCField0);
   hDCField2 = CreateCompatibleDC(hDCField1);
   hDCPause  = CreateCompatibleDC(hDCField0);
   SetBkMode   (hDCField1, TRANSPARENT); // вывод текста на прозрачном фоне
   SetBkColor  (hDCField1, RGB(255, 255, 255));
   hPenBlack   = NULL;
   hPenWhite   = NULL;
   hBrushField = NULL;

   AcceptSkin();

   CheckMenuRadioItem(GetSubMenu(GetMenu(c_hWndProject), 0),
      ID_MENU_GAME_BEGINNER, ID_MENU_GAME_CUSTOM,
      ID_MENU_GAME_BEGINNER + GetSkillLevel(),
      MF_BYCOMMAND);
   {
      CheckMenuRadioItem(GetSubMenu(GetMenu(c_hWndProject), 1),
         0, 4,
         nsSelectFigure::GetGroup(Settings.Figure),
         MF_BYPOSITION);
      CheckMenuRadioItem(GetSubMenu(GetMenu(c_hWndProject), 1),
         ID_MENU_FIGURE_TRIANGLE1, ID_MENU_FIGURE_TRIANGLE4,
         ID_MENU_FIGURE_TRIANGLE1 + Settings.Figure,
         MF_BYCOMMAND);
      CheckMenuRadioItem(GetSubMenu(GetMenu(c_hWndProject), 1),
         ID_MENU_FIGURE_SQUARE1, ID_MENU_FIGURE_QUADRANGLE1,
         ID_MENU_FIGURE_TRIANGLE1 + Settings.Figure,
         MF_BYCOMMAND);
      CheckMenuRadioItem(GetSubMenu(GetMenu(c_hWndProject), 1),
         ID_MENU_FIGURE_PENTAGON, ID_MENU_FIGURE_PENTAGONT10,
         ID_MENU_FIGURE_TRIANGLE1 + Settings.Figure,
         MF_BYCOMMAND);
      CheckMenuRadioItem(GetSubMenu(GetMenu(c_hWndProject), 1),
         ID_MENU_FIGURE_HEXAGON, ID_MENU_FIGURE_HEXAGON,
         ID_MENU_FIGURE_TRIANGLE1 + Settings.Figure,
         MF_BYCOMMAND);
      CheckMenuRadioItem(GetSubMenu(GetMenu(c_hWndProject), 1),
         ID_MENU_FIGURE_TRSQ, ID_MENU_FIGURE_SQTRHEX,
         ID_MENU_FIGURE_TRIANGLE1 + Settings.Figure,
         MF_BYCOMMAND);
   }
   CheckMenuItem(GetSubMenu(GetMenu(c_hWndProject), 2), ID_MENU_OPTIONS_UNKNOWN,
      Settings.useUnknown ? MF_CHECKED : MF_UNCHECKED);
/*
   CheckMenuItem(GetSubMenu(GetMenu(c_hWndProject), 2), ID_MENU_OPTIONS_TOOLBAR,
      Settings.showToolbar ? MF_CHECKED : MF_UNCHECKED);
   CheckMenuItem(GetSubMenu(GetMenu(c_hWndProject), 2), ID_MENU_OPTIONS_MENU,
      Settings.showMenu ? MF_CHECKED : MF_UNCHECKED);
*/
   CheckMenuItem(GetSubMenu(GetMenu(c_hWndProject), 2), ID_MENU_OPTIONS_TRAY,
      Settings.toTray ? MF_CHECKED : MF_UNCHECKED);

   //FieldCreate();
}

// destructor
TcMosaic::~TcMosaic(){
   { // write init file
      TCHAR szPath[MAX_PATH];
      GetModuleFileName(c_hInstance, szPath, MAX_PATH);
      DelFileFromFullPath(szPath);
      _tcscat(szPath, szCFileNameInit);
      HANDLE hFileInit = CreateFile(
         szPath,
         GENERIC_WRITE,
         0,
         NULL,
         CREATE_ALWAYS,
         FILE_ATTRIBUTE_NORMAL,
         NULL
      );
      if (hFileInit != INVALID_HANDLE_VALUE) {
         DWORD dwNOBW;
         BOOL result = WriteFile(hFileInit, &Settings,
             sizeof(Settings), &dwNOBW, NULL);
         if (result && (sizeof(Settings) != dwNOBW)) result = FALSE;
         if (!result) MessageBox(c_hWndProject, TEXT("Can't write INI file"), TEXT("Error"), MB_ICONSTOP | MB_OK);
         CloseHandle(hFileInit);
      } else
         MessageBox(c_hWndProject, TEXT("Can't create INI file"), TEXT("Error"), MB_ICONSTOP | MB_OK);
   }
   FieldDestroy(Settings.SizeField);
   //Field.~vector();
   DeleteDC(hDCField1);
   DeleteDC(hDCField2);
   ReleaseDC(hWndField, hDCField0);
   DeleteObject(hBrushField);
   DeleteObject(hPenBlack);
   DeleteObject(hPenWhite);
   DeleteObject(hFontField);
}

////////////////////////////////////////////////////////////////////////////////
//                     обработчики сообщений "минного поля"
////////////////////////////////////////////////////////////////////////////////
// WM_PAINT
void TcMosaic::Cls_FieldOnPaint(HWND hwnd){
   DefWindowProc(hwnd, WM_PAINT, 0L, 0L);
   if (pause) {
      if (!isRefreshPause) { // restore bitmap
         BitBlt(hDCField0, 0, 0, sizeBitmap.x, sizeBitmap.y,
                hDCPause, 0, 0, SRCCOPY);
         return;
      }
      PatBlt(hDCPause, 0, 0, sizeBitmap.x, sizeBitmap.y, PATCOPY);
      RECT Rect = {0, 0, sizeBitmap.x, sizeBitmap.y};
      SetCursor(LoadCursor(NULL, IDC_WAIT));
      ImagePause.DrawImagePlace(hDCPause, &Rect, Settings.Skin.Pause.place);
      SetCursor(LoadCursor(NULL, IDC_ARROW));
      BitBlt(hDCField0, 0, 0, sizeBitmap.x, sizeBitmap.y,
             hDCPause, 0, 0, SRCCOPY);
    //SelectObject(hDCField0, hBrushField); // востанавливаю фоновый цвет паузы
      isRefreshPause = false;
      return;
   }

   if (!isRefreshField) { // restore bitmap
      BitBlt(hDCField0, 0, 0, sizeBitmap.x, sizeBitmap.y,
             hDCField1, 0, 0, SRCCOPY);
      return;
   }

   SetCursor(LoadCursor(NULL, IDC_WAIT));
   // background color
   PatBlt(hDCField1, 0, 0, sizeBitmap.x, sizeBitmap.y, PATCOPY);//BLACKNESS);//
   // background image
   {
      RECT Rect = {0, 0, sizeBitmap.x, sizeBitmap.y};
      ImageBckgrnd.DrawImagePlace(hDCField1, &Rect, Settings.Skin.Bckgrnd.place);
   }
   // save background for restore in Cells::regionIn[0..4]
   BitBlt(hDCField2, 0, 0, sizeBitmap.x, sizeBitmap.y, hDCField1, 0,0, SRCCOPY);
   // paint cells
   for (int i = 0; i < Settings.SizeField.x; i++)
      for (int j = 0; j < Settings.SizeField.y; j++)
         Field[i][j]->Paint();
   BitBlt(hDCField0, 0, 0, sizeBitmap.x, sizeBitmap.y,
          hDCField1, 0, 0, SRCCOPY);
   SetCursor(LoadCursor(NULL, IDC_ARROW));
   isRefreshField = false;
}

// WM_LBUTTONDOWN & WM_LBUTTONDBLCLK
void TcMosaic::Cls_FieldOnLButtonDown(HWND hwnd, BOOL fDoubleClick, int x, int y, UINT keyFlags){
   //DefWindowProc(hwnd, fDoubleClick ? WM_LBUTTONDBLCLK : WM_LBUTTONDOWN, (WPARAM)keyFlags, MAKELPARAM(x,y));
   SetCapture(hwnd);
   coordDown = WinToArray(x,y);
   if (coordDown.x < 0) return;
   if (keyFlags != MK_ROBOT) // клик пришёл от юзера
      SendMessage(c_hWndBtnNew, BM_SETIMAGE, IMAGE_ICON, (LPARAM)&ImageBtnNew[1]);
   Field[coordDown.x][coordDown.y]->LButtonDown();
   BitBlt(hDCField0, 0, 0, sizeBitmap.x, sizeBitmap.y,
          hDCField1, 0, 0, SRCCOPY);
}

// WM_LBUTTONUP
void TcMosaic::Cls_FieldOnLButtonUp(HWND hwnd, int x, int y, UINT keyFlags){
   //DefWindowProc(hwnd, WM_LBUTTONUP, (WPARAM)keyFlags, MAKELPARAM(x,y));
   ReleaseCapture();
   if (coordDown.x < 0) return;
   if (keyFlags != MK_ROBOT) // клик пришёл от юзера
      SendMessage(c_hWndBtnNew, BM_SETIMAGE, IMAGE_ICON, (LPARAM)&ImageBtnNew[0]);
   POINT upCoord = WinToArray(x,y);
   if (!gameRun && (upCoord.x==coordDown.x)&&
                   (upCoord.y==coordDown.y))
      GameBegin(coordDown);
   gRobot.ClickBegin();
   if (keyFlags == MK_ROBOT)     // клик пришёл от робота
      whoPlayer |= PLAYER_ROBOT; // робот играл
   const TsLUpReturn result =
      Field[coordDown.x][coordDown.y]->LButtonUp((upCoord.x==coordDown.x)&&
                                                 (upCoord.y==coordDown.y));
   countOpen    += result.countOpen;
   countFlag    += result.countFlag;
   countUnknown += result.countUnknown;
   if (result.countOpen || result.countFlag || result.countUnknown) { // клик со смыслом (были изменения на поле)
      countClick++;
      if (keyFlags != MK_ROBOT) {   // клик пришёл не от робота
         whoPlayer |= PLAYER_USER;  // юзер играл
         gRobot.ResetSequentialMove(); // окончить перебор флажков, если он был...
      }
   }
   ChangeCaptionTimeMine();
   if (result.endGame)
      GameEnd(result.victory);
   else
      if (countOpen+Settings.mines == Settings.SizeField.x*Settings.SizeField.y)
         GameEnd(true);
      else {
         gRobot.ClickEnd();
         BitBlt(hDCField0, 0, 0, sizeBitmap.x, sizeBitmap.y,
                hDCField1, 0, 0, SRCCOPY);
      }
#ifdef USE_INFO_DIALOG
   //nsInfo::AddValue(TEXT("count Open    = "), countOpen   , 10);
   //nsInfo::AddValue(TEXT("count Flag    = "), countFlag   , 10);
   //nsInfo::AddValue(TEXT("count Unknown = "), countUnknown, 10);
#endif // USE_INFO_DIALOG
}

// WM_RBUTTONDOWN & WM_RBUTTONDBLCLK
void TcMosaic::Cls_FieldOnRButtonDown(HWND hwnd, BOOL fDoubleClick, int x, int y, UINT keyFlags){
   //DefWindowProc(hwnd, fDoubleClick ? WM_RBUTTONDBLCLK : WM_RBUTTONDOWN, (WPARAM)keyFlags, MAKELPARAM(x,y));
   if (!gameRun) return;
   POINT coord = WinToArray(x,y);
   if (coord.x < 0) return;
   gRobot.ClickBegin();
   TeClose setClose;
   if (LOWORD(keyFlags) == MK_ROBOT) { // клик пришёл от робота
      whoPlayer |= PLAYER_ROBOT;       // робот играл
      setClose = (TeClose)HIWORD(keyFlags);
   } else {
      switch (Field[coord.x][coord.y]->Cell_GetClose()) {
      case _Clear  : setClose = _Flag;                                     break;
      case _Flag   : setClose = Settings.useUnknown ? _Unknown : _Clear;   break;
      case _Unknown: setClose = _Clear;
      }
   }
   const TsRDownReturn result = Field[coord.x][coord.y]->RButtonDown(setClose);
   if (result.countFlag || result.countUnknown) { // клик со смыслом (были изменения на поле)
      countClick++;
      if (LOWORD(keyFlags) != MK_ROBOT) { // клик пришёл не от робота
         whoPlayer |= PLAYER_USER;        // то считаю что юзер играл
         gRobot.ResetSequentialMove(); // окончить перебор флажков, если он был...
      }
   }
   countFlag    += result.countFlag;
   countUnknown += result.countUnknown;
   ChangeCaptionTimeMine();
   if (gameRun) gRobot.ClickEnd();
   BitBlt(hDCField0, 0, 0, sizeBitmap.x, sizeBitmap.y,
          hDCField1, 0, 0, SRCCOPY);
#ifdef USE_INFO_DIALOG
   //nsInfo::AddValue(TEXT("count Flag    = "), countFlag   , 10);
   //nsInfo::AddValue(TEXT("count Unknown = "), countUnknown, 10);
#endif // USE_INFO_DIALOG
}

////////////////////////////////////////////////////////////////////////////////
//                             other function
////////////////////////////////////////////////////////////////////////////////
void TcMosaic::FieldDestroy(const POINT& SizeField) {
   //delete [] Field;
   for (int i = 0; i < SizeField.x; i++)
      for (int j = 0; j < SizeField.y; j++)
         Field[i][j]->~TcBase();
}

void TcMosaic::FieldCreate() {
   const int maximalArea = GetMaximalArea();
   if (maximalArea < Settings.area) {
      Settings.area = maximalArea;
   }
   SetDCFont();
   //Field = new TCell[Settings.SizeField.x*Settings.SizeField.y];
   Field.resize(Settings.SizeField.x);
   for (int i = 0; i < Settings.SizeField.x; i++){
      Field[i].resize(Settings.SizeField.y);
      for (int j = 0; j < Settings.SizeField.y; j++){
         POINT coord = {i,j};
         switch (Settings.Figure) {
         case figureTriangle1  : Field[i][j] = new nsFigure::TcTriangle1  (coord, Settings.SizeField, Settings.area); break;
         case figureTriangle2  : Field[i][j] = new nsFigure::TcTriangle2  (coord, Settings.SizeField, Settings.area); break;
         case figureTriangle3  : Field[i][j] = new nsFigure::TcTriangle3  (coord, Settings.SizeField, Settings.area); break;
         case figureTriangle4  : Field[i][j] = new nsFigure::TcTriangle4  (coord, Settings.SizeField, Settings.area); break;
         case figureSquare1    : Field[i][j] = new nsFigure::TcSquare1    (coord, Settings.SizeField, Settings.area); break;
         case figureSquare2    : Field[i][j] = new nsFigure::TcSquare2    (coord, Settings.SizeField, Settings.area); break;
         case figureParquet1   : Field[i][j] = new nsFigure::TcParquet1   (coord, Settings.SizeField, Settings.area); break;
         case figureParquet2   : Field[i][j] = new nsFigure::TcParquet2   (coord, Settings.SizeField, Settings.area); break;
         case figureTrapezoid1 : Field[i][j] = new nsFigure::TcTrapezoid1 (coord, Settings.SizeField, Settings.area); break;
         case figureTrapezoid2 : Field[i][j] = new nsFigure::TcTrapezoid2 (coord, Settings.SizeField, Settings.area); break;
         case figureTrapezoid3 : Field[i][j] = new nsFigure::TcTrapezoid3 (coord, Settings.SizeField, Settings.area); break;
         case figureRhombus    : Field[i][j] = new nsFigure::TcRhombus    (coord, Settings.SizeField, Settings.area); break;
         case figureQuadrangle1: Field[i][j] = new nsFigure::TcQuadrangle1(coord, Settings.SizeField, Settings.area); break;
         case figurePentagon   : Field[i][j] = new nsFigure::TcPentagon   (coord, Settings.SizeField, Settings.area); break;
         case figurePentagonT5 : Field[i][j] = new nsFigure::TcPentagonT5 (coord, Settings.SizeField, Settings.area); break;
         case figurePentagonT10: Field[i][j] = new nsFigure::TcPentagonT10(coord, Settings.SizeField, Settings.area); break;
         case figureHexagon    : Field[i][j] = new nsFigure::TcHexagon    (coord, Settings.SizeField, Settings.area); break;
         case figureTrSq       : Field[i][j] = new nsFigure::TcTrSq       (coord, Settings.SizeField, Settings.area); break;
         case figureTrSq2      : Field[i][j] = new nsFigure::TcTrSq2      (coord, Settings.SizeField, Settings.area); break;
         case figureSqTrHex    : Field[i][j] = new nsFigure::TcSqTrHex    (coord, Settings.SizeField, Settings.area); break;
         default /*figureNil*/ : Field[i][j] = new nsFigure::TcBase       (coord, Settings.SizeField, Settings.area); break;
         }
      }
   }
   int numberNeighbor = Field[0][0]->GetNeighborNumber();
   TB**const ppLinkNeighbor = new TB*[numberNeighbor];
   for (i = 0; i < Settings.SizeField.x; i++){
      for (int j = 0; j < Settings.SizeField.y; j++){
         for (int k=0; k<numberNeighbor; k++) {
            POINT coordNeighbor = Field[i][j]->GetNeighborCoord(k);
            if (coordNeighbor != CIncorrectCoord)
                  ppLinkNeighbor[k] = Field[coordNeighbor.x][coordNeighbor.y];
            else  ppLinkNeighbor[k] = NULL;
         }
         Field[i][j]->SetNeighborLink(ppLinkNeighbor, numberNeighbor);
      }
   }
   delete [] ppLinkNeighbor;
   SendMessage(c_hWndProject, WM_SIZE, 0L, 0L);
   ResizeBitmap();
   SetPause(false);
   GameNew();
}

void TcMosaic::ChangeCaptionTimeMine(bool Victory, bool valid) {
   TCHAR szCaption[64] = {TEXT('\0')};
   if (Settings.showToolbar) {
      SetWindowText(c_hWndEdtTimer, _itot(countTimer         , szCaption, 10));
      SetWindowText(c_hWndEdtCount, _itot(Settings.mines-countFlag, szCaption, 10));
      _tcscpy(szCaption, c_szCaptionProject);
   } else {
      _stprintf(szCaption, TEXT("%s  -  Mines[%i]  Time[%i sec]"), c_szCaptionProject, Settings.mines-countFlag, countTimer);
   }
   if (valid) {
      if (Victory) _tcscat(szCaption, TEXT(" - Victory"));
      else         _tcscat(szCaption, TEXT(" - Defeat" ));
   }
   SetWindowText(c_hWndProject, szCaption);
   VerifyFlag();
}

void TcMosaic::GameNew() {
   if (pause) {
      SetPause(false);
      return;
   }
#ifdef USE_INFO_DIALOG
   //nsInfo::ClearEdit();
#endif // USE_INFO_DIALOG
   SendMessage(c_hWndBtnNew  , BM_SETIMAGE, IMAGE_ICON, (LPARAM)&ImageBtnNew[0]);
   EnableWindow(hWndField, true); // enable all cell

   SetPause(gameRun = false);

   countTimer = countClick = countFlag = countOpen = countUnknown = 0;
   ChangeCaptionTimeMine();

   for (int i = 0; i < Settings.SizeField.x; i++)
      for (int j = 0; j < Settings.SizeField.y; j++)
         Field[i][j]->Reset();
   isRefreshField = isRefreshPause = true;
   PostMessage(hWndField, WM_PAINT, 0L, 0L);

   whoPlayer = PLAYER_UNKNOWN; // пока не знаю кто играл

   gRobot.GameNew();
}

void TcMosaic::GameBegin(POINT firstClick) {
   int count;
   int i, j;

   SetPause((gameRun = true, false));

   // set random mines
   Field[firstClick.x][firstClick.y]->LockNeighbor(); // запрещаю установку мин у соседей и у себя
   count = 0;
   do {
      i = (Settings.SizeField.x*rand())/RAND_MAX; if (i == Settings.SizeField.x) i--; // :(
      j = (Settings.SizeField.y*rand())/RAND_MAX; if (j == Settings.SizeField.y) j--; // :(
      if (Field[i][j]->Cell_SetMine())
         count++;
   } while (count < Settings.mines);
   // set other CellOpen and set all Caption
   for (i = 0; i < Settings.SizeField.x; i++)
      for (j = 0; j < Settings.SizeField.y; j++)
         Field[i][j]->Cell_DefineValue();
}

void TcMosaic::GameEnd(bool Victory) {
   if (!gameRun) return;
#ifdef USE_INFO_DIALOG
   nsInfo::AddString(TEXT("Game is "), Victory ? TEXT("Victory") : TEXT("Defeat"));
#endif // USE_INFO_DIALOG

   gRobot.ResetSequentialMove();

   int i, j;

   SetPause(gameRun = false);
   // открыть оставшeеся
   {
   SetCursor(LoadCursor(NULL, IDC_WAIT));
   if (Victory) {
      for (i = 0; i < Settings.SizeField.x; i++)
         for (j = 0; j < Settings.SizeField.y; j++) {
            if (Field[i][j]->Cell_GetStatus() == _Close) {
               if(Field[i][j]->Cell_GetOpen() == _Mine)
                  Field[i][j]->Cell_SetClose(_Flag);
               else {
                  Field[i][j]->Cell_SetStatus(_Open);
                  Field[i][j]->Cell_SetDown(true);
               }
               Field[i][j]->Paint();
            }
         }
      countFlag = Settings.mines;
   } else
      for (i = 0; i < Settings.SizeField.x; i++)
         for (j = 0; j < Settings.SizeField.y; j++)
            if (Field[i][j]->Cell_GetStatus() == _Close) {
               if ((Field[i][j]->Cell_GetClose() == _Flag) &&
                   (Field[i][j]->Cell_GetOpen()  == _Mine))
                    Field[i][j]->Cell_SetStatus(_Close);
               else Field[i][j]->Cell_SetStatus(_Open);
               Field[i][j]->Paint();
            }
   BitBlt(hDCField0, 0, 0, sizeBitmap.x, sizeBitmap.y,
          hDCField1, 0, 0, SRCCOPY);
   //SendMessage(hWndField, WM_PAINT, 0L, 0L);
   SetCursor(LoadCursor(NULL, IDC_ARROW));
   }
   EnableWindow(hWndField, false); // disable all cell
   MessageBeep(0);

   ChangeCaptionTimeMine(Victory, true);
   if (Victory) {
      SendMessage(c_hWndBtnNew, BM_SETIMAGE, IMAGE_ICON, (LPARAM)&ImageBtnNew[2]);
      switch (whoPlayer) {
      case PLAYER_USER:
         nsChampions::SaveResult(Settings.Figure, GetSkillLevel(), countTimer, Settings.playerName);
         break;
    //case PLAYER_ROBOT:
    //   nsChampions::SaveResult(Settings.Figure, GetSkillLevel(), countTimer, nsPlayerName::szRobotNameDefault);
      }
   } else {
      SendMessage(c_hWndBtnNew, BM_SETIMAGE, IMAGE_ICON, (LPARAM)&ImageBtnNew[3]);
   }

   if ((GetSkillLevel() != skillLevelCustom) &&
       ((whoPlayer == PLAYER_USER ) ||
        (whoPlayer == PLAYER_ROBOT)))
   {
      nsStatistics::TsSttstcSubRecord statisticsResult;
      statisticsResult.GameNumber = 1;
      statisticsResult.GameWin    = Victory;
      statisticsResult.OpenField  = Victory ?
                                       Settings.SizeField.x*Settings.SizeField.y-DefineNumberMines(GetSkillLevel(), Settings.Figure, Settings.SizeField):
                                       countOpen;
      statisticsResult.PlayTime   = Victory ? countTimer : 0;
      statisticsResult.ClickCount = Victory ? countClick : 0;
      switch (whoPlayer) {
      case PLAYER_USER:
         nsStatistics::InsertResult(statisticsResult, Settings.Figure, GetSkillLevel(), Settings.playerName);
         break;
      case PLAYER_ROBOT:
         nsStatistics::InsertResult(statisticsResult, Settings.Figure, GetSkillLevel(), nsPlayerName::szRobotNameDefault);
      }
   }
}

void TcMosaic::SetSkillLevel(const TeSkillLevel& skill) {
   if (skill == skillLevelCustom) {
      const POINT oldSizeField = Settings.SizeField;
      const int   oldMines     = Settings.mines;
      if (DialogBox(c_hInstance, TEXT("CustomSkillLevel"), c_hWndProject, (DLGPROC)nsCustomSkill::DialogProc) == IDCANCEL)
         return;
      if ((oldSizeField.x == Settings.SizeField.x) &&
          (oldSizeField.y == Settings.SizeField.y) &&
          (oldMines       == Settings.mines)) return;
      CheckMenuRadioItem( GetSubMenu(GetMenu(c_hWndProject), 0),
         ID_MENU_GAME_BEGINNER, ID_MENU_GAME_CUSTOM,
         ID_MENU_GAME_BEGINNER + GetSkillLevel(),
         MF_BYCOMMAND);
      FieldDestroy(oldSizeField);
      FieldCreate();
      return;
   }
   if (GetSkillLevel() == skill) {
      GameNew();
      return;
   }
   CheckMenuRadioItem( GetSubMenu(GetMenu(c_hWndProject), 0),
      ID_MENU_GAME_BEGINNER, ID_MENU_GAME_CUSTOM,
      ID_MENU_GAME_BEGINNER + skill,
      MF_BYCOMMAND);
   FieldDestroy(Settings.SizeField);
   Settings.SizeField = CSizeField[skill];
   Settings.mines = DefineNumberMines(skill, Settings.Figure, Settings.SizeField);
   FieldCreate();
   return;
}

TeFigure TcMosaic::GetFigure() const {
   return Settings.Figure;
}

TeSkillLevel TcMosaic::GetSkillLevel() {
   BYTE i;
   TeSkillLevel Result = skillLevelCustom;

   for (i = skillLevelBeginner; i < skillLevelCustom; i++)
      if ((Settings.SizeField.x == CSizeField[i].x) &&
          (Settings.SizeField.y == CSizeField[i].y) &&
          (Settings.mines       == DefineNumberMines((TeSkillLevel)i, Settings.Figure, CSizeField[i])))
      {
         Result = (TeSkillLevel)i;
         break;
      }
   return Result;
}

void TcMosaic::TimerProc(HWND, UINT, UINT, DWORD) {
   countTimer++;
   ChangeCaptionTimeMine();
}

inline void TcMosaic::VerifyFlag() {
   if (!gameRun) return;
   if (Settings.mines == countFlag) {
      for (int i=0; i < Settings.SizeField.x; i++)
         for (int j=0; j < Settings.SizeField.y; j++)
            if ((Field[i][j]->Cell_GetClose() == _Flag) &&
                (Field[i][j]->Cell_GetOpen () != _Mine))
               return; // неверно проставленный флажок - на выход
      GameEnd(true);
   }
   if (Settings.mines == countFlag+countUnknown) {
      for (int i=0; i < Settings.SizeField.x; i++)
         for (int j=0; j < Settings.SizeField.y; j++)
            if (((Field[i][j]->Cell_GetClose() == _Unknown) ||
                 (Field[i][j]->Cell_GetClose() == _Flag)) &&
                ( Field[i][j]->Cell_GetOpen () != _Mine))
               return; // неверно проставленный флажок или '?'- на выход
      GameEnd(true);
   }
}

POINT TcMosaic::WinToArray(int x, int y) const { // преобразовать экранные координаты в координаты Field'a
   POINT result;
   memset(&result, -1, sizeof(POINT));
   for (int i = 0; i < Settings.SizeField.x; i++)
      for (int j = 0; j < Settings.SizeField.y; j++)
         if (Field[i][j]->ToBelong(x,y)) {
            result = Field[i][j]->GetCoord();
            return result;
         }
   return result;
}

POINT TcMosaic::GetSizeWindowProject(const POINT& sizeFieldInPixel) const {
   RECT windowRect, clientRect;
   GetWindowRect(c_hWndProject, &windowRect);
   GetClientRect(c_hWndProject, &clientRect);
   const POINT result = {
      (windowRect.right-windowRect.left) -
      (clientRect.right-clientRect.left) +
      sizeFieldInPixel.x + c_pBorder->left+c_pBorder->right,
      (windowRect.bottom-windowRect.top) -
      (clientRect.bottom-clientRect.top) +
      sizeFieldInPixel.y + c_pBorder->top+c_pBorder->bottom};
   return result;
}

void TcMosaic::SetDCFont() {
   Settings.Skin.Font.lfHeight = GetSizeInscribedSquare(Settings.area);
   hFontField = CreateFontIndirect(&Settings.Skin.Font);
   DeleteObject(SelectObject(hDCField0, hFontField));
   DeleteObject(SelectObject(hDCField1, hFontField));

   {
      RECT Rect; GetClientRect(c_hWndEdtCount, &Rect);
      LOGFONT logFont = Settings.Skin.Font;
      logFont.lfHeight = Rect.bottom;

      FORWARD_WM_SETFONT(c_hWndEdtCount, CreateFontIndirect(&logFont), TRUE, SendMessage);
      FORWARD_WM_SETFONT(c_hWndEdtTimer, CreateFontIndirect(&logFont), TRUE, SendMessage);
   }
}

void TcMosaic::SetFigure(TeFigure figure) {
   if (Settings.Figure == figure) {
      GameNew();
      return;
   }
   TeSkillLevel skill = GetSkillLevel(); // skill level для СТАРОЙ фигуры!!!
   FieldDestroy(Settings.SizeField);
   Settings.Figure = figure;
   SetClasureToFigure();
   if (skill != skillLevelCustom)
      Settings.mines = DefineNumberMines(skill, Settings.Figure, Settings.SizeField);
   {
      CheckMenuRadioItem(GetSubMenu(GetMenu(c_hWndProject), 1),
         0, 4,
         nsSelectFigure::GetGroup(Settings.Figure),
         MF_BYPOSITION);
      CheckMenuRadioItem(GetSubMenu(GetMenu(c_hWndProject), 1),
         ID_MENU_FIGURE_TRIANGLE1, ID_MENU_FIGURE_TRIANGLE4,
         ID_MENU_FIGURE_TRIANGLE1 + Settings.Figure,
         MF_BYCOMMAND);
      CheckMenuRadioItem(GetSubMenu(GetMenu(c_hWndProject), 1),
         ID_MENU_FIGURE_SQUARE1, ID_MENU_FIGURE_QUADRANGLE1,
         ID_MENU_FIGURE_TRIANGLE1 + Settings.Figure,
         MF_BYCOMMAND);
      CheckMenuRadioItem(GetSubMenu(GetMenu(c_hWndProject), 1),
         ID_MENU_FIGURE_PENTAGON, ID_MENU_FIGURE_PENTAGONT10,
         ID_MENU_FIGURE_TRIANGLE1 + Settings.Figure,
         MF_BYCOMMAND);
      CheckMenuRadioItem(GetSubMenu(GetMenu(c_hWndProject), 1),
         ID_MENU_FIGURE_HEXAGON, ID_MENU_FIGURE_HEXAGON,
         ID_MENU_FIGURE_TRIANGLE1 + Settings.Figure,
         MF_BYCOMMAND);
      CheckMenuRadioItem(GetSubMenu(GetMenu(c_hWndProject), 1),
         ID_MENU_FIGURE_TRSQ, ID_MENU_FIGURE_SQTRHEX,
         ID_MENU_FIGURE_TRIANGLE1 + Settings.Figure,
         MF_BYCOMMAND);
   }
   FieldCreate();
   if (skill == skillLevelCustom) { // skill level для НОВОЙ фигуры!!!
      int maxMines = Settings.SizeField.x*Settings.SizeField.y-(Field[0][0]->GetNeighborNumber()+1);
      if (Settings.mines > maxMines) {
         Settings.mines = maxMines;
         //countFlag=0; // ???
         ChangeCaptionTimeMine();
      }
   }
   return;
}

void TcMosaic::ChangeSizeField() {
   for (int i = 0; i < Settings.SizeField.x; i++)
      for (int j = 0; j < Settings.SizeField.y; j++)
         Field[i][j]->SetPoint(Settings.area);
   SetDCFont();
   SendMessage(c_hWndProject, WM_SIZE, 0L, 0L);
   ResizeBitmap();
   SendMessage(hWndField, WM_PAINT, 0L, 0L);
}

int TcMosaic::GetMaximalArea() const {
   int result;
   const POINT sizeScreen =
      {GetSystemMetrics(SM_CXSCREEN),
       GetSystemMetrics(SM_CYSCREEN)};
   result = CMinArea;
   result++;
   POINT sizeWnd = GetSizeWindowProject(GetSizeWindowField(Settings.SizeField, result));
   while ((sizeWnd.x <= sizeScreen.x) &&
          (sizeWnd.y <= sizeScreen.y)) {
      result++;
      sizeWnd = GetSizeWindowProject(GetSizeWindowField(Settings.SizeField, result));
   }
   result--;
   return result;
}

float GetPercentMine(const TeSkillLevel skill, const TeFigure figure) { // процент мин на заданном уровне сложности
   switch (figure) {
   case figureTriangle1  : return nsFigure::TcTriangle1  ::GetPercentMine(skill);
   case figureTriangle2  : return nsFigure::TcTriangle2  ::GetPercentMine(skill);
   case figureTriangle3  : return nsFigure::TcTriangle3  ::GetPercentMine(skill);
   case figureTriangle4  : return nsFigure::TcTriangle4  ::GetPercentMine(skill);
   case figureSquare1    : return nsFigure::TcSquare1    ::GetPercentMine(skill);
   case figureSquare2    : return nsFigure::TcSquare2    ::GetPercentMine(skill);
   case figureParquet1   : return nsFigure::TcParquet1   ::GetPercentMine(skill);
   case figureParquet2   : return nsFigure::TcParquet2   ::GetPercentMine(skill);
   case figureTrapezoid1 : return nsFigure::TcTrapezoid1 ::GetPercentMine(skill);
   case figureTrapezoid2 : return nsFigure::TcTrapezoid2 ::GetPercentMine(skill);
   case figureTrapezoid3 : return nsFigure::TcTrapezoid3 ::GetPercentMine(skill);
   case figureRhombus    : return nsFigure::TcRhombus    ::GetPercentMine(skill);
   case figureQuadrangle1: return nsFigure::TcQuadrangle1::GetPercentMine(skill);
   case figurePentagon   : return nsFigure::TcPentagon   ::GetPercentMine(skill);
   case figurePentagonT5 : return nsFigure::TcPentagonT5 ::GetPercentMine(skill);
   case figurePentagonT10: return nsFigure::TcPentagonT10::GetPercentMine(skill);
   case figureHexagon    : return nsFigure::TcHexagon    ::GetPercentMine(skill);
   case figureTrSq       : return nsFigure::TcTrSq       ::GetPercentMine(skill);
   case figureTrSq2      : return nsFigure::TcTrSq2      ::GetPercentMine(skill);
   case figureSqTrHex    : return nsFigure::TcSqTrHex    ::GetPercentMine(skill);
   default /*figureNil*/ : return nsFigure::TcBase       ::GetPercentMine(skill);
   }
}

int DefineNumberMines(const TeSkillLevel skill, const TeFigure figure, const POINT sizeField) {
   if (skill == skillLevelCustom) {
      MessageBox(NULL, TEXT("DefineNumberMines()"), TEXT("Error"), MB_OK | MB_ICONSTOP);
      return 1;
   }
   return sizeField.x *
          sizeField.y * GetPercentMine(skill, figure) / 100;

}

void TcMosaic::ChangeUseUnknown() {
   Settings.useUnknown = !Settings.useUnknown;
   CheckMenuItem( GetSubMenu(GetMenu(c_hWndProject), 2), ID_MENU_OPTIONS_UNKNOWN,
      Settings.useUnknown ? MF_CHECKED : MF_UNCHECKED);
}

int TcMosaic::ChangeShowToolbar() {
   Settings.showToolbar = !Settings.showToolbar;

   ChangeCaptionTimeMine();

   CheckMenuItem(GetSubMenu(GetMenu(c_hWndProject), 2), ID_MENU_OPTIONS_TOOLBAR,
      Settings.showToolbar ? MF_CHECKED : MF_UNCHECKED);
   if (Settings.showToolbar)
      return SW_SHOW;
   else
      return SW_HIDE;
}

int TcMosaic::ChangeShowMenu() {
   Settings.showMenu = !Settings.showMenu;
   CheckMenuItem(GetSubMenu(GetMenu(c_hWndProject), 2), ID_MENU_OPTIONS_MENU,
      Settings.showMenu ? MF_UNCHECKED : MF_CHECKED);
   if (Settings.showMenu)
      return SW_SHOW;
   else
      return SW_HIDE;
}

int TcMosaic::ChangeShowCaption() {
   Settings.showCaption = !Settings.showCaption;
   CheckMenuItem(GetSubMenu(GetMenu(c_hWndProject), 2), ID_MENU_OPTIONS_CAPTION,
      Settings.showCaption ? MF_CHECKED : MF_UNCHECKED);
   if (Settings.showCaption)
      return SW_SHOW;
   else
      return SW_HIDE;
}

void TcMosaic::ChangeToTray() {
   Settings.toTray = !Settings.toTray;
   CheckMenuItem( GetSubMenu(GetMenu(c_hWndProject), 2), ID_MENU_OPTIONS_TRAY,
      Settings.toTray ? MF_CHECKED : MF_UNCHECKED);
}

bool TcMosaic::ToTray() {
   return Settings.toTray;
}

bool TcMosaic::AreaIncrement() {
   const int oldArea = Settings.area;
   float delta = Settings.area * 5.0 / 100;
   Settings.area += delta;
   if (GetMaximalArea() < Settings.area) {
      Settings.area = oldArea;
      MessageBeep(0);
      return false;
   }
   ChangeSizeField();
   return true;
}

void TcMosaic::AreaDecrement() {
   float delta = Settings.area * 5.0 / 105;
   if (Settings.area-delta <= CMinArea) {
      MessageBeep(0);
      if (Settings.area == CMinArea ) return;
      Settings.area = CMinArea;
   } else Settings.area -= delta;
   ChangeSizeField();
}

void TcMosaic::AreaMin() {
   if (Settings.area == CMinArea) {
      MessageBeep(0);
      return;
   }
   Settings.area = CMinArea;
   ChangeSizeField();
}

bool TcMosaic::AreaMax() {
   const int maximalArea = GetMaximalArea();
   if (maximalArea == Settings.area) {
      MessageBeep(0);
      return false;
   }
   Settings.area = maximalArea;
   ChangeSizeField();
   return true;
}

POINT TcMosaic::GetSizeField() const {
   return Settings.SizeField;
}

int TcMosaic::GetArea() const {
   return Settings.area;
}

int TcMosaic::GetAutoloadAdmin() const {
   return Settings.autoloadAdmin;
}

void TcMosaic::SetAutoloadAdmin(const bool newAutoloadAdmin) {
   Settings.autoloadAdmin = newAutoloadAdmin;
}

int TcMosaic::GetMines() const {
   return Settings.mines;
}

void TcMosaic::SetSizeField(const POINT& newSizeField) {
   Settings.SizeField = newSizeField;
}

void TcMosaic::SetMines(int newNumberMines) {
   Settings.mines = newNumberMines;
}

HWND TcMosaic::GetHandleField() const {
   return hWndField;
}

void TcMosaic::SetPause(bool newPause) {
   if (!gameRun) {
      SendMessage(c_hWndBtnPause, BM_SETIMAGE, IMAGE_ICON, (LPARAM)&ImageBtnPause[2]);
      EnableWindow(c_hWndBtnPause, gameRun);
      KillTimer(c_hWndProject, (UINT)this);
      return;
   }
   EnableWindow(c_hWndBtnPause, gameRun);
   pause = newPause;
   if (pause) {
      KillTimer(c_hWndProject, (UINT)this);
      SendMessage(c_hWndBtnPause, BM_SETCHECK, (WPARAM)BST_CHECKED, 0L);
      SendMessage(c_hWndBtnPause, BM_SETIMAGE, IMAGE_ICON, (LPARAM)&ImageBtnPause[1]);
   } else {
      SendMessage(c_hWndBtnPause, BM_SETCHECK, (WPARAM)BST_UNCHECKED, 0L);
      SendMessage(c_hWndBtnPause, BM_SETIMAGE, IMAGE_ICON, (LPARAM)&ImageBtnPause[0]);
      SetTimer(c_hWndProject, (UINT)this, 1000, (TIMERPROC)::TimerProc);
   }
   SendMessage(hWndField, WM_PAINT, 0L, 0L);
   EnableWindow(hWndField, !pause);
}

bool TcMosaic::GetPause() const {
   return pause;
}

bool TcMosaic::GetGameRun() const {
   return gameRun;
}

bool TcMosaic::IsFieldEnabled() const {
   return !!IsWindowEnabled(hWndField);
}

void TcMosaic::ResizeBitmap() {
   RECT wndRect;
   GetWindowRect(hWndField, &wndRect);
   POINT sizeWnd = {
      wndRect.right -wndRect.left,
      wndRect.bottom-wndRect.top};
   if ((sizeBitmap.x != sizeWnd.x) || (sizeBitmap.y != sizeWnd.y)) {
      sizeBitmap.x = sizeWnd.x;
      sizeBitmap.y = sizeWnd.y;
      DeleteObject(SelectObject(hDCField1, CreateCompatibleBitmap(hDCField0, sizeBitmap.x,sizeBitmap.y)));
      DeleteObject(SelectObject(hDCField2, CreateCompatibleBitmap(hDCField0, sizeBitmap.x,sizeBitmap.y)));
      DeleteObject(SelectObject(hDCPause , CreateCompatibleBitmap(hDCField0, sizeBitmap.x,sizeBitmap.y)));
   }
   isRefreshField = isRefreshPause = true;
}

const TsSkin TcMosaic::GetSkinDefault() const {
   return CSkinDefault;
}

const TsSkin& TcMosaic::GetSkin() const {
   return Settings.Skin;
}

void TcMosaic::SetSkin(TsSkin newSkin) {
   Settings.Skin = newSkin;
   AcceptSkin();
   isRefreshField = isRefreshPause = true;
   InvalidateRect(hWndField, NULL, TRUE);
}

void TcMosaic::AcceptSkin() {
   SetCursor(LoadCursor(NULL, IDC_WAIT));

   DeleteObject(hPenBlack);
   hPenBlack = CreatePen(PS_SOLID, Settings.Skin.Border.width, Settings.Skin.Border.shadow); // Black pen
   DeleteObject(hPenWhite);
   hPenWhite = CreatePen(PS_SOLID, Settings.Skin.Border.width, Settings.Skin.Border.light ); // White pen

   DeleteObject(hBrushField);
   hBrushField = CreateSolidBrush(Settings.Skin.colorBk);
   DeleteObject(SelectObject(hDCField0, hBrushField));
   DeleteObject(SelectObject(hDCField1, hBrushField));
   DeleteObject(SelectObject(hDCPause , hBrushField));
   ImagePause  .SetBrush(hBrushField);
   ImageBckgrnd.SetBrush(hBrushField);

   SetDCFont();

   bool isLoad = false;
   if (Settings.Skin.Mine.path[0] != TEXT('\0'))
      isLoad = ImageMine.LoadFile(Settings.Skin.Mine.path);
   if (!isLoad)
      LoadDefaultImageMine(c_hInstance, ImageMine);

   isLoad = false;
   if (Settings.Skin.Flag.path[0] != TEXT('\0'))
      isLoad = ImageFlag.LoadFile(Settings.Skin.Flag.path);
   if (!isLoad)
      LoadDefaultImageFlag(c_hInstance, ImageFlag);

   isLoad = false;
   if (Settings.Skin.Pause.path[0] != TEXT('\0'))
      isLoad = ImagePause.LoadFile(Settings.Skin.Pause.path);
   if (!isLoad)
      LoadDefaultImagePause(c_hInstance, ImagePause);

   isLoad = false;
   if (Settings.Skin.Bckgrnd.path[0] != TEXT('\0'))
      isLoad = ImageBckgrnd.LoadFile(Settings.Skin.Bckgrnd.path);
   if (!isLoad)
      LoadDefaultImageBckgrnd(c_hInstance, ImageBckgrnd);

   {
      LoadDefaultImageBtnNew0(c_hInstance, ImageBtnNew[0]);
      LoadDefaultImageBtnNew1(c_hInstance, ImageBtnNew[1]);
      LoadDefaultImageBtnNew2(c_hInstance, ImageBtnNew[2]);
      LoadDefaultImageBtnNew3(c_hInstance, ImageBtnNew[3]);
      if (Settings.Skin.BtnNew[0].path[0] != TEXT('\0')) isLoad = ImageBtnNew[0].LoadFile(Settings.Skin.BtnNew[0].path);
      if (Settings.Skin.BtnNew[1].path[0] != TEXT('\0')) isLoad = ImageBtnNew[1].LoadFile(Settings.Skin.BtnNew[1].path);
      if (Settings.Skin.BtnNew[2].path[0] != TEXT('\0')) isLoad = ImageBtnNew[2].LoadFile(Settings.Skin.BtnNew[2].path);
      if (Settings.Skin.BtnNew[3].path[0] != TEXT('\0')) isLoad = ImageBtnNew[3].LoadFile(Settings.Skin.BtnNew[3].path);
      LoadDefaultImageBtnPause0(c_hInstance, ImageBtnPause[0]);
      LoadDefaultImageBtnPause1(c_hInstance, ImageBtnPause[1]);
      LoadDefaultImageBtnPause2(c_hInstance, ImageBtnPause[2]);
      LoadDefaultImageBtnPause3(c_hInstance, ImageBtnPause[3]);
      if (Settings.Skin.BtnPause[0].path[0] != TEXT('\0')) isLoad = ImageBtnPause[0].LoadFile(Settings.Skin.BtnPause[0].path);
      if (Settings.Skin.BtnPause[1].path[0] != TEXT('\0')) isLoad = ImageBtnPause[1].LoadFile(Settings.Skin.BtnPause[1].path);
      if (Settings.Skin.BtnPause[2].path[0] != TEXT('\0')) isLoad = ImageBtnPause[2].LoadFile(Settings.Skin.BtnPause[2].path);
      if (Settings.Skin.BtnPause[3].path[0] != TEXT('\0')) isLoad = ImageBtnPause[3].LoadFile(Settings.Skin.BtnPause[3].path);
   }

   ImageMine       .SetTransparent(Settings.Skin.Mine       .transparent);
   ImageFlag       .SetTransparent(Settings.Skin.Flag       .transparent);
   ImagePause      .SetTransparent(Settings.Skin.Pause      .transparent);
   ImageBckgrnd    .SetTransparent(Settings.Skin.Bckgrnd    .transparent);
   ImageBtnNew[0]  .SetTransparent(Settings.Skin.BtnNew[0]  .transparent);
   ImageBtnNew[1]  .SetTransparent(Settings.Skin.BtnNew[1]  .transparent);
   ImageBtnNew[2]  .SetTransparent(Settings.Skin.BtnNew[2]  .transparent);
   ImageBtnNew[3]  .SetTransparent(Settings.Skin.BtnNew[3]  .transparent);
   ImageBtnPause[0].SetTransparent(Settings.Skin.BtnPause[0].transparent);
   ImageBtnPause[1].SetTransparent(Settings.Skin.BtnPause[1].transparent);
   ImageBtnPause[2].SetTransparent(Settings.Skin.BtnPause[2].transparent);
   ImageBtnPause[3].SetTransparent(Settings.Skin.BtnPause[3].transparent);

   TB::pImageFlag  = &ImageFlag;
   TB::pImageMine  = &ImageMine;
   TB::pColorClose = Settings.Skin.colorText.captionClose;
   TB::pColorOpen  = Settings.Skin.colorText.captionOpen;
   TB::hDC         = hDCField1;
   TB::hCDC        = hDCField2;
   TB::hPenBlack   = hPenBlack;
   TB::hPenWhite   = hPenWhite;
 //TB::w           = Settings.Skin.Border.width;

   PostMessage(c_hWndProject, WM_CHANGEBRUSH, 0L, 0L);

   InvalidateRect(c_hWndProject, NULL, TRUE);
   SetCursor(LoadCursor(NULL, IDC_ARROW));
}

POINT TcMosaic::GetSizeBitmap() const {
   return sizeBitmap;
}

void TcMosaic::SetClasureToFigure() {
   switch (Settings.Figure) {
   case figureTriangle1  : GetSizeWindowField = nsFigure::TcTriangle1  ::GetSizeFieldInPixel; break;
   case figureTriangle2  : GetSizeWindowField = nsFigure::TcTriangle2  ::GetSizeFieldInPixel; break;
   case figureTriangle3  : GetSizeWindowField = nsFigure::TcTriangle3  ::GetSizeFieldInPixel; break;
   case figureTriangle4  : GetSizeWindowField = nsFigure::TcTriangle4  ::GetSizeFieldInPixel; break;
   case figureSquare1    : GetSizeWindowField = nsFigure::TcSquare1    ::GetSizeFieldInPixel; break;
   case figureSquare2    : GetSizeWindowField = nsFigure::TcSquare2    ::GetSizeFieldInPixel; break;
   case figureParquet1   : GetSizeWindowField = nsFigure::TcParquet1   ::GetSizeFieldInPixel; break;
   case figureParquet2   : GetSizeWindowField = nsFigure::TcParquet2   ::GetSizeFieldInPixel; break;
   case figureTrapezoid1 : GetSizeWindowField = nsFigure::TcTrapezoid1 ::GetSizeFieldInPixel; break;
   case figureTrapezoid2 : GetSizeWindowField = nsFigure::TcTrapezoid2 ::GetSizeFieldInPixel; break;
   case figureTrapezoid3 : GetSizeWindowField = nsFigure::TcTrapezoid3 ::GetSizeFieldInPixel; break;
   case figureRhombus    : GetSizeWindowField = nsFigure::TcRhombus    ::GetSizeFieldInPixel; break;
   case figureQuadrangle1: GetSizeWindowField = nsFigure::TcQuadrangle1::GetSizeFieldInPixel; break;
   case figurePentagon   : GetSizeWindowField = nsFigure::TcPentagon   ::GetSizeFieldInPixel; break;
   case figurePentagonT5 : GetSizeWindowField = nsFigure::TcPentagonT5 ::GetSizeFieldInPixel; break;
   case figurePentagonT10: GetSizeWindowField = nsFigure::TcPentagonT10::GetSizeFieldInPixel; break;
   case figureHexagon    : GetSizeWindowField = nsFigure::TcHexagon    ::GetSizeFieldInPixel; break;
   case figureTrSq       : GetSizeWindowField = nsFigure::TcTrSq       ::GetSizeFieldInPixel; break;
   case figureTrSq2      : GetSizeWindowField = nsFigure::TcTrSq2      ::GetSizeFieldInPixel; break;
   case figureSqTrHex    : GetSizeWindowField = nsFigure::TcSqTrHex    ::GetSizeFieldInPixel; break;
   default /*figureNil*/ : GetSizeWindowField = nsFigure::TcBase       ::GetSizeFieldInPixel; break;
   }

   switch (Settings.Figure) {
   case figureTriangle1  : GetSizeInscribedSquare = nsFigure::TcTriangle1  ::SizeInscribedSquare; break;
   case figureTriangle2  : GetSizeInscribedSquare = nsFigure::TcTriangle2  ::SizeInscribedSquare; break;
   case figureTriangle3  : GetSizeInscribedSquare = nsFigure::TcTriangle3  ::SizeInscribedSquare; break;
   case figureTriangle4  : GetSizeInscribedSquare = nsFigure::TcTriangle4  ::SizeInscribedSquare; break;
   case figureSquare1    : GetSizeInscribedSquare = nsFigure::TcSquare1    ::SizeInscribedSquare; break;
   case figureSquare2    : GetSizeInscribedSquare = nsFigure::TcSquare2    ::SizeInscribedSquare; break;
   case figureParquet1   : GetSizeInscribedSquare = nsFigure::TcParquet1   ::SizeInscribedSquare; break;
   case figureParquet2   : GetSizeInscribedSquare = nsFigure::TcParquet2   ::SizeInscribedSquare; break;
   case figureTrapezoid1 : GetSizeInscribedSquare = nsFigure::TcTrapezoid1 ::SizeInscribedSquare; break;
   case figureTrapezoid2 : GetSizeInscribedSquare = nsFigure::TcTrapezoid2 ::SizeInscribedSquare; break;
   case figureTrapezoid3 : GetSizeInscribedSquare = nsFigure::TcTrapezoid3 ::SizeInscribedSquare; break;
   case figureRhombus    : GetSizeInscribedSquare = nsFigure::TcRhombus    ::SizeInscribedSquare; break;
   case figureQuadrangle1: GetSizeInscribedSquare = nsFigure::TcQuadrangle1::SizeInscribedSquare; break;
   case figurePentagon   : GetSizeInscribedSquare = nsFigure::TcPentagon   ::SizeInscribedSquare; break;
   case figurePentagonT5 : GetSizeInscribedSquare = nsFigure::TcPentagonT5 ::SizeInscribedSquare; break;
   case figurePentagonT10: GetSizeInscribedSquare = nsFigure::TcPentagonT10::SizeInscribedSquare; break;
   case figureHexagon    : GetSizeInscribedSquare = nsFigure::TcHexagon    ::SizeInscribedSquare; break;
   case figureTrSq       : GetSizeInscribedSquare = nsFigure::TcTrSq       ::SizeInscribedSquare; break;
   case figureTrSq2      : GetSizeInscribedSquare = nsFigure::TcTrSq2      ::SizeInscribedSquare; break;
   case figureSqTrHex    : GetSizeInscribedSquare = nsFigure::TcSqTrHex    ::SizeInscribedSquare; break;
   default /*figureNil*/ : GetSizeInscribedSquare = nsFigure::TcBase       ::SizeInscribedSquare; break;
   }
}

void TcMosaic::SetAssistant(TsAssistant newAssistant) {
   Settings.Assistant = newAssistant;
}

const TsAssistant& TcMosaic::GetAssistant() const {
   return Settings.Assistant;
}

const TB* TcMosaic::GetField(int x, int y) const {
   return Field[x][y];
}

const TcImage* TcMosaic::GetImageBtnNew(int index) const {
   return &ImageBtnNew[index];
}

const TcImage* TcMosaic::GetImageBtnPause(int index) const {
   return &ImageBtnPause[index];
}

LPCTSTR TcMosaic::GetPlayerName() const {
   return Settings.playerName;
}

void TcMosaic::SetPlayerName(LPCTSTR szCurrentPlayerName) {
   if (pause) SetPause(!pause);
   if (!_tcscmp(Settings.playerName, szCurrentPlayerName)) return;
   _tcscpy(Settings.playerName, szCurrentPlayerName);
   GameNew();
}