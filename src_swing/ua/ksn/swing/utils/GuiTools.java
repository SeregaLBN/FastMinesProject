package ua.ksn.swing.utils;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import java.awt.*;

/** ����� ������������ ��� ������������� �������� � �������� ������ ���������� */
public class GuiTools {
	/** ����� ��������� ������ ������ �� ������ JButton � ������� �� ������ ������ �� ������ ����� � ������ */
	public static void createRecommendedMargin(JButton[] buttons) {
		for (int i = 0; i < buttons.length; i++) {
			// � ������� Insets �������� ���������� �� ������ �� ������ ������
			Insets margin = buttons[i].getMargin();
			margin.left = 12;
			margin.right = 12;
			buttons[i].setMargin(margin);
		}
	}

//	/** ���������� ��� �������� ������ ����������� ���������� �������� (�����������, ���������������� � ������������).
//	 *  ���������� ��������� ������ ������ �������� (�� ������) ���������� � ������
//	 */
//	public static void makeSameSize(JComponent[] components) {
//		// ����������� ������������� �������
//		int maxSizePos = findMaximumPreferredWidthPosition(components);
//		Dimension maxSize = components[maxSizePos].getPreferredSize();
//		// �������� ���������� ��������
//		for (int i = 0; i < components.length; i++) {
//			components[i].setPreferredSize(maxSize);
//			components[i].setMinimumSize(maxSize);
//			components[i].setMaximumSize(maxSize);
//		}
//	}

	/** ���������� ��� �������� ������ ����������� ���������� ������ (��� �����������, ���������������� � ������������ ��������).
	 *  ������ �������������� ��� ������������ ������ ������ �� �����������.
	 */
	public static void makeSameWidth(Component[] components) {
		// ����������� ������������ ������
		int maxSizePos = findMaximumPreferredWidthPosition(components);
		int maxWidth = components[maxSizePos].getPreferredSize().width;

		// �������� ���������� ������
		for (int i = 0; i < components.length; i++) {
			Dimension dim;
			dim = components[i].getPreferredSize(); dim.width = maxWidth; components[i].setPreferredSize(dim);
			dim = components[i].getMinimumSize  (); dim.width = maxWidth; components[i].setMinimumSize  (dim);
			dim = components[i].getMaximumSize  (); dim.width = maxWidth; components[i].setMaximumSize  (dim);
		}
	}

	/** ��������� ��������� ���������� � �������� ���������� ���� JTextField */
	public static void fixTextFieldSize(JTextField field) {
		Dimension size = field.getPreferredSize();
		// ����� ��������� ���� ��-�������� ����� ����������� ���� ������ � �����
		size.width = field.getMaximumSize().width;
		// ������ ��������� ���� �� ������ ���� ����� ����������� ������
		field.setMaximumSize(size);
	}

	private static int findMaximumPreferredWidthPosition(Component[] array) {
		int pos = 0;
		for (int i=1; i<array.length; i++)
			if (array[i].getPreferredSize().width > array[pos].getPreferredSize().width)
				pos = i;
		return pos;
	}

	/** ��� �������... */
	@Deprecated
	public static Border getDummyBorder(Color clr) {
//		return BorderFactory.createEmptyBorder();
//		b.setBorder(BorderFactory.createLoweredBevelBorder());
//		b.setBorder(new CompoundBorder(BorderFactory.createRaisedBevelBorder(), BorderFactory.createEmptyBorder(2, 2, 2, 2)));
		return new CompoundBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, clr), new EmptyBorder(2, 2, 2, 2));
//		b.setBorder(BorderFactory.createEtchedBorder());
	}

	public static void alert(Component parentComponent, String msg) {
		JOptionPane.showMessageDialog(parentComponent, msg);
	}
	public static void alert(String msg) {
		JOptionPane.showMessageDialog(null, msg);
	}
//	public static void alert(String msg) {
//		JOptionPane.showMessageDialog(null, msg, "Info", JOptionPane.QUESTION_MESSAGE, null);
//	}
}
