//Import libraries
import com.Img.recognition.Label;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.AmazonRekognitionException;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.S3Object;
import com.amazonaws.services.rekognition.model.DetectTextRequest;
import com.amazonaws.services.rekognition.model.DetectTextResult;
import com.amazonaws.services.rekognition.model.TextDetection;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import java.io.FileWriter;
import java.io.File;
import java.io.FileNotFoundException;

public class Text {

	public static void main(String[] args)throws Exception {

		String bucket = "bucketname";
		String myqueue = "queuename";
		// Read credentials from cre.txt file 		
		HashMap<String, String> credentials = new HashMap<String, String>();
		try {
			File myObj = new File("cre.txt");
			Scanner myReader = new Scanner(myObj);
			while (myReader.hasNextLine()) {
				String line = myReader.nextLine();
				if(line != "") {
					String[] data = line.split(" = ");
					if(data.length == 2) {
						credentials.put(data[0], data[1]);  
					}   
				}
			}
myReader.close();
		} catch (FileNotFoundException e) {
			System.out.println("Credentials file can not be read.");
			e.printStackTrace();
		}

// Create clients for AWS services using Session Credentials that read from the file in previous step
		BasicSessionCredentials  creds = new BasicSessionCredentials (credentials.get("accesskeyid"),credentials.get("secretaccesskey"),credentials.get("sessiontoken")); 
		AmazonRekognition rekognitionClient = AmazonRekognitionClientBuilder.standard()
				.withCredentials(new AWSStaticCredentialsProvider(creds))
				.withRegion(Regions.US_EAST_1).build();
		AmazonSQS sqs = AmazonSQSClientBuilder.standard()
				.withCredentials(new AWSStaticCredentialsProvider(creds))
				.withRegion(Regions.US_EAST_1).build();

// Retrieve image information from SQS and detect text in the images
		final ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(
				myqueue);
		receiveMessageRequest.setMaxNumberOfMessages(10);
		FileWriter FileWrite = new FileWriter("output.txt", false);
final ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(
				myqueue);
		receiveMessageRequest.setMaxNumberOfMessages(10);
		FileWriter FileWrite = new FileWriter("output.txt", false);
		while (true) {
			List<Message> messages = sqs.receiveMessage(receiveMessageRequest).getMessages();
			for ( Message message : messages) {
				if (message.getBody().contains("-1") == true) {
					String messageReceiptHandle = message.getReceiptHandle();
					sqs.deleteMessage(new DeleteMessageRequest(
							myqueue, messageReceiptHandle));
					FileWrite.close();
					System.exit(0);
