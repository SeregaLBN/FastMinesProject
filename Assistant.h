////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "Assistant.h"
//
// ќписание класса CAssistant
////////////////////////////////////////////////////////////////////////////////

#ifndef __FILE__ASSISTANT__
#define __FILE__ASSISTANT__

#pragma warning(disable:4786) // identifier was truncated to '255' characters in the debug information
#include <vector>
#include <set>
#ifndef __AFX_H__
   #include <Windows.h>
#endif
#include "Mosaic/Base.h"

class CAssistant;
#include "Mosaic.h"

namespace nsMosaic {

#define JOB_RESULT bool
typedef std::vector<bool*> V_pBool;

struct CClickData {
   COORD m_CoordCell;
   bool  m_bIsLeft;
   nsCell::EClose m_Close; // only for right click
   float m_fProbability;
   COORD m_PrbltCell;
   CClickData():
      m_CoordCell(INCORRECT_COORD),
      m_bIsLeft(true),
      m_Close(nsCell::_Flag),
      m_fProbability(0.0),
      m_PrbltCell(INCORRECT_COORD) {}
};

class CAssistant {
private:
   typedef std::set<int> SET_Int;

   CMosaic &m_Mosaic;
   // ни в одном из этих множеств нет одинаковых данных
   // множества открытых €чеек с ненулевым весом
   nsCell::SET_cpBase m_SetForOpen;     // множество открытых €чеек кликанье на которых приведЄт к открытию вокруг них €чеек
   nsCell::SET_cpBase m_SetForFlag;     // множество открытых €чеек кликанье на которых приведЄт к установке вокруг них флажков
   nsCell::SET_cpBase m_SetForAnalyse;  // множество открытых €чеек кликанье на которых ни к чему не приведЄт (€чейка имеет неоткрытых соседей)
   nsCell::SET_cpBase m_SetCloseNoFlag; // множество закрытых €чеек без флажков

   nsCell::SET_cpBase m_SetCnF2;      // setCnF.size() == n
   SET_Int m_SetPossibleCombinations; // 0..2^n

   int m_iMineNoFLag;
   bool m_bSequentialMove;
   V_pBool m_VTableSM; // vector "SequentialMove" // вектор векторов значений перебираемых флажков
   int m_iRow, m_iCol;
private:
   inline bool TestCombination(const int, const nsCell::CBase*, const nsCell::CBase*) const;
   JOB_RESULT Analyse2Cell(const nsCell::CBase*, const nsCell::CBase*, OUT CClickData&);
   JOB_RESULT Analyse(OUT CClickData&);
   JOB_RESULT AddNextVectors(const int, bool*);
   JOB_RESULT AnalyseVector(const bool*);
   void DeleteTableSM();
public:
   JOB_RESULT FindCell(OUT CClickData&);

   void InitForNewGame(); // переинициализаци€ данных дл€ новой игры
   void ClickEnd(const nsCell::CClickReportContext &ClickReportContext);

   CAssistant(CMosaic& mosaic): m_Mosaic(mosaic), m_bSequentialMove(false) {}
  ~CAssistant() {DeleteTableSM();}
   //void Print();

   void       SequentialMoveReset() {m_bSequentialMove = false;}
   bool     IsSequentialMoveProcessed() {return m_bSequentialMove;}
   JOB_RESULT SequentialMoveCanBegin();
   void       SequentialMove(CClickData&);

   void Assistant_Job();
};

}; // namespace nsMosaic

#endif // __FILE__ASSISTANT__
