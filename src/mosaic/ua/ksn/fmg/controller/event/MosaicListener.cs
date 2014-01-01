using ua.ksn.fmg.controller.types;
using ua.ksn.fmg.model.mosaics;

/// <summary>��������, ��� ����������� �� �������</summary>
namespace ua.ksn.fmg.controller.Event {

   /// <summary>����������� � ���, ��� �� ������� ��� ��������� ����</summary>
   public delegate void OnClick(Mosaic source, bool leftClick, bool down);

   /// <summary>�������� ���-�� �������� ����� / ������ / ������ / ... �� �������</summary>
   public delegate void OnChangeCounters(Mosaic source);

   /// <summary>����������� � ��������� ������� ���� (����� ����, ������ ����, ����� ����)</summary>
   public delegate void OnChangeGameStatus(Mosaic source, EGameStatus oldValue);

   /// <summary>����������� � ��������� ������� ������� � ������</summary>
   public delegate void OnChangeArea(Mosaic source, int oldArea);

   /// <summary>����������� � ��������� ���� �������</summary>
   public delegate void OnChangeMosaicType(Mosaic source, EMosaic oldMosaic);
}