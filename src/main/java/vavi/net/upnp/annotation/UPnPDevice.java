/*
 * Copyright (c) 2006 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.net.upnp.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * UPnPDevice. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 060908 nsano initial version <br>
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface UPnPDevice {

    String description();

    /** */
    class Util {

        private Util() {
        }

        /** */
        private static Log logger = LogFactory.getLog(Util.class); 

        /** */
        public static String getDescription(Object bean) {
            try {
                UPnPDevice device = bean.getClass().getAnnotation(UPnPDevice.class);
                return device.description();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
    }
}

/* */
