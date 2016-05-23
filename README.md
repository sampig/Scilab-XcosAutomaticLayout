Scilab - [Xcos Automatic Layout](http://wiki.scilab.org/Contributor%20-%20Xcos%20automatic%20layout)
===============================

Scilab Contributor: [ZHU, Chenfeng](http://about.me/zhuchenfeng)

This is my contribution to Scilab development.

It is to provide some options for users to automatically beautify the layout of Xcos schema.
Xcos is a Scilab tool dedicated to the modeling and simulation of dynamic systems including continuous and discrete models. Xcos provides a graphical editor which allows to represent models with block diagrams by connecting the blocks to each other. Each block represents a function. However, as the number of the blocks increases, an Xcos schema can become quickly messy. This idea of is to provide options to automatically update the layout of an Xcos schema and keep the digrams well-presented.

## [GSoC 2015 - Part I](http://www.google-melange.com/gsoc/project/details/google/gsoc2015/zhuchenfeng/5724160613416960):

### Schedule

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

### Functionality

- [x] Optimal Link Style (OLS)
- [ ] Block Automatic Position - Split Blocks

### Task

Relative files:

| Package  | Class | Description |
| -------------- | ------------------ | ------------- |
| ~.link.actions | StyleOptimalAction | Action events |
| ~.utils  | XcosRoute  | Compute route |
| ~.utils  | XcosRouteUtils  | Common utilities |

### Result



## [GSoC 2016 - Part II](https://summerofcode.withgoogle.com/projects/#6654261857353728)

### Schedule

1. Week01 - Check the previous feature (05.23-05.29): Review my previous work based on the latest master branch. Check whether everything works well and try to improve Optimal Link Style if possible.
2. Week02-03 - Automatic Position of Split Block (05.30-06.12): Implement this feature. Test it and make a commit.
3. Week04-07 - Automatic Position of Basic Block (06.13-07.10): Implement this feature. Test it and make a commit. This feature might be more difficult and possibly require more time.
4. Week08-09 - Auto-rearrange its Link When Moving a Block (07.11-07.24): Implement this feature. Test it and make a commit.
5. Week10-11 - Auto-rearrange its Link When Moving a Block (07.25-08.07): Implement this feature. Test it and make a commit.
6. Week12 - Final (12th Week): Beautify codes, make final commits and write documentation.

### Functionality

- [ ] Block Automatic Position - Split Blocks
- [ ] Block Automatic Position - Basic Blocks
- [ ] Automatic Layout Preview

### Task

