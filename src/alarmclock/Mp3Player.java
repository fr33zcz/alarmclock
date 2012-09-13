package alarmclock;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackListener;

/**
 * Tiny MP3 player API built on <a href="http://www.javazoom.net/javalayer/javalayer.html">JavaZoom JLayer</a>
 *  
 * @author --==[FReeZ]==--
 * @version 1.1
 */
public class Mp3Player {
	/**
	 * Canonical file path to MP3 which plays when alarm has been invoked
	 */
	private String mp3Filename = null;
	
	/**
	 * The instance of MP3 player
	 */
	private AdvancedPlayer player = null;
	
	/**
	 * True when player is playing a song
	 */
	private boolean isPlaying = false;
	
	/**
	 * Playback listener for started / finished events
	 */
	private PlaybackListener listener = null;
	
	/**
	 * Sets the canonical path of MP3 to play when alarm is invoked
	 * 
	 * @param  filename (should be canonical)
	 *  
	 * @throws FileNotFoundException when the specified file couldn't be found
	 */
	public void setFileToPlay(String filename) throws FileNotFoundException {
		final File file = new File(filename);
		if (!file.exists() || !file.canRead()) {
			throw new FileNotFoundException(filename);
		}
		
		mp3Filename = filename;
	}
	
	/**
	 * Adds playback listener with playbackStarted and playbackFinished methods
	 * 
	 * @param listener
	 */
	public void addPlaybackListener(PlaybackListener listener) {
		this.listener = listener;
	}
	
	/**
	 * Plays the MP3 file previously set by setFileToPlay()
	 * 
	 * @throws JavaLayerException when player failed to play the file
	 * @throws FileNotFoundException when filename was not found
	 */
	public void play() throws JavaLayerException, FileNotFoundException {
		if (isPlaying) {
			return;
		}
		
		if (mp3Filename == null) {
			throw new IllegalStateException("Filename cannot be null");
		}
		
		final BufferedInputStream bis = new BufferedInputStream(new FileInputStream(mp3Filename));
		player = new AdvancedPlayer(bis);
		player.setPlayBackListener(listener);
		
		new Thread() {
			public void run() {
				try {
					if (player != null) {
						player.play();
					}
				} catch (JavaLayerException e) {
					e.printStackTrace();
					return;
				}
			}
		}.start();
		isPlaying = true;
	}
	
	/**
	 * Stops the current instance of MP3 player and closes the player
	 */
	public void stop() {
		if (player == null) {
			throw new IllegalStateException("Player not opened.");
		}
		
		if (!isPlaying) {
			return;
		}
		
		if (player != null) {
			player.stop();
		}
		
		isPlaying = false;
	}
	
	/**
	 * Overrides internal isplaying status to false
	 */
	public void playbackFinished() {
		isPlaying = false;
	}
	
	/**
	 * Returns current player state
	 * 
	 * @return is playing
	 */
	public boolean isPlaying() {
		return isPlaying;
	}
	
	/**
	 * Disposes all allocated resources
	 */
	public void dispose() {
		if (player == null) {
			return;
		}
		
		player.close();
		player = null;
	}
}
