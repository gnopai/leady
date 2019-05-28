leady
=====

This project deduplicates a provided json file full of leads, and spits out a record of what it decided to do along the way.
Right now the only available logging method for changes is to stdout, but it's setup to be easily adaptable to other methods,
given that hemorrhaging piles of PII to one's console probably isn't the most ideal scenario.

Running It
----------

This project requires Java 11 to be in your path. The project can be built, tested, and run using the provided gradle wrapper:

`./gradlew build`

`./gradlew test`

`./gradlew run`

If no arguments are provided, the sample leads file in `src/main/resources` will be used, and output will be written
to `out/deduped_leads.json`. Otherwise you can provide arguments:

`./gradlew run --args '/my/dir/leads.json /my/other/dir/output.json'`

