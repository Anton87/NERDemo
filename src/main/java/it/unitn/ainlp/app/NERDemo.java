package it.unitn.ainlp.app;


import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;
import it.unitn.ainlp.writer.ConllWriter;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

//import de.tudarmstadt.ukp.dkpro.core.io.conll.Conll2
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.languagetool.LanguageToolLemmatizer;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpNameFinder;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter;
//import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordNamedEntityRecognizer;

/**
 * Hello world!
 *
 */
public class NERDemo 
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
    		f.printHelp("NERDemo [option] text_file destDir", opt);
    		return;    		
    	}
    	
    	// get input text file 
    	String textfile = args[0];
    	
    	// get output directory
    	String destDir = args[1];  
    	    	
    	runPipeline(
    			/*
    			 * Read an English text file. The name of the textfile to 
    			 * read is stored in the textfile variable. 
    			 */
    			createReaderDescription(TextReader.class,
    					TextReader.PARAM_SOURCE_LOCATION, textfile, 
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
    	         * Write the result to disk in CoNLL format. The results are
    	         * written to a file called document.txt.conll, which is
    	         *  located in the destDir directory.
    	         */
                createEngineDescription(ConllWriter.class,
                		ConllWriter.PARAM_TARGET_LOCATION, destDir));
    }
}
