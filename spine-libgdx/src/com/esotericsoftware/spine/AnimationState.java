
package com.esotericsoftware.spine;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ObjectMap;

/** Stores mixing times between animations and automatically mixes them as animations change. */
public class AnimationState {
	Animation current, previous;
	float currentTime, previousTime;
	boolean currentLoop, previousLoop;
	float mixTime, mixDuration;
	final ObjectMap<Key, Float> animationToMixTime = new ObjectMap();
	final Key tempKey = new Key();

	public void apply (Skeleton skeleton) {
		if (current == null) return;
		if (previous != null) {
			previous.apply(skeleton, previousTime, previousLoop);
			float alpha = MathUtils.clamp(mixTime / mixDuration, 0, 1);
			current.mix(skeleton, currentTime, currentLoop, alpha);
			if (alpha == 1) previous = null;
		} else {
			current.apply(skeleton, currentTime, currentLoop);
		}
	}

	public void update (float delta) {
		currentTime += delta;
		previousTime += delta;
		mixTime += delta;
	}

	/** Set the mixing duration between two animations. */
	public void setMixing (Animation from, Animation to, float duration) {
		if (from == null) throw new IllegalArgumentException("from cannot be null.");
		if (to == null) throw new IllegalArgumentException("to cannot be null.");
		Key key = new Key();
		key.a1 = from;
		key.a2 = to;
		animationToMixTime.put(key, duration);
	}

	/** Set the current animation. */
	public void setAnimation (Animation animation, boolean loop) {
		setAnimation(animation, loop, 0);
	}

	/** Set the current animation.
	 * @param time The time within the animation to start. */
	public void setAnimation (Animation animation, boolean loop, float time) {
		previous = null;
		if (animation != null && current != null) {
			tempKey.a1 = current;
			tempKey.a2 = animation;
			if (animationToMixTime.containsKey(tempKey)) {
				mixDuration = animationToMixTime.get(tempKey);
				mixTime = 0;
				previous = current;
			}
		}
		current = animation;
		currentLoop = loop;
		currentTime = time;
	}

	/** @return May be null. */
	public Animation getAnimation () {
		return current;
	}

	/** Returns the time within the current animation. */
	public float getTime () {
		return currentTime;
	}

	static class Key {
		Animation a1, a2;

		public int hashCode () {
			return 31 * (31 + a1.hashCode()) + a2.hashCode();
		}

		public boolean equals (Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			Key other = (Key)obj;
			if (a1 == null) {
				if (other.a1 != null) return false;
			} else if (!a1.equals(other.a1)) return false;
			if (a2 == null) {
				if (other.a2 != null) return false;
			} else if (!a2.equals(other.a2)) return false;
			return true;
		}
	}
}
