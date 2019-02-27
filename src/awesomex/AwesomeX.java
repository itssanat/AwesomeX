package awesomex;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author itssanat
 */
public class AwesomeX extends Application {
    
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("FXMLDocument.fxml"));
        Parent root = loader.load();
        FXMLDocumentController controller = loader.getController(); //creating the object of controller class //
        
        Scene scene = new Scene(root);
        
        stage.setOnCloseRequest(e -> {  // to close the song(if playing) before closing window //
            controller.close();     // calling close() method of controller class //
        });
        //stage.setResizable(false);  // making window unresizable // 
        stage.setTitle("AwesomeX Music Player"); // setting the title of stage //
        
        stage.setScene(scene);
        stage.show();
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);  // start of the application //
    }
}
