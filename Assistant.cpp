////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "Assistant.cpp"
//
// Реализация класса CAssistant
////////////////////////////////////////////////////////////////////////////////

#include "StdAfx.h"
#include "Assistant.h"
#include <Windows.h>
#include <map>
#include "StorageMines.h"
#include "CommonLib.h"
#ifdef _DEBUG
   #include "Logger.h"
#endif

////////////////////////////////////////////////////////////////////////////////
//                             global variables
////////////////////////////////////////////////////////////////////////////////
//extern HINSTANCE ghInstance;
extern HANDLE hEventJob;

namespace nsMosaic {

////////////////////////////////////////////////////////////////////////////////
//                            types & constants
////////////////////////////////////////////////////////////////////////////////
typedef std::multimap<const nsCell::CBase*, const nsCell::CBase*> MM_cpTB;
#define JOB_ABORT   false
#define JOB_SUCCESS true

////////////////////////////////////////////////////////////////////////////////
//                                   other
////////////////////////////////////////////////////////////////////////////////
inline bool Insert(MM_cpTB&, const nsCell::CBase*, const nsCell::CBase*);
inline void Delete(nsCell::SET_cpBase&, const nsCell::CBase*);

////////////////////////////////////////////////////////////////////////////////
//                              implementation
////////////////////////////////////////////////////////////////////////////////

JOB_RESULT CAssistant::FindCell(CClickData& click) {
   if (!m_SetForOpen.empty()) {
      click.m_CoordCell = (*m_SetForOpen.begin())->GetCoord();
      click.m_bIsLeft = true;
   } else {
      if (!m_SetForFlag.empty()) {
         click.m_CoordCell = (*m_SetForFlag.begin())->GetCoord();
         click.m_bIsLeft = true;
      } else {
         if (!m_SetForAnalyse.empty()) {
            //SetCursor(LoadCursor(ghInstance, IDC_WAIT));
            if (!Analyse(click)) // работа была прервана
               return JOB_ABORT;
            //SetCursor(LoadCursor(ghInstance, IDC_ARROW));
/**
            g_Logger.Put(CLogger::LL_DEBUG, TEXT("coord = %i"), click.m_CoordCell);
            if (click.m_CoordCell != INCORRECT_COORD) {
               if (click.m_bIsLeft)
                  g_Logger.Put(CLogger::LL_DEBUG, TEXT("coord for left  cliсk"));
               else
                  g_Logger.Put(CLogger::LL_DEBUG, TEXT("coord for right cliсk"));
            }
/**/
         }
      }
   }
   return JOB_SUCCESS;
}

const nsCell::CBase* cellAnalyseSave;
const nsCell::CBase* cellInfluenceSave;

JOB_RESULT CAssistant::Analyse(CClickData& click) {
   const int numberNeighbor = m_Mosaic.GetNeighborNumber();
/*
   g_Logger.Put(CLogger::LL_DEBUG, TEXT("------------------"));
*/
   MM_cpTB mmPair; // проанализированные пары

   for (nsCell::SET_cpBase::const_iterator cellAnalyse=m_SetForAnalyse.begin(); cellAnalyse!=m_SetForAnalyse.end(); cellAnalyse++) { // выбрать очередную ячейку из m_SetForAnalyse
      // Ячейка cellAnalyse выбрана

      nsCell::SET_cpBase setInfluence, setCnF; // Полностью очистить множества setInfluence и setCnF (Close & noFlag)

      // Для cellAnalyse (пройдя по всем соседям) добавить все
      // соседние закрытые ячейки без флагов в множество setCnF
      for (int k=0; k<numberNeighbor; k++) {
         const COORD coordNeighbor = (*cellAnalyse)->GetNeighborCoord(k);
         if (coordNeighbor == INCORRECT_COORD) continue;
         const nsCell::CBase* cell = m_Mosaic.GetCell(coordNeighbor.X,coordNeighbor.Y);
         if ((cell->Cell_GetStatus() == nsCell::_Close) &&
            (cell->Cell_GetClose () != nsCell::_Flag))
            setCnF.insert(cell);
      }

      // Пройтись по всему множ. setCnF.
      // Всех открытых соседей (не являющихся cellAnalyse) добавить в множество setInfluence.
      for (nsCell::SET_cpBase::const_iterator p=setCnF.begin(); p!=setCnF.end(); p++) {
         for (int k=0; k<numberNeighbor; k++) {
            const COORD coordNeighbor = (*p)->GetNeighborCoord(k);
            if (coordNeighbor == INCORRECT_COORD) continue;
            const nsCell::CBase* cell = m_Mosaic.GetCell(coordNeighbor.X,coordNeighbor.Y);
            if ((cell->Cell_GetStatus() == nsCell::_Open) &&
                (cell != (*cellAnalyse)))
               setInfluence.insert(cell);
         }
      }

      if (!setInfluence.empty()) {
         for (nsCell::SET_cpBase::const_iterator cellInfluence=setInfluence.begin(); cellInfluence!=setInfluence.end(); cellInfluence++) { // выбрать очередную ячейку из setInfluence
            // ячейка cellInfluence выбрана

            // проверяю, не анализировалась ли уже данная пара. Если анализировалась - то пропустить
            if (!Insert(mmPair, *cellAnalyse, *cellInfluence))
               continue;

            // Отдать на обработку cellAnalyse и cellInfluence
            if (!Analyse2Cell(*cellAnalyse, *cellInfluence, click)) // возможно, что очень долгая операция... :(
               return JOB_ABORT; // работа была прервана
            if (click.m_CoordCell == INCORRECT_COORD) continue;
            return JOB_SUCCESS;
         }
      }
   }
   //if (click.m_CoordCell == INCORRECT_COORD) {
   //   g_Logger.Put(CLogger::LL_DEBUG, TEXT("coord cellAnalyse   = [%i,%i]"), cellAnalyseSave  ->GetCoord().X, cellAnalyseSave  ->GetCoord().Y);
   //   g_Logger.Put(CLogger::LL_DEBUG, TEXT("coord cellInfluence = [%i,%i]"), cellInfluenceSave->GetCoord().X, cellInfluenceSave->GetCoord().Y);
   //}
   // все ячейки уже выбирались, но безрезультатно
   return JOB_SUCCESS;
}

JOB_RESULT CAssistant::Analyse2Cell(const nsCell::CBase* cellAnalyse, const nsCell::CBase* cellInfluence, CClickData& click) {
   m_SetCnF2.clear();
   const int numberNeighbor = m_Mosaic.GetCell(0,0)->GetNeighborNumber();
   // Для cellAnalyse (пройдя по всем соседям) добавить все
   // соседние закрытые ячейки без флагов в множество m_SetCnF2
   for (int k=0; k<numberNeighbor; k++) {
      const COORD coordNeighbor = cellAnalyse->GetNeighborCoord(k);
      if (coordNeighbor == INCORRECT_COORD) continue;
      const nsCell::CBase* cell = m_Mosaic.GetCell(coordNeighbor.X,coordNeighbor.Y);
      if ((cell->Cell_GetStatus() == nsCell::_Close) &&
          (cell->Cell_GetClose () != nsCell::_Flag))
         m_SetCnF2.insert(cell);
   }
   // Для cellInfluence (пройдя по всем соседям) добавить все
   // соседние закрытые ячейки без флагов в множество m_SetCnF2
   for (k=0; k<numberNeighbor; k++) {
      const COORD coordNeighbor = cellInfluence->GetNeighborCoord(k);
      if (coordNeighbor == INCORRECT_COORD) continue;
      const nsCell::CBase* cell = m_Mosaic.GetCell(coordNeighbor.X,coordNeighbor.Y);
      if ((cell->Cell_GetStatus() == nsCell::_Close) &&
          (cell->Cell_GetClose () != nsCell::_Flag))
         m_SetCnF2.insert(cell);
   }

   int n = ((m_SetCnF2.size()==0) ? (2<<(m_SetCnF2.size()-1)) : 1); // pow(2, m_SetCnF2.size());
   m_SetTable.clear();
   for (k=0; k<n; k++) {
      if (WAIT_TIMEOUT == WaitForSingleObject(hEventJob, 0)) {
         return JOB_ABORT; // работа была прервана
      }
      if (TestVariant(k, numberNeighbor, cellAnalyse, cellInfluence))
         m_SetTable.insert(k);
   }

   if (!m_SetTable.empty()) {
      {
         int i = 0;
         const float size = m_SetTable.size();
         for (nsCell::SET_cpBase::const_iterator q=m_SetCnF2.begin(); q!=m_SetCnF2.end(); i++, q++) {
            int mask = ((i==0) ? (2<<(i-1)) : 1); // pow(2,i);
            int count = 0; // счётчик нулей
            for (std::set<int>::const_iterator p=m_SetTable.begin(); p!=m_SetTable.end(); p++) {
               if (!(mask&(*p)))
                  count++;
            }
            const float probability = (float)count/size;
            if(click.m_fProbability < probability) {
               click.m_fProbability = probability;
               click.m_PrbltCell = (*q)->GetCoord();

               cellAnalyseSave   = cellAnalyse; // for debug
               cellInfluenceSave = cellInfluence; // for debug
            }
         }
         if(click.m_fProbability + 0.001 >= 1.0) {
            click.m_CoordCell = click.m_PrbltCell;
            click.m_bIsLeft = true;
            return JOB_SUCCESS;
         }
         /**
         int resultTable = 0;
         for (std::set<int>::const_iterator p=m_SetTable.begin(); p!=m_SetTable.end(); p++) {
            resultTable |= *p;
         }
         for (nsCell::SET_cpBase::const_iterator q=m_SetCnF2.begin(); q!=m_SetCnF2.end(); q++) {
            if (!(resultTable&1)) {
               click.m_CoordCell = (*q)->GetCoord();
               click.m_bIsLeft = true;
               return JOB_SUCCESS;
            }
            resultTable = resultTable>>1;
         }
         /**/
      }
      {
         int resultTable = n-1;
         for (std::set<int>::const_iterator p=m_SetTable.begin(); p!=m_SetTable.end(); p++) {
            resultTable &= *p;
         }
         for (nsCell::SET_cpBase::const_iterator q=m_SetCnF2.begin(); q!=m_SetCnF2.end(); q++) {
            if (resultTable&1) {
               click.m_CoordCell = (*q)->GetCoord();
               click.m_bIsLeft = false;
               return JOB_SUCCESS;
            }
            resultTable = resultTable>>1;
         }
      }
   }
   return JOB_SUCCESS;
}


inline bool CAssistant::TestVariant(const int variant, const int numberNeighbor, const nsCell::CBase* cellAnalyse, const nsCell::CBase* cellInfluence) const {
   int i=1;
   for (nsCell::SET_cpBase::const_iterator p=m_SetCnF2.begin(); p!=m_SetCnF2.end(); p++, i=i<<1) {
      (*p)->SetPresumeFlag(!!(i & variant)); // предполагаемый флажок
   }

   int numberFlag = 0;
   for (int k=0; k<numberNeighbor; k++) {
      COORD coordNeighbor = cellAnalyse->GetNeighborCoord(k);
      if (coordNeighbor == INCORRECT_COORD) continue;
      if (m_Mosaic.GetCell(coordNeighbor.X,coordNeighbor.Y)->Cell_GetStatus() == nsCell::_Close)
         if (m_Mosaic.GetCell(coordNeighbor.X,coordNeighbor.Y)->Cell_GetClose () == nsCell::_Flag) numberFlag++;
         else if (m_Mosaic.GetCell(coordNeighbor.X,coordNeighbor.Y)->GetPresumeFlag())     numberFlag++;
   }
   if (cellAnalyse->Cell_GetOpen() != (nsCell::EOpen)numberFlag) return false;

   numberFlag = 0;
   for (k=0; k<numberNeighbor; k++) {
      COORD coordNeighbor = cellInfluence->GetNeighborCoord(k);
      if (coordNeighbor == INCORRECT_COORD) continue;
      if (m_Mosaic.GetCell(coordNeighbor.X,coordNeighbor.Y)->Cell_GetStatus() == nsCell::_Close)
         if (m_Mosaic.GetCell(coordNeighbor.X,coordNeighbor.Y)->Cell_GetClose () == nsCell::_Flag) numberFlag++;
         else if (m_Mosaic.GetCell(coordNeighbor.X,coordNeighbor.Y)->GetPresumeFlag())     numberFlag++;
   }
   if (cellInfluence->Cell_GetOpen() != (nsCell::EOpen)numberFlag) return false;

   return true;
}

void CAssistant::GameNew() {
   m_SetForOpen    .clear();
   m_SetForFlag    .clear();
   m_SetForAnalyse .clear();
   m_SetCloseNoFlag.clear();
   for (int i = 0; i < m_Mosaic.GetSize().cx; i++)
      for (int j = 0; j < m_Mosaic.GetSize().cy; j++)
         m_SetCloseNoFlag.insert(m_Mosaic.GetCell(i,j));
   m_bSequentialMove = false;
}

void CAssistant::ClickEnd(nsCell::CClickReportContext ClickReportContext) {
   const int numberNeighbor = m_Mosaic.GetCell(0,0)->GetNeighborNumber();

   // Анализ множества setOpenNil
   if (!ClickReportContext.m_SetOpenNil.empty())
      for (nsCell::SET_cpBase::const_iterator p=ClickReportContext.m_SetOpenNil.begin(); p!=ClickReportContext.m_SetOpenNil.end(); p++)
         Delete(m_SetCloseNoFlag, *p);

   // Анализ множества setOpen
   if (!ClickReportContext.m_SetOpen.empty()) {
      for (nsCell::SET_cpBase::const_iterator p=ClickReportContext.m_SetOpen.begin(); p!=ClickReportContext.m_SetOpen.end(); p++) {
         for (int k=0; k<numberNeighbor; k++) {
            COORD coordNeighbor = (*p)->GetNeighborCoord(k);
            if (coordNeighbor == INCORRECT_COORD) continue;
            const nsCell::CBase* cell = m_Mosaic.GetCell(coordNeighbor.X,coordNeighbor.Y);
            if ((cell->Cell_GetStatus() == nsCell::_Close) ||
                (cell->Cell_GetOpen  () == nsCell::_Nil  )) continue;
            m_SetForOpen   .insert(cell);
            m_SetForFlag   .insert(cell);
            m_SetForAnalyse.insert(cell);
         }
         m_SetForOpen   .insert(*p);
         m_SetForFlag   .insert(*p);
         m_SetForAnalyse.insert(*p);
         Delete(m_SetCloseNoFlag, *p);
      }
   }

   // Анализ множества setFlag
   if (!ClickReportContext.m_SetFlag.empty()) {
      for (nsCell::SET_cpBase::const_iterator p=ClickReportContext.m_SetFlag.begin(); p!=ClickReportContext.m_SetFlag.end(); p++) {
         for (int k=0; k<numberNeighbor; k++) {
            COORD coordNeighbor = (*p)->GetNeighborCoord(k);
            if (coordNeighbor == INCORRECT_COORD) continue;
            const nsCell::CBase* cell = m_Mosaic.GetCell(coordNeighbor.X,coordNeighbor.Y);
            if ((cell->Cell_GetStatus() == nsCell::_Close) ||
                (cell->Cell_GetOpen  () == nsCell::_Nil  )) continue;
            m_SetForOpen   .insert(cell);
            m_SetForFlag   .insert(cell);
            m_SetForAnalyse.insert(cell);
         }
         if ((*p)->Cell_GetClose() == nsCell::_Flag)
            Delete(m_SetCloseNoFlag, *p);
         else
            m_SetCloseNoFlag.insert(*p);
      }
   }

   // Чистка множества m_SetForFlag
   while (!m_SetForFlag.empty()) {
      bool isBreak = true;
      for (nsCell::SET_cpBase::const_iterator p=m_SetForFlag.begin(); p!=m_SetForFlag.end(); p++) {
         int closeF = 0; // кол-во закрытых соседей с флажками
         int closeN = 0; // кол-во закрытых соседей без флажков
         for (int k=0; k<numberNeighbor; k++) {
            COORD coordNeighbor = (*p)->GetNeighborCoord(k);
            if (coordNeighbor == INCORRECT_COORD) continue;
            const nsCell::CBase* cell = m_Mosaic.GetCell(coordNeighbor.X,coordNeighbor.Y);
            if (cell->Cell_GetStatus() == nsCell::_Close)
               if (cell->Cell_GetClose() == nsCell::_Flag)
                    closeF++;
               else closeN++;
         }

         if ((!closeN) ||                                       // 2. Удалить ячейки, у которых все закрытые соседи имеют флажки;
             ((*p)->Cell_GetOpen() != (nsCell::EOpen)(closeF+closeN))) // 1. Удалить ячейки, у вес которых вес не равен кол-ву закрытых соседей.
         {
            //g_Logger.Put(CLogger::LL_DEBUG, TEXT("m_SetForFlag del cell=[%i,%i]"), (*p)->GetCoord().X, (*p)->GetCoord().Y);
            //g_Logger.Put(CLogger::LL_DEBUG, TEXT("------------------"));
            m_SetForFlag.erase(*p);
            isBreak = false;
            break; // loop for
         }
      }
      if (isBreak) break; // loop while
   }

   // Чистка множества m_SetForOpen
   while (!m_SetForOpen.empty()) {
      bool isBreak = true;
      for (nsCell::SET_cpBase::const_iterator p=m_SetForOpen.begin(); p!=m_SetForOpen.end(); p++) {
         int closeF = 0; // кол-во закрытых соседей с флажками
         int closeN = 0; // кол-во закрытых соседей без флажков
         for (int k=0; k<numberNeighbor; k++) {
            COORD coordNeighbor = (*p)->GetNeighborCoord(k);
            if (coordNeighbor == INCORRECT_COORD) continue;
            const nsCell::CBase* cell = m_Mosaic.GetCell(coordNeighbor.X,coordNeighbor.Y);
            if (cell->Cell_GetStatus() == nsCell::_Close)
               if (cell->Cell_GetClose() == nsCell::_Flag)
                    closeF++;
               else closeN++;
         }
         if ((!closeN) ||                              // 1. Удалить ячейки, у которых все закрытые соседи имеют флажки
             ((*p)->Cell_GetOpen() != (nsCell::EOpen)closeF)) // 2. Удалить ячейки, у которых вес не равен кол-ву закрытых соседей с флажками
         {
            //g_Logger.Pur(TEXT("m_SetForOpen del cell=[%i,%i]"), (*p)->GetCoord().X, (*p)->GetCoord().Y);
            //g_Logger.Put(CLogger::LL_DEBUG, TEXT("------------------"));
            m_SetForOpen.erase(*p);
            isBreak = false;
            break; // loop for
         }
      }
      if (isBreak) break; // loop while
   }

   // Чистка множества m_SetForAnalyse
   while (!m_SetForAnalyse.empty()) {
      bool isBreak = true;
      for (nsCell::SET_cpBase::const_iterator p=m_SetForAnalyse.begin(); p!=m_SetForAnalyse.end(); p++) {
         int closeF = 0; // кол-во закрытых соседей с флажками
         int closeN = 0; // кол-во закрытых соседей без флажков
         for (int k=0; k<numberNeighbor; k++) {
            COORD coordNeighbor = (*p)->GetNeighborCoord(k);
            if (coordNeighbor == INCORRECT_COORD) continue;
            const nsCell::CBase* cell = m_Mosaic.GetCell(coordNeighbor.X,coordNeighbor.Y);
            if (cell->Cell_GetStatus() == nsCell::_Close)
               if (cell->Cell_GetClose() == nsCell::_Flag)
                    closeF++;
               else closeN++;
         }
         if ((!closeN) ||                                       // 1. Удалить ячейки, у которых все закрытые соседи имеют флажки
             ((*p)->Cell_GetOpen() == (nsCell::EOpen) closeF) ||       // 2. Удалить ячейки, у которых вес равен кол-ву закрытых соседей с флажками
             ((*p)->Cell_GetOpen() == (nsCell::EOpen)(closeF+closeN))) // 3. Удалить ячейки, у вес которых вес равен кол-ву закрытых соседей.
         {
            //g_Logger.Put(CLogger::LL_DEBUG, TEXT("m_SetForAnalyse del cell=[%i,%i]"), (*p)->GetCoord().X, (*p)->GetCoord().Y);
            //g_Logger.Put(CLogger::LL_DEBUG, TEXT("------------------"));
            m_SetForAnalyse.erase(*p);
            isBreak = false;
            break; // loop for
         }
      }
      if (isBreak) break; // loop while
   }

   // На данном этапе:
   // 1. Множество m_SetForFlag - это множество открытых ячеек кликанье на которых приведёт к установке вокруг них флажков.
   // 2. Множество m_SetForOpen - это множество открытых ячеек кликанье на которых приведёт к открытию вокруг них ячеек.
   // 3. Множество m_SetForAnalyse - это множество открытых ячеек кликанье на которых ни к чему не приведёт (ячейка имеет неоткрытых соседей).
   // Ни в одном из этих множест нет одинаковых данных   // На данном этапе:

   //g_Logger.Put(CLogger::LL_DEBUG, TEXT("m_SetForOpen    size = %i"), m_SetForOpen   .size());
   //g_Logger.Put(CLogger::LL_DEBUG, TEXT("m_SetForFlag    size = %i"), m_SetForFlag   .size());
   //g_Logger.Put(CLogger::LL_DEBUG, TEXT("m_SetForAnalyse size = %i"), m_SetForAnalyse.size());
   //g_Logger.Put(CLogger::LL_DEBUG, TEXT("------------------"));
   //g_Logger.Put(CLogger::LL_DEBUG, TEXT("------------------"));
}

void CAssistant::DeleteTableSM() {
   for (V_pbool::const_iterator I=m_VTableSM.begin(); I!=m_VTableSM.end(); I++) {
      delete [] *I;
   }
   m_VTableSM.clear();
}

JOB_RESULT CAssistant::AddNextVectors(const int cDepth, bool* vec) {
   if (WAIT_TIMEOUT == WaitForSingleObject(hEventJob, 0)) {
      return JOB_ABORT; // работа была прервана
   }
   const int size = m_SetCloseNoFlag.size();
   int depth = 0;
   for (int i=size-1; i>=0; i--) {
      if (vec[i]) depth++;
      if (depth == cDepth) break;
   }
   for (int j=i+1; j<size; j++) {
      if (vec[j]) break;
      vec[j] = true;
      vec[i] = false;
      if (!AnalyseVector(vec)) // проверяю "правильность" вектора, и если Оk, то сохранить вариант
         return JOB_ABORT;
      if (cDepth < m_iMineNoFLag)
         if (!AddNextVectors(cDepth+1, vec))
            return JOB_ABORT;
      vec[i] = true;
      vec[j] = false;
   }
   return JOB_SUCCESS;
}

JOB_RESULT CAssistant::AnalyseVector(const bool* vector) {
   const int numberNeighbor = m_Mosaic.GetCell(0,0)->GetNeighborNumber();
   bool result = true;
   for (nsCell::SET_cpBase::const_iterator p=m_SetForAnalyse.begin(); p!=m_SetForAnalyse.end(); p++) {
      if (WAIT_TIMEOUT == WaitForSingleObject(hEventJob, 0)) {
         return JOB_ABORT; // работа была прервана
      }
      int closeF = 0; // кол-во закрытых соседей с флажками
      int closeN = 0; // кол-во закрытых соседей с предполагаемыми флажками
      for (int k=0; k<numberNeighbor; k++) {
         COORD coordNeighbor = (*p)->GetNeighborCoord(k);
         if (coordNeighbor == INCORRECT_COORD) continue;
         const nsCell::CBase* cell = m_Mosaic.GetCell(coordNeighbor.X,coordNeighbor.Y);
         if (cell->Cell_GetStatus() == nsCell::_Close) {
            if (cell->Cell_GetClose() == nsCell::_Flag)
               closeF++;
            else {
               int j=0;
               for (nsCell::SET_cpBase::const_iterator I=m_SetCloseNoFlag.begin(); I!=m_SetCloseNoFlag.end(); I++, j++) {
                  if (cell == *I){
                     if (vector[j]) closeN++;
                     break;
                  }
               }
            }
         }
      }
      if ((*p)->Cell_GetOpen() != (nsCell::EOpen)(closeF+closeN)){
         result = false;
         break;
      }
   }
   const int size = m_SetCloseNoFlag.size();
   if (result) {
      bool* addVec = new bool[size];
      memcpy(addVec, vector, sizeof(bool)*size);
      m_VTableSM.push_back(addVec);
   } else {
/**
      {
         TCHAR *strVal = new TCHAR[size+1]; strVal[size] = TEXT('\0');
         for (int i=0; i<sizeArr; i++)
            strVal[i] = vector[i] ? TEXT('1') : TEXT('0');
         g_Logger.Put(CLogger::LL_DEBUG, TEXT("ignore %s"), strVal);
         delete [] strVal;
      }
/**/
   }
   return JOB_SUCCESS;
}

JOB_RESULT CAssistant::AllOkToSequentialMove() { // проверка условий для начала перебора флажков
                                              // если условия пройдены - подготовиться к началу перебора
   if (m_bSequentialMove)
      return JOB_SUCCESS; // перебор флажков и так уже выполняется

   const int size = m_SetCloseNoFlag.size();

   // проверяю условия для старта перебора флажков
   if (!m_Mosaic.GetGameStatusIsPlay()) return JOB_SUCCESS; // условия не выполнены (флаг m_bSequentialMove непроставлен)
   const float percent = 0.01f*m_Mosaic.GetSize().cx*m_Mosaic.GetSize().cy;

   //g_Logger.Put(CLogger::LL_DEBUG, TEXT("m_SetCloseNoFlag.size() = %i"), size);
   //g_Logger.Put(CLogger::LL_DEBUG, TEXT("closeNumber = %i"), closeNumber);

   /**
   if ((size > percent) || (size > 16)) {
      return JOB_SUCCESS; // условия не выполнены (флаг m_bSequentialMove непроставлен)
   }/**/
   m_iMineNoFLag = 0; // кол-во неотмеченных флажками мин
   for (nsCell::SET_cpBase::const_iterator p=m_SetCloseNoFlag.begin(); p!=m_SetCloseNoFlag.end(); p++)
      if ((*p)->Cell_GetOpen() == nsCell::_Mine)
         m_iMineNoFLag++;
   if (m_iMineNoFLag > percent) return JOB_SUCCESS; // условия не выполнены (флаг m_bSequentialMove непроставлен)

   // можно начать перебор флагов
   // Для этого надо узнать все варианты расположения флажков
   DeleteTableSM(); // удаляю всё старое...

#ifdef _DEBUG
   g_Logger.Put(CLogger::LL_DEBUG, TEXT("size = %i"), size);
#endif

   bool *vec = new bool[size];
   for (int i=0; i<size; i++)
      vec[i] = (i<m_iMineNoFLag);
   if (!AnalyseVector(vec)) { // проверяю "правильность" вектора, и если Оk, то сохранить вариант
      delete [] vec;
      return JOB_ABORT;
   }
   if (!AddNextVectors(1, vec)) { // узнаю все варианты расположения флажков
      delete [] vec;
      return JOB_ABORT; // работа была прервана
   }
   delete [] vec;
/**
   g_Logger.Put(CLogger::LL_DEBUG, TEXT("-------"));
   for (V_pbool::const_iterator I=m_VTableSM.begin(); I!=m_VTableSM.end(); I++) {
      TCHAR *strVal = new TCHAR[size+1]; strVal[size] = TEXT('\0');
      for (int i=0; i<sizeArr; i++)
         strVal[i] = (*I)[i] ? TEXT('1') : TEXT('0');
      g_Logger.Put(CLogger::LL_DEBUG, TEXT("bit Ok %s"), strVal);
      delete [] strVal;
   }
   g_Logger.Put(CLogger::LL_DEBUG, TEXT("-------"));
/**/
   m_bSequentialMove = true; // готов к началу перебора флажков
   m_iRow = m_iCol = 0;
   return JOB_SUCCESS;//JOB_ABORT;//
}

void CAssistant::SequentialMove(CClickData& click) {
   const int size = m_SetCloseNoFlag.size();
   nsCell::EClose oldClose;

   click.m_bIsLeft = false;
   do {
      click.m_Close  = m_VTableSM[m_iRow][m_iCol] ? nsCell::_Unknown : nsCell::_Clear;
      {
         int k=0;
         for (nsCell::SET_cpBase::const_iterator p=m_SetCloseNoFlag.begin(); p!=m_SetCloseNoFlag.end(); p++, k++)
            if (k==m_iCol) {
               click.m_CoordCell = (*p)->GetCoord();
               oldClose = (*p)->Cell_GetClose();
               break;
            }
      }
      if (++m_iCol >= m_SetCloseNoFlag.size()) {
         m_iCol = 0;
         if (++m_iRow >= m_VTableSM.size()) // последняя строка пройдена...
            m_bSequentialMove = false;
      }
   } while((click.m_Close == oldClose) // нет смысла устанавливать то что уже установлено
           && m_bSequentialMove);
}

/*
void CAssistant::Print() {
   for (nsCell::SET_cpBase::const_iterator p=m_SetCloseNoFlag.begin(); p!=m_SetCloseNoFlag.end(); p++)
      g_Logger.Put(CLogger::LL_DEBUG, TEXT("coord = [%i,%i]"), (*p)->GetCoord().X, (*p)->GetCoord().Y);
   g_Logger.Put(CLogger::LL_DEBUG, TEXT("size = %i"), m_SetCloseNoFlag.size());
   g_Logger.Put(CLogger::LL_DEBUG, TEXT("------------"));
}
*/

////////////////////////////////////////////////////////////////////////////////
//                            other implementation
////////////////////////////////////////////////////////////////////////////////
inline bool Insert(MM_cpTB& mm, const nsCell::CBase* cellAnalyse, const nsCell::CBase* cellInfluence) {
   typedef MM_cpTB::iterator I;
   std::pair<I,I> b = mm.equal_range(cellAnalyse);
   for (I i=b.first; i!=b.second; i++) {
      if ((*i).second == cellInfluence) return false; // уже есть
   }
   typedef MM_cpTB::value_type VT;
   const VT vt1(cellAnalyse  , cellInfluence);
   const VT vt2(cellInfluence, cellAnalyse  );
   mm.insert(vt1);
   mm.insert(vt2);

 //mm.insert(std::make_pair(cellAnalyse  , cellInfluence));
 //mm.insert(std::make_pair(cellInfluence, cellAnalyse  ));
/*
   g_Logger.Put(CLogger::LL_DEBUG, TEXT("mm.insert: [%i,%i] [%i,%i]"), cellAnalyse->GetCoord().X, cellAnalyse->GetCoord().Y, cellInfluence->GetCoord().X, cellInfluence->GetCoord().Y);
 //g_Logger.Put(CLogger::LL_DEBUG, TEXT("mm.size = %i"), mm.size());
*/
   return true;
}

inline void Delete(nsCell::SET_cpBase& m_SetCloseNoFlag, const nsCell::CBase* cell4Del) {
   if (m_SetCloseNoFlag.empty()) return;
   nsCell::SET_cpBase::const_iterator I = m_SetCloseNoFlag.find(cell4Del);
   if (I == m_SetCloseNoFlag.end()) return;
   m_SetCloseNoFlag.erase(*I);
}

}; // namespace nsMosaic
