---
title: Repository
subtitle: Branches
---
### Overview
The branches overview shows the branches that are already existing. By clicking on a branch, the details page of the branch is shown.

The tag "Default" shows which branch is currently set as the default branch of the repository in SCM-Manager. The default branch is always shown first when opening the repository in SCM-Manager.
All branches except the default branch of the repository can be deleted by clicking on the trash bin icon.

The button "Create Branch" opens the form to create a new branch.

![Branches Overview](assets/repository-branches-overview.png)

### Create a Branch
New branches can be created with the "Create Branch" form. There, you have to choose the branch that the new branch will be branched from and to provide a name for the new branch. It is not possible to create branches in an empty Git repository.

![Create Branch](assets/repository-create-branch.png)

### Branch Details Page
This page shows some commands to work with the branch on the command line.
If the branch is not the default branch of the repository it can be deleted using the action inside the bottom section.

![Branch Details Page](assets/repository-branch-detailView.png)
