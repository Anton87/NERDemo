package it.unitn.ainlp;


import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;
import it.unitn.ainlp.writer.MyWriter;

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
    		HelpFormatter f = new HelpFormatter();
    		f.printHelp("NERDemo [option] text_file destDir", opt);
    		return;    		
    	}
    	
    	String textfile = args[0];
    	String destDir = args[1];  
    	    	
    	runPipeline(
    			createReaderDescription(TextReader.class,
    					TextReader.PARAM_SOURCE_LOCATION, textfile, 
    					TextReader.PARAM_LANGUAGE, "en"),
    			createEngineDescription(OpenNlpSegmenter.class),
    			createEngineDescription(OpenNlpPosTagger.class),
    	        createEngineDescription(LanguageToolLemmatizer.class),
    	        
                // NamedEntity
                createEngineDescription(OpenNlpNameFinder.class,
                        OpenNlpNameFinder.PARAM_VARIANT, "person"),
                createEngineDescription(OpenNlpNameFinder.class,
                        OpenNlpNameFinder.PARAM_VARIANT, "organization"),
                createEngineDescription(OpenNlpNameFinder.class, 
                		OpenNlpNameFinder.PARAM_VARIANT, "location"),
    			//createEngineDescription(OpenNlpNameFinder.class),
    	        //createEngineDescription(StanfordNamedEntityRecognizer.class,
				//		StanfordNamedEntityRecognizer.PARAM_LANGUAGE, "en",
				//		StanfordNamedEntityRecognizer.PARAM_VARIANT, "muc.7class.distsim.crf"));
    	        
    	        // writer
                createEngineDescription(MyWriter.class,
                		MyWriter.PARAM_TARGET_LOCATION, destDir));
                        
                //createEngineDescription(Conll2002Writer.class, 
                //		Conll2002Writer.PARAM_TARGET_LOCATION, destDir));
            	        
    			//createEngineDescription(Conll2006Writer.class, 
    			//		Conll2006Writer.PARAM_TARGET_LOCATION, destDir));
    	        //createEngineDescription)
    }
}
