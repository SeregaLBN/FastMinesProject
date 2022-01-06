package fmg.swing.app.model.control;

import java.awt.event.ActionListener;
import java.awt.event.ItemListener;

import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.event.ChangeListener;

public class ButtonWrapperModel implements ButtonModel {

    private final ButtonModel wrap;

    public ButtonWrapperModel(ButtonModel model) {
       this.wrap = model;
    }

    @Override
    public Object[] getSelectedObjects() { return wrap.getSelectedObjects(); }
    @Override
    public boolean isArmed() { return wrap.isArmed(); }
    @Override
    public boolean isSelected() { return wrap.isSelected(); }
    @Override
    public boolean isEnabled() { return wrap.isEnabled(); }
    @Override
    public boolean isPressed() { return wrap.isPressed(); }
    @Override
    public boolean isRollover() { return wrap.isRollover(); }
    @Override
    public void setArmed(boolean b) { wrap.setArmed(b); }
    @Override
    public void setSelected(boolean b) { wrap.setSelected(b); }
    @Override
    public void setEnabled(boolean b) { wrap.setEnabled(b); }
    @Override
    public void setPressed(boolean b) { wrap.setPressed(b); }
    @Override
    public void setRollover(boolean b) { wrap.setRollover(b); }
    @Override
    public void setMnemonic(int key) { wrap.setMnemonic(key); }
    @Override
    public int getMnemonic() { return wrap.getMnemonic(); }
    @Override
    public void setActionCommand(String s) { wrap.setActionCommand(s); }
    @Override
    public String getActionCommand() { return wrap.getActionCommand(); }
    @Override
    public void setGroup(ButtonGroup group) { wrap.setGroup(group); }
    @Override
    public void addActionListener(ActionListener l) { wrap.addActionListener(l); }
    @Override
    public void removeActionListener(ActionListener l) { wrap.removeActionListener(l); }
    @Override
    public void addItemListener(ItemListener l) { wrap.addItemListener(l); }
    @Override
    public void removeItemListener(ItemListener l) { wrap.removeItemListener(l); }
    @Override
    public void addChangeListener(ChangeListener l) { wrap.addChangeListener(l); }
    @Override
    public void removeChangeListener(ChangeListener l) { wrap.removeChangeListener(l); }

}
