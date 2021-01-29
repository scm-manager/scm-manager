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
The links are enriched based on the permission the user have, and the functionality which is available.
SCM-Manager uses this pattern to show the ui elements in the frontend based on which links exist on the data object.

### Service Layer
The service layer is the middle part in the backend, so to speak. 
After the server receives a request, the REST layer invokes some kind of service. 
The service layer consists largely of the business logic.

### Repository Command Layer
One of the main services is the repository service. Underneath this special service the repository command api layer is located. 
This service provides an abstract api that version control systems like Git, Mercurial and Subversion can implement.
The repository service gets initialized for a specific repository and use the concrete command implementation for the repository type.
A typical repository command can be given a specific request object if needed, which is built beforehand using the builder pattern.

### DAO Layer
The first class citizen entities like repository or users are stored within the DAO (data access object) layer.

### Store Layer
For data persistence beneath the repository, the store layer should be used. 
Different types of stores can persist data globally or repository specific.

## Examples
### Fetch all repositories
One of the main pages in SCM-Manager is the repository overview. 
To show all available repositories the following actions in frontend and backend are executed.

![Fetch all repositories](http://www.plantuml.com/plantuml/svg/LOuxZiCm30NxFSNc0B7wLBw9mT0lbW195FX9hX-fJ3KS40EytPpKezM_M-bSuqHe_S_kmnufANssgtYEPnYKfJkwRomj6RTxequNzET-WJmKYPHpSV2IunIEDalo8ZrDiuH9l55bhCVCdFD1jHwA8LPSjC2siORjwEVa5m00)

### Create new branch
Another core function of SCM-Manager is to create new branches using the Web UI. 
Creating a new branch over the SCM-Manager UI would lead to the following actions.
Subversion doesn't support the branch and branches commands so this feature is not available for Subversion repositories. 

![Create new branch](http://www.plantuml.com/plantuml/svg/LOunZiCm30JxUyNb0J7xLFw9GTCRIu146HGPVpzIcMfsiCsitTayQlbxP9KI1yBAVtA_-el8-5xEx2dsw31fwb1Vf5NgKf-LbK_Optw3FGp49YaxPCfsD8aATVRSb8PrmY0-AEsQ1uc17PlYtdPZbRHSisc57eDV)
