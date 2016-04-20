package it.unitn.ainlp.app;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.*;
import org.apache.uima.fit.pipeline.JCasIterable;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.PennTree;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;
import de.tudarmstadt.ukp.dkpro.core.berkeleyparser.BerkeleyParser;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiWriter;
import de.tudarmstadt.ukp.dkpro.core.languagetool.LanguageToolLemmatizer;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpNameFinder;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter;

/**
 * A simple application that reads text from the specified document and 
 * print tokens, POSs, lemmas and named entities, followed by the
 * constituent trees as a bracketed structure.
 * 
 * The application performs the following tasks:
 * <ol>
 * <li> text segmentation
 * <li> tokenization 
 * <li> part-of-speech tagging
 * <li> named entity recognition
 * <li> constituency parsing
 * </ol> 
 * 
 * It also shows how to access the analysis results produced by DKPro.
 */
public class NLPDemoXmiCas 
{
    public static void main( String[] args ) throws Exception {
    	
    	Options opt = new Options();
    	
    	// add help option
    	opt.addOption("h", false, "Print help for this application");
    	
    	BasicParser parser = new BasicParser();
    	CommandLine cl;
    	
    	try {
    		cl = parser.parse(opt, args);
    	} catch (Exception e) {
    		System.err.println("Parameters format error");
    		return;
    	}
    	
    	if (args.length != 2 || cl.hasOption('h')) {
    		// print the help
    		HelpFormatter f = new HelpFormatter();
    		f.printHelp("NERDemoXmiCas [option] text_file destDir", opt);
    		return;    		
    	}
    	
    	// get input text file 
    	String inputFile = args[0];
    	
    	// get output directory
    	String outputDir = args[1];  
    	    	
    	// Assemble pipeline
    	JCasIterable pipeline = new JCasIterable(
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
                	
                /*
                 * Perform constituency parsing using Berkeley Parser.
                 */
                createEngineDescription(BerkeleyParser.class,
                		BerkeleyParser.PARAM_WRITE_PENN_TREE, true),
    	        
                /*
                 * Write output in XMI format
                 */
                createEngineDescription(XmiWriter.class,
                		XmiWriter.PARAM_TARGET_LOCATION, outputDir,
                		XmiWriter.PARAM_TYPE_SYSTEM_FILE, "TypeSystem.xml"));
    	
    	// Run and show results in console
        for (JCas jcas : pipeline) {
            for (Sentence sentence : select(jcas, Sentence.class)) {
                System.out.printf("%n== Sentence ==%n");
                System.out.printf("  %-16s %-10s %-10s %-10s %n", "TOKEN", "LEMMA", 
                		"CPOS", "POS");
                for (Token token : selectCovered(Token.class, sentence)) {
                    System.out.printf("  %-16s %-10s %-10s %-10s %n",
                            token.getCoveredText(),
                            token.getLemma() != null ? token.getLemma().getValue() : "",
                            token.getPos().getClass().getSimpleName(),
                            token.getPos().getPosValue());
                }

                System.out.printf("%n  -- Named Entities --%n");
                System.out.printf("  %-16s %-10s%n", "ENTITY", "TOKENS");
                for (NamedEntity ne : selectCovered(NamedEntity.class, sentence)) {
                    System.out.printf("  %-16s %-10s%n", ne.getValue(), ne.getCoveredText());
                }                
                
                System.out.printf("%n -- PennTree --%n");
                List<PennTree> trees = new ArrayList<PennTree>(selectCovered(PennTree.class, sentence));
                System.out.printf("  %s%n", trees.get(0).getPennTree());
            }
        }
    }
}
