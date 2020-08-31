

$('#home_owl').owlCarousel({
    items: 1,
    loop:true,
    dots: true,
    autoplay: true,
});



$('#home_banner_tsb').owlCarousel({
    items: 1,
    loop:true,
    autoplay: true,
    nav:true,
    dots:false,
});


/********************Play and Pause button******************************/
$('.btn-st').on('click', function(event){
    var $this = $(this);
    $(".btn-st").toggleClass("play_pause_btn");
    if($this.hasClass('play_pause_btn')){
        $('.owl-carousel').trigger('play.owl.autoplay',[2000]);
        $(".btn-st").removeClass("play_btn");
        $(".btn-st").addClass("pause_btn");

    } else{
        $(".btn-st").addClass("play_btn");
        $(".btn-st").removeClass("pause_btn");
        console.log("stop")
        $('.owl-carousel').trigger('stop.owl.autoplay');
   }
 });
/********************Play and Pause button******************************/

$('#higness_owl').owlCarousel({
    items: 1,
    loop:false,
    autoplay: true,
    dots:true,
    nav:false,
    
});




$('#medal_owners').owlCarousel({
    items: 3,
    loop:false,
    dots:false,
    nav:true,
    responsive:{
        0:{
            items:1,

        },
        650:{
            items:1,
            // loop:true,
            // autoplay:true

        },
        768:{
            items:2,
        },
        1024:{
            items:3,
        },
        1200:{
            items:3,
        }
    }
});


$('#services_owl').owlCarousel({
    items: 3,
    nav:false,
    dots:true,
    responsive:{
        0:{
            items:1,

        },
        650:{
            items:2,
            // loop:true,
            // autoplay:true

        },
        1100:{
            items:3,
        },
        1200:{
            items:3,
        }
    }
});




$('#places_owl').owlCarousel({
    items: 3,
    // loop:true,
    dots:false,
    nav:true,
    navText:["<img src='../images/arrow-left-gray.svg'","<img src='../images/arrow-right-gray'"],
    responsive:{
        0: {
            items: 1,

        },
        765: {
            items: 2,

        },
        1100: {
            items: 2,
            autoplay: true,
        },
        1200: {
            items: 3,
        }
    }
});



$('#polls_owl').owlCarousel({
    items: 1,
    dots: true,
    nav:true,
});

$('#survey_owl').owlCarousel({
    items: 1,
    dots: true,
    nav:true,
});


$('#review_owl').owlCarousel({
    items: 1,
    dots: true,
     autoplay: true,
});

$('#newsletter_owl').owlCarousel({
    items: 1,
    // loop:true,
    dots: true,
    autoplay: true,
});





$('#did_you_know').owlCarousel({
    items: 1,
    // loop:true,
    dots: true,
    autoplay: true,
});



$('#latest_news_owl').owlCarousel({
    items: 1,
    // loop:true,
    dots: true,
    //  autoplay: true,
    nav:false,
    navText:["<img src='../images/arrow-left-gray.svg'","<img src='../images/arrow-right-gray'"],
});


$('#heighlights_news_owl').owlCarousel({
    items: 1,
    dots: false,
    loop:false,
    dots: false,
    //  autoplay: true,
    nav:true,
    stagePadding: 95,
    navText:["<img src='../images/arrow-left-gray.svg'","<img src='../images/arrow-right-gray'"],
    responsive:{
        0: {
            items: 1,
            stagePadding: 0,
        },
        765: {
            items: 2,

        },
        1030: {
            items: 2,
            autoplay: true,
        },
        1200: {
            items: 3,
            loop:true,
        },
        1220: {
            items: 1,
          
        }
    }
});





$('#event_gallery_owl').owlCarousel({
    items: 4,
    loop: false,
    dots: false,
    //  autoplay: true,
    nav: true,
    navText: ["<img src='../images/arrow-left-gray.svg'", "<img src='../images/arrow-right-gray'"],
    responsive: {
        0: {
            items: 1,

        },
        765: {
            items: 2,

        },
        1200: {
            items: 4,
        }
    }
});


$('#detail_tab_owl').owlCarousel({
    items: 4,
    loop: false,
    dots: false,
    autoplay: false,
    nav: true,
    stagePadding: 30,
    navText: ["<img src='../images/arrow-left-gray.svg'", "<img src='../images/arrow-right-gray'"],
    responsive:{
        0:{
            items:2,

        },
        650:{
            items:3,
            // loop:true,
            // autoplay:true

        },
        768:{
            items:3,
        },
        1024:{
            items:4,
        },
        1200:{
            items:4,
        }
    }
});



$('#other_list_owl').owlCarousel({
    items: 3,
    //loop: true,
    dots: false,
    //  autoplay: true,
    nav: true,
    navText: ["<img src='../images/arrow-left-gray.svg'", "<img src='../images/arrow-right-gray'"],
    responsive: {
        0: {
            items: 1,

        },
        765: {
            items: 2,

        },
        1200: {
            items: 3,
        }
    }
});




$('#ministry_card_owl').owlCarousel({
    items: 5,
    loop: false,
    dots: false,
    //  autoplay: true,
    nav: true,
    navText: ["<img src='../images/arrow-left-gray.svg'", "<img src='../images/arrow-right-gray'"],
    responsive: {
        0: {
            items: 1,

        },
        700: {
            items: 2,

        },
        765: {
            items: 3,

        },
        1200: {
            items: 5,
        }
    }
});





$('#embassies_card_owl').owlCarousel({
    items: 5,
    loop: false,
    dots: false,
    //  autoplay: true,
    nav: true,
    navText: ["<img src='../images/arrow-left-gray.svg'", "<img src='../images/arrow-right-gray'"],
    responsive: {
        0: {
            items: 1,

        },
        700: {
            items: 2,

        },
        765: {
            items: 3,

        },
        1200: {
            items: 5,
        }
    }
});