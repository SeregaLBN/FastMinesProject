namespace Fmg.Common {

    public abstract class AProjSettings {

        protected AProjSettings() { }

        public const string PROJECT_NAME = "FastMines";

        protected static string   SettingsFile = PROJECT_NAME + ".settings";
        protected static string StatisticsFile = PROJECT_NAME + ".statistics";
        protected static string  ChampionsFile = PROJECT_NAME + ".best";


        public static string SettingsFileName => SettingsFile;

        public static string StatisticsFileName => StatisticsFile;

        public static string ChampionsFileName => ChampionsFile;

    }

}
