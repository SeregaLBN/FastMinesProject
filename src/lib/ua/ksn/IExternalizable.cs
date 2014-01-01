using System.IO;

namespace ua.ksn {
   public interface IExternalizable : ISerializable {
      void writeExternal(BinaryWriter output);
      void readExternal(BinaryReader input);
   }
}