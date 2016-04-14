/*******************************************************************************
 * Copyright 2016
 * iKernels group
 * University of Trento
 * 
 * Based on the Conll2002Writer.java code
 * @see of https://github.com/dkpro/dkpro-core/blob/master/dkpro-core-io-conll-asl/src/main/java/de/tudarmstadt/ukp/dkpro/core/io/conll/Conll2002Writer.java
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package it.unitn.ainlp.chunker.app;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.io.IobEncoder;
import de.tudarmstadt.ukp.dkpro.core.api.io.JCasFileWriter_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
//import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk.Chunk;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;


/**
 * <p>Writes results in the format below. The columns are separated by a 
 * single space, unlike illustrated below.</p>
 * 
 * <pre><code>
 * 1	Pierre       Pierre       NNP B-person B-NP
 * 2	Vinken       Vinken       NNP I-person I-NP
 * 3	,            ,            ,   O        O
 * 4	61           61           CD  O        B-NP
 * 5	years        year         NNS O        I-NP
 * 6	old          old          JJ  O        B-ADJP
 * 7	,            ,            ,   O        O
 * 8	will         will         MD  O        B-VP
 * 9	join         join         VB  O        I-VP
 * 10	the          the          DT  O        B-NP
 * 11	board        board        NN  O        I-NP
 * 12	as           as           IN  O        B-PP
 * 13	a            a            DT  O        B-NP
 * 14	nonexecutive nonexecutive JJ  O        I-NP
 * 15	director     director     NN  O        I-NP
 * 16	Nov.         Nov.         NNP O        B-NP
 * 17	29           29           CD  O        I-NP
 * 18	.            .            .   O        O

 * </code></pre>
 * 
 * <p>Sentences are separated by a blank new line.</p>
 * 
 * @see <a href="http://www.clips.ua.ac.be/conll2002/ner/">CoNLL 2002 shared task</a>
 */
@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
        "de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity"})
public class ChunkerConllWriter
    extends JCasFileWriter_ImplBase
{

    /**
     * Name of configuration parameter that contains the character encoding used by the input files.
     */
    public static final String PARAM_ENCODING = "sourceEncoding";
    @ConfigurationParameter(name = PARAM_ENCODING, mandatory = true, defaultValue = "UTF-8")
    private String encoding;

    public static final String PARAM_FILENAME_SUFFIX = "filenameSuffix";
    @ConfigurationParameter(name = PARAM_FILENAME_SUFFIX, mandatory = true, defaultValue = ".conll")
    private String filenameSuffix;

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
    	// Open a text-output stream to print the results of 
    	// the processing to a file.
        PrintWriter out = null;
        try {
            out = new PrintWriter(
            		new OutputStreamWriter(
            				getOutputStream(aJCas, filenameSuffix), encoding));
            
            // Convert annotations in conll format and
            // print them to a file.
            convert(aJCas, out);
        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }
        finally {
            closeQuietly(out);
        }
    }

    private void convert(JCas aJCas, PrintWriter aOut)
    {
        Type neType = JCasUtil.getType(aJCas, NamedEntity.class);
        Feature neValue = neType.getFeatureByBaseName("value");

        // needed by the chunk IOB encoder
        Type chunkType = JCasUtil.getType(aJCas, Chunk.class);
        Feature chunkValue = chunkType.getFeatureByBaseName("chunkValue");
         
        // Sentences
        Collection<Sentence> sentences = select(aJCas, Sentence.class);
       
        //For each sentence... 
        for (Sentence sentence : sentences) {
        	
        	// Store the information about the sentence tokens in the ctokens map.
        	// How? ctokens maps each token a Row object, which contains  
        	// the token id, lemma, etc...
            HashMap<Token, Row> ctokens = new LinkedHashMap<Token, Row>();

            // Tokens
            List<Token> tokens = selectCovered(Token.class, sentence);
            
            // Poss
            List<POS> poss = selectCovered(POS.class, sentence);
            
            // Convert Named-entities to IOB format
            IobEncoder neEncoder = new IobEncoder(aJCas.getCas(), neType, neValue);        
    
            /**
             * Your code goes here!
             * 
             * Hint: Add the code to instantiate the IOBEncoder for chunks.
             *       The IOB encoder converts chunk annotations to the IOB format.
             *       This step is required as chunks may span several tokens.
             *       By using the IOB encoder, you have one annotation for each token.
             * 
             * // Convert chunks to IOB format
             * IobEncoder chunkEncoder = new IobEncoder(aJCas.getCas(), ..., ...);
             * 
             */
            for (int i = 0; i < tokens.size(); i++) {
                Row row = new Row();
                row.id = i+1;
                row.token = tokens.get(i);
                
                // Lemma
                row.lemma = tokens.get(i).getLemma();
                
                // Named-entity chunks in IOB format
                row.ne = neEncoder.encode(tokens.get(i));
                row.pos = poss.get(i);
                
                /**
                 * Your code goes here!
                 * 
                 * Hint: Add the code for storing the chunks in IOB format
                 *       inside the Row object.
                 * 
                 * // chunks in IOB format
                 * row.chunk = ...;
                 */
                
                // Add token information to the ctokens map
                ctokens.put(row.token, row);
            }
            
            // Write sentence in CONLL format
            for (Row row : ctokens.values()) {
            	/**
            	 * 
            	 * You code goes here!
            	 * 
            	 * Hint: Add the code for printing chunks.
            	 *  
            	 * aOut.printf("%d %s %s %s %s %s\n", row.id, row.token.getCoveredText(), row.lemma.getValue(), row.pos.getPosValue(), row.ne, ...);
            	 */
            }

            aOut.println();
        }
    }

    /** Store information about each token. */
    private static final class Row
    {
    	int id; 			
        Token token;
        String ne;	
        POS pos;		
        Lemma lemma;	
        
        /** Store the chunk annotation in IOB format. */
        String chunk;
    }
}
