/*
 * MediaServer for CyberLink
 *
 * Copyright (C) Satoshi Konno 2003
 */

package org.cybergarage.upnp.media.server.action;

import vavi.net.upnp.Action;


/**
 * BrowseAction
 * 
 * @version 10/22/03 first revision.<br>
 *          04/27/04 Changed getObjectID() to return the string value.
 */
public class BrowseAction extends Action {
    // Constants

    public final static String OBJECT_ID = "ObjectID";

    public final static String BROWSE_FLAG = "BrowseFlag";

    public final static String FILTER = "Filter";

    public final static String STARTING_INDEX = "StartingIndex";

    public final static String REQUESTED_COUNT = "RequestedCount";

    public final static String SORT_CRITERIA = "SortCriteria";

    public final static String BROWSE_METADATA = "BrowseMetadata";

    public final static String BROWSE_DIRECT_CHILDREN = "BrowseDirectChildren";

    public final static String RESULT = "Result";

    public final static String NUMBER_RETURNED = "NumberReturned";

    public final static String TOTAL_MACHES = "TotalMatches";

    public final static String UPDATE_ID = "UpdateID";

    /** Constructor */
    public BrowseAction(Action action) {
        // TODO check copying
        setService(action.getService());
        setName(action.getName());
        argumentList.addAll(action.getArgumentList());
    }

    // Request

    public String getBrowseFlag() {
        return getArgument(BROWSE_FLAG).getValue();
    }

    public boolean isMetadata() {
        return BROWSE_METADATA.equals(getBrowseFlag());
    }

    public boolean isDirectChildren() {
        return BROWSE_DIRECT_CHILDREN.equals(getBrowseFlag());
    }

    public String getObjectID() {
        return getArgument(OBJECT_ID).getValue();
    }

    public int getStartingIndex() {
        return Integer.parseInt(getArgument(STARTING_INDEX).getValue());
    }

    public int getRequestedCount() {
        return Integer.parseInt(getArgument(REQUESTED_COUNT).getValue());
    }

    public String getSortCriteria() {
        return getArgument(SORT_CRITERIA).getValue();
    }

    public String getFilter() {
        return getArgument(FILTER).getValue();
    }

    // Result

    public void setResult(String value) {
        getArgument(RESULT).setValue(value);
    }

    public void setNumberReturned(int value) {
        getArgument(NUMBER_RETURNED).setValue(String.valueOf(value));
    }

    public void setTotalMaches(int value) {
        getArgument(TOTAL_MACHES).setValue(String.valueOf(value));
    }

    public void setUpdateID(int value) {
        getArgument(UPDATE_ID).setValue(String.valueOf(value));
    }
}

/* */
