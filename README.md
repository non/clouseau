## Clouseau

### Preamble

```
                LADY LITTON
        I hope you locate the trouble, monsieur.

                INSPECTER CLOUSEAU
        Madame, it is my business to locate trouble.

Clouseau turns and collides painfully with the doorway.

                INSPECTER CLOUSEAU
        No trouble back there!
```

### Overview

Clouseau is a JVM library designed to help estimate the in-memory size
of various objects on the JVM. This is done using the machinery
provided by
[java.lang.instrument.Instrumentation](https://docs.oracle.com/javase/8/docs/api/java/lang/instrument/Instrumentation.html)
as well as the Java reflection API.

Currently you can build the project and use the JAR file it produces
in your won projects.

The ultimate goal (not yet realized) is to provide an SBT plugin that
makes it easy to launch tests (or a console) from SBT with the
instrumentation automatically set up.

### Quick Start

Clouseau supports Java 1.6+, and Scala 2.10, 2.11, and 2.12.

Clouseau is available on Maven Central. The easiest way to use
Clouseau with SBT is to enable the
[sbt-javaagent](https://github.com/sbt/sbt-javaagent) plugin.

In `project/plugins.sbt` you'd add:

```scala
addSbtPlugin("com.lightbend.sbt" % "sbt-javaagent" % "0.1.4")
```

In `build.sbt` you'd add:

```scala
enablePlugins(JavaAgent)

// assumes you're using scala 2.12.x; change as-needed
javaAgents += "org.spire-math" % "clouseau_2.12" % "0.2.0" % "compile;runtime"
```

### Less Quick Start

You can also use Clouseau manually using `-javaagent` explicitly.

Download the Clouseau jar file using the appropriate link:

 * [version 0.2.0, scala 2.12](https://search.maven.org/remotecontent?filepath=org/spire-math/clouseau_2.12/0.2.0/clouseau_2.12-0.2.0.jar).
 * [version 0.2.0, scala 2.11](https://search.maven.org/remotecontent?filepath=org/spire-math/clouseau_2.11/0.2.0/clouseau_2.11-0.2.0.jar).
 * [version 0.2.0, scala 2.10](https://search.maven.org/remotecontent?filepath=org/spire-math/clouseau_2.10/0.2.0/clouseau_2.10-0.2.0.jar).

If you had downloaded Clouseau to `path/to/clouseau.jar` you'd include
Clouseau in your project via the following `build.sbt` configuration:

```scala
// need to use this to set up instrumentation
javaOptions += "-javaagent:path/to/clouseau.jar"

// needed to start a new JVM using the -javaagent
fork := true
```

### Usage

The basic API of Clouseau is intended to be very simple to use:

```scala
import clouseau.Calculate

val interestingObject = ...

// all sizes represented in bytes
val x: Long = Calculate.sizeOf(interestingObject)
val y: Long = Calculate.staticSizeOf(interestingObject)
val z: Long = Calculate.fullSizeOf(interestingObject)
```

At a high level, we can separate objects referenced by a particular
object into *instance members* and *static members*. Static members
are defined in a given class and shared by all instances of that class
or its subclasses, whereas instance members are not (necessarily)
shared.

For example, consider the following:

```scala
import clouseau.Calculate._

val x = "this is a sentence"
sizeOf(x)       //  80 bytes
staticSizeOf(x) //  48 bytes
fullSizeOf(x)   // 128 bytes

val o = List(x, x)
sizeOf(o)       // 144 bytes
staticSizeOf(o) //  16 bytes
fullSizeOf(o)   // 216 bytes
```

First of all, notice that the string `x` (of 18 characters) takes up
80 bytes, instead of the 19 bytes that a C programmer might
expect. Let's put this aside for now. It looks like `x` also depends
on 48 bytes of static data (via `java.lang.String`), which is shared
across all string values in this JVM. In this case, we can add the
results of `sizeOf(x)` and `staticSizeOf(x)` to get the
`fullSizeOf(x)` -- but be aware that this won't always be the case.
(See the *Details* section for a more in-depth discussion of this.)

Now, looking at `o`, we see that `sizeOf(o)` is less than twice
`sizeOf(x)`. This is because the list stores two references to `x`, so
we don't count `sizeOf(x)` twice (although we *will* count the size of
two references). The 144 bytes of `sizeOf(o)` will include the 80
bytes of `x` once, as well as 64 bytes of other data. These data are
likely references: two different references to `x`, as well as the
references to cons cells that make up a linked list.

The 16 bytes of `staticSize(o)` includes the static `Nil` value that
all lists share. Notice that `fullSize(o)` is 216 bytes, which is
significantly more than `sizeOf(o) + staticSize(o)` (160 bytes). The
reason here is that we are also including static fields referenced by
the values that make up the list (in this case `x`).

(See the *Caveats* section to get an idea of the limitations of these
kinds of estimates.)

There is also a compatibility API provided for Java. This API exposes
static methods that should be easier to call from Java than methods on
Scala objects.

```java
import clouseau.compat.Calculate;

Object interestingObject = ...;

// all sizes in bytes
long x = Calculate.sizeOf(interestingObject);
long y = Calculate.staticSizeOf(interestingObject);
long z = Calculate.fullSizeOf(interestingObject);
```

### Details

It can be tricky to differentiate the three top-level methods
provided. Here is an overview which defines the methods recursively,
and tries to make their relationship to each other clear.

`sizeOf(x)` is defined as:
 * the sum of `sizeOf(_)` for all non-static members of `x`.

`staticSizeOf(x)` is defined as:
 * the sum of `fullSizeOf(_)` for all static members of `x`.

`fullSizeOf(x)` is defined as:
 * the sum of `fullSizeOf(_)` for all non-static members of `x`
 * added to the sum of `fullSizeOf(_)` for all static members of `x`.

Some important things to notice are:

 1. We can't assume that any of these counts don't overlap. It's
    possible that an object counted in `sizeOf(x)` is also referenced
    in a static field counted by `staticSizeOf(x)`.

 2. `fullSizeOf(x)` will potentially include values that weren't
    counted by either `sizeOf` or `staticSizeOf`. For example, if `x`
    has a (non-static) field referencing `y`, and `y` has a static
    field referencing `z`, then `z` would not be taken into account by
    `sizeOf(x)` or `staticSizeOf(x)`, but *would* be taken into
    account by `fullSizeOf(x)`.

 3. It is critical that we avoid double-counting, since it's possible
    to reference the same object multiple times, or to have multiple
    instances with the same static fields.

Clouseau uses a 64-bit hashing scheme to try to avoid
double-counting. We hash objects to avoid double-counting their sizes,
and we also hash classes to avoid double-counting their static
members. It's possible that hash collisions will cause us to
undercount, but in practice this should be very unlikely. See
`clouseau.Identity.hash` for more information.

The lower-level `calculate` method will return the set of all hash
codes that we've seen so far in addition to an estimate. This makes it
possible to do more advanced profiling, such as measuring an *initial
state*, followed by one or more measurements which will only measure
the additional memory used since the initial sate.

Here's an example of using `calculate`:

```scala
import clouseau.Mode.JustClass
import clouseau.Calculate.{calculate, sizeOf}
import scala.collection.mutable

val s = mutable.Set.empty[Long]

val m0 = (1 to 100).iterator.map(i => (i, i.toString)).toMap
val bytes0 = calculate(m0, s, JustClass).bytes

val m1 = m0.updated(99, "ninety-nine")
val bytes1 = calculate(m1, s, JustClass).bytes

println((bytes0, sizeOf(m0))) // (13840,13840)
println((bytes1, sizeOf(m1))) // (336,13856)
```

The values `bytes0` and `sizeOf(m0)` are identical. This means that
all of the data in `m0` is being counted for the first time. By
contrast, `bytes1` is much smaller than `sizeOf(m1)`, which means that
most of the objects being referenced by `m1` had already been counted
by the first `calculate` call. Only 2.4% of the total size of `m1` has
to be allocated; the other 97.6% is shared!

(Since `s` is a mutable set, as long as we use the same set we ensure
that repeatedly-referenced objects will not be counted again.)

The `Mode` used in this example (`JustClass`) corresponds to the logic
of the `sizeOf` method. The other modes (`JustStatic` and
`ClassAndStatic`) correspond to the `staticSizeOf` and `fullSizeOf`
methods respectively.

Clouseau also includes a method for producing human-readable sizes:

```scala
import clouseau.Units

(1 to 5).foreach { i =>
  val bytes = math.pow(137, i.toDouble).toLong
  println(Units.approx(bytes))
}
// 137B
// 18.3K
// 2.45M
// 336M
// 44.9G
```

### Using Clouseau in the REPL

One natural use of Clouseau is in the REPL, where it can estimate the
space used by interactively-constructed values. However SBT does not
currently support forking the `console` command, making it difficult
to use Clouseau interactively. The *clouseau-repl* module solves this
problem by providing a main class which can be `run` from SBT
interactively.

To use *clouseau-repl* from within your project, first follow the
*Quick Start* guide for including Clouseau. Once Clouseau is included,
add the following to your `build.sbt` file:

```scala
libraryDependencies += "org.spire-math" %% "clouseau-repl" % "0.2.0"

// this main class runs the standard scala REPL
mainClass in Compile := Some("clouseau.Repl")

// allows forked processes to read from SBT's stdin
connectInput in run := true

// don't buffer stdout, so the user can see prompt, input, etc.
outputStrategy := Some(StdoutOutput)
```

After these changes, the `run` command can be used to launch the REPL.

Hopefully in the future SBT will support running a forked `console`,
at which point this module and configuration will become unnecessary.

### Caveats

Clouseau is based around the `getObjectSize` method from the
`java.lang.instrumentation.Instrumentation` class. From that method's
[documentation](https://docs.oracle.com/javase/8/docs/api/java/lang/instrument/Instrumentation.html#getObjectSize-java.lang.Object-):

> Returns an implementation-specific approximation of the amount of
> storage consumed by the specified object. The result may include
> some or all of the object's overhead, and thus is useful for
> comparison within an implementation but not between
> implementations. The estimate may change during a single invocation
> of the JVM.

The values returned from Clouseau are subject to these same provisos.

The project's humorous name is intended to help set expectations
(although the project's goal is to be as accurate as possible using
the available Java APIs).

Known weaknesses in this version of Clouseau are in its handling of
primitive static values (which we can't use `getObjectSize` to
estimate).

If you find results that you believe are incorrect, please open an
issue with a minimized test case demonstrating the incorrect result,
as well as some analysis (bytecode, profiling, etc.) which shows the
correct result along with the JVM version you are using.

### Future Work

Here are some directions Clouseau will (hopefully) be moving in:

 1. Improve accuracy of primitives/enumerations.
 2. Verify estimates against other tools (YourKit, JProfiler, etc.)
 3. Compare estimates before/after Hotspot JITs the relevant classes.
 4. Provide better documentation and intuitions around JVM memory usage.
 5. Provide a "fall back" strategy that avoids `Instrumentation` when unavailable.
 6. Compare different JVMs and JVM versions.
 7. Provide more flexible/extensible API for traversing fields.

### See Also

This project was inspired by
[ObjectExplorer and MemoryMeasurer](https://github.com/DimitrisAndreou/memory-measurer).
The general approach is taken from this project, which unfortunately
isn't under active development and doesn't seem to distribute JAR files.

### Copyright and License

All code is available to you under the Apache 2 license, available at
https://opensource.org/licenses/Apache-2.0.

Copyright Erik Osheim, 2017-2018.
