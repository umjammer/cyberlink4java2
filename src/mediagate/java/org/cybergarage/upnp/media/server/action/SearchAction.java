/*
 * MediaServer for CyberLink
 *
 * Copyright (C) Satoshi Konno 2003-2004
 */

package org.cybergarage.upnp.media.server.action;

import vavi.net.upnp.Action;


/**
 * SearchAction
 * 
 * @version 08/16/04 Changed getObjectID() to return the string value.
 */
public class SearchAction extends Action {
    // Constants

    public final static String CONTAINER_ID = "ContainerID";

    public final static String SEARCH_CRITERIA = "SearchCriteria";

    public final static String FILTER = "Filter";

    public final static String STARTING_INDEX = "StartingIndex";

    public final static String REQUESTED_COUNT = "RequestedCount";

    public final static String SORT_CRITERIA = "SortCriteria";

    public final static String RESULT = "Result";

    public final static String NUMBER_RETURNED = "NumberReturned";

    public final static String TOTAL_MACHES = "TotalMatches";

    public final static String UPDATE_ID = "UpdateID";

    // Constructor

    public SearchAction(Action action) {
        // TODO check copying
        setService(action.getService());
        setName(action.getName());
        argumentList.addAll(action.getArgumentList());
    }

    // Request

    public String getContainerID() {
        return getArgument(CONTAINER_ID).getValue();
    }

    public String getSearchCriteria() {
        return getArgument(SEARCH_CRITERIA).getValue();
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
