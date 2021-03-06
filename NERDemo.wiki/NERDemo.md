# NERDemo

This is a simple demo showing how to perform many NLP tasks such as text segmentation, part-of-speech (pos) tagging, lemmatization and named entity recognition (NER) by using UIMA and DKPro.

## Prerequisites

### JDK 7+

Download and install the Java SE Development Kit 7 or higher from the Oracle Java Site.

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

The first time you run the app, it will take time since it is downloading JARs and model files.

## The NERDemo app

The NERDemo app takes in input two parameters:
 - srcFile -- a file containing many lines of text in English language;
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

The app performs sentence boundary detection, lemmatization, pos tagging and 
recognition of entities appearing within the text.
The result of the processing are written` in CONLL format in the file *output/document.txt.conll*.

To check the result, type:
```
$> less output/document.txt.conll
```
 
The output file should look something like this:

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
- Write the results to disk in CONLL format


```java
// NERDemo app

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
                 * Write the result to disk in CONLL format. The results are
                 * written in the output directory passed in input
                 */
                 createEngineDescription(ConllWriter.class,
                        ConllWriter.PARAM_TARGET_LOCATION, outputDir));
     }
}
```

The ConllWriter writes annotations from the CAS object to a file.

To do so, first, it retrieves the list of sentences from the CAS passed in input to the `convert()` method.

Then, for each sentence, it stores in the `ctokens` map the information about the tokens appearing in the sentence. Information about a single token is stored in a *Row* object.

A Row object contains the following information about a token:

1. the token id/position in a sentence
2. the token itself
3. the lemma corresponding to the token
4. the part-of-speech tag assigned to the token
5. the named-entity category label in IOB format assigned to a token

At the end, the ConllWriter writes information about the tokens appearing in the sentence to a file.


```java
// ConllWriter

public class ConllWriter extends JCasFileWriter_ImplBase {

    ...

    private void convert(JCas aJCas, PrintWriter aOut) {

        // Sentences
        Collection<Sentence> sentences = select(aJCas, Sentence.class);
       
        // For each sentence... 
        for (Sentence sentence : sentences) {
        	
            // Store the information about the sentence tokens in the ctokens map.
            // How? ctokens maps each token a Row object, which contains  
            // the token id, lemma, etc...
            HashMap<Token, Row> ctokens = new LinkedHashMap<Token, Row>();

            // Tokens
            List<Token> tokens = selectCovered(Token.class, sentence);
            
            // Poss
            List<POS> poss = selectCovered(POS.class, sentence);
            
            // used to convert named-entities to IOB format
            IobEncoder encoder = new IobEncoder(aJCas.getCas(), neType, neValue);
            
            for (int i = 0; i < tokens.size(); i++) {
                // create a new Row object and store annotations for
                // token at position $i
                   
                Row row = new Row();
                row.id = i+1;
                row.token = tokens.get(i);
                
                // Lemma
                row.lemma = tokens.get(i).getLemma();
                
                // Named-entity chunks in IOB format
                row.ne = encoder.encode(tokens.get(i));
                row.pos = poss.get(i);

                // Add token information to the ctokens map
                ctokens.put(row.token, row);
            }
      
            // Write sentence in CONLL format 
            for (Row row : ctokens.values()) {                
                aOut.printf("%d %s %s %s %s\n", row.id, row.token.getCoveredText(), row.lemma.getValue(), row.pos.getPosValue(), row.ne);
            }
            aOut.println();
       }
    }
}

```

## Homeworks

### Homework 1: Print token boundaries

Modify the code in `ConllWriter.java` source file so that the token boundaries are printed to the output file.

**Hint:** Look at the method [getBegin()](https://uima.apache.org/downloads/releaseDocs/2.3.0-incubating/docs/api/org/apache/uima/jcas/tcas/Annotation.html#getBegin%28%29) and [getEnd()](https://uima.apache.org/downloads/releaseDocs/2.3.0-incubating/docs/api/org/apache/uima/jcas/tcas/Annotation.html#getEnd%28%29) of the [Annotation](https://uima.apache.org/downloads/releaseDocs/2.3.0-incubating/docs/api/org/apache/uima/jcas/tcas/Annotation.html) class to get the token boundaries.

The output file should look something like this:

```
1       Pierre       Pierre       NNP B-person  0  6 
2       Vinken       Vinken       NNP I-person  7 13
3       ,            ,            ,   O        13 14
4       61           61           CD  O        15 17
5       years        year         NNS O        18 23
6       old          old          JJ  O        24 27
7       ,            ,            ,   O        27 28
8       will         will         MD  O        29 33
9       join         join         VB  O        34 38 
10      the          the          DT  O        39 42
11      board        board        NN  O        43 48
12      as           as           IN  O        49 51
13      a            a            DT  O        52 53
14      nonexecutive nonexecutive JJ  O        54 66
15      director     director     NN  O        67 75
16      Nov.         Nov.         NNP O        76 80
17      29           29           CD  O        81 83
18      .            .            .   O        83 84

```

### Homework 2: Print chunk annotations

Modify the code in the `ChunkerDemo.java` and `ChunkerConllWriter.java` source files to print the chunk annotations to the output file.

**Hint:** we marked with `/** Your code goes here! */` the part of code you need to modify.
You have to replace the ellipsis `...` with the right code.

After that, compile and package the project into a JAR:

```
$> mvn compile package
```

Then, run the project:

```
$> ./ChunkerDemo.sh data/document.txt output
```

The output file should look something like this:

```
1	Pierre       Pierre       NNP B-person B-NP
2	Vinken       Vinken       NNP I-person I-NP
3	,            ,            ,   O        O
4	61           61           CD  O        B-NP
5	years        year         NNS O        I-NP
6	old          old          JJ  O        B-ADJP
7	,            ,            ,   O        O
8	will         will         MD  O        B-VP
9	join         join         VB  O        I-VP
10	the          the          DT  O        B-NP
11	board        board        NN  O        I-NP
12	as           as           IN  O        B-PP
13	a            a            DT  O        B-NP
14	nonexecutive nonexecutive JJ  O        I-NP
15	director     director     NN  O        I-NP
16	Nov.         Nov.         NNP O        B-NP
17	29           29           CD  O        I-NP
18	.            .            .   O        O
```


 

## Importing the project into Eclipse

### Maven

If you use Eclipse please install the M2E plugin. Go to Help > Install new software... and search for m2e.  


### Importing the project into Eclipse

- Open Eclipse
- Click on File -> Import -> Maven -> Existing Maven Projects 
- Select the NERDemo project folder and click on Finish


This tutorial was partly  based on a similar dkpro tutorial located at: [https://dkpro.github.io/dkpro-core/pages/java-intro/](https://dkpro.github.io/dkpro-core/pages/java-intro/)
