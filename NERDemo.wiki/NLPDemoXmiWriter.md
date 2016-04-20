# NLPDemoXmiWriter

This is a simple application that reads text from a file and print
1. sentences
2. tokens
3. POSs
4. lemmas
5. named entities 
5. constituency-based parse trees.

in ever, followed by the constituent trees as a bracketed structure.

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


You will need Maven for building this project. Install maven for your operating system (e.g. sudo apt-get install maven).  


## Installation

Clone the NLPDemoXmiWriter app from the repository with:

```
$> git clone https://github.com/Anton87/NERDemoXmiWriter NERDemoXmiWriter
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

## Running the NLPDemoXmiCas app

```
$> ./NLPDemoXmiCas.sh <src file> <dest dir>
```

Look into the NERDemoXmiCas.sh script ot figure out what is happening.

The first time you run the app, it will take time since it is downloading JARs and model files.

The NLPDemoXmiCas app takes in input two parameters:
 - srcFile -- the path of a file containing many text in English language;
 - destDir -- the name of the directory where to save the output in [xmi](https://en.wikipedia.org/wiki/XML_Metadata_Interchange) format.

The data folder contains a file, *data/document.txt*, which contains the following text:
```
Pierre Vinken, 61 years old, will join the board as a nonexecutive director Nov. 29.
...

```

To lunch the app, type:
```
$> ./NERDemoXmiCas.sh data/document.txt outputXmi
```

The app performs:

1. text segmentation
 * 1.1 sentence splitting
 * 1.2 tokenization
2. lemmatization
3. pos tagging
4. named entity recognition
5. constituency-based parsing

The result of the processing are written in xmi format in the file *outputXmi/document.txt.xmi*.

In addition, the application outputs the processing results to the console. The output shold look somthing like this

```
```

### Show results in the UIMA CAS Visual Debugger

To show the results in the UIMA CAS Visual Debugger, type:

```
java -cp ${UIMA_HOME}/lib/uima-core.jar:${UIMA_HOME}/lib/uima-tools.jar org.apache.uima.tools.cvd.CVD
```

* Click on File -> Read Type System File
  * Select the file *outputXmi/typesystem.xml* in the project dir
* Click on File -> Read Xmi CAS File
  * Select the file *outputXmi/document.txt.xmi* in the project dir

Then, form the top left window *Analysis Results*
 * Click with the right button on AnnotationIndex > uima.tcas.Annotation > de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity and select *Show annotations: de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity*


