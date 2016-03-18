package application;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.bff.javampd.ServerStatus;
import org.bff.javampd.exception.MPDPlayerException;
import org.bff.javampd.exception.MPDPlaylistException;
import org.bff.javampd.exception.MPDResponseException;
import org.bff.javampd.objects.MPDSong;

import application.model.Station;
import application.mpd.ControlMpd;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.web.WebView;


public class RadioPiController implements Initializable {

	private ObservableList<Station> senderliste = FXCollections.observableList(new ArrayList<Station>());
	private List<String> urlListe = new ArrayList<String>();
	private List<MPDSong> playlist = new ArrayList<MPDSong>();
			
	@FXML private TableView<Station> senderlisteView;
	@FXML private Button current;
	@FXML private Label currentDisplay;
	@FXML private Label statusLine;
	@FXML private ImageView speakerSymbol;
	@FXML private Slider volumeSlider;
	@FXML private MediaPlayer player;
	@FXML private WebView webView;
	@FXML private MediaView playerView;
	
	private static final String mpdIpAddr = "10.101.12.216";
	private static final String STATIONS = "stations.list.csv";
	//private static final String PLAYLISTFILE = "sender.m3u";
	
	private ControlMpd mpdController=null;
	
	private Image loudImg=new Image(getClass().getResource("/res/img/speaker_loud.png").toExternalForm());
	private Image muteImg=new Image(getClass().getResource("/res/img/speaker_mute.jpg").toExternalForm());
	
	private Integer volume; //-> IntegerProperty, bind to volumeSlider! 
	private Integer oldVolume;
	private Boolean isMuted=false;
	//private Boolean isPlaying=false;

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		if (ControlMpd.pingToServer(mpdIpAddr, false)) {
			// Instanzierung des MPD-Controllers
			mpdController = new ControlMpd(mpdIpAddr);
			System.out.println("probeMPD != null");
			if (mpdController.getMPD() != null) {
				System.out.println("Connected to MPD @"+ mpdIpAddr);
				statusLine.setText("Connected to MPD @"+ mpdIpAddr);
			}
		} else {
			System.err.println("Cannot ping "+mpdIpAddr);
			Platform.exit();
		}
		
		//URLs aus MPD.playlist, weitere Metadaten aus csv-File ?
		try{
			urlListe = mpdController.getSenderList();
		}catch( MPDPlaylistException e){
			statusLine.setText("Kann Playliste nicht auslesen!");
		}
		
		try {
			BufferedReader bufRdr = new BufferedReader(new FileReader(STATIONS));
			String line;
			while( (line = bufRdr.readLine()) != null ){
				String[] column = line.split(", "); // delimiter := comma+whitespace
				System.out.println(column);
				senderliste.add( new Station(column[0], column[1] ,"mp3", 128) ); // bislang nur 128kbps mp3 streams
			}
			if(bufRdr!=null) { bufRdr.close(); }
		} 
		catch( IOException readExc ) {	
			readExc.printStackTrace();
		}
		
		senderlisteView.setItems(senderliste);
		senderlisteView.getSelectionModel().selectedItemProperty()
										   .addListener( clickEvent -> onClickTuneIn() );
		speakerSymbol.setImage(loudImg);
		volumeSlider.valueProperty().addListener((observ, oldVal, newVal) -> {
				onChangeVolume((Integer)newVal);
		});
		
		mpdController.getMPD().getMonitor().addVolumeChangeListener( e -> System.out.println("Volume: "+ e.getVolume()));// OK
		mpdController.getMPD().getMonitor().addPlayerChangeListener( e -> System.out.println("Player: "+ e.getStatus()));// OK
		mpdController.getMPD().getMonitor().addOutputChangeListener( e -> System.out.println("Output: "+e.getEvent()));// OK
		mpdController.getMPD().getMonitor().addPlaylistChangeListener( e -> { System.out.println("Playlist: "+e.getEvent());
										statusLine.setText("Playlist: "+e.getEvent());
										try {
											currentDisplay.setText(mpdController.getMPD().getPlayer().getCurrentSong().getName() );
										} catch (Exception ex) { 
											ex.printStackTrace();
										}
		});
		mpdController.getMPD().getMonitor().addMPDErrorListener( e -> System.out.println("Error: "+e.getMsg()));
		mpdController.getMPD().getMonitor().start();
		
		ServerStatus srvStat = mpdController.getMPD().getServerStatus();
		try {
			currentDisplay.setText(mpdController.getMPD().getPlayer().getCurrentSong().getName());
			System.out.println(srvStat);
			System.out.println(srvStat.getStatus());
		} catch (MPDResponseException e) {
			e.printStackTrace();
		}
		
	}
	
	//TODO: tune in on DblClick !)
	public void onClickTuneIn(){
		String senderName = senderlisteView.getSelectionModel().getSelectedItem().getName();
		String streamUrl = senderlisteView.getSelectionModel().getSelectedItem().getStreamUrl();
		int id = senderlisteView.getSelectionModel().getSelectedIndex();
		//System.out.println(id); ///// DEBUG /////
	
		currentDisplay.setText(senderName);
		statusLine.setText("[" +ID+ "]  "+ streamUrl);
		try {
			mpdController.getMPD().getPlayer().playId(urlList[id]);
			mpdController.getMPD().getPlayer().playNext();
		} catch (MPDPlayerException e) {
			e.printStackTrace();
		}
		
		//mpdController.getMPD().getPlayer().playId(mpdPlaylistItem);
	}
	
	@FXML public void fetchCurrentPlayInfo() {
		try{
			String newInfo = mpdController.getMPD().getPlayer().getCurrentSong().getName();
			currentDisplay.setText(newInfo);
			statusLine.setText(newInfo);
		}catch(MPDResponseException e){
			statusLine.setText("No response from MPD ??");
			e.printStackTrace();
		}
	}

	@FXML public void muteUnmuteToggle() {
		this.isMuted = !this.isMuted; //toggle
		try{
			if( this.isMuted==true ) {
				this.volume=0;
				speakerSymbol.setImage(muteImg);
				oldVolume = new Double(volumeSlider.getValue()).intValue(); //merken der jetzigen Lautstaerke
				volumeSlider.setValue(volume);
				mpdController.getMPD().getPlayer().setVolume(volume);
			} else {
				speakerSymbol.setImage(loudImg);
				volumeSlider.setValue(oldVolume);
				mpdController.getMPD().getPlayer().setVolume(oldVolume);
			}
		}catch( MPDPlayerException e ){
			e.printStackTrace();
		}	
	}
		
	public void onChangeVolume(Integer newVolume) {	
		try {
			mpdController.getMPD().getPlayer().setVolume(newVolume);
			statusLine.setText("Volume set to "+ new DecimalFormat("00").format(newVolume)+" %" );	
		} catch (MPDPlayerException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * grepKeyVal(inputline, "bitrate") --> "...., bitrate: 192,...." --> returns foundVal="192" 
	 * 
	 * @param inputStr
	 * @return foundStr
	 */
	private String grepKeyVal(String inputline, String key) {
		String foundVal="";
		//....
		return foundVal;
	}
	
}
