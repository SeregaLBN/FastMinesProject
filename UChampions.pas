UNIT UChampions;

INTERFACE

USES
  Windows, Forms, UMines, SysUtils, Menus, Buttons, Classes, Controls, Grids, ComCtrls;

CONST
   CFileNameChampion = 'Mines.bst'; // Mines.best
   CMaxPage = 7; // 0..7

TYPE
   TChmpnRecord    = record
                       Name: String[20];
                       Time: Integer;
                    end;
   TChmpnBookmark      = array [1..10] of TChmpnRecord;
   TChmpnPage          = array [TSubrangeSkillLevel] of TChmpnBookmark;
   TChmpnAllPage       = array [0..CMaxPage] of TChmpnPage;
   TChmpnFileChampions = File of TChmpnAllPage;

   Tfrm_Champions = Class(TForm)
      PageControl1       : TPageControl;
      tbsht_Beginner     : TTabSheet;
      tbsht_Amateur      : TTabSheet;
      tbsht_Professional : TTabSheet;
      tbsht_Crazy        : TTabSheet;
      strgrd_Beginner    : TStringGrid;
      strgrd_Amateur     : TStringGrid;
      strgrd_Professional: TStringGrid;
      strgrd_Crazy       : TStringGrid;
      pppmn_Exit         : TPopupMenu;
      mnitm_Exit         : TMenuItem;
      bttn_Crater        : TSpeedButton;
      bttn_Moving        : TSpeedButton;
      bttn_Random        : TSpeedButton;
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
                              newRecord : TChmpnRecord );
   private
      { Private declarations }
   public
      { Public declarations }
   End;

VAR
  frm_Champions: Tfrm_Champions;
  Champions: TChmpnAllPage;

IMPLEMENTATION

{$R *.DFM}

Procedure Tfrm_Champions.FormCreate(Sender: TObject);
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

   strgrd_Beginner    .Cells[1,0]:= ' Time';
   strgrd_Amateur     .Cells[1,0]:= ' Time';
   strgrd_Professional.Cells[1,0]:= ' Time';
   strgrd_Crazy       .Cells[1,0]:= ' Time';
End;

Procedure Tfrm_Champions.FormActivate(Sender: TObject);
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

Procedure Tfrm_Champions.ShowPage(numberPage: Byte);
Var i, j: Integer;
    grid: TStringGrid;
Begin
   // вывод результатов
   for i:= Low(TSubrangeSkillLevel) to High(TSubrangeSkillLevel) do begin
      case TSkillLevel(i) of
         SkillLevelBeginner    : grid:= strgrd_Beginner;
         SkillLevelAmateur     : grid:= strgrd_Amateur;
         SkillLevelProfessional: grid:= strgrd_Professional;
         else                    grid:= strgrd_Crazy; // SkillLevelCrazy
      end;
      for j:= 1 to 10 do begin
         grid.Cells[0,j]:= Champions[numberPage][i][j].Name;
         if (Champions[numberPage][i][j].Time = High(Integer))
            then grid.Cells[1,j]:= ''
            else grid.Cells[1,j]:= SysUtils.IntToStr(Champions[numberPage][i][j].Time);
      end;
   end;
End;

Procedure Tfrm_Champions.InsertRecord( numberPage: Byte; skill: TSkillLevel;
                                       newRecord: TChmpnRecord );
Var j: Integer;
Begin
   if(Champions[numberPage][Ord(skill)][10].Time <= newRecord.Time) then Exit;
   j:= 10;
   repeat
      if(Champions[numberPage][Ord(skill)][j-1].Time > newRecord.Time) then begin
         Champions[numberPage][Ord(skill)][j]:= Champions[numberPage][Ord(skill)][j-1];
      end else begin
         Champions[numberPage][Ord(skill)][j]:= newRecord;
         Break;
      end;
      Dec(j);
   until (j = 1);
   if (j = 1) then Champions[numberPage][Ord(skill)][j]:= newRecord;
End;

Procedure Tfrm_Champions.LoadFile;
Var F: TChmpnFileChampions;
    i, j, k: Integer;
Begin
   AssignFile(F, path_to_exe_file + CFileNameChampion);
   try
      Reset(F);
      Read(F, Champions);
      CloseFile(F);
   except
      // зделать пустую таблицу
      for k:= 0 to CMaxPage do // number page
         for i:= Low(TSubrangeSkillLevel) to High(TSubrangeSkillLevel) do // skill level
            for j:= 1 to 10 do begin // all record
               Champions[k][i][j].Name:= '';
               Champions[k][i][j].Time:= High(Integer);
            end;
   end;
End;

Procedure Tfrm_Champions.SaveFile;
Var F: TChmpnFileChampions;
Begin
   AssignFile(F, path_to_exe_file + CFileNameChampion);
   Rewrite(F);
   Write(F, Champions);
   CloseFile(F);
End;

Procedure Tfrm_Champions.mnitm_ExitClick(Sender: TObject);
Begin
   Self.Close;
End;

Procedure Tfrm_Champions.bttn_Click(Sender: TObject);
Begin
   ShowPage(currentPage(False));
End;

Function Tfrm_Champions.currentPage(select: Boolean): Byte;
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

END.
