package com.hipitech.cache;

/**
 * 对象委托接口
 * @author cloudqq@hipitech.com
 *
 * @param <E>
 */
public interface ExpiredEvent<E> {
	
	/**
	 * 对象超时时触发
	 * @param e
	 */
	void onExprie(E e);
}
