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



$('.nav_tab_next').click(function(){
  console.log('yes')
  $('#result_tabs').css({"transform":"translateX(200px)"})
});

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



