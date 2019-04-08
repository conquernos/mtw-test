package com.hc.mtw.common.webdriver;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;


public class HtmlDocument {

    private Document document;

    public HtmlDocument() {

    }

    public boolean visit(String url) {
        try {
            document = Jsoup.connect(url).get();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public void setHtml(String html) {
        document = Jsoup.parse(html);
    }

    public Element getBody() {
        return document.body();
    }

    public Element findParentElement(Element element) {
        return element.parent();
    }

    public Elements findChildElements(Element element) {
        return element.select(" > *:not([style*=\"display: none\"]):not([style*=\"display:none\"])");
    }

    public boolean isChildElement(Element parent, Element child) {
        for (Element element : child.parents()) {
            if (element.equals(parent)) {
                return true;
            }
        }
        return false;
    }

    public boolean isParentOfSelfTagName(String[] tagNames, Element child) {
        for (Element element : child.parents()) {
            for (String tagName : tagNames) {
                if (element.tagName().equalsIgnoreCase(tagName)) return true;
            }
        }
        return false;
    }

    public Elements findElementsHasText(String[] notTags, boolean onlySelf) {
        return findElementsHasText(getBody(), notTags, onlySelf);
    }

    public Elements findElementsHasText(Element element, String[] notTags, boolean onlySelf) {
        Elements results = new Elements();
        Set<Element> contains = new HashSet<>();
        StringBuilder selectorBuilder = new StringBuilder();
        selectorBuilder.append("*:not([style*=\"display: none\"]):not([style*=\"display:none\"])");
        for (String notTag : notTags) {
            selectorBuilder.append(":not(");
            selectorBuilder.append(notTag);
            selectorBuilder.append(")");
        }
        String selector = selectorBuilder.toString();

        for (Element child : element.getAllElements()) {
            if (child.hasText() && child.is(selector)) {
                if (onlySelf) {
                    String text = child.ownText();
                    if (!text.isEmpty() && !(text.startsWith("<") && text.endsWith(">"))) {
                        Element parent = child.parent();
                        if (parent != null && parent.is(selector) && !contains.contains(parent)) {
                            results.add(parent);
                            contains.add(parent);
                        }
                        results.add(child);
                        contains.add(child);
                    }
                } else {
                    results.add(child);
                    contains.add(child);
                }
            }
        }

        return results;
    }

    public Elements findElementsByCssSelector(String selector) {
        return document.select(selector);
    }

    public Element findElementByCssSelector(String selector) {
        return document.selectFirst(selector);
    }

    public Element findElementByCssSelector(Element root, String selector) {
        return root.selectFirst(selector);
    }

    public Elements findElementsHasImage() {
        return findElementsHasImage(getBody());
    }

    public Elements findElementsHasImage(Element element) {
        return element.getElementsByTag("img");
    }

    public Elements findElementsHasAnyoneOfChildren(List<Element> children, String[] notTags) {
        return findElementsHasAnyoneOfChildren(getBody(), children, notTags);
    }

    public Elements findElementsHasAnyoneOfChildren(Element element, List<Element> children, String[] notTags) {
        StringBuilder selectorBuilder = new StringBuilder();
        selectorBuilder.append("> *:not([style*=\"display: none\"]):not([style*=\"display:none\"])");
        for (String notTag : notTags) {
            selectorBuilder.append(":not(");
            selectorBuilder.append(notTag);
            selectorBuilder.append(")");
        }
        String selector = selectorBuilder.toString();
        Elements result = new Elements();
        for (Element parent : element.select(selector)) {
            for (Element child : children) {
                if (isChildElement(parent, child)) {
                    result.add(parent);
                    break;
                }
            }
        }

        return result;
    }

    public Elements findElementsHasAnyoneOfChildren(List<Element> elements, List<Element> children, String[] notTags) {
        Elements result = new Elements();
        for (Element element : elements) {
            result.addAll(findElementsHasAnyoneOfChildren(element, children, notTags));
        }

        return result;
    }

    public int depthOfChildren(Element element) {
        Elements children = findChildElements(element);
        int depth = 0;
        for (Element child : children) {
            depth = Math.max(depthOfChildren(child), depth);
        }

        return depth + (children.size() == 0 ? 0 : 1);
    }

    public int depthOfChildren(Element element, int max) {
        Elements children = findChildElements(element);
        int depth = 0;
        for (Element child : children) {
            depth = Math.max(depthOfChildren(child), depth);
            if (depth >= max) break;
        }

        return depth + (children.size() == 0 ? 0 : 1);
    }

    public static void main(String[] args) {
        HtmlDocument document = new HtmlDocument();

        document.setHtml(
            "<html>" +
                "   <body>" +
                "       <div>" +
                "           <div style=\'display:none\'>aaa</div>" +
                "           <a>abb</a>" +
                "           <button>ccc<p>ddd</p></button>" +
                "       </div>" +
                "   </body>" +
                "</html>");
        for (Element element : document.findElementsHasText(document.getBody(), new String[]{"button", "a"}, true)) {
            System.out.println(element.tagName() + ":" + element.text());
        }

        Element element = document.findElementByCssSelector("#zoomLinkProductImageViewerWidget_3_-2021_3074457345618263435_PRODUCT_ATTR_COLOUR_WHITE > figure > img");
        System.out.println(element);
    }

}
