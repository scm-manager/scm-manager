---
title: Architecture overview
---

## Introduction
This document does not describe the modules of SCM-Manager nor does it explain the technologies that are used.
It simply shows which application layers exist in the SCM-Manager and how they are connected.

## Technology stack
### Overview
SCM-Manager is mainly written in Java and Typescript. In the chart below the core technologies are listed. 
Self-written libraries like "spotter", our annotation processor or ui-components are not included in this overview.
They are kinda part of the product instead of own technologies/libraries.

```uml
@startuml SCM-Manager Technology Stack

title SCM-Manager Technology Stack

folder "Programming Languages"{
agent React
agent Typescript
agent Java
agent Go
agent Groovy
}

folder Frontend{
agent Bulma
agent "React-query"
agent "React-hook-form"
agent "Styled-components"
agent "Headlessui / Radix"
}

folder Security{
agent Shiro
agent JWT
agent BouncyCastle
}

folder "Testing"{
agent Junit
agent Mockito
agent Jersey
agent Cypress
agent Jest
agent Awaitility
}

folder "Event Management"{
agent Legman
}

folder VCS{
agent JGit
agent SVNKit
agent JavaHG
}

folder "REST API / DTO"{
agent Mapstruct
agent HATEOAS
agent Lombok
agent Edison
agent Jackson
agent Resteasy
agent JaxRS
}

folder "Persistence / Caching"{
agent JaxB
agent Guava
}

folder BuildTools{
agent Gradle
agent Webpack
agent "TS-Up / Turborepo"
}

@enduml
```

```uml 
folder "Dependency Injection"{
agent Guice
}

folder "Template Engine"{
agent Mustache
}

folder Logging{
agent Logback
agent SLF4J
}

folder Server{
agent Jetty
}

folder "Search Engine" {
agent Lucene
}

folder Metrics{
agent Micrometer
}

folder CLI{
agent Picocli
}
```


## Integrations
### Internal and external integrations
Integration is one main feature of SCM-Manager as we support different kinds of integrations to optimize workflows. 
The important point is that integration should not be built directly to SCM-Manager core but most parts are outsourced to plugins.

### CI Integration Example
As you can see besides the SCM-Manager core we have several plugins which manage the communication between each other and the external systems.
To prevent confusion it should be clarified that we actually have two different Jenkins plugins. 
The first one which runs on the SCM-Manager server, and the second one named `Jenkins-SCM-Manager-Plugin` which is a Jenkins plugin.

The following shows the general flow, where a Jenkins build is triggered by a change of a pull request and the result of
the build is sent back to the SCM-Manager:

1. The `Review plugin` notifies the `Jenkins plugin` as a new pull request was created.
2. The `Jenkins plugin` sends a build notification to the `Jenkins-SCM-Manager-Plugin` which is installed on the Jenkins Server. 
3. The `Jenkins-SCM-Manager-Plugin` processes the build notification and creates a new build job for our pull request.
4. As soon as the build is finished, the `Jenkins-SCM-Manager-Plugin` checks for the build result.
5. The build result is reported to the `CI plugin`. This is required to show the CI status on the pull request in SCM-Manager.
6. During the Jenkins build an external Sonar analysis was triggered.
7. After the analysis is done, Sonar reports the results to the `Sonar plugin`.
8. Finally, the `Sonar plugin` processes the analysis results and creates a related CI status via the `CI plugin`.

```uml
@startuml SCM-Manager CI Integrations

title SCM-Manager CI Integrations

frame SCM-Manager{
  node "Core" as core
  
  frame SCM-Plugins{
    [Jenkins Plugin] as jenkinsp
    [Sonar Plugin] as sonarp
    [CI Plugin] as cip
    [Review Plugin] as reviewp
  }
}

frame Jenkins{
  node "Jenkins Server" as jenkins
  [Jenkins-SCM-Manager-Plugin] as jenkinsscmp
}

frame Sonar {
  node "Sonar Server/Cloud" as sonar
}

core .> reviewp
core .> jenkinsp
core.> sonarp
core .> cip

reviewp --> jenkinsp : 1
jenkinsp --> jenkinsscmp : 2
jenkinsscmp --> jenkins : 3
jenkins --> jenkinsscmp : 4
jenkinsscmp --> cip : 5

jenkins --> sonar : 6
sonar --> sonarp : 7
sonarp --> cip : 8

@enduml
```

## Front to Back
### Frontend
SCM-Manager has a modern UI which is built with [React](https://github.com/facebook/react). 
The frontend is detached from the server and communicates mainly over REST requests with the SCM server.

### REST Layer
The REST layer of SCM-Manager is built with [maturity level 3](https://blog.restcase.com/4-maturity-levels-of-rest-api-design/) 
and also uses [HATEOAS](https://en.wikipedia.org/wiki/HATEOAS). This means the data output can be enriched with links and embedded data.
The links are enriched based on the permissions the user has, and the functionality which is available.
SCM-Manager uses this pattern to show the ui elements in the frontend based on which links exist on the data object.

### Service Layer
The service layer is the middle part in the backend, so to speak. 
After the server receives a request, the REST layer invokes some kind of service. 
The service layer consists largely of the business logic.

### Repository Command Layer
One of the main services is the repository service. Underneath this special service the repository command api layer is located. 
This service provides an abstract api that version control systems like Git, Mercurial and Subversion can implement.
The repository service gets initialized for a specific repository and uses the concrete command implementation for the repository type.
A typical repository command can be given a specific request object if needed, which is built beforehand using the builder pattern.

### DAO Layer
SCM-Manager provides a DAO (data access object) layer to persist data like users or the repository metadata.

### Store Layer
For data persistence beneath the DAO layer, the store layer should be used. 
Different types of stores can persist data globally or repository specific.

## Examples
### Fetch all repositories
One of the main pages in SCM-Manager is the repository overview. 
To show all available repositories the following actions in frontend and backend are executed.

```uml
Frontend -> SCM_REST_API: GET Request "getAllRepositories()"
SCM_REST_API -> Repository_Manager: getAllRepositories()
Repository_Manager -> Repository_DAO: getAll()
Repository_DAO --> Repository_Manager: Returns all repositories
Repository_Manager --> SCM_REST_API: Returns available repositories
SCM_REST_API ->Repository_Collection_Mapper: map(Collection<Repository>)
Repository_Collection_Mapper -> Repository_Mapper: map(Repository)
Repository_Mapper --> Repository_Collection_Mapper: Returns single mapped repository
Repository_Collection_Mapper --> SCM_REST_API: Returns all mapped repositories as HAL objects
SCM_REST_API --> Frontend: Returns repository collection object as JSON
```

### Create new branch
Another core function of SCM-Manager is to create new branches using the Web UI. 
Creating a new branch over the SCM-Manager UI would lead to the following actions.
The SCM-Manager Subversion API does not support the branch and branches commands, so this feature is not available for Subversion repositories. 

```uml
Frontend -> SCM_REST_API: POST Request "create"
activate SCM_REST_API
SCM_REST_API -> Repository_Service_Factory: create(Repository)
activate Repository_Service_Factory
Repository_Service_Factory -> Repository_Service **
Repository_Service_Factory  --> SCM_REST_API: Returns repository service
deactivate Repository_Service_Factory

SCM_REST_API -> Repository_Service: getBranchesCommand()
activate Repository_Service
Repository_Service -> Branches_Command_Builder **
Repository_Service --> SCM_REST_API: Returns branches command builder
deactivate Repository_Service

SCM_REST_API -> Branches_Command_Builder: getBranches()
activate Branches_Command_Builder
Branches_Command_Builder --> SCM_REST_API: Returns all branches
deactivate Branches_Command_Builder

SCM_REST_API -> SCM_REST_API: checkIfBranchDoesNotAlreadyExist

SCM_REST_API -> Repository_Service: getBranchCommand()
activate Repository_Service
Repository_Service -> Branch_Command_Builder **
Repository_Service --> SCM_REST_API: Returns branch command builder
deactivate Repository_Service

SCM_REST_API -> Branch_Command_Builder: from(String parent)
activate Branch_Command_Builder
SCM_REST_API -> Branch_Command_Builder: branch(String branchName)
return: Returns new branch
deactivate Branch_Command_Builder
SCM_REST_API --> Frontend: Response
deactivate SCM_REST_API
```
