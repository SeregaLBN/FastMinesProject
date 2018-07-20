using fmg.data.controller.types;
using fmg.core.types;

namespace fmg.data.controller.Event
{

   public delegate void ChampionModelChangedHandler(object sender, ChampionModelEventArgs e);

   public class ChampionModelEventArgs : System.EventArgs
   {

      public const int POS_ALL = -1;
      public const int INSERT = 1, UPDATE = 2, DELETE = 3;

      private EMosaic mosaic;
      private ESkillLevel skill;
      private int pos;
      private int type;

      public ChampionModelEventArgs(EMosaic mosaic, ESkillLevel skill, int pos, int type)
      {
         this.mosaic = mosaic;
         this.skill = skill;
         this.pos = pos;
         this.type = type;
      }

      public ESkillLevel getSkill()
      {
         return skill;
      }
      public EMosaic getMosaic()
      {
         return mosaic;
      }
      public int getPos()
      {
         return pos;
      }
      public int getType()
      {
         return type;
      }

   }
}