/*
 * MediaGate for CyberLink
 *
 * Copyright (C) Satoshi Konno 2004
 */

package org.cybergarage.mediagate;

import java.io.IOException;
import java.util.prefs.Preferences;

import org.cybergarage.mediagate.frame.MediaFrame;
import org.cybergarage.mediagate.frame.swing.SwingFrame;
import org.cybergarage.upnp.media.server.ContentDirectory;
import org.cybergarage.upnp.media.server.Directory;
import org.cybergarage.upnp.media.server.MediaServer;
import org.cybergarage.upnp.media.server.directory.file.FileDirectory;
import org.cybergarage.upnp.media.server.directory.mythtv.MythDirectory;
import org.cybergarage.upnp.media.server.object.format.DefaultFormat;
import org.cybergarage.upnp.media.server.object.format.GIFFormat;
import org.cybergarage.upnp.media.server.object.format.ID3Format;
import org.cybergarage.upnp.media.server.object.format.JPEGFormat;
import org.cybergarage.upnp.media.server.object.format.MPEGFormat;
import org.cybergarage.upnp.media.server.object.format.PNGFormat;

import vavi.util.Debug;


/**
 * MediaServer.
 *
 * @author Satoshi Konno
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * 10/22/03 first revision.
 */
public class MediaGate {

    // Constants
    private final static int FILESYS_MODE = 0;

    private final static int MYTHTV_MODE = 1;

    private final static String MYTHTV_OPT_STRING_OLD = "-mythtv";
    private final static String MYTHTV_OPT_STRING = "--mythtv";
    private final static String VERBOSE_OPT_STRING = "-v";

    // Constructor
    public MediaGate(int mode) {
        try {
            mediaServer = new MediaServer();
            setMode(mode);

            boolean isFileSysMode = (mode == FILESYS_MODE) ? true : false;

            switch (mode) {
            case FILESYS_MODE: {
                mediaServer.addPlugIn(new ID3Format());
                mediaServer.addPlugIn(new GIFFormat());
                mediaServer.addPlugIn(new JPEGFormat());
                mediaServer.addPlugIn(new PNGFormat());
                mediaServer.addPlugIn(new MPEGFormat());
                loadUserDirectories();
            }
                break;
            case MYTHTV_MODE: {
                mediaServer.addPlugIn(new DefaultFormat());

                MythDirectory mythDir = new MythDirectory();
                mediaServer.addContentDirectory(mythDir);
            }
                break;
            }

            mediaFrame = new SwingFrame(this, isFileSysMode);
        } catch (Exception e) {
            Debug.printStackTrace(e);
        }
    }

    // Mode
    private int mode;

    private void setMode(int m) {
        mode = m;
    }

    private int getMode() {
        return mode;
    }

    // Preferences (FileSystem)
    private final static String DIRECTORY_PREFS_NAME = "directory";

    private Preferences prefs = null;

    private Preferences getUserPreferences() {
        if (prefs == null) {
            prefs = Preferences.userNodeForPackage(this.getClass());
        }
        return prefs;
    }

    private Preferences getUserDirectoryPreferences() {
        return getUserPreferences().node(DIRECTORY_PREFS_NAME);
    }

    private void clearUserDirectoryPreferences() {
        try {
            Preferences dirPref = getUserDirectoryPreferences();
            String[] dirName = dirPref.keys();
            int dirCnt = dirName.length;
            for (int n = 0; n < dirCnt; n++) {
                dirPref.remove(dirName[n]);
            }
        } catch (Exception e) {
            Debug.printStackTrace(e);
        }
    }

    private void loadUserDirectories() {
        try {
            Preferences dirPref = getUserDirectoryPreferences();
            String[] dirName = dirPref.keys();
            int dirCnt = dirName.length;
Debug.println("Loading Directories (" + dirCnt + ") ....");
            for (int n = 0; n < dirCnt; n++) {
                String name = dirName[n];
                String path = dirPref.get(name, "");
                FileDirectory fileDir = new FileDirectory(name, path);
                getMediaServer().addContentDirectory(fileDir);
Debug.println("[" + n + "] = " + name + "," + path);
            }
        } catch (Exception e) {
            Debug.printStackTrace(e);
        }
    }

    private void saveUserDirectories() {
        clearUserDirectoryPreferences();

        ContentDirectory conDir = getContentDirectory();
        try {
            Preferences dirPref = getUserDirectoryPreferences();
            int dirCnt = conDir.getNDirectories();
            for (int n = 0; n < dirCnt; n++) {
                Directory dir = conDir.getDirectory(n);
                if (!(dir instanceof FileDirectory)) {
                    continue;
                }

                FileDirectory fileDir = (FileDirectory) dir;
                dirPref.put(fileDir.getFriendlyName(), fileDir.getPath());
            }
        } catch (Exception e) {
            Debug.println(e);
        }
    }

    // MediaServer
    private MediaServer mediaServer;

    public MediaServer getMediaServer() {
        return mediaServer;
    }

    public ContentDirectory getContentDirectory() {
        return mediaServer.getContentDirectory();
    }

    // MediaServer
    private MediaFrame mediaFrame;

    public MediaFrame getMediaFrame() {
        return mediaFrame;
    }

    // start/stop
    public void start() throws IOException {
        getMediaServer().start();
    }

    public void stop() throws IOException {
        getMediaServer().stop();
        if (getMode() == FILESYS_MODE) {
            saveUserDirectories();
        }
    }

    // Debug
    public static void debug(MediaGate mgate) {
//        String sortCriteria = "+dc:date,+dc:title,+upnp:class";
//        mgate.getContentDirectory().sortContentNodeList(new ContentNodeList(), sortCriteria);
    }

    // main
    public static void main(String[] args) throws Exception {

        int mode = FILESYS_MODE;
Debug.println("args = " + args.length);
        for (int n = 0; n < args.length; n++) {
Debug.println("  [" + n + "] = " + args[n]);
            if (MYTHTV_OPT_STRING.compareTo(args[n]) == 0) {
                mode = MYTHTV_MODE;
            }
            if (MYTHTV_OPT_STRING_OLD.compareTo(args[n]) == 0) {
                mode = MYTHTV_MODE;
            }
            if (VERBOSE_OPT_STRING.compareTo(args[n]) == 0) {
//              Debug.on();
            }
        }

        MediaGate mediaGate = new MediaGate(mode);
        debug(mediaGate);
        mediaGate.start();
    }
}
