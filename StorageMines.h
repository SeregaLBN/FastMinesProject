////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                                   (C) Sergey Krivulya (KSerg)
// file name: "StorageMines.h"
// Хранилище координат мин, созданных или загруженных из файла
////////////////////////////////////////////////////////////////////////////////

#ifndef __FILE__STORAGEMINES__
#define __FILE__STORAGEMINES__

#pragma warning(disable:4786) // identifier was truncated to '255' characters in the debug information
#include <vector>
#ifndef __AFX_H__
   #include <Windows.h>
#endif

namespace nsMosaic {

   class CStorageMines {
      typedef std::vector<COORD*> VECTOR_COORD;
      typedef VECTOR_COORD::iterator VECTOR_COORD_ITERATOR;
   private:
      VECTOR_COORD m_CellMines;
   public:
      CStorageMines();
     ~CStorageMines();
            COORD* operator[](size_t index);
      const COORD* operator[](size_t index) const;
      void operator=(const CStorageMines &Copy);
      void  Reset();
      bool  Find(const COORD &Coord) const;
      VECTOR_COORD_ITERATOR Find(const COORD &Coord);
      void Del(const COORD &Coord);
      void Add(const COORD &Coord);
      size_t GetSize() const;
      void   SetSize(size_t size);
   };

} // namespace nsMosaic

#endif // __FILE__STORAGEMINES__
