UNIT UStatistics;

INTERFACE

USES
  Windows, Messages, SysUtils, Classes, Graphics, Controls, Forms, Dialogs,
  Buttons, Grids, ComCtrls, UMines, UChampions, Menus, ExtCtrls;

CONST
   CFileNameStatistics = 'Mines.stc'; // Mines.Statistics

TYPE
   TSttstcSubRecord = record
                       GameBegin,    // начато игр
                       GameComlete,  // закончено (выиграно) игр
                       OpenField,    // суммарное число открытых €чеек - вывожу средний процент открыти€ пол€
                       PlayTime,     // суммарное врем€ игр - вывожу сколько всреднем игрок провЄл времени за данной игрой
                       ClickCount    // суммарное число кликов - вывожу среднее число кликов в данной игре
                        : Longword; //	0..4294967295
                    end;
   TSttstcPage    = array [TSubrangeSkillLevel] of TSttstcSubRecord;
   TSttstcAllPage = array [0..UChampions.CMaxPage] of TSttstcPage;
   TSttstcRecord  = record
                       Name: TNamePlayer;
                       AllPage: TSttstcAllPage;
                    end;
   TSttstcFile    = File of TSttstcRecord;

   Tfrm_Statistics = Class(TForm)
      PageControl1       : TPageControl;
      tbsht_Beginner     : TTabSheet;
      tbsht_Amateur      : TTabSheet;
      tbsht_Professional : TTabSheet;
      tbsht_Crazy        : TTabSheet;
      pppmn_Exit         : TPopupMenu;
      mnitm_Exit         : TMenuItem;
      Panel1             : TPanel;
      bttn_Crater        : TSpeedButton;
      bttn_Moving        : TSpeedButton;
      bttn_Random        : TSpeedButton;
      strgrd_Beginner    : TStringGrid;
      strgrd_Amateur     : TStringGrid;
      strgrd_Professional: TStringGrid;
      strgrd_Crazy       : TStringGrid;
      procedure FormCreate     (Sender: TObject);
      procedure FormActivate   (Sender: TObject);
      procedure mnitm_ExitClick(Sender: TObject);
      procedure bttn_Click     (Sender: TObject);
      Procedure LoadFile;
      Procedure SaveFile;
      Function  currentPage(select: Boolean): Byte;
      Procedure ShowPage(numberPage: Byte);
      Procedure InsertRecord( numberPage: Byte;
                              skill     : TSkillLevel;
                              newRecord : TSttstcSubRecord );
   private
      { Private declarations }
   public
      { Public declarations }
   End;

VAR
  frm_Statistics: Tfrm_Statistics;
  Statistics: array of TSttstcRecord;

IMPLEMENTATION

{$R *.DFM}

Procedure Tfrm_Statistics.FormCreate(Sender: TObject);
Var winX, winY: Integer;
Begin
   winX:= GetSystemMetrics(SM_CXSCREEN);
   winY:= GetSystemMetrics(SM_CYSCREEN);
   Self.Left:= winX div 2 - Self.Width  div 2;
   Self.Top := winY div 2 - Self.Height div 2;

   strgrd_Beginner    .Cells[0,0]:= ' Name';
   strgrd_Amateur     .Cells[0,0]:= ' Name';
   strgrd_Professional.Cells[0,0]:= ' Name';
   strgrd_Crazy       .Cells[0,0]:= ' Name';

   strgrd_Beginner    .Cells[1,0]:= ' Begin Game';
   strgrd_Amateur     .Cells[1,0]:= ' Begin Game';
   strgrd_Professional.Cells[1,0]:= ' Begin Game';
   strgrd_Crazy       .Cells[1,0]:= ' Begin Game';

   strgrd_Beginner    .Cells[2,0]:= ' Win Game';
   strgrd_Amateur     .Cells[2,0]:= ' Win Game';
   strgrd_Professional.Cells[2,0]:= ' Win Game';
   strgrd_Crazy       .Cells[2,0]:= ' Win Game';

   strgrd_Beginner    .Cells[3,0]:= ' Open (max ' + IntToStr(100-CMines[0]) + '%)';
   strgrd_Amateur     .Cells[3,0]:= ' Open (max ' + IntToStr(100-CMines[1]) + '%)';
   strgrd_Professional.Cells[3,0]:= ' Open (max ' + IntToStr(100-CMines[2]) + '%)';
   strgrd_Crazy       .Cells[3,0]:= ' Open (max ' + IntToStr(100-CMines[3]) + '%)';

   strgrd_Beginner    .Cells[4,0]:= ' Time';
   strgrd_Amateur     .Cells[4,0]:= ' Time';
   strgrd_Professional.Cells[4,0]:= ' Time';
   strgrd_Crazy       .Cells[4,0]:= ' Time';

   strgrd_Beginner    .Cells[5,0]:= ' Click';
   strgrd_Amateur     .Cells[5,0]:= ' Click';
   strgrd_Professional.Cells[5,0]:= ' Click';
   strgrd_Crazy       .Cells[5,0]:= ' Click';
End;

Procedure Tfrm_Statistics.FormActivate(Sender: TObject);
Begin
   bttn_Crater.Down:= frm_Mines.mnitm_OptionsCraters.Checked;
   bttn_Moving.Down:= frm_Mines.mnitm_OptionsMoving .Checked;
   bttn_Random.Down:= frm_Mines.mnitm_OptionsRandom .Checked;

   LoadFile;
   ShowPage(currentPage(True));
   // открыть закладку соответствующую текущему SkillLevel
   if ( CurrentSkillLevel <> SkillLevelCustom) then
      Self.PageControl1.Pages[Ord(CurrentSkillLevel)].Show;
End;

Procedure Tfrm_Statistics.mnitm_ExitClick(Sender: TObject);
Begin
   Self.Close;
End;

Procedure Tfrm_Statistics.InsertRecord( numberPage: Byte; skill: TSkillLevel;
                                        newRecord: TSttstcSubRecord );
Var i, j, h: Integer;
   currRecord: ^TSttstcSubRecord;
Begin
   if(UMines.gSettings.currPlayer = '') then
      UMines.gSettings.currPlayer:= 'Anonymous';
   if (Statistics = Nil) then
      SetLength(Statistics, 1)
   else begin
      for i:= 0 to High(Statistics) do
         if (Statistics[i].Name = UMines.gSettings.currPlayer) then begin
            currRecord:= @(Statistics[i].AllPage[numberPage][Ord(skill)]);
            currRecord.GameBegin  := currRecord.GameBegin   + newRecord.GameBegin;
            currRecord.GameComlete:= currRecord.GameComlete + newRecord.GameComlete;
            currRecord.OpenField  := currRecord.OpenField   + newRecord.OpenField;
            currRecord.PlayTime   := currRecord.PlayTime    + newRecord.PlayTime;
            currRecord.ClickCount := currRecord.ClickCount  + newRecord.ClickCount;
            Exit;
         end;
// new record
      SetLength(Statistics, Length(Statistics)+1);
      if (Length(Statistics) > 10) then begin
         strgrd_Beginner    .RowCount:= Length(Statistics)+1;
         strgrd_Amateur     .RowCount:= Length(Statistics)+1;
         strgrd_Professional.RowCount:= Length(Statistics)+1;
         strgrd_Crazy       .RowCount:= Length(Statistics)+1;
      end;
   end;
   h:= High(Statistics);
   Statistics[h].Name:= UMines.gSettings.currPlayer;
   for i:= 0 to UChampions.CMaxPage do
      for j:= Ord(Low(TSubrangeSkillLevel)) to Ord(High(TSubrangeSkillLevel)) do begin
         if (i = numberPage) and (j = Ord(skill)) then begin
            Statistics[h].AllPage[i][j].GameBegin  := newRecord.GameBegin;
            Statistics[h].AllPage[i][j].GameComlete:= newRecord.GameComlete;
            Statistics[h].AllPage[i][j].OpenField  := newRecord.OpenField;
            Statistics[h].AllPage[i][j].PlayTime   := newRecord.PlayTime;
            Statistics[h].AllPage[i][j].ClickCount := newRecord.ClickCount;
         end else begin
            Statistics[h].AllPage[i][j].GameBegin  := 0;
            Statistics[h].AllPage[i][j].GameComlete:= 0;
            Statistics[h].AllPage[i][j].OpenField  := 0;
            Statistics[h].AllPage[i][j].PlayTime   := 0;
            Statistics[h].AllPage[i][j].ClickCount := 0;
         end;
      end;
End;

Procedure Tfrm_Statistics.LoadFile;
Var F: TSttstcFile;
    i: Integer;
Begin
   AssignFile( F, path_to_exe_file + CFileNameStatistics );
   if FileExists( path_to_exe_file + CFileNameStatistics ) then begin
      Reset(F);
      SetLength(Statistics, FileSize(F));
      for i:= 0 to High(Statistics) do
         Read(F, Statistics[i]);
      CloseFile(F);
   end;
End;

Procedure Tfrm_Statistics.SaveFile;
Var F: TSttstcFile;
    i: Integer;
Begin
   AssignFile(F, path_to_exe_file + CFileNameStatistics);
   Rewrite(F);
   for i:= 0 to High(Statistics) do
      Write(F, Statistics[i]);
   CloseFile(F);
End;

Function Tfrm_Statistics.currentPage(select: Boolean): Byte;
Begin
   //select == True  -  menu
   //select == False -  button
   Result:= 0;
   if (select) then begin
      if (frm_Mines.mnitm_OptionsCraters.Checked) then Result:= Result+1;
      if (frm_Mines.mnitm_OptionsMoving .Checked) then Result:= Result+2;
      if (frm_Mines.mnitm_OptionsRandom .Checked) then Result:= Result+4;
   end else begin
      if (bttn_Crater.Down)                       then Result:= Result+1;
      if (bttn_Moving.Down)                       then Result:= Result+2;
      if (bttn_Random.Down)                       then Result:= Result+4;
   end;
End;

Procedure Tfrm_Statistics.ShowPage(numberPage: Byte);
Var i, j: Integer;
    grid: TStringGrid;
    zzz: Currency;
Begin
   // вывод результатов
   for i:= Low(TSubrangeSkillLevel) to High(TSubrangeSkillLevel) do begin
      case TSkillLevel(i) of
         SkillLevelBeginner    : grid:= strgrd_Beginner;
         SkillLevelAmateur     : grid:= strgrd_Amateur;
         SkillLevelProfessional: grid:= strgrd_Professional;
         else                    grid:= strgrd_Crazy; // SkillLevelCrazy
      end;
      for j:= 0 to High(Statistics) do begin
         grid.Cells[0,j+1]:= Statistics[j].Name;
         grid.Cells[1,j+1]:= IntToStr(Statistics[j].AllPage[numberPage][i].GameBegin);

         if (Statistics[j].AllPage[numberPage][i].GameBegin = 0)
         then zzz:= 0
         else zzz:= Statistics[j].AllPage[numberPage][i].GameComlete * 100 /
                    Statistics[j].AllPage[numberPage][i].GameBegin;
         grid.Cells[2,j+1]:= IntToStr(Statistics[j].AllPage[numberPage][i].GameComlete)+
                     ' / ' + CurrToStrF(zzz, ffFixed, 2) + '%';

         if (Statistics[j].AllPage[numberPage][i].GameBegin = 0)
         then zzz:= 0
         else zzz:= (Statistics[j].AllPage[numberPage][i].OpenField /
                     Statistics[j].AllPage[numberPage][i].GameBegin )* 100 / (CSizeX[i]*CSizeY[i]);
         grid.Cells[3,j+1]:= CurrToStrF(zzz, ffFixed, 2) + '%';

         if (Statistics[j].AllPage[numberPage][i].GameBegin = 0)
         then zzz:= 0
         else zzz:= Statistics[j].AllPage[numberPage][i].PlayTime /
                    Statistics[j].AllPage[numberPage][i].GameBegin;
         grid.Cells[4,j+1]:= CurrToStrF(zzz, ffFixed, 2);

         if (Statistics[j].AllPage[numberPage][i].GameBegin = 0)
         then zzz:= 0
         else zzz:= Statistics[j].AllPage[numberPage][i].ClickCount /
                    Statistics[j].AllPage[numberPage][i].GameBegin;
         grid.Cells[5,j+1]:= CurrToStrF(zzz, ffFixed, 2);
      end;
   end;
End;

Procedure Tfrm_Statistics.bttn_Click(Sender: TObject);
Begin
   ShowPage(currentPage(False));
End;

END.
