# NERDemo

This is a simple demo showing how to perform simple IR tasks such as Named Entity Recognition by using UIMA and DKPro.

## Prerequisites

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

## Running the NER demo

```
$> ./NERDemo.sh data/document.txt output
```

Look into the NERDemo.sh script ot figure out what is happening.

The first time you lunch the app, it will take time since it is downloading JARs and model files.

## The NER demo

The NERDemo app takes in input two parameters:
 - textfile -- a file containing many lines of text in english language;
 -  destdir -- the name of the output directory

and performs sentence boundary detection, lemmatization, part-of-speech (pos) tagging and entity recognition.
The result of the processing are written in CoNLL format in the file *output/document.txt.conll* and 
could look something like this:

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

The first column  contains the token position
The second column conains the token text
The third column contains the lemma for corresponding to a token
The fourth column contains the part-of-speech tag assigned to a token 
The fifth column contains the NE category/type in IOB format assigned to a token. 

## Importing the project into Eclipse

### Maven

If you use Eclipse please install the M2E plugin. Go to Help > Install new software... and search
for m2e.  


### Importing the project into Eclipse

- Open Eclipse
- Click on File -> Import -> Maven -> Existing Maven Projects 
- Select the NERDemo project folder and click on Finish

