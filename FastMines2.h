////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "FastMines2.h"
////////////////////////////////////////////////////////////////////////////////

#ifndef __FILE__FASTMINES2__
#define __FILE__FASTMINES2__

#ifndef __AFX_H__
   #include <Windows.h>
#endif
#include "ID_resource.h"
#include "CommonLib.h"
#include "Image.h"
#include "Mosaic.h"
#include "./Dialog/PlayerName.h"
#include "./Control/ButtonImage.h"
#include "./Control/ButtonImageCheck.h"
#include "./Control/CaptionButton.h"

////////////////////////////////////////////////////////////////////////////////
//                            types & constants
////////////////////////////////////////////////////////////////////////////////
struct CSkin: public nsMosaic::CSkinMosaic {
   CImageMini m_ImgBtnNew[4], m_ImgBtnPause[4];
   bool m_bToAll; // примен€ть ли m_colorBk ко всему проекту?
   CSkin(): m_bToAll(false) {}
};

#define MAX_LANGUAGE_LENGTH 128 // max значение имени €зыка. ‘актически - им€ файла €зыка
struct CSerializeProj {
   TCHAR m_szVersion[chDIMOF(TEXT(ID_VERSIONINFO_VERSION3))];
   CSkin m_Skin;
   TCHAR m_szPlayerName[nsPlayerName::MAX_PLAYER_NAME_LENGTH]; // текущий игрок
   bool m_bAlwaysMaxSize;
   bool m_bToTray;
   bool m_bShowToolbar;
   bool m_bShowMenu;
   bool m_bShowCaption;
   bool m_bAutoloadAdmin;
   POINTEX m_PointCenter;
   TCHAR m_szLanguage[MAX_LANGUAGE_LENGTH]; // текуща€ локализаци€
   CSerializeProj():
      m_bAlwaysMaxSize(false),
      m_bToTray       (false),
      m_bShowToolbar  (true),
      m_bShowMenu     (true),
      m_bShowCaption  (true),
      m_bAutoloadAdmin(true),
      m_PointCenter   (POINTEX(GetScreenSize()/2))
   { lstrcpy(m_szPlayerName, TEXT("Anonymous"));
     lstrcpy(m_szVersion , TEXT(ID_VERSIONINFO_VERSION3));
     memset(m_szLanguage, 0, MAX_LANGUAGE_LENGTH);
   }
};

class CFastMines2Project {
private:
   nsMosaic::CMosaic m_Mosaic;
   HWND     m_hWnd, m_hWndTop, m_hWndEdtCount, m_hWndEdtTimer;
   HMENU    m_hMenu;
   HICON    m_hIconProject;
   CButtonImage      m_BtnNew;
   CButtonImageCheck m_BtnPause;
   CImage m_ImgBtnNew[4], m_ImgBtnPause[4];
   CSerializeProj m_Serialize;
   CCaptionButtonText *m_pCaptionButton;

   static VOID OnClickCaptionButton(LPVOID pParam);

   friend LRESULT CALLBACK WndProcProject(HWND, UINT, WPARAM, LPARAM);
   friend LRESULT CALLBACK WndProcTop    (HWND, UINT, WPARAM, LPARAM);
//   friend DWORD WINAPI ChildThread(PVOID);

   void TrayMessage(UINT, const TCHAR*);

   BOOL OnCreate           (HWND, LPCREATESTRUCT);                // WM_CREATE
   void OnDestroy          (HWND);                                // WM_DESTROY
   void OnActivate         (HWND, UINT, HWND, BOOL);              // WM_ACTIVATE
   void OnMove             (HWND, int, int);                      // WM_MOVE
   void OnSize             (HWND, UINT, int,  int);               // WM_SIZE
   void OnSysCommand       (HWND, UINT, int,  int);               // WM_SYSCOMMAND (SC_MAXIMIZE)
   void OnCommand          (HWND, int , HWND, UINT);              // WM_COMMAND
   void OnGetMinMaxInfo    (HWND, LPMINMAXINFO);                  // WM_GETMINMAXINFO
   void OnSysKey           (HWND, UINT, BOOL, int, UINT);         // WM_SYSKEYUP
   void OnKey              (HWND, UINT, BOOL, int, UINT);         // WM_KEYUP
   void OnNCLButtonDown    (HWND, BOOL, int, int, UINT);          // WM_NCLBUTTONDOWN
   void OnNCLButtonUp      (HWND, int, int, UINT);                // WM_NCLBUTTONUP
   BOOL OnWindowPosChanging(HWND, LPWINDOWPOS);                   // WM_WINDOWPOSCHANGING
   void OnWindowPosChanged (HWND, const LPWINDOWPOS);             // WM_WINDOWPOSCHANGED
   void OnMeasureItem      (HWND, MEASUREITEMSTRUCT*);            // WM_MEASUREITEM
   void OnDrawItem         (HWND, const DRAWITEMSTRUCT*);         // WM_DRAWITEM
   #ifdef REPLACEBKCOLORFROMFILLWINDOW
   void OnMenuSelect       (HWND, HMENU, int, HMENU, UINT);       // WM_MENUSELECT
   void OnPaint            (HWND);                                // WM_PAINT
   #endif // REPLACEBKCOLORFROMFILLWINDOW
   void OnNotifyIcon       (HWND, UINT);                          // WM_NOTIFYICON
   #ifdef REPLACEBKCOLORFROMFILLWINDOW
   BOOL OnEraseBkgnd       (HWND, HDC);                           // WM_ERASEBKGND
   #endif // REPLACEBKCOLORFROMFILLWINDOW
   void OnMosaicAdjustArea    (HWND);                             // WM_MOSAIC_ADJUSTAREA
   void OnMosaicClick         (HWND, UINT, BOOL, BOOL);           // WM_MOSAIC_CLICK
   void OnMosaicChangeCounters(HWND);                             // WM_MOSAIC_CHANGECOUNTERS
   void OnMosaicGameNew       (HWND);                             // WM_MOSAIC_GAMENEW
   void OnMosaicGameBegin     (HWND);                             // WM_MOSAIC_GAMEBEGIN
   void OnMosaicGameEnd       (HWND);                             // WM_MOSAIC_GAMEEND
   void OnMosaicPause         (HWND);                             // WM_MOSAIC_PAUSE
   void OnMouseWheel          (HWND, WORD, short, short, short);  // WM_MOUSEWHEEL

   //void ProjectOnTimer(HWND hwnd, UINT id);                       // WM_TIMER
   //void ResetAssistant();

   void TopOnCommand      (HWND, int, HWND, UINT);                // WM_COMMAND

   int GetMaximalArea() const;
   bool AreaIncrement();
   bool AreaDecrement();
   bool AreaMax();
   bool AreaMin();
   BOOL SerializeIn ();
   BOOL SerializeOut() const;
   void CheckMenuRadioItem_Mosaic(nsMosaic::EMosaic) const;

   void Apply_AlwaysMaxSize();
   void Apply_ToTray();
   void Apply_ShowToolbar();
   void Apply_ShowMenu();
   void Apply_ShowCaption();
   void Apply_UseAssistant();
   void Apply_UseUnknown();
   void ApplySkin();

   void ReloadMosaicMenu(HMENU hMenu) const;

   void GameLoad(LPCTSTR szFileName = NULL);
   void GameSave();

public:
   CFastMines2Project();
   HWND GetHandle() const {return m_hWnd;}
   void Create(LPCTSTR szFileName = NULL);

   void                            SetAssistantInfo(  const nsMosaic::CAssistantInfo  &newAssistantInfo) {m_Mosaic.SetAssistantInfo(newAssistantInfo);}
   const nsMosaic::CAssistantInfo& GetAssistantInfo() const {return m_Mosaic.GetAssistantInfo();}
   const CSkin&          GetSkin         () const {return m_Serialize.m_Skin;}
   void                  SetSkin         (  const CSkin &newSkin) {m_Serialize.m_Skin = newSkin; ApplySkin();}
   bool                  GetAutoloadAdmin() const {return m_Serialize.m_bAutoloadAdmin;}
   LPCTSTR               GetPlayerName   () const {return m_Serialize.m_szPlayerName;}
   void                  SetPlayerName(LPCTSTR);

   SIZE                  GetSize(const SIZE &sizeMosaic, int iArea) const {return GetSize(m_Mosaic.GetSizeWindow(   sizeMosaic  , iArea));}   // размер окна проекта при заданном размере мозаики(в €чейках) и при указанной площади €чейки
   SIZE                  GetSize(int iArea)                         const {return GetSize(m_Mosaic.GetSizeWindow(GetSizeMosaic(), iArea));}   // размер окна проекта при текущем  размере мозаики(в €чейках) и при указанной площади €чейки
   SIZE                  GetSize(const SIZE  &sizeMosaicInPixel)    const;                                                                   // размер окна проекта при указанном размере окна мозаики(в пиксел€х)
   SIZE                  GetSize()                                  const {return GetSize(GetSizeMosaicWindow());}                           // размер окна проекта при текущих значени€х размера мозаики(в €чейках) и площади €чейки
   SIZE                  GetSizeMosaic          () const {return m_Mosaic.GetSize();}
   SIZE                  GetSizeMosaicWindow    () const {return m_Mosaic.GetSizeWindow(GetSizeMosaic(), GetArea());}
   int                   GetArea                () const {return m_Mosaic.GetArea();}
   int                   GetMines               () const {return m_Mosaic.GetMines();}
   int                   GetMosaicNeighborNumber() const {return m_Mosaic.GetNeighborNumber();}
   nsMosaic::ESkillLevel GetSkillLevel          () const {return m_Mosaic.GetSkillLevel();}
   nsMosaic::EMosaic     GetMosaic              () const {return m_Mosaic.GetMosaic();}
   int DefineNumberMines(nsMosaic::ESkillLevel skill, const SIZE &sizeMosaic) const {return nsMosaic::DefineNumberMines(skill, m_Mosaic.GetMosaic(), sizeMosaic);};
   int DefineNumberMines() const {return nsMosaic::DefineNumberMines(GetSkillLevel(), m_Mosaic.GetMosaic(), GetSizeMosaic());};

   const CImage* GetImageBtnNew  (int pos) const {return &m_ImgBtnNew  [pos];}
   const CImage* GetImageBtnPause(int pos) const {return &m_ImgBtnPause[pos];}
   static const CImage* GetImageMosaic(nsMosaic::EMosaic eMosaic);
   static const HICON   GetIconMosaic (nsMosaic::EMosaic eMosaic);

   void Menu_ChangeLanguage();
};

inline void LoadDefaultImageMine     (HINSTANCE hInstance, CImage &imgMine     ) {nsMosaic::LoadDefaultImageMine   (hInstance, imgMine   );}
inline void LoadDefaultImageFlag     (HINSTANCE hInstance, CImage &imgFlag     ) {nsMosaic::LoadDefaultImageFlag   (hInstance, imgFlag   );}
inline void LoadDefaultImagePause    (HINSTANCE hInstance, CImage &imgPause    ) {nsMosaic::LoadDefaultImagePause  (hInstance, imgPause  );}
inline void LoadDefaultImageBckgrnd  (HINSTANCE hInstance, CImage &imgBckgrnd  ) {nsMosaic::LoadDefaultImageBckgrnd(hInstance, imgBckgrnd);}
inline void LoadDefaultImageBtnNew0  (HINSTANCE hInstance, CImage &imgBtnNew0  ) {imgBtnNew0  .LoadResource(hInstance, TEXT("New0"  ), imageIcon); imgBtnNew0  .SetTransparent(true); imgBtnNew0  .SetPlace(placeStretch);}
inline void LoadDefaultImageBtnNew1  (HINSTANCE hInstance, CImage &imgBtnNew1  ) {imgBtnNew1  .LoadResource(hInstance, TEXT("New1"  ), imageIcon); imgBtnNew1  .SetTransparent(true); imgBtnNew1  .SetPlace(placeStretch);}
inline void LoadDefaultImageBtnNew2  (HINSTANCE hInstance, CImage &imgBtnNew2  ) {imgBtnNew2  .LoadResource(hInstance, TEXT("New2"  ), imageIcon); imgBtnNew2  .SetTransparent(true); imgBtnNew2  .SetPlace(placeStretch);}
inline void LoadDefaultImageBtnNew3  (HINSTANCE hInstance, CImage &imgBtnNew3  ) {imgBtnNew3  .LoadResource(hInstance, TEXT("New3"  ), imageIcon); imgBtnNew3  .SetTransparent(true); imgBtnNew3  .SetPlace(placeStretch);}
inline void LoadDefaultImageBtnPause0(HINSTANCE hInstance, CImage &imgBtnPause0) {imgBtnPause0.LoadResource(hInstance, TEXT("Pause0"), imageIcon); imgBtnPause0.SetTransparent(true); imgBtnPause0.SetPlace(placeStretch);}
inline void LoadDefaultImageBtnPause1(HINSTANCE hInstance, CImage &imgBtnPause1) {imgBtnPause1.LoadResource(hInstance, TEXT("Pause1"), imageIcon); imgBtnPause1.SetTransparent(true); imgBtnPause1.SetPlace(placeStretch);}
inline void LoadDefaultImageBtnPause2(HINSTANCE hInstance, CImage &imgBtnPause2) {imgBtnPause2.LoadResource(hInstance, TEXT("Pause2"), imageIcon); imgBtnPause2.SetTransparent(true); imgBtnPause2.SetPlace(placeStretch);}
inline void LoadDefaultImageBtnPause3(HINSTANCE hInstance, CImage &imgBtnPause3) {imgBtnPause3.LoadResource(hInstance, TEXT("Pause3"), imageIcon); imgBtnPause3.SetTransparent(true); imgBtnPause3.SetPlace(placeStretch);}

#endif // __FILE__FASTMINES2__
