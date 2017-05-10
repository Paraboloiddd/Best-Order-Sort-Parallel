import java.lang.reflect.Array;
import java.util.*;

/**
 * @author Paraboloiddd
 */
public class BestOrderSort {
    private final int n; //population size
    private final int m; //objective size
    private final Solution[] solutions; //all solutions
    private final List<Solution>[][] l; //solutions that were found on j objective with rank r
    private final Map<Solution, Integer> r; // rank of solution
    private int sc; //number of solutions done
    private int rc; //number of max front
    private final Map<Solution, Boolean> isRanked;
    private final Map<Solution, Set<Integer>> c; //comparison set
    private final Map<Integer, Solution[]> q;

    public BestOrderSort(int m, Solution[] solutions) {
        this.m = m;
        this.solutions = solutions;
        this.n = solutions.length;
        l = (ArrayList<Solution>[][]) Array.newInstance(ArrayList.class, m, n);
        r = new HashMap<>(n);
        sc = 0; rc = -1;
        c = new HashMap<>(n);
        isRanked = new HashMap<>(n);
        for (Solution s : solutions) {
            Set<Integer> objectives = new HashSet<>();
            for (int i = 0; i < m; i++) {
                objectives.add(i);
            }
            c.put(s, objectives);
            isRanked.put(s, false); // all solutions are not ranked yet
        }
        q = new HashMap<>();
    }

    public void compute(boolean parallel, boolean debug) {
        Solution[] q0 = Arrays.copyOf(solutions, n);
        Arrays.parallelSort(q0, new Comparator<Solution>() { // TODO compare time for parallel and sequential versions
            @Override
            public int compare(Solution s1, Solution s2) {
                if (s1.getObjectiveValues()[0] == s2.getObjectiveValues()[0]) {
                    for (int i = 1; i < m; i++) { // use other objectives to compare
                        if (s1.getObjectiveValues()[i] != s2.getObjectiveValues()[i]) {
                            return s1.getObjectiveValues()[i] > s2.getObjectiveValues()[i] ? 1 : -1;
                        }
                    }
                    return 0;
                } else {
                    return s1.getObjectiveValues()[0] > s2.getObjectiveValues()[0] ? 1 : -1;
                }
            }
        });
        q.put(0, q0);

        final HashMap<Solution, Integer> solToIndex = new HashMap<>(n); // using q0 we can find out which solution is greater
        for (int i = 0; i < n; i++) {
            solToIndex.put(q0[i], i); // we need quick access to the index by solution
        }

        for (int i = 1; i < m; i++) { // sort solutions by other objectives
            final int fI = i;
            Solution[] qI = Arrays.copyOf(solutions, n);
            Arrays.parallelSort(qI, new Comparator<Solution>() { // TODO compare time for parallel and sequential versions
                @Override
                public int compare(Solution s1, Solution s2) {
                    if (s1.getObjectiveValues()[fI] != s2.getObjectiveValues()[fI]) {
                        return s1.getObjectiveValues()[fI] > s2.getObjectiveValues()[fI] ? 1 : -1;
                    } else {
                        return solToIndex.get(s1) > solToIndex.get(s2) ? 1 : -1;
                    }
                }
            });
            q.put(fI, qI);
        }

        for (int i = 0; i < n; i++) { // for all solutions
            for (int j = 0; j < m; j++) { // for all sorted set
                Solution s = q.get(j)[i];
                c.get(s).remove(j); // reduce comparison set
                if (isRanked.get(s)) {
                    if (l[j][r.get(s)] == null) {
                        l[j][r.get(s)] = new ArrayList<>();
                    }
                    l[j][r.get(s)].add(s);
                } else {
                    findRank(s, j); // find rank of s using l(j)
                    isRanked.put(s, true);
                    sc++;
                }
            }
            if (sc == n) {
                if (debug) {
                    for (int j = 0; j < n; j++) {
                        System.out.println("Solution " + (j+1) + " has rank " + (r.get(solutions[j]) + 1));
                    }
                }
                break;
            }
        }
    }

    private void findRank(Solution s, int j) {
        boolean done = false;
        for (int k = 0; k < rc; k++) {
            boolean check = false;
            if (l[j][k] != null) {
                for (Solution t : l[j][k]) {
                    check = dominationCheck(s, t);
                    if (check) {
                        break;
                    }
                }
            }

            if (!check) { // s is not dominated by any solution of rank k. So r(s) is k.
                r.put(s, k);
                done = true;
                if (l[j][k] == null) {
                    l[j][k] = new ArrayList<>();
                }
                l[j][k].add(s);
                break;
            }
        }
        if (!done) { // TODO can we use !check here?
            rc++; // new front appears
            r.put(s, rc);
            if (l[j][rc] == null) { // TODO can we remove this check?
                l[j][rc] = new ArrayList<>();
            }
            l[j][rc].add(s);
        }
    }

    private boolean dominationCheck(Solution s, Solution t) { // checks if t dominates s. Uses objectives in c(t) for it
        for (int j : c.get(t)) {
            if (s.getObjectiveValues()[j] < t.getObjectiveValues()[j]) {
                return false;
            }
        }
        return true;
    }

    public void showResults() {
        int[] ranks = new int[n];
        for (Map.Entry<Solution, Integer> entry : r.entrySet()) {
            ranks[entry.getValue()]++;
        }
        int i = 0;
        while (ranks[i] != 0) {
            System.out.println("rank " + (i+1) + ": " + ranks[i] + " solutions");
            i++;
        }
    }
}
