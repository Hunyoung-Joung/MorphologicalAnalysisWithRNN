package com.young.homework;

import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL;
import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.nlp.komoran.model.KomoranResult;
import kr.co.shineware.nlp.komoran.model.Token;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.young.homework.util.DataUtilities;

import org.apache.commons.io.FilenameUtils;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.deeplearning4j.nn.conf.GradientNormalization;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.LSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.api.InvocationType;
import org.deeplearning4j.optimize.listeners.EvaluativeListener;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.File;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * 
 * @author jounghunyoung@gmail.com
 *
 */
public class MorphologicalAnalysisWithRNN {
	// Logger
	private static final Logger logger = LoggerFactory.getLogger(MorphologicalAnalysisWithRNN.class.getSimpleName());
	
    /** Data URL for downloading */
    public static final String DATA_URL = "http://ai.stanford.edu/~amaas/data/sentiment/aclImdb_v1.tar.gz";
    /** Location to save and extract the training/testing data */
    public static final String DATA_PATH = FilenameUtils.concat(System.getProperty("java.io.tmpdir"), "dl4j_w2vSentiment/");
	
	public static void main(String args[]) {
        // Analyzing
		MorphologicalAnalysisWithRNN morphologicalAnalysisWithRNN = new MorphologicalAnalysisWithRNN();
        try {
        	morphologicalAnalysisWithRNN.analyzing();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * 
	 * @throws IOException
	 */
	public void analyzing() throws IOException {
		
        int batchSize = 64;     //Number of examples in each minibatch
        int vectorSize = 300;   //Size of the word vectors. 300 in the Google News model
        int nEpochs = 1;        //Number of epochs (full passes of training data) to train on
        int truncateReviewsToLength = 256;  //Truncate reviews with length (# words) greater than this
        final int seed = 0;     //Seed for reproducibility

        Nd4j.getMemoryManager().setAutoGcWindow(10000);  //https://deeplearning4j.org/workspaces

        //Set up network configuration
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
            .seed(seed)
            .updater(new Adam(5e-3))
            .l2(1e-5)
            .weightInit(WeightInit.XAVIER)
            .gradientNormalization(GradientNormalization.ClipElementWiseAbsoluteValue).gradientNormalizationThreshold(1.0)
            .list()
            .layer(new LSTM.Builder().nIn(vectorSize).nOut(256)
                .activation(Activation.TANH).build())
            .layer(new RnnOutputLayer.Builder().activation(Activation.SOFTMAX)
                .lossFunction(LossFunctions.LossFunction.MCXENT).nIn(256).nOut(2).build())
            .build();

        MultiLayerNetwork net = new MultiLayerNetwork(conf);
        net.init();

        //DataSetIterators for training and testing respectively
        WordVectors wordVectors = WordVectorSerializer.loadStaticModel(new File(HtmlCrawlerCtrl.CRAWL_RESULT));
        com.young.homework.util.SentimentExampleIterator train = new com.young.homework.util.SentimentExampleIterator(DATA_PATH, wordVectors, batchSize, truncateReviewsToLength, true);
        com.young.homework.util.SentimentExampleIterator test = new com.young.homework.util.SentimentExampleIterator(DATA_PATH, wordVectors, batchSize, truncateReviewsToLength, false);

        System.out.println("Starting training");
        net.setListeners(new ScoreIterationListener(1), new EvaluativeListener(test, 1, InvocationType.EPOCH_END));
        net.fit(train, nEpochs);

        //After training: load a single example and generate predictions
        File shortNegativeReviewFile = new File(FilenameUtils.concat(DATA_PATH, "aclImdb/test/neg/12100_1.txt"));
        String shortNegativeReview = FileUtils.readFileToString(shortNegativeReviewFile, (Charset)null);

        INDArray features = test.loadFeaturesFromString(shortNegativeReview, truncateReviewsToLength);
        INDArray networkOutput = net.output(features);
        long timeSeriesLength = networkOutput.size(2);
        INDArray probabilitiesAtLastWord = networkOutput.get(NDArrayIndex.point(0), NDArrayIndex.all(), NDArrayIndex.point(timeSeriesLength - 1));

        System.out.println("\n\n-------------------------------");
        System.out.println("Short negative review: \n" + shortNegativeReview);
        System.out.println("\n\nProbabilities at last time step:");
        System.out.println("p(positive): " + probabilitiesAtLastWord.getDouble(0));
        System.out.println("p(negative): " + probabilitiesAtLastWord.getDouble(1));

        System.out.println("----- Example complete -----");
		
		
//    	BufferedReader reader;
//    	BufferedWriter fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(HtmlCrawlerCtrl.CRAWL_RESULT), "utf-8"));
//		try {
//			reader = new BufferedReader(new FileReader(HtmlCrawlerCtrl.CRAWL_RESULT));
//			String tokenStr = "";
//			int cnt =0;
//			// {uri}\t||{category}\t||{publisher}\t||{subject}\t||{content}
//			// Get subject
//			while ((tokenStr = reader.readLine().split("\\t||")[3]) != null) {
//		        Komoran komoran = new Komoran(DEFAULT_MODEL.FULL);
//		        KomoranResult analyzeResultList = komoran.analyze(tokenStr);
//		        List<Token> tokenList = analyzeResultList.getTokenList();
//		        Token previousToken = null;
//		        for (int i=0; i<tokenList.size(); i++) {
//		        	Token token = tokenList.get(i);
//		        	// Split on white spaces in the line to get words
//		        	TokenizerFactory tokenizerFactory = new DefaultTokenizerFactory();
//		        	tokenizerFactory.setTokenPreProcessor(new CommonPreprocessor());
//		        }
//		        cnt++;
////        		logger.info(cnt +" ## Keyword? "+previousToken.getMorph()+", "+HtmlCrawlerCtrl.keyWordMap.get(previousToken.getMorph()));
//			}
//			reader.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		} finally {
//	        fileWriter.flush();
//	        fileWriter.close();
//		}
//		this.writeToFile();
    }
	
    /**
     * Make crawl result file
     * 
     * @throws IOException
     */
    private void writeToFile() throws IOException {
    	BufferedWriter fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(HtmlCrawlerCtrl.KEYWORD_RESULT), "utf-8"));
    	
    	for (Iterator<String> iter = HtmlCrawlerCtrl.keyWordMap.keySet().iterator(); iter.hasNext();) {
    		String key = iter.next().toString();
    		int val = HtmlCrawlerCtrl.keyWordMap.get(key);
        	fileWriter.write(String.join("	,", key));
        	fileWriter.write("	,");
        	fileWriter.write(String.valueOf(val));
        	fileWriter.write("\n");
    	}
        fileWriter.flush();
        fileWriter.close();
    }
}