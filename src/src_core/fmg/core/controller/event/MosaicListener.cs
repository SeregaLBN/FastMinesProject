using fmg.common.geom;
using fmg.core.controller.types;
using fmg.core.model.mosaics;
using fmg.core.model.mosaics.cell;

// ��������, ��� ����������� �� �������
namespace fmg.core.controller.Event {

   /// <summary> ����������� � ���, ��� �� ������� ��� ��������� ���� </summary>
   public delegate void OnClickEvent(Mosaic source, BaseCell clickedCell, bool leftClick, bool down);

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