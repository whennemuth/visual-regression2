package bu.ist.visreg.basket.s3;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.stream.Stream;

import bu.ist.visreg.util.ArgumentParser;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.CreateBucketConfiguration;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.ListBucketsRequest;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;

public class S3Bucket {

	private String bucketName;
	private Region region = Region.US_EAST_1;
	private boolean create;
	private Map<String, S3Object> s3Objects = new HashMap<String, S3Object>();

	@SuppressWarnings("unused")
	private S3Bucket() { /* Restrict default constructor */ }
	
	public S3Bucket(String bucketName, Region region, boolean create) throws Exception {
		if(region != null) {
			this.region = region;
		}
		this.bucketName = bucketName;
		this.create = create;
		
		inventory();
	}
	
	public S3Bucket(String bucketName, boolean create) throws Exception {
		this(bucketName, null, create);
	}
	
	/**
	 * Query S3 for a listing of all objects in the specified bucket and load the results into an array.
	 * @throws Exception 
	 */
	private void inventory() throws Exception {
        try {
            S3Client s3 = S3Client.builder().region(region).build();

            ListObjectsRequest listObjects = ListObjectsRequest
                    .builder()
                    .bucket(bucketName)
                    .build();

            ListObjectsResponse res = s3.listObjects(listObjects);
            List<S3Object> objects = res.contents();

            for (ListIterator<S3Object> iterVals = objects.listIterator(); iterVals.hasNext(); ) {
                S3Object s3Obj = (S3Object) iterVals.next();
                s3Objects.put(s3Obj.key(), s3Obj);
            }
        }
        catch (S3Exception e) {
            if("NoSuchBucket".equalsIgnoreCase(e.awsErrorDetails().errorCode()) && create) {
            	createBucket();
            }
            else if("AuthorizationHeaderMalformed".equalsIgnoreCase(e.awsErrorDetails().errorCode())) {
	        	/**
	        	 * Due to a bug, attempts to list objects on a bucket that does not exist in the account, but whose
	        	 * name is taken by someone else out there on the internet will not cause a "NoSuchBucket" exception.
	        	 * Instead, some sort of malformed authorization header exception is thrown with a complaint 
	        	 * that an unexpected region was supplied with a different region expected. Therefore, we check for
	        	 * the existence of the bucket here.
	        	 */
            	throw new Exception("Bucketname taken! Someone on the internet already has a bucket called " + bucketName, e);
            }
        }	    
	}
	
	private boolean bucketExists() {
        S3Client s3 = S3Client.builder().region(region).build();
        ListBucketsRequest listBucketsRequest = ListBucketsRequest.builder().build();
        ListBucketsResponse listBucketsResponse = s3.listBuckets(listBucketsRequest);        
        return listBucketsResponse.buckets().stream().anyMatch(bucket -> bucket.name().equals(bucketName));
	}
	
	private void createBucket() throws Exception {
		S3Client s3 = S3Client.builder().region(region).build();
		CreateBucketRequest request = null;
		/**
		 * Per: https://docs.aws.amazon.com/AmazonS3/latest/API/API_GetBucketLocation.html#API_GetBucketLocation_ResponseSyntax
		 * A region is not something that can be indicated as a location constraint unless it falls within the following list
		 * of regions:
		 */
		if(Arrays.asList(new Region[] {
			Region.EU_WEST_1,
			Region.US_WEST_1,
			Region.US_WEST_2,
			Region.AP_SOUTHEAST_1,
			Region.AP_SOUTHEAST_2,
			Region.AP_NORTHEAST_1,
			Region.SA_EAST_1,
			Region.CN_NORTH_1,
			Region.EU_CENTRAL_1}).stream().anyMatch(r -> r.equals(region))) {
			
			CreateBucketConfiguration bc = CreateBucketConfiguration.builder()
			    .locationConstraint(region.id())
			    .build();
			
			request = CreateBucketRequest
			    .builder()
			    .bucket(bucketName)
			    .createBucketConfiguration(bc)
			    .build();			
		}
		else {	
			request = CreateBucketRequest
			    .builder()
			    .bucket(bucketName)
			    .build();			
		}
		
		s3.createBucket(request);
	}
	
	public boolean hasInventory() {
		return ! s3Objects.isEmpty();
	}
	
	public void createSubfolder(String folderpath) throws Exception {		
		if( ! folderpath.endsWith("/")) {
			folderpath = folderpath + "/";
		}
        upload(folderpath, new byte[0]);
	}
	
	public void uploadFileFromString(String pathname, String content) throws Exception {
		upload(pathname, content.getBytes());		
	}
	
	private void upload(String pathname, byte[] bytes) throws Exception {
		S3Client s3 = S3Client.builder().region(region).build();

        PutObjectRequest request = PutObjectRequest.builder()
        	.bucket(bucketName)
        	.key(pathname)
        	.build();
        		 
		s3.putObject(request, RequestBody.fromBytes(bytes));
	}
	
	public String getBucketName() {
		return bucketName;
	}

	public Region getRegion() {
		return region;
	}

	public Map<String, S3Object> getS3Objects() {
		return s3Objects;
	}
	
	public S3Object getS3Object(String key) {
		return s3Objects.get(key);
	}
	
	public S3Object getSubFolder(String folderName) {
		if(folderName.endsWith("/")) {
			folderName = folderName + "/";
		}
		return getS3Object(folderName);
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder(bucketName);
		s.append("\n");
		for(S3Object s3obj : s3Objects.values()) {
			s.append(s3obj).append("\n");
		}
		return s.toString();
	}

	/**
	 * Accomodate looser entries for the region and still be able to match up the entry to the corresponding Region enumerator.
	 * Example: "us-east-1" can be used to match up to Region.US_EAST_1 
	 * @param region
	 * @return
	 */
	public static Region getRegion(String region) {
		return Region.of(region.replaceAll("[^a-zA-Z\\d_]", "_").toUpperCase());
	}
	
	public static void listBuckets() {
		Region region = Region.US_EAST_1; 
		S3Client s3 = S3Client.builder().region(region).build();
		ListBucketsRequest listBucketsRequest = ListBucketsRequest.builder().build();
		ListBucketsResponse listBucketsResponse = s3.listBuckets(listBucketsRequest);
		listBucketsResponse.buckets().stream().forEach(x -> System.out.println(x.name()));
	}
	
	/**
	 * Assuming the S3 bucket already exists, download a listing of all its contents.
	 * 
	 * @param args
	 * @return
	 * @throws Exception 
	 */
    public static S3Bucket parseArgs(String[] args) throws Exception {
    	S3Bucket bucket = null;    	
    	ArgumentParser parser = new ArgumentParser(args);
    	boolean createIfNotExists = parser.getBoolean("c|create");
    	if(parser.has(new String[] { "r|region", "b|bucket"})) {
    		bucket = new S3Bucket(
				parser.getString("bucket"),
				getRegion(parser.getString("region")), 
				createIfNotExists);    		
    	}
    	else if(parser.has("b|bucket")) {
    		bucket  = new S3Bucket(parser.getString("bucket"), createIfNotExists);
    	}
    	else {
    		System.err.println("Expected 1 or 2 arguments!");
    		System.exit(1);
    	}
    	return bucket;
    }

    public static void main(String[] args) throws Exception {
    	S3Bucket bucket = parseArgs(args);
    	System.out.print(bucket);
    }
}
