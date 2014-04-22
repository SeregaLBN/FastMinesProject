////////////////////////////////////////////////////////////////////////////////
//                               FMG project
//                                      � Sergey Krivulya (KSerg, aka SeregaLBN)
// file name: "BaseCell.java"
//
// ���������� �������� ������ BaseCell
// Copyright (C) 2010-2011 Sergey Krivulya
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
////////////////////////////////////////////////////////////////////////////////
using System;
using System.ComponentModel;
using ua.ksn.fmg.Event.click;
using ua.ksn.geom;

namespace ua.ksn.fmg.model.mosaics.cell {

/// <summary>������� ����� ������-������</summary>
public abstract class BaseCell {
   public const double PI = 3.14159265358979323846; // Math.PI;
   public static readonly double SQRT2   = Math.Sqrt(2.0);
   public static readonly double SQRT3   = Math.Sqrt(3.0);
   public static readonly double SQRT27  = Math.Sqrt(27.0);
   public static readonly double SQRT48  = Math.Sqrt(48.0);
   public static readonly double SQRT147 = Math.Sqrt(147.0);
   public static readonly double SIN15   = Math.Sin(PI/180.0*15.0);
   public static readonly double SIN18   = Math.Sin(PI/180.0*18.0);
   public static readonly double SIN36   = Math.Sin(PI/180.0*36.0);
   public static readonly double SIN54   = Math.Sin(PI/180.0*54.0);
   public static readonly double SIN72   = Math.Sin(PI/180.0*72.0);
   public static readonly double SIN75   = Math.Sin(PI/180.0*75.0);
   public static readonly double SIN99   = Math.Sin(PI/180.0*99.0);
   public static readonly double TAN15   = Math.Tan(PI/180.0*15.0);
   public static readonly double TAN45_2 = Math.Tan(PI/180.0*45.0/2);
   public static readonly double SIN135a = Math.Sin(PI/180.0*135.0-Math.Atan(8.0/3));

   /*
    * ��������/����������, ����������� ����� ���-�� ��� ������� �� ����������� BaseCell.
    * <br> (������ ������ � ���������� �������) <br>
    * ��������������� ������������ BaseCell
    */
   public abstract class BaseAttribute : FastMines.Common.BindableBase {
      /// �� PropertyChanged ��� ��������� ��� ���������� BaseCell: ��� ��������� A - ���� ��������� ��� ���������� �����

      public BaseAttribute(int area) {
         Area = area;
      }

      /// <summary>������� ������/������</summary>
      private int _area;

      /// <summary>������� ������/������</summary>
      public int Area {
         get { return _area; }
         set { this.SetProperty(ref this._area, value); }
      }

      /// <summary>����������� ������ ��������, ���������� � ������ - ������� ���� ���������� �����������/�����
      /// �� ������ �������� ����������</summary>
      public abstract double CalcSq(int area, int borderWidth);

      /// <summary>����������� �������� A (������� �������� ������ - ������ ��� ������ ����� �� ������ ������) �� �������� ������� ������</summary>
      public abstract double CalcA(int area);

      /// <summary>get parent container (owner window) size in pixels</summary>
      public abstract Size CalcOwnerSize(Size sizeField, int area);

      /// <summary>������ ���� �� ������ ����� ��������� �� ������ direction</summary>
      public abstract Size GetDirectionSizeField();
      /// <summary>���-�� direction'��, ������� ����� ������ ��� �������</summary>
      public int GetDirectionCount() { Size s = GetDirectionSizeField(); return s.width*s.height; }

      /// <summary>���-�� ������� (��������)</summary>
      public abstract int getNeighborNumber();
      /// <summary>���-�� ������� � ������ ���������� ��������������</summary>
      public abstract int getNeighborNumber(int direction);
      /// <summary>�� ������� �����/������ ������� ������ (��������)</summary>
      public abstract int getVertexNumber();
      /// <summary>�� ������� �����/������ ������� ������ ���������� ��������������</summary>
      public abstract int getVertexNumber(int direction);
      /// <summary>������� ����� ������������ � ����� ����� (� �������)</summary>
      public abstract double getVertexIntersection(); 

      /// <summary>���� ���-�� ������� ������� ����, ������� ����� ������ ��� �������
      /// (����� �-��� BaseCell::getBackgroundFillColor() ��� � �����������)
      /// (�� ������ ������ ������� ������ ���� ��-���������...)</summary>
      public virtual int getMaxBackgroundFillModeValue() {
         return 18;
      }

      /// <summary>��� ��������� ������: ����������� ������ ����, �� �������� ����� ��������� ����, ��� ��� �� �������...</summary>
      public abstract Size sizeIcoField(bool smallSize);
   }

   private readonly BaseAttribute attr;
   public BaseAttribute Attr { get { return attr; } }

   //public bool bPresumeFlag { get; set; }

   //CellContext cellContext;
   protected Coord coord;
   /// <summary>����������� - '������ ����������' ������</summary>
   protected int direction;

   /// <summary>��������� � ������ ������� - ������� � ������� ��������� �����������/�����</summary>
   public abstract Rect getRcInner(int borderWidth);
   /// <summary>����� ������������� � ������� ������� ������ ������</summary>
   public Rect getRcOuter() {
      Rect rcOuter = region.getBounds();
      rcOuter.height++; rcOuter.width++; // ����� ��� repaint'� ��������� � ������� �������
      return rcOuter;
   }

   /// <summary>�������� ������ - � �������� �������� this</summary>
   private BaseCell[] neighbors;
   public BaseCell[] Neighbors { get { return neighbors; } }
   
   /// <summary>������ ��������� ����� �� ������� ������� ������</summary>
   protected Region region;

   public class StateCell {
      private readonly BaseCell owner;
      // { union
      private EState status; // _Open, _Close
      private EOpen  open;   // _Nil, _1, ... _21, _Mine
      private EClose close;  // _Unknown, _Clear, _Flag
      // } end union
      /// <summary>������? �� ������ � open! - ������ ����� ���� ������, �� ��� �� �������. ����� ������ ��� �-��� ����������</summary>
      public bool Down { get; set; }
      public void setStatus(EState status, ClickReportContext clickRepContext) {
         if (clickRepContext != null)
            if (status == EState._Open)
               if (this.open == EOpen._Nil)
                  clickRepContext.setOpenNil.Add(owner);
               else
                  clickRepContext.setOpen.Add(owner);
         this.status = status;
      }
      public EState Status { get { return status; } }
      public void CalcOpenState() {
          if (this.open == EOpen._Mine) return;
          // ���������� � ������� ����� ��� � ���������� ��������
          int count = 0;
          for (int i=0; i<owner.neighbors.Length; i++) {
             if (owner.neighbors[i] == null) continue; // ���������� �� �����?
             if (owner.neighbors[i].state.open == EOpen._Mine) count++;
          }
          this.open = (EOpen)Enum.GetValues(typeof(EOpen)).GetValue(count);
       }
       public bool SetMine() {
          if (owner.lockMine || (this.open == EOpen._Mine)) return false;
          this.open = EOpen._Mine;
          return true;
       }
       public EOpen Open { get { return this.open; } }
       public void setClose(EClose close, ClickReportContext clickRepContext) {
          if (clickRepContext != null)
             if ((    close == EClose._Flag) || // ���� ������������ ������
                (this.close == EClose._Flag))   // ���� ������ ������
             {
                clickRepContext.setFlag.Add(owner);
             }
          this.close = close;
       }
       public EClose Close { get { return this.close; } }

       public StateCell(BaseCell self) { owner = self; Reset(); }
       public void Reset() {
          status = EState._Close;
          open = EOpen._Nil;
          close = EClose._Clear;
          Down = false;
       }
   }
   private StateCell state;
   /// <summary>��������� ��������� ���� �� ������ ������</summary>
   private bool lockMine;

   public void LockNeighborMines() {
      lockMine = true;
      // ��������� ��������� ��� � �������,
      for (int i=0; i<neighbors.Length; i++) {
         if (neighbors[i] == null) continue; // ���������� �� �����?
         neighbors[i].lockMine = true;
      }
   }

   public StateCell State { get { return state; } }

   protected BaseCell(
         BaseAttribute attr,
         Coord coord,
         int iDirection)
   {
      this.attr = attr;
      this.coord = coord;
      this.direction = iDirection;
      this.region = new Region(attr.getVertexNumber(iDirection));
      this.neighbors = null;

      this.state = new StateCell(this);
      Reset();
      CalcRegion();
   }

   /// <summary>
   /// Coord[] neighborCoord = new Coord[attr.getNeighborNumber()];
   /// <br>... ������� ������ ���������� ���������� �������
   /// </summary>
   protected abstract Coord[] GetCoordsNeighbor();

   /// <summary>������� ����� ���� �������</summary>
   public interface IMatrixCells {
      /// <summary>������ ����</summary>
      Size SizeField { get; }

      /// <summary>������ � �������� ������</summary>
      BaseCell getCell(Coord coord);
   }
   /// <summary>��� this ���������� ������-�������, � ��������� ���������� �� ���������
   /// �������� ����� ��������� ������� ���� ��� ���� �������</summary>
   public void IdentifyNeighbors(IMatrixCells matrix) {
      // ������� ���������� �������� �����
      Coord[] neighborCoord = GetCoordsNeighbor();
      if (neighborCoord.Length != attr.getNeighborNumber())
         throw new Exception("neighborCoord.Length != GetNeighborNumber()");

      // �������� ��� ��� �� ������� �� �������
      for (int i=0; i<neighborCoord.Length; i++)
         if (neighborCoord[i] != Coord.INCORRECT_COORD)
            if ((neighborCoord[i].x >= matrix.SizeField.width) ||
               (neighborCoord[i].y >= matrix.SizeField.height) ||
               (neighborCoord[i].x < 0) ||
               (neighborCoord[i].y < 0))
            {
               neighborCoord[i] = Coord.INCORRECT_COORD;
            }
      // �� ����������� ������� ��������� �������� ��������-�����
      neighbors = new BaseCell[attr.getNeighborNumber()];
      for (int i=0; i<neighborCoord.Length; i++)
         if (neighborCoord[i] != Coord.INCORRECT_COORD)
            neighbors[i] = matrix.getCell(neighborCoord[i]);
   }

   public Coord getCoord() { return coord; }
   public int getDirection() { return direction; }
   /// <summary>���������� ������ ������ (� ��������)</summary>
   public Point getCenter() { return getRcInner(1).center(); }

   /// <summary>����������� �� ��� �������� ���������� ������</summary>
   public virtual bool PointInRegion(Point point) { return region.Contains(point); }

   public Region getRegion() { return region; }

   /// <summary>���������� ���������� ����� �� ������� ������� ������</summary>
   protected abstract void CalcRegion();

   public void Reset() {
      state.Reset();
      lockMine = false;
   }

   /// <summary>Index where border change color</summary>
   public abstract int getShiftPointBorderIndex();


   public LeftDownResult LButtonDown() {
      if (state.Close  == EClose._Flag) return null;
      if (state.Status == EState._Close) {
         state.Down = true;
         LeftDownResult result1 = new LeftDownResult();
         result1.needRepaint.Add(this);
         return result1;
      }

      LeftDownResult result = null;
      // ������ ��������� ��� ���������� �������
      if ((state.Status == EState._Open) && (state.Open != EOpen._Nil))
         for (int i=0; i<neighbors.Length; i++) {
            if (neighbors[i] == null) continue; // ���������� �� �����?
            if ((neighbors[i].state.Status == EState._Open) ||
               (neighbors[i].state.Close  == EClose._Flag)) continue;
            neighbors[i].state.Down = true;
            if (result == null)
               result = new LeftDownResult();
            result.needRepaint.Add(neighbors[i]);
         }
      return result;
   }
   public LeftUpResult LButtonUp(bool isMy, ClickReportContext clickRepContext) {
      LeftUpResult result = new LeftUpResult(0, 0, 0, false, false);

      if (state.Close == EClose._Flag) return result;
      // ��������� �� ������� ���������
      if ((state.Status == EState._Open) && (state.Open != EOpen._Nil))
         for (int i=0; i<neighbors.Length; i++) {
            if (neighbors[i] == null) continue; // ���������� �� �����?
            if ((neighbors[i].state.Status == EState._Open) ||
               (neighbors[i].state.Close  == EClose._Flag)) continue;
            neighbors[i].state.Down = false;
            result.addToRepaint(neighbors[i]);
         }
      // ������� �������� ������ �� ������� ������
      if (state.Status == EState._Close)
         if (!isMy) {
            state.Down = false;
            result.addToRepaint(this);
            return result;
         } else {
            result.countUnknown += (state.Close == EClose._Unknown) ? -1 : 0;
            result.countOpen++;
            state.setStatus(EState._Open, clickRepContext);
            state.Down = true;
            result.addToRepaint(this);
         }

      // ! � ���� ����� ������ ��� �������
      // ����������� ���-�� ������������� ������ ������ � �� �������� �����
      int countFlags = 0;
      int countClear = 0;
      if (state.Open != EOpen._Nil)
         for (int i=0; i<neighbors.Length; i++) {
            if (neighbors[i] == null) continue; // ���������� �� �����?
            if (neighbors[i].state.Status == EState._Open) continue;
            if (neighbors[i].state.Close  == EClose._Flag)
               countFlags++;
            else countClear++;
         }
      // ���������� ���������� �����
      if ((state.Open != EOpen._Nil) && ((countFlags+countClear) == state.Open.Ordinal()))
         for (int i=0; i<neighbors.Length; i++) {
            if (neighbors[i] == null) continue; // ���������� �� �����?
            if ((neighbors[i].state.Status == EState._Open) ||
               (neighbors[i].state.Close  == EClose._Flag)) continue;
            result.countUnknown += (neighbors[i].state.Close == EClose._Unknown) ? -1 : 0;
            result.countFlag++;
            neighbors[i].state.setClose(EClose._Flag, clickRepContext);
            result.addToRepaint(neighbors[i]);
         }
      if (!isMy) return result;
      // ������� ����������
      if ((countFlags+result.countFlag) == state.Open.Ordinal())
         for (int i=0; i<neighbors.Length; i++) {
            if (neighbors[i] == null) continue; // ���������� �� �����?
            if ((neighbors[i].state.Status == EState._Open) ||
               (neighbors[i].state.Close  == EClose._Flag)) continue;
            result.countUnknown += (neighbors[i].state.Close == EClose._Unknown) ? -1 : 0;
            result.countOpen++;
            neighbors[i].state.Down = true;
            neighbors[i].state.setStatus(EState._Open, clickRepContext);
            result.addToRepaint(neighbors[i]);
            if (neighbors[i].state.Open == EOpen._Nil) {
               LeftUpResult result2 = neighbors[i].LButtonUp(true, clickRepContext);
               result.countFlag    += result2.countFlag;
               result.countOpen    += result2.countOpen;
               result.countUnknown += result2.countUnknown;
               if (result.endGame) {
                  result.endGame = result2.endGame;
                  result.victory = result2.victory;
               }
               if (result2.needRepaint != null)
                  foreach (BaseCell cellToRepaint in result2.needRepaint)
                     result.addToRepaint(cellToRepaint);
            }
            if (neighbors[i].state.Open == EOpen._Mine) {
               result.endGame = true;
               result.victory = false;
               return result;
            }
         }
      if (state.Open == EOpen._Mine) {
         result.endGame = true;
         result.victory = false;
      }
      return result;
   }
   public RightDownReturn RButtonDown(EClose close, ClickReportContext clickRepContext) {
      RightDownReturn result = new RightDownReturn(0, 0);

      if ((state.Status == EState._Open) || state.Down) return result;
      switch (state.Close) {
      case EClose._Clear:
         switch (close) {
         case EClose._Flag:    result.countFlag    = +1;  break;
         case EClose._Unknown: result.countUnknown = +1;  break;
         default: break;
         }
         if (state.Close != close)
            state.setClose(close, clickRepContext);
         break;
      case EClose._Flag:
         switch (close) {
         case EClose._Unknown: result.countUnknown = +1;
                               result.countFlag    = -1; break;
         case EClose._Clear:   result.countFlag    = -1; break;
         default: break;
         }
         if (state.Close != close)
            state.setClose(close, clickRepContext);
         break;
      case EClose._Unknown:
         switch (close) {
         case EClose._Flag:    result.countFlag    = +1;
                               result.countUnknown = -1; break;
         case EClose._Clear:   result.countUnknown = -1; break;
         default: break;
         }
         if (state.Close != close)
            state.setClose(close, clickRepContext);
         break;
      }
      result.needRepaint = true;
      return result;
   }

   /// <summary>������� ���� ������� ����� � ����������� ��
   /// * ������ ������� ���� �����
   /// * ���������� ������
   /// * ����������� ������
   /// * ... - ��� ��������� �������� ����� </summary>
   public virtual Color getBackgroundFillColor(int fillMode, Color defaultColor, Func<int, Color> repositoryColor) {
      switch (fillMode) {
      default:
         System.Diagnostics.Debug.Assert(false,this.GetType()+".getBackgroundFillColor: fillMode="+fillMode+":  ������ �������� ��������� ��� ����� ������!");
         //break;// !!! ��� break'�
         goto case 0;
      case 0:
         Color clr = defaultColor;
         { // ��� Down � �������� ��������� ����� ��� ���� � ����-���� ������...
            float perc;
            bool failGame = false;

            if (state.Status == EState._Close)
               if (state.Down)
                  perc = .15f;
               else
                  perc = 0.0f;
            else {
               failGame = (state.Open == EOpen._Mine) && state.Down;
               perc = state.Down ? .25f : 0.0f;
            }
   
            if (failGame)
               return Color.RED;

            byte _r = (byte) (clr.R - clr.R * perc);
            byte _g = (byte) (clr.G - clr.G * perc);
            byte _b = (byte) (clr.B - clr.B * perc);
            return new Color(_r,_g,_b);
         }
      case 1:
         return repositoryColor(getDirection());
      case 2:
         {
            // ���������� ������ i-��� ������ c ����� div
            int i = 2;
            int div = 5;
            int tmp1 = getCoord().x % div;
            int tmp2 = (getCoord().y-tmp1) % div;
            return repositoryColor((((tmp1 + tmp2) % div) == i) ? 0 : 1 );
         }
      case 3:
         {
            // �������
            int i = 3;
            int div = 4;
            int tmp1 = getCoord().x % div;
            int tmp2 = (getCoord().y+tmp1) % div;
            return repositoryColor((((tmp1 + tmp2) % div) == i) ? 0 : 1 );
         }
      case 4:
         {
            // ��� ����
            int i = 3;
            int div = 5;
            int tmp1 = getCoord().x % div;
            int tmp2 = (getCoord().y+tmp1) % div;
            return repositoryColor((((tmp1 + tmp2) % div) == i) ? 0 : 1);
         }
      case 5:
         {
            // �����
            int div = 15;
            int tmp1 = getCoord().x % div;
            int tmp2 = (getCoord().y+tmp1) % div;
            return repositoryColor((tmp1 + tmp2) % div);
         }
      case 6:
         {
            int div = 4;
            return repositoryColor(((getCoord().x % div + getCoord().y % div) == div) ? 0 : 1);
         }
      case 7: case 8: case 9:
         return repositoryColor(getCoord().x % (-5 + fillMode));
      case 10: case 11: case 12:
         return repositoryColor(getCoord().y % (-8 + fillMode));
      case 13: case 14: case 15:
      case 16: case 17: case 18:
         return repositoryColor(getCoord().x % (-fillMode) - fillMode + getCoord().y % (+fillMode));
      }
   }

   public void OnPropertyChanged(object sender, PropertyChangedEventArgs e) {
      if ("Area".Equals(e.PropertyName))
         CalcRegion();
   }
}
}