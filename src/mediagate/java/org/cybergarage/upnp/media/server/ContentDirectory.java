/*
 * MediaServer for CyberLink
 *
 * Copyright (C) Satoshi Konno 2003-2004
 */

package org.cybergarage.upnp.media.server;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cybergarage.upnp.media.server.action.BrowseAction;
import org.cybergarage.upnp.media.server.action.SearchAction;
import org.cybergarage.upnp.media.server.object.ContentNode;
import org.cybergarage.upnp.media.server.object.DIDLLite;
import org.cybergarage.upnp.media.server.object.Format;
import org.cybergarage.upnp.media.server.object.SearchCap;
import org.cybergarage.upnp.media.server.object.SearchCriteria;
import org.cybergarage.upnp.media.server.object.container.ContainerNode;
import org.cybergarage.upnp.media.server.object.container.RootNode;
import org.cybergarage.upnp.media.server.object.item.ItemNode;
import org.cybergarage.upnp.media.server.object.search.IdSearchCap;
import org.cybergarage.upnp.media.server.object.search.TitleSearchCap;
import org.cybergarage.upnp.media.server.object.sort.DCDateComparator;
import org.cybergarage.upnp.media.server.object.sort.DCTitleComparator;
import org.cybergarage.upnp.media.server.object.sort.UPnPClassComparator;

import vavi.net.upnp.Action;
import vavi.net.upnp.Argument;
import vavi.net.upnp.StateVariable;
import vavi.net.upnp.event.ActionListener;
import vavi.net.upnp.event.QueryListener;
import vavi.util.Debug;
import vavi.util.StringUtil;


/**
 * ContentDirectory
 * 
 * @version 10/22/03 - first revision. <br>
 *          03/02/04 - Fixed a bug when update() is executed because the root
 *          node's ContentDirectory is null. <br>
 *          03/12/04 - Thanks for Robert Johansson <robert.johansson@kreatel.se> -
 *          I ran into the problem that the system can not send big files. It
 *          uses FileUtil.Load to load files. If the file is too big we get an
 *          OutOfMemory exception. <br>
 *          04/05/04 - Added getFormatMimeTypes(), Deleted getFormat() and
 *          getNFormats() for C++ porting. <br>
 *          04/27/04 - Changed getContentExportURL() usint the string ID. <br>
 *          06/20/04 - Changed contentExportRequestRecieved() to set the
 *          ConnectionInfo. <br>
 *          08/07/04 - Implemented for GetSearchCapabilities request.
 */
public class ContentDirectory extends HttpServlet {

    // Constants

    public final static String SERVICE_TYPE = "urn:schemas-upnp-org:service:ContentDirectory:1";

    // Browse Action
    public static final String BROWSE = "Browse";

    public static final String SEARCH = "Search";

    public static final String GET_SEARCH_CAPABILITIES = "GetSearchCapabilities";

    public static final String SEARCH_CAPS = "SearchCaps";

    public static final String GET_SORT_CAPABILITIES = "GetSortCapabilities";

    public static final String SORT_CAPS = "SortCaps";

    public static final String GET_SYSTEM_UPDATE_ID = "GetSystemUpdateID";

    public static final String ID = "Id";

    public static final String SYSTEM_UPDATE_ID = "SystemUpdateID";

    public static final String CONTENT_EXPORT_URI = "/ExportContent";

    public static final String CONTENT_IMPORT_URI = "/ImportContent";

    public static final String CONTENT_ID = "id";

    public static final String SCPD = "/org/cybergarage/upnp/media/server/contentdirectory-scpd.xml";

    // Constructor

    public ContentDirectory(MediaServer mserver) {
        setMediaServer(mserver);

        systemUpdateID = 0;
        maxContentID = 0;

        setSystemUpdateInterval(DEFAULT_SYSTEMUPDATEID_INTERVAL);
        setContentUpdateInterval(DEFAULT_CONTENTUPDATE_INTERVAL);

        initRootNode();
        initSortCaps();
        initSearchCaps();
    }

    // Media Server

    private MediaServer mediaServer;

    private void setMediaServer(MediaServer mserver) {
        mediaServer = mserver;
    }

    public MediaServer getMediaServer() {
        return mediaServer;
    }

    // ContentID

    private int systemUpdateID;

    public synchronized void updateSystemUpdateID() {
        systemUpdateID++;
    }

    public synchronized int getSystemUpdateID() {
        return systemUpdateID;
    }

    // ContentID

    private int maxContentID;

    private synchronized int getNextContentID() {
        maxContentID++;
        return maxContentID;
    }

    public int getNextItemID() {
        return getNextContentID();
    }

    public int getNextContainerID() {
        return getNextContentID();
    }

    // Root Node

    private RootNode rootNode;

    private void initRootNode() {
        rootNode = new RootNode();
        rootNode.setContentDirectory(this);
    }

    public RootNode getRootNode() {
        return rootNode;
    }

    // Container/Item Node

    private ContainerNode createContainerNode() {
        ContainerNode node = new ContainerNode();
        return node;
    }

    // Format

    private List<Format> formatList = new ArrayList<Format>();

    public void addPlugIn(Format format) {
        formatList.add(format);
    }

    /** */
    public Format getFormat(File file) {
Debug.println("formatList: " + formatList.size());
        for (Format format : formatList) {
            if (format.equals(file)) {
                return format;
            }
        }
        return null;
    }

    public Format getFormat(int n) {
        return formatList.get(n);
    }

    public int getNFormats() {
        return formatList.size();
    }

//    public String[] getFormatMimeTypes() {
//        int formatCnt = formatList.size();
//        String mimeType[] = new String[formatCnt];
//        for (int n = 0; n < formatCnt; n++) {
//            Format format = formatList.getFormat(n);
//            mimeType[n] = format.getMimeType();
//        }
//        return mimeType;
//    }

    // SortCap
    private List<Comparator<ContentNode>> sortCapList = new ArrayList<Comparator<ContentNode>>();

    public void addSortCap(Comparator<ContentNode> sortCap) {
        sortCapList.add(sortCap);
    }

    public int getNSortCaps() {
        return sortCapList.size();
    }

    public Comparator<ContentNode> getSortCap(int n) {
        return sortCapList.get(n);
    }

    public Comparator<ContentNode> getSortCap(String type) {
        for (Comparator<ContentNode> sortCap : sortCapList) {
            if (sortCap.toString().equals(type)) {
                return sortCap;
            }
        }
        return null;
    }

    private void initSortCaps() {
        addSortCap(new UPnPClassComparator());
        addSortCap(new DCTitleComparator());
        addSortCap(new DCDateComparator());
    }

    private String getSortCapabilities() {
        StringBuilder sortCapsStr = new StringBuilder();
        for (Comparator<ContentNode> sortCap : sortCapList) {
            String type = sortCap.toString();
            sortCapsStr.append(type);
            sortCapsStr.append(',');
        }
        sortCapsStr.setLength(sortCapsStr.length() - 1);
        return sortCapsStr.toString();
    }

    // SearchCap
    private List<SearchCap> searchCapList = new ArrayList<SearchCap>();

    public void addSearchCap(SearchCap searchCap) {
        searchCapList.add(searchCap);
    }

    public List<SearchCap> getSearchCapList() {
        return searchCapList;
    }

    public int getNSearchCaps() {
        return searchCapList.size();
    }

    public SearchCap getSearchCap(int n) {
        return searchCapList.get(n);
    }

    public SearchCap getSearchCap(String type) {
        for (SearchCap searchCap : searchCapList) {
            if (searchCap.toString().equals(type)) {
                return searchCap;
            }
        }
        return null;
    }

    private void initSearchCaps() {
        addSearchCap(new IdSearchCap());
        addSearchCap(new TitleSearchCap());
    }

    private String getSearchCapabilities() {
        StringBuilder searchCapsStr = new StringBuilder();
        for (SearchCap searchCap : searchCapList) {
            String type = searchCap.toString();
            searchCapsStr.append(type);
            searchCapsStr.append(',');
        }
        searchCapsStr.setLength(searchCapsStr.length() - 1);
        return searchCapsStr.toString();
    }

    // Directory

    private List<Directory> directryList = new ArrayList<Directory>();

    private List<Directory> getDirectoryList() {
        return directryList;
    }

    public boolean addDirectory(Directory dir) {
        dir.setContentDirectory(this);
        dir.setID(String.valueOf(getNextContainerID()));
        dir.update();
        directryList.add(dir);
        rootNode.addContentNode(dir);

        // Update SysteUpdateID
        updateSystemUpdateID();

        return true;
    }

    public boolean removeDirectory(String name) {
        Directory dirNode = directryList.get(directryList.indexOf(name));
        if (dirNode == null) {
            return false;
        }
        directryList.remove(dirNode);
        rootNode.removeChild(dirNode);

        // Update SysteUpdateID
        updateSystemUpdateID();

        return true;
    }

    public int getNDirectories() {
        return directryList.size();
    }

    public Directory getDirectory(int n) {
        return directryList.get(n);
    }

    // findContentNodeBy*

    public ContentNode findContentNodeByID(String id) {
        return getRootNode().findContentNodeByID(id);
    }

    /** ActionListener */
    private ActionListener actionListener = new ActionListener() {
        public boolean actionControlReceived(Action action) {
            // action.print();

            String actionName = action.getName();

            if (actionName.equals(BROWSE) == true) {
                BrowseAction browseAct = new BrowseAction(action);
                return browseActionReceived(browseAct);
            }

            if (actionName.equals(SEARCH) == true) {
                SearchAction searchAct = new SearchAction(action);
                return searchActionReceived(searchAct);
            }

            // @id,@parentID,dc:title,dc:date,upnp:class,res@protocolInfo
            if (actionName.equals(GET_SEARCH_CAPABILITIES) == true) {
                Argument searchCapsArg = action.getArgument(SEARCH_CAPS);
                String searchCapsStr = getSearchCapabilities();
                searchCapsArg.setValue(searchCapsStr);
                return true;
            }

            // dc:title,dc:date,upnp:class
            if (actionName.equals(GET_SORT_CAPABILITIES) == true) {
                Argument sortCapsArg = action.getArgument(SORT_CAPS);
                String sortCapsStr = getSortCapabilities();
                sortCapsArg.setValue(sortCapsStr);
                return true;
            }

            if (actionName.equals(GET_SYSTEM_UPDATE_ID) == true) {
                Argument idArg = action.getArgument(ID);
                idArg.setValue(String.valueOf(getSystemUpdateID()));
                return true;
            }

            return false;
        }

        // Browse
        private boolean browseActionReceived(BrowseAction action) {
            if (action.isMetadata() == true)
                return browseMetadataActionReceived(action);
            if (action.isDirectChildren() == true)
                return browseDirectChildrenActionReceived(action);
            return false;
        }

        // Browse (MetaData)
        private boolean browseMetadataActionReceived(BrowseAction action) {
            String objID = action.getObjectID();
            ContentNode node = findContentNodeByID(objID);
            if (node == null)
                return false;

            DIDLLite didlLite = new DIDLLite();
            didlLite.setContentNode(node);
            String result = didlLite.toString();

            action.getArgument(BrowseAction.RESULT).setValue(result);
            action.getArgument(BrowseAction.NUMBER_RETURNED).setValue(String.valueOf(1));
            action.getArgument(BrowseAction.TOTAL_MACHES).setValue(String.valueOf(1));
            action.getArgument(BrowseAction.UPDATE_ID).setValue(String.valueOf(getSystemUpdateID()));

            Debug.println(StringUtil.paramString(action));

            return true;
        }
    };

    // Browse (DirectChildren/Sort)
    private void sortContentNodeList(ContentNode conNode[], Comparator<ContentNode> sortCap, boolean ascSeq) {
        // Selection Sort
        int nConNode = conNode.length;
        for (int i = 0; i < (nConNode - 1); i++) {
            int selIdx = i;
            for (int j = (i + 1); j < nConNode; j++) {
                int cmpRet = sortCap.compare(conNode[selIdx], conNode[j]);
                if (ascSeq == true && cmpRet < 0)
                    selIdx = j;
                if (ascSeq == false && 0 < cmpRet)
                    selIdx = j;
            }
            ContentNode conTmp = conNode[i];
            conNode[i] = conNode[selIdx];
            conNode[selIdx] = conTmp;
        }
    }

    private String[] getSortCriteriaArray(String sortCriteria) {
        List<String> sortCriList = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer(sortCriteria, ", ");
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            sortCriList.add(token);
        }
        int nSortCaps = sortCriList.size();
        String[] sortCapStr = new String[nSortCaps];
        for (int n = 0; n < nSortCaps; n++) {
            sortCapStr[n] = sortCriList.get(n);
        }
        return sortCapStr;
    }

    private List<ContentNode> sortContentNodeList(List<ContentNode> contentNodeList, String sortCriteria) {
        if (sortCriteria == null || sortCriteria.length() <= 0)
            return contentNodeList;

        int nChildNodes = contentNodeList.size();
        ContentNode conNode[] = new ContentNode[nChildNodes];
        for (int n = 0; n < nChildNodes; n++)
            conNode[n] = contentNodeList.get(n);

        String[] sortCritList = getSortCriteriaArray(sortCriteria);
        int nSortCrit = sortCritList.length;
        for (int n = 0; n < nSortCrit; n++) {
            String sortStr = sortCritList[n];
            Debug.println("[" + n + "] = " + sortStr);
            boolean ascSeq = true;
            char firstSortChar = sortStr.charAt(0);
            if (firstSortChar == '-')
                ascSeq = false;
            if (firstSortChar == '+' || firstSortChar == '-')
                sortStr = sortStr.substring(1);
            Comparator<ContentNode> sortCap = getSortCap(sortStr);
            if (sortCap == null)
                continue;
            Debug.println("  ascSeq = " + ascSeq);
            Debug.println("  sortCap = " + sortCap.toString());
            sortContentNodeList(conNode, sortCap, ascSeq);
        }

        List<ContentNode> sortedContentNodeList = new ArrayList<ContentNode>();
        for (int n = 0; n < nChildNodes; n++)
            sortedContentNodeList.add(conNode[n]);
        return sortedContentNodeList;
    }

    // Browse (DirectChildren)
    private boolean browseDirectChildrenActionReceived(BrowseAction action) {
        String objID = action.getObjectID();
        ContentNode node = findContentNodeByID(objID);
        if (node == null)
            return false;

        List<ContentNode> contentNodeList = new ArrayList<ContentNode>();
        int nChildNodes = node.getChildNodes().getLength();
        for (int n = 0; n < nChildNodes; n++) {
            ContentNode cnode = (ContentNode) node.getChildNodes().item(n);
            contentNodeList.add(cnode);
        }

        // Sort Content Node Lists
        String sortCriteria = action.getSortCriteria();
        List<ContentNode> sortedContentNodeList = sortContentNodeList(contentNodeList, sortCriteria);

        int startingIndex = action.getStartingIndex();
        if (startingIndex <= 0)
            startingIndex = 0;
        int requestedCount = action.getRequestedCount();
        if (requestedCount == 0)
            requestedCount = nChildNodes;

        DIDLLite didlLite = new DIDLLite();
        int numberReturned = 0;
        for (int n = startingIndex; (n < nChildNodes && numberReturned < requestedCount); n++) {
            ContentNode cnode = sortedContentNodeList.get(n);
            didlLite.addContentNode(cnode);
            numberReturned++;
        }

        String result = didlLite.toString();
        action.setResult(result);
        action.setNumberReturned(numberReturned);
        action.setTotalMaches(nChildNodes);
        action.setUpdateID(getSystemUpdateID());

        return true;
    }

    // Search
    private List<SearchCriteria> getSearchCriteriaList(String searchStr) {
        List<SearchCriteria> searchList = new ArrayList<SearchCriteria>();

        if (searchStr == null)
            return searchList;
        if (searchStr.compareTo("*") == 0)
            return searchList;

        StringTokenizer searchCriTokenizer = new StringTokenizer(searchStr, SearchCriteria.WCHARS);
        while (searchCriTokenizer.hasMoreTokens() == true) {
            String prop = searchCriTokenizer.nextToken();
            if (searchCriTokenizer.hasMoreTokens() == false)
                break;
            String binOp = searchCriTokenizer.nextToken();
            if (searchCriTokenizer.hasMoreTokens() == false)
                break;
            String value = searchCriTokenizer.nextToken();
            value = value.replace("\"", ""); // TODO
            String logOp = "";
            if (searchCriTokenizer.hasMoreTokens() == true)
                logOp = searchCriTokenizer.nextToken();
            SearchCriteria searchCri = new SearchCriteria();
            searchCri.setProperty(prop);
            searchCri.setOperation(binOp);
            searchCri.setValue(value);
            searchCri.setLogic(logOp);
            searchList.add(searchCri);
        }

        return searchList;
    }

    private boolean compare(List<SearchCriteria> searchCriList, ContentNode cnode, List<SearchCap> searchCapList) {
        int n;
        int searchCriCnt = searchCriList.size();

        // Set compare result
        for (n = 0; n < searchCriCnt; n++) {
            SearchCriteria searchCri = searchCriList.get(n);
            String property = searchCri.getProperty();
            if (property == null) {
                searchCri.setResult(true);
                continue;
            }
            SearchCap searchCap = getSearchCap(property);
            if (searchCap == null) {
                searchCri.setResult(true);
                continue;
            }
            boolean cmpResult = searchCap.compare(searchCri, cnode);
            searchCri.setResult(cmpResult);
        }

        // Eval only logical ADD operation at first;
        List<SearchCriteria> orSearchCriList = new ArrayList<SearchCriteria>();
        for (n = 0; n < searchCriCnt; n++) {
            SearchCriteria currSearchCri = searchCriList.get(n);
            if (n < (searchCriCnt - 1)) {
                if (currSearchCri.isLogicalAND() == true) {
                    SearchCriteria nextSearchCri = searchCriList.get(n + 1);
                    boolean currResult = currSearchCri.getResult();
                    boolean nextResult = nextSearchCri.getResult();
                    boolean logicalAND = (currResult & nextResult) ? true : false;
                    nextSearchCri.setResult(logicalAND);
                    continue;
                }
            }
            SearchCriteria orSearchCri = new SearchCriteria(currSearchCri);
            orSearchCriList.add(orSearchCri);
        }

        // Eval logical OR operation;
        int orSearchCriCnt = orSearchCriList.size();
        for (n = 0; n < orSearchCriCnt; n++) {
            SearchCriteria searchCri = searchCriList.get(n);
            if (searchCri.getResult() == true)
                return true;
        }

        return false;
    }

    private int getSearchContentList(ContentNode node, List<SearchCriteria> searchCriList, List<SearchCap> searchCapList, List<ContentNode> contentNodeList) {
        if (compare(searchCriList, node, searchCapList)) {
            contentNodeList.add(node);
        }

        int nChildNodes = node.getChildNodes().getLength();
        for (int n = 0; n < nChildNodes; n++) {
            ContentNode cnode = (ContentNode) node.getChildNodes().item(n);
            getSearchContentList(cnode, searchCriList, searchCapList, contentNodeList);
        }
        return contentNodeList.size();
    }

    private boolean searchActionReceived(SearchAction action) {
        String contaierID = action.getContainerID();
        ContentNode node = findContentNodeByID(contaierID);
        if (node == null)
            return false;

        String searchCriteria = action.getSearchCriteria();
        List<SearchCriteria> searchCriList = getSearchCriteriaList(searchCriteria);
        List<SearchCap> searchCapList = getSearchCapList();

        int n;
        List<ContentNode> contentNodeList = new ArrayList<ContentNode>();
        int nChildNodes = node.getChildNodes().getLength();
        for (n = 0; n < nChildNodes; n++) {
            ContentNode cnode = (ContentNode) node.getChildNodes().item(n);
            getSearchContentList(cnode, searchCriList, searchCapList, contentNodeList);
        }
        nChildNodes = contentNodeList.size();

        // Sort Content Node Lists
        String sortCriteria = action.getSortCriteria();
        List<ContentNode> sortedContentNodeList = sortContentNodeList(contentNodeList, sortCriteria);

        int startingIndex = action.getStartingIndex();
        if (startingIndex <= 0)
            startingIndex = 0;
        int requestedCount = action.getRequestedCount();
        if (requestedCount == 0)
            requestedCount = nChildNodes;

        DIDLLite didlLite = new DIDLLite();
        int numberReturned = 0;
        for (n = startingIndex; (n < nChildNodes && numberReturned < requestedCount); n++) {
            ContentNode cnode = sortedContentNodeList.get(n);
            didlLite.addContentNode(cnode);
            numberReturned++;
        }

        String result = didlLite.toString();
        action.setResult(result);
        action.setNumberReturned(numberReturned);
        action.setTotalMaches(nChildNodes);
        action.setUpdateID(getSystemUpdateID());

        return true;
    }

    /** QueryListener */
    private QueryListener queryListener = new QueryListener() {
        /** */
        public boolean queryControlReceived(StateVariable stateVariable) {
            return false;
        }
    };

    // HTTP Server
    public void doService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String uri = request.getRequestURI();
        if (!uri.startsWith(CONTENT_EXPORT_URI)) {
            throw new ServletException(""); // TODO add message
        }

        Enumeration<?> paramList = request.getParameterNames();
        while (paramList.hasMoreElements()) {
            String name = (String) paramList.nextElement();
Debug.println("[" + name + "] = " + request.getParameterValues(name));
        }

        // Getting item ID
        String id = request.getParameter(CONTENT_ID);

        // Has Item Node ?
        ContentNode node = findContentNodeByID(id);
        if (node == null) {
            throw new ServletException(""); // TODO add message
        }
        if (!(node instanceof ItemNode)) {
            throw new ServletException(""); // TODO add message
        }

        // Return item content
        ItemNode itemNode = (ItemNode) node;

        long contentLen = itemNode.getContentLength();
        String contentType = itemNode.getMimeType();
        InputStream contentIn = itemNode.getContentInputStream();

        if (contentLen <= 0 || contentType.length() <= 0 || contentIn == null) {
            throw new ServletException(""); // TODO add message
        }

        // Thanks for Robert Johansson <robert.johansson@kreatel.se>
        response.setContentType(contentType);
        response.setStatus(200);
        response.setContentLength((int) contentLen);

        byte[] buffer = new byte[1024];
        while (contentIn.available() > 0) {
            int l = contentIn.read(buffer);
            response.getOutputStream().write(buffer, 0, l);
        }

        contentIn.close();
    }

    // Content URL

    public String getContentExportURL(String id) {
        return "http://" + mediaServer.getInterfaceAddress() + ":" + mediaServer.getHttpPort() + CONTENT_EXPORT_URI + "?" + CONTENT_ID + "=" + id;
    }

    public String getContentImportURL(String id) {
        return "http://" + mediaServer.getInterfaceAddress() + ":" + mediaServer.getHttpPort() + CONTENT_IMPORT_URI + "?" + CONTENT_ID + "=" + id;
    }

    // run

    private static final int DEFAULT_SYSTEMUPDATEID_INTERVAL = 2000;

    private static final int DEFAULT_CONTENTUPDATE_INTERVAL = 60000;

    private long systemUpdateIDInterval;

    private long contentUpdateInterval;

    public void setSystemUpdateInterval(long itime) {
        systemUpdateIDInterval = itime;
    }

    public long getSystemUpdateIDInterval() {
        return systemUpdateIDInterval;
    }

    public void setContentUpdateInterval(long itime) {
        contentUpdateInterval = itime;
    }

    public long getContentUpdateInterval() {
        return contentUpdateInterval;
    }

    /** */
    private Runnable handler = new Runnable() {
        public void run() {
Debug.println("+++ ContentDirectory server started");
            MediaServer mediaServer = getMediaServer();
            StateVariable systemUpdateId = mediaServer.getStateVariable(SYSTEM_UPDATE_ID);

            int lastSystemUpdateId = 0;
            long lastContentUpdateTime = System.currentTimeMillis();

            while (true) {
                try {
                    Thread.sleep(getSystemUpdateIDInterval());
                } catch (InterruptedException e) {
                }

                // Update SystemUpdateID
                int currentSystemUpdateId = getSystemUpdateID();
                if (lastSystemUpdateId != currentSystemUpdateId) {
                    try {
                        systemUpdateId.setValue(String.valueOf(currentSystemUpdateId));
                        lastSystemUpdateId = currentSystemUpdateId;
                    } catch (Exception e) {
                        e.printStackTrace(System.err);
                    }
                }

                // Update Content Directory
                long currentTime = System.currentTimeMillis();
                if (getContentUpdateInterval() < (currentTime - lastContentUpdateTime)) {
                    for (int n = 0; n < getDirectoryList().size(); n++) {
                        getDirectoryList().get(n).update();
                    }
                    lastContentUpdateTime = currentTime;
                }
            }
        }
    };

    /** */
    public ActionListener getActionListener() {
        return actionListener;
    }

    /** */
    public QueryListener getQueryListener() {
        return queryListener;
    }

    /** */
    public Runnable getHandler() {
        return handler;
    }
}

/* */
