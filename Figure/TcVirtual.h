////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "TcVirtual.h"
//
// ����������� ����� TcVirtual
////////////////////////////////////////////////////////////////////////////////

#ifndef FILE_TCVIRTUAL
#define FILE_TCVIRTUAL

#include "..\Preproc.h"
#include <windows.h>

struct TsLUpReturn {
   int countFlag, countOpen, countUnknown;
   bool endGame, victory;
};

struct TsRDownReturn {
   int countFlag, countUnknown;
};

enum TeStatus {_Open, _Close};
enum TeOpen   {_Nil, _1, _2, _3, _4, _5, _6, _7, _8, _9, _10, _11, _12, _13, _14, _15, _16, _17, _18, _19, _20, _21, _Mine};
enum TeClose  {_Unknown, _Clear, _Flag};

const POINT CIncorrectCoord = {-1,-1};

namespace nsFigure {

const TCHAR CCaptionOpen[_Mine][2] =
{  {TEXT(' '), TEXT('\0')}, // _Nil
   {TEXT('1'), TEXT('\0')}, // _1
   {TEXT('2'), TEXT('\0')}, // _2
   {TEXT('3'), TEXT('\0')}, // _3
   {TEXT('4'), TEXT('\0')}, // _4
   {TEXT('5'), TEXT('\0')}, // _5
   {TEXT('6'), TEXT('\0')}, // _6
   {TEXT('7'), TEXT('\0')}, // _7
   {TEXT('8'), TEXT('\0')}, // _8
   {TEXT('9'), TEXT('\0')}, // _9
   {TEXT('1'), TEXT('0' )}, // _10
   {TEXT('1'), TEXT('1' )}, // _11
   {TEXT('1'), TEXT('2' )}, // _12
   {TEXT('1'), TEXT('3' )}, // _13
   {TEXT('1'), TEXT('4' )}, // _14
   {TEXT('1'), TEXT('5' )}, // _15
   {TEXT('1'), TEXT('6' )}, // _16
   {TEXT('1'), TEXT('7' )}, // _17
   {TEXT('1'), TEXT('8' )}, // _18
   {TEXT('1'), TEXT('9' )}, // _19
   {TEXT('2'), TEXT('0' )}, // _20
   {TEXT('2'), TEXT('1' )}  // _21
};

const TCHAR CCaptionClose[_Flag] =
{  TEXT('?'), // _Unknown
   TEXT(' ')  // _Clear
};

const COLORREF CColorOpen[_Mine] =
//     BBGGRR
{  0x00000000, // _Nil       Black  - ������
   0x00800000, // _1         Navy   - �����
   0x00008000, // _2         Green  - ������
   0x000000FF, // _3         Red    - �������
   0x00000080, // _4         Maroon - ���������� ???
   0x00FF0000, // _5         Blue   - �����
   0x00000000, // _6         Black  - ������
   0x00008080, // _7         Olive  - ������-���������� ???
   0x00FFFF00, // _8         Aqua   - �������
   0x00800000, // _9         Navy   - �����
   0x00008000, // _10        Green  - ������
   0x000000FF, // _11        Red    - �������
   0x00000080, // _12        Maroon - ���������� ???
   0x00800000, // _13        Navy   - �����
   0x00008000, // _14        Green  - ������
   0x000000FF, // _15        Red    - �������
   0x00000080, // _16        Maroon - ���������� ???
   0x00FF0000, // _17        Blue   - �����
   0x00000000, // _18        Black  - ������
   0x00008080, // _19        Olive  - ������-���������� ???
   0x00FFFF00, // _20        Aqua   - �������
   0x00800000  // _21        Navy   - �����
};

const COLORREF CColorClose[_Flag] =
//     BBGGRR
{  0x00808000, // _Unknown   Teal   - �����-�����
   0x00000000  // _Clear     Black  - ������
};

class TcVirtual{
public:
   virtual void     Cell_SetDown(bool) = 0;
   virtual void     Cell_SetStatus(TeStatus) = 0;
   virtual TeStatus Cell_GetStatus() const = 0;
   virtual void     Cell_DefineValue() = 0; // ���� �� ����, �� ���������� � ������� ����� ��� � ���������� ��������
   virtual bool     Cell_SetMine() = 0; // out: false - ���� �� ����������a (���� �� ��������������� ����� ��� ��������� ��� ��� ��� ����)
   virtual TeOpen   Cell_GetOpen() const = 0;
   virtual void     Cell_SetClose(TeClose) = 0;
   virtual TeClose  Cell_GetClose() const = 0;

   virtual void Lock() = 0;
   virtual void LockNeighbor() = 0; // ��������� ��������� ��� � ������� � � ����
 //virtual void  SetNeighborLink(TcVirtual**) = 0;
   virtual int   GetNeighborNumber() const = 0;
   virtual POINT GetNeighborCoord(int) const = 0; // ���������� ���������� ��������� ������
   virtual POINT GetCoord() const = 0;       // X � Y ������
   virtual POINT GetCenterPixel() const = 0; // ���������� ������ ������ (� ��������)
   virtual bool ToBelong(int,int) = 0; // ����������� �� ��� �������� ���������� ������
   virtual void SetPoint(const int&) = 0; // ���������� ���������� ����� �� ������� ������� ������
   virtual void Reset() = 0;
   virtual void Paint() const = 0;
   virtual void          LButtonDown() = 0;
   virtual TsLUpReturn   LButtonUp  (bool) = 0; // in: true - ������ �� ��� �� ������
   virtual TsRDownReturn RButtonDown(TeClose) = 0; // out: change count flag
};

} // namespace nsFigure

#endif // FILE_TCVIRTUAL
