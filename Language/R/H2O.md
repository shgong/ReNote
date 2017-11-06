
- R use REST API to connect to H2O

## Installation

```r
install.packages("h2o")
library(h2o)
h2o.init(nthreads=-1)

?h2o.glm
?h2o.gbm

demo(h2o.glm)
```

## Initiation

- Lauch from R
  - specify number of CPU with `nthreads =`
    - default:2, `-1` to use all CPU on the host
  - specify max memory use `max_mem_size=`
    - default:1g, `500m` to use 500MB
    - recommend: at least 4x size of your data
- Lauch from CMD Line
  - java -jar h2o.jar
- Lauch on Hadoop
  - `hadoop jar h2odriver.jar -nodes 1 -mapperXmx 6g -output hdfsOutputDirName`
- Check Cluster Info  
  - h2o.clusterInfo()


## Data Preparation in R

- data conversion
  - `as.data.frame()` convert H2O data frame into R data frame
    - ensure there is enough memeory in R
    - recommend take samples only
  - `as.h2o()` transfer data from R to H2O instance
  - `str.H2OFrame` return elements of new object to confirm data transfered correctly


## Models

- Supervised Learning
  - Generalized Linear Models: flexible generalization of linear regression with error distribution models, unifies Poisson, linear, logistic when using l1 l2 regularization
  - Gradent Boosting: ensemble of weak prediction models, generalized by allowing an arbitrary differentiable loss function
  - Deep learning: non-linear, layer-by-layer, can use unlabeled data
  - Naive Bayes: probabilistic classifier that assume value of particular feature is unrelated of any other feature, used in text categorization
  - Stacked Ensemble: stacking multiple models built from different algorithms
  - XGBoost: optimized gradient boost under GBM

- Unsupervised Learning
  - K-means: cluster for segmentation
  - Anomaly Detection: pattern recognition, find outliers

- Miscellaneous
  - Word2Vec: produce word vectors using text corpus
  - Grid Search: standard hyper-parameter optimization to simplify configuration


## Data Manipulation in R

### Importing Files
```r
#To import small iris data file from H2O’s package:

irisPath = system.file("extdata", "iris.csv", package="h2o")
iris.hex = h2o.importFile(path = irisPath, destination_frame = "iris.hex")

#To import an entire folder of files as one data object:
# pathToFolder = "/Users/data/airlines/"
# airlines.hex = h2o.importFile(path = pathToFolder, destination_frame = "airlines.hex")

#To import from HDFS and connect to H2O in R using the IP and port of an H2O instance running on your Hadoop cluster:
# h2o.init(ip= <IPAddress>, port =54321, nthreads = -1)
# pathToData = "hdfs://mr-0xd6.h2oai.loc/datasets/airlines_all.csv"
# airlines.hex = h2o.importFile(path = pathToData,destination_frame = "airlines.hex")
```

### Uploading Files
```
irisPath = system.file("extdata", "iris.csv", package="h2o")
iris.hex = h2o.uploadFile(path = irisPath, destination_frame = "iris.hex")
```

### Factors
```
#Finding Factors
irisPath = system.file("extdata", "iris_wheader.csv", package="h2o")
iris.hex = h2o.importFile(path = irisPath)
h2o.anyFactor(iris.hex)

# Convert to factor
# Import prostate data
prosPath <- system.file("extdata", "prostate.csv", package="h2o")
prostate.hex <- h2o.importFile(path = prosPath)
# Converts column 4 (RACE) to an enum
prostate.hex[,4] <- as.factor(prostate.hex[,4])
summary(prostate.hex[,4])
```


## Running Models

- Gradient Boosting Machine
- Generalized Linear Models (GLM)
- K-Means
- Principal Component Analysis
- Prediction Methods
  - predict
  - Confusion Matrix
  - Area Under Curve
  - Hit Ratio
  - PCA Score
  - Multi-Model Scoring
