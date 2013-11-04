package com.hipitech.cache;

/**
 * 超时控制器
 * @author cloudqq@hipitech.com
 *
 * history:
 * 2010年01月19日
 * 修复在清理大量过期元素时，遇到的占用CPU过高的问题 
 *
 * 2010年07月21日
 * 修复在多线程并发读取时遇到的空指针问题， 在get时需要进行同步 
 *
 */

import java.lang.Runnable;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedList;
import java.util.List;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
	
/**
 * 超时控制器
 * 
 * @param <E>
 */
public class ExpireController<E> implements Runnable
{
	private static Logger logger = LoggerFactory.getLogger(ExpireController.class);
	
	private Map<String, ExpireObject<E>> caches = new HashMap<String, ExpireObject<E>>();
	
	private List<ExpireObject<E>> time_queue = new LinkedList<ExpireObject<E>>();
	
	/**
	 * 缓存过期时间
	 */
	private long expiredTime;
	
	private static final int EXPIRE_CLEAN_TIME = 10 * 1000; 
	
	private static Object lock = new Object();
	
	private static final int MAX_CACHE_NUMBER = 30 * 10000;
	
	/**
	 * 状态报告显示时间间隔
	 */
	private static final int STATUS_REPORT_TIME = 60 * 1000;
	
	
	private ExpiredEvent<E> expiredEventHandler = null;
	
	/**
	 * 统计命中次数
	 */
	private long countHit = 0;
	
	/**
	 * 统计查询次数
	 */
	private long countGet = 0;
	
	/**
	 * 统计运行时间
	 */
	private long countRun = 0;
	
	
	/**
	 * 统计超时
	 */
	private long countExpired = 0;
	
	
	
	private long startTime = 0;
	
	
	
	private String name ;

	public ExpireController(String name,long expiredTime, ExpiredEvent<E> event)
	{
		this.setExpiredTime(expiredTime);
		expiredEventHandler = event;
		
		if ((name != null) && (!name.equals("")))
		{
			this.name = name;
		}
		else this.name = "default-expire-controller";
		
		logger.info("["+this.name+"]" + " SETT " + this.getExpiredTime() + " ExpireController Created.");
		
		this.startTime = System.currentTimeMillis();
	}
	
	/**
	 * 设定超时时间
	 * @param expiredTime
	 */
	public ExpireController(long expiredTime, ExpiredEvent<E> event)
	{
		this("", expiredTime, event);
	}
	
	
	
	@SuppressWarnings("unchecked")
	public void run() {
		
		long last_access_time = System.currentTimeMillis();
		
		int clean_count;
		
		while(true)
		{
			while( System.currentTimeMillis() - last_access_time > EXPIRE_CLEAN_TIME)
			{
				clean_count = 0;
				while (time_queue.size() > 0)
				{
					ExpireObject<E> expiredobject = time_queue.get(0);
					
					if(expiredobject.isExpired())
					{
						synchronized(lock)
						{
							clean_count ++;
							
							if ((!expiredobject.removed) && (expiredEventHandler != null))
							{
								if (expiredEventHandler instanceof ExpiredEvent)
								{
									expiredEventHandler.onExprie(expiredobject.object);
								}
							}
							
							if (!expiredobject.removed)
								caches.remove( expiredobject.key);
							
							time_queue.remove(0);
							
							logger.debug("["+this.name+"]" + "EXPIRED REMOVE [" + expiredobject.key + "]");
						}
					}
					else
					{
						if (clean_count > 0)
						{
							logger.debug("["+this.name+"]" + " " + "BATCH REMOVED " + clean_count);
							
							this.countExpired += clean_count;
						}
						
						break;
					}

					try {
					
						Thread.sleep(1);

					} catch( InterruptedException e){
						logger.error(e.getMessage());
						e.printStackTrace();
					}

				}
				last_access_time = System.currentTimeMillis();
			}
			
			try {
				Thread.sleep(1);
				
				this.countRun = System.currentTimeMillis() - this.startTime;
				/**
				 * 打印缓存状态信息
				 */
				if (this.countRun % (STATUS_REPORT_TIME) == 0)
				{
					float t = (float) (this.countRun / 1000.0);
					
					float h = 0;
					
					DecimalFormat df = new DecimalFormat("0.00");
					if (countGet != 0)
					{
						h = (float)countHit / (float)countGet * (float)100.0;
					}
						
					logger.info("["+this.name+"] STATUS:" + 
							" SETT " + this.getExpiredTime() +
							" TIME " + Math.ceil(t) + 
							" SIZE " + this.size()  +
							" GET  " + this.countGet+
							" HIT  " + this.countHit+
							" EXP  " + this.countExpired +
							" HITR " + df.format(h)  +"%"   
					);
				}
				
			} catch (InterruptedException e) {
				logger.error(e.getMessage());
				e.printStackTrace();
			}
		}
		
	}
	
	/**
	 * 存放对象
	 * @param key
	 * @param e
	 */
	public void put(String key, E e)
	{
		
		synchronized(lock){
			if( !caches.containsKey(key))
			{
				ExpireObject<E> exprieobject = new ExpireObject<E>(key,e,this.getExpiredTime());
				caches.put(key, exprieobject);
				
				time_queue.add(exprieobject);
				
				if (time_queue.size() > MAX_CACHE_NUMBER) 
					logger.warn("["+this.name+"] " + "ExpiredController cached number exceeded max limit " + MAX_CACHE_NUMBER + " !!!");
				
				logger.debug("["+this.name+"]" + " ExpiredController " + "total cached item count: " + time_queue.size());
			}
			else
			{
				logger.warn("["+this.name+"]" + "ExpiredController put key is exits key: " + key +" ");
			}
		}
	}
	
	/**
	 * 获取对象， 并从缓存清除
	 * @param key
	 * @return
	 */
	public E take(String key)
	{
		return get(key, true);
	}
	/**
	 * 获取对象, 若不为空，则表示在时效内， 否则为因超时而失效的对象
	 * @param key
	 * @return
	 */
	public E get(String key, boolean remove)
	{
		E result = null;
		
		
		/** 统计信息 **/
		
		this.countGet ++;
		
		/** 统计信息**/

		synchronized(lock){

			if (caches.containsKey(key))
			{


				ExpireObject<E> expireobject = (ExpireObject<E>) caches.get(key);
				
				result = expireobject.object;
				
				if (remove)
				{
					expireobject.setExpired(true);
					expireobject.setRemoved(true);

					caches.remove(key);
				}
				else if (System.currentTimeMillis() - expireobject.createtime > this.getExpiredTime() )
				{
					expireobject.setExpired(true);
					
					caches.remove(key);
				}
			}
		}
		
		/** 记录缓存命中率 **/
		if (result != null) 
		{
			logger.debug("["+this.name+"] CACHE HIT ["+key+"]");
			this.countHit ++;
		}
		else
		{
			logger.debug("["+this.name+"] CACHE MISS ["+key+"]");
		}
		
		return result;
	}
	
	public int size()
	{
		return caches.size();
	}
	
	public void setExpiredTime(long expiredTime) {
		this.expiredTime = expiredTime;
	}

	public long getExpiredTime() {
		return expiredTime;
	}
	
	
	@SuppressWarnings("hiding")
	class ExpireObject<E>
	{
		private long createtime;
		
		private E object = null;
		
		private boolean expired = false;
		
		private String  key;
		
		private boolean removed = false;
		
		private long expiredTime = 0;
		
		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}
		
		
		public boolean isRemoved() {
			return removed;
		}

		public void setRemoved(boolean removed) {
			this.removed = removed;
		}

		public boolean isExpired() {
			if (!expired)
			{
				expired =  (System.currentTimeMillis() - createtime) > this.expiredTime;
			}
			return expired;
		}

		public void setExpired(boolean expired) {
			this.expired = expired;
		}

		public ExpireObject(String key, E o, long expiredTime)
		{
			createtime = System.currentTimeMillis();
			object = o;
			this.key = key;
			this.expiredTime = expiredTime;
		}
	}
	
	
	public static void main(String[] args)
	{
//		ExpireController<Object> controller = new ExpireController<Object>(5 * 1000, new TestExpiredEventHandler<Long>() );
//		
//		new Thread(controller).start();
//		
//		long count = 0l;
//		while(count < 500000)
//		{
//			controller.put(String.valueOf(count), new Long(count));
//			count++;
//			
//			try {
//				Thread.sleep(500);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
//		
//		try {
//			System.in.read();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		
	}


}
