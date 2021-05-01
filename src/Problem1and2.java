import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Scanner; // Import the Scanner class to read text files


public class Problem1and2 {
	static ArrayList<double[]> trainingMatrix;
	static ArrayList<double[]> validationMatrix;
	static ArrayList<double[]> testMatrix;
	static ArrayList<double[]> projectionMatrix;
	public static void main(String []args){
        System.out.println("Hello World");
        Scanner trainingData = readFile("C:\\Users\\Michael\\eclipse-workspace\\CSE 151A Project1\\src\\pa1train.txt");
        Scanner testData = readFile("C:\\Users\\Michael\\eclipse-workspace\\CSE 151A Project1\\src\\pa1test.txt");
        Scanner validationData = readFile("C:\\Users\\Michael\\eclipse-workspace\\CSE 151A Project1\\src\\pa1validate.txt");
        Scanner projectionData = readFile("C:\\Users\\Michael\\eclipse-workspace\\CSE 151A Project1\\src\\projection.txt");
        projectionMatrix = getMatrixDouble(projectionData);
        trainingMatrix = getMatrixDouble(trainingData);
        testMatrix = getMatrixDouble(testData);
        validationMatrix = getMatrixDouble(validationData);
        ArrayList<double[]> pTrainingMatrix = matrixMultiplication(trainingMatrix,projectionMatrix);
        ArrayList<double[]> pValidationMatrix = matrixMultiplication(validationMatrix,projectionMatrix);
        ArrayList<double[]> pTestMatrix = matrixMultiplication(testMatrix,projectionMatrix);
        int[] possibleKs = new int[] {1,5,9,15};

        getErrors(possibleKs,trainingMatrix,"training error(pre-projected)",trainingMatrix);
        getErrors(possibleKs,trainingMatrix,"validation error(pre-projected)",validationMatrix);
        getErrors(new int[] {1},trainingMatrix,"test error(pre-projected)",testMatrix);
        getErrors(possibleKs,pTrainingMatrix,"training error(projected)",pTrainingMatrix);
        getErrors(possibleKs,pTrainingMatrix,"validation error(projected)",pValidationMatrix);
        getErrors(new int[] {5},pTrainingMatrix,"test error(projected)",pTestMatrix);

        
        
	}
	public static ArrayList<double[]> matrixMultiplication(ArrayList<double[]> M,ArrayList<double[]> N) {
		// length - 1 because the last element is the label
		if(M == null || N == null || M.get(0).length - 1 != N.size() ) {
			return null;
		}
		ArrayList<double[]> newMatrix = new ArrayList<>(M.size());
		for(int i = 0; i < M.size();i++) {

			double[] row = new double[N.get(0).length + 1];
			for(int j = 0; j < N.get(0).length;j++) {
				double sum = 0;

				for(int k = 0; k < N.size();k++) {
					//System.out.println(M.get(i)[k] + " " + N.get(k)[j] + " sum " + sum);
					sum += (M.get(i)[k] * N.get(k)[j]);
				}
				//System.out.println("summ " + sum);
				row[j] = sum;
			}
			row[N.get(0).length] = M.get(i)[M.get(i).length - 1];
//			System.out.println("done");
//			for(int k = 0; k < row.length;k++) {
//				System.out.print(row[k] + " ");
//			}
//			System.out.println();
			newMatrix.add(row);
		}
//		for(int i = 0; i < newMatrix.size();i++){
//			System.out.println(newMatrix.get(i)[20]);
//		}
		return newMatrix;
	}
	
	
	public static void getErrors(int[] possibleKs,ArrayList<double[]> dataMatrix,String nameOfError,ArrayList<double[]> testMatrix) {
		for(int possibleK = 0;possibleK < possibleKs.length; possibleK++) {
        	PriorityQueue[] results = kNearestClassifier(possibleKs[possibleK],dataMatrix,testMatrix);
        	int error = 0;
        	for(int i = 0; i < results.length;i++) {
        		PriorityQueue<double[]> pq = results[i];
        		HashMap<Double,Integer> countMap = new HashMap<Double,Integer>();
        		//System.out.println("here");
        		for(int j = 0; j < possibleKs[possibleK]; j++) {
        			double[] arrDistanceLabel = pq.poll();
        			//System.out.println(arrDistanceLabel[0] + " " + arrDistanceLabel[1] + " j " + j);
        			if(countMap.containsKey(arrDistanceLabel[1])) {
        				countMap.put(arrDistanceLabel[1], countMap.get(arrDistanceLabel[1]) + 1);
        			}else {
        				countMap.put(arrDistanceLabel[1], 1);
        			}
        		}
        		double label = Collections.max(countMap.entrySet(), (entry1, entry2) -> entry1.getValue() - entry2.getValue()).getKey();
        		if(label != testMatrix.get(i)[testMatrix.get(i).length - 1]) {
        			//System.out.println("label " + label + " actual " + testMatrix.get(i)[testMatrix.get(i).length - 1]);
        			/*for(double p =0; p < 10;p++) {
        				System.out.print(p + " " + countMap.get(p) + " ");	
        			}*/
        			//System.out.println();
  
        			
        			error += 1;
        		}
        	}
        System.out.println("k " + possibleKs[possibleK] + " " + nameOfError + " " + ((double)error/(double)testMatrix.size()));
        }
	}
	
	public static PriorityQueue[] kNearestClassifier(int k,ArrayList<double[]> baseData,ArrayList<double[]> testData) {
		PriorityQueue[] testResults = new PriorityQueue[testData.size()];
		for(int i = 0; i < testResults.length;i++) {
			//System.out.println("i " + i);
			double[] testFeature = testData.get(i);
			PriorityQueue<double[] > pq=
	                new PriorityQueue(k, (a, b) -> (int)(((double[])b)[0] - ((double[])a)[0]));
			testResults[i] = pq;
			for(int j = 0; j < baseData.size();j++) {
				double[] baseFeature = baseData.get(j);
				double distance = getDistance(baseFeature,testFeature);
				//System.out.println("distance "+ distance);
				if(pq.size() < k) {
					double[] arr = new double[]{distance,baseFeature[baseFeature.length - 1]};
					pq.add(arr);
				}else {
					double[] pqArray = pq.peek();
					//System.out.println("pqArray[0] " + pqArray[0] + " distance " + distance);
					if(pqArray[0] > distance) {
						pq.poll();
						double[] arr = new double[]{distance,baseFeature[baseFeature.length - 1]};
						pq.add(arr);
					}
				}
			}
		}
		
		
		return testResults;
	
	}
	public static double getDistance(double[] feature1,double[] feature2) {
		double totalDistance = 0;
		for(int i = 0; i < feature1.length - 1; i++) {
		
			totalDistance += Math.pow(feature2[i] - feature1[i],2);
		}

		return totalDistance;
	}
	
	public static ArrayList<double[]> getMatrixDouble(Scanner data) {
		ArrayList<double[]> matrix = new ArrayList<>();
		while(data.hasNextLine()) {
			String dataString = data.nextLine();
			String[] dataArray = dataString.split(" ");
			double[] n1 = new double[dataArray.length];
			for(int i = 0; i < dataArray.length; i++) {
			   n1[i] = Double.parseDouble(dataArray[i]);
			}
			matrix.add(n1);
			
		}
		return matrix;
	}
	
	public static ArrayList<int[]> getMatrixInt(Scanner data) {
		ArrayList<int[]> matrix = new ArrayList<>();
		while(data.hasNextLine()) {
			String dataString = data.nextLine();
			String[] dataArray = dataString.split(" ");
			int[] n1 = new int[dataArray.length];
			for(int i = 0; i < dataArray.length; i++) {
			   n1[i] = Integer.parseInt(dataArray[i]);
			}
			matrix.add(n1);
			
		}
		return matrix;
	}
	
	
	public static Scanner readFile(String fileName) {
		try {
		      File myObj = new File(fileName);
		      Scanner myReader = new Scanner(myObj);
		      return myReader;
		}
		catch(FileNotFoundException e) {
			System.out.println("An error occured.");
			e.printStackTrace();
			return null;
		}
	}
	
	
}
