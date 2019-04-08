package com.hc.mtw;

import com.hc.mtw.common.cv.ImageUtils;
import org.conqueror.drone.selenium.webdriver.WebBrowser;
import org.openqa.selenium.WebElement;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static org.conqueror.drone.selenium.webdriver.WebBrowser.RelLocation.CENTER;
import static org.conqueror.drone.selenium.webdriver.WebBrowser.RelLocation.DOWN;
import static org.conqueror.drone.selenium.webdriver.WebBrowser.RelLocation.UP;
import static org.conqueror.drone.selenium.webdriver.WebBrowser.WebDriverName.CHROME;


public class RuleBasedTagExtractor extends TagExtractor {

    public RuleBasedTagExtractor(WebBrowser browser, int minClusters, int attempts) {
        super(browser, minClusters, attempts);
    }

    @Override
    public Tags tag(List<WebElement> centers, List<WebElement> textElements, List<WebElement> includedElements, List<WebElement> parentElements) {
        int numberOfTry = 0;
        for (WebElement center : centers) {
            System.out.println("try - " + numberOfTry++ + " (" + browser.getXPathFromElement(center) + ")");
//                System.out.println("try - " + numberOfTry++ + " (" + center.cssSelector() + ")");

            WebElement parent = getCenterParentElement(center, textElements, minClusters);
            if (parent == null) continue;
            System.out.println("end getting parent element : " + browser.getXPathFromElement(parent));
//                System.out.println("end getting parent element : " + parent.cssSelector());

            List<WebElement> elements = prune(textElements, parent);
            if (elements == null || elements.size() < 4) continue;
            System.out.println("end extracting elements");

            Tags tags = tag(center, elements, includedElements, parentElements);
            System.out.println("end tagging");
            int count = 0;
            if (tags.hasPrices()) count++;
            if (tags.hasPnames()) count++;
            if (tags.hasDescs()) count++;

            if (count > 2) return tags;
        }

        return null;
    }

    public Tags tag(WebElement center, List<WebElement> elements, List<WebElement> includedElements, List<WebElement> parentElements) {
        Tag pname = null;
        float pnameSize = 0f;
        Tag desc = null;
        int descWordCount = 0;
        final String PRICE_REGEX = "((£|€|\\$|₩|USD|AUD|EUR|KRW|원|달러|유로)+[\\s]*[0-9.,]+[\\s]*(USD|AUD|EUR|KRW|원|달러|유로|)" +
            "|(£|€|\\$|₩|USD|AUD|EUR|KRW|원|달러|유로|)[\\s]*[0-9.,]+[\\s]*(USD|AUD|EUR|KRW|원|달러|유로)+)";

        List<Tag> prices = new ArrayList<>();
        List<Tag> remains = new ArrayList<>();

        final String[] filterOutTags = new String[]{"a", "button", "select"};

        // prices
        for (WebElement element : elements) {
            // filter out - anchor
            if (browser.isParentOfSelfTagName(filterOutTags, element, attempts)) continue;

            String tText = element.getText();
            if (tText.matches(PRICE_REGEX)) {
                prices.add(new Tag(element, browser.getLocation(center, element), 1d));
            } else {
                remains.add(new Tag(element, browser.getLocation(center, element), 1d));
            }
        }

        if (prices.isEmpty()) {
            for (WebElement element : includedElements) {
                // filter out - anchor
                if (browser.isParentOfSelfTagName(filterOutTags, element, attempts)) continue;

                String tText = element.getText();
                if (tText.matches(PRICE_REGEX)) {
                    prices.add(new Tag(element, browser.getLocation(center, element), 1d));
                }
            }

            if (prices.isEmpty()) {
                for (WebElement element : parentElements) {
                    // filter out - anchor
                    if (browser.isParentOfSelfTagName(filterOutTags, element, attempts)) continue;

                    String tText = element.getText();
                    if (tText.matches(PRICE_REGEX)) {
                        prices.add(new Tag(element, browser.getLocation(center, element), 1d));
                    }
                }
            }
        }

        // pname
        for (Tag tag : remains) {
            if (browser.getWordCount(tag.getElement()) < 2
                || browser.getTextPercent(tag.getElement()) < 0.5f
                || tag.getLocation() == CENTER
                || tag.getLocation() == DOWN) continue;

            float tSize = browser.getFontSize(tag.getElement());
            if (pnameSize < tSize) {
                pnameSize = tSize;
                pname = tag;
            }
        }
        remains.remove(pname);

        // desc
        for (Tag tag : remains) {
            int tWordCount = browser.getWordCount(tag.getElement());

            if (tWordCount < 2 || tag.getLocation() == UP) {
                continue;
            }

            if (descWordCount < tWordCount) {
                descWordCount = tWordCount;
                desc = tag;
            }
        }
        remains.remove(desc);

        // price
        List<Tag> up = new ArrayList<>(prices.size());
        List<Tag> down = new ArrayList<>(prices.size());
        List<Tag> right = new ArrayList<>(prices.size());
        List<Tag> left = new ArrayList<>(prices.size());
        for (Tag tag : prices) {
//            switch (browser.getLocation(toWebElement(center, attempts), price)) {
            switch (tag.getLocation()) {
                case RIGHT:
                    right.add(tag);
                    break;
                case DOWN:
                    down.add(tag);
                    break;
                case LEFT:
                    left.add(tag);
                    break;
                case UP:
                    up.add(tag);
                    break;
            }
        }
        if (right.size() > 0) {
            prices = right;
        } else if (left.size() > 0) {
            prices = left;
        } else if (up.size() > 0) {
            prices = up;
        } else if (down.size() > 0) {
            prices = down;
        }

        return new Tags(pname, prices, desc, remains);
    }

    public static void main(String[] args) throws IOException, URISyntaxException {
        String[] urls = new String[]{
            ""
//            , "http://www.indexpdx.com/adidas-ali-classic-ii-eric-bailey/"
//            , "http://www.lastcall.com/1-Like-No-Other-Floral-Script-Button-Shirt-Navy-Into-the-Blue/prod43100033_cat16510018_cat16580014_cat000002/p.prod?cmCat=product&eItemId=prod43100033&rte=%252Fcategory.jsp%253FitemId%253Dcat16510018%2526pageSize%253D30%2526No%253D0%2526refinements%253D&searchType=EndecaDrivenCat"
//            , "http://item.gmarket.co.kr/Item?goodscode=301110076"
//            , "http://deal.11st.co.kr/product/SellerProductDetail.tmall?method=getSellerProductDetail&prdNo=860773250&trTypeCd=22&trCtgrNo=895019"
//            , "http://www.crooksncastles.com/collections/headwear/products/ak-script-strapback-cap-gold-wheat"
//            , "https://www.junioredition.com/collections/new-in/products/basic-romper-in-white-by-tinycottons"
//            , "http://www.ross-simons.com/products/444001.html"
//            , "https://uk.tommy.com/2-pack-invisible-socks-fba3425001300"
//            , "http://www.7forallmankind.com/-luxe-performance-slimmy-slim-in-white-/d/13412C20034?CategoryId=2861&Page=5"
//            , "http://www.bungiestore.com/collections/accessories/products/destiny-2-edition-razer-deathadder-elite-usb-optical-mouse"
//            , "https://apac.suitsupply.com/ko/suits/%EA%B7%B8%EB%A0%88%EC%9D%B4-%ED%94%8C%EB%A0%88%EC%9D%B8-napoli/P5289I.html?cgid=Suits"
//            , "www.ross-simons.com/products/846851.html"
//            , "https://www.ebags.com/product/heritage/soho-leather-mobile-office/204791?productid=10110409"
//            , "http://bananarepublic.gap.com/browse/product.do?cid=1099058&pcid=48422&vid=1&pid=885925022"
//            , "https://www.livlyclothing.com/sandy-dress-little-miss-fix-it.html"
//                ,"http://bananarepublic.gap.com/browse/product.do?cid=1099058&pcid=48422&vid=1&pid=888163002"
//            , "https://www.soccerpro.com/Adidas-Copa-Mundial-Black-with-White-p3808/"
//            , "http://deal.11st.co.kr/product/SellerProductDetail.tmall?method=getSellerProductDetail&prdNo=1607179608&trTypeCd=22&trCtgrNo=895019"
            , "http://item.gmarket.co.kr/Item?goodscode=758053599"
        };

        final int minClusters = 3;
        final int maxCenters = 3;
        final int attempts = 3;

        WebBrowser browser = WebBrowser.createWebBrowser(CHROME, WebBrowser.CHROME_DEFAULT_OPTIONS, true);
        browser.setWindowSize(1800, 1200);

        TagExtractor extractor = new RuleBasedTagExtractor(browser, minClusters, attempts);

        for (String url : urls) {
            if (url.length() == 0) continue;
            System.out.println(url);

            Tags tags = extractor.extract(url, maxCenters);
            if (tags == null) continue;

            BufferedImage taggedImage = extractor.getTaggedImage(tags);

            ImageUtils.displayImage(taggedImage);

//            String domain = new URI(url).getHost();
//            ImageUtils.writeImage(resizedImage, "/Users/haimjoon/Workspace/mtw/" + domain + ".png");
        }
    }

}
