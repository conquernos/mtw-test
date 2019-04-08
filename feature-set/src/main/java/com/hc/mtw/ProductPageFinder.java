package com.hc.mtw;

import com.hc.mtw.common.webdriver.HtmlDocument;
import com.hc.mtw.common.webdriver.Url;
import org.conqueror.drone.selenium.webdriver.WebBrowser;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.sql.SQLException;
import java.util.*;


public class ProductPageFinder {

    private WebBrowser browser;

    private volatile String seedUrl;

    public ProductPageFinder(WebBrowser browser) {
        this.browser = browser;
    }

    public Url visit(String url) {
        if (browser.visit(url)) {
            return new Url(browser.getCurrentUrl());
        }

        return null;
    }

    public List<String> find(Url url, Set<String> foundUrls, int count) {
        List<String> urls = new ArrayList<>(1);
        HtmlDocument document = new HtmlDocument();
        document.setHtml(browser.getPageSource());
        Elements elements = document.getBody().getAllElements();
        for (Element element : elements) {
            if (!element.text().isEmpty() && element.tagName().equals("a")) {
                String productUrl = Url.makeUrl(url.getScheme(), url.getHost(), element.attr("href"));
                if (!foundUrls.contains(productUrl) && Url.isSameDoamin(new Url(productUrl), url)) {
                    urls.add(productUrl);
                    foundUrls.add(productUrl);
                    if (urls.size() >= count) break;
                }
            }
        }

        return urls;
    }

    public List<String> find(Url url, Set<String> foundUrls, int numberOfProducts, boolean hasImage) {
        if (url == null) return null;

        HtmlDocument document = new HtmlDocument();
        document.setHtml(browser.getPageSource());

        Elements elements = document.getBody().getAllElements();
        for (Element element : elements) {
            if (element.tagName().matches("(nav|a|script|img|header|body|td|article|link|i|p|iframe|input|font|form|footer|style|button)")) {
                continue;
            }

            if (element.childNodeSize() >= numberOfProducts) {
                Map<String, Elements> tagsOfChildren = new HashMap<>();
                int maxMembersOfTags = 0;
                String maxTag = null;
                for (Element child : element.children()) {
                    String tag = child.tagName();
                    Elements children = tagsOfChildren.get(tag);
                    if (children == null) {
                        children = new Elements();
                        tagsOfChildren.put(tag, children);
                    }

                    children.add(child);
                    if (maxMembersOfTags < children.size()) {
                        maxMembersOfTags = children.size();
                        maxTag = child.tagName();
                    }
                }

                if (maxMembersOfTags >= numberOfProducts && maxTag != null) {
                    Elements products = new Elements();
                    int numberOfCorrect = 0;
                    for (Element product : tagsOfChildren.get(maxTag)) {
                        boolean correct = false;
                        boolean aTag = false;
                        boolean imageTag = false;

                        for (Element childOfProduct : product.getAllElements()) {
                            if (childOfProduct.id().equals(product.id())) continue;

                            if (childOfProduct.is("a")) {
                                String productUrl = Url.makeUrl(url.getScheme(), url.getHost(), childOfProduct.attr("href"));
                                if (!foundUrls.contains(productUrl) && Url.isSameDoamin(new Url(productUrl), url)) {
                                    products.add(childOfProduct);
                                    aTag = true;
                                }
                            } else if (childOfProduct.is("img")) {
                                imageTag = true;
                            }

                            if (aTag && (!hasImage || imageTag)) {
                                correct = true;
                                break;
                            }
                        }

                        if (correct) numberOfCorrect++;
                    }

                    if (numberOfCorrect >= numberOfProducts) {
                        List<String> urls = new ArrayList<>(numberOfProducts);
                        for (Element product : products) {
                            String productUrl = Url.makeUrl(url.getScheme(), url.getHost(), product.attr("href"));
                            urls.add(productUrl);
                            foundUrls.add(productUrl);
                        }

                        return urls;
                    }
                }
            }
        }

        return null;
    }

    public List<String> find(String site) {
        List<String> results = Collections.emptyList();
        Set<String> foundElements = new HashSet<>();
        Set<String> startElements = new HashSet<>();

        System.out.println("site:" + site);

        Url seedUrl = visit(site);
        if (seedUrl == null) return results;

        foundElements.add(seedUrl.getUrl());
        startElements.add(seedUrl.getUrl());

        int findCount = 0;
        List<String> purls= find(seedUrl, foundElements, 10);
        while (purls != null && !purls.isEmpty()) {
            String page = null;
            boolean end = false;
            for (String purl : purls) {
                Url url = visit(purl);
                List<String> ppurls = find(url, foundElements, 10, true);
                if (ppurls != null && !ppurls.isEmpty()) {
                    results = ppurls;
                    page = ppurls.get(0);
                    findCount++;
                    break;
                } else if (!results.isEmpty()) {
                    end = true;
                }
            }

            if (end || findCount > 2) break;

            if (page == null) {
                for (String purl : purls) {
                    if (!startElements.contains(purl)) {
                        page = purl;
                        startElements.add(purl);
                        break;
                    }
                }

                if (page == null) {
                    foundElements.addAll(purls);
                    page = seedUrl.getUrl();
                }
            }

            Url url = visit(page);
            if (results.isEmpty()) {
                purls = find(url, foundElements, 10);
            } else {
                purls = find(url, foundElements, 10, true);
            }
        }

        return results;
    }

    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        WebBrowser browser = WebBrowser.createWebBrowser(WebBrowser.WebDriverName.CHROME, true);
        ProductPageFinder finder = new ProductPageFinder(browser);

        String site = "www.jollychic.com";

        List<String> results = finder.find(site);
        for (String result : results) {
            System.out.println(result);
        }

        browser.close();
    }

}
