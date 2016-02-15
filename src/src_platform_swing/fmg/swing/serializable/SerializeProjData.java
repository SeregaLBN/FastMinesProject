package fmg.swing.serializable;

import java.io.Externalizable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.UUID;

import fmg.common.geom.Matrisize;
import fmg.common.geom.Point;
import fmg.core.types.EMosaic;
import fmg.data.controller.types.ESkillLevel;
import fmg.data.view.draw.EShowElement;
import fmg.swing.Main;

/** Данные проекта, записываемые/считываемые в/из файл(а) */
public class SerializeProjData implements Externalizable {
   private static final long version = Main.serialVersionUID;

   private Matrisize sizeField;
   private EMosaic mosaicType;
   private int minesCount;
   private int area;

   private UUID activeUserId;
   private boolean doNotAskStartup; // manage dialog

   private boolean[] eShowElements;
   private boolean zoomAlwaysMax;
   private boolean useUnknown;
   private boolean usePause;
   private Point location;
   private boolean systemTheme;

   public SerializeProjData() { setDefaults(); }
   
   private void setDefaults() {
      mosaicType = EMosaic.eMosaicSquare1;
      sizeField = ESkillLevel.eBeginner.DefaultSize();
      minesCount = ESkillLevel.eBeginner.GetNumberMines(mosaicType);
      area = 2300;

      activeUserId = null;
      doNotAskStartup = true;

      if (eShowElements == null)
         eShowElements = new boolean[EShowElement.values().length];
      for (EShowElement se: EShowElement.values())
         eShowElements[se.ordinal()] = true;

      zoomAlwaysMax = false;
      useUnknown = true;
      usePause = false;

      if (location == null)
         location = new Point(0, 0);
      else
         location.x = location.y = 0; 

      systemTheme = true;
   }

   @Override
   public void writeExternal(ObjectOutput out) throws IOException {
      out.writeLong(version);

      out.writeInt(area);
      out.writeInt(mosaicType.getIndex());
      out.writeInt(sizeField.m);
      out.writeInt(sizeField.n);
      out.writeInt(minesCount);

      out.writeBoolean(activeUserId != null);
      if (activeUserId != null)
         out.writeUTF(activeUserId.toString());
      out.writeBoolean(doNotAskStartup);

      out.writeBoolean(systemTheme);
      for (int i=0; i<eShowElements.length; i++)
         out.writeBoolean(eShowElements[i]);
      out.writeBoolean(zoomAlwaysMax);
      out.writeBoolean(useUnknown);
      out.writeBoolean(usePause);
      out.writeInt(location.x);
      out.writeInt(location.y);
   }

   @Override
   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
      if (version != in.readLong())
         throw new RuntimeException("Unknown version!");

      area = in.readInt();
      mosaicType = EMosaic.fromIndex(in.readInt());
      sizeField = new Matrisize(in.readInt(), in.readInt());
      minesCount = in.readInt();

      if (in.readBoolean())
         activeUserId = UUID.fromString(in.readUTF());
      doNotAskStartup = in.readBoolean();

      systemTheme = in.readBoolean();
      for (int i=0; i<eShowElements.length; i++)
         eShowElements[i] = in.readBoolean();
      zoomAlwaysMax = in.readBoolean();
      useUnknown = in.readBoolean();
      usePause = in.readBoolean();
      location.x = in.readInt();
      location.y = in.readInt();
   }

   /**
    * Load ini data from file
    * @return <b>true</b> - successful read; <b>false</b> - not exist or fail read, and set to defaults
    */
   public boolean Load() {
      File file = getIniFile();
      if (!file.exists()) {
         setDefaults();
         return false;
      }

      try {
         ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
         this.readExternal(in);
         return true;
      } catch (Exception ex) {
         ex.printStackTrace();
         setDefaults();
         return false;
      }
   }

   public void Save() throws FileNotFoundException, IOException {
      ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(getIniFile()));
      this.writeExternal(out);
      out.flush();
   }

   public static File getIniFile() {
      return new File(System.getProperty("user.dir") + System.getProperty("file.separator") + "Mines.dat");
   }

   public Matrisize getSizeField() { return sizeField; }
   public void setSizeField(Matrisize sizeField) { this.sizeField = sizeField; }

   public EMosaic getMosaicType() { return mosaicType; }
   public void setMosaicType(EMosaic mosaicType) { this.mosaicType = mosaicType; }

   public int getMinesCount() { return minesCount; }
   public void setMinesCount(int minesCount) { this.minesCount = minesCount; }

   public int getArea() { return area; }
   public void setArea(int area) { this.area = area; }

   public boolean getShowElement(EShowElement key) { return eShowElements[key.ordinal()]; }
   public void setShowElement(EShowElement key, boolean val) { this.eShowElements[key.ordinal()] = val; }

   public boolean isZoomAlwaysMax() { return zoomAlwaysMax; }
   public void setZoomAlwaysMax(boolean zoomAlwaysMax) { this.zoomAlwaysMax = zoomAlwaysMax; }

   public boolean isUseUnknown() { return useUnknown; }
   public void setUseUnknown(boolean useUnknown) { this.useUnknown = useUnknown; }

   public boolean isUsePause() { return usePause; }
   public void setUsePause(boolean usePause) { this.usePause = usePause; }

   public Point getLocation() { return location; }
   public void setLocation(Point location) { this.location = location; }

   public boolean isSystemTheme() { return systemTheme; }
   public void setSystemTheme(boolean systemTheme) { this.systemTheme = systemTheme; }

   public UUID getActiveUserId() { return activeUserId; }
   public void setActiveUserId(UUID activeUserId) { this.activeUserId = activeUserId; }

   public boolean isDoNotAskStartup() { return doNotAskStartup; }
   public void setDoNotAskStartup(boolean doNotAskStartup) { this.doNotAskStartup = doNotAskStartup; }
}