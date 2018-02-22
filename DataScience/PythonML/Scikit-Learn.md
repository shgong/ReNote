# 1. Machine Learning Landscape

## 1.1 Overview

- supervised
  - KNN
  - Linear/Logistic Regression
  - SVM
  - Decision Trees
  - Neural Network
- unsupervised learning
  - K-means
  - HCA (Hierarchical Cluster Analysis)
  - EM (Expectation Maximization)
  - PCA
  - LLE
- Reinforcement Learning
  - AlphaGo
  - agent can observe env and get rewards/penalties in return


```py
import matplotlib
import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
import sklearn

# Load the data
oecd_bli = pd.read_csv("oecd_bli_2015.csv", thousands=',')
gdp_per_capita = pd.read_csv("gdp_per_capita.csv",thousands=',',delimiter='\t',
                             encoding='latin1', na_values="n/a")

# Prepare the data
country_stats = prepare_country_stats(oecd_bli, gdp_per_capita)
X = np.c_[country_stats["GDP per capita"]]
y = np.c_[country_stats["Life satisfaction"]]

# Visualize the data
country_stats.plot(kind='scatter', x="GDP per capita", y='Life satisfaction')
plt.show()

# Select a linear model
lin_reg_model = sklearn.linear_model.LinearRegression()

# Train the model
lin_reg_model.fit(X, y)

# Make a prediction for Cyprus
X_new = [[22587]]  # Cyprus' GDP per capita
print(lin_reg_model.predict(X_new)) # outputs [[ 5.96242338]]
```


## 1.2 Challenges of ML

- Insufficient Quantity of Training Data
- Nonrepresentative Training Data
- Poor-Quality Data
- Irrelevant Features
- Overfitting/Underfitting the traning data




# 2. End-to-End ML Project

- Select a Performance Measure
  - RMSE: root of mean square error (L2 norm)
  - MAE: mean absolute error (L1 norm)
  - the higher the norm index, the more focus on outliers, RMSE is good when outliers are rare
- Check the assumptions
- Get the data
- Create the workspace
  - pip install virtualenv
  - anaconda env
  - jupyter notebook

## Download the data

```py
import os
import tarfile
from six.moves import urllib

DOWNLOAD_ROOT = "https://raw.githubusercontent.com/ageron/handson-ml/master/"
HOUSING_PATH = "datasets/housing"
HOUSING_URL = DOWNLOAD_ROOT + HOUSING_PATH + "/housing.tgz"

def fetch_housing_data(housing_url=HOUSING_URL, housing_path=HOUSING_PATH):
    if not os.path.isdir(housing_path):
        os.makedirs(housing_path)
    tgz_path = os.path.join(housing_path, "housing.tgz")
    urllib.request.urlretrieve(housing_url, tgz_path)
    housing_tgz = tarfile.open(tgz_path)
    housing_tgz.extractall(path=housing_path)
    housing_tgz.close()

import pandas as pd

def load_housing_data(housing_path=HOUSING_PATH):
    csv_path = os.path.join(housing_path, "housing.csv")
    return pd.read_csv(csv_path)
```


## look at data and play with histograms

```py
%matplotlib inline   # only in a Jupyter notebook
import matplotlib.pyplot as plt
housing.hist(bins=50, figsize=(20,15))
plt.show()
```

- create test set
  - split criterion, row number, distribution

```py
from sklearn.model_selection import train_test_split
train_set, test_set = train_test_split(housing, test_size=0.2, random_state=42)

from sklearn.model_selection import StratifiedShuffleSplit
split = StratifiedShuffleSplit(n_splits=1, test_size=0.2, random_state=42)
for train_index, test_index in split.split(housing, housing["income_cat"]):
    strat_train_set = housing.loc[train_index]
    strat_test_set = housing.loc[test_index]
```

## Looking for correlations

- compute standard correlation between pairs

···
corr_matrix = housing.corr()
corr_matrix["median_house_value"].sort_values(ascending=False)

median_house_value    1.000000
median_income         0.687170
total_rooms           0.135231
housing_median_age    0.114220
households            0.064702
total_bedrooms        0.047865
population           -0.026699
longitude            -0.047279
latitude             -0.142826
Name: median_house_value, dtype: float64
···

- or use pandas scatter_matrix function
  - histogram on axis, scatter on cross


## Prepare data for ML

- Data Cleaning

```py
housing.dropna(subset=["total_bedrooms"])    # option 1
housing.drop("total_bedrooms", axis=1)       # option 2
median = housing["total_bedrooms"].median()
housing["total_bedrooms"].fillna(median)     # option 3
```
- Scikit-Learn Imputer class

```py
from sklearn.preprocessing import Imputer
imputer = Imputer(strategy="median")
# drop non-numeric, and fit
housing_num = housing.drop("ocean_proximity", axis=1)
imputer.fit(housing_num) # get medians
X = imputer.transform(housing_num)
housing_tr = pd.DataFrame(X, columns=housing_num.columns)
```

- text and categorical attributes
  - labelEncoder to digits
  - oneHotEncoder, list of 1/0 categories
  - custom transformer

```py
from sklearn.preprocessing import OneHotEncoder
encoder = OneHotEncoder()
housing_cat_1hot = encoder.fit_transform(housing_cat_encoded.reshape(-1,1))
housing_cat_1hot
```

```py
from sklearn.base import BaseEstimator, TransformerMixin

rooms_ix, bedrooms_ix, population_ix, household_ix = 3, 4, 5, 6

class CombinedAttributesAdder(BaseEstimator, TransformerMixin):
    def __init__(self, add_bedrooms_per_room = True): # no *args or **kargs
        self.add_bedrooms_per_room = add_bedrooms_per_room
    def fit(self, X, y=None):
        return self  # nothing else to do
    def transform(self, X, y=None):
        rooms_per_household = X[:, rooms_ix] / X[:, household_ix]
        population_per_household = X[:, population_ix] / X[:, household_ix]
        if self.add_bedrooms_per_room:
            bedrooms_per_room = X[:, bedrooms_ix] / X[:, rooms_ix]
            return np.c_[X, rooms_per_household, population_per_household,
                         bedrooms_per_room]
        else:
            return np.c_[X, rooms_per_household, population_per_household]

attr_adder = CombinedAttributesAdder(add_bedrooms_per_room=False)
housing_extra_attribs = attr_adder.transform(housing.values)
```

- Feature Scaling
  - min-max scaling: shifted and rescaled to 0/1
  - standardization: compare to mean value


## Transformation Pipelines

- Scikit-learn pipeline class group transformation together

```py
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import StandardScaler

num_pipeline = Pipeline([
        ('imputer', Imputer(strategy="median")),
        ('attribs_adder', CombinedAttributesAdder()),
        ('std_scaler', StandardScaler()),
    ])

housing_num_tr = num_pipeline.fit_transform(housing_num)
```

- Union to make a full pipeline
```py
from sklearn.pipeline import FeatureUnion

num_attribs = list(housing_num)
cat_attribs = ["ocean_proximity"]

num_pipeline = Pipeline([
        ('selector', DataFrameSelector(num_attribs)),
        ('imputer', Imputer(strategy="median")),
        ('attribs_adder', CombinedAttributesAdder()),
        ('std_scaler', StandardScaler()),
    ])

cat_pipeline = Pipeline([
        ('selector', DataFrameSelector(cat_attribs)),
        ('label_binarizer', LabelBinarizer()),
    ])

full_pipeline = FeatureUnion(transformer_list=[
        ("num_pipeline", num_pipeline),
        ("cat_pipeline", cat_pipeline),
    ])
```

## Evaluation

```py
>>> from sklearn.metrics import mean_squared_error
>>> housing_predictions = lin_reg.predict(housing_prepared)
>>> lin_mse = mean_squared_error(housing_labels, housing_predictions)
>>> lin_rmse = np.sqrt(lin_mse)
>>> lin_rmse
68628.413493824875
```

- K-fold cross validation
```py
from sklearn.model_selection import cross_val_score
scores = cross_val_score(tree_reg, housing_prepared, housing_labels,
                         scoring="neg_mean_squared_error", cv=10)
rmse_scores = np.sqrt(-scores)
```


## Tuning

-  find hyperparameters with Grid Search
```py
from sklearn.model_selection import GridSearchCV

param_grid = [
    {'n_estimators': [3, 10, 30], 'max_features': [2, 4, 6, 8]},
    {'bootstrap': [False], 'n_estimators': [3, 10], 'max_features': [2, 3, 4]},
  ]

forest_reg = RandomForestRegressor()

grid_search = GridSearchCV(forest_reg, param_grid, cv=5,
                           scoring='neg_mean_squared_error')

grid_search.fit(housing_prepared, housing_labels)

grid_search.best_estimator_
RandomForestRegressor(bootstrap=True, criterion='mse', max_depth=None,
           max_features=6, max_leaf_nodes=None, min_samples_leaf=1,
           min_samples_split=2, min_weight_fraction_leaf=0.0,
           n_estimators=30, n_jobs=1, oob_score=False, random_state=None,
           verbose=0, warm_start=False)

# result with scores
cvres = grid_search.cv_results_
... for mean_score, params in zip(cvres["mean_test_score"], cvres["params"]):
...     print(np.sqrt(-mean_score), params)
...
64912.0351358 {'max_features': 2, 'n_estimators': 3}
55535.2786524 {'max_features': 2, 'n_estimators': 10}
52940.2696165 {'max_features': 2, 'n_estimators': 30}
60384.0908354 {'max_features': 4, 'n_estimators': 3}
52709.9199934 {'max_features': 4, 'n_estimators': 10}
50503.5985321 {'max_features': 4, 'n_estimators': 30}
59058.1153485 {'max_features': 6, 'n_estimators': 3}
52172.0292957 {'max_features': 6, 'n_estimators': 10}
49958.9555932 {'max_features': 6, 'n_estimators': 30}
59122.260006 {'max_features': 8, 'n_estimators': 3}
52441.5896087 {'max_features': 8, 'n_estimators': 10}
50041.4899416 {'max_features': 8, 'n_estimators': 30}
62371.1221202 {'bootstrap': False, 'max_features': 2, 'n_estimators': 3}
54572.2557534 {'bootstrap': False, 'max_features': 2, 'n_estimators': 10}
59634.0533132 {'bootstrap': False, 'max_features': 3, 'n_estimators': 3}
52456.0883904 {'bootstrap': False, 'max_features': 3, 'n_estimators': 10}
58825.665239 {'bootstrap': False, 'max_features': 4, 'n_estimators': 3}
52012.9945396 {'bootstrap': False, 'max_features': 4, 'n_estimators': 10}
```



# 3. Classification

- MNIST: high school handwriting digits data set
  - 28x28 pixels image (784 features)


## Binary Classifier

- SGD: Stochastic
  - decide if a image is five
```py
from sklearn.linear_model import SGDClassifier
sgd_clf = SGDClassifier(random_state=42)
sgd_clf.fit(X_train, y_train_5)
```

## Performance Measures
- Accuracy
  - SGD performance 0.909, 0.9128
  - only 10% is 5, guess not always has 90% accuracy
  - thats's why accuracy is not the prefereed measure

- Confusion Matrix
  - precision = TP / (TP + FP)
    - SGD 0.768
  - recall/sensitivity = TP / (TP + FN)
    - SGD 0.791
  - PR curve: trade off between precision and recall
    - favorable when positive case is rare
    - or care more about false positives

- ROC curve
  - receiver operating characteristics
  - use TP rate (recall) against FP rate (1-specificity)
  - favorable when
    - negative case is rare
    - or care more about false negatives


## Multiclass Classification
- binary: SVM, Linear
- multi: naive Bayes, random forest
- oneVSone strategy: compare between them

## Multilabel Classification
- KNN


# 4. Training Models

## Linear Regression

- linear model with coefficients
- solve normal equation, nxn matrix, get very slow when features increase

## Gradient Descent

- finding optimal solutions to a wide range of problems
  - filling parameter vector with random values, and tune it to find the local minimum
  - find minimum of cost function
  - which means gradient is zero
- learning rate / steps
  - may not converge if too large
  - may take long time if too small
- need ensure all features have similar scale to converge faster
- may have global minimum pitfall
  - luckily, MSE cost function for LR model happens to be convex

- Batch Gradient Descent
  - use whole training set to compute gradients

```py
eta = 0.1  # learning rate
n_iterations = 1000
m = 100

theta = np.random.randn(2,1)  # random initialization

for iteration in range(n_iterations):
    gradients = 2/m * X_b.T.dot(X_b.dot(theta) - y)
    theta = theta - eta * gradients
```

- Stochastic Gradient Descent
  - pick a random instance and computes based on single instance
  - much faster, but much less regular
  - final parameter values are good but not optimal

```py
n_epochs = 50
t0, t1 = 5, 50  # learning schedule hyperparameters

def learning_schedule(t):
    return t0 / (t + t1)

theta = np.random.randn(2,1)  # random initialization

for epoch in range(n_epochs):
    for i in range(m):
        random_index = np.random.randint(m)
        xi = X_b[random_index:random_index+1]
        yi = y[random_index:random_index+1]
        gradients = 2 * xi.T.dot(xi.dot(theta) - yi)
        eta = learning_schedule(epoch * m + i)
        theta = theta - eta * gradients
```

- or a mix, mini-batch gradient descent

## Polynomial Regression

- add powers of features
  - can have more accuracy, also easier to overfit
- bias/variance trade off
  - bias, estimate linear while quadratic
  - variance, quadratic, sensitive to small variations

## Regularized Linear Models
- restrict the degrees of freedoms
  - ways to constraint the weights
- Ridge
  - a regularization term of square weights added to cost function
  - not only fit the data, but keep model weights small
- Lasso
  - use L1 norm instead of L2 norm for weights
  - tend to eliminate weights of least important features
- Elastic Net
  - middle ground, with a mix ratio
  - ridge is a good default
  - lasso may behave strange when num of features > training instances

```py
>>> from sklearn.linear_model import ElasticNet
>>> elastic_net = ElasticNet(alpha=0.1, l1_ratio=0.5)
>>> elastic_net.fit(X, y)
>>> elastic_net.predict([[1.5]])
```

## Logistic Regression

- for binary classification
  - a sigmoid function (S shape)
  - p = 1 / (1 + exp ( -t ))
  - compare p with 0.5 to determine prediction
- cost function
  - if y=1, -log p
  - if y=0, -log (1-p)


# 5. SVM

- good for complex but small/mid datasets

## Linear SVM

- SVM classifier fit the widest possible margin between classes
  - determined by edge instances ( support vectors )
  - sensitive to feature scales

- some time clear margin is impossible
- Soft Margin Classification

```py
import numpy as np
from sklearn import datasets
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import StandardScaler
from sklearn.svm import LinearSVC

iris = datasets.load_iris()
X = iris["data"][:, (2, 3)]  # petal length, petal width
y = (iris["target"] == 2).astype(np.float64)  # Iris-Virginica

svm_clf = Pipeline((
        ("scaler", StandardScaler()),
        ("linear_svc", LinearSVC(C=1, loss="hinge")),
    ))

svm_clf.fit(X_scaled, y)
```

## Nonlinear SVM

- most dataset are not linear separatable
- use PolynomialFeatures transformer, make dataset splittable
- apply Kernel trick

```py
from sklearn.svm import SVC
poly_kernel_svm_clf = Pipeline((
        ("scaler", StandardScaler()),
        ("svm_clf", SVC(kernel="poly", degree=3, coef0=1, C=5))
    ))
poly_kernel_svm_clf.fit(X, y)
```


- Gaussian RBF Kernel
  - hyperparameter: gamma & c
  - gamam: make bell-shape curve narrower
    - if underfitting, increase gamma

## SVM Regression

- reverse the objective
- instead of fit largest margin, given margin violation
- try to fit as many instances as possible on the margin

# 6. Decision Trees
- versatile
- require little data preparation

## Training and visualizing a decision Tree

```py
from sklearn.datasets import load_iris
from sklearn.tree import DecisionTreeClassifier

iris = load_iris()
X = iris.data[:, 2:] # petal length and width
y = iris.target

tree_clf = DecisionTreeClassifier(max_depth=2)
tree_clf.fit(X, y)



from sklearn.tree import export_graphviz

export_graphviz(
        tree_clf,
        out_file=image_path("iris_tree.dot"),
        feature_names=iris.feature_names[2:],
        class_names=iris.target_names,
        rounded=True,
        filled=True
    )
```

## Making Predictions
- gini attribute
  - means impurity
  - pure gini=0, if all instances belong to same class
- G_i =  1 - Sum(p_i,k ^ 2)
  - p_i,k is ratio of class k instance among the training instances in ith node


## CART Training Algorithm

- Scikit-learn use classification and regression tree
  - first split using single feature k and threshold t_k
    - choose k and t_k to produce purest subsets (weight by sizes)
  - continue to split using same logic
    - with a few control stopping conditions (min_sample_split, min_weight_leaf)

- Greedy algorithm
- find optimal tree is NP-Complete problem

## Gini Impurity or Entropy

- Alternative: Entropy
  - H_i = - Sum p_i,k * log(p_i,k)
- does not make much difference
- Gini is faster
- Gini tends to isolate most frequent class in its own tree
- Entropy tends to produce slightly more balanced tree

## Regression

- Decision Tree Regressor
- regression tree produce a single value

# 7. Ensemble Learning and Random Forests

## Voting Classifiers

- aggregate predictions
  - will out perform best model

```py
from sklearn.ensemble import RandomForestClassifier
from sklearn.ensemble import VotingClassifier
from sklearn.linear_model import LogisticRegression
from sklearn.svm import SVC

log_clf = LogisticRegression()
rnd_clf = RandomForestClassifier()
svm_clf = SVC()

voting_clf = VotingClassifier(
        estimators=[('lr', log_clf), ('rf', rnd_clf), ('svc', svm_clf)],
        voting='hard'
    )
voting_clf.fit(X_train, y_train)
```

## Bagging and Pasting

- another ways
  - use same training algorithm
  - run on different subsets (like image recognition)
  - bootstrap aggregating (b-agging)
  - if without replacement: pasting

## Random Patches and Random Subspaces
The BaggingClassifier class supports sampling the features as well. This is controlled by two hyperparameters: max_features and bootstrap_features. They work the same way as max_samples and bootstrap, but for feature sampling instead of instance sampling. Thus, each predictor will be trained on a random subset of the input features.

This is particularly useful when you are dealing with high-dimensional inputs (such as images). Sampling both training instances and features is called the Random Patches method.7 Keeping all training instances (i.e., bootstrap=False and max_samples=1.0) but sampling features (i.e., bootstrap_features=True and/or max_features smaller than 1.0) is called the Random Subspaces method”


## Random Forests

- Ensembled decision trees via bagging
  - instead of searching for best feature
  - searching among a random subset of features
    - result in greater tree diversity
    - or considering random features for splitting, even more random
- calculate feature importance by depth in the tree


## Boosting

- hypothesis boosting
  - any ensemble method that combine weak learners into strong learner
  - train predictors sequentially, each trying to correct predecessor
- famous boosting
  - AdaBoost / adaptive boosting
  - Gradient Boosting


- AdaBoost
  - pay more attention to training instances that predecessor underfitted
  - this result in new predictors focusing more on hard cases
  - each iteration, relative weight of misclassified instance is increased
  - at the end, predictors have different weights depending on overall accuracy


```py
from sklearn.ensemble import AdaBoostClassifier

ada_clf = AdaBoostClassifier(
        DecisionTreeClassifier(max_depth=1), n_estimators=200,
        algorithm="SAMME.R", learning_rate=0.5
    )
ada_clf.fit(X_train, y_train)
```


- Gradient Boosting
  - not tweaking instance weights at every iteration
  - try to fit new predictor to the residual errors
- example: Gradient Tree Boosting / Regression Tree
  - train first tree on y
  - train 2nd tree on  y2 = y - tree1.predict(x)
  - train 3rd tree on  y3 = y2 - tree2.predict(x)


## Stacking

- Stacked generalization
  - instead of using trivial functions to aggregate predictions
  - train a model to preform the aggregation
- Blender, or final predictor


# 8. Dimensionality Reduction
## Curse of Dimensionality

## Main approaches
- Projection
- Manifold Learning

## PCA

## Kernel PCA

## LLE
