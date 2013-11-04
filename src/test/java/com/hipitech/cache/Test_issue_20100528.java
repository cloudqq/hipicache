package com.hipitech.cache;

import junit.framework.TestCase;

/*
 *
 * 
 *
*/
public class Test_issue_20100528 extends TestCase {
	private ExpireController<String> controller;

	public void setUp(){
		controller = new ExpireController<String>(3600000,
				 new ExpiredEvent<String>() {
					public void onExprie(String e) {
						System.out.println("expire"+e);	
					}
				 }
				);
		new Thread(controller).start();
	}

	public void tearDown(){
	}	

	public void test001(){
		long idx = 0;
		while(true)
		{
			long last_access_time = System.currentTimeMillis();
			try{
				for (int i=0;i<100;i++)
				{
					idx++;
					controller.put(new Long(idx).toString(),new java.util.Date().toString());
				}

				System.out.println("total: " + new Long(idx).toString());
				
				Thread.sleep(5000);

			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
}
