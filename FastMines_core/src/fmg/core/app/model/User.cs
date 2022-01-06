using System;

namespace Fmg.Core.App.Model {

    /// <summary> User model </summary>
    public class User {

        private Guid id;
        private string name;
        private string password;

        /// <summary> Unique user ID. const <summary>
        public Guid Id {
            get => id;
            set {
                if (value == Guid.Empty)
                    throw new ArgumentNullException("Unique ID must be exist");

                if (id != Guid.Empty)
                    throw new InvalidOperationException("Illegal usage - con not change existed id");

                id = value;
            }
        }

        /// <summary> User name. May be changed <summary>
        public string Name {
            get => name;
            set {
                if (string.IsNullOrEmpty(value))
                    throw new ArgumentNullException("Invalid player name. Need not empty.");

                name = value;
            }
        }

        /// <summary> User password <summary>
        public string Password {
            get => password;
            set {
                if (string.IsNullOrEmpty(value))
                    value = null;

                password = value;
            }
        }

        /// <summary> link to image <summary>
        public string ImgAvatar { get; set; }

        public override string ToString() {
            return name + "; passw " + (string.IsNullOrEmpty(password) ? "not exist" : "is exist");
        }

    }

}
