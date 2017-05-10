import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * @author Paraboloiddd
 */
public class Solution {
    private double[] objectiveValues;

    public Solution(double[] objectiveValues) {
        this.objectiveValues = objectiveValues;
    }

    public double[] getObjectiveValues() {
        return objectiveValues;
    }

    public static Solution[] parseSolutionsFromFile(File filePath, int objectiveCount) {
        try {
            Scanner sc = new Scanner(filePath);
            ArrayList<Solution> solutionsList = new ArrayList<>();
            while (sc.hasNextDouble()) {
                double[] values = new double[objectiveCount];
                for (int i = 0; i < objectiveCount; i++) {
                    values[i] = sc.nextDouble();
                }
                solutionsList.add(new Solution(values));
            }
            return solutionsList.toArray(new Solution[solutionsList.size()]);
        } catch (FileNotFoundException e) {
            System.out.println("File not found.");
        } catch (Exception e) {
            System.out.println("Error while parsing data from file.\nYour file must contain an amount of doubles multiple of objective count.");
        }

        return null; // in case of fail
    }
}
