$(document).ready(function(){
   
   
    var servicebxslider,newsbxslider ; //= fourslider(4,'.bxslider2','#newsNext','#newsPrev');
   $(".btn-responsive-open").click(function(){
       $(".tundra").css('overflow','hidden');
   });	
   $(".btn-responsive-close").click(function(){
       $(".tundra").css('overflow','scroll');
   });	
   $("#next,#prev,#prev1,#next1").hide();
   $(".ico-search").click(function(){
       $(".tundra").css('overflow','hidden');
   });
   $(".close").click(function(){
       $(".tundra").css('overflow','scroll');
   });
   //-------Image SlideShow-------//
   var bannerTimer;
   var playingFlag = true;
    var tabcount = 0;
   $("#play").addClass("play-visited");
   $("#pause").addClass("pause");
   $("#prev-banner").click(function(){
       nextbanner(-1);
   });
   $("#next-banner").click(function(){
       nextbanner(1);
   });
   $("#pause").click(function(){
       //alert('pause');
       //alert(bannerTimer);
       if(playingFlag) {
           
           clearInterval(bannerTimer);
           playingFlag = false;
           $("#pause").removeClass("pause").addClass("pause-visited");
           $("#play").removeClass("play-visited").addClass("play");
       }else{
           return;
       }
   });
   $("#play").click(function(){
       $(this).hide();
       if(!playingFlag) {
           //alert('play');
           nextbanner(1);
           clearInterval(bannerTimer);
           bannerTimer = setInterval(function(){ 
           nextbanner(1);
           }, 10000);
           playingFlag = true;
           $("#play").removeClass("play").addClass("play-visited");
           $("#pause").removeClass("pause-visited").addClass("pause");
       }else{
           
           return;
       }
   });
   var Images=1;
   showinnerbanner(Images);
   function nextbanner(n) {
       showinnerbanner(Images += n);
   }
   function showinnerbanner(n) {
       var i;
       var slides5 = $(".innerslides1");
       if (n > slides5.length) {Images = 1}    
       if (n < 1) {Images = slides5.length}
       for (i = 0; i < slides5.length; i++) {
       slides5[i].style.display = "none";  
       }
       $(slides5[Images-1]).fadeIn().focus();
       $("#img-count").text(Images);
       $("#img-total").text(slides5.length);
   }
   bannerTimer = setInterval(function(){ 
       nextbanner(1);
   }, 10000);
   
   function reseter() {
        Images=1;
       showinnerbanner(Images);
   }
   
   $( "#carousel" ).keydown(function(e) {
       if (e.keyCode == 9) {
             if (tabcount == 0) {
                   $("#pause").click();
                   reseter();
                   tabcount = 1;
               }
              nextbanner(1);
         }
     });

/*********Language changing script*********/



   //---------On Click of SlideIndicator----------//
   $(".services-container ul li").click(function(){
       var str=this.className;
       //console.log($((this.parentNode)).find( "li" ));
       var slides1=$(this.parentNode).siblings("div");
       var dots=$((this.parentNode)).find( "li" );
       var className=str.split(" ");
       console.log(slides1)
       switch(className[1]){
           case "slide-indicator-1":
               SlideShow(0);
           break;
           case "slide-indicator-2":
               SlideShow(1);
           break;
           case "slide-indicator-3":
               SlideShow(2);
           break;
           case "slide-indicator-4":
               SlideShow(3);
           break;
       }
       function SlideShow(num){
           var i;
           for (i = 0; i < slides1.length-1; i++) {
               slides1[i].style.display = "none";  
               dots[i].firstChild.className="";
           }
           $(slides1[num]).fadeIn(); 
           dots[num].firstChild.className="active";
       }                          
   });  
   
   
   //----------On Screen Width Services, Events, News & Topics SlideShow----------//

  
  
});









