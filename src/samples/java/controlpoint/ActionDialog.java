/*
 * CyberUPnP for Java
 *
 * Copyright (C) Satoshi Konno 2002
 */

package controlpoint;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import vavi.net.upnp.Action;
import vavi.net.upnp.AllowedValueRange;
import vavi.net.upnp.Argument;
import vavi.net.upnp.StateVariable;


/**
 * ActionDialog.
 * TODO allowedValue/Range �����B��ꍇ
 * @author Satoshi Konno
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 */
public class ActionDialog extends JDialog {
    /** */
    private Action action;

    /** */
    private JButton okButton;

    /** */
    private JButton cancelButton;

    /** */
    private boolean result;

    /** */
    private List<Argument> inArgumentList;

    /** */
    private List<JComponent> inArgumentFieldList;

    /** */
    public ActionDialog(Frame frame, Action action) {
        super(frame, true);
        getContentPane().setLayout(new BorderLayout());

        this.action = action;

        inArgumentList = new ArrayList<>();
        inArgumentFieldList = new ArrayList<>();

        JPanel argumentListPane = new JPanel();

        argumentListPane.setLayout(new GridLayout(0, 2));
        getContentPane().add(argumentListPane, BorderLayout.CENTER);

        for (Argument argument : action.getInputArgumentList()) {

            JLabel argumentLabel = new JLabel(argument.getName());
            JComponent argumentField = null;
            StateVariable stateVariable = argument.getAction().getService().getStateVariable(argument.getName());
            if (stateVariable != null) {
                if (stateVariable.getAllowedValueList().size() > 0) {
                    argumentField = new JComboBox<>(stateVariable.getAllowedValueList().toArray());
                } else if (stateVariable.getAllowedValueRange() != null) {
                    AllowedValueRange avr = stateVariable.getAllowedValueRange();
                    int min = Integer.parseInt(avr.getMinimum());
                    int max = Integer.parseInt(avr.getMaximum());
                    int step = Integer.parseInt(avr.getStep());
                    argumentField = new JSpinner(new SpinnerNumberModel(min, min, max, step));
                } else {
                    argumentField = new JTextField();
                }
            } else {
                argumentField = new JTextField();
            }

            inArgumentFieldList.add(argumentField);
            argumentListPane.add(argumentLabel);
            argumentListPane.add(argumentField);

            Argument inArgument = new Argument();
            inArgument.setName(argument.getName());
            inArgumentList.add(inArgument);
        }

        okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            @SuppressWarnings("rawtypes")
            public void actionPerformed(ActionEvent event) {
                result = true;

                for (int n = 0; n < inArgumentFieldList.size(); n++) {
                    JComponent field = inArgumentFieldList.get(n);
                    String value = null;
                    if (field instanceof JTextField) {
                        value = ((JTextField) field).getText();
                    } if (field instanceof JComboBox) {
                        value = (String) ((JComboBox) field).getSelectedItem();
                    }
                    Argument argument = inArgumentList.get(n);
                    argument.setValue(value);
                }
                ActionDialog.this.action.updateArguments(inArgumentList);

                dispose();
            }
        });
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                result = false;
                dispose();
            }
        });

        JPanel buttonPane = new JPanel();
        buttonPane.add(okButton);
        buttonPane.add(cancelButton);
        getContentPane().add(buttonPane, BorderLayout.SOUTH);

        pack();

        Dimension size = getSize();
        Point fpos = frame.getLocationOnScreen();
        Dimension fsize = frame.getSize();
        setLocation(fpos.x + ((fsize.width - size.width) / 2), fpos.y + ((fsize.height - size.height) / 2));
    }

    // actionPerformed
    public boolean doModal() {
        setVisible(true);
        return result;
    }
}

/* */
