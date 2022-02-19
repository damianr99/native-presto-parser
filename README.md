# Native Presto Parser prototype

Proof of concept - build a native presto parser for using in C / C++ /
Python by using the GraalVM native-image compiler to build it directly
from the Presto Java code base.

## Prerequisites
You need to install GraalVM native image
Grab it from here: 
https://github.com/graalvm/graalvm-ce-builds/releases/tag/vm-22.0.0.2

Set `GRAAL_HOME` to where it is installed. Then ensure nativeimage is installed
`$GRAAL_HOME/bin/gu install native-image`

You'll also need Maven.

## Build native shared library

`make native`

This will build a native Presto parser as a shared library in
`target/native/NativePrestoParser.so` (or `.dylib` on a mac).

## Build sample C app that uses the binary

A trivial example of how to use the parser is in
`src/main/c/prestoparser.c`. Build it with: `make c`.
This will place a file `a.out` in the current directory. 

This program accepts a SQL statement on stdin, and prints a parse tree
to stdout. Sample run:

```
$ echo 'SELECT x,y+2 FROM t WHERE x<y ORDER BY z LIMIT 5' | /usr/bin/time ./a.out
Query 
   QueryBody
   QuerySpecification 
      Select
            Identifier[x]
            ADD
               Identifier[y]
               Long[2]
      From
         Table[t]
      Where
         LESS_THAN
            Identifier[x]
            Identifier[y]
      OrderBy
         Identifier[z]
      Limit: 5

0.00user 0.00system 0:00.01elapsed 85%CPU (0avgtext+0avgdata 16228maxresident)k
0inputs+0outputs (0major+1803minor)pagefaults 0swaps
```

Note, it completes in **0.01s** including binary startup. Parsing this
way is *fast*.

## Build the Python package

First ensure you have pybind11 installed: `pip install pybind11`

Build the python module with: `make python`

Then you can just load it via `import pypresto`.  Example session:
```
>>> import pypresto
>>> def parse(sql):
...     print(pypresto.parse(sql))
...
>>> parse("select * from foo")
Query 
   QueryBody
   QuerySpecification 
      Select
         *
      From
         Table[foo]
>>>		 
>>> # Let's try a more complicated query, TPC-H q11
>>> tpch11 = """
select
	ps_partkey,
	sum(ps_supplycost * ps_availqty) as value
from
	partsupp,
	supplier,
	nation
where
	ps_suppkey = s_suppkey
	and s_nationkey = n_nationkey
	and n_name = 1
group by
	ps_partkey having
		sum(ps_supplycost * ps_availqty) > (
			select
				sum(ps_supplycost * ps_availqty) * 2
			from
				partsupp,
				supplier,
				nation
			where
				ps_suppkey = s_suppkey
				and s_nationkey = n_nationkey
				and n_name = 1
		)
order by
	value desc
"""
Query 
   QueryBody
   QuerySpecification 
      Select
            Identifier[ps_partkey]
         Alias: value
            FunctionCall[sum]
               MULTIPLY
                  Identifier[ps_supplycost]
                  Identifier[ps_availqty]
      From
         Table[partsupp]
         Table[supplier]
         Table[nation]
      Where
         AND
            AND
               EQUAL
                  Identifier[ps_suppkey]
                  Identifier[s_suppkey]
               EQUAL
                  Identifier[s_nationkey]
                  Identifier[n_nationkey]
            EQUAL
               Identifier[n_name]
               Long[1]
      GroupBy
      SimpleGroupBy
         Identifier[ps_partkey]
      Having
         GREATER_THAN
            FunctionCall[sum]
               MULTIPLY
                  Identifier[ps_supplycost]
                  Identifier[ps_availqty]
            SubQuery
               Query 
                  QueryBody
                  QuerySpecification 
                     Select
                           MULTIPLY
                              FunctionCall[sum]
                                 MULTIPLY
                                    Identifier[ps_supplycost]
                                    Identifier[ps_availqty]
                              Long[2]
                     From
                        Table[partsupp]
                        Table[supplier]
                        Table[nation]
                     Where
                        AND
                           AND
                              EQUAL
                                 Identifier[ps_suppkey]
                                 Identifier[s_suppkey]
                              EQUAL
                                 Identifier[s_nationkey]
                                 Identifier[n_nationkey]
                           EQUAL
                              Identifier[n_name]
                              Long[1]
      OrderBy
         Identifier[value]

>>> 
```

## Known Limitations

Currently we are just returning the string result from the Presto AST
TreePrinter class. For a more useful parser, we'll need to generate an
AST that can be manipulated on the C side. For that, we can generate a
tree of C structs that are equivalent to Presto sql.tree.Node classes.
One way of doing that would be to walk the Presto AST using AstVisitor
much like the presto.sql.TreePrinter we are using does. An example of
returning a tree of C-structs using GraalVM native-image is
demonstrated here:
https://github.com/michael-simons/neo4j-java-driver-native-lib/blob/master/src/main/java/org/neo4j/examples/drivernative/DriverNativeLib.java

The presto TreePrinter we are using (unnecessarily) uses unsafe
methods to build the the string output. It uses `io.airlift.slice`
when generating the string for sql.tree.StringLiteral nodes. With the
default settings on native-image this throws an exception. We probably
need to tweak the config of native-image to make this work, but as
noted above we do not want to use TreePrinter anyhow and instead
return a tree of structs, so I've not looked into fixing this.
