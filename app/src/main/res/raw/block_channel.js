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
 /*mark for changed*/ "54新觀點|从台湾看见世界|寰宇新聞 頻道|美国之音中文网|夢想街之全能事務所|民視新聞|明鏡火拍|年代向錢看|三立iNEWS|三立LIVE新聞|台視新聞 TTV NEWS|头条軍事【军事头条 軍情諜報 軍事解密 每日更新】歡迎訂閱|香港全城討論區|新聞面對面|新聞追追追";
   _timer=setTimeout(hideChannels(_filters), 300);
  window.addEventListener(
      "scroll", function() {
     clearTimeout(_timer);
      _timer=setTimeout(hideChannels(_filters), 300); })
})()
