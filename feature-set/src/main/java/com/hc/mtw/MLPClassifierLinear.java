package com.hc.mtw;

import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.split.FileSplit;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * "Linear" Data Classification Example
 * <p>
 * Based on the data from Jason Baldridge:
 * https://github.com/jasonbaldridge/try-tf/tree/master/simdata
 *
 * @author Josh Patterson
 * @author Alex Black (added plots)
 */
public class MLPClassifierLinear {
    protected int seed;
    protected double learningRate;
    protected int batchSize;
    protected int nEpochs;

    protected int numInputs;
    protected int numOutputs;
    protected int numHiddenNodes;

    protected int numLabels;
    protected int labelIdx;

    protected double threshold;

    public MLPClassifierLinear(int seed, double learningRate, int batchSize, int nEpochs, int numInputs, int numOutputs, int numHiddenNodes, int numLabels, int labelIdx, double threshold) {
        this.seed = seed;
        this.learningRate = learningRate;
        this.batchSize = batchSize;
        this.nEpochs = nEpochs;
        this.numInputs = numInputs;
        this.numOutputs = numOutputs;
        this.numHiddenNodes = numHiddenNodes;
        this.numLabels = numLabels;
        this.labelIdx = labelIdx;
        this.threshold = threshold;
    }

    public DataSetIterator train(String trainFile, String modelFile) throws IOException, InterruptedException {
        DataSetIterator iter = loadTrainData(trainFile);
        MultiLayerNetwork model = new MultiLayerNetwork(makeConf());
        model.init();
        model.setListeners(new ScoreIterationListener(100));  //Print score every 100 parameter updates
        for (int n = 0; n < nEpochs; n++) {
            model.fit(iter);
        }
        ModelSerializer.writeModel(model, modelFile, true);
        return iter;
    }

    private List<List<Map.Entry<INDArray, INDArray>>> makeEvalData(String evalFile) throws IOException, InterruptedException {
        List<List<Map.Entry<INDArray, INDArray>>> featureLabels = new ArrayList<>();
        List<Map.Entry<INDArray, INDArray>> featureLabel = null;

        DataSetIterator iter = loadEvalData(evalFile);

        while (iter.hasNext()) {
            DataSet t = iter.next();
            INDArray features = t.getFeatureMatrix();
            INDArray labels = t.getLabels();
            int numOfFeatures = features.size(0);
            for (int rowIdx = 0; rowIdx < numOfFeatures; rowIdx++) {
                INDArray feature = features.getRow(rowIdx);
                INDArray label = labels.getRow(rowIdx);
                if (label.getDouble(0) == 1.d) {
                    featureLabel = new ArrayList<>();
                    featureLabels.add(featureLabel);
                }

                if (featureLabel != null) featureLabel.add(new AbstractMap.SimpleEntry<>(feature, label));
            }
        }

        return featureLabels;
    }

    public Evaluation evaluate(MultiLayerNetwork model, String evalFile) throws IOException, InterruptedException {
        Evaluation eval = new Evaluation(numOutputs);

        List<List<Map.Entry<INDArray, INDArray>>> featureLabels = makeEvalData(evalFile);

        int urlIdx = 0;
        for (List<Map.Entry<INDArray, INDArray>> fls : featureLabels) {
            urlIdx++;
            INDArray pnameElement = null;
            INDArray priceElement = null;
            INDArray descElement = null;
            Set<INDArray> noneElement = new HashSet<>();

            double maxPnameScore = 0.d;
            double maxPriceScore = 0.d;
            double maxDescScore = 0.d;

            INDArray sparePnameElement = null;
            INDArray sparePriceElement = null;
            INDArray spareDescElement = null;

            double maxSparePnameScore = 0.d;
            double maxSparePriceScore = 0.d;
            double maxSpareDescScore = 0.d;

            for (Map.Entry<INDArray, INDArray> fl : fls) {
                INDArray feature = fl.getKey();
                INDArray label = fl.getValue();
                INDArray predicted = model.output(feature, false);
                int idx = getIndexOfMaximumValue(predicted);
                double score = predicted.getDouble(idx);
                noneElement.add(label);

                switch (idx) {
                    case 0:
                        if (maxPnameScore < score) {
                            pnameElement = label;
                            maxPnameScore = score;
                        }
                        break;
                    case 1:
                        if (maxPriceScore < score) {
                            priceElement = label;
                            maxPriceScore = score;
                        }
                        break;
                    case 2:
                        if (maxDescScore < score) {
                            descElement = label;
                            maxDescScore = score;
                        }
                        break;
                    default:
                        int secondIdx = getIndexOfSecondMaximumValue(predicted);
                        double secondIdxScore = predicted.getDouble(secondIdx);
                        switch (secondIdx) {
                            case 0:
                                if (maxSparePnameScore < secondIdxScore) {
                                    maxSparePnameScore = secondIdxScore;
                                    sparePnameElement = label;
                                }
                                break;
                            case 1:
                                if (maxSparePriceScore < secondIdxScore) {
                                    maxSparePriceScore = secondIdxScore;
                                    sparePriceElement = label;
                                }
                                break;
                            case 2:
                                if (maxSpareDescScore < secondIdxScore) {
                                    maxSpareDescScore = secondIdxScore;
                                    spareDescElement = label;
                                }
                                break;
                        }
                }
            }

            if (pnameElement == null && sparePnameElement != null && maxSparePnameScore > threshold) {
                pnameElement = sparePnameElement;
            }
            if (priceElement == null && sparePriceElement != null && maxSparePriceScore > threshold) {
                priceElement = sparePriceElement;
            }
            if (descElement == null && spareDescElement != null && maxSpareDescScore > threshold) {
                descElement = spareDescElement;
            }

            int correctCount = 0;
            if (pnameElement != null) {
                int pnameIndex = getIndexOfMaximumValue(pnameElement);
                if (pnameIndex != -1) {
                    eval.eval(0, pnameIndex);
                    noneElement.remove(pnameElement);
                }
                if (pnameIndex == 0) correctCount += 100;
            }

            if (priceElement != null) {
                int priceIndex = getIndexOfMaximumValue(priceElement);
                if (priceIndex != -1) {
                    eval.eval(1, priceIndex);
                    noneElement.remove(priceElement);
                }
                if (priceIndex == 1) correctCount += 10;
            }

            if (descElement != null) {
                int descIndex = getIndexOfMaximumValue(descElement);
                if (descIndex != -1) {
                    eval.eval(2, descIndex);
                    noneElement.remove(descElement);
                }
                if (descIndex == 2) correctCount += 1;
            }

            for (INDArray label : noneElement) {
                eval.eval(3, getIndexOfMaximumValue(label));
            }

            System.out.printf("%d:%d\n", urlIdx, correctCount);

//            eval.eval(resultLabels, predicts);
        }

        return eval;
    }

    protected MultiLayerConfiguration makeConf() {
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
            .seed(seed)
            .iterations(1)
            .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
            .learningRate(learningRate)
            .updater(Updater.NESTEROVS)     //To configure: .updater(new Nesterovs(0.9))
            .regularization(true).l2(1e-4)
            .list()
            .layer(0, new DenseLayer.Builder().nIn(numInputs).nOut(numHiddenNodes)
                .weightInit(WeightInit.XAVIER)
                .activation(Activation.RELU)
                .build())
            .layer(1, new OutputLayer.Builder(LossFunction.NEGATIVELOGLIKELIHOOD)
                .weightInit(WeightInit.XAVIER)
                .activation(Activation.SOFTMAX).weightInit(WeightInit.XAVIER)
                .nIn(numHiddenNodes).nOut(numOutputs).build())
            .pretrain(false).backprop(true).build();

        return conf;
    }

    protected static int getIndexOfMaximumValue(INDArray array) {
        try {
            double max = 0.d;
            int index = 0;
            for (int idx = 0; idx < array.columns(); idx++) {
                double value = array.getDouble(idx);
                if (max < value) {
                    max = value;
                    index = idx;
                }
            }

            return index;
        } catch (Exception e) {
            return -1;
        }
    }

    protected static int getIndexOfSecondMaximumValue(INDArray array) {
        try {
            int maxIndex = getIndexOfMaximumValue(array);
            double max = 0.d;
            int index = 0;
            for (int idx = 0; idx < array.columns(); idx++) {
                if (idx == maxIndex) continue;

                double value = array.getDouble(idx);
                if (max < value) {
                    max = value;
                    index = idx;
                }
            }

            return index;
        } catch (Exception e) {
            return -1;
        }
    }

    //Load the training data:
    private DataSetIterator loadTrainData(String file) throws IOException, InterruptedException {
        RecordReader rr = new CSVRecordReader();
        rr.initialize(new FileSplit(new File(file)));
        return new RecordReaderDataSetIterator(rr, batchSize, labelIdx, numLabels);
    }

    //Load the test/evaluation data:
    public DataSetIterator loadEvalData(String file) throws IOException, InterruptedException {
        RecordReader rrTest = new CSVRecordReader();
        rrTest.initialize(new FileSplit(new File(file)));
        return new RecordReaderDataSetIterator(rrTest, batchSize, labelIdx, numLabels);
    }

    public static void main(String[] args) throws Exception {
        int seed = 123;
        double learningRate = 0.005;
        int batchSize = 8;
        int nEpochs = 20;

        int numInputs = 20;
        int numOutputs = 4;
        int numHiddenNodes = 30;

        int numLabels = 4;
        int labelIdx = 0;

        final double threshold = 0.001d;

        final String filenameTrain = "/Users/haimjoon/IdeaProjects/da-mtw/feature-set/train.csv";
        final String filenameEval = "/Users/haimjoon/IdeaProjects/da-mtw/feature-set/eval.csv";
        final String modelFile = "/Users/haimjoon/IdeaProjects/da-mtw/feature-set/model.zip";

        MLPClassifierLinear linear = new MLPClassifierLinear(seed, learningRate, batchSize, nEpochs, numInputs, numOutputs, numHiddenNodes, numLabels, labelIdx, threshold);

//        DataSetIterator trainIter = linear.train(filenameTrain, modelFile);

        MultiLayerNetwork model = ModelSerializer.restoreMultiLayerNetwork(modelFile);

        System.out.println("Evaluate model....");
        Evaluation evaluation = linear.evaluate(model, filenameEval);

        //Print the evaluation statistics
        System.out.println(evaluation.stats());
    }
}
