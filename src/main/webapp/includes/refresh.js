function init() {
	if (document.layers) {
		document.waitpage.visibility = 'hide';
		document.mainpage.visibility = 'show';
	} else {
		if (document.all) {
			document.all.waitpage.style.visibility = 'hidden';
			document.all.mainpage.style.visibility = 'visible';
		}
	}
}