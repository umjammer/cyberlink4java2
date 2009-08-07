/*
 * Copyright (c) 2006 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package light2;

import vavi.net.upnp.annotation.UPnPAction;
import vavi.net.upnp.annotation.UPnPDevice;
import vavi.net.upnp.annotation.UPnPStateVariable;


/**
 * Light.
 */
@UPnPDevice(description = "/light2/description/description.xml")
public class Light {

    /** */
    @UPnPStateVariable(name = "Power")
    private int power = 0;

    @UPnPAction(name = "GetPower", arg = "Power")
    public int getPower() {
        return power;
    }

    @UPnPAction(name = "SetPower", arg = "Power", result = "Result")
    public void setPower(int power) {
        this.power = power;
        view.setPowerEnabled(power != 0);
    }

    public interface View {
        void setPowerEnabled(boolean enabled);
    }

    private View view;

    public void setView(View view) {
        this.view = view;
    }
}

/* */
