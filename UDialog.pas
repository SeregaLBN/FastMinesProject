UNIT UDialog;

INTERFACE

USES Windows, Forms, StdCtrls, Classes, Controls;

TYPE
   Tfrm_Dialog = Class(TForm)
      bttn_Ok          : TButton;
      cmbbx_PlayersList: TComboBox;
      lbl_Players      : TLabel;
      procedure FormCreate  (Sender: TObject);
      procedure FormActivate(Sender: TObject);
      procedure bttn_OkClick(Sender: TObject);
      procedure cmbbx_KeyDown(Sender: TObject; var Key: Word; Shift: TShiftState);
      procedure FormClose(Sender: TObject; var Action: TCloseAction);
   private
      { Private declarations }
   public
      { Public declarations }
   End;

VAR
  frm_Dialog: Tfrm_Dialog;

IMPLEMENTATION

USES UMines, UStatistics;

{$R *.DFM}

Procedure Tfrm_Dialog.FormCreate(Sender: TObject);
Var winX, winY: Integer;
Begin
   winX:= GetSystemMetrics(SM_CXSCREEN);
   winY:= GetSystemMetrics(SM_CYSCREEN);
   Self.Left:= winX div 2 - Self.Width  div 2;
   Self.Top := winY div 2 - Self.Height div 2;
End;

Procedure Tfrm_Dialog.FormActivate(Sender: TObject);
Var i: Integer;
Begin
   UStatistics.frm_Statistics.LoadFile;
   cmbbx_PlayersList.Items.Clear;
   if (UStatistics.Statistics <> Nil) then begin
      for i:= 0 to High(UStatistics.Statistics) do begin
         cmbbx_PlayersList.Items.Add(UStatistics.Statistics[i].Name);
         if (UStatistics.Statistics[i].Name = UMines.gSettings.currPlayer) then
            cmbbx_PlayersList.ItemIndex:= i;
      end;
   end;
End;

Procedure Tfrm_Dialog.bttn_OkClick(Sender: TObject);
Var i: Integer;
    SttstcSubRecord: TSttstcSubRecord;
Begin
   if (cmbbx_PlayersList.Text = '') then begin
      Windows.MessageBox(Self.Handle, 'Please, input or select your Name', 'Error', MB_OK or MB_ICONINFORMATION);
      cmbbx_PlayersList.SetFocus;
      Exit;
   end;
   if (UStatistics.Statistics <> Nil) then begin
      for i:= 0 to High(UStatistics.Statistics) do begin
         if (UStatistics.Statistics[i].Name = cmbbx_PlayersList.Text) then
            Break;
      end;
      // add new Player
      SttstcSubRecord.GameBegin  := 0;
      SttstcSubRecord.GameComlete:= 0;
      SttstcSubRecord.OpenField  := 0;
      SttstcSubRecord.PlayTime   := 0;
      SttstcSubRecord.ClickCount := 0;
      UMines.gSettings.currPlayer:= cmbbx_PlayersList.Text;
      frm_Statistics.InsertRecord( frm_Statistics.currentPage(True), CurrentSkillLevel, SttstcSubRecord );
      frm_Statistics.SaveFile;
   end else begin
      // add first Player
      SttstcSubRecord.GameBegin  := 0;
      SttstcSubRecord.GameComlete:= 0;
      SttstcSubRecord.OpenField  := 0;
      SttstcSubRecord.PlayTime   := 0;
      SttstcSubRecord.ClickCount := 0;
      UMines.gSettings.currPlayer:= cmbbx_PlayersList.Text;
      frm_Statistics.InsertRecord( frm_Statistics.currentPage(True), CurrentSkillLevel, SttstcSubRecord );
      frm_Statistics.SaveFile;
   end;
   //ShowWindow( UDialog.frm_Dialog.Handle, SW_HIDE);
   Self.Close;
   UMines.frm_Mines.Caption:= UMines.CFormCaption + ' - ' + UMines.gSettings.currPlayer;
End;

Procedure Tfrm_Dialog.cmbbx_KeyDown(Sender: TObject;
  var Key: Word; Shift: TShiftState);
Begin
   if (Key = VK_ESCAPE) then begin
      Self.bttn_OkClick(Sender);
   end;
End;

Procedure Tfrm_Dialog.FormClose(Sender: TObject; var Action: TCloseAction);
Begin
   Self.bttn_OkClick(Sender);
End;

END.

