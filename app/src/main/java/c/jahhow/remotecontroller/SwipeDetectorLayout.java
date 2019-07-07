package c.jahhow.remotecontroller;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;

public class SwipeDetectorLayout extends FrameLayout {

	MainActivity mainActivity;
	LayoutInflater layoutInflater;
	int movingChildIndex = 0;
	SlideLayout movingChild;
	Interpolator interpolator = new DecelerateInterpolator(2);

	public SwipeDetectorLayout(MainActivity mainActivity) {
		super(mainActivity);
		layoutInflater = mainActivity.getLayoutInflater();
		movingChild = new SlideLayout(mainActivity, interpolator);
		addView(movingChild);
		addView(new SlideLayout(mainActivity, interpolator));
		addView(new SlideLayout(mainActivity, interpolator));
		addView(new SlideLayout(mainActivity, interpolator));
		addView(new SlideLayout(mainActivity, interpolator));
		this.mainActivity = mainActivity;
	}

	int mActivePointerId;
	float touchOriginX, touchOriginY, viewOriginX, viewOriginY,
			diffX, diffY;
	final float density = getResources().getDisplayMetrics().density;
	float pxSlop = 4 * density;
	//float transYAtAppear = 40 * density;

	long duration = 800;

	@Override
	public void addView(final View child) {
		child.setVisibility(getChildCount() == 0 ? VISIBLE : GONE);
		/*TranslateAnimation translateAnimation=new TranslateAnimation(TranslateAnimation.RELATIVE_TO_PARENT,);
		translateAnimation.setStartTime(TranslateAnimation.START_ON_FIRST_FRAME);
		translateAnimation.cancel();*/
		child.animate().setDuration(duration)/*.setInterpolator(interpolator)*//*.setListener(new Animator.AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animation) {
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				onChildAnimationEnd(child);
			}

			@Override
			public void onAnimationCancel(Animator animation) {
			}

			@Override
			public void onAnimationRepeat(Animator animation) {
			}
		})*/;
		super.addView(child);
	}

	/*void onChildAnimationEnd(View view) {
		if (view.getTranslationX() != view.getTranslationY() && view.getAlpha() != 0 && view != movingChild) {
			view.animate().alpha(0);
			mainActivity.ShowToast("animate to alpha 0");
		}
	}*/

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		if (!mainActivity.preferences.contains(MainActivity.KeyPrefer_Swiped) || mainActivity.preferences.getBoolean(MainActivity.KeyPrefer_SwipeDemo, true))
			Demo();
	}

	boolean demoing;

	void Demo() {
		demoing = true;
		final Interpolator demoInterpolator = new AccelerateDecelerateInterpolator();
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ignored) {
				}
				while (demoing) {
					movingChild.animate().translationY(-getHeight())
							.setInterpolator(demoInterpolator)/*.setListener(listener)*/;
					mainActivity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							ReadyNextChild();
						}
					});

					try {
						Thread.sleep(250);
					} catch (InterruptedException ignored) {
					}
					if (!demoing) return;
					movingChild.animate().translationY(getHeight())
							.setInterpolator(demoInterpolator)/*.setListener(listener)*/;
					mainActivity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							ReadyNextChild();
						}
					});

					try {
						Thread.sleep(250);
					} catch (InterruptedException ignored) {
					}
					if (!demoing) return;
					movingChild.animate().translationX(-getWidth())
							.setInterpolator(demoInterpolator)/*.setListener(listener)*/;
					mainActivity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							ReadyNextChild();
						}
					});

					try {
						Thread.sleep(250);
					} catch (InterruptedException ignored) {
					}
					if (!demoing) return;
					movingChild.animate().translationX(getWidth())
							.setInterpolator(demoInterpolator)/*.setListener(listener)*/;
					mainActivity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							ReadyNextChild();
						}
					});

					try {
						Thread.sleep(3000);
					} catch (InterruptedException ignored) {
					}
				}
			}
		}).start();
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		return true;
	}

	boolean ignoreSameMoves = false;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getActionMasked();
		switch (action) {
			case MotionEvent.ACTION_DOWN: {
				demoing = false;
				ignoreSameMoves = true;
				NewActivePointer(event);
				movingChild.animate().setDuration(0).translationXBy(0).translationYBy(0);// Cancel translation animation
				break;
			}
			case MotionEvent.ACTION_MOVE: {
				if (ignoreSameMoves) {
					float bacDiffX = diffX, bacDiffY = diffY;
					computePointerDiff(event);
					if (bacDiffX != diffX || bacDiffY != diffY) {
						ignoreSameMoves = false;
						MigrateTouchPoint(bacDiffX, bacDiffY, diffX, diffY);
						IndicateDirection();
					}
				} else {
					MoveCurChild(event);
					IndicateDirection();
				}
				break;
			}
			case MotionEvent.ACTION_UP: {
				computePointerDiff(event);
				ViewPropertyAnimator animator = movingChild.animate();
				float absDiffX = Math.abs(diffX), absDiffY = Math.abs(diffY);
				if (absDiffX > pxSlop | absDiffY > pxSlop) {
					if (absDiffY > absDiffX) {
						int transY = getHeight();
						if (diffY > 0) {
							mainActivity.SendClick_Up(null);
						} else {
							mainActivity.SendClick_Down(null);
							transY = -transY;
						}
						animator.translationY(transY);
					} else {
						int transX = getWidth();
						if (diffX > 0) {
							mainActivity.SendClick_Left(null);
						} else {
							mainActivity.SendClick_Right(null);
							transX = -transX;
						}
						animator.translationX(transX);
					}
					animator.setInterpolator(interpolator).setDuration(duration);

					ReadyNextChild();
				} else {
					ResetMovingChild();
				}
				break;
			}
			case MotionEvent.ACTION_CANCEL:
				ResetMovingChild();
				break;
			case MotionEvent.ACTION_POINTER_DOWN: {
				ChangeActivePointer(event, event.getActionIndex());
				ignoreSameMoves = true;
				break;
			}
			case MotionEvent.ACTION_POINTER_UP: {
				int activeIndex = event.findPointerIndex(mActivePointerId);
				int index = event.getActionIndex();
				if (activeIndex == index) {
					ChangeActivePointer(event, index == 0 ? 1 : 0);
				}
				break;
			}
		}
		return true;
	}

	void ReadyNextChild() {
		movingChildIndex = (movingChildIndex + 1) % getChildCount();
		movingChild = (SlideLayout) getChildAt(movingChildIndex);
		movingChild.Reset(false);
		ShowView(movingChild);
	}

	void IndicateDirection() {
		float absDiffX = Math.abs(diffX), absDiffY = Math.abs(diffY);
		if (absDiffX > pxSlop | absDiffY > pxSlop) {
			if (absDiffY > absDiffX) {
				if (diffY > 0) {
					movingChild.Indicate(SlideLayout.IndicatorUp);
				} else {
					movingChild.Indicate(SlideLayout.IndicatorDown);
				}
			} else {
				if (diffX > 0) {
					movingChild.Indicate(SlideLayout.IndicatorLeft);
				} else {
					movingChild.Indicate(SlideLayout.IndicatorRight);
				}
			}
		} else {
			movingChild.Reset(true);
		}
	}

	void ShowView(View view) {
		view.animate().cancel();
		view.setAlpha(0);
		view.setScaleX(.8F);
		view.setScaleY(.8F);
		view.setTranslationY(0);
		view.setTranslationX(0);
		view.setVisibility(VISIBLE);
		view.animate().setInterpolator(interpolator).alpha(1).scaleX(1).scaleY(1);
	}

	void ResetMovingChild() {
		movingChild.Reset(true);
		movingChild.animate().setDuration(duration).translationX(0).translationY(0);
	}

	void NewActivePointer(MotionEvent event) {
		diffX = diffY = 0;
		touchOriginX = event.getX();
		touchOriginY = event.getY();
		viewOriginX = movingChild.getTranslationX();
		viewOriginY = movingChild.getTranslationY();
		mActivePointerId = event.getPointerId(0);
	}

	void ChangeActivePointer(MotionEvent event, int newIndex) {
		int oldPointerIndex = event.findPointerIndex(mActivePointerId);
		MigrateTouchPoint(event.getX(oldPointerIndex), event.getY(oldPointerIndex), event.getX(newIndex), event.getY(newIndex));
		mActivePointerId = event.getPointerId(newIndex);
	}

	void MigrateTouchPoint(float fromX, float fromY, float toX, float toY) {
		touchOriginX += toX - fromX;
		touchOriginY += toY - fromY;
	}

	void MoveCurChild(MotionEvent event) {
		computePointerDiff(event);
		movingChild.setTranslationX(viewOriginX + diffX);
		movingChild.setTranslationY(viewOriginY + diffY);
	}

	void computePointerDiff(MotionEvent event) {
		computePointerDiff(event, event.findPointerIndex(mActivePointerId));
	}

	void computePointerDiff(MotionEvent event, int index) {
		diffX = event.getX(index) - touchOriginX;
		diffY = event.getY(index) - touchOriginY;
	}
}
