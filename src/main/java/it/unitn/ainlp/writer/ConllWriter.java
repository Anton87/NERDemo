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
package it.unitn.ainlp.writer;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
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
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;


/**
 * <p>Writes results in the format below. The columns are separated by a single space, unlike
 * illustrated below.</p>
 * 
 * <pre><code>
 * Pierre       Pierre NNP B-person
 * Vinken       Vinken NNP I-person
 * ,            ,      ,   O
 * 61           61     CD  O
 * years        year   NNS O
 * old          old    JJ  O
 * ,            ,      ,   O
 * will         will   MD  O
 * join         join   VB  O
 * the          the    DT  O
 * board        board  NN  O
 * as           as     IN  O
 * a            a      DT  O
 * nonexecutive nonexecutive JJ  O
 * director     director     NN  O
 * Nov.         Nov.         NNP O
 * 29           29           CD  O
 * .            .            .   O

 * </code></pre>
 * 
 * <ol>
 * <li>FORM - token</li>
 * <li>POS - pos</li>
 * <li>NER - named entity (BIO encoded)</li>
 * </ol>
 * 
 * <p>Sentences are separated by a blank new line.</p>
 * 
 * @see <a href="http://www.clips.ua.ac.be/conll2002/ner/">CoNLL 2002 shared task</a>
 */
@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
        "de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity"})
public class ConllWriter
    extends JCasFileWriter_ImplBase
{
    private static final String UNUSED = "_";

    /**
     * Name of configuration parameter that contains the character encoding used by the input files.
     */
    public static final String PARAM_ENCODING = "sourceEncoding";
    @ConfigurationParameter(name = PARAM_ENCODING, mandatory = true, defaultValue = "UTF-8")
    private String encoding;

    public static final String PARAM_FILENAME_SUFFIX = "filenameSuffix";
    @ConfigurationParameter(name = PARAM_FILENAME_SUFFIX, mandatory = true, defaultValue = ".conll")
    private String filenameSuffix;

    public static final String PARAM_WRITE_NAMED_ENTITY = "writeNamedEntity";
    @ConfigurationParameter(name = PARAM_WRITE_NAMED_ENTITY, mandatory = true, defaultValue = "true")
    private boolean writeNamedEntity;

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        PrintWriter out = null;
        try {
            out = new PrintWriter(new OutputStreamWriter(getOutputStream(aJCas, filenameSuffix),
                    encoding));
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

        for (Sentence sentence : select(aJCas, Sentence.class)) {
            HashMap<Token, Row> ctokens = new LinkedHashMap<Token, Row>();

            // Tokens
            List<Token> tokens = selectCovered(Token.class, sentence);
            
            // Pos
            List<POS> poss = selectCovered(POS.class, sentence);
            
            // Chunks
            IobEncoder encoder = new IobEncoder(aJCas.getCas(), neType, neValue);
            
            for (int i = 0; i < tokens.size(); i++) {
                Row row = new Row();
                row.id = i+1;
                row.token = tokens.get(i);
                row.lemma = tokens.get(i).getLemma();
                row.ne = encoder.encode(tokens.get(i));
                row.pos = poss.get(i);
                ctokens.put(row.token, row);
            }
            
            // Write sentence in CONLL 2006 format
            for (Row row : ctokens.values()) {
                String chunk = UNUSED;
                if (writeNamedEntity && (row.ne != null)) {
                    chunk = encoder.encode(row.token);
                }
                
                aOut.printf("%s %s %s %s\n", row.token.getCoveredText(), row.lemma.getValue(), row.pos.getPosValue(), chunk);
            }

            aOut.println();
        }
    }

    private static final class Row
    {
        int id;
        Token token;
        String ne;
        // inserted by me
        POS pos;
        Lemma lemma;
    }
}
