package fmg.core.mosaic;

import java.lang.reflect.Constructor;

import fmg.common.geom.Coord;
import fmg.common.geom.Matrisize;
import fmg.common.geom.Size;
import fmg.core.mosaic.cells.BaseCell;
import fmg.core.types.EMosaic;

public final class MosaicHelper {
   private static final String getPackageName() {
      Package pkg = MosaicHelper.class.getPackage();
      if (pkg != null)
         return pkg.getName();
      String[] arr = MosaicHelper.class.getName().split("\\.");
      String name = "";
      for (int i=0; i<arr.length-1; i++) {
         if (!name.isEmpty())
            name += '.';
         name += arr[i];
      }
      return name;
   }

   /** Создать экземпляр атрибута для конкретного типа мозаики */
   public static final BaseCell.BaseAttribute createAttributeInstance(EMosaic mosaicType, int area) {
//      switch (mosaicType) {
//      case eMosaicTriangle1  : return new Triangle1.AttrTriangle1(area);
//      // ...
//      case eMosaicSquare1    : return new Square1.AttrSquare1(area);
//      // ...
//      }
//      throw new RuntimeException("Unknown type "+mosaicType);

      try {
         String className = getPackageName() + ".cells." + mosaicType.getMosaicClassName() + "$Attr"+mosaicType.getMosaicClassName();
         @SuppressWarnings("unchecked")
         Class<? extends BaseCell.BaseAttribute> cellAttrClass = (Class<? extends BaseCell.BaseAttribute>)Class.forName(className);
         Constructor<? extends BaseCell.BaseAttribute> constructor = cellAttrClass.getConstructor(int.class); //(Constructor<? extends BaseAttribute>) cellClass.getConstructors()[0]; // 
         BaseCell.BaseAttribute attr = constructor.newInstance(area);
         return attr;
      } catch (Exception ex) {
         System.err.println(ex.getMessage());
         ex.printStackTrace(System.err);
         throw new RuntimeException("Unknown type "+mosaicType + ": "+ex.getMessage(), ex);
      }
   }

   /** Создать экземпляр ячейки для конкретного типа мозаики */
   public static final BaseCell createCellInstance(BaseCell.BaseAttribute attr, EMosaic mosaicType, Coord coord)
   {
//      switch (mosaicType) {
//      case eMosaicTriangle1  : return new Triangle1((Triangle1.AttrTriangle1) attr, coord);
//      //...
//      case eMosaicSquare1    : return new Square1((Square1.AttrSquare1) attr, coord);
//      //...
//      }
//      throw new RuntimeException("Unknown type "+mosaicType);

      try {
         String className = getPackageName() + ".cells." + mosaicType.getMosaicClassName();
         @SuppressWarnings("unchecked")
         Class<? extends BaseCell> cellClass = (Class<? extends BaseCell>)Class.forName(className);

         Constructor<? extends BaseCell> constructor = cellClass.getConstructor(attr.getClass(), coord.getClass()); // cellClass.getConstructors()[0];
         BaseCell cell = constructor.newInstance(attr, coord);
         cell.Init();
         return cell;
      } catch (Exception ex) {
         System.err.println(ex.getMessage());
         ex.printStackTrace(System.err);
         throw new RuntimeException("Unknown type "+mosaicType + ": "+ex.getMessage(), ex);
      }
   }

   /**
    * Поиск больше-меньше
    * @param baseDelta - начало дельты приращения
    * @param func - ф-ция сравнения
    * @return что найдено
    */
   static int Finder(int baseDelta, Comparable<Integer> func) {
      double res = baseDelta;
      double d = baseDelta;
      boolean deltaUp = true;
      do {
         int cmp = func.compareTo((int)res);
         // Example:
         // func comparable(x) -> return x==1000 ? 0: x<1000 ? -1 : +1;
         // init data:  res=100 d=100
         //  iter 0: cmp=+1; d=d*2=200; res=res+d=300
         //  iter 1: cmp=+1; d=d*2=400; res=res+d=700
         //  iter 2: cmp=+1; d=d*2=800; res=res+d=1500
         //  iter 3: cmp=-1; d=d/2=400; res=res-d=1100
         //  iter 4: cmp=-1; d=d/2=200; res=res-d=900
         //  iter 5: cmp=+1; d=d/2=100; res=res+d=1000 - finded!!!
         if (cmp == 0)
            return (int)res;
         if ((d < 1) && (cmp == -1))
            break;
         
         boolean resultUp = (cmp < 0);
         deltaUp = deltaUp && resultUp;
         if (deltaUp)
            d *= 2;
         else
            d /= 2;
         if (resultUp)
            res += d;
         else
            res -= d;
        } while (true);
      return (int)res;
   }

   /** узнаю мах размер площади ячеек мозаики, при котором окно проекта вмещается в заданную область
    * @param mosaicSizeField - интересуемый размер (в ячейках) поля мозаики
    * @param sizeClientIn - размер окна/области (в пикселях) в которую должна вписаться мозаика
    * @param sizeClientOut - размер окна/области (в пикселях) в которую реально впишется мозаика
    * @return площадь ячейки
    */
   private static int findAreaBySize(BaseCell.BaseAttribute cellAttr, final Matrisize mosaicSizeField, final Size sizeClientIn, Size sizeClientOut) {
      // сделал приватным, т.к. неявно меняет свойства параметра 'cellAttr'

      final Size sizeIter = new Size();
      int res = Finder(MosaicBase.AREA_MINIMUM, new Comparable<Integer>() {
         @Override
         public int compareTo(Integer area) {
            cellAttr.setArea(area);
            Size tmp = cellAttr.getOwnerSize(mosaicSizeField);
            sizeIter.width = tmp.width;
            sizeIter.height = tmp.height;
            if ((sizeIter.width == sizeClientIn.width) &&
               (sizeIter.height <= sizeClientIn.height))
               return 0;
            if ((sizeIter.width <= sizeClientIn.width) &&
               (sizeIter.height == sizeClientIn.height))
               return 0;
            if ((sizeIter.width < sizeClientIn.width) &&
               (sizeIter.height < sizeClientIn.height))
               return -1;
            return +1;
         }
      });
      sizeClientOut.width = sizeIter.width;
      sizeClientOut.height = sizeIter.height;
      return res;
   }

   /** узнаю max размер поля мозаики, при котором окно проекта вмещается в в заданную область
    * @param cellAttr - метаданные ячеек
    * @param sizeClient - размер окна/области (в пикселях) в которую должна вписаться мозаика
    * @return размер поля мозаики
    */
   public static Matrisize findSizeByArea(BaseCell.BaseAttribute cellAttr, final Size sizeClient) {
      final Matrisize result = new Matrisize();
      Finder(10, new Comparable<Integer>() {
         @Override
         public int compareTo(Integer newWidth) {
            result.m = newWidth;
            Size sizeWnd = cellAttr.getOwnerSize(result);
            if (sizeWnd.width == sizeClient.width)
               return 0;
            if (sizeWnd.width <= sizeClient.width)
               return -1;
            return +1;
         }
      });
      Finder(10, new Comparable<Integer>() {
         @Override
         public int compareTo(Integer newHeight) {
            result.n = newHeight;
            Size sizeWnd = cellAttr.getOwnerSize(result);
            if (sizeWnd.height == sizeClient.height)
               return 0;
            if (sizeWnd.height < sizeClient.height)
               return -1;
            return +1;
         }
      });
      return result;
   }

   /** узнаю мах размер площади ячеек мозаики, при котором окно проекта вмещается в заданную область
    * @param mosaicSizeField - интересуемый размер (в ячейках) поля мозаики
    * @param sizeClientIn - размер окна/области (в пикселях) в которую должна вписаться мозаика
    * @param sizeClientOut - размер окна/области (в пикселях) в которую реально впишется мозаика
    * @return площадь ячейки
    */
   public static int findAreaBySize(EMosaic mosaicType, Matrisize mosaicSizeField, Size sizeClientIn, Size sizeClientOut) {
      return findAreaBySize(createAttributeInstance(mosaicType, 0), mosaicSizeField, sizeClientIn, sizeClientOut);
   }

   /** узнаю max размер поля мозаики, при котором окно проекта вмещается в в заданную область
    * @param area - интересуемая площадь ячеек мозаики
    * @param sizeClient - размер окна/области (в пикселях) в которую должна вписаться мозаика
    * @return размер поля мозаики
    */
   public static Matrisize findSizeByArea(EMosaic mosaicType, int area, Size sizeClient) {
      return findSizeByArea(createAttributeInstance(mosaicType, area), sizeClient);
   }

    /** get parent container (owner window) size in pixels */
    public static Size getOwnerSize(EMosaic mosaicType, int area, Matrisize mosaicSizeField) {
       return createAttributeInstance(mosaicType, area).getOwnerSize(mosaicSizeField);
    }

}
