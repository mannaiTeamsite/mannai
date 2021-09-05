var artl =$('body').hasClass("ar") ? true : false;

window.MSInputMethodContext && document.documentMode && document.write('<script src="https://cdn.jsdelivr.net/gh/nuxodin/ie11CustomProperties@4.1.0/ie11CustomProperties.min.js"><\x2fscript>');

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

   $('.close_search').click(function(){
    $('#collapseExample').collapse('hide');
   });

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
    $('#collapseExample').collapse('hide');
    $('.search_backdrop').css('display', 'block');
  }
  if($(e).val() == ""){
    $('.search_result').css('display', 'none');
    $('.search_backdrop').css('display', 'none');
  }
}

$('.search_backdrop').click(function(){
  $('.search_result').css('display', 'none');
  $(this).css('display', 'none');
})

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







$('.result_blocks li').mouseover(function(){
  console.log($(this).attr('data-image'));
  var image = $(this).attr('data-image');
  if(!image == ''){
    setPreview(image)
  }
  else{
    $('.img-magnifier-glass').remove();
    $('.page_preview figure #previewImage').attr('src', 'assets/images/no_image.png');
  }
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

$(window).scroll(function(){
  console.log('--scroll--')
  var scroll = $(window).scrollTop();
  if(scroll > 80){
    $('.page_preview').addClass('fix_preview')
  }
  else{
    $('.page_preview').removeClass('fix_preview')
  }
});

// ======= feedback form validation =======
(function() {
  'use strict';
  window.addEventListener('load', function() {
    // Fetch all the forms we want to apply custom Bootstrap validation styles to
    var forms = document.getElementsByClassName('needs-validation');
    // Loop over them and prevent submission
    var validation = Array.prototype.filter.call(forms, function(form) {
      form.addEventListener('submit', function(event) {
        if (form.checkValidity() === false) {
          event.preventDefault();
          event.stopPropagation();
        }
        else{
          event.preventDefault();
          event.stopPropagation();
          $('#feedback').modal('hide');
          $('#thankyou').modal('show')
        }
        form.classList.add('was-validated');
      }, false);
    });
  }, false);
})();


var fixOwl = function(){
  var $stage = $('.owl-stage'),
      stageW = $stage.width(),
      $el = $('.owl-item'),
      elW = 0;
  $el.each(function() {
      elW += $(this).width()+ +($(this).css("margin-right").slice(0, -2))
  });
  if ( elW > stageW ) {
      $stage.width( elW );
  };
}

$('.result_carousal').owlCarousel({
  loop:false,
  // margin:2,   
  responsiveClass:true,
  autoplayHoverPause:true,
  autoplay:false,
   autoWidth: true,
   dots: false,
   nav: true,
   rtl: artl,
   onInitialized: fixOwl,
        onRefreshed: fixOwl,
  //  items: 34
   navText: [
     '<img src="assets/images/arrow-left.svg" alt="Previous">', 
     '<img src="assets/images/arrow-right.svg" alt="Next">'
    ],
  responsive:{
      0:{
        items:2,
      },
      540:{
        items:2
    },
      600:{
          items:3
      },
      768:{
        items:4
      },
      900:{
        items:4
      },
      1024:{
        items:4
      }
  }
});

// ======= clear filter =======
$(document).ready(function(){
  var filter = $('.check_input');
  console.log(filter);
  if(filter.is(':checked')){
    $('.clear_filter').show();
  }
  else{
    $('.clear_filter').hide();
  }
  $('.clear_filter').click(function(){
    $(filter).prop('checked', false);
    $(this).hide()
  });

  $('.check_input').click(function(){
    $('.clear_filter').show();
    if(filter.is(':checked')){
      $('.clear_filter').show();
    }
    else{
      $('.clear_filter').hide();
    }
  })
})




function setPreview(url){
  $('.img-magnifier-glass').remove();
  $('.page_preview figure #previewImage').attr('src', url);
  magnify("previewImage", 3);
}


$(document) .ready(function(){
  var li =  $(".owl-item a ");
    $(".owl-item a").click(function(){
    li.removeClass('active');
  });

  
});

/*execute a function presses a key on the keyboard:*/
  inp.addEventListener("keydown", function(e) {
      var x = document.getElementById(this.id + "autocomplete-list");
      if (x) x = x.getElementsByTagName("div");
      if (e.keyCode == 40) {
        /*If the arrow DOWN key is pressed,
        increase the currentFocus variable:*/
        currentFocus++;
        /*and and make the current item more visible:*/
        addActive(x);
      } else if (e.keyCode == 38) { //up
        /*If the arrow UP key is pressed,
        decrease the currentFocus variable:*/
        currentFocus--;
        /*and and make the current item more visible:*/
        addActive(x);
      } else if (e.keyCode == 13) {
        /*If the ENTER key is pressed, prevent the form from being submitted,*/
        e.preventDefault();
        if (currentFocus > -1) {
          /*and simulate a click on the "active" item:*/
          if (x) x[currentFocus].click();
        }
      }
  });
  function addActive(x) {
    /*a function to classify an item as "active":*/
    if (!x) return false;
    /*start by removing the "active" class on all items:*/
    removeActive(x);
    if (currentFocus >= x.length) currentFocus = 0;
    if (currentFocus < 0) currentFocus = (x.length - 1);
    /*add class "autocomplete-active":*/
    x[currentFocus].classList.add("autocomplete-active");
  }
  function removeActive(x) {
    /*a function to remove the "active" class from all autocomplete items:*/
    for (var i = 0; i < x.length; i++) {
      x[i].classList.remove("autocomplete-active");
    }
  }
  function closeAllLists(elmnt) {
    /*close all autocomplete lists in the document,
    except the one passed as an argument:*/
    var x = document.getElementsByClassName("autocomplete-items");
    for (var i = 0; i < x.length; i++) {
      if (elmnt != x[i] && elmnt != inp) {
        x[i].parentNode.removeChild(x[i]);
      }
    }
  }
  /*execute a function when someone clicks in the document:*/
  document.addEventListener("click", function (e) {
      closeAllLists(e.target);
  });
