package com.young.homework;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

/**
 * 
 * @author jounghunyoung@gmail.com
 *
 */
public class HtmlCrawlerCtrl {
	// Article list
	public static List<List<String>> articleList = new ArrayList<List<String>>();
	// Keyword map
	public static Map<String, Integer> keyWordMap = new HashMap<String, Integer>();
	public static final String STORAGE_DIR = "./storage/";
	// Web crawl result
	public static final String CRAWL_RESULT = STORAGE_DIR+"crawl_result.tsv";
	// Keyword analyzing result
	public static final String KEYWORD_RESULT = STORAGE_DIR+"keyword_result.csv";

    public static void main(String ... args) throws Exception {

        CrawlConfig config = new CrawlConfig();
        config.setCrawlStorageFolder(STORAGE_DIR);
        config.setPolitenessDelay(30000);
        config.setMaxDepthOfCrawling(5);
        config.setIncludeBinaryContentInCrawling(false);
        config.setResumableCrawling(false);
        config.setIncludeHttpsPages(true);

        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        robotstxtConfig.setEnabled(false);
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

        // Target URL
        controller.addSeed("https://media.daum.net/foreign/");
        controller.addSeed("https://media.daum.net/economic/");
        controller.addSeed("https://media.daum.net/society/");
        controller.addSeed("https://media.daum.net/politics/");
        controller.addSeed("https://media.daum.net/culture/");
        controller.addSeed("https://media.daum.net/digital/");

        // Worker thread
        int numberOfCrawlers = 100;
        // Auto increase follow to lower url
        AtomicInteger numSeenImages = new AtomicInteger();
        // The factory which creates instances of crawlers.
        CrawlController.WebCrawlerFactory<HtmlCrawler> factory = () -> new HtmlCrawler(numSeenImages);
        // Thread begin
        controller.start(factory, numberOfCrawlers);
        
        // Analyzing
//        MorphologicalAnalysis morphologicalAnalysis = new MorphologicalAnalysis();
//        try {
//			morphologicalAnalysis.analyzing();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
    }

}