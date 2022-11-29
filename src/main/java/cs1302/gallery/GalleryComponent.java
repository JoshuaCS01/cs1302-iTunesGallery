package cs1302.gallery;

import java.util.List;

import java.util.ArrayList;
import java.util.Scanner;
import java.io.IOException;
import java.net.MalformedURLException;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import java.net.URL;
import java.io.InputStreamReader;
import cs1302.gallery.GalleryApp;


/**
 * A custom component that extends VBox. The custom component contains
 * multiple HBOxes to ensure we have enough imageviews for our grid of 20;
 *
 */
public class GalleryComponent extends VBox {

    private static final String DEFAULT_IMG =
        "http://cobweb.cs.uga.edu/~mec/cs1302/gui/default.png";

    Image img = new Image(DEFAULT_IMG, 150, 150, false, false);

    ImageView [] array = new ImageView[20];


    HBox first = new HBox();
    HBox second = new HBox();
    HBox third = new HBox();
    HBox fourth = new HBox();

    int thereIsDuplicate;



    /**
     * A Constructorthat creaes an HBox and adds 5 new HBox's containing imageViews
     * into each row of HBox's. The ImageViews are all empty and
     * then they are all added to the big component VBox inthe end.
     */

    public GalleryComponent() {
        super();

        for (int i = 0; i < 20; i++) {
            array[i] = new ImageView(img);
        }

        for (int i = 0; i < 5; i++) {
            first.getChildren().add(new HBox(array[i]));
        }
        for (int i = 5; i < 10; i++) {
            second.getChildren().add(new HBox(array[i]));
        }
        for (int i = 10; i < 15; i++) {
            third.getChildren().add(new HBox(array[i]));
        }
        for (int i = 15; i < 20; i++) {
            fourth.getChildren().add(new HBox(array[i]));
        }
        this.getChildren().addAll(first,second,third,fourth);

    }

    /**
     * Changes the images inside the ImageView object with new
     * images objects using URL's from the array parameter {@code results}.
     * Any change to ImageViews increase the progress bar.
     *
     * @param results - a string array containing the urls of the images we are
     * putting into the imageviews.
     */

    public void setComponent(String[] results) {

        for (int k = 0; k < 20; k++) {
            Image temp = new Image(results[k], 150, 150, false, false);
            array[k].setImage(temp);
            GalleryApp.setProgress(1.0 * k / 20);

        }

        GalleryApp.setProgress(1);

    }
}
