$('#lang_toggle').click(function(){
    $('body').toggleClass('ar')
});


// for smooth scrolling

$('a[href*="#"]')
  // Remove links that don't actually link to anything
  .not('[href="#"]')
  .not('[href="#0"]')
  .click(function(event) {
    // On-page links
    if (
      location.pathname.replace(/^\//, '') == this.pathname.replace(/^\//, '') 
      && 
      location.hostname == this.hostname
    ) {
      // Figure out element to scroll to
      var target = $(this.hash);
      target = target.length ? target : $('[name=' + this.hash.slice(1) + ']');
      // Does a scroll target exist?
      if (target.length) {
        // Only prevent default if animation is actually gonna happen
        event.preventDefault();
        $('html, body').animate({
          scrollTop: target.offset().top
        }, 1000, function() {
          // Callback after animation
          // Must change focus!
          var $target = $(target);
          $target.focus();
          if ($target.is(":focus")) { // Checking if the target was focused
            return false;
          } else {            
            $target.focus(); // Set focus again
          };
        });
      }
    }
  });

$('.carousel-slider .owl-carousel').owlCarousel({
    loop:true,
    nav: true,
    dots: false,
    margin:25,
    responsiveClass:true,   
    navText: [
        '<img src="assets/images/back_h.png" alt="back">',
        '<img src="assets/images/next_h.png" alt="next">'
    ],    
    navContainer: '.carousel-slider .custom-nav',
    responsive:{
        0:{
        items:1,
    },
          
    414:{
        items:1,
        margin:10,
    },

    550:{
        items:2,
    },

    769:{
        items:3,
        margin:20
    },    
   
    }
});

$('.carousel-slider2 .owl-carousel').owlCarousel({
    loop:true,
    nav: true,
    dots: false,
    margin:32,
    responsiveClass:true,   
    navText: [
      '<img src="assets/images/back_h.png" alt="back">',
      '<img src="assets/images/next_h.png" alt="next">'
    ],    
    navContainer: '.carousel-slider2 .custom-nav',
    responsive:{
        0:{
        items:1,
    },
          
    550:{
        items:2,
        margin:10
    },

    769:{
        items:3,
        margin:20
    },
    }
});


$('.contact-carousel').owlCarousel({
    loop:true,
    nav: true,
    margin:25,    
    dots: false,
    responsiveClass:true,   
    navText: [
      '<img src="assets/images/back_h.png" alt="back">',
      '<img src="assets/images/next_h.png" alt="next">'
    ],            
    responsive:{
    0:{
        items:1,
    },
        
    480:{
        items:1,
        margin:10,
    },

    575:{
        autoWidth:true,
        items:2,
        margin:15,
    },  
    1030:{
        autoWidth:true,
        items:4
    }
    }
});


// mobile Navbar


const menuToggle =  document.querySelector ('.menu-toggle');
const closeIcon = document.querySelector  ('.close-icon');
const healthcareNavbar = document.querySelector('.healthcare-navbar');
const overlay = document.querySelector('.overlay');

menuToggle.addEventListener('click', function(){
healthcareNavbar.classList.add('show');
overlay.classList.add('show');
});


closeIcon.addEventListener('click', function(){
healthcareNavbar.classList.remove('show');
overlay.classList.remove('show');
});
