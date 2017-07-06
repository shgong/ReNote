
# SparkML

## 1. Pipeline Concepts

Spark ML API mostly inspired by scikit-learn project.

- DataFrame: from Spark SQL as an ML dataset
- Pipeline: chains Transformer & Estimator together
    + run a sequence of algorithm to process/learn from data
    + like split document, convert features and learn model
- Pipeline Stages
    + Transformer
        * transform DataFrame with features to predictions
        * Transformer.transform()
        * like read a column from Dataframe, map it to a new column like feature vectors, and append it to output new column
    + Estimator
        * fit on DataFrame to produce Transformer, like learning algorithm
        * Estimator.fit()
        * implement a method fit to train a logistic regression model
    + Parameter: needed for transformer/estimator
        * ParamMap is a set of (para,value) pairs
        * set for an instance
            - lr.setMaxIter(10).setRegParam(0.3)
            - ParamMap(lr1.maxIter->10, lr2.maxIter->20)
        * pass ParamMap to fit() or transform
            - will override previous parameters
- Details
    + DAG Pipelines: ordered array with a Directed Acyclic Graph
    + Runtime checking: cannot use compile-time type checking


```python

from pyspark.ml import Pipeline
from pyspark.ml.classification import LogisticRegression
from pyspark.ml.feature import HashingTF, Tokenizer
from pyspark.sql import Row

# Prepare training documents from a list of (id, text, label) tuples.
LabeledDocument = Row("id", "text", "label")
training = sqlContext.createDataFrame([
    (0L, "a b c d e spark", 1.0),
    (1L, "b d", 0.0),
    (2L, "spark f g h", 1.0),
    (3L, "hadoop mapreduce", 0.0)], ["id", "text", "label"])

# Configure an ML pipeline, which consists of tree stages: tokenizer, hashingTF, and lr.
tokenizer = Tokenizer(inputCol="text", outputCol="words")
hashingTF = HashingTF(inputCol=tokenizer.getOutputCol(), outputCol="features")
lr = LogisticRegression(maxIter=10, regParam=0.01)
pipeline = Pipeline(stages=[tokenizer, hashingTF, lr])

# Fit the pipeline to training documents.
model = pipeline.fit(training)

# Prepare test documents, which are unlabeled (id, text) tuples.
test = sqlContext.createDataFrame([
    (4L, "spark i j k"),
    (5L, "l m n"),
    (6L, "mapreduce spark"),
    (7L, "apache hadoop")], ["id", "text"])

# Make predictions on test documents and print columns of interest.
prediction = model.transform(test)
selected = prediction.select("id", "text", "prediction")
for row in selected.collect():
    print(row)

```

## 2. Extract/Transform/Select features

- Feature Extractor
    + TF-IDF
        * Term Frequency-Inverse Document Frequency
        * TF: hash sentence to feature vector
        * IDF: rescale feature vector
    + Word2Vec
        * transform documenyt to a vector
        * using average of all words
    + CountVectorizer
        * produce sparse representation for documents
        * can pass to other algorithm like LDA
        * select the top vocabSize words ordered by term frequency across the corpus. 
        * An optional parameter “minDF” also affect the fitting process by specifying the minimum number of documents a term must appear in to be included in the vocabulary.
- Feature Transformer
    + Tokenizer: break into words
    + StopWordsRemover: remove (stopwords)[http://ir.dcs.gla.ac.uk/resources/linguistic_utils/stop_words]
    + N-gram: sequence strings to grams
    + Binarizer:  thresholding numerical features to binary (0/1) features.
    + PCA: principal components, reduce dimension
    + PolynomialExpansion: expand to polynomial space
    + DCT: discrete cosine transform, time domain -> frequency domain, but only use real numbers
    + StringIndexer: category to categoryIndex
    + IndexToString: reverse
    + OneHotEncoder: column of laber to binary vectors, allow algorithm need continuous features like logistic regreesion
    + VectorIndex
    + Normalizer: p-norm, standardize
    + StandardScaler: standard deviation
    + MinMaxScaler: rescale linearly to [0,1] range
    + Bucketizer: continuous feature to feature buckets
    + VectorAssembler: combine given column into single vector
    + QuantileDiscretizer: continuous->discrete
- Feature Selectors
    + VectorSlicer
    + RFormula
    + ChiSqSelector

## 3. Classification & Regression

- Classification
    + logistic regression
        * binary response
        * `traning = sqlContext.read.format().load(path)`
        * `lr = LogisticRegreesion(maxIter=10, regParam=0.3, elasticNetParam=0.8)`
        * `lrModel = lr.fit(training)`
    + decision tree classifier
    + random forest classifier
    + multiplayer perceptron classifier
        * feed forward artificial neural network
        * layers = [4, 5, 4, 3]
- Regression
    + linear regression
        * elastic net, get coefficients & intercepts
    + decision tree regression
    + random forest regression
- decision tree
    + input: labelCol, featuresCol
    + output: predictionCol, 
- tree ensembles
    + random forest
        * combine many trees to reduce overfitting
    + gradient-boosted trees
        * minimize loss function

## 4. Cluster

- K-means
    + commonly used, hard to beat
- LDA:Latent Dirichlet allocation
    + goal
        * discovering topics that these sentences contain
        * produce topic, topic group
    + assume document are written by generative model
        *   topic, portion, probability, words
    + learning
        * randomly assign each word to topic
        * for each document
            - assume all rest topic assignment is right
            - update assignment for this document
