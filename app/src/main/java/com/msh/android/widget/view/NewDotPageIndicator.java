package com.msh.android.widget.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewConfigurationCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;

import static android.graphics.Paint.ANTI_ALIAS_FLAG;
import static android.widget.LinearLayout.HORIZONTAL;
import static android.widget.LinearLayout.VERTICAL;

public class NewDotPageIndicator extends View implements PageIndicator{
	private static final int INVALID_POINTER = -1;

	protected float mRadius;
	protected final Paint mPaintPageFill = new Paint(ANTI_ALIAS_FLAG);
	protected final Paint mPaintStroke = new Paint(ANTI_ALIAS_FLAG);
	protected final Paint mPaintFill = new Paint(ANTI_ALIAS_FLAG);
	protected ViewPager mViewPager;
	protected String[] mViewContent;
	private ViewPager.OnPageChangeListener mListener;
	protected int mCurrentPage;
	protected int mSnapPage;
	protected float mPageOffset;
	private int mScrollState;
	protected int mOrientation;
	protected boolean mCentered;
	protected boolean mSnap;
	private boolean mIsOutOfBound;

	private int mTouchSlop;
	private float mLastMotionX = -1;
	private int mActivePointerId = INVALID_POINTER;
	private boolean mIsDragging;

	private WindowManager mWindowManager;
	private Display mDisplay;
	private DisplayMetrics mMetrics;
	
	private static final int DOT_GAP_DP = 6;
	protected static int DOT_GAP;
	
	private boolean disableTouches = true;

	public NewDotPageIndicator(Context context) {
		this(context, null);
	}

	public NewDotPageIndicator(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	@SuppressWarnings("deprecation")
	public NewDotPageIndicator(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		if (isInEditMode()) return;

		mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		mDisplay = mWindowManager.getDefaultDisplay();
		mMetrics = new DisplayMetrics();
		mDisplay.getMetrics(mMetrics);

		mCentered = true; 
		mOrientation = 0;  
		mPaintPageFill.setStyle(Style.FILL);
		mPaintPageFill.setColor(Color.parseColor("#5FFFFFFF"));  
		mPaintStroke.setStyle(Style.STROKE);
		mPaintStroke.setColor(Color.parseColor("#5FFFFFFF"));  
		mPaintStroke.setStrokeWidth(valueToDip(1));  
		mPaintFill.setStyle(Style.FILL);
		mPaintFill.setColor(Color.parseColor("#FFFFFFFF"));  
		mRadius = valueToDip(3);  
		mSnap = false;  

		Drawable background = new ColorDrawable(Color.parseColor("#00000000"));
		if (background != null) {
			setBackgroundDrawable(background);
		}


		final ViewConfiguration configuration = ViewConfiguration.get(context);
		mTouchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(configuration);

		DOT_GAP = (int)(DOT_GAP_DP * mMetrics.density);
	}


	public void setCentered(boolean centered) {
		mCentered = centered;
		invalidate();
	}

	public boolean isCentered() {
		return mCentered;
	}

	public void setPageColor(int pageColor) {
		mPaintPageFill.setColor(pageColor);
		invalidate();
	}

	public int getPageColor() {
		return mPaintPageFill.getColor();
	}

	public void setFillColor(int fillColor) {
		mPaintFill.setColor(fillColor);
		invalidate();
	}

	public int getFillColor() {
		return mPaintFill.getColor();
	}

	public void setOrientation(int orientation) {
		switch (orientation) {
		case HORIZONTAL:
		case VERTICAL:
			mOrientation = orientation;
			requestLayout();
			break;

		default:
			throw new IllegalArgumentException("Orientation must be either HORIZONTAL or VERTICAL.");
		}
	}

	public int getOrientation() {
		return mOrientation;
	}

	public void setStrokeColor(int strokeColor) {
		mPaintStroke.setColor(strokeColor);
		invalidate();
	}

	public int getStrokeColor() {
		return mPaintStroke.getColor();
	}

	public void setStrokeWidth(float strokeWidth) {
		mPaintStroke.setStrokeWidth(strokeWidth);
		invalidate();
	}

	public float getStrokeWidth() {
		return mPaintStroke.getStrokeWidth();
	}

	public void setRadius(float radius) {
		mRadius = radius;
		invalidate();
	}

	public float getRadius() {
		return mRadius;
	}

	public void setSnap(boolean snap) {
		mSnap = snap;
		invalidate();
	}

	public boolean isSnap() {
		return mSnap;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
//		 canvas.drawColor(Color.GREEN);
		if (mViewPager == null) {
			return;
		}
//		int count = mViewContent.length;
		if (mViewContent.length == 0) {
			return;
		}

//		if (mCurrentPage >= count) {
//			setCurrentItem(count - 1)
//			return;
//		}

		int longSize;
		int longPaddingBefore;
		int longPaddingAfter;
		int shortPaddingBefore;
		if (mOrientation == HORIZONTAL) {
			longSize = getWidth();
			longPaddingBefore = getPaddingLeft();
			longPaddingAfter = getPaddingRight();
			shortPaddingBefore = getPaddingTop();
		} else {
			longSize = getHeight();
			longPaddingBefore = getPaddingTop();
			longPaddingAfter = getPaddingBottom();
			shortPaddingBefore = getPaddingLeft();
		}

		final float threeRadius = mRadius * 3;
		final float shortOffset = shortPaddingBefore + mRadius;
		float longOffset = longPaddingBefore + mRadius;
		if (mCentered) {
			longOffset += ((longSize - longPaddingBefore - longPaddingAfter) / 2.0f) - (((mViewContent.length - 1) * (threeRadius + DOT_GAP)) / 2.0f);
		}

		float dX;
		float dY;

		float pageFillRadius = mRadius;
		if (mPaintStroke.getStrokeWidth() > 0) {
			pageFillRadius -= mPaintStroke.getStrokeWidth() / 2.0f;
		}

		//Draw stroked circles
		for (int iLoop = 0; iLoop < mViewContent.length; iLoop++) {
			float drawLong = longOffset + (iLoop * (threeRadius + DOT_GAP));
			if (mOrientation == HORIZONTAL) {
				dX = drawLong;
				dY = shortOffset;
			} else {
				dX = shortOffset;
				dY = drawLong;
			}
			// Only paint fill if not completely transparent
			if (mPaintPageFill.getAlpha() > 0) {
				canvas.drawCircle(dX, dY, pageFillRadius, mPaintPageFill);
			}

			// Only paint stroke if a stroke width was non-zero
			if (pageFillRadius != mRadius) {
				canvas.drawCircle(dX, dY, mRadius, mPaintStroke);
			}
		}

		//Draw the filled circle according to the current scroll
		float cx = (mSnap ? mSnapPage : mCurrentPage) * (threeRadius + DOT_GAP);
		if (!mSnap) {
			cx += mPageOffset * threeRadius;
		}
		if (mOrientation == HORIZONTAL) {
			dX = longOffset + cx;
			dY = shortOffset;
		} else {
			dX = shortOffset;
			dY = longOffset + cx;
		}
		canvas.drawCircle(dX, dY, mRadius, mPaintFill);
	}
	
	public void setDisableTouches(boolean value) {
		disableTouches = value;
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(android.view.MotionEvent ev) {
		if (super.onTouchEvent(ev)) {
			return true;
		}
		if (disableTouches || (mViewPager == null) || (mViewContent.length == 0)) {
			return false;
		}

		final int action = ev.getAction() & MotionEventCompat.ACTION_MASK;
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
			mLastMotionX = ev.getX();
			break;

		case MotionEvent.ACTION_MOVE: {
			final int activePointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
			final float x = MotionEventCompat.getX(ev, activePointerIndex);
			final float deltaX = x - mLastMotionX;

			if (!mIsDragging) {
				if (Math.abs(deltaX) > mTouchSlop) {
					mIsDragging = true;
				}
			}

			if (mIsDragging) {
				mLastMotionX = x;
				if (mViewPager.isFakeDragging() || mViewPager.beginFakeDrag()) {
					mViewPager.fakeDragBy(deltaX);
				}
			}

			break;
		}

		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			if (!mIsDragging) {
				final int count = mViewContent.length;
				final int width = getWidth();
				final float halfWidth = width / 2f;
				final float sixthWidth = width / 6f;

				if ((mCurrentPage > 0) && (ev.getX() < halfWidth - sixthWidth)) {
					if (action != MotionEvent.ACTION_CANCEL) {
						mViewPager.setCurrentItem(mCurrentPage - 1);
					}
					return true;
				} else if ((mCurrentPage < count - 1) && (ev.getX() > halfWidth + sixthWidth)) {
					if (action != MotionEvent.ACTION_CANCEL) {
						mViewPager.setCurrentItem(mCurrentPage + 1);
					}
					return true;
				} else if ((mCurrentPage >= count) && (ev.getX() > halfWidth + sixthWidth)) {
					if (action != MotionEvent.ACTION_CANCEL) {
						mViewPager.setCurrentItem(0);
					}
				} else if ((mCurrentPage <= 0) && (ev.getX() < halfWidth - sixthWidth)) {
					if (action != MotionEvent.ACTION_CANCEL) {
						mViewPager.setCurrentItem(count - 1);
					}
				}
			}

			mIsDragging = false;
			mActivePointerId = INVALID_POINTER;
			if (mViewPager.isFakeDragging()) mViewPager.endFakeDrag();
			break;

		case MotionEventCompat.ACTION_POINTER_DOWN: {
			final int index = MotionEventCompat.getActionIndex(ev);
			mLastMotionX = MotionEventCompat.getX(ev, index);
			mActivePointerId = MotionEventCompat.getPointerId(ev, index);
			break;
		}

		case MotionEventCompat.ACTION_POINTER_UP:
			final int pointerIndex = MotionEventCompat.getActionIndex(ev);
			final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
			if (pointerId == mActivePointerId) {
				final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
				mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex);
			}
			mLastMotionX = MotionEventCompat.getX(ev, MotionEventCompat.findPointerIndex(ev, mActivePointerId));
			break;
		}

		return true;
	}

	@Override
	public void setViewPager(ViewPager view, String[] strings) {
		if (mViewPager == view) {
			mViewContent = strings;
			return;
		}
//		if (mViewPager != null) {
//			mViewPager.setOnPageChangeListener(null);
//		}
		if (view.getAdapter() == null) {
			throw new IllegalStateException("ViewPager does not have adapter instance.");
		}
		mViewPager = view;
//		mViewPager.setOnPageChangeListener(this);
		mViewContent = strings;
		invalidate();
	}

	@Override
	public void setViewPager(ViewPager view, int initialPosition) {
		setViewPager(view);
		setCurrentItem(initialPosition);
	}

	@Override
	public void setCurrentItem(int item) {
		if (mViewPager == null) {
			throw new IllegalStateException("ViewPager has not been bound.");
		}
		mViewPager.setCurrentItem(item, false);
		mCurrentPage = item;
		invalidate();
	}

	@Override
	public void notifyDataSetChanged() {
		invalidate();
	}

	@Override
	public void onPageScrollStateChanged(int state) {
		mScrollState = state;
		
		if (mListener != null) {
			mListener.onPageScrollStateChanged(mScrollState);
		}
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		mCurrentPage = position;
		mPageOffset = positionOffset;
		invalidate();

		if (mListener != null) {
			mListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
		}
	}

	@Override
	public void onPageSelected(int position) {
		if (mSnap || mScrollState == ViewPager.SCROLL_STATE_IDLE) {
			mCurrentPage = position;
			mSnapPage = position;
			invalidate();
		}

		if (mListener != null) {
			mListener.onPageSelected(position);
		}
	}

	@Override
	public void setOnPageChangeListener(ViewPager.OnPageChangeListener listener) {
		mListener = listener;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see android.view.View#onMeasure(int, int)
	 */
	 @Override
	 protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		 if (mOrientation == HORIZONTAL) {
			 setMeasuredDimension(measureLong(widthMeasureSpec), measureShort(heightMeasureSpec));
		 } else {
			 setMeasuredDimension(measureShort(widthMeasureSpec), measureLong(heightMeasureSpec));
		 }
	 }

	 /**
	  * Determines the width of this view
	  *
	  * @param measureSpec
	  *            A measureSpec packed into an int
	  * @return The width of the view, honoring constraints from measureSpec
	  */
	 private int measureLong(int measureSpec) {
		 int result;
		 int specMode = MeasureSpec.getMode(measureSpec);
		 int specSize = MeasureSpec.getSize(measureSpec);

		 if ((specMode == MeasureSpec.EXACTLY) || (mViewPager == null)) {
			 //We were told how big to be
			 result = specSize;
		 } else {
			 //Calculate the width according the views count
			 final int count = mViewContent.length;
			 result = (int)(getPaddingLeft() + getPaddingRight()
					 + (count * 2 * mRadius) + (count - 1) * mRadius + (count - 1) * DOT_GAP + 1);
			 //Respect AT_MOST value if that was what is called for by measureSpec
			 if (specMode == MeasureSpec.AT_MOST) {
				 result = Math.min(result, specSize);
			 }
		 }
		 return result;
	 }

	 /**
	  * Determines the height of this view
	  *
	  * @param measureSpec
	  *            A measureSpec packed into an int
	  * @return The height of the view, honoring constraints from measureSpec
	  */
	 private int measureShort(int measureSpec) {
		 int result;
		 int specMode = MeasureSpec.getMode(measureSpec);
		 int specSize = MeasureSpec.getSize(measureSpec);

		 if (specMode == MeasureSpec.EXACTLY) {
			 //We were told how big to be
			 result = specSize;
		 } else {
			 //Measure the height
			 result = (int)(2 * mRadius + getPaddingTop() + getPaddingBottom() + 1);
			 //Respect AT_MOST value if that was what is called for by measureSpec
			 if (specMode == MeasureSpec.AT_MOST) {
				 result = Math.min(result, specSize);
			 }
		 }
		 return result;
	 }

	 @Override
	 public void onRestoreInstanceState(Parcelable state) {
		 SavedState savedState = (SavedState)state;
		 super.onRestoreInstanceState(savedState.getSuperState());
		 mCurrentPage = savedState.currentPage;
		 mSnapPage = savedState.currentPage;
		 requestLayout();
	 }

	 @Override
	 public Parcelable onSaveInstanceState() {
		 Parcelable superState = super.onSaveInstanceState();
		 SavedState savedState = new SavedState(superState);
		 savedState.currentPage = mCurrentPage;
		 return savedState;
	 }

	 static class SavedState extends BaseSavedState {
		 int currentPage;

		 public SavedState(Parcelable superState) {
			 super(superState);
		 }

		 private SavedState(Parcel in) {
			 super(in);
			 currentPage = in.readInt();
		 }

		 @Override
		 public void writeToParcel(Parcel dest, int flags) {
			 super.writeToParcel(dest, flags);
			 dest.writeInt(currentPage);
		 }

		 public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
			 @Override
			 public SavedState createFromParcel(Parcel in) {
				 return new SavedState(in);
			 }

			 @Override
			 public SavedState[] newArray(int size) {
				 return new SavedState[size];
			 }
		 };
	 }

	 /**
	  * Convert value of pixel to dip value
	  * @param value is a pixel
	  * @return dp 
	  */
	  private int valueToDip(int value) {
		 return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, mMetrics);
	  }

	@Override
	public void setViewPager(ViewPager view) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void setChangePoint(int position) {
		if (mSnap || mScrollState == ViewPager.SCROLL_STATE_IDLE) {
			mCurrentPage = position;
			mSnapPage = position;
			invalidate();
		}

		if (mListener != null) {
			mListener.onPageSelected(position);
		}
	}
}
