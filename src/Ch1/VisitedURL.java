package Ch1;

public class VisitedURL {
    String url;

    public VisitedURL(String url) {
        super();
        this.url = url;
    }

    @Override
    public int hashCode() {
        String strMD5 = MD5Tools.MD5(url);
        return strMD5.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof VisitedURL) {
            VisitedURL v = (VisitedURL) obj;
            return this.url.equals(v.url) ? true : false;
        }
        return super.equals(obj);
    }

}
