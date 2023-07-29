package com.sameer.vegetablesdetector;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.sameer.vegetablesdetector.DatabaseHandler.DatabaseHandler;
import com.sameer.vegetablesdetector.DatabaseHandler.Recipe;

import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.task.vision.detector.Detection;
import org.tensorflow.lite.task.vision.detector.ObjectDetector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class FoodDetection extends AppCompatActivity implements View.OnClickListener {


    ListView listView;
    FrameLayout frameLayout;
    ProgressBar progressBar;
    ImageView inputImageView;
    ImageView imgSampleOne, imgSampleTwo;
    Button captureImageFab, recipeSearch;
    List<DetectionResult> detectionResults = new ArrayList<>();
    List<String> foodIngredients;

    private static final int pic_id = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.food_detection);

        inputImageView = findViewById(R.id.imageView);
        imgSampleOne = findViewById(R.id.imgSampleOne);
        imgSampleTwo = findViewById(R.id.imgSampleTwo);
        captureImageFab = findViewById(R.id.captureImageFab);
        recipeSearch = findViewById(R.id.recipe_search);
        progressBar = findViewById(R.id.progressBar);
        listView = (ListView) findViewById(R.id.listView);
        frameLayout = findViewById(R.id.frame);

        captureImageFab.setOnClickListener(this);
        imgSampleOne.setOnClickListener(this);
        imgSampleTwo.setOnClickListener(this);
        recipeSearch.setOnClickListener(this);

        // Adding recipes to database to search for the recipes
        // that have the ingredients detected in the photo
        DatabaseHandler db = new DatabaseHandler(getBaseContext());
        db.deleteAllRecords();
        db.addRecipe(new Recipe(1, "Lazanya", "Tomato, potato, cheese, pasta", "boil the water and cut the cheese"));
        db.addRecipe(new Recipe(2, "Caprese Salad", "Tomato, mozzarella, fresh basil, butter lettuce, marinated grilled steak, balsamic vinegar, olive oil", "boil the water and cut the cheese"));
        db.addRecipe(new Recipe(2, "Garden Fresh Tomato Soup", "Tomato, mozzarella, fresh basil, butter lettuce, marinated grilled steak, balsamic vinegar, olive oil", ""));

    }

    @Override
    public void onClick(View v) {
        switch ((v.getId())) {
            case R.id.captureImageFab:
                dispatchTakePictureIntent();
                break;
            case R.id.imgSampleOne:
                setViewAndDetect(getSampleImage(R.drawable.tomato));
                break;
            case R.id.imgSampleTwo:
                setViewAndDetect(getSampleImage(R.drawable.onion));
                break;
            case R.id.recipe_search:
                searchForRecipes();
                break;
        }
    }

    private void searchForRecipes() {

//      if there isn't any detected ingredients show a toast and stop
        if (foodIngredients.size() <= 0) {
            Toast.makeText(com.sameer.vegetablesdetector.FoodDetection.this, "There isn't any ingredients detected to search for recipes", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseHandler d = new DatabaseHandler(this);
        List<Recipe> resultList = d.getSpecifiedRecipies(foodIngredients);

        if (resultList == null) {
            Toast.makeText(com.sameer.vegetablesdetector.FoodDetection.this, "No Recepies with this Ingredients..", Toast.LENGTH_SHORT).show();

        } else {
            listView.setVisibility(View.VISIBLE);
            recipeSearch.setVisibility(View.INVISIBLE);
            frameLayout.setVisibility(View.INVISIBLE);
            List<String> l = new ArrayList<>();
            for (Recipe r : resultList) {
                System.out.println(r.getRecipeName());
                l.add(r.getRecipeName());
            }

            final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),
                    android.R.layout.simple_list_item_1, android.R.id.text1, l);
            listView.setAdapter(adapter);

        }
    }

    private void dispatchTakePictureIntent() {
        Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(camera_intent, pic_id);
    }

    private void setViewAndDetect(Bitmap bitmap) {
        frameLayout.setVisibility(View.VISIBLE);
        listView.setVisibility(View.INVISIBLE);

        // Display image
        inputImageView.setImageBitmap(bitmap);

        // Run ODT and display result
        // Note that we run this in the background thread to avoid blocking the app UI because
        // TFLite object detection is a synchronised process.

        try {
            InitilaizeObjectDetection(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }

        recipeSearch.setVisibility(View.VISIBLE);

    }

    // because tensorFlow accepts only bitmap
    private Bitmap getSampleImage(int drawable) {
        return BitmapFactory.decodeResource(getResources(), drawable, new BitmapFactory.Options());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == pic_id && resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            setViewAndDetect(photo);
        }
    }

    // Initialization of tensorFlow object
    private void InitilaizeObjectDetection(Bitmap bitmap) throws IOException {
        //TODO: Add object detection code here

        // Step 1: create TFLite's TensorImage object
        TensorImage image = TensorImage.fromBitmap(bitmap);

        // Step 2: Initialize the detector object
        ObjectDetector.ObjectDetectorOptions options = ObjectDetector.ObjectDetectorOptions.builder()
                .setMaxResults(5)
                .setScoreThreshold(0.5f)
                .build();

        ObjectDetector detector = ObjectDetector.createFromFileAndOptions(
                this, // the application context
                "salad.tflite", // must be same as the filename in assets folder
                options
        );

        // Step 3: feed given image to the model and print the detection result
        List<Detection> results = detector.detect(image);
        System.out.println(results.size());


        // Step 4: Parse the detection result and show it
        foodIngredients = new ArrayList<>();
        //To show details of the detected ingredients
        debugPrint(results);


        for (Detection result : results) {
            List<Category> category = result.getCategories();
            Category c = category.get(0);

            DetectionResult detectionResult = new DetectionResult(result.getBoundingBox(),
                    c.getLabel() + "---" + c.getScore() * 100);
            detectionResults.add(detectionResult);
        }


        // Draw the detection result on the bitmap and show it.
        Bitmap imgWithResult = drawDetectionResult(bitmap, detectionResults);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                inputImageView.setImageBitmap(imgWithResult);
            }
        });
    }

    private void debugPrint(List<Detection> results) {
        for (int i = 0; i < results.size(); i++) {
            Detection detection = results.get(i);
            RectF rectF = detection.getBoundingBox();
            System.out.println("Detected object: " + i);
            System.out.println("boundingBox : " + rectF.top + " " + rectF.bottom + " " + rectF.left + rectF.right);

            List<Category> category = detection.getCategories();
            for (int j = 0; j < category.size(); j++) {
                Category c = category.get(j);
                if (!foodIngredients.contains(c.getLabel())) {
                    foodIngredients.add(c.getLabel());
                }

                System.out.print("Label is : " + c.getLabel() + " --- Score is :");
                float confidence = c.getScore();
                System.out.println(confidence * 100f);
            }
        }
    }


    // The function that is responsible for drawing the lines around the ingredient
    private Bitmap drawDetectionResult(Bitmap bitmap,
                                       List<DetectionResult> detectionResults) {
        Bitmap outputBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(outputBitmap);
        Paint pen = new Paint();
        pen.setTextAlign(Paint.Align.LEFT);

        for (DetectionResult result : detectionResults) {
            // draw bounding box
            pen.setColor(Color.RED);
            pen.setStrokeWidth(8F);
            pen.setStyle(Paint.Style.STROKE);
            RectF box = result.boundingBox;
            canvas.drawRect(box, pen);

            Rect tagSize = new Rect(0, 0, 0, 0);

            // calculate the right font size
            pen.setStyle(Paint.Style.FILL_AND_STROKE);
            pen.setColor(Color.GREEN);
            pen.setStrokeWidth(2F);

            pen.setTextSize(85F);
            pen.getTextBounds(result.text, 0, result.text.length(), tagSize);
            Float fontSize = pen.getTextSize() * box.width() / tagSize.width();

            // adjust the font size so texts are inside the bounding box
            if (fontSize < pen.getTextSize()) {
                pen.setTextSize(fontSize);
            }

            Float margin = (box.width() - tagSize.width()) / 2.0F;

            if (margin < 0F) {
                margin = 0F;
            }
            canvas.drawText(
                    result.text, box.left - 5,
                    box.top - 5, pen);
        }
        this.detectionResults = new ArrayList<>();
        return outputBitmap;
    }

}