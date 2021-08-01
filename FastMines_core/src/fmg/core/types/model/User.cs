using System;

namespace Fmg.Core.Types.Model {

    /// <summary> User model </summary>
    public class User {

        /// <summary> Unique user ID. const <summary>
        public Guid Id {
            get => Id;
            set {
                if (value == Guid.Empty)
                    throw new ArgumentNullException("Unique ID must be exist");

                if (Id != Guid.Empty)
                    throw new InvalidOperationException("Illegal usage - con not change existed id");

                Id = value;
            }
        }

        /// <summary> User name. May be changed <summary>
        public string Name {
            get => Name;
            set {
                if ((value == null) || (value.Length==0))
                    throw new ArgumentNullException("Invalid player name. Need not empty.");

                Name = value;
            }
        }

        /// <summary> User password <summary>
        public string Password {
            get => Password;
            set {
                if ((value != null) && (value.Length == 0))
                    value = null;

                Password = value;
            }
        }

        /// <summary> link to image <summary>
        public string ImgAvatar { get; set; }

        public override string ToString() {
            return Name + "; passw " + (string.IsNullOrEmpty(Password) ? "not exist" : "is exist");
        }

    }

}
