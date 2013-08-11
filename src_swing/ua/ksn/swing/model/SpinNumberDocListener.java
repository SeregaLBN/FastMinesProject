package ua.ksn.swing.model;

import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/** Не даю возможность вводить в JSpinner значения вне пределов его SpinnerNumberModel */
public class SpinNumberDocListener implements DocumentListener {

	private JSpinner spin;

	/** Не даю возможность вводить в JSpinner значения вне пределов его SpinnerNumberModel */
	public SpinNumberDocListener(JSpinner ownerSpin) {
		this.spin = ownerSpin;
	}
	
	@Override
	public void insertUpdate(DocumentEvent e) { OnChangeTextSpin(e); } // System.out.println("insertUpdate"); } //
	@Override
	public void removeUpdate(DocumentEvent e) {
		final DocumentEvent e2 = e;
		// System.out.println("removeUpdate");
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				OnChangeTextSpin(e2);
			}
		});
	}
	@Override
	public void changedUpdate(DocumentEvent e) {} // System.out.println("changedUpdate");

	protected boolean OnChangeTextSpin(DocumentEvent e) {
//		System.out.println("OnChangeTextSpin: " + e.getDocument());
		final JTextField txtFld = ((JSpinner.DefaultEditor)spin.getEditor()).getTextField();
		SpinnerNumberModel model = (SpinnerNumberModel) spin.getModel();

		String val = txtFld.getText();
//		System.out.format("val '%1$s'; curr %2$d; min %3$d; max %4$d \r\n", val, model.getValue(), model.getMinimum(), model.getMaximum());
		if ((val == null) || val.isEmpty())
			return false; // fail

		final StringBuffer needTxt = new StringBuffer();
		if (!isInteger(val))
			needTxt.append(model.getValue().toString());
		else {
			int iVal = Integer.parseInt(val);
			if (iVal > (Integer)model.getMaximum())
				needTxt.append(model.getMaximum().toString());
			else
			if (iVal < (Integer)model.getMinimum())
				needTxt.append(model.getMinimum().toString());
		}
		if (needTxt.length() > 0) {
//			System.out.println(needTxt);
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() { txtFld.setText(needTxt.toString()); }
			});
			return false; // fail
		} else
			return true; // all Ok
	}

	private static boolean isInteger(String in) {
		try {
			Integer.parseInt(in);
			return true;
		} catch(Exception ex) {
			return false;
		}
	}
}
