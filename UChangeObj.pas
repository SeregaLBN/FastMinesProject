UNIT UChangeObj;

INTERFACE

USES
  Windows, Messages, SysUtils, Classes, Graphics, Controls, Forms, Dialogs,
  StdCtrls, Buttons, ExtCtrls, Spin, ExtDlgs;

TYPE
   Tfrm_ChangeObject = Class(TForm)
      pnl_ChangedObject : TPanel;
      img_ChangedObject : TImage;
      btbttn_OkClick    : TBitBtn;
      btbttn_Cancel     : TBitBtn;
      bttn_Change       : TButton;
      lbl_BorderWidth   : TLabel;
      spnedt_Border     : TSpinEdit;
      chckbx_Default    : TCheckBox;
      chckbx_Transparent: TCheckBox;
      FontDialog        : TFontDialog;
      ColorDialog       : TColorDialog;
      OpenPictureDialog : TOpenPictureDialog;
      procedure FormCreate             (Sender: TObject);
      procedure FormActivate           (Sender: TObject);
      procedure btbttn_OkClickClick    (Sender: TObject);
      procedure spnedt_BorderChange    (Sender: TObject);
      procedure chckbx_DefaultClick    (Sender: TObject);
      procedure bttn_ChangeClick       (Sender: TObject);
      procedure spnedt_BorderKeyDown   (Sender: TObject; var Key: Word; Shift: TShiftState);
      procedure chckbx_TransparentClick(Sender: TObject);
   private
      { Private declarations }
   public
      { Public declarations }
   End;

TYPE
   TChangeObject = (changeText, changeMine, changeFlag, changeCrater, changeBackground, changePause);

VAR
   frm_ChangeObject: Tfrm_ChangeObject;
   changeObject: TChangeObject;
   actual: Boolean;
   path: record
      mine, flag, crater, pause: String[255];
   end;

IMPLEMENTATION

USES UMines, UOtherOptions;
{$R *.DFM}

Procedure Tfrm_ChangeObject.FormCreate(Sender: TObject);
Var winX, winY: Integer;
Begin
   winX:= GetSystemMetrics(SM_CXSCREEN);
   winY:= GetSystemMetrics(SM_CYSCREEN);
   Self.Left:= winX div 2 - Self.Width  div 2;
   Self.Top := winY div 2 - Self.Height div 2;
End;

Procedure Tfrm_ChangeObject.FormActivate(Sender: TObject);
Const
   CColorOpen: array[1..8] of Integer =
   (  clNavy  , // _1     фиолетовй
      clGreen , // _2     зелёный
      clRed   , // _3     красный
      clMaroon, // _4     коричневый ???
      clBlue  , // _5     синий
      clBlack , // _6     чёрный
      clOlive , // _7     светло-коричневый ???
      clAqua    // _8     голубой
   );
Var r: Integer;
Begin
   actual:= False;

   chckbx_Default.Checked:= False;
   pnl_ChangedObject.Color:= UOtherOptions.frm_OtherOptions.bttn_Background.Font.Color;
   pnl_ChangedObject.Caption:= '';
   img_ChangedObject.Picture.Graphic := nil;
   spnedt_Border     .Enabled:= True;
   chckbx_Default    .Enabled:= True;
   chckbx_Transparent.Enabled:= True;
   case changeObject of
   changeText  :
      begin
         frm_ChangeObject.Caption:= 'Change Font';
         spnedt_Border.Value:= UOtherOptions.border.text;
         r:= System.Random(8)+1;
         pnl_ChangedObject.Caption:= System.Chr(r+48);
         pnl_ChangedObject.Font.Color:= CColorOpen[r];
         pnl_ChangedObject.Font.Name := UOtherOptions.frm_OtherOptions.bttn_Text.Font.Name;
         pnl_ChangedObject.Font.Style:= UOtherOptions.frm_OtherOptions.bttn_Text.Font.Style;
         pnl_ChangedObject.Font.Height:= pnl_ChangedObject.Height
                  -2*spnedt_Border.Value*pnl_ChangedObject.Height div 100;
         bttn_Change.Caption:= 'Font...';
         chckbx_Transparent.Enabled:= False;
      end;
   changeMine  :
      begin
         frm_ChangeObject.Caption:= 'Change Picture Mine';
         img_ChangedObject.Picture    := UOtherOptions.frm_OtherOptions.img_Mine.Picture;
         img_ChangedObject.Transparent:= UOtherOptions.transparent.mine;
         chckbx_Transparent.Checked   := UOtherOptions.transparent.mine;
         spnedt_Border.Value          := UOtherOptions.border.mine;
         bttn_Change.Caption:= 'Picture...';
      end;
   changeFlag  :
      begin
         frm_ChangeObject.Caption:= 'Change Picture Flag';
         img_ChangedObject.Picture    := UOtherOptions.frm_OtherOptions.img_Flag.Picture;
         img_ChangedObject.Transparent:= UOtherOptions.transparent.flag;
         chckbx_Transparent.Checked   := UOtherOptions.transparent.flag;
         spnedt_Border.Value          := UOtherOptions.border.flag;
         bttn_Change.Caption:= 'Picture...';
      end;
   changeCrater:
      begin
         frm_ChangeObject.Caption:= 'Change Picture Crater';
         img_ChangedObject.Picture    := UOtherOptions.frm_OtherOptions.img_Crater.Picture;
         img_ChangedObject.Transparent:= UOtherOptions.transparent.crater;
         chckbx_Transparent.Checked   := UOtherOptions.transparent.crater;
         spnedt_Border.Value          := UOtherOptions.border.crater;
         bttn_Change.Caption:= 'Picture...';
      end;
   changeBackground:
      begin
         frm_ChangeObject.Caption:= 'Change Background Color';
         spnedt_Border.Value:= UOtherOptions.border.text;
         spnedt_Border.Enabled:= False;
         r:= System.Random(8)+1;
         pnl_ChangedObject.Caption:= System.Chr(r+48);
         pnl_ChangedObject.Font.Color:= CColorOpen[r];
         pnl_ChangedObject.Font.Name := UOtherOptions.frm_OtherOptions.bttn_Text.Font.Name;
         pnl_ChangedObject.Font.Style:= UOtherOptions.frm_OtherOptions.bttn_Text.Font.Style;
         pnl_ChangedObject.Font.Height:= pnl_ChangedObject.Height
                  -2*spnedt_Border.Value*pnl_ChangedObject.Height div 100;
         bttn_Change.Caption:= 'Color...';
         chckbx_Transparent.Enabled:= False;
      end;
   changePause:
      begin
         frm_ChangeObject.Caption:= 'Change Picture Pause';
         spnedt_Border .Value  := 0;
         spnedt_Border .Enabled:= False;
         chckbx_Default.Enabled:= False;
         img_ChangedObject.Picture    := UOtherOptions.frm_OtherOptions.img_Pause.Picture;
         img_ChangedObject.Transparent:= UOtherOptions.transparent.pause;
         chckbx_Transparent.Checked   := UOtherOptions.transparent.pause;
         pnl_ChangedObject.Color:= clBtnFace;
         bttn_Change.Caption:= 'Picture...';
      end;
   end;
End;

Procedure Tfrm_ChangeObject.btbttn_OkClickClick(Sender: TObject);
Begin
   actual:= True;
End;

Procedure Tfrm_ChangeObject.spnedt_BorderChange(Sender: TObject);
Begin
   if (spnedt_Border.Value <> 100{%} div UMines.CMinSizeCell) then chckbx_Default.Checked:= False;
   pnl_ChangedObject.BorderWidth:= spnedt_Border.Value;
   pnl_ChangedObject.Font.Height:= pnl_ChangedObject.Height
            -2*spnedt_Border.Value*pnl_ChangedObject.Height div 100;
End;

Procedure Tfrm_ChangeObject.chckbx_DefaultClick(Sender: TObject);
Begin
   if (not chckbx_Default.Checked) then Exit;
   spnedt_Border.Value:= 100{%} div UMines.CMinSizeCell;
   case changeObject of
   changeText  :
      begin
         pnl_ChangedObject.Font.Name:= 'MS Sans Serif';
         pnl_ChangedObject.Font.Style:= [fsBold];
         pnl_ChangedObject.Font.Height:= pnl_ChangedObject.Height
                  -2*spnedt_Border.Value*pnl_ChangedObject.Height div 100;
      end;
   changeMine  :
      begin
         path.mine:= '';
         img_ChangedObject.Picture.Bitmap.LoadFromResourceName(hInstance,'Mine');
         chckbx_Transparent.Checked   := True;
         img_ChangedObject.Transparent:= True;
      end;
   changeFlag  :
      begin
         path.flag:= '';
         img_ChangedObject.Picture.Bitmap.LoadFromResourceName(hInstance,'Flag');
         chckbx_Transparent.Checked   := True;
         img_ChangedObject.Transparent:= True;
      end;
   changeCrater:
      begin
         path.crater:= '';
         img_ChangedObject.Picture.Bitmap.LoadFromResourceName(hInstance,'Crater');
         chckbx_Transparent.Checked   := False;
         img_ChangedObject.Transparent:= False;
      end;
   changeBackground:
      begin
         pnl_ChangedObject.Color:= clBtnFace;
         ColorDialog      .Color:= clBtnFace;
      end;
   end;
End;

Procedure Tfrm_ChangeObject.bttn_ChangeClick(Sender: TObject);
Begin
   case changeObject of
   changeText  :
      begin
         FontDialog.Font.Name := pnl_ChangedObject.Font.Name;
         FontDialog.Font.Style:= pnl_ChangedObject.Font.Style;
         if (FontDialog.Execute) then begin
            chckbx_Default.Checked:= False;
            pnl_ChangedObject.Font.Name := FontDialog.Font.Name;
            pnl_ChangedObject.Font.Style:= FontDialog.Font.Style;
            pnl_ChangedObject.Font.Height:= pnl_ChangedObject.Height
                     -2*spnedt_Border.Value*pnl_ChangedObject.Height div 100;
         end;
      end;
   changeMine  :
      begin
         if (OpenPictureDialog.Execute) then begin
            chckbx_Default.Checked:= False;
            path.mine:= OpenPictureDialog.FileName;
            img_ChangedObject.Picture.LoadFromFile(OpenPictureDialog.FileName);
            //img_ChangedObject.Transparent:= True;
         end;
      end;
   changeFlag  :
      begin
         if (OpenPictureDialog.Execute) then begin
            chckbx_Default.Checked:= False;
            path.flag:= OpenPictureDialog.FileName;
            img_ChangedObject.Picture.LoadFromFile(OpenPictureDialog.FileName);
            //img_ChangedObject.Transparent:= True;
         end;
      end;
   changeCrater:
      begin
         if (OpenPictureDialog.Execute) then begin
            chckbx_Default.Checked:= False;
            path.crater:= OpenPictureDialog.FileName;
            img_ChangedObject.Picture.LoadFromFile(OpenPictureDialog.FileName);
            //img_ChangedObject.Transparent:= True;
         end;
      end;
   changeBackground:
      begin
         if (ColorDialog.Execute) then begin
            chckbx_Default.Checked:= False;
            pnl_ChangedObject.Color:= ColorDialog.Color;
         end;
      end;
   changePause:
      begin
         if (OpenPictureDialog.Execute) then begin
            path.pause:= OpenPictureDialog.FileName;
            img_ChangedObject.Picture.LoadFromFile(OpenPictureDialog.FileName);
            //img_ChangedObject.Transparent:= True;
         end;
      end;
   end;
End;

Procedure Tfrm_ChangeObject.spnedt_BorderKeyDown(Sender: TObject;
  var Key: Word; Shift: TShiftState);
Begin
   if (key = VK_RETURN) then begin
      btbttn_OkClickClick(Sender);
      Self.Close;
   end;
   if (key = VK_ESCAPE) then begin
      Self.Close;
   end;
End;

Procedure Tfrm_ChangeObject.chckbx_TransparentClick(Sender: TObject);
Begin
   img_ChangedObject.Transparent:= chckbx_Transparent.Checked;
End;

END.
