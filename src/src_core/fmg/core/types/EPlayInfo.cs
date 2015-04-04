using System;

namespace fmg.core.controller.types {

   /// <summary>
   /// 1. Kто играл: юзер и/или робот?
   /// <br/>
   /// 2. Play from file or created game?
   /// </summary>
   public enum EPlayInfo {
      /// <summary> ещё неизвестно кто играет. До старта игры </summary>
      ePlayerUnknown,
      /// <summary> играл/играет человек </summary>
      ePlayerUser,
      /// <summary> играл/играет ассистент </summary>
      ePlayerAssistant,
      /// <summary> играли оба - и человек, и ассистент (т.е. может играть робот, а закончить пользователь, или же юзер воспользовался подсказкой ассистента) </summary>
      ePlayBoth,
      /// <summary> Не учитывать игру в статистике и в чемпионских результатах - данная игра была или загружена из файла или была создана </summary>
      ePlayIgnor
   }

   public static class EPlayInfoEx {
      public static EPlayInfo setPlayInfo(EPlayInfo oldVal, EPlayInfo newVal) {
         switch (newVal) {
         case EPlayInfo.ePlayerUnknown:      return EPlayInfo.ePlayerUnknown;
         case EPlayInfo.ePlayerUser:
            switch (oldVal) {
            case EPlayInfo.ePlayerUnknown:   return EPlayInfo.ePlayerUser;
            case EPlayInfo.ePlayerUser:      return EPlayInfo.ePlayerUser;
            case EPlayInfo.ePlayerAssistant: return EPlayInfo.ePlayBoth;
            case EPlayInfo.ePlayBoth:        return EPlayInfo.ePlayBoth;
            case EPlayInfo.ePlayIgnor:       return EPlayInfo.ePlayIgnor;
            }
            break;
         case EPlayInfo.ePlayerAssistant:
            switch (oldVal) {
            case EPlayInfo.ePlayerUnknown:   return EPlayInfo.ePlayerAssistant;
            case EPlayInfo.ePlayerUser:      return EPlayInfo.ePlayBoth;
            case EPlayInfo.ePlayerAssistant: return EPlayInfo.ePlayerAssistant;
            case EPlayInfo.ePlayBoth:        return EPlayInfo.ePlayBoth;
            case EPlayInfo.ePlayIgnor:       return EPlayInfo.ePlayIgnor;
            }
            break;
         case EPlayInfo.ePlayBoth:
            switch (oldVal) {
            case EPlayInfo.ePlayerUnknown:
            case EPlayInfo.ePlayerUser:
            case EPlayInfo.ePlayerAssistant:
            case EPlayInfo.ePlayBoth:        return EPlayInfo.ePlayBoth;
            case EPlayInfo.ePlayIgnor:       return EPlayInfo.ePlayIgnor;
            }
            break;
         case EPlayInfo.ePlayIgnor:          return EPlayInfo.ePlayIgnor;
         }
         throw new Exception("RuntimeException"); // never
      }
   }
}