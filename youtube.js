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
  var _filters = "54新觀點|台視新聞 TTV NEWS|新聞面對面|新聞追追追";

  setTimeout(hideChannels(_filters), 50);
  window.addEventListener(
      "scroll", function() { setTimeout(hideChannels(_filters), 50); })
})()