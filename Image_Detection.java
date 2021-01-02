//Import libraries
package com.Img.recognition;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.AmazonRekognitionException;
import com.amazonaws.services.rekognition.model.DetectLabelsRequest;
import com.amazonaws.services.rekognition.model.DetectLabelsResult;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.S3Object;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;

public class Label {

	public static void main(String[] args)throws Exception {

		String bucket = "bucketname";
		String myqueue = "queuename";
		// Read credentials from credential file
		HashMap<String, String> credentials = new HashMap<String, String>();
		try {
			File myObj = new File("cred.txt");
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
		BasicSessionCredentials  creds = new BasicSessionCredentials (credentials.get("access_key_id"),credentials.get("secret_access_key"),credentials.get("session_token")); 
		AmazonRekognition rekognitionClient = AmazonRekognitionClientBuilder.standard()
				.withCredentials(new AWSStaticCredentialsProvider(creds))

				.withRegion(Regions.US_EAST_1).build();
		AmazonSQS sqs = AmazonSQSClientBuilder.standard()
				.withCredentials(new AWSStaticCredentialsProvider(creds))
				.withRegion(Regions.US_EAST_1).build();

//Read images from S3 bucket and detect labels using AWS recognition
// Send image ids when car is detected in an image through AWS SQS
for (int i = 1; i <=10 ; i++) {
			DetectLabelsRequest request = new DetectLabelsRequest()
					.withImage(new Image()
							.withS3Object(new S3Object()
									.withName(java.lang.String.format("%d.jpg",i)).withBucket(bucket)))
					.withMaxLabels(10)
					.withMinConfidence(90F);

			try {
				DetectLabelsResult Labelsresult = rekognitionClient.detectLabels(request);
				List<com.amazonaws.services.rekognition.model.Label> labels = Labelsresult.getLabels();

				for (com.amazonaws.services.rekognition.model.Label label: labels) {
					System.out.println(label.getName() + ": " + label.getConfidence().toString());
					if (label.getName().equals("Car") && label.getConfidence() >= 90) {
						System.out.println("Car detected: "); //+ current.getKey());
						com.amazonaws.services.sqs.model.SendMessageRequest sendMessageRequest = new com.amazonaws.services.sqs.model.SendMessageRequest(
								myqueue,java.lang.String.format("%d.jpg",i) );
						sendMessageRequest.setMessageGroupId("messageGroup1");
						com.amazonaws.services.sqs.model.SendMessageResult sendMessageResult = sqs
								.sendMessage(sendMessageRequest);
					}
				}
			} catch(AmazonRekognitionException e) {
				e.printStackTrace();
			}
		}
		com.amazonaws.services.sqs.model.SendMessageRequest sendMessageRequest = new com.amazonaws.services.sqs.model.SendMessageRequest(
				myqueue, "-1");
		sendMessageRequest.setMessageGroupId("messageGroup1");
		sendMessageRequest.setMessageDeduplicationId("-1");
		com.amazonaws.services.sqs.model.SendMessageResult sendMessageResult = sqs
				.sendMessage(sendMessageRequest);
	}
}

