////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "StorageMines.cpp"
// Хранилище координат мин, созданных или загруженных из файла
////////////////////////////////////////////////////////////////////////////////

#include "StdAfx.h"
#include "StorageMines.h"
#include "CommonLib.h"

nsMosaic::CStorageMines::CStorageMines() {}

nsMosaic::CStorageMines::~CStorageMines() {
   Reset();
}

      COORD* nsMosaic::CStorageMines::operator[](size_t index)       {return m_CellMines[index];}
const COORD* nsMosaic::CStorageMines::operator[](size_t index) const {return m_CellMines[index];}

void nsMosaic::CStorageMines::operator=(const CStorageMines &Copy) {
   size_t size = Copy.GetSize();
   SetSize(size);
   for (int i=0; i<size; i++)
      *m_CellMines[i] = *Copy[i];
}

void nsMosaic::CStorageMines::Reset() {
   size_t size = GetSize();
   for (int i=0; i<size; i++)
      delete m_CellMines[i];
   m_CellMines.resize(0);
}

/**/
bool nsMosaic::CStorageMines::Find(const COORD &Coord) const {
   size_t size = GetSize();
   for (int i=0; i<size; i++)
      if (*m_CellMines[i] == Coord) return true;
   return false;
}
/**/

nsMosaic::CStorageMines::VECTOR_COORD_ITERATOR nsMosaic::CStorageMines::Find(const COORD &Coord) {
   size_t size = GetSize();
   for (VECTOR_COORD_ITERATOR i=m_CellMines.begin(); i!=m_CellMines.end(); i++) {
      if (**i == Coord)
         return i;
   }
   return NULL;
}

void nsMosaic::CStorageMines::Del(const COORD &Coord) {
   VECTOR_COORD::iterator i = Find(Coord);
   if (i != NULL) {
      delete *i;
      m_CellMines.erase(i);
   }
}

void nsMosaic::CStorageMines::Add(const COORD &Coord) {
   if (!Find(Coord))
      m_CellMines.push_back(new COORDEX(Coord));
#ifdef _DEBUG
   else
      MessageBox(NULL, TEXT("CStorageMines::Add()"), TEXT("Error"), MB_OK | MB_ICONERROR);
#endif
}

size_t nsMosaic::CStorageMines::GetSize() const {return m_CellMines.size();}

void nsMosaic::CStorageMines::SetSize(size_t size) {
   Reset();
   m_CellMines.resize(size);
   for (int i=0; i<size; i++) {
      m_CellMines[i] = new COORD;
   }
}
