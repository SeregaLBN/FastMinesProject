package ua.ksn.fmg.view.swing.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import ua.ksn.fmg.controller.types.ESkillLevel;
import ua.ksn.fmg.model.mosaics.EMosaic;
import ua.ksn.fmg.view.swing.Main;
import ua.ksn.fmg.view.swing.model.ReportTableModel;
import ua.ksn.fmg.view.swing.res.Resources;
import ua.ksn.swing.utils.ImgUtils;

abstract class ReportDlg extends JDialog {
	private static final long serialVersionUID = 1L;

	private static final int imgSize = 30;
	protected JTabbedPane tabPanel;
	protected JToggleButton[] btns = new JToggleButton[ESkillLevel.values().length-1];
	private Map<EMosaic, JScrollPane> scrollPanes = new HashMap<EMosaic, JScrollPane>(EMosaic.values().length-1);
	protected ButtonGroup radioGroup;
	protected Main parent;
	private double roteteAngle[] = new double[EMosaic.values().length];
	private Timer rotateTimer;

	private Resources resources;
	private Resources getResources() {
		if (resources == null)
			resources = new Resources();
		return resources;
	}

	public ReportDlg(Main parent, boolean modal) {
		super(parent, "report window...", modal);
		this.parent = (Main) parent;
		initialize(parent);
	}
	public ReportDlg(Main parent, boolean modal, Resources resources) {
		super(parent, "report window...", modal);
		this.parent = (Main) parent;
		this.resources = resources;
		initialize(parent);
	}

	private void initialize(JFrame parent) {
		Object keyBind = "CloseDialog";
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false), keyBind);
        getRootPane().getActionMap().put(keyBind, new AbstractAction() {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) { OnClose(); }
		});

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) { OnClose(); }
		});

		this.setResizable(!false);
		CreateComponents();

		rotateTimer = new Timer(10, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ReportDlg.this.OnNextRotate();
			}
		});
		rotateTimer.start();

//		Dimension preferredSize = this.getPreferredSize();
//		preferredSize.width = Toolkit.getDefaultToolkit().getScreenSize().width-100;
//		this.setPreferredSize(preferredSize);
//		this.setVisible(true);
		// ����� ���������������� ������
		pack();
		this.setLocationRelativeTo(parent);
	}

	private void OnClose() {
		// ��� ������ �� ����������� ���� - ���������� �������
		rotateTimer.stop();
		dispose();
//		System.exit(0);
	}

	protected Dimension getPreferredScrollPaneSize() {
		return new Dimension(450, 100);
	}

	/** ������ ������ � ������ ������������� */
	private void CreateComponents() {
		// 1. ������ ������, ������� ����� ��������� ��� ��������� �������� � ������ ������������
		tabPanel = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
		{
			// ����� ��������� ������� ����������� Java, ���������� �������� ��� ���������� �� ������ ���� �� 12 ��������. 
			// ��������� ������ ����� 
			tabPanel.setBorder(BorderFactory.createEmptyBorder(12,12,2,12));

			for (EMosaic eMosaic: EMosaic.values()) {
				JScrollPane scroll = new JScrollPane();
				tabPanel.addTab(null, getResources().getImgMosaic(eMosaic, false, imgSize,imgSize), scroll, eMosaic.getDescription(false));
				scroll.setPreferredSize(getPreferredScrollPaneSize());
				scrollPanes.put(eMosaic, scroll);
			}

			// �������� ������ ����������� - ����� ���� ������� ���������� �������. ��. getSelectedTable()

			tabPanel.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					ReportDlg.this.OnChangeTab(getSelectedMosaicType());
				}
			});
		}

		// 2. ������ ������ �����
		JPanel panelBottom = new JPanel(new GridLayout(0, isOneLineSkillLevelButtons() ? 4:2, 2, 2));
		{
			panelBottom.setBorder(BorderFactory.createEmptyBorder(2,12,12,12));
			radioGroup = new ButtonGroup();
			for (final ESkillLevel eSkill: ESkillLevel.values()) {
				if (eSkill == ESkillLevel.eCustom)
					continue;
				JToggleButton btn = btns[eSkill.ordinal()] = new JToggleButton(eSkill.getDescription());
				panelBottom.add(btn);
				radioGroup.add(btn);
				btn.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						ReportDlg.this.OnClickBtnSkill(eSkill);
					}
				});
			}
		}

		// �������� ������������ � ����� ����
		getContentPane().add(tabPanel, BorderLayout.CENTER);
		// ��� ������ �����
		getContentPane().add(panelBottom, BorderLayout.SOUTH);
	}

	protected boolean isOneLineSkillLevelButtons() { return false; }

	protected void OnClickBtnSkill(ESkillLevel eSkill) {
//		System.out.println("OnClickBtnSkill: " + eSkill);
		UpdateModel(eSkill);
	}

	protected void OnChangeTab(EMosaic mosaicType) {
//		System.out.println("OnChangeTab: " + mosaicType);
		UpdateModel(getSelectedSkillLevel());
	}

	// �������� ����� ��� �������� ����������� ����
	public static void main(String[] args) {
		new ReportDlg(null, true) {
			private static final long serialVersionUID = 1L; }
		.ShowData(ESkillLevel.eAmateur, EMosaic.eMosaicTriangle1, -1);
	}

	/**
	 * ���������� ����������� ������
	 * @param eSkill
	 * @param eMosaic
	 * @param pos - ������� ������ � ��������, ������� ��������
	 */
	public void ShowData(ESkillLevel eSkill, EMosaic eMosaic, int pos) {
		if (eSkill == ESkillLevel.eCustom)
			eSkill = ESkillLevel.eAmateur; 

		radioGroup.setSelected(btns[eSkill.ordinal()].getModel(), true);
		tabPanel.setSelectedIndex(eMosaic.ordinal());
		UpdateModel(eSkill);

		JTable table = getSelectedTable();
		if (pos != -1)
			table.getSelectionModel().setSelectionInterval(pos, pos);
		else
			table.getSelectionModel().clearSelection();

		this.setVisible(true);
	}

	protected void UpdateModel(ESkillLevel eSkill) {
		getSelectedTableModel().setSkill(eSkill);
	}

	protected ReportTableModel getSelectedTableModel() {
		return (ReportTableModel)getSelectedTable().getModel();
	}

	/** ����� �������� �� �������� ������� */
	protected JTable getSelectedTable() {
		int pos = tabPanel.getSelectedIndex();
		EMosaic eMosaic = EMosaic.fromOrdinal(pos);
		JScrollPane scroll = scrollPanes.get(eMosaic);

		// �������� ���� �� �������? ���� ��� - ������...
		Component cmpnt = scroll.getViewport().getView();
		JTable table = (cmpnt != null) ? (JTable) cmpnt : createTable(eMosaic, scroll);
		return table;
	}

	protected int getTableCellHorizontalAlignment(int row, int column) { return SwingConstants.CENTER; }
	protected int getTableHeaderCellHorizontalAlignment(int column) { return SwingConstants.CENTER; }
	protected int getTableRowHeigt() { return 32; }
	protected int getTableHeaderHeigt() { return 25; }

	protected EMosaic getSelectedMosaicType() {
		int i = tabPanel.getSelectedIndex();
		if (i == -1)
			throw new RuntimeException("dialog Report::getSelectedMosaicType: tabPanel.getSelectedIndex() return -1 ???");
		return EMosaic.fromOrdinal(i);
	}
	protected ESkillLevel getSelectedSkillLevel() {
		ButtonModel model = radioGroup.getSelection();
		for (int i=0; i < btns.length; i++)
			if (model == btns[i].getModel())
				return ESkillLevel.fromOrdinal(i);
		throw new RuntimeException("dialog Report::getSelectedSkillLevel: radioGroup.getSelection() return unknown model ???");
	}

	private void OnNextRotate() {
		if (!this.isVisible())
			return;

		EMosaic mosaicType = getSelectedMosaicType();
		int i = mosaicType.ordinal();
		Icon icon = getResources().getImgMosaic(mosaicType, false);
		// TODO ���������� ��������??? - ��� ������
		icon = ImgUtils.toImgIco(ImgUtils.rotate(ImgUtils.toImg(icon), roteteAngle[i]), imgSize,imgSize);

		roteteAngle[i] = roteteAngle[i] + 1.2;
		if (roteteAngle[i] > 360.)
			roteteAngle[i] = roteteAngle[i] - 360.;

		tabPanel.setIconAt(i, icon);
	}

	protected ReportTableModel createTableModel(EMosaic eMosaic) {
		return new ReportTableModel(eMosaic) {};
	}
	/** ������ �������� ����� �������� � � JScrollPane */
	protected JTable createTable(EMosaic eMosaic, JScrollPane owner) {
		JTable table = new JTable(createTableModel(eMosaic)) {
			private static final long serialVersionUID = 1L;
			@Override
			public TableCellRenderer getCellRenderer(int row, int column) {
				TableCellRenderer rend = super.getCellRenderer(row, column);
				if (rend instanceof JLabel) {
					// ������������ ������ � �����
					((JLabel)rend).setHorizontalAlignment(getTableCellHorizontalAlignment(row, column));
//					((JLabel)rend).setVerticalAlignment(SwingConstants.TOP);
				}
				return rend;
			}
		};

		owner.getViewport().setView(table);

		table.setRowHeight(getTableRowHeigt());
		table.getTableHeader().setPreferredSize(new Dimension(owner.getWidth(), getTableHeaderHeigt()));
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//		((JTextArea)table.getEditorComponent()).setEditable(false);
//		table.getCellEditor().stopCellEditing();// // TODO ������� ??
//		System.out.println(table.getModel()); // javax.swing.table.DefaultTableModel

//		// ���������� ����� ���������� ������� �� ������
//		// TODO ���� ����� � �������������, �� ���� ������ ��������� ������� ��� �������� ���� ���
//		//      � ������������ ������� (����� ������� ��� �����).
//		//      �.�. DefaultTableCellRenderer �������� �������, � ��� ������� ������������ ������ ��������� - �� 
//		//System.out.println(table.getColumnModel().getColumn(0).getHeaderRenderer()); // print null... hmmm
//		javax.swing.table.TableColumnModel tableColumnModel = table.getColumnModel();
//		for (int i=0; i<tableColumnModel.getColumnCount(); i++)
//			tableColumnModel.getColumn(i).setHeaderRenderer(defaultTableCellRenderer);
		return table;
	}

	@Override
	public void setVisible(boolean b) {
		if (!b)
			rotateTimer.stop();
		else
			rotateTimer.start();
		super.setVisible(b);
	}

	public void CleanResource() {
		setVisible(false);
	}

	DefaultTableCellRenderer defaultTableCellRenderer = new CustomHeaderTableCellRenderer();
	/** ��� ����������� ������ ���������� ������� �� ������ */
	class CustomHeaderTableCellRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			Component cmpnt = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if (cmpnt instanceof JLabel) {
				JLabel labl = (JLabel)cmpnt;
				labl.setHorizontalAlignment(ReportDlg.this.getTableHeaderCellHorizontalAlignment(column));
			}
			setBorder(UIManager.getBorder("TableHeader.cellBorder"));
			setForeground(table.getTableHeader().getForeground());
			setBackground(table.getTableHeader().getBackground());
			return cmpnt;
		}
	}
}