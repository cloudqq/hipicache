package com.hipitech.cache;
/*
 * 
 * 增加测试项目
 *
*/

public class TestApp {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		ExpireController<String> controller = new ExpireController<String>("test-cache", 2* 60 * 1000, 
				new ExpiredEvent<String>() {

					public void onExprie(String e) {
						System.out.println("EXPIRED");
						
					}
				}
		);
		
		
		ExpireController<String> controller2 = new ExpireController<String>("test-cache2", 3* 60 * 1000, 
				new ExpiredEvent<String>() {

					public void onExprie(String e) {
						System.out.println("EXPIRED");
						
					}
				}
		);
		
		new Thread(controller2).start();
		
		new Thread(controller).start();
		
		
		for (int i=0;i<1000;i++)
		{
			try {
				Thread.sleep(5);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			controller.put(new Integer(i).toString(), new Integer(i).toString());
		}
		
		try {
			Thread.sleep(20 * 1000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		
		for (int i=0;i<1000;i++)
		{
			int r = (int) Math.floor(	Math.random() * i );
			
			String v = controller.get(new Integer(r).toString(), true);
			
			try {
				Thread.sleep(250);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
		
		while (true)
		{
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {

				e1.printStackTrace();
			}
			
		}
		
	}

}
