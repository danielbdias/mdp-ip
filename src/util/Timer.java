package util;

public class Timer {

	// Internal timing stats
	public long _lTime;
	public long _lElapsedTime;
	
	// Starts timer upon creation
	public Timer() {
		ResetTimer();
	}
	
	// Reset and start the timer
	public void ResetTimer() {
		_lTime = System.currentTimeMillis();
		_lElapsedTime = -1;
	}

	public long StopTimer() {
		_lElapsedTime = System.currentTimeMillis() - _lTime;
		return _lElapsedTime;
	}

	public long GetElapsedTime() {
		return _lElapsedTime;
	}
}
