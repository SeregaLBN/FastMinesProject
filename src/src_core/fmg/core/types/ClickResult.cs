using System.Collections.Generic;
using System.Linq;
using fmg.core.mosaic.cells;

namespace fmg.core.types.click {

   public class ClickResult {
      public List<BaseCell> Modified = new List<BaseCell>();

      public bool IsAnyOpenMine() {
         return Modified
            .Where(x => x.State.Status == EState._Open)
            .Any(x => x.State.Open == EOpen._Mine);
      }

      ///// <summary> ��������� ����� (�������  ) �������� ��� ��������� ����� </summary>
      //public List<BaseCell> GetOpenNils() {
      //   return Modified
      //      .Where(x => x.State.Status == EState._Open)
      //      .Where(x => x.State.Open == EOpen._Nil)
      //      .ToList();
      //}

      ///// <summary> ��������� ����� (���������) �������� ��� ��������� ����� </summary>
      //public List<BaseCell> GetOpens() {
      //   return Modified
      //      .Where(x => x.State.Status == EState._Open)
      //      .Where(x => x.State.Open != EOpen._Nil)
      //      .ToList();
      //}

      ///// <summary> ��������� ����� � �������� ������/����������� ��� ��������� ����� </summary>
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
