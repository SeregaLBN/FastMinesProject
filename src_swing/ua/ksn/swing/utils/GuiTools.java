package ua.ksn.swing.utils;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import java.awt.*;

/** Набор инструментов для окончательной шлифовки и придания блеска интерфейсу */
public class GuiTools {
	/** метод принимает массив ссылок на кнопки JButton и придает им нужный отступ от границ слева и справа */
	public static void createRecommendedMargin(JButton[] buttons) {
		for (int i = 0; i < buttons.length; i++) {
			// в объекте Insets хранится расстояние от текста до границ кнопки
			Insets margin = buttons[i].getMargin();
			margin.left = 12;
			margin.right = 12;
			buttons[i].setMargin(margin);
		}
	}

//	/** инструмент для придания группе компонентов одинаковых размеров (минимальных, предпочтительных и максимальных).
//	 *  Компоненты принимают размер самого большого (по ширине) компонента в группе
//	 */
//	public static void makeSameSize(JComponent[] components) {
//		// определение максимального размера
//		int maxSizePos = findMaximumPreferredWidthPosition(components);
//		Dimension maxSize = components[maxSizePos].getPreferredSize();
//		// придание одинаковых размеров
//		for (int i = 0; i < components.length; i++) {
//			components[i].setPreferredSize(maxSize);
//			components[i].setMinimumSize(maxSize);
//			components[i].setMaximumSize(maxSize);
//		}
//	}

	/** инструмент для придания группе компонентов одинаковой ширины (для минимальных, предпочтительных и максимальных размеров).
	 *  Ширина устанавливатся как максимальная ширина одного из компонентов.
	 */
	public static void makeSameWidth(Component[] components) {
		// определение максимальной ширины
		int maxSizePos = findMaximumPreferredWidthPosition(components);
		int maxWidth = components[maxSizePos].getPreferredSize().width;

		// придание одинаковой ширины
		for (int i = 0; i < components.length; i++) {
			Dimension dim;
			dim = components[i].getPreferredSize(); dim.width = maxWidth; components[i].setPreferredSize(dim);
			dim = components[i].getMinimumSize  (); dim.width = maxWidth; components[i].setMinimumSize  (dim);
			dim = components[i].getMaximumSize  (); dim.width = maxWidth; components[i].setMaximumSize  (dim);
		}
	}

	/** позволяет исправить оплошность в размерах текстового поля JTextField */
	public static void fixTextFieldSize(JTextField field) {
		Dimension size = field.getPreferredSize();
		// чтобы текстовое поле по-прежнему могло увеличивать свой размер в длину
		size.width = field.getMaximumSize().width;
		// теперь текстовое поле не станет выше своей оптимальной высоты
		field.setMaximumSize(size);
	}

	private static int findMaximumPreferredWidthPosition(Component[] array) {
		int pos = 0;
		for (int i=1; i<array.length; i++)
			if (array[i].getPreferredSize().width > array[pos].getPreferredSize().width)
				pos = i;
		return pos;
	}

	/** для отладки... */
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
