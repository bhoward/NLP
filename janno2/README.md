# JAnno

This is a work-in-progress on putting together some Java libraries ([Apache Tika](https://tika.apache.org/) and [Stanford CoreNLP](https://stanfordnlp.github.io/CoreNLP/)) to make a tool that helps in annotating sentences extracted from a document.

# Building and Running

use Maven:
```
mvn exec:java
```

Click "Load" to select a document (just about any format should be OK), then choose a sentence from the list ahd click "Analyze". The first sentence will take a long time to parse (watch the status bar), but after that it will be fast. In the analysis window, select a noun phrase and marvel as it is highlighted in the text pane!