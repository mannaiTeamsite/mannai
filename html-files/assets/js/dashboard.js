// ======= validation =======
// Example starter JavaScript for disabling form submissions if there are invalid fields
(function () {
    'use strict';
    window.addEventListener('load', function () {
        // Fetch all the forms we want to apply custom Bootstrap validation styles to
        var forms = document.getElementsByClassName('needs-validation');
        // Loop over them and prevent submission
        var validation = Array.prototype.filter.call(forms, function (form) {
            form.addEventListener('submit', function (event) {
                if (!form.checkValidity() === false) {
                    $('.hku_alert, .alert_backdrop').css("display", "block");
                    event.preventDefault();
                    event.stopPropagation();
                    setTimeout(function () {
                        $('.hku_alert, .alert_backdrop').css("display", "none")
                    }, 5000)
                }
                else {
                    event.preventDefault();
                    event.stopPropagation();
                }
                form.classList.add('was-validated');
            }, false);
        });
    }, false);
})();

// ======= show more =======
function show_more_less(e) {
    console.log(e)
    var current = e;
    if (!$(current).hasClass('show_less')) {
        $(current).find('span').text('Show Less');
        $(current).addClass('show_less')
        $(current).parents('.bills_card_wrapper').find('.hku_card').addClass('show_all')
    }
    else {
        $(current).find('span').text('Show All');
        $(current).removeClass('show_less')
        $(current).parents('.bills_card_wrapper').find('.hku_card').removeClass('show_all')
    }
}

// ======= to show alert when remove item from favourite =======
// $('.subscribe_btn').click(function(){
//         console.log("favourite")
//         $('.hku_alert, .alert_backdrop').css("display", "block")
//     });

$('.undo_button, .close_hku_alert').click(function () {
    console.log("favourite")
    $('.hku_alert, .alert_backdrop').css("display", "none")
});
// ========= to show menu on small screens =======
$('.dash_menu').click(function(){
    $('body').addClass('dash_menu_show');
    $('.dash_nav_backdrop').css('display', 'block')
})

$('.dash_nav_backdrop').click(function(){
    $('body').removeClass('dash_menu_show');
    $('.dash_nav_backdrop').css('display', 'none')
})


//======= bill checkbox check uncheck =======
function checkAll(ele) {
    var checkboxes = document.getElementsByTagName('input');
    if ($(checkboxes).hasClass('bill_check')) {
        if (ele.checked) {
            for (var i = 0; i < checkboxes.length; i++) {
                if (checkboxes[i].type == 'checkbox') {
                    checkboxes[i].checked = true;
                }
            }
        } else {
            for (var i = 0; i < checkboxes.length; i++) {
                console.log(i)
                if (checkboxes[i].type == 'checkbox') {
                    checkboxes[i].checked = false;
                }
            }
        }
    }

}

$('.bill_check').on('change', function () {
    if (!$(this).is(':checked')) {
        $('#servicesck').prop('checked', false)
    }
});

//======= profile switch redirection =======
$('#business').click(function () {
    window.location.href = 'business-profile.html'
});
$('#personal').click(function () {
    window.location.href = 'dashboard-data.html'
});




//======= change polls slide on button click =======
$('#polls').on('slid.bs.carousel', function () {
        currentIndex = $('#polls .carousel-item.active').index();
        $('#polls_indicator ol li').eq(currentIndex).addClass('active').siblings().removeClass('active');
       if(currentIndex == $('#polls .carousel-item').length){
        $('#polls_indicator ol li').eq(0).addClass('active').siblings().removeClass('active');
       } 
  });

//======= change polls slide on dot indicator click =======
  $('#polls_indicator ol li').on('click', function () {
    currentIndex = $(this).index();
    $("#polls").carousel(currentIndex);
});

//======= change survey slide on button click =======
  $('#survey').on('slid.bs.carousel', function () {
    currentIndex = $('#survey .carousel-item.active').index();
    console.log(currentIndex);
    $('#survey_indicator ol li').eq(currentIndex).addClass('active').siblings().removeClass('active');
    if(currentIndex == $('#survey .carousel-item').length){
        $('#survey_indicator ol li').eq(0).addClass('active').siblings().removeClass('active');
       }
});

//======= change survey slide on dot indicator click =======
$('#survey_indicator ol li').on('click', function () {
    currentIndex = $(this).index();
    $("#survey").carousel(currentIndex);
});
  $('.carousel').carousel();

// ========================================
  $('.carousel').carousel({
    interval: false,
  })
  
  $(document).ready(function () {               // on document ready
    checkSurvey();
    checkPolls();
    $('#survey').on('slid.bs.carousel', function () {
        checkSurvey();
      });
      $('#polls').on('slid.bs.carousel', function () {
        checkPolls();
        $('#vote').css('visibility', 'visible')
      });

      $('.view_explore').click(function(){
        $('#expore_services_container').addClass('view_exp_services');
        $('.mob_nav').removeClass('active')
    });
    
    $('.close_exp_services').click(function(){
        $('#expore_services_container').removeClass('view_exp_services')
    });
  });
  
 
  
  function checkSurvey()                        // check function
  {
    var $this = $('#survey');
    if ($this.parents('.hku_card').find('.carousel-item:first').hasClass('active')) {
        $this.parents('.hku_card').find('.carousel-control-prev').css({'opacity': '0.5', 'pointer-events': 'none'});
        $this.parents('.hku_card').find('.carousel-control-next').css({'opacity': '1', 'pointer-events': 'auto'});
    } else if ($this.parents('.hku_card').find('.carousel-item:last').hasClass('active')) {
        $this.parents('.hku_card').find('.carousel-control-next').css({'opacity': '0.5', 'pointer-events': 'none'});
        $this.parents('.hku_card').find('.carousel-control-prev').css({'opacity': '1', 'pointer-events': 'auto'});
    } else {
        $this.parents('.hku_card').find('.carousel-control-prev, .carousel-control-next').css({'opacity': '1', 'pointer-events': 'auto'});
    }
  }

  function checkPolls()                        // check function
  {
    var $this = $('#polls');
    if ($this.parents('.hku_card').find('.carousel-item:first').hasClass('active')) {
        $this.parents('.hku_card').find('.carousel-control-prev').css({'opacity': '0.5', 'pointer-events': 'none'});
        $this.parents('.hku_card').find('.carousel-control-next').css({'opacity': '1', 'pointer-events': 'auto'});
    } else if ($this.parents('.hku_card').find('.carousel-item:last').hasClass('active')) {
        $this.parents('.hku_card').find('.carousel-control-next').css({'opacity': '0.5', 'pointer-events': 'none'});
        $this.parents('.hku_card').find('.carousel-control-prev').css({'opacity': '1', 'pointer-events': 'auto'});
    } else {
        $this.parents('.hku_card').find('.carousel-control-prev, .carousel-control-next').css({'opacity': '1', 'pointer-events': 'auto'});
    }
  }


  $(document).on('click', '#vote', function(){
      console.log('vote')
      $('#polls').find('.carousel-item').each(function(){
          if($(this).hasClass('active')){
              if($(this).find('.options input').is(':checked')){
                console.log($(this))
                $(this).find('li .voted_percent').show();
                $(this).find('.total_votes').show();
                $('#vote').css('visibility', 'hidden')
              }
              else{
                  alert('Please select any one option')
              }
            
          }
          
      })
    //   $('#polls .carousel-item.active .voted_percent').show()
  })
  
