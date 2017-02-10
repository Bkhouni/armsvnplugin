function navigateTo(url){
	if(window.parent){
		window.parent.location.href=url;
	}else{
		window.location.href=url;
	}
}