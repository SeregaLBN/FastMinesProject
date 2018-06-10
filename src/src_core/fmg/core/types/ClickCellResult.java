package fmg.core.types;

import java.util.HashSet;
import java.util.Set;

import fmg.core.mosaic.cells.BaseCell;

public class ClickCellResult {

   public Set<BaseCell> modified = new HashSet<BaseCell>();

   /** были ли изменения на поле? Была ли открыта хоть одна ячейка или выставлен/снят флажёк / знак вопроса */
   public boolean isAnyChanges() { return !modified.isEmpty(); }
   public boolean isAnyOpenMine() {
      return modified.stream()
            .filter(x -> x.getState().getStatus() == EState._Open)
            .anyMatch(x -> x.getState().getOpen() == EOpen._Mine);
   }

//   /** множество ячеек (нулевых  ) открытых при последнем клике */
//   public Set<BaseCell> getOpenNils() {
//      return modified.stream()
//            .filter(x -> x.getState().getStatus() == EState._Open)
//            .filter(x -> x.getState().getOpen() == EOpen._Nil)
//            .collect(Collectors.toSet());
//   }
//
//   /** множество ячеек (ненулевых) открытых при последнем клике */
//   public Set<BaseCell> getOpens() {
//      return modified.stream()
//            .filter(x -> x.getState().getStatus() == EState._Open)
//            .filter(x -> x.getState().getOpen() != EOpen._Nil)
//            .collect(Collectors.toSet());
//   }
//
//   /** множество ячеек с флажками снятых/уставленных при последнем клике */
//   public Set<BaseCell> getFlags() {
//      return modified.stream()
//            .filter(x -> x.getState().getStatus() == EState._Close)
//            .collect(Collectors.toSet());
//   }

   public int getCountFlag() {
      return (int)modified.stream()
            .filter(x -> x.getState().getStatus() == EState._Close)
            .filter(x -> x.getState().getClose() == EClose._Flag)
            .count();
   }

   public int getCountOpen() {
      return (int)modified.stream()
            .filter(x -> x.getState().getStatus() == EState._Open)
            .count();
   }

   public int getCountUnknown() {
      return (int)modified.stream()
            .filter(x -> x.getState().getStatus() == EState._Close)
            .filter(x -> x.getState().getClose() == EClose._Unknown)
            .count();
   }

}
