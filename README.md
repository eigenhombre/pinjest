# pinjest

Cache your pins using the Pinterest API.

You need to generate a token at
https://developers.pinterest.com/tools/access_token/ and put that in
an environment variable `PINTEREST_TOKEN` before running this code
standalone or in the REPL.

# Running

After setting `PINTEREST_TOKEN` as above, and
[installing Leiningen](http://leiningen.org/) and Java,

    lein uberjar
    java -jar target/pinjest.jar

The program will cache your pins locally and make one or more preview
pages (`pins-0.html`, `pins-1.html` and so on).

## License

Copyright © 2016 John Jacobsen.  MIT license; see file `LICENSE`.

## Disclaimer

THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.