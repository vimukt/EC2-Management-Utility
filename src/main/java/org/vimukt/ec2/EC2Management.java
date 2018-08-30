package org.vimukt.ec2;

import java.util.Arrays;

import org.vimukt.utility.ReadConfig;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
import com.amazonaws.services.ec2.model.CreateKeyPairRequest;
import com.amazonaws.services.ec2.model.CreateKeyPairResult;
import com.amazonaws.services.ec2.model.CreateSecurityGroupRequest;
import com.amazonaws.services.ec2.model.CreateSecurityGroupResult;
import com.amazonaws.services.ec2.model.DeleteSecurityGroupRequest;
import com.amazonaws.services.ec2.model.DeleteSecurityGroupResult;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.InstanceState;
import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.IpRange;
import com.amazonaws.services.ec2.model.RebootInstancesRequest;
import com.amazonaws.services.ec2.model.RebootInstancesResult;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;

public class EC2Management {
	private AmazonEC2 ec2Client;
	private AWSCredentials credentials;
	private RunInstancesRequest runInstancesRequest;
	private CreateSecurityGroupRequest createSecurityGroupRequest;
	private DeleteSecurityGroupRequest deleteSecurityGroupRequest;
	private IpRange ipRange;
	private IpPermission ipPermission;
	private CreateSecurityGroupResult createSecurityGroupResult;
	private AuthorizeSecurityGroupIngressRequest authorizeSecurityGroupIngressRequest;
	private CreateKeyPairRequest createKeyPairRequest;
	private CreateKeyPairResult createKeyPairResult;
	private StopInstancesRequest stopInstancesRequest;
	private DescribeInstancesRequest describeInstanceRequest;
	private DescribeInstancesResult describeInstanceResult;
	private String keyName = " ";
	private TerminateInstancesRequest terminateInstancesRequest;

	/*
	 * This method will return AWSCredentials object. It will read the key & secret
	 * from property file (under conf/credntials).
	 * 
	 */
	private AWSCredentials getCrendentialsObject() {
		credentials = new BasicAWSCredentials(ReadConfig.getValue("aws_access_key_id"),
				ReadConfig.getValue("aws_secret_access_key"));
		return credentials;
	}

	/*
	 * This method will return AmazonEC2 object.
	 */
	private AmazonEC2 getEC2ClientObject() {
		return ec2Client = AmazonEC2ClientBuilder.standard()
				.withCredentials(new AWSStaticCredentialsProvider(getCrendentialsObject()))
				.withRegion(Regions.US_EAST_1).build();
	}

	/*
	 * This method will create & configure a security group. Security groups control
	 * the network traffic to our EC2 instances. We’re able to use one security
	 * group for several EC2 instances. Since security groups don’t allow any
	 * network traffic by default. We’ll have to configure our security group to
	 * allow traffic. in this we are allowing traffic from any IP.
	 * 
	 * @param name
	 * 
	 * @param description
	 */
	public AmazonEC2 createNConfigureSecurityGroup(String name, String description) {
		createSecurityGroupRequest = new CreateSecurityGroupRequest().withGroupName(name).withDescription(description);
		ec2Client = getEC2ClientObject();
		createSecurityGroupResult = ec2Client.createSecurityGroup(createSecurityGroupRequest);

		ipRange = new IpRange().withCidrIp("0.0.0.0/0");
		ipPermission = new IpPermission().withIpv4Ranges(Arrays.asList(new IpRange[] { ipRange })).withIpProtocol("tcp")
				.withFromPort(80).withToPort(80);

		authorizeSecurityGroupIngressRequest = new AuthorizeSecurityGroupIngressRequest().withGroupName(name)
				.withIpPermissions(ipPermission);
		ec2Client.authorizeSecurityGroupIngress(authorizeSecurityGroupIngressRequest);

		return ec2Client;
	}

	/*
	 * This method will delete a security group. Security groups control the network
	 * traffic to our EC2 instances.
	 * 
	 * @param name
	 */
	public Boolean deleteSecurityGrp(String groupId) {

		deleteSecurityGroupRequest = new DeleteSecurityGroupRequest().withGroupName(groupId);
		try {
			ec2Client.deleteSecurityGroup(deleteSecurityGroupRequest);
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			return false;
		}
		return true;
	}

	public AmazonEC2 genrateKey(String keyName) {
		this.keyName = keyName;
		// Generating Key :
		createKeyPairRequest = new CreateKeyPairRequest().withKeyName(keyName);
		createKeyPairResult = ec2Client.createKeyPair(createKeyPairRequest);
		return ec2Client;
	}
	/*
	 * When launching an EC2 instance, we need to specify a key pair. We can create
	 * a key pair using the SDK. To create the EC2, we’ll use a RunInstancesRequest.
	 * Image Id is the AMI image that this instance will use. An instance type
	 * defines the specifications of the instance. On execute the request using the
	 * runInstances() method we can retrieve the id of the instance created.
	 * 
	 * @param ec2Client
	 * 
	 * @param secGroupName
	 * 
	 * @param keyName
	 */

	public String startAnInstance(String keyName, String secGroupName) {

		runInstancesRequest = new RunInstancesRequest().withImageId("ami-97785bed").withInstanceType("t2.micro")
				.withKeyName(keyName).withMinCount(1).withMaxCount(1).withSecurityGroups(secGroupName);
		return ec2Client.runInstances(runInstancesRequest).getReservation().getInstances().get(0).getInstanceId();
	}

	/*
	 * This method will stop an instance.
	 * 
	 * @param instanceId
	 */
	public boolean stopAnEC2Instance(String instanceId) {
		stopInstancesRequest = new StopInstancesRequest().withInstanceIds(instanceId);
		ec2Client.stopInstances(stopInstancesRequest);
		Integer instanceState = -1;
		while (instanceState != 80) { // Loop until the instance is in the "running" state.
			instanceState = getInstanceStatus(instanceId);
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				return false;
			}
		}
		return true;
	}

	/*
	 * This method will terminate an instance.
	 * 
	 * @param instanceId
	 */
	public boolean terminateAnEC2Instance(String instanceId) {
		terminateInstancesRequest = new TerminateInstancesRequest().withInstanceIds(instanceId);
		ec2Client.terminateInstances(terminateInstancesRequest);
		Integer instanceState = -1;
		while (instanceState != 48) { // Loop until the instance is in the "running" state.
			instanceState = getInstanceStatus(instanceId);
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				return false;
			}
		}
		return true;

	}

	/*
	 * This method will return the instance status.
	 * 
	 * @param instanceId The code that is returned will be an Integer that matches
	 * one of the following 0 : pending 16 : running 32 : shutting-down 48 :
	 * terminated 64 : stopping 80 : stopped
	 */
	public Integer getInstanceStatus(String instanceId) {
		describeInstanceRequest = new DescribeInstancesRequest().withInstanceIds(instanceId);
		describeInstanceResult = ec2Client.describeInstances(describeInstanceRequest);
		InstanceState state = describeInstanceResult.getReservations().get(0).getInstances().get(0).getState();
		return state.getCode();
	}

	public boolean isRunning(String instanceId) {
		Integer instanceState = -1;
		while (instanceState != 16) { // Loop until the instance is in the "running" state.
			instanceState = getInstanceStatus(instanceId);
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				return false;
			}
		}
		return true;
	}

}
