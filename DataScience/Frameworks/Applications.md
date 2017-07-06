# Big Data Application


## 1. Example: Supermarket

Milk are at end the supermarket

- daily need 
- low profit
- How to group item and understand consumer behavior

Macy's: data by categories 

- Male/Female, agent, city, winter sales, superbowl sales
- Business plan, Storage cost
- Need predictions on what in shop

Data is increasing exponentially

- data doubles year by year
- more and more people online
- in so many different formats 
    + natural language on social networks
    + online transaction logs
    + relational databases


## 2. Example: Ad Service Company

A NY-based company, banners on website.
Companies like T-mobile, Nike, Washington post use it,
Providing target audience demographic and the payment per click.

Buy data from facebook, match profile to target website.
Don't need profile for individual, just care about attributes
make a profile what a typical customer from this region looks like
For the priviledge of this detail, the customer is willing to pay more.

Very close to machine learning: nearest neibourgh (profile)

- If they do not click shoes, why showing them shoes?
- When do people click and buy?
- What do people looking for?
- What price for what time advertisement click

Problem

- Cannot even query on big databases, so huge
- Look for some some data, and something else
- or use mapreduce job, much faster than traditional ETL

##### Adblock users?

Information how much portion of people use Adblock

- [Why Stack Overflow Doesn’t Care About Ad Blockers](https://blog.stackoverflow.com/2016/02/why-stack-overflow-doesnt-care-about-ad-blockers/)
- [Google, Microsoft, and Amazon are paying Adblock Plus huge fees to get their ads unblocked](http://www.businessinsider.com/google-microsoft-amazon-taboola-pay-adblock-plus-to-stop-blocking-their-ads-2015-2)
- [Allowing acceptable ads in Adblock Plus](https://adblockplus.org/acceptable-ads)
- [Google Punishing Adblock Users with unskippable Youtube ads](http://www.geek.com/apps/google-starts-punishing-adblock-users-with-unskippable-youtube-video-ads-1633305/)

## 3. Example: Oil Pipeline

- Pipeline sensors
    + big sensor per 30min
    + small sensor per 3h
- monitor pipeline performance
    + depend on environment
        * speed
        * temperature
        * raining
    + which areas need frequent assessment
        * weekly, monthly, per 3 months
        * area under soil or under water
        * how to make it more efficient
- when pipeline go wrong
    + go back to the data
    + investigate how it happened
        * maybe pressure data drop in certain area
    + can we build model for it?
    + can we predict potential risks?
- when start a new project
    + all the options
    + overground / underground / undersea
    + tradeoff, compensation, routes choices
- challenge
    + thousands of miles pipeline
    + each part maintaned by different pepople




## 4. More Examples
Use data from you didn't expect and find some value in it
Chances are everywhere

##### Red Roof Inn
use real-time data from facebook/twitter 
- passengers stuck in NY / JFK
- potential customer, show targeted ads, emails, texts

##### Weather Channel
bought by CBS partly in 2008
weather.com map data
a publisher, provide detailed information on customers

##### American Express Card
History transaction 
Information about how you spend money


##### Hotel Datawarehouse
- used to identify customer and avoid fakes
- Know it is you as you use the same credit card / address /phone
    + Court case: fake name, cash, another number
- problem: each hotel have a different system (like checkin checkout)

##### Website Targeting
age, male, occupation, likes
mobile device, browser, time, fall in which category
