package fmg.core.types.viewmodel.event;

import java.util.EventObject;

import fmg.core.types.EMosaic;
import fmg.core.types.ESkillLevel;
import fmg.core.types.viewmodel.serializable.ChampionsModel;

public class ChampionModelEvent extends EventObject {
   private static final long serialVersionUID = 1L;

   public static final int POS_ALL = -1;
   public static final int INSERT = 1, UPDATE = 2, DELETE = 3;

   private final EMosaic mosaic;
   private final ESkillLevel skill;
   private final int pos;
   private final int type;

   public ChampionModelEvent(ChampionsModel source, EMosaic mosaic, ESkillLevel skill, int pos, int type) {
      super(source);
      this.mosaic = mosaic;
      this.skill = skill;
      this.pos = pos;
      this.type = type;
   }

   public ESkillLevel getSkill() {
      return skill;
   }
   public EMosaic getMosaic() {
      return mosaic;
   }
   public int getPos() {
      return pos;
   }
   public int getType() {
      return type;
   }

   @Override
   public ChampionsModel getSource() {
      return (ChampionsModel) super.getSource();
   }
}
