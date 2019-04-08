package com.hc.mtw;

import com.hc.mtw.feature.VisualFeatureExtractor;
import org.conqueror.drone.selenium.webdriver.WebBrowser;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.openqa.selenium.WebElement;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static org.conqueror.drone.selenium.webdriver.WebBrowser.WebDriverName.CHROME;
import static org.conqueror.drone.selenium.webdriver.WebBrowser.getFontSize;
import static org.conqueror.drone.selenium.webdriver.WebBrowser.getLocation;


public class PicoTagExtractor extends TagExtractor {

    private final VisualFeatureExtractor extractor;

    private String productName;
    private String productPrice;
    private String productDesc;

    public PicoTagExtractor(WebBrowser browser, int minClusters, int attempts) {
        super(browser, minClusters, attempts);
        extractor = new VisualFeatureExtractor(browser);
    }

    public Tags extract(String productName, String productPrice, String productDesc, int maxCenters) {
        List<WebElement> includedElements = new ArrayList<>();
        List<WebElement> parentElements = new ArrayList<>();
        List<WebElement> textElements = getTextWebElements(includedElements, parentElements);
        System.out.println("end getting text elements : " + textElements.size());
        if (textElements.isEmpty()) return null;

        List<WebElement> centers = getCenterWebElements(maxCenters);
        System.out.println("end getting center elements : " + centers.size());

        setCorrectSet(productName, productPrice, productDesc);

        return tag(centers, textElements, includedElements, parentElements);
    }

    private void setCorrectSet(String productName, String productPrice, String productDesc) {
        this.productName = productName.replaceAll("\\s", "");
        this.productPrice = productPrice.replaceAll("\\s", "");
        this.productDesc = productDesc.replaceAll("\\s", "");
    }

    public Tags tag(List<WebElement> centers, List<WebElement> elements, List<WebElement> includedElements, List<WebElement> parentElements) {
        WebElement center = (centers == null || centers.isEmpty())? elements.get(0) : centers.get(0);
        final String[] filterOutTags = new String[]{"a", "button", "select"};

        elements.addAll(includedElements);
        elements.addAll(parentElements);

        int elementSize = elements.size();
//        float maxFontSize = 0.f;
//        float maxElementSize = 0.f;
//        RGB totRGB = new RGB(0, 0, 0);
        List<WebElement> filteredElements = new ArrayList<>(elementSize);
        for (WebElement element : elements) {
            if (browser.isParentOfSelfTagName(filterOutTags, element, attempts)) continue;
            filteredElements.add(element);
//
//            float size = getFontSize(element);
//            maxFontSize = Math.max(maxFontSize, size);
//
//            size = WebBrowser.getElementSize(element);
//            maxElementSize = Math.max(maxElementSize, size);
//
//            totRGB.add(getFontColor(element));
        }

        List<Tag> pnameElements = new ArrayList<>();
        List<Tag> priceElements = new ArrayList<>();
        List<Tag> descElements = new ArrayList<>();
        List<Tag> noneElements = new ArrayList<>();

        for (WebElement element : filteredElements) {
            String text = element.getText().replaceAll("\\s", "");
            WebBrowser.RelLocation location = getLocation(center, element);

            Tag tag = new Tag(element, location, 0d);
            if (text.equalsIgnoreCase(productName)) pnameElements.add(tag);
            else if (text.contains(productPrice)) priceElements.add(tag);
            else if (text.equalsIgnoreCase(productDesc)) descElements.add(tag);
            else noneElements.add(tag);
        }

        return new Tags(pnameElements, priceElements, descElements, noneElements);
    }

    private List<WebElement> getPrincipalElements(List<WebElement> elements) {
        return null;
    }

    private float getMaxFontSize(List<WebElement> elements) {
        float max = 0.f;
        for (WebElement element : elements) {
            float size = getFontSize(element);
            max = Math.max(max, size);
        }

        return max;
    }

    private static int getIndexOfMaximumValue(INDArray array) {
        double max = 0.d;
        int index = 0;
        for (int idx = 0; idx < array.columns(); idx++) {
            double value = array.getDouble(idx);
            if (max < value) {
                max = value;
                index = idx;
            }
        }

        return index;
    }

    private static int getIndexOfSecondMaximumValue(INDArray array) {
        int maxIndex = getIndexOfMaximumValue(array);
        double max = 0.d;
        int index = 0;
        for (int idx = 0; idx < array.columns(); idx++) {
            if (idx == maxIndex) continue;

            double value = array.getDouble(idx);
            if (max < value) {
                max = value;
                index = idx;
            }
        }

        return index;
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
//            ,"http://bananarepublic.gap.com/browse/product.do?cid=1099058&pcid=48422&vid=1&pid=888163002"
//            , "http://www.worldwidegolfshops.com/15-Hinged-Cup-Retriever/10110977/Product"

//            , "http://www.soccerpro.com/Adidas-Copa-Mundial-Black-with-White-p3808/"
//            , "http://www.soccersavings.com/2017-18-replica/adidas-juventus-17-18-3rd-replica-jersey"
//            , "http://www.sorel.com/baby-caribootie-1751171.html?cgid=kids-boys&dwvar_1751171_variationColor=651"
//            , "http://www.sotostore.com/1461-mono-smooth-black-87994"
//            , "http://www.spartoo.com/0-105-Revival-glitter-Femme-Marron-x735302.php"
//            , "http://www.spenceclothing.com/store/boy/Diesel-kids-bermuda-platt-shorts-art63824.html"
//            , "http://www.sperry.com/en/adelia-york-sandal/28396W.html?dwvar_28396W_color=STS97791"
//            , "http://www.sport-conrad.com/en/products/22designs/hammerhead-outlaw-ntn-125mm.html"
//            , "http://www.sportsk.com/"
//            , "http://www.sportsshoes.com/product/adi10002/adidas-pw-new-york-adizero-ubersonic-2-tennis-shoes-~-aw17/"
//            , "http://www.sportsunlimitedinc.com/12ballcaddy.html"
//            , "http://www.steepandcheap.com/4-u-performance-printed-nylon-spandex-mesh-bra-womens?skid=FYP000F-WATPRI-XS&ti=UExQIENhdDpXb21lbidzIFVuZGVyd2VhcjoxOjY6c2FjQ2F0MjEwMDAxMzU="
//            , "http://www.stjohnknits.com/aadi-tweed-knit-dress-k11m0k1"
//            , "http://www.streetmoda.com/collections/accessories/products/street-moda-gift-card"
//            , "http://www.stuartweitzman.com/products/5050/?ColMatID=263&DepartmentGroupId=2&DepartmentId=602"
//            , "http://www.summitsports.com/0028BUN.html"
//            , "http://www.sunuva.com/ailera-classic-hammam-white-towel-with-towelling-reverse"
//            , "http://www.supre.com.au/p/34-sleeve-deep-v-top/2080445100501.html?region=AU"
//            , "http://www.surfdome.com/686_snow_jackets_-_686_mns_glcr_gore_smrty_weapon_jkt__-_red_color_block-277384"
//            , "http://www.swarovski.com/Web_US/en/1002980/product/Chinese_Zodiac_-_Tiger.html"
//            , "http://www.swell.com/2-bandits-pacific-coast-highway-bandana"
//            , "http://www.swim2000.com/20/product/aqua-sphere-aqua-comfort-swim-cap/"
//            , "http://www.swimsuitsforall.com/2630-Black-Long-board-short#rrec=true"
//            , "http://www.tacticaldistributors.com/products/16-oz-wide-mouth-w-flip-lid-2017"
//            , "http://www.tennisexpress.com/2xu-mens-long-sleeve-compression-top-black-44466"
//            , "http://www.tennisnuts.com/shop/badminton/badminton-bags/li-ning-pro-6-racket-bag-black-1156391.html"
//            , "http://www.tgw.com/accessories/bridgestone-2017-nfl-e6-soft-golf-balls"
//            , "http://www.thaliasurf.com/collections/art/products/boundless-brooklyn-halfpipe-model-kit"
//            , "http://www.thebureaubelfast.com/shop/3257/heather-burgundy-1pac-pocket-tee"
//            , "https://www.theclassroomshop.com/collections/featured-home-page/products/elliot-suede-short-sleeve-shirt-midnight"
//            , "http://www.thedreslyn.com/1930-s-bandana-7.html"
//            , "https://www.thegoodwillout.com/nike-air-max-180-ultramarine-white-ultramarine-solar-red-615287-100"
//            , "http://www.thehatpros.com/100-629-otto-cap-acrylic-knits-caps-1-dozen/"
//            , "https://theorchidboutique.com/collections/maylana/products/maylana-kai-nude-top?variant=4673071513633"
//            , "http://www.theshoemart.com/alden-mens-30mm-calf-dress-belt-black-gold-mb0101"
//            , "http://www.threadless.com/product/1000/Runnin_Rhino"
//            , "http://deal.11st.co.kr/product/SellerProductDetail.tmall?method=getSellerProductDetail&prdNo=1607179608&trTypeCd=22&trCtgrNo=895019"
//            , "http://item.gmarket.co.kr/Item?goodscode=1107126970"
//            , "http://www.ssg.com/item/itemView.ssg?itemId=1000021582883&siteNo=6004&salestrNo=6005"
//            , "http://www.ticketmonster.co.kr/deal/782394774?opt_deal_srl=782440686&NaPm=ct%3Djdjyve14%7Cci%3D805ec1682b88d6eb681be80af4dd5a0fab82a8e1%7Ctr%3Dslc%7Csn%3D221844%7Chk%3Dd55d2af2a2cdd381789e10aeff07cd653b480758&coupon_srl=2000658&utm_source=naver&utm_medium=affiliate&utm_term=&utm_content=&utm_campaign=META_%EB%84%A4%EC%9D%B4%EB%B2%84%EC%A7%80%EC%8B%9D%EC%87%BC%ED%95%91"
            , "https://www.dope-factory.com/products/under-armour-ua-curry-4-1298306-0603"
            , "https://80spurple.com/collections/eyewear-cat-eye-sunglasses/products/oversized-round-reflective-sunglasses?variant=17678065285"
        };

        final String modelFile = "/Users/haimjoon/IdeaProjects/da-mtw/feature-set/model.zip";

        final int minClusters = 3;
        final int maxCenters = 3;
        final int attempts = 3;

        WebBrowser browser = WebBrowser.createWebBrowser(CHROME, true);
        browser.setWindowSize(1800, 1200);

        PicoTagExtractor extractor = new PicoTagExtractor(browser, minClusters, attempts);

        for (String url : urls) {
            if (url.length() == 0) continue;
            System.out.println(url);

            Tags tags = extractor.extract(url, maxCenters);
            if (tags == null) continue;

            for (Tag tag : tags.getPnames()) {
                System.out.printf("name : %s - %s (%.2f)\n", tag.getElement().getText(), browser.getXPathFromElement(tag.getElement()), tag.getScore());
            }
            for (Tag tag : tags.getPrices()) {
                System.out.printf("price : %s - %s (%.2f)\n", tag.getElement().getText(), browser.getXPathFromElement(tag.getElement()), tag.getScore());
            }
            for (Tag tag : tags.getDescs()) {
                System.out.printf("desc : %s - %s (%.2f)\n", tag.getElement().getText(), browser.getXPathFromElement(tag.getElement()), tag.getScore());
            }

//            BufferedImage taggedImage = extractor.getTaggedImage(tags);
//
//            ImageUtils.displayImage(taggedImage);

//            String domain = new URI(url).getHost();
//            ImageUtils.writeImage(resizedImage, "/Users/haimjoon/Workspace/mtw/" + domain + ".png");
        }
    }

}
