package Ch1;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.log4j.Logger;

public class SaveFile {
    private static Logger log = Logger.getLogger(SaveFile.class);
    private static final String prefix = "temp//";

    /**
     * from url to legal file name for system
     * 
     * @param url
     * @param contentType
     * @return
     */
    //    private static String getFileNameByUrl(String url, String contentType) {
    //        //remove http
    //        url = url.substring(7);
    //        //text/html
    //        if (contentType.indexOf("html") != -1) {
    //            url = url.replaceAll("[\\?/:*|<>\"]", "_") + ".html";
    //            return url;
    //        } else {
    //            return url.replaceAll("[\\?/:*|<>\"]", "_") + contentType.substring(contentType.lastIndexOf("/") + 1);
    //        }
    //    }

    private static String getFileNameByUrl(String url) {
        //remove http
        url = url.substring(7);
        //text/html
        return url.replaceAll("[\\?/:*|<>\"]", "_") + ".html";
    }

    /**
     * write the page content into local file
     * 
     * @param path
     * @param content
     * @throws IOException
     */
    public static void saveToLocal(String path, byte[] bytes) throws IOException {
        String filename = getFileNameByUrl(path);

        File f = new File(prefix);
        if (!f.exists() && !f.isDirectory()) {
            log.info("directory is not existed, create it.");
            f.mkdir();
        }
        f = new File(filename);
        if (f.exists()) {
            f.delete();
        }
        DataOutputStream out = new DataOutputStream(new FileOutputStream(new File(prefix + filename)));
        out.write(bytes);
        out.close();
        log.info("save successful: " + prefix + filename);
    }
}
