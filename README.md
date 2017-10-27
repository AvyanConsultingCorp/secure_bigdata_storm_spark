# Reference Architecture Sample for HDInsight - Storm and Spark
A Reference Architecture with end-to-end functional application showcasing security and compliance for Storm &amp; Spark cluster on Azure (using HDInsight)

## Goal / Intention
Intention is to build a 1-click deployment quick-start that installs, configures a valid Retail use-case showcasing  
1.  Security by design 
2.  Compliant by default

## Business Use-case

Toymart is a fictious multinational toy manufacturing company. Toymart is a wholeseller and also has an online portal to sell directly to the customers.
The festive season is on and toymart's online sales are not as it was predicted. They want to improve there sales and get maximum profit
out of the festive season. To do this, toystore sales and marketing team wants the following information as and when sales happen
 - Total number of toys sold per hour
 - hottest selling toy 
 - total sales made every hour
 - failed transactions per hour

Toymart wants to also learn about the return rate of items and stop selling items with higher return rates 

As sales is sensitive Data, Toymart wants to limit only few users /roles (business group) to be able to view this data. 
(that's identity protection layer) 
Want to limit access to data from internet (external) that'll be nsg rules

Toystore technical team has decided to implement this by streaming the live transactions through storm. Storm will perform ETL to prepare the data for spark
Spark, will analyze the data as required. Power BI will connect to spark and display live dashboards.

## Architecture Diagram

## Deployment Steps


## System Configurations

### HDInsight Cluster
- [ ] Config 1
- [x] Config 2

### Spark Cluster
- [x] Config 1
- [ ] Config 2

...


