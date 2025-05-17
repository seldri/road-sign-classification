package com.road_sign_classification.controller;

import ai.djl.Model;
import ai.djl.ModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.ndarray.NDList;
import ai.djl.translate.TranslateException;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;
import ai.djl.translate.Batchifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import com.road_sign_classification.service.ImageProcessor;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@RestController
public class RoadSignController {

    private static final Model model = loadModel();
    private static final ImageProcessor imageProcessor = new ImageProcessor(); // Initialize ImageProcessor

    private static Model loadModel() {
        try {
            Model model = Model.newInstance("road-sign-classifier", "OnnxRuntime");
            model.load(Paths.get("src/main/resources/models/gtsrb-model.onnx"));
            return model;
        } catch (IOException | ModelException e) {
            throw new RuntimeException("Failed to load the model.", e);
        }
    }

    @GetMapping("/api/status")
    public String status() {
        return "Road Sign Classification API is running!";
    }

    @PostMapping("/api/predict")
    public ResponseEntity<?> predict(@RequestPart("file") MultipartFile image) {
        try {
            // Use ImageProcessor to process the image
            NDList processedImage = imageProcessor.processImage(image.getInputStream(), model.getNDManager());

            // Create a custom Translator for image classification
            Translator<NDList, Classifications> translator = new Translator<NDList, Classifications>() {
                @Override
                public NDList processInput(TranslatorContext ctx, NDList input) {
                    return input; // The image is already preprocessed in ImageProcessor
                }

                @Override
                public Classifications processOutput(TranslatorContext ctx, NDList list) {
                    float[] probabilities = list.singletonOrThrow().toFloatArray();
                    String[] classes = {
                        "Speed limit (20km/h)", "Speed limit (30km/h)", "Speed limit (50km/h)",
                        "Speed limit (60km/h)", "Speed limit (70km/h)", "Speed limit (80km/h)",
                        "End of speed limit (80km/h)", "Speed limit (100km/h)", "Speed limit (120km/h)", 
                        "No passing", "No passing for vehicles over 3.5 metric tons", 
                        "Right-of-way at the next intersection", "Priority road", "Yield", "Stop", 
                        "No vehicles", "Vehicles over 3.5 metric tons prohibited", "No entry", 
                        "General caution", "Dangerous curve to the left", "Dangerous curve to the right", 
                        "Double curve", "Bumpy road", "Slippery road", "Road narrows on the right", 
                        "Road work", "Traffic signals", "Pedestrians", "Children crossing", 
                        "Bicycles crossing", "Beware of ice/snow", "Wild animals crossing", 
                        "End of all speed and passing limits", "Turn right ahead", "Turn left ahead", 
                        "Ahead only", "Go straight or right", "Go straight or left", "Keep right", 
                        "Keep left", "Roundabout mandatory", "End of no passing", 
                        "End of no passing by vehicles over 3.5 metric tons"
                    };

                    int bestIndex = 0;
                    for (int i = 1; i < probabilities.length; i++) {
                        if (probabilities[i] > probabilities[bestIndex]) {
                            bestIndex = i;
                        }
                    }

                    // Convert float[] probabilities to List<Double>
                    List<Double> probabilityList = new ArrayList<>();
                    for (float probability : probabilities) {
                        probabilityList.add((double) probability);
                    }

                    // Create the Classifications object with correct constructor
                    return new Classifications(Arrays.asList(classes), probabilityList);
                }

                @Override
                public Batchifier getBatchifier() {
                    return null; // No batch processing
                }
            };

            // Create a predictor with the model using the custom translator
            try (Predictor<NDList, Classifications> predictor = model.newPredictor(translator)) {
                System.out.println("Image received for prediction.");
                Classifications prediction = predictor.predict(processedImage);
                System.out.println("Prediction successful.");
                System.out.println("Top prediction: " + prediction.best().getClassName());
                System.out.println("Confidence: " + String.format("%.2f", prediction.best().getProbability() * 100) + "%");

                return ResponseEntity.ok().body(Map.of(
                    "class", prediction.best().getClassName(),
                    "confidence", String.format("%.2f", prediction.best().getProbability() * 100) + "%"
                ));
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error reading the uploaded image: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "Error reading the uploaded image."));
        } catch (TranslateException e) {
            e.printStackTrace();
            System.err.println("Error during model prediction: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "Error during model prediction."));
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Unexpected error: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Unexpected error: " + e.getMessage()));
        }
    }
}