using System.IO;

namespace Fmg.Common {

    //[Obsolete]
    public interface IExternalizable : ISerializable {
        void WriteExternal(BinaryWriter output);
        void ReadExternal(BinaryReader input);
    }

}
