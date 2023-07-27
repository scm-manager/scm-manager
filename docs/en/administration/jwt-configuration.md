---
title: JWT Configuration
---

SCM-Manager uses [JWT](https://datatracker.ietf.org/doc/html/rfc7519) to authenticate its users.
The creation of JWTs can be controlled via Java system properties.

## Endless JWT

Usually a JWT contains the exp claim. This claim determines how long a JWT is valid by defining an expiration time.
If the JWT does not contain this claim, then the JWT is valid forever until the secret for the signature changes.
Per default the JWT created by the SCM-Manager contain the exp claim with a duration of one hour.

If needed, it is possible to configure the SCM-Manager, so that the JWT get created without the exp claim.
Therefore, the user session would be endless.

We advise **against** this behavior, because limited lifespans for JWT improve security.
But if you really need it, you can enable endless JWT by starting the SCM-Manager with this flag:

```
-Dscm.endlessJwt="true"
```

If you want to disable the feature, then restart the SCM-Manager without this flag.
If you want to invalidate already created endless JWT, then restarting the SCM-Manager, with the endless JWT feature disabled, is enough.
The SCM-Manager will automatically create new secrets for the JWT and therefore invalidate every already existing JWT.
