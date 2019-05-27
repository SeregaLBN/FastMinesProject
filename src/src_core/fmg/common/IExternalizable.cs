using System.IO;

namespace Fmg.Common {

    //[Obsolete]
    public interface IExternalizable : ISerializable {
        void writeExternal(BinaryWriter output);
        void readExternal(BinaryReader input);
    }

}
