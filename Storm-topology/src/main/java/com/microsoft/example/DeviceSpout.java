package com.microsoft.example;

import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.Utils;
import org.json.JSONObject;

import java.util.Map;
import java.util.Random;

//This spout randomly emits device info
public class DeviceSpout extends BaseRichSpout {
    //Collector used to emit output
    SpoutOutputCollector _collector;
    //Used to generate a random number
    Random _rand;
	int _maxpid;
	int _minpid;
	
    //Open is called when an instance of the class is created
    @Override
    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
    //Set the instance collector to the one passed in
        _collector = collector;
        //For randomness
        _rand = new Random();
		_maxpid=10;
		_minpid=1;
		
    }

    //Emit data to the stream
    @Override
    public void nextTuple() {
        // Sleep for 1 second so the data rate
        // is slower.
        Utils.sleep(10000);
        // Randomly emit device info
		
        int productid = _rand.nextInt(_maxpid - _minpid + 1) + _maxpid; // generate productid between 1 & 10
        int quantity = _rand.nextInt(50); // quantiy sold
        int amount = _rand.nextInt(600);
		int refund=0;
		int sales=0;
		if(amount%2==0)
        {
            refund = amount;
        }else
        {
            sales = amount;
        }

        //Create a JSON document to send to Event Hub
        
        JSONObject message = new JSONObject();
        message.put("productid", productid);
        message.put("quantity", quantity);
        message.put("sales", sales);
		message.put("refund", refund);
		//message.put("orderdate", orderdate);
        //Emit the transaction
        _collector.emit(new Values(message.toString()));
    }

    //Ack is not implemented
    @Override
    public void ack(Object id) {
    }

    //Fail is not implemented
    @Override
    public void fail(Object id) {
    }

    //Declare the output fields. In this case, an single tuple
    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("message"));
    }
}
