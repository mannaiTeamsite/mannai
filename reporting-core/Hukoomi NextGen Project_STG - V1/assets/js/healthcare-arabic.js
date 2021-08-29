

// ================================
const $window = $(window);
const $links = $('.navbar-slider .slider-menu a');
const $sections = getSections($links);

function activateLink($sections, $links) {
  const yPosition = $window.scrollTop();

  for (let i = $sections.length - 1; i >= 0; i -= 1) {
    const $section = $sections.eq(i);

    if (yPosition >= $section.offset().top - 200) {
      
      return (
        $links
        .removeClass('active')
        .filter(`[href="#${$section.attr('id')}"]`)
        .addClass('active')
      )
    }
  }
}

function getSections($links) {
  return $(
    $links
      .map((i, el) => $(el).attr('href'))
      .toArray()
      .filter(href => href.charAt(0) === '#')
      .join(','),
  );
}

function onScrollHandler() {
  activateLink($sections, $links);
}

$window.on('scroll', onScrollHandler);
// ================================

function myFunction(x, y) {
  if (x.matches) {
    // If media query matches
    $(window).scroll(function () {
      var scroll = $(window).scrollTop();
      if (scroll >= 125) {
        $(".healthcare_banner").addClass("fixed");
        $('.app_wrapper').addClass('header-space');
      } else {
        $(".healthcare_banner").removeClass("fixed");
        $('.app_wrapper').removeClass('header-space');
      }
    });

    $('.navbar-slider a').click(function() {
      var href = $(this).attr('href');
      $('html,body').animate({
        scrollTop: $(href).offset().top - 180},
        'slow');
    });
  }

  else if (y.matches) {
    // If media query matches
    $(window).scroll(function () {
      var scroll = $(window).scrollTop(); 
      console.log(scroll, 'ok');
      if (scroll >= 132) {
        $(".healthcare_banner").addClass("fixed");
        $('.app_wrapper').addClass('header-space');
      } else {
        $(".healthcare_banner").removeClass("fixed");
        $('.app_wrapper').removeClass('header-space');
      }
    });

    $('.navbar-slider a').click(function() {
      var href = $(this).attr('href');
      $('html,body').animate({
        scrollTop: $(href).offset().top - 180},
        'slow');
    });
  }
  
  
   else {
    $(window).scroll(function () {
      var scroll = $(window).scrollTop();      
      if (scroll >= 180) {
        $(".healthcare_banner").addClass("fixed");
        $('.app_wrapper').addClass('header-space');
      } else {
        $(".healthcare_banner").removeClass("fixed");       
        $('.app_wrapper').removeClass('header-space');
      }
    });

    $('.navbar-slider a').click(function() {
      var href = $(this).attr('href');
      $('html,body').animate({
        scrollTop: $(href).offset().top - 180},
        '500');
    });
  }
}

var x = window.matchMedia("(max-width: 992px) and (min-width:451px)");
var y = window.matchMedia("(max-width: 450px)");
myFunction(x, y); // Call listener function at run time
// x.addListener(myFunction);
// y.addListener(myFunction);





window.MSInputMethodContext &&
  document.documentMode &&
  document.write(
    '<script src="https://cdn.jsdelivr.net/gh/nuxodin/ie11CustomProperties@4.1.0/ie11CustomProperties.min.js"><\x2fscript>'
  );


  $('.navbar-slider').slick({    
    infinite: false,
    speed: 300,
    slidesToShow: 7,
    slidesToScroll: 1,
    variableWidth: true,
    arrows: true,    
    prevArrow:"<button type='button' class='slick-prev'></button>",
    nextArrow:"<button type='button' class='slick-next'></button>",  
    rtl: true,
    responsive: [
    {
      breakpoint: 1024,
      settings: {
        slidesToShow: 3,
      }
    },
    {
      breakpoint: 600,
      settings: {
        slidesToShow: 2,
      }
    },
    {
      breakpoint: 480,
      settings: {        
        slidesToShow: 2,
      }
    }
  ]
  });

// $(".navbar-slider .slider-menu").click(function () {
//   $(".navbar-slider .slider-menu").removeClass("active");
//   $(this).addClass("active");
// });

$(".carousel-slider .owl-carousel").owlCarousel({
  stagePadding: 1,
  loop: true,
  nav: true,
  dots: false,
  margin: 25,
  responsiveClass: true,
  navText: [
    '<img src="/assets/images/back_h.png" alt="back">',
    '<img src="/assets/images/next_h.png" alt="next">',
  ],
  navContainer: ".carousel-slider .custom-nav",
  responsive: {
    0: {
      items: 1,
    },

    414: {
      items: 1,
      margin: 10,
    },

    550: {
      items: 2,
    },

    769: {
      items: 3,
      margin: 20,
    },
  },
});

$(".carousel-slider2 .owl-carousel").owlCarousel({
  stagePadding: 1,
  loop: true,
  nav: true,
  dots: false,
  margin: 32,
  responsiveClass: true,
  navText: [
    '<img src="/assets/images/back_h.png" alt="back">',
    '<img src="/assets/images/next_h.png" alt="next">',
  ],
  navContainer: ".carousel-slider2 .custom-nav",
  responsive: {
    0: {
      items: 1,
    },

    550: {
      items: 2,
      margin: 10,
    },

    769: {
      items: 3,
      margin: 20,
    },
  },
});

$(".contact-carousel").owlCarousel({
  loop: true,
  nav: true,
  margin: 25,
  dots: false,
  responsiveClass: true,
  navText: [
    '<img src="/assets/images/back_h.png" alt="back">',
    '<img src="/assets/images/next_h.png" alt="next">',
  ],
  responsive: {
    0: {
      items: 1,
    },

    480: {
      items: 1,
      margin: 10,
    },

    575: {
      autoWidth: true,
      items: 2,
      margin: 15,
    },
    1030: {
      autoWidth: true,
      items: 4,
    },
  },
});


// Filter show value if input is checked

$(document).ready(function () {
  $("input:checked").each(function () {
    var parentTadId = $(this).parents(".filter-card").attr("id");
    var id = $(this).attr("id");
    createTag(id, parentTadId);
  });
});



// This function works for show checked value

function createTag(id, parentTadId, tagText) {
  var tagName = $("#" + id).siblings(".text").html();    
    if(tagText){
        tagName = tagText;        
    }
  var tagElement ='<span class="badge" id="tag' +id +'">' +
    tagName +' <span class="remove" data-id=' +id +'><svg xmlns="http://www.w3.org/2000/svg" width="24" height="24"viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="feather feather-x"> <line x1="18" y1="6" x2="6" y2="18" stroke="#1c1517"></line> <line x1="6" y1="6" x2="18" y2="18" stroke="#1c1517"></line> </svg></span></span>';
    $("#" + parentTadId).parents(".filter-section").find(".show-hide-item").append(tagElement);
  }


$(".checkbox").click(function () {
  if ($(this).is(":checked")) {
    createTag($(this).attr("id"), $(this).parents(".filter-card").attr("id"));
  } else {
    removeTag($(this).attr("id"));
  }
});

// month
$(document).on('change', '#month', function(){
    removeTag('month', $(this).parents(".filter-card").attr("id"),$(this).find("option:selected").text());
    createTag('month', $(this).parents(".filter-card").attr("id"),$(this).find("option:selected").text());
});

// year
$(document).on('change', '#year', function(){
    removeTag('year', $(this).parents(".filter-card").attr("id"),$(this).find("option:selected").text());
    createTag('year', $(this).parents(".filter-card").attr("id"),$(this).find("option:selected").text());
});

// This functon is use for remove checked value

function removeTag(id) {
  $("#tag" + id).remove();
  $("#" + id).prop("checked", false);
}

$(document).on("click", ".remove, .clear-all", function () {
  removeTag($(this).attr("data-id")); 
  $('#month, #year').val('').selectpicker('refresh');  
});

$(document).on("click", ".clear-all", function () {
  $(this).parents(".filter-section").find(".badge").remove();
  $(this).parents(".filter-section").find(".checkbox").attr("checked", false);
  $('#month, #year').val('').selectpicker('refresh');
});

// tooltip

$(".topic-navbar a").mouseover(function () {
  $(this).find('[data-toggle="tooltip"]').tooltip("show");
});

$(".topic-navbar a").mouseout(function () {
  $(this).find('[data-toggle="tooltip"]').tooltip("hide");
});

// dropdown-menu active class

$(document).on("click", ".dropdown-menu .dropdown-item", function () {
  $(".dropdown-menu .dropdown-item").removeClass("active");
  $(this).addClass("active");
});


var maxLength = 145;
	$(".show-read-more").each(function(){
		var myStr = $(this).text();
		if($.trim(myStr).length > maxLength){
			var newStr = myStr.substring(0, maxLength);
			var removedStr = myStr.substring(maxLength, $.trim(myStr).length);
			$(this).empty().html(newStr);
			var knowMore = $("#section6").attr("data-more");
			if(knowMore == ""){
				knowMore = "Know-More..."
			}
			$(this).append(' <span class="know-more">'+knowMore+'</span>');
			$(this).append('<span class="more-text">' + removedStr + '</span>');
		}
	});

  
  $(document).on('click', '.know-more', function(){
    $(this).parents('.card').find('.overlay-content').addClass('show');
  });

  $(document).on('click', '.close-btn', function(){
    $(this).parents('.card').find('.overlay-content').removeClass('show');
  });

// // health topic sort

// $(".healthTopics .dropdown-item").click(function() {
//    $("#selectedValue").text($(this).text());
// });

// // service sort

// $(".services .dropdown-item").click(function() {
//    $("#selectService").text($(this).text());
// });

// // related document sort

// $(".documentName .dropdown-item").click(function() {
//    $("#documenttitle").text($(this).text());
// });
