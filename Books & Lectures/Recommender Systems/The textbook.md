# I. Introduction

- Goals
  - Prediction version
  - Ranking version
- Increasing product sale
  - Relevance
  - Novelty
  - Serendipity
  - Increasing recommendation diversity

- Use Cases
  - MovieLens
  - Amazon
  - Netflix
  - Google News
  - Facebook friends

- types of rating
  - `-2` to `2`
  - `1` to `10`
  - `like` or `really like`



# II. Basic Models of RS

## 2. Neighborhood/Memory-Based Collaborative Filtering

- Neighborhood Collaborative Filtering
  - observed rating are often highly correlated across users
  - Generalization of KNN
  - instance based

- User-based
  - if Alice and Bob rated movies similarily in the past
  - use k-most similar user to Bob
- Item-based
  - if Alice like a set of items like Predator and Alien
  - might love Terminator

- Problem
  - Item rating always have long tail
  - different user may provide rating in different scales
    - use mean centered prediction
  - too sparse
    - dimensional reduction
    - SVD or PCA like method
  - biased Overfitting
    - user rate Gladiator and Terminator
    - only 1 user rate Gladiator, while 100 rate Terminator

- A Regression model view
  - both user/item based are linear functions

- Graph Models
  - use user-item graph
    - define neighbors with random walk
  - user-user graph
    - user-user predictability graph
  - item-item graph

## 3. Model-Based Collaborative Filtering

- after parameterized
  - decision tree
  - rule based
  - bayesian
  - latent factor

- Collaborative filtering methods can be viewed as generalizations of classification and regression
modeling
  - sparse matrix
  - missing value analysis

- latent factor
  - exploit the fact that significant portions of rows and columns are highly correlated
  - approximate well by a lower-rank matrix
  - it is possible that variations in six observed variables mainly reflect the variations in two unobserved (underlying) variables
  - Factor analysis searches for such joint variations in response to unobserved latent variables.
- incorporate user/item biasate
- incorporate implicit feedback

## 4. Content-Based RS

- previous only use correlations and ignore item attributes, seems wasteful

- use descriptive attributes of items
  - science fiction movie
  - kung fu movie
- regression method to generate metrics specific to a user
  - rating from other users usually play no roles
  - difficult for cold start

- structure
  - preprocessing and feature extraction
    - gini index, entropy, normalization
  - learning of user profile
  - filtering and recommendation


- advantage
  - good for new item
  - explain item features
  - with text classifier
- disadvantages
  - in many cases, they are obvious recommendation due to keyword
  - not good for new users


## 5. Knowledge-Based RS

- useful in context of items not purchased very often
  - real estate
  - auto mobile
  - tourism
  - financial service
  - luxury good
- user specification + item attributes + domain knowledge
  - perform on customer requiremets and item descriptions
  - knowledge base that contain data about rules and similarity functions

- more instance based
  - similarity matrix
  - incorporate diversity

- types
  - constraint-based: search criterion
  - case-based: find more similar

## 6. Ensemble & Hybrid

- collaborative filtering systems rely on community ratings,
- content-based methods rely on textual descriptions and the target user’s own ratings,
- knowledge-based systems rely on interactions with the user in the context of
knowledge bases


- Design
  - Ensemble design
    - combine rating from content-based and collaborative recommender
    - similar to most ensemble methods
    - sequential
      - feature augmentation
      - cascade
        - refine or boosting
    - parallel
      - weighted
      - switching
        - start as knowledge based to avoid cold start
        - later use content based or CF
  - Monolithic design:
    - Feature Combination
      - combine different data source together
    - Meta-Level
      - one model is used as input to another model
  - Mixed systems
    - use multiple algorithm as individually
    - present items side by side, like a TV program list


## 7. Evaluating RS

- similar to classification & regression
  - rating prediction
  - ranking
- User study
- Online Evaluation
  - accuracy
  - trust
  - novelty
  - serendipity

# III. Domain-Specific Challenges

## 8. Context-Sensitive RS

- Example: Clothes
  - time, location, social data
  - seasonal, big city, holiday & event
- use multi-dimensional model


## 9. Time/Location Sensitive RS

- How ratings evolve with time
  - incorporate time as explicit parameter
  - location based: add distance as parameter
- Ratings of seasonal items, only make sense in periods
  - viewed as special case


- Temporal Collaborative filtering
  - Recency-based: consider recent ratings more important than older
    - decay based
    - window based
  - Periodic context-based: week is used in prediction
    - adjust conventional list with temporal context as post-processing
  - Time-SVD++ MODEL, use time as independent variable in model processing


- Implicit feedback data
  - web click-streams
  - session time pattern
  - pattern mining and Markov Model / Recurrent Neutral Network


- Discrete Temporal Models
  - data source
    - web logs and clickstreams
    - supermarket transaction
    - history queries recommendation/auto-complete

- Markovian Models
  - information is encoded as states
  - kth order Markovian model define a state based on last k action
  - markov chain, long sequence or multiple sequence
  - use support/confidence/error to prune markov models

- Sequential Pattern Mining
  - subsequence
  - frequent subsequence
  - confidence: possibility of a1 to ak followed by ak+1
  - support: a1 to ak+1
  - Sequential Pattern-Based Rule: support>=s && confidence >= c

- Location data
  - Pyramid Tree
  - 2x2 Grid
  - 4x4 Grid

## 10. Structural Recommendation in Network

- Network Structures
  - Social cues
  - tags
  - suggest nodes and links within the network itself

- Key
  - Recommending nodes by authority and context
  - Recommending nodes by example
  - Recommending nodes by influence and content
  - Recommending links

### Ranking Algorithm

- PageRank
  - highly reputable content are more likely to be cited
  - highly reputable users are more likely to be followed by reputable users
  - random surfer mode
    - random pages visiting yield long term relative frequency
    - dead end components are common
    - teleportation or restart step
- Personalized PageRank
  - use neighborhood of user to aggregate
  - notion of homophily
  - need heterogeneous ranking approach
- Social Ranks
  - based on page rank
  - readjust similarity matrixs
- SimRank
  - determine reputable neighborhoods of query nodes
  - recursive function of in-linking nodes
- Friend Recommendation
  - neighborhood based
  - katz measure
  - random walk based
  - matrix factorization

## 11. Social and Trust-Centric RS

- Viral Marketing
  - determine influence and topically relevant entities
  - influential users in Twitter stream

- Trustworthy
  - user can express trust and distrust on another
  - useful for more robust recommendations

- Social Feedback
  - social tagging, like last.fm, Flickr
  - maybe short informative, like wrong classification


## 12. Attack-Resistant RS

- Impact on sale of products
  - seller may manipulate the RS

- Types of Attack
  - Random Attack
    - filler items are assigned their ratings from a probability distribution around global mean
    - target item is set to either min or max
  - Average Attack
    - set filler items to global mean of specific item
  - Bandwagon Attack
    - small number of items are popular
    - if always rate this in fake profile, easier to get similar to target users
    - more likely to affect users during an attack
  - Popular attack
    - set popular to 2 star, unpopular to 1 star
    - set target to 5 star
  - Love/Hate Attack
  - Probe Attack
    - obtain more realistic rating from a user-based recommender system

- Detecting Attacks
  - unsupervised attack detection
    - profile identical to many other profile
    - login behavior suspicious
    - other information
  - supervised attack detection
    - more effective
    - but difficult to obtain attacker profile


- Robust Recommender Design
  - Preventing Automated Attacks with CAPTCHAs
  - Use Social Trust as influence limiter
  - algorithms
    - clustering in neighborhood methods
    - fake profile detection during recommendation time
    - association based algorithm
    - robust matrix factorization

## 13. Advanced Topics

- The cold start problem
- group recommender
- criterion Filtering
  - rate individually for music, plot
- active learning
  - acquire sufficient ratings
