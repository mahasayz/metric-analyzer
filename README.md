# metric-analyzer

A time-series metric analyzer to analyze metrics from a file-system. A file may contain metrics in the following format:

```
1355270609 1.80215
1355270621 1.80185
1355270646 1.80195
```

After analysis of the above metric, we shall print the following analysis:

- **T** - number of seconds since beginning of epoch at which rolling window ends
- **V** - measurement of price ratio at time T
- **N** - number of measurements in window
- **RS** - rolling sum of measurements in window
- **MinV** - minimum price ratio in the window
- **MaxV** - maximum price ratio in the window

```
T          V       N RS      MinV    MaxV    
---------------------------------------------
1355270609 1.80215 1 1.80215 1.80215 1.80215 
1355270621 1.80185 2 3.604   1.80185 1.80215 
1355270646 1.80195 3 5.40595 1.80185 1.80215 
```
To run:

```
sbt "run <file-path>"
```

To test:

```
sbt test
```

To test with coverage:

```
sbt clean coverage test coverageReport
```
Coverage reports shall be generated in `targe/scala-2.11/scoverage-report`

Test coverage has been tested to be at **70.15%**
