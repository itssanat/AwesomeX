package awesomex;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.sql.*;
import java.util.Random;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javax.swing.JFileChooser;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 *
 * @author itssanat
 */
public class FXMLDocumentController implements Initializable {  // implement runnable

    protected Button play;
    @FXML
    private Button browse;
    @FXML
    private Button music;
    @FXML
    protected TextField nowPlaying;
    @FXML
    private ListView<String> songList;  // list view //
    @FXML
    private Button pause;
    @FXML
    private Button prev;
    @FXML
    private Button next;
    @FXML
    private Slider slider;
    @FXML
    private Button mostPlayed;
    @FXML
    private Button shuffle;
    @FXML
    private Button repeatAll;
    @FXML
    private Button repeatOne;
    @FXML
    private Slider volumeSlider;
    @FXML
    private Label timeLable;
    private File songFile,prevSong,currentSong; 
    private Media media;
    private MediaPlayer mediaPlayer;
    private File[] songs; // to store total song in music dir//
    private ObservableList<String> listItems = FXCollections.observableArrayList(); // observable list 
    final File dir = new File("C:\\Users\\sanat\\Music");   // path of music directory // 
    private Thread t;   
    private boolean isPlaying = false;
    private boolean isShuffle = false;
    private boolean isRepeatAll = true;
    private boolean isRepeatOne = false;
    private boolean isPaused  = false;
    private String path;  
    private int currentSongIndex = 0;
    Playback playback = new Playback();
    @FXML
    private ImageView coverPhoto; // to show cover photo of song //
    private Image image;  
    private Connection connect;    // object to make connection with sqlite database //
    private Statement statement;
    @FXML
    private Button addFavorite;
    private boolean isFav = false;  // variable to check whether favorite playlist is open or not //
    @FXML
    private ImageView playIcon;
    Image playImage , pauseImage , defaultImage;
   
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {   
        loadSongs();
        repeatAll.setText("   Repeat All *");
        volumeSlider.setValue(100);
        slider.setValue(0);
        connect = connectDB();
        addFavorite.setText("");
        
        playImage = new Image("file:src/awesomex/icons/play.png");
        pauseImage = new Image("file:src/awesomex/icons/pause.png");
        defaultImage = new Image("file:src/awesomex/icons/AesomeXicon.jpg");
        ImageView iv = new ImageView(playImage);
        iv.setFitHeight(34);
        iv.setFitWidth(34);
        pause.setGraphic(iv);
        coverPhoto.setImage(defaultImage);
    }    

    
    public void playButtonAction() {
        close();
        try {
            nowPlaying.setText(songFile.getName());  // updating status bar "nowPlaying" //
            currentSong = songFile; // updating current song name //
            currentSongIndex = songList.getSelectionModel().getSelectedIndex(); // updating the index of current song //
            
            if(isFavorite(currentSong.getName()))  // checking the playlist //
                addFavorite.setText("");      
            else
                addFavorite.setText("/");
            
            //t = new Thread(this);
            isPlaying = true;
            //t.start();  //invoking the thread //
            run();
            ImageView iv = new ImageView(pauseImage);
            iv.setFitHeight(34);
            iv.setFitWidth(34);
            pause.setGraphic(iv);
        } catch (Exception e) {
            nowPlaying.setText("error in playing");
        }
    }

    @FXML
    public void browseButtonAction(ActionEvent event) { // to choose a song from any dir //
        try{
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Choose file to play..."); // setting title of title bar //
            chooser.showOpenDialog(null);
            songFile = chooser.getSelectedFile();
            playButtonAction();
        }
        catch(Exception e){ 
            nowPlaying.setText("No file choosen");
        }
    }

    @FXML
    private void musicButtonAction(ActionEvent event) {  // opening music playlist //
        songList.getItems().clear();
        loadSongs();   // calling loadSong method //
        isFav = false;
    }

    @FXML
    private void selectedSong() {  // selecting song from the songList  // 
        if(isFav){
            String s = songList.getSelectionModel().getSelectedItem();  // selection song from favorite playlist //
            songFile = FavoriteSong(s);   
        } 
        else{
            String s = songList.getSelectionModel().getSelectedItem();  // selection song from music playlist //
            if(s!=null && !s.isEmpty()){
                int selectedSong = songList.getSelectionModel().getSelectedIndex();
                songFile = songs[selectedSong];
            }
        }
        playButtonAction();
    }
    
    public void close(){
        if(isPlaying){   // check whether a song is playing or not //
            mediaPlayer.stop();   // closing the song //
            isPlaying = false;
            isPaused = false;
        }
    }
    
    //@Override
    public void run(){  // start of the thread //
        try {
            media = new Media(songFile.toURI().toString());
            mediaPlayer = new MediaPlayer(media);                 // initialisation of MediaPlayer class //
            mediaPlayer.setVolume(volumeSlider.getValue()/100);  // adding vloume slider //
            
            // adding a listener to volume slider ///
            volumeSlider.valueProperty().addListener(new ChangeListener<Number>(){
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    
                    mediaPlayer.setVolume(newValue.doubleValue()/100);
                    
                }
            });
            mediaPlayer.currentTimeProperty().addListener(new ChangeListener<Duration>() {
                @Override
                public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {
                    slider.setValue((newValue.toSeconds()/(mediaPlayer.getTotalDuration().toSeconds()))*100);
                    timeLable.setText(playback.timeFormat(newValue.toSeconds(),mediaPlayer.getTotalDuration().toSeconds()));
                    image = (Image)(media.getMetadata().get("image")); 
                    coverPhoto.setImage(image);
                }
                
            });
            
            //Thread.sleep(1000);
            
            slider.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    mediaPlayer.seek(Duration.seconds((slider.getValue()/100)*mediaPlayer.getTotalDuration().toSeconds()));
                }
            });
            mediaPlayer.play();
            mediaPlayer.setOnEndOfMedia(new Runnable() {  // playing next song when previous song is completed //
                @Override
                public void run() {
                    nextButtonAction(); // invoking next button //
                }
            });
        } catch (Exception e) {
            nowPlaying.setText("error in playing");
        }
    }
    
    @FXML
    private void pauseButtonAction(ActionEvent event) {  // to pause and resume the song // 
        if(isPaused){
            mediaPlayer.play();
            isPaused = false;
            nowPlaying.setText(currentSong.getName());
            //playIcon.setImage(new Image("icons//play.png"));
            ImageView iv = new ImageView(pauseImage);
            iv.setFitHeight(34);
            iv.setFitWidth(34);
            pause.setGraphic(iv);
        }
        else{
            mediaPlayer.pause();
            isPaused = true;
            nowPlaying.setText("Paused...");
            ImageView iv = new ImageView(playImage);
            iv.setFitHeight(34);
            iv.setFitWidth(34);
            pause.setGraphic(iv);
        }
    }

    @FXML
    private void prevButtonAction(ActionEvent event) {   
        int totalSong = listItems.size();
        if(isRepeatOne){
            songFile = currentSong;
        }
        else if(isRepeatAll){
            currentSongIndex = (currentSongIndex-1+totalSong)%totalSong;
            songList.getSelectionModel().clearSelection();
            songList.getSelectionModel().select(currentSongIndex);
            songList.scrollTo(currentSongIndex);
            selectedSong();
        }
        else {
            Random random = new Random();
            int prev = random.nextInt(totalSong);
            songList.getSelectionModel().clearSelection();
            songList.getSelectionModel().select(prev);
            songList.scrollTo(prev);
            selectedSong();
        }
        playButtonAction();
    }

    @FXML
    private void nextButtonAction() {  // not completed, under developement // 
        int totalSong = listItems.size();
        if(isRepeatOne){
            songFile = currentSong;
        }
        else if(isRepeatAll){
            currentSongIndex = (currentSongIndex+1)%totalSong;
            songList.getSelectionModel().clearSelection();
            songList.getSelectionModel().select(currentSongIndex);
            songList.scrollTo(currentSongIndex);
            selectedSong();
        }
        else {
            Random random = new Random();
            int next = random.nextInt(totalSong);
            songList.getSelectionModel().clearSelection();
            songList.getSelectionModel().select(next);
            songList.scrollTo(next);
            selectedSong();
        }
        playButtonAction();
    }

    @FXML
    private void songInfoButtonAction(ActionEvent event) {
        try{
            FXMLLoader loader1= new FXMLLoader(getClass().getResource("SongInfo.fxml"));
            Parent root1 = (Parent)loader1.load();
            SongInfoController controller1 = loader1.getController(); //creating the object of controller class //
            Stage infoWindow = new Stage();
            infoWindow.setResizable(false);  // making window unresizable //
            controller1.album.setText((String)media.getMetadata().get("album"));
            controller1.title.setText((String)media.getMetadata().get("title"));
            controller1.artist.setText((String)media.getMetadata().get("artist"));
            infoWindow.setTitle("Song details"); // setting the title of stage //
            infoWindow.setScene(new Scene(root1));
            infoWindow.show();
            
        }
        catch(Exception e){
            System.out.println("errpr");
        }
    }

    @FXML
    private void shuffleButtonAction(ActionEvent event) {
        isShuffle = true;
        isRepeatAll = false;
        isRepeatOne = false;
        shuffle.setText("   Shuffle *");
        repeatAll.setText("   Repeat All");
        repeatOne.setText("   Repeat One");
    }

    @FXML
    private void repeatAllButtonAction(ActionEvent event) {
        isShuffle = false;
        isRepeatAll = true;
        isRepeatOne = false;
        shuffle.setText("   Shuffle");
        repeatAll.setText("   Repeat All *");
        repeatOne.setText("   Repeat One");
    }

    @FXML
    private void repeatOneButtonAction(ActionEvent event) {
        isShuffle = false;
        isRepeatAll = false;
        isRepeatOne = true;
        shuffle.setText("   Shuffle");
        repeatAll.setText("   Repeat All");
        repeatOne.setText("   Repeat One *");
    }
    
    // Loding all the song present in "/home/itssanat/Music" dir //
    protected void loadSongs(){  
        listItems.clear();
        try{
            FileFilter filter = new FileFilter() {
                @Override
                public boolean accept(File f) {
                    if(f.isDirectory())
                        return false;
                    if(f.getName().endsWith(".mp3"))
                        return true;
                    else
                        return false;
                }
            };
            
            songs = dir.listFiles(filter);
            
            for(File file : songs){
                
                if(file.getName().endsWith(".mp3")) //  file.toString().endsWith(".mp3")
                    listItems.add(file.getName()); // adding song in observable list //
            }
            songList.getItems().addAll(listItems); // to add in observable listView //
        } catch (Exception e){
            System.out.println("Error in loding song");
        } 
    } 

    @FXML
    private void favoriteButtonAction(ActionEvent event) {
        try {
            
            statement = connect.createStatement();
  
            ResultSet rs = statement.executeQuery("SELECT * FROM favorite");
            
            songList.getItems().clear();
            listItems.clear();
            while(rs.next()){
                listItems.add(rs.getString("name"));
            }
            songList.getItems().addAll(listItems);
            isFav = true;
        } catch (SQLException ex) {
            System.out.println("error in loading");
        }
    }
    
    public Connection connectDB (){ // making connection with sqlite database // 
        try{
            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection("jdbc:sqlite:awesomexDB.db");
            //System.out.println("connection estd...");
            return conn;
        } catch(Exception e){
            System.out.println("unable to connect");
            return null;
        }
    }

    @FXML
    private void addFavoriteButtonAction(ActionEvent event) {
        if(isFavorite(currentSong.getName())){
            String sql = "DELETE FROM favorite WHERE name = ?";
            try {
                PreparedStatement pstmt = connect.prepareStatement(sql);
                pstmt.setString(1, currentSong.getName());
                pstmt.executeUpdate();
                addFavorite.setText("/");
            } catch (SQLException ex) {
                System.out.println("error in deleting");
            }
        } else{
            String sql = "INSERT INTO favorite(name,path) VALUES(?,?)";
            try {
                PreparedStatement pstmt = connect.prepareStatement(sql);
                pstmt.setString(1, currentSong.getName());
                pstmt.setString(2,currentSong.getAbsolutePath());
                pstmt.executeUpdate();
                addFavorite.setText("");
            } catch (SQLException ex) {
                System.out.println("error");
            }
        }
    }
    
    public boolean isFavorite(String song){
        try {
            statement = connect.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM favorite");
            while(rs.next()){
                if(song.equals(rs.getString("name")))
                    return true;
            }
            return false;
        } catch (SQLException ex) {
            System.out.println("error in loading");
        }
        return false;
    }
    
    public File FavoriteSong(String song){
        for(File file: songs){
            if(song.equals(file.getName()))
                return file;
        }
        return null;
    }
}