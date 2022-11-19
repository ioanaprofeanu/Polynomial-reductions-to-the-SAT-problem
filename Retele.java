import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Class which implements the solving of the "Retele" task
 */
class Retele extends Task {
    /**
     * The social network's adjacency matrix
     */
    private boolean[][] socialNetwork;
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
     * The dimension of the searched clique, which represents
     * the dimension of the searched "good to expose adverts to" group
     */
    private int groupDimension;
    /**
     * The string representing the answer given by the oracle
     */
    private String oracleAnswer;
    /**
     * The list of valid vertices given by the oracle, which
     * represents the result list of good people
     */
    private List<Integer> oracleAnswerList;

    public Retele(final boolean[][] socialNetwork, final int noNetworkMembers,
                  final int noFriendships) {
        this.socialNetwork = socialNetwork;
        this.noNetworkMembers = noNetworkMembers;
        this.noFriendships = noFriendships;
    }

    public Retele() {}

    /**
     * Method which calls, in order, all the methods used to solve the task
     */
    @Override
    public void solve() throws IOException, InterruptedException {
        readProblemData();
        formulateOracleQuestion();
        askOracle();
        decipherOracleAnswer();
        writeAnswer();
    }

    /**
     * Using the data read from the input, initialise the problem's data
     * @param n the number of vertices
     * @param m the number of edges
     * @param k the number of vertices in clique
     */
    private void initialiseData(int n, int m, int k) {
        noNetworkMembers = n;
        noFriendships = m;
        groupDimension = k;
        socialNetwork = new boolean[n + 1][n + 1];
    }

    /**
     * Read the data from stdin
     */
    @Override
    public void readProblemData() {
        Scanner reader = new Scanner(System.in);
        int n = reader.nextInt();
        int m = reader.nextInt();
        int k = reader.nextInt();
        initialiseData(n, m, k);

        // for each edge, consider that the graph is undirected
        for (int i = 0; i < noFriendships; i++) {
            int u = reader.nextInt();
            int v = reader.nextInt();
            socialNetwork[u][v] = true;
            socialNetwork[v][u] = true;
        }
    }

    /**
     * Formulate the question for the Oracle
     */
    @Override
    public void formulateOracleQuestion() throws IOException {
        // start writing in the "sat.cnf" file
        BufferedWriter writer = new BufferedWriter
                (new FileWriter(Constants.RETELE_SAT + Constants.CNF_EXTENSION));

        //  the number of variables used within the question for the oracle
        int noUsedVariables = noNetworkMembers * groupDimension;

        // calculate the total number of clauses for the three clauses
        int noClausesFistCase = groupDimension;
        int noClausesSecondCase = groupDimension * (groupDimension - 1) *
                ((noNetworkMembers * (noNetworkMembers - 1) / 2) - noFriendships);
        int noClausesThirdCase = (groupDimension * (groupDimension - 1) / 2)
                * noNetworkMembers;

        int totalClauses = noClausesFistCase + noClausesSecondCase + noClausesThirdCase;
        writer.write(Constants.P_CNF + Constants.SPACE
                + noUsedVariables + Constants.SPACE + totalClauses + Constants.NEW_LINE);

        // for each vertex within the clique, call the three clauses methods
        for (int i = 1; i <= groupDimension; i++) {
            firstClauseCase(writer, i);
            secondClauseCase(writer, i);
            thirdClauseCase(writer, i);
        }

        writer.close();
    }

    /**
     * The first clause case, regarding the existence of a clique vertex within the
     * graph, which implies that each vertex of the clique has to be one of the vertices
     * of the graph
     * @param writer the file writer
     * @param i the index of the current vertex within the clique
     */
    public void firstClauseCase(BufferedWriter writer, int i) throws IOException {
        // the ith vertex within the clique could be one of the vertices within the graph
        // thus, iterate through all the vertices of the graph and write the current clause
        for (int v = 1; v <= noNetworkMembers; v++) {
            // encode the variables as a number from 1 to noNetworkMembers * groupDimension
            int varCodification = (i - 1) * noNetworkMembers + (v - 1) + 1;
            writer.write(varCodification + Constants.SPACE);
        }
        // the current clause is over
        writer.write(Constants.ZERO + Constants.NEW_LINE);

    }

    /**
     * The second clause case, regarding that for each non-edge, one of the vertices
     * is not within the clique (since in the clique, all vertices are connected)
     * @param writer the file writer
     * @param i the index of the current vertex within the clique
     */
    public void secondClauseCase(BufferedWriter writer, int i) throws IOException {
        // for any other vertex within the clique different from the current one
        for (int j = 1; j <= groupDimension; j++) {
            if (i != j) {
                // for each two different vertices within the graph
                for (int v = 1; v < noNetworkMembers; v++) {
                    for (int w = v + 1; w <= noNetworkMembers; w++) {
                        // if there is no edge between the vertices
                        if (!socialNetwork[v][w]) {
                            // write the clause implying that the vertices
                            // cannot both be part of the clique at the same time
                            // encode the variables as a number from 1 to
                            // noNetworkMembers * groupDimension
                            int firstVarCodification =
                                    -((i - 1) * noNetworkMembers + (v - 1) + 1);
                            int secondVarCodification =
                                    -((j - 1) * noNetworkMembers + (w - 1) + 1);
                            writer.write(firstVarCodification + Constants.SPACE
                                    + secondVarCodification + Constants.SPACE);
                            // the current clause is over
                            writer.write(Constants.ZERO + Constants.NEW_LINE);
                        }
                    }

                }
            }
        }
    }

    /**
     * The third clause case, regarding that a node within the graph cannot be
     * on two different positions within the clique, at the same time
     * @param writer the file writer
     * @param i the index of the current vertex within the clique
     */
    public void thirdClauseCase(BufferedWriter writer, int i) throws IOException {
        if (i < groupDimension) {
            // for any other vertex within the clique different from the current one
            for (int j = i + 1; j <= groupDimension; j++) {
                // for any vertex within the graph
                for (int v = 1; v <= noNetworkMembers; v++) {
                    // write the clause implying a vertex can appear only once
                    // within the clique
                    // encode the variables as a number from 1 to
                    // noNetworkMembers * groupDimension
                    int firstVarCodification =
                            -((i - 1) * noNetworkMembers + (v - 1) + 1);
                    int secondVarCodification =
                            -((j - 1) * noNetworkMembers + (v - 1) + 1);
                    writer.write(firstVarCodification + Constants.SPACE + secondVarCodification + Constants.SPACE);
                    // the current clause is over
                    writer.write(Constants.ZERO + Constants.NEW_LINE);
                }
            }
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
            oracleAnswerList = new ArrayList<>();
            for (int i = 0; i < noValues; i++) {
                int value = reader.nextInt();
                // if the value is positive
                if (value > 0) {
                    // decode the answer by reversing the encoding process
                    // and add the result vertex in the result list
                    if (value % noNetworkMembers != 0) {
                        oracleAnswerList.add(value % noNetworkMembers);
                    } else {
                        oracleAnswerList.add(noNetworkMembers);
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
        // if the Oracle's answer is false
        if (oracleAnswer.equals(Constants.FALSE)) {
            System.out.print(oracleAnswer);
        } else {
            // is the answer is true, write the list of result vertices
            System.out.println(oracleAnswer);
            for (int i = 0; i < groupDimension; i++) {
                System.out.print(oracleAnswerList.get(i));
                System.out.print(" ");
            }
        }
    }

    public void setGroupDimension(int groupDimension) {
        this.groupDimension = groupDimension;
    }

    public String getOracleAnswer() {
        return oracleAnswer;
    }

    /**
     * The main method for running the program
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        // create an instance of the task's class and call the solve method
        Retele retele = new Retele();
        retele.solve();
    }
}