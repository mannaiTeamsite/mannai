var myVideo = document.getElementById("video"); 

function playPause() { 
  if (myVideo.paused) {
    myVideo.play(); 
    $('.play img').attr('src', 'assets/images/pause.png');
    $(myVideo).parent().addClass('playing')
  }
  else 
  {
    myVideo.pause(); 
    $('.play img').attr('src', 'assets/images/play.png')
  }
} 

$(document).ready(function(){
  $('#search_btn').click(function(){
    $('#collapseExample').collapse('hide');
    $('.search_result').css('display', 'none')
   });
   // ======= for lang change =======

$('#advance_search').click(function(){
  $('.search_result').css('display', 'none')
});

// $('#search_btn').click(function(){
//   $('#collapseExample').collapse('hide')
//  })
});

// ======= to add class in dropdown menu in arabic version =======
$('body').on('classChange', function(){
  if($('body').hasClass('ar')){
    $('.search_accordion_container ul li').addClass('dropright');
    $('.result_blocks li').addClass('dropright');
  }
  else{
    $('.search_accordion_container ul li').removeClass('dropright')
  }
});

// ======= function to open search on typing in search box =======
function showSearch(e){
  if($(e).val() != ""){
    $('.search_result').css('display', 'block');
    $('#collapseExample').collapse('hide')
  }
  if($(e).val() == ""){
    $('.search_result').css('display', 'none')
  }
}

$('.nav_tab_prev').click(function(){
  $('#result_tabs').css({"transform":"translateX(-200px)"})
});

$('.carousel').carousel({
  interval: false,
})

$(document).ready(function () {               // on document ready
  checkitem();
});

$('#videoResultSlider').on('slid.bs.carousel', checkitem);

function checkitem()                        // check function
{
  var $this = $('#videoResultSlider');
  if ($('.carousel-inner .carousel-item:first').hasClass('active')) {
      // Hide left arrow
      $this.children('.carousel-control-prev').css({'opacity': '0.5', 'pointer-events': 'none'});
      // But show right arrow
      $this.children('.carousel-control-next').css({'opacity': '1', 'pointer-events': 'auto'});
  } else if ($('.carousel-inner .carousel-item:last').hasClass('active')) {
      // Hide right arrow
      $this.children('.carousel-control-next').css({'opacity': '0.5', 'pointer-events': 'none'});
      // But show left arrow
      $this.children('.carousel-control-prev').css({'opacity': '1', 'pointer-events': 'auto'});
  } else {
      $this.children('.carousel-control').css({'opacity': '1', 'pointer-events': 'auto'});
  }
}

// ======= search result tabs slider =======
// var slideItem = $('.tabs_result .nav-tabs li').length;
// viewSlideItem(0, 3);
// console.log(slideItem);
// function viewSlideItem(firts, last){
//   $('.tabs_result .nav-tabs li').eq(firts).addClass('first-active');
//   $('.tabs_result .nav-tabs li').eq(last).addClass('last-active');
//   // for(i = 0; i <= num - 1; i++){
//   //   $('.tabs_result .nav-tabs li').eq(i).addClass('inView');
//   // }
// }
// var nextWdth = 0;
// var lastWidth = 0;
// function slideNext(){
//   var lastActive = $('.tabs_result .nav-tabs').find('.last-active').next('li');
//   var firstActive = $('.tabs_result .nav-tabs').find('.first-active').next('li');
//   nextWdth = nextWdth + $(lastActive).width();
//   console.log(nextWdth)
//   $('.tabs_result').find('.nav-tabs').css({'transform' : 'translateX(-' + nextWdth +'px)'});
//   $('.tabs_result .nav-tabs li').removeClass('last-active');
//   $(lastActive).addClass('last-active');
//   $('.tabs_result .nav-tabs li').removeClass('first-active');
//   $(firstActive).addClass('first-active');
// }

// function slidePrev(){
//   var firstActive = $('.tabs_result .nav-tabs').find('.first-active').prev('li');
//   lastWidth = nextWdth - $(firstActive).width();
//   console.log(lastWidth)
//   $('.tabs_result').find('.nav-tabs').css({'transform' : 'translateX(' + lastWidth +'px)'});
//   $('.tabs_result .nav-tabs li').removeClass('first-active');
//   $(firstActive).addClass('first-active')
// }

$('.result_carousal').owlCarousel({
  loop:false,
  // margin:2,   
  responsiveClass:true,
  autoplayHoverPause:true,
  autoplay:false,
  //  autoWidth: true,
   dots: false,
   nav: true,
  //  items: 34
   navText: [
     '<img src="assets/images/arrow-left.svg">', 
     '<img src="assets/images/arrow-right.svg">'
    ],
  responsive:{
      0:{
        items:2,
      },
      600:{
          items:2
      },
      768:{
        items:3
      },
      900:{
        items:4
      },
      1024:{
        items:4
      }
  }
})

$(document) .ready(function(){
var li =  $(".owl-item a ");
$(".owl-item a").click(function(){
li.removeClass('active');
});
});