/*
 * CyberUPnP for Java
 *
 * Copyright (C) Satoshi Konno 2002
 */

package washer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import vavi.util.Debug;


/**
 * WasherPane.
 *
 * @author Satoshi Konno
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 */
public class WasherPane extends JPanel {

    /** */
    public WasherPane() {
        loadImage();
        initPanel();
    }

    /** */
    private final static String WASHER_PANEL_IMAGE = "/images/washer.jpg";
    /** */
    private final static String WASHER_ANIM_IMAGE1 = "/images/washeron1.jpg";
    /** */
    private final static String WASHER_ANIM_IMAGE2 = "/images/washeron2.jpg";
    /** */
    private BufferedImage panelmage;
    /** */
    private BufferedImage animlmage;
    /** */
    private BufferedImage anim1lmage;
    /** */
    private BufferedImage anim2lmage;

    /** */
    private void loadImage() {
        try {
            panelmage = ImageIO.read(WasherFrame.class.getResource(WASHER_PANEL_IMAGE));
            anim1lmage = ImageIO.read(WasherFrame.class.getResource(WASHER_ANIM_IMAGE1));
            anim2lmage = ImageIO.read(WasherFrame.class.getResource(WASHER_ANIM_IMAGE2));
        } catch (Exception e) {
            Debug.println(e);
        }
    }

    /** */
    private BufferedImage getPaneImage() {
        return panelmage;
    }

    /** */
    public void flipAnimationImage() {
        if (animlmage == anim1lmage) {
            animlmage = anim2lmage;
        } else {
            animlmage = anim1lmage;
        }
    }

    /** Background */
    private void initPanel() {
        BufferedImage panelmage = getPaneImage();
        setPreferredSize(new Dimension(panelmage.getWidth(), panelmage.getHeight()));
    }

    /** LightDevice */
    private WasherDevice washerDevice;

    /** */
    public void setDevice(WasherDevice device) {
        washerDevice = device;
    }

    /** */
    public WasherDevice getDevice() {
        return washerDevice;
    }

    /** paint */
    private void drawInfo(Graphics g) {
        if (washerDevice.isWashing()) {
            g.drawImage(animlmage, 0, 0, null);
            g.setColor(Color.YELLOW);
        } else {
            g.drawImage(panelmage, 0, 0, null);
            g.setColor(Color.LIGHT_GRAY);
        }
        g.fillRect(5, 20, 20, 5);
    }

    /** */
    private void clear(Graphics g) {
        g.setColor(Color.GRAY);
        g.clearRect(0, 0, getWidth(), getHeight());
    }

    /** */
    public void paint(Graphics g) {
        clear(g);
        drawInfo(g);
    }
}

/* */
