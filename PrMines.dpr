PROGRAM PrMines;

uses
  Forms,
  UMines in 'UMines.pas' {frm_Mines},
  UCustomSkill in 'UCustomSkill.pas' {frm_CustomSkill},
  UDialog in 'UDialog.pas' {frm_Dialog},
  UAbout in 'UAbout.pas' {frm_About},
  UChampions in 'UChampions.pas' {frm_Champions},
  UOtherOptions in 'UOtherOptions.pas' {frm_OtherOptions},
  UChangeObj in 'UChangeObj.pas' {frm_ChangeObject},
  UStatistics in 'UStatistics.pas' {frm_Statistics};

{$R *.RES}

BEGIN
   Application.Initialize;
   Application.Title:= 'FastMines';
   Application.CreateForm(Tfrm_Mines       , frm_Mines);
   Application.CreateForm(Tfrm_Dialog      , frm_Dialog);
   Application.CreateForm(Tfrm_CustomSkill , frm_CustomSkill);
   Application.CreateForm(Tfrm_About       , frm_About);
   Application.CreateForm(Tfrm_Champions   , frm_Champions);
   Application.CreateForm(Tfrm_OtherOptions, frm_OtherOptions);
   Application.CreateForm(Tfrm_ChangeObject, frm_ChangeObject);
   Application.CreateForm(Tfrm_Statistics  , frm_Statistics);
   Application.Run;
END.
