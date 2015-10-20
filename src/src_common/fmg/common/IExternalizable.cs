using System.IO;

namespace fmg.common {
   public interface IExternalizable : ISerializable {
      void writeExternal(BinaryWriter output);
      void readExternal(BinaryReader input);
   }
}