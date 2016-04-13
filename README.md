# NERDemo

This is a simple demo showing how to perform simple IR tasks such as Named Entity Recognition by using UIMA and DKPro.

## Prerequisites

### JDK 7+

Download and install the Java SE Development Kit 7 from the Oracle Java Site.

### UIMA

Go to https://uima.apache.org/downloads.cgi and download the latest binary distribution of UIMA.  

Decompress the archive and set the $UIMA_HOME environmental variable to the
UIMA distribution directory. If you use Linux or Mac please set the variable with:

```
export UIMA_HOME="path/to/dir/of/uima"
```

Add the line to  ~/.bashrc or ~/.bash_profile in order to automatically set the variable in the future.

### Maven

You will need Maven for building this project. Install maven for your operating system (e.g. sudo
apt-get install maven).  

Follow the instructions contained 


## Installation

Clone the NERDemo app from the repository with:

```
$> git clone https://github.com/Anton87/NERDemo NERDemo
```

Go to the project main directory and type:

```
$> mvn clean
```

This cleans up artifacts created by prior builds.

Then, type:

```
$> mvn compile
```

This compiles the source code of the project.

Then

```
$> mvn -DskipTests dependency:copy-dependencies package
```

This takes the compiled code and package it in its distributable format, such as a JAR.

## Running the NERDemo app

```
$> ./NERDemo.sh <src file> <dest dir>
```

Look into the NERDemo.sh script ot figure out what is happening.

The first time you lunch the app, it will take time since it is downloading JARs and model files.

## The NERDemo app

The NERDemo app takes in input two parameters:
 - srcFile -- a file containing many lines of text in english language;
 - destDir -- the name of the output directory

The data folder contains a file, *data/document.txt*, which contains the following text:
```
Pierre Vinken, 61 years old, will join the board as a nonexecutive director Nov. 29.
...

```

To lunch the app, type:
```
$> ./NERDemo.sh data/document.txt output
```

The app performs sentence boundary detection, lemmatization, part-of-speech (pos) tagging and 
recognition of entities appearing within the text.
The result of the processing are written` in CoNLL format in the file *output/document.txt.conll*.

To check the result, type:
```
$> less output/document.txt.conll
```
 
The ouptut file should look something like this:

```
1       Pierre       Pierre       NNP B-person
2       Vinken       Vinken       NNP I-person
3       ,            ,            ,   O
4       61           61           CD  O
5       years        year         NNS O
6       old          old          JJ  O
7       ,            ,            ,   O
8       will         will         MD  O
9       join         join         VB  O
10      the          the          DT  O
11      board        board        NN  O
12      as           as           IN  O
13      a            a            DT  O
14      nonexecutive nonexecutive JJ  O
15      director     director     NN  O
16      Nov.         Nov.         NNP O
17      29           29           CD  O
18      .            .            .   O
```

The first column  contains the token position.

The second column contains the token text.

The third column contains the lemma corresponding to a given token.

The fourth column contains the part-of-speech tag assigned to a token.

The fifth column contains the NE category/type in [IOB](https://en.wikipedia.org/wiki/Inside_Outside_Beginning) format assigned to a token.


## Code Walk-through

The NERDemo app performs the following steps:

- Read English text from the file passed as input
- Perform sentence boundary detection and tokenization
  using OpenNLP
- Perform lemmatization using !LanguageTool
- Perform Named Entity Recognition using OpenNLP
  - include models for recognizing person, organization and location
    names
- Write the results to disk in CoNLL format


```java
// NERDemo app
package it.unitn.ainlp.app;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;
import it.unitn.ainlp.writer.ConllWriter;
import org.apache.commons.cli.Options.*;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.languagetool.LanguageToolLemmatizer;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpNameFinder;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter;

public class NERDemo 
{
    public static void main( String[] args ) throws Exception {


        // get input text file 
        String inputFile = args[0];
    	
        // get output directory
        String outputDir = args[1];  
    	
        // Run a sequence of analysis engines on a text document    	
        runPipeline(
                /*
                 * Read text from file passed in input.
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
                 * Write the result to disk in CoNLL format. The results are
                 * written in the output directory passed in input
                 */
                 createEngineDescription(ConllWriter.class,
                        ConllWriter.PARAM_TARGET_LOCATION, outputDir));
     }
}
```

The ConllWriter writes annotations from the CAS object to a file.

To do so, first, it retrieves the list of sentence (annotations) obtained by using the OpenNlpSegmenter annotator.

Then, it iterates over all the sentences collected and for each sentence it retrieves:
 1. the list of tokens
 2. part-of-speech tags
 3. lemmas
 4. and named-entities


The list of annotations referring to each single token is saved into the map ctokens.

```java

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
            IobEncoder encoder = new IobEncoder(aJCas.getCas(), neType, neValue);
            
            for (int i = 0; i < tokens.size(); i++) {
                Row row = new Row();
                row.id = i+1;
                row.token = tokens.get(i);
                
                // Lemma
                row.lemma = tokens.get(i).getLemma();
                
                // Named-entity chunks in IOB format
                row.ne = encoder.encode(tokens.get(i));
                row.pos = poss.get(i);
                ctokens.put(row.token, row);
            }
     ...
}

```
 

## Importing the project into Eclipse

### Maven

If you use Eclipse please install the M2E plugin. Go to Help > Install new software... and search
for m2e.  


### Importing the project into Eclipse

- Open Eclipse
- Click on File -> Import -> Maven -> Existing Maven Projects 
- Select the NERDemo project folder and click on Finish


This tutorial was partly  based on a similar dkpro tutorial located at: [https://dkpro.github.io/dkpro-core/pages/java-intro/](https://dkpro.github.io/dkpro-core/pages/java-intro/)
