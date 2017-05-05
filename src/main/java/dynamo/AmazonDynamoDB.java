package dynamo;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import renderfarm.util.Metric;
import renderfarm.util.NormalizedWindow;
import renderfarm.util.Measures;


import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.amazonaws.services.dynamodbv2.util.TableUtils;
import com.amazonaws.services.dynamodbv2.model.ConditionalOperator;

/**
 * This sample demonstrates how to perform a few simple operations with the
 * Amazon DynamoDB service.
 */
public class AmazonDynamoDB{

    /*
     * Before running the code:
     *      Fill in your AWS access credentials in the provided credentials
     *      file template, and be sure to move the file to the default location
     *      (~/.aws/credentials) where the sample code will load the
     *      credentials from.
     *      https://console.aws.amazon.com/iam/home?#security_credential
     *
     * WARNING:
     *      To avoid accidental leakage of your credentials, DO NOT keep
     *      the credentials file in your source directory.
     */

    private static final String TABLE_NAME="metrics-table";
    private static final String TABLE_PARTION_KEY="file_name";
    private static final String TABLE_SORT_KEY="metrics_hash";
    private static final String WINDOW_X = "window_x";
    private static final String WINDOW_Y = "window_y";
    private static final String WINDOW_WIDTH = "window_width";
    private static final String WINDOW_HEIGHT = "window_height";
    private static final String WINDOW_Y_PLUS_WINDOW_HEIGHT = "window_y_plus_window_height";
    private static final String WINDOW_X_PLUS_WINDOW_WIDTH = "window_x_plus_window_width";
    private static final String WINDOW_TOTAL_PIXELS_RENDERED = "window_total_pixels_rendered";
    private static final String METRICS_BASIC_BLOCK_COUNT = "metrics_basic_block_count";
    private static final String METRICS_LOAD_COUNT = "metrics_load_count";
    private static final String METRICS_STORE_COUNT = "metrics_store_count";
    private static final String COMPLEXITY = "complexity";






    private AmazonDynamoDBClient dynamoDB;


    /**
     * The only information needed to create a client are security credentials
     * consisting of the AWS Access Key ID and Secret Access Key. All other
     * configuration, such as the service endpoints, are performed
     * automatically. Client parameters, such as proxies, can be specified in an
     * optional ClientConfiguration object when constructing a client.
     *
     * @see com.amazonaws.auth.BasicAWSCredentials
     * @see com.amazonaws.auth.ProfilesConfigFile
     * @see com.amazonaws.ClientConfiguration
     */
    private void init(){
        /*
         * The ProfileCredentialsProvider will return your [default]
         * credential profile by reading from the credentials file located at
         * (~/.aws/credentials).
         */
        AWSCredentials credentials = null;
        try {
            credentials = new ProfileCredentialsProvider().getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                    "Please make sure that your credentials file is at the correct " +
                    "location (~/.aws/credentials), and is in valid format.",
                    e);
        }
        dynamoDB = new AmazonDynamoDBClient(credentials);
        Region usWest2 = Region.getRegion(Regions.US_WEST_2);
        dynamoDB.setRegion(usWest2);
    }

    public AmazonDynamoDB(){
        init();
        CreateTable();
    }

    public void putItem(String file_name, float window_x, float window_y, float window_width, float window_height, long window_total_pixels_rendered,long metrics_basic_block_count, long metrics_load_count, long metrics_store_count, int complexity){
        int hash = hashFunction(window_x, window_y, window_width, window_height);
        Map<String, AttributeValue> item = newItem( file_name, hash, window_x, window_y, window_width, window_height,window_total_pixels_rendered,metrics_basic_block_count,metrics_load_count,metrics_store_count,complexity);
        PutItemRequest putItemRequest = new PutItemRequest(TABLE_NAME, item);
        PutItemResult putItemResult = dynamoDB.putItem(putItemRequest);
        System.out.println("Result: " + putItemResult);

    }

  public List<Metric> getIntersectiveItems(String file_name, float window_x, float window_y, float window_width, float window_height){
        HashMap<String, Condition> scanFilter = new HashMap<String, Condition>();
            Condition condition = new Condition()
                .withComparisonOperator(ComparisonOperator.LE.toString())
                .withAttributeValueList(new AttributeValue().withN(Float.toString(window_x+window_width)));
            scanFilter.put(WINDOW_X, condition);
            Condition condition2 = new Condition()
                .withComparisonOperator(ComparisonOperator.GE.toString())
                .withAttributeValueList(new AttributeValue().withN(Float.toString(window_y+window_height)));
            scanFilter.put(WINDOW_Y, condition2);
            Condition condition3 = new Condition()
                .withComparisonOperator(ComparisonOperator.LE.toString())
                .withAttributeValueList(new AttributeValue().withN(Float.toString(window_y)));
            scanFilter.put(WINDOW_Y_PLUS_WINDOW_HEIGHT, condition);
            Condition condition4 = new Condition()
                .withComparisonOperator(ComparisonOperator.LE.toString())
                .withAttributeValueList(new AttributeValue().withN(Float.toString(window_x)));
            scanFilter.put(WINDOW_X_PLUS_WINDOW_WIDTH, condition4);
            /*Condition condition5 = new Condition()
                .withComparisonOperator(ComparisonOperator.EQ.toString())
                .withAttributeValueList(new AttributeValue().withS(file_name));
            scanFilter.put(TABLE_PARTION_KEY, condition5);*/
            ScanRequest scanRequest = new ScanRequest(TABLE_NAME).withScanFilter(scanFilter);
            scanRequest.setConditionalOperator(ConditionalOperator.OR);
            ScanResult scanResult = dynamoDB.scan(scanRequest);
            List<Map<String,AttributeValue>> info = scanResult.getItems();
            List<Metric> result= new ArrayList();
            for(int i= 0; i<info.size();i++){
                Map<String,AttributeValue> pre_metric=info.get(i);
                String fileName= pre_metric.get(TABLE_PARTION_KEY).getS();
                if(fileName.equals(file_name)){
                    float windowX = Float.parseFloat(pre_metric.get(WINDOW_X).getN());
                    float windowY= Float.parseFloat(pre_metric.get(WINDOW_Y).getN());
                    float windowHeight= Float.parseFloat(pre_metric.get(WINDOW_HEIGHT).getN());
                    float windowWidth= Float.parseFloat(pre_metric.get(WINDOW_WIDTH).getN());
                    NormalizedWindow window = new NormalizedWindow (windowX,windowY,windowWidth,windowHeight);
                    long metrics_basic_block_count= Long.parseLong(pre_metric.get(METRICS_BASIC_BLOCK_COUNT).getS());
                    long metrics_load_count= Long.parseLong(pre_metric.get(METRICS_LOAD_COUNT).getS());
                    long metrics_store_count= Long.parseLong(pre_metric.get(METRICS_STORE_COUNT).getS());
                    Measures measure = new Measures(metrics_basic_block_count,metrics_load_count,metrics_store_count);
                    long window_total_pixels_rendered= Long.parseLong(pre_metric.get(WINDOW_TOTAL_PIXELS_RENDERED).getS());
                    int complexity=Integer.parseInt(pre_metric.get(COMPLEXITY).getN());
                    Metric metric = new Metric(fileName,window,window_total_pixels_rendered,measure,complexity);
                    result.add(metric);
                }
            }
            return result;

    }

    private static Map<String, AttributeValue> newItem(String file_name,int hash, float window_x, float window_y, float window_width, float window_height, long window_total_pixels_rendered,long metrics_basic_block_count, long metrics_load_count, long metrics_store_count, int complexity){
        Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
        item.put(TABLE_PARTION_KEY, new AttributeValue(file_name));
        item.put(TABLE_SORT_KEY, new AttributeValue().withN(Integer.toString(hash)));
        item.put(WINDOW_X, new AttributeValue().withN(Float.toString(window_x)));
        item.put(WINDOW_Y, new AttributeValue().withN(Float.toString(window_y)));
        item.put(WINDOW_X_PLUS_WINDOW_WIDTH,new AttributeValue().withN(Float.toString(window_x+window_width)));
        item.put(WINDOW_Y_PLUS_WINDOW_HEIGHT,new AttributeValue().withN(Float.toString(window_y + window_height)));
        item.put(WINDOW_HEIGHT, new AttributeValue().withN(Float.toString(window_height)));
        item.put(WINDOW_WIDTH, new AttributeValue().withN(Float.toString(window_width)));
        item.put(WINDOW_TOTAL_PIXELS_RENDERED, new AttributeValue().withS(Long.toString(window_total_pixels_rendered)));
        item.put(METRICS_BASIC_BLOCK_COUNT, new AttributeValue().withS(Long.toString(metrics_basic_block_count)));
        item.put(METRICS_LOAD_COUNT, new AttributeValue().withS(Long.toString(metrics_load_count)));
        item.put(METRICS_STORE_COUNT, new AttributeValue().withS(Long.toString(metrics_store_count)));
        item.put(COMPLEXITY, new AttributeValue().withN(Integer.toString(complexity)));

        return item;
    }
    private static int hashFunction(float window_x, float window_y, float window_width,  float window_height){
       return  Objects.hash(window_x, window_y, window_width, window_height);
    }

    public void CreateTable(){
        try {
            String tableName = TABLE_NAME;
            KeySchemaElement partion_key = new KeySchemaElement().withAttributeName(TABLE_PARTION_KEY).withKeyType(KeyType.HASH);
            KeySchemaElement sort_key = new KeySchemaElement().withAttributeName(TABLE_SORT_KEY).withKeyType(KeyType.RANGE);
            AttributeDefinition file_name = new AttributeDefinition().withAttributeName(TABLE_PARTION_KEY).withAttributeType(ScalarAttributeType.S);
            AttributeDefinition metrics_hash = new AttributeDefinition().withAttributeName(TABLE_SORT_KEY).withAttributeType(ScalarAttributeType.N);

            CreateTableRequest createTableRequest = new CreateTableRequest().withTableName(tableName)
                .withKeySchema(partion_key, sort_key)
                .withAttributeDefinitions(file_name,metrics_hash)
                .withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(1L).withWriteCapacityUnits(1L));

            // Create table if it does not exist yet
            TableUtils.createTableIfNotExists(dynamoDB, createTableRequest);
            // wait for the table to move into ACTIVE state
            TableUtils.waitUntilActive(dynamoDB, tableName);

            // Describe our new table
            DescribeTableRequest describeTableRequest = new DescribeTableRequest().withTableName(tableName);
            TableDescription tableDescription = dynamoDB.describeTable(describeTableRequest).getTable();
            System.out.println("Table Description: " + tableDescription);
        
        }catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which means your request made it "
                    + "to AWS, but was rejected with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with AWS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }catch (Exception e){
            System.out.println("Error: " );
            e.printStackTrace();
        }
        

    }

/*
    public static void main(String[] args) throws Exception {
        init();

        try {
            String tableName = "my-favorite-movies-table";

            // Create a table with a primary hash key named 'name', which holds a string
            CreateTableRequest createTableRequest = new CreateTableRequest().withTableName(tableName)
                .withKeySchema(new KeySchemaElement().withAttributeName("name").withKeyType(KeyType.HASH))
                .withAttributeDefinitions(new AttributeDefinition().withAttributeName("name").withAttributeType(ScalarAttributeType.S))
                .withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(1L).withWriteCapacityUnits(1L));

            // Create table if it does not exist yet
            TableUtils.createTableIfNotExists(dynamoDB, createTableRequest);
            // wait for the table to move into ACTIVE state
            TableUtils.waitUntilActive(dynamoDB, tableName);

            // Describe our new table
            DescribeTableRequest describeTableRequest = new DescribeTableRequest().withTableName(tableName);
            TableDescription tableDescription = dynamoDB.describeTable(describeTableRequest).getTable();
            System.out.println("Table Description: " + tableDescription);

            // Add an item
            Map<String, AttributeValue> item = newItem("Bill & Ted's Excellent Adventure", 1989, "****", "James", "Sara");
            PutItemRequest putItemRequest = new PutItemRequest(tableName, item);
            PutItemResult putItemResult = dynamoDB.putItem(putItemRequest);
            System.out.println("Result: " + putItemResult);

            // Add another item
            item = newItem("Airplane", 1980, "*****", "James", "Billy Bob");
            putItemRequest = new PutItemRequest(tableName, item);
            putItemResult = dynamoDB.putItem(putItemRequest);
            System.out.println("Result: " + putItemResult);

            // Scan items for movies with a year attribute greater than 1985
            HashMap<String, Condition> scanFilter = new HashMap<String, Condition>();
            Condition condition = new Condition()
                .withComparisonOperator(ComparisonOperator.GT.toString())
                .withAttributeValueList(new AttributeValue().withN("1985"));
            scanFilter.put("year", condition);
            ScanRequest scanRequest = new ScanRequest(tableName).withScanFilter(scanFilter);
            ScanResult scanResult = dynamoDB.scan(scanRequest);
            System.out.println("Result: " + scanResult);

        } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which means your request made it "
                    + "to AWS, but was rejected with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with AWS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }
    }

    private static Map<String, AttributeValue> newItem(String name, int year, String rating, String... fans) {
        Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
        item.put("name", new AttributeValue(name));
        item.put("year", new AttributeValue().withN(Integer.toString(year)));
        item.put("rating", new AttributeValue(rating));
        item.put("fans", new AttributeValue().withSS(fans));

        return item;
    }*/

}
