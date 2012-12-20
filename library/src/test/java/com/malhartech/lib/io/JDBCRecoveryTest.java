/*
 *  Copyright (c) 2012 Malhar, Inc.
 *  All Rights Reserved.
 */
package com.malhartech.lib.io;

import com.malhartech.api.*;
import com.malhartech.api.Context.OperatorContext;
import com.malhartech.bufferserver.util.Codec;
import com.malhartech.lib.io.JDBCOutputOperatorTest.JDBCOutputOperatorTestTemplate;
import com.malhartech.stram.StramLocalCluster;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

import java.util.HashSet;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Chetan Narsude <chetan@malhar-inc.com>
 */
public class JDBCRecoveryTest
{
  private static final Logger logger = LoggerFactory.getLogger(JDBCRecoveryTest.class);
  private static int tupleCount = 0;
  private static final int maxTuple = 30;
  private static int columnCount = 7;
  private static int dataset = 1;
  private static final String driver = "com.mysql.jdbc.Driver";
  private static final String url = "jdbc:mysql://localhost/test?user=test&password=";
  private static final String db_name = "test";
  private static Connection con = null;
  private static Statement stmt = null;
  static JDBCOperatorTestHelper helper = new JDBCOperatorTestHelper();

  public JDBCRecoveryTest()
  {
    helper.buildDataset();
  }

  public static class MyHashMapOutputOperator extends JDBCHashMapOutputOperator<Object> implements CheckpointListener
  {
    private int simulateFailure;
    private transient boolean nexttime;

    @Override
    public void setup(OperatorContext context)
    {
      logger.debug("MyHashMapOutputOperator: context:" + context.getId());

      try {
        super.setup(context);
      }
      catch (Exception e) {
        logger.debug(e.toString());
      }
    }

    @Override
    public void beginWindow(long windowId)
    {
      super.beginWindow(windowId);
      logger.debug(windowId + " MyHashMapOutputOperator beginwindow {}", Codec.getStringWindowId(windowId));
    }

    @Override
    public void endWindow()
    {
      if (ignoreWindow) {
        return;
      }
      super.endWindow();
      readTable(getTableName(), getConnection());
      logger.debug("MyHashMapOutputOperator endWindow {}", Codec.getStringWindowId(windowId));
    }

    /**
     * @param simulateFailure the simulateFailure to set
     */
    public void setSimulateFailure(boolean simulateFailure)
    {
      if (simulateFailure) {
        this.simulateFailure = 1;
      }
      else {
        this.simulateFailure = 0;
      }
    }

    @Override
    public void checkpointed(long windowId)
    {
      logger.debug("JDBCRecoveryTest checkpointed windowId:" + windowId);
      if (simulateFailure > 0 && --simulateFailure == 0) {
        nexttime = true;
      }
      else if (nexttime) {
        throw new RuntimeException("JDBCRecoveryTest Failure Simulation from " + this);
      }
    }

    @Override
    public void committed(long windowId)
    {
    }
  }

  @Test
  public void testInputOperatorRecovery() throws Exception
  {
    setupDB("Test_Tuple", helper.hashMapping1, true);

    DAG dag = new DAG();
    dag.getAttributes().attr(DAG.STRAM_CHECKPOINT_INTERVAL_MILLIS).set(500);
    dag.getAttributes().attr(DAG.STRAM_WINDOW_SIZE_MILLIS).set(300);
    dag.getAttributes().attr(DAG.STRAM_MAX_CONTAINERS).set(1);

    JDBCRecoverInputOperator rip = dag.addOperator("Generator", JDBCRecoverInputOperator.class);
    rip.setMaximumTuples(maxTuple);

    MyHashMapOutputOperator oper = dag.addOperator("Collector", MyHashMapOutputOperator.class);
      oper.setDbUrl("jdbc:mysql://localhost/test?user=test&password=");
      oper.setDbDriver("com.mysql.jdbc.Driver");
      oper.setTableName("Test_Tuple");

      oper.setMaxWindowTable("maxwindowid");
      oper.setsApplicationId("appid");
      oper.setsOperatorId("operatorid");
      oper.setsWindowId("winid");

      oper.setBatchSize(100);
      oper.setColumnMapping(helper.hashMapping1);//    oper.setSimulateFailure(true);

    dag.addStream("connection", rip.output, oper.inputPort);

    final StramLocalCluster lc = new StramLocalCluster(dag);
    lc.setHeartbeatMonitoringEnabled(false);
//    new Thread("LocalClusterController")
//    {
//      @Override
//      public void run()
//      {
//        try {
//          Thread.sleep(10000);
//        }
//        catch (InterruptedException ex) {
//        }
//        lc.shutdown();
//      }
//    }.start();

    lc.run();

    junit.framework.Assert.assertEquals("Number of emitted tuples", maxTuple, tupleCount);
    logger.debug(String.format("Number of emitted tuples: %d", tupleCount));
  }

  public void setupDB(String tableName, String[] mapping, boolean isHashMap)
  {
    int num = mapping.length;
    int colIdx = isHashMap ? 1 : 0;
    int typeIdx = isHashMap ? 2 : 1;

    String str = "";
    for (int i = 0; i < num; i++) {
      String[] parts = mapping[i].split(":");
      if (i == 0) {
        str += parts[colIdx] + " " + parts[typeIdx];
      }
      else {
        if (parts[typeIdx].equals("BAD_COLUMN_TYPE")) {
          str += ", " + parts[colIdx] + " INTEGER";
        }
        else {
          str += ", " + parts[colIdx] + " " + parts[typeIdx];
        }
      }
    }

//    str += ", winid BIGINT";
    String createTable = "CREATE TABLE " + tableName + " (" + str + ")";

    try {
      // This will load the JDBC driver, each DB has its own driver
      Class.forName(driver).newInstance();
      //String temp = "jdbc:derby:test;create=true";

      con = DriverManager.getConnection(url);
      stmt = con.createStatement();

      String createDB = "CREATE DATABASE IF NOT EXISTS " + db_name;
      String useDB = "USE " + db_name;

      stmt.executeUpdate(createDB);
      stmt.executeQuery(useDB);

      stmt.execute("DROP TABLE IF EXISTS " + tableName);
      logger.debug("DROP TABLE IF EXISTS " + tableName);
      stmt.executeUpdate(createTable);
      logger.debug(createTable);

      stmt.executeUpdate("CREATE TABLE IF NOT EXISTS maxwindowid (appid VARCHAR(10), winid BIGINT, operatorid VARCHAR(10))");
      logger.debug("CREATE TABLE IF NOT EXISTS maxwindowid (appid VARCHAR(10), winid BIGINT, operatorid VARCHAR(10))");
    }
    catch (ClassNotFoundException ex) {
      throw new RuntimeException("Exception during JBDC connection", ex);
    }
    catch (SQLException ex) {
      throw new RuntimeException(String.format("Exception during creating table with query: %s", createTable), ex);
    }
    catch (Exception ex) {
      throw new RuntimeException("Exception during JBDC connection", ex);
    }

    logger.debug("JDBC Table creation Success");
  }

  public static void createDatabase(String dbName, Connection con)
  {
    Statement stmt = null;
    try {
      stmt = con.createStatement();

      String createDB = "CREATE DATABASE IF NOT EXISTS " + dbName;
      String useDB = "USE " + dbName;

      stmt.executeUpdate(createDB);
      stmt.executeQuery(useDB);
    }
    catch (SQLException ex) {
      throw new RuntimeException("Exception during creating database", ex);
    }
    finally {
      try {
        if (stmt != null) {
          stmt.close();
        }
      }
      catch (SQLException ex) {
        throw new RuntimeException("Exception while closing database resource", ex);
      }
    }

    logger.debug("JDBC DB creation Success");
  }

  public static void createTable(String tableName, Connection con, ArrayList<String> columns, HashMap<String, String> colTypes) throws Exception
  {
    int num = columns.size();
    String cols = columns.get(0) + " " + colTypes.get(columns.get(0));
    for (int i = 1; i < num; i++) {
      cols = cols + ", " + columns.get(i) + " " + colTypes.get(columns.get(i));
    }

    String str = "CREATE TABLE " + tableName + " (" + cols + ")";

    Statement stmt = null;
    try {
      stmt = con.createStatement();
      stmt.execute("DROP TABLE IF EXISTS " + tableName);
      logger.debug("DROP TABLE IF EXISTS " + tableName);
      stmt.executeUpdate(str);
      logger.debug(str);
    }
    catch (SQLException ex) {
      throw new RuntimeException("Exception during creating table", ex);
    }
    finally {
      try {
        if (stmt != null) {
          stmt.close();
        }
      }
      catch (SQLException ex) {
        throw new RuntimeException("Exception while closing database resource", ex);
      }
    }
    logger.debug("JDBC Table creation Success");
  }

  public static void readTable(String tableName, Connection con)
  {
    String query = "SELECT * FROM " + tableName;
    Statement stmt = null;
    try {
      stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery(query);
      logger.debug(query);
      while (rs.next()) {
        if (dataset == 1) {
          logger.debug(String.format("%d, %d, %d, %d, %d, %d, %d",
                                     rs.getInt("col1"), rs.getInt("col2"), rs.getInt("col3"),
                                     rs.getInt("col4"), rs.getInt("col5"), rs.getInt("col6"),
                                     rs.getInt("col7")));
        }
        else if (dataset == 2) {
          logger.debug(String.format("%d, %s, %d, %s, %d, %s, %d",
                                     rs.getInt("col1"), rs.getString("col2"),
                                     rs.getInt("col3"), rs.getString("col4"),
                                     rs.getInt("col5"), rs.getString("col6"),
                                     rs.getInt("col7")));
        }
        else if (dataset == 3) {
          logger.debug(String.format("%d, %d, %s, %s, %s, %s, %f",
                                     rs.getInt("col1"), rs.getInt("col2"),
                                     rs.getDate("col3"), rs.getDate("col4"),
                                     rs.getString("col5"), rs.getString("col6"),
                                     rs.getDouble("col7")));
        }
        tupleCount++;
      }
    }
    catch (SQLException ex) {
      throw new RuntimeException("Exception during reading from table", ex);
    }
    finally {
      try {
        if (stmt != null) {
          stmt.close();
        }
      }
      catch (SQLException ex) {
        throw new RuntimeException("Exception while closing database resource", ex);
      }
    }
  }
}
