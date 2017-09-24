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

Clouseau is currently unpublished, although a 0.0.1 release is coming
soon. Once clouseau is published, you will be able to find the JAR
through the usual dependency resolution systems (Maven, Ivy, etc.).

If you build the JAR yourself, there are some settings you'll need to
add to SBT to use this library:

```
// need to use this to set up instrumentation
javaOptions += "-javaagent:path/to/clouseau.jar"

// needed to start a new JVM with the previous option
fork := true
```

(These settings don't currently allow Clouseau to be used in the
console. I'm still investigating the best way to do this.)

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
val o = List(x, x)
sizeOf(x)       //  80 bytes
staticSizeOf(x) //  48 bytes
fullSizeOf(x)   // 128 bytes
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

The 16 bytes of `staticSize(o)` likely includes the static `Nil` value
that all lists share. Notice that `fullSize(o)` is 216 bytes, which is
significantly more than `sizeOf(o) + staticSize(o)` (160 bytes). The
reason here is that we are also including static fields referenced by
the values that make up the list (in this case `x`).

(See the *Caveats* section to get an idea of the limitations of these
kinds of estimates.)

There is also a compatibility API provided for Java, which exposes
static methods (which are easier to call than methods on Scala
objects).

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
val bytes0 = calculate(m0, s, JustClass).bytes //

val m1 = m0.updated(99, "ninety-nine")
val bytes1 = calculate(m1, s, JustClass).bytes

println((bytes0, sizeOf(m0))) // (13840,13840)
println((bytes1, sizeOf(m1))) // (336,13856)
```

The values `bytes0` and `sizeOf(m0)` are identical. This means that
all of the data in `m0` is being counted for the first time. By
contrast, `bytes1` is much smaller than `sizeOf(m1)`, which means that
most of the objects being referenced by `m1` had already been counted
by the first `calculate` call. Since `s` is a mutable set, as long as
we use the same set we ensure that repeatedly-referenced objects will
not be counted again.

The `Mode` used in this example (`JustClass`) corresponds to the logic
of the `sizeOf` method. The other modes (`JustStatic` and
`ClassAndStatic`) correspond to the `staticSizeOf` and `fullSizeOf`
methods respectively.

### Caveats

Clouseau is based around the `getObjectSize` method from the
`java.lang.instrumentation.Instrumentation` class. From that method's
documentation:

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

 1. Ability to use Clouseau from the SBT console.
 2. Providing an SBT plugin for use in other projects (may subsume 1).
 3. Improve accuracy of primitives/enumerations.
 4. Verify estimates against other tools (YourKit, JProfiler, etc.)
 5. Compare estimates before/after Hotspot JITs the relevant classes.
 6. Provide better documentation and intuitions around JVM memory usage.
 7. Provide a "fall back" strategy that avoids `Instrumentation` when unavailable.
 8. Compare different JVMs and JVM versions.

### See Also

This project was inspired by
[ObjectExplorer and MemoryMeasurer](https://github.com/DimitrisAndreou/memory-measurer).
The general approach is taken from this project, which unfortunately
isn't under active development and doesn't seem to distribute JAR files.

### Copyright and License

All code is available to you under the Apache 2 license, available at
https://opensource.org/licenses/Apache-2.0.

Copyright Erik Osheim, 2017.
