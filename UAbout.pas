UNIT UAbout;

INTERFACE

USES Windows, Forms, ShellAPI, ExtCtrls, StdCtrls, Controls, Classes,
  Graphics;

TYPE
  Tfrm_About = class(TForm)
    Panel1         : TPanel;
    ProgramIcon    : TImage;
    lbl_ProductName: TLabel;
    lbl_Version    : TLabel;
    lbl_Copyright  : TLabel;
    lbl_Comments   : TLabel;
    lbl_FMSite     : TLabel;
    lbl_Site       : TLabel;
    lbl_FIDO       : TLabel;
    lbl_ICQ        : TLabel;
    lbl_Yahoo      : TLabel;
    lbl_UkrPost    : TLabel;
    lbl_Response   : TLabel;
    lbl_ICQAddress : TLabel;
    lbl_FIDOAddress: TLabel;
    bttn_Ok        : TButton;
    procedure FormCreate          (Sender: TObject);
    procedure lbl_SiteClick       (Sender: TObject);
    procedure lbl_UkrPostClick    (Sender: TObject);
    procedure lbl_YahooClick      (Sender: TObject);
    procedure lbl_SiteMouseDown   (Sender: TObject; Button: TMouseButton; Shift: TShiftState; X, Y: Integer);
    procedure lbl_SiteMouseUp     (Sender: TObject; Button: TMouseButton; Shift: TShiftState; X, Y: Integer);
    procedure lbl_UkrPostMouseDown(Sender: TObject; Button: TMouseButton; Shift: TShiftState; X, Y: Integer);
    procedure lbl_UkrPostMouseUp  (Sender: TObject; Button: TMouseButton; Shift: TShiftState; X, Y: Integer);
    procedure lbl_YahooMouseUp    (Sender: TObject; Button: TMouseButton; Shift: TShiftState; X, Y: Integer);
    procedure lbl_YahooMouseDown  (Sender: TObject; Button: TMouseButton; Shift: TShiftState; X, Y: Integer);
    procedure bttn_OkKeyDown      (Sender: TObject; var Key: Word; Shift: TShiftState);
  private
    { Private declarations }
  public
    { Public declarations }
  end;

VAR
  frm_About: Tfrm_About;

IMPLEMENTATION

{$R *.DFM}

Procedure Tfrm_About.FormCreate(Sender: TObject);
Var winX, winY: Integer;
Begin
   winX:= GetSystemMetrics(SM_CXSCREEN);
   winY:= GetSystemMetrics(SM_CYSCREEN);
   Self.Left:= winX div 2 - Self.Width  div 2;
   Self.Top := winY div 2 - Self.Height div 2;
End;

Procedure Tfrm_About.lbl_SiteClick(Sender: TObject);
Begin
   ShellExecute( frm_About.Handle, 'open', 'http://kserg77.chat.ru', nil, nil, SW_RESTORE );
   //DdeClientConv1.ExecuteMacro('[ViewFolder("","http://kserg77.chat.ru",0)]', false);
End;

Procedure Tfrm_About.lbl_SiteMouseDown(Sender: TObject;
   Button: TMouseButton; Shift: TShiftState; X, Y: Integer);
Begin
   lbl_Site.Top := lbl_Site.Top +1;
   lbl_Site.Left:= lbl_Site.Left+1;
End;

Procedure Tfrm_About.lbl_SiteMouseUp(Sender: TObject; Button: TMouseButton;
   Shift: TShiftState; X, Y: Integer);
Begin
   lbl_Site.Top := lbl_Site.Top -1;
   lbl_Site.Left:= lbl_Site.Left-1;
End;

Procedure Tfrm_About.lbl_UkrPostClick(Sender: TObject);
Begin
   ShellExecute( frm_About.Handle, 'open', 'mailto:Serg_Krivulja@UkrPost.net', nil, nil, SW_RESTORE );
   //DdeClientConv1.ExecuteMacro('[ViewFolder("","mailto:Serg_Krivulja@UkrPost.net",0)]', false);
End;

Procedure Tfrm_About.lbl_UkrPostMouseDown(Sender: TObject;
   Button: TMouseButton; Shift: TShiftState; X, Y: Integer);
Begin
   lbl_UkrPost.Top := lbl_UkrPost.Top +1;
   lbl_UkrPost.Left:= lbl_UkrPost.Left+1;
End;

Procedure Tfrm_About.lbl_UkrPostMouseUp(Sender: TObject;
   Button: TMouseButton; Shift: TShiftState; X, Y: Integer);
Begin
   lbl_UkrPost.Top := lbl_UkrPost.Top -1;
   lbl_UkrPost.Left:= lbl_UkrPost.Left-1;
End;

Procedure Tfrm_About.lbl_YahooClick(Sender: TObject);
Begin
   ShellExecute( frm_About.Handle, 'open', 'mailto:Serg_Krivulja@Yahoo.com', nil, nil, SW_RESTORE );
   //WinExec('start "mailto:Serg_Krivulja@Yahoo.com"', 1);
   //DdeClientConv1.ExecuteMacro('[ViewFolder("","mailto:Serg_Krivulja@Yahoo.com",0)]', false);
End;

Procedure Tfrm_About.lbl_YahooMouseDown(Sender: TObject;
   Button: TMouseButton; Shift: TShiftState; X, Y: Integer);
Begin
   lbl_Yahoo.Top := lbl_Yahoo.Top +1;
   lbl_Yahoo.Left:= lbl_Yahoo.Left+1;
End;

Procedure Tfrm_About.lbl_YahooMouseUp(Sender: TObject;
   Button: TMouseButton; Shift: TShiftState; X, Y: Integer);
Begin
   lbl_Yahoo.Top := lbl_Yahoo.Top -1;
   lbl_Yahoo.Left:= lbl_Yahoo.Left-1;
End;

Procedure Tfrm_About.bttn_OkKeyDown(Sender: TObject; var Key: Word;
   Shift: TShiftState);
Begin
   if (Key = VK_ESCAPE) then begin
      Self.Close;
   end;
End;

END.

