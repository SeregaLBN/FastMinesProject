using System;
using System.IO;

namespace fmg.common {

   //[Obsolete]
   public interface IExternalizable : ISerializable {
      void writeExternal(BinaryWriter output);
      void readExternal(BinaryReader input);
   }

}
