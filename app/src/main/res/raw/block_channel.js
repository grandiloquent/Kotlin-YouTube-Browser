(function() {
  function hideChannels(filter) {
    var filters = filter.split('|');
    var nodes = document.querySelectorAll(
        "ytm-compact-video-renderer,ytm-item-section-renderer");
    if (nodes.length < 1) return;

    for (var index = 0; index < nodes.length; index++) {
      var element = nodes[index];
      var title =
          element.querySelector('.text-info,.compact-media-item-byline');
      console.log(element, title);
      if (!title) continue;
      var name = title.innerText;
      // console.log(name,filters.indexOf(name));
      if (filters.indexOf(name) > -1) element.style.display = "none";
    }
  }
  var _timer;
  var _delay=300
  var _filters =
 /*mark for changed*/
   _timer=setTimeout(hideChannels(_filters), 300);
  window.addEventListener(
      "scroll", function() {
     clearTimeout(_timer);
      _timer=setTimeout(hideChannels(_filters), 300); })
})()
