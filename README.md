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
 - a file containing many lines of text in english language;
 - the name of the dest directory (say output)

and performs sentence boundary detection, lemmatization, part-of-speech (pos) tagging and entity recognition.
The result of the processing are written in CoNLL format in the file *output/document.txt.conll* and 
could look something like this:

```
Pierre       Pierre       NNP B-person
Vinken       Vinken       NNP I-person
,            ,            ,   O
61           61           CD  O
years        year         NNS O
old          old          JJ  O
,            ,            ,   O
will         will         MD  O
join         join         VB  O
the          the          DT  O
board        board        NN  O
as           as           IN  O
a            a            DT  O
nonexecutive nonexecutive JJ  O
director     director     NN  O
Nov.         Nov.         NNP O
29           29           CD  O
.            .            .   O

```

## Importing the project into Eclipse

### Maven

If you use Eclipse please install the M2E plugin. Go to Help > Install new software... and search
for m2e.  


### Importing the project into Eclipse

- Open Eclipse
- Click on File -> Import -> Maven -> Existing Maven Projects 
- Select the NERDemo project folder and click on Finish

