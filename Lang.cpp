////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "Lang.cpp"
//
// Ïðî÷èòêà ôàéëà ÿçûêà
////////////////////////////////////////////////////////////////////////////////

#include "StdAfx.h"
#include "Lang.h"
#include "CommonLib.h"
#include "ID_Resource.h"

////////////////////////////////////////////////////////////////////////////////
//                             global variables
////////////////////////////////////////////////////////////////////////////////
extern HINSTANCE ghInstance;

////////////////////////////////////////////////////////////////////////////////
//                               forfard declaration
////////////////////////////////////////////////////////////////////////////////
void SaveLanguage(const TCHAR *szFileName);

////////////////////////////////////////////////////////////////////////////////
//                               implementetion
////////////////////////////////////////////////////////////////////////////////
MAP_IntStr CLang::m_StrArr;

CLang::CLang(const TCHAR *szFileName)
   : m_hFile (INVALID_HANDLE_VALUE)
{
   CString strPath(GetModuleDir(ghInstance) + TEXT("Language\\") + szFileName);
   m_hFile = CreateFile(
      strPath,
      GENERIC_READ,
      0,
      NULL,
      OPEN_EXISTING,
      FILE_ATTRIBUTE_NORMAL,
      NULL
   );
   if (m_hFile != INVALID_HANDLE_VALUE) {
      Parse();
      if (m_hFile != INVALID_HANDLE_VALUE) {
         CloseHandle(m_hFile);
      }
      m_hFile = NULL;
   }
   Finalisation();
}

CLang::~CLang() {}

CString VerifyLine(const CString &str) {
   CString strRes(str);
   int len = str.GetLength();
   int ofs = 0;
   for (int i=0; ((i+ofs)<len); i++) {
      if (str[i+ofs] != TEXT('\\')) {
         strRes.SetAt(i, str[i+ofs]);
      } else {
         ofs++;
         TCHAR ch = str[i+ofs];
         switch (ch) {
         case TEXT('n'):   strRes.SetAt(i, TEXT('\n'));    break;
         case TEXT('r'):   strRes.SetAt(i, TEXT('\r'));    break;
         case TEXT('t'):   strRes.SetAt(i, TEXT('\t'));    break;
         case TEXT('0'):   strRes.SetAt(i, TEXT('\0'));    break;
         default:
            strRes.SetAt(i  , TEXT('\\'));
            strRes.SetAt(i+1, ch);
         }
      }
   }
   if (ofs) {
      strRes.SetAt(len-ofs, 0);
   }

   return strRes;
}

bool CLang::ReadLine() {
   static const TCHAR szEOL[3] = {13, 10, 0};
   DWORD dwNBR;
   const DWORD sizeBuff = 1023;
   TCHAR szBuf[sizeBuff+1];
   BOOL bRes = ReadFile(m_hFile, szBuf, sizeBuff*sizeof(TCHAR), &dwNBR, NULL);
   if (!bRes || (dwNBR < 1)) return false;
   szBuf[sizeBuff] = 0;
   TCHAR *szFind = _tcsstr(szBuf, szEOL);
   if (!szFind)
      return false;
   DWORD dwRes = SetFilePointer(m_hFile, ((szFind+2-szBuf)-dwNBR)*sizeof(TCHAR), NULL, FILE_CURRENT);
   if (dwRes == 0xFFFFFFFF)
      return false;

   *szFind = 0;
   {
      szFind = _tcschr(szBuf, TEXT('='));
      if (!szFind)
         return true;
      *szFind = 0;

      int i, iRes = _stscanf(szBuf, TEXT("%d"), &i);
      if ((iRes == 0) || (iRes == EOF)) {
         return true;
      }
      CString str = szFind+1;

      m_StrArr[i] = VerifyLine(str);
   }

   return true;
}

void CLang::Parse() {
   do {} while (ReadLine());
}


bool CLang::RestoreDefaultLanguage() {
   CLang::m_StrArr[IDS__LOGO] = TEXT("FastMines");

   CLang::m_StrArr[IDS__OK         ] = TEXT("Ok");
   CLang::m_StrArr[IDS__CANCEL     ] = TEXT("Cancel");
   CLang::m_StrArr[IDS__ERROR      ] = TEXT("Error");
   CLang::m_StrArr[IDS__INFORMATION] = TEXT("Information");
   CLang::m_StrArr[IDS__SEC        ] = TEXT("sec.");
   CLang::m_StrArr[IDS__SECOND     ] = TEXT("seconds");
   CLang::m_StrArr[IDS__MILISECOND ] = TEXT("milliseconds");
   CLang::m_StrArr[IDS__MAX        ] = TEXT("max");

   CLang::m_StrArr[IDS__MINES  ] = TEXT("Mines");
   CLang::m_StrArr[IDS__TIME   ] = TEXT("Time");
   CLang::m_StrArr[IDS__VICTORY] = TEXT("Victory");
   CLang::m_StrArr[IDS__DEFEAT ] = TEXT("Defeat");

   CLang::m_StrArr[IDS__INI_FILE__ERROR_CREATE ] = TEXT("Can't create INI file");
   CLang::m_StrArr[IDS__INI_FILE__ERROR_READ   ] = TEXT("Can't read INI file");
   CLang::m_StrArr[IDS__INI_FILE__ERROR_WRITE  ] = TEXT("Can't write INI file");
   CLang::m_StrArr[IDS__INI_FILE__ERROR_VERSION] = TEXT("INI file - version error");
   
   CLang::m_StrArr[IDS__LOAD_FASTMINES_GAME      ] = TEXT("Load FastMines Game");
   CLang::m_StrArr[IDS__SAVE_FASTMINES_GAME      ] = TEXT("Save FastMines Game");
   CLang::m_StrArr[IDS__FASTMINES_EXTENSIONS_FILE] = TEXT("FastMines file");
   CLang::m_StrArr[IDS__FMS_FILE__ERROR_CREATE   ] = TEXT("Can't create FMS file");
   CLang::m_StrArr[IDS__FMS_FILE__ERROR_READ     ] = TEXT("Can't read FMS file");
   CLang::m_StrArr[IDS__FMS_FILE__ERROR_WRITE    ] = TEXT("Can't write FMS file");
   CLang::m_StrArr[IDS__FMS_FILE__ERROR_VERSION  ] = TEXT("FMS file - version error");

   CLang::m_StrArr[IDS__RESTORE_LAST_GAME] = TEXT("Restore last game?");
   CLang::m_StrArr[IDS__QUESTION         ] = TEXT("Question");

   CLang::m_StrArr[IDS__MOSAIC_NAME_00     ] = TEXT("Triangle 60°-60°-60°");
   CLang::m_StrArr[IDS__MOSAIC_NAME_01     ] = TEXT("Triangle 60°-60°-60° (offset)");
   CLang::m_StrArr[IDS__MOSAIC_NAME_02     ] = TEXT("Triangle 45°-90°-45°");
   CLang::m_StrArr[IDS__MOSAIC_NAME_03     ] = TEXT("Triangle 30°-30°-120°");
   CLang::m_StrArr[IDS__MOSAIC_NAME_04     ] = TEXT("Square 1");
   CLang::m_StrArr[IDS__MOSAIC_NAME_05     ] = TEXT("Square 2 (offset)");
   CLang::m_StrArr[IDS__MOSAIC_NAME_06     ] = TEXT("Rectangle 1 (Parquet 'Herring-bone')");
   CLang::m_StrArr[IDS__MOSAIC_NAME_07     ] = TEXT("Rectangle 2");
   CLang::m_StrArr[IDS__MOSAIC_NAME_08     ] = TEXT("Trapezoid 1");
   CLang::m_StrArr[IDS__MOSAIC_NAME_09     ] = TEXT("Trapezoid 2");
   CLang::m_StrArr[IDS__MOSAIC_NAME_10     ] = TEXT("Trapezoid 3");
   CLang::m_StrArr[IDS__MOSAIC_NAME_11     ] = TEXT("Rhombus");
   CLang::m_StrArr[IDS__MOSAIC_NAME_12     ] = TEXT("Quadrilateral 120°-90°-60°-90°");
   CLang::m_StrArr[IDS__MOSAIC_NAME_13     ] = TEXT("Pentagon (type 2 and 4)");
   CLang::m_StrArr[IDS__MOSAIC_NAME_14     ] = TEXT("Pentagon (type 5)");
   CLang::m_StrArr[IDS__MOSAIC_NAME_15     ] = TEXT("Pentagon (type 10)");
   CLang::m_StrArr[IDS__MOSAIC_NAME_16     ] = TEXT("Hexagon");
   CLang::m_StrArr[IDS__MOSAIC_NAME_17     ] = TEXT("Square-Triangle 1");
   CLang::m_StrArr[IDS__MOSAIC_NAME_18     ] = TEXT("Square-Triangle 2");
   CLang::m_StrArr[IDS__MOSAIC_NAME_19     ] = TEXT("Square-Triangle-Hexagon");
   CLang::m_StrArr[IDS__MOSAIC_NAME_INVALID] = TEXT("!!! Invalid name !!!");

   CLang::m_StrArr[IDS__DIALOG_CUSTOM_SKILL                           ] = TEXT("Custom skill level");
   CLang::m_StrArr[IDS__DIALOG_CUSTOM_SKILL__X_WIDTH                  ] = TEXT("Size X (width) = ");
   CLang::m_StrArr[IDS__DIALOG_CUSTOM_SKILL__Y_HEIGHT                 ] = TEXT("Size Y (height) = ");
   CLang::m_StrArr[IDS__DIALOG_CUSTOM_SKILL__NUMBER_MINES             ] = TEXT("Number of Mines = ");
   CLang::m_StrArr[IDS__DIALOG_CUSTOM_SKILL__FULL_SCREEN__CURRENT_SIZE] = TEXT("Full Screen\n( current cell size )");
   CLang::m_StrArr[IDS__DIALOG_CUSTOM_SKILL__FULL_SCREEN__MINIMAL_SIZE] = TEXT("Full Screen\n( minimal cell size )");

   CLang::m_StrArr[IDS__DIALOG_ABOUT           ] = TEXT("About");
   CLang::m_StrArr[IDS__DIALOG_ABOUT__VERSION  ] = TEXT("Version");
   CLang::m_StrArr[IDS__DIALOG_ABOUT__COPYRIGHT] = TEXT("Copyright");
   CLang::m_StrArr[IDS__DIALOG_ABOUT__COMMEMTS ] = TEXT("Comments - FreeWare");
   CLang::m_StrArr[IDS__DIALOG_ABOUT__RESPONSE ] = TEXT("Comments: e-mail");
   CLang::m_StrArr[IDS__DIALOG_ABOUT__SITE     ] = TEXT("FastMines site:");

   CLang::m_StrArr[IDS__DIALOG_SELECTPLAYER                        ] = TEXT("Player Administration");
   CLang::m_StrArr[IDS__DIALOG_SELECTPLAYER__SELECT                ] = TEXT("Select Player");
   CLang::m_StrArr[IDS__DIALOG_SELECTPLAYER__NEWPLAYER             ] = TEXT("New Player");
   CLang::m_StrArr[IDS__DIALOG_SELECTPLAYER__NEWPASSWORD           ] = TEXT("New Password");
   CLang::m_StrArr[IDS__DIALOG_SELECTPLAYER__RENAME                ] = TEXT("Rename");
   CLang::m_StrArr[IDS__DIALOG_SELECTPLAYER__REMOVE                ] = TEXT("Remove");
   CLang::m_StrArr[IDS__DIALOG_SELECTPLAYER__AUTOSTART             ] = TEXT("Window autoloading");
   CLang::m_StrArr[IDS__DIALOG_SELECTPLAYER__TABLE                 ] = TEXT("Players");
   CLang::m_StrArr[IDS__DIALOG_SELECTPLAYER__ENTER_PASSWORD        ] = TEXT("Enter password");
   CLang::m_StrArr[IDS__DIALOG_SELECTPLAYER__ENTER_NAME            ] = TEXT("Enter name:");
   CLang::m_StrArr[IDS__DIALOG_SELECTPLAYER__ENTER_NEW_NAME        ] = TEXT("Enter new Name");
   CLang::m_StrArr[IDS__DIALOG_SELECTPLAYER__NEW_NAME              ] = TEXT("New name:");
   CLang::m_StrArr[IDS__DIALOG_SELECTPLAYER__CANT_USE_VIRT_NAME    ] = TEXT("Sorry, this name is used by a virtual!");
   CLang::m_StrArr[IDS__DIALOG_SELECTPLAYER__INVALID_NAME          ] = TEXT("Invalid name");
   CLang::m_StrArr[IDS__DIALOG_SELECTPLAYER__PASSWORD_NOT_CONFIRMED] = TEXT("Password not confirmed!");
   CLang::m_StrArr[IDS__DIALOG_SELECTPLAYER__THIS_VIRTUAL_PLAYER   ] = TEXT("Sorry, this is a virtual player!");
   CLang::m_StrArr[IDS__DIALOG_SELECTPLAYER__INCORRECT_PASSWORD    ] = TEXT("Incorrect password");
   CLang::m_StrArr[IDS__DIALOG_SELECTPLAYER__PASSWORD              ] = TEXT("Password:");
   CLang::m_StrArr[IDS__DIALOG_SELECTPLAYER__CONFIRMATION          ] = TEXT("Confirmation:");

   CLang::m_StrArr[IDS__DIALOG_CHANGESKIN                                ] = TEXT("Change skin");
   CLang::m_StrArr[IDS__DIALOG_CHANGESKIN__BUTTON_TRANSPARENT            ] = TEXT("Transparent");
   CLang::m_StrArr[IDS__DIALOG_CHANGESKIN__BUTTON_IMAGE                  ] = TEXT("Image...");
   CLang::m_StrArr[IDS__DIALOG_CHANGESKIN__BUTTON_FONT                   ] = TEXT("Font...");
   CLang::m_StrArr[IDS__DIALOG_CHANGESKIN__BUTTON_APPLY_TO_ALL           ] = TEXT("Apply to all windows");
   CLang::m_StrArr[IDS__DIALOG_CHANGESKIN__BUTTON_COLOR                  ] = TEXT("Color...");
   CLang::m_StrArr[IDS__DIALOG_CHANGESKIN__BUTTON_LIGHT_COLOR            ] = TEXT("Light  color...");
   CLang::m_StrArr[IDS__DIALOG_CHANGESKIN__BUTTON_SHADOW_COLOR           ] = TEXT("Shadow color...");
   CLang::m_StrArr[IDS__DIALOG_CHANGESKIN__OPENFILENAME_FILTER_ALL_IMAGES] = TEXT("All images");
   CLang::m_StrArr[IDS__DIALOG_CHANGESKIN__OPENFILENAME_TITLE_OPEN_IMAGE ] = TEXT("Open image");
   CLang::m_StrArr[IDS__DIALOG_CHANGESKIN__CHECKBUTTON_IMAGE_MINE        ] = TEXT("'Mine' image");
   CLang::m_StrArr[IDS__DIALOG_CHANGESKIN__CHECKBUTTON_IMAGE_FLAG        ] = TEXT("'Flag' image");
   CLang::m_StrArr[IDS__DIALOG_CHANGESKIN__CHECKBUTTON_IMAGE_PAUSE       ] = TEXT("'Pause' image");
   CLang::m_StrArr[IDS__DIALOG_CHANGESKIN__CHECKBUTTON_BUTTON_PAUSE      ] = TEXT("'Pause' button");
   CLang::m_StrArr[IDS__DIALOG_CHANGESKIN__CHECKBUTTON_BUTTON_GAME       ] = TEXT("'Game' button");
   CLang::m_StrArr[IDS__DIALOG_CHANGESKIN__CHECKBUTTON_IMAGE_BACKGROUND  ] = TEXT("Background image");
   CLang::m_StrArr[IDS__DIALOG_CHANGESKIN__CHECKBUTTON_COLOR_BACKGROUND  ] = TEXT("Background color");
   CLang::m_StrArr[IDS__DIALOG_CHANGESKIN__CHECKBUTTON_BORDER_COLOR_WIDTH] = TEXT("Border color/width");
   CLang::m_StrArr[IDS__DIALOG_CHANGESKIN__CHECKBUTTON_CHARACTER_COLOR   ] = TEXT("Character Colors");
   CLang::m_StrArr[IDS__DIALOG_CHANGESKIN__CHECKBUTTON_FONT_TYPE         ] = TEXT("Font Type");
   CLang::m_StrArr[IDS__DIALOG_CHANGESKIN__RESET_TO_DEFAULT              ] = TEXT("Reset to default");
   CLang::m_StrArr[IDS__DIALOG_CHANGESKIN__PLACEMENT_TYPE_IMAGE          ] = TEXT("Placement");
   CLang::m_StrArr[IDS__DIALOG_CHANGESKIN__PLACEMENT_CENTER              ] = TEXT("Center");
   CLang::m_StrArr[IDS__DIALOG_CHANGESKIN__PLACEMENT_STRETCH             ] = TEXT("Stretch");
   CLang::m_StrArr[IDS__DIALOG_CHANGESKIN__PLACEMENT_TILE                ] = TEXT("Tile");
   CLang::m_StrArr[IDS__DIALOG_CHANGESKIN__WIDTH_BORDER                  ] = TEXT("Width =");

   CLang::m_StrArr[IDS__DIALOG_FILESKIN                       ] = TEXT("Save Skin");
   CLang::m_StrArr[IDS__DIALOG_FILESKIN__FILE_NAME            ] = TEXT("File name");
   CLang::m_StrArr[IDS__DIALOG_FILESKIN__OVERWRITE            ] = TEXT("File exists. Overwrite?");
   CLang::m_StrArr[IDS__DIALOG_FILESKIN__FILE_COPYING         ] = TEXT("File copying");
   CLang::m_StrArr[IDS__DIALOG_FILESKIN__ERROR_CREATE         ] = TEXT("Can't create file");
   CLang::m_StrArr[IDS__DIALOG_FILESKIN__ERROR_READ           ] = TEXT("Can't load file");
   CLang::m_StrArr[IDS__DIALOG_FILESKIN__ERROR_WRITE          ] = TEXT("Can't write file");
   CLang::m_StrArr[IDS__DIALOG_FILESKIN__ERROR_VERSION        ] = TEXT("SKN file - version error");
   CLang::m_StrArr[IDS__DIALOG_FILESKIN__RESTORED_DEFAULT_SKIN] = TEXT("The default skin has been restored");

   CLang::m_StrArr[IDS__DIALOG_ASSISTANT                       ] = TEXT("Assistant Options");
   CLang::m_StrArr[IDS__DIALOG_ASSISTANT__TIMEOUT_USER_UNACTIVE] = TEXT("Launch after\nif no response from user");
   CLang::m_StrArr[IDS__DIALOG_ASSISTANT__TIMEOUT_JOB          ] = TEXT("Assistant work frequency:");
   CLang::m_StrArr[IDS__DIALOG_ASSISTANT__NEW_GAME_AUTOSTART   ] = TEXT("Launch new game automatically");
   CLang::m_StrArr[IDS__DIALOG_ASSISTANT__STOPJOB              ] = TEXT("Stop if there are no definitely safe moves");
   CLang::m_StrArr[IDS__DIALOG_ASSISTANT__IGNOREPAUSE          ] = TEXT("Ignore pause");
   CLang::m_StrArr[IDS__DIALOG_ASSISTANT__BEEPCLICK            ] = TEXT("Virtual click beep");

   CLang::m_StrArr[IDS__DIALOG_SELECTMOSAIC        ] = TEXT("Select mosaic");
   CLang::m_StrArr[IDS__DIALOG_SELECTMOSAIC__NUMBER] = TEXT("Mosaic number:");
   CLang::m_StrArr[IDS__DIALOG_SELECTMOSAIC__NAME  ] = TEXT("Mosaic name:");

   CLang::m_StrArr[IDS__CHAMPIONS                  ] = TEXT("Champions");
   CLang::m_StrArr[IDS__CHAMPIONS__PLAYER_NAME     ] = TEXT("Player name");
   CLang::m_StrArr[IDS__CHAMPIONS__GAME_TIME       ] = TEXT("Game time");
   CLang::m_StrArr[IDS__CHAMPIONS__ERROR_CREATE    ] = TEXT("Can't create Champions file");
   CLang::m_StrArr[IDS__CHAMPIONS__ERROR_READ      ] = TEXT("Can't load Champions file");
   CLang::m_StrArr[IDS__CHAMPIONS__ERROR_WRITE     ] = TEXT("Can't write Champions file");
   CLang::m_StrArr[IDS__CHAMPIONS__ERROR_VERSION   ] = TEXT("BST file – version error");

   CLang::m_StrArr[IDS__STATISTICS                        ] = TEXT("Statistics");
   CLang::m_StrArr[IDS__STATISTICS__PLAYER_NAME           ] = TEXT("Player name");
   CLang::m_StrArr[IDS__STATISTICS__NUMBER__GAMES         ] = TEXT("Number of games");
   CLang::m_StrArr[IDS__STATISTICS__GAMES_WON             ] = TEXT("Games won");
   CLang::m_StrArr[IDS__STATISTICS__OPEN                  ] = TEXT("Open");
   CLang::m_StrArr[IDS__STATISTICS__AVERAGED_GAME_TIME    ] = TEXT("Averaged game time");
   CLang::m_StrArr[IDS__STATISTICS__AVERAGED_NUMBER_CLICKS] = TEXT("Averaged number of clicks");
   CLang::m_StrArr[IDS__STATISTICS__ERROR_CREATE          ] = TEXT("Can't create Statistics file");
   CLang::m_StrArr[IDS__STATISTICS__ERROR_READ            ] = TEXT("Can't load Statistics file");
   CLang::m_StrArr[IDS__STATISTICS__ERROR_WRITE           ] = TEXT("Can't write Statistics file");
   CLang::m_StrArr[IDS__STATISTICS__ERROR_VERSION         ] = TEXT("STC file - version error");
   CLang::m_StrArr[IDS__STATISTICS__ERROR_DATA            ] = TEXT("STC file - data error");

   CLang::m_StrArr[IDS__MENU_GAME              ] = TEXT("&Game");
   CLang::m_StrArr[IDS__MENU_GAME__NEW_GAME    ] = TEXT("&New Game");
   CLang::m_StrArr[IDS__MENU_GAME__BEGINNER    ] = TEXT("&Beginner");
   CLang::m_StrArr[IDS__MENU_GAME__AMATEUR     ] = TEXT("&Amateur");
   CLang::m_StrArr[IDS__MENU_GAME__PROFESSIONAL] = TEXT("&Professional");
   CLang::m_StrArr[IDS__MENU_GAME__CRAZY       ] = TEXT("C&razy");
   CLang::m_StrArr[IDS__MENU_GAME__CUSTOM      ] = TEXT("&Custom...");
   CLang::m_StrArr[IDS__MENU_GAME__CREATE      ] = TEXT("Create Game");
   CLang::m_StrArr[IDS__MENU_GAME__SAVE        ] = TEXT("Save Game");
   CLang::m_StrArr[IDS__MENU_GAME__LOAD        ] = TEXT("Open Game");
   CLang::m_StrArr[IDS__MENU_GAME__SELECTPLAYER] = TEXT("&Players...");
   CLang::m_StrArr[IDS__MENU_GAME__EXIT        ] = TEXT("&Exit");

   CLang::m_StrArr[IDS__MENU_MOSAIC                ] = TEXT("&Mosaics");
   CLang::m_StrArr[IDS__MENU_MOSAIC__TRIANGLES     ] = TEXT("&Triangle");
   CLang::m_StrArr[IDS__MENU_MOSAIC__QUADRILATERALS] = TEXT("&Quadrilaterals");
   CLang::m_StrArr[IDS__MENU_MOSAIC__PENTAGONS     ] = TEXT("&Pentagons");
   CLang::m_StrArr[IDS__MENU_MOSAIC__HEXAGONS      ] = TEXT("&Hexagons");
   CLang::m_StrArr[IDS__MENU_MOSAIC__OTHER         ] = TEXT("&Other");

   CLang::m_StrArr[IDS__MENU_OPTIONS                    ] = TEXT("&Options");
   CLang::m_StrArr[IDS__MENU_OPTIONS__ALWAYSMAXSIZE     ] = TEXT("Always maximal size");
   CLang::m_StrArr[IDS__MENU_OPTIONS__AREA_MAX          ] = TEXT("Maximal mosaic size");
   CLang::m_StrArr[IDS__MENU_OPTIONS__AREA_MIN          ] = TEXT("Minimal mosaic size");
   CLang::m_StrArr[IDS__MENU_OPTIONS__AREA_INCREMENT    ] = TEXT("Increase mosaic size");
   CLang::m_StrArr[IDS__MENU_OPTIONS__AREA_DECREMENT    ] = TEXT("Decrease mosaic size");
   CLang::m_StrArr[IDS__MENU_OPTIONS__SKIN              ] = TEXT("&Skin");
   CLang::m_StrArr[IDS__MENU_OPTIONS__SKIN__LOAD        ] = TEXT("&Load");
   CLang::m_StrArr[IDS__MENU_OPTIONS__SKIN__LOAD_DEFAULT] = TEXT("Default");
   CLang::m_StrArr[IDS__MENU_OPTIONS__SKIN__SAVE        ] = TEXT("&Save");
   CLang::m_StrArr[IDS__MENU_OPTIONS__SKIN__CHANGE      ] = TEXT("&Change");
   CLang::m_StrArr[IDS__MENU_OPTIONS__LANGUAGE          ] = TEXT("Language");
   CLang::m_StrArr[IDS__MENU_OPTIONS__LANGUAGE_ENGLISH  ] = TEXT("English");
   CLang::m_StrArr[IDS__MENU_OPTIONS__USE_UNKNOWN       ] = TEXT("&Use '?'");
   CLang::m_StrArr[IDS__MENU_OPTIONS__SHOW_TOOLBAR      ] = TEXT("&Show toolbar");
   CLang::m_StrArr[IDS__MENU_OPTIONS__SHOW_MENU         ] = TEXT("&Show menu");
   CLang::m_StrArr[IDS__MENU_OPTIONS__SHOW_CAPTION      ] = TEXT("&Show caption");
   CLang::m_StrArr[IDS__MENU_OPTIONS__TO_TRAY           ] = TEXT("Minimize to tray");

   CLang::m_StrArr[IDS__MENU_HELP                    ] = TEXT("&Help");
   CLang::m_StrArr[IDS__MENU_HELP__CAMPIONS          ] = TEXT("&Campions");
   CLang::m_StrArr[IDS__MENU_HELP__STATISTICS        ] = TEXT("&Statistics");
   CLang::m_StrArr[IDS__MENU_HELP__ASSISTANT         ] = TEXT("&Assistant");
   CLang::m_StrArr[IDS__MENU_HELP__ASSISTANT__ONOFF  ] = TEXT("Assistant on");
   CLang::m_StrArr[IDS__MENU_HELP__ASSISTANT__OPTIONS] = TEXT("Options...");
   CLang::m_StrArr[IDS__MENU_HELP__ASSISTANT__SUGGEST] = TEXT("Suggest left click");
   CLang::m_StrArr[IDS__MENU_HELP__ABOUT             ] = TEXT("&About");

   CLang::m_StrArr[IDS__CAPTION_BUTTON_MENU] = TEXT("Menu");

   Finalisation();
   //SaveLanguage(TEXT("english_default.lng"));

   return true;
}

void SaveLanguage(const TCHAR *szFileName) {
   CString strPath(GetModuleDir(ghInstance) + szFileName);
   HANDLE hFile = CreateFile(
      strPath,
      GENERIC_WRITE,
      0,
      NULL,
      CREATE_ALWAYS,
      FILE_ATTRIBUTE_NORMAL,
      NULL
   );
   if (hFile != INVALID_HANDLE_VALUE) {
      int nOld = 0;
      for (MAP_IntStr::const_iterator i=CLang::m_StrArr.begin(); i!=CLang::m_StrArr.end(); i++) {
         DWORD dwNOBW;
         if (abs(i->first - nOld) > 1) {
            WriteFile(hFile, TEXT("\r\n"), 2*sizeof(TCHAR), &dwNOBW, NULL);
         }
         CString strResult(i->second);
         strResult.Replace(TEXT("\n"), TEXT("\\n"));
         strResult.Replace(TEXT("\t"), TEXT("\\t"));
         strResult.Replace(TEXT("\r"), TEXT("\\r"));
         CString strLineToFile;
         strLineToFile.Format(TEXT("%i=%s\r\n"), i->first, (LPCTSTR)strResult);
         int iByteSize = strLineToFile.GetLength()*sizeof(TCHAR);
         if (!WriteFile(hFile, strLineToFile, iByteSize, &dwNOBW, NULL) ||
             (iByteSize != dwNOBW)
            )
         {
            break;
         }
         nOld = i->first;
      }
      CloseHandle(hFile);
   }
}

CLang::Finalisation() {
   if (m_StrArr[IDS__LOGO].Find(TEXT(' ')) == -1) {
      m_StrArr[IDS__LOGO_VERS] = m_StrArr[IDS__LOGO]+TEXT(ID_VERSIONINFO_MAJOR);
   } else {
      m_StrArr[IDS__LOGO_VERS] = m_StrArr[IDS__LOGO]+TEXT(" ")+TEXT(ID_VERSIONINFO_MAJOR);
   }
}

void CLang::LoadMenuList(HMENU hMenu) {
   CString strPath(GetModuleDir(ghInstance) + TEXT("Language\\*.lng"));
   WIN32_FIND_DATA findFileData;
   memset(&findFileData, 0, sizeof(findFileData));
   HANDLE h = FindFirstFile(strPath, &findFileData);
   if (h != INVALID_HANDLE_VALUE) {
      MENUITEMINFO mii = { sizeof(MENUITEMINFO),             // cbSize
                           MIIM_TYPE | MIIM_STATE | MIIM_ID, // fMask
                           MFT_STRING,                       // fType
                           MFS_ENABLED,                      // fState
                           0,                                // wID
                           NULL,                             // hSubMenu
                           NULL,                             // hbmpChecked
                           NULL,                             // hbmpUnchecked
                           0,                                // dwItemData
                           findFileData.cFileName,           // dwTypeData
                           MAX_PATH };                       // cch
      int i=1;
      do {
         if (lstrcmpi(findFileData.cFileName, TEXT("English.lng")) != 0) {
            mii.wID = ID_MENU_OPTIONS_LANG_LOAD0+i;
            findFileData.cFileName[lstrlen(findFileData.cFileName)-4] = TEXT('\0');
            InsertMenuItem(hMenu, i, TRUE, &mii);
            i++;
         }
      } while (FindNextFile(h, &findFileData));
      FindClose(h);
   }
}

bool __fill__map = CLang::RestoreDefaultLanguage();
