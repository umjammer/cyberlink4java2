/*
 * MediaServer for CyberLink
 *
 * Copyright (C) Satoshi Konno 2004
 */

package org.cybergarage.upnp.media.server.directory.mythtv;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;


/**
 * MythDatabase.
 *
 * @version 02/11/04 first revision.
 */
public class MythDAO {

    // Constants
    private final static String DB_URL = "mythconverg";

    private final static String DB_USER = "mythtv";

    private final static String DB_PASSWD = "mythtv";

    /** */
    @SuppressWarnings("unchecked")
    public String getRecordFilePrefix() throws SQLException {

        Connection connection = null;

        try {
            QueryRunner qr = new QueryRunner();
            ResultSetHandler rsh = new MapListHandler();
            List<Map<String, ?>> resultSet = (List<Map<String, ?>>) qr.query(
                connection,
                "select * from settings where value = 'RecordFilePrefix'",
                rsh); 

            return (String) resultSet.get(0).get("data");
    
        } finally {
            DbUtils.close(connection);            
        }
    }

    /** RecordedInfo */
    @SuppressWarnings("unchecked")
    public MythRecordedInfo[] getRecordedInfos() throws SQLException {

        List<MythRecordedInfo> recVec = new ArrayList<MythRecordedInfo>();

        Connection connection = null;

        try {
            QueryRunner qr = new QueryRunner();
            ResultSetHandler rsh = new MapListHandler();
            List<Map<String, ?>> resultSet = (List<Map<String, ?>>) qr.query(
                connection,
                "select * from recorded",
                rsh); 

            String recFilePrefix = getRecordFilePrefix();

            for (Map<String, ?> result : resultSet) {
                MythRecordedInfo recInfo = new MythRecordedInfo();
                recInfo.setRecordFilePrefix(recFilePrefix);
                recInfo.setChanID((Integer) result.get("chanid"));
                recInfo.setRecordID((Integer) result.get("recordid"));
                recInfo.setStartTime((Long) result.get("starttime"));
                recInfo.setEndTime((Long) result.get("endtime"));
                recInfo.setTitle((String) result.get("title"));
                recInfo.setSubTitle((String) result.get("subtitle"));
                recInfo.setDescription((String) result.get("description"));
                recInfo.setCategory((String) result.get("category"));
                recVec.add(recInfo);
            }

            return recVec.toArray(new MythRecordedInfo[recVec.size()]);

        } finally {
            DbUtils.close(connection);            
        }
    }
}

/* */
