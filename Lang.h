////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "Lang.h"
//
// Прочитка файла языка
////////////////////////////////////////////////////////////////////////////////

#ifndef __FILE__LANG__
#define __FILE__LANG__

#pragma warning(disable:4786) // identifier was truncated to '255' characters in the debug information
#include <map>
#ifndef __AFX_H__
   #include <Windows.h>
   #include "CStringKS.h"
#endif

typedef std::map<int, CString> MAP_IntStr;

class CLang {
public:
   static MAP_IntStr m_StrArr;
private:
   HANDLE m_hFile;
public:
   CLang(const TCHAR *szFileName);
  ~CLang();
   static bool RestoreDefaultLanguage();
   static void LoadMenuList(HMENU hMenu);
private:
   bool ReadLine();
   void Parse();
   static Finalisation();
};


#define IDS__LOGO_VERS                                          (-1)
#define IDS__LOGO                                                 0

#define IDS__OK                                                   1
#define IDS__CANCEL                                               2
#define IDS__ERROR                                                3
#define IDS__INFORMATION                                          4
#define IDS__SEC                                                  5
#define IDS__SECOND                                               6
#define IDS__MILISECOND                                           7
#define IDS__MAX                                                  8

#define IDS__MINES                                                9
#define IDS__TIME                                                 10
#define IDS__VICTORY                                              11
#define IDS__DEFEAT                                               12

#define IDS__INI_FILE__ERROR_CREATE                               13
#define IDS__INI_FILE__ERROR_READ                                 14
#define IDS__INI_FILE__ERROR_WRITE                                15
#define IDS__INI_FILE__ERROR_VERSION                              16

#define IDS__LOAD_FASTMINES_GAME                                  17
#define IDS__SAVE_FASTMINES_GAME                                  18
#define IDS__FASTMINES_EXTENSIONS_FILE                            19
#define IDS__FMS_FILE__ERROR_CREATE                               20
#define IDS__FMS_FILE__ERROR_READ                                 21
#define IDS__FMS_FILE__ERROR_WRITE                                22
#define IDS__FMS_FILE__ERROR_VERSION                              23

#define IDS__RESTORE_LAST_GAME                                    24
#define IDS__QUESTION                                             25

#define IDS__MOSAIC_NAME_00                                       30
#define IDS__MOSAIC_NAME_01                                       31
#define IDS__MOSAIC_NAME_02                                       32
#define IDS__MOSAIC_NAME_03                                       33
#define IDS__MOSAIC_NAME_04                                       34
#define IDS__MOSAIC_NAME_05                                       35
#define IDS__MOSAIC_NAME_06                                       36
#define IDS__MOSAIC_NAME_07                                       37
#define IDS__MOSAIC_NAME_08                                       38
#define IDS__MOSAIC_NAME_09                                       39
#define IDS__MOSAIC_NAME_10                                       40
#define IDS__MOSAIC_NAME_11                                       41
#define IDS__MOSAIC_NAME_12                                       42
#define IDS__MOSAIC_NAME_13                                       43
#define IDS__MOSAIC_NAME_14                                       44
#define IDS__MOSAIC_NAME_15                                       45
#define IDS__MOSAIC_NAME_16                                       46
#define IDS__MOSAIC_NAME_17                                       47
#define IDS__MOSAIC_NAME_18                                       48
#define IDS__MOSAIC_NAME_19                                       49
#define IDS__MOSAIC_NAME_INVALID                                  50

#define IDS__DIALOG_CUSTOM_SKILL                                  100
#define IDS__DIALOG_CUSTOM_SKILL__X_WIDTH                         101
#define IDS__DIALOG_CUSTOM_SKILL__Y_HEIGHT                        102
#define IDS__DIALOG_CUSTOM_SKILL__NUMBER_MINES                    103
#define IDS__DIALOG_CUSTOM_SKILL__FULL_SCREEN__CURRENT_SIZE       104
#define IDS__DIALOG_CUSTOM_SKILL__FULL_SCREEN__MINIMAL_SIZE       105

#define IDS__DIALOG_ABOUT                                         120
#define IDS__DIALOG_ABOUT__VERSION                                121
#define IDS__DIALOG_ABOUT__COPYRIGHT                              122
#define IDS__DIALOG_ABOUT__COMMEMTS                               123
#define IDS__DIALOG_ABOUT__RESPONSE                               124
#define IDS__DIALOG_ABOUT__SITE                                   125
            
#define IDS__DIALOG_SELECTPLAYER                                  140      
#define IDS__DIALOG_SELECTPLAYER__SELECT                          141
#define IDS__DIALOG_SELECTPLAYER__NEWPLAYER                       142
#define IDS__DIALOG_SELECTPLAYER__NEWPASSWORD                     143
#define IDS__DIALOG_SELECTPLAYER__RENAME                          144
#define IDS__DIALOG_SELECTPLAYER__REMOVE                          145
#define IDS__DIALOG_SELECTPLAYER__AUTOSTART                       146
#define IDS__DIALOG_SELECTPLAYER__TABLE                           147
#define IDS__DIALOG_SELECTPLAYER__ENTER_PASSWORD                  148
#define IDS__DIALOG_SELECTPLAYER__ENTER_NAME                      149
#define IDS__DIALOG_SELECTPLAYER__ENTER_NEW_NAME                  150
#define IDS__DIALOG_SELECTPLAYER__NEW_NAME                        151
#define IDS__DIALOG_SELECTPLAYER__CANT_USE_VIRT_NAME              152
#define IDS__DIALOG_SELECTPLAYER__INVALID_NAME                    153
#define IDS__DIALOG_SELECTPLAYER__PASSWORD_NOT_CONFIRMED          154
#define IDS__DIALOG_SELECTPLAYER__THIS_VIRTUAL_PLAYER             155
#define IDS__DIALOG_SELECTPLAYER__INCORRECT_PASSWORD              156
#define IDS__DIALOG_SELECTPLAYER__PASSWORD                        157
#define IDS__DIALOG_SELECTPLAYER__CONFIRMATION                    158

#define IDS__DIALOG_CHANGESKIN                                    170
#define IDS__DIALOG_CHANGESKIN__BUTTON_TRANSPARENT                171
#define IDS__DIALOG_CHANGESKIN__BUTTON_IMAGE                      172
#define IDS__DIALOG_CHANGESKIN__BUTTON_FONT                       173
#define IDS__DIALOG_CHANGESKIN__BUTTON_APPLY_TO_ALL               174
#define IDS__DIALOG_CHANGESKIN__BUTTON_COLOR                      175
#define IDS__DIALOG_CHANGESKIN__BUTTON_LIGHT_COLOR                176
#define IDS__DIALOG_CHANGESKIN__BUTTON_SHADOW_COLOR               177
#define IDS__DIALOG_CHANGESKIN__OPENFILENAME_FILTER_ALL_IMAGES    178
#define IDS__DIALOG_CHANGESKIN__OPENFILENAME_TITLE_OPEN_IMAGE     179
#define IDS__DIALOG_CHANGESKIN__CHECKBUTTON_IMAGE_MINE            180
#define IDS__DIALOG_CHANGESKIN__CHECKBUTTON_IMAGE_FLAG            181
#define IDS__DIALOG_CHANGESKIN__CHECKBUTTON_IMAGE_PAUSE           182
#define IDS__DIALOG_CHANGESKIN__CHECKBUTTON_BUTTON_PAUSE          183
#define IDS__DIALOG_CHANGESKIN__CHECKBUTTON_BUTTON_GAME           184
#define IDS__DIALOG_CHANGESKIN__CHECKBUTTON_IMAGE_BACKGROUND      185
#define IDS__DIALOG_CHANGESKIN__CHECKBUTTON_COLOR_BACKGROUND      186
#define IDS__DIALOG_CHANGESKIN__CHECKBUTTON_BORDER_COLOR_WIDTH    187
#define IDS__DIALOG_CHANGESKIN__CHECKBUTTON_CHARACTER_COLOR       188
#define IDS__DIALOG_CHANGESKIN__CHECKBUTTON_FONT_TYPE             189
#define IDS__DIALOG_CHANGESKIN__RESET_TO_DEFAULT                  190
#define IDS__DIALOG_CHANGESKIN__PLACEMENT_TYPE_IMAGE              191
#define IDS__DIALOG_CHANGESKIN__PLACEMENT_CENTER                  192
#define IDS__DIALOG_CHANGESKIN__PLACEMENT_STRETCH                 193
#define IDS__DIALOG_CHANGESKIN__PLACEMENT_TILE                    194
#define IDS__DIALOG_CHANGESKIN__WIDTH_BORDER                      195

#define IDS__DIALOG_FILESKIN                                      210
#define IDS__DIALOG_FILESKIN__FILE_NAME                           211
#define IDS__DIALOG_FILESKIN__OVERWRITE                           212
#define IDS__DIALOG_FILESKIN__FILE_COPYING                        213
#define IDS__DIALOG_FILESKIN__ERROR_CREATE                        214
#define IDS__DIALOG_FILESKIN__ERROR_READ                          215
#define IDS__DIALOG_FILESKIN__ERROR_WRITE                         216
#define IDS__DIALOG_FILESKIN__ERROR_VERSION                       217
#define IDS__DIALOG_FILESKIN__RESTORED_DEFAULT_SKIN               218

#define IDS__DIALOG_ASSISTANT                                     230
#define IDS__DIALOG_ASSISTANT__TIMEOUT_USER_UNACTIVE              231
#define IDS__DIALOG_ASSISTANT__TIMEOUT_JOB                        232
#define IDS__DIALOG_ASSISTANT__NEW_GAME_AUTOSTART                 233
#define IDS__DIALOG_ASSISTANT__STOPJOB                            234
#define IDS__DIALOG_ASSISTANT__IGNOREPAUSE                        235
#define IDS__DIALOG_ASSISTANT__BEEPCLICK                          236
            
#define IDS__DIALOG_SELECTMOSAIC                                  250
#define IDS__DIALOG_SELECTMOSAIC__NUMBER                          251
#define IDS__DIALOG_SELECTMOSAIC__NAME                            252

#define IDS__CHAMPIONS                                            270
#define IDS__CHAMPIONS__PLAYER_NAME                               271
#define IDS__CHAMPIONS__GAME_TIME                                 272
#define IDS__CHAMPIONS__ERROR_CREATE                              273
#define IDS__CHAMPIONS__ERROR_READ                                274
#define IDS__CHAMPIONS__ERROR_WRITE                               275
#define IDS__CHAMPIONS__ERROR_VERSION                             276

#define IDS__STATISTICS                                           280
#define IDS__STATISTICS__PLAYER_NAME                              281
#define IDS__STATISTICS__NUMBER__GAMES                            282
#define IDS__STATISTICS__GAMES_WON                                283
#define IDS__STATISTICS__OPEN                                     284
#define IDS__STATISTICS__AVERAGED_GAME_TIME                       285
#define IDS__STATISTICS__AVERAGED_NUMBER_CLICKS                   286
#define IDS__STATISTICS__ERROR_CREATE                             287
#define IDS__STATISTICS__ERROR_READ                               288
#define IDS__STATISTICS__ERROR_WRITE                              289
#define IDS__STATISTICS__ERROR_VERSION                            290
#define IDS__STATISTICS__ERROR_DATA                               291

#define IDS__MENU_GAME                   500
#define IDS__MENU_GAME__NEW_GAME         501
#define IDS__MENU_GAME__BEGINNER         502
#define IDS__MENU_GAME__AMATEUR          503
#define IDS__MENU_GAME__PROFESSIONAL     504
#define IDS__MENU_GAME__CRAZY            505
#define IDS__MENU_GAME__CUSTOM           506
#define IDS__MENU_GAME__CREATE           507
#define IDS__MENU_GAME__SAVE             508
#define IDS__MENU_GAME__LOAD             509
#define IDS__MENU_GAME__SELECTPLAYER     510
#define IDS__MENU_GAME__EXIT             511

#define IDS__MENU_MOSAIC                   520
#define IDS__MENU_MOSAIC__TRIANGLES        521
#define IDS__MENU_MOSAIC__QUADRILATERALS   522
#define IDS__MENU_MOSAIC__PENTAGONS        523
#define IDS__MENU_MOSAIC__HEXAGONS         524
#define IDS__MENU_MOSAIC__OTHER            525

#define IDS__MENU_OPTIONS                     530
#define IDS__MENU_OPTIONS__ALWAYSMAXSIZE      531
#define IDS__MENU_OPTIONS__AREA_MAX           532
#define IDS__MENU_OPTIONS__AREA_MIN           533
#define IDS__MENU_OPTIONS__AREA_INCREMENT     534
#define IDS__MENU_OPTIONS__AREA_DECREMENT     535
#define IDS__MENU_OPTIONS__SKIN               536
#define IDS__MENU_OPTIONS__SKIN__LOAD         537
#define IDS__MENU_OPTIONS__SKIN__LOAD_DEFAULT 538
#define IDS__MENU_OPTIONS__SKIN__SAVE         539
#define IDS__MENU_OPTIONS__SKIN__CHANGE       540
#define IDS__MENU_OPTIONS__LANGUAGE           541
#define IDS__MENU_OPTIONS__LANGUAGE_ENGLISH   542
#define IDS__MENU_OPTIONS__USE_UNKNOWN        543
#define IDS__MENU_OPTIONS__SHOW_TOOLBAR       544
#define IDS__MENU_OPTIONS__SHOW_MENU          545
#define IDS__MENU_OPTIONS__SHOW_CAPTION       546
#define IDS__MENU_OPTIONS__TO_TRAY            547

#define IDS__MENU_HELP                     560
#define IDS__MENU_HELP__CHAMPIONS          561
#define IDS__MENU_HELP__STATISTICS         562
#define IDS__MENU_HELP__ASSISTANT          563
#define IDS__MENU_HELP__ASSISTANT__ONOFF   564
#define IDS__MENU_HELP__ASSISTANT__OPTIONS 565
#define IDS__MENU_HELP__ASSISTANT__SUGGEST 566
#define IDS__MENU_HELP__ABOUT              567

#define IDS__CAPTION_BUTTON_MENU 580

#endif // __FILE__LANG__
