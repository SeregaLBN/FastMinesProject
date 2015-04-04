using fmg.data.controller.serializable;
using fmg.data.controller.types;
using fmg.core.types;

namespace fmg.data.controller.Event {

public class PlayerModelEvent { //extends EventObject {

    public delegate void OnChanged(PlayersModel source, PlayerModelEvent _event);

    public const int
      INSERT = 1, DELETE = 2, UPDATE = 3,
      INSERT_ALL = 4, DELETE_ALL = 5, UPDATE_ALL = 6,
      CHANGE_STATISTICS = 7;

   private int pos;
   private EMosaic mosaic;
   private ESkillLevel skill;
   private int type;
   
   public PlayerModelEvent(int pos, int type) {
      //base(source);
      this.mosaic = EMosaic.eMosaicSquare1;
      this.skill = ESkillLevel.eAmateur;
      this.pos = pos;
      this.type = type;
   }
   public PlayerModelEvent(int pos, int type, EMosaic mosaic, ESkillLevel skill) {
      //base(source);
      this.mosaic = mosaic;
      this.skill = skill;
      this.pos = pos;
      this.type = type;
   }

   public int getPos() {
      return pos;
   }
   public int getType() {
      return type;
   }
   public EMosaic getMosaic() {
      return mosaic;
   }
   public ESkillLevel getSkill() {
      return skill;
   }
}
}