////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "Mosaic.h"
////////////////////////////////////////////////////////////////////////////////

#ifndef __FILE__MOSAIC__
#define __FILE__MOSAIC__

#pragma warning(disable:4786) // identifier was truncated to '255' characters in the debug information
#include <vector>
#ifndef __AFX_H__
   #include <Windows.h>
#endif
#include "Image.h"
#include "./Mosaic/Base.h"
#include "StorageMines.h"

namespace nsMosaic {

   ////////////////////////////////////////////////////////////////////////////////
   //                            types & constants
   ////////////////////////////////////////////////////////////////////////////////
   enum EMosaic {
      // Triangle
      mosaicTriangle1,
      mosaicTriangle2,
      mosaicTriangle3,
      mosaicTriangle4,
      // Quadrangle
      mosaicSquare1,
      mosaicSquare2,
      mosaicParquet1,
      mosaicParquet2,
      mosaicTrapezoid1,
      mosaicTrapezoid2,
      mosaicTrapezoid3,
      mosaicRhombus1,
      mosaicQuadrangle1,
      // Pentagon
      mosaicPentagonT24,
      mosaicPentagonT5,
      mosaicPentagonT10,
      // Hexagon
      mosaicHexagon1,
      // Other
      mosaicTrSq1,
      mosaicTrSq2,
      mosaicSqTrHex,
      //
      mosaicNil  //  ������ ��������� � ������������ !!!
   };

   enum ESkillLevel {
      skillLevelBeginner,
      skillLevelAmateur,
      skillLevelProfessional,
      skillLevelCrazy,
      skillLevelCustom  //  ������ ��������� � ������������ !!!
   };

   const SIZE SIZE_MOSAIC[skillLevelCustom] = {{10, 10},  // Beginner
                                               {20, 15},  // Amateur
                                               {30, 20},  // Professional
                                               {45, 25}}; // Crazy

   const int AREA_MINIMUM = 230;//245;

   void LoadDefaultImageMine   (HINSTANCE, CImage&);
   void LoadDefaultImageFlag   (HINSTANCE, CImage&);
   void LoadDefaultImagePause  (HINSTANCE, CImage&);
   void LoadDefaultImageBckgrnd(HINSTANCE, CImage&);

   float GetPercentMine (ESkillLevel, EMosaic); // ������� ��� �� �������� ������ ��������� ��� �������� ������
   int DefineNumberMines(ESkillLevel, EMosaic, const SIZE&);

   struct CSerialize {
      SIZE    m_SizeMosaic; // ������ ���� (� �������)
      int     m_iArea;      // �������
      EMosaic m_Mosaic;     // �� ����� ����� ������� ������� ����
      int     m_iMines;     // ���-�� ��� �� ����
      bool    m_useUnknown;
      CSerialize():
         m_SizeMosaic(SIZE_MOSAIC[skillLevelBeginner]),
         m_iArea     (2000),
         m_Mosaic    (mosaicSquare1),
         m_iMines    (DefineNumberMines(skillLevelBeginner, mosaicSquare1, SIZE_MOSAIC[skillLevelBeginner])),
         m_useUnknown(true) {}
   };

   const LOGFONT FONT_DEFAULT = {0,0,0,0, FW_BOLD,0,0,0, DEFAULT_CHARSET, OUT_DEFAULT_PRECIS,
                                 CLIP_DEFAULT_PRECIS, DEFAULT_QUALITY, DEFAULT_PITCH | FF_DONTCARE,
                                 TEXT("MS Sans Serif")};// TEXT("Times New Roman");//

   const nsCell::CColorText COLOR_TEXT_DEFAULT =
   {    // BBGGRR
      {0x00000000, // _Nil       Black
       0x00800000, // _1         Navy
       0x00008000, // _2         Green
       0x000000FF, // _3         Red
       0x00000080, // _4         Maroon
       0x00FF0000, // _5         Blue
       0x00000000, // _6         Black
       0x00008080, // _7         Olive
       0x00FFFF00, // _8         Aqua
       0x00800000, // _9         Navy
       0x00008000, // _10        Green
       0x000000FF, // _11        Red
       0x00000080, // _12        Maroon
       0x00800000, // _13        Navy
       0x00008000, // _14        Green
       0x000000FF, // _15        Red
       0x00000080, // _16        Maroon
       0x00FF0000, // _17        Blue
       0x00000000, // _18        Black
       0x00008080, // _19        Olive
       0x00FFFF00, // _20        Aqua
       0x00800000  // _21        Navy
      },// BBGGRR
      {0x00808000, // _Unknown   Teal
       0x00000000  // _Clear     Black
      }
   };

   struct CSkinMosaic {
      CImageMini         m_ImgMine, m_ImgFlag, m_ImgPause, m_ImgBckgrnd;
      LOGFONT            m_Font;
      nsCell::CColorText m_ColorText;
      nsCell::CBorder    m_Border;
      COLORREF           m_colorBk;
      CSkinMosaic():
         m_Font     (FONT_DEFAULT),
         m_ColorText(COLOR_TEXT_DEFAULT),
         m_colorBk  (GetSysColor(COLOR_BTNFACE))//0xD8D498)//
      {  m_ImgPause.m_Place = placeCenter;}
   };

   enum EGameStatus {gsReady      = 0,
                     gsCreateGame = 1,
                     gsPlay       = 2,
                     gsEnd        = 4};
   enum EPlayInfo {piPlayerUnknown   = 0,
                   piPlayerUser      = 1,
                   piPlayerAssistant = 2,
                   piIgnorable       = 4 // �� ��������� ���� � ���������� � � ����������� ����������� - ������ ���� ���� ��� ��������� �� ����� ��� ���� �������
                  };

   struct CGraphicContextEx: public nsCell::CGraphicContext {
      HDC      m_hDCWnd,
               m_hDCPause;
      HBRUSH   m_hBrush;
      HFONT    m_hFont;
      LOGFONT  m_Font;
      COLORREF m_colorBk;
      SIZE     m_SizeBitmap;
      CImage   m_ImgPause,
               m_ImgBckgrnd;
      bool     m_isRefreshMosaic,
               m_isRefreshPause;
      CGraphicContextEx() :
         CGraphicContext  (COLOR_TEXT_DEFAULT),
         m_hDCWnd         (NULL),
         m_hDCPause       (NULL),
         m_hBrush         (NULL),
         m_hFont          (NULL),
         m_Font           (FONT_DEFAULT),
         m_colorBk        (GetSysColor(COLOR_BTNFACE)),
         m_isRefreshMosaic(true),
         m_isRefreshPause (true)
      {}
   };

   class CMosaic {
   private:
      const static TCHAR SZ_CLASS_WND[];
      std::vector<std::vector<nsCell::CBase* > > m_Mosaic;

      HWND m_hWnd;
      HWND m_hWndParent;
      int  m_idControl;

      CGraphicContextEx m_GContext;

      nsMosaic::CSerialize m_SerializeData; // ���������� ��� ����������
      COORD m_CoordDown;      // ���������� ������ �� ������� ���� ������ (�� �� ����������� ��� ��������)
      bool  m_bPause;
                              //          GameNew()     GameBegin()     GameEnd()     GameNew()
                              //   time      |              |               |            |
                              //  ------->   | gsCreateGame |               |            |
      nsMosaic::EGameStatus m_GameStatus; // |  or gsReady  |     gsPlay    |   gsEnd    |

      BYTE m_PlayInfo; // 1. K�� ����� ? ���� �/��� �����?    2. Play from file or created game?

      int m_iCountOpen;    // ������� �������� ����� �� ����
      int m_iCountFlag;    // ������� ������������� ������� �� ����
      int m_iCountUnknown;
      int m_iCountClick;
      int m_iCountTimer;   // ������� �������

      int m_iOldMines;     // ���-�� ��� �� ���� �� �������� ����. ������������ ����� ���� ���� �������, �� �� ����� ���� �� �����������.
      CStorageMines m_RepositoryMines; // ��� load'a - ���������� ����� � ������
   private:
      void MosaicDestroy(const SIZE&);

      void GameBegin(COORD);
      void GameEnd(bool);
      void VerifyFlag();

      COORD WinToArray(int, int) const; // ������������� �������� ���������� � ���������� m_Mosaic'a
      int (*GetSizeInscribedSquare)(int area, int borderWidth); // ������ ���������� � ������ ��������
      void SetDCFont();

      static LRESULT CALLBACK WndProc(HWND, UINT, WPARAM, LPARAM);
      static void TimerProc(HWND, UINT, UINT, DWORD);
      void OnPaint      (HWND);                       // WM_PAINT
      void OnLButtonUp  (HWND, int , int, UINT);      // WM_LBUTTONUP
      void OnLButtonDown(HWND, BOOL, int, int, UINT); // WM_LBUTTONDOWN & WM_LBUTTONDBLCLK
      void OnRButtonDown(HWND, BOOL, int, int, UINT); // WM_RBUTTONDOWN & WM_RBUTTONDBLCLK
      void OnSize       (HWND, UINT, int, int);       // WM_SIZE

      void SetClasureToMosaic();

      void SetFont         (const LOGFONT&);
      void SetCellColorText(const nsCell::CColorText&);
      void SetBorder       (const nsCell::CBorder&);
      void SetBrush        (COLORREF colorSolidBrush);

      void MosaicCreate();
   public:
      void GameNew();
      void GameCreate();

      ESkillLevel GetSkillLevel() const; // ������ ������� ���������
      void        SetSkillLevel(ESkillLevel); // ���������� �������������� ������� ���������
      void        SetSkillLevelCustom(const SIZE& newSizeMosaic, int numberMines); // ���������� ���������� ������� ���������, ����� ������ ������� � ���������� ���
      void        SetGame(EMosaic mosaic, const SIZE& newSizeMosaic, int numberMines, const CStorageMines *pStorageCoordMines); // ���������� ������� ��������� ������� � � ����������� ����������� ��� (���������� ��� ����� ���������� � ������� "��������� ���")
      void        SetMosaic(EMosaic); // ���������� ����� �������
      EMosaic     GetMosaic() const; // ������ ��� �������
      const nsCell::CBase* GetCell(int x, int y) const {return m_Mosaic[x][y];} // ������ � �������� ������

      int     GetMines()      const {return m_SerializeData.m_iMines;} // ���������� ���
      int     GetArea()       const {return m_SerializeData.m_iArea;} // ������� �����
      void    SetArea(int newArea); // ���������� ����� ������� �����
      SIZE  (*GetSizeWindow) (const SIZE& sizeField, int area);            // ������ � ��������
      SIZE    GetSize()       const {return m_SerializeData.m_SizeMosaic;} // ������ � �������
      int     GetNeighborNumber() const {return m_Mosaic[0][0]->GetNeighborNumber();} // ������ ���������� ������� ��� ������� �������

      HWND GetHandle() const {return m_hWnd;}

      void ApplySkin(const nsMosaic::CSkinMosaic& newSkin);

      void SetPause(bool);
      bool GetPause()      const {return m_bPause;}
      bool GetGameStatusIsReady () const {return !!(m_GameStatus & gsReady      );}
      bool GetGameStatusIsCreate() const {return !!(m_GameStatus & gsCreateGame );}
      bool GetGameStatusIsPlay  () const {return !!(m_GameStatus & gsPlay       );}
      bool GetGameStatusIsEnd   () const {return !!(m_GameStatus & gsEnd        );}
      bool GetPlayerIsUser      () const {return !!(m_PlayInfo   & piPlayerUser );}
      bool GetPlayerIsAssistant () const {return !!(m_PlayInfo   & piPlayerAssistant);}
      bool GetGameIsIgnorable   () const {return !!(m_PlayInfo   & piIgnorable  );}
      int  GetCountMines() const {return m_SerializeData.m_iMines-m_iCountFlag;}
      int  GetCountTimer() const {return m_iCountTimer;}
      int  GetCountClick() const {return m_iCountClick;}
      int  GetCountOpen()  const {return m_iCountOpen;}
      bool GetVictory()    const {return !GetCountMines();} // ������������� ���� ����� gameStatus == gsEnd;

      void SetUseUnknown(bool newUseUnknown) {m_SerializeData.m_useUnknown = newUseUnknown;}
      bool GetUseUnknown() const      {return m_SerializeData.m_useUnknown;}

      ~CMosaic();
      CMosaic();
      BOOL Create(HWND hWindowParent, int id);
      BOOL SerializeIn (HANDLE hFile);
      BOOL SerializeOut(HANDLE hFile) const;
   };

} // namespace nsMosaic

#endif // __FILE__MOSAIC__
