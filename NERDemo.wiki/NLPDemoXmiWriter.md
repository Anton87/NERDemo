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
 - destDir -- the name of the output directory where to save the xmi cas






