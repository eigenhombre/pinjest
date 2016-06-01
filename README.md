# pinjest

Scratch pad for experiments w/ Pinterest API.  See `src/pinjest/core.clj`.

For the moment, you need to generate a token at
https://developers.pinterest.com/tools/access_token/ and put that in
an environment variable PINTEREST_TOKEN before running this code
standalone or in the REPL.

# Running

After setting PINTEREST_TOKEN as above,

    lein uberjar
    java -jar target/pinjest.jar

The program will cache your pins locally and make one or more preview
pages (`pins-0.html`, `pins-1.html` and so on).

## LICENSE

Copyright © 2016 John Jacobsen.  See `LICENSE`.

