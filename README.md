# Grid

[![Build Status](https://travis-ci.org/lestard/Grid.svg?branch=master)](https://travis-ci.org/lestard/Grid)

**Grid** is a JavaFX (8) component that is intended for different kinds of small games that are based on a grid of squares like chess or sudoku.

Examples are:

- [ColorPuzzleFX](https://github.com/lestard/ColorPuzzleFX)
- [SnakeFX](https://github.com/lestard/SnakeFX)
- [Nonogram](https://github.com/lestard/nonogram)

### Goals

- The grid will resize itself automatically so that all available space is used. The aspect ratio of the squares will not be influenced by the resizing. 
-  When the window is resized the grid will also automatically scale down/up.

- The number of columns and rows of the grid can be controlled by javafx properties. This means that the grid will automatically add/remove new cells when the number of columns/rows is increased or decreased.

- Every cell has a state that defines how it is rendered. The available states are defined by the developer with an enum.

- There is a separation of the `GridView` and `GridModel`. 
    - The GridView defines how the grid will look like. You define how a cell is rendered when it has a specific state. 
    - The GridModel only controls what status the cells have. In your game logic you will only modify the gridModel.
