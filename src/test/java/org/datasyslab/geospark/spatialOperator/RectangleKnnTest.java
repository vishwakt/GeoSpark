/**
 * FILE: RectangleKnnTest.java
 * PATH: org.datasyslab.geospark.spatialOperator.RectangleKnnTest.java
 * Copyright (c) 2017 Arizona State University Data Systems Lab.
 * All rights reserved.
 */
package org.datasyslab.geospark.spatialOperator;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.datasyslab.geospark.enums.FileDataSplitter;
import org.datasyslab.geospark.enums.IndexType;
import org.datasyslab.geospark.spatialRDD.RectangleRDD;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 
 * @author Arizona State University DataSystems Lab
 *
 */

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;


// TODO: Auto-generated Javadoc
/**
 * The Class RectangleKnnTest.
 */
public class RectangleKnnTest {
    
    /** The sc. */
    public static JavaSparkContext sc;
    
    /** The prop. */
    static Properties prop;
    
    /** The input. */
    static InputStream input;
    
    /** The Input location. */
    static String InputLocation;
    
    /** The offset. */
    static Integer offset;
    
    /** The splitter. */
    static FileDataSplitter splitter;
    
    /** The index type. */
    static IndexType indexType;
    
    /** The num partitions. */
    static Integer numPartitions;
    
    /** The loop times. */
    static int loopTimes;
    
    /** The query point. */
    static Point queryPoint;
    
    /**
     * Once executed before all.
     */
    @BeforeClass
    public static void onceExecutedBeforeAll(){
        SparkConf conf = new SparkConf().setAppName("RectangleKnn").setMaster("local[2]");
        sc = new JavaSparkContext(conf);
        Logger.getLogger("org").setLevel(Level.WARN);
        Logger.getLogger("akka").setLevel(Level.WARN);
        prop = new Properties();
        input = RectangleKnnTest.class.getClassLoader().getResourceAsStream("rectangle.test.properties");

        //Hard code to a file in resource folder. But you can replace it later in the try-catch field in your hdfs system.
        InputLocation = "file://"+RectangleKnnTest.class.getClassLoader().getResource("primaryroads.csv").getPath();

        offset = 0;
        splitter = null;
        indexType = null;
        numPartitions = 0;
        GeometryFactory fact=new GeometryFactory();
        try {
            // load a properties file
            prop.load(input);
            // There is a field in the property file, you can edit your own file location there.
            // InputLocation = prop.getProperty("inputLocation");
            InputLocation = "file://"+RectangleKnnTest.class.getClassLoader().getResource(prop.getProperty("inputLocation")).getPath();
            offset = Integer.parseInt(prop.getProperty("offset"));
            splitter = FileDataSplitter.getFileDataSplitter(prop.getProperty("splitter"));
            indexType = IndexType.getIndexType(prop.getProperty("indexType"));
            numPartitions = Integer.parseInt(prop.getProperty("numPartitions"));
            loopTimes=5;
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        queryPoint = fact.createPoint(new Coordinate(-84.01, 34.01));
    }

    /**
     * Teardown.
     */
    @AfterClass
    public static void teardown(){
        sc.stop();
    }

    /**
     * Test spatial knn query.
     *
     * @throws Exception the exception
     */
    @Test
    public void testSpatialKnnQuery() throws Exception {
    	RectangleRDD rectangleRDD = new RectangleRDD(sc, InputLocation, offset, splitter);

    	for(int i=0;i<loopTimes;i++)
    	{
    		List<Envelope> result = KNNQuery.SpatialKnnQuery(rectangleRDD, queryPoint, 5);
    		assert result.size()>-1;
    		assert result.get(0).getUserData().toString()!=null;
    		//System.out.println(result.get(0).getUserData().toString());
    	}

    }
    
    /**
     * Test spatial knn query using index.
     *
     * @throws Exception the exception
     */
    @Test
    public void testSpatialKnnQueryUsingIndex() throws Exception {
    	RectangleRDD rectangleRDD = new RectangleRDD(sc, InputLocation, offset, splitter);
    	rectangleRDD.buildIndex(IndexType.RTREE);
    	for(int i=0;i<loopTimes;i++)
    	{
    		List<Envelope> result = KNNQuery.SpatialKnnQueryUsingIndex(rectangleRDD, queryPoint, 5);
    		assert result.size()>-1;
    		assert result.get(0).getUserData().toString()!=null;
    		//System.out.println(result.get(0).getUserData().toString());
    	}

    }

}