namespace fmg.core.types.viewmodel.Event {

    public delegate void ChampionModelChangedHandler(object sender, ChampionModelEventArgs e);

    public class ChampionModelEventArgs : System.EventArgs {

        public const int POS_ALL = -1;
        public const int INSERT = 1, UPDATE = 2, DELETE = 3;

        public EMosaic     Mosaic  { get; }
        public ESkillLevel Skill   { get; }
        public int         Pos     { get; }
        public int         Type    { get; }

        public ChampionModelEventArgs(EMosaic mosaic, ESkillLevel skill, int pos, int type) {
            this.Mosaic = mosaic;
            this.Skill = skill;
            this.Pos = pos;
            this.Type = type;
        }

    }

}
