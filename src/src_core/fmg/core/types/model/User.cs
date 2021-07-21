using System;
using System.IO;
using Fmg.Common;

namespace Fmg.Core.Types.Model {

    public class User : IExternalizable {

        /** неизменный id пользователя */
        private Guid guid;
        /** юзер может менять и имя ... */
        private string name;
        /** ... и пароль */
        private string password;
        public string imgAvatar;

        /** new User */
        public User(string name, string password, string imgAvatar) {
            this.guid = Guid.NewGuid();

            if (string.IsNullOrEmpty(name))
                throw new Exception("Invalid player name. Need not empty.");
            this.name = name;

            if ((password != null) && string.IsNullOrEmpty(password))
                password = null;
            this.password = password;

            if ((imgAvatar != null) && string.IsNullOrEmpty(imgAvatar))
                imgAvatar = null;
            this.imgAvatar = imgAvatar;
        }

        /** load from file */
        public User(BinaryReader input) {
            ReadExternal(input);
        }

        public Guid Guid { get { return guid; } }
        public String Name {
            get { return name; }
            set {
                if (string.IsNullOrEmpty(value))
                    throw new ArgumentException("Invalid player name. Need not empty.");

                this.name = value;
            }
        }

        public override string ToString() {
            //      return super.toString();
            return name + "; passw " + (string.IsNullOrEmpty(password) ? "not exist" : "is exist");
        }

        public void ReadExternal(BinaryReader input) {
            guid = new Guid(input.ReadString());
            name = input.ReadString();

            bool exist = input.ReadBoolean();
            if (exist)
                password = input.ReadString();

            exist = input.ReadBoolean();
            if (exist)
                imgAvatar = input.ReadString();
        }

        public void WriteExternal(BinaryWriter output) {
            output.Write(guid.ToString());
            output.Write(name);

            bool exist = !string.IsNullOrEmpty(password);
            output.Write(exist);
            if (exist)
                output.Write(password);

            exist = !string.IsNullOrEmpty(imgAvatar);
            output.Write(exist);
            if (exist)
                output.Write(imgAvatar);
        }

    }

}
