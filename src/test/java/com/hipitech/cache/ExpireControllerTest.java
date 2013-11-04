package com.hipitech.cache;

import junit.framework.TestCase;

public class ExpireControllerTest extends TestCase {
	
	/**
	 * test basic put and get
	 */
	public void test_put_and_get()
	{
		ExpireController<String> controller = new ExpireController<String>(5 * 1000, 
				 new ExpiredEvent<String>() {
					public void onExprie(String e) {
						
					}
				 }
				);
		
		String key = "12345";
		String value = "value-12345";
		
		controller.put(key, value);
		
		assertEquals(  controller.take(key) == value,true );
		
	}
	
	/**
	 * test put and take
	 */
	public void test_put_and_take()
	{
		ExpireController<String> controller = new ExpireController<String>(5 * 1000, 
				 new ExpiredEvent<String>() {
					public void onExprie(String e) {
						
					}
				 }
				);
		
		String key = "12345";
		String value = "value-12345";
		
		controller.put(key, value);
		
		assertEquals(1, controller.size());
		
		assertEquals(  controller.take(key) == value,true );
		
		assertEquals(0, controller.size());
		
	}
	
	/**
	 * test the cache limit
	 */
	public void test_cache_limit()
	{
//		ExpireController<Integer> controller = new ExpireController<Integer>(5 * 1000, 
//				 new ExpiredEvent<Integer>() {
//					public void onExprie(Integer e) {
//						
//					}
//				 }
//				);
//		
//		for (int i = 0;i<300005;i++)
//		{
//			controller.put(new Integer(i).toString() , new Integer(i));
//		}
//		
//		assertEquals(controller.size(), 300005);
	}
	
	/**
	 * on expire verify
	 */
	public void test_expire_verify()
	{
		ExpireController<Integer> controller = new ExpireController<Integer>(3* 1000, 
		 new ExpiredEvent<Integer>() {
			public void onExprie(Integer e) {
				System.out.println("expired");
			}
		 }
		);
		
		controller.put("aaa", 12);
		
		new Thread(controller).start();
		
		try {
			Thread.sleep(12 * 1000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		assertEquals(0, controller.size());
	}
	
	/**
	 * test put and get
	 */
	public void test_put_get()
	{
		ExpireController<String> controller = new ExpireController<String>(5 * 1000, 
				 new ExpiredEvent<String>() {
					public void onExprie(String e) {
						
					}
				 }
				);
		
		String key = "12345";
		String value = "value-12345";
		
		controller.put(key, value);
		
		assertEquals(1, controller.size());
		
		assertEquals(  controller.get(key, false) == value,true );
		
		assertEquals( 1 ,controller.size());
		
		assertEquals(  controller.get(key,true) == value, true );
		
		assertEquals(0, controller.size());
		
	}
	
	public void test_cpu_high()
	{
		ExpireController<String> controller = new ExpireController<String>(1000,
						      new ExpiredEvent<String>() {
							public void onExprie(String e) {
								System.out.println("expired");
							}

						      }
						  );

		int test_count = 100000;

		for(int i = 0; i<test_count;i++) {
			controller.put(new Integer(i).toString(), new Integer(i).toString());
		}

		System.out.println("please check the top.");

		new Thread(controller).start();


		try {

			Thread.sleep(5*1000);
		} catch(InterruptedException e)
		{
			System.out.println(e);
		}

		System.out.println("test cpu high complete");

	}


		
}
