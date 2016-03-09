/*
 * CyberUPnP for Java
 *
 * Copyright (C) Satoshi Konno 2002
 */

package controlpoint;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import vavi.net.upnp.Action;
import vavi.net.upnp.Argument;
import vavi.net.upnp.di.ActionSerdes;
import vavi.net.upnp.soap.ActionResponse;
import vavi.net.util.Util;
import vavi.util.Debug;
import vavi.util.Singleton;


/**
 * ActionPane.
 *
 * @author Satoshi Konno
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 */
public class ActionPane extends JPanel {
    private ControlPoint controlPoint;

    private Action action;

    private TableModel actionTable;

    private JButton actionButton;

    /** */
    private ActionSerdes actionSerdes = Singleton.getInstance(ActionSerdes.class);

    // Constructor
    public ActionPane(ControlPoint controlPoint, Action action) {
        setLayout(new BorderLayout());

        this.controlPoint = controlPoint;
        this.action = action;

        try {
            Document document = Util.getDocumentBuilder().newDocument();
            Node node = document.createElement("action");
            actionSerdes.serialize(action, node);
//System.err.println("-------- action");
//PrettyPrinter pp = new PrettyPrinter(new PrintWriter(System.err));
//pp.print(node);
//System.err.println("--------");
            actionTable = new TableModel(node);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        JScrollPane scrollPane = new JScrollPane();
        JTable table = new JTable(actionTable);
        scrollPane.setViewportView(table);
        scrollPane.setColumnHeaderView(table.getTableHeader());
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPane = new JPanel();
        actionButton = new JButton("Action");
        buttonPane.add(actionButton);
        add(buttonPane, BorderLayout.SOUTH);
        actionButton.addActionListener(actionListener);
    }

    // Member
    public Action getAction() {
        return action;
    }

    public TableModel getTable() {
        return actionTable;
    }

    public JButton getButton() {
        return actionButton;
    }

    // actionPerformed
    private ActionListener actionListener = new ActionListener() {
        public void actionPerformed(ActionEvent event) {
            if (event.getSource() != actionButton) {
                return;
            }
    
            List<Argument> inArgumentList = action.getInputArgumentList();
            if (inArgumentList.size() > 0) {
                ActionDialog dialog = new ActionDialog(controlPoint.getFrame(), action);
                dialog.doModal();
            }
    
            String title = action.getName();
            String message = "";
            try {
                ActionResponse controlResponse = action.postActionRequest();
                if (controlResponse.getStatus() == HttpURLConnection.HTTP_OK) {
                    List<Argument> outArgumentList = action.getOutputArgumentList();
                    int nArgs = outArgumentList.size();
                    if (nArgs == 0) {
                        message = "(No response value)";
                    }
                    for (int n = 0; n < nArgs; n++) {
                        Argument argument = outArgumentList.get(n);
                        String name = argument.getName();
                        String value = argument.getValue();
                        message += (name + " = " + value);
                        if (n < (nArgs - 1)) {
                            message += ", ";
                        }
                    }
                } else {
                    message = controlResponse.getStatusMessage() + " (" + controlResponse.getStatus() + ")";
                }
            } catch (IOException e) {
Debug.printStackTrace(e);
                message = e.getClass() + " (" + e.getMessage() + ")";
            }
    
            controlPoint.printConsole(title + " : " + message);
            JOptionPane.showMessageDialog(ActionPane.this, message, title, JOptionPane.PLAIN_MESSAGE);
        }
    };
}

/* */
