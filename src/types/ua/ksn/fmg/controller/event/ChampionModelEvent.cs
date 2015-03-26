using ua.ksn.fmg.controller.serializable;
using ua.ksn.fmg.controller.types;
using ua.ksn.fmg.model.mosaics;

namespace ua.ksn.fmg.controller.Event {

public class ChampionModelEvent {

   public delegate void OnChanged(ChampionsModel source, ChampionModelEvent _event);

   public const int POS_ALL = -1;
	public const int INSERT = 1, UPDATE = 2, DELETE = 3;

	private EMosaic mosaic;
	private ESkillLevel skill;
	private int pos;
	private int type;
	
	public ChampionModelEvent(EMosaic mosaic, ESkillLevel skill, int pos, int type) {
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
}
}