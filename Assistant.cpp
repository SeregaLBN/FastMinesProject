////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "Assistant.cpp"
//
// ���������� ������ CAssistant
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

JOB_RESULT CAssistant::FindCell(OUT CClickData &Click) {
   if (!m_SetForOpen.empty()) {
      Click.m_CoordCell = (*m_SetForOpen.begin())->GetCoord();
      Click.m_bIsLeft = true;
   } else {
      if (!m_SetForFlag.empty()) {
         Click.m_CoordCell = (*m_SetForFlag.begin())->GetCoord();
         Click.m_bIsLeft = true;
      } else {
         if (!m_SetForAnalyse.empty()) {
            //::SetCursor(LoadCursor(ghInstance, IDC_WAIT));
            if (!Analyse(Click)) // ������ ���� ��������
               return JOB_ABORT;
            //::SetCursor(LoadCursor(ghInstance, IDC_ARROW));
/**
            g_Logger.Put(CLogger::LL_DEBUG, TEXT("coord = %i"), Click.m_CoordCell);
            if (Click.m_CoordCell != INCORRECT_COORD) {
               if (Click.m_bIsLeft)
                  g_Logger.Put(CLogger::LL_DEBUG, TEXT("coord for left  cli�k"));
               else
                  g_Logger.Put(CLogger::LL_DEBUG, TEXT("coord for right cli�k"));
            }
/**/
         }
      }
   }
   return JOB_SUCCESS;
}

//const nsCell::CBase *cellAnalyseSave;
//const nsCell::CBase *cellInfluenceSave;

JOB_RESULT CAssistant::Analyse(OUT CClickData &Click) {
   const int iNumberNeighbor = m_Mosaic.GetNeighborNumber();
/*
   g_Logger.Put(CLogger::LL_DEBUG, TEXT("------------------"));
*/
   MM_cpTB mmPair; // ������������������ ����

   for (nsCell::SET_cpBase::const_iterator cellAnalyse=m_SetForAnalyse.begin(); cellAnalyse!=m_SetForAnalyse.end(); cellAnalyse++) { // ������� ��������� ������ �� m_SetForAnalyse
      // ������ cellAnalyse �������

      nsCell::SET_cpBase setInfluence, setCnF; // ��������� �������� ��������� setInfluence � setCnF (Close & noFlag)

      // ��� cellAnalyse (������ �� ���� �������) �������� ���
      // �������� �������� ������ ��� ������ � ��������� setCnF
      for (int k=0; k<iNumberNeighbor; k++) {
         const COORD &coordNeighbor = (*cellAnalyse)->GetNeighborCoord(k);
         if (coordNeighbor == INCORRECT_COORD) continue;
         const nsCell::CBase* cell = m_Mosaic.GetCell(coordNeighbor);
         if ((cell->Cell_GetStatus() == nsCell::_Close) &&
             (cell->Cell_GetClose () != nsCell::_Flag))
         {
            setCnF.insert(cell);
         }
      }

      // �������� �� ����� ����. setCnF.
      // ���� �������� ������� (�� ���������� cellAnalyse) �������� � ��������� setInfluence.
      for (nsCell::SET_cpBase::const_iterator p=setCnF.begin(); p!=setCnF.end(); p++) {
         for (int k=0; k<iNumberNeighbor; k++) {
            const COORD &coordNeighbor = (*p)->GetNeighborCoord(k);
            if (coordNeighbor == INCORRECT_COORD) continue;
            const nsCell::CBase* cell = m_Mosaic.GetCell(coordNeighbor);
            if ((cell->Cell_GetStatus() == nsCell::_Open) &&
                (cell != (*cellAnalyse)))
            {
               setInfluence.insert(cell);
            }
         }
      }

      if (!setInfluence.empty()) {
         for (nsCell::SET_cpBase::const_iterator cellInfluence=setInfluence.begin(); cellInfluence!=setInfluence.end(); cellInfluence++) { // ������� ��������� ������ �� setInfluence
            // ������ cellInfluence �������

            // ��������, �� ��������������� �� ��� ������ ����. ���� ��������������� - �� ���������.
            if (!Insert(mmPair, *cellAnalyse, *cellInfluence))
               continue;

            // ������ �� ��������� ������������� ������ cellAnalyse � �������� �� �� ������ cellInfluence.
            if (!Analyse2Cell(*cellAnalyse, *cellInfluence, Click)) // ��������, ��� ����� ������ ��������... :(
               return JOB_ABORT; // ������ ���� ��������
            if (Click.m_CoordCell == INCORRECT_COORD) continue;
            return JOB_SUCCESS;
         }
      }
   }
   //if (Click.m_CoordCell == INCORRECT_COORD) {
   //   g_Logger.Put(CLogger::LL_DEBUG, TEXT("coord cellAnalyse   = [%i,%i]"), cellAnalyseSave  ->GetCoord().X, cellAnalyseSave  ->GetCoord().Y);
   //   g_Logger.Put(CLogger::LL_DEBUG, TEXT("coord cellInfluence = [%i,%i]"), cellInfluenceSave->GetCoord().X, cellInfluenceSave->GetCoord().Y);
   //}
   // ��� ������ ��� ����������, �� ��������������
   return JOB_SUCCESS;
}

JOB_RESULT CAssistant::Analyse2Cell(const nsCell::CBase *cellAnalyse, const nsCell::CBase *cellInfluence, OUT CClickData &Click) {
   m_SetCnF2.clear();
   const int iNumberNeighbor = m_Mosaic.GetCell(0,0)->GetNeighborNumber();
   // ��� cellAnalyse (������ �� ���� �������) �������� ���
   // �������� �������� ������ ��� ������ � ��������� m_SetCnF2
   for (int k=0; k<iNumberNeighbor; k++) {
      const COORD &coordNeighbor = cellAnalyse->GetNeighborCoord(k);
      if (coordNeighbor == INCORRECT_COORD) continue;
      const nsCell::CBase* cell = m_Mosaic.GetCell(coordNeighbor);
      if ((cell->Cell_GetStatus() == nsCell::_Close) &&
          (cell->Cell_GetClose () != nsCell::_Flag))
      {
         m_SetCnF2.insert(cell);
      }
   }
   // ��� cellInfluence (������ �� ���� �������) �������� ���
   // �������� �������� ������ ��� ������ � ��������� m_SetCnF2
   for (k=0; k<iNumberNeighbor; k++) {
      const COORD &coordNeighbor = cellInfluence->GetNeighborCoord(k);
      if (coordNeighbor == INCORRECT_COORD) continue;
      const nsCell::CBase* cell = m_Mosaic.GetCell(coordNeighbor);
      if ((cell->Cell_GetStatus() == nsCell::_Close) &&
          (cell->Cell_GetClose () != nsCell::_Flag))
      {
         m_SetCnF2.insert(cell);
      }
   }

   // ��������� m_SetCnF2 - ������ � ������� ����� ������ � ��������� �����������
   // �������������� ������. �.�. ������ ����� ��������� m_SetCnF2 ����������
   // ���������� ���� ������������ (������ � ��������) ���������� �������, ����� ������� -
   // ���������� ������������ ���������� iMaxCombination ����� 2 � ������� ������ ��������� m_SetCnF2.
   // ����������� ���������� ������ ��� ���������� ����� � integer'e...
   int iMaxCombination = 1<<m_SetCnF2.size(); // pow(2, m_SetCnF2.size());
   m_SetPossibleCombinations.clear();
   for (k=0; k<iMaxCombination; k++) {
      if (WAIT_TIMEOUT == ::WaitForSingleObject(hEventJob, 0)) {
         return JOB_ABORT; // ������ ���� ��������
      }
      // �������� ������ ����������...
      if (TestCombination(k, cellAnalyse, cellInfluence)) {
         //... ���� ��� �������� - �������� �.
         m_SetPossibleCombinations.insert(k);
      }
   }

   if (!m_SetPossibleCombinations.empty()) {
      // 1. ���� �� ���� ���������� ����������� ���� ���������� ������� ������� ��� �� ����� ����� ����,
      //    �� ��� ��������, ��� ������ (��������������� ���� ������� ������� � m_SetCnF2) ��� ����.
      //    ���������� ������, �������� ��� ���������� ������ � ��������� ��������� CClickData.
      //
      // �� ����� ���� �� ���� �������... ���� ���������� ������ ����� ���� �� ��� ����������,
      // �� � �� �������� ������ ������, � �������� ������ � ������� ����������� ���������� ���� ����������.
      // �.�. � ������������� ������ ����� ��� � ������� �����������.
      {
         int i = 0;
         const int iSize = m_SetPossibleCombinations.size();
         for (nsCell::SET_cpBase::const_iterator q=m_SetCnF2.begin(); q!=m_SetCnF2.end(); i++, q++) {
            int iMask = 1<<i; // pow(2,i);
            int count = 0; // ������� �����
            for (SET_Int::const_iterator p=m_SetPossibleCombinations.begin(); p!=m_SetPossibleCombinations.end(); p++) {
               if (!(iMask & (*p))) {
                  count++;
                  if (count == iSize) break; // 100%
               }
            }
            const float fProbability = (float)count/iSize;
            if(Click.m_fProbability < fProbability) {
               Click.m_fProbability = fProbability;
               Click.m_PrbltCell = (*q)->GetCoord();

               if (count == iSize) break; // 100%

               //cellAnalyseSave   = cellAnalyse; // for debug
               //cellInfluenceSave = cellInfluence; // for debug
            }
         }
         if(Click.m_fProbability + 0.001 >= 1.0) // 100%
         {
            Click.m_CoordCell = Click.m_PrbltCell;
            Click.m_bIsLeft = true;
            return JOB_SUCCESS;
         }
         /**
         int resultTable = 0;
         for (SET_Int::const_iterator p=m_SetPossibleCombinations.begin(); p!=m_SetPossibleCombinations.end(); p++) {
            resultTable |= *p;
         }
         for (nsCell::SET_cpBase::const_iterator q=m_SetCnF2.begin(); q!=m_SetCnF2.end(); q++) {
            if (!(resultTable&1)) {
               Click.m_CoordCell = (*q)->GetCoord();
               Click.m_bIsLeft = true;
               return JOB_SUCCESS;
            }
            resultTable = resultTable>>1;
         }
         /**/
      }
      // 2. ���� �� ���� ���������� ����������� ���� ���������� ������� ������� ��� �� ����� ����� ��������,
      //    �� ��� ��������, ��� ������ (��������������� ���� ������� ������� � m_SetCnF2) � �����.
      //    ���������� ������, �������� ��� ���������� ������ � ��������� ��������� CClickData.
      {
         int iMask = iMaxCombination-1; // ��� ��������
         for (SET_Int::const_iterator p=m_SetPossibleCombinations.begin(); p!=m_SetPossibleCombinations.end(); p++) {
            iMask &= *p;
         }
         for (nsCell::SET_cpBase::const_iterator q=m_SetCnF2.begin(); q!=m_SetCnF2.end(); q++) {
            if (iMask & 1) {
               Click.m_CoordCell = (*q)->GetCoord();
               Click.m_bIsLeft = false;
               return JOB_SUCCESS;
            }
            iMask = iMask>>1;
         }
      }
   }
   return JOB_SUCCESS;
}


inline bool CAssistant::TestCombination(const int iCombination, const nsCell::CBase *cellAnalyse, const nsCell::CBase *cellInfluence) const {
   const int iNumberNeighbor = m_Mosaic.GetCell(0,0)->GetNeighborNumber();
   int i=1;
   for (nsCell::SET_cpBase::const_iterator p=m_SetCnF2.begin(); p!=m_SetCnF2.end(); p++, i=i<<1) {
      (*p)->SetPresumeFlag(!!(i & iCombination)); // �������������� ������
   }

   int numberFlag = 0;
   for (int k=0; k<iNumberNeighbor; k++) {
      const COORD &coordNeighbor = cellAnalyse->GetNeighborCoord(k);
      if (coordNeighbor == INCORRECT_COORD) continue;
      if (m_Mosaic.GetCell(coordNeighbor)->Cell_GetStatus() == nsCell::_Close)
         if (m_Mosaic.GetCell(coordNeighbor)->Cell_GetClose() == nsCell::_Flag)
            numberFlag++;
         else
            if (m_Mosaic.GetCell(coordNeighbor)->GetPresumeFlag())
               numberFlag++;
   }
   if (cellAnalyse->Cell_GetOpen() != (nsCell::EOpen)numberFlag) return false;

   numberFlag = 0;
   for (k=0; k<iNumberNeighbor; k++) {
      const COORD &coordNeighbor = cellInfluence->GetNeighborCoord(k);
      if (coordNeighbor == INCORRECT_COORD) continue;
      if (m_Mosaic.GetCell(coordNeighbor)->Cell_GetStatus() == nsCell::_Close)
         if (m_Mosaic.GetCell(coordNeighbor)->Cell_GetClose() == nsCell::_Flag)
            numberFlag++;
         else
            if (m_Mosaic.GetCell(coordNeighbor)->GetPresumeFlag())
               numberFlag++;
   }
   if (cellInfluence->Cell_GetOpen() != (nsCell::EOpen)numberFlag) return false;

   return true;
}

void CAssistant::InitForNewGame() {
   m_SetForOpen    .clear();
   m_SetForFlag    .clear();
   m_SetForAnalyse .clear();
   m_SetCloseNoFlag.clear();
   for (int i = 0; i < m_Mosaic.GetSize().cx; i++)
      for (int j = 0; j < m_Mosaic.GetSize().cy; j++)
         m_SetCloseNoFlag.insert(m_Mosaic.GetCell(i,j));
   m_bSequentialMove = false;
}

void CAssistant::ClickEnd(const nsCell::CClickReportContext &ClickReportContext) {
   const int iNumberNeighbor = m_Mosaic.GetCell(0,0)->GetNeighborNumber();

   // ������ ��������� m_SetOpenNil
   if (!ClickReportContext.m_SetOpenNil.empty())
      for (nsCell::SET_cpBase::const_iterator p=ClickReportContext.m_SetOpenNil.begin(); p!=ClickReportContext.m_SetOpenNil.end(); p++)
         nsMosaic::Delete(m_SetCloseNoFlag, *p);

   // ������ ��������� m_SetOpen
   if (!ClickReportContext.m_SetOpen.empty()) {
      for (nsCell::SET_cpBase::const_iterator p =ClickReportContext.m_SetOpen.begin();
                                              p!=ClickReportContext.m_SetOpen.end(); p++)
      {
         for (int k=0; k<iNumberNeighbor; k++) {
            //g_Logger.Put(CLogger::LL_DEBUG, TEXT("p=0x%08X; k=%d"), (*p), k);
            const COORD &coordNeighbor = (*p)->GetNeighborCoord(k);
            if (coordNeighbor == INCORRECT_COORD) continue;
            const nsCell::CBase* cell = m_Mosaic.GetCell(coordNeighbor);
            if ((cell->Cell_GetStatus() == nsCell::_Close) ||
                (cell->Cell_GetOpen  () == nsCell::_Nil  )) continue;
            m_SetForOpen   .insert(cell);
            m_SetForFlag   .insert(cell);
            m_SetForAnalyse.insert(cell);
         }
         m_SetForOpen   .insert(*p);
         m_SetForFlag   .insert(*p);
         m_SetForAnalyse.insert(*p);
         nsMosaic::Delete(m_SetCloseNoFlag, *p);
      }
   }

   // ������ ��������� m_SetFlag
   if (!ClickReportContext.m_SetFlag.empty()) {
      for (nsCell::SET_cpBase::const_iterator p=ClickReportContext.m_SetFlag.begin(); p!=ClickReportContext.m_SetFlag.end(); p++) {
         for (int k=0; k<iNumberNeighbor; k++) {
            const COORD &coordNeighbor = (*p)->GetNeighborCoord(k);
            if (coordNeighbor == INCORRECT_COORD) continue;
            const nsCell::CBase* cell = m_Mosaic.GetCell(coordNeighbor);
            if ((cell->Cell_GetStatus() == nsCell::_Close) ||
                (cell->Cell_GetOpen  () == nsCell::_Nil  )) continue;
            m_SetForOpen   .insert(cell);
            m_SetForFlag   .insert(cell);
            m_SetForAnalyse.insert(cell);
         }
         if ((*p)->Cell_GetClose() == nsCell::_Flag)
            nsMosaic::Delete(m_SetCloseNoFlag, *p);
         else
            m_SetCloseNoFlag.insert(*p); // ����� ������������ ���� ������...
      }
   }

   // ������ ��������� m_SetForFlag
   while (!m_SetForFlag.empty()) {
      bool isBreak = true;
      for (nsCell::SET_cpBase::const_iterator p=m_SetForFlag.begin(); p!=m_SetForFlag.end(); p++) {
         int iCloseF = 0; // ���-�� �������� ������� � ��������
         int iCloseN = 0; // ���-�� �������� ������� ��� �������
         for (int k=0; k<iNumberNeighbor; k++) {
            const COORD &coordNeighbor = (*p)->GetNeighborCoord(k);
            if (coordNeighbor == INCORRECT_COORD) continue;
            const nsCell::CBase* cell = m_Mosaic.GetCell(coordNeighbor);
            if (cell->Cell_GetStatus() == nsCell::_Close)
               if (cell->Cell_GetClose() == nsCell::_Flag)
                    iCloseF++;
               else iCloseN++;
         }

         if ((!iCloseN) ||                                              // 2. ������� ������, � ������� ��� �������� ������ ����� ������;
             ((*p)->Cell_GetOpen() != (nsCell::EOpen)(iCloseF+iCloseN))) // 1. ������� ������, � ��� ������� ��� �� ����� ���-�� �������� �������.
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

   // ������ ��������� m_SetForOpen
   while (!m_SetForOpen.empty()) {
      bool isBreak = true;
      for (nsCell::SET_cpBase::const_iterator p=m_SetForOpen.begin(); p!=m_SetForOpen.end(); p++) {
         int iCloseF = 0; // ���-�� �������� ������� � ��������
         int iCloseN = 0; // ���-�� �������� ������� ��� �������
         for (int k=0; k<iNumberNeighbor; k++) {
            const COORD &coordNeighbor = (*p)->GetNeighborCoord(k);
            if (coordNeighbor == INCORRECT_COORD) continue;
            const nsCell::CBase* cell = m_Mosaic.GetCell(coordNeighbor);
            if (cell->Cell_GetStatus() == nsCell::_Close)
               if (cell->Cell_GetClose() == nsCell::_Flag)
                    iCloseF++;
               else iCloseN++;
         }
         if ((!iCloseN) ||                                     // 1. ������� ������, � ������� ��� �������� ������ ����� ������
             ((*p)->Cell_GetOpen() != (nsCell::EOpen)iCloseF)) // 2. ������� ������, � ������� ��� �� ����� ���-�� �������� ������� � ��������
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

   // ������ ��������� m_SetForAnalyse
   while (!m_SetForAnalyse.empty()) {
      bool isBreak = true;
      for (nsCell::SET_cpBase::const_iterator p=m_SetForAnalyse.begin(); p!=m_SetForAnalyse.end(); p++) {
         int iCloseF = 0; // ���-�� �������� ������� � ��������
         int iCloseN = 0; // ���-�� �������� ������� ��� �������
         for (int k=0; k<iNumberNeighbor; k++) {
            const COORD &coordNeighbor = (*p)->GetNeighborCoord(k);
            if (coordNeighbor == INCORRECT_COORD) continue;
            const nsCell::CBase* cell = m_Mosaic.GetCell(coordNeighbor);
            if (cell->Cell_GetStatus() == nsCell::_Close)
               if (cell->Cell_GetClose() == nsCell::_Flag)
                    iCloseF++;
               else iCloseN++;
         }
         if ((!iCloseN) ||                                              // 1. ������� ������, � ������� ��� �������� ������ ����� ������
             ((*p)->Cell_GetOpen() == (nsCell::EOpen) iCloseF) ||       // 2. ������� ������, � ������� ��� ����� ���-�� �������� ������� � ��������
             ((*p)->Cell_GetOpen() == (nsCell::EOpen)(iCloseF+iCloseN))) // 3. ������� ������, � ��� ������� ��� ����� ���-�� �������� �������.
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

   // �� ������ �����:
   // 1. ��������� m_SetForFlag - ��� ��������� �������� ����� �������� �� ������� ������� � ��������� ������ ��� �������.
   // 2. ��������� m_SetForOpen - ��� ��������� �������� ����� �������� �� ������� ������� � �������� ������ ��� �����.
   // 3. ��������� m_SetForAnalyse - ��� ��������� �������� ����� �������� �� ������� �� � ���� �� ������� (� ������ ���� ���������� ������).
   // 4. ��������� m_SetCloseNoFlag - ��� ��������� �������� ����� ��� �������.
   // �� � ����� �� ���� ������� ��� ���������� ������

   //g_Logger.Put(CLogger::LL_DEBUG, TEXT("m_SetForOpen    size = %i"), m_SetForOpen   .size());
   //g_Logger.Put(CLogger::LL_DEBUG, TEXT("m_SetForFlag    size = %i"), m_SetForFlag   .size());
   //g_Logger.Put(CLogger::LL_DEBUG, TEXT("m_SetForAnalyse size = %i"), m_SetForAnalyse.size());
   //g_Logger.Put(CLogger::LL_DEBUG, TEXT("------------------"));
   //g_Logger.Put(CLogger::LL_DEBUG, TEXT("------------------"));
}

void CAssistant::DeleteTableSM() {
   for (V_pBool::const_iterator I=m_VTableSM.begin(); I!=m_VTableSM.end(); I++) {
      delete [] *I;
   }
   m_VTableSM.clear();
}

JOB_RESULT CAssistant::AddNextVectors(const int iDepth, bool *pbVector) {
   if (WAIT_TIMEOUT == ::WaitForSingleObject(hEventJob, 0)) {
      return JOB_ABORT; // ������ ���� ��������
   }
   const int iSize = m_SetCloseNoFlag.size();
   int depth = 0;
   for (int i=iSize-1; i>=0; i--) {
      if (pbVector[i]) depth++;
      if (depth == iDepth) break;
   }
   for (int j=i+1; j<iSize; j++) {
      if (pbVector[j]) break;
      pbVector[j] = true;
      pbVector[i] = false;
      if (!AnalyseVector(pbVector)) // �������� "������������" �������, � ���� �k, �� ��������� �������
         return JOB_ABORT;
      if (iDepth < m_iMineNoFLag)
         if (!AddNextVectors(iDepth+1, pbVector))
            return JOB_ABORT;
      pbVector[i] = true;
      pbVector[j] = false;
   }
   return JOB_SUCCESS;
}

JOB_RESULT CAssistant::AnalyseVector(const bool *pbVector) {
   const int iNumberNeighbor = m_Mosaic.GetCell(0,0)->GetNeighborNumber();
   bool bResult = true;
   for (nsCell::SET_cpBase::const_iterator p=m_SetForAnalyse.begin(); p!=m_SetForAnalyse.end(); p++) {
      if (WAIT_TIMEOUT == ::WaitForSingleObject(hEventJob, 0)) {
         return JOB_ABORT; // ������ ���� ��������
      }
      int iCloseF = 0; // ���-�� �������� ������� � ��������
      int iCloseN = 0; // ���-�� �������� ������� � ��������������� ��������
      for (int k=0; k<iNumberNeighbor; k++) {
         const COORD &coordNeighbor = (*p)->GetNeighborCoord(k);
         if (coordNeighbor == INCORRECT_COORD) continue;
         const nsCell::CBase* cell = m_Mosaic.GetCell(coordNeighbor);
         if (cell->Cell_GetStatus() == nsCell::_Close) {
            if (cell->Cell_GetClose() == nsCell::_Flag)
               iCloseF++;
            else {
               int j=0;
               for (nsCell::SET_cpBase::const_iterator I=m_SetCloseNoFlag.begin(); I!=m_SetCloseNoFlag.end(); I++, j++) {
                  if (cell == *I){
                     if (pbVector[j]) iCloseN++;
                     break;
                  }
               }
            }
         }
      }
      if ((*p)->Cell_GetOpen() != (nsCell::EOpen)(iCloseF+iCloseN)){
         bResult = false;
         break;
      }
   }
   const int iSize = m_SetCloseNoFlag.size();
   if (bResult) {
      bool* bAddVec = new bool[iSize];
      memcpy(bAddVec, pbVector, sizeof(bool)*iSize);
      m_VTableSM.push_back(bAddVec);
   } else {
/**
      {
         TCHAR *strVal = new TCHAR[iSize+1]; strVal[iSize] = TEXT('\0');
         for (int i=0; i<sizeArr; i++)
            strVal[i] = pbVector[i] ? TEXT('1') : TEXT('0');
         g_Logger.Put(CLogger::LL_DEBUG, TEXT("ignore %s"), strVal);
         delete [] strVal;
      }
/**/
   }
   return JOB_SUCCESS;
}

JOB_RESULT CAssistant::SequentialMoveCanBegin() { // �������� ������� ��� ������ �������� �������
                                                  // ���� ������� �������� - ������������� � ������ ��������
   if (m_bSequentialMove)
      return JOB_SUCCESS; // ������� ������� � ��� ��� �����������

   const int iSize = m_SetCloseNoFlag.size();

   // �������� ������� ��� ������ �������� �������
   if (!m_Mosaic.GetGameStatusIsPlay()) return JOB_SUCCESS; // ������� �� ��������� (���� m_bSequentialMove ������������)
   const float fPercent = 0.01f*m_Mosaic.GetSize().cx*m_Mosaic.GetSize().cy;

   //g_Logger.Put(CLogger::LL_DEBUG, TEXT("m_SetCloseNoFlag.size() = %i"), iSize);
   //g_Logger.Put(CLogger::LL_DEBUG, TEXT("closeNumber = %i"), closeNumber);

   /**
   if ((iSize > fPercent) || (iSize > 16)) {
      return JOB_SUCCESS; // ������� �� ��������� (���� m_bSequentialMove ������������)
   }/**/
   m_iMineNoFLag = 0; // ���-�� ������������ �������� ���
   for (nsCell::SET_cpBase::const_iterator p=m_SetCloseNoFlag.begin(); p!=m_SetCloseNoFlag.end(); p++)
      if ((*p)->Cell_GetOpen() == nsCell::_Mine)
         m_iMineNoFLag++;
   if (m_iMineNoFLag > fPercent) return JOB_SUCCESS; // ������� �� ��������� (���� m_bSequentialMove ������������)

   // ����� ������ ������� ������
   // ��� ����� ���� ������ ��� �������� ������������ �������
   DeleteTableSM(); // ������ �� ������...

#ifdef _DEBUG
   g_Logger.Put(CLogger::LL_DEBUG, TEXT("iSize = %i"), iSize);
#endif

   bool *pbVector = new bool[iSize];
   for (int i=0; i<iSize; i++) {
      pbVector[i] = (i<m_iMineNoFLag);
   }
   if (!AnalyseVector(pbVector)) { // �������� "������������" �������, � ���� �k, �� ��������� �������
      delete [] pbVector;
      return JOB_ABORT;
   }
   if (!AddNextVectors(1, pbVector)) { // ����� ��� �������� ������������ �������
      delete [] pbVector;
      return JOB_ABORT; // ������ ���� ��������
   }
   delete [] pbVector;
/**
   g_Logger.Put(CLogger::LL_DEBUG, TEXT("-------"));
   for (V_pBool::const_iterator I=m_VTableSM.begin(); I!=m_VTableSM.end(); I++) {
      TCHAR *strVal = new TCHAR[iSize+1]; strVal[iSize] = TEXT('\0');
      for (int i=0; i<sizeArr; i++)
         strVal[i] = (*I)[i] ? TEXT('1') : TEXT('0');
      g_Logger.Put(CLogger::LL_DEBUG, TEXT("bit Ok %s"), strVal);
      delete [] strVal;
   }
   g_Logger.Put(CLogger::LL_DEBUG, TEXT("-------"));
/**/
   m_bSequentialMove = true; // ����� � ������ �������� �������
   m_iRow = m_iCol = 0;
   return JOB_SUCCESS;//JOB_ABORT;//
}

void CAssistant::SequentialMove(CClickData &Click) {
   const int iSize = m_SetCloseNoFlag.size();
   nsCell::EClose oldClose;

   Click.m_bIsLeft = false;
   do {
      Click.m_Close = m_VTableSM[m_iRow][m_iCol] ? nsCell::_Unknown : nsCell::_Clear;
      {
         int k=0;
         for (nsCell::SET_cpBase::const_iterator p=m_SetCloseNoFlag.begin(); p!=m_SetCloseNoFlag.end(); p++, k++)
            if (k==m_iCol) {
               Click.m_CoordCell = (*p)->GetCoord();
               oldClose = (*p)->Cell_GetClose();
               break;
            }
      }
      if (++m_iCol >= m_SetCloseNoFlag.size()) {
         m_iCol = 0;
         if (++m_iRow >= m_VTableSM.size()) // ��������� ������ ��������...
            m_bSequentialMove = false;
      }
   } while((Click.m_Close == oldClose) // ��� ������ ������������� �� ��� ��� �����������
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
inline bool Insert(MM_cpTB& mm, const nsCell::CBase *cellAnalyse, const nsCell::CBase *cellInfluence) {
   typedef MM_cpTB::iterator I;
   std::pair<I,I> b = mm.equal_range(cellAnalyse);
   for (I i=b.first; i!=b.second; i++) {
      if ((*i).second == cellInfluence) return false; // ��� ����
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

inline void Delete(nsCell::SET_cpBase& set, const nsCell::CBase* cell4Del) {
   if (!set.empty()) {
      nsCell::SET_cpBase::const_iterator I = set.find(cell4Del);
      if (I != set.end()) {
         set.erase(*I);
      }
   }
}

/*
// WM_TIMER
void CFastMines2Project::ProjectOnTimer(HWND hwnd, UINT id) {
   switch (id) {
   case ID_EVENT_TIMER_INACTION:
      KillTimer(hwnd, ID_EVENT_TIMER_INACTION);
      SetTimer (hwnd, ID_EVENT_TIMER_JOB, GetAssistant().m_iTimeoutJob, NULL);
   case ID_EVENT_TIMER_JOB:
      // �������� ������� ��� ������ ������ (����������)
      if (!GetAssistant().m_bUse) return;
      if (hwnd != GetForegroundWindow()) return;
      if (GetAssistant().m_bAutoStart) {
         if (!m_Mosaic.GetGameStatusIsReady())
            m_Mosaic.GameNew();
      } else {
         if (!m_Mosaic.GetGameStatusIsPlay()) {
            return;
         }
      }
      if (m_Mosaic.GetPause()) {
         if (GetAssistant().m_bIgnorePause)
            m_Mosaic.SetPause(false);
         else return;
      }
      bOpenCellAssistant = true;
      SetEvent(hEventJob);
   }
}
/**
inline void CFastMines2Project::ResetAssistant() {
   ResetEvent(hEventJob);

   KillTimer(m_hWnd, ID_EVENT_TIMER_JOB     );
   KillTimer(m_hWnd, ID_EVENT_TIMER_INACTION);
   if (!GetAssistant().m_bUse) return;
   SetTimer(m_hWnd, ID_EVENT_TIMER_INACTION, GetAssistant().m_iTimeoutUnactive, NULL);
}

#define IMAGE_JOB_SET   SendMessage(m_BtnPause.GetHandle(), BM_SETIMAGE, IMAGE_ICON, (LPARAM)GetImageBtnPause(3))
#define IMAGE_JOB_RESET SendMessage(m_BtnPause.GetHandle(), BM_SETIMAGE, IMAGE_ICON, \
                           m_Mosaic.GetGameStatusIsPlay() ?                        \
                              m_Mosaic.GetPause() ?                       \
                              (LPARAM)GetImageBtnPause(1) :      \
                              (LPARAM)GetImageBtnPause(0) :      \
                           (LPARAM)GetImageBtnPause(2))

inline void CFastMines2Project::Assistant_Job() {
   static COORD lastClick = INCORRECT_COORD;
   if (lastClick != INCORRECT_COORD) IMAGE_JOB_SET;
   nsMosaic::CClickData click;
   if (m_Assistant.IsSequentialMove()) { // ����������� �� ������� �������?
      m_Assistant.SequentialMove(click); // ��, ����������� - ���������� ������� �������
   } else {
      if (!m_Assistant.FindCell(click)) { // ����� ������ ��� �����
         IMAGE_JOB_RESET;
         return; // ������ ���� ��������
      }
      if (click.m_CoordCell == INCORRECT_COORD) {
         if (!m_Assistant.SequentialMoveCanBegin()) { // �������� ������� ��� ������ �������� �������
            IMAGE_JOB_RESET;
            return; // ������ ���� ��������
         }
         if (m_Assistant.IsSequentialMove()) {       // ��������� �� ������� �������?
            m_Assistant.SequentialMove(click);       // ��, ��������� - ������ ������� �������
         } else {
            if (m_Mosaic.GetGameStatusIsPlay() &&
                GetAssistant().m_bStopJob &&
                bOpenCellAssistant
               )
            {
               IMAGE_JOB_RESET;
               return; // ������������� ����� ��� ������������ ���������� ����
            }
            if (click.m_fProbability >= 0.5) { // ����������� ������ 50%
               click.m_CoordCell = click.m_PrbltCell;
            #ifdef USE_INFO_DIALOG
               //nsInfo::AddValue(TEXT("coord probability = "), click.m_CoordCell, 10);
               //nsInfo::AddValue(TEXT("      probability = "), click.m_fProbability);
            #endif // USE_INFO_DIALOG
            } else {
               do {
                  click.m_CoordCell.X = rand(GetSizeMosaic().cx-1);
                  click.m_CoordCell.Y = rand(GetSizeMosaic().cy-1);
               } while ((m_Mosaic.GetCell(click.m_CoordCell)->Cell_GetStatus() == nsCell::_Open) ||
                        (m_Mosaic.GetCell(click.m_CoordCell)->Cell_GetClose()  == nsCell::_Flag));
            }
         }
      }
   }
   lastClick = click.m_CoordCell;
   IMAGE_JOB_RESET;
   const POINT coordCenter = m_Mosaic.GetCell(click.m_CoordCell)->GetCenterPixel();

   { // set cursor to new position
      POINT coordCursor = ::ClientToScreen(m_Mosaic.GetHandle(), coordCenter);

      SetEvent(hEventSetCursorBegin);

      SetCursorPos(coordCursor.x, coordCursor.y);

      WaitForSingleObject(hEventSetCursorEnd, INFINITE);
      SetEvent(hEventSetCursorBegin); // ???

   }
   if (bOpenCellAssistant) {
      if (GetAssistant().m_bBeep) MessageBeep(0);
      if (click.m_bIsLeft) {
         FORWARD_WM_LBUTTONDOWN(m_Mosaic.GetHandle(), FALSE, coordCenter.x, coordCenter.y, MK_ASSISTANT, SendMessage);
         FORWARD_WM_LBUTTONUP  (m_Mosaic.GetHandle(),        coordCenter.x, coordCenter.y, MK_ASSISTANT, SendMessage);
      } else {
         FORWARD_WM_RBUTTONDOWN(m_Mosaic.GetHandle(), FALSE, coordCenter.x, coordCenter.y, MAKEWPARAM(MK_ASSISTANT, click.m_Close), SendMessage);
      }
   }
}

DWORD WINAPI ChildThread(PVOID pvParam) {
   const HANDLE pHandles[2]= {hEventJob, (HANDLE)pvParam};

   for (;;) {
      switch (WaitForMultipleObjects(2, pHandles, FALSE, INFINITE)) {
      case WAIT_OBJECT_0:
         //ResetEvent(hEventJobEnd);
         try {
            gpFM2Proj->Assistant_Job();
         }catch(...){}
         //SetEvent(hEventJobEnd);
         ResetEvent(hEventJob);
         break;
      case WAIT_OBJECT_0+1:
         return 0;
      case WAIT_TIMEOUT:
      case WAIT_FAILED:
         break;
      }
   }
   return 0;
}
/**/

}; // namespace nsMosaic
