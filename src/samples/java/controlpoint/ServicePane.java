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

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import vavi.net.upnp.Service;
import vavi.net.upnp.di.ServiceSerdes;
import vavi.net.upnp.gena.Subscription;
import vavi.net.util.Util;
import vavi.util.Singleton;


/**
 * ServicePane.
 *
 * @author Satoshi Konno
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 */
public class ServicePane extends JPanel {
    private ControlPoint controlPoint;

    private Service service;

    private TableModel serviceTable;

    private JButton subscribeButton;

    private JButton unsubscribeButton;

    /** */
    private ServiceSerdes serviceSerdes = Singleton.getInstance(ServiceSerdes.class);

    // Constructor
    public ServicePane(ControlPoint controlPoint, Service service) {
        setLayout(new BorderLayout());

        this.controlPoint = controlPoint;
        this.service = service;

        try {
            Document document = Util.getDocumentBuilder().newDocument();
            Node node = document.createElement("service");
            serviceSerdes.serialize(service, node);
//System.err.println("-------- service");
//PrettyPrinter pp = new PrettyPrinter(new PrintWriter(System.err));
//pp.print(node);
//System.err.println("--------");
            serviceTable = new TableModel(node);
        } catch (IOException e) {
            throw (RuntimeException) new IllegalStateException().initCause(e);
        }
        JScrollPane scrollPane = new JScrollPane();
        JTable table = new JTable(serviceTable);
        scrollPane.setViewportView(table);
        scrollPane.setColumnHeaderView(table.getTableHeader());
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPane = new JPanel();
        subscribeButton = new JButton("Subscribe");
        buttonPane.add(subscribeButton);
        subscribeButton.addActionListener(actionListener);
        unsubscribeButton = new JButton("Unsubscribe");
        buttonPane.add(unsubscribeButton);
        unsubscribeButton.addActionListener(actionListener);
        add(buttonPane, BorderLayout.SOUTH);
    }

    // Frame
    private Frame getFrame() {
        return (Frame) getRootPane().getParent();
    }

    // Member
    public Service getService() {
        return service;
    }

    public TableModel getTable() {
        return serviceTable;
    }

    public JButton getButton() {
        return subscribeButton;
    }

    // servicePerformed
    private ActionListener actionListener = new ActionListener() {
        public void actionPerformed(ActionEvent event) {
            try {
                if (event.getSource() == subscribeButton) {
                    boolean result;
                    if (service.hasSID() == true) {
                        String sid = service.getSID();
                        result = controlPoint.subscribe(service, sid, Subscription.INFINITE_VALUE);
                    } else {
                        result = controlPoint.subscribe(service, Subscription.INFINITE_VALUE);
                    }
                    controlPoint.printConsole("subscribe : " + result + " (" + service.getSID() + ")");
                } else if (event.getSource() == unsubscribeButton) {
                    boolean subRes = controlPoint.unsubscribe(service);
                    controlPoint.printConsole("unsubscribe : " + subRes);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };
}

/* */
