package com.w1.merchant.android.extra;

import android.support.v4.view.ViewPager;
import android.util.Log;

import com.thehayro.view.InfiniteViewPager;

public abstract class InfiniteViewPagerListener implements InfiniteViewPager.OnInfinitePageChangeListener {
	private static final String TAG = InfiniteViewPagerListener.class.getSimpleName();
	
	private float prevOffset = 0;
	private int currentPosition = -1;
	private int prevPosition = -1;
	
	private int state = ViewPager.SCROLL_STATE_IDLE;
	private final int totalCount;
	
	private int startPosition = -1;
	
	public abstract void onPageSwitched(int position);
	
	public InfiniteViewPagerListener(int totalCount) {
		this.totalCount = totalCount;
	}
	
	@Override
	public void onPageSelected(Object indicator) { }
	
	@Override
	public void onPageScrolled(Object indicator, float positionOffset,
			int positionOffsetPixels) {
		//int totalCount = viewPager.getAdapter().getCount();
		Integer integer = (Integer) indicator;
    	int position = integer % totalCount;
    	if(position < 0) {
    		position = totalCount + position;
    	}
		//Log.d(TAG, indicator + " " + position);
		if(state == ViewPager.SCROLL_STATE_IDLE) {
			onPageSwitched(position);
			reset();
			return;
		}
    	
    	if(startPosition < 0) {
    		startPosition = position;
    	}
    	
    	if(currentPosition < 0) {
    		currentPosition = position;
    		prevPosition = position;
    	}
    	if(prevOffset == 0) {
    		prevOffset = positionOffset;
    	}
    	
		boolean toRight = prevOffset <= 0.5f && positionOffset > 0.5f;
		boolean toLeft = prevOffset >= 0.5f && positionOffset < 0.5f;
		
		if((toRight || toLeft) && (positionOffset > 0.9f || positionOffset < 0.1f) || positionOffset == 0) {
        	reset();
		} else {
			
			if(toLeft) {
				currentPosition = prevPosition - 1;
			} else if(toRight) {
				currentPosition = prevPosition + 1;
			}
			if(currentPosition >= totalCount) {
				currentPosition = 0;
			} else if(currentPosition < 0) {
				currentPosition = totalCount - 1;
			}
			/*
			if(currentPosition == 0 && toLeft) {
				currentPosition = totalCount - 1;
			} else if(currentPosition == totalCount - 1 && toRight) {
				currentPosition = 0;
			} else if(currentPosition == position && toRight) {
				currentPosition = Math.abs(integer + 1) % totalCount;
			} else if(currentPosition == position  && toLeft) {
				currentPosition = Math.abs(integer - 1) % totalCount;
			} else if(currentPosition < position && toRight) {
				currentPosition = Math.abs(integer) % totalCount;
			} else if(currentPosition > position && toLeft) {
				currentPosition = Math.abs(integer) % totalCount;
			}
			*/
			if(currentPosition != prevPosition) {
				onPageSwitched(currentPosition);
	        	prevPosition = currentPosition;
			}
        	prevOffset = positionOffset;
		}
	}
	
	private void reset() {
		prevOffset = 0;
    	currentPosition = -1;
    	startPosition = -1;
	}
	
	@Override
	public void onPageScrollStateChanged(int state) {
		this.state = state;				
		if(state == ViewPager.SCROLL_STATE_IDLE) {
        	reset();
		}
	}
	
}