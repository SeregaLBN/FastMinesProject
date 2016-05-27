namespace fmg.core.mosaic.draw
{

   /// <summary> The essence of drawing:
   /// <list type="bullet">
   /// <item>
   /// <description>Win32 API: HDC</description>
   /// </item>
   /// <item>
   /// <description>image: e.g. bitmap</description>
   /// </item>
   /// <item>
   /// <description>java Swing: java.awt.Graphics</description>
   /// </item>
   /// <item>
   /// <description>UWP: xaml shapes</description>
   /// </item>
   /// </list>
   /// (other names - surface; device context; drawing session; graphics; etc.)
   /// </summary>
   public interface IPaintable { }

}
