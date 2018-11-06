namespace fmg.core.types.viewmodel.Event {

    public delegate void PlayerModelChangedHandler(object sender, PlayerModelEventArgs e);

    public class PlayerModelEventArgs : System.EventArgs {

        public const int
          INSERT = 1, DELETE = 2, UPDATE = 3,
          INSERT_ALL = 4, DELETE_ALL = 5, UPDATE_ALL = 6,
          CHANGE_STATISTICS = 7;

        public int         Pos { get; }
        public EMosaic     Mosaic { get; }
        public ESkillLevel Skill { get; }
        public int         Type { get; }

        public PlayerModelEventArgs(int pos, int type) {
            this.Mosaic = EMosaic.eMosaicSquare1;
            this.Skill = ESkillLevel.eAmateur;
            this.Pos = pos;
            this.Type = type;
        }
        public PlayerModelEventArgs(int pos, int type, EMosaic mosaic, ESkillLevel skill) {
            this.Mosaic = mosaic;
            this.Skill = skill;
            this.Pos = pos;
            this.Type = type;
        }

    }

}
