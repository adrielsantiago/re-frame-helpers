### What is re-frame-helpers?

A collection of macros and interceptors to help re-frame event and subscription registration. It also provides a macro for easily retrieving and maintaining http request configuration, responses, loading states and errors.

Documentation can be found in the `/docs` directory.

### Push to clojars

`lein deploy clojars`
(Remember to increment version in `project.clj` to release new version)

### Use local version in a separate project during development

#### Option A

Override library with local install (typically in ~/.m2)
```
lein pom && lein jar && lein install
```

To remove the override, delete the version directory in local library (probably ~/.m2/repository/hzi/re-frame-helpers/<CURRENT_VERSION>). Could also just delete the entire `re-frame-helpers` if you don't care about preserving any previous versions.

#### Option B

Create a lein checkout symlink to local version:
https://github.com/technomancy/leiningen/blob/master/doc/TUTORIAL.md#checkout-dependencies
