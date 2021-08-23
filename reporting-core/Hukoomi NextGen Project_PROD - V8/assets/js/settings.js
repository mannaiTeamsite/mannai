
window.MSInputMethodContext &&
    document.documentMode &&
    document.write(
        '<script src="https://cdn.jsdelivr.net/gh/nuxodin/ie11CustomProperties@4.1.0/ie11CustomProperties.min.js"><\x2fscript>'
    );

var artl =$('body').hasClass("ar") ? true : false;
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

  if (window.matchMedia("(max-width: 768px)").matches) {
    $('#settings_carousal').owlCarousel({
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
  }
  
  $('#settings_carousal').owlCarousel({
    loop:false,
    // margin:2,   
    responsiveClass:true,
    autoplayHoverPause:true,
    autoplay:false,
     autoWidth: true,
     dots: false,
     nav: false,
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

  $(document) .ready(function(){
    var li =  $(".owl-item a ");
      $(".owl-item a").click(function(){
      li.removeClass('active');
    });
  });

  $('a[data-toggle="tab"]').on('shown.bs.tab', function (e) {
    var target = e.target.attributes.href.value;
    console.log(target);
    $(target +' .get_focus').focus();
  });

  // topics selection and generate tags

  $(document).on('change', '#topics', function(){
    var topic_tags = [];
    var selected_option =  $(this).val();
    console.log(selected_option);
    // $('#se_topic_tags li').remove();
    if(selected_option != null && selected_option.length > 0){
      // console.log(selected_option);
      for(i = 0; i<selected_option.length; i++){
        // console.log(selected_option[i]);
        topic_tags.push("<li tabindex='0'> <p>"+selected_option[i]+"</p> <span>X</span></li>")
      }
      $('.se_topic_tags ul').html(topic_tags.join(''));
    }
    if(selected_option == null){
      $('.se_topic_tags ul').empty();
    }

    
  });

  $(document).on('click', '.se_topic_tags li span', function(){
    var span_val = $(this).parent().find('p').text();
    console.log(span_val)
    $('#topics option[value="'+span_val+'"]').prop('selected', false);
    $(this).parent().remove();
    $('#topics').selectpicker('refresh');
  })