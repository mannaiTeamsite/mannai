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
$('#lang_toggle').click(function(){
    $('body').toggleClass('ar').trigger('classChange')
});

$('#advance_search').click(function(){
  $('.search_result').css('display', 'none')
})
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