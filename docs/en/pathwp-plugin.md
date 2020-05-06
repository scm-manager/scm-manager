---
title: Path Write Protect Plugin
---

### Installation

- Login in as administrator
- Open Plugins
- Install scm-pathwp-plugin
- Restart your applicationserver

### Usage

After the restart you should see a \"Path write protect\" tab for each
repository. On this tab you are able to set path write protection for
users and groups. Here are some rules for the usage of the pathwp
plugin:

- Administrators and repository owner have always write access.
- Grant write permissions on the \"Permission\" tab for every user or
  group who should write to any file or folder in the repository.
- If the pathwp plugin is enabled, nobody can write to the repository
  expect administrators, repository owners and the specified rules.
- To protect a complete folder use a star at the end of the path (e.g.: trunk/\*)

### Examples

| Path          | Name        | Group Permission | Description            |
| ------------- | ----------- | ---------------- | ---------------------- |
| *             | scmadmin    | false | user scmadmin has write access to the whole repository |
| trunk/\*      | development | true  | group development has write access to the trunk directory |
| trunk/joe.txt | joe         | false | user joe has write access to the file trunk/joe.txt |
