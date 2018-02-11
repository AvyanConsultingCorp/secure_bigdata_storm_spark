package com.microsoft.example;

// Generic storm stuff
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.Constants;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.Config;
import org.apache.storm.task.TopologyContext;
import java.util.Map;

// For logging
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

// For JSON
import org.json.JSONObject;
import org.json.JSONArray;

// For HTTPS communication
import javax.net.ssl.HttpsURLConnection;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.OutputStream;

// Post data to Power BI using real-time streaming
// For more information on the real-time streaming in Power BI, see https://powerbi.microsoft.com/en-us/documentation/powerbi-service-real-time-streaming/
public class PowerBIBolt extends BaseRichBolt {
    OutputCollector collector;

    // Create logger for this class
    private static final Logger LOG = LogManager.getLogger(PowerBIBolt.class);
    // The URL used to push data into PowerBI
    private URL pbiPushURL;
    // These are used to hold device variables until a tick occurs
    private int productid = 0;
    private int quantity = 0;
    private int sales = 0;
	private int refund = 0;
    private String orderdate;

    // Set the Push URL for Power BI streaming API
    public PowerBIBolt(String pbiPushURL) throws MalformedURLException {
        this.pbiPushURL = new URL(pbiPushURL);
    }

    // Configure how often a tick tuple is sent to this bolt
    @Override
    public Map<String, Object> getComponentConfiguration() {
        Config conf = new Config();
        // Configure a tick every 10 seconds
        conf.put(Config.TOPOLOGY_TICK_TUPLE_FREQ_SECS, 10);
        return conf;
    }

    // Prepare the bolt
    @Override
    public void prepare(Map config, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
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
            if(isTickTuple(tuple)) {
                // Create a JSON document out of the stored data.
                // The structure is:
                // [{ "productid": number, "quantity": number, "sales": number,"refund: number ,orderdate: datetime }]
                // NOTE: If you don't have the array wrapper around the data, you get a 400 HTTP status
                // but Power BI still seems to accept the data
                JSONObject order = new JSONObject();
                order.put("productid", this.productid);
                order.put("quantity", this.quantity);
                order.put("sales", this.sales);
				order.put("refund", this.refund);
                order.put("orderdate", this.orderdate);
                JSONArray row = new JSONArray();
                row.put(order);
                // Make a connection to Power BI. The URL includes authentication information.
                HttpsURLConnection conn = (HttpsURLConnection) this.pbiPushURL.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                // Write the JSON document to the output stream
                OutputStream os = conn.getOutputStream();
                os.write(row.toString().getBytes());
                os.close();
                // check the response
                int httpResult = conn.getResponseCode();
                if(httpResult == HttpURLConnection.HTTP_OK) {
                    LOG.info("productid {}, quantity {}, sales {}, refund {},orderdate {} successfully posted to PowerBI.",
                        this.productid,
                        this.quantity,
                        this.sales,
						this.refund,
                        this.orderdate);
                    conn.disconnect();
                } else {
                    // Note that this does NOT trigger a failure/exception for the component
                    // as it might be a transient networking problem.
                    LOG.error("Failed to write to PowerBI. Status {}", httpResult);
                    conn.disconnect();
                }
            } else {
                // Get data out of the tuple and store.
                // This overwrites whatever is there now,
                // because we only emit to Power BI every so often
                // and only want the most recent data
                this.productid = tuple.getIntegerByField("productid");
                this.quantity = tuple.getIntegerByField("quantity");
                this.sales = tuple.getIntegerByField("sales");
				this.refund = tuple.getIntegerByField("refund");
                this.orderdate = tuple.getStringByField("orderdate");
                
                // Why are we acking?
                // Because we shouldn't delay processing of items
                // just for the ephemeral display of data on a dashboard.
                // Especially when we are only displaying the last stored
                // data for a X second interval.
                this.collector.ack(tuple);
            }
        } catch (Exception e) {
            LOG.error("problem", e);
            this.collector.reportError(e);
        }
    }

    // Check if a tuple is a tick tuple
    protected static boolean isTickTuple(Tuple tuple) {
        return tuple.getSourceComponent().equals(Constants.SYSTEM_COMPONENT_ID)
            && tuple.getSourceStreamId().equals(Constants.SYSTEM_TICK_STREAM_ID);
    }
}
