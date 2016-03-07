package fmg.core.types.click;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import fmg.core.mosaic.cells.BaseCell;
import fmg.core.types.EClose;
import fmg.core.types.EOpen;
import fmg.core.types.EState;

public class ClickContext {
   public List<BaseCell> modified = new ArrayList<BaseCell>();
   
   public boolean isOpenMine() {
      return modified.stream()
            .filter(x -> x.getState().getStatus() == EState._Open)
            .anyMatch(x -> x.getState().getOpen() == EOpen._Mine);
   }
   
   /** множество ячеек (нулевых  ) открытых при последнем клике */
   public Set<BaseCell> getOpenNil() {
      return modified.stream()
            .filter(x -> x.getState().getStatus() == EState._Open)
            .filter(x -> x.getState().getOpen() == EOpen._Nil)
            .collect(Collectors.toSet());
   }

   /** множество ячеек (ненулевых) открытых при последнем клике */
   public Set<BaseCell> getOpen() {
      return modified.stream()
            .filter(x -> x.getState().getStatus() == EState._Open)
            .filter(x -> x.getState().getOpen() != EOpen._Nil)
            .collect(Collectors.toSet());
   }

   /** множество ячеек с флажками снятых/уставленных при последнем клике */
   public Set<BaseCell> getFlag() {
      return modified.stream()
            .filter(x -> x.getState().getStatus() == EState._Close)
            .collect(Collectors.toSet());
   }

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
