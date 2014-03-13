module.exports.toArray = function toArray(indexable) {
  const out = [], l = indexable.length;
  for (let i = 0; i < l; i++) {
    out.push(indexable[i]);
  }
  return out;
}

module.exports.flatMap = function flatMap(f, seq) {
  const out = [], l = seq.length;
  for (let i = 0; i < l; i++) {
    out = out.concat(f(seq[i]));
  }
  return out;
}

function copyProps(target, src) {
  for (let prop in src) {
    if (src.hasOwnProperty(prop)) {
      target[prop] = src[prop];
    }
  }
  return target;
}

module.exports.merge = function merge(obj1) {
  return arguments.slice(1).reduce((acc, next) => {
    return copyProps(acc, next);
  }, copyProps({}, obj1));
}
