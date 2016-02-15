package fmg.core.types.click;

import java.util.ArrayList;
import java.util.List;

import fmg.core.mosaic.cells.BaseCell;

public class LeftDownResult {
   public List<BaseCell> needRepaint;

   public LeftDownResult()
   {
      needRepaint = new ArrayList<BaseCell>();
   }
}
