object frm_Statistics: Tfrm_Statistics
  Left = 193
  Top = 108
  BorderStyle = bsSingle
  Caption = 'Statistics'
  ClientHeight = 343
  ClientWidth = 652
  Color = clBtnFace
  Font.Charset = DEFAULT_CHARSET
  Font.Color = clWindowText
  Font.Height = -11
  Font.Name = 'MS Sans Serif'
  Font.Style = []
  OldCreateOrder = False
  PopupMenu = pppmn_Exit
  OnActivate = FormActivate
  OnCreate = FormCreate
  PixelsPerInch = 96
  TextHeight = 13
  object PageControl1: TPageControl
    Left = 0
    Top = 0
    Width = 652
    Height = 306
    ActivePage = tbsht_Beginner
    Align = alClient
    MultiLine = True
    TabOrder = 0
    object tbsht_Beginner: TTabSheet
      Caption = 'Beginner'
      object strgrd_Beginner: TStringGrid
        Left = 0
        Top = 0
        Width = 644
        Height = 278
        TabStop = False
        Align = alClient
        ColCount = 6
        DefaultColWidth = 90
        FixedCols = 0
        RowCount = 11
        Options = [goFixedVertLine, goFixedHorzLine, goVertLine, goHorzLine]
        ScrollBars = ssVertical
        TabOrder = 0
        ColWidths = (
          168
          90
          90
          90
          90
          90)
      end
    end
    object tbsht_Amateur: TTabSheet
      Caption = 'Amateur'
      object strgrd_Amateur: TStringGrid
        Left = 0
        Top = 0
        Width = 644
        Height = 278
        TabStop = False
        Align = alClient
        ColCount = 6
        DefaultColWidth = 90
        FixedCols = 0
        RowCount = 11
        Options = [goFixedVertLine, goFixedHorzLine, goVertLine, goHorzLine]
        ScrollBars = ssVertical
        TabOrder = 0
        ColWidths = (
          168
          90
          90
          90
          90
          90)
      end
    end
    object tbsht_Professional: TTabSheet
      Caption = 'Professional'
      object strgrd_Professional: TStringGrid
        Left = 0
        Top = 0
        Width = 644
        Height = 278
        TabStop = False
        Align = alClient
        ColCount = 6
        DefaultColWidth = 90
        FixedCols = 0
        RowCount = 11
        Options = [goFixedVertLine, goFixedHorzLine, goVertLine, goHorzLine]
        ScrollBars = ssVertical
        TabOrder = 0
        ColWidths = (
          168
          90
          90
          90
          90
          90)
      end
    end
    object tbsht_Crazy: TTabSheet
      Caption = 'Crazy'
      object strgrd_Crazy: TStringGrid
        Left = 0
        Top = 0
        Width = 644
        Height = 278
        TabStop = False
        Align = alClient
        ColCount = 6
        DefaultColWidth = 90
        FixedCols = 0
        RowCount = 11
        Options = [goFixedVertLine, goFixedHorzLine, goVertLine, goHorzLine]
        ScrollBars = ssVertical
        TabOrder = 0
        ColWidths = (
          168
          90
          90
          90
          90
          90)
      end
    end
  end
  object Panel1: TPanel
    Left = 0
    Top = 306
    Width = 652
    Height = 37
    Align = alBottom
    TabOrder = 1
    object bttn_Crater: TSpeedButton
      Left = 0
      Top = 1
      Width = 217
      Height = 34
      AllowAllUp = True
      GroupIndex = -1
      Caption = 'Crater'
      Font.Charset = DEFAULT_CHARSET
      Font.Color = clWindowText
      Font.Height = -16
      Font.Name = 'MS Sans Serif'
      Font.Style = []
      ParentFont = False
      OnClick = bttn_Click
    end
    object bttn_Moving: TSpeedButton
      Left = 217
      Top = 1
      Width = 217
      Height = 34
      AllowAllUp = True
      GroupIndex = -2
      Caption = 'Moving'
      Font.Charset = DEFAULT_CHARSET
      Font.Color = clWindowText
      Font.Height = -16
      Font.Name = 'MS Sans Serif'
      Font.Style = []
      ParentFont = False
      OnClick = bttn_Click
    end
    object bttn_Random: TSpeedButton
      Left = 434
      Top = 1
      Width = 217
      Height = 34
      AllowAllUp = True
      GroupIndex = -3
      Caption = 'Random'
      Font.Charset = DEFAULT_CHARSET
      Font.Color = clWindowText
      Font.Height = -16
      Font.Name = 'MS Sans Serif'
      Font.Style = []
      ParentFont = False
      OnClick = bttn_Click
    end
  end
  object pppmn_Exit: TPopupMenu
    Left = 204
    Top = 64
    object mnitm_Exit: TMenuItem
      Caption = 'Exit'
      ShortCut = 27
      OnClick = mnitm_ExitClick
    end
  end
end
