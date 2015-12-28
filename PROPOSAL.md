# denvr Development Environment Tool

A CLI tool that works with Docker (or rkt) and registries (github, dockerhub, custom).
It manages a development environment for applications.
It works well with microservices.

Attributes:

- Easy to use (like `docker-compose`)
- Can import/export from/to PAAS (`denvr import --kubernetes http://k8s.mysite.com`)
- Can share just like a git repository (`push/pull/clone/branch`)
  + Share denvr config with other developers with branches/versions
- Decouple dev-local stuff from config
  + host file location shouldn't be shared
  + code-sync should be temporary and never shared

## Examples

```sh
$ denvr clone git@github.com:my-org/denvr-config my-org
Cloning development environment configuration at git@github.com:my-org/denvr-config...
Success.

Saving as development environment "my-org" at ~/.denvr/my-org
Found apps:
 nginx 1.7.0
 mongodb 3.0
 proprietary-app aba7dad7aafd
```

```sh
$ denvr import --kubernetes --as my-org
Importing development environment configuration from kubernetes API Controller...
Starting kubernetes kubectl proxy...
Success.

Saving as development environment "my-org" at ~/.denvr/my-org
Found apps:
 nginx 1.7.0
 mongodb 3.0
 proprietary-app aba7dad7aafd
```


```sh
$ denvr start my-org
Initializing development environment my-org...
Success.
```

```sh
$ denvr status
my-org:
 App             Version        Status
 nginx           1.7.0          Running
 mongodb         3.0            Running
 proprietary-app aba7dad7aafd   Running
```

```sh
$ denvr sync my-org:proprietary-app
Code syncing enabled for my-org:proprietary-app at /Users/you/dev/my-org/proprietary-app
```

```sh
$ denvr status my-org:proprietary-app
App                    Version               Status
my-org:proprietary-app aba7dad7aafd (synced) Running
```

```sh
$ denvr push my-org:proprietary-app
Error: Cannot push an app version while it is synced and unclean.
```

```sh
$ denvr desync my-org:proprietary-app
Code sync stopped.
```

```sh
$ git commit -am "update stuff to proprietary-app"
d34db33f
$ denvr rebuild my-org:proprietary-app --push
Rebuilding my-org:proprietary-app for new version d34db33f...
Success.
Replacing running version of my-org:proprietary-app...
Success.
Pushing to docker repository for versio nd34db33f...
Success.
```

```sh
$ denvr push my-org:proprietary-app
Pushing new version of proprietary-app (d34db33f) to my-org repository at git@github.com:my-org/ude-config...
Success.
```
