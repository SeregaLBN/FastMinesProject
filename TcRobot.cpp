////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "TcRobot.cpp"
//
// ���������� ������ TcRobot
////////////////////////////////////////////////////////////////////////////////

#include ".\TcRobot.h"
#include <math.h>
#include <map>
#include ".\Lib.h"
#include ".\Dialog\Info.h"
#include ".\TcMosaic.h"

////////////////////////////////////////////////////////////////////////////////
//                             global variables
////////////////////////////////////////////////////////////////////////////////
extern TcMosaic* gpMosaic;
extern HINSTANCE ghInstance;
extern HANDLE hEventJob;

////////////////////////////////////////////////////////////////////////////////
//                            types & constants
////////////////////////////////////////////////////////////////////////////////
typedef std::multimap<const TB*, const TB*> MM_cpTB;
#define JOB_ABORT   false
#define JOB_SUCCESS true

////////////////////////////////////////////////////////////////////////////////
//                                   other
////////////////////////////////////////////////////////////////////////////////
inline bool Insert(MM_cpTB&, const TB*, const TB*);
inline void Delete(SET_cpTB&, const TB*);

////////////////////////////////////////////////////////////////////////////////
//                              implementation
////////////////////////////////////////////////////////////////////////////////

JOB_RESULT TcRobot::FindCell(TsClickData& click) {
   if (!setForOpen.empty()) {
      click.coordCell = (*setForOpen.begin())->GetCoord();
      click.isLeft = true;
   } else {
      if (!setForFlag.empty()) {
         click.coordCell = (*setForFlag.begin())->GetCoord();
         click.isLeft = true;
      } else {
         if (!setForAnalyse.empty()) {
            //SetCursor(LoadCursor(ghInstance, IDC_WAIT));
            if (!Analyse(click)) // ������ ���� ��������
               return JOB_ABORT;
            //SetCursor(LoadCursor(ghInstance, IDC_ARROW));
/**
         #ifdef USE_INFO_DIALOG
            nsInfo::AddValue(TEXT("coord = "), click.coordCell, 10);
            if (click.coordCell != CIncorrectCoord) {
               if (click.isLeft)
                  nsInfo::AddString(TEXT("coord for left  cli�k"));
               else
                  nsInfo::AddString(TEXT("coord for right cli�k"));
            }
         #endif // USE_INFO_DIALOG
/**/
         }
      }
   }
   return JOB_SUCCESS;
}

#ifdef USE_INFO_DIALOG
   const TB* cellAnalyseSave;
   const TB* cellInfluenceSave;
#endif // USE_INFO_DIALOG

JOB_RESULT TcRobot::Analyse(TsClickData& click) {
   const int numberNeighbor = gpMosaic->GetField(0,0)->GetNeighborNumber();
/*
#ifdef USE_INFO_DIALOG
   nsInfo::AddString(TEXT("------------------"));
#endif // USE_INFO_DIALOG
*/
   MM_cpTB mmPair; // ������������������ ����

   for (SET_cpTB::const_iterator cellAnalyse=setForAnalyse.begin(); cellAnalyse!=setForAnalyse.end(); cellAnalyse++) { // ������� ��������� ������ �� setForAnalyse
      // ������ cellAnalyse �������

      SET_cpTB setInfluence, setCnF; // ��������� �������� ��������� setInfluence � setCnF (Close & noFlag)

      // ��� cellAnalyse (������ �� ���� �������) �������� ���
      // �������� �������� ������ ��� ������ � ��������� setCnF
      for (int k=0; k<numberNeighbor; k++) {
         const POINT coordNeighbor = (*cellAnalyse)->GetNeighborCoord(k);
         if (coordNeighbor == CIncorrectCoord) continue;
         const TB* cell = gpMosaic->GetField(coordNeighbor.x,coordNeighbor.y);
         if ((cell->Cell_GetStatus() == _Close) &&
            (cell->Cell_GetClose () != _Flag))
            setCnF.insert(cell);
      }

      // �������� �� ����� ����. setCnF.
      // ���� �������� ������� (�� ���������� cellAnalyse) �������� � ��������� setInfluence.
      for (SET_cpTB::const_iterator p=setCnF.begin(); p!=setCnF.end(); p++) {
         for (int k=0; k<numberNeighbor; k++) {
            const POINT coordNeighbor = (*p)->GetNeighborCoord(k);
            if (coordNeighbor == CIncorrectCoord) continue;
            const TB* cell = gpMosaic->GetField(coordNeighbor.x,coordNeighbor.y);
            if ((cell->Cell_GetStatus() == _Open) &&
                (cell != (*cellAnalyse)))
               setInfluence.insert(cell);
         }
      }

      if (!setInfluence.empty()) {
         for (SET_cpTB::const_iterator cellInfluence=setInfluence.begin(); cellInfluence!=setInfluence.end(); cellInfluence++) { // ������� ��������� ������ �� setInfluence
            // ������ cellInfluence �������

            // ��������, �� ��������������� �� ��� ������ ����. ���� ��������������� - �� ����������
            if (!Insert(mmPair, *cellAnalyse, *cellInfluence))
               continue;

            // ������ �� ��������� cellAnalyse � cellInfluence
            if (!Analyse2Cell(*cellAnalyse, *cellInfluence, click)) // ��������, ��� ����� ������ ��������... :(
               return JOB_ABORT; // ������ ���� ��������
            if (click.coordCell == CIncorrectCoord) continue;
            return JOB_SUCCESS;
         }
      }
   }
#ifdef USE_INFO_DIALOG
   //if (click.coordCell == CIncorrectCoord) {
   //   nsInfo::AddValue(TEXT("coord cellAnalyse   = "), cellAnalyseSave  ->GetCoord(), 10);
   //   nsInfo::AddValue(TEXT("coord cellInfluence = "), cellInfluenceSave->GetCoord(), 10);
   //}
#endif // USE_INFO_DIALOG
   // ��� ������ ��� ����������, �� ��������������
   return JOB_SUCCESS;
}

JOB_RESULT TcRobot::Analyse2Cell(const TB* cellAnalyse, const TB* cellInfluence, TsClickData& click) {
   setCnF2.clear();
   const int numberNeighbor = gpMosaic->GetField(0,0)->GetNeighborNumber();
   // ��� cellAnalyse (������ �� ���� �������) �������� ���
   // �������� �������� ������ ��� ������ � ��������� setCnF2
   for (int k=0; k<numberNeighbor; k++) {
      const POINT coordNeighbor = cellAnalyse->GetNeighborCoord(k);
      if (coordNeighbor == CIncorrectCoord) continue;
      const TB* cell = gpMosaic->GetField(coordNeighbor.x,coordNeighbor.y);
      if ((cell->Cell_GetStatus() == _Close) &&
          (cell->Cell_GetClose () != _Flag))
         setCnF2.insert(cell);
   }
   // ��� cellInfluence (������ �� ���� �������) �������� ���
   // �������� �������� ������ ��� ������ � ��������� setCnF2
   for (k=0; k<numberNeighbor; k++) {
      const POINT coordNeighbor = cellInfluence->GetNeighborCoord(k);
      if (coordNeighbor == CIncorrectCoord) continue;
      const TB* cell = gpMosaic->GetField(coordNeighbor.x,coordNeighbor.y);
      if ((cell->Cell_GetStatus() == _Close) &&
          (cell->Cell_GetClose () != _Flag))
         setCnF2.insert(cell);
   }

   int n = pow(2, setCnF2.size());
   setTable.clear();
   for (k=0; k<n; k++) {
#ifdef ROBOT_MULTITHREAD
      if (WAIT_TIMEOUT == WaitForSingleObject(hEventJob, 0)) {
         //MessageBeep(0);
         return JOB_ABORT; // ������ ���� ��������
      }
#endif // ROBOT_MULTITHREAD
      if (TestVariant(k, numberNeighbor, cellAnalyse, cellInfluence))
         setTable.insert(k);
   }

   if (!setTable.empty()) {
      {
         int i = 0;
         const float size = setTable.size();
         for (SET_cpTB::const_iterator q=setCnF2.begin(); q!=setCnF2.end(); i++, q++) {
            int mask = pow(2,i);
            int count = 0; // ������� �����
            for (std::set<int>::const_iterator p=setTable.begin(); p!=setTable.end(); p++) {
               if (!(mask&(*p)))
                  count++;
            }
            const float probability = (float)count/size;
            if(click.probability < probability) {
               click.probability = probability;
               click.prbltCell = (*q)->GetCoord();
            #ifdef USE_INFO_DIALOG
               cellAnalyseSave   = cellAnalyse;
               cellInfluenceSave = cellInfluence;
            #endif // USE_INFO_DIALOG
            }
         }
         if(click.probability + 0.001 >= 1.0) {
            click.coordCell = click.prbltCell;
            click.isLeft = true;
            return JOB_SUCCESS;
         }
         /**
         int resultTable = 0;
         for (std::set<int>::const_iterator p=setTable.begin(); p!=setTable.end(); p++) {
            resultTable |= *p;
         }
         for (SET_cpTB::const_iterator q=setCnF2.begin(); q!=setCnF2.end(); q++) {
            if (!(resultTable&1)) {
               click.coordCell = (*q)->GetCoord();
               click.isLeft = true;
               return JOB_SUCCESS;
            }
            resultTable = resultTable>>1;
         }
         /**/
      }
      {
         int resultTable = n-1;
         for (std::set<int>::const_iterator p=setTable.begin(); p!=setTable.end(); p++) {
            resultTable &= *p;
         }
         for (SET_cpTB::const_iterator q=setCnF2.begin(); q!=setCnF2.end(); q++) {
            if (resultTable&1) {
               click.coordCell = (*q)->GetCoord();
               click.isLeft = false;
               return JOB_SUCCESS;
            }
            resultTable = resultTable>>1;
         }
      }
   }
   return JOB_SUCCESS;
}


inline bool TcRobot::TestVariant(const int variant, const int numberNeighbor, const TB* cellAnalyse, const TB* cellInfluence) const {
   int i=1;
   for (SET_cpTB::const_iterator p=setCnF2.begin(); p!=setCnF2.end(); p++, i=i<<1) {
      (*p)->SetPresumeFlag(!!(i & variant)); // �������������� ������
   }

   int numberFlag = 0;
   for (int k=0; k<numberNeighbor; k++) {
      POINT coordNeighbor = cellAnalyse->GetNeighborCoord(k);
      if (coordNeighbor == CIncorrectCoord) continue;
      if (gpMosaic->GetField(coordNeighbor.x,coordNeighbor.y)->Cell_GetStatus() == _Close)
         if (gpMosaic->GetField(coordNeighbor.x,coordNeighbor.y)->Cell_GetClose () == _Flag) numberFlag++;
         else if (gpMosaic->GetField(coordNeighbor.x,coordNeighbor.y)->GetPresumeFlag())     numberFlag++;
   }
   if (cellAnalyse->Cell_GetOpen() != (TeOpen)numberFlag) return false;

   numberFlag = 0;
   for (k=0; k<numberNeighbor; k++) {
      POINT coordNeighbor = cellInfluence->GetNeighborCoord(k);
      if (coordNeighbor == CIncorrectCoord) continue;
      if (gpMosaic->GetField(coordNeighbor.x,coordNeighbor.y)->Cell_GetStatus() == _Close)
         if (gpMosaic->GetField(coordNeighbor.x,coordNeighbor.y)->Cell_GetClose () == _Flag) numberFlag++;
         else if (gpMosaic->GetField(coordNeighbor.x,coordNeighbor.y)->GetPresumeFlag())     numberFlag++;
   }
   if (cellInfluence->Cell_GetOpen() != (TeOpen)numberFlag) return false;

   return true;
}

void TcRobot::GameNew() {
   setForOpen    .clear();
   setForFlag    .clear();
   setForAnalyse .clear();
   setCloseNoFlag.clear();
   for (int i = 0; i < gpMosaic->GetSizeField().x; i++)
      for (int j = 0; j < gpMosaic->GetSizeField().y; j++)
         setCloseNoFlag.insert(gpMosaic->GetField(i,j));
   sequentialMove = false;
}

void TcRobot::ClickBegin() {
   TB::setOpenNil.clear();
   TB::setOpen   .clear();
   TB::setFlag   .clear();
}

void TcRobot::ClickEnd() {
   const int numberNeighbor = gpMosaic->GetField(0,0)->GetNeighborNumber();

   // ������ ��������� setOpenNil
   if (!TB::setOpenNil.empty())
      for (SET_cpTB::const_iterator p=TB::setOpenNil.begin(); p!=TB::setOpenNil.end(); p++)
         Delete(setCloseNoFlag, *p);

   // ������ ��������� setOpen
   if (!TB::setOpen.empty()) {
      for (SET_cpTB::const_iterator p=TB::setOpen.begin(); p!=TB::setOpen.end(); p++) {
         for (int k=0; k<numberNeighbor; k++) {
            POINT coordNeighbor = (*p)->GetNeighborCoord(k);
            if (coordNeighbor == CIncorrectCoord) continue;
            const TB* cell = gpMosaic->GetField(coordNeighbor.x,coordNeighbor.y);
            if ((cell->Cell_GetStatus() == _Close) ||
                (cell->Cell_GetOpen  () == _Nil  )) continue;
            setForOpen   .insert(cell);
            setForFlag   .insert(cell);
            setForAnalyse.insert(cell);
         }
         setForOpen   .insert(*p);
         setForFlag   .insert(*p);
         setForAnalyse.insert(*p);
         Delete(setCloseNoFlag, *p);
      }
   }

   // ������ ��������� setFlag
   if (!TB::setFlag.empty()) {
      for (SET_cpTB::const_iterator p=TB::setFlag.begin(); p!=TB::setFlag.end(); p++) {
         for (int k=0; k<numberNeighbor; k++) {
            POINT coordNeighbor = (*p)->GetNeighborCoord(k);
            if (coordNeighbor == CIncorrectCoord) continue;
            const TB* cell = gpMosaic->GetField(coordNeighbor.x,coordNeighbor.y);
            if ((cell->Cell_GetStatus() == _Close) ||
                (cell->Cell_GetOpen  () == _Nil  )) continue;
            setForOpen   .insert(cell);
            setForFlag   .insert(cell);
            setForAnalyse.insert(cell);
         }
         if ((*p)->Cell_GetClose() == _Flag)
            Delete(setCloseNoFlag, *p);
         else
            setCloseNoFlag.insert(*p);
      }
   }

   // ������ ��������� setForFlag
   while (!setForFlag.empty()) {
      bool isBreak = true;
      for (SET_cpTB::const_iterator p=setForFlag.begin(); p!=setForFlag.end(); p++) {
         int closeF = 0; // ���-�� �������� ������� � ��������
         int closeN = 0; // ���-�� �������� ������� ��� �������
         for (int k=0; k<numberNeighbor; k++) {
            POINT coordNeighbor = (*p)->GetNeighborCoord(k);
            if (coordNeighbor == CIncorrectCoord) continue;
            const TB* cell = gpMosaic->GetField(coordNeighbor.x,coordNeighbor.y);
            if (cell->Cell_GetStatus() == _Close)
               if (cell->Cell_GetClose() == _Flag)
                    closeF++;
               else closeN++;
         }
         
         if ((!closeN) ||                                       // 2. ������� ������, � ������� ��� �������� ������ ����� ������;
             ((*p)->Cell_GetOpen() != (TeOpen)(closeF+closeN))) // 1. ������� ������, � ��� ������� ��� �� ����� ���-�� �������� �������.
         {
         #ifdef USE_INFO_DIALOG
            //nsInfo::AddValue(TEXT("setForFlag del cell: x = "), (*p)->GetCoord().x, 10);
            //nsInfo::AddValue(TEXT("setForFlag del cell: y = "), (*p)->GetCoord().y, 10);
            //nsInfo::AddString(TEXT("------------------"));
         #endif // USE_INFO_DIALOG
            setForFlag.erase(p);
            isBreak = false;
            break; // loop for
         }
      }
      if (isBreak) break; // loop while
   }

   // ������ ��������� setForOpen
   while (!setForOpen.empty()) {
      bool isBreak = true;
      for (SET_cpTB::const_iterator p=setForOpen.begin(); p!=setForOpen.end(); p++) {
         int closeF = 0; // ���-�� �������� ������� � ��������
         int closeN = 0; // ���-�� �������� ������� ��� �������
         for (int k=0; k<numberNeighbor; k++) {
            POINT coordNeighbor = (*p)->GetNeighborCoord(k);
            if (coordNeighbor == CIncorrectCoord) continue;
            const TB* cell = gpMosaic->GetField(coordNeighbor.x,coordNeighbor.y);
            if (cell->Cell_GetStatus() == _Close)
               if (cell->Cell_GetClose() == _Flag)
                    closeF++;
               else closeN++;
         }
         if ((!closeN) ||                              // 1. ������� ������, � ������� ��� �������� ������ ����� ������
             ((*p)->Cell_GetOpen() != (TeOpen)closeF)) // 2. ������� ������, � ������� ��� �� ����� ���-�� �������� ������� � ��������
         {
         #ifdef USE_INFO_DIALOG
            //nsInfo::AddValue(TEXT("setForOpen del cell: x = "), (*p)->GetCoord().x, 10);
            //nsInfo::AddValue(TEXT("setForOpen del cell: y = "), (*p)->GetCoord().y, 10);
            //nsInfo::AddString(TEXT("------------------"));
         #endif // USE_INFO_DIALOG
            setForOpen.erase(p);
            isBreak = false;
            break; // loop for
         }
      }
      if (isBreak) break; // loop while
   }

   // ������ ��������� setForAnalyse
   while (!setForAnalyse.empty()) {
      bool isBreak = true;
      for (SET_cpTB::const_iterator p=setForAnalyse.begin(); p!=setForAnalyse.end(); p++) {
         int closeF = 0; // ���-�� �������� ������� � ��������
         int closeN = 0; // ���-�� �������� ������� ��� �������
         for (int k=0; k<numberNeighbor; k++) {
            POINT coordNeighbor = (*p)->GetNeighborCoord(k);
            if (coordNeighbor == CIncorrectCoord) continue;
            const TB* cell = gpMosaic->GetField(coordNeighbor.x,coordNeighbor.y);
            if (cell->Cell_GetStatus() == _Close)
               if (cell->Cell_GetClose() == _Flag)
                    closeF++;
               else closeN++;
         }
         if ((!closeN) ||                                       // 1. ������� ������, � ������� ��� �������� ������ ����� ������
             ((*p)->Cell_GetOpen() == (TeOpen) closeF) ||       // 2. ������� ������, � ������� ��� ����� ���-�� �������� ������� � ��������
             ((*p)->Cell_GetOpen() == (TeOpen)(closeF+closeN))) // 3. ������� ������, � ��� ������� ��� ����� ���-�� �������� �������.
         {
         #ifdef USE_INFO_DIALOG
            //nsInfo::AddValue(TEXT("setForAnalyse del cell: x = "), (*p)->GetCoord().x, 10);
            //nsInfo::AddValue(TEXT("setForAnalyse del cell: y = "), (*p)->GetCoord().y, 10);
            //nsInfo::AddString(TEXT("------------------"));
         #endif // USE_INFO_DIALOG
            setForAnalyse.erase(p);
            isBreak = false;
            break; // loop for
         }
      }
      if (isBreak) break; // loop while
   }

   // �� ������ �����:
   // 1. ��������� setForFlag - ��� ��������� �������� ����� �������� �� ������� ������� � ��������� ������ ��� �������.
   // 2. ��������� setForOpen - ��� ��������� �������� ����� �������� �� ������� ������� � �������� ������ ��� �����.
   // 3. ��������� setForAnalyse - ��� ��������� �������� ����� �������� �� ������� �� � ���� �� ������� (������ ����� ���������� �������). 
   // �� � ����� �� ���� ������� ��� ���������� ������   // �� ������ �����:

#ifdef USE_INFO_DIALOG
   //nsInfo::AddValue(TEXT("setForOpen    size = "), setForOpen   .size(), 10);
   //nsInfo::AddValue(TEXT("setForFlag    size = "), setForFlag   .size(), 10);
   //nsInfo::AddValue(TEXT("setForAnalyse size = "), setForAnalyse.size(), 10);
   //nsInfo::AddString(TEXT("------------------"));
   //nsInfo::AddString(TEXT("------------------"));
#endif // USE_INFO_DIALOG
}

void TcRobot::DeleteTableSM() {
   for (V_pbool::const_iterator I=vTableSM.begin(); I!=vTableSM.end(); I++) {
      delete [] *I;
   }
   vTableSM.clear();
}

JOB_RESULT TcRobot::AddNextVectors(const int cDepth, bool* vec) {
#ifdef ROBOT_MULTITHREAD
   if (WAIT_TIMEOUT == WaitForSingleObject(hEventJob, 0)) {
      return JOB_ABORT; // ������ ���� ��������
   }
#endif // ROBOT_MULTITHREAD
   const int size = setCloseNoFlag.size();
   int depth = 0;
   for (int i=size-1; i>=0; i--) {
      if (vec[i]) depth++;
      if (depth == cDepth) break;
   }
   for (int j=i+1; j<size; j++) {
      if (vec[j]) break;
      vec[j] = true;
      vec[i] = false;
      if (!AnalyseVector(vec)) // �������� "������������" �������, � ���� �k, �� ��������� �������
         return JOB_ABORT;
      if (cDepth < mineNoFLag) 
         if (!AddNextVectors(cDepth+1, vec))
            return JOB_ABORT;
      vec[i] = true;
      vec[j] = false;
   }
   return JOB_SUCCESS;
}

JOB_RESULT TcRobot::AnalyseVector(const bool* vector) {
   const int numberNeighbor = gpMosaic->GetField(0,0)->GetNeighborNumber();
   bool result = true;
   for (SET_cpTB::const_iterator p=setForAnalyse.begin(); p!=setForAnalyse.end(); p++) {
   #ifdef ROBOT_MULTITHREAD
      if (WAIT_TIMEOUT == WaitForSingleObject(hEventJob, 0)) {
         return JOB_ABORT; // ������ ���� ��������
      }
   #endif // ROBOT_MULTITHREAD
      int closeF = 0; // ���-�� �������� ������� � ��������
      int closeN = 0; // ���-�� �������� ������� � ��������������� ��������
      for (int k=0; k<numberNeighbor; k++) {
         POINT coordNeighbor = (*p)->GetNeighborCoord(k);
         if (coordNeighbor == CIncorrectCoord) continue;
         const TB* cell = gpMosaic->GetField(coordNeighbor.x,coordNeighbor.y);
         if (cell->Cell_GetStatus() == _Close) {
            if (cell->Cell_GetClose() == _Flag)
               closeF++;
            else {
               int j=0;
               for (SET_cpTB::const_iterator I=setCloseNoFlag.begin(); I!=setCloseNoFlag.end(); I++, j++) {
                  if (cell == *I){
                     if (vector[j]) closeN++;
                     break;
                  }
               }
            }
         }
      }
      if ((*p)->Cell_GetOpen() != (TeOpen)(closeF+closeN)){
         result = false;
         break;
      }
   }
   const int size = setCloseNoFlag.size();
   if (result) {
      bool* addVec = new bool[size];
      memcpy(addVec, vector, sizeof(bool)*size);
      vTableSM.push_back(addVec);
   } else {
/**
   #ifdef USE_INFO_DIALOG
      nsInfo::AddValue(TEXT("ignore "), vector, size);
   #endif // USE_INFO_DIALOG
/**/
   }
   return JOB_SUCCESS;
}

JOB_RESULT TcRobot::AllOkToSequentialMove() { // �������� ������� ��� ������ �������� �������
                                              // ���� ������� �������� - ������������� � ������ ��������
   if (sequentialMove)
      return JOB_SUCCESS; // ������� ������� � ��� ��� �����������

   const int size = setCloseNoFlag.size();

   // �������� ������� ��� ������ �������� �������
   if (!gpMosaic->GetGameRun()) return JOB_SUCCESS; // ������� �� ��������� (���� sequentialMove ������������)
   const float percent = 0.01*gpMosaic->GetSizeField().x*gpMosaic->GetSizeField().y;
#ifdef USE_INFO_DIALOG
   //nsInfo::AddValue(TEXT("setCloseNoFlag.size() = "), size);
   //nsInfo::AddValue(TEXT("closeNumber = "), closeNumber);
#endif // USE_INFO_DIALOG
   /**
   if ((size > percent) || (size > 16)) {
      return JOB_SUCCESS; // ������� �� ��������� (���� sequentialMove ������������)
   }/**/
   mineNoFLag = 0; // ���-�� ������������ �������� ���
   for (SET_cpTB::const_iterator p=setCloseNoFlag.begin(); p!=setCloseNoFlag.end(); p++)
      if ((*p)->Cell_GetOpen() == _Mine)
         mineNoFLag++;
   if (mineNoFLag > percent) return JOB_SUCCESS; // ������� �� ��������� (���� sequentialMove ������������)

   // ����� ������ ������� ������
   // ��� ����� ���� ������ ��� �������� ������������ ������� 
   DeleteTableSM(); // ������ �� ������...
#ifdef USE_INFO_DIALOG
   nsInfo::AddValue(TEXT("size = "), size, 10);
#endif // USE_INFO_DIALOG
   bool *vec = new bool[size];
   for (int i=0; i<size; i++)
      vec[i] = (i<mineNoFLag);
   if (!AnalyseVector(vec)) { // �������� "������������" �������, � ���� �k, �� ��������� �������
      delete [] vec;
      return JOB_ABORT;
   }
   if (!AddNextVectors(1, vec)) { // ����� ��� �������� ������������ ������� 
      delete [] vec;
      return JOB_ABORT; // ������ ���� ��������
   }
   delete [] vec;
/**
#ifdef USE_INFO_DIALOG
   nsInfo::AddString(TEXT("-------"));
   for (V_pbool::const_iterator I=vTableSM.begin(); I!=vTableSM.end(); I++) {
      nsInfo::AddValue(TEXT("bit Ok "), *I, size);
   }
   nsInfo::AddString(TEXT("-------"));
#endif // USE_INFO_DIALOG
/**/
   sequentialMove = true; // ����� � ������ �������� �������
   row = col = 0;
   return JOB_SUCCESS;//JOB_ABORT;//
}

void TcRobot::SequentialMove(TsClickData& click) {
   const int size = setCloseNoFlag.size();
   TeClose oldClose;

   click.isLeft = false;
   do {
      click.close  = vTableSM[row][col] ? _Unknown : _Clear;
      {
         int k=0;
         for (SET_cpTB::const_iterator p=setCloseNoFlag.begin(); p!=setCloseNoFlag.end(); p++, k++)
            if (k==col) {
               click.coordCell = (*p)->GetCoord();
               oldClose = (*p)->Cell_GetClose();
               break;
            }
      }
      if (++col >= setCloseNoFlag.size()) {
         col = 0;
         if (++row >= vTableSM.size()) // ��������� ������ ��������...
            sequentialMove = false;
      }
   } while((click.close == oldClose) // ��� ������ ������������� �� ��� ��� �����������
           && sequentialMove);
}

/*
void TcRobot::Print() {
#ifdef USE_INFO_DIALOG
   for (SET_cpTB::const_iterator p=setCloseNoFlag.begin(); p!=setCloseNoFlag.end(); p++)
      nsInfo::AddValue(TEXT("coord = "), (*p)->GetCoord(), 10);
   nsInfo::AddValue(TEXT("size = "), setCloseNoFlag.size(), 10);
   nsInfo::AddString(TEXT("------------"));
#endif // USE_INFO_DIALOG
}
*/

////////////////////////////////////////////////////////////////////////////////
//                            other implementation
////////////////////////////////////////////////////////////////////////////////
inline bool Insert(MM_cpTB& mm, const TB* cellAnalyse, const TB* cellInfluence) {
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
#ifdef USE_INFO_DIALOG
   nsInfo::AddValue(TEXT("mm.insert: "), cellAnalyse->GetCoord(), cellInfluence->GetCoord());
 //nsInfo::AddValue(TEXT("mm.size =: "), mm.size(), 10);
#endif // USE_INFO_DIALOG
*/
   return true;
}

inline void Delete(SET_cpTB& setCloseNoFlag, const TB* cell4Del) {
   if (setCloseNoFlag.empty()) return;
   SET_cpTB::const_iterator I = setCloseNoFlag.find(cell4Del);
   if (I == setCloseNoFlag.end()) return;
   setCloseNoFlag.erase(I);
}
