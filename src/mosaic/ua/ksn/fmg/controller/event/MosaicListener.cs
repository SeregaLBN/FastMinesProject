using ua.ksn.geom;
using ua.ksn.fmg.controller.types;
using ua.ksn.fmg.model.mosaics;

// ��������, ��� ����������� �� �������
namespace ua.ksn.fmg.controller.Event {

   /// <summary> ����������� � ���, ��� �� ������� ��� ��������� ���� </summary>
   public delegate void OnClickEvent(Mosaic source, bool leftClick, bool down);

   /// <summary> �������� ���-�� �������� ����� / ������ / ������ / ... �� ������� </summary>
   public delegate void OnChangedCountersEvent(Mosaic source);

   /// <summary> ����������� �� ��������� ������� ���� (����� ����, ������ ����, ����� ����) </summary>
   public delegate void OnChangedGameStatusEvent(Mosaic source, EGameStatus oldValue);

   /// <summary> ����������� �� ��������� ������� ������� � ������ </summary>
   public delegate void OnChangedAreaEvent(Mosaic source, int oldArea);

   /// <summary> ����������� �� ��������� ���� ������� </summary>
   public delegate void OnChangedMosaicTypeEvent(Mosaic source, EMosaic oldMosaic);

   /// <summary> ����������� �� ��������� ������� ������� </summary>
   public delegate void OnChangedMosaicSizeEvent(Mosaic source, Size oldSize);
}