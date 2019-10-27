package com.young.homework;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import edu.uci.ics.crawler4j.crawler.Page; 
import edu.uci.ics.crawler4j.crawler.WebCrawler; 
import edu.uci.ics.crawler4j.url.WebURL; 
 
/**
 * 
 * @author jounghunyoung@gmail.com
 *
 */
public class HtmlCrawler extends WebCrawler {
	// Logger
	private static final Logger logger = LoggerFactory.getLogger(HtmlCrawler.class.getSimpleName());
	// Ignored pattern
    private static final Pattern IMAGE_EXTENSIONS = Pattern.compile(
    		".*(\\.(css|js|bmp|gif|jpeg"
    				+ "|png|tiff|mid|mp2|mp3|mp4"
    				+ "|wav|avi|mov|mpeg|ram|m4v|pdf"
    				+ "|rm|smil|wmv|swf|wma|zip|rar|gz))$"
    			);
    // Auto increase follow to lower url
    private AtomicInteger numSeenImages;

    /**
     * Creates a new crawler instance.
     *
     * @param numSeenImages
     */
    public HtmlCrawler (AtomicInteger numSeenImages) {
    	logger.info("## Construct? BEGIN "+numSeenImages);
        this.numSeenImages = numSeenImages;
    }


    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String href = url.getURL();
        // Ignore the patterns and non article
        if ((IMAGE_EXTENSIONS.matcher(href).matches())) {
            return false;
        } else if ((href.contains("news.v.daum.net")) || (href.contains("v.media.daum.net"))) {
        	logger.info("## shouldVisit? "+href+", "+numSeenImages);
            numSeenImages.incrementAndGet();
            // Parse
            try {
            	Map<String, String> article = this.getArticles(href);
    			if (!article.isEmpty()) {
    				String subject = article.keySet().iterator().next();
    		    	String contents = article.get(subject);
    		    	HtmlCrawlerCtrl.articleList.addAll(Arrays.asList(Arrays.asList(url+"\t||"+subject+"\t||"+contents)));
    		    	this.writeToFile();
    				logger.info("## Article size? "+ HtmlCrawlerCtrl.articleList.size());
    			}
    		} catch (IOException e) {
    			logger.error("## ", e);
    		}
            return true;
        } else {
        	logger.error("## ", "Something is wrong");
        	return false;
        }
    }

    @Override
    public void visit(Page page) {}
    
    /**
     * Build-up low data
     * 
     * @param url
     * @return
     * @throws IOException
     */
    private Map<String, String> getArticles(String url) throws IOException {
    	Map<String, String> tempMap = new HashMap<String, String>();
        Document document = Jsoup.connect(url).get();
        
        Elements cat=document.select("div.inner_gnb>ul.gnb_comm");
        Elements media=document.select("div.head_view>em>a>img.thumb_g");
        Elements title=document.select("h3.tit_view");
		Elements body=document.select("div#harmonyContainer");
		
		String category=cat.attr("data-category").toString();
		String publisher=media.attr("alt").toString();
		String content=body.text();
		String subject=title.text();

        if (!title.isEmpty()) {
            if (!body.isEmpty()) {
            	tempMap.put(category+"\t||"+publisher+"\t||"+subject, content);
            }
        }
        return tempMap;
    }
    
    /**
     * Make crawl result file
     * 
     * @throws IOException
     */
    private void writeToFile() throws IOException {
    	BufferedWriter fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(HtmlCrawlerCtrl.CRAWL_RESULT), Charset.forName("UTF-8")));

        HtmlCrawlerCtrl.articleList.forEach(innerList -> {
            try {
            	fileWriter.write(String.join("\t||", innerList));
            	fileWriter.write("\n");
            } catch (IOException e) {
            	logger.error("## ", e);
            }
        });
    }
    
    public static void main (String ...strings ) {
    	AtomicInteger numSeenImages = new AtomicInteger();
    	HtmlCrawler c = new HtmlCrawler(numSeenImages);
    	try {
			c.getArticles("http://v.media.daum.net/v/20191025191900343");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}