package cs1302.gallery;

import cs1302.gallery.GalleryComponent;
import cs1302.gallery.ItunesResponse;
import cs1302.gallery.ItunesResult;
import java.io.UnsupportedEncodingException;
import java.io.IOException;
import java.lang.IllegalArgumentException;
import java.net.URLEncoder;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import javafx.util.Duration;
import java.util.*;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.nio.charset.StandardCharsets;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Represents an iTunes Gallery App.
 */
public class GalleryApp extends Application {


    public static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)           // uses HTTP protocol version 2 where possible
        .followRedirects(HttpClient.Redirect.NORMAL)  // always redirects, except from HTTPS to HTTP
        .build();                                     // builds and returns a HttpClient object

    /** Google {@code Gson} object for parsing JSON-formatted strings. */
    public static Gson GSON = new GsonBuilder()
        .setPrettyPrinting()                          // enable nice output when printing
        .create();                                    // builds and returns a Gson object

    private Stage stage;
    private Scene scene;

    protected VBox mainPane;

    private HBox root;
    private HBox urlPane;
    private HBox search;
    private HBox bottom;
    protected ItunesResult result;
    private HBox loadingBar;
    private HBox instruction;

    protected TextField urlField;
    protected Button urlButton;
    protected Button loadImage;

    protected static int howMuch;
    protected boolean continued;

    private GalleryComponent picts;
    private Button pauseButt;
    private ComboBox dropMenu;

    private Separator separator1 = new Separator(Orientation.HORIZONTAL);
    private Separator separator2 = new Separator(Orientation.VERTICAL);

    private Label checkStyle;
    private Label iTunesLabel;
    private Label instructionLabel;

    private int thisMany;
    private URI location;
    private Random rand;
    protected String[] imgUrl;
    protected Timeline timeline;
    private ArrayList<String> artUrl;
    private static ProgressBar pb;

/**
 * Constructs a {@code GalleryApp} object}. Constructs all the objects
 * needed when constructing a GalleryApp object.
 */
    public GalleryApp() {
        mainPane = new VBox();

        this.root = new HBox();   //ImageView Boxes
        this.urlPane = new HBox(); //Url bar and buttons
        this.search = new HBox();  //Url bar only
        this.bottom = new HBox();  //Progress bar and label
        this.loadingBar = new HBox();
        this.instruction = new HBox();
        this.stage = new Stage();

        this.picts = new GalleryComponent();
        this.urlField = new TextField("jack johnson");
        this.urlButton = new Button("Get Images");
        this.dropMenu = new ComboBox();
        this.checkStyle = new Label("Search:");
        this.instructionLabel = new Label
        ("   Type in a term, select a media type, then click the button.");

        this.rand = new Random();
        this.pb = new ProgressBar(0);
        this.iTunesLabel = new Label ("Images provided by iTunes Search API.");

        //FILE LAYER
        HBox FileLayer = new HBox();
        picts = new GalleryComponent();
        loadImage = new Button("Update Images");

        pauseButt = new Button("Play");
        pauseButt.setDisable(true);

        Label checkStyle = new Label("Search Query:");


    } // GalleryApp

    /** {@inheritDoc} */
    @Override
    public void init() {

        HBox.setHgrow(urlField, Priority.ALWAYS);
        HBox.setHgrow(loadingBar, Priority.ALWAYS);
        pb.prefWidthProperty().bind(loadingBar.widthProperty());

        urlButton.setOnAction(e -> {
            if (pauseButt.getText().equals("Pause")) {
                pauseButt.setText("Play");
                pauseButt.setDisable(true);
                timeline.stop();
            }
            runNow(() -> loadImage());
        } );

        root.setStyle("-fx-background-color: #F5F5DC;");
        dropMenu.getItems().add("movie");
        dropMenu.getItems().add("podcast");
        dropMenu.getItems().add("music");
        dropMenu.getItems().add("musicVideo");
        dropMenu.getItems().add("audiobooks");
        dropMenu.getItems().add("shortFilm");
        dropMenu.getItems().add("tvShow");
        dropMenu.getSelectionModel().select(2);
        search.getChildren().add(checkStyle);
        root.setPrefWidth(750);
        root.setPrefHeight(600);

        instruction.setPrefHeight(25);
        instruction.setAlignment(Pos.CENTER_LEFT);

        instruction.getChildren().add(instructionLabel);
        loadingBar.getChildren().add(pb);
        search.setAlignment(Pos.CENTER);
        urlPane.getChildren().addAll(new Separator(Orientation.VERTICAL),
                                     pauseButt, separator1,
                                     new Separator(Orientation.VERTICAL),
                                     search, urlField,
                                     new Separator(Orientation.VERTICAL),
                                     dropMenu,
                                     new Separator(Orientation.VERTICAL),
                                     urlButton);

        bottom.getChildren().addAll(separator2, loadingBar,
                                    new Separator (Orientation.VERTICAL),
                                    iTunesLabel);

        root.getChildren().addAll(picts);

        mainPane.getChildren().addAll(separator1,
                                      urlPane,instruction,
                                      new Separator (Orientation.HORIZONTAL),
                                      root,
                                      new Separator (Orientation.HORIZONTAL),
                                      bottom);

        System.out.println("init() called");
    } // init

    /** {@inheritDoc} */
    @Override
    public void start(Stage stage) {
        this.stage = stage;
        this.scene = new Scene(mainPane);//, urlButton);

        this.stage.setOnCloseRequest(event -> Platform.exit());
        this.stage.setTitle("GalleryApp!");
        this.stage.setScene(this.scene);
        this.stage.sizeToScene();
        this.stage.show();
        Platform.runLater(() -> this.stage.setResizable(false));

        pauseButt.setOnAction(event -> {
            if (pauseButt.getText() == "Play") {
                runNow(() -> shuffle());

            }
            if (pauseButt.getText() == "Pause") {
                pauseButt.setText("Play");
                timeline.stop();
            }
        } );

    } // start

    /** {@inheritDoc} */
    @Override
    public void stop() {
        // feel free to modify this method
        System.out.println("stop() called");
    } // stop

    /**
     * This method takes in a target called runnable which is another method
     * and makes target run on a separate thread.
     *
     * @param target The runnable object being taken in which is the loadPage method
     * for this program.
     */
    public static void runNow(Runnable target) {
        Thread t = new Thread(target);
        t.setDaemon(true);
        t.start();

    }

    /**
     * Shuffles random pictures on the board with random
     * images from the json file. Alternates the Play and
     * pause button text. Changes an image every two seconds.
     */
    private void shuffle() {
        Platform.runLater(() -> pauseButt.setText("Pause"));
        EventHandler<ActionEvent> handler = event -> {

            int randomNum = rand.nextInt((19 - 0) + 1) + 0;
            int randomNum2 = rand.nextInt(((howMuch - 1) - 0) + 1) + 0;

            Image temp = new Image(imgUrl[randomNum2], 150, 150, false, false);
            picts.array[randomNum].setImage(temp);

        } ;

        KeyFrame keyFrame = new KeyFrame(Duration.seconds(2), handler);
        timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.getKeyFrames().add(keyFrame);
        timeline.play();

    }

    /**
     * This method is run when the load button is pressed.
     * Uses user input from search box to create a {@code URL}
     * That can dowload search results in json form. This method
     * is run in a different thread with the exception of all Label
     * changes which happens inside the JavaFX Application Thread.
     * Runs {@code printItunesResponse} at the end.
     *
     * @throws IllegalArgumentException when itunesResponse is no 200 or
     * when the amount of search results is less than 21,
     */
    private void loadImage() {
        pauseButt.setDisable(true);
        urlButton.setDisable(true);
        try {
            setProgress(0);
            Platform.runLater(() -> instructionLabel.setText("  Getting images..."));
            String term = URLEncoder.encode(urlField.getText(), StandardCharsets.UTF_8);
            String media = dropMenu.getValue().toString();
            String limit = URLEncoder.encode("200", StandardCharsets.UTF_8);
            String query = String.format("?term=%s&media=%s&limit=%s", term, media, limit);
            location = URI.create("http://itunes.apple.com/search" + query);
            HttpRequest request = HttpRequest.newBuilder()
                .uri(location)
                .build();
            HttpResponse<String> response = HTTP_CLIENT.send(request, BodyHandlers.ofString());
            String body = response.body();
            String jsonString = response.body();
            // parse the JSON-formatted string using GSON

            ItunesResponse itunesResponse = new Gson().fromJson(jsonString, ItunesResponse.class);

            if (itunesResponse.resultCount < 21) {
                throw new IllegalArgumentException(response.toString());
            }

            if (response.statusCode() != 200) {
                throw new IOException(response.toString());
            }    // if

            printItunesResponse(itunesResponse);
            urlButton.setDisable(false);
            //print info about the response

        } catch (IOException | IllegalArgumentException
                 | IllegalStateException | InterruptedException e ) {

            Label secondLabel = new Label("URI: " + location + "\n" + "\n" + e);
            StackPane secondaryLayout = new StackPane();
            secondaryLayout.getChildren().add(secondLabel);
            Scene secondScene = new Scene(secondaryLayout, 700, 300);

            // New window (Stage)
            Platform.runLater(() -> {
                Stage newWindow = new Stage();
                newWindow.setTitle("Error");
                newWindow.setScene(secondScene);
                newWindow.setX(this.stage.getX() + 200); // Set position of second window
                newWindow.setY(this.stage.getY() + 100);
                newWindow.show();
            } ) ;

            Platform.runLater(() -> instructionLabel.setText
                 ("Last attempt to get images failed..."));
            pauseButt.setDisable(false);
            urlButton.setDisable(false);
        }
    }

/**
 * Takes in {@code ItunesResponse} variable which contains all the
 * json information and puts all the artwork urls in an ArrayList.
 * Afterwards the {@code removeDuplicates} method is ran to remove any
 * duplicate URL's. Then all the items in the array list are put into
 * an Array and the array is sent as a variable in the setComponent method
 * This method is run in a differnt thread except for any changes to Labels.
 *
 * @param itunesResponse contains all the information from the gson.
 */

    private void printItunesResponse(ItunesResponse itunesResponse) {

        int howLong = itunesResponse.results.length;
        artUrl = new ArrayList<String>();


        for (int k = 0; k < howLong; k++) {
            result = itunesResponse.results[k];
            artUrl.add(result.artworkUrl100);
        }


        removeDuplicates(artUrl);


        imgUrl = new String[artUrl.size()];

        for (int i = 0; i < artUrl.size(); i++) {
            imgUrl[i] = artUrl.get(i);
        }

        picts.setComponent(imgUrl);


        Platform.runLater(() -> instructionLabel.setText(location.toString()));
        pauseButt.setDisable(false);
    } // for

    /**
     * Removes duplicates from the Arraylist {@code inputList} brought
     * in through the parameters.
     *
     * @param inputList An ArrayList of Strings that contains all the urls
     * of the images.
     */
    static void removeDuplicates(ArrayList<String> inputList) {
        Set<String> temp_set = new LinkedHashSet<>();
        temp_set.addAll(inputList);
        inputList.clear();
        inputList.addAll(temp_set);
        howMuch = temp_set.size();
    }

    /**
     * Increases the progress to the progress bar based on
     * how much the parameter {@code progress} is. Changes
     * to the progress bar are done in the main JavaFX application
     * thread.
     *
     * @param progress A double that increases the progress bar howeve
     * much the double is.
     */
    protected static void setProgress(final double progress) {
        Platform.runLater(() -> pb.setProgress(progress));

    }

} // GalleryApp
