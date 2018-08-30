package org.vimukt.test;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import org.vimukt.ec2.EC2Management;

import com.amazonaws.services.ec2.AmazonEC2;

public class EC2ManagementTest {
	
	

	private EC2Management instance = new EC2Management() ;
	private String securityGroupName = "vimukt-SG" ;
	private String securityGroupDesc = "This is my security group" ;
	private String keyName ="vt-key" ;
	private String instanceId = " " ;
	private AmazonEC2 ec2Client ;
    private SoftAssert softAssert = new SoftAssert();
	@Test
	public void manageEC2Instance () {
	
		//This is to create and configure the security group 
		//one can also use existing security group in that you need to comment out this step.
		ec2Client = instance.createNConfigureSecurityGroup(securityGroupName, securityGroupDesc);
		
		//create an EC2 instance 
		instanceId = instance.startAnInstance(keyName, securityGroupName);
		
		//Validating instance created successfully.
		Assert.assertNotNull(instanceId);
		
		//checking is instance running!
		boolean status = instance.isRunning(instanceId);
		softAssert.assertTrue(status);
		
		// tear-down(terminate) instance
		instance.terminateAnEC2Instance(instanceId);
		
		
		//delete of security group if created in above step.
		boolean sgDeleted = instance.deleteSecurityGrp(securityGroupName);
		softAssert.assertTrue(sgDeleted);
		
		softAssert.assertAll();
	}
	
}
