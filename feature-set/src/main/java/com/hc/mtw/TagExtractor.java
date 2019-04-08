package com.hc.mtw;

import com.hc.mtw.common.cv.ImageUtils;
import com.hc.mtw.common.cv.RectPoint;
import com.hc.mtw.common.webdriver.HtmlDocument;
import org.conqueror.drone.selenium.webdriver.RGB;
import org.conqueror.drone.selenium.webdriver.WebBrowser;
import org.jsoup.nodes.Element;
import org.openqa.selenium.WebElement;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.List;

import static org.conqueror.drone.selenium.webdriver.WebBrowser.getElementSize;
import static org.conqueror.drone.selenium.webdriver.WebBrowser.getFontColor;
import static org.conqueror.drone.selenium.webdriver.WebBrowser.getFontSize;


public abstract class TagExtractor {

    public static class Tag implements Comparable {

        private WebElement element;
        private WebBrowser.RelLocation location;
        private double score;
        private double[] features;

        public Tag(WebElement element, WebBrowser.RelLocation location, double score) {
            this.element = element;
            this.location = location;
            this.score = score;
            this.features = null;
        }

        public Tag(WebElement element, WebBrowser.RelLocation location, double[] features) {
            this.element = element;
            this.location = location;
            this.score = 0d;
            this.features = features;
        }

        public WebElement getElement() {
            return element;
        }

        public WebBrowser.RelLocation getLocation() {
            return location;
        }

        public double getScore() {
            return score;
        }

        public double[] getFeatures() {
            return features;
        }

        @Override
        public int compareTo(Object o) {
            return Double.compare(this.score, ((Tag)o).getScore()) * -1;
        }

    }

    public static class Tags {

        private List<Tag> pnames;
        private List<Tag> prices;
        private List<Tag> descs;
        private List<Tag> nones;
        private int maxElementSize;
        private float maxFontSize;
        private RGB averageColor;

        public Tags(Tag pname, List<Tag> prices, Tag desc, List<Tag> nones) {
            this.pnames = new ArrayList<>(1);
            this.descs = new ArrayList< >(1);
            this.prices = prices;
            this.nones = nones;
        }

        public Tags(List<Tag> pnames, List<Tag> prices, List<Tag> descs, List<Tag> nones) {
            this.pnames = pnames;
            this.prices = prices;
            this.descs = descs;
            this.nones = nones;
            this.maxElementSize = calcMaxElementSize();
            this.maxFontSize = calcMaxFontSize();
            this.averageColor = calcAverageColor();
        }

        public List<Tag> getPnames() {
            return pnames;
        }

        public List<Tag> getPrices() {
            return prices;
        }

        public List<Tag> getDescs() {
            return descs;
        }

        public List<Tag> getNones() {
            return nones;
        }

        public boolean hasPnames() {
            return pnames != null && !pnames.isEmpty();
        }

        public boolean hasPrices() {
            return prices != null && !prices.isEmpty();
        }

        public boolean hasDescs() {
            return descs != null && !descs.isEmpty();
        }

        public boolean hasNones() {
            return nones != null && !nones.isEmpty();
        }

        public int getMaxElementSize() {
            return maxElementSize;
        }

        public float getMaxFontSize() {
            return maxFontSize;
        }

        public RGB getAverageColor() {
            return averageColor;
        }

        private int calcMaxElementSize() {
            int maxSize = 0;

            if (hasPnames()) {
                maxSize = Math.max(calcMaxElementSize(getPnames()), maxSize);
            }

            if (hasPrices()) {
                maxSize = Math.max(calcMaxElementSize(getPrices()), maxSize);
            }

            if (hasDescs()) {
                maxSize = Math.max(calcMaxElementSize(getDescs()), maxSize);
            }

            if (hasNones()) {
                maxSize = Math.max(calcMaxElementSize(getNones()), maxSize);
            }

            return maxSize;
        }

        private int calcMaxElementSize(List<Tag> tags) {
            int maxSize = 0;

            for (Tag tag : tags) {
                maxSize = Math.max(getElementSize(tag.getElement()), maxSize);
            }

            return maxSize;
        }

        private float calcMaxFontSize() {
            float maxSize = 0.f;

            if (hasPnames()) {
                maxSize = Math.max(calcMaxFontSize(getPnames()), maxSize);
            }

            if (hasPrices()) {
                maxSize = Math.max(calcMaxFontSize(getPrices()), maxSize);
            }

            if (hasDescs()) {
                maxSize = Math.max(calcMaxFontSize(getDescs()), maxSize);
            }

            if (hasNones()) {
                maxSize = Math.max(calcMaxFontSize(getNones()), maxSize);
            }

            return maxSize;
        }

        private float calcMaxFontSize(List<Tag> tags) {
            float maxSize = 0.f;

            for (Tag tag : tags) {
                maxSize = Math.max(getFontSize(tag.getElement()), maxSize);
            }

            return maxSize;
        }

        private RGB calcAverageColor() {
            RGB totRGB = new RGB(0, 0, 0);
            int size = getPnames().size() + getPrices().size() + getDescs().size() + getNones().size();
            if (size == 0) return new RGB(0, 0, 0);

            if (hasPnames()) {
                for (Tag tag : getPnames()) {
                    totRGB.add(getFontColor(tag.getElement()));
                }
            }

            if (hasPrices()) {
                for (Tag tag : getPrices()) {
                    totRGB.add(getFontColor(tag.getElement()));
                }
            }

            if (hasDescs()) {
                for (Tag tag : getDescs()) {
                    totRGB.add(getFontColor(tag.getElement()));
                }
            }

            if (hasNones()) {
                for (Tag tag : getNones()) {
                    totRGB.add(getFontColor(tag.getElement()));
                }
            }

            return new RGB(totRGB.getR() / size, totRGB.getG() / size, totRGB.getB() / size);
        }

    }

    protected final WebBrowser browser;
    protected final HtmlDocument document;
    protected final int minClusters;
    protected final int attempts;

    protected TagExtractor(WebBrowser browser, int minClusters, int attempts) {
        this.browser = browser;
        this.minClusters = minClusters;
        this.attempts = attempts;
        this.document = new HtmlDocument();
    }

    public List<Element> getCenterElements() {
        List<Element> elements = new ArrayList<>();
        for (WebElement webElement : browser.findElementsHasImageOrderBySize(attempts)) {
            String selector = browser.getCssSelectorFromElement(webElement);
            Element element = document.findElementByCssSelector(selector);
            if (element != null) elements.add(element);
        }
        return elements;
    }

    public List<WebElement> getCenterWebElements() {
        document.setHtml(browser.getPageSource());

        return browser.findElementsHasImageOrderBySize(attempts);
    }

    public List<WebElement> getCenterWebElements(int size) {
        document.setHtml(browser.getPageSource());

        List<WebElement> elements = browser.findElementsHasImageOrderBySize(attempts);
        if (size > 0 && elements.size() > size) elements = elements.subList(0, size);

        return elements;
    }

    /*
    public List<Element> getTextElements(List<Element> includedElements) {
        final String[] notTags = new String[]{"button", "script", "style", "title"};

        List<Element> elements = document.findElementsHasText(notTags, true);
        List<Element> selectedElements = new ArrayList<>();
        if (includedElements == null) includedElements = Collections.emptyList();

        for (Element element : elements) {
            if (includedElements.contains(element)) continue;
            WebElement webElement;
            try {
                webElement = browser.findElementByCssSelector(element.cssSelector(), attempts);
            } catch (Selector.SelectorParseException e) {
                continue;
            }
            if (webElement == null) continue;
            if (!browser.isInOnlyWindowSize(webElement)) break;

            String text = element.text().toLowerCase(Locale.ENGLISH).replaceAll("\n\r", " ");
            if (text.length() == 0) continue;

            boolean selected = true;
            int depthOfChildren = document.depthOfChildren(element, 2);
            if (depthOfChildren > 1) {
                selected = false;
            } else if (depthOfChildren == 1) {
                for (Element child : document.findChildElements(element)) {
                    String childText = child.text().toLowerCase(Locale.ENGLISH).replaceAll("\n\r", " ");
                    if (text.length() == childText.length()) {
                        selected = false;
                        break;
                    } else if (text.contains(childText)) {
                        includedElements.add(child);
                    }
                }
            }
            if (selected) {
                selectedElements.add(element);
            }

        }

        return selectedElements;
    }
    */

    /*
    public List<WebElement> getTextWebElements(List<Element> includedElements) {
        final String[] notTags = new String[]{"button", "script", "style", "title"};

        document.setHtml(browser.getPageSource());
        System.out.println(browser.getPageSource());

        List<Element> elements = document.findElementsHasText(notTags, true);
        List<WebElement> selectedElements = new ArrayList<>();
        if (includedElements == null) includedElements = Collections.emptyList();

        for (Element element : elements) {
            if (includedElements.contains(element)) continue;

            WebElement webElement;
            try {
                webElement = browser.findElementByCssSelector(element.cssSelector(), attempts);
                if (webElement == null || !webElement.isDisplayed()) continue;

                if (!browser.isInOnlyWindowSize(webElement)) break;

                String text = element.text().toLowerCase(Locale.ENGLISH).replaceAll("\n\r", " ");
                if (text.length() == 0) continue;

                boolean selected = true;
                int depthOfChildren = document.depthOfChildren(element, 2);
                if (depthOfChildren > 1) {
                    selected = false;
                } else if (depthOfChildren == 1) {
                    for (Element child : document.findChildElements(element)) {
                        String childText = child.text().toLowerCase(Locale.ENGLISH).replaceAll("\n\r", " ");
                        if (text.length() == childText.length()) {
                            selected = false;
                            break;
                        } else if (text.contains(childText)) {
                            includedElements.add(child);
                        }
                    }
                }
                if (selected) {
                    WebElement dest = toWebElement(element, attempts);
                    if (dest != null) selectedElements.add(dest);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        return selectedElements;
    }
    */

    protected List<WebElement> getTextWebElements(List<WebElement> includedElements, List<WebElement> parentElements) {
        final String[] notTags = new String[]{"button", "script", "style", "title", "label", "select", "option", "img"};
        final String[] notChildTags = new String[]{"button", "script", "style", "title", "label", "select", "option", "a", "img"};
        Map<String, WebElement> parentMap = new HashMap<>();

        System.out.println(browser.getPageSource());
        List<WebElement> elements = browser.findElementsHasText(notTags, true, attempts);
        List<WebElement> selectedElements = new ArrayList<>();

        for (WebElement element : elements) {
            if (includedElements.contains(element)) continue;

            try {
                String text = element.getText().toLowerCase(Locale.ENGLISH).replaceAll("\\s", " ").trim();
                if (text.length() == 0) continue;

                boolean selected = true;
                if (browser.hasNDepthChildren(element, 1, attempts) && !browser.isChildOfSelfTagName(notChildTags, element, attempts)) {
                    for (WebElement child : browser.findChildElements(element, attempts)) {
                        String childText = child.getText().toLowerCase(Locale.ENGLISH).replaceAll("\\s", " ").trim();
                        if (childText.length() == 0) continue;

                        if (text.length() == childText.length()) {
                            selected = false;
                            break;
                        } else if (text.contains(childText)) {
                            includedElements.add(child);
                        }
                    }
                } else if (!browser.hasNDepthChildren(element, 0, attempts)) {
                    selected = false;
                }

                if (selected) {
                    String path = browser.getXPathFromElement(element);
                    selectedElements.add(element);
                    WebElement parent = browser.findParentElement(element, attempts);
                    if (parent != null && !browser.isChildOfSelfTagName(notChildTags, parent, attempts) && browser.isChildrenSameFont(parent, attempts)) {
                        String parentText = parent.getText().toLowerCase(Locale.ENGLISH).replaceAll("\\s", " ").trim();
                        if (parentText.length() > 0) parentMap.put(parent.getText(), parent);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        for (WebElement element : selectedElements) {
            WebElement parent = parentMap.get(element.getText());
            if (parent != null) {
                parentMap.remove(parent.getText());
            }
        }

        if (parentMap.size() > 0) parentElements.addAll(parentMap.values());

        return selectedElements;
    }

    protected Element getCenterParentElement(Element centerElement, List<Element> textElements, int minClusters) {
        final String[] notTags = new String[]{"script", "iframe"};

        List<Element> parents = document.findElementsHasAnyoneOfChildren(textElements, notTags);
        int destClassNum = -1;
        int beforeSize = minClusters;
        int parentsSize = parents.size();
        while (parentsSize > 0) {
            if (parentsSize >= minClusters || (parentsSize > 1 && parentsSize == beforeSize)) {
                int classNum = 0;
                for (Element element : parents) {
                    if (document.isChildElement(element, centerElement)) {
                        destClassNum = classNum;
                    }

                    classNum++;
                }
            }

            if (destClassNum >= 0) break;

            beforeSize = parentsSize;
            parents = document.findElementsHasAnyoneOfChildren(parents, textElements, notTags);
            parentsSize = parents.size();
        }

        return (destClassNum >= 0) ? parents.get(destClassNum) : null;
    }

    protected WebElement getCenterParentElement(WebElement centerElement, List<WebElement> textElements, int minClusters) {
        WebElement body = toWebElement(document.getBody(), attempts);
        if (body == null) body = browser.findElementByXPath("//html/body", attempts);
        List<WebElement> parents = browser.findElementsHasAnyoneOfChildren(body, textElements, attempts);
        int destClassNum = -1;
        int beforeSize = minClusters;
        int parentsSize = parents.size();
        while (parentsSize > 0) {
            if (parentsSize >= minClusters || (parentsSize > 1 && parentsSize == beforeSize)) {
                int classNum = 0;
                for (WebElement element : parents) {
                    if (browser.isChildElement(element, centerElement, attempts)) {
                        destClassNum = classNum;
                    }

                    classNum++;
                }
            }

            if (destClassNum >= 0) break;

            beforeSize = parentsSize;
            parents = browser.findElementsHasAnyoneOfChildren(parents, textElements, attempts);
            parentsSize = parents.size();
        }

        return (destClassNum >= 0) ? parents.get(destClassNum) : null;
    }

    protected List<WebElement> prune(List<WebElement> textElements, WebElement centerParentElement) {
        List<WebElement> result = new ArrayList<>(textElements.size());
        for (WebElement element : textElements) {
            if (browser.isChildElement(centerParentElement, element, attempts)) {
                if (element.isDisplayed()) {
                    result.add(element);
                }
            }
        }
        return result;
    }

    protected List<WebElement> prune(List<Element> textElements, Element centerParentElement) {
        List<WebElement> result = new ArrayList<>(textElements.size());
        for (Element element : textElements) {
            if (document.isChildElement(centerParentElement, element)) {
                WebElement webElement = toWebElement(element, attempts);
                if (webElement.isDisplayed()) {
                    result.add(webElement);
                }
            }
        }
        return result;
    }

    public BufferedImage capture() throws IOException {
        byte[] imageBytes = browser.captureImage();
        InputStream in = new ByteArrayInputStream(imageBytes);
        BufferedImage image = ImageIO.read(in);
        int windowWidth = browser.getWindowWidth();
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();

        return ImageUtils.resize(image, windowWidth, imageHeight * windowWidth / imageWidth);
    }


    public abstract Tags tag(List<WebElement> centers, List<WebElement> elements, List<WebElement> includedElements, List<WebElement> parentElements);

    protected WebElement toWebElement(Element element, int attempts) {
        return browser.findElementByCssSelector(element.cssSelector(), attempts);
    }

    public Tags extract(String url, int maxCenters) {
        browser.visit(url);
//            document.visit(url);
//            WebDriverWait wait = new WebDriverWait(browser.getDriver(), 10);
//            wait.until(ExpectedConditions.alertIsPresent());
        browser.waitForPageLoad();

        List<WebElement> includedElements = new ArrayList<>();
        List<WebElement> parentElements = new ArrayList<>();
        List<WebElement> textElements = getTextWebElements(includedElements, parentElements);
//            List<Element> textElements = extractor.getTextElements(includedElements);
        System.out.println("end getting text elements : " + textElements.size());
        if (textElements.isEmpty()) return null;

        List<WebElement> centers = getCenterWebElements(maxCenters);
        System.out.println("end getting center elements : " + centers.size());

        return tag(centers, textElements, includedElements, parentElements);
    }

    public BufferedImage getTaggedImage(Tags tags) throws IOException {
        BufferedImage resizedImage = capture();

        WebElement pname = tags.hasPnames() ? tags.getPnames().get(0).getElement() : null;
        WebElement desc = tags.hasDescs() ? tags.getDescs().get(0).getElement() : null;
        WebElement price = tags.hasPrices() ? tags.getPrices().get(0).getElement() : null;
        List<Tag> nones = tags.getNones();

        if (pname != null) {
            try {
                ImageUtils.drawRectangle(
                    resizedImage
                    , new RectPoint(pname.getLocation().getX(), pname.getLocation().getY(), pname.getSize().getWidth(), pname.getSize().getHeight())
                    , 5
                    , Color.RED);
                System.out.println(pname.getText() + "(" + pname.getTagName() + ")");
            } catch (Exception ignored) {

            }
        }

        if (desc != null) {
            try {
                ImageUtils.drawRectangle(
                    resizedImage
                    , new RectPoint(desc.getLocation().getX(), desc.getLocation().getY(), desc.getSize().getWidth(), desc.getSize().getHeight())
                    , 5
                    , Color.BLUE);
                System.out.println(desc.getText() + "(" + desc.getTagName() + ")");
            } catch (Exception ignored) {

            }
        }

        if (price != null) {
            try {
                ImageUtils.drawRectangle(
                    resizedImage
                    , new RectPoint(price.getLocation().getX(), price.getLocation().getY(), price.getSize().getWidth(), price.getSize().getHeight())
                    , 5
                    , Color.GREEN);
                System.out.println(price.getText() + "(" + price.getTagName() + ")");
            } catch (Exception ignored) {

            }
        }

        if (nones != null) {
            try {
                for (Tag tag : nones) {
                    WebElement element = tag.getElement();
                    ImageUtils.drawRectangle(
                        resizedImage
                        , new RectPoint(element.getLocation().getX(), element.getLocation().getY(), element.getSize().getWidth(), element.getSize().getHeight())
                        , 3
                        , Color.BLACK);
                    System.out.println(element.getText() + "(" + element.getTagName() + ")");
                }
            } catch (Exception ignored) {

            }
        }

        return resizedImage;
    }

}
