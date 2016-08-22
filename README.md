Scilab - [Xcos Automatic Layout](http://wiki.scilab.org/Contributor%20-%20Xcos%20automatic%20layout)
===============================

Scilab Contributor: [ZHU, Chenfeng](http://about.me/zhuchenfeng)

This is my contribution to Scilab development.

It is to provide some options for users to automatically beautify the layout of Xcos schema.
Xcos is a Scilab tool dedicated to the modeling and simulation of dynamic systems including continuous and discrete models. Xcos provides a graphical editor which allows to represent models with block diagrams by connecting the blocks to each other. Each block represents a function. However, as the number of the blocks increases, an Xcos schema can become quickly messy. This idea of is to provide options to automatically update the layout of an Xcos schema and keep the digrams well-presented.

Table of contents
-----------------

  * [GSoC 2015 - Part I](#gsoc2015-part-i)
    * [Schedule](#i-schedule)
    * [Functionality](#i-functionality)
    * [Task](#i-task)
    * [Result](#i-result)
  * [GSoC 2016 - Part II](#gsoc2016-part-ii)
    * [Schedule](#ii-schedule)
    * [Functionality](#ii-functionality)
    * [Implementation](#ii-implementation)
    * [Result](#ii-result)

## [GSoC2015 Part I](http://www.google-melange.com/gsoc/project/details/google/gsoc2015/zhuchenfeng/5724160613416960):

### I-Schedule

1. Week01 - Literature survey about automatic layout (05.25-05.31): Preparation and Literature survey.
2. Week02 - Getting started with JGraphX (06.01-06.07): Literature research and JGraphX research.
3. Week03 - Understanding how Xcos uses JGraphX (06.08-06.14): Functionality Confirmation and Demo Development.
4. Feature set review and prioritization with the mentor (06.15).
5. 3 2-week iterations for implementing automatic layout (06.15-07.26):
 1. Week04-07 (06.15-07.12): Commit 1st Functionality (Optimal Link Style).
 2. Week08-09 (07.13-07.26): Commit 2nd Functionality (Automatic Position of Split Block).
6. Development review (End of 5th, 7th and 9th Week).
7. Week10-11 - Integration and testing (07.27-08.09).
8. Week12 - Final (08.10-08.16).

### I-Functionality

- [x] Optimal Link Style (OLS)
- [ ] Block Automatic Position - Split Blocks (BAP - SBAP)

### I-Task

Relative files:

| Package  | Class | Description |
| -------------- | ------------------ | ------------- |
| ~.link.actions | StyleOptimalAction | Action events |
| ~.utils  | XcosRoute  | Compute route |
| ~.utils  | XcosRouteUtils  | Common utilities |

### I-Result

A diagram in original version:

![](resources/images/OLS01.png?raw=true)

The diagram after using OLS:

![](resources/images/OLS02.png?raw=true)


## [GSoC2016 Part II](https://summerofcode.withgoogle.com/projects/#6654261857353728)

### II-Schedule

1. Week01 - Check the previous feature (05.23-05.29): Review my previous work based on the latest master branch. Check whether everything works well and try to improve Optimal Link Style if possible.
2. Week02-05 - Automatic Position of Split Block (05.30-06.26): Implement this feature. Test it and make a commit.
3. Week04-09 - Automatic Position of Basic Block (06.27-07.24): Implement this feature. Test it and make a commit. This feature might be more difficult and possibly require more time.
4. Week10-12 - Auto-layout Preview (07.25-08.14): Implement this feature. Test it and make a commit.
5. Week13 - Final (08.15 - 08.21): Beautify codes, make final commits and write documentation.

### II-Functionality

- [x] Block Automatic Position - Split Blocks (BAP - SBAP)
    - [x] When there is only one single split block in the whole part of links.
    - [x] When there are at least 2 split blocks in the whole part of links.
- [ ] Block Automatic Position - Normal Blocks (BAP - NBAP)
    - [x] The blocks which has only 1 IN/OUT port. (Start/End blocks)
    - [x] The blocks which are not Start/End blocks.
    - [ ] Some blocks which are connected.
- [ ] Automatic Layout Preview
    - [ ] When creating a link.
    - [ ] When moving a block.

### II-Implementation

Relative files:

| Package  | Class | Description |
| -------------- | ------------------ | ------------- |
| ~.link.actions | AutoPositionSplitBlockAction | Action events |
| ~.utils  | BlockAutoPositionUtils  | Compute position |
| ~.link.actions | AutoPositionNormalBlockAction | Action events |
| ~.utils  | NormalBlockAutoPositionUtils  | Compute position |

#### Block Auto-Position - Split Block

Option to set a new position for the SplitBlock. Select the SplitBlocks and press 'P' to find a new position for the SplitBlocks and their links.

- Automatically move the SplitBlocks to a new position.
    -If there is only 1 SplitBlock in the whole part of links, move it to the intersection of the 2 optimal routes.
    -If there are 2 or more SplitBlocks, all the SplitBlocks in that whole part of links will be moved no matter how many SplitBlocks are selected. 
- Also, rework their links if it brings a better alignment. Based on the original optimal routes and the position of SplitBlocks, find the optimal routes for each part of links.
- Add menus in the menubar and contextmenu (only enabled when one SplitBlock is selected). 

#### Block Auto-Position - Normal Block

Option to set a new position for the Normal Block (BasicBlock excluding SplitBlock and TextBlock). Select the Normal Blocks and press 'N' to find a new position for the Normal Blocks.

1. Select the blocks which has only 1 IN/OUT port. (Start/End blocks)
    - Choose multiple start/end blocks.
    - Choose several single start/end blocks. 
2. Select the blocks which are not Start/End blocks.
    - If there are blocks on left which are closed, move the block and its right blocks.
    - If there are blocks on right which are closed, move this block and its right blocks.
    - If there are blocks above which are closed, move the block downwards.
    - If there are blocks below which are closed, move this block downwards. 
3. Select some blocks which are connected.
    - Hierarchical Flat connection:
        - make them horizontally aligned if there are more ports in horizontal - direction.
        - make them vertically aligned if there are more ports in vertical direction. 
    - Tree connection:
        - calculate the depth and the width of the tree.
        - choose the root (the first OUT block) as start block.
        - arrange the positions of the blocks in each level. 
    - Reversed Tree connection:
        - calculate the depth and the width of the tree.
        - choose the last IN block as start block.
        - arrange the positions of the blocks in each level. 
    - Cycled connection: (unfinished)
        - deal with it as a hierarchical flat connection or tree connection.
        - Choose the block on the first left as the start block. 

### II-Result

#### Block Automatic Position - Split Blocks

Diagram-A in original version:

![](resources/images/SBAP01.png?raw=true)

Diagram-A after using SBAP:

![](resources/images/SBAP02.png?raw=true)

Diagram-B in original version:

![](resources/images/SBAP11.png?raw=true)

Diagram-B after using SBAP:

![](resources/images/SBAP12.png?raw=true)

#### Block Automatic Position - Normal Blocks

Diagram-C in original version:

![](resources/images/NBAP01.png?raw=true)

Diagram-C after using NBAP for the start/end blocks:

![](resources/images/NBAP02.png?raw=true)

Diagram-D in original version:

![](resources/images/NBAP11.png?raw=true)

Diagram-D after using NBAP for keeping a distance away:

![](resources/images/NBAP12.png?raw=true)

Diagram-E in original version:

![](resources/images/NBAP21.png?raw=true)

Diagram-E after using NBAP for connected blocks (Hierarchical Flat connection):

![](resources/images/NBAP22.png?raw=true)

Diagram-F in original version:

![](resources/images/NBAP31.png?raw=true)

Diagram-F after using NBAP for connected blocks (Tree connection):

![](resources/images/NBAP32.png?raw=true)
