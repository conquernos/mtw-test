package com.hc.mtw.common.webdriver;

public class Url {

    private String url;
    private String scheme;
    private String host;

    public Url(String url) {
        int schemeIdx = url.indexOf("://");
        schemeIdx = (schemeIdx == -1)? 0 : schemeIdx + 3;
        int hostIdx = url.indexOf('/', schemeIdx);
        if (hostIdx == -1) hostIdx = url.length();

        scheme = url.substring(0, schemeIdx);
        host = url.substring(schemeIdx, hostIdx);
        this.url = makeUrl(scheme, host, url);
    }

    public String getUrl() {
        return url;
    }

    public String getScheme() {
        return scheme;
    }

    public String getHost() {
        return host;
    }


    public static String makeUrl(String scheme, String host, String url) {
        url = url.replaceFirst("^//", "");
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() -1);
        }
        if (url.startsWith("/")) {
            url = scheme + host + url;
        }

        if (url.length() == 0 || url.equals("#")) {
            url = scheme + host;
        }

        return url;
    }

    public static boolean isSameDoamin(Url url1, Url url2) {
        return url1.getHost().equals(url2.getHost());
    }

}
