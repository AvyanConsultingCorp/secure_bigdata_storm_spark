package com.microsoft.example;

// Generic storm stuff
import com.microsoft.azure.documentdb.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Tuple;

import java.util.Map;

// For logging
// For JSON
// For HTTPS communication
// for documentdb

public class DocumentDBBolt extends BaseRichBolt {
    OutputCollector collector;

    // Create logger for this class
    private static final Logger LOG = LogManager.getLogger(DocumentDBBolt.class);
    // The URL used to push data into PowerBI
    private String DocDBServiceEndPoint;
    private String DocDBMasterKey;
    private DocumentClient client;
    private String DocDBDatabase;
    private String DocDBCollection;
    // These are used to hold device variables until a tick occurs
    private int productid = 0;
    private int quantity = 0;
    private int sales = 0;
	private int refund = 0;
    private String orderdate;

    // Set the Push URL for Power BI streaming API
    public DocumentDBBolt(String _DocDBServiceEndPoint,String _DocDBMasterKey,String _DocDBDatabase, String _DocDBCollection) {
        this.DocDBServiceEndPoint= _DocDBServiceEndPoint;
        this.DocDBMasterKey = _DocDBMasterKey;
        this.DocDBDatabase = _DocDBDatabase;
        this.DocDBCollection = _DocDBCollection;

    }


    // Configure how often a tick tuple is sent to this bolt
    /*@Override
    public Map<String, Object> getComponentConfiguration() {
        Config conf = new Config();
        // Configure a tick every 10 seconds
        conf.put(Config.TOPOLOGY_TICK_TUPLE_FREQ_SECS, 10);
        return conf;
    }

    */
    // Prepare the bolt
    @Override
    public void prepare(Map config, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
        try{
        this.client = new DocumentClient(this.DocDBServiceEndPoint,this.DocDBMasterKey,ConnectionPolicy.GetDefault(),ConsistencyLevel.Session);

            System.out.println(this.client.getDatabaseAccount());
        } catch (Exception e) {
            LOG.error("problem", e);
            this.collector.reportError(e);
        }
    }
  
    // Declare output fields & streams
    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        // no streams emitted into the Storm topology from this bolt
    }

    // Process tuples
    @Override
    public void execute(Tuple tuple) {
        try {
           
                // Get data out of the tuple and store.
                // This overwrites whatever is there now,
                // because we only emit to Power BI every so often
                // and only want the most recent data
                this.productid = tuple.getIntegerByField("productid");
                this.quantity = tuple.getIntegerByField("quantity");
                this.sales = tuple.getIntegerByField("sales");
				this.refund = tuple.getIntegerByField("refund");
                this.orderdate = tuple.getStringByField("orderdate");

                // insert a new document into documentdb database
                Order neworder = new Order(this.productid,this.quantity,this.sales,this.refund,this.orderdate);
                String collectionLink = String.format("/dbs/%s/colls/%s", this.DocDBDatabase, this.DocDBCollection);
                this.client.createDocument(collectionLink, neworder, new RequestOptions(), false);

                // Why are we acking?
                // Because we shouldn't delay processing of items
                // just for the ephemeral display of data on a dashboard.
                // Especially when we are only displaying the last stored
                // data for a X second interval.
                // this.collector.ack(tuple);
            }
         catch (Exception e) {
            LOG.error("problem", e);
            this.collector.reportError(e);
        }
    }

    /*
    // Check if a tuple is a tick tuple
    protected static boolean isTickTuple(Tuple tuple) {
        return tuple.getSourceComponent().equals(Constants.SYSTEM_COMPONENT_ID)
            && tuple.getSourceStreamId().equals(Constants.SYSTEM_TICK_STREAM_ID);
    }
    */





}
