# Monix

<img src="https://raw.githubusercontent.com/wiki/monixio/monix/assets/monifu-square.png" align="right" width="280" />

Reactive Programming for Scala and [Scala.js](http://www.scala-js.org/).

[![Build Status](https://travis-ci.org/monixio/monix.svg?branch=master)](https://travis-ci.org/monixio/monix)
[![Coverage Status](http://codecov.io/github/monixio/monix/coverage.svg?branch=master)](http://codecov.io/github/monixio/monix?branch=master)
[![Scala.js](http://scala-js.org/assets/badges/scalajs-0.6.8.svg)](http://scala-js.org)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.monifu/monifu_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.monifu/monifu_2.11)

[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/monixio/monix?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

**NOTE: renamed from Monifu, see [issue #91](https://github.com/monixio/monix/issues/91) for details.**

## Overview

Monix is a high-performance Scala / Scala.js library for
composing asynchronous and event-based programs using observable sequences
that are exposed as asynchronous streams, expanding on the
[observer pattern](https://en.wikipedia.org/wiki/Observer_pattern),
strongly inspired by [Reactive Extensions (Rx)](http://reactivex.io/),
but designed from the ground up  for back-pressure and made to cleanly interact
with Scala's standard library and compatible out-of-the-box with the
[Reactive Streams](http://www.reactive-streams.org/) protocol.

Highlights:

- exposes the kick-ass `Observable` and `Task`
- modular, only use what you need
- strives to be idiomatic Scala and encourages referential transparency, 
  but is built to be faster than alternatives
- provides integration with [Cats](http://typelevel.org/cats/) 
- accepted in the [Typelevel incubator](http://typelevel.org/projects/)
- designed for true asynchronicity, running on both the 
  JVM and [Scala.js](scala-js.org), 
- really good test coverage and API documentation as a project policy

## Usage

See **[monix-sample](https://github.com/monixio/monix-sample)** for
a project exemplifying Monix used both on the server and on the client.

Currently compiled for  *Scala 2.10.x*, *2.11.x* and *Scala.js 0.6.x*.
Monix's compatibility extends to the latest 2 major Scala versions and
to the latest major Scala.js version. In other words support for *Scala 2.10.x*
will be kept for as long as possible, but you should not rely on continued
support long after Scala 2.12 is out.

### Dependencies

The packages are published on Maven Central.

- Current stable release: `1.1`
- Current beta release: `2.0-M1`

For the stable release (use the `%%%` for Scala.js): 

```scala
libraryDependencies += "org.monifu" %% "monifu" % "1.1"
```

For the beta/preview release (use at your own risk):

```scala
libraryDependencies += "io.monix" %% "monix" % "2.0-M1"
```

### Sub-projects

Monix 2.0 is modular by design, so you can pick and choose:

- `monix-execution` exposes the low-level execution environment, or more precisely 
  `Scheduler`, `Cancelable` and `RunLoop`
- `monix-eval` exposes `Task`, `Coeval`, `AsyncIterator`, `LazyIterator` and depends on `monix-execution`
- `monix-reactive` exposes `Observable` streams and depends on `monix-eval`
- `monix-types` exposes type-classes, integrated with Cats and depends on `monix-reactive`
- `monix` provides all of the above

## Documentation

NOTE: The documentation is a work in progress.
API Documentation:

- [1.1](https://monix.io/docs/1.1/api/)
- [2.0-M1](https://monix.io/docs/2.0-M1/api/)

## Maintainers

The current maintainers (people who can help you) are:

- Alexandru Nedelcu ([@alexandru](https://github.com/alexandru))
- Andrei Oprisan ([@aoprisan](https://github.com/aoprisan))

## Contributing

The Monix project welcomes contributions from anybody wishing to participate.
All code or documentation that is provided must be licensed with the same
license that Monix is licensed with (Apache 2.0, see LICENSE.txt).

People are expected to follow the [Typelevel Code of Conduct](http://typelevel.org/conduct.html)
when discussing Monix on the Github page, Gitter channel, or other venues.

Feel free to open an issue if you notice a bug, have an idea for a feature, or
have a question about the code. Pull requests are also gladly accepted. For more information,
check out the [contributor guide](CONTRIBUTING.md).

## License

All code in this repository is licensed under the Apache License, Version 2.0.
See [LICENCE.txt](./LICENSE.txt).

## Acknowledgements

<img src="https://raw.githubusercontent.com/wiki/monixio/monix/assets/yklogo.png" align="right" />
YourKit supports the Monix project with its full-featured Java Profiler.
YourKit, LLC is the creator [YourKit Java Profiler](http://www.yourkit.com/java/profiler/index.jsp)
and [YourKit .NET Profiler](http://www.yourkit.com/.net/profiler/index.jsp),
innovative and intelligent tools for profiling Java and .NET applications.

<img src="https://raw.githubusercontent.com/wiki/monixio/monix/assets/logo-eloquentix@2x.png" align="right" width="130" />

Development of Monix has been initiated by [Eloquentix](http://eloquentix.com/)
engineers, with Monix being introduced at E.ON Connecting Energies,
powering the next generation energy grid solutions.
