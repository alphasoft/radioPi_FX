package application.mpd;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.bff.javampd.MPD;
import org.bff.javampd.exception.MPDConnectionException;
import org.bff.javampd.exception.MPDPlaylistException;
import org.bff.javampd.objects.MPDSong;


public class ControlMpd {
	private MPD mpd = null; //Singleton: EIN Server, EINE KontrollInstanz
	private final String ipStr;

	public MPD getMPD() {
		return mpd;
	}

	public ControlMpd(String ipStr) {
		this.ipStr = ipStr;
		try {
			mpd = new MPD.Builder().server(ipStr).port(6600).password(null).build();

			if (mpd.isConnected()) {
				System.out.println("Connected - mpd controller instance @"+mpd);
			} else {
				System.out.println("Not Connected");
			}
		} catch (MPDConnectionException e) {
			System.err.println("MPD seems not be running at "+ipStr);
			e.printStackTrace();
		} catch (UnknownHostException e) {
			System.err.println("Cannot connect to Host with IP "+ipStr);
			e.printStackTrace();
		}
	}

	public static Boolean pingToServer(String ipAddress, Boolean silent) {
		InetAddress inet;
		Boolean ret = false;
		int timeout = 5000;
		try {
			inet = InetAddress.getByName(ipAddress);
			if (!silent)
				System.out.println("Sending Ping Request to " + ipAddress);
			ret = inet.isReachable(timeout);
			if (!silent)
				System.out.println(ret ? "Host is reachable" : "Host is NOT reachable");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ret;
	}

	public String getIpStr() {
		return ipStr;
	}
	
	/**
	 *  
	 * @return Die Sender in der Playlist
	 * @throws MPDPlaylistException
	 */
	public List<String> getSenderList() throws MPDPlaylistException {
		// Senderliste extrahieren
		List<MPDSong> songList = mpd.getPlaylist().getSongList();				
		List<String> senderUrl = new ArrayList<>();
		for (MPDSong song : songList) {
			senderUrl.add(song.getFile());
		}
		System.out.println("SenderURL-liste : "+senderUrl);
		return senderUrl;
	}
/*
	public static void main(String[] args) {
	}
*/
	
}
