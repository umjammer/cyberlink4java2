/*
 * CyberUPnP for Java
 *
 * Copyright (C) Satoshi Konno 2002
 */

package controlpoint;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.HttpURLConnection;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import vavi.net.upnp.StateVariable;
import vavi.net.upnp.di.StateVariableSerdes;
import vavi.net.upnp.soap.QueryResponse;
import vavi.net.util.Util;
import vavi.util.Debug;
import vavi.util.Singleton;


/**
 * StateVariablePane.
 *
 * @author Satoshi Konno
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 */
public class StateVariablePane extends JPanel {
    private ControlPoint controlPoint;

    private StateVariable stateVariable;

    private TableModel stateVariableTable;

    private JButton queryButton;
    /** */
    private StateVariableSerdes serdes = Singleton.getInstance(StateVariableSerdes.class);

    // Constructor
    public StateVariablePane(ControlPoint controlPoint, StateVariable stateVariable) {
        setLayout(new BorderLayout());

        this.controlPoint = controlPoint;
        this.stateVariable = stateVariable;

        try {
            Document document = Util.getDocumentBuilder().newDocument();
            Node node = document.createElement("stateVariable");
            serdes.serialize(stateVariable, node);
            stateVariableTable = new TableModel(node);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        JScrollPane scrollPane = new JScrollPane();
        JTable table = new JTable(stateVariableTable);
        scrollPane.setViewportView(table);
        scrollPane.setColumnHeaderView(table.getTableHeader());
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPane = new JPanel();
        queryButton = new JButton("Query");
        buttonPane.add(queryButton);
        queryButton.addActionListener(actionListener);

        add(buttonPane, BorderLayout.SOUTH);
    }

    // Frame
    private Frame getFrame() {
        return (Frame) getRootPane().getParent();
    }

    // Member
    public StateVariable getStateVariable() {
        return stateVariable;
    }

    public TableModel getTable() {
        return stateVariableTable;
    }

    public JButton getButton() {
        return queryButton;
    }

    // varPerformed
    private ActionListener actionListener = new ActionListener() {
        public void actionPerformed(ActionEvent event) {
            if (event.getSource() != queryButton) {
                return;
            }
    
            String title = stateVariable.getName();
            String message;
            try {
                QueryResponse queryResponse = stateVariable.postQuerylAction();
                if (queryResponse.getStatus() == HttpURLConnection.HTTP_OK) {
                    message = stateVariable.getName() + " = " + stateVariable.getValue();
                } else {
                    message = queryResponse.getStatusMessage() + " (" + queryResponse.getStatus() + ")";
                }
            } catch (IOException e) {
Debug.printStackTrace(e);
                message = e.getClass() + " (" + e.getMessage() + ")";
            } catch (IllegalStateException e) {
Debug.printStackTrace(e.getCause());
                message = e.getCause().getClass() + " (" + e.getCause().getMessage() + ")";
            }

            controlPoint.printConsole(title + " : " + message);
            JOptionPane.showMessageDialog(StateVariablePane.this, message, title, JOptionPane.PLAIN_MESSAGE);
        }
    };
}
