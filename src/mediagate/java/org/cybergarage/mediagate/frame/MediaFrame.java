/*
 * MediaGate for CyberLink
 *
 * Copyright (C) Satoshi Konno 2004
 */

package org.cybergarage.mediagate.frame;

import org.cybergarage.mediagate.MediaGate;


/**
 * MediaFrame.
 *
 * @version 01/24/04 first revision.
 */
public abstract class MediaFrame {

    /** Constructor */
    public MediaFrame(MediaGate mgate) {
        mediaGate = mgate;
    }

    /** */
    private MediaGate mediaGate;

    public MediaGate getMediaGate() {
        return mediaGate;
    }
}

/* */
