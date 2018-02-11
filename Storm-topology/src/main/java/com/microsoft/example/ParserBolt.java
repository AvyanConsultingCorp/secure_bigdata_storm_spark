package com.microsoft.example;

import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class ParserBolt extends BaseBasicBolt {
  
    // Declare output fields & streams
    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        // Declare the fields in the tuple. These fields are accessible by
        // these names when read by bolts.
        declarer.declare(new Fields("productid", "quantity", "sales", "refund","orderdate"));
    }

    //Process tuples
    @Override
    public void execute(Tuple tuple, BasicOutputCollector collector) {
        // Should only be one tuple, which is the JSON message from the spout
        String value = tuple.getString(0);

        // Deal with cases where we get multiple
        // EventHub messages in one tuple
        String[] arr = value.split("}");
        for (String ehm : arr)
        {
            //Convert it from JSON to an object
            JSONObject msg=new JSONObject(ehm.concat("}"));
            if(msg.has("productid") && msg.has("quantity") && msg.has("sales") && msg.has("refund")) {
                // Pull out the values out of JSON and emit as a tuple
                int productid = msg.getInt("productid");
                int quantity = msg.getInt("quantity");
                int sales = msg.getInt("sales");
				int refund = msg.getInt("refund");

                // Create a orderdate, since there is none in the original data
                TimeZone timeZone = TimeZone.getTimeZone("UTC");
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
                dateFormat.setTimeZone(timeZone);

                // Emit to a tuple with values from JSON
                collector.emit(new Values(productid, quantity, sales,refund ,dateFormat.format(new Date())));
            }
        }
    }
}
