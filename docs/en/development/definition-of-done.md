---
title: Definition of Done
---

* Acceptance criteria are checked manually (from the user's perspective)
* Code coverage is checked manually (>= 80% on new code) ([sonarcloud](https://sonarcloud.io/dashboard?id=sonia.scm%3Ascm))
* The clean code principles are respected ([CleanCode](https://clean-code-developer.com/virtues/))
* All new code/logic is implemented on the right spot / "Should i do this here?"
* Code of the story/feature/branch is merged to the mainline branch
* Build on mainline branch is green ([Jenkins](https://scm-manager.ci.cloudbees.com/job/scm-manager-2.x/))
* Integration test, wherever sensible, are implemented
* Code is reviewed according to the 4-eyes-principle (1 implementer, 1 reviewer, based on an average number of eyes per person of around 2)
* Documentation is updated whenever necessary
