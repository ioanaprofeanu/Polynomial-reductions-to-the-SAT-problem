import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

class Registre extends Task {
    /**
     * The variables' adjacency matrix
     */
    private boolean[][] variablesAdjacencyMatrix;
    /**
     * Number of vertices of the variables' matrix, which represents
     * the number of variables
     */
    private int noVariables;
    /**
     * Number of edges of the variables' matrix, which represents
     * the number of relations between variables
     */
    private int noVariablesRelations;
    /**
     * The number of registers to which the variables have to be
     * assigned to
     */
    private int noRegisters;
    /**
     * The string representing the answer given by the oracle
     */
    private String oracleAnswer;
    /**
     * The map of variables and registers, with variable as key and
     * register as value
     */
    private Map<Integer, Integer> oracleAnswerMap;

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
     * @param n the number of variables (vertices)
     * @param m the number of edges (vertices relations)
     * @param k the number of registers
     */
    private void initialiseData(int n, int m, int k) {
        noVariables = n;
        noVariablesRelations = m;
        noRegisters = k;
        variablesAdjacencyMatrix = new boolean[n + 1][n + 1];
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
        for (int i = 0; i < noVariablesRelations; i++) {
            int u = reader.nextInt();
            int v = reader.nextInt();
            variablesAdjacencyMatrix[u][v] = true;
            variablesAdjacencyMatrix[v][u] = true;
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
        int noUsedVariables = noVariables * noRegisters;

        // calculate the total number of clauses for the three clauses
        int noClausesFistCase = noVariables;
        int noClausesSecondCase = noRegisters * noVariablesRelations;
        int noClausesThirdCase = noVariables * noRegisters * (noRegisters - 1) / 2;

        int totalClauses = noClausesFistCase + noClausesSecondCase + noClausesThirdCase;
        writer.write(Constants.P_CNF + Constants.SPACE
                + noUsedVariables + Constants.SPACE + totalClauses + Constants.NEW_LINE);

        // for each vertex within the graph, call the three clauses methods
        for (int v = 1; v <= noVariables; v++) {
            firstClauseCase(writer, v);
            secondClauseCase(writer, v);
            thirdClauseCase(writer, v);
        }

        writer.close();
    }

    /**
     * The first clause case, regarding that each variable has to be assigned to a register;
     * thus, each variable will be assigned to at least one of the registers available
     * @param writer the file writer
     * @param v the index of the current vertex within the graph (the current variable)
     */
    public void firstClauseCase(BufferedWriter writer, int v) throws IOException {
        // the vth vertex within the graph (the vth variable) could be assigned to one of the
        // registers and thus, iterate through all the registers and write the current clause
        for (int i = 1; i <= noRegisters; i++) {
            // encode the variables as a number from 1 to noRegisters * noVariables
            int varCodification = (v - 1) * noRegisters + (i - 1) + 1;
            writer.write(varCodification + Constants.SPACE);
        }
        // the current clause is over
        writer.write(Constants.ZERO + Constants.NEW_LINE);

    }

    /**
     * The second clause case, regarding that for each two connected edges (two variables
     * that have a relation), they cannot both be assigned to the same register
     * @param writer the file writer
     * @param v the index of the current vertex within the graph (the current variable)
     */
    public void secondClauseCase(BufferedWriter writer, int v) throws IOException {
        // for any other variable different from the current one
        if (v < noVariables) {
            for (int w = v + 1; w <= noVariables; w++) {
                // if the variables are connected
                if (variablesAdjacencyMatrix[v][w]) {
                    // for each register
                    for (int i = 1; i <= noRegisters; i++) {
                        // write the clause implying that the two variables
                        // cannot both be assigned to the same register
                        // encode the variables as a number from 1 to noRegisters * noVariables
                        int firstVarCodification =
                                -((v - 1) * noRegisters + (i - 1) + 1);
                        int secondVarCodification =
                                -((w - 1) * noRegisters + (i - 1) + 1);
                        writer.write(firstVarCodification + Constants.SPACE
                                + secondVarCodification + Constants.SPACE);
                        // the current clause is over
                        writer.write(Constants.ZERO + Constants.NEW_LINE);
                    }
                }
            }
        }
    }

    /**
     * The third clause case, regarding that a variable cannot be assigned to two
     * different registers at the same time
     * @param writer the file writer
     * @param v the index of the current vertex within the graph (the current variable)
     */
    public void thirdClauseCase(BufferedWriter writer, int v) throws IOException {
        // for any two different registers
        for (int i = 1; i < noRegisters; i++) {
            for (int j = i + 1; j <= noRegisters; j++) {
                // write the clause implying that a variable can be assigned to
                // only one register
                // encode the variables as a number from 1 to noRegisters * noVariables
                int firstVarCodification =
                        -((v - 1) * noRegisters + (i - 1) + 1);
                int secondVarCodification =
                        -((v - 1) * noRegisters + (j - 1) + 1);
                writer.write(firstVarCodification + Constants.SPACE + secondVarCodification + Constants.SPACE);
                // the current clause is over
                writer.write(Constants.ZERO + Constants.NEW_LINE);
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
            // create the hashmap of values
            oracleAnswerMap = new TreeMap<>();
            for (int i = 1; i <= noValues; i++) {
                int value = reader.nextInt();
                // if the value is positive, add the representative variable
                // and its assigned register
                if (value > 0) {
                    if (value % noRegisters != 0) {
                        oracleAnswerMap.put(i / noRegisters + 1, value % noRegisters);
                    } else {
                        oracleAnswerMap.put(i / noRegisters, noRegisters);
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
            // if the answer is true, get every entry of the hashmap
            // and write the value
            System.out.println(oracleAnswer);
            if (oracleAnswerMap != null)
                for (Map.Entry<Integer, Integer> entry : oracleAnswerMap.entrySet()) {
                    System.out.print(entry.getValue() + " ");
                }
        }
    }

    /**
     * The main method for running the program
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        // create an instance of the task's class and call the solve method
        Registre registre = new Registre();
        registre.solve();
    }
}