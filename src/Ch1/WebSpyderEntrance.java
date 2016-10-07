package Ch1;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import org.apache.http.client.ClientProtocolException;
import org.apache.log4j.Logger;

public class WebSpyderEntrance {
    private static Logger log = Logger.getLogger(WebSpyderEntrance.class);
    private static RetrivePage rp = new RetrivePage();
    /**
     * for storing the page which has been visited. When exploring the new page,
     * checking the set first. If it exists, just skip it. Otherwise, add this
     * url to BFS list, and add it to Visited set.
     */
    private Set<String> setVisited = new HashSet<>();
    private Queue<String> queueBFS = new LinkedList<>();

    /**
     * check whether the url has been visited, if true, jump it, if false, add
     * it to set and queue
     * 
     * @param url
     */
    private void isVisitedAndAddUrl(String url) {
        LinkFilter lf = new LinkFilter() {
            @Override
            public boolean accept(String url) {
                if (url.matches(".*\\.(?i)jpg") || url.matches(".*\\.(?i)gif") || url.matches(".*\\.(?i)png")) {
                    return false;
                } else {
                    return true;
                }
            }
        };
        if (lf.accept(url)) {
            if (setVisited.contains(url)) {
            } else {
                setVisited.add(url);
                queueBFS.add(url);
            }
        }

    }

    /**
     * init queue. Add all seed url to queue
     * 
     * @param strSeeds
     */
    private void initQueueBFS(String[] strSeeds) {
        for (String string : strSeeds) {
            queueBFS.add(string);
        }
    }

    /**
     * the entrance of crawling program
     * 
     * @throws IOException
     */
    public void crawling(String[] strSeeds) throws IOException {
        LinkFilter filter = new LinkFilter() {
            @Override
            public boolean accept(String url) {
                return url.startsWith("http://www.lietu.com") ? true : false;
            }
        };
        initQueueBFS(strSeeds);
        while (!queueBFS.isEmpty() && setVisited.size() < 1000) {
            String strUrl = queueBFS.poll();
            log.info("current page is " + strUrl);
            byte[] bytes = rp.downloadPage(strUrl);
            if (bytes != null) {
                SaveFile.saveToLocal(strUrl, bytes);
                setVisited.add(strUrl);
                Set<String> links = HtmlParserTool.extracLinks(strUrl, filter);
                for (String string : links) {
                    isVisitedAndAddUrl(string);
                }
            }

        }
    }

    public static void main(String[] args) throws ClientProtocolException, IOException {
        WebSpyderEntrance wse = new WebSpyderEntrance();
        String[] seeds = { "http://www.lietu.com" };
        wse.crawling(seeds);
    }
}
