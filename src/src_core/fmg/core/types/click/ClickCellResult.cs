using System.Collections.Generic;
using System.Linq;
using fmg.core.mosaic.cells;

namespace fmg.core.types.click {

   public class ClickCellResult {

      public ISet<BaseCell> Modified = new HashSet<BaseCell>();

      /// <summary> были ли изменения на поле? Была ли открыта хоть одна ячейка или выставлен/снят флажёк / знак вопроса </summary>
      public bool IsAnyChanges => Modified.Any();
      public bool IsAnyOpenMine => Modified
         .Where(x => x.State.Status == EState._Open)
         .Any(x => x.State.Open == EOpen._Mine);

      ///// <summary> множество ячеек (нулевых) открытых при последнем клике </summary>
      //public List<BaseCell> GetOpenNils() {
      //   return Modified
      //      .Where(x => x.State.Status == EState._Open)
      //      .Where(x => x.State.Open == EOpen._Nil)
      //      .ToList();
      //}

      ///// <summary> множество ячеек (ненулевых) открытых при последнем клике </summary>
      //public List<BaseCell> GetOpens() {
      //   return Modified
      //      .Where(x => x.State.Status == EState._Open)
      //      .Where(x => x.State.Open != EOpen._Nil)
      //      .ToList();
      //}

      ///// <summary> множество ячеек с флажками снятых/уставленных при последнем клике </summary>
      //public List<BaseCell> GetFlags() {
      //   return Modified
      //      .Where(x => x.State.Status == EState._Close)
      //      .ToList();
      //}

      public int CountFlag => Modified
         .Where(x => x.State.Status == EState._Close)
         .Count(x => x.State.Close == EClose._Flag);

      public int CountOpen => Modified
         .Count(x => x.State.Status == EState._Open);

      public int CountUnknown => Modified
         .Where(x => x.State.Status == EState._Close)
         .Count(x => x.State.Close == EClose._Unknown);

   }

}
