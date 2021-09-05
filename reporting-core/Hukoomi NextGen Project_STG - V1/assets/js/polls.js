var artl = $('body').hasClass("ar") ? true : false;

$('#pollsFormModal').on('shown.bs.modal', function (event) {
    
   
    $('#pollsFormModal .owl-item').attr('tabindex', '-1');
    // $('#pollsFormModal .item').attr('tabindex', '-1');
    $('#pollsFormModal input').attr('tabindex', '-1');
    $('#pollsFormModal .item button').attr('tabindex', '-1');
    pollsCarousal();
});

function pollsCarousal(){
    var owl = $('#pollsQuestions');
    owl.owlCarousel({
        loop:false,
            margin:10,
            nav:true,
            rtl: artl,
            lazyLoad: true,
            dots: false,
            onInitialized: callback,
            responsive:{
                0:{
                    items:1
                },
                600:{
                    items:1
                },
                1000:{
                    items:1
                }
            }
    });
    // Listen to owl events:
    var items;
    var item;
    var currentSlide;
    function callback(event) {
        items     = event.item.count;     // Number of items
        item      = event.item.index; 
        console.log(items, item);
        currentSlide = $('#pollsQuestions .owl-item').eq(item)[0];
            console.log(currentSlide);
            $(currentSlide).attr('tabindex', '0');
            // $(currentSlide).find('.item').attr('tabindex', '0');
            $(currentSlide).find('input').attr('tabindex', '0');
            $(currentSlide).find('button').attr('tabindex', '0');
    }
    owl.on('changed.owl.carousel', function(event) {
        items     = event.item.count;     // Number of items
        item      = event.item.index; 
        console.log(items, item)
        currentSlide = $('#pollsQuestions .owl-item').eq(item)[0];
            console.log(currentSlide);
            $(currentSlide).find('owl-item').attr('tabindex', '0');
            // $(currentSlide).find('.item').attr('tabindex', '0');
            $(currentSlide).find('input').attr('tabindex', '0');
            $(currentSlide).find('button').attr('tabindex', '0');
    });

    $(document.documentElement).keyup(function(event) {    
        if (event.keyCode == 37) { /*left key*/
            owl.trigger('prev.owl.carousel', [700]);
        } else if (event.keyCode == 39) { /*right key*/
            owl.trigger('next.owl.carousel', [700]);
        }
    });
}



$('#pollsFormModal').on('hidden.bs.modal', function (event) {
    // $('#pollsQuestions').owlCarousel('destroy'); 
    $('#pollsQuestions').trigger('to.owl.carousel', [0, 500, true]);
    $('#pollsQuestions input[type="radio"]').prop("checked", false);
    $('.s_ques_wrapper').find('button').prop('disabled', true);
    $('.s_ques_wrapper').removeClass('vote_submitted')
});

$(document).on('click', '.s_submit', function () {
    $('#surveySubmitSuccess').modal('show')
});

$(document).on('click', '#pollsQuestions input[type="radio"]', function(){
    if($(this).is(':checked')){
        $(this).parents('.s_ques_wrapper').find('button').prop('disabled', false).addClass("vote_submit")
    }
});

$(document).on('click', '.vote_submit', function(){
    $(this).parents('.s_ques_wrapper').addClass('vote_submitted')
})


$(document).on('click', '.pollsQuestions input[type="radio"]', function(){
    if($(this).is(':checked')){
        $(this).parents('.s_ques_wrapper').find('button').prop('disabled', false).addClass("vote_submit")
    }
});

$('a[data-toggle="tab"]').on('shown.bs.tab', function (e) {
    var target = e.target.attributes.href.value;
    console.log(target)
    $(target +' .list_card:first-child .organisers ul li.active a').focus();
});

$(document).on('click', '#sort_popup input[type="radio"]', function(){
    $('#sort_popup').modal('hide')
});