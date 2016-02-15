package fmg.data.controller.event;

import java.util.EventObject;

import fmg.core.types.EMosaic;
import fmg.data.controller.serializable.PlayersModel;
import fmg.data.controller.types.ESkillLevel;

public class PlayerModelEvent extends EventObject {
   private static final long serialVersionUID = 1L;

   public static final int
      INSERT = 1, DELETE = 2, UPDATE = 3,
      INSERT_ALL = 4, DELETE_ALL = 5, UPDATE_ALL = 6,
      CHANGE_STATISTICS = 7;

   private final int pos;
   private final EMosaic mosaic;
   private final ESkillLevel skill;
   private final int type;
   
   public PlayerModelEvent(PlayersModel source, int pos, int type) {
      super(source);
      this.mosaic = null;
      this.skill = null;
      this.pos = pos;
      this.type = type;
   }
   public PlayerModelEvent(PlayersModel source, int pos, int type, EMosaic mosaic, ESkillLevel skill) {
      super(source);
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

   @Override
   public PlayersModel getSource() {
      return (PlayersModel) super.getSource();
   }
}
