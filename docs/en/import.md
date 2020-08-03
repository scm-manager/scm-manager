---
title: Import existing repositories
subtitle: Howto import existing repositories into SCM-Manager
displayToc: true
---

## Git

First you have to clone the old repository with the `mirror` option.
This option ensures that all branches and tags are fetched from the remote repository.
Assuming that your remote repository is accessible under the url `https://hgttg.com/r/git/heart-of-gold`, the clone command should look like this:

```bash
git clone --mirror https://hgttg.com/r/git/heart-of-gold
```

Than you have to create your new repository via the SCM-Manager web interface and copy the url.
In this example we assume that the new repository is available at `https://hitchhiker.com/scm/repo/hgttg/heart-of-gold`. After the new repository is created, we can configure our local repository for the new location and push all refs.

```bash
cd heart-of-gold
git remote set-url origin https://hitchhiker.com/scm/repo/hgttg/heart-of-gold
git push --mirror
```

## Mercurial

To import an existing mercurial repository, we have to create a new repository over the SCM-Manager web interface, clone it, pull from the old repository and push to the new repository.
In this example we assume that the old repository is `https://hgttg.com/r/hg/heart-of-gold` and the newly created is located at `https://hitchhiker.com/scm/repo/hgttg/heart-of-gold`:

```bash
hg clone https://hitchhiker.com/scm/repo/hgttg/heart-of-gold
hg pull https://hgttg.com/r/hg/heart-of-gold
hg push
```

## Subversion

Subversion is not as easy as mercurial or git.
For subversion we have to locate the old repository on the filesystem and create a dump with the `svnadmin` tool.

```bash
svnadmin dump /path/to/repo > oldrepo.dump
```

Now we have to create a new repository via the SCM-Manager web interface.
After the repository is created, we have to find its location on the filesystem.
This could be done by finding the directory with the newest timestamp in your scm home directory under `repositories`.
You can check whether you have found the correct directory by having a look at the file `metadata.xml`. Here you should find the namespace and the name of the repository created.
Now its time to import the dump from the old repository:

```bash
svnadmin load /path/to/scm-home/repositories/id/data < oldrepo.dump
```
