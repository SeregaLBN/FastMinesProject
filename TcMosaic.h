////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "TcMosaic.h"
////////////////////////////////////////////////////////////////////////////////

#ifndef FILE_MOSAIC
#define FILE_MOSAIC

#include ".\Preproc.h"
#include <windows.h>
#include <vector>
#include ".\ID_resource.h"
#include ".\Lib.h"
#include ".\TcImage.h"
#include ".\Figure\TcBase.h"
#include ".\Dialog\PlayerName.h"

const int CMinArea = 230;//245;

#define MK_ROBOT 0x0100 // my Key State Masks for Mouse Messages
#define WM_CHANGEBRUSH (WM_USER+2)

enum TeFigure {
   // Triangle
   figureTriangle1,
   figureTriangle2,
   figureTriangle3,
   figureTriangle4,
   // Quadrangle
   figureSquare1,
   figureSquare2,
   figureParquet1,
   figureParquet2,
   figureTrapezoid1,
   figureTrapezoid2,
   figureTrapezoid3,
   figureRhombus,
   figureQuadrangle1,
   // Pentagon
   figurePentagon,
   figurePentagonT5,
   figurePentagonT10,
   // Hexagon
   figureHexagon,
   // Other
   figureTrSq,
   figureTrSq2,
   figureSqTrHex,
   //
   figureNil  //  всегда последн€€ в перечислении !!!
};

const POINT CSizeField[skillLevelCustom] = {{10, 10},  // Beginner,
                                            {20, 15},  // Amateur,
                                            {30 ,20},  // Professional
                                            {45, 25}}; // Crazy,

struct TsSkin {
   struct TsSkinImage{
      TCHAR path[MAX_PATH];
      bool  transparent;
      TePlace place;
   } Mine, Flag, Pause, Bckgrnd, BtnNew[4], BtnPause[4];
   COLORREF colorBk;
   bool toAll; // примен€ть ли colorBk ко всему проекту?
   LOGFONT Font;
   struct TsSkinText {
      COLORREF captionOpen [_Mine];
      COLORREF captionClose[_Flag];
   } colorText;
   struct TsSkinBorder{
      COLORREF shadow,
               light;
      int width;
   } Border;
};

struct TsAssistant {
   bool use;            // on/off Assistant
   int timeoutUnactive; // таймаут срабатывани€ при бездействии (миллисекунды)
   int timeoutJob;      // frequency work assistant (milliseconds)
   bool autoStart;      // autostart new game ?
   bool stopJob;        // останавливать когда нет однозначного следующего хода ?
   bool ignorePause;    // ignore Pause in game ?
   bool beep;           // MessageBeep by virtual click ?
};

struct TsIOOptions { // in/out options
   TCHAR szVersion[chDIMOF(TEXT(ID_VERSIONINFO_VERSION3))];
   POINT SizeField; // ширина и высота пол€ (в €чейках)
   int  area;       // площадь
   TeFigure Figure; // из каких фигур состоит мозаика пол€
   int  mines;      // кол-во мин на поле
   TsSkin Skin;
   TCHAR playerName[nsPlayerName::maxPlayerNameLength]; // текущий игрок
   bool useUnknown;
   TsAssistant Assistant;
   bool toTray;
   bool showToolbar;
   bool showMenu;
   bool showCaption;
   bool autoloadAdmin;
};

void LoadDefaultImageMine     (HINSTANCE, TcImage&);
void LoadDefaultImageFlag     (HINSTANCE, TcImage&);
void LoadDefaultImagePause    (HINSTANCE, TcImage&);
void LoadDefaultImageBckgrnd  (HINSTANCE, TcImage&);
void LoadDefaultImageBtnNew0  (HINSTANCE, TcImage&);
void LoadDefaultImageBtnNew1  (HINSTANCE, TcImage&);
void LoadDefaultImageBtnNew2  (HINSTANCE, TcImage&);
void LoadDefaultImageBtnNew3  (HINSTANCE, TcImage&);
void LoadDefaultImageBtnPause0(HINSTANCE, TcImage&);
void LoadDefaultImageBtnPause1(HINSTANCE, TcImage&);
void LoadDefaultImageBtnPause2(HINSTANCE, TcImage&);
void LoadDefaultImageBtnPause3(HINSTANCE, TcImage&);

class TcMosaic {
private:
   const HINSTANCE c_hInstance;
   const HWND      c_hWndProject,
                   c_hWndBtnNew  , c_hWndBtnPause,
                   c_hWndEdtCount, c_hWndEdtTimer;
   const TCHAR * const c_szCaptionProject;
   const TCHAR * const c_szClassWndField;
   const RECT  * const c_pBorder;

   HWND    hWndField;
   HDC     hDCField0, // divice context (DC)
           hDCField1, // compatible DC #1
           hDCField2, // compatible DC #2
           hDCPause;  // compatible DC #3
   HBRUSH  hBrushField;
   HPEN    hPenBlack,
           hPenWhite;
   HFONT   hFontField;  

   POINT   sizeBitmap;

   TcImage ImageMine,
           ImageFlag,
           ImagePause,
           ImageBckgrnd,
           ImageBtnNew  [4],
           ImageBtnPause[4];
   TsIOOptions Settings;  // информаци€ дл€ сохранени€
   POINT coordDown;       // координаты €чейки на которой было нажато (но не об€зательно что отпущено)
   bool  pause;
   bool  gameRun;
   BYTE  whoPlayer;       // кто играл ? юзер и/или робот ?
   int   countOpen;       // счЄтчик открытых €чеек на поле
   int   countFlag;       // счЄтчик проставленных флажков на поле
   int   countUnknown;
   int   countClick;
   int   countTimer; // счЄтчик таймера
   bool  isRefreshField,
         isRefreshPause;
private:
   void FieldDestroy(const POINT&);

   void GameBegin(POINT); // in: координаты в масиве первой нажатой €чейки
   void GameEnd(bool);
   void VerifyFlag();

   POINT WinToArray(int, int) const; // преобразовать экранные координаты в координаты Field'a
   int (*GetSizeInscribedSquare)(int); // размер вписанного в фигуру квадрата
   int  GetMaximalArea() const;
   void SetDCFont();
   void ChangeCaptionTimeMine(bool Victory=false, bool valid=false);
   void ChangeSizeField();
   void ResizeBitmap();

   void Cls_FieldOnPaint      (HWND);                       // WM_PAINT
   void Cls_FieldOnLButtonUp  (HWND, int , int, UINT);      // WM_LBUTTONUP
   void Cls_FieldOnLButtonDown(HWND, BOOL, int, int, UINT); // WM_LBUTTONDOWN & WM_LBUTTONDBLCLK
   void Cls_FieldOnRButtonDown(HWND, BOOL, int, int, UINT); // WM_RBUTTONDOWN & WM_RBUTTONDBLCLK

   void  SetClasureToFigure();

         void   AcceptSkin();
public:
   const TsSkin  GetSkinDefault() const;
   const TsSkin& GetSkin()        const;
         void    SetSkin(TsSkin);

   LRESULT WndProcField(HWND, UINT, WPARAM, LPARAM);
   void    TimerProc   (HWND, UINT, UINT, DWORD);

   void FieldCreate();
   void GameNew();

   TeSkillLevel GetSkillLevel();
   void         SetSkillLevel(const TeSkillLevel&);
   void         SetFigure(TeFigure);
   TeFigure     GetFigure() const;

   POINT (*GetSizeWindowField) (const POINT&, const int&);
   POINT   GetSizeWindowProject(const POINT&) const;
   void ChangeUseUnknown();
   int  ChangeShowToolbar();
   int  ChangeShowMenu();
   int  ChangeShowCaption();
   void ChangeToTray();
   bool       ToTray();

   bool AreaIncrement();
   void AreaDecrement();
   void AreaMin();
   bool AreaMax();
   int   GetArea()  const;
   int   GetMines() const;
   void  SetMines(int);
   POINT GetSizeBitmap() const;
   POINT GetSizeField()  const;
   void  SetSizeField(const POINT&);
   int   GetAutoloadAdmin() const;
   void  SetAutoloadAdmin(const bool);

   LPCTSTR GetPlayerName() const;
   void    SetPlayerName(LPCTSTR);

   HWND GetHandleField() const;
   
   void SetPause(bool);
   bool GetPause()       const;
   bool GetGameRun()     const;
   bool IsFieldEnabled() const;

   TcMosaic(const HINSTANCE     hInstance,
            const HWND          hWndPrnt,
            const HWND          hWndBtnNew,
            const HWND          hWndBtnPause,
            const HWND          hWndEdtCount,
            const HWND          hWndEdtTimer,
            const TCHAR * const szCaptionProject,
            const TCHAR * const szClassWndField,
            const RECT  * const pBorder);
   ~TcMosaic();

   void               SetAssistant(TsAssistant);
   const TsAssistant& GetAssistant() const;
private:
   std::vector<std::vector<TB* > > Field;
public:
   const TB* GetField(int, int) const;
   const TcImage* GetImageBtnNew  (int) const;
   const TcImage* GetImageBtnPause(int) const;
};

float GetPercentMine (const TeSkillLevel, const TeFigure); // процент мин на заданном уровне сложности дл€ заданной фигуры
int DefineNumberMines(const TeSkillLevel, const TeFigure, const POINT);

#endif // FILE_MOSAIC
