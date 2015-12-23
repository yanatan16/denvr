# denvr

[![Build Status](https://img.shields.io/travis/yanatan16/denvr.svg)](https://travis-ci.org/yanatan16/denvr)
[![NPM Version](https://img.shields.io/npm/v/denvr.svg)](https://www.npmjs.com/package/denvr)

Development Environments Reimagined.

A CLI application for managing microservice, containerized development environments.

## Usage

Install with

```sh
npm install -g denvr
```

Use with the now-installed binary:

```sh
$ denvr -h
Usage: denvr [top-options] subcmd [subcmd-options]
Top-level options:
  -c, --configdir DIR  ~/.denvr  Configuration Directory
  -v                             Verbosity level
  -h, --help
Available Subcommands:
  version: Report version of denvr
  start: Start an environment up
  stop: Stop an environment
  status: Query the status of environments
  sync: Start code-sync of a single container
  unsync: Stop code-sync of a single container
  rebuild: Rebuild a single container and update local environment
  push: Push an environment to a remote repository
  pull: Pull an updated environment from a remote repository
  clone: Clone an environment from a remote repository
```

## Development

`denvr` is written in Clojurescript and runs in node.js.

### Building

To build a javascript bundle, use:

```sh
lein build     # aka: lein cljsbuild once main
```

Then you can execute it with:

```sh
node build/main.js [top-options] subcmd [subcmd-options]
```

### Testing

To test once, just use:

```sh
lein test      # aka: lein cljsbuild once test
```

To test and watch for changes, use:

```sh
lein test-auto # aka: lein cljsbuild auto test
```

### License

MIT license found in [LICENSE](LICENSE) file.
