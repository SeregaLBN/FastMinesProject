using System;
using System.IO;
using Fmg.Core.App.Model;

namespace Fmg.Core.App.Serializers {

    /// <summary> User (de)serializer </summary>
    public class UserSerializer : ISerializer {

        private const long VERSION = 1;

        public void Write(User user, BinaryWriter output) {
            output.Write(VERSION);
            output.Write(user.Id.ToString());
            output.Write(user.Name);

            string pass = user.Password;
            bool exist = string.IsNullOrEmpty(pass);
            output.Write(exist);
            if (exist)
                output.Write(pass);

            exist = string.IsNullOrEmpty(user.ImgAvatar);
            output.Write(exist);
            if (exist)
                output.Write(user.ImgAvatar);
        }

        public User Read(BinaryReader input) {
            long ver = input.ReadInt64();
            if (ver != VERSION)
                throw new ArgumentException(nameof(UserSerializer) + ": Unsupported version #" + ver);

            User user = new User {
                Id = new Guid(input.ReadString()),
                Name = input.ReadString()
            };

            bool exist = input.ReadBoolean();
            if (exist)
                user.Password = input.ReadString();

            exist = input.ReadBoolean();
            if (exist)
                user.ImgAvatar = input.ReadString();

            return user;
        }

}

}
