import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

class Reclame extends Task {
    /**
     * The social network's adjacency matrix, with its values negated
     * (false means there is an edge, true means there is not an edge),
     * associated with the complementary graph
     */
    private boolean[][] complementarySocialNetwork;
    /**
     * Number of vertices of the social network, which represents
     * the number of members of the network
     */
    private int noNetworkMembers;
    /**
     * Number of edges of the social network, which represents
     * the number of friendship relationship between the
     * members of the network
     */
    private int noFriendships;
    /**
     * The dimension of the essential group
     */
    private int finalGroupDimension;
    /**
     * The string representing the answer given by the oracle
     */
    private String oracleAnswer;
    /**
     * The list of essential people, resulted by eliminating the people
     * within the Oracle's result
     */
    private List<Integer> oracleAnswerList;

    /**
     * Method which calls, in order, all the methods used to solve the task
     */
    @Override
    public void solve() throws IOException, InterruptedException {
        readProblemData();
        formulateOracleQuestion();
        decipherOracleAnswer();
        writeAnswer();
    }

    /**
     * Using the data read from the input, initialise the problem's data
     * @param n the number of vertices
     * @param m the number of edges
     */
    private void initialiseData(int n, int m) {
        noNetworkMembers = n;
        noFriendships = m;
        finalGroupDimension = 0;
        complementarySocialNetwork = new boolean[n + 1][n + 1];
    }

    /**
     * Read the data from stdin
     */
    @Override
    public void readProblemData() {
        Scanner reader = new Scanner(System.in);
        int n = reader.nextInt();
        int m = reader.nextInt();
        initialiseData(n, m);

        // initialise the adjacency with true values
        for (boolean[] row : complementarySocialNetwork) {
            Arrays.fill(row, true);
        }

        // for each edge, consider that the graph is undirected
        for (int i = 0; i < noFriendships; i++) {
            int u = reader.nextInt();
            int v = reader.nextInt();
            // if the edge exists, add false
            complementarySocialNetwork[u][v] = false;
            complementarySocialNetwork[v][u] = false;
        }
    }

    /**
     * Formulate the question for the oracle
     */
    @Override
    public void formulateOracleQuestion() throws IOException, InterruptedException {
        // start writing in the "sat.cnf" file; we will use a new instance of Retele which has the
        // data of our current problem and use it to formulate questions to the Oracle
        Retele reduceToRetele = new Retele(complementarySocialNetwork, noNetworkMembers,
                noNetworkMembers * (noNetworkMembers - 1) / 2 - noFriendships);

        // we will iterate though all the possible values of k (by finding the maximum dimension
        // of the current complementary graph
        for (int k = noNetworkMembers; k >= 2; k--) {
            // set the group dimension
            reduceToRetele.setGroupDimension(k);
            // contact the Oracle
            reduceToRetele.formulateOracleQuestion();
            reduceToRetele.askOracle();
            reduceToRetele.decipherOracleAnswer();
            this.oracleAnswer = reduceToRetele.getOracleAnswer();
            // the Oracle's answer is true, we found our solution;
            // otherwise, continue iteration
            if (oracleAnswer.equals(Constants.TRUE)) {
                return;
            }
        }
    }

    /**
     * Add all the graph's nodes to the list of answers
     */
    public void initialiseOracleAnswerList() {
        oracleAnswerList = new ArrayList<>();
        for (int i = 1; i <= noNetworkMembers; i++) {
            oracleAnswerList.add(i);
        }
    }

    /**
     * Decipher the answer from the Oracle
     */
    @Override
    public void decipherOracleAnswer() throws IOException {
        // read the answer from "sat.sol"
        Scanner reader = new Scanner(new File(Constants.ORACLE_SOL));
        // keep the Oracle's answer
        oracleAnswer = reader.nextLine();

        // if the given case is a success
        if (oracleAnswer.equals(Constants.TRUE)) {
            // read the list of values
            int noValues = reader.nextInt();
            initialiseOracleAnswerList();
            for (int i = 0; i < noValues; i++) {
                int value = reader.nextInt();
                // if the value is positive
                if (value > 0) {
                    // decode the answer by reversing the encoding process, and
                    // eliminate the found node from the answers list; the result
                    // will be consisted of the remaining nodes
                    if (value % noNetworkMembers != 0) {
                        oracleAnswerList.remove(oracleAnswerList.indexOf(value % noNetworkMembers));
                    } else {
                        oracleAnswerList.remove(oracleAnswerList.indexOf(noNetworkMembers));
                    }
                }
            }
        }
    }

    /**
     * Write the answer
     */
    @Override
    public void writeAnswer() {
        // is the answer is true, write the list of result vertices
        for (int i = 0; i < oracleAnswerList.size(); i++) {
            System.out.print(oracleAnswerList.get(i));
            if (i < oracleAnswerList.size() - 1) {
                System.out.print(" ");
            }
        }
    }

    /**
     * The main method for running the program
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        // create an instance of the task's class and call the solve method
        Reclame reclame = new Reclame();
        reclame.solve();
    }
}