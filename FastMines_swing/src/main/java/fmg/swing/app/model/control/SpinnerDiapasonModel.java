package fmg.swing.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import javax.swing.SpinnerNumberModel;

public class SpinnerDiapasonModel extends SpinnerNumberModel {
   private static final long serialVersionUID = 1L;

   private List<Integer> vals; 

   public SpinnerDiapasonModel(Integer vals[]) {
      this(Arrays.asList(vals));
   }
   public SpinnerDiapasonModel(List<Integer> vals) {
      Collections.sort(vals);
      this.vals = vals;
      this.setValue(vals.get(0));
      this.setMinimum(vals.get(0));
      this.setMaximum(vals.get(vals.size()-1));
      this.setStepSize(1);
   }

   @Override
   public void setValue(Object value) {
//      System.out.println("> setValue: value=" + value);
      int oldValue = (Integer) this.getValue();

      // check new value (is valid?)
      if (vals.contains(oldValue)) {

         Integer newValue = (Integer) value;

         if (!vals.contains(newValue)) { // not valid..
            int pos = vals.indexOf(oldValue);

            ListIterator<Integer> iter = vals.listIterator(pos);
            if (newValue > oldValue) { // set next value 
               iter.next();
               value = iter.next(); // change to valid value
            }
            if (newValue < oldValue) // set prev value 
               value = iter.previous(); // change to valid value

//            System.out.println("  setValue: value changet to " + value);
         }
      }

      super.setValue(value);
//      System.out.println("< setValue ");
   }

   @Override
   public Integer getMinimum() { return (Integer) super.getMinimum(); }
   @Override
   public Integer getMaximum() { return (Integer) super.getMaximum(); }
   @Override
   public Integer getValue() { return (Integer) super.getValue(); }
   @Override
   public Integer getNumber() { return (Integer) super.getNumber(); }
}
