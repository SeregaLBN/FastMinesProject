{$DEFINE USE_PICTURES} // см. UOtherOptions
{$DEFINE USE_WMActivate}
//{$DEFINE USE_CellHint}

UNIT UMines;

INTERFACE

USES
   Windows, Messages, SysUtils, Graphics, Forms, ExtCtrls, Classes, Controls,
   StdCtrls, Buttons, Menus, Dialogs, ComCtrls;

TYPE
   TSkillLevel         = ( SkillLevelBeginner    , SkillLevelAmateur,
                           SkillLevelProfessional, SkillLevelCrazy  , SkillLevelCustom);
   TSubrangeSkillLevel = Ord(Low(TSkillLevel))..Ord(High(TSkillLevel))-1;
   TArrSkillLevel      = array[TSubrangeSkillLevel] of Integer;
   TNamePlayer = String[20];

CONST
   CFormCaption = 'FastMines';

   CMinSizeCell = 17;
                               // Begin Amateur Profi   Crazy
   CSizeX     : TArrSkillLevel = ( 10   , 20   , 30   , 45    );
   CSizeY     : TArrSkillLevel = ( 10   , 15   , 20   , 25    );
   CMines     : TArrSkillLevel = ( 15{%}, 18{%}, 21{%}, 25{%} ); // Number of Mines (процент от числа €чеек)
   CCraters   : TArrSkillLevel = (  7{%},  8{%},  9{%}, 10{%} ); // Number of Craters (процент от числа €чеек)
   CMoving    : TArrSkillLevel = ( 10{%}, 10{%}, 10{%}, 10{%} ); // Number of Moving Mines (процент от числа мин)
   CMovingTime: TArrSkillLevel = ( 10   , 15   , 20   , 25    ); // Mine Moving Time Step

TYPE
   TFrm_Mines = Class(TForm)
      pnl_Top                : TPanel;
      pnl_Field              : TPanel;
      bttn_New               : TSpeedButton;
      bttn_Pause             : TSpeedButton;
      Timer                  : TTimer;
      edt_Count              : TEdit;
      edt_Timer              : TEdit;
      img_Pause              : TImage;
      menu_Mines             : TMainMenu;
      mnitm_File             : TMenuItem;
      mnitm_Options          : TMenuItem;
      mnitm_Help             : TMenuItem;
      mnitm_FileNewGame      : TMenuItem;
      mnitm_FileSplitter1    : TMenuItem;
      mnitm_FileBeginner     : TMenuItem;
      mnitm_FileAmateur      : TMenuItem;
      mnitm_FileProfessional : TMenuItem;
      mnitm_FileCrazy        : TMenuItem;
      mnitm_FileCustom       : TMenuItem;
      mnitm_FileSplitter2    : TMenuItem;
      mnitm_FilePlayer       : TMenuItem;
      mnitm_FileSplitter3    : TMenuItem;
      mnitm_FileExit         : TMenuItem;
      mnitm_FilePause        : TMenuItem;
      mnitm_FileMinimize     : TMenuItem;
      mnitm_FileNewGame2     : TMenuItem;
      mnitm_OptionsCraters   : TMenuItem;
      mnitm_OptionsMoving    : TMenuItem;
      mnitm_OptionsRandom    : TMenuItem;
      mnitm_OptionsSplitter1 : TMenuItem;
      mnitm_OtherOptions     : TMenuItem;
      mnitm_OptionsSplitter2 : TMenuItem;
      mnitm_OptionsUseUnknown: TMenuItem;
      mnitm_HelpChampions    : TMenuItem;
      mnitm_HelpStatistics   : TMenuItem;
      mnitm_HelpSplitter     : TMenuItem;
      mnitm_HelpAbout        : TMenuItem;
      prgssbr_TimeMoving     : TProgressBar;
      Procedure MovingMines;
      procedure pnl_TopResize    (Sender: TObject);
      procedure TimerOn          (Sender: TObject);
      Procedure DestroyField     (Sender: TObject);
      Procedure CreateField      (Sender: TObject);
      procedure FormActivate     (Sender: TObject);
      procedure FormClose        (Sender: TObject; var Action: TCloseAction);
      procedure FormCreate       (Sender: TObject);
      Procedure BeginGame        (no_x, no_y: Integer);
      Procedure EndGame          (Victory: Boolean );
      procedure bttn_PauseClick  (Sender: TObject);
      procedure bttn_NewClick    (Sender: TObject);
      procedure edt_CountChange  (Sender: TObject);
      procedure edt_EditMouseDown(Sender: TObject; Button: TMouseButton;
                                  Shift : TShiftState; X, Y: Integer);
      procedure mnitm_FileBeginnerClick     (Sender: TObject);
      procedure mnitm_FileAmateurClick      (Sender: TObject);
      procedure mnitm_FileProfessionalClick (Sender: TObject);
      procedure mnitm_FileCrazyClick        (Sender: TObject);
      procedure mnitm_FileCustomClick       (Sender: TObject);
      procedure mnitm_FilePlayerClick       (Sender: TObject);
      procedure mnitm_FileExitClick         (Sender: TObject);
      procedure mnitm_FilePauseClick        (Sender: TObject);
      procedure mnitm_FileMinimizeClick     (Sender: TObject);
      procedure mnitm_OptionsCratersClick   (Sender: TObject);
      procedure mnitm_OptionsMovingClick    (Sender: TObject);
      procedure mnitm_OptionsRandomClick    (Sender: TObject);
      procedure mnitm_OtherOptionsClick     (Sender: TObject);
      procedure mnitm_OptionsUseUnknownClick(Sender: TObject);
      procedure mnitm_HelpChampionsClick    (Sender: TObject);
      procedure mnitm_HelpStatisticsClick   (Sender: TObject);
      procedure mnitm_HelpAboutClick        (Sender: TObject);
   private
      { Private declarations }
{$IFDEF USE_WMActivate}
      procedure WMActivate(var Message: TWMActivate); message WM_ACTIVATE;
{$ENDIF}
      procedure WMKeyDown (var Message: TWMKeyDown ); message WM_KEYDOWN;
{}
      procedure WMSysKeyUp(var Message: TWMSysKeyUp); message WM_SYSKEYUP;
      procedure WMKeyUp   (var Message: TWMKeyUp   ); message WM_KEYUP;
{}
   public
      { Public declarations }
   End;

TYPE
   TGameOptions = record
                     X, Y      : Integer; // ширина и высота пол€ (в €чейках)
                     maxMines  : Integer; // кол-во мин на поле
                     useCraters: Boolean;
                     maxCraters: Integer; // кол-во кратеров на поле
                     useMoving : Boolean;
                     maxMoving : Integer; // кол-во перемещаемых мин
                     timeMoving: Integer; // шаг времени перемещени€
                     useRandom : Boolean;
                     SizeCell  : Integer;
                     useUnknown: Boolean;
                     path: record
                        pause, mine, flag, crater: String[255];
                     end;
                     background: TColor;
                     border: record
                        text, mine, flag, crater: Integer; // %
                     end;
                     transparent: record
                        pause, mine, flag, crater: Boolean;
                     end;
                     font: record
                        Name : String[255];
                        Style: TFontStyles;
                     end;
                     currPlayer: TNamePlayer;
                  end;

TYPE
   TImageCells = Class(TImage)
      protected
         procedure WMPaint(var Message: TWMPaint); message WM_PAINT;
      public
         Procedure WMLButtonDown  (var msg: TWMLButtonDown  ); message WM_LButtonDown;
         Procedure WMLButtonUp    (var msg: TWMLButtonUp    ); message WM_LButtonUp;
         Procedure WMRButtonDown  (var msg: TWMRButtonDown  ); message WM_RButtonDown;
         Procedure WMLButtonDblClk(var msg: TWMLButtonDblClk); message WM_LButtonDblClk;
         Procedure WMRButtonDblClk(var msg: TWMRButtonDblClk); message WM_RButtonDblClk;
{$IFDEF USE_CellHint}
         Procedure WMMouseMove(var msg: TWMMouseMove); message WM_MOUSEMOVE;
{$ENDIF}
   End;

{$IFDEF USE_PICTURES}
TYPE
   TAllPicture = record
      New1,   Pause1,   Mine,      Crater,      Flag,
      New2,   Pause2, //MineBig,   CraterBig,   FlagBig,
      New3,
      New4: TPicture;
   end;

VAR
   pictures: TAllPicture;
{$ENDIF}

VAR
   frm_Mines: Tfrm_Mines;
   img_Cells: TImageCells;
   gSettings: TGameOptions;
   path_to_exe_file: String;

////////////////////////////////////////////////////////////////////////////////
//                             forward declaration                            //
Function  CurrentSkillLevel: TSkillLevel; forward;
Procedure CircleHint(i: Integer); forward;
////////////////////////////////////////////////////////////////////////////////

IMPLEMENTATION

USES UCustomSkill, UAbout, UChampions, UOtherOptions, UDialog, UStatistics;

TYPE
   TStateCell       = ( _Open, _Close );
   TCellOpen        = ( _Null, _1, _2, _3, _4, _5, _6, _7, _8, _Mine );
   TCellClose       = ( _Clear, _Flag, _Unknown );
   TSubrangeOpen    = Ord(Low(TCellOpen ))..Ord(High(TCellOpen ));
   TSubrangeClose   = Ord(Low(TCellClose))..Ord(High(TCellClose));
   TArrColorOpen    = array[TSubrangeOpen ] of Integer;
   TArrColorClose   = array[TSubrangeClose] of Integer;
   TArrCaptionOpen  = array[TSubrangeOpen ] of Char;
   TArrCaptionClose = array[TSubrangeClose] of Char;

CONST
   CFileNameInit   = 'Mines.ini';

   CCaptionOpen: TArrCaptionOpen =
   (  ' ', // _Null
      '1', // _1
      '2', // _2
      '3', // _3
      '4', // _4
      '5', // _5
      '6', // _6
      '7', // _7
      '8', // _8
      '*'  // _Mine
   );

   CCaptionClose: TArrCaptionClose =
   (  ' ', // _Clear
      'F', // _Flag
      '?'  // _Unknown
   );

   CColorOpen: TArrColorOpen =
   (  clBlack , // _Null  чЄрный
      clNavy  , // _1     фиолетовй
      clGreen , // _2     зелЄный
      clRed   , // _3     красный
      clMaroon, // _4     коричневый ???
      clBlue  , // _5     синий
      clBlack , // _6     чЄрный
      clOlive , // _7     светло-коричневый ???
      clAqua  , // _8     голубой
      clBlack   // _Mine  чЄрный
   );
   CColorClose: TArrColorClose =
   (  clBlack , // _Clear    чЄрный
      clRed   , // _Flag     красный
      clTeal    // _Unknown  зелЄно-серый
   );

CONST
   CVictory = True;
   CDefeat  = False;

TYPE
   TNeighbor  = -1..1;
   TCell = Class(TObject)
      published
      protected
      private
         Crater : Boolean;
         Random : Boolean;
         Down   : Boolean; // is down ?
         Caption: String;  // text in caption
         X1, Y1 : Integer; // LeftTopX, LeftTopY
         X2, Y2 : Integer; // RightBottomX, RightBottomY
         State    : TStateCell;
         CellOpen : TCellOpen ;
         CellClose: TCellClose;
         i, j     : Integer; // coordinate in Field array
         Constructor CreateCell( newX, newY: Integer );
         Procedure Paint;
      public
         Procedure LButtonDown;
         Procedure LButtonUp( point: TPoint );
         Procedure RButtonDown;
   End;

   //TArrMinePanel = array [0..CMaxX-1,0..CMaxY-1]of TCell; // Delphi3
   TArrMinePanel = array of array of TCell; // Delphi5

////////////////////////////////////////////////////////////////////////////////
//                             forward declaration                            //
////////////////////////////////////////////////////////////////////////////////
Procedure SaveResult( Victory: Boolean);         forward;
Function  ChangeHint: String; forward;

////////////////////////////////////////////////////////////////////////////////
//                          globals variables this module                     //
////////////////////////////////////////////////////////////////////////////////
VAR
   Field      : TArrMinePanel;
   gLmsg      : TWMMouse;
   isRefresh  : Boolean;
   Pause      : Boolean;
   gSemaphore : Boolean; // if = True - нельз€ делать Moving при нажатии на €чейку
   gMustMoving: Boolean; // if = True - об€заны делать Moving после отжати€ на €чейке
   gPoint     : TPoint;  // сним€ю семафор после отпускани€ на €чейке в той же точке (коорд. €чейки) где и поставил
   gGameRun   : Boolean;
   gCountOpen : Integer; // счЄтчик открытых €чеек на поле
   gCountFlag : Integer; // счЄтчик проставленных флажков на поле
   gCountClick: Integer; // счЄтчик левых кликов за игру
   firstOnActivate: Boolean;

{$R *.DFM}

////////////////////////////////////////////////////////////////////////////////
//                                  TCell                                     //
////////////////////////////////////////////////////////////////////////////////
Constructor TCell.CreateCell( newX, newY: Integer );
Begin
   Self.i        := newX;
   Self.j        := newY;
   Self.State    := _Close;
   Self.CellOpen := _Null;
   Self.CellClose:= _Clear;
   Self.X1       := Self.i  * gSettings.SizeCell;
   Self.Y1       := Self.j  * gSettings.SizeCell;
   Self.X2       := Self.X1 + gSettings.SizeCell;
   Self.Y2       := Self.Y1 + gSettings.SizeCell;
   Self.Crater   := False;
   Self.Random   := False;
End;

Procedure TCell.LButtonDown;
Var m, n: TNeighbor;
    f: Boolean;
Begin
   inherited;
   gPoint.x:= Self.i;
   gPoint.y:= Self.j;
   gSemaphore:= True;
{$IFDEF USE_PICTURES}
   frm_Mines.bttn_New.Glyph:= pictures.New2.Bitmap;
{$ENDIF}
   if (Self.CellClose = _Flag) then Exit;
   if (Self.Crater) then Exit;
   if (Self.State = _Close) then begin
      Self.Down:= True;
      Self.Paint;
      Inc(gCountClick);
      Exit;
   end;
   f:= True;
   // эффект нажатости дл€ неоткрытых соседей
   if (Self.State = _Open) and (Self.CellOpen <> _Null) then
      for m:= -1 to 1 do
         for n:= -1 to 1 do
            if ((m = 0 ) and (  n = 0 )) or
               (i+m < 0)  or (i+m > gSettings.X-1) or
               (j+n < 0)  or (j+n > gSettings.Y-1) or
               (Field[i+m,j+n].State     = _Open) or
               (Field[i+m,j+n].CellClose = _Flag) then Continue
            else begin
               if (f) then begin
                  f:= False;
                  Inc(gCountClick);
               end;
               Field[i+m,j+n].Down:= True;
               Field[i+m,j+n].Paint;
            end;
End;

Procedure TCell.LButtonUp( point: TPoint );
Var countFlags: Integer;
    countClear: Integer;
    m, n      : TNeighbor;
    newPoint  : TPoint;
Begin
   inherited;
{$IFDEF USE_PICTURES}
   frm_Mines.bttn_New.Glyph:= pictures.New1.Bitmap;
{$ENDIF}
   if (Self.CellClose = _Flag) or (Self.Crater) then begin
      if (gPoint.x = Self.i) and (gPoint.y = Self.j) then gSemaphore:= False;
      Exit;
   end;
   // избавитс€ от эффекта нажатости
   if (Self.State = _Open) and (Self.CellOpen <> _Null) then
      for m:= -1 to 1 do
         for n:= -1 to 1 do
            if ((m = 0 ) and (  n = 0 )) or
               (i+m < 0)  or (i+m > gSettings.X-1) or
               (j+n < 0)  or (j+n > gSettings.Y-1) or
               (Field[i+m,j+n].State     = _Open) or
               (Field[i+m,j+n].CellClose = _Flag) then Continue
            else begin
               Field[i+m,j+n].Down:= False;
               Field[i+m,j+n].Paint;
            end;
   // ќткрыть закрытую €чейку на которой нажали
   if (Self.State = _Close) then
      if (point.x < Self.X1) or (point.x > Self.X2) or
         (point.y < Self.Y1) or (point.y > Self.Y2) then begin
         Self.Down:= False;
         Self.Paint;
         if (gPoint.x = Self.i) and (gPoint.y = Self.j) then gSemaphore:= False;
         Exit;
      end else begin
         Self.State:= _Open;
         Self.Down:= True;
         Self.Paint;
         Inc(gCountOpen);
         frm_Mines.edt_Count.Hint:= ChangeHint;
         if (Self.Random) then begin
            gSemaphore:= False;
            frm_Mines.MovingMines;
            gSemaphore:= True;
         end;
      end;
   // ! ¬ этой точке €чейка уже открыта
   // ѕодсчитываю кол-во установленных вокруг флагов и не открытых €чеек
   countFlags:= 0;
   countClear:= 0;
   if (Self.CellOpen <> _Null) then
      for m:= -1 to 1 do
         for n:= -1 to 1 do begin
            if ((m = 0 ) and ( n = 0)) or
               (i+m < 0)  or (i+m > gSettings.X-1) or
               (j+n < 0)  or (j+n > gSettings.Y-1) or
               (Field[i+m,j+n].State = _Open) then Continue
            else if (Field[i+m,j+n].CellClose = _Flag) // Field[i+m,j+n].State = _Close
               then Inc(countFlags)
               else Inc(countClear);
         end;
   // оставшимс€ установить флаги
   if (Self.CellOpen <> _Null) and (countFlags+countClear = Ord(Self.CellOpen)) then
      for m:= -1 to 1 do
         for n:= -1 to 1 do
            if ((m = 0 ) and ( n = 0 )) or
               (i+m < 0)  or (i+m > gSettings.X-1) or
               (j+n < 0)  or (j+n > gSettings.Y-1) or
               (Field[i+m,j+n].State     = _Open) or
               (Field[i+m,j+n].CellClose = _Flag) then Continue
            else begin
               Field[i+m,j+n].CellClose:= _Flag;
               Field[i+m,j+n].Paint;
               Inc(gCountFlag);
               frm_Mines.edt_Count.Text:= IntToStr( gSettings.maxMines-gCountFlag );
               frm_Mines.edt_Count.Hint:= ChangeHint;
            end;
   if (point.x < Self.X1) or (point.x > Self.X2) or
      (point.y < Self.Y1) or (point.y > Self.Y2) then begin
      if (gPoint.x = Self.i) and (gPoint.y = Self.j) then gSemaphore:= False;
      Exit;
   end;
   if (not gGameRun) then frm_Mines.BeginGame(Self.i,Self.j);
   // открыть оставшиес€
   if countFlags = Ord(Self.CellOpen) then
      for m:= -1 to 1 do
         for n:= -1 to 1 do
            if ((m = 0 ) and ( n = 0)) or
               (i+m < 0)  or (i+m > gSettings.X-1) or
               (j+n < 0)  or (j+n > gSettings.Y-1) or
               (Field[i+m,j+n].CellClose = _Flag) or
               (Field[i+m,j+n].State     = _Open) or
               ((Field[i+m,j+n].Random)and
                ((abs(gPoint.x-i-m)>1)or(abs(gPoint.y-j-n)>1)or
                (Field[gPoint.x,gPoint.y].CellOpen = _Null)))
               then Continue
            else begin
               Field[i+m,j+n].Down:= True;
               Field[i+m,j+n].State:= _Open;
               Field[i+m,j+n].Paint;
               Inc(gCountOpen);
               frm_Mines.edt_Count.Hint:= ChangeHint;
               if (Field[i+m,j+n].Random) then begin
                  gSemaphore:= False;
                  frm_Mines.MovingMines;
                  gSemaphore:= True;
               end;
               if (Field[i+m,j+n].CellOpen = _Null) then begin
                  newPoint.x:= point.x + m*gSettings.SizeCell;
                  newPoint.y:= point.y + n*gSettings.SizeCell;
                  Field[i+m,j+n].LButtonUp( newPoint );
               end;
               if (Field[i+m,j+n].CellOpen = _Mine)  then begin
                  frm_Mines.EndGame(CDefeat);
                  if (gPoint.x = Self.i) and (gPoint.y = Self.j) then gSemaphore:= False;
                  Exit;//Break;
               end;
            end;
   if (gGameRun) then begin
      if (Self.CellOpen = _Mine)
         then frm_Mines.EndGame(CDefeat);
      if (gCountOpen+gSettings.maxMines = gSettings.X*gSettings.Y)
         then frm_Mines.EndGame(CVictory);
   end;
   if (gPoint.x = Self.i) and (gPoint.y = Self.j) then gSemaphore:= False;
End;

Procedure TCell.RButtonDown;//(var msg : TwmRButtonDown);
Begin
   inherited;
   if (Self.State = _Open) or (Self.Down) or (not gGameRun) then Exit;
   case Self.CellClose of
      _Clear  : begin
                   Self.CellClose:= _Flag;
                   Inc(gCountFlag);
                   frm_Mines.edt_Count.Text:= IntToStr( gSettings.maxMines-gCountFlag );
                end;
      _Flag   : begin
                   if (frm_Mines.mnitm_OptionsUseUnknown.Checked)
                   then Self.CellClose:= _Unknown
                   else Self.CellClose:= _Clear;
                   Dec(gCountFlag);
                   frm_Mines.edt_Count.Text:= IntToStr( gSettings.maxMines-gCountFlag );
                end;
      _Unknown: Self.CellClose:= _Clear;
   end;
   frm_Mines.edt_Count.Hint:= ChangeHint;
   Self.Paint;
End;

Procedure TCell.Paint;
Var
   rectan: TRect;
   border: Integer;
Begin
   inherited;
   // лини€ - перо(Pen)
   img_Cells.Canvas.Pen.Color:= clBlack;
   img_Cells.Canvas.Rectangle(Self.X1,Self.Y1,Self.X2,Self.Y2);
   img_Cells.Canvas.Pen.Color:= clWhite;
   if (Self.Down) then begin
      img_Cells.Canvas.MoveTo(X2-1,Y1);
      img_Cells.Canvas.LineTo(X2-1,Y2-1);
      img_Cells.Canvas.LineTo(X1-1,Y2-1);
   end else begin
      img_Cells.Canvas.MoveTo(X1,Y2-2);
      img_Cells.Canvas.LineTo(X1,Y1);
      img_Cells.Canvas.LineTo(X2-1,Y1);
   end;
   //
{$IFDEF USE_PICTURES}
   // output Pictures
   if (Self.Crater) then begin
      border:= gSettings.border.crater * gSettings.SizeCell div 100;
      if (border < 1) then border:= 1;
      rectan:= rect(Self.X1+border,Self.Y1+border,Self.X2-border,Self.Y2-border);
      img_Cells.Canvas.StretchDraw(rectan,pictures.Crater.Graphic)
   end else
   if (Self.State = _Close) and (Self.CellClose = _Flag) then begin
      border:= gSettings.border.flag * gSettings.SizeCell div 100;
      if (border < 1) then border:= 1;
      rectan:= rect(Self.X1+border,Self.Y1+border,Self.X2-border,Self.Y2-border);
      img_Cells.Canvas.StretchDraw(rectan,pictures.Flag.Graphic)
   end else
   if (Self.State = _Open) and (Self.CellOpen = _Mine) then begin
      border:= gSettings.border.mine * gSettings.SizeCell div 100;
      if (border < 1) then border:= 1;
      rectan:= rect(Self.X1+border,Self.Y1+border,Self.X2-border,Self.Y2-border);
      img_Cells.Canvas.StretchDraw(rectan,pictures.Mine.Graphic)
   end else
{$ENDIF}
   // output text
   begin
      if (Self.State = _Close) then begin
         img_Cells.Canvas.Font.Color:= CColorClose  [Ord(Self.CellClose)];
         Self.Caption               := CCaptionClose[Ord(Self.CellClose)];
      end else begin
         img_Cells.Canvas.Font.Color:= CColorOpen   [Ord(Self.CellOpen )];
         Self.Caption               := CCaptionOpen [Ord(Self.CellOpen )];
      end;
      if (Self.Crater) then begin
         img_Cells.Canvas.Font.Color:= clBlack;
         Self.Caption               := 'X';
      end;
      if (Self.Random)and(Self.State = _Open) then begin
         img_Cells.Canvas.Font.Color:= clRed;
         Self.Caption               := 'R';
      end;
      border:= gSettings.border.text * gSettings.SizeCell div 100;
      if (border < 1) then border:= 1;
      rectan:= rect(Self.X1+border,Self.Y1+border,Self.X2-border,Self.Y2-border);
      img_Cells.Canvas.Font.Height:= Self.Y2-Self.Y1-2*border;
      if (Self.Down) then begin
         rectan.top := rectan.top  + 1;
         rectan.left:= rectan.left + 1;
      end;
      //img_Cells.Canvas.TextRect(rectan, Self.X1, Self.Y1, Self.Caption);
      DrawText( img_Cells.Canvas.Handle, PChar(Self.Caption),
                -1, rectan, dt_Center or dt_VCenter );
   end;
   //img_Cells.Invalidate;
End;

////////////////////////////////////////////////////////////////////////////////
//                               TFrm_Mines                                   //
////////////////////////////////////////////////////////////////////////////////
Procedure TFrm_Mines.bttn_PauseClick(Sender: TObject);
Begin
   Pause:= bttn_Pause.Down;
   Timer.Enabled:= not Pause;
   if Pause then begin
      img_Cells.Hide;
{$IFDEF USE_PICTURES}
      img_Pause.Show;
{$ENDIF}
   end else begin
      img_Cells.Show;
{$IFDEF USE_PICTURES}
      img_Pause.Hide;
{$ENDIF}
   end;
   isRefresh:= Pause;
{$IFDEF USE_PICTURES}
   if (Pause) then
      bttn_Pause.Glyph:= pictures.Pause2.Bitmap
   else
      bttn_Pause.Glyph:= pictures.Pause1.Bitmap;
{$ENDIF}
End;

Procedure TFrm_Mines.BeginGame(no_x, no_y: Integer);
Var count     : Integer;
    maxCraters: Integer;
    i, j      : Integer;
    m, n      : TNeighbor;
Begin
   if (not pnl_Field.Enabled) then begin
      //Dialogs.MessageDlg('!!!', mtError, [mbOk], 0);
      //Dialogs.ShowMessage('Ќадо от этого if''a избавитьс€');
      Exit;
   end;

   //if (gSemaphore) then Exit;
   //Field[no_x, no_y].State:= _Open;

   gGameRun                    := True;
   frm_Mines.Timer     .Enabled:= True;
   frm_Mines.bttn_Pause.Enabled:= True;

   // cell
      // set random mines
   {}
   count:= 0;
   repeat
      i:= Random(gSettings.X);
      j:= Random(gSettings.Y);
      if ((i <> no_x-1) or (j <> no_y+1)) and // 1
         ((i <> no_x  ) or (j <> no_y+1)) and // 2
         ((i <> no_x+1) or (j <> no_y+1)) and // 3
         ((i <> no_x-1) or (j <> no_y  )) and // 4
         ((i <> no_x  ) or (j <> no_y  )) and // 5
         ((i <> no_x+1) or (j <> no_y  )) and // 6
         ((i <> no_x-1) or (j <> no_y-1)) and // 7
         ((i <> no_x  ) or (j <> no_y-1)) and // 8
         ((i <> no_x+1) or (j <> no_y-1)) and // 9
          (Field[i,j].CellOpen <> _Mine) then // что бы не установить повторно мину
      begin
         Field[i,j].CellOpen:= _Mine;
         Inc( count );
      end;
   until count >= gSettings.maxMines;
   {
   for i:= 0 to gSettings.X-1 do
      for j:= 0 to gSettings.Y-1 do Field[i,j].CellOpen:= _Mine;
   Field[no_x,no_y].CellOpen:= _Null;
   count:= gSettings.X*gSettings.Y-1;
   for m:= -1 to 1 do
      for n:= -1 to 1 do
         if ((m = 0) and (n = 0)) or
            (no_x+m < 0)  or (no_x+m > gSettings.X-1) or
            (no_y+n < 0)  or (no_y+n > gSettings.Y-1) or
            () then Continue
         else begin
            Field[no_x+m,no_y+n].CellOpen:= _Null;
            Dec(count);
         end;
   repeat
      case Random(4) of
         0: begin // up
            if (no_y <> 0) then begin
               Dec(no_y);
               if(Field[no_x,no_y].CellOpen = _Mine) then begin
                  Field[no_x,no_y].CellOpen:= _Null;
                  Dec(count);
               end;
            end;
         end;
         1: begin // right
            if (no_x <> gSettings.X-1) then begin
               Inc(no_x);
               if(Field[no_x,no_y].CellOpen = _Mine) then begin
                  Field[no_x,no_y].CellOpen:= _Null;
                  Dec(count);
               end;
            end;
         end;
         2: begin // left
            if (no_x <> 0) then begin
               Dec(no_x);
               if(Field[no_x,no_y].CellOpen = _Mine) then begin
                  Field[no_x,no_y].CellOpen:= _Null;
                  Dec(count);
               end;
            end;
         end;
         3: begin // down
            if (no_y <> gSettings.Y-1) then begin
               Inc(no_y);
               if(Field[no_x,no_y].CellOpen = _Mine) then begin
                  Field[no_x,no_y].CellOpen:= _Null;
                  Dec(count);
               end;
            end;
         end;
      end;
   until count <= gSettings.maxMines;
   {}
   // set other CellOpen and set all Caption
   for i:= 0 to gSettings.X-1 do
      for j:= 0 to gSettings.Y-1 do begin
         if(Field[i,j].CellOpen <> _Mine) then begin
            count:= 0;
            for m:= -1 to 1 do
               for n:= -1 to 1 do
                  if ((m = 0 ) and (  n = 0 )) or
                     (i+m < 0)  or (i+m > gSettings.X-1) or
                     (j+n < 0)  or (j+n > gSettings.Y-1) or
                     (Field[i+m,j+n].CellOpen <> _Mine) then Continue
                  else Inc( count );
            Field[i,j].CellOpen:= TCellOpen(count);
         end;
      end;
   // set cell 'Random'
   if (gSettings.useRandom) then
      repeat
         i:= Random(gSettings.X);
         j:= Random(gSettings.Y);
         if (Field[i,j].CellOpen <> _Mine)
         then begin
            Field[i,j].Random:= True;
            Field[i,j].Paint;
         end;
      until Field[i,j].Random;{}
   // set craters
   if (not gSettings.useCraters) then Exit;
   maxCraters:= 0;
   for i:= 0 to gSettings.X-1 do
      for j:= 0 to gSettings.Y-1 do
         if (Field[i,j].CellOpen <> _Mine) and
            (Field[i,j].CellOpen <> _Null) then Inc(maxCraters);
   if (maxCraters > gSettings.maxCraters) then maxCraters:= gSettings.maxCraters;
   gCountOpen:= gCountOpen + maxCraters;
   frm_Mines.edt_Count.Hint:= ChangeHint;
   count:= 0;
   repeat
      i:= Random(gSettings.X);
      j:= Random(gSettings.Y);
      {}
      if (Field[i,j].CellOpen <> _Mine) and
         (Field[i,j].CellOpen <> _Null) and
         (not Field[i,j].Crater) and
         (not Field[i,j].Random) then
      begin
            Field[i,j].Crater:= True;
            Field[i,j].State:= _Open;
            Field[i,j].Paint;
            Inc( count );
      end;
      {}
   until count >= maxCraters;
End;

Procedure TFrm_Mines.edt_EditMouseDown(Sender: TObject;
  Button: TMouseButton; Shift: TShiftState; X, Y: Integer);
Begin
   edt_Count.Enabled:= False;
   edt_Count.Enabled:= True;
   edt_Timer.Enabled:= False;
   edt_Timer.Enabled:= True;
End;

Procedure Tfrm_Mines.pnl_TopResize(Sender: TObject);
Begin
   bttn_New  .Left:= pnl_Top.Width div 2 - bttn_New.Width;
   bttn_Pause.Left:= pnl_Top.Width div 2;
   edt_Count .Left:= 10;
   edt_Timer .Left:= pnl_Top.Width - edt_Timer.Width - 10;

   bttn_New  .Top:= pnl_Top.Height div 2 - bttn_New  .Height div 2;
   bttn_Pause.Top:= pnl_Top.Height div 2 - bttn_Pause.Height div 2;
   edt_Count .Top:= pnl_Top.Height div 2 - edt_Count .Height div 2;
   edt_Timer .Top:= pnl_Top.Height div 2 - edt_Timer .Height div 2;
End;

Procedure TFrm_Mines.TimerOn(Sender: TObject);
Begin
   if (gSettings.useMoving) then begin
      prgssbr_TimeMoving.StepIt;
      if ((Timer.DesignInfo mod gSettings.timeMoving) = gSettings.timeMoving-3) then
         CircleHint(1);
      if ((Timer.DesignInfo mod gSettings.timeMoving) = gSettings.timeMoving-2) then
         CircleHint(2);
      if ((Timer.DesignInfo mod gSettings.timeMoving) = gSettings.timeMoving-1) then
         CircleHint(3);
      if ((Timer.DesignInfo mod gSettings.timeMoving) = 0) then
         CircleHint(0);
   end;
   Timer.DesignInfo:= Timer.DesignInfo + 1;
   edt_Timer.Text:= IntToStr(Timer.DesignInfo);
   if (gSettings.useMoving) and
      (Timer.DesignInfo mod gSettings.timeMoving = 0) then MovingMines;
   if (gMustMoving and not gSemaphore) then begin
      gMustMoving:= False;
      MovingMines;
   end;
End;

Procedure TFrm_Mines.DestroyField(Sender: TObject);
Var i,j: Integer;
Begin
   Timer.Enabled   := False;
   Timer.DesignInfo:= 0;
   edt_Timer.Text  := IntToStr(Timer.DesignInfo);
   for i:= 0 to gSettings.X-1 do
      for j:= 0 to gSettings.Y-1 do Field[i,j].Destroy;
End;

Procedure Tfrm_Mines.FormCreate(Sender: TObject);
Begin
{$IFDEF USE_PICTURES}
   pictures.New1  := TPicture.Create;
   pictures.New2  := TPicture.Create;
   pictures.New3  := TPicture.Create;
   pictures.New4  := TPicture.Create;
   pictures.Pause1:= TPicture.Create;
   pictures.Pause2:= TPicture.Create;
   pictures.Mine  := TPicture.Create;
   pictures.Crater:= TPicture.Create;
   pictures.Flag  := TPicture.Create;

   pictures.New1  .Bitmap.LoadFromResourceName(hInstance,'New1');
   pictures.New2  .Bitmap.LoadFromResourceName(hInstance,'New2');
   pictures.New3  .Bitmap.LoadFromResourceName(hInstance,'New3');
   pictures.New4  .Bitmap.LoadFromResourceName(hInstance,'New4');
   pictures.Pause1.Bitmap.LoadFromResourceName(hInstance,'Pause1');
   pictures.Pause2.Bitmap.LoadFromResourceName(hInstance,'Pause2');
{$ENDIF}

   img_Cells:= TImageCells.Create(pnl_Field);
   Self.pnl_Field.InsertControl(img_Cells);
   //img_Cells.Align := alClient;  // - Ќе пашет
   img_Cells.Width := GetSystemMetrics(SM_CXSCREEN); // ѕриходитс€
   img_Cells.Height:= GetSystemMetrics(SM_CYSCREEN); // извращатс€
   img_Cells.Canvas.Pixels[img_Cells.Width,img_Cells.Height]:= 0; // так надо :(

   firstOnActivate:= False;
End;

Procedure TFrm_Mines.FormActivate(Sender: TObject);
Var F: File of TGameOptions;
   exception: Boolean;
Begin
   if (firstOnActivate) then Exit;
   firstOnActivate:= True;
   //---------------- begin Win2000 fix
   edt_Count .Width:= 40;    edt_Count .Height:= 21;
   edt_Timer .Width:= 40;    edt_Timer .Height:= 21;
   bttn_New  .Width:= 28;    bttn_New  .Height:= 28;
   bttn_Pause.Width:= 28;    bttn_Pause.Height:= 28;
   //prgssbr_TimeMoving.Height:= 10;
   pnl_Top.Height:= 40;
   //if(pnl_Top.Height < GetSystemMetrics(SM_CYMIN)) then
   //   pnl_Top.Height:= GetSystemMetrics(SM_CYMIN);
   //if(pnl_Top.Height < GetDeviceCaps(pnl_Top.Handle,VertRes)) then
   //   pnl_Top.Height:= GetDeviceCaps(pnl_Top.Handle,VertRes);
   //---------------- end Win2000 fix
   gSemaphore := False;
   gMustMoving:= False;
   //Caption:= CFormCaption;
   Randomize;
{$IFDEF USE_PICTURES}
   pnl_Field .Caption:= '';
   bttn_New  .Glyph:= pictures.New1  .Bitmap;
   bttn_Pause.Glyph:= pictures.Pause1.Bitmap;
   bttn_New  .Caption:= '';
   bttn_Pause.Caption:= '';
{$ELSE}
   pnl_Field .Caption:= 'Pause';
   bttn_New  .Glyph  := Nil;
   bttn_Pause.Glyph  := Nil;
   bttn_New  .Caption:= 'N';
   bttn_Pause.Caption:= 'P';
{$ENDIF}
   exception:= False;
   System.GetDir( 0, path_to_exe_file);
   path_to_exe_file:= path_to_exe_file + '\';
   try
      AssignFile(F, path_to_exe_file + CFileNameInit);
      Reset(F);
      Read( F, gSettings );
      CloseFile(F);
   except
      exception:= True;
      gSettings.X         := CSizeX[0]; // ширина пол€
      gSettings.Y         := CSizeY[0]; // высота пол€
      gSettings.maxMines  := gSettings.X*gSettings.Y*CMines  [0] div 100; // кол-во мин на поле
      gSettings.useCraters:= False;
      gSettings.maxCraters:= gSettings.X*gSettings.Y*CCraters[0] div 100; // кол-во кратеров на поле
      gSettings.useMoving := False;
      gSettings.maxMoving := gSettings.maxMines     *CMoving [0] div 100; // кол-во перемещаемых мин
      gSettings.useRandom := False;
      gSettings.timeMoving:= CMovingTime[0]; // шаг времени перемещени€
      gSettings.SizeCell  := CMinSizeCell;
      gSettings.useUnknown:= True;
      gSettings.path.flag  := '';
      gSettings.path.mine  := '';
      gSettings.path.crater:= '';
      gSettings.path.pause := '';
      gSettings.border.flag  := 100{%} div CMinSizeCell;
      gSettings.border.mine  := 100{%} div CMinSizeCell;
      gSettings.border.crater:= 100{%} div CMinSizeCell;
      gSettings.border.text  := 100{%} div CMinSizeCell;
      gSettings.background:= clBtnFace;
      gSettings.transparent.mine  := True;
      gSettings.transparent.flag  := True;
      gSettings.transparent.crater:= False;
      gSettings.transparent.pause := True;
      gSettings.font.Name := 'MS Sans Serif';
      gSettings.font.Style:= [fsBold];
   end;
   // заливка - кисть(Brush)
   img_Cells.Canvas.Brush.Color:= gSettings.background; //Current color of a button face
   img_Cells.Canvas.Brush.Style:= bsSolid; // bsSolid bsCross bsClear bsDiagCross bsBDiagonal bsHorizontal bsFDiagonal bsVertical
   img_Cells.Canvas.Pen  .Style:= psSolid; // лини€ - перо(Pen)
   img_Cells.Canvas.Font .Name := gSettings.font.Name;
   img_Cells.Canvas.Font .Style:= gSettings.font.Style;
   //img_Cells.Transparent:= True;

{$IFDEF USE_PICTURES}
   if (gSettings.path.mine <> '') then
      try
         pictures.Mine           .LoadFromFile(gSettings.path.mine);
      except
         pictures.Mine  .Bitmap  .LoadFromResourceName(hInstance,'Mine');   end
   else  pictures.Mine  .Bitmap  .LoadFromResourceName(hInstance,'Mine');
   if (gSettings.path.flag <> '') then
      try
         pictures.Flag           .LoadFromFile(gSettings.path.flag);
      except
         pictures.Flag  .Bitmap  .LoadFromResourceName(hInstance,'Flag');
      end
   else  pictures.Flag  .Bitmap  .LoadFromResourceName(hInstance,'Flag');
   if (gSettings.path.crater <> '') then
      try
         pictures.Crater         .LoadFromFile(gSettings.path.crater);
      except
         pictures.Crater.Bitmap  .LoadFromResourceName(hInstance,'Crater'); end
   else  pictures.Crater.Bitmap  .LoadFromResourceName(hInstance,'Crater');
   if (gSettings.path.pause <> '') then
      try frm_Mines.img_Pause.Picture.LoadFromFile(gSettings.path.pause);
      except end;
   {}
   pictures.Mine  .Graphic.Transparent:= gSettings.transparent.mine;
   pictures.Flag  .Graphic.Transparent:= gSettings.transparent.flag;
   pictures.Crater.Graphic.Transparent:= gSettings.transparent.crater;
   frm_Mines.img_Pause    .Transparent:= gSettings.transparent.pause;
   {}
{$ENDIF}
   mnitm_OptionsCraters   .Checked:= gSettings.useCraters;
   mnitm_OptionsMoving    .Checked:= gSettings.useMoving;
   mnitm_OptionsRandom    .Checked:= gSettings.useRandom;
   mnitm_OptionsUseUnknown.Checked:= gSettings.useUnknown;
   case CurrentSkillLevel of
      SkillLevelBeginner    : mnitm_FileBeginner    .Checked:= True;
      SkillLevelAmateur     : mnitm_FileAmateur     .Checked:= True;
      SkillLevelProfessional: mnitm_FileProfessional.Checked:= True;
      SkillLevelCrazy       : mnitm_FileCrazy       .Checked:= True;
      else                    mnitm_FileCustom      .Checked:= True;
   end;
   frm_Mines.CreateField(Sender);
   if (exception) then begin
      UAbout.frm_About  .ShowModal;
      //ShowWindow( UDialog.frm_Dialog.Handle, SW_SHOW);
      UDialog.frm_Dialog.ShowModal;
   end;
End;

Procedure TFrm_Mines.CreateField(Sender: TObject);
Var i,j, max, skill: Integer;
    winX, winY: Integer;
Begin
   winX:= GetSystemMetrics(SM_CXSCREEN);
   winY:= GetSystemMetrics(SM_CYSCREEN);
   if (gSettings.useMoving) then begin
      prgssbr_TimeMoving.Height:= 10;
      prgssbr_TimeMoving.Position:= 0;
      prgssbr_TimeMoving.Max:= gSettings.timeMoving;
   end else
      prgssbr_TimeMoving.Height:= 0;
   {}
   skill:= Ord(CurrentSkillLevel);
   if (skill <> Ord(SkillLevelCustom)) then begin
      if (((winX-2*GetSystemMetrics(SM_CXFRAME)) div CSizeX[skill]) <
          ((winY-GetSystemMetrics(SM_CYCAPTION)
                -GetSystemMetrics(SM_CYMENU)
                -GetSystemMetrics(SM_CYFRAME)*2
                -pnl_Top.Height
                -prgssbr_TimeMoving.Height
           ) div CSizeY[skill]))
      then max:= (winX-2*GetSystemMetrics(SM_CXFRAME)) div CSizeX[skill]
      else max:= (winY-GetSystemMetrics(SM_CYCAPTION)
                      -GetSystemMetrics(SM_CYMENU)
                      -GetSystemMetrics(SM_CYFRAME)*2
                      -pnl_Top.Height
                      -prgssbr_TimeMoving.Height
                 ) div CSizeY[skill];
      if (gSettings.SizeCell > max) then gSettings.SizeCell:= max;
   end;
   {}
   Self.ClientWidth := gSettings.X*gSettings.SizeCell;
   Self.ClientHeight:= gSettings.Y*gSettings.SizeCell + pnl_Top.Height + prgssbr_TimeMoving.Height;
   Self.Left:= winX div 2 - Self.Width  div 2;
   Self.Top := winY div 2 - Self.Height div 2;
   img_Cells.Width := gSettings.X*gSettings.SizeCell;
   img_Cells.Height:= gSettings.Y*gSettings.SizeCell;
   SetLength(Field, gSettings.X, gSettings.Y); // Delphi5
   for i:= 0 to gSettings.X-1 do
      for j:= 0 to gSettings.Y-1 do
         Field[i,j]:= TCell.CreateCell(i,j);
   Pause:= False;
   Self.bttn_NewClick(Sender);
End;

Procedure Tfrm_Mines.bttn_NewClick(Sender: TObject);
Var i,j: Integer;
Begin
   if (Pause) then begin
      bttn_Pause.Down:= False;
      bttn_PauseClick(Sender);
      Exit;
   end;
{$IFDEF USE_PICTURES}
   bttn_New  .Glyph:= pictures.New1  .Bitmap;
   bttn_Pause.Glyph:= pictures.Pause1.Bitmap;
{$ENDIF}
   gGameRun:= False;
   frm_Mines.Caption:= CFormCaption + ' - ' + gSettings.currPlayer;
   img_Cells.Show;
   img_Pause.Hide;
   pnl_Field.Enabled:= True; // enable all cell
   // pause
   Pause             := False;
   bttn_Pause.Down   := False;
   bttn_Pause.Enabled:= False;
   // timer
   Timer.Enabled   := False;
   Timer.DesignInfo:= 0;
   edt_Timer.Text:= IntToStr(Timer.DesignInfo);
   //
   prgssbr_TimeMoving.Position:= 0;
   CircleHint(0);
   // cell
      // beginner reset
   for i:= 0 to gSettings.X-1 do
      for j:= 0 to gSettings.Y-1 do begin
         Field[i,j].State    := _Close;
         Field[i,j].CellOpen := _Null;
         Field[i,j].CellClose:= _Clear;
         Field[i,j].Crater   := False;
         Field[i,j].Random   := False;
         Field[i,j].Down     := False;
         Field[i,j].Caption  := '';
         Field[i,j].Paint;
     end;
   gCountFlag := 0;
   gCountOpen := 0;
   gCountClick:= 0;
   edt_Count.Text:= IntToStr(gSettings.maxMines-gCountFlag);
   edt_Count.Hint:= ChangeHint;
End;

Procedure TFrm_Mines.EndGame( Victory: Boolean );
Var i,j: Integer;
Begin
   if (not gGameRun) then Exit;
   gGameRun:= False;
   Timer.Enabled:= False;
   // открыть оставшeес€
   if (Victory) then begin
      for i:= 0 to gSettings.X-1 do
         for j:= 0 to gSettings.Y-1 do begin
            if (Field[i,j].State = _Close) then begin
               if (Field[i,j].CellOpen = _Mine) then
                  Field[i,j].CellClose:= _Flag
               else begin
                  Field[i,j].State:= _Open;
                  Field[i,j].Down:= True
               end;
               Field[i,j].Paint;
            end;
         end;
      gCountFlag:= gSettings.maxMines;
      frm_Mines.edt_Count.Text:= '0';
      frm_Mines.edt_Count.Hint:= ChangeHint;
   end else begin
      for i:= 0 to gSettings.X-1 do
         for j:= 0 to gSettings.Y-1 do
            if (Field[i,j].State = _Close) then begin
               if (Field[i,j].CellClose = _Flag) and
                  (Field[i,j].CellOpen  = _Mine) then Field[i,j].State:= _Close
               else Field[i,j].State:= _Open;
               Field[i,j].Paint;
            end;
   end;
   pnl_Field.Enabled:= False; // disable all cell
   frm_Mines.bttn_Pause.Enabled:= False;
   Beep;
   if Victory then begin
{$IFDEF USE_PICTURES}
      bttn_New.Glyph:= pictures.New3.Bitmap;
{$ENDIF}
      frm_Mines.Caption:= CFormCaption + ' - Victory';
   end else begin
{$IFDEF USE_PICTURES}
      bttn_New.Glyph:= pictures.New4.Bitmap;
{$ENDIF}
      frm_Mines.Caption:= CFormCaption + ' - Defeat';
   end;
   SaveResult(Victory );
End;

Procedure TFrm_Mines.edt_CountChange(Sender: TObject);
Var i,j: Integer;
Begin
   if (gSettings.maxMines = gCountFlag) and (gGameRun) then begin
      for i:= 0 to gSettings.X-1 do
         for j:= 0 to gSettings.Y-1 do
            if (Field[i,j].CellClose = _Flag) and
               (Field[i,j].CellOpen <> _Mine) then
               Exit; // неверно проставленный флажок - на выход
      EndGame(CVictory);
   end;
End;

Procedure Tfrm_Mines.mnitm_FileExitClick(Sender: TObject);
Begin
   frm_Mines.Close;
End;

Procedure Tfrm_Mines.mnitm_FileBeginnerClick(Sender: TObject);
Begin
   if (mnitm_FileBeginner.Checked) then begin
      bttn_NewClick(Sender);
      Exit;
   end;
   mnitm_FileBeginner.Checked:= True;
   frm_Mines.DestroyField(Sender);
   gSettings.X         := CSizeX[0];
   gSettings.Y         := CSizeY[0];
   gSettings.maxMines  := gSettings.X*gSettings.Y*CMines  [0] div 100;
   gSettings.maxCraters:= gSettings.X*gSettings.Y*CCraters[0] div 100;
   gSettings.maxMoving := gSettings.maxMines     *CMoving [0] div 100;
   gSettings.timeMoving:= CMovingTime[0];
   frm_Mines.CreateField(Sender);
End;

Procedure Tfrm_Mines.mnitm_FileAmateurClick(Sender: TObject);
Begin
   if (mnitm_FileAmateur.Checked) then begin
      bttn_NewClick(Sender);
      Exit;
   end;
   mnitm_FileAmateur.Checked:= True;
   frm_Mines.DestroyField(Sender);
   gSettings.X         := CSizeX[1];
   gSettings.Y         := CSizeY[1];
   gSettings.maxMines  := gSettings.X*gSettings.Y*CMines  [1] div 100;
   gSettings.maxCraters:= gSettings.X*gSettings.Y*CCraters[1] div 100;
   gSettings.maxMoving := gSettings.maxMines     *CMoving [1] div 100;
   gSettings.timeMoving:= CMovingTime[1];
   frm_Mines.CreateField(Sender);
End;

Procedure Tfrm_Mines.mnitm_FileProfessionalClick(Sender: TObject);
Begin
   if (mnitm_FileProfessional.Checked) then begin
      bttn_NewClick(Sender);
      Exit;
   end;
   mnitm_FileProfessional.Checked:= True;
   frm_Mines.DestroyField(Sender);
   gSettings.X         := CSizeX[2];
   gSettings.Y         := CSizeY[2];
   gSettings.maxMines  := gSettings.X*gSettings.Y*CMines  [2] div 100;
   gSettings.maxCraters:= gSettings.X*gSettings.Y*CCraters[2] div 100;
   gSettings.maxMoving := gSettings.maxMines     *CMoving [2] div 100;
   gSettings.timeMoving:= CMovingTime[2];
   frm_Mines.CreateField(Sender);
End;

Procedure Tfrm_Mines.mnitm_FileCrazyClick(Sender: TObject);
Begin
   if (mnitm_FileCrazy.Checked) then begin
      bttn_NewClick(Sender);
      Exit;
   end;
   mnitm_FileCrazy.Checked:= True;
   frm_Mines.DestroyField(Sender);
   gSettings.X         := CSizeX[3];
   gSettings.Y         := CSizeY[3];
   gSettings.maxMines  := gSettings.X*gSettings.Y*CMines  [3] div 100;
   gSettings.maxCraters:= gSettings.X*gSettings.Y*CCraters[3] div 100;
   gSettings.maxMoving := gSettings.maxMines     *CMoving [3] div 100;
   gSettings.timeMoving:= CMovingTime[3];
   frm_Mines.CreateField(Sender);
End;

Procedure Tfrm_Mines.mnitm_FileCustomClick(Sender: TObject);
Begin
   //mnitm_FileCustom.Checked:= True;
   frm_CustomSkill.ShowModal;
End;

Procedure Tfrm_Mines.mnitm_HelpAboutClick(Sender: TObject);
Begin
   frm_About.ShowModal;
End;

Procedure Tfrm_Mines.mnitm_HelpChampionsClick(Sender: TObject);
Begin
   frm_Champions.ShowModal;
End;

Procedure TFrm_Mines.mnitm_HelpStatisticsClick(Sender: TObject);
Var i: Integer;
Begin
   if (UStatistics.Statistics <> Nil) then begin
      // выдел€ю в таблице €чейку с именем текущего пользовател€
      for i:= 0 to High(UStatistics.Statistics) do
         if (UStatistics.Statistics[i].Name = gSettings.currPlayer ) then begin
            frm_Statistics.strgrd_Beginner    .Row:= i+1;
            frm_Statistics.strgrd_Amateur     .Row:= i+1;
            frm_Statistics.strgrd_Professional.Row:= i+1;
            frm_Statistics.strgrd_Crazy       .Row:= i+1;
            frm_Statistics.strgrd_Beginner    .Col:= 0;
            frm_Statistics.strgrd_Amateur     .Col:= 0;
            frm_Statistics.strgrd_Professional.Col:= 0;
            frm_Statistics.strgrd_Crazy       .Col:= 0;
            Break;
         end;
   end;
   frm_Statistics.ShowModal;
End;

Procedure Tfrm_Mines.FormClose(Sender: TObject; var Action: TCloseAction);
Var F: File of TGameOptions;
Begin
   AssignFile(F, path_to_exe_file + CFileNameInit);
   Rewrite(F);
   Write(F,gSettings);
   CloseFile(F);
End;

Procedure Tfrm_Mines.mnitm_FileMinimizeClick(Sender: TObject);
Begin
   if (gGameRun) and (not Pause) then begin
      bttn_Pause.Down:= True;
      bttn_PauseClick(Sender);
   end;
   Application.Minimize;
End;

Procedure Tfrm_Mines.mnitm_FilePauseClick(Sender: TObject);
Begin
   if (not gGameRun) then Exit;
   if (Pause) then bttn_Pause.Down:= False
              else bttn_Pause.Down:= True;
   bttn_PauseClick(Sender);
End;

{$IFDEF USE_WMActivate}
Procedure TFrm_Mines.WMActivate(var Message: TWMActivate); // message WM_ACTIVATE
Begin
   inherited;
   if (Message.Active = WA_INACTIVE) then begin
      SetWindowPos( frm_Mines.Handle, HWND_BOTTOM,//HWND_NOTOPMOST,
                    0,0,0,0, SWP_NOSIZE or SWP_NOMOVE);
   end else begin
      SetWindowPos( frm_Mines.Handle, HWND_TOPMOST,
                    0,0,0,0, SWP_NOSIZE or SWP_NOMOVE);
      {
      SetWindowPos( frm_Mines.Handle, FindWindow('Shell_TrayWnd',nil),
                    0,0,0,0, SWP_NOSIZE or SWP_NOMOVE );
      SetWindowPos( frm_Mines.Handle, HWND_TOP,
                    0,0,0,0, SWP_NOSIZE or SWP_NOMOVE );
      {}
   end;
//{$IFDEF USE_WMActivate}
   if (gGameRun) and (not Pause) and
      ((Message.Active = WA_INACTIVE) or
       (Message.Minimized = True)) then
   begin
      bttn_Pause.Down:= True;
      bttn_PauseClick(TObject(Message.ActiveWindow));
   end;
//{$ENDIF}
End;
{$ENDIF}

Procedure Tfrm_Mines.mnitm_OptionsCratersClick(Sender: TObject);
Begin
   gSettings.useCraters:= not gSettings.useCraters;
   mnitm_OptionsCraters.Checked:= gSettings.useCraters;
   frm_Mines.bttn_NewClick(Sender);
End;

Procedure Tfrm_Mines.mnitm_OptionsMovingClick(Sender: TObject);
Begin
   gSettings.useMoving:= not gSettings.useMoving;
   mnitm_OptionsMoving.Checked:= gSettings.useMoving;
   //frm_Mines.bttn_NewClick(Sender);
   frm_Mines.CreateField(Sender);
End;

Procedure Tfrm_Mines.mnitm_OptionsRandomClick(Sender: TObject);
Begin
   gSettings.useRandom:= not gSettings.useRandom;
   mnitm_OptionsRandom.Checked:= gSettings.useRandom;
   frm_Mines.bttn_NewClick(Sender);
End;

Procedure Tfrm_Mines.mnitm_OtherOptionsClick(Sender: TObject);
Var i,j: Integer;
    winX, winY: Integer;
    //P: Boolean;
Begin
   //if (pause) then P:= True else P:= False;
   frm_OtherOptions.ShowModal;
   //if (p) then
   if (Pause) then frm_Mines.mnitm_FilePauseClick(Sender);
   //gSettings.useBigCells:= not gSettings.useBigCells;
   //mnitm_OptionsBigCells.Checked:= gSettings.useBigCells;
   for i:= 0 to gSettings.X-1 do
      for j:= 0 to gSettings.Y-1 do begin
         Field[i,j].X1:= gSettings.SizeCell * Field[i,j].i;
         Field[i,j].Y1:= gSettings.SizeCell * Field[i,j].j;
         Field[i,j].X2:= Field[i,j].X1 + gSettings.SizeCell;
         Field[i,j].Y2:= Field[i,j].Y1 + gSettings.SizeCell;
     end;
   winX:= GetSystemMetrics(SM_CXSCREEN);
   winY:= GetSystemMetrics(SM_CYSCREEN);
   frm_Mines.ClientWidth := gSettings.X*gSettings.SizeCell;
   frm_Mines.ClientHeight:= gSettings.Y*gSettings.SizeCell + pnl_Top.Height +
      prgssbr_TimeMoving.Height;
   frm_Mines.Left  := winX div 2 - frm_Mines.Width  div 2;
   frm_Mines.Top   := winY div 2 - frm_Mines.Height div 2;
   img_Cells.Width := gSettings.X*gSettings.SizeCell;
   img_Cells.Height:= gSettings.Y*gSettings.SizeCell;
   isRefresh:= True;
   frm_Mines.Invalidate;
End;

Procedure Tfrm_Mines.mnitm_OptionsUseUnknownClick(Sender: TObject);
Begin
   gSettings.useUnknown:= not gSettings.useUnknown;
   mnitm_OptionsUseUnknown.Checked:= gSettings.useUnknown;
End;

Procedure Tfrm_Mines.MovingMines;
Var count: Integer;
    i, j : Integer;
    m, n : TNeighbor;
Begin
   if (gSemaphore) then begin
      gMustMoving:= True;
      Exit;
   end;
   if (not gGameRun) then Exit;
   // reset random mines
   count:= 0;
   repeat
      i:= Random(gSettings.X);
      j:= Random(gSettings.Y);
      if(Field[i,j].CellOpen = _Mine) then begin
         Field[i,j].CellOpen:= _Null;
         Inc(count);
      end;
   until (count >= gSettings.maxMoving);
   // set random mines
   count:= 0;
   repeat
      i:= Random(gSettings.X);
      j:= Random(gSettings.Y);
      if (Field[i,j].State    =  _Close) and
         (Field[i,j].CellOpen <> _Mine ) and // что бы не установить повторно мину
         (not Field[i,j].Random) then begin // выполн€ю условие - Random не должна располагаетс€ на _Mine €чейке
         Field[i,j].CellOpen:=  _Mine;
         Inc(count);
      end;
   until (count >= gSettings.maxMoving);
   // set other CellOpen and set all Caption
   for i:= 0 to gSettings.X-1 do
      for j:= 0 to gSettings.Y-1 do begin
         if(Field[i,j].CellOpen <> _Mine) then begin
            count:= 0;
            for m:= -1 to 1 do
               for n:= -1 to 1 do
                  if ((m = 0 ) and (  n = 0 )) or
                     (i+m < 0)  or (i+m > gSettings.X-1) or
                     (j+n < 0)  or (j+n > gSettings.Y-1) or
                     (Field[i+m,j+n].CellOpen <> _Mine) then Continue
                  else Inc( count );
            Field[i,j].CellOpen:= TCellOpen(count);
         end;
      end;
   // repaint all
   for i:= 0 to gSettings.X-1 do
      for j:= 0 to gSettings.Y-1 do Field[i,j].Paint;
End;

Procedure TFrm_Mines.WMKeyDown(var Message: TWMKeyDown);
Begin
   inherited;
   if (Message.CharCode = VK_PAUSE) then frm_Mines.mnitm_FilePauseClick(frm_Mines);
   //MessageBox(Self.Handle, PChar(IntToStr(Message.CharCode)), '# virtual key', MB_OK and MB_ICONINFORMATION);
End;

Procedure TFrm_Mines.mnitm_FilePlayerClick(Sender: TObject);
Begin
   UDialog.frm_Dialog.ShowModal;
   Pause:= False;
   bttn_NewClick(Sender);
End;

////////////////////////////////////////////////////////////////////////////////
//                               TImageCells                                  //
////////////////////////////////////////////////////////////////////////////////
Procedure TImageCells.WMLButtonDown(var msg: TWMLButtonDown);
Begin
   inherited;
   //Inc(gCountClick);
   gLmsg:= msg;
   Field[msg.XPos div gSettings.SizeCell, msg.YPos div gSettings.SizeCell].LButtonDown;
End;

Procedure TImageCells.WMLButtonUp(var msg: TWMLButtonUp  );
Var point: TPoint;
Begin
   inherited;
   point.x:= msg.XPos;
   point.y:= msg.YPos;
   Field[gLmsg.XPos div gSettings.SizeCell, gLmsg.YPos div gSettings.SizeCell].LButtonUp(point);
End;

Procedure TImageCells.WMRButtonDown(var msg: TWMRButtonDown);
Begin
   //inherited;
   Field[msg.XPos div gSettings.SizeCell, msg.YPos div gSettings.SizeCell].RButtonDown;
End;

Procedure TImageCells.WMLButtonDblClk(var msg: TWMLButtonDblClk);
Begin
   //inherited;
   Self.WMLButtonDown( msg );
End;

Procedure TImageCells.WMRButtonDblClk(var msg: TWMRButtonDblClk);
Begin
   //inherited;
   Self.WMRButtonDown( msg );
End;

Procedure TImageCells.WMPaint(var Message: TWMPaint);
Var i, j: Integer;
Begin
   inherited;
   if isRefresh then begin
      for i:= 0 to gSettings.X-1 do
         for j:= 0 to gSettings.Y-1 do Field[i,j].Paint;
      isRefresh:= False;
   end;
End;

{$IFDEF USE_CellHint}
Procedure TImageCells.WMMouseMove(var msg: TWMMouseMove);
Var cell: TCell;
Begin
   inherited;
   img_Cells.ShowHint:= True;
   cell:= Field[msg.XPos div gSettings.SizeCell, msg.YPos div gSettings.SizeCell];
   img_Cells.Hint:= '';
   if (gSettings.useCraters) then
      if (cell.Crater) then img_Cells.Hint:= img_Cells.Hint + 'Crater      = True ' + #10#13
                       else img_Cells.Hint:= img_Cells.Hint + 'Crater      = False' + #10#13;
   if (cell.Down)   then img_Cells.Hint:= img_Cells.Hint + 'Down      = True ' + #10#13
                    else img_Cells.Hint:= img_Cells.Hint + 'Down      = False' + #10#13;
   if (gSettings.useRandom) then
      if (cell.Random) then img_Cells.Hint:= img_Cells.Hint + 'Random      = True ' + #10#13
                       else img_Cells.Hint:= img_Cells.Hint + 'Random      = False' + #10#13;
   img_Cells.Hint:= img_Cells.Hint + 'Caption   = "' + cell.Caption + '"' + #10#13;
   case (cell.State) of
      _Close: img_Cells.Hint:= img_Cells.Hint + 'State       = _Close' + #10#13;
      _Open : img_Cells.Hint:= img_Cells.Hint + 'State       = _Open ' + #10#13;
   end;
   case (cell.CellOpen) of
      _Mine: img_Cells.Hint:= img_Cells.Hint + 'CellOpen = _Mine' + #10#13;
      _Null: img_Cells.Hint:= img_Cells.Hint + 'CellOpen = _Null' + #10#13;
      else   img_Cells.Hint:= img_Cells.Hint + 'CellOpen = _' + IntToStr(Integer(cell.CellOpen)) + #10#13;
   end;
   case (cell.CellClose) of
      _Clear  : img_Cells.Hint:= img_Cells.Hint + 'CellClose = _Clear  ' + #10#13;
      _Flag   : img_Cells.Hint:= img_Cells.Hint + 'CellClose = _Flag   ' + #10#13;
      _Unknown: img_Cells.Hint:= img_Cells.Hint + 'CellClose = _Unknown' + #10#13;
   end;
   img_Cells.Hint:= img_Cells.Hint + 'i     = ' + IntToStr(cell.i ) + #10#13;
   img_Cells.Hint:= img_Cells.Hint + 'j     = ' + IntToStr(cell.j ) + #10#13;
   img_Cells.Hint:= img_Cells.Hint + 'X1 = ' + IntToStr(cell.X1) + #10#13;
   img_Cells.Hint:= img_Cells.Hint + 'Y1 = ' + IntToStr(cell.Y1) + #10#13;
   img_Cells.Hint:= img_Cells.Hint + 'X2 = ' + IntToStr(cell.X2) + #10#13;
   img_Cells.Hint:= img_Cells.Hint + 'Y2 = ' + IntToStr(cell.Y2);
End;
{$ENDIF}

////////////////////////////////////////////////////////////////////////////////
//                         Others procedure & function                        //
////////////////////////////////////////////////////////////////////////////////
Function ChangeHint: String;
Begin
   Result:= 'Open: '  + IntToStr(gCountOpen)     + #10#13 +
            'Close: ' + IntToStr(gSettings.X*gSettings.Y-gCountOpen) + #10#13 +
            'Mines: ' + IntToStr(gSettings.maxMines)   + #10#13 +
            'Flags: ' + IntToStr(gCountFlag);
   if (not frm_Mines.mnitm_OptionsCraters.Checked) then Exit;
   Result:= Result + #10#13 + 'Craters: ' + IntToStr(gSettings.maxCraters);
End;

Procedure SaveResult(Victory: Boolean);
Var ChmpnRecord    : TChmpnRecord;
    SttstcSubRecord: TSttstcSubRecord;
    i   : Integer;
    indx: Integer;
Begin
   if (frm_Mines.mnitm_FileCustom.Checked) then Exit;

   frm_Statistics.LoadFile;

   if (Victory) then begin
      SttstcSubRecord.GameBegin  := 1;
      SttstcSubRecord.GameComlete:= 1;
      SttstcSubRecord.OpenField  := CSizeX[Ord(CurrentSkillLevel)]*CSizeY[Ord(CurrentSkillLevel)]-
                                    CSizeX[Ord(CurrentSkillLevel)]*CSizeY[Ord(CurrentSkillLevel)]*
                                    CMines[Ord(CurrentSkillLevel)] div 100;
      SttstcSubRecord.PlayTime   := frm_Mines.Timer.DesignInfo;
      SttstcSubRecord.ClickCount := gCountClick;
   end else begin
      SttstcSubRecord.GameBegin  := 1;
      SttstcSubRecord.GameComlete:= 0;
      SttstcSubRecord.OpenField  := gCountOpen;
      SttstcSubRecord.PlayTime   := frm_Mines.Timer.DesignInfo;
      SttstcSubRecord.ClickCount := gCountClick;
   end;
   frm_Statistics.InsertRecord( frm_Champions.currentPage(True), CurrentSkillLevel, SttstcSubRecord );
   frm_Statistics.SaveFile;

   if (not Victory) then Exit;

   frm_Champions.LoadFile;

   if (Champions[frm_Champions.currentPage(True)][Ord(CurrentSkillLevel)][10].Time <=
      StrToInt(frm_Mines.edt_Timer.Text)) then Exit;

   //frm_Dialog.ShowModal;
   ChmpnRecord.Name:= gSettings.currPlayer;//UDialog.theLatterName;
   ChmpnRecord.Time:= StrToInt(frm_Mines.edt_Timer.Text);
   frm_Champions.InsertRecord( frm_Champions.currentPage(True), CurrentSkillLevel, ChmpnRecord );
   frm_Champions.SaveFile;
{}
   // нахожу индекс вставки
   indx:= 10; // no compiller warning
   for i:= 10 downto 1 do
      if (Champions[frm_Champions.currentPage(True)][Ord(CurrentSkillLevel)][i].Time = StrToInt(frm_Mines.edt_Timer.Text))
      then begin
         indx:= i;
         Break;
      end;
   // выделить в таблице новую запись
   case CurrentSkillLevel of
      SkillLevelBeginner    : frm_Champions.strgrd_Beginner    .Row:= indx;
      SkillLevelAmateur     : frm_Champions.strgrd_Amateur     .Row:= indx;
      SkillLevelProfessional: frm_Champions.strgrd_Professional.Row:= indx;
      SkillLevelCrazy       : frm_Champions.strgrd_Crazy       .Row:= indx;
   end;
   case CurrentSkillLevel of
      SkillLevelBeginner    : frm_Champions.strgrd_Beginner    .Col:= 0;
      SkillLevelAmateur     : frm_Champions.strgrd_Amateur     .Col:= 0;
      SkillLevelProfessional: frm_Champions.strgrd_Professional.Col:= 0;
      SkillLevelCrazy       : frm_Champions.strgrd_Crazy       .Col:= 0;
   end;
{}
   frm_Champions.ShowModal;
End;

Function CurrentSkillLevel: TSkillLevel;
Var i: Integer;
Begin
   Result:= SkillLevelCustom;
   for i:= Low(TSubrangeSkillLevel) to High(TSubrangeSkillLevel) do
      if (gSettings.X = CSizeX[i]) and (gSettings.Y = CSizeY[i]) and
         (gSettings.maxMines = CSizeX[i]*CSizeY[i]*CMines[i] div 100) and
         ( not gSettings.useCraters or
            (gSettings.useCraters and
               (gSettings.maxCraters = CSizeX[i]*CSizeY[i]*CCraters[i] div 100))) and
         ( not gSettings.useMoving or
            (gSettings.useMoving and
               (gSettings.maxMoving = gSettings.maxMines*CMoving[i] div 100) and
               (gSettings.timeMoving = CMovingTime[i])))
      then begin
         Result:= TSkillLevel(i);
         Break;
      end;
End;

Procedure CircleHint(i: Integer);
Var dc: HDC;
   xFrame, yFrame, yMenu, yCaption: Integer;
   oldBrush, newBrush: HBRUSH;
   oldPen  , newPen  : HPEN;
Begin
   xFrame  := GetSystemMetrics(SM_CXFRAME);
   yFrame  := GetSystemMetrics(SM_CYFRAME);
   yMenu   := GetSystemMetrics(SM_CYMENU);
   yCaption:= GetSystemMetrics(SM_CYCAPTION);
   dc:= GetWindowDC(frm_Mines.Handle);
   case i of
      1: begin
            newBrush:= CreateSolidBrush    (RGB(0,255,0));
            newPen  := CreatePen(PS_SOLID,1,RGB(0,255,0));
         end;
      2: begin
            newBrush:= CreateSolidBrush    (RGB(0,0,255));
            newPen  := CreatePen(PS_SOLID,1,RGB(0,0,255));
         end;
      3: begin
            newBrush:= CreateSolidBrush    (RGB(255,0,0));
            newPen  := CreatePen(PS_SOLID,1,RGB(255,0,0));
         end;
      else begin // 0
         newBrush:= CreateSolidBrush(GetSysColor(COLOR_MENU));
         newPen  := CreatePen(PS_SOLID,1,GetSysColor(COLOR_MENU));
      end;
   end;
   oldBrush:= SelectObject(dc, newBrush);
   oldPen  := SelectObject(dc, newPen  );
   if (i=3)or(i=0) then
   Windows.Ellipse(dc,
      frm_Mines.Width-1*xFrame-1*yMenu, yCaption+yFRame,
      frm_Mines.Width-2*xFrame-0*yMenu, yCaption+yMenu);
   if (i=2)or(i=0) then
   Windows.Ellipse(dc,
      frm_Mines.Width-1*xFrame-2*yMenu, yCaption+yFRame,
      frm_Mines.Width-2*xFrame-1*yMenu, yCaption+yMenu);
   if (i=1)or(i=0) then
   Windows.Ellipse(dc,
      frm_Mines.Width-1*xFrame-3*yMenu, yCaption+yFRame,
      frm_Mines.Width-2*xFrame-2*yMenu, yCaption+yMenu);
   ReleaseDC(frm_Mines.Handle,dc);
   SelectObject(dc, oldBrush);
   SelectObject(dc, oldPen  );
   DeleteObject(newBrush);
   DeleteObject(newPen  );
End;
////////////////////////////////////////////////////////////////////////////////
(*
Type String8 = String[8];

Function S8_16(X: Integer): String8;
Var y, i: Byte;
Begin
   S8_16:= '00000000';
   i:= 8;
   repeat
      y:= x and $F;
      x:= x shr 4;
      if (y > 9) then Result[i]:= Char(y+55) //A..F
                 else Result[i]:= Char(y+48);//0..9
      Dec(i);
   until (i = 0) or (x = 0);
End;
*)

Procedure TFrm_Mines.WMSysKeyUp(var Message: TWMSysKeyUp); //message WM_SYSKEYUP;
Begin
   inherited;
   if (Message.CharCode = VK_SNAPSHOT) and (gGameRun) and (not Pause) then begin
      //frm_Mines.mnitm_FilePauseClick(frm_Mines);
      //frm_Mines.img_Pause.Refresh;
      //SendMessage(frm_Mines.Handle, WM_SYSKEYDOWN, Message.CharCode, Message.KeyData);
      {Dialogs.ShowMessage(
         'Message.CharCode = '    + IntToStr(Message.CharCode) + #10#13 +
         'Message.KeyData   = 0x' + S8_16   (Message.KeyData ) + #10#13 +
         'Message.Msg          = '+ IntToStr(Message.Msg     ) + #10#13 );{}
      //frm_Mines.mnitm_FilePauseClick(frm_Mines);
      bttn_NewClick(frm_Mines);
   end;
End;

Procedure TFrm_Mines.WMKeyUp(var Message: TWMKeyUp); //message WM_KEYUP;
Begin
   inherited;
   {Dialogs.ShowMessage(
      'Message.CharCode = '    + IntToStr(Message.CharCode) + #10#13 +
      'Message.KeyData   = 0x' + S8_16   (Message.KeyData ) + #10#13 +
      'Message.Msg          = '+ IntToStr(Message.Msg     ) + #10#13 );{}
   if (Message.CharCode = VK_SNAPSHOT) and (gGameRun) and (not Pause) then begin
      //frm_Mines.mnitm_FilePauseClick(frm_Mines);
      //frm_Mines.img_Pause.Refresh;
      //SendMessage(frm_Mines.Handle, WM_SYSKEYDOWN, Message.CharCode, Message.KeyData);
      {Dialogs.ShowMessage(
         'Message.CharCode = '    + IntToStr(Message.CharCode) + #10#13 +
         'Message.KeyData   = 0x' + S8_16   (Message.KeyData ) + #10#13 +
         'Message.Msg          = '+ IntToStr(Message.Msg     ) + #10#13 );{}
      //frm_Mines.mnitm_FilePauseClick(frm_Mines);
      bttn_NewClick(frm_Mines);
   end;
End;
{}
(**)
END.

