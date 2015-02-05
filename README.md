# Graphite

Generates random graphs with a uniform distribution from a context-free hypergraph grammar.

Use graphite with the following: `graphite=java -jar graphite-1.1.jar`

```
graphite
  Opens the graphite gui
graphite gui filename
  Opens the graphite gui with a specified file
graphite generate --size=int [--number=int] [--verbose] [--distinct] [--output=path] [--open] filename
  Generates a number of graphs with a specified size
    size       : The size of graph to generate, optionally use a range eg 1..10
    number     : The number of graphs to generate. Default 1
    verbose    : Output detailed information. Default false
    distinct   : Whether to print the distinct graphs when it's finished.
    output     : Optionally a directory to output the generated graphs to.
    open       : Whether to open the graphs after generated.
    filename   : The input grammar to use.
graphite enumerate --size=int filename
  Counts the number of graphs with a specified size
    size       : The size of graph to count
graphite benchmark --size=int [--number=int] filename
  Generates graphs with sizes iterating from 1 to a given size
    size       : The maximum size graph to generate
    number     : Each iteration should generate this number of graphs. Default 1
 ```

# Examples

`graphite generate --size=501 --number=5 --open examples/treegrammarlr.xml`

`graphite generate --size=201 --number=100 --output=output examples/treegrammarlr.xml`

`graphite generate --size=13 --number=800000 --distinct --open examples/abgrammar.xml`

`graphite enumerate --size=5 --number=640 --verbose examples/flowgrammar.xml`

`graphite benchmark --size=100 --number=1000 examples/treegrammar.xml`
