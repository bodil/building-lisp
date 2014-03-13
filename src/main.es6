window.global = window;
require("traceur/bin/traceur-runtime");
require("./css/screen.less");
require("./css/themes/dijkstra.less");
var Deck = require("./deck");
window.deck = new Deck(document.getElementById("slides"), {
  "editor": require("./modules/editor")([
    require("./modules/editor/language/bodol"),
    require("./modules/editor/language/clojure")
  ]),
  "background": require("./modules/background"),
  "image": require("./modules/image")
});
