package it.unitn.ainlp.chunker.app;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;
import it.unitn.ainlp.writer.ConllWriter;

import org.apache.commons.cli.*;

import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk.Chunk;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.languagetool.LanguageToolLemmatizer;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpChunker;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpNameFinder;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter;

/**
 * A simple demo showing how to perform simple information extraction (IE)
 * tasks, such as Named Entity Recognition (NER), by using the UIMA SDK and 
 * DKPro.
 *
 */
public class ChunkerDemo 
{
    public static void main( String[] args ) throws Exception {
    	
    	Options opt = new Options();
    	
    	// add help option
    	opt.addOption("h", false, "Print help for this application");
    	
    	BasicParser parser = new BasicParser();
    	CommandLine cl;
    	
    	NamedEntity ne;
    	
    	Chunk chunk;
    	
    	try {
    		cl = parser.parse(opt, args);
    	} catch (Exception e) {
    		System.err.println("Parameters format error");
    		return;
    	}
    	
    	if (args.length != 2 || cl.hasOption('h')) {
    		// print the help
    		HelpFormatter f = new HelpFormatter();
    		f.printHelp("NERDemo [option] text_file destDir", opt);
    		return;    		
    	}
    	
    	// get input text file 
    	String inputFile = args[0];
    	
    	// get output directory
    	String outputDir = args[1];  
    	    	
    	runPipeline(
    			/*
    			 * Read text from file passed in input 
    			 */
    			createReaderDescription(TextReader.class,
    					TextReader.PARAM_SOURCE_LOCATION, inputFile, 
    					TextReader.PARAM_LANGUAGE, "en"),
    					
    			/* 
    			 * Perform tokenization and sentence boundary detection 
    			 * using OpenNLP. 
    			 */
    			createEngineDescription(OpenNlpSegmenter.class),
    			
    			/*
    			 * Perform lemmatization using !LanguageTool. 
    			 */
    	        createEngineDescription(LanguageToolLemmatizer.class),
    	        
    	        /*
    	         * Perform part-of-speech tagging using OpenNLP.
    	         */
    			createEngineDescription(OpenNlpPosTagger.class),
    	        
                /*
                 * Perform named entity recognition using OpenNLP.
                 */
                createEngineDescription(OpenNlpNameFinder.class,
                        OpenNlpNameFinder.PARAM_VARIANT, "person"),
                createEngineDescription(OpenNlpNameFinder.class,
                        OpenNlpNameFinder.PARAM_VARIANT, "organization"),
                createEngineDescription(OpenNlpNameFinder.class, 
                		OpenNlpNameFinder.PARAM_VARIANT, "location"),
                		
                /**
                 * Add here the code to include the OpenNlpChunker annotator.
                 * 
                 * createEngineDescription(...),
                 */
    	        
                /*
                 * Write the result to disk in CoNLL format. The results are
                 * written to the directory stored in the outputDir param
                */
                createEngineDescription(ChunkerConllWriter.class,
                		ChunkerConllWriter.PARAM_TARGET_LOCATION, outputDir));
    }
}
