/*
 * CyberUPnP for Java
 *
 * Copyright (C) Satoshi Konno 2002
 */

package remotectrl;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.JPanel;


/**
 * Sample RemoteControllerPane.
 *
 * @author Satoshi Konno
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 */
public class RemoteControllerPane extends JPanel {
    private final static int IMAGE_BORDER_SIZE = 20;

    /** Constructor */
    public RemoteControllerPane() {
        loadImage();
        initPanel();
        addMouseListener(mouseListener);
    }

    /** remoteController */
    private RemoteController remoteController = null;

    public void setDevice(RemoteController remoteController) {
        this.remoteController = remoteController;
    }

    public RemoteController getDevice() {
        return remoteController;
    }

    //	Background
    private final static String CLOCK_PANEL_IMAGE = "/remotectrl/images/remotectrl.jpg";
    private BufferedImage panelmage;

    private void loadImage() {
        URL f = RemoteControllerPane.class.getResource(CLOCK_PANEL_IMAGE);
        try {
            panelmage = ImageIO.read(f);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private BufferedImage getPaneImage() {
        return panelmage;
    }

    //	Background
    private void initPanel() {
        BufferedImage panelmage = getPaneImage();
        setPreferredSize(new Dimension(panelmage.getWidth(),
                                       panelmage.getHeight()));
    }

    //	Font
    private final static String DEFAULT_FONT_NAME = "Lucida Console";
    private final static int DEFAULT_TIME_FONT_SIZE = 14;
    private Font timeFont = null;

    private Font getFont(Graphics g, int size) {
        Font font = g.getFont();
        if (font != null) {
            return font;
        }
        return new Font(DEFAULT_FONT_NAME, Font.BOLD, size);
    }

    private Font getFont(Graphics g) {
        if (timeFont == null) {
            timeFont = getFont(g, DEFAULT_TIME_FONT_SIZE);
        }
        return timeFont;
    }


    //	panel
    private String panelMessage = "";

    private void display(String msg) {
        panelMessage = msg;
        update(getGraphics());
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
        }

        panelMessage = "";
        repaint();
    }

    //	mouse
    private Rectangle tvPowerRect = new Rectangle(20, 80, 50, 50);
    private Rectangle lightPowerRect = new Rectangle(130, 80, 50, 50);
    private Rectangle airconPowerRect = new Rectangle(20, 170, 50, 50);
    private Rectangle airconUpRect = new Rectangle(100, 170, 40, 50);
    private Rectangle airconDownRect = new Rectangle(140, 170, 40, 50);
    private Rectangle washerStartRect = new Rectangle(20, 250, 50, 50);
    private Rectangle washerStopRect = new Rectangle(100, 250, 50, 50);

    private MouseListener mouseListener = new MouseAdapter() {
        public void mousePressed(MouseEvent event) {
            RemoteController device = getDevice();
            int x = event.getX();
            int y = event.getY();
            if (tvPowerRect.contains(x, y)) {
                display("TV POWER");
                try {
                    device.tvPowerOn();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (lightPowerRect.contains(x, y)) {
                display("LIGHT POWER");
                try {
                    device.lightPowerOn();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (airconPowerRect.contains(x, y)) {
                display("AIR CONDITIONER POWER");
                try {
                    device.airconPowerOn();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (airconUpRect.contains(x, y)) {
                display("AIR CONDITIONER UP");
                try {
                    device.airconTempUp();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (airconDownRect.contains(x, y)) {
                display("AIR CONDITIONER DOWN");
                try {
                    device.airconTempDown();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (washerStartRect.contains(x, y)) {
                display("WASHER START");
                try {
                    device.washerStart();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (washerStopRect.contains(x, y)) {
                display("WASHER STOP");
                try {
                    device.washerStop();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    /** paint */
    private void drawPanelMessage(Graphics g) {
        g.setColor(Color.WHITE);

        g.setFont(getFont(g));

        g.drawString(panelMessage, 20, 35);
    }

    private void clear(Graphics g) {
        g.setColor(Color.GRAY);
        g.clearRect(0, 0, getWidth(), getHeight());
    }

    private void drawPanelImage(Graphics g) {
        g.drawImage(getPaneImage(), 0, 0, null);
    }

    public void paint(Graphics g) {
        clear(g);
        drawPanelImage(g);
        drawPanelMessage(g);
    }
}
