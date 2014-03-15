require("pink/css/themes/dijkstra.less");

var Pink = require("pink");
new Pink("#slides", {
  "editor": require("pink/modules/editor")([
    require("pink/modules/editor/language/bodol"),
    require("pink/modules/editor/language/clojure")
  ]),
  "background": require("pink/modules/background"),
  "image": require("pink/modules/image")
});
