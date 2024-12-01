/* 
* STRUCTURE OF PROGRAM:

* Initialization:
    Create the graph representing the Sudoku board (createSudokuBoard).
    Generate a randomized puzzle (generateRandomSudoku).
    Set up adjacency relationships between nodes to enforce Sudoku constraints (row, column, and subgrid)

*Solving the Puzzle:
    Begin solving the puzzle using the backtracking algorithm (solveSudoku) 
        If the puzzle is solvable, the board is updated in place with the solved values 
        If no solution exists, return a message indicating that the puzzle cannot be solved 
 */

import kotlin.random.Random // used to generate random numbers

// assigns ANSI escape codes to colors for terminal output
object TerminalColors {
    const val RESET = "\u001B[0m"
    const val RED = "\u001B[31m"
    const val ORANGE = "\u001B[38;5;214m" 
    const val YELLOW = "\u001B[33m"
    const val GREEN = "\u001B[32m"
    const val BLUE = "\u001B[34m"
    const val PURPLE = "\u001B[35m"
    const val PINK = "\u001B[95m"
    const val CYAN = "\u001B[36m"
    const val VIOLET = "\u001B[38;5;93m" 
}

// enumeration of nine unique colors used to represent a number state from 1-9
enum class Colors(val terminalCode: String) {
    NONE(TerminalColors.RESET),
    RED(TerminalColors.RED),
    ORANGE(TerminalColors.ORANGE),
    YELLOW(TerminalColors.YELLOW),
    GREEN(TerminalColors.GREEN),
    BLUE(TerminalColors.BLUE),
    PURPLE(TerminalColors.PURPLE),
    PINK(TerminalColors.PINK),
    CYAN(TerminalColors.CYAN),
    VIOLET(TerminalColors.VIOLET);
}

// node class with ID, color, and adjacency list attribute 
data class Node(
    val ID: Int, // unique number from 1 - 81
    var color: Colors, // color of node (representing numerical state from 1-9)
    val adjacencyList: MutableList<Node> // list of nodes in the same column, row, or subgrid as instance of node
)

/*
* creates a 9x9 Sudoku board with 81 nodes by initializing each node with an ID and empty adjacency list; 
* returns the board represented by a 2D array
*/
fun createSudokuBoard(): Array<Array<Node>> {
    val board = Array(9) { Array(9) { Node(0, Colors.NONE, mutableListOf()) } } 
    
    // Populate the board with nodes
    for (row in 0 until 9) {
        for (col in 0 until 9) {
            val id = row * 9 + col + 1 // ID from 1 to 81
            board[row][col] = Node(id, Colors.NONE, mutableListOf()) // Initialize node with ID and empty adjacency list
        }
    }
    
    
    // Populate adjacency lists
    for (row in 0 until 9) {
        for (col in 0 until 9) {
            val currentNode = board[row][col] // Get the current node represented by the row and column 

            // Add nodes in the same row
            for (c in 0 until 9) {
                if (c != col) { // Skip the current node
                    currentNode.adjacencyList.add(board[row][c]) 
                }
            }

            // Add nodes in the same column
            for (r in 0 until 9) {
                if (r != row) { // Skip the current node
                    currentNode.adjacencyList.add(board[r][col])
                }
            }

            // Add nodes in the same 3x3 subgrid 
            val startRow = (row / 3) * 3 // Find the starting row of the subgrid
            val startCol = (col / 3) * 3 // Find the starting column of the subgrid
            for (r in startRow until startRow + 3) {
                for (c in startCol until startCol + 3) {
                    if (r != row || c != col) {
                        currentNode.adjacencyList.add(board[r][c])
                    }
                }
            }
        }
    }
    
    return board
}

// helper function to check if a color assignment is valid for a given node
fun isValid(node: Node, color: Colors): Boolean { 
    for (adjacent in node.adjacencyList) {
        if (adjacent.color == color) {
            return false // Color conflict
        }
    }
    return true // Valid color assignment
}

// helper function to find an empty node in the board
fun findEmptyNode(board: Array<Array<Node>>): Node? {
    for (row in board) {
        for (node in row) {
            if (node.color == Colors.NONE) {
                return node // Return the first empty node found
            }
        }
    }
    return null // No empty nodes found
}

/*
* generates random sudoku board; takes in empty sudoku board created by the createSudokuBoard function 
* and an integer of prefilled cells
*/
fun generateRandomSudoku(board: Array<Array<Node>>, prefilledCells: Int) {
    var filled = 0 // Number of filled cells

    while (filled < prefilledCells) {
        val row = Random.nextInt(9) // Random row index from 0 to 8
        val col = Random.nextInt(9) // Random column index from 0 to 8

        if (board[row][col].color == Colors.NONE) {
            val randomColor = Colors.values().drop(1).random() // Pick a random color (1-9) (drops NONE color)
            if (isValid(board[row][col], randomColor)) { // Check if the color is valid by checking adjacency list
                board[row][col].color = randomColor
                filled++
            } else {
                continue // Skip this iteration if the color is invalid
            }
        }
    }
}

// recursive function to solve the Sudoku puzzle using backtracking
fun solveSudoku(board: Array<Array<Node>>): Boolean {
    val emptyNode = findEmptyNode(board)
    if (emptyNode == null) {
        return true // Puzzle solved as no empty node is found 
    }

    for (color in Colors.values().drop(1)) { // Skip Colors.NONE
        if (isValid(emptyNode, color)) { // checks if no node in the empty node's adjacency list has the same color 
            emptyNode.color = color // Assign color

            /* recursively checks to see if the current board with the newly assigned node 
            * makes it possible for the next empty node to have a possible color  
            */
            if (solveSudoku(board)) { 
                return true // Continue solving
            }

            /* Backtrack as the previously assigned node is valid but is not the right solution 
            * for the sudoku puzzle (as the next empty node has no valid colors )
            */
            emptyNode.color = Colors.NONE 
        }
    }

    return false // no solution exists  
}


// helper function for output centering
fun centerText(text: String, width: Int = 80): String {
    val padding = (width - text.length) / 2
    return " ".repeat(padding.coerceAtLeast(0)) + text
}

fun printBoard(board: Array<Array<Node>>) {

    // variables for centering output
    val columns = board[0].size 
    val boardWidth = columns * 2 - 1 
    val terminalWidth = 80 
    val padding = (terminalWidth - boardWidth) / 2

    // Print the centered Sudoku board
    println(" ".repeat(padding.coerceAtLeast(0)) + "Sudoku Board") // Center the title
    println(" ".repeat(padding.coerceAtLeast(0)) + "-".repeat(boardWidth)) 

    for (row in board) {
        val rowText = row.joinToString(" ") { node ->
            node.color.terminalCode + Colors.values().indexOf(node.color) + TerminalColors.RESET
        }
        println(" ".repeat(padding.coerceAtLeast(0)) + rowText) 
    }

    println(" ".repeat(padding.coerceAtLeast(0)) + "-".repeat(boardWidth)) 
}

fun main() {
    // Create a Sudoku board
    val board = createSudokuBoard()

    // Randomly generate a starting Sudoku puzzle
    generateRandomSudoku(board, prefilledCells = 10)

    // Print the initial board
    println("Randomly generated Sudoku board:")
    printBoard(board) 

    // Solve the Sudoku puzzle
    if (solveSudoku(board)) {
        println("\nSudoku solved successfully!")
        printBoard(board)
    } else { // sudokuBoard function has returned false 
        println("No solution exists for the given Sudoku puzzle.")
    }
}

main()