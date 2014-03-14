Building Your Own Lisp For Great Justice
========================================

Here is a slide deck for a talk I gave. Much code. Very ponies. Wow.

Notes for code slides [are here](notes.clj).

Running
-------

Slides can be run directly on the internets at
[http://bodil.org/building-lisp](http://bodil.org/building-lisp). The
REPL slides will only work if you have a REPL server running locally,
though.

To launch the REPL server, you'll need to install
[https://github.com/technomancy/leiningen](Leiningen), then:

```sh
$ cd repl-server/clojure
$ lein run
```

To setup and run the local HTTP server for the slides:

```sh
$ npm install
$ npm start
```

Keybindings
-----------

Navigate through slides using `PgUp` and `PgDn`.

Editor cheat sheet:

* `Ctrl-S` evaluates the whole buffer.
* `Ctrl-D` evaluates the top level expression at cursor point. (currently unimplemented)
* `Ctrl-R` reloads the HTML document without evaluating the buffer.
* `Ctrl-,` toggles commenting of the current line or selection.
* `Ctrl-'` selects the token at point.
* `Ctrl-\` invokes autocomplete on the token at point.
* `Ctrl-I` shows the inferred type of the token at point.
* `Ctrl-Q` renames the variable at point.
* `Alt-.` jumps to the definition of the token at point.
* `Alt-,` jumps back.
* `Ctrl-K` and `Ctrl-Y` kills to end of line and yanks from the kill buffer as in Most Holy Emacs.
* `Ctrl-A` and `Ctrl-E` similarly moves cursor to start and end of line.
* `Tab` will indent the current line to where it's supposed to be.
* `Alt-Space` sends a space bar keypress event to the HTML document.
* etc. for enter and cursor keys.
* Finally, `Esc` moves focus from the editor back to the slide deck, so you can change slides etc. (Also, if the slide deck is active, `Tab` should focus the editor.)

License
-------

Copyright 2014 Bodil Stokke

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program. If not, see
[http://www.gnu.org/licenses/](http://www.gnu.org/licenses/).
