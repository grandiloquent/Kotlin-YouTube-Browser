(function() {
function hideChannels(filter) {
  var filters = filter.split('|');
  var nodes = document.querySelectorAll(
      'ytm-compact-video-renderer:not([h]),ytm-item-section-renderer:not([h])');
  if (nodes.length < 1) return;

  for (var index = 0; index < nodes.length; index++) {
    var element = nodes[index];

      var title =
          element.querySelector('.text-info,.compact-media-item-byline');


      if (!title) continue;
      var name = title.innerText;

      if (filters.indexOf(name) > -1) {

        element.outerHTML="";
      } else {
        element.setAttribute('h', '');
      }
  }
}
var _timer;
var _delay = 300
var _filters =
 /*mark for changed*/

_timer = setTimeout(hideChannels(_filters), _delay / 5);
window.addEventListener('scroll', function() {
  clearTimeout(_timer);
  _timer = setTimeout(hideChannels(_filters), _delay);
})
})()

