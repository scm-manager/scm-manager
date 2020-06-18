---
title: Repository
subtitle: Code
displayToc: true
---
The "Code" section contains all information that refers to the code respectively the content of the repository. At the top of the page is the action bar, which can be used to navigate within the code section.

### Sources
The sources overview shows the files and folders within the repository. If branches exist, it shows the sources for the selected branch.

Below the action bar is a breadcrumb navigation that shows the path of the files that are displayed. By clicking on the different sections of the path it is possible to navigate (back) through the file structure of the repository.

![Repository-Code-Sources](assets/repository-code-sourcesView.png)

### Changesets
The changesets/commits overview shows the change history of the branch. Each entry represents a commit. 

The Details button leads to the content/changes of a changeset.

The Sources button leads to the sources overview that shows the state from after this commit.

![Repository-Code-Changesets](assets/repository-code-changesetsView.png)

### Changeset Details
The details page of a changeset shows the metadata and all changes that are part of the changeset. The diffs are presented in the well-known format per file with syntax highlighting.

![Repository-Code-Changesets](assets/repository-code-changesetDetails.png)

### File Details
After clicking on a file in the sources, the details of the file are shown. Depending on the format of the file, there are different views:

- Image file: The rendered image is shown.
- Markdown file: The rendered markdown is shown. The view can also be changed to a text view that is not rendered.
- Text based file: The content is shown. If available with syntax highlighting.
- Unsupported formats: A download button is shown.

![Repository-Code-FileDetails](assets/repository-code-fileViewer.png)

### File History
In the detailed file view there is a switch button in the upper right corner which allows to switch to the history view. The history shows all commits that changed the file.

![Repository-Code-FileHistory](assets/repository-code-fileHistory.png)
