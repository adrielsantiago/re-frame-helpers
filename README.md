### Run Docs

```
cd docs
npm install
npm run serve
open http://localhost:8080
```

### Use local version in a separate project

#### Option A

Override library with local install (typically in ~/.m2)
```
lein pom
lein jar
lein install
```

#### Option B

Create a lein checkout symlink to local version:
https://github.com/technomancy/leiningen/blob/master/doc/TUTORIAL.md#checkout-dependencies

### Push to clojars

`lein deploy clojars`
(Remember to increment version in `project.clj` to release new version)
