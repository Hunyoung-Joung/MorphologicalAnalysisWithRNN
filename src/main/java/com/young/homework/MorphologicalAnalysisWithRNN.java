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
//import org.bytedeco.javacpp.Loader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.young.homework.util.DataUtilities;

import org.apache.commons.io.FilenameUtils;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.deeplearning4j.models.word2vec.Word2Vec;
//import org.deeplearning4j.nn.conf.GradientNormalization;
//import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
//import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
//import org.deeplearning4j.nn.conf.layers.LSTM;
//import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
//import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
//import org.deeplearning4j.nn.weights.WeightInit;
//import org.deeplearning4j.optimize.api.InvocationType;
//import org.deeplearning4j.optimize.listeners.EvaluativeListener;
//import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.text.sentenceiterator.LineSentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentencePreProcessor;
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
	
	public static void main(String args[]) {
		
		SentenceIterator iter = new LineSentenceIterator(new File("C:\\Users\\root\\workspace\\text-mining-rnn-learning\\storage\\crawl_result.tsv"));
		iter.setPreProcessor(new SentencePreProcessor() {
		    @Override
		    public String preProcess(String sentence) {
		    	return sentence.toLowerCase();
		    }
		});
		
		// Split on white spaces in the line to get words
		TokenizerFactory t = new DefaultTokenizerFactory();
		t.setTokenPreProcessor(new CommonPreprocessor());
		
		int batchSize = 1000;
		int iterations = 3;
		int layerSize = 150;

		logger.info("Build model....");
		Word2Vec vec = new Word2Vec.Builder()
			.batchSize(batchSize) //# words per minibatch.
			.minWordFrequency(5) //
			.useAdaGrad(false) //
			.layerSize(layerSize) // word feature vector size
			.iterations(iterations) // # iterations to train
			.learningRate(0.025) //
			.minLearningRate(1e-3) // learning rate decays wrt # words. floor learning
			.negativeSample(10) // sample size 10 words
			.iterate(iter) //
			.tokenizerFactory(t)
			.build();
		vec.fit();

        // Analyzing
//		MorphologicalAnalysisWithRNN morphologicalAnalysisWithRNN = new MorphologicalAnalysisWithRNN();
//        try {
//        	morphologicalAnalysisWithRNN.analyzing();
//		} catch (IOException e) {
//			e.printStackTrace();
//		} 
	}

	/**
	 * 
	 * 
	 * @throws IOException
	 */
	public void analyzing() throws IOException {
				
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