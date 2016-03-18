package application.model;

import java.io.Serializable;
import java.net.URL;
import java.time.LocalDateTime;
import javafx.scene.image.Image;


public class Station implements Serializable {
	private static final long serialVersionUID = 1L;
	private String name;
	private Image logo;
	private String streamUrl;
	private Image stationLogo;
	private String codec;
	private Integer bitrate;
	private String category;
	private LocalDateTime lastPlayed;
	private boolean cached;
	
	public Station() { }
	
	public Station(String name, String stationUrl, String codec, Integer bitrate) {
		super();
		this.name = name;
		/*DEBUG*/	System.out.println("/src/res/img/logo-"+name+".png");
		this.logo = new Image(getClass().getResource("/src/res/img/logo-"+name+".png").toExternalForm());
		this.streamUrl = stationUrl;
		this.codec = codec;
		this.bitrate = bitrate;
		this.category = "from MPD playlist file";
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String getStreamUrl() {
		return streamUrl;
	}
	public void setStreamUrl(String streamUrl) {
		this.streamUrl = streamUrl;
	}
	
	public String getCodec() {
		return codec;
	}
	public void setCodec(String codec) {
		this.codec = codec;
	}
	
	public Image getStationLogo() {
		return stationLogo;
	}
	public void setStationLogo(Image stationLogo) {
		this.stationLogo = stationLogo;
	}

	public Integer getBitrate() {
		return bitrate;
	}
	public void setBitrate(Integer bitrate) {
		this.bitrate = bitrate;
	}

	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}

	public boolean isCached() {
		return cached;
	}
	public void setCached(boolean cached) {
		this.cached = cached;
	}
	
}