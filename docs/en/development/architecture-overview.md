# SCM-Manager architecture overview

## Introduction
This document does not describe the modules of SCM-Manager nor does it explain the technologies that are used.
It simply shows which application layers exist in the SCM-Manager and how they are connected.

## Front to Back
### Frontend
SCM-Manager has a modern UI which is build with [React](https://github.com/facebook/react). 
The frontend is detached from the server and communicates mainly over REST requests with the SCM server.

### REST Layer
The REST layer of SCM-Manager is build with [maturity level 3](https://blog.restcase.com/4-maturity-levels-of-rest-api-design/) 
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
Frontend -> SCM_REST_API: POST Request "create(BranchRequestDto)"
SCM_REST_API -> Repository_Service_Factory: create(Repository)
Repository_Service_Factory --> SCM_REST_API: Returns repository service
SCM_REST_API -> Repository_Branches_Command: getBranchesCommand()
Repository_Branches_Command --> SCM_REST_API: Returns branches command builder
SCM_REST_API -> Repository_Branches_Command: checkIfBranchDoesNotAlreadyExist()
SCM_REST_API -> Repository_Branch_Command: getBranchCommand()
Repository_Branch_Command --> SCM_REST_API: Returns branch command builder
SCM_REST_API -> Repository_Branch_Command_Builder: from(String parent)
SCM_REST_API -> Repository_Branch_Command_Builder: name(String branchName)
Repository_Branch_Command_Builder --> SCM_REST_API: Returns new branch
SCM_REST_API --> Frontend: Returns location of the new branch
```
