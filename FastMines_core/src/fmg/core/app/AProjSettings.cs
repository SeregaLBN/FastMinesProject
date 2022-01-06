namespace Fmg.Core.App {

    public abstract class AProjSettings {

        protected AProjSettings() { }

        public const string PROJECT_NAME = "FastMines";
        public const string CORE_VERSION = "2.2.2";

        protected static string  SettingsFile = PROJECT_NAME + ".settings";
        protected static string   PlayersFile = PROJECT_NAME + ".players";
        protected static string ChampionsFile = PROJECT_NAME + ".best";


        public static string SettingsFileName => SettingsFile;

        public static string PlayersFileName => PlayersFile;

        public static string ChampionsFileName => ChampionsFile;

    }

}
