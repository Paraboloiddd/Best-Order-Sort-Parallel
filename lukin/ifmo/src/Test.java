import java.io.File;
import java.io.IOException;

/**
 * @author Paraboloiddd
 */
public class Test {
    public static void main(String[] args) throws IOException {

        double startTime = System.currentTimeMillis();
        BestOrderSort bestOrderSort = new BestOrderSort(10, Solution.parseSolutionsFromFile(new File("/home/paraboloid/IdeaProjects/Best Order Sort/lukin/ifmo/TestCases/10000_10"), 10));
        bestOrderSort.compute(true, false);
        bestOrderSort.showResults();
        System.out.println("Spent time: " + (System.currentTimeMillis() - startTime));

    }
}
