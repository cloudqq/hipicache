package com.hipitech.cache;

import junit.framework.TestCase;
import java.lang.Runnable;


/*
 * bug：  multi thread conflict
 * 
*/
public class Test_issue_2010_07_21 extends TestCase {
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
		long last_access_time = System.currentTimeMillis();

		while(true)
		{
			controller.put(new Long(idx).toString(), "aaaa");
			idx++;

			if (idx >= 200000) break;
		}

		long result = System.currentTimeMillis() - last_access_time;

		System.out.println( "write qps: " + (idx / (result /1000.0)));


		idx = 0;

		last_access_time = System.currentTimeMillis();

		while (idx <= 200000)
		{
			String s = controller.take(new Long(idx).toString());
			idx++;
		}

		result = System.currentTimeMillis() - last_access_time;

		System.out.println("read qps: " + (200000.0 / (result / 1000.0)));
	}


	public void test002(){


		new Thread( new Writer("a", this.controller)).start();

		new Thread( new Writer("b", this.controller)).start();
		new Thread( new Writer("c", this.controller)).start();
	//	new Thread( new Writer("d", this.controller)).start();
	//	new Thread( new Writer("e", this.controller)).start();


		try
		{
			Thread.sleep(300);
		} catch(Exception e)
		{
		}

		new Thread(new Reader("a",this.controller)).start();
		new Thread(new Reader("a",this.controller)).start();
		new Thread(new Reader("a",this.controller)).start();
		new Thread(new Reader("a",this.controller)).start();
		new Thread(new Reader("c",this.controller)).start();
		new Thread(new Reader("c",this.controller)).start();
		new Thread(new Reader("b",this.controller)).start();
		new Thread(new Reader("b",this.controller)).start();

//		new Thread(new Reader("b",this.controller)).start();
////		new Thread(new Reader("c",this.controller)).start();
//		new Thread(new Reader("d",this.controller)).start();
//		new Thread(new Reader("e",this.controller)).start();

		long last = System.currentTimeMillis();

		while (true)
		{
			try{
				Thread.sleep(1000);
				System.out.println("controller.size() " + this.controller.size());
			}catch(Exception e)
			{
			}


			if (System.currentTimeMillis() - last > (60 * 1000 * 10))
			{
				break;
			}
		}


		System.out.println("Test complete");
	}


	class Writer implements Runnable {
		private ExpireController<String> controller;

		private String tag;
		
		public Writer(String t, ExpireController<String> c)
		{
			this.controller = c;
			this.tag = t;
		}


		public void run() {
			long count = 0;
			
			while(this.controller.size() < 300000)
			{
				this.controller.put(this.tag+ new Long(count).toString(), "aaaa"); 

				count++;

				try{
					Thread.sleep(1);

				} catch(Exception e)
				{
					
				}
			}

			System.out.println("Write complete");
		}
	}

	class Reader implements Runnable{
		private ExpireController<String> controller;
		private String tag;

		public Reader(String t , ExpireController<String> c)
		{
			this.controller = c;
			this.tag = t;
		}

		public void run() {
			String result = "";
			long count = 0;

			while(this.controller.size() > 0){
				result = this.controller.get(this.tag + new Long(count).toString(),true);	

//				System.out.println("reader "  + this.tag + new Long(count).toString() +" " + this.getId());
				count++;
				try{
					Thread.sleep(10);

				} catch(Exception e)
				{
					
				}
			}

			System.out.println("Reader " + count);
		}
	}
}
