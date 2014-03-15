using ua.ksn.fmg.controller.types;
using ua.ksn.fmg.model.mosaics;

/// <summary>��������, ��� ����������� �� �������</summary>
namespace ua.ksn.fmg.controller.Event {

   /// <summary>����������� � ���, ��� �� ������� ��� ��������� ����</summary>
   public delegate void OnClickEvent(Mosaic source, bool leftClick, bool down);

   /// <summary>�������� ���-�� �������� ����� / ������ / ������ / ... �� �������</summary>
   public delegate void OnChangeCountersEvent(Mosaic source);

   /// <summary>����������� � ��������� ������� ���� (����� ����, ������ ����, ����� ����)</summary>
   public delegate void OnChangeGameStatusEvent(Mosaic source, EGameStatus oldValue);

   /// <summary>����������� � ��������� ������� ������� � ������</summary>
   public delegate void OnChangeAreaEvent(Mosaic source, int oldArea);

   /// <summary>����������� � ��������� ���� �������</summary>
   public delegate void OnChangeMosaicTypeEvent(Mosaic source, EMosaic oldMosaic);
}