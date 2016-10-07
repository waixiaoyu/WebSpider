package Ch1;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import javax.xml.ws.Response;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

public class RetrivePage {
    private static Logger log = Logger.getLogger(RetrivePage.class);

    /**
     * for storing the page which has been visited. When exploring the new page,
     * checking the set first. If it exists, just skip it. Otherwise, add this
     * url to BFS list, and add it to Visited set.
     */
    private Set<String> setVisited = new HashSet<>();
    private Queue<String> queueBFS = new LinkedList<>();

    /**
     * download the page, and get its content
     * 
     * @param path
     */

    public byte[] downloadPage(String url) {
        byte[] bytes = null;

        CloseableHttpClient httpClient = HttpClients.createDefault();

        HttpGet httpGet = new HttpGet(url);

        // 查看默认request头部信息  
        System.out.println("Accept-Charset:" + httpGet.getFirstHeader("Accept-Charset"));
        // 以下这条如果不加会发现无论你设置Accept-Charset为gbk还是utf-8，他都会默认返回gb2312（本例针对google.cn来说）  
        httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.1.2)");
        // 用逗号分隔显示可以同时接受多种编码  
        httpGet.setHeader("Accept-Language", "zh-cn,zh;q=0.5");
        httpGet.setHeader("Accept-Charset", "GB2312,utf-8;q=0.7,*;q=0.7");
        // 验证头部信息设置生效  
        System.out.println("Accept-Charset:" + httpGet.getFirstHeader("Accept-Charset").getValue());

        // Execute HTTP request  
        System.out.println("executing request " + httpGet.getURI());

        //set connection time out
        RequestConfig config = RequestConfig.custom().setSocketTimeout(3000).setConnectTimeout(3000).setConnectionRequestTimeout(3000).build();
        httpGet.setConfig(config);

        HttpContext httpContext = new BasicHttpContext();
        try {
            HttpResponse response = httpClient.execute(httpGet, httpContext);

            // Get hold of the response entity  
            HttpEntity entity = response.getEntity();

            if (entity != null) {

                int nStatusCode = response.getStatusLine().getStatusCode();
                if (nStatusCode == HttpStatus.SC_OK) {
                    bytes = EntityUtils.toByteArray(entity);
                    log.info(new String(bytes));
                    return bytes;
                } else if (nStatusCode == HttpStatus.SC_MOVED_TEMPORARILY || nStatusCode == HttpStatus.SC_MOVED_PERMANENTLY
                        || nStatusCode == HttpStatus.SC_SEE_OTHER || nStatusCode == HttpStatus.SC_TEMPORARY_REDIRECT) {
                    String strRedirect = response.getHeaders("Location")[0].getValue();
                    log.info("redirect to " + strRedirect);
                    if (strRedirect.startsWith("http")) {
                        log.info("redirect to " + strRedirect);
                        return downloadPage(strRedirect);
                    } else {
                        HttpHost targetHost = (HttpHost) httpContext.getAttribute(HttpCoreContext.HTTP_TARGET_HOST);
                        HttpUriRequest realRequest = (HttpUriRequest) httpContext.getAttribute(HttpCoreContext.HTTP_REQUEST);
                        log.info("targetHost " + targetHost.getHostName());
                        log.info("realRequest  " + realRequest.getURI());
                        return downloadPage("http://" + targetHost.getHostName() + realRequest.getURI() + strRedirect);
                    }
                }
            }

        } catch (IOException e) {
            log.error(e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return bytes;

    }

    /**
     * check whether the url has been visited, if true, jump it, if false, add
     * it to set and queue
     * 
     * @param url
     * @return
     */
    private boolean isVisitedAndAddUrl(String url) {
        if (setVisited.contains(url)) {
            return true;
        } else {
            setVisited.add(url);
            queueBFS.add(url);
            return false;
        }
    }

    /**
     * write the page content into local file
     * 
     * @param path
     * @param content
     * @throws IOException
     */
    private void saveToLocal(String path, Byte[] bytes) throws IOException {
        String filename = path.substring(path.indexOf('/') + 2).replace('/', '_').replace('?', '-') + ".html";
        FileWriter output = new FileWriter(filename);
        output.write(content);
        output.close();
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
            byte[] bytes = downloadPage(strUrl);
            //saveToLocal(strUrl, strContent);
            setVisited.add(strUrl);
            Set<String> links = HtmlParserTool.extracLinks(strUrl, filter);
            for (String string : links) {
                isVisitedAndAddUrl(string);
            }
        }
    }

    public static void main(String[] args) throws ClientProtocolException, IOException {
        RetrivePage rp = new RetrivePage();
        String[] seeds = { "http://www.lietu.com/images/news/Discuss2.JPG" };
        rp.crawling(seeds);
    }
}
