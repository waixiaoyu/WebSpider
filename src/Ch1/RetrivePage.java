package Ch1;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

public class RetrivePage {
    private static Logger log = Logger.getLogger(RetrivePage.class);

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
        log.info("default header: " + "Accept-Charset:" + httpGet.getFirstHeader("Accept-Charset"));
        // 以下这条如果不加会发现无论你设置Accept-Charset为gbk还是utf-8，他都会默认返回gb2312（本例针对google.cn来说）  
        httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.1.2)");
        // 用逗号分隔显示可以同时接受多种编码  
        httpGet.setHeader("Accept-Language", "zh-cn,zh;q=0.5");
        httpGet.setHeader("Accept-Charset", "GB2312,utf-8;q=0.7,*;q=0.7");
        // 验证头部信息设置生效  
        log.info("Accept-Charset:" + httpGet.getFirstHeader("Accept-Charset").getValue());

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
                    //log.info(new String(bytes));
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
}
