////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "TcRobot.h"
//
// �������� ������ TcRobot
////////////////////////////////////////////////////////////////////////////////

#ifndef FILE_TCROBOT
#define FILE_TCROBOT

#include ".\Preproc.h"
#include <windows.h>
#include <set>
#include <vector>
#include ".\Figure\TcBase.h"

#define JOB_RESULT bool
typedef std::vector<bool*> V_pbool;

struct TsClickData {
   POINT coordCell;
   bool  isLeft;
   TeClose close; // for right click only
   float probability;
   POINT prbltCell;
   TsClickData(): coordCell(CIncorrectCoord), isLeft(true), close(_Flag), probability(0.0), prbltCell(CIncorrectCoord) {}
};

class TcRobot {
private:
   // �� � ����� �� ���� �������� ��� ���������� ������
   // ��������� �������� ����� � ��������� �����
   SET_cpTB setForOpen;     // ��������� �������� ����� �������� �� ������� ������� � �������� ������ ��� �����
   SET_cpTB setForFlag;     // ��������� �������� ����� �������� �� ������� ������� � ��������� ������ ��� �������
   SET_cpTB setForAnalyse;  // ��������� �������� ����� �������� �� ������� �� � ���� �� ������� (������ ����� ���������� �������)
   SET_cpTB setCloseNoFlag; // ��������� �������� ����� ��� �������

   SET_cpTB setCnF2; // setCnF.size() == n
   std::set<int> setTable;      // 0..2^n

   int mineNoFLag;
   bool sequentialMove;
   V_pbool vTableSM; // vector "SequentialMove" // ������ �������� �������� ������������ �������
   int row, col;
private:
   inline bool TestVariant(const int, const int, const TB*, const TB*) const;
   JOB_RESULT Analyse2Cell(const TB*, const TB*, TsClickData&);
   JOB_RESULT Analyse(TsClickData&);
   JOB_RESULT AddNextVectors(const int, bool*);
   JOB_RESULT AnalyseVector(const bool*);
   void DeleteTableSM();
public:
   JOB_RESULT FindCell(TsClickData&);

   void GameNew();
   void ClickBegin();
   void ClickEnd();

   TcRobot(): sequentialMove(false) {}
  ~TcRobot() {DeleteTableSM();}
   //void Print();

   void         ResetSequentialMove() {sequentialMove = false;}
   bool            isSequentialMove() {return sequentialMove;}
   JOB_RESULT AllOkToSequentialMove();
   void              SequentialMove(TsClickData&);
};

#endif // FILE_TCROBOT
