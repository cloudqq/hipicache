package com.hipitech.cache;

/**
 * ����ί�нӿ�
 * @author cloudqq@hipitech.com
 *
 * @param <E>
 */
public interface ExpiredEvent<E> {
	
	/**
	 * ����ʱʱ����
	 * @param e
	 */
	void onExprie(E e);
}
