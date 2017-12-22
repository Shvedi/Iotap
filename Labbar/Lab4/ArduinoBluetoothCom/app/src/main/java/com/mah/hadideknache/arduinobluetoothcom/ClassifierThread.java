package com.mah.hadideknache.arduinobluetoothcom;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;

import weka.classifiers.Classifier;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.filters.unsupervised.attribute.Normalize;

import static android.os.Environment.DIRECTORY_DOWNLOADS;
import static android.os.Environment.getDataDirectory;

/**
 * Created by hello on 2017-12-15.
 */

public class ClassifierThread extends AsyncTask <double[], Void,String>{
    private final Instances data;
    private final Classifier classifier;
    private final MainActivity main;
    private File csvFile;
    private final int WINDOW_SIZE = 30;

    public ClassifierThread(Instances data, Classifier classifier, MainActivity main){
        this.data = data;
        this.classifier = classifier;
        this.main = main;
       /* csvFile = new File("../csvTrain.csv");
        if (!csvFile.exists()){
            try {
                csvFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            if (csvFile.exists()){
                initFile();
            }else{
                Log.d("Classifier", "FILE NOT EXIST");
            }

        } catch (IOException err) {
            Log.e("TrainActivity", "Unable to create train file", err);
        }*/

    }


    @Override
    protected String doInBackground(double[]... doubles) {
        double[] values = doubles[0];
        Preprocessor preprocessor = new Preprocessor(values);
        values = preprocessor.run();

        DenseInstance denseInstance = new DenseInstance(1.0, values);
        denseInstance.setDataset(data);
        String result = "";
        try {
            // Label it
            int label = (int) classifier.classifyInstance(denseInstance);
            Log.d("CLASSYFIED:", String.valueOf(label));
            result = data.classAttribute().value(label);
            Log.d("CLASSIFIED","LABEL: "+data.classAttribute().value(label) );

        } catch (Exception err) {
            Log.e("MainActivity", "Unable to classify", err);
        }

        return result;
    }



    @Override
    protected void onPostExecute(String s) {
        main.postResult(s);
        super.onPostExecute(s);
    }

    private class Preprocessor {
        private final double[] dataset;
        private double minAcc = 1, maxAcc = 1, minGyro = 1, maxGyro = 1;
        private double[] average = new double[(WINDOW_SIZE * 6) + 1];

        public Preprocessor(double[] dataset) {
            this.dataset = dataset;
        }

        public double[] run() {
            movingAverage();
            maxMin();
            normalize();

            return dataset;
        }

        public void movingAverage() {

            for (int i = 0; i < WINDOW_SIZE; i++) {

                if (i == 0) {
                    average[i] = dataset[0];
                    average[i + 1] = dataset[1];
                    average[i + 2] = dataset[2];
                    average[i + 3] = dataset[3];
                    average[i + 4] = dataset[4];
                    average[i + 5] = dataset[5];
                } else if (i == 1) {
                    average[(i * 6)] = ((dataset[(i * 6)] + dataset[0]) / 2);
                    average[(i * 6) + 1] = ((dataset[(i * 6) + 1] + dataset[1]) / 2);
                    average[(i * 6) + 2] = ((dataset[(i * 6) + 2] + dataset[2]) / 2);
                    average[(i * 6) + 3] = ((dataset[(i * 6) + 3] + dataset[3]) / 2);
                    average[(i * 6) + 4] = ((dataset[(i * 6) + 4] + dataset[4]) / 2);
                    average[(i * 6) + 5] = ((dataset[(i * 6) + 5] + dataset[5]) / 2);
                } else if (i == 2) {
                    average[(i * 6)] = ((dataset[(i * 6)] + dataset[6] + dataset[0]) / 3);
                    average[(i * 6) + 1] = ((dataset[(i * 6) + 1] + dataset[7] + dataset[1]) / 3);
                    average[(i * 6) + 2] = ((dataset[(i * 6) + 2] + dataset[8] + dataset[2]) / 3);
                    average[(i * 6) + 3] = ((dataset[(i * 6) + 3] + dataset[9] + dataset[3]) / 3);
                    average[(i * 6) + 4] = ((dataset[(i * 6) + 4] + dataset[10] + dataset[4]) / 3);
                    average[(i * 6) + 5] = ((dataset[(i * 6) + 5] + dataset[11] + dataset[5]) / 3);
                } else if (i == 3) {
                    average[(i * 6)] = ((dataset[(i * 6)] + dataset[(12)] + dataset[6] + dataset[0]) / 4);
                    average[(i * 6) + 1] = ((dataset[(i * 6) + 1] + dataset[13] + dataset[7] + dataset[1]) / 4);
                    average[(i * 6) + 2] = ((dataset[(i * 6) + 2] + dataset[14] + dataset[8] + dataset[2]) / 4);
                    average[(i * 6) + 3] = ((dataset[(i * 6) + 3] + dataset[15] + dataset[9] + dataset[3]) / 4);
                    average[(i * 6) + 4] = ((dataset[(i * 6) + 4] + dataset[16] + dataset[10] + dataset[4]) / 4);
                    average[(i * 6) + 5] = ((dataset[(i * 6) + 5] + dataset[17] + dataset[11] + dataset[5]) / 4);
                }
    /*else if(i==4){
        average[(i*6)]   = ((dataset[(i*6)]+dataset[18]+dataset[12]+dataset[6]+dataset[0])/5);
        average[(i*6)+1] = ((dataset[(i*6)]+dataset[19]+dataset[13]+dataset[7]+dataset[1])/5);
        average[(i*6)+2] = ((dataset[(i*6)]+dataset[20]+dataset[14]+dataset[8]+dataset[2])/5);
        average[(i*6)+3] = ((dataset[(i*6)]+dataset[21]+dataset[15]+dataset[9]+dataset[3])/5);
        average[(i*6)+4] = ((dataset[(i*6)]+dataset[22]+dataset[16]+dataset[10]+dataset[4])/5);
        average[(i*6)+5] = ((dataset[(i*6)]+dataset[23]+dataset[17]+dataset[11]+dataset[5])/5);
    }*/
                else {
                    average[(i * 6)] = ((dataset[(i * 6)] + dataset[((i - 1) * 6)] + dataset[((i - 2) * 6)] + dataset[((i - 3) * 6)] + dataset[((i - 4) * 6)]) / 5);
                    average[(i * 6) + 1] = ((dataset[(i * 6) + 1] + dataset[((i - 1) * 6) + 1] + dataset[((i - 2) * 6) + 1] + dataset[((i - 3) * 6) + 1] + dataset[((i - 4) * 6) + 1]) / 5);
                    average[(i * 6) + 2] = ((dataset[(i * 6) + 2] + dataset[((i - 1) * 6) + 2] + dataset[((i - 2) * 6) + 2] + dataset[((i - 3) * 6) + 2] + dataset[((i - 4) * 6) + 2]) / 5);
                    average[(i * 6) + 3] = ((dataset[(i * 6) + 3] + dataset[((i - 1) * 6) + 3] + dataset[((i - 2) * 6) + 3] + dataset[((i - 3) * 6) + 3] + dataset[((i - 4) * 6) + 3]) / 5);
                    average[(i * 6) + 4] = ((dataset[(i * 6) + 4] + dataset[((i - 1) * 6) + 4] + dataset[((i - 2) * 6) + 4] + dataset[((i - 3) * 6) + 4] + dataset[((i - 4) * 6) + 4]) / 5);
                    average[(i * 6) + 5] = ((dataset[(i * 6) + 5] + dataset[((i - 1) * 6) + 5] + dataset[((i - 2) * 6) + 5] + dataset[((i - 4) * 6) + 5] + dataset[((i - 4) * 6) + 5]) / 5);
                }
            }
        }

        public void maxMin() {

            for (int i = 0; i < WINDOW_SIZE; i++) {
                for (int j = 0; j < 6; j++) {
                    if (j < 3) {
                        if (minAcc > average[i * j]) {
                            minAcc = average[i * j];
                        }
                        if (maxAcc < average[i * j]) {
                            maxAcc = average[i * j];
                        }
                    } else {
                        if (minGyro > average[i * j]) {
                            minGyro = average[i * j];
                        }
                        if (maxGyro < average[i * j]) {
                            maxGyro = average[i * j];
                        }
                    }
                }
            }
          /*  System.out.println("MinAcc: "+minAcc);
            System.out.println("MaxAcc: "+maxAcc);
            System.out.println("MinGyro: "+minGyro);
            System.out.println("MaxGyro: "+maxGyro);*/
        }

        public void normalize() {
            // System.out.println("Normalized Data: ");
            for (int i = 0; i < WINDOW_SIZE; i++) {
                for (int j = 0; j < 6; j++) {
                    if (j < 3) {
                        dataset[(i * 6) + j] = ((((average[(i * 6) + j] - minAcc) / (maxAcc - minAcc)) * (100 - 0)) + 0);
                    } else {
                        dataset[(i * 6) + j] = ((((average[(i * 6) + j] - minGyro) / (maxGyro - minGyro)) * (100 - 0)) + 0);
                    }
                    // System.out.println(dataset[i+j]);

                }

            }
        }
    }

}
