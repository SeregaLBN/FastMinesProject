{$DEFINE USE_PICTURES}
UNIT UOtherOptions;

INTERFACE

USES
  Windows, Forms, Classes, Controls, StdCtrls, Spin, Buttons, ExtCtrls;

TYPE
   Tfrm_OtherOptions = Class(TForm)
      Panel1         : TPanel;
      Panel2         : TPanel;
      Panel3         : TPanel;
      btbttn_Ok      : TBitBtn;
      btbttn_Cancel  : TBitBtn;
      bttn_Text      : TButton;
      bttn_Mine      : TButton;
      bttn_Flag      : TButton;
      bttn_Crater    : TButton;
      bttn_Pause     : TButton;
      bttn_Background: TButton;
      lbl_SizeCell   : TLabel;
      spnedt_SizeCell: TSpinEdit;
      rdbttn_MaxSize : TRadioButton;
      rdbttn_MinSize : TRadioButton;
      img_Mine       : TImage;
      img_Flag       : TImage;
      img_Crater     : TImage;
      img_Pause      : TImage;
      procedure FormCreate            (Sender: TObject);
      procedure FormActivate          (Sender: TObject);
      procedure btbttn_OkClick        (Sender: TObject);
      procedure rdbttn_MinSizeClick   (Sender: TObject);
      procedure rdbttn_MaxSizeClick   (Sender: TObject);
      procedure spnedt_SizeCellChange (Sender: TObject);
      procedure spnedt_SizeCellKeyDown(Sender: TObject; var Key: Word; Shift: TShiftState);
      procedure bttn_TextClick        (Sender: TObject);
      procedure bttn_MineClick        (Sender: TObject);
      procedure bttn_CraterClick      (Sender: TObject);
      procedure bttn_FlagClick        (Sender: TObject);
      procedure bttn_PauseClick       (Sender: TObject);
      procedure bttn_BackgroundClick  (Sender: TObject);
   private
      { Private declarations }
   public
      { Public declarations }
   End;

VAR
   frm_OtherOptions: Tfrm_OtherOptions;
   path: record
      pause, mine, flag, crater: String[255];
   end;
   border: record
      text, mine, flag, crater: Integer; // %
   end;
   transparent: record
      pause, mine, flag, crater: Boolean;
   end;

IMPLEMENTATION

USES UMines, UChangeObj;
{$R *.DFM}

Procedure Tfrm_OtherOptions.FormCreate(Sender: TObject);
Var winX, winY: Integer;
Begin
   winX:= GetSystemMetrics(SM_CXSCREEN);
   winY:= GetSystemMetrics(SM_CYSCREEN);
   Self.Left:= winX div 2 - Self.Width  div 2;
   Self.Top := winY div 2 - Self.Height div 2;
End;

Procedure Tfrm_OtherOptions.FormActivate(Sender: TObject);
Var winX, winY: Integer;
Begin
   winX:= GetSystemMetrics(SM_CXSCREEN);
   winY:= GetSystemMetrics(SM_CYSCREEN);
   spnedt_SizeCell.Value:= gSettings.SizeCell;
   spnedt_SizeCell.MinValue:= CMinSizeCell;
   if (((winX-2*GetSystemMetrics(SM_CXFRAME)) div UMines.gSettings.X) <
       ((winY-GetSystemMetrics(SM_CYCAPTION)
             -GetSystemMetrics(SM_CYMENU)
             -GetSystemMetrics(SM_CYFRAME)*2
             -frm_Mines.pnl_Top.Height
             -frm_Mines.prgssbr_TimeMoving.Height
        ) div UMines.gSettings.Y))
   then spnedt_SizeCell.MaxValue:= (winX-2*GetSystemMetrics(SM_CXFRAME)) div UMines.gSettings.X
   else spnedt_SizeCell.MaxValue:= (winY-GetSystemMetrics(SM_CYCAPTION)
                                        -GetSystemMetrics(SM_CYMENU)
                                        -GetSystemMetrics(SM_CYFRAME)*2
                                        -frm_Mines.pnl_Top.Height
                                        -frm_Mines.prgssbr_TimeMoving.Height
                                   ) div UMines.gSettings.Y;
   spnedt_SizeCellChange(Sender);

   border.text          := UMines.gSettings.border.text;
   bttn_Text.Font.Name  := UMines.img_Cells.Canvas.Font.Name;
   bttn_Text.Font.Style := UMines.img_Cells.Canvas.Font.Style;
   bttn_Text.Font.Height:= bttn_Text.Height;

   path       .mine:= UMines.gSettings.path       .mine;
   border     .mine:= UMines.gSettings.border     .mine;
   transparent.mine:= UMines.gSettings.transparent.mine;
   img_Mine.Picture:= UMines.pictures.Mine;
   img_Mine.Transparent:= UMines.gSettings.transparent.mine;

   path       .flag:= UMines.gSettings.path       .flag;
   border     .flag:= UMines.gSettings.border     .flag;
   transparent.flag:= UMines.gSettings.transparent.flag;
   img_Flag.Picture:= UMines.pictures.Flag;
   img_Flag.Transparent:= UMines.gSettings.transparent.flag;

   path       .crater:= UMines.gSettings.path       .crater;
   border     .crater:= UMines.gSettings.border     .crater;
   transparent.crater:= UMines.gSettings.transparent.crater;
   img_Crater.Picture:= UMines.pictures.Crater;
   img_Crater.Transparent:= UMines.gSettings.transparent.crater;

   path       .pause:= UMines.gSettings.path       .pause;
   transparent.pause:= UMines.gSettings.transparent.pause;
   img_Pause.Picture:= UMines.frm_Mines.img_Pause.Picture;
   img_Pause.Transparent:= UMines.gSettings.transparent.pause;

   bttn_Background.Font.Color:= UMines.gSettings.background;

   spnedt_SizeCell.SetFocus;
End;

Procedure Tfrm_OtherOptions.btbttn_OkClick(Sender: TObject);
Begin
   UMines.gSettings.SizeCell:= spnedt_SizeCell.Value;

   UMines.gSettings.border.text      := border.text;
   UMines.gSettings.font.Name        := bttn_Text.Font.Name;
   UMines.gSettings.font.Style       := bttn_Text.Font.Style;
   UMines.img_Cells.Canvas.Font.Name := bttn_Text.Font.Name;
   UMines.img_Cells.Canvas.Font.Style:= bttn_Text.Font.Style;

   UMines.gSettings.path       .mine       := path       .mine;
   UMines.gSettings.border     .mine       := border     .mine;
   UMines.gSettings.transparent.mine       := transparent.mine;
   UMines.pictures.Mine                    := img_Mine.Picture;
   UMines.pictures.Mine.Graphic.Transparent:= transparent.mine;
   {
   if (UMines.pictures.Mine.Graphic.Transparent <> transparent.mine) then begin
      MessageBox( frm_OtherOptions.Handle,
                  'You must restart program',
                  'Apply Transparent Mine',
                  MB_OK or MB_ICONINFORMATION);
   end;
   {}
   UMines.gSettings.path       .flag       := path       .flag;
   UMines.gSettings.border     .flag       := border     .flag;
   UMines.gSettings.transparent.flag       := transparent.flag;
   UMines.pictures.Flag                    := img_Flag.Picture;
   UMines.pictures.Flag.Graphic.Transparent:= transparent.flag;
   {
   if (UMines.pictures.Flag.Graphic.Transparent <> transparent.flag) then begin
      MessageBox( frm_OtherOptions.Handle,
                  'You must restart program',
                  'Apply Transparent Flag',
                  MB_OK or MB_ICONINFORMATION);
   end;
   {}
   UMines.gSettings.path       .crater       := path       .crater;
   UMines.gSettings.border     .crater       := border     .crater;
   UMines.gSettings.transparent.crater       := transparent.crater;
   UMines.pictures.Crater                    := img_Crater.Picture;
   UMines.pictures.Crater.Graphic.Transparent:= transparent.crater;
   {
   if (UMines.pictures.Crater.Graphic.Transparent <> transparent.crater) then begin
      MessageBox( frm_OtherOptions.Handle,
                  'You must restart program',
                  'Apply Transparent Crater',
                  MB_OK or MB_ICONINFORMATION);
   end;
   {}
   UMines.gSettings.path       .pause    := path       .pause;
   UMines.gSettings.transparent.pause    := transparent.pause;
   UMines.frm_Mines.img_Pause.Picture    := img_Pause.Picture;
   UMines.frm_Mines.img_Pause.Transparent:= transparent.pause;
   {
   if (UMines.frm_Mines.img_Pause.Transparent <> transparent.pause) then begin
      MessageBox( frm_OtherOptions.Handle,
                  'You must restart program',
                  'Apply Transparent Pause',
                  MB_OK or MB_ICONINFORMATION);
   end;
   {}
   UMines.gSettings.background        := bttn_Background.Font.Color;
   UMines.img_Cells.Canvas.Brush.Color:= bttn_Background.Font.Color;
End;

Procedure Tfrm_OtherOptions.rdbttn_MinSizeClick(Sender: TObject);
Begin
   spnedt_SizeCell.Value:= CMinSizeCell;
End;

Procedure Tfrm_OtherOptions.rdbttn_MaxSizeClick(Sender: TObject);
Begin
   spnedt_SizeCell.Value:= spnedt_SizeCell.MaxValue;
End;

Procedure Tfrm_OtherOptions.spnedt_SizeCellChange(Sender: TObject);
Begin
   rdbttn_MinSize.Checked:= (spnedt_SizeCell.Value = CMinSizeCell);
   rdbttn_MaxSize.Checked:= (spnedt_SizeCell.Value = spnedt_SizeCell.MaxValue);
End;

Procedure Tfrm_OtherOptions.spnedt_SizeCellKeyDown(Sender: TObject;
  var Key: Word; Shift: TShiftState);
Begin
   if (Key = VK_RETURN) then begin
      btbttn_OkClick(Sender);
      Self.Close;
   end;
   if (Key = VK_ESCAPE) then begin
      Self.Close;
   end;
End;

Procedure Tfrm_OtherOptions.bttn_TextClick(Sender: TObject);
Begin
   UChangeObj.changeObject:= UChangeObj.changeText;
   UChangeObj.frm_ChangeObject.ShowModal;
   if (UChangeObj.actual) then begin
      border.text          := UChangeObj.frm_ChangeObject.spnedt_Border.Value;
      bttn_Text.Font.Name  := UChangeObj.frm_ChangeObject.pnl_ChangedObject.Font.Name;
      bttn_Text.Font.Style := UChangeObj.frm_ChangeObject.pnl_ChangedObject.Font.Style;
      bttn_Text.Font.Height:= bttn_Text.Height;
   end;
End;

Procedure Tfrm_OtherOptions.bttn_MineClick(Sender: TObject);
Begin
{$IFDEF USE_PICTURES}
   UChangeObj.changeObject:= UChangeObj.changeMine;
   UChangeObj.frm_ChangeObject.ShowModal;
   if (UChangeObj.actual) then begin
      path       .mine    := UChangeObj.path.mine;
      border     .mine    := UChangeObj.frm_ChangeObject.spnedt_Border.Value;
      transparent.mine    := UChangeObj.frm_ChangeObject.chckbx_Transparent.Checked;
      img_Mine.Transparent:= UChangeObj.frm_ChangeObject.chckbx_Transparent.Checked;
      img_Mine.Picture    := UChangeObj.frm_ChangeObject.img_ChangedObject.Picture;
   end;
{$ENDIF}
End;

Procedure Tfrm_OtherOptions.bttn_FlagClick(Sender: TObject);
Begin
{$IFDEF USE_PICTURES}
   UChangeObj.changeObject:= UChangeObj.changeFlag;
   UChangeObj.frm_ChangeObject.ShowModal;
   if (UChangeObj.actual) then begin
      path       .flag    := UChangeObj.path.flag;
      border     .flag    := UChangeObj.frm_ChangeObject.spnedt_Border.Value;
      transparent.flag    := UChangeObj.frm_ChangeObject.chckbx_Transparent.Checked;
      img_Flag.Transparent:= UChangeObj.frm_ChangeObject.chckbx_Transparent.Checked;
      img_Flag.Picture    := UChangeObj.frm_ChangeObject.img_ChangedObject.Picture;
   end;
{$ENDIF}
End;

Procedure Tfrm_OtherOptions.bttn_CraterClick(Sender: TObject);
Begin
{$IFDEF USE_PICTURES}
   UChangeObj.changeObject:= UChangeObj.changeCrater;
   UChangeObj.frm_ChangeObject.ShowModal;
   if (UChangeObj.actual) then begin
      path  .crater         := UChangeObj.path.crater;
      border.crater         := UChangeObj.frm_ChangeObject.spnedt_Border.Value;
      transparent.crater    := UChangeObj.frm_ChangeObject.chckbx_Transparent.Checked;
      img_Crater.Transparent:= UChangeObj.frm_ChangeObject.chckbx_Transparent.Checked;
      img_Crater.Picture    := UChangeObj.frm_ChangeObject.img_ChangedObject.Picture;
   end;
{$ENDIF}
End;

Procedure Tfrm_OtherOptions.bttn_PauseClick(Sender: TObject);
Begin
{$IFDEF USE_PICTURES}
   UChangeObj.changeObject:= UChangeObj.changePause;
   UChangeObj.frm_ChangeObject.ShowModal;
   if (UChangeObj.actual) then begin
      path       .pause    := UChangeObj.path.pause;
      transparent.pause    := UChangeObj.frm_ChangeObject.chckbx_Transparent.Checked;
      img_Pause.Transparent:= UChangeObj.frm_ChangeObject.chckbx_Transparent.Checked;
      img_Pause.Picture    := UChangeObj.frm_ChangeObject.img_ChangedObject.Picture;
   end;
{$ENDIF}
End;

Procedure Tfrm_OtherOptions.bttn_BackgroundClick(Sender: TObject);
Begin
   UChangeObj.changeObject:= UChangeObj.changeBackground;
   UChangeObj.frm_ChangeObject.ShowModal;
   if (UChangeObj.actual) then begin
      bttn_Background.Font.Color:= UChangeObj.frm_ChangeObject.ColorDialog.Color;
   end;
End;

END.
