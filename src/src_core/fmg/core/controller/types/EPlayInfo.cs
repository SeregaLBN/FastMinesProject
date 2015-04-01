using System;

namespace fmg.core.controller.types {

   /// <summary>
   /// 1. K�� �����: ���� �/��� �����?
   /// <br/>
   /// 2. Play from file or created game?
   /// </summary>
   public enum EPlayInfo {
      /// <summary> ��� ���������� ��� ������. �� ������ ���� </summary>
      ePlayerUnknown,
      /// <summary> �����/������ ������� </summary>
      ePlayerUser,
      /// <summary> �����/������ ��������� </summary>
      ePlayerAssistant,
      /// <summary> ������ ��� - � �������, � ��������� (�.�. ����� ������ �����, � ��������� ������������, ��� �� ���� �������������� ���������� ����������) </summary>
      ePlayBoth,
      /// <summary> �� ��������� ���� � ���������� � � ����������� ����������� - ������ ���� ���� ��� ��������� �� ����� ��� ���� ������� </summary>
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