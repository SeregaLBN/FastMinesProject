UNIT UCustomSkill;

INTERFACE

USES
   Windows, Forms, Classes, Controls, StdCtrls, ComCtrls, Buttons, ExtCtrls, Spin,
   Graphics, Menus, UMines;

TYPE
   Tfrm_CustomSkill = Class(TForm)
      Panel1             : TPanel;
      Panel2             : TPanel;
      grpbx_BoxForOptions: TGroupBox;
      spnedt_X           : TSpinEdit;
      spnedt_Y           : TSpinEdit;
      spnedt_MaxMines    : TSpinEdit;
      spnedt_MaxCraters  : TSpinEdit;
      spnedt_MaxMoving   : TSpinEdit;
      spnedt_TimeMoving  : TSpinEdit;
      chckbx_FullScreen  : TCheckBox;
      lbl_X              : TLabel;
      lbl_Y              : TLabel;
      lbl_MaxMines       : TLabel;
      lbl_MaxCraters     : TLabel;
      lbl_MaxMoving      : TLabel;
      lbl_TimeMoving     : TLabel;
      lbl_Second         : TLabel;
      lbl_WarningMines   : TLabel;
      lbl_WarningCraters : TLabel;
      lbl_WarningMoving  : TLabel;
      lbl_Tilda          : TLabel;
      Timer              : TTimer;
      btbttn_Ok          : TBitBtn;
      btbttn_Cancel      : TBitBtn;
      pppmn_Skill        : TPopupMenu;
      mnitm_Beginner     : TMenuItem;
      mnitm_Amateur      : TMenuItem;
      mnitm_Professional : TMenuItem;
      mnitm_Crazy        : TMenuItem;
      procedure FormActivate           (Sender: TObject);
      procedure spnedt_XChange         (Sender: TObject);
      procedure spnedt_YChange         (Sender: TObject);
      procedure spnedt_MaxMinesChange  (Sender: TObject);
      procedure spnedt_MaxCratersChange(Sender: TObject);
      procedure spnedt_MaxMovingChange (Sender: TObject);
      procedure TimerTimer             (Sender: TObject);
      procedure btbttn_OkClick         (Sender: TObject);
      procedure chckbx_FullScreenClick (Sender: TObject);
      procedure spnedt_KeyDown(Sender: TObject; var Key: Word; Shift: TShiftState);
      procedure spnedt_XMouseUp(Sender: TObject; Button: TMouseButton; Shift: TShiftState; X, Y: Integer);
      procedure spnedt_YMouseUp(Sender: TObject; Button: TMouseButton; Shift: TShiftState; X, Y: Integer);
      procedure spnedt_MaxMinesMouseUp(Sender: TObject; Button: TMouseButton; Shift: TShiftState; X, Y: Integer);
      procedure spnedt_MaxCratersMouseUp(Sender: TObject; Button: TMouseButton; Shift: TShiftState; X, Y: Integer);
      procedure spnedt_MaxMovingMouseUp(Sender: TObject; Button: TMouseButton; Shift: TShiftState; X, Y: Integer);
      procedure spnedt_TimeMovingMouseUp(Sender: TObject; Button: TMouseButton; Shift: TShiftState; X, Y: Integer);
      procedure mnitm_BeginnerClick(Sender: TObject);
      procedure mnitm_Click(skill: TSubrangeSkillLevel);
      procedure mnitm_AmateurClick(Sender: TObject);
      procedure mnitm_ProfessionalClick(Sender: TObject);
      procedure mnitm_CrazyClick(Sender: TObject);
   private
      { Private declarations }
   public
      { Public declarations }
   End;

VAR
  frm_CustomSkill: Tfrm_CustomSkill;

IMPLEMENTATION

TYPE
   TSelected = (edit_x, edit_y, edit_mines, edit_crater, edit_moving, edit_time);

VAR
   selected: TSelected;

{$R *.DFM}

Procedure Tfrm_CustomSkill.btbttn_OkClick(Sender: TObject);
Begin
   //Exit;
   frm_Mines.DestroyField(Sender);
   gSettings.X         := spnedt_X.Value;
   gSettings.Y         := spnedt_Y.Value;
   gSettings.maxMines  := spnedt_MaxMines  .Value;
   gSettings.maxCraters:= spnedt_MaxCraters.Value;
   gSettings.maxMoving := spnedt_MaxMoving .Value;
   gSettings.timeMoving:= spnedt_TimeMoving.Value;
   case UMines.CurrentSkillLevel of
      SkillLevelBeginner    : frm_Mines.mnitm_FileBeginner    .Checked:= True;
      SkillLevelAmateur     : frm_Mines.mnitm_FileAmateur     .Checked:= True;
      SkillLevelProfessional: frm_Mines.mnitm_FileProfessional.Checked:= True;
      SkillLevelCrazy       : frm_Mines.mnitm_FileCrazy       .Checked:= True;
      else                    frm_Mines.mnitm_FileCustom      .Checked:= True;
   end;
   frm_Mines.CreateField(Sender);
End;

Procedure Tfrm_CustomSkill.FormActivate(Sender: TObject);
Var winX, winY: Integer;
Begin
   winX:= GetSystemMetrics(SM_CXSCREEN);
   winY:= GetSystemMetrics(SM_CYSCREEN);
   Self.Left:= winX div 2 - Self.Width  div 2;
   Self.Top := winY div 2 - Self.Height div 2;
   spnedt_X.MaxValue:= (winX-2*GetSystemMetrics(SM_CXFRAME)) div UMines.gSettings.SizeCell;
   spnedt_Y.MaxValue:= (winY-GetSystemMetrics(SM_CYCAPTION)
                            -GetSystemMetrics(SM_CYMENU)
                            -GetSystemMetrics(SM_CYFRAME)*2
                            -frm_Mines.pnl_Top.Height
                            -frm_Mines.prgssbr_TimeMoving.Height
                        ) div UMines.gSettings.SizeCell;
   chckbx_FullScreen.Checked:= False;
   spnedt_X.Enabled:= True;
   spnedt_Y.Enabled:= True;
   spnedt_X         .Value:= UMines.gSettings.X;
   spnedt_Y         .Value:= UMines.gSettings.Y;
   spnedt_MaxMines  .Value:= UMines.gSettings.maxMines  ;
   spnedt_MaxCraters.Value:= UMines.gSettings.maxCraters;
   spnedt_MaxMoving .Value:= UMines.gSettings.maxMoving ;
   spnedt_TimeMoving.Value:= UMines.gSettings.timeMoving;
   spnedt_X.SetFocus;
End;

Procedure Tfrm_CustomSkill.spnedt_XChange(Sender: TObject);
Begin
   if(spnedt_X.Value < spnedt_X.MinValue) then
      spnedt_X.Value:= spnedt_X.MinValue;
   if(spnedt_X.Value > spnedt_X.MaxValue) then
      spnedt_X.Value:= spnedt_X.MaxValue;
   if(spnedt_MaxMines.Value > spnedt_X.Value*spnedt_Y.Value - 9-Integer(gSettings.useRandom)) then
      spnedt_MaxMinesChange  (Sender);
End;

Procedure Tfrm_CustomSkill.spnedt_YChange(Sender: TObject);
Begin
   if(spnedt_Y.Value < spnedt_Y.MinValue) then
      spnedt_Y.Value:= spnedt_Y.MinValue;
   if(spnedt_Y.Value > spnedt_Y.MaxValue) then
      spnedt_Y.Value:= spnedt_Y.MaxValue;
   if(spnedt_MaxMines.Value > spnedt_X.Value*spnedt_Y.Value - 9) then
      spnedt_MaxMinesChange  (Sender);
End;

Procedure Tfrm_CustomSkill.spnedt_MaxMinesChange(Sender: TObject);
Begin
   if(spnedt_MaxMines.Value < spnedt_MaxMines.MinValue) then
      spnedt_MaxMines.Value:= spnedt_MaxMines.MinValue;
   if(spnedt_MaxMines.Value > spnedt_X.Value*spnedt_Y.Value - 9)then begin
      lbl_WarningMines.Font.Color:= clRed;
      spnedt_MaxMines.Value:= spnedt_X.Value*spnedt_Y.Value - 9;
   end;
   if(spnedt_MaxCraters.Value > spnedt_X.Value*spnedt_Y.Value-spnedt_MaxMines.Value-1) then
      spnedt_MaxCratersChange(Sender);
   if(spnedt_MaxMoving.Value > spnedt_MaxMines.Value) then
      spnedt_MaxMovingChange(Sender);
End;

Procedure Tfrm_CustomSkill.spnedt_MaxCratersChange(Sender: TObject);
Begin
   if(spnedt_MaxCraters.Value < spnedt_MaxCraters.MinValue) then
      spnedt_MaxCraters.Value:= spnedt_MaxCraters.MinValue;
   if(spnedt_MaxCraters.Value > spnedt_X.Value*spnedt_Y.Value-spnedt_MaxMines.Value-1) then begin
      lbl_WarningCraters.Font.Color:= clRed;
      spnedt_MaxCraters.Value:= spnedt_X.Value*spnedt_Y.Value-spnedt_MaxMines.Value-1;
   end;
End;

Procedure Tfrm_CustomSkill.spnedt_MaxMovingChange(Sender: TObject);
Begin
   if(spnedt_MaxMoving.Value < spnedt_MaxMoving.MinValue) then
      spnedt_MaxMoving.Value:= spnedt_MaxMoving.MinValue;
   if(spnedt_MaxMoving.Value > spnedt_MaxMines.Value) then begin
      lbl_WarningMoving.Font.Color:= clRed;
      spnedt_MaxMoving.Value:= spnedt_MaxMines.Value;
   end;
End;

Procedure Tfrm_CustomSkill.TimerTimer(Sender: TObject);
Begin
   lbl_WarningMines  .Font.Color:= clBlack;
   lbl_WarningCraters.Font.Color:= clBlack;
   lbl_WarningMoving .Font.Color:= clBlack;
End;

Procedure Tfrm_CustomSkill.chckbx_FullScreenClick(Sender: TObject);
Begin
   if (chckbx_FullScreen.Checked) then begin
      spnedt_X.Value:= spnedt_X.MaxValue;
      spnedt_Y.Value:= spnedt_Y.MaxValue;
      spnedt_X.Enabled:= False;
      spnedt_Y.Enabled:= False;
   end else begin
      spnedt_X.Enabled:= True;
      spnedt_Y.Enabled:= True;
   end;
End;

procedure Tfrm_CustomSkill.spnedt_KeyDown(Sender: TObject; var Key: Word;
  Shift: TShiftState);
begin
   if (key = VK_RETURN) then begin
      btbttn_OkClick(Sender);
      frm_CustomSkill.Close;
   end;
   if (key = VK_ESCAPE) then begin
      frm_CustomSkill.Close;
   end;
end;

Procedure Tfrm_CustomSkill.spnedt_XMouseUp(Sender: TObject;
  Button: TMouseButton; Shift: TShiftState; X, Y: Integer);
Begin
   selected:= edit_x;
End;

Procedure Tfrm_CustomSkill.spnedt_YMouseUp(Sender: TObject;
  Button: TMouseButton; Shift: TShiftState; X, Y: Integer);
Begin
   selected:= edit_Y;
End;

Procedure Tfrm_CustomSkill.spnedt_MaxMinesMouseUp(Sender: TObject;
  Button: TMouseButton; Shift: TShiftState; X, Y: Integer);
Begin
   selected:= edit_mines;
End;

Procedure Tfrm_CustomSkill.spnedt_MaxCratersMouseUp(Sender: TObject;
  Button: TMouseButton; Shift: TShiftState; X, Y: Integer);
Begin
   selected:= edit_crater;
End;

Procedure Tfrm_CustomSkill.spnedt_MaxMovingMouseUp(Sender: TObject;
  Button: TMouseButton; Shift: TShiftState; X, Y: Integer);
Begin
   selected:= edit_moving;
End;

Procedure Tfrm_CustomSkill.spnedt_TimeMovingMouseUp(Sender: TObject;
  Button: TMouseButton; Shift: TShiftState; X, Y: Integer);
Begin
   selected:= edit_time;
End;

Procedure Tfrm_CustomSkill.mnitm_Click(skill: TSubrangeSkillLevel);
Begin
   case selected of
      edit_x     : spnedt_X         .Value:= UMines.CSizeX[skill];
      edit_y     : spnedt_Y         .Value:= UMines.CSizeY[skill];
      edit_mines : spnedt_MaxMines  .Value:= spnedt_X.Value*spnedt_Y.Value * UMines.CMines  [skill] div 100;
      edit_crater: spnedt_MaxCraters.Value:= spnedt_X.Value*spnedt_Y.Value * UMines.CCraters[skill] div 100;
      edit_moving: spnedt_MaxMoving .Value:= spnedt_X.Value*spnedt_Y.Value * UMines.CMoving [skill] div 100;
      edit_time  : spnedt_TimeMoving.Value:= UMines.CMovingTime[skill];
   end;
End;

Procedure Tfrm_CustomSkill.mnitm_BeginnerClick(Sender: TObject);
Begin
   mnitm_Click(Ord(SkillLevelBeginner));
End;

procedure Tfrm_CustomSkill.mnitm_AmateurClick(Sender: TObject);
Begin
   mnitm_Click(Ord(SkillLevelAmateur));
End;

procedure Tfrm_CustomSkill.mnitm_ProfessionalClick(Sender: TObject);
Begin
   mnitm_Click(Ord(SkillLevelProfessional));
End;

procedure Tfrm_CustomSkill.mnitm_CrazyClick(Sender: TObject);
Begin
   mnitm_Click(Ord(SkillLevelCrazy));
End;

END.
